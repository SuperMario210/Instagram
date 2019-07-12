package com.example.instagram.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.R;
import com.example.instagram.activities.AuthenticateActivity;
import com.example.instagram.adapters.ProfileAdapter;
import com.example.instagram.adapters.SpaceItemDecoration;
import com.example.instagram.interfaces.BackPressListenerFragment;
import com.example.instagram.models.Post;
import com.example.instagram.models.User;
import com.example.instagram.util.BitmapUtils;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends BackPressListenerFragment {
    // The number of columns in the posts grid
    private final static int COLUMN_COUNT = 3;

    private Unbinder mUnbinder;
    private ProfileAdapter mProfileAdapter;
    private boolean mIsCurrentUser;
    private User mUser; // The user whose profile we are displaying
    private List<Post> mPosts; // The list of the user's posts

    @BindView(R.id.rv_posts) RecyclerView rvPosts;

    /**
     * @param user the user whose information to display in the fragment
     * @param isCurrentUser is the user the current user
     */
    public ProfileFragment(User user, boolean isCurrentUser) {
        mUser = user;
        mIsCurrentUser = isCurrentUser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup the recycler view adapter
        mPosts = new ArrayList<>();
        mProfileAdapter = new ProfileAdapter(mPosts, getContext(), mUser, mIsCurrentUser,
                (Void v) -> logOut());
        rvPosts.setAdapter(mProfileAdapter);

        // Setup the recycler view layout manager
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), COLUMN_COUNT,
                RecyclerView.VERTICAL, false);
        // Override the span size lookup to make the first element of the recycler view wider
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // Make the first element take up the entire width
                if(mProfileAdapter.getItemViewType(position) == ProfileAdapter.FIRST_ITEM) {
                    return COLUMN_COUNT;
                } else {
                    return 1;
                }
            }
        });
        rvPosts.setLayoutManager(layoutManager);

        // Add spacing in between elements of the grid
        rvPosts.addItemDecoration(new SpaceItemDecoration(6, COLUMN_COUNT));

        // Load the user's posts
        getUserPosts();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    /**
     * Handle intent results for uploading a profile picture from the gallery
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH);
            handleMediaUpload(mPaths.get(0));
        }
    }

    /**
     * Gets posts that the user has authored and stores them in the mPosts list
     */
    private void getUserPosts() {
        // Build a query to get the current user's posts
        final ParseQuery<Post> query = new Post.Query()
                .withUser()
                .forUser(mUser);

        query.findInBackground((List<Post> posts, ParseException e) -> {
            if(e == null) {
                // Add the posts to the user's post list
                mPosts.clear();
                mPosts.addAll(posts);
                mProfileAdapter.notifyDataSetChanged();
            } else {
                Log.e("MainActivity", "Couldn't load posts", e);
            }
        });
    }

    /**
     * Log out the current user the go back to the authentication activity
     */
    private void logOut() {
        ParseUser.getCurrentUser().logOut();
        Intent i = new Intent(getContext(), AuthenticateActivity.class);
        startActivity(i);
        getActivity().finish();
    }

    /**
     * Format and upload a new profile picture selected by the media selector
     * @param filePath
     */
    private void handleMediaUpload(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        bitmap = BitmapUtils.scaleToFitWidth(bitmap, 1024);
        bitmap = BitmapUtils.cropToAspectRatio(bitmap, 1, 1);

        mProfileAdapter.setProfileImage(bitmap);

        // Configure byte output stream
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // Compress the image further
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

        // Create a new file for the resized bitmap (`getPhotoFileUri` defined above)
        File imageFile = BitmapUtils.getPhotoFileUri("temp.jpg", getContext());
        try {
            imageFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(imageFile);
            // Write the bytes of the bitmap to file
            fos.write(bytes.toByteArray());
            fos.close();

            // Create the parseFile image and caption
            ParseFile image = new ParseFile(imageFile);
            mUser.setProfileImage(image, (e) -> {
                if(e == null) {
                    mUser.getParseUser().saveInBackground();
                } else {
                    Log.e("ProfileFragment", "Couldn't upload profile image", e);
                    Toast.makeText(getContext(), "Couldn't upload profile image", Toast.LENGTH_LONG);
                }
            });


        } catch (IOException e) {
            Log.e("ProfileFragment", "Couldn't write image to file", e);
            Toast.makeText(getContext(), "Couldn't save image", Toast.LENGTH_LONG);
        }

    }

    /**
     * Let the main activity handle the back press
     * @return false
     */
    @Override
    public boolean onBackPressed() {
        return false;
    }
}
