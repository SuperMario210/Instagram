package com.example.instagram.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.CommentActivity;
import com.example.instagram.R;
import com.example.instagram.models.GlideApp;
import com.example.instagram.models.Post;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostViewHolder extends RecyclerView.ViewHolder {
    private ParseUser mUser;
    private Context mContext;
    private Post mPost;
    private View mRoot;

    @BindView(R.id.iv_profile) ImageView ivProfile;
    @BindView(R.id.iv_post) ImageView ivPost;
    @BindView(R.id.iv_favorite) ImageView ivFavorite;
    @BindView(R.id.iv_comment) ImageView ivComment;
    @BindView(R.id.iv_direct) ImageView ivDirect;
    @BindView(R.id.iv_bookmark) ImageView ivBookmark;

    @BindView(R.id.tv_username) TextView tvUsername;
    @BindView(R.id.tv_date) TextView tvDate;
    @BindView(R.id.tv_caption) TextView tvCaption;
    @BindView(R.id.tv_favorites) TextView tvFavorites;

    public PostViewHolder(@NonNull View view) {
        super(view);
        mRoot = view;
        ButterKnife.bind(this, view);
    }

    public void bindPost(Post post, Context context) {
        mPost = post;
        mContext = context;
        mUser = ParseUser.getCurrentUser();

        tvUsername.setText(mPost.getUser().getUsername());
        tvDate.setText(mPost.formatDate(mPost.getCreatedAt()));

        String caption = mPost.getUser().getUsername() + " " + mPost.getDescription();
        SpannableStringBuilder builder = new SpannableStringBuilder(caption);
        builder.setSpan(new StyleSpan(Typeface.BOLD),
                0, mPost.getUser().getUsername().length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        tvCaption.setText(builder);

        GlideApp.with(mContext)
                .load(mPost.getImage().getUrl())
                .into(ivPost);

        mRoot.setOnClickListener(v -> {
            Intent i = new Intent(mContext, CommentActivity.class);
            i.putExtra("postId", mPost.getObjectId());
            mContext.startActivity(i);
        });

        initFavoriteIcon();
    }

    private void initFavoriteIcon() {
        final Drawable active_icon = ContextCompat.getDrawable(mContext, R.drawable.ufi_heart_active);
        DrawableCompat.setTint(
                DrawableCompat.wrap(active_icon),
                ContextCompat.getColor(mContext, R.color.red_5)
        );
        final Drawable icon = ContextCompat.getDrawable(mContext, R.drawable.ufi_heart);
        final Animation bounceAnim = AnimationUtils.loadAnimation(mContext, R.anim.bounce_scale);

        tvFavorites.setText(mPost.getNumFavoritesString());

        if(!mPost.isLikedByUser(mUser)) {
            ivFavorite.setImageDrawable(icon);
        } else {
            ivFavorite.setImageDrawable(active_icon);
        }

        ivFavorite.setOnClickListener(v -> {
            if(!mPost.isLikedByUser(mUser)) {
                mPost.addFavorite(mUser);
                mPost.saveInBackground(e -> {
                    tvFavorites.setText(mPost.getNumFavoritesString());
                    ivFavorite.setImageDrawable(active_icon);
                    ivFavorite.startAnimation(bounceAnim);
                });
            } else {
                mPost.removeFavorite(mUser);
                mPost.saveInBackground(e -> {
                    tvFavorites.setText(mPost.getNumFavoritesString());
                    ivFavorite.setImageDrawable(icon);
                    ivFavorite.startAnimation(bounceAnim);
                });
            }
        });
    }
}
