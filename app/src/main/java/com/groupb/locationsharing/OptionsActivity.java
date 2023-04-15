package com.groupb.locationsharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupb.locationsharing.Model.User;

import java.util.HashMap;

public class OptionsActivity extends AppCompatActivity {
    TextView logout;
    Switch findableSwitch;
    FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        logout = findViewById(R.id.logout);
        findableSwitch = findViewById(R.id.findableBtn);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        checkFindable();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(OptionsActivity.this, StartActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

            }
        });
        findableSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                        .getReference("Users").child(firebaseUser.getUid());
                if (findableSwitch.isChecked()){
                    databaseReference.child("findable").setValue("1");
                } else {
                    databaseReference.child("findable").setValue("0");
                }
            }
        });
    }

    private void checkFindable() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null && user.getFindable().equals("1")) {
                    findableSwitch.setChecked(true);
                } else {
                    findableSwitch.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}