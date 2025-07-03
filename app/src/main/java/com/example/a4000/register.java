package com.example.a4000;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class register extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText nameEditText;
    private Button signButton;
    private Button backButton;
    private DatePicker birthday;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String dob;

    private static final String USERS = "users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase and UI components
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference(USERS);

        usernameEditText = findViewById(R.id.register_usernameEditText);
        passwordEditText = findViewById(R.id.register_passwordEditText);
        nameEditText = findViewById(R.id.register_nameEditText);
        progressBar = findViewById(R.id.progressBar);
        birthday = findViewById(R.id.register_dob);
        backButton = findViewById(R.id.register_loginButton);
        signButton = findViewById(R.id.register_signButton);

        // --- Improved Date Picker Logic ---
        // Set the maximum date to today
        birthday.setMaxDate(Calendar.getInstance().getTimeInMillis());

        // Initialize the 'dob' variable with the default date of the picker
        int year = birthday.getYear();
        int month = birthday.getMonth();
        int day = birthday.getDayOfMonth();
        dob = (month + 1) + "/" + day + "/" + year;

        // Set up a listener to update 'dob' when the user changes the date
        birthday.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dob = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year;
            }
        });

        // --- Button Click Listeners ---
        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), login.class);
            startActivity(intent);
            finish();
        });

        signButton.setOnClickListener(view -> {
            String username = String.valueOf(usernameEditText.getText());
            String password = String.valueOf(passwordEditText.getText());
            String fullName = String.valueOf(nameEditText.getText());

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(fullName)) {
                Toast.makeText(register.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            registerUser(username, password, fullName);
        });
    }

    private void registerUser(String email, String password, String fullName) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser != null) {
                        // Update the user's display name in Firebase Auth
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName).build();
                        firebaseUser.updateProfile(profileUpdate);

                        // Save the user's info to the Realtime Database using their UID
                        userInfo userInfo = new userInfo(fullName, dob, email);
                        databaseReference.child(firebaseUser.getUid()).setValue(userInfo);

                        Toast.makeText(register.this, "Account created successfully.", Toast.LENGTH_SHORT).show();
                        // Redirect to login so the user can sign in with their new account
                        Intent intent = new Intent(getApplicationContext(), login.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    // If sign up fails, display a message to the user.
                    if (password.length() < 6) {
                        Toast.makeText(register.this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(register.this, "Authentication failed. An account with this email may already exist.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}