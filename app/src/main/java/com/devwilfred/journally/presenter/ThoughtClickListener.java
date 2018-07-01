package com.devwilfred.journally.presenter;

import android.widget.ImageView;

import com.devwilfred.journally.model.Thought;

public interface ThoughtClickListener {


    void onThoughtClicked(Thought pModel, ImageView pImageView);

    void onThoughtLongClicked(Thought pModel, ImageView pImageView);
}
