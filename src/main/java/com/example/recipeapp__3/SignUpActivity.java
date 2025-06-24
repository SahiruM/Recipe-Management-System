package com.example.recipeapp__3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button goBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        emailInput = findViewById(R.id.inputEmail);
        passwordInput = findViewById(R.id.inputPassword);
        signUpButton = findViewById(R.id.signUpButton);
        goBackButton = findViewById(R.id.goBackButton);

        goBackButton.setOnClickListener(v -> {
            // Navigate back to Sign-In Activity
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            finish(); // Finish the current activity so the user can't return here using the back button
        });

        // Set up the sign-up button click listener
        signUpButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                signUpUser(email, password); // Call the sign-up method
            }
        });
    }

    // Method to handle user sign-up and saving credentials to Firestore
    private void signUpUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User registered successfully, now save credentials to Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserCredentialsToFirestore(user.getUid(), email, password);
                        }
                    } else {
                        // Registration failed, show an error message
                        Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Method to save user credentials (email and password) to Firestore
    private void saveUserCredentialsToFirestore(String userId, String email, String password) {
        UserCredentials userCredentials = new UserCredentials(email, password);

        db.collection("users").document(userId).set(userCredentials)
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved user credentials, now redirect to SignInActivity
                    Toast.makeText(SignUpActivity.this, "Sign Up Successful! Please sign in.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                    finish(); // Close SignUpActivity so the user cannot go back
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignUpActivity.this, "Failed to save credentials: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Create a UserCredentials class to hold the user's email and password
    public static class UserCredentials {
        private String email;
        private String password;

        public UserCredentials() {
            // Default constructor required for Firestore
        }

        public UserCredentials(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }
}
