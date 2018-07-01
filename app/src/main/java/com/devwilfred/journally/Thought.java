package com.devwilfred.journally;

import android.os.Parcelable;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * this class represents an entry in the database
 */
public class Thought implements Serializable {

    private String mId;
    private String mTitle;
    private String mDescription;
    private String mTag;
    private String mPhotoUrl;
    private Date mWhen;

    @Exclude
    private String mIdentifier;


    public Thought() {

    }

    public Thought(String pmId, String pmTitle, String pmDescription, String pTag) {
        mId = pmId;
        mTitle = pmTitle;
        mDescription = pmDescription;
        mTag = "#" + pTag;
    }

    public Thought(String pmId, String pmTitle, String pmDescription, String pTag, String pPhotoUrl) {
        mId = pmId;
        mTitle = pmTitle;
        mDescription = pmDescription;
        mTag = pTag;
        mPhotoUrl = pPhotoUrl;
    }


    public String getIdentifier() {
        return mIdentifier;
    }

    public void setIdentifier(String pIdentifier) {
        mIdentifier = pIdentifier;
    }

    public String getId() {
        return mId;
    }

    public void setId(String pId) {
        mId = pId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String pTitle) {
        mTitle = pTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String pDescription) {
        mDescription = pDescription;
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String pTag) {
        mTag = pTag;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String pPhotoUrl) {
        mPhotoUrl = pPhotoUrl;
    }

    @ServerTimestamp
    public Date getWhen() {
        return mWhen;
    }

    public void setWhen(Date pWhen) {
        mWhen = pWhen;
    }
}
