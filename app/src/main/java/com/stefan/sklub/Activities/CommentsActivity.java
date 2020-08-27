package com.stefan.sklub.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.stefan.sklub.Adapters.AttendeeAdapter;
import com.stefan.sklub.Adapters.CommentAdapter;
import com.stefan.sklub.Database.FirestoreDB;
import com.stefan.sklub.Interfaces.OnAddItem;
import com.stefan.sklub.Model.Comment;
import com.stefan.sklub.Model.Event;
import com.stefan.sklub.Model.User;
import com.stefan.sklub.R;

import java.time.LocalDateTime;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentsActivity extends BaseActivity {

    @BindView(R.id.recyclerview_comments)
    RecyclerView commentsRecycleView;
    @BindView(R.id.til_comment)
    TextInputLayout til_comment;

    private FirestoreDB db;
    private CommentAdapter commentAdapter;

    private Event event;
    private User me;
    private final String TAG = "CommentsActivity ispis";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Comments");
        ButterKnife.bind(this);

        db = FirestoreDB.getInstance();

        if (getIntent().hasExtra("event")) {
            event = getIntent().getParcelableExtra("event");
        } else {
            Log.e(TAG, "No event data");
        }
        if (getIntent().hasExtra("me")) {
            me = getIntent().getParcelableExtra("me");
        } else {
            Log.e(TAG, "No app user data");
        }

        til_comment.setEndIconOnClickListener(view -> {
            addComment();
        });

        loadComments();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                Log.d(TAG, "Overriding up action...");
                Intent intentData = new Intent();
                intentData.putExtra("event", event);
                intentData.putExtra("me", me);
                setResult(3, intentData);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadComments() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        commentsRecycleView.setLayoutManager(layoutManager);
        commentsRecycleView.setHasFixedSize(true);
        commentAdapter = new CommentAdapter();
        commentsRecycleView.setAdapter(commentAdapter);
        commentAdapter.setContext(this);
        commentAdapter.setCommentsData(event.getComments());
    }

    private void addComment() {
        if (TextUtils.isEmpty(til_comment.getEditText().getText())) {
            Toast.makeText(this, "Comment field can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Comment comment = new Comment();
        comment.setUserDocId(me.getUserDocId());
        comment.setFirstName(me.getFirstName());
        comment.setLastName(me.getLastName());
        comment.setDateTime(LocalDateTime.now());
        comment.setText(til_comment.getEditText().getText().toString());
        event.getComments().add(comment);
//        commentAdapter.addNewComment(comment);
        db.addComment(event, comment);
        til_comment.getEditText().setText("");
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(til_comment.getWindowToken(), 0);
        til_comment.clearFocus();
    }

    @Override
    public void onBackPressed() {
        Intent intentData = new Intent();
        intentData.putExtra("event", event);
        intentData.putExtra("me", me);
        setResult(3, intentData);
        finish();
    }
}