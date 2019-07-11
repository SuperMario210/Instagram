package com.example.instagram.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Favorite")
public class Favorite extends ParseObject {
    private static final String KEY_USER = "user";
    private static final String KEY_POST = "post";

    public static Favorite createFavorite(User user, Post post) {
        final Favorite newFavorite = new Favorite();
        newFavorite.setUser(user.getParseUser());
        newFavorite.setPost(post);

        return newFavorite;
    }
    public ParseUser getUser() {
        return getParseUser(KEY_USER);
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

    public static class Query extends ParseQuery<Favorite> {
        public Query() {
            super(Favorite.class);
        }

        public Favorite.Query getTop() {
            setLimit(20);
            return this;
        }

        public Favorite.Query withUser() {
            include("user");
            return this;
        }

        public Favorite.Query withPost() {
            include("post");
            return this;
        }
    }
}
