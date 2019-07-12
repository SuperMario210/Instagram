package com.example.instagram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.instagram.ParseApp;
import com.example.instagram.R;
import com.example.instagram.fragments.ComposeFragment;
import com.example.instagram.fragments.HomeFragment;
import com.example.instagram.fragments.ProfileFragment;
import com.example.instagram.models.Post;
import com.example.instagram.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements ComposeFragment.OnFragmentClosedListener, HomeFragment.OnProfileOpenedListener {
    HomeFragment mHomeFragment;
    ComposeFragment mComposeFragment;
    ProfileFragment mProfileFragment;
    FragmentManager mFragmentManager;
    Fragment mCurrentFragment;

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

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ComposeFragment) {
            ComposeFragment composeFragment = (ComposeFragment) fragment;
            composeFragment.setOnFragmentClosedListener(this);
        }
    }

    private void initializeBottomNavigation() {
        bnMenu.setOnNavigationItemSelectedListener(item -> {
            mCurrentFragment = mHomeFragment;
            switch (item.getItemId()) {
                case R.id.action_home:
                    mCurrentFragment = mHomeFragment;
                    break;
                case R.id.action_compose:
                    mCurrentFragment = mComposeFragment;
                    break;
                case R.id.action_profile:
                default:
                    Toast.makeText(this, "Profile Fragment", Toast.LENGTH_SHORT).show();
                    mCurrentFragment = mProfileFragment;
                    break;
            }
            mFragmentManager.beginTransaction().replace(R.id.fl_container, mCurrentFragment).commit();
            return true;
        });

        bnMenu.setSelectedItemId(R.id.action_home);
    }

    @Override
    public void onBackPressed() {
        if(mCurrentFragment == mComposeFragment) {
            mComposeFragment.onBackPressed();
        } else {
            mFragmentManager.popBackStack();
        }
    }

    @Override
    public void onFragmentClosed() {
        mFragmentManager.beginTransaction().replace(R.id.fl_container, mHomeFragment).commit();
        bnMenu.setSelectedItemId(R.id.action_home);
    }

    @Override
    public void onPostSubmitted(Post post) {
        ((ParseApp) getApplication()).getPosts().addPost(0, post);
        mFragmentManager.beginTransaction().replace(R.id.fl_container, mHomeFragment).commit();
        bnMenu.setSelectedItemId(R.id.action_home);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCurrentFragment.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onProfileOpened(User user) {
        if(!user.getObjectId().equals(User.getCurrentUser().getObjectId())) {
            ProfileFragment fragment = new ProfileFragment(user, false);
            mFragmentManager.beginTransaction()
                    .replace(R.id.fl_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            bnMenu.setSelectedItemId(R.id.action_profile);
        }
    }
}
