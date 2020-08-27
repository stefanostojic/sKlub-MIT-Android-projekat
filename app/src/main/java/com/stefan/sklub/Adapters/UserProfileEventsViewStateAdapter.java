package com.stefan.sklub.Adapters;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.stefan.sklub.Activities.UserEventsFragment;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Interfaces.OnClickCallback;
import com.stefan.sklub.Interfaces.OnGetItems;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.User;

import java.util.ArrayList;
import java.util.List;

public class UserProfileEventsViewStateAdapter extends FragmentStateAdapter {

    private static final String TAG = "ViewStateAdapter ispis";
    private Context context;
    private OnClickCallback clickListenerForMyEventsList;
    private OnClickCallback clickListenerForAttendedEventsList;
    private FirestoreDB db;
    private User user;
    public UserEventsFragment myEventsFragment;
    public UserEventsFragment attendedEventsFragment;
    public List<Event> eventsForMyEventsList = new ArrayList<Event>();
    public List<Event> eventsForAttendedEventsList = new ArrayList<Event>();

    public UserProfileEventsViewStateAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d(TAG, "createFragment(" + position + ")");
        // Hardcoded in this order, you'll want to use lists and make sure the titles match
        db = FirestoreDB.getInstance();
        if (position == 0) {
//            myEventsFragment = new UserEventsFragment(0, clickListenerForMyEventsList, clickListenerForAttendedEventsList);
            myEventsFragment.setUser(user);
            myEventsFragment.setContext(context);
            return myEventsFragment;
        } else
//            attendedEventsFragment = new UserEventsFragment(1, clickListenerForMyEventsList, clickListenerForAttendedEventsList);
            attendedEventsFragment.setUser(user);
            attendedEventsFragment.setContext(context);
            return attendedEventsFragment;
    }

    @Override
    public int getItemCount() {
        // Hardcoded, use lists
        return 2;
    }

    public void loadOrganisedEvents() {
        db.getEventsByOrganiser(user.getUserDocId(), new OnGetItems<Event>() {
            @Override
            public void onGetItem(Event item) {
                Log.d(TAG, "Organised event loaded into " + TAG);
            }

            @Override
            public void onFinishedGettingItems() {
                Log.d(TAG, "Finished getting organised events for " + TAG);
            }
        });
    }

    public void loadAttendedEvents() {
        db.getEventsByAttendee(user.getUserDocId(), new OnGetItems<Event>() {
            @Override
            public void onGetItem(Event item) {
                Log.d(TAG, "Attended event loaded into " + TAG);
            }

            @Override
            public void onFinishedGettingItems() {
                Log.d(TAG, "Finished getting attended events for " + TAG);
            }
        });
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public UserEventsFragment getMyEventsFragment() {
        return myEventsFragment;
    }

    public UserEventsFragment getAttendedEventsFragment() {
        return attendedEventsFragment;
    }

    public void setClickListenerForMyEventsList(OnClickCallback clickCallback) {
        this.clickListenerForMyEventsList = clickCallback;
    }

    public void setClickListenerForAttendedEventsList(OnClickCallback clickCallback) {
        this.clickListenerForAttendedEventsList = clickCallback;
    }
}