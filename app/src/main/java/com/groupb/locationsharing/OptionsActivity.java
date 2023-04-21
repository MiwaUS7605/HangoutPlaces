package com.groupb.locationsharing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.groupb.locationsharing.Model.User;

import java.util.HashMap;

public class OptionsActivity extends AppCompatActivity {
    TextView logout, change_password;
    Switch findableSwitch;
    FirebaseUser firebaseUser;
    ImageView back;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        logout = findViewById(R.id.logout);
        back = findViewById(R.id.icon);
        change_password = findViewById(R.id.change_password);
        findableSwitch = findViewById(R.id.findableBtn);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        Thread checkFindableThread = new Thread(() -> checkFindable());
        checkFindableThread.start();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(OptionsActivity.this, StartActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });
        findableSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                        .getReference("Users").child(firebaseUser.getUid());
                if (findableSwitch.isChecked()) {
                    databaseReference.child("findable").setValue("1");
                } else {
                    databaseReference.child("findable").setValue("0");
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OptionsActivity.this);
                builder.setTitle("Change Password");
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
                builder.setView(view);

                EditText oldPasswordEditText = view.findViewById(R.id.oldPasswordEditText);
                EditText newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
                EditText confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);

                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String oldPassword = oldPasswordEditText.getText().toString().trim();
                        String newPassword = newPasswordEditText.getText().toString().trim();
                        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                        if (TextUtils.isEmpty(oldPassword)) {
                            oldPasswordEditText.setError("Enter old password");
                            return;
                        }
                        if (TextUtils.isEmpty(newPassword)) {
                            newPasswordEditText.setError("Enter new password");
                            return;
                        }
                        if (TextUtils.isEmpty(confirmPassword)) {
                            confirmPasswordEditText.setError("Confirm new password");
                            return;
                        }
                        if (!newPassword.equals(confirmPassword)) {
                            confirmPasswordEditText.setError("Passwords do not match");
                            return;
                        }

                        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
                        currentUser.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Change password
                                            currentUser.updatePassword(newPassword)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                // Show success message
                                                                Toast.makeText(OptionsActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                // Show error message
                                                                Toast.makeText(OptionsActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            // Show error message for incorrect old password
                                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                                Toast.makeText(OptionsActivity.this, "Incorrect old password", Toast.LENGTH_SHORT).show();
                                            } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                                // Show error message for invalid user
                                                Toast.makeText(OptionsActivity.this, "User is no longer valid", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Show generic error message
                                                Toast.makeText(OptionsActivity.this, "Failed to reauthenticate", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
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