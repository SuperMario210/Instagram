package com.example.instagram.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.R;
import com.example.instagram.callbacks.VoidCallback;
import com.example.instagram.models.GlideApp;
import com.example.instagram.models.Post;
import com.parse.ParseUser;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int FIRST_ITEM = 0;
    public static final int NORMAL_ITEM = 1;

    private List<Post> mPosts;
    private ParseUser mUser;
    private Context mContext;
    private VoidCallback mCallback;

    public ProfileAdapter(List<Post> posts, Context context, ParseUser user, VoidCallback logOutCallback) {
        mPosts = posts;
        mContext = context;
        mUser = user;
        mCallback = logOutCallback;
    }

    // Clean all elements of the recycler
    public void clear() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return FIRST_ITEM;
        else return NORMAL_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View gridView;

        if (viewType == FIRST_ITEM) {
            gridView = inflater.inflate(R.layout.item_profile, parent, false);
            return new ProfileViewHolder(gridView);
        } else {
            gridView = inflater.inflate(R.layout.item_post_grid, parent, false);
            return new PostGridViewHolder(gridView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType() == FIRST_ITEM) {
            ((ProfileViewHolder) holder).bindUser(mUser, mContext);
        } else {
            Post post = mPosts.get(position - 1);
            ((PostGridViewHolder) holder).bindPost(post, mContext);
        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size() + 1;
    }

    public class PostGridViewHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        private Post mPost;

        @BindView(R.id.iv_post) ImageView ivPost;

        public PostGridViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindPost(Post post, Context context) {
            mPost = post;
            mContext = context;

            GlideApp.with(mContext)
                    .load(mPost.getImage().getUrl())
                    .into(ivPost);
        }
    }

    public class ProfileViewHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        private ParseUser mUser;

        @BindView(R.id.iv_profile) ImageView ivProfile;
        @BindView(R.id.tv_username) TextView tvUsername;
        @BindView(R.id.tv_bio) TextView tvBio;
        @BindView(R.id.tv_posts) TextView tvPosts;
        @BindView(R.id.tv_followers) TextView tvFollowers;
        @BindView(R.id.tv_following) TextView tvFollowing;
        @BindView(R.id.btn_log_out) Button btnLogout;

        public ProfileViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindUser(ParseUser user, Context context) {
            mUser = user;
            mContext = context;

            tvUsername.setText(mUser.getUsername());
            tvPosts.setText(String.format(Locale.getDefault(), "%d", mPosts.size()));

            btnLogout.setOnClickListener(v -> {
                mCallback.done(null);
            });
//            GlideApp.with(mContext)
//                    .load(mPost.getImage().getUrl())
//                    .into(ivPost);
        }
    }
}
