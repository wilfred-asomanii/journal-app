package com.devwilfred.journally.views;

import android.content.Intent;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
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
import com.devwilfred.journally.presenter.DiaryAdapter;
import com.devwilfred.journally.R;
import com.devwilfred.journally.presenter.SearchAdapter;
import com.devwilfred.journally.model.Thought;
import com.devwilfred.journally.presenter.ThoughtClickListener;
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

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, ThoughtClickListener, FilterFragment.ReceiveFilter {

    RecyclerView mRecyclerView;
    TextView mErrorView, mIsFilterTv, mFilterTv;
    FrameLayout mFilterBarContainer;
    Toolbar mToolbar;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseFirestore mFirebaseFirestore;
    private Query mQuery;
    private StorageReference mReference;
    private DiaryAdapter mDiaryAdapter;
    private GoogleApiClient mGoogleApiClient;
    private String mColorPrimaryDark = "#9e9e9e";
    private String mColorPrimary = "#f5f5f5";

    // hold entries in a list to be able to search
    private List<Thought> mThoughts = new ArrayList<>();
    private SearchAdapter mSearchAdapter;


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
            // launch the sign in activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }

        FirebaseFirestore.setLoggingEnabled(true);

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseFirestore.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true).build());

        mToolbar = findViewById(R.id.home_toolbar);
        mRecyclerView = findViewById(R.id.thought_recycler);
        mFilterBarContainer = findViewById(R.id.filter_bar_container);
        mFilterTv = findViewById(R.id.current_filter);
        mIsFilterTv = findViewById(R.id.is_filtered_tv);


        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mFirebaseUser.getDisplayName());
        }

        mErrorView = findViewById(R.id.no_notes);
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        mReference = firebaseStorage.getReference();
        mQuery = mFirebaseFirestore.collection(mFirebaseUser.getUid()).orderBy(getString(R.string.field_when),
                Query.Direction.DESCENDING);

        setRecyclerWithQuery();


        /*
         * implementing swipe left to delete.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                DocumentReference document = mFirebaseFirestore.collection(mFirebaseUser.getUid())
                        .document((String) viewHolder.itemView.getTag());

                document.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MainActivity.this, R.string.delete_done, Toast.LENGTH_LONG).show();

                                if (mSearchAdapter != null) {
                                    mSearchAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                                    mSearchAdapter.notifyDataSetChanged();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this, R.string.failure_message, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        }).attachToRecyclerView(mRecyclerView);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mDiaryAdapter != null) {
            mDiaryAdapter.startListening();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mDiaryAdapter != null) mDiaryAdapter.stopListening();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult pConnectionResult) {

    }

    public void openAddActivity(View view) {
        Intent addActivityIntent = new Intent(this, AddActivity.class);
        startActivity(addActivityIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchView = menu.findItem(R.id.search);

        searchView.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem pMenuItem) {

                mToolbar.setBackgroundColor(Color.parseColor(mColorPrimaryDark));
                mFilterBarContainer.setVisibility(View.GONE);

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem pMenuItem) {

                mToolbar.setBackgroundColor(Color.parseColor(mColorPrimary));
                mFilterBarContainer.setVisibility(View.VISIBLE);

                mSearchAdapter = null;
                // reset the recycler view
                mQuery = mFirebaseFirestore.collection(mFirebaseUser.getUid()).orderBy(getString(R.string.field_when),
                        Query.Direction.DESCENDING);

                setRecyclerWithQuery();

                return true;
            }
        });

        ((SearchView) searchView.getActionView()).setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String pS) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String pS) {

                List<Thought> searchRes = new ArrayList<>();

                for (Thought i : mThoughts) {
                    if (i.getTitle().toLowerCase().contains(pS.toLowerCase()) ||
                            i.getDescription().toLowerCase().contains(pS.toLowerCase()))
                        searchRes.add(i);
                }

                mSearchAdapter = new SearchAdapter(searchRes);
                mRecyclerView.setAdapter(mSearchAdapter);

                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:

                // sign out user
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                break;


            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }


    // filter bar on click method
    public void filter(View view) {
        FragmentManager manager = getSupportFragmentManager();
        FilterFragment dialog = FilterFragment
                .newInstance(mFirebaseUser.getUid());
        dialog.show(manager, FilterFragment.TAG);
    }

    private void setRecyclerWithQuery() {

        /*
          hint from
          https://github.com/firebase/FirebaseUI-Android/issues/1131
         */
        FirestoreRecyclerOptions<Thought> options = new FirestoreRecyclerOptions.Builder<Thought>()
                .setQuery(mQuery, new SnapshotParser<Thought>() {
                    @NonNull
                    @Override
                    public Thought parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Thought parsedThought = snapshot.toObject(Thought.class);
                        mThoughts.add(parsedThought);
                        parsedThought.setIdentifier(snapshot.getId());
                        return parsedThought;
                    }
                }).build();

        // update the mDiaryAdapter and set it to the recycler mErrorView
        mDiaryAdapter = new DiaryAdapter(options, mErrorView, mReference, this);
        mDiaryAdapter.startListening();


        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mRecyclerView.setAdapter(mDiaryAdapter);
    }

    public void clearFilter(View view) {

        mIsFilterTv.setText(R.string.no_filter);
        mFilterTv.setText(R.string.showing_all_entries);

        mQuery = mFirebaseFirestore.collection(mFirebaseUser.getUid()).orderBy(getString(R.string.field_when),
                Query.Direction.DESCENDING);

        setRecyclerWithQuery();
    }

    @Override
    public void onThoughtClicked(Thought pModel, ImageView pImageView) {

        Intent intent = new Intent(this, ThoughtDetailActivity.class);
        intent.putExtra(ThoughtDetailActivity.VIEW_THOUGHT_EXTRA, pModel);
        intent.putExtra(ThoughtDetailActivity.TRANSITION_EXTRA, ViewCompat.getTransitionName(pImageView));

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, pImageView, ViewCompat.getTransitionName(pImageView));

        startActivity(intent, options.toBundle());
    }


    @Override
    public void onThoughtLongClicked(Thought pModel, ImageView pImageView) {
        Intent intent = new Intent(this, UpdateActivity.class);
        intent.putExtra(UpdateActivity.UPDATE_THOUGHT, pModel);
        intent.putExtra(UpdateActivity.TRANSITION_EXTRA, ViewCompat.getTransitionName(pImageView));

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, pImageView, ViewCompat.getTransitionName(pImageView));

        startActivity(intent, options.toBundle());
    }

    @Override
    public void onReceivedFilter(Thought pFilter) {

        // show the filter applied
        mIsFilterTv.setText(R.string.filter_applied);
        mFilterTv.setText(getString(R.string.filtered_by, pFilter.getTag()));

        // change the database query
        mQuery = mFirebaseFirestore.collection(mFirebaseUser.getUid())
                .whereEqualTo(getString(R.string.field_tag), pFilter.getTag())
                .orderBy(getString(R.string.field_when), Query.Direction.DESCENDING);

        setRecyclerWithQuery();
    }
}
