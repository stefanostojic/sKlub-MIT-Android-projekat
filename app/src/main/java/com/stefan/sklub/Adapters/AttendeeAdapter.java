package com.stefan.sklub.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stefan.sklub.Model.Attendee;
import com.stefan.sklub.Model.User;
import com.stefan.sklub.R;

import java.util.ArrayList;
import java.util.List;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.AttendeeAdapterViewHolder> {
    private List<Attendee> mAttendeesData;
    private AttendeeAdapterOnClickHandler mClickHandler;
    private Context context;

    final String TAG = "AttendeeAdapter ispis";

    public AttendeeAdapter() {

    }

    public interface AttendeeAdapterOnClickHandler {
        void onAttendeeRecycleViewItemClick(Attendee attendee);
    }

    public AttendeeAdapter(AttendeeAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public void setOnClickHandler(AttendeeAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public class AttendeeAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public final ImageView iv_attendee_img;
        public final TextView tv_firstname;
        public final TextView tv_lastname;

        public AttendeeAdapterViewHolder(View view) {
            super(view);
            iv_attendee_img = (ImageView) view.findViewById(R.id.iv_user_img);
            tv_firstname = (TextView) view.findViewById(R.id.tv_organiser_firstname);
            tv_lastname = (TextView) view.findViewById(R.id.tv_organiser_lastname);
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            mClickHandler.onAttendeeRecycleViewItemClick(mAttendeesData.get(adapterPosition));
        }
    }

    @Override
    public AttendeeAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.recyclerview_user_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new AttendeeAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AttendeeAdapterViewHolder attendeeAdapterViewHolder, int position) {
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
                .load(mAttendeesData.get(position).getImgUrl())
                .apply(options)
                .into(attendeeAdapterViewHolder.iv_attendee_img);

        attendeeAdapterViewHolder.tv_firstname.setText(mAttendeesData.get(position).getFirstName());
        attendeeAdapterViewHolder.tv_lastname.setText(mAttendeesData.get(position).getLastName());
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        if (null == mAttendeesData)
            return 0;
        return mAttendeesData.size();
    }

    public void addAttendee(Attendee attendee) {
        if (mAttendeesData == null)
            mAttendeesData = new ArrayList<Attendee>();
        mAttendeesData.add(attendee);
//        Log.d(TAG, "attendee added to recycle view");
        notifyDataSetChanged();
    }

    public void addMe(User me) {
        Attendee attendeeMe = new Attendee();
        attendeeMe.setUserDocId(me.getUserDocId());
        attendeeMe.setFirstName(me.getFirstName());
        attendeeMe.setLastName(me.getLastName());
        attendeeMe.setImgUrl(me.getImgUrl());
        mAttendeesData.add(0, attendeeMe);
        notifyDataSetChanged();
    }

    public void removeMe(User me) {
//        Log.d(TAG, "removeMe()");
//        Log.d(TAG, "mAttendeesData.size(): before: " + mAttendeesData.size());
        mAttendeesData.remove(0);
//        Log.d(TAG, "mAttendeesData.size(): after: " + mAttendeesData.size());
//        notifyDataSetChanged();
        notifyItemRemoved(0);
        notifyItemRangeChanged(0, mAttendeesData.size());
    }

    public void setAttendeesData(List<Attendee> attendees) {
        this.mAttendeesData = attendees;
    }
}
