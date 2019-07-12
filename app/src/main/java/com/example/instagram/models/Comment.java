package com.example.instagram.models;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import com.example.instagram.util.DateUtil;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * This class holds the data for a comment made on a post.  Each comment stores the user who made
 * the comment, the post the comment was made on, and the text of the comment.
 */
@ParseClassName("Comment")
public class Comment extends ParseObject {
    private static final String KEY_USER = "user";
    private static final String KEY_POST = "post";
    private static final String KEY_TEXT = "text";

    /**
     * Creates a new comment from the required fields
     */
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

    /**
     * Prepends the text of a comment with the bolded username of the user who made the comment
     * @return the formatted comment
     */
    public SpannableStringBuilder getFormattedText() {
        String caption = getUser().getUsername() + " " + getText();
        SpannableStringBuilder builder = new SpannableStringBuilder(caption);
        builder.setSpan(new StyleSpan(Typeface.BOLD),
                0, getUser().getUsername().length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        return builder;
    }

    /**
     * @return a relative timestamp to when the comment was created
     */
    public String getFormattedDate() {
        return DateUtil.formatTimestamp(getCreatedAt());
    }

    public static class Query extends ParseQuery<Comment> {
        public Query() {
            super(Comment.class);
            orderByDescending("createdAt");
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
