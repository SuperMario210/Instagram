package com.example.instagram.interfaces;

import com.example.instagram.models.User;

public interface UserCallback extends Callback<User> {
    /**
     * Override this function with the code you want to run after the operation is complete.
     *
     * @param u
     */
    @Override
    void done(User u);
}
