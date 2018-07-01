package com.devwilfred.journally;

import android.widget.ImageView;

public interface ThoughtClickListener {


    void onThoughtClicked(Thought pModel, ImageView pImageView);

    void onThoughtLongClicked(Thought pModel, ImageView pImageView);
}
