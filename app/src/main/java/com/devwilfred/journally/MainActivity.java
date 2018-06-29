package com.devwilfred.journally;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private String mUserName = "nobody";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirebaseFirestore;
    private Query mQuery;
    DiaryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();


        if (mFirebaseUser == null) {
            // not signed in
            startActivity(new Intent(this, SigninActivity.class));
            finish();
            return;
        }

        FirebaseFirestore.setLoggingEnabled(true);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseFirestore.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true).build());

        mUserName = mFirebaseUser.getDisplayName();
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        RecyclerView recyclerView = findViewById(R.id.thought_recycler);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(mUserName);


        mQuery = mFirebaseFirestore.collection("wilfred").orderBy("when", Query.Direction.DESCENDING);


        TextView view = findViewById(R.id.no_notes);

        /*
          had issues with setting an identifier on diary items for update/delete
          got a hint from
          https://github.com/firebase/FirebaseUI-Android/issues/1131
         */
        FirestoreRecyclerOptions<Thought> options = new FirestoreRecyclerOptions.Builder<Thought>()
                .setQuery(mQuery, new SnapshotParser<Thought>() {
                    @NonNull
                    @Override
                    public Thought parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Thought parsedThought = snapshot.toObject(Thought.class);
                        parsedThought.setIdentifier(snapshot.getId());
                        return parsedThought;
                    }
                }).build();

        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference reference = firebaseStorage.getReference();
        adapter = new DiaryAdapter(options, view, reference);
        adapter.startListening();


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        /*
         * implementing swipe left to delete.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                mFirebaseFirestore.collection("wilfred")
                        .document((String) viewHolder.itemView.getTag())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("asdsad", "onSuccess: Removed list item");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("ASDSa", "onFailure: "+e.getLocalizedMessage());
                            }
                        });
            }
        }).attachToRecyclerView(recyclerView);

        }

    @Override
    protected void onStart() {
        super.onStart();

        if (adapter != null) {
            adapter.startListening();
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult pConnectionResult) {

    }

    public void openAddActivity(View view) {
        Intent addActivityIntent = new Intent(this, AddActivity.class);
        addActivityIntent.putExtra("userUid", mFirebaseUser.getUid());
        startActivity(addActivityIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                mFirebaseAuth.signOut();
                startActivity(new Intent(this, SigninActivity.class));
                finish();
                break;
                default:
                    super.onOptionsItemSelected(item);
        }
        return true;
    }
}
