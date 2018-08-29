package com.moonshot.imageloaderdetector;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ImageView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = findViewById(R.id.iv);
        TestViewTarget viewTarget1 = new TestViewTarget(imageView);


        GlideApp.with(this)
                .asBitmap()
                .load("http://7xqi8h.com1.z0.glb.clouddn.com/blog/picture/bg-default.jpg")
                .override(1000, 1000)
                .into(imageView);
    }
}
