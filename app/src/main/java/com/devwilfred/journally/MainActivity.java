package com.devwilfred.journally;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, DiaryAdapter.ThoughtClickListener {

    private String mUserName = "nobody";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirebaseFirestore;
    private Query mQuery;
    DiaryAdapter adapter;
    FloatingActionButton fab;

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
        fab = findViewById(R.id.fab);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(mUserName);


        mQuery = mFirebaseFirestore.collection(mFirebaseUser.getUid()).orderBy("when", Query.Direction.DESCENDING);


        final TextView view = findViewById(R.id.no_notes);

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

        final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference reference = firebaseStorage.getReference();
        adapter = new DiaryAdapter(options, view, reference, this);
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
                DocumentSnapshot snapshot =  adapter.getSnapshots().getSnapshot(viewHolder.getAdapterPosition());
                DocumentReference document =  mFirebaseFirestore.collection("wilfred")
                        .document((String) viewHolder.itemView.getTag());


                       document.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, "Removed", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, "Removed", Toast.LENGTH_SHORT).show();
                            }
                        });

                /*firebaseStorage.getReference().child("wilfred/" + ).putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot pTaskSnapshot) {
                                Toast.makeText(AddActivity.this, "Image uploaded", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception pE) {
                        Toast.makeText(AddActivity.this, "image fail", Toast.LENGTH_LONG).show();
                    }
                });*/

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

    @Override
    public void onThoughtClicked(Thought pThought, ImageView pView) {
        Log.e("olhiuigjf", "lklhnkljhnkl");
        Intent thoughtDetailIntent = new Intent(this, ThoughtDetailActivity.class);
        thoughtDetailIntent.putExtra("thought", pThought);
        thoughtDetailIntent.putExtra("thought", pThought);

        // BEGIN_INCLUDE(start_activity)
        /**
         * Now create an {@link android.app.ActivityOptions} instance using the
         * {@link ActivityOptionsCompat#makeSceneTransitionAnimation(Activity, Pair[])} factory
         * method.
         */
        ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,

                // Now we provide a list of Pair items which contain the view we can transitioning
                // from, and the name of the view it is transitioning to, in the launched activity
                new Pair<View, String>(pView, "detail:header:image"),
                new Pair<View, String>(fab, "detail:fab:image"));

        // Now we can start the Activity, providing the activity options as a bundle
        ActivityCompat.startActivity(this, thoughtDetailIntent, activityOptions.toBundle());

    }
}
