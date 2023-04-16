package com.groupb.locationsharing.Fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.groupb.locationsharing.Adapter.SearchUserAdapter;
import com.groupb.locationsharing.Adapter.ViewUserOnMapAdapter;
import com.groupb.locationsharing.Model.User;
import com.groupb.locationsharing.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsFrag extends Fragment implements OnMapReadyCallback {
    GoogleMap mMap;
    SupportMapFragment mapFragment;
    BroadcastReceiver receiver;
    FirebaseUser firebaseUser;
    String profileId;
    Bitmap profileAvatar;

    BitmapDrawable bd;
    StorageReference storageReference;
    HashMap<String, Object> dataUpdate;
    String city;

    List<User> mUsers;
    ViewUserOnMapAdapter viewUserOnMapAdapter;
    RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_map, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewUserOnMapAdapter = new ViewUserOnMapAdapter(getContext(), mUsers);
        recyclerView.setAdapter(viewUserOnMapAdapter);

        mUsers = new ArrayList<>();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        getUserInfor();
        dataUpdate = new HashMap<>();
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        bd=(BitmapDrawable)getResources().getDrawable(R.drawable.test);
        profileAvatar = bd.getBitmap();
        return view;
    }
    private void getUserInfor() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        profileId = prefs.getString("profileId", "none");
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users").child(profileId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) {
                    return;
                }
                User user = snapshot.getValue(User.class);
                String imgSTR = user.getImageUrl();

                if (user.getImageUrl().equals("default")) {
                    //Toast.makeText(getContext(), "No image", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(getContext(), "Get image from server: "+imgSTR, Toast.LENGTH_SHORT).show();
                    try {
                        downloadImage(imgSTR);
                    } catch (IOException e) {
                        //Toast.makeText(getContext(), "Error when download image: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        throw new RuntimeException(e);
                    }
                    getImageFromStorage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void downloadImage(String imageUrl) throws IOException {
// Create a new URL object from the image URL string
        URL url = new URL(imageUrl);

// Open a connection to the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

// Set the request method to GET
        connection.setRequestMethod("GET");

// Get the input stream from the connection
        InputStream inputStream = connection.getInputStream();

// Create a Bitmap object from the input stream
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

// Close the input stream and connection
        inputStream.close();
        connection.disconnect();

// Save the image to the device
        String filename = "profile.jpg";
        FileOutputStream outputStream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        outputStream.close();

    }
    public void getImageFromStorage(){
        // Get the file path for the saved image
        String filename = "profile.jpg";
        File file = new File(getActivity().getFilesDir(), filename);

// Create a new Bitmap object from the saved image file
        profileAvatar = BitmapFactory.decodeFile(file.getAbsolutePath());
    }
    public String getCity(Double latitude, Double longitude) throws IOException {
        Geocoder geoCoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> matches = geoCoder.getFromLocation(latitude, longitude, 1);
        Address bestMatch = (matches.isEmpty() ? null : matches.get(0));
        //Toast.makeText(getContext(), bestMatch.getAdminArea(), Toast.LENGTH_SHORT).show();
        return bestMatch.getAdminArea();
    }
    public void getPeopleSameCity(String city){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    User user = snapshot1.getValue(User.class);
                    Toast.makeText(getContext(), user.getCity(), Toast.LENGTH_SHORT).show();
                    if (user.getCity().equals(city)) {
                        mUsers.add(user);
                    }
                }
                //Load people
                viewUserOnMapAdapter = new ViewUserOnMapAdapter(getContext(), mUsers);
                recyclerView.setAdapter(viewUserOnMapAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 44);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().isCompassEnabled();
        mMap.getUiSettings().isMapToolbarEnabled();

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
//                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.test);
//                Bitmap b=bitmapdraw.getBitmap();
                getImageFromStorage();
                Bitmap smallMarker = Bitmap.createScaledBitmap(profileAvatar, 154, 154, false);
                // Handle the new location
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                try {
                    city = getCity(latitude, longitude);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                getPeopleSameCity(city);
                //Toast.makeText(getContext(), String.valueOf(mUsers.isEmpty()), Toast.LENGTH_SHORT).show();
                String lng= String.valueOf(longitude);
                String lat= String.valueOf(latitude);
                //Update location on firebase
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                storageReference = FirebaseStorage.getInstance().getReference("uploads");
                dataUpdate.put("lat", lat);
                dataUpdate.put("lon", lng);
                dataUpdate.put("city", city);
                reference.updateChildren(dataUpdate);
                LatLng latLng = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(latLng).title("Marker").icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        }, null);

    }
}
