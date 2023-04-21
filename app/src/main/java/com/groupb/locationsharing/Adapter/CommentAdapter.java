package com.groupb.locationsharing.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupb.locationsharing.CommentActivity;
import com.groupb.locationsharing.MainActivity;
import com.groupb.locationsharing.Model.Comment;
import com.groupb.locationsharing.Model.User;
import com.groupb.locationsharing.R;
import com.groupb.locationsharing.TranslateActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> mComments;
    private String postId;

    FirebaseUser firebaseUser;

    public CommentAdapter(Context mContext, List<Comment> mComments, String postId) {
        this.mContext = mContext;
        this.mComments = mComments;
        this.postId = postId;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profile_image;
        public TextView username, comment, translate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            translate = itemView.findViewById(R.id.translate);
            profile_image = itemView.findViewById(R.id.profile_image);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
        }
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        final boolean[] isTrans = {false};
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Comment comment = mComments.get(position);

        holder.comment.setText(comment.getComments());

        Thread getUserInfoThread = new Thread(() -> getUserInfo(holder.profile_image, holder.username, comment.getPublisher()));
        getUserInfoThread.start();

        holder.profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
                mContext.startActivity(intent);
            }
        });
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("publisherId", comment.getPublisher());
                mContext.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (comment.getPublisher().equals(firebaseUser.getUid())) {
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle("Delete this comment?");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseDatabase.getInstance().getReference("Comments")
                                    .child(postId).child(comment.getCommentId())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(mContext, "Deleted Successful", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            dialogInterface.dismiss();
                        }
                    });
                    alertDialog.show();
                }
                return true;
            }
        });
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        holder.translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isTrans[0]) {
                    holder.translate.setText("Restore");
                    String input = holder.comment.getText().toString();
                    try {
                        if (isVietnamese(input)) {
                            holder.comment.setText(translate("vi", "en", input));
                        } else {
                            holder.comment.setText(translate("en", "vi", input));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    isTrans[0] = true;
                } else {
                    holder.translate.setText("Translate this section");
                    holder.comment.setText(comment.getComments());
                    isTrans[0] = false;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    private void getUserInfo(final ImageView imageView, final TextView username, String publisherId) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users").child(publisherId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    username.setText(user.getUsername());
                    if (isValidContextForGlide(mContext)) {
                        // Load image via Glide lib using context
                        Glide.with(mContext).load(user.getImageUrl()).into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static boolean isVietnamese(String input) {
        // Danh sách các ký tự tiếng Việt trong bảng mã Unicode
        final String vietnameseCharacters = "ĂẮẰẤẾẶẲẨÊẾỀỂỆƠÓỐỒỐỚỢỞỜỤỨỪỰỬÍỐỚỜỢỞÚỨỪỰỬÝĐ";
        String upperCase = input.toUpperCase(Locale.ROOT);
        for (char c : upperCase.toCharArray()) {
            // Kiểm tra xem ký tự c có nằm trong danh sách ký tự tiếng Việt không
            if (vietnameseCharacters.contains(Character.toString(c))) {
                return true;
            }
        }
        return false;
    }

    public static String translate(String langFrom, String langTo, String text) throws IOException {
        // INSERT YOU URL HERE
        String urlStr = "https://script.google.com/macros/s/AKfycbxM5RTr1vtx3e5HvzsWjPIhR9M46ok16FG0V6mjmajg2oPSvZ0duHq2mEXuB1fXnPiIoQ/exec" +
                "?q=" + URLEncoder.encode(text, "UTF-8") +
                "&target=" + langTo +
                "&source=" + langFrom;
        String responseString = "";
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000 /* milliseconds */);
            connection.setConnectTimeout(15000 /* milliseconds */);

            // Connect to the API and get the response
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Response code: " + responseCode);
            }
            inputStream = connection.getInputStream();

            // Parse the JSON response and extract the weather information
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            responseString = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the input stream and disconnect the connection
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return responseString;
    }

    public static boolean isValidContextForGlide(final Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            final Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                return false;
            }
        }
        return true;
    }
}
