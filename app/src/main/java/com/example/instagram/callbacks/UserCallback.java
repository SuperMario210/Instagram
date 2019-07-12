package com.example.instagram.callbacks;

import com.example.instagram.models.User;

public interface UserCallback extends Callback<User> {
    /**
     * Override this function with the code you want to run after the operation is complete.
     *
     * @param v
     */
    @Override
    void done(User v);
}
