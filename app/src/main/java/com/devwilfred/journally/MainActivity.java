package com.devwilfred.journally;

import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        FilterFragment.RecieveFilter, DiaryAdapter.ThoughtClickListener {

    private String mUserName = "nobody";
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirebaseFirestore;
    private Query mQuery;
    RecyclerView recyclerView;
    FirestoreRecyclerOptions<Thought> options;
    StorageReference reference;
    DiaryAdapter adapter;
    FloatingActionButton fab;
    TextView view;
    FirebaseStorage firebaseStorage;
    private GoogleApiClient mGoogleApiClient;
    Toolbar toolbar;
    FrameLayout filterBarContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new android.transition.Fade());
        }
        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

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
        toolbar = findViewById(R.id.home_toolbar);
        recyclerView = findViewById(R.id.thought_recycler);
        fab = findViewById(R.id.fab);
        filterBarContainer = findViewById(R.id.filter_bar_container);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(mUserName);


        mQuery = mFirebaseFirestore.collection(mFirebaseUser.getUid()).orderBy("when", Query.Direction.DESCENDING);



        view = findViewById(R.id.no_notes);

        /*
          got a hint from
          https://github.com/firebase/FirebaseUI-Android/issues/1131
         */
        firebaseStorage = FirebaseStorage.getInstance();
        reference = firebaseStorage.getReference();
       setRecyclerWithQuery();


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
                DocumentReference document =  mFirebaseFirestore.collection(mFirebaseUser.getUid())
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

        final MenuItem searchView = menu.findItem(R.id.search);

        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Do something when action item collapses
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    toolbar.setBackgroundColor(getColor(R.color.colorPrimary));

                }
                filterBarContainer.setVisibility(View.VISIBLE);
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    toolbar.setBackgroundColor(getColor(R.color.colorPrimaryDark));
                }
                filterBarContainer.setVisibility(View.GONE);
                return true;  // Return true to expand action view
            }
        };

        MenuItemCompat.setOnActionExpandListener(searchView, expandListener);

        ((SearchView)searchView.getActionView()).setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String pS) {

                // change the database query
                mQuery = mFirebaseFirestore.collection(mFirebaseUser.getUid())
                        .whereEqualTo("description", pS)
                        .orderBy("when", Query.Direction.DESCENDING);
                setRecyclerWithQuery();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String pS) {

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:

                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                startActivity(new Intent(this, SigninActivity.class));
                finish();
                break;


                default:
                    super.onOptionsItemSelected(item);
        }
        return true;
    }


    @Override
    public void onThoughtClicked(int pAdapterPosition, Thought pModel, ImageView pImageView) {

        Intent intent = new Intent(this, ThoughtDetailActivity.class);
        intent.putExtra("thought", pModel);
        intent.putExtra("transition", ViewCompat.getTransitionName(pImageView));

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, pImageView, ViewCompat.getTransitionName(pImageView));

        startActivity(intent, options.toBundle());
    }

    @Override
    public void onThoughtNoImageClicked(int pAdapterPosition, Thought pModel) {
        Intent intent = new Intent(this, ThoughtDetailActivity.class);
        intent.putExtra("thought", pModel);

        startActivity(intent);
    }

    public void filter(View view) {
        FragmentManager manager = getSupportFragmentManager();
        FilterFragment dialog = FilterFragment
                .newInstance(mFirebaseUser.getUid());
        dialog.show(manager, FilterFragment.TAG);
    }

    @Override
    public void onRevievedFilter(Thought pFilter) {
        // change the database query
        mQuery = mFirebaseFirestore.collection(mFirebaseUser.getUid())
                .whereEqualTo("tag", pFilter.getTag())
                .orderBy("when", Query.Direction.DESCENDING);

        setRecyclerWithQuery();
    }

    private void setRecyclerWithQuery() {

        options = new FirestoreRecyclerOptions.Builder<Thought>()
                .setQuery(mQuery, new SnapshotParser<Thought>() {
                    @NonNull
                    @Override
                    public Thought parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Thought parsedThought = snapshot.toObject(Thought.class);
                        parsedThought.setIdentifier(snapshot.getId());
                        return parsedThought;
                    }
                }).build();

        // update the adapter and set it to the recyclerview
        adapter = new DiaryAdapter(options, view, reference, MainActivity.this);
        adapter.startListening();


        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(adapter);
    }
}
