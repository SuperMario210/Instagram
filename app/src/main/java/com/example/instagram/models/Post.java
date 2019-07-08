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

@ParseClassName("Post")
public class Post extends ParseObject {
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_USER = "user";

    public static Post createPost(String description, ParseFile image,
                                  ParseUser user, final SaveCallback callback) {
        final Post newPost = new Post();
        newPost.setDescription(description);
        newPost.setUser(user);

        // Set the image the save the post after the image has finished saving
        newPost.setImage(image, e -> newPost.saveInBackground(callback));

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

                // If there is a callback defined then run it
                if (callback != null) callback.done(null);
            } else {
                Log.e("Post", "Could not save image", e);
            }
        });
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
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
    }
}