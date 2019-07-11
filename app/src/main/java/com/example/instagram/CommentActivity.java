package com.example.instagram;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
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

        // Setup the recycler view layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvComments.setLayoutManager(linearLayoutManager);

        toolbar.setNavigationOnClickListener(v -> finish());

        GlideApp.with(this)
                .load(mUser.getProfileUrl())
                .transform(new CircleCrop())
                .into(ivProfile);

        String postId = getIntent().getStringExtra("postId");
        mPost = ((ParseApp) getApplication()).getPosts().getPostById(postId);
        getComments(mPost);
    }

    private void getComments(Post post) {
        final Comment.Query query = new Comment.Query().withUser().forPost(post);
        query.findInBackground((List<Comment> comments, ParseException e) -> {
            if(e == null) {
                mComments.addAll(comments);
                mCommentAdapter.notifyDataSetChanged();
            } else {
                Log.e("CommentActivity", "Couldn't load comments", e);
            }
        });
    }

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

    private void hideKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

}
