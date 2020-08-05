package com.stefan.sklub.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import java.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.DayOfWeek;
import com.stefan.sklub.R;
import com.stefan.sklub.User;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final String TAG = "RegisterActivityDebug";
    private EditText et_email;
    private EditText et_firstname;
    private EditText et_lastname;
    private EditText et_birthday;
    private Calendar birthday;
    private String gender;
    private EditText et_password;
    private EditText et_repeatpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        birthday = Calendar.getInstance();

        et_email = (EditText) findViewById(R.id.et_email);
        et_firstname = (EditText) findViewById(R.id.et_firstname);
        et_lastname = (EditText) findViewById(R.id.et_lastname);
        et_birthday = (EditText) findViewById(R.id.et_birthday);
        et_password = (EditText) findViewById(R.id.et_password);
        et_repeatpassword = (EditText) findViewById(R.id.et_repeatpassword);

    }

    public void onBtnRegisterClick(View view) {

        if (TextUtils.isEmpty(et_email.getText())) {
            Toast.makeText(this, "Email field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(et_email.getText()).matches()) {
            Toast.makeText(this, "Email in wrong format", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(et_firstname.getText())) {
            Toast.makeText(this, "Firstname field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(et_lastname.getText())) {
            Toast.makeText(this, "Lastname field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(et_birthday.getText())) {
            Toast.makeText(this, "Birthday field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (gender == null) {
            Toast.makeText(this, "Gender field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(et_password.getText())) {
            Toast.makeText(this, "Password field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (et_password.getText().toString().length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(et_repeatpassword.getText())) {
            Toast.makeText(this, "You have to repeat your password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!et_password.getText().toString().equals(et_repeatpassword.getText().toString())) {
            Toast.makeText(this, "Passwords don't match :/", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(et_email.getText().toString(), et_password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser authUser = mAuth.getCurrentUser();

                            User newUser = new User(authUser.getUid(),
                                    et_firstname.getText().toString(),
                                    et_lastname.getText().toString(),
                                    null,
                                    gender,
                                    null);


                            db.collection("users")
                                    .add(newUser)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error adding document", e);
                                        }
                                    });
                            updateUI(authUser);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showDatePickerDialog(View v) {

        final Calendar c = Calendar.getInstance();
        int mYear, mMonth, mDay;
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

//        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
//                new DatePickerDialog.OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                        birthday.set(year, monthOfYear, dayOfMonth);
//                    }
//                }, mYear, mMonth, mDay);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this::onDateSet, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    private void setBirthday() {

    }

    public void updateUI(FirebaseUser firebaseUser) {
        Intent mainActivityIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainActivityIntent);
    }

    public void onRadioButtonClicked(View view) {
        if (((RadioButton) view).getText().equals("Male")) {
            gender = "m";
        } else {
            gender = "f";
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        birthday.set(year, monthOfYear, dayOfMonth);
        String birthdayText = dayOfMonth + "." + monthOfYear + "." + year + ".";
        et_birthday.setText(birthdayText);
    }
}