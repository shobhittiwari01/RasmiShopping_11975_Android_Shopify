package com.rasmishopping.app.collectionsection.models

import android.content.Intent
import android.util.Base64
import android.view.Gravity
import android.view.View

import com.shopify.graphql.support.ID
import com.rasmishopping.app.basesection.activities.Weblink
import com.rasmishopping.app.productsection.activities.ProductList
import com.rasmishopping.app.utils.Constant
import java.nio.charset.Charset

class Collection {

    var category_name: String? = null
    var alignment: Gravity? = null

    var id: ID? = null

    fun blockClick(view: View, collection: Collection) {
        val intent = Intent(view.context, ProductList::class.java)
        intent.putExtra("tittle", collection.category_name)
        intent.putExtra("ID", collection.id!!.toString())
        view.context.startActivity(intent)
        Constant.activityTransition(view.context)
    }

    fun gridClick(view: View, collection: Collection) {
        when (collection.type) {
            "collections" -> {
                val intent = Intent(view.context, ProductList::class.java)
                intent.putExtra("tittle", collection.category_name)
                intent.putExtra("ID", "gid://shopify/Collection/"+collection.value)
                view.context.startActivity(intent)
                Constant.activityTransition(view.context)
            }
            else -> {
                if(!collection.value!!.trim().equals("#")){
                    val intent = Intent(view.context, Weblink::class.java)
                    intent.putExtra("name", collection.category_name)
                    intent.putExtra("link", collection.value)
                    view.context.startActivity(intent)
                    Constant.activityTransition(view.context)
                }
            }
        }

    }

    var type: String? = null
    var value: String? = null
    private fun getcategoryID(id: String?): String? {
        var cat_id: String? = null
        try {
            val data = id
            cat_id = data
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cat_id
    }
}

