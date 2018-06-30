package com.devwilfred.journally;



public class Filter {
    private String mTag;

    public Filter(String pTag) {
        mTag = pTag;
    }

    public Filter() {
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String pTag) {
        mTag = pTag;
    }
}
