package com.example.instagram.callbacks;

public interface VoidCallback extends Callback<Void> {
    /**
     * Override this function with the code you want to run after the operation is complete.
     *
     * @param v
     */
    @Override
    void done(Void v);
}
