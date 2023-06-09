package com.groupb.locationsharing;

import static com.groupb.locationsharing.Fragments.MapsFrag.mainLocation;
import static com.groupb.locationsharing.Fragments.MapsFrag.saveLocationForReload;
import static com.groupb.locationsharing.Fragments.MapsFrag.saveNameForReload;
import static com.groupb.locationsharing.Fragments.MapsFrag.saveUsernameForReload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.groupb.locationsharing.Fragments.MapsFrag;
import com.groupb.locationsharing.Fragments.NewsFeedFragment;
import com.groupb.locationsharing.Fragments.NotificationFragment;
import com.groupb.locationsharing.Fragments.PostDetailFragment;
import com.groupb.locationsharing.Fragments.ProfileFragment;
import com.groupb.locationsharing.Fragments.SearchFragment;

import java.io.File;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    FirebaseUser firebaseUser;
    DatabaseReference reference;

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "Delete temp files", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        //delete temp files
        File file = new File(getFilesDir(), "profile.jpg");
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                //Do nothing
            }
        }
        if (saveNameForReload != null) {
            for (int i = 0; i < saveNameForReload.size(); i++) {
                File file1 = new File(getFilesDir(), saveNameForReload.get(i));
                if (file1.exists()) {
                    boolean deleted = file1.delete();
                    if (!deleted) {
                        //Do nothing
                    }
                }
            }
            saveLocationForReload.clear();
            saveNameForReload.clear();
            saveUsernameForReload.clear();
            mainLocation.clear();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        Bundle intent = getIntent().getExtras();
        if (intent != null) {

            String publisher = getIntent().getStringExtra("publisherId");

            String postId = getIntent().getStringExtra("postId");

            if (publisher != null) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileId", publisher);
                editor.apply();

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                        , new ProfileFragment()).commit();
            } else if (postId != null) {
                SharedPreferences.Editor editor = getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postId", postId);
                editor.apply();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new PostDetailFragment()).commit();
            }
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                    , new NewsFeedFragment()).commit();
        }

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            selectedFragment = new NewsFeedFragment();
                            break;
                        case R.id.nav_search:
                            selectedFragment = new SearchFragment();
                            break;
                        case R.id.nav_add:
//                            selectedFragment = null;
//                            startActivity(new Intent(getApplicationContext(), AddPostActivity.class));
                            selectedFragment = new MapsFrag();
                            break;
                        case R.id.nav_heart:
                            selectedFragment = new NotificationFragment();
                            //selectedFragment = new ChatsFragment();
                            break;
                        case R.id.nav_profile:
                            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                            editor.putString("profileId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            editor.apply();
                            selectedFragment = new ProfileFragment();
                            break;
                    }
                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                                , selectedFragment).commit();
                    }
                    return true;
                }
            };

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();

        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();

        status("offline");
    }

}