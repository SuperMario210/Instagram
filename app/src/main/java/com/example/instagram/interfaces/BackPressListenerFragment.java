package com.example.instagram.interfaces;

import androidx.fragment.app.Fragment;

/**
 * A fragment that performs an action when the back button is pressed
 */
public abstract class BackPressListenerFragment extends Fragment {
    /**
     * Called when the back button is pressed
     * @return true if the fragment handled the back press and false if the back press should be
     *         handled elsewhere
     */
    public abstract boolean onBackPressed();
}
