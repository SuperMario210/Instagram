package com.example.instagram.interfaces;

public interface VoidCallback extends Callback<Void> {
    /**
     * Override this function with the code you want to run after the operation is complete.
     *
     * @param v
     */
    @Override
    void done(Void v);
}
