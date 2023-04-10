package com.rasmishopping.app.homesection.models

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import com.rasmishopping.app.basesection.activities.Weblink
import com.rasmishopping.app.productsection.activities.ProductList
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.productsection.activities.VideoPlayerActivity
import com.rasmishopping.app.utils.Constant
import java.io.UnsupportedEncodingException

class Home {
    var link_to: String? = null
    var imageurl: String? = null
    var id: String? = null

    fun Click(view: View, home: Home) {
        when (home.link_to) {
            "collections" -> {
                val collection = "gid://shopify/Collection/" + home.id!!
                val intent = Intent(view.context, ProductList::class.java)
                intent.putExtra("ID", (collection))
                intent.putExtra("tittle", " ")
                view.context.startActivity(intent)
                Constant.activityTransition(view.context)
            }
            "products" -> {
                val product = "gid://shopify/Product/" + home.id!!
                val prod_link = Intent(view.context, ProductView::class.java)
                prod_link.putExtra("ID", (product))
                view.context.startActivity(prod_link)
                Constant.activityTransition(view.context)
            }
            "web_url" -> {
                if(home.id!!.contains("www.youtube.com")){
                    var str = home.id
                    var lastIndex = str!!.lastIndexOf(" ")
                    val finalString: String = str.substring(0, lastIndex)
                    view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(finalString)))
                    Constant.activityTransition(view.context!!)
                } else {
                    if(!home.id!!.trim().equals("#")){
                        val weblink = Intent(view.context, Weblink::class.java)
                        weblink.putExtra("link", home.id)
                        weblink.putExtra("name", " ")
                        view.context.startActivity(weblink)
                        Constant.activityTransition(view.context)
                    }
                }
            }
        }
    }

    fun getBase64Encode(id: String): String {
        var id = id
        val data = id
        try {
            id = data
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return id
    }
}
