package com.groupb.locationsharing.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.groupb.locationsharing.Fragments.PostDetailFragment;
import com.groupb.locationsharing.Model.Post;
import com.groupb.locationsharing.R;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private Context mContext;
    private List<Post> mPosts;

    public PhotoAdapter(Context mContext, List<Post> mPosts) {
        this.mContext = mContext;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.photos_item, parent, false);
        return new PhotoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = mPosts.get(position);

        Glide.with(mContext).load(post.getPostImage()).into(holder.post_images);

        holder.post_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postId", post.getPostId());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PostDetailFragment())
                        .addToBackStack("ProfileFragment")
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView post_images;
        public ViewHolder(@NonNull View item){
            super(item);
            post_images = item.findViewById(R.id.post_image);
        }
    }
}
