package com.rasmishopping.app.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(private val horspaceSize: Int,val verspaceSize: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        with(outRect) {
            top = verspaceSize
            left = horspaceSize
            right = horspaceSize
            bottom = verspaceSize
        }
    }
}