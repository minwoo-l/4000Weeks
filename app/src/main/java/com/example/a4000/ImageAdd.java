package com.example.a4000;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ImageAdd extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 2;

    // New UI Components
    private Button buttonSelectImage;
    private MaterialButton buttonSave;
    private ImageView imageViewPreview;
    private TextInputEditText editTextComment;
    private ProgressBar progressBar;

    // Data and Firebase variables
    private Uri imageUri;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private String week, ym, year, month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_add);

        // Initialize new views from the layout
        buttonSelectImage = findViewById(R.id.button_select_image);
        buttonSave = findViewById(R.id.button_save);
        imageViewPreview = findViewById(R.id.image_view_preview);
        editTextComment = findViewById(R.id.edit_text_comment);
        progressBar = findViewById(R.id.progress_bar);

        // Get data passed from the previous screen
        week = getIntent().getStringExtra("WEEK");
        ym = getIntent().getStringExtra("YM");
        String[] date = ym.split("\\.");
        year = date[0];
        month = date[1];

        // Initialize Firebase Auth and check if user is logged in
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no user is logged in
            return;
        }

        String dynamicTitle = ym + " - " + week;
        getSupportActionBar().setTitle(dynamicTitle);

        // Load any existing image or comment for this week
        loadExistingData();

        // Set up Click Listeners for the buttons
        buttonSelectImage.setOnClickListener(v -> openFileChooser());

        buttonSave.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            saveData();
        });
    }

    private void openFileChooser() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    private void loadExistingData() {
        String uid = user.getUid();
        DatabaseReference weekRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("Image").child(year).child(month).child(week);

        // Listener to load the data once
        weekRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Load existing comment
                    if (snapshot.hasChild("comment")) {
                        editTextComment.setText(snapshot.child("comment").getValue(String.class));
                    }

                    // Load existing image
                    // We loop because the image model is under a unique push key
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        if (childSnapshot.hasChild("imageUrl")) {
                            Model model = childSnapshot.getValue(Model.class);
                            if (model != null && model.getImageUrl() != null) {
                                Glide.with(ImageAdd.this)
                                        .load(model.getImageUrl())
                                        .into(imageViewPreview);
                                break; // Found the image, no need to loop further
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ImageAdd.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveData() {
        String comment = editTextComment.getText().toString();

        // If a new image was selected, upload it first.
        // Otherwise, just save the comment.
        if (imageUri != null) {
            uploadImageAndSaveData(imageUri, comment);
        } else {
            saveCommentOnly(comment);
        }
    }

    private void saveCommentOnly(String comment) {
        String uid = user.getUid();
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("Image").child(year).child(month).child(week).child("comment");

        commentRef.setValue(comment).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(ImageAdd.this, "Comment Saved!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(ImageAdd.this, "Failed to save comment.", Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadImageAndSaveData(Uri uri, String comment) {
        String uid = user.getUid();
        // The path in Firebase Storage where the image will be saved
        final StorageReference fileRef = storageReference.child(uid).child(year).child(month).child(week + "." + getFileExtension(uri));

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    // When storage upload is successful, get the download URL
                    fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // Save both the comment and the image URL to the database
                        DatabaseReference weekRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("Image").child(year).child(month).child(week);

                        // Save comment
                        weekRef.child("comment").setValue(comment);

                        // Save image URL model (clears old image models if they exist)
                        Model model = new Model(downloadUri.toString());
                        String modelId = weekRef.push().getKey();
                        if (modelId != null) {
                            weekRef.child(modelId).setValue(model);
                        }

                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ImageAdd.this, "Saved successfully!", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ImageAdd.this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            // Set the selected image in the preview
            imageViewPreview.setImageURI(imageUri);
        }
    }

    private String getFileExtension(Uri mUri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }
}