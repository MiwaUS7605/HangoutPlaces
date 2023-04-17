package com.groupb.locationsharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.groupb.locationsharing.Adapter.CommentAdapter;
import com.groupb.locationsharing.Fragments.APIService;
import com.groupb.locationsharing.Model.Comment;
import com.groupb.locationsharing.Model.User;
import com.groupb.locationsharing.Service.Notifications.Client;
import com.groupb.locationsharing.Service.Notifications.Data;
import com.groupb.locationsharing.Service.Notifications.MyResponse;
import com.groupb.locationsharing.Service.Notifications.Sender;
import com.groupb.locationsharing.Service.Notifications.Token;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentActivity extends AppCompatActivity {
    EditText addComments;
    ImageView profile_image;
    TextView post;
    String postId, publisherId;
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private ImageView back;
    FirebaseUser firebaseUser;
    APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        addComments = findViewById(R.id.add_comments);
        profile_image = findViewById(R.id.profile_image);
        post = findViewById(R.id.post_comment);
        back = findViewById(R.id.icon);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        publisherId = intent.getStringExtra("publisherId");

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        recyclerView.setAdapter(commentAdapter);
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

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!addComments.getText().toString().equals("")) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                            .getReference("Comments").child(postId);

                    HashMap<String, Object> map = new HashMap<>();
                    map.put("comments", addComments.getText().toString());
                    map.put("publisher", firebaseUser.getUid());
                    databaseReference.push().setValue(map);
                    notify = true;
                    addNotifications();
                    addComments.setText("");

                    final String msg = "Some one commented your post";

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if (notify) {
                                if (!user.getId().equals(firebaseUser.getUid())) {
                                    sendNotifications(publisherId, user.getUsername(), msg);
                                }
                            }
                            notify = false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
        getImage();
        getComments();
    }

    private void updateToken(String token) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        databaseReference.child(firebaseUser.getUid()).setValue(token1);
    }

    private void getImage() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user.getImageUrl().equals("default")) {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getComments() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Comments").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
                }

                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotifications() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Notifications").child(publisherId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", firebaseUser.getUid());
        map.put("text", "commented: " + addComments.getText().toString());
        map.put("postId", postId);
        map.put("isPost", "yes");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        LocalDateTime now = LocalDateTime.now();
        map.put("time", dtf.format(now));

        reference.push().setValue(map);
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
                            , username + " commented your post "
                            , "Someone interact with your post," + postId
                            , receiver);
                    Sender sender = new Sender(data, token.getToken());

                    //Toast.makeText(mContext, token.getToken(), Toast.LENGTH_SHORT).show();

                    apiService.sendNotifications(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
                                    //Toast.makeText(mContext, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {
                            //Toast.makeText(getApplicationContext(), "Failed on send Notifications!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
//    private String getUsername(String id){
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
//                .getReference("Users").child(id);
//        final String[] res = {""};
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                User user = snapshot.getValue(User.class);
//                res[0] = user.getUsername();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        return res[0];
//    }
}