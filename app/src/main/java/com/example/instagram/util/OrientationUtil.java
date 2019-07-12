package com.example.instagram.util;

import android.view.Surface;

import androidx.exifinterface.media.ExifInterface;

public class OrientationUtil {
    /**
     * Converts an exif orientation into degrees
     * @param exifOrientation the exif orientation
     * @return the exif orientation in degrees
     */
    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    /**
     * Converts an display orientation into degrees
     * @param displayRotation the display orientation
     * @return the display orientation in degrees
     */
    public static int displayRotationToDegrees(int displayRotation) {
        if (displayRotation == Surface.ROTATION_90) { return 90; }
        else if (displayRotation == Surface.ROTATION_180) {  return 180; }
        else if (displayRotation == Surface.ROTATION_270) {  return 270; }
        return 0;
    }
}
