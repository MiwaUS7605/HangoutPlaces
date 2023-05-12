package com.groupb.locationsharing.Adapter;

import static android.service.controls.ControlsProviderService.TAG;
import static com.groupb.locationsharing.Adapter.CommentAdapter.isValidContextForGlide;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.groupb.locationsharing.MessageActivity;
import com.groupb.locationsharing.Model.Chat;
import com.groupb.locationsharing.Model.User;
import com.groupb.locationsharing.R;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context mContext;
    private List<Chat> mChat;
    private String image_url;
    FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String image_url) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.image_url = image_url;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mChat.get(position);

        //Toast.makeText(mContext, position + ": " + chat.isImage(), Toast.LENGTH_SHORT).show();

        if (!chat.isIsImage()) {
            holder.show_message.setText(chat.getMessage());
            holder.show_image.setVisibility(View.GONE);
        } else if (chat.isIsImage()) {
            if (isValidContextForGlide(mContext)) {
                Glide.with(mContext).load(chat.getMessage()).into(holder.show_image);
            }
            holder.show_message.setVisibility(View.GONE);
        }

        if (isValidContextForGlide(mContext)) {
            Glide.with(mContext).load(image_url).into(holder.profile_image);
        }

        if (position == mChat.size() - 1) {
            if (chat.isIsseen()) {
                holder.txt_seen.setText("Seen");
            } else {
                holder.txt_seen.setText("Delivered");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message, txt_seen;
        public ImageView profile_image, show_image;

        public ViewHolder(View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            show_image = itemView.findViewById(R.id.show_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mChat.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
