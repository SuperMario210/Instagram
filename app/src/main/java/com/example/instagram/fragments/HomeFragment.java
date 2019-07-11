package com.example.instagram.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.instagram.ParseApp;
import com.example.instagram.R;
import com.example.instagram.adapters.PostAdapter;
import com.example.instagram.models.Post;
import com.example.instagram.models.PostData;
import com.parse.ParseException;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class HomeFragment extends Fragment {
    private Unbinder mUnbinder;
    private PostAdapter mPostAdapter;
    private PostData mPosts;

    @BindView(R.id.rv_posts) RecyclerView rvPosts;
    @BindView(R.id.swipe_container) SwipeRefreshLayout swipeContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup the recycler view adapter
        mPosts = ((ParseApp) getContext().getApplicationContext()).getPosts();
        mPostAdapter = new PostAdapter(mPosts, getContext());
        rvPosts.setAdapter(mPostAdapter);

        // Setup the recycler view layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPosts.setLayoutManager(linearLayoutManager);

        // Setup the swipe container
        swipeContainer.setOnRefreshListener(() -> {
            getPosts();
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(
                R.color.red_5,
                R.color.green_5,
                R.color.blue_5,
                R.color.purple_5);

        getPosts();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void getPosts() {
        final Post.Query query = new Post.Query().getTop().withUser().withFavorites();
        query.orderByDescending("createdAt").findInBackground((List<Post> posts, ParseException e) -> {
            if(e == null) {
                for(Post post : posts) {
                    mPostAdapter.notifyItemInserted(mPosts.addPost(post));
                }
                swipeContainer.setRefreshing(false);
            } else {
                Log.e("MainActivity", "Couldn't load posts", e);
            }
        });
    }
}
