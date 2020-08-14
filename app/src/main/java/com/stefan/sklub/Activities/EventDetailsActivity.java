package com.stefan.sklub.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.core.view.MenuItemCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.Sport;
import com.stefan.sklub.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventDetailsActivity extends BaseActivity {

    @BindView(R.id.tv_event_name)
    TextView tv_event_name;
    @BindView(R.id.tv_event_organiser_firstname)
    TextView tv_event_organiser_firstname;
    @BindView(R.id.tv_event_organiser_lastname)
    TextView tv_event_organiser_lastname;
    @BindView(R.id.iv_event)
    ImageView iv_event;
    @BindView(R.id.iv_event_organiser)
    ImageView iv_event_organiser;
    @BindView(R.id.iv_event_sport)
    ImageView iv_event_sport;

    private ShareActionProvider actionProvider;

    private Event event;
    final String TAG = "ispis";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        ButterKnife.bind(this);

        event = getIntent().getParcelableExtra("event");

        tv_event_name.setText(event.getName());
        tv_event_organiser_firstname.setText(event.getOrganiser().getFirstname());
        tv_event_organiser_lastname.setText(event.getOrganiser().getLastname());

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_person_black_18dp)
                .error(R.drawable.ic_person_black_18dp)
                .circleCrop();

        Glide.with(this)
                .load(event.getPlace().getImgUri())
                .into(iv_event);
        Glide.with(this)
                .load(event.getOrganiser().getImgUri())
                .apply(options)
                .into(iv_event_organiser);
        Glide.with(this)
                .load(Sport.getImgUri(event.getSport()))
                .into(iv_event_sport);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_action_share);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_share:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "New message for sharing.");
                startActivity(Intent.createChooser(intent, "Share"));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onBtnAllPeopleClick(View view) {
        Toast.makeText(this, "dugme radi", Toast.LENGTH_SHORT).show();
    }


}