package com.example.instagram.fragments;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
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
import android.widget.EditText;
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
import com.example.instagram.models.Post;
import com.example.instagram.models.User;
import com.example.instagram.util.BitmapUtils;
import com.parse.ParseException;
import com.parse.ParseFile;

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
    private View mView;
    private OnFragmentClosedListener mClosedListener;

    @BindView(R.id.camera) CameraKitView mCameraView;
    @BindView(R.id.fl_options) FrameLayout flOptions;
    @BindView(R.id.iv_shutter) ImageView ivShutter;
    @BindView(R.id.iv_preview) ImageView ivPreview;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_share) TextView tvShare;
    @BindView(R.id.et_caption) EditText etCaption;

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

        mView = view;
        switchToPictureView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    /**
     * Initializes the view for taking a picture
     */
    private void switchToPictureView() {
        mCurrentState = ComposeState.PICTURE;

        // Setup the toolbar
        toolbar.setNavigationOnClickListener(v -> {
            mClosedListener.onFragmentClosed();
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
                mCameraView.onStop();
                mCameraView = null;
                Toast.makeText(getContext(), "Picture taken", Toast.LENGTH_SHORT).show();
                handleImageCapture(capturedImage);
            });

            switchToCaptionView();
        });

        // Start the camera
        mCameraView = mView.findViewById(R.id.camera);
        mCameraView.onStart();
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
        toolbar.setNavigationOnClickListener(v -> switchToPictureView());
        toolbar.setNavigationIcon(R.drawable.ic_vector_back);
        tvShare.setVisibility(View.VISIBLE);
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
        mBitmap = BitmapFactory.decodeByteArray(capturedImage, 0, capturedImage.length);

        // Crop and scale the bitmap
        mBitmap = BitmapUtils.cropToAspectRatio(mBitmap, 1, 1);
        mBitmap = BitmapUtils.scaleToFitWidth(mBitmap, 1024);

        // Display the bitmap in the preview image view
        ivPreview.setVisibility(View.VISIBLE);
        ivPreview.setImageBitmap(mBitmap);

        tvShare.setOnClickListener(v -> submitPost());
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

    /**
     * Creates a new post from the image currently stored in mBitmap and the caption written in
     * etCaption
     */
    private void submitPost() {
        // Configure byte output stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // Compress the image further
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

        // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
        File imageFile = getPhotoFileUri("temp.jpg");
        try {
            imageFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(imageFile);
            // Write the bytes of the bitmap to file
            fos.write(bytes.toByteArray());
            fos.close();

            // Create the parseFile image and caption
            ParseFile image = new ParseFile(imageFile);
            String caption = etCaption.getText().toString();

            // todo show some sort of loading indicator
            // Create the post
            Post.createPost(caption, image, User.getCurrentUser(), (ParseException e) -> {
                if(e == null) {
                    Toast.makeText(getContext(), "Post shared successfully", Toast.LENGTH_SHORT).show();
                    // todo: switch to home activity
                } else {
                    Log.e("ComposeFragment", "Could not save post", e);
                    Toast.makeText(getContext(), "Couldn't share post!", Toast.LENGTH_LONG);
                }
            });
        } catch (IOException e) {
            Log.e("ComposeFragment", "Couldn't write image to file", e);
            Toast.makeText(getContext(), "Couldn't share post!", Toast.LENGTH_LONG);
        }
    }

    public void setOnFragmentClosedListener(OnFragmentClosedListener listener) {
        mClosedListener = listener;
    }

    public void onBackPressed() {
        switch(mCurrentState) {
            case FILTER:
                break;
            case CAPTION:
                switchToPictureView();
                break;
            case PICTURE:
                mClosedListener.onFragmentClosed();
                break;
            default:
                break;
        }
    }

    public interface OnFragmentClosedListener {
        public void onFragmentClosed();
    }
}
