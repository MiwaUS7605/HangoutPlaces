package com.groupb.locationsharing;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    EditText username, email, password;
    Button btn_register;
    TextView returnLogin, error;
    FirebaseAuth auth;
    DatabaseReference reference;
    ImageView passwordDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_register = findViewById(R.id.btn_register);
        returnLogin = findViewById(R.id.returnLogin);
        error = findViewById(R.id.error);

        returnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        auth = FirebaseAuth.getInstance();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username_Txt = String.valueOf(username.getText());
                String email_Txt = String.valueOf(email.getText());
                String password_Txt = String.valueOf(password.getText());

                if (TextUtils.isEmpty(username_Txt) || TextUtils.isEmpty(password_Txt)
                        || TextUtils.isEmpty(email_Txt)) {
                    error.setText("All field can't be empty");
                } else if (!checkPassword(password_Txt)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("Password Validation");
                    builder.setMessage("Must have at least one numeric character\n" +
                            "Must have at least one lowercase character\n" +
                            "Must have at least one uppercase character\n" +
                            "Must have at least one special symbol among @#$%\n" +
                            "Password length should be between 8 and 20");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else if (!checkUsername(username_Txt)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("Password Validation");
                    builder.setMessage("The username length must range between 7 to 20 characters otherwise, it will consider as an invalid username.\n" +
                            "The username is allowed to contain only underscores ( _ ) other than alphanumeric characters.\n" +
                            "The first character of the username must be an alphabetic character, i.e., [a-z] or [A-Z].\n");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    auth.fetchSignInMethodsForEmail(email_Txt).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            if (task.getResult().getSignInMethods().size() == 0) {

                                register(username_Txt, password_Txt, email_Txt);

                            } else {
                                error.setText("Your email had already created");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    private void register(String usernamee, String passwordd, String emaill) {
        auth.createUserWithEmailAndPassword(emaill, passwordd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // send verification link
                            FirebaseUser firebaseUser = auth.getCurrentUser();

                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "Check your mail to verify", Toast.LENGTH_SHORT).show();
                                                email.setText("");
                                                username.setText("");
                                                password.setText("");
                                                if (firebaseUser.isEmailVerified()) {

                                                }

                                                assert firebaseUser != null;
                                                String userid = firebaseUser.getUid();

                                                reference = FirebaseDatabase.getInstance()
                                                        .getReference("Users").child(userid);
                                                HashMap<String, String> hashMap = new HashMap<>();
                                                hashMap.put("id", userid);
                                                hashMap.put("username", usernamee);
                                                hashMap.put("imageUrl", "https://e7.pngegg.com/pngimages/178/595/png-clipart-user-profile-computer-icons-login-user-avatars-monochrome-black.png");
                                                hashMap.put("status", "offline");
                                                hashMap.put("name", usernamee.toLowerCase());
                                                hashMap.put("bio", "");
                                                hashMap.put("findable", "1");
                                                hashMap.put("lon", "0");
                                                hashMap.put("lat", "0");
                                                hashMap.put("city", "unknown");
                                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG, "on Failure: Email not sent" + e.getMessage());
                                        }
                                    });

                        } else {
                            error.setText("Your email had already created");
                        }
                    }
                });
    }

    public boolean checkUsername(String username) {
        String regularExpression = "^[a-zA-Z][a-zA-Z0-9_]{6,19}$";
        return isValidPassword(username, regularExpression);
    }

    public boolean checkPassword(String password) {
        String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,20}$";
        return isValidPassword(password, regex);
    }

    public static boolean isValidPassword(String password, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}