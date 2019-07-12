package com.example.instagram.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import java.io.File;

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

    public static Bitmap rotateBitmap(Bitmap b, int degrees) {
        Matrix matrix = new Matrix();
        if (degrees != 0) {
            matrix.preRotate(degrees);
        }
        return Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
    }

    /**
     * Returns the File for a photo stored on disk given the fileName
     * @param fileName the filename of the file to store the photo in
     * @return a file to store the photo in
     */
    public static File getPhotoFileUri(String fileName, Context context) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "fbu_instagram");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d("BitmapUtils", "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }
}
