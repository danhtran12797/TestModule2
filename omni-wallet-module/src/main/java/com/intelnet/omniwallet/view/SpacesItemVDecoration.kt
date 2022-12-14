package com.intelnet.omniwallet.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemVDecoration(val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildLayoutPosition(view) != state.itemCount) {
            outRect.top = space
        } else {
            outRect.top = 0
        }

    }
}