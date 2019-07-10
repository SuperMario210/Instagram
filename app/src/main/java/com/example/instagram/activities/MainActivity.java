package com.example.instagram.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.instagram.R;
import com.example.instagram.fragments.ComposeFragment;
import com.example.instagram.fragments.HomeFragment;
import com.example.instagram.models.Post;
import com.example.instagram.models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    Fragment mHomeFragment, mComposeFragment, mProfileFragment;
    FragmentManager mFragmentManager;

    @BindView(R.id.fl_container) FrameLayout flContainer;
    @BindView(R.id.bottom_navigation) BottomNavigationView bnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // Initialize fragments
        mComposeFragment = new ComposeFragment();
        mHomeFragment = new HomeFragment();
        mFragmentManager = getSupportFragmentManager();

        initializeBottomNavigation();
//        requestPerms();
//        testQuery();
    }

    private void initializeBottomNavigation() {
        bnMenu.setOnNavigationItemSelectedListener(item -> {
            Fragment newFragment = mHomeFragment;
            switch (item.getItemId()) {
                case R.id.action_home:
                    newFragment = mHomeFragment;
                    break;
                case R.id.action_compose:
                    newFragment = mComposeFragment;
                    break;
                case R.id.action_profile:
                default:
                    Toast.makeText(this, "Profile Fragment", Toast.LENGTH_SHORT).show();
//                    newFragment = mProfileFragment;
                    break;
            }
            mFragmentManager.beginTransaction().replace(R.id.fl_container, newFragment).commit();
            return true;
        });

        bnMenu.setSelectedItemId(R.id.action_home);
    }

    private void requestPerms() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            testPost();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 0) {
            testPost();
        }
    }

    private void testPost() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "test.jpg");
        ParseFile file = new ParseFile(f);

        Post.createPost("test post", file, User.getCurrentUser(), (ParseException e) -> {
            if(e == null) {
                Toast.makeText(this, "Post saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("MainActivity", "Could not save post", e);
            }
        });
    }

    public void testQuery() {
        final Post.Query query = new Post.Query().getTop().withUser();
        query.findInBackground((List<Post> posts, ParseException e) -> {
            if(e == null) {
                for(int i = 0; i < posts.size(); i++) {
                    Log.d("MainActivity",
                            String.format("Post[%d] = %s, username = %s",
                                    i,
                                    posts.get(i).getDescription(),
                                    posts.get(i).getUser().getUsername()));
                }
            } else {
                Log.e("MainActivity", "Couldn't load posts", e);
            }
        });
    }
}
