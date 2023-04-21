package com.groupb.locationsharing.Adapter;

import static com.groupb.locationsharing.Adapter.CommentAdapter.isValidContextForGlide;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.groupb.locationsharing.Fragments.APIService;
import com.groupb.locationsharing.Fragments.ProfileFragment;
import com.groupb.locationsharing.Model.User;
import com.groupb.locationsharing.R;
import com.groupb.locationsharing.Service.Notifications.Client;
import com.groupb.locationsharing.Service.Notifications.Data;
import com.groupb.locationsharing.Service.Notifications.MyResponse;
import com.groupb.locationsharing.Service.Notifications.Sender;
import com.groupb.locationsharing.Service.Notifications.Token;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;

    private FirebaseUser firebaseUser;
    APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
    boolean notify = false;
    public SearchUserAdapter(Context mContext, List<User> mUser) {
        this.mContext = mContext;
        this.mUsers = mUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_search_item, parent, false);
        return new SearchUserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final User user = mUsers.get(position);

        holder.followBtn.setVisibility(View.VISIBLE);

        holder.username.setText(user.getUsername());

        holder.fullname.setText(user.getFullname());

        isFollowing(user.getId(), holder.followBtn);

        if (isValidContextForGlide(mContext)) {
            Glide.with(mContext).load(user.getImageUrl()).into(holder.profile_image);
        }

        if(user.getId().equals(firebaseUser.getUid())){
            holder.followBtn.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileId", user.getId());
                editor.apply();
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            }
        });

        holder.followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.followBtn.getText().toString().equals("Follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("following")
                            .child(user.getId()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(user.getId()).child("followers")
                            .child(firebaseUser.getUid()).setValue(true);
                    addNotifications(user.getId());
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.getException());
                                        return;
                                    }

                                    // Get new FCM registration token
                                    String token = task.getResult();
                                    //Toast.makeText(getContext(), token, Toast.LENGTH_SHORT).show();
                                    // Log and/or update token as needed
                                    updateToken(token);
                                }
                            });
                    notify = true;
                    final String msg = "Some one following you";
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if (notify) {
                                sendNotifications(mUsers.get(position).getId(), user.getUsername(), msg);
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else{
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("following")
                            .child(user.getId()).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(user.getId()).child("followers")
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });
    }

    private void sendNotifications(String receiver, final String username, final String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid()
                            , R.mipmap.ic_launcher
                            , username + " following you"
                            , "Someone is following you"
                            , receiver);
                    Sender sender = new Sender(data, token.getToken());

                    //Toast.makeText(mContext, token.getToken(), Toast.LENGTH_SHORT).show();

                    apiService.sendNotifications(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
                                    Toast.makeText(mContext, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {
                            Toast.makeText(mContext, "Failed on send Notifications!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateToken(String token) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        databaseReference.child(firebaseUser.getUid()).setValue(token1);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username, fullname;
        public Button followBtn;
        public ImageView profile_image;
        public ViewHolder(@NonNull View item){
            super(item);
            username = item.findViewById(R.id.username);
            fullname = item.findViewById(R.id.fullname);
            followBtn = item.findViewById(R.id.followBtn);
            profile_image = item.findViewById(R.id.profile_image);
        }
    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    private void isFollowing(final String uid, final Button button){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(uid).exists()){
                    button.setText("Following");
                }
                else{
                    button.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void addNotifications(String userId){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Notifications").child(userId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", firebaseUser.getUid());
        map.put("text", "started following you");
        map.put("postId", "");
        map.put("isPost", "no");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        LocalDateTime now = LocalDateTime.now();
        map.put("time", dtf.format(now));

        reference.push().setValue(map);
    }
}
