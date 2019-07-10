package com.example.instagram;

import android.content.Context;
import android.util.AttributeSet;

import com.camerakit.CameraKitView;

public class CameraKit extends CameraKitView {
    public CameraKit(Context context) {
        super(context);
    }

    public CameraKit(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraKit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void captureImage(ImageCallback callback) {
        super.captureImage(callback);
    }
}
