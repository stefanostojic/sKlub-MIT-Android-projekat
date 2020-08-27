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
import com.stefan.sklub.Model.User;
import com.stefan.sklub.Model.Sport;
import com.stefan.sklub.R;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserAdapterViewHolder> {
    private List<User> mUsersData;
    private UserAdapterOnClickHandler mClickHandler;
    private Context context;

    final String TAG = "UserAdapter ispis";

    public UserAdapter() {

    }

    public interface UserAdapterOnClickHandler {
        void onUserRecycleViewItemClick(User user);
    }

    public UserAdapter(UserAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public void setOnClickHandler(UserAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public class UserAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public final ImageView iv_user_img;
        public final TextView tv_firstname;
        public final TextView tv_lastname;

        public UserAdapterViewHolder(View view) {
            super(view);
            iv_user_img = (ImageView) view.findViewById(R.id.iv_user_img);
            tv_firstname = (TextView) view.findViewById(R.id.tv_organiser_firstname);
            tv_lastname = (TextView) view.findViewById(R.id.tv_organiser_lastname);
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

            mClickHandler.onUserRecycleViewItemClick(mUsersData.get(adapterPosition));
        }
    }

    @Override
    public UserAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.recyclerview_user_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new UserAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserAdapterViewHolder userAdapterViewHolder, int position) {
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
                .load(mUsersData.get(position).getImgUrl())
                .apply(options)
                .into(userAdapterViewHolder.iv_user_img);

        userAdapterViewHolder.tv_firstname.setText(mUsersData.get(position).getFirstName());
        userAdapterViewHolder.tv_lastname.setText(mUsersData.get(position).getLastName());
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        if (null == mUsersData)
            return 0;
        return mUsersData.size();
    }

    public void addUser(User user) {
        if (mUsersData == null)
            mUsersData = new ArrayList<User>();
        mUsersData.add(user);
//        Log.d(TAG, "user added to recycle view");
        notifyDataSetChanged();
    }
}
