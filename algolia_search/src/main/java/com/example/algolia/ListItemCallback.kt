package com.example.algolia

import androidx.recyclerview.widget.DiffUtil
import com.example.algolia.ItemModel

object ListItemCallback : DiffUtil.ItemCallback<ItemModel>() {

    override fun areItemsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
        return oldItem.listItem.objectId == newItem.listItem.objectId
    }

    override fun areContentsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
        return oldItem.listItem == newItem.listItem
    }
}
