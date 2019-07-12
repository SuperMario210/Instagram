package com.example.instagram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.instagram.ParseApp;
import com.example.instagram.R;
import com.example.instagram.fragments.ComposeFragment;
import com.example.instagram.fragments.HomeFragment;
import com.example.instagram.fragments.ProfileFragment;
import com.example.instagram.interfaces.BackPressListenerFragment;
import com.example.instagram.models.Post;
import com.example.instagram.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements ComposeFragment.OnFragmentClosedListener, HomeFragment.OnProfileOpenedListener {
    // Fragments that this activity holds
    HomeFragment mHomeFragment;
    ComposeFragment mComposeFragment;
    ProfileFragment mProfileFragment;
    FragmentManager mFragmentManager;
    BackPressListenerFragment mCurrentFragment;

    @BindView(R.id.fl_container) FrameLayout flContainer;
    @BindView(R.id.bottom_navigation) BottomNavigationView bnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // Initialize fragments
        mHomeFragment = new HomeFragment();
        mComposeFragment = new ComposeFragment();
        mProfileFragment = new ProfileFragment(User.getCurrentUser(), true);
        mFragmentManager = getSupportFragmentManager();

        initializeBottomNavigation();
    }

    /**
     * Sets up a listener to swap fragments when the bottom navigation is selected
     */
    private void initializeBottomNavigation() {
        bnMenu.setOnNavigationItemSelectedListener(item -> {
            // Switch to the correct fragment based on which item was selected
            if(item.getItemId() == R.id.action_home) {
                mCurrentFragment = mHomeFragment;
            } else if(item.getItemId() == R.id.action_compose) {
                mCurrentFragment = mComposeFragment;
            } else {
                mCurrentFragment = mProfileFragment;
            }
            mFragmentManager.beginTransaction().replace(R.id.fl_container, mCurrentFragment).commit();
            return true;
        });
        bnMenu.setSelectedItemId(R.id.action_home);
    }

    @Override
    public void onBackPressed() {
        // Let the active fragment handle the back press
        if(mCurrentFragment.onBackPressed()) {
            return;
        }

        if(mFragmentManager.getBackStackEntryCount() > 0) {
            // If the active fragment does not handle the back press, try to pop the back stack
            mFragmentManager.popBackStack();
        } else {
            // If the back stack is empty, pass the back press up
            super.onBackPressed();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Pass on activity result to fragments.  This is mainly necessary for the media selector
        // in the compose activity.
        mCurrentFragment.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Called when the compose fragment is exited without submitting a post
     */
    @Override
    public void onComposeCancel() {
        mFragmentManager.beginTransaction().replace(R.id.fl_container, mHomeFragment).commit();
        bnMenu.setSelectedItemId(R.id.action_home);
    }

    /**
     * Called when the compose fragment sucessfully submits a post
     * @param post the post submitted by the compose fragment
     */
    @Override
    public void onComposeComplete(Post post) {
        ((ParseApp) getApplication()).getPosts().addPost(0, post);
        mFragmentManager.beginTransaction().replace(R.id.fl_container, mHomeFragment).commit();
        bnMenu.setSelectedItemId(R.id.action_home);
    }

    /**
     * Handle a profile being opened
     * @param user the user whose profile is being opened
     */
    @Override
    public void onProfileOpened(User user) {
        boolean isCurrentUser = user.getObjectId().equals(User.getCurrentUser().getObjectId());
        if(isCurrentUser) {
            // Switch to the profile tab on the bottom navigation
            bnMenu.setSelectedItemId(R.id.action_profile);
        } else {
            // Create a new profile fragment for the user and add it to the back stack
            ProfileFragment fragment = new ProfileFragment(user, false);
            mFragmentManager.beginTransaction()
                    .replace(R.id.fl_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
