package com.stefan.sklub.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.stefan.sklub.R;

public abstract class BaseActivity extends AppCompatActivity {

    public static final int ACCESS_NETWORK_STATE_REQUEST_CODE = 101;
    public static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 102;
    public static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 103;
    public static final int INTERNET_REQUEST_CODE = 104;
    public static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 105;
    public static final int ACCESS_BACKGROUND_LOCATION_REQUEST_CODE = 106;
    public static final int CAMERA_REQUEST_CODE = 107;

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
        } else {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_my_profile:
                Intent userProfileIntent = new Intent(this, UserProfileActivity.class);
                userProfileIntent.putExtra("userUid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(userProfileIntent);
                return true;
            case R.id.menu_action_add_place:
                startActivity(new Intent(this, AddPlaceActivity.class));
                return true;
            case R.id.menu_action_edit:
                startActivity(new Intent(this, EditProfileActivity.class));
                return true;
            case R.id.menu_action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_action_sign_out:
                FirebaseAuth.getInstance().signOut();
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(getLogTag(), "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(getLogTag(), "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(getLogTag(), "onPause()");
        if(isFinishing()){
            Log.d(getLogTag(), "onPause() is finishing");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(getLogTag(), "onStop()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(getLogTag(), "onRestart()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(getLogTag(), "onDestroy()");
    }

    private String getLogTag() {
        return this.getLocalClassName().replace("Activities.", "") + " ispis";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.gc();
        this.finishAffinity();
    }
}
