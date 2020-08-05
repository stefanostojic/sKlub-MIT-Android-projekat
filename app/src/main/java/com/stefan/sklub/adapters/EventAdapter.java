package com.stefan.sklub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stefan.sklub.Event;
import com.stefan.sklub.R;
import com.stefan.sklub.activities.EventDetailsActivity;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventAdapterViewHolder> {
    private Event[] mEventsData;
    private EventAdapterOnClickHandler mClickHandler;
    private Context context;

    final String TAG = "ispis";

    public EventAdapter() {

    }
    public interface EventAdapterOnClickHandler {
        void onClick(String eventName);
    }

    public EventAdapter(EventAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public class EventAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public final ImageView iv_event;
        public final ImageView iv_event_icon;
        public final TextView tv_event_name;
        public final ImageView iv_organiser;
        public final TextView tv_organiser_firstname;
        public final TextView tv_organiser_lastname;
        public final TextView tv_time;
        public final TextView tv_date;
        public final TextView tv_place;


        public EventAdapterViewHolder(View view) {
            super(view);
            iv_event = (ImageView) view.findViewById(R.id.iv_event);
            iv_event_icon = (ImageView) view.findViewById(R.id.iv_event_icon);
            tv_event_name = (TextView) view.findViewById(R.id.tv_event_name);
            iv_organiser = (ImageView) view.findViewById(R.id.iv_organiser);
            tv_organiser_firstname = (TextView) view.findViewById(R.id.tv_organiser_firstname);
            tv_organiser_lastname = (TextView) view.findViewById(R.id.tv_organiser_lastname);
            tv_time = (TextView) view.findViewById(R.id.tv_time);
            tv_date = (TextView) view.findViewById(R.id.tv_date);
            tv_place = (TextView) view.findViewById(R.id.tv_place);
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            Intent eventDetailsIntent = new Intent(context, EventDetailsActivity.class);
            eventDetailsIntent.putExtra("event", mEventsData[adapterPosition]);
            eventDetailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(eventDetailsIntent);
//            mClickHandler.onClick(eventName);
        }
    }

    @Override
    public EventAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.event_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new EventAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventAdapterViewHolder eventAdapterViewHolder, int position) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_person_black_18dp)
                .error(R.drawable.ic_person_black_18dp)
                .circleCrop();

        Glide.with(context)
                .asBitmap()
                .load(mEventsData[position].getPlace().getImgUri())
                .into(eventAdapterViewHolder.iv_event);
        Glide.with(context)
                .asBitmap()
                .load(mEventsData[position].getOrganiser().getImgUri())
                .apply(options)
                .into(eventAdapterViewHolder.iv_event_icon);
        Glide.with(context)
                .asBitmap()
                .load(mEventsData[position].getOrganiser().getImgUri())
                .apply(options)
                .into(eventAdapterViewHolder.iv_organiser);

        eventAdapterViewHolder.tv_event_name.setText(mEventsData[position].getName());
        eventAdapterViewHolder.tv_organiser_firstname.setText(mEventsData[position].getOrganiser().getFirstname());
        eventAdapterViewHolder.tv_organiser_lastname.setText(mEventsData[position].getOrganiser().getLastname());
        eventAdapterViewHolder.tv_time.setText(mEventsData[position].getDate().format(DateTimeFormatter.ofPattern("HH:mm")));
        eventAdapterViewHolder.tv_date.setText(mEventsData[position].getDate().format(DateTimeFormatter.ofPattern("dd.MM")));
        eventAdapterViewHolder.tv_place.setText(mEventsData[position].getPlace().getName());
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        if (null == mEventsData) return 0;
        return mEventsData.length;
    }

    public void setEventsData(Event[] eventsData) {
        mEventsData = eventsData;
        notifyDataSetChanged();
    }
}
