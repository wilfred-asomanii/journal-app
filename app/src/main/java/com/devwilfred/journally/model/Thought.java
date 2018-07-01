/**
 * Copyright 2018 Wilfred Agyei Asomani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.devwilfred.journally.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * this class represents an entry in the database
 */
public class Thought implements Serializable {

    private String mTitle;
    private String mDescription;
    private String mTag;
    private String mPhotoUrl;
    private Date mWhen;

    @Exclude
    private String mIdentifier;


    public Thought() {

    }

    /**
     * an ID of sorts
     * @return a unique identifier for each entry
     */
    public String getIdentifier() {
        return mIdentifier;
    }

    /**
     * set the identifier
     * @param pIdentifier the identifier
     */
    public void setIdentifier(String pIdentifier) {
        mIdentifier = pIdentifier;
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

    /**
     * get the date this entry was made
     * @return a date (from firestore)
     */
    @ServerTimestamp
    public Date getWhen() {
        return mWhen;
    }

    public void setWhen(Date pWhen) {
        mWhen = pWhen;
    }
}
