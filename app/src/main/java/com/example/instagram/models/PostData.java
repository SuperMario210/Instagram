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
     * Gets a post by uid
     * @param id
     * @return post with the specified uid
     */
    public Post getPostById(String id) {
        return mPosts.get(id);
    }

    public Post getPostByIndex(int index) {
        return mPosts.get(mIds.get(index));
    }

    /**
     * Adds a post to the data holder
     * @param post the post to store
     * @return the index of the post in descending order
     */
//    public int addPost(Post post) {
//        mPosts.put(post.getObjectId(), post);
//        for(int i = 0; i < mIds.size(); i++) {
//            if(post.uid > mIds.get(i)) {
//                mIds.add(i, post.uid);
//                return i;
//            } else if(post.uid == mIds.get(i)) {
//                return i;
//            }
//        }
//        mIds.add(post.uid);
//        return mIds.size() - 1;
//    }

    public int addPost(int index, Post post) {
        mPosts.put(post.getObjectId(), post);
        mIds.add(index, post.getObjectId());
        return index;
    }

    public int addPost(Post post) {
        return addPost(mIds.size(), post);
    }

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
