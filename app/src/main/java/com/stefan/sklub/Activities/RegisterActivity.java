package com.stefan.sklub.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

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
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Interfaces.OnAddItem;
import com.stefan.sklub.R;
import com.stefan.sklub.Model.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirestoreDB db;

    private final String TAG = "RegisterActivity ispis";
    private LocalDate birthday;
    private String gender;

    @BindView(R.id.til_email)
    TextInputLayout tilEmail;
    @BindView(R.id.til_firstname)
    TextInputLayout tilFirstName;
    @BindView(R.id.til_lastname)
    TextInputLayout tilLastName;
    @BindView(R.id.til_birthday)
    TextInputLayout tilBirthday;
    @BindView(R.id.til_password)
    TextInputLayout tilPassword;
    @BindView(R.id.til_repeatpassword)
    TextInputLayout tilRepeatPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirestoreDB.getInstance();

        tilBirthday.setEndIconOnClickListener(view -> {
            onDatePick();
        });
    }

    private void onDatePick() {
        final View dialogView = View.inflate(this, R.layout.dialog_date_picker, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        dialogView.findViewById(R.id.date_set).setOnClickListener(view -> {

            DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);

            LocalDate receivedDate = LocalDate.of(datePicker.getYear(),
                    datePicker.getMonth() + 1,
                    datePicker.getDayOfMonth());
            LocalDate today = LocalDate.now();
            receivedDate.plus(13, ChronoUnit.YEARS);
            if (receivedDate.plus(13, ChronoUnit.YEARS).isBefore(today)) {
                birthday = receivedDate;
                tilBirthday.getEditText().setText(birthday.format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")));
                alertDialog.dismiss();
            } else {
                birthday = null;
                Toast.makeText(RegisterActivity.this, "The user has to be at least 13 years old.", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.show();
    }

    private boolean validInput() {
        if (TextUtils.isEmpty(tilEmail.getEditText().getText().toString())) {
            Toast.makeText(this, "Email field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(tilEmail.getEditText().getText().toString()).matches()) {
            Toast.makeText(this, "Email in wrong format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(tilFirstName.getEditText().getText().toString())) {
            Toast.makeText(this, "Firstname field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(tilLastName.getEditText().getText().toString())) {
            Toast.makeText(this, "Lastname field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(tilBirthday.getEditText().getText().toString())) {
            Toast.makeText(this, "Birthday field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (gender == null) {
            Toast.makeText(this, "Gender field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(tilPassword.getEditText().getText().toString())) {
            Toast.makeText(this, "Password field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (tilPassword.getEditText().getText().toString().length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(tilRepeatPassword.getEditText().getText().toString())) {
            Toast.makeText(this, "You have to repeat your password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!tilPassword.getEditText().getText().toString().equals(tilRepeatPassword.getEditText().getText().toString())) {
            Toast.makeText(this, "Passwords don't match :/", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void onBtnRegisterClick(View view) {

        if (!validInput()) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(tilEmail.getEditText().getText().toString(), tilPassword.getEditText().getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            User newUser = new User(
                                    mAuth.getCurrentUser().getUid(),
                                    tilFirstName.getEditText().getText().toString(),
                                    tilLastName.getEditText().getText().toString(),
                                    birthday,
                                    gender);
                            newUser.setLocation(new GeoPoint(0, 0));
                            newUser.setImgUrl("default");

                            db.addUser(newUser, new OnAddItem() {
                                @Override
                                public void onAdd(String docId) {
                                    Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                }

                                @Override
                                public void onError(String error) {
                                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_SHORT).show();
                                }
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
                LocalDate receivedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth);
                LocalDate today = LocalDate.now();
                receivedDate.plus(13, ChronoUnit.YEARS);
                if (receivedDate.plus(13, ChronoUnit.YEARS).isBefore(today)) {
                    birthday = receivedDate;
                    tilBirthday.getEditText().setText(birthday.format(DateTimeFormatter.ofPattern("dd.MMM.yyyy.")));
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