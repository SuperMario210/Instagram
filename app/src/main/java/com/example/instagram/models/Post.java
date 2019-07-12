package com.example.instagram.models;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
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

/**
 * This class holds the data for a post.  Each post stores the user who made the post, the the post
 * description, the image associated with the post, and an array of users who favorited the post.
 */
@ParseClassName("Post")
public class Post extends ParseObject {
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_USER = "user";
    private static final String KEY_FAVORITES = "favorites";

    /**
     * Creates a new post from the required fields
     */
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

    public User getUser() {
        return new User(getParseUser(KEY_USER));
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    private List<ParseUser> getFavorites() {
        List<ParseUser> users = getList(KEY_FAVORITES);
        if(users == null)
            return new ArrayList<>();
        return users;
    }

    public void addFavorite(User user) {
        addAllUnique(KEY_FAVORITES, java.util.Collections.singleton(user.getParseUser()));
    }

    public void removeFavorite(User user) {
        removeAll(KEY_FAVORITES, java.util.Collections.singleton(user.getParseUser()));
    }

    public boolean isFavoritedByUser(User user) {
        List<ParseUser> users = getFavorites();
        for(ParseUser u : users)
            if(u.getObjectId().equals(user.getObjectId())) return true;
        return false;
    }

    public int getNumFavorites() {
        return getFavorites().size();
    }

    /**
     * Prepends the description of a post with the bolded username of the user who made the post
     * @return the formatted description
     */
    public SpannableStringBuilder getFormattedDescription() {
        String caption = getUser().getUsername() + " " + getDescription();
        SpannableStringBuilder builder = new SpannableStringBuilder(caption);
        builder.setSpan(new StyleSpan(Typeface.BOLD),
                0, getUser().getUsername().length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return builder;
    }

    /**
     * Appends "likes" (or "like" if there is only 1 like) to the number of favorites for displaying
     * @return the formatted number of likes
     */
    public String formatNumFavorites() {
        int numFaves = getNumFavorites();
        if(numFaves == 1)
            return String.format(Locale.getDefault(), "%d like", numFaves);
        return String.format(Locale.getDefault(), "%d likes", numFaves);
    }

    /**
     * @return the post creation date in Month Date Year, Time format
     */
    public static String formatDate(Date date) {
        String timeFormat = (date.getYear() != new Date().getYear()) ? "MMM dd yyyy, h:mma" : "MMM dd, h:mma";
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.ENGLISH);
        return sdf.format(date).replace("AM", "am").replace("PM", "pm");
    }

    public static class Query extends ParseQuery<Post> {
        public Query() {
            super(Post.class);
            orderByDescending("createdAt");
        }

        public Query getTop(int limit) {
            setLimit(limit);
            return this;
        }

        public Query forUser(User user) {
            whereEqualTo("user", user.getParseUser());
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

        public Query olderThan(Date date) {
            whereLessThan("createdAt", date);
            return this;
        }
    }
}
