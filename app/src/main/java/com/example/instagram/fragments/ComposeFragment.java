package com.example.instagram.fragments;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.instagram.R;
import com.example.instagram.interfaces.BackPressListenerFragment;
import com.example.instagram.models.GlideApp;
import com.example.instagram.models.Post;
import com.example.instagram.models.User;
import com.example.instagram.util.BitmapUtils;
import com.example.instagram.util.CameraXUtil;
import com.parse.ParseException;
import com.parse.ParseFile;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

public class ComposeFragment extends BackPressListenerFragment {
    // Request code for camera permission
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 10;

    private Unbinder mUnbinder;
    private Bitmap mBitmap;
    private OnFragmentClosedListener mListener;
    private Post mPost;

    // CameraX variables
    private ImageCapture mImageCapture;
    private Preview mPreview;

    @BindView(R.id.camera) TextureView mCameraView;
    @BindView(R.id.fl_options) FrameLayout flOptions;
    @BindView(R.id.iv_shutter) ImageView ivShutter;
    @BindView(R.id.iv_preview) ImageView ivPreview;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_share) TextView tvShare;
    @BindView(R.id.et_caption) EditText etCaption;
    @BindView(R.id.iv_gallery) ImageView ivGallery;
    @BindView(R.id.pb_loading) ProgressBar pbLoading;

    /**
     * This enum holds the different states that the compose fragment can be in (taking a picture,
     * adding a filter, captioning the picture).  Used for keeping handling back presses
     */
    public enum ComposeState {
        PICTURE,
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

        // Check camera permissions and request them if necessary
        if (isCameraPermissionGranted()) {
            mCameraView.post(this::startCamera);
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                     new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        // Every time the provided texture view changes, recompute the layout to ensure that the
        // preview displays correctly
        mCameraView.addOnLayoutChangeListener(
                (v, i1, i2, i3, i4, i5, i6, i7, i8) -> updateTransform());

        // Setup the view
        switchToPictureView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (isCameraPermissionGranted()) {
                mCameraView.post(this::startCamera);
            } else {
                Toast.makeText(getContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onBackPressed() {
        if(mCurrentState == ComposeState.CAPTION) {
            switchToPictureView();
        } else if(mCurrentState == ComposeState.PICTURE) {
            mListener.onComposeCancel();
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH);
            handleGalleryUpload(mPaths.get(0));
            switchToCaptionView();
        }
    }

    /**
     * Override the onAttach function to keep a reference to the attached context for interfacing
     * with the activity this fragment is attached to
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentClosedListener) {
            mListener = (OnFragmentClosedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Initialize the camera preview and image capture
     */
    private void startCamera() {
        int width = mCameraView.getWidth();
        int height = mCameraView.getHeight();

        mImageCapture = CameraXUtil.getImageCapture(width, height);
        mPreview = CameraXUtil.getPreview(width, height);

        // Create a preview listener to update the viewfinder
        mPreview.setOnPreviewOutputUpdateListener((output) -> {
            ViewGroup parent = (ViewGroup) mCameraView.getParent();
            parent.removeView(mCameraView);
            parent.addView(mCameraView, 0);

            mCameraView.setSurfaceTexture(output.getSurfaceTexture());
            updateTransform();
        });

        // Bind use cases to lifecycle
        CameraX.bindToLifecycle(this, mPreview, mImageCapture);
    }

    /**
     * Checks if the camera permission is granted
     * @return true if the camera permission is granted, false if not
     */
    private boolean isCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Updates the transformation to ensure the camera preview is displayed correctly
     */
    private void updateTransform() {
        Matrix matrix = CameraXUtil.getTransformMatrix(
                mCameraView.getWidth(),
                mCameraView.getHeight(),
                mCameraView.getDisplay().getRotation());
        mCameraView.setTransform(matrix);
    }

    /**
     * Initializes the view for taking a picture
     */
    private void switchToPictureView() {
        mCurrentState = ComposeState.PICTURE;

        // Setup the toolbar
        toolbar.setNavigationOnClickListener(v -> mListener.onComposeCancel());
        toolbar.setNavigationIcon(R.drawable.ic_vector_close);
        tvShare.setOnClickListener(null);
        tvShare.setVisibility(View.INVISIBLE);
        pbLoading.setVisibility(View.GONE);

        // Set up the shutter
        flOptions.setVisibility(View.INVISIBLE);
        ivPreview.setVisibility(View.GONE);
        ivShutter.setVisibility(View.VISIBLE);
        ivGallery.setVisibility(View.VISIBLE);
        GlideApp.with(getContext())
                .load(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.white_90_transparent)))
                .transform(new CircleCrop())
                .into(ivShutter);

        // Handle shutter press
        ivShutter.setOnClickListener(v -> {
            switchToCaptionView();
            mImageCapture.takePicture(BitmapUtils.getPhotoFileUri("temp", getContext()), new ImageCapture.OnImageSavedListener() {
                @Override
                public void onImageSaved(@NonNull File file) {
                    handleImageCapture(file);
                }

                @Override
                public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                    Toast.makeText(getContext(), "Error saving image", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Select gallery image
        ivGallery.setOnClickListener(v -> new ImagePicker.Builder(getActivity())
            .mode(ImagePicker.Mode.GALLERY)
            .compressLevel(ImagePicker.ComperesLevel.MEDIUM)
            .directory(ImagePicker.Directory.DEFAULT)
            .extension(ImagePicker.Extension.PNG)
            .scale(600, 600)
            .allowMultipleImages(false)
            .enableDebuggingMode(true)
            .build());
    }

    /**
     * Initializes the view for adding a caption
     */
    private void switchToCaptionView() {
        mCurrentState = ComposeState.CAPTION;

        // Hide the shutter and reveal the options layout
        ivShutter.setVisibility(View.GONE);
        flOptions.setVisibility(View.VISIBLE);
        ivGallery.setVisibility(View.GONE);

        // Create the circular reveal animation for the options layout
        int x = flOptions.getMeasuredWidth() / 2;
        int y = flOptions.getMeasuredHeight() / 2;
        int endRadius = (int) Math.hypot((double) x, (double) y);
        Animator anim = ViewAnimationUtils.createCircularReveal(flOptions, x, y, 0, endRadius);
        anim.start();

        // Setup the toolbar
        toolbar.setNavigationOnClickListener(v -> switchToPictureView());
        toolbar.setNavigationIcon(R.drawable.ic_vector_back);
        tvShare.setVisibility(View.VISIBLE);
    }

    /**
     * Handles an image captured by CameraX.  Scales, crops, and displays the image
     *
     * @param imageFile the file of the captured image returned by cameraX
     */
    private void handleImageCapture(final File imageFile) {
        int rotation = 0;
        try {
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            rotation = exif.getRotationDegrees();
        } catch (IOException e) {
            Log.e("ComposeFragment", "Couldn't get exif data", e);
        }

        // Decode the image into a bitmap
        mBitmap = BitmapFactory.decodeFile(imageFile.getPath());

        // Rotate, crop and scale the bitmap
        mBitmap = BitmapUtils.rotateBitmap(mBitmap, rotation);
        mBitmap = BitmapUtils.cropToAspectRatio(mBitmap, 1, 1);
        mBitmap = BitmapUtils.scaleToFitWidth(mBitmap, 1024);

        // Display the bitmap in the preview image view
        ivPreview.setVisibility(View.VISIBLE);
        ivPreview.setImageBitmap(mBitmap);

        tvShare.setOnClickListener(v -> {
            tvShare.setVisibility(View.INVISIBLE);
            pbLoading.setVisibility(View.VISIBLE);
            submitPost();
        });
    }

    /**
     * Handles an upload from the media picker
     */
    private void handleGalleryUpload(String filePath) {
        mBitmap = BitmapFactory.decodeFile(filePath);
        mBitmap = BitmapUtils.scaleToFitWidth(mBitmap, 1024);

        // Display the bitmap in the preview image view
        ivPreview.setVisibility(View.VISIBLE);
        ivPreview.setImageBitmap(mBitmap);

        tvShare.setOnClickListener(v -> {
            tvShare.setVisibility(View.INVISIBLE);
            pbLoading.setVisibility(View.VISIBLE);
            submitPost();
        });
    }

    /**
     * Creates a new post from the image currently stored in mBitmap and the caption written in
     * etCaption
     */
    private void submitPost() {
        // Configure byte output stream and compress the image further
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

        // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
        File imageFile = BitmapUtils.getPhotoFileUri("temp.jpg", getContext());
        try {
            // Write the bytes of the bitmap to file
            imageFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(bytes.toByteArray());
            fos.close();

            // Create the parseFile image and caption
            ParseFile image = new ParseFile(imageFile);
            String caption = etCaption.getText().toString();

            // Create the post
            mPost = Post.createPost(caption, image, User.getCurrentUser(), (ParseException e) -> {
                if(e == null) {
                    Toast.makeText(getContext(), "Post shared successfully", Toast.LENGTH_SHORT).show();
                    etCaption.setText("");
                    mListener.onComposeComplete(mPost);
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

    /**
     * Interface for interacting with the activity the fragment is attached to
     */
    public interface OnFragmentClosedListener {
        /**
         * Called when the compose fragment is closed without submitting a post
         */
        void onComposeCancel();

        /**
         * Called when the compose fragment successfully submits a post
         * @param post the post submitted by the compose fragment
         */
        void onComposeComplete(Post post);
    }
}
