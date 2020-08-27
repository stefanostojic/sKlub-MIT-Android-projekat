package com.stefan.sklub.Activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Interfaces.OnUpdateItem;
import com.stefan.sklub.Model.User;
import com.stefan.sklub.R;
import com.stefan.sklub.RotateBitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditProfileActivity extends BaseActivity {

    private static final String TAG = "EditProfileActivity ispis";
    private FirestoreDB db;
    private User me;
    private LocalDate birthday;
    private String gender;
    private User myNewUserData;
    private byte[] imgByteArray;
    private boolean removeImg;
    private boolean savingChangesInProgress;
    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 101;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 7502;
    private Bitmap imgBitmap;
    private ArrayList<Image> images = new ArrayList<>();

    @BindView(R.id.iv_img)
    ImageView iv_img;
//    @BindView(R.id.til_edit_email)
//    TextInputLayout til_edit_email;
    @BindView(R.id.til_edit_firstname)
    TextInputLayout til_edit_firstname;
    @BindView(R.id.til_edit_lastname)
    TextInputLayout til_edit_lastname;
    @BindView(R.id.til_edit_birthday)
    TextInputLayout til_edit_birthday;
    @BindView(R.id.til_edit_old_password)
    TextInputLayout til_edit_old_password;
    @BindView(R.id.til_edit_new_password)
    TextInputLayout til_edit_new_password;
    @BindView(R.id.til_edit_repeat_new_password)
    TextInputLayout til_edit_repeat_new_password;
    @BindView(R.id.radio_male)
    RadioButton radio_male;
    @BindView(R.id.radio_female)
    RadioButton radio_female;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        db = FirestoreDB.getInstance();

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Edit profile");

        if (getIntent().hasExtra("me")) {
            me = getIntent().getParcelableExtra("me");
            fillUserData();
        } else {
            Log.e(TAG, "No user data passed");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.confirmation_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_confirm:
                saveChanges();
                return true;
            case R.id.home:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("me", me);
                setResult(3, returnIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void fillUserData() {
//        til_edit_email.getEditText().setText(user.getUserDocId());
        til_edit_firstname.getEditText().setText(me.getFirstName());
        til_edit_lastname.getEditText().setText(me.getLastName());
        til_edit_birthday.getEditText().setText(me.getBirthday().format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")));
        birthday = me.getBirthday();
        if (me.getGender().equals("m")) {
            radio_male.setChecked(true);
        } else {
            radio_female.setChecked(true);
        }
        gender = me.getGender();

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_person_white_36dp)
                .error(R.drawable.ic_person_white_36dp)
                .circleCrop();
        if (!me.getImgUrl().equals("default")) {
            Glide.with(this)
                    .load(me.getImgUrl())
                    .apply(options)
                    .into(iv_img);
        } else {
            Log.i(TAG, "No image data");
            Glide.with(this)
                    .load(R.drawable.ic_person_white_36dp)
                    .apply(options)
                    .into(iv_img);
        }
    }

    private boolean isValidInput() {
//        if (TextUtils.isEmpty(til_edit_email.getEditText().getText())) {
//            Toast.makeText(this, "Email field can't be empty", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        if (!Patterns.EMAIL_ADDRESS.matcher(til_edit_email.getEditText().getText()).matches()) {
//            Toast.makeText(this, "Email in wrong format", Toast.LENGTH_SHORT).show();
//            return false;
//        }
        if (TextUtils.isEmpty(til_edit_firstname.getEditText().getText())) {
            Toast.makeText(this, "Firstname field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(til_edit_lastname.getEditText().getText())) {
            Toast.makeText(this, "Lastname field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(til_edit_birthday.getEditText().getText())) {
            Toast.makeText(this, "Birthday field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (gender == null) {
            Toast.makeText(this, "Gender field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(til_edit_old_password.getEditText().getText())) {
            Toast.makeText(this, "Password field can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!TextUtils.isEmpty(til_edit_repeat_new_password.getEditText().getText()) ||
        !TextUtils.isEmpty(til_edit_repeat_new_password.getEditText().getText())) {
            if (til_edit_new_password.getEditText().getText().toString().length() <= 6) {
                Toast.makeText(this, "New password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (TextUtils.isEmpty(til_edit_repeat_new_password.getEditText().getText())) {
                Toast.makeText(this, "You have to repeat your new password", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (!til_edit_new_password.getEditText().getText().toString().equals(til_edit_repeat_new_password.getEditText().getText().toString())) {
                Toast.makeText(this, "Passwords don't match :/", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public void showDatePickerDialogEditProfile(View v) {

        if (savingChangesInProgress)
            return;
        final Calendar c = Calendar.getInstance();
        int mYear, mMonth, mDay;
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, monthOfYear, dayOfMonth) -> {
            LocalDate receivedDate = LocalDate.of(year, monthOfYear, dayOfMonth);
            LocalDate today = LocalDate.now();
            receivedDate.plus(13, ChronoUnit.YEARS);
            if (receivedDate.plus(13, ChronoUnit.YEARS).isBefore(today)) {
                birthday = LocalDate.of(year, monthOfYear, dayOfMonth);
                til_edit_birthday.getEditText().setText(birthday.format(DateTimeFormatter.ofPattern("dd.MMM.yyyy.")));
            } else {
                birthday = null;
                Toast.makeText(EditProfileActivity.this, "The user has to be at least 13 years old.", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 12345 && resultCode == Activity.RESULT_OK) {
            Uri selectedImageUri = data.getData();

            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_person_white_18dp)
                    .error(R.drawable.ic_person_white_18dp)
                    .circleCrop();

            Bitmap bmp1 = null;
            try {
                RotateBitmap rb = new RotateBitmap();
                bmp1 = rb.HandleSamplingAndRotationBitmap(this, selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int sideLength = 0;
            int height = bmp1.getHeight(); // 400
            int width = bmp1.getWidth(); // 580
            int startX = 0;
            int startY = 0;
            if ((double)width / height > 1) {
                // landscape img
                sideLength = height;
                startX = (width - sideLength) / 2;
            } else {
                // portrait img
                sideLength = width;
                startY = (height - sideLength) / 2;
            }

            Bitmap bitmap2 = Bitmap.createBitmap(bmp1, startX, startY, sideLength, sideLength);
            Bitmap bitmap3 = Bitmap.createScaledBitmap(bitmap2, 300, 300, false);
            imgBitmap = bitmap3;
            removeImg = false;
            iv_img.setPadding(0, 0, 0, 0);
            Glide.with(this)
                    .load(bitmap3)
                    .apply(options)
                    .into(iv_img);
        }
    }

    public void editImg(View view) {
        if (savingChangesInProgress)
            return;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 12345);
    }

    public void removeImg(View view) {
        if (savingChangesInProgress)
            return;
        if (imgBitmap == null) {
            if (me.getImgUrl().equals("default")) {
                Toast.makeText(this, "No image to remove", Toast.LENGTH_SHORT).show();
            } else {
                removeImg = true;
            }
        } else {
            imgBitmap = null;
        }
        Glide.with(this)
                .load(R.drawable.ic_person_white_18dp)
                .into(iv_img);
    }

    private void saveChanges() {
        if (!isValidInput()) {
            return;
        } else {
            Toast.makeText(this, "Saving changes...", Toast.LENGTH_SHORT).show();
            savingChangesInProgress = true;
            String oldPassword = til_edit_old_password.getEditText().getText().toString();
            String newPassword;
            if (til_edit_new_password.getEditText().getText().toString().isEmpty()) {
                newPassword = oldPassword;
            } else {
                newPassword = til_edit_new_password.getEditText().getText().toString();
            }

            if (imgBitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                imgByteArray = baos.toByteArray();
            } else {
                imgByteArray = null;
            }


            myNewUserData = me;
            myNewUserData.setFirstName(til_edit_firstname.getEditText().getText().toString());
            myNewUserData.setLastName(til_edit_lastname.getEditText().getText().toString());
            myNewUserData.setBirthday(birthday);
            myNewUserData.setGender(gender);
            if (removeImg)
                myNewUserData.setImgUrl("default");
            db.updateUserProfile(myNewUserData, imgByteArray, removeImg, oldPassword, newPassword, new OnUpdateItem<User>() {
                @Override
                public void onSuccessfulUpdate(User newUserData) {
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("updatedUser", newUserData);
                    setResult(2, intent);
                    finish();
                }

                @Override
                public void onError(String error) {
                    MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(EditProfileActivity.this);
                    materialAlertDialogBuilder
                            .setTitle("Problem encountered...")
                            .setMessage("Your data was not successfully updated. What do you want to do with the changes you made?")
                            .setPositiveButton("Try again", (dialogInterface, i) -> {
                                saveChanges();
                            })
                            .setNegativeButton("Discard 'em", (dialogInterface, i) -> {
                                finishAffinity();
                                Toast.makeText(EditProfileActivity.this, "Discarding changes...", Toast.LENGTH_SHORT).show();
                            });
                    materialAlertDialogBuilder.show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {

        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this);
        materialAlertDialogBuilder
                .setTitle("Wait, wait, wait...")
                .setMessage("What do you want to do with the changes you made?")
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    saveChanges();
                })
                .setNegativeButton("Discard", (dialogInterface, i) -> {
                    super.onBackPressed();
                    Toast.makeText(this, "Discarding changes...", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Cancel", (dialogInterface, i) -> {
//                    just don't do anything :D;
                });
        materialAlertDialogBuilder.show();
    }
}