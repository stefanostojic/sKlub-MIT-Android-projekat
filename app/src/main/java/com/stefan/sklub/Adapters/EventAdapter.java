package com.stefan.sklub.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.Sport;
import com.stefan.sklub.R;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventAdapterViewHolder> {
//    private Event[] mEventsData;
    private List<Event> mEventsData;
    private EventAdapterOnClickHandler mClickHandler;
    private Context context;

    final String TAG = "EventAdapter ispis";

    public EventAdapter() {

    }

    public interface EventAdapterOnClickHandler {
        void onEventRecycleViewItemClick(Event event);
    }

    public EventAdapter(EventAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public void setOnClickHandler(EventAdapterOnClickHandler clickHandler) {
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

            mClickHandler.onEventRecycleViewItemClick(mEventsData.get(adapterPosition));
        }
    }

    @Override
    public EventAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.recyclerview_event_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new EventAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventAdapterViewHolder eventAdapterViewHolder, int position) {
        if (context == null) {
            return;
        }

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.ic_person_black_18dp)
                .error(R.drawable.ic_person_black_18dp)
                .circleCrop();

        Glide.with(context)
                .asBitmap()
                .load(mEventsData.get(position).getPlace().getImgUri())
                .into(eventAdapterViewHolder.iv_event);
        Glide.with(context)
                .asBitmap()
                .load(Sport.getImgUri(mEventsData.get(position).getSport()))
                .apply(options)
                .into(eventAdapterViewHolder.iv_event_icon);
        Glide.with(context)
                .asBitmap()
                .load(mEventsData.get(position).getOrganiser().getImgUri())
                .apply(options)
                .into(eventAdapterViewHolder.iv_organiser);

        eventAdapterViewHolder.tv_event_name.setText(mEventsData.get(position).getName());
        eventAdapterViewHolder.tv_organiser_firstname.setText(mEventsData.get(position).getOrganiser().getFirstname());
        eventAdapterViewHolder.tv_organiser_lastname.setText(mEventsData.get(position).getOrganiser().getLastname());
        eventAdapterViewHolder.tv_time.setText(mEventsData.get(position).getDate().format(DateTimeFormatter.ofPattern("HH:mm")));
        eventAdapterViewHolder.tv_date.setText(mEventsData.get(position).getDate().format(DateTimeFormatter.ofPattern("dd.MM")));
        eventAdapterViewHolder.tv_place.setText(mEventsData.get(position).getPlace().getName());
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        if (null == mEventsData)
            return 0;
        return mEventsData.size();
    }

//    public void setEventsData(List<Event> eventsData) {
//        mEventsData = eventsData;
//        notifyDataSetChanged();
//    }

    public void addEvent(Event event) {
        if (mEventsData == null)
            mEventsData = new ArrayList<Event>();
        mEventsData.add(event);
//        Log.d(TAG, "event added to recycle view");
        Collections.sort(mEventsData);
        notifyDataSetChanged();
    }
}
