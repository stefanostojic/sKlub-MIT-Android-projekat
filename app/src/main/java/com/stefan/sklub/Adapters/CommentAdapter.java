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
import com.google.common.collect.Lists;
import com.stefan.sklub.Model.Comment;
import com.stefan.sklub.R;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentAdapterViewHolder> {
    private List<Comment> mCommentsData;
    private CommentAdapterOnClickHandler mClickHandler;
    private Context context;

    final String TAG = "CommentAdapter ispis";

    public CommentAdapter() {

    }

    public interface CommentAdapterOnClickHandler {
        void onCommentRecycleViewItemClick(Comment comment);
    }

    public CommentAdapter(CommentAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public void setOnClickHandler(CommentAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public class CommentAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        public final TextView tv_firstname;
        public final TextView tv_lastname;
        public final TextView tv_datetime;
        public final TextView tv_text;

        public CommentAdapterViewHolder(View view) {
            super(view);
            tv_firstname = (TextView) view.findViewById(R.id.tv_firstname);
            tv_lastname = (TextView) view.findViewById(R.id.tv_lastname);
            tv_datetime = (TextView) view.findViewById(R.id.tv_datetime);
            tv_text = (TextView) view.findViewById(R.id.tv_text);
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();

//            mClickHandler.onCommentRecycleViewItemClick(mCommentsData.get(adapterPosition));
        }
    }

    @Override
    public CommentAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.recyclerview_comment_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new CommentAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentAdapterViewHolder commentAdapterViewHolder, int position) {
        if (context == null) {
            return;
        }

        commentAdapterViewHolder.tv_firstname.setText(mCommentsData.get(position).getFirstName());
        commentAdapterViewHolder.tv_lastname.setText(" " + mCommentsData.get(position).getLastName() + ": ");
        String datetime = mCommentsData.get(position).getDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        int day = mCommentsData.get(position).getDateTime().getDayOfMonth();
        LocalDateTime today = LocalDateTime.now();
//        Log.d(TAG, "comment.dateTime.getDayOfMonth(): " + day);
//        Log.d(TAG, "today.getDayOfMonth(): " + today.getDayOfMonth());
        if (day == today.getDayOfMonth()) {
            datetime += ", today";
//            Log.d(TAG, "The comment was posted today");
        }
        else if (day == today.getDayOfMonth() - 1) {
            datetime += ", yesturday";
        } else {
            datetime += mCommentsData.get(position).getDateTime().format(DateTimeFormatter.ofPattern(", dd.MM.yyyy."));
        }
        commentAdapterViewHolder.tv_datetime.setText(datetime + " ");
        commentAdapterViewHolder.tv_text.setText(mCommentsData.get(position).getText());
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getItemCount() {
        if (null == mCommentsData)
            return 0;
        return mCommentsData.size();
    }

    public void addComment(Comment comment) {
        if (mCommentsData == null)
            mCommentsData = new ArrayList<Comment>();
        mCommentsData.add(comment);
//        Log.d(TAG, "comment added to recycle view");
        notifyDataSetChanged();
    }

    public void addNewComment(Comment comment) {
        if (mCommentsData == null)
            mCommentsData = new ArrayList<Comment>();
        mCommentsData.add(comment);
//        Log.d(TAG, "comment added to recycle view");
        notifyDataSetChanged();
    }

    public void setCommentsData(List<Comment> comments) {
        this.mCommentsData = comments;
        Collections.sort(mCommentsData);
        Lists.reverse(mCommentsData);
        notifyDataSetChanged();
    }
}
