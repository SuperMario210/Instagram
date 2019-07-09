package com.example.instagram.callbacks;

import com.example.instagram.models.User;
import com.parse.ParseException;

public interface LoginCallback {
    /**
     * A call
     *
     * @param b The boolean value returned by the operation.
     */
    void done(User user, ParseException e);
}
