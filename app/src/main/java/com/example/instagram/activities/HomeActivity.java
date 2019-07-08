package com.example.instagram.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.instagram.R;
import com.example.instagram.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        requestPerms();
        testQuery();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 0) {
            testPost();
        }
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

    private void testPost() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "test.jpg");
        ParseFile file = new ParseFile(f);

        Post.createPost("test post", file, ParseUser.getCurrentUser(), (ParseException e) -> {
            if(e == null) {
                Toast.makeText(this, "Post saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("HomeActivity", "Could not save post", e);
            }
        });
    }

    public void testQuery() {
        final Post.Query query = new Post.Query().getTop().withUser();
        query.findInBackground((List<Post> posts, ParseException e) -> {
            if(e == null) {
                for(int i = 0; i < posts.size(); i++) {
                    Log.d("HomeActivity",
                            String.format("Post[%d] = %s, username = %s",
                                    i,
                                    posts.get(i).getDescription(),
                                    posts.get(i).getUser().getUsername()));
                }
            } else {
                Log.e("HomeActivity", "Couldn't load posts", e);
            }
        });
    }
}
