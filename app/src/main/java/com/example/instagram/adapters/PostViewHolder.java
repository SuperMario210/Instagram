package com.example.instagram.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.R;
import com.example.instagram.models.GlideApp;
import com.example.instagram.models.Post;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.iv_profile) ImageView ivProfile;
    @BindView(R.id.iv_post) ImageView ivPost;
    @BindView(R.id.iv_favorite) ImageView ivFavorite;
    @BindView(R.id.iv_comment) ImageView ivComment;
    @BindView(R.id.iv_direct) ImageView ivDirect;
    @BindView(R.id.iv_bookmark) ImageView ivBookmark;

    @BindView(R.id.tv_username) TextView tvUsername;
    @BindView(R.id.tv_date) TextView tvDate;
    @BindView(R.id.tv_caption) TextView tvCaption;

    public PostViewHolder(@NonNull View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void bindPost(Post post, Context context) {
        tvUsername.setText(post.getUser().getUsername());
        tvDate.setText(Post.formatDate(post.getCreatedAt()));

        String caption = post.getUser().getUsername() + " " + post.getDescription();
        SpannableStringBuilder builder = new SpannableStringBuilder(caption);
        builder.setSpan(new StyleSpan(Typeface.BOLD),
                0, post.getUser().getUsername().length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        tvCaption.setText(builder);

        GlideApp.with(context)
                .load(post.getImage().getUrl())
                .into(ivPost);
    }
}
