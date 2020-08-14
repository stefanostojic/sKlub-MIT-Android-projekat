package com.stefan.sklub.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.stefan.sklub.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setTitle("Settings");

    }

    public void set() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public void onBtnChangeTheme1(View view) {
        getApplication().setTheme(R.style.BlueAppTheme);
        SettingsActivity.this.recreate();
    }

    public void onBtnChangeTheme2(View view) {
        getApplication().setTheme(R.style.AppTheme);
        SettingsActivity.this.recreate();
    }
}