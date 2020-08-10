package com.stefan.sklub.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.stefan.sklub.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setTitle("Settings");
    }
}