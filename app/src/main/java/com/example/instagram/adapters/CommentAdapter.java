package com.example.instagram.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagram.R;
import com.example.instagram.models.Comment;

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

        @BindView(R.id.iv_profile) ImageView ivProfile;
        @BindView(R.id.tv_comment) TextView tvComment;

        public CommentViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bindComment(Comment comment, Context context) {
            mComment = comment;
            mContext = context;

            String commentText = mComment.getUser().getUsername() + " " + mComment.getText();
            SpannableStringBuilder builder = new SpannableStringBuilder(commentText);
            builder.setSpan(new StyleSpan(Typeface.BOLD),
                    0, mComment.getUser().getUsername().length(),
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            tvComment.setText(builder);
        }
    }
}
