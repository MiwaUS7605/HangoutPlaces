package com.groupb.locationsharing;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference("User");
        writeNewUser("HCMUS0003", "Banh Hao Toan", "20127699@gmail.com");
    }

    public void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        ref.child("users").child(userId).setValue(user);
    }
}