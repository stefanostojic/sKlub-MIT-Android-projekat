package com.stefan.sklub.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Model.User;
import com.stefan.sklub.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirestoreDB firestoreDB;

    @BindView(R.id.iv_img)
    ImageView iv_img;
    @BindView(R.id.tv_firstname)
    TextView tv_firstname;
    @BindView(R.id.tv_lastname)
    TextView tv_lastname;

    private RequestOptions options = new RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.ic_person_black_18dp)
            .error(R.drawable.ic_person_black_18dp)
            .circleCrop();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportActionBar().setTitle("My profile");
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        firestoreDB = FirestoreDB.getInstance();

        Glide.with(this)
                .load(R.drawable.ic_person_black_18dp)
                .apply(options)
                .into(iv_img);

        String userUidToLoad = mAuth.getUid();
        if (getIntent().hasExtra("userUid")) {
            userUidToLoad = getIntent().getStringExtra("userUid");
        }

        firestoreDB.getUserProfile(userUidToLoad, user -> {

            Glide.with(this)
                    .load(user.getImgUri())
                    .apply(options)
                    .into(iv_img);
            tv_firstname.setText(user.getFirstname());
            tv_lastname.setText(user.getLastname());
        });
    }
}