package com.example.instagram.models;

import android.util.Log;

import com.example.instagram.callbacks.BooleanCallback;
import com.example.instagram.callbacks.LoginCallback;
import com.parse.ParseClassName;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

@ParseClassName("User")
public class User {
    private static final String KEY_PROFILE_IMAGE = "profile_image";
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

    public String getUsername() {
        return mUser.getUsername();
    }

    public void setEmail(String email) {
        mUser.setEmail(email);
    }

    public String getEmail() {
        return mUser.getEmail();
    }

    public void setPassword(String password) {
        mUser.setPassword(password);
    }

    public void signUpInBackground(SignUpCallback callback) {
        mUser.signUpInBackground(callback);
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
                Log.e("User", "DISASTER", e);
                // TODO: handle error
            }
        });
        return query;
    }

    public static User getCurrentUser() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        if(parseUser == null) return null;
        return new User(parseUser);
    }

    public static void logInInBackground(String username, String password, LoginCallback callback) {
        ParseUser.logInInBackground(username, password,
                (user, e) -> callback.done(new User(user), e));
    }

}
