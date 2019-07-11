package com.example.instagram.fragments;

import android.Manifest;
import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
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
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.instagram.R;
import com.example.instagram.models.GlideApp;
import com.example.instagram.models.Post;
import com.example.instagram.models.User;
import com.example.instagram.util.BitmapUtils;
import com.example.instagram.util.OrientationUtils;
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
    // Directory to save images in
    private static final String APP_TAG = "fbu_instagram";
    // Request code for camera permission
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 10;

    private Unbinder mUnbinder;
    private Bitmap mBitmap;
    private View mView;
    private OnFragmentClosedListener mClosedListener;

    // CameraX variables
    private ImageCapture mImageCapture;
    private Preview mPreview;
    private Preview.OnPreviewOutputUpdateListener mPreviewListener;


    @BindView(R.id.camera) TextureView mCameraView;
    @BindView(R.id.fl_options) FrameLayout flOptions;
    @BindView(R.id.iv_shutter) ImageView ivShutter;
    @BindView(R.id.iv_preview) ImageView ivPreview;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_share) TextView tvShare;
    @BindView(R.id.et_caption) EditText etCaption;
    @BindView(R.id.pb_loading) ProgressBar pbLoading;

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
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onStop() {
        super.onStop();
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

    /**
     * Updates the transformation to ensure the camera preview is displayed correctly
     */
    @TargetApi(21)
    private void updateTransform() {
        Matrix matrix = new Matrix();

        // Compute the center of the view finder
        float centerX = mCameraView.getWidth() / 2f;
        float centerY = mCameraView.getHeight() / 2f;

        // Correct preview output to account for display rotation
        float rotationDegrees =
                OrientationUtils.displayRotationToDegrees(mCameraView.getDisplay().getRotation());
        matrix.postRotate(-rotationDegrees, centerX, centerY);

        // Finally, apply transformations to our TextureView
        mCameraView.setTransform(matrix);
    }

    /**
     * Initialize the camera preview and image capture
     */
    @TargetApi(21)
    private void startCamera() {
        int width = mCameraView.getWidth();
        int height = mCameraView.getHeight();

        // Create configuration object for the preview use case
        PreviewConfig config = new PreviewConfig.Builder()
                .setTargetAspectRatio(new Rational(width, height))
                .setTargetResolution(new Size(width, height))
                .build();
        mPreview = new Preview(config);

        // Create a preview listener to update the viewfinder
        mPreviewListener = (output) -> {
            ViewGroup parent = (ViewGroup) mCameraView.getParent();
            parent.removeView(mCameraView);
            parent.addView(mCameraView, 0);

            mCameraView.setSurfaceTexture(output.getSurfaceTexture());
            updateTransform();
        };
        mPreview.setOnPreviewOutputUpdateListener(mPreviewListener);

        // Create configuration object for the image capture use case
        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setTargetAspectRatio(new Rational(width, height))
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .build();
        mImageCapture = new ImageCapture(imageCaptureConfig);

        // Bind use cases to lifecycle
        CameraX.bindToLifecycle(this, mPreview, mImageCapture);

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
        tvShare.setOnClickListener(null);
        tvShare.setVisibility(View.INVISIBLE);
        pbLoading.setVisibility(View.GONE);

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
            mImageCapture.takePicture(getPhotoFileUri("temp"), new ImageCapture.OnImageSavedListener() {
                @Override
                public void onImageSaved(@NonNull File file) {
                    Toast.makeText(getContext(), "Image saved sucessfully", Toast.LENGTH_SHORT).show();
                    handleImageCapture(file);
                }

                @Override
                public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                    Toast.makeText(getContext(), "Error saving image", Toast.LENGTH_SHORT).show();
                }
            });

            switchToCaptionView();
        });
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
    private File getPhotoFileUri(String fileName) {
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
     * @param imageFile the file of the captured image returned by cameraX
     */
    private void handleImageCapture(final File imageFile) {
        int rotationInDegrees = 0;
        try {
            ExifInterface exif = new ExifInterface(imageFile.getPath());
            rotationInDegrees = OrientationUtils.exifToDegrees(
                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Matrix matrix = new Matrix();
        if (rotationInDegrees != 0) {matrix.preRotate(rotationInDegrees);}

        // Decode the image into a bitmap
        mBitmap = BitmapFactory.decodeFile(imageFile.getPath());
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);

        // Crop and scale the bitmap
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
     * Checks if the camera permission is granted
     * @return true if the camera permission is granted, false if not
     */
    private boolean isCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
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

            // Create the post
            Post.createPost(caption, image, User.getCurrentUser(), (ParseException e) -> {
                if(e == null) {
                    Toast.makeText(getContext(), "Post shared successfully", Toast.LENGTH_SHORT).show();
                    etCaption.setText("");
                    mClosedListener.onFragmentClosed();
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
        void onFragmentClosed();
    }
}
