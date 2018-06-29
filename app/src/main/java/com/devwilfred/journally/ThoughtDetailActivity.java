package com.devwilfred.journally;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.common.net.InternetDomainName;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ThoughtDetailActivity extends AppCompatActivity {

    private Thought mThought;
    private TextView mDescriptionTv, mDateTv;
    private ImageView mThoughtImage;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thought_detail);

        mThought = (Thought) getIntent().getSerializableExtra("thought");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(mThought.getTitle());
        getSupportActionBar().setSubtitle(mThought.getTag());

        mDescriptionTv = findViewById(R.id.thought_description);
        mDescriptionTv.setText(mThought.getDescription());
        mDateTv = findViewById(R.id.thought_date);
        mDateTv.setText(DateUtils.getRelativeTimeSpanString(mThought.getWhen().getTime()));

        mThoughtImage = findViewById(R.id.diary_image);

        StorageReference reference = FirebaseStorage.getInstance().getReference();
        Glide.with(this)
                .using(new FirebaseImageLoader())
                .load(reference.child(mThought.getPhotoUrl()))
                .into(mThoughtImage);

        fab = findViewById(R.id.fab);

        // BEGIN_INCLUDE(detail_set_view_name)
        /**
         * Set the name of the view's which will be transition to, using the static values above.
         * This could be done in the layout XML, but exposing it via static variables allows easy
         * querying from other Activities
         */
        ViewCompat.setTransitionName(mThoughtImage, "detail:header:image");
        ViewCompat.setTransitionName(fab, "detail:fab:image");
        // END_INCLUDE(detail_set_view_name)

    }

    public void updateThought(View view) {
        Intent thoughtDetailIntent = new Intent(this, AddActivity.class);
        thoughtDetailIntent.putExtra("updateThought", mThought);
        startActivity(thoughtDetailIntent);
    }
}
