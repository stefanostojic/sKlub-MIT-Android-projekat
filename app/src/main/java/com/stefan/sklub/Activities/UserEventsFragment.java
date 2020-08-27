package com.stefan.sklub.Activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.stefan.sklub.Adapters.EventAdapter;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Interfaces.OnClickCallback;
import com.stefan.sklub.Interfaces.OnGetItems;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.User;
import com.stefan.sklub.R;

import java.util.ArrayList;
import java.util.List;

public class UserEventsFragment extends Fragment {

    private static final String TAG = "UserEventsFragment ispis";
    private RecyclerView mRecyclerView;
    private EventAdapter mEventAdapter;
    private Context context;
    private int mode;
    private User user;
    private FirestoreDB db;
    private OnClickCallback clickListenerForMyEventsList;
    private OnClickCallback clickListenerForAttendedEventsList;
    public List<Event> events = new ArrayList<Event>();

    public UserEventsFragment() {

    }

//    public UserEventsFragment(int mode, OnClickCallback clickListenerForMyEventsList, OnClickCallback clickListenerForAttendedEventsList) {
//        this.db = FirestoreDB.getInstance();
//        this.user = FirestoreDB.getSpecialUser();
//        this.mode = mode;
//        this.clickListenerForMyEventsList = clickListenerForMyEventsList;
//        this.clickListenerForAttendedEventsList = clickListenerForAttendedEventsList;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = getView().findViewById(R.id.recyclerview_events);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mEventAdapter = new EventAdapter();
        mEventAdapter.setEventsData(events);
        mEventAdapter.setContext(context);
        mRecyclerView.setAdapter(mEventAdapter);
//        if (mode == 0) {
//            mEventAdapter.setOnClickHandler(event -> {
//                clickListenerForMyEventsList.onClick(event);
//            });
//        } else {
//            mEventAdapter.setOnClickHandler(event -> {
//                clickListenerForAttendedEventsList.onClick(event);
//            });
//        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void loadOrganisedEvents() {
        db.getEventsByOrganiser(user.getUserDocId(), new OnGetItems<Event>() {
            @Override
            public void onGetItem(Event event) {
                Log.d(TAG, "Organised event loaded into " + event);
                mEventAdapter.addEvent(event);
            }

            @Override
            public void onFinishedGettingItems() {
                Log.d(TAG, "Finished getting organised events");
            }
        });
    }

    public void setEventsData(List<Event> events) {
        this.events = events;
    }

//    public void loadAttendedEvents() {
//        db.getEventsByAttendee(user.getUserDocId(), new OnGetItems<Event>() {
//            @Override
//            public void onGetItem(Event event) {
//                Log.d(TAG, "Attended event loaded into " + event);
//                mEventAdapter.addEvent(event);
//            }
//
//            @Override
//            public void onFinishedGettingItems() {
//                Log.d(TAG, "Finished getting attended events");
//            }
//        });
//    }
}