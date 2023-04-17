package com.groupb.locationsharing;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;


public class AddPostActivity extends AppCompatActivity {

    Uri imageUri;
    String myUrl = "";
    StorageTask uploadTask;
    StorageReference storageReference;
    ImageView close, imageView;
    TextView post;
    EditText description;
    private static final int IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        close = findViewById(R.id.close);
        imageView = findViewById(R.id.added_image);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        storageReference = FirebaseStorage.getInstance().getReference("posts");

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(intent, CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE);

    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Posting");
        pd.show();

        if (imageUri != null) {
            StorageReference reference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));
            uploadTask = reference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

                        String postId = databaseReference.push().getKey();

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("postId", postId);
                        map.put("postImage", mUri);
                        map.put("postDescription", description.getText().toString());
                        map.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
                        LocalDateTime now = LocalDateTime.now();
                        map.put("time", dtf.format(now));

                        databaseReference.child(postId).setValue(map);

                        pd.dismiss();

                        //Glide.with(getApplicationContext()).load(mUri).into(imageView);

                        startActivity(new Intent(AddPostActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    //pd.dismiss();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            imageUri = result.getUri();
            imageView.setImageURI(imageUri);
        }
        else if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            imageUri = data.getData(); // Get the selected image's Uri
            Log.e(TAG, imageUri.toString());
            // Start the CropImage activity with the selected image's Uri
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(AddPostActivity.this);

            //imageView.setImageURI(imageUri);
        }
        else {
            Toast.makeText(getApplicationContext(), "Something gone wrong", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddPostActivity.this, MainActivity.class));
            finish();
        }
    }

}