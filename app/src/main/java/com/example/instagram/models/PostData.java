package com.example.instagram.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data type for quickly directly accessing tweets by id and sorting tweets by id.  Accomplishes
 * this through a combination of a hashmap and an arraylist.  The TweetDataHolder is also not
 * associated with the application as a whole so any class with access to a context can access it.
 *
 * The TweetDataHolder also stores and retrieves tweet data from room.
 */
public class PostData {
    private Map<String, Post> mPosts;
    private List<String> mIds;

    public PostData() {
        mPosts = new HashMap<>();
        mIds = new ArrayList<>();
    }

    /**
     * Removes all data from the holder, used for refreshing
     */
    public void clearData() {
        mPosts.clear();
        mIds.clear();
    }

    /**
     * Gets a post by id
     * @param id the id of the post to get
     * @return post with the specified id
     */
    public Post getPostById(String id) {
        return mPosts.get(id);
    }

    /**
     * Gets a post by index
     * @param index the index of the post to get
     * @return the post at the specified index
     */
    public Post getPostByIndex(int index) {
        return mPosts.get(mIds.get(index));
    }

    /**
     * Adds a post to the data holder
     * @param index the index at which to store the post
     * @param post the post to store
     * @return the index of the post
     */
    public int addPost(int index, Post post) {
        mPosts.put(post.getObjectId(), post);
        mIds.add(index, post.getObjectId());
        return index;
    }

    /**
     * Adds a post to the end of the data holder
     * @param post the post to store
     * @return the index of the post
     */
    public int addPost(Post post) {
        return addPost(mIds.size(), post);
    }

    /**
     * @return the date of the oldest post currently stored
     */
    public Date getOldestDate() {
        if(mIds.isEmpty())
            return new Date();
        return mPosts.get(mIds.get(mIds.size() - 1)).getCreatedAt();
    }

    /**
     * @return The number of posts stored
     */
    public int size() {
        return mIds.size();
    }
}
