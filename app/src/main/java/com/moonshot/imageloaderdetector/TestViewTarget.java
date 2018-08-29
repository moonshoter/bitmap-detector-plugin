package com.moonshot.imageloaderdetector;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.request.target.ImageViewTarget;
import com.moonshot.library.imagedetector.GlideHelper;


public class TestViewTarget extends ImageViewTarget<Bitmap> {
    public TestViewTarget(ImageView view) {
        super(view);
    }

    @Override
    protected void setResource(Bitmap resource) {
        view.setImageBitmap(resource);
        GlideHelper.detect(this, resource);

    }
}
