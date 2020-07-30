package com.stefan.sklub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements EventAdapter.EventAdapterOnClickHandler, View.OnClickListener {

    // COMPLETED (34) Add a private RecyclerView variable called mRecyclerView
    private RecyclerView mRecyclerView;
    // COMPLETED (35) Add a private ForecastAdapter variable called mForecastAdapter
    private EventAdapter mEventAdapter;

    final String TAG = "ispis";
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.i("ulogovanost", currentUser.getUid());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        getSupportActionBar().hide();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "raspored.jpg");
        Log.i(TAG, file.exists() + "");
        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 3);

        // Buttons
        findViewById(R.id.btnSignOut).setOnClickListener(this);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_event);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        // COMPLETED (41) Set the layoutManager on mRecyclerView
        mRecyclerView.setLayoutManager(layoutManager);

        // COMPLETED (42) Use setHasFixedSize(true) on mRecyclerView to designate that all items in the list will have the same size
        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        // COMPLETED (43) set mForecastAdapter equal to a new ForecastAdapter
        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         */
        mEventAdapter = new EventAdapter(this);

        // COMPLETED (44) Use mRecyclerView.setAdapter and pass in mForecastAdapter
        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mEventAdapter);
        Event[] events = new Event[3];
        events[0] = new Event("Prvi dog", "", "", null);
        events[1] = new Event("Drugi dog", "", "", null);
        events[2] = new Event("Treci dog", "", "", null);
        mEventAdapter.setEventsData(events);

        TextView mTextView = (TextView) findViewById(R.id.textView);
//        Button mButton = (Button) findViewById(R.id.button);
        Button mBtnLogin = (Button) findViewById(R.id.btnLogin);
//        final EditText mEtUserName = (EditText) findViewById(R.id.et_user_name);
//        EditText mEtUserSurname = (EditText) findViewById(R.id.et_user_surname);

//        mButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//            Log.i(TAG, "klik na dugme");
//
//            Map<String, Object> user = new HashMap<>();
//            user.put("first", "Ada");
//            user.put("last", "Lovelace");
//            user.put("born", 1815);
//
//            db.collection("users")
//            .add(user)
//            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                @Override
//                public void onSuccess(DocumentReference documentReference) {
//                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                }
//            })
//            .addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                Log.w(TAG, "Error adding document", e);
//                }
//            });
//            }
//        });

//        mBtnLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//            Log.i(TAG, "klik na login");
//
//            db.collection("users")
//            .whereEqualTo("name", mEtUserName.getText())
//            .get()
//            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        Log.i(TAG, document.getId() + " => " + document.getData());
//                    }
//
//                } else {
//                    Log.w(TAG, "Error getting documents.", task.getException());
//                }
//                }
//            });
//            }
//        });

    }

    private void signOut() {
        Log.i(TAG, "signOut() klik!");
        mAuth.signOut();
        this.onDestroy();
    }

    @Override
    public void onClick(String eventName) {
        Context context = this;
        Toast.makeText(context, eventName, Toast.LENGTH_SHORT)
        .show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btnSignOut) {
            signOut();
        }
//        else if (i == R.id.emailSignInButton) {
//            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
//        }
    }

    @Override
    public void onDestroy() {
        mAuth.signOut();

        super.onDestroy();
    }

    public void onBtnSignOutClick(View view) {
        mAuth.signOut();
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    public void onBtnUpload(View view) {
//        Uri file = Uri.fromFile(new File("path/to/images/rivers.jpg"));
        Uri file = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "raspored.jpg"));
        Log.i(TAG, file.getPath());
        StorageReference riversRef = mStorageRef.child("images/raspored.jpg");

        riversRef.putFile(file)
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, "Upload slike uspesan :)");
                // Get a URL to the uploaded content
//                Uri downloadUrl = taskSnapshot.getMetadata().getPath();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i(TAG, "Upload slike prop'o");
                Log.i(TAG, exception.getCause().toString());
                Log.i(TAG, exception.getMessage());
                Log.i(TAG, exception.getStackTrace().toString());
                // Handle unsuccessful uploads
                // ...
            }
        });
    }
}
