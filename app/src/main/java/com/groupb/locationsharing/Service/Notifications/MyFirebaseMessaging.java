package com.groupb.locationsharing.Service.Notifications;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.groupb.locationsharing.MessageActivity;
import com.groupb.locationsharing.Model.Post;
import com.groupb.locationsharing.Model.User;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            updateToken(s);
        }
    }

    private void updateToken(String refreshToken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(refreshToken);
        reference.child(firebaseUser.getUid()).setValue(token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        //Log.e(TAG, message.toString());
        String sented = message.getData().get("sented");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //Log.w(TAG, firebaseUser.getUid());

        if (firebaseUser != null && sented.equals(firebaseUser.getUid())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendOreoNotification(message);
            } else {
                sendNotifications(message);
            }
        }
    }

    private void sendOreoNotification(RemoteMessage message) {
        String user = message.getData().get("user");
        String icon = message.getData().get("icon");
        String body = message.getData().get("body");
        String title = message.getData().get("title");

        RemoteMessage.Notification notification = message.getNotification();

        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        Log.e(TAG, user + "bbbbbbbbbbbbbbb");
        bundle.putString("userid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_MUTABLE);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent, defaultSound, icon);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        oreoNotification.getNotificationManager().notify(i, builder.build());
    }

    private void sendNotifications(RemoteMessage message) {
        String user = message.getData().get("user");
        String icon = message.getData().get("icon");
        String body = message.getData().get("body");
        String title = message.getData().get("title");

        RemoteMessage.Notification notification = message.getNotification();

        int j = Integer.parseInt(user.replaceAll("[\\D]", ""));

        Intent intent = new Intent(this, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userid", user);
        Log.e(TAG, user + "aaaaaaaaaaaaaaaaaaaa");
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int i = 0;
        if (j > 0) {
            i = j;
        }

        noti.notify(i, builder.build());
    }
    private boolean checkIsPost(String id){
        DatabaseReference userReference = FirebaseDatabase.getInstance()
                .getReference("Users").child(id);
        final User[] user = {new User()};
        final boolean[] result = {true};
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) result[0] = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return result[0];
    }
}
