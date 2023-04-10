package com.shopify.algolia

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.algolia.instantsearch.helper.tracker.HitsTracker
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rasmishopping.app.basesection.models.ListData
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.utils.Constant
import kotlinx.android.synthetic.main.list_item_large.view.*

class ListItemViewHolder(itemView: View, private val hitsTracker: HitsTracker) :
    RecyclerView.ViewHolder(itemView) {

    private val title = itemView.itemTitle
    private val substitle = itemView.itemSubtitle
    private val imageView = itemView.itemImage

    fun bind(model: ItemModel) {
        val listItem = model.listItem
        title.text = listItem.title.trim()
        substitle.text = listItem.body_html_safe!!.trim()
        itemView.setOnClickListener {
            hitsTracker.trackClick(listItem, model.position)
            productClick(itemView,listItem)
        }
        Glide
            .with(itemView.context)
            .load(listItem.product_image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }
        fun productClick(view: View, data: ListItem) {
            val productintent = Intent(view.context, ProductView::class.java)
            productintent.putExtra("ID", "gid://shopify/Product/${data.id}")
            productintent.putExtra("tittle", data.title)
            view.context.startActivity(productintent)
            Constant.activityTransition(view.context)
        }
    }