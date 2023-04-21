package com.groupb.locationsharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupb.locationsharing.Adapter.SearchUserAdapter;
import com.groupb.locationsharing.Model.User;

import java.util.ArrayList;
import java.util.List;

public class FollowersActivity extends AppCompatActivity {

    String id;
    String title;

    List<String> listId;

    RecyclerView recyclerView;
    SearchUserAdapter userAdapter;
    List<User> userList;
    ImageView finish;
    TextView titleToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        finish = findViewById(R.id.finish);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new SearchUserAdapter(getApplicationContext(), userList);
        recyclerView.setAdapter(userAdapter);
        listId = new ArrayList<>();

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        titleToolbar = findViewById(R.id.title);
        titleToolbar.setText(title);

        switch (title) {
            case "likes":
                Thread getLikesThread = new Thread(() -> getLikes());
                getLikesThread.start();
                break;
            case "following":
                Thread getFollowingThread = new Thread(() -> getFollowing());
                getFollowingThread.start();
                break;
            case "followers":
                Thread getFollowersThread = new Thread(() -> getFollowers());
                getFollowersThread.start();
                break;
            case "views":
                Thread getViewsThread = new Thread(() -> getViews());
                getViewsThread.start();
                break;
        }
    }

    private void getViews() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(id).child(getIntent().getStringExtra("storyId")).child("views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    listId.add(snapshot.getKey());
                }
                Thread showUsersThread = new Thread(() -> showUsers());
                showUsersThread.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLikes() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Likes").child(id);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    listId.add(snapshot.getKey());
                }
                Thread showUsersThread = new Thread(() -> showUsers());
                showUsersThread.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowers() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Follow").child(id).child("followers");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    listId.add(snapshot.getKey());
                }
                Thread showUsersThread = new Thread(() -> showUsers());
                showUsersThread.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowing() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Follow").child(id).child("following");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listId.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    listId.add(snapshot.getKey());
                }
                Thread showUsersThread = new Thread(() -> showUsers());
                showUsersThread.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (String id : listId) {
                        if (user.getId().equals(id)) {
                            userList.add(user);
                        }
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}