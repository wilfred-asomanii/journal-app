package com.devwilfred.journally;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class AddActivity extends AppCompatActivity {

    EditText tag, title, description;
    private FirebaseFirestore mFirebaseFirestore;
    private String collectionPath;
    private String imageUrl;
    private Uri filePath;
    ImageView diaryImage;
    byte[] imageData;
    public static final int IMAGE_REQUEST = 1022;
    FirebaseStorage mStorage;
    FloatingActionButton fab;
    //private FloatingActionButton hiddenfab;
    Button addPhotoBtn;
    File compressedFile;
    Thought thought;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("ASDasd");
        getSupportActionBar().setSubtitle("ASDasd");

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mStorage = FirebaseStorage.getInstance();

        tag = findViewById(R.id.tag_edit_text);
        title = findViewById(R.id.title_edit_text);

        description = findViewById(R.id.description_edit_text);
        diaryImage = findViewById(R.id.diary_image);

        collectionPath = getIntent().getStringExtra("userUid");



        //hiddenfab = findViewById(R.id.hidden_fab);
        fab = findViewById(R.id.fab);

        addPhotoBtn = findViewById(R.id.add_photo);

        /*if (!fab.isShown()) {
           // hiddenfab.show();
        } else hiddenfab.hide();
*/

        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (Math.abs(verticalOffset) > 200) fab.show();
                else fab.hide();

            }
        });

    }

    public void selectImage(View view) {
        Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
        imageIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(imageIntent, "Choose a photo"), IMAGE_REQUEST);
    }

    public void addToDiary(View view) {

        thought = new Thought();
        if (filePath != null) {
            uploadImage(imageData);
            thought.setPhotoUrl(imageUrl);
        }

        thought.setDescription(description.getText().toString());
        thought.setTitle(getString(R.string.tag_template, title.getText().toString()));
        thought.setTag(tag.getText().toString());
        thought.setWhen(new Date());


        mFirebaseFirestore.collection(collectionPath).document(title.getText().toString()).set(thought).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> pTask) {
                if (pTask.isSuccessful()) {
                    Toast.makeText(AddActivity.this, "Success", Toast.LENGTH_LONG).show();
                } else
                    Toast.makeText(AddActivity.this, "fail", Toast.LENGTH_LONG).show();

            }
        });



    }

    protected void uploadImage(byte[] pData) {

        mStorage.getReference().child(collectionPath + "/" + title.getText().toString()).putBytes(pData)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot pTaskSnapshot) {
                        Toast.makeText(AddActivity.this, "Image uploaded", Toast.LENGTH_LONG).show();
                        openDetail();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception pE) {
                Toast.makeText(AddActivity.this, "image fail", Toast.LENGTH_LONG).show();
            }
        });
        imageUrl = "wilfred/" + title.getText().toString();
    }

    void openDetail() {
        Intent thoughtDetailIntent = new Intent(this, ThoughtDetailActivity.class);
        thoughtDetailIntent.putExtra("thought", thought);

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
                new Pair<View, String>(diaryImage, "detail:header:image"));

        // Now we can start the Activity, providing the activity options as a bundle
        ActivityCompat.startActivity(this, thoughtDetailIntent, activityOptions.toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null &&
                data.getData() != null) {
            filePath = data.getData();
            addPhotoBtn.setVisibility(View.GONE);

            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                diaryImage.setImageBitmap(bitmap);
            } catch (IOException pE) {
                pE.printStackTrace();
            }

            diaryImage.setDrawingCacheEnabled(true);
            diaryImage.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) diaryImage.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            imageData = baos.toByteArray();

        }
    }
}
