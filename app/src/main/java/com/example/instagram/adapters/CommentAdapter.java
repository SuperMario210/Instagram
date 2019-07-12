package com.example.instagram.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.instagram.R;
import com.example.instagram.models.Comment;
import com.example.instagram.models.GlideApp;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> mComments;
    private Context mContext;

    public CommentAdapter(List<Comment> comments, Context context) {
        mComments = comments;
        mContext = context;
    }

    // Clean all elements of the recycler
    public void clear() {
        mComments.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View commentView = inflater.inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(commentView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapter.CommentViewHolder holder, int position) {
        holder.bindComment(mComments.get(position), mContext);
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        private Context mContext;
        private Comment mComment;

        @BindView(R.id.iv_outline) ImageView ivProfile;
        @BindView(R.id.tv_description) TextView tvComment;
        @BindView(R.id.tv_timestamp) TextView tvTimestamp;

        public CommentViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindComment(Comment comment, Context context) {
            mComment = comment;
            mContext = context;

            tvComment.setText(mComment.getFormattedText());

            tvTimestamp.setText(mComment.getFormattedDate());
            GlideApp.with(mContext)
                    .load(comment.getUser().getProfileUrl())
                    .transform(new CircleCrop())
                    .into(ivProfile);
        }
    }
}
