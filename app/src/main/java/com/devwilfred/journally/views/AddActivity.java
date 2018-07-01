package com.devwilfred.journally.views;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.devwilfred.journally.R;
import com.devwilfred.journally.model.Thought;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;


public class AddActivity extends AppCompatActivity {

    private EditText mTagText, mTitleText, mDescriptionText;
    private ImageView diaryImage;
    private Button addPhotoBtn;

    private FirebaseFirestore mFirebaseFirestore;
    private String collectionPath;
    private String imageUrl;
    private Uri filePath;
    byte[] imageData;
    FirebaseStorage mStorage;
    Thought thought;
    ProgressDialog progressDialog;

    public static final int IMAGE_REQUEST = 1022;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.write_down);
        }


        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

        mTagText = findViewById(R.id.tag_edit_text);
        mTitleText = findViewById(R.id.title_edit_text);

        mDescriptionText = findViewById(R.id.description_edit_text);
        diaryImage = findViewById(R.id.diary_image);

        // get the user's uid from shared preferences
        // that will be a unique document path for the user in the database
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        collectionPath = sharedPreferences.getString(getString(R.string.user_uid_preference), "default");

        addPhotoBtn = findViewById(R.id.add_photo);

    }

    public void selectImage(View view) {
        Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        imageIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(imageIntent, getString(R.string.image_chooser_title)),
                IMAGE_REQUEST);
    }

    public void addToDiary(View view) {

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

        Toast.makeText(this, R.string.loading_message, Toast.LENGTH_LONG).show();

        // create new entry
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.loading_message));
            progressDialog.setMessage("Just A Second");

            thought = new Thought();
            if (filePath != null) {
                progressDialog.show();
                uploadImage(imageData);
                thought.setPhotoUrl(imageUrl);
            }

            thought.setDescription(mDescriptionText.getText().toString());
            thought.setTitle(mTitleText.getText().toString());
            thought.setTag(getString(R.string.tag_template, mTagText.getText().toString()));
            thought.setWhen(new Date());


            mFirebaseFirestore.collection(collectionPath).document(mTitleText.getText().toString())
                    .set(thought).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> pTask) {
                    if (pTask.isSuccessful()) {
                        // show entry on detail view
                        if (thought.getPhotoUrl() == null) {
                            openDetail(thought);
                            finish();
                        }
                    } else
                        Toast.makeText(AddActivity.this, R.string.failure_message, Toast.LENGTH_LONG).show();

                }
            });
    }

    protected void uploadImage(byte[] pData) {

        // upload image to firebase storage
        mStorage.getReference().child(collectionPath + "/" + mTitleText.getText().toString()).putBytes(pData)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot pTaskSnapshot) {

                        progressDialog.dismiss();
                        openDetail(thought);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception pE) {
                Toast.makeText(AddActivity.this, R.string.failure_message, Toast.LENGTH_LONG).show();
            }
        });
        imageUrl = collectionPath + "/" + mTitleText.getText().toString();
    }

    void openDetail(Thought pThought) {
        Intent intent = new Intent(this, ThoughtDetailActivity.class);
        intent.putExtra(ThoughtDetailActivity.VIEW_THOUGHT_EXTRA, pThought);
        intent.putExtra(ThoughtDetailActivity.TRANSITION_EXTRA, ThoughtDetailActivity.TRANSITION_VALUE);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, diaryImage, ThoughtDetailActivity.TRANSITION_VALUE);


        startActivity(intent, options.toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null &&
                data.getData() != null) {

            filePath = data.getData();
            addPhotoBtn.setVisibility(View.GONE);

            // set the received image onto the image mErrorView and compress it
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                /*diaryImage.setDrawingCacheEnabled(true);
                diaryImage.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) diaryImage.getDrawable()).getBitmap();*/
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArray);

                imageData = byteArray.toByteArray();

                diaryImage.setImageBitmap(bitmap);
            } catch (IOException pE) {
                pE.printStackTrace();
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
