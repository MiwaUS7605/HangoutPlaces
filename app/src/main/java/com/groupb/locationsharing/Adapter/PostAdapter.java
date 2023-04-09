package com.groupb.locationsharing.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupb.locationsharing.CommentActivity;
import com.groupb.locationsharing.Model.Post;
import com.groupb.locationsharing.Model.User;
import com.groupb.locationsharing.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter <PostAdapter.ViewHolder> {
    public Context mContext;
    public List<Post> mPost;
    FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);

        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPost.get(position);

        Glide.with(mContext).load(post.getPostImage()).into(holder.post_image);

        if(post.getPostDescription().equals("")){
            holder.description.setVisibility(View.GONE);
        }else{
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getPostDescription());
        }

        publisherInfor(holder.profile_image, holder.username, holder.publisher, post.getPublisher());
        isLiked(post.getPostId(), holder.likeSymbol);
        totalLikes(holder.likes, post.getPostId());
        totalComments(holder.comments, post.getPostId());

        holder.likeSymbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.likeSymbol.getTag().equals("like")){
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                            .child(firebaseUser.getUid()).setValue(true);
                }
                else{
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.commentSymbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("publisherId", post.getPublisher());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView profile_image, post_image, likeSymbol, commentSymbol;
        public TextView username, likes, comments, publisher, description, viewComments;
        public ViewHolder(@NonNull View itemView){
            super(itemView);

            profile_image = itemView.findViewById(R.id.profile_image);
            post_image = itemView.findViewById(R.id.post_image);
            likeSymbol = itemView.findViewById(R.id.like);
            commentSymbol = itemView.findViewById(R.id.comment);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.totalLikes);
            comments = itemView.findViewById(R.id.totalComments);
            publisher = itemView.findViewById(R.id.publisher);
            description = itemView.findViewById(R.id.description);
        }
    }

    private void isLiked(String postId, ImageView view){
        FirebaseUser firebaseUser1 = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(firebaseUser1.getUid()).exists()){
                    view.setImageResource(R.drawable.ic_liked);
                    view.setTag("liked");
                }
                else{
                    view.setImageResource(R.drawable.ic_like);
                    view.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void totalLikes(TextView likes, String postId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes").child(postId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                likes.setText(dataSnapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void totalComments(TextView comments, String postId){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Comments").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comments.setText(snapshot.getChildrenCount() + " comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void publisherInfor(final ImageView imageProfile, final TextView username, final TextView publisher, final String userid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    if(user.getImageUrl().equals("default")){
                        imageProfile.setImageResource(R.mipmap.ic_launcher);
                    }else{
                        Glide.with(mContext).load(user.getImageUrl()).into(imageProfile);
                    }
                    username.setText(user.getUsername());
                    publisher.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
