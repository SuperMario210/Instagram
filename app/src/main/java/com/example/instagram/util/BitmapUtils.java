package com.example.instagram.util;

import android.graphics.Bitmap;

public class BitmapUtils
{
    /**
     * Scales a bitmap to maintain its aspect ratio given a desired width
     * @param b the bitmap to scale
     * @param width the desired width to scale the bitmap to
     * @return a new bitmap with the desired width
     */
    public static Bitmap scaleToFitWidth(Bitmap b, int width) {
        float factor = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
    }


    /**
     * Scales a bitmap to maintain its aspect ratio given a desired height
     * @param b the bitmap to scale
     * @param height the desired height to scale the bitmap to
     * @return a new bitmap with the desired height
     */
    public static Bitmap scaleToFitHeight(Bitmap b, int height) {
        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    }

    /**
     * Crops a bitmap to obtain the desired aspect ratio (width:height)
     * @param b the bitmap to crop
     * @param width the desired width relative to the height
     * @param height the desired height relative to the width
     * @return a new cropped bitmap with the desired aspect ratio
     */
    public static Bitmap cropToAspectRatio(Bitmap b, int width, int height) {
        int widthRatio = b.getWidth() / width;
        int heightRatio = b.getHeight() / height;
        if(widthRatio < heightRatio) {
            return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getWidth() * height / width);
        } else {
            return Bitmap.createBitmap(b, 0, 0, b.getHeight() * width / height, b.getHeight());
        }
    }

}
