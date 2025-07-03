package com.example.a4000;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userDbRef;

    private TextView profileName, profileEmail, profileDob, profileImageCount;
    private Button logoutButton, changePasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        getSupportActionBar().setTitle("Profile");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        profileName = findViewById(R.id.profile_name_textview);
        profileEmail = findViewById(R.id.profile_email_textview);
        profileDob = findViewById(R.id.profile_dob_textview);
        profileImageCount = findViewById(R.id.profile_image_count_textview);
        logoutButton = findViewById(R.id.profile_logout_button);
        changePasswordButton = findViewById(R.id.profile_change_password_button);

        if (currentUser == null) {
            goToLogin();
            return;
        }

        // Set up the reference to this specific user's data in the database
        userDbRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        setupUserInfo();
        setupBottomNav();

        logoutButton.setOnClickListener(v -> logoutUser());

        changePasswordButton.setOnClickListener(v -> sendPasswordReset());
    }

    private void sendPasswordReset() {
        if (currentUser != null && currentUser.getEmail() != null) {
            mAuth.sendPasswordResetEmail(currentUser.getEmail())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProfileActivity.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProfileActivity.this, "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setupUserInfo() {
        // Set basic info we already have
        profileName.setText(currentUser.getDisplayName());
        profileEmail.setText(currentUser.getEmail());

        // Fetch additional info from the Realtime Database
        userDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch and display Date of Birth
                    if (snapshot.hasChild("dob")) {
                        profileDob.setText(snapshot.child("dob").getValue(String.class));
                    } else {
                        profileDob.setText("Not set");
                    }

                    // Fetch and count images
                    if (snapshot.hasChild("Image")) {
                        long totalImageCount = 0;
                        DataSnapshot imageNode = snapshot.child("Image");
                        for (DataSnapshot yearSnapshot : imageNode.getChildren()) {
                            for (DataSnapshot monthSnapshot : yearSnapshot.getChildren()) {
                                for (DataSnapshot weekSnapshot : monthSnapshot.getChildren()) {
                                    // Loop through the final items under the week (the pushID and the comment)
                                    for (DataSnapshot itemSnapshot : weekSnapshot.getChildren()) {
                                        // We only count the item if its key is NOT "comment"
                                        if (!"comment".equals(itemSnapshot.getKey())) {
                                            totalImageCount++;
                                        }
                                    }
                                }
                            }
                        }
                        profileImageCount.setText(String.valueOf(totalImageCount));
                    } else {
                        profileImageCount.setText("0");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load user details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.navigation_profile); // Highlight the "Profile" icon

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                startActivity(new Intent(getApplicationContext(), user.class));
                overridePendingTransition(0, 0);
                finish(); // Finish this activity so back button works correctly
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Already here, do nothing
                return true;
            }
            return false;
        });
    }

    private void logoutUser() {
        mAuth.signOut();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(getApplicationContext(), login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}