package com.stefan.sklub.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserProfileActivity extends BaseActivity {

    private static final String TAG = "UserProfileActivity ispis";
    private FirebaseAuth mAuth;
    private FirestoreDB db;
    String userUid;
    boolean isMyProfile;

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

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("My profile");
        ButterKnife.bind(this);

        db = FirestoreDB.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (getIntent().hasExtra("userUid")) {
            userUid = getIntent().getStringExtra("userUid");
            isMyProfile = false;
        } else {
            userUid = mAuth.getUid();
            isMyProfile = true;
        }

        Log.i(TAG, "Requesting User data for Uid: " + userUid);
        db.getUserByUserUid(userUid, user -> {
            Log.i(TAG, "User data fetched");

            Glide.with(this)
                    .load(user.getImgUri())
                    .apply(options)
                    .into(iv_img);
            tv_firstname.setText(user.getFirstname());
            tv_lastname.setText(user.getLastname());
        });
        // TODO: Fetch the events which are organised by the user


        // TODO: Fetch the events which are attended by the user
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isMyProfile) {
            getMenuInflater().inflate(R.menu.edit_delete_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.report_menu, menu);
        }
        return true;
    }

    private void loadUsersEvents() {
//        db.getEventsByUserDocRef()
    }
}