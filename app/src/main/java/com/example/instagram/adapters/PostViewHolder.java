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

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.instagram.CommentActivity;
import com.example.instagram.R;
import com.example.instagram.interfaces.UserCallback;
import com.example.instagram.models.GlideApp;
import com.example.instagram.models.Post;
import com.example.instagram.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostViewHolder extends RecyclerView.ViewHolder {
    private User mUser;
    private Context mContext;
    private Post mPost;
    private UserCallback mCallback;
    private View mRoot;

    @BindView(R.id.iv_outline) ImageView ivProfile;
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

    public void bindPost(Post post, Context context, UserCallback callback) {
        mPost = post;
        mContext = context;
        mUser = post.getUser();
        mCallback = callback;

        if(mPost.getDescription().isEmpty()) {
            tvCaption.setVisibility(View.GONE);
        } else {
            String caption = mPost.getUser().getUsername() + " " + mPost.getDescription();
            SpannableStringBuilder builder = new SpannableStringBuilder(caption);
            builder.setSpan(new StyleSpan(Typeface.BOLD),
                    0, mPost.getUser().getUsername().length(),
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            tvCaption.setText(builder);
        }

        GlideApp.with(mContext)
                .load(mPost.getImage().getUrl())
                .into(ivPost);

        mRoot.setOnClickListener(v -> {
            Intent i = new Intent(mContext, CommentActivity.class);
            i.putExtra("postId", mPost.getObjectId());
            mContext.startActivity(i);
        });

        initFavoriteIcon();
        initUserInfo();
    }

    private void initUserInfo() {
        GlideApp.with(mContext)
                .load(mUser.getProfileUrl())
                .transform(new CircleCrop())
                .into(ivProfile);

        tvUsername.setText(mPost.getUser().getUsername());
        tvDate.setText(mPost.formatDate(mPost.getCreatedAt()));

        View.OnClickListener listener = v -> mCallback.done(mUser);
        tvUsername.setOnClickListener(listener);
        ivProfile.setOnClickListener(listener);
    }

    private void initFavoriteIcon() {
        final Drawable active_icon = ContextCompat.getDrawable(mContext, R.drawable.ufi_heart_active);
        DrawableCompat.setTint(
                DrawableCompat.wrap(active_icon),
                ContextCompat.getColor(mContext, R.color.red_5)
        );
        final Drawable icon = ContextCompat.getDrawable(mContext, R.drawable.ufi_heart);
        final Animation bounceAnim = AnimationUtils.loadAnimation(mContext, R.anim.bounce_scale);

        tvFavorites.setText(mPost.formatNumFavorites());

        User currentUser = User.getCurrentUser();
        if(mPost.isFavoritedByUser(currentUser)) {
            ivFavorite.setImageDrawable(active_icon);
        } else {
            ivFavorite.setImageDrawable(icon);
        }

        ivFavorite.setOnClickListener(v -> {
            if(mPost.isFavoritedByUser(currentUser)) {
                mPost.removeFavorite(currentUser);
                mPost.saveInBackground(e -> {
                    tvFavorites.setText(mPost.formatNumFavorites());
                    ivFavorite.setImageDrawable(icon);
                    ivFavorite.startAnimation(bounceAnim);
                });
            } else {
                mPost.addFavorite(currentUser);
                mPost.saveInBackground(e -> {
                    tvFavorites.setText(mPost.formatNumFavorites());
                    ivFavorite.setImageDrawable(active_icon);
                    ivFavorite.startAnimation(bounceAnim);
                });
            }
        });
    }
}
