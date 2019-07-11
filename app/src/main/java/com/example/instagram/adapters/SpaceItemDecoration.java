package com.example.instagram.adapters;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int mSpace;
    private int mColumns;

    public SpaceItemDecoration(int space, int columns) {
        mSpace = space;
        mColumns = columns;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        int index = parent.getChildAdapterPosition(view) - 1;

        if (index < mColumns) {
            outRect.top = 0;
        } else {
            outRect.top = mSpace;
        }

        if (index % mColumns == 0) {
            outRect.left = 0;
            outRect.right = mSpace * 2 / 3;
        } else if(index % mColumns == mColumns - 1) {
            outRect.left = mSpace * 2 / 3;
            outRect.right = 0;
        } else {
            outRect.left = mSpace / 3;
            outRect.right = mSpace / 3;
        }

        outRect.bottom = 0;
    }
}