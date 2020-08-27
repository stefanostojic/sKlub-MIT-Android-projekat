package com.stefan.sklub.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.stefan.sklub.Adapters.EventAdapter;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.User;
import com.stefan.sklub.R;

import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    public List<Event> events = new ArrayList<Event>();
    private FloatingActionButton fab;
    private RecyclerView mRecyclerView;
    private EventAdapter mEventAdapter;
    private OnReadyListener listener;
    private Context context;
    private User me;

    public EventsFragment() {

    }

    public EventsFragment(List<Event> events, Context context, User me) {
        this.events = events;
        this.context = context;
        this.me = me;
    }

    public interface OnReadyListener {
        void onReady();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recyclerview_events);
        fab = (FloatingActionButton) getView().findViewById(R.id.floating_action_button);
        fab.setColorFilter(Color.WHITE);

        fab.setOnClickListener(view1 -> {
            Intent addEventIntent = new Intent(context, AddEventActivity.class);
            addEventIntent.putExtra("me", me);
            startActivityForResult(addEventIntent, 3);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mEventAdapter = new EventAdapter();
        mEventAdapter.setEventsData(events);
        mEventAdapter.setOnClickHandler(event -> {
            Intent eventDetailsIntent = new Intent(context, EventDetailsActivity.class);
            eventDetailsIntent.putExtra("event", event);
            eventDetailsIntent.putExtra("me", me);
            startActivity(eventDetailsIntent);
        });
        mEventAdapter.setContext(context);
        mRecyclerView.setAdapter(mEventAdapter);
    }

    public void addEvent(Event event) {
        mEventAdapter.addEvent(event);
    }
}
