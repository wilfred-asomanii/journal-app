package com.devwilfred.journally.views;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.devwilfred.journally.R;
import com.devwilfred.journally.model.Thought;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ThoughtDetailActivity extends AppCompatActivity {

    public static final String TRANSITION_EXTRA = "transition";
    public static final String TRANSITION_VALUE = "diary_image";
    public static final String VIEW_THOUGHT_EXTRA = "thought";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thought_detail);

        Thought thought = (Thought) getIntent().getSerializableExtra(VIEW_THOUGHT_EXTRA);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(thought.getTitle());
        }

        TextView descriptionTv = findViewById(R.id.thought_description);
        descriptionTv.setText(thought.getDescription());
        TextView dateTv = findViewById(R.id.thought_date);
        dateTv.setText(DateUtils.getRelativeTimeSpanString(thought.getWhen().getTime()));

        ImageView thoughtImage = findViewById(R.id.diary_image);

        StorageReference reference = FirebaseStorage.getInstance().getReference();

        if (thought.getPhotoUrl() != null) {
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(reference.child(thought.getPhotoUrl()))
                    .into(thoughtImage);
        }
        if (getIntent().getStringExtra(TRANSITION_EXTRA) != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String imageTransitionName = getIntent().getStringExtra(TRANSITION_EXTRA);
                thoughtImage.setTransitionName(imageTransitionName);
            }


        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
