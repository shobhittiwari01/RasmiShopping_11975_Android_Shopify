package com.shopify.algolia.extension

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.algolia.instantsearch.helper.android.list.autoScrollToStart
import com.shopify.algolia.ListItemAdapter

import kotlinx.android.synthetic.main.alogolia_search.*
fun AppCompatActivity.configureRecyclerView(adapter: ListItemAdapter) {
   // recyclerView.layoutManager = GridLayoutManager(this, 2)
    recyclerView.let {
        it.visibility = View.VISIBLE
        it.layoutManager = LinearLayoutManager(this)
        it.adapter = adapter
        it.itemAnimator = null
        it.autoScrollToStart(adapter)
    }
}
