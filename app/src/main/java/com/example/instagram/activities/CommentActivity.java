package com.example.instagram.activities;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.instagram.ParseApp;
import com.example.instagram.R;
import com.example.instagram.adapters.CommentAdapter;
import com.example.instagram.models.Comment;
import com.example.instagram.models.GlideApp;
import com.example.instagram.models.Post;
import com.example.instagram.models.User;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentActivity extends AppCompatActivity {
    private CommentAdapter mCommentAdapter;
    private List<Comment> mComments;
    private User mUser;
    private Post mPost;
    private Comment mComment;

    @BindView(R.id.iv_profile) ImageView ivProfile;
    @BindView(R.id.iv_outline) ImageView ivOutline;
    @BindView(R.id.iv_border) ImageView ivBorder;
    @BindView(R.id.iv_profile_comment) ImageView ivProfileComment;
    @BindView(R.id.tv_description) TextView tvDescription;
    @BindView(R.id.rv_comments) RecyclerView rvComments;
    @BindView(R.id.et_comment) EditText etComment;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

        // Setup the recycler view adapter
        mUser = User.getCurrentUser();
        mComments = new ArrayList<>();
        mCommentAdapter = new CommentAdapter(mComments, this);
        rvComments.setAdapter(mCommentAdapter);
        mPost = ((ParseApp) getApplication()).getPosts().getPostById(
                getIntent().getStringExtra("postId"));

        tvDescription.setText(mPost.getFormattedDescription());

        // Setup the recycler view layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        rvComments.setLayoutManager(linearLayoutManager);

        // Setup the toolbar navigation icon
        toolbar.setNavigationOnClickListener(v -> finish());

        initProfileImages();

        getComments(mPost);
    }

    /**
     * Load and round the profile images in the layout
     */
    private void initProfileImages() {
        GlideApp.with(this)
                .load(R.drawable.background)
                .transform(new CircleCrop())
                .into(ivOutline);

        GlideApp.with(this)
                .load(new ColorDrawable(getResources().getColor(R.color.white)))
                .transform(new CircleCrop())
                .into(ivBorder);

        GlideApp.with(this)
                .load(mPost.getUser().getProfileUrl())
                .transform(new CircleCrop())
                .into(ivProfile);

        GlideApp.with(this)
                .load(mUser.getProfileUrl())
                .transform(new CircleCrop())
                .into(ivProfileComment);
    }

    /**
     * Queries the comments for a given post from parse
     * @param post the post to get the comments for
     */
    private void getComments(Post post) {
        // Build a query to get the comments for a post
        final Comment.Query query = new Comment.Query().withUser().forPost(post);
        query.findInBackground((List<Comment> comments, ParseException e) -> {
            if(e == null) {
                // Add the comments to the adapter and update it
                mComments.addAll(comments);
                mCommentAdapter.notifyDataSetChanged();
            } else {
                Log.e("CommentActivity", "Couldn't load comments", e);
            }
        });
    }

    /**
     * Posts a new comment, called when the post button is clicked
     */
    public void postComment(View view) {
        String text = etComment.getText().toString();

        mComment = Comment.createComment(mUser, mPost, text, (ParseException e) -> {
            if(e == null) {
                mComments.add(0, mComment);
                mCommentAdapter.notifyItemInserted(0);
                etComment.setText("");
                hideKeyboard();
            } else {
                Toast.makeText(this, "Could not post comment", Toast.LENGTH_SHORT).show();
                Log.e("CommentActivity", "Could not create comment", e);
            }

        });
    }

    /**
     * Utility method for hiding the keyboard
     */
    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}
