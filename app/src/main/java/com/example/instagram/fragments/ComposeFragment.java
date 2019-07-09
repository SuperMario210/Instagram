package com.example.instagram.fragments;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.camerakit.CameraKitView;
import com.example.instagram.R;
import com.example.instagram.models.GlideApp;
import com.example.instagram.util.BitmapUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ComposeFragment extends Fragment {
    private static final String APP_TAG = "fbu_instagram";

    private Unbinder mUnbinder;
    private Bitmap mBitmap;

    @BindView(R.id.camera) CameraKitView mCameraView;
    @BindView(R.id.fl_options) FrameLayout flOptions;
    @BindView(R.id.iv_shutter) ImageView ivShutter;
    @BindView(R.id.iv_preview) ImageView ivPreview;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_share) TextView tvShare;

    /**
     * This enum holds the different states that the compose fragment can be in (taking a picture,
     * adding a filter, captioning the picture)
     */
    public enum ComposeState {
        PICTURE,
        FILTER,
        CAPTION
    }
    private ComposeState mCurrentState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_compose, container, false);

        // Bind views using butterknife
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCameraView.onStart();
        switchToPictureView();
    }

    /**
     * Initializes the view for taking a picture
     */
    private void switchToPictureView() {
        mCurrentState = ComposeState.PICTURE;

        // Setup the toolbar
        toolbar.setNavigationOnClickListener(v -> {
            // todo: return to previous fragment
        });
        toolbar.setNavigationIcon(R.drawable.ic_vector_close);
        tvShare.setVisibility(View.GONE);

        // Set up the shutter
        flOptions.setVisibility(View.INVISIBLE);
        ivPreview.setVisibility(View.GONE);
        ivShutter.setVisibility(View.VISIBLE);
        GlideApp.with(getContext())
                .load(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.white_90_transparent)))
                .transform(new CircleCrop())
                .into(ivShutter);

        // Handle shutter press
        ivShutter.setOnClickListener(v -> {
            // Capture the image using CameraKit
            mCameraView.captureImage((CameraKitView cameraKitView, final byte[] capturedImage) -> {
                mCameraView.onPause();
                Toast.makeText(getContext(), "Picture taken", Toast.LENGTH_SHORT).show();
                handleImageCapture(capturedImage);
            });

            switchToCaptionView();
        });

        // Start the camera
        mCameraView.onResume();
    }

    /**
     * Initializes the view for adding a caption
     */
    @TargetApi(21)
    private void switchToCaptionView() {
        mCurrentState = ComposeState.CAPTION;

        // Hide the shutter and reveal the options layout
        ivShutter.setVisibility(View.GONE);
        flOptions.setVisibility(View.VISIBLE);

        // Create the circular reveal animation for the options layout
        int x = flOptions.getMeasuredWidth() / 2;
        int y = flOptions.getMeasuredHeight() / 2;
        int startRadius = 0;
        int endRadius = (int) Math.hypot((double) x, (double) y);
        Animator anim = ViewAnimationUtils.createCircularReveal(flOptions, x, y, startRadius, endRadius);
        anim.start();

        // Setup the toolbar
        toolbar.setNavigationOnClickListener(v -> {
            switchToPictureView();
        });
        toolbar.setNavigationIcon(R.drawable.ic_vector_back);
        tvShare.setVisibility(View.VISIBLE);
        tvShare.setOnClickListener(v -> {
            // todo: submit post!
            Toast.makeText(getContext(), "Submit post!", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Returns the File for a photo stored on disk given the fileName
     * @param fileName the filename of the file to store the photo in
     * @return a file to store the photo in
     */
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    /**
     * Handles an image captured by cameraKit.  Scales, crops, and displays the image
     *
     * @param capturedImage the captured image returned by cameraKit
     */
    private void handleImageCapture(final byte[] capturedImage) {
        // Decode the image into a bitmap
        Bitmap bitmap = BitmapFactory.decodeByteArray(capturedImage, 0, capturedImage.length);

        // Crop and scale the bitmap
        bitmap = BitmapUtils.cropToAspectRatio(bitmap, 1, 1);
        mBitmap = BitmapUtils.scaleToFitWidth(bitmap, 1024);

        // Display the bitmap in the preview image view
        ivPreview.setImageBitmap(mBitmap);
        ivPreview.setVisibility(View.VISIBLE);

        // Configure byte output stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // Compress the image further
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
        File imageFile = getPhotoFileUri("temp");

        try {
            imageFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(imageFile);
            // Write the bytes of the bitmap to file
            fos.write(bytes.toByteArray());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mCurrentState == ComposeState.PICTURE)
            mCameraView.onResume();
    }
    @Override
    public void onPause() {
        mCameraView.onPause();
        super.onPause();
    }
    @Override
    public void onStop() {
        mCameraView.onStop();
        super.onStop();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mCameraView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
