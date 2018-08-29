package com.moonshot.library.imagedetector;


public class DetectorInfo {
    public String mLoadActivity;
    public String mUrl;
    public int mViewHeight;
    public int mViewWidth;
    public int mImgWidth;
    public int mImgHeight;

    public boolean isTooLarge;

    public boolean isLegalUrl;


    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
