package com.groupb.locationsharing.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupb.locationsharing.Adapter.FollowingAdapter;
import com.groupb.locationsharing.Model.User;
import com.groupb.locationsharing.R;

import java.util.ArrayList;
import java.util.List;

public class FollowingFragment extends Fragment {
    private RecyclerView recyclerView;
    private FollowingAdapter followingAdapter;
    private List<User> userList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        userList = new ArrayList<>();
        followingAdapter = new FollowingAdapter(getContext(), userList);
        recyclerView.setAdapter(followingAdapter);

        readFollowing();
        return view;
    }

    private void readFollowing() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Follow").child(firebaseUser.getUid()).child("following");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    String userId = snapshot.getKey();
                    DatabaseReference databaseReference1 = FirebaseDatabase.getInstance()
                            .getReference("Users").child(userId);
                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            userList.add(user);
                            followingAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}