package com.example.instagram.adapters;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * The space item decoration class is used to add e ven spacings in between elements of a grid
 * layout without introducing margins on the borders of the grid layout
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int mSpace;
    private int mColumns;

    /**
     * @param space the amount of padding in between grid elements
     * @param columns the number of columns in the grid layout
     */
    public SpaceItemDecoration(int space, int columns) {
        mSpace = space;
        mColumns = columns;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        // Get the index of the item, subtract one because of the custom first view
        int index = parent.getChildAdapterPosition(view) - 1;

        // Add padding to the top of the item
        if (index < mColumns) {
            outRect.top = 0;
        } else {
            outRect.top = mSpace;
        }

        // Add padding to the left and right depending on if the item is on the left, right, or
        // middle of the grid layout
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