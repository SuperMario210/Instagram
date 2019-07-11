package com.example.instagram.models;

import android.util.Log;

import androidx.annotation.Nullable;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@ParseClassName("Post")
public class Post extends ParseObject {
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_USER = "user";
    private static final String KEY_FAVORITES = "favorites";

    public static Post createPost(String description, ParseFile image,
                                  User user, final SaveCallback callback) {
        final Post newPost = new Post();
        newPost.setDescription(description);
        newPost.setUser(user.getParseUser());

        // Set the image the save the post after the image has finished saving
        newPost.setImage(image, e -> {
            if(e == null) {
                // If the image is set successfully then save the post
                newPost.saveInBackground(callback);
            } else {
                // If there is an error then pass it along to the callback
                callback.done(e);
            }
        });

        return newPost;
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseFile getImage() {
       return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image, @Nullable SaveCallback callback) {
        // Make sure we save the image to parse before we try to set it
        image.saveInBackground((ParseException e) -> {
            if(e == null) {
                // After the image has finished saving, set it
                put(KEY_IMAGE, image);
            } else {
                Log.e("Post", "Could not save image", e);
            }

            // If there is a callback defined then run it
            if (callback != null) callback.done(e);
        });
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public List<ParseUser> getFavorites() {
        List<ParseUser> users = getList(KEY_FAVORITES);
        if(users == null)
            return new ArrayList<>();
        return users;
    }

    public void addFavorite(ParseUser user) {
        addAllUnique(KEY_FAVORITES, java.util.Collections.singleton(user));
    }

    public void removeFavorite(ParseUser user) {
        removeAll(KEY_FAVORITES, java.util.Collections.singleton(user));
    }

    public boolean isLikedByUser(ParseUser user) {
        List<ParseUser> users = getFavorites();
        for(ParseUser u : users)
            if(u.getObjectId().equals(user.getObjectId())) return true;
        return false;
    }

    public int getNumFavorites() {
        return getFavorites().size();
    }

    public String getNumFavoritesString() {
        int numFaves = getNumFavorites();
        if(numFaves == 1)
            return String.format(Locale.getDefault(), "%d like", numFaves);
        return String.format(Locale.getDefault(), "%d likes", numFaves);
    }

    public static String formatDate(Date date) {
        String timeFormat = (date.getYear() != new Date().getYear()) ? "MMM dd yyyy, h:mma" : "MMM dd, h:mma";
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.ENGLISH);
        return sdf.format(date).replace("AM", "am").replace("PM", "pm");
    }

    public static class Query extends ParseQuery<Post> {
        public Query() {
            super(Post.class);
        }

        public Query getTop() {
            setLimit(20);
            return this;
        }

        public Query withUser() {
            include("user");
            return this;
        }

        public Query byId(String postId) {
            whereEqualTo("objectId", postId);
            return this;
        }

        public Query withFavorites() {
            include("favorites");
            return this;
        }
    }
}
