package com.example.instagram.util;

import android.graphics.Matrix;
import android.util.Rational;
import android.util.Size;

import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;

public class CameraXUtil {
    public static Matrix getTransformMatrix(int width, int height, int rotation) {
        Matrix matrix = new Matrix();

        // Compute the center of the view finder
        float centerX = width / 2f;
        float centerY = height / 2f;

        // Correct preview output to account for display rotation
        float rotationDegrees =
                OrientationUtil.displayRotationToDegrees(rotation);
        matrix.postRotate(-rotationDegrees, centerX, centerY);

        return matrix;
    }

    public static Preview getPreview(int width, int height) {
        // Create configuration object for the preview use case
        PreviewConfig config = new PreviewConfig.Builder()
                .setTargetAspectRatio(new Rational(width, height))
                .setTargetResolution(new Size(width, height))
                .build();
        return new Preview(config);
    }

    public static ImageCapture getImageCapture(int width, int height) {
        // Create configuration object for the image capture use case
        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setTargetAspectRatio(new Rational(width, height))
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build();
        return new ImageCapture(imageCaptureConfig);
    }

}
