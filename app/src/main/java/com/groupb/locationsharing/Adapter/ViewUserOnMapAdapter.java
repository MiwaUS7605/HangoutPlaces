package com.groupb.locationsharing.Adapter;

import static com.groupb.locationsharing.Fragments.MapsFrag.mainLocation;

import android.app.MediaRouteButton;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = R * c;
        return distance;
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
        Double distanceCal= distance(Double.parseDouble(user.getLat()), Double.parseDouble(user.getLon()), mainLocation.get(0), mainLocation.get(1));
        Double distanceToShow=Double.parseDouble(String.format("%.2f", distanceCal));
        holder.followBtn.setVisibility(View.VISIBLE);

        holder.username.setText(user.getUsername());

        holder.fullname.setText(user.getFullname() +" - " + distanceToShow + " km");

        if (user.getImageUrl().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageUrl()).into(holder.profile_image);
        }

        if(user.getId().equals(firebaseUser.getUid())){
            holder.followBtn.setVisibility(View.GONE);
            holder.username.setText(user.getUsername()+" (You)");
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
                //holder.followBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.my_color)));
                Intent intent = new Intent("com.example.ACTION_UPDATE_CAMERA_CENTER");
                intent.putExtra("latitude", user.getLat());
                intent.putExtra("longitude", user.getLon());
                intent.putExtra("username", user.getUsername());
                intent.putExtra("urlImageSent", user.getImageUrl());
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