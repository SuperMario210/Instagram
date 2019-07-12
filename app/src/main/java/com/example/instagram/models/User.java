package com.example.instagram.models;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.instagram.interfaces.BooleanCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

/**
 * Utility class that wraps a user object in order to make it easier to perform certain operations
 * on the user such as setting a profile image
 */
public class User {
    private static final String DEFAULT_PROFILE = "https://instagram.fhel3-1.fna.fbcdn.net/vp/f7f9ca3c7981731efe2de600e0b99c46/5DA39AF1/t51.2885-19/44884218_345707102882519_2446069589734326272_n.jpg?_nc_ht=instagram.fhel3-1.fna.fbcdn.net";
    private static final String KEY_PROFILE_IMAGE = "profileImage";
    private static final String KEY_BANNER_IMAGE = "banner_image";
    private static final String KEY_PROFILE_DESCRIPTION = "profile_description";

    private final ParseUser mUser;

    public User() {
        mUser = new ParseUser();
    }

    public User(ParseUser user) {
        mUser = user;
    }

    public void setUsername(String username) {
        mUser.setUsername(username);
    }

    public String getObjectId() {
        return mUser.getObjectId();
    }

    public String getUsername() {
        return mUser.getUsername();
    }

    public void setPassword(String password) {
        mUser.setPassword(password);
    }

    public void signUpInBackground(SignUpCallback callback) {
        mUser.signUpInBackground(callback);
    }

    public void setProfileImage(ParseFile image, @Nullable SaveCallback callback) {
        // Make sure we save the image to parse before we try to set it
        image.saveInBackground((ParseException e) -> {
            if(e == null) {
                // After the image has finished saving, set it
                mUser.put(KEY_PROFILE_IMAGE, image);
            } else {
                Log.e("Post", "Could not save image", e);
            }

            // If there is a callback defined then run it
            if (callback != null) callback.done(e);
        });
    }

    public ParseFile getProfileImage() {
        return mUser.getParseFile(KEY_PROFILE_IMAGE);
    }

    public String getProfileUrl() {
        return getProfileImage() == null ? DEFAULT_PROFILE : getProfileImage().getUrl();
    }

    public ParseUser getParseUser() {
        return mUser;
    }

    public static ParseQuery isUsernameAvailable(String username, BooleanCallback callback) {
        ParseQuery<ParseUser> query = ParseUser.getQuery().whereEqualTo("username", username);
        query.findInBackground((users, e) -> {
            if(e == null) {
                callback.done(users.isEmpty());
            } else {
                callback.done(false);
                Log.e("User", "Could not query users", e);
            }
        });
        return query;
    }

    public static User getCurrentUser() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        if(parseUser == null) return null;
        return new User(parseUser);
    }

}
