package com.stefan.sklub.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.stefan.sklub.Event;
import com.stefan.sklub.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventDetailsActivity extends AppCompatActivity {

//    @BindView(R.id.tv_event_name)
//    TextView tv_event_name;
//    @BindView(R.id.tv_event_organiser_name)
//    TextView tv_event_organiser_name;
//    @BindView(R.id.iv_event)
//    ImageView iv_event;
    TextView tv_event_name;
    TextView tv_event_organiser_name;
    ImageView iv_event;

    private Event event;
    final String TAG = "ispis";
    private StorageReference mStorageRef;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
//        ButterKnife.bind(this);
        tv_event_name = findViewById(R.id.tv_event_name);
        tv_event_organiser_name = findViewById(R.id.tv_event_organiser_name);
        iv_event = findViewById(R.id.iv_event);

        if (getIntent() != null && getIntent().hasExtra("event")) {
            event = getIntent().getParcelableExtra("event");
            tv_event_name.setText(event.getName());
            tv_event_organiser_name.setText(event.getOrganiser().getFirstname());
            Glide.with(this)
                    .asBitmap()
                    .load(event.getPlace().getImgUri())
                    .into(iv_event);
        }
    }

    public void onBtnAllPeopleClick(View view) {
        Toast.makeText(this, "dugme radi", Toast.LENGTH_SHORT);
    }


}