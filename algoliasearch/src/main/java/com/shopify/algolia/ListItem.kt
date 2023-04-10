package com.shopify.algolia

import com.algolia.search.model.ObjectID
import com.algolia.search.model.indexing.Indexable
import kotlinx.serialization.Serializable

@Serializable
data class ListItem(
    override val objectID: ObjectID,
    val title: String,
    val id: String,
    val handle: String,
    val body_html_safe: String? = null,
    val product_image: String,
    val queryId: String? = null,
    val objectId: String? = null,
    val price: Double? = null,
) : Indexable

data class ItemModel(val listItem: ListItem, val position: Int)
