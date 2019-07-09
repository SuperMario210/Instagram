package com.example.instagram.callbacks;

public interface BooleanCallback extends Callback<Boolean> {
    /**
     * Override this function with the code you want to run after the operation is complete.
     *
     * @param b The boolean value returned by the operation.
     */
    @Override
    void done(Boolean b);
}
