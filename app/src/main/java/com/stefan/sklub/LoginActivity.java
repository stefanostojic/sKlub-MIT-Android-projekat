package com.stefan.sklub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

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

        // Buttons
        findViewById(R.id.btnLogin).setOnClickListener(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        getSupportActionBar().hide();

        // Check if user is signed in (non-null) and update UI accordingly.
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
        if (email == null || email == "" || password == null || password == "") {
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
                    // Sign in success, update UI with the signed-in user's information
                    Log.i(TAG, "signInWithEmail:successs");
                    FirebaseUser user = mAuth.getCurrentUser();
                    Intent continueToMainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(continueToMainActivityIntent);
                    finish();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.i(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
//                    updateUI(null);
                }

                // ...
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnLogin) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
//        else if (i == R.id.emailSignInButton) {
//            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
//        }
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
