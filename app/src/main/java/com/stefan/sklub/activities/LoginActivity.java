package com.stefan.sklub.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stefan.sklub.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LoginActivity extends AppCompatActivity {

    String TAG = "ispis";
    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Views
        mEmailField = findViewById(R.id.fieldEmail);
        mPasswordField = findViewById(R.id.fieldPassword);

//        // Buttons
//        findViewById(R.id.btnLogin).setOnClickListener(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        getSupportActionBar().hide();

        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.i(TAG, "korisnik je vec ulogovan :)");
            cw(currentUser.getEmail());
            Intent continueToMainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(continueToMainActivityIntent);
        } else {
            Log.i(TAG, "uloguj se :)");
        }
    }

    private void signIn(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "popuni polja pls", Toast.LENGTH_SHORT).show();
            return;
        }

        cw("email", email);
        cw("password", password);

        mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "signInWithEmail:successs");
                    FirebaseUser user = mAuth.getCurrentUser();
                    Intent continueToMainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(continueToMainActivityIntent);
                    finish();
                } else {
                    Log.i(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onBtnLoginClick(View v) {
        int i = v.getId();
        if (i == R.id.btnLogin) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }

    public void onBtnRegisterClick(View view) {
        Intent registerActivityIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerActivityIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy(): m.Auth.signOut()");
        mAuth.signOut();
    }

    private void cw(String msg) {
        Log.i(TAG, msg);
    }

    private void cw(String src, String msg) {
        Log.i(TAG, src + ": " + msg);
    }
}
