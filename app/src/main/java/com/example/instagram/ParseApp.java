package com.example.instagram;

import android.app.Application;

import com.parse.Parse;

public class ParseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initializeParse();
    }

    /**
     * Initializes the Parse SDK
     */
    private void initializeParse() {
        final Parse.Configuration config = new Parse.Configuration.Builder(this)
                .applicationId("instagram")
                .clientKey(getResources().getString(R.string.master_key))
                .server("http://mjruiz-instagram.herokuapp.com/parse")
                .build();
        Parse.initialize(config);
    }
}
