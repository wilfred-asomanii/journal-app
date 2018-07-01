/**
 * Copyright 2018 Wilfred Agyei Asomani
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.devwilfred.journally.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.devwilfred.journally.R;
import com.devwilfred.journally.model.Thought;
import com.devwilfred.journally.presenter.DataPresenter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


/**
 * update the details of an entry
 * currently, entries with images cannot have their images changed or removed
 */
public class UpdateActivity extends AppCompatActivity {

    public static final String TRANSITION_EXTRA = "transition";
    public static final String UPDATE_THOUGHT = "update_thought";
    FirebaseStorage mStorage;
    Thought updateThought;
    private EditText mTagText, mTitleText, mDescriptionText;
    private ImageView diaryImage;
    private String collectionPath;
    private DataPresenter mDataPresenter;
    private CoordinatorLayout mParentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        updateThought = (Thought) getIntent().getSerializableExtra(UPDATE_THOUGHT);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(updateThought.getTitle());
        }

        mParentView = findViewById(R.id.update_container);
        diaryImage = findViewById(R.id.diary_image);

        mStorage = FirebaseStorage.getInstance();
        StorageReference reference = FirebaseStorage.getInstance().getReference();
        mDataPresenter = DataPresenter.getInstance();


        if (updateThought.getPhotoUrl() != null) {
            Glide.with(this)
                    .using(new FirebaseImageLoader())
                    .load(reference.child(updateThought.getPhotoUrl()))
                    .into(diaryImage);
        }

        // for shared element transition
        if (getIntent().getStringExtra(TRANSITION_EXTRA) != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                String imageTransitionName = getIntent().getStringExtra(TRANSITION_EXTRA);
                diaryImage.setTransitionName(imageTransitionName);
            }


        }

        mTagText = findViewById(R.id.tag_edit_text);
        mTitleText = findViewById(R.id.title_edit_text);
        mDescriptionText = findViewById(R.id.description_edit_text);

        mTitleText.setText(updateThought.getTitle());
        mTagText.setText(updateThought.getTag());
        mDescriptionText.setText(updateThought.getDescription());


        // get the user's uid from shared preferences
        // that will be a unique document path for the user in the database
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        collectionPath = sharedPreferences.getString(getString(R.string.user_uid_preference), "default");


    }

    // update
    public void updateDiary(View view) {


        TextInputLayout titleTextInput = findViewById(R.id.title_text_input_layout);
        titleTextInput.setErrorEnabled(true);
        TextInputLayout tagTextInput = findViewById(R.id.tag_text_input_layout);
        tagTextInput.setErrorEnabled(true);
        TextInputLayout descriptionTextInput = findViewById(R.id.description_text_input_layout);
        descriptionTextInput.setErrorEnabled(true);


        // none of the fields must be empty
        if (mTitleText.getText().length() < 1 || mTagText.getText().length() < 1
                || mDescriptionText.getText().length() < 1) {
            titleTextInput.setError(getString(R.string.empty_field_error));
            tagTextInput.setError(getString(R.string.empty_field_error));
            descriptionTextInput.setError(getString(R.string.empty_field_error));

            return;
        }

        Snackbar.make(mParentView, R.string.loading_message, Snackbar.LENGTH_LONG).show();

        // update an entry
        String oldTitle = updateThought.getTitle();
        updateThought.setTitle(mTitleText.getText().toString());
        updateThought.setTag(mTagText.getText().toString());
        updateThought.setDescription(mDescriptionText.getText().toString());


        mDataPresenter.updateThought(collectionPath, oldTitle, updateThought)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> pTask) {
                        if (pTask.isSuccessful()) {


                            openDetail(updateThought);
                            finish();
                        } else
                            Snackbar.make(mParentView, R.string.failure_message, Snackbar.LENGTH_LONG).show();
                    }
                });
    }


    private void openDetail(Thought pThought) {
        Intent intent = new Intent(this, ThoughtDetailActivity.class);
        intent.putExtra(ThoughtDetailActivity.VIEW_THOUGHT_EXTRA, pThought);
        intent.putExtra(ThoughtDetailActivity.TRANSITION_EXTRA, ThoughtDetailActivity.TRANSITION_VALUE);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, diaryImage, ThoughtDetailActivity.TRANSITION_VALUE);


        startActivity(intent, options.toBundle());
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
