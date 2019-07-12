package com.example.instagram.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.R;
import com.example.instagram.interfaces.UserCallback;
import com.example.instagram.models.Post;
import com.example.instagram.models.PostData;

public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {
    private PostData mPosts;
    private Context mContext;
    private UserCallback mCallback;

    public PostAdapter(PostData posts, Context context, UserCallback callback) {
        mPosts = posts;
        mContext = context;
        mCallback = callback;
    }

    // Clean all elements of the recycler
    public void clear() {
        mPosts.clearData();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View postView = inflater.inflate(R.layout.item_post, parent, false);
        PostViewHolder viewHolder = new PostViewHolder(postView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder holder, int position) {
        Post post = mPosts.getPostByIndex(position);
        holder.bindPost(post, mContext, mCallback);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }
}
