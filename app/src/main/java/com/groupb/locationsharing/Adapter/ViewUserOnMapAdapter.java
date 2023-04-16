package com.groupb.locationsharing.Adapter;

import android.app.MediaRouteButton;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupb.locationsharing.Fragments.MapsFrag;
import com.groupb.locationsharing.Fragments.ProfileFragment;
import com.groupb.locationsharing.Model.User;
import com.groupb.locationsharing.R;

import java.util.HashMap;
import java.util.List;

public class ViewUserOnMapAdapter extends RecyclerView.Adapter<ViewUserOnMapAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;

    private LocalBroadcastManager localBroadcastManager;

    private FirebaseUser firebaseUser;

    public ViewUserOnMapAdapter(Context mContext, List<User> mUser) {
        this.mContext = mContext;
        this.mUsers = mUser;
        localBroadcastManager = MapsFrag.getLocalBroadcastManager(mContext);
    }

    @NonNull
    @Override
    public ViewUserOnMapAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_map_item, parent, false);
        return new ViewUserOnMapAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewUserOnMapAdapter.ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final User user = mUsers.get(position);

        holder.followBtn.setVisibility(View.VISIBLE);

        holder.username.setText(user.getUsername());

        holder.fullname.setText(user.getFullname());

        if (user.getImageUrl().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
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
                Intent intent = new Intent("com.example.ACTION_UPDATE_CAMERA_CENTER");
                intent.putExtra("latitude", user.getLat());
                intent.putExtra("longitude", user.getLon());
                intent.putExtra("username", user.getUsername());
                //Toast.makeText(mContext, user.getLat()+" "+user.getLon(), Toast.LENGTH_SHORT).show();
                localBroadcastManager.sendBroadcast(intent);

            }
        });
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
        if(mUsers == null)
            return 0;
        return mUsers.size();
    }
}