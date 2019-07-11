package com.example.instagram.fragments;

import android.content.Context;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.R;
import com.example.instagram.activities.AuthenticateActivity;
import com.example.instagram.adapters.ProfileAdapter;
import com.example.instagram.adapters.SpaceItemDecoration;
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

public class ProfileFragment extends Fragment {
    private final static int COLUMN_COUNT = 3;

    private Unbinder mUnbinder;
    private User mUser;
    private OnFragmentInteractionListener mListener;
    private ProfileAdapter mProfileAdapter;
    private List<Post> mPosts;

    @BindView(R.id.rv_posts) RecyclerView rvPosts;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mUser = User.getCurrentUser();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getPosts();

        // Setup the recycler view adapter
        mPosts = new ArrayList<>();
        mProfileAdapter = new ProfileAdapter(mPosts, getContext(), mUser, (Void v) -> logOut());
        rvPosts.setAdapter(mProfileAdapter);

        // Setup the recycler view layout manager
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), COLUMN_COUNT, RecyclerView.VERTICAL, false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(mProfileAdapter.getItemViewType(position) == mProfileAdapter.FIRST_ITEM) {
                    return COLUMN_COUNT;
                } else {
                    return 1;
                }
            }
        });

//        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), COLUMN_COUNT);
        rvPosts.setLayoutManager(layoutManager);

        rvPosts.addItemDecoration(new SpaceItemDecoration(6, COLUMN_COUNT));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }

    private void logOut() {
        ParseUser.getCurrentUser().logOut();
        Intent i = new Intent(getContext(), AuthenticateActivity.class);
        startActivity(i);
        getActivity().finish();
    }

    private void getPosts() {
        final ParseQuery<Post> query = new Post.Query().getTop().withUser().withFavorites().whereEqualTo("user", mUser.getParseUser());
        query.orderByDescending("createdAt").findInBackground((List<Post> posts, ParseException e) -> {
            if(e == null) {
                mPosts.clear();
                mPosts.addAll(posts);
                mProfileAdapter.notifyDataSetChanged();
            } else {
                Log.e("MainActivity", "Couldn't load posts", e);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = data.getStringArrayListExtra(ImagePicker.EXTRA_IMAGE_PATH);
            handleGalleryUpload(mPaths.get(0));
        }
    }

    private void handleGalleryUpload(String filePath) {
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
}
