package com.example.instagram.models;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import com.example.instagram.util.DateFormatter;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

@ParseClassName("Comment")
public class Comment extends ParseObject {
    private static final String KEY_USER = "user";
    private static final String KEY_POST = "post";
    private static final String KEY_TEXT = "text";

    public static Comment createComment(User user, Post post, String text, final SaveCallback callback) {
        final Comment newComment = new Comment();
        newComment.setUser(user.getParseUser());
        newComment.setPost(post);
        newComment.setText(text);
        newComment.saveInBackground(callback);

        return newComment;
    }

    public User getUser() {
        return new User(getParseUser(KEY_USER));
    }

    public SpannableStringBuilder getFormattedText() {
        String caption = getUser().getUsername() + " " + getText();
        SpannableStringBuilder builder = new SpannableStringBuilder(caption);
        builder.setSpan(new StyleSpan(Typeface.BOLD),
                0, getUser().getUsername().length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return builder;
    }

    public String getFormattedDate() {
        return DateFormatter.formatTimestamp(getCreatedAt());
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public Post getPost() {
        return (Post) getParseObject(KEY_POST);
    }

    public void setPost(Post post) {
        put(KEY_POST, post);
    }

    public void setText(String text) {
        put(KEY_TEXT, text);
    }

    public String getText() {
        return getString(KEY_TEXT);
    }

    public static class Query extends ParseQuery<Comment> {
        public Query() {
            super(Comment.class);
            orderByDescending("createdAt");
        }

        public Comment.Query getTop() {
            setLimit(20);
            return this;
        }

        public Comment.Query withUser() {
            include("user");
            return this;
        }

        public Comment.Query withPost() {
            include("post");
            return this;
        }

        public Comment.Query forPost(Post post) {
            whereEqualTo("post", post);
            return this;
        }
    }
}
