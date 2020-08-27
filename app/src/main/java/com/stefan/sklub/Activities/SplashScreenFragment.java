package com.stefan.sklub.Activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.stefan.sklub.R;

public class SplashScreenFragment extends Fragment {

    boolean isOnline;

    public SplashScreenFragment() {

    }

    public SplashScreenFragment(boolean isOnline) {
        this.isOnline = isOnline;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!isOnline)
            hideProgressBar();
    }

    public void hideProgressBar() {
        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.progress_loader);
        progressBar.setVisibility(View.INVISIBLE);
    }
}