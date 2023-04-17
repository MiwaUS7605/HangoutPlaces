package com.groupb.locationsharing.Adapter;

import static com.google.firebase.messaging.Constants.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupb.locationsharing.Fragments.PostDetailFragment;
import com.groupb.locationsharing.Fragments.ProfileFragment;
import com.groupb.locationsharing.Model.Notification;
import com.groupb.locationsharing.Model.Post;
import com.groupb.locationsharing.Model.User;
import com.groupb.locationsharing.R;

import java.util.List;
import java.util.zip.Inflater;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private Context mContext;
    private List<Notification> mNotifications;

    public NotificationAdapter(Context mContext, List<Notification> mNotifications) {
        this.mContext = mContext;
        this.mNotifications = mNotifications;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = mNotifications.get(position);

        holder.text.setText(notification.getText());
        holder.date.setText(notification.getTime());

        getUserInfo(holder.profile_image, holder.username, notification.getUserId());

        if (notification.getIsPost().equals("true")) {
            holder.post_image.setVisibility(View.VISIBLE);
            getPostImage(holder.post_image, notification.getPostId());
        } else {
            holder.post_image.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, notification.getIsPost() + "");
                if (notification.getIsPost().equals("yes")) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("postId", notification.getPostId());
                    editor.apply();
                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                            , new PostDetailFragment()).commit();
                } else {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileId", notification.getUserId());
                    editor.apply();
                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                            , new ProfileFragment()).commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profile_image, post_image;
        public TextView username, text, date;

        public ViewHolder(@NonNull View item) {
            super(item);
            profile_image = item.findViewById(R.id.profile_image);
            post_image = item.findViewById(R.id.post_image);
            username = item.findViewById(R.id.username);
            text = item.findViewById(R.id.comment);
            date = item.findViewById(R.id.date);
        }
    }

    private void getUserInfo(final ImageView imageView, final TextView username, String publisherId) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users").child(publisherId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user.getImageUrl().equals("default")) {
                    imageView.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(mContext).load(user.getImageUrl()).into(imageView);
                }
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPostImage(final ImageView imageView, final String postId) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Posts").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                Glide.with(mContext).load(post.getPostImage()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
