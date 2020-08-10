package com.stefan.sklub.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.R;
import com.stefan.sklub.Model.User;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirestoreDB firestoreDB;

    private final String TAG = "RegisterActivity ispis";
    private EditText et_email;
    private EditText et_firstname;
    private EditText et_lastname;
    private EditText et_birthday;
    private LocalDate birthday;
    private String gender;
    private EditText et_password;
    private EditText et_repeatpassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firestoreDB = FirestoreDB.getInstance();

        et_email = (EditText) findViewById(R.id.et_email);
        et_firstname = (EditText) findViewById(R.id.et_firstname);
        et_lastname = (EditText) findViewById(R.id.et_lastname);
        et_birthday = (EditText) findViewById(R.id.et_birthday);
        et_password = (EditText) findViewById(R.id.et_password);
        et_repeatpassword = (EditText) findViewById(R.id.et_repeatpassword);

        et_email.setText("natasa@mail.com");
        et_password.setText("123456");
        et_firstname.setText("Nataša");
        et_lastname.setText("Zvekić");
        birthday = LocalDate.of(2020, 7, 16);
        gender = "f";
    }

    private boolean validInput() {
        if (TextUtils.isEmpty(et_email.getText())) {
            Toast.makeText(this, "Email field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(et_email.getText()).matches()) {
            Toast.makeText(this, "Email in wrong format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(et_firstname.getText())) {
            Toast.makeText(this, "Firstname field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(et_lastname.getText())) {
            Toast.makeText(this, "Lastname field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(et_birthday.getText())) {
            Toast.makeText(this, "Birthday field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (gender == null) {
            Toast.makeText(this, "Gender field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(et_password.getText())) {
            Toast.makeText(this, "Password field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (et_password.getText().toString().length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(et_repeatpassword.getText())) {
            Toast.makeText(this, "You have to repeat your password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!et_password.getText().toString().equals(et_repeatpassword.getText().toString())) {
            Toast.makeText(this, "Passwords don't match :/", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void onBtnRegisterClick(View view) {

        if (!validInput()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(et_email.getText().toString(), et_password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            User newUser = new User(
                                    mAuth.getCurrentUser().getUid(),
                                    et_firstname.getText().toString(),
                                    et_lastname.getText().toString(),
                                    birthday,
                                    gender);

                            firestoreDB.addUser(newUser, () -> {
                                Toast.makeText(RegisterActivity.this, "Registratio successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            });

                        } else {
                            Log.d(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

//    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showDatePickerDialog(View v) {

        final Calendar c = Calendar.getInstance();
        int mYear, mMonth, mDay;
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                LocalDate receivedDate = LocalDate.of(year, monthOfYear, dayOfMonth);
                LocalDate today = LocalDate.now();
                receivedDate.plus(13, ChronoUnit.YEARS);
                if (receivedDate.plus(13, ChronoUnit.YEARS).isBefore(today)) {
                    birthday = LocalDate.of(year, monthOfYear, dayOfMonth);
                    et_birthday.setText(birthday.format(DateTimeFormatter.ofPattern("dd.MMM.yyyy.")));
                } else {
                    birthday = null;
                    Toast.makeText(RegisterActivity.this, "The user has to be at least 13 years old.", Toast.LENGTH_SHORT).show();
                }
            }
        }, mYear, mMonth, mDay);

        datePickerDialog.show();
    }

    public void onRadioButtonClicked(View view) {
        if (((RadioButton) view).getText().equals("Male")) {
            gender = "m";
        } else {
            gender = "f";
        }
    }
}