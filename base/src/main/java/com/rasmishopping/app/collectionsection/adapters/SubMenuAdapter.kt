package com.rasmishopping.app.collectionsection.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.collectionsection.viewholders.CollectionItem
import com.rasmishopping.app.databinding.MSubMenuTitleBinding
import com.shopify.graphql.support.ID
import org.json.JSONArray
import java.util.*
import javax.inject.Inject

class SubMenuAdapter @Inject
constructor() : RecyclerView.Adapter<CollectionItem>() {
    private var layoutInflater: LayoutInflater? = null
    lateinit var collectionEdges: JSONArray
    var activity: Context? = null
        private set

    fun setData(collectionEdges: JSONArray, activity: Context) {
        this.collectionEdges = collectionEdges
        this.activity = activity
    }

    init {
        setHasStableIds(true)
    }

    var subMenuback: SubMenuCallback? = null

    interface SubMenuCallback {
        fun subMenuCallback(variantName: String, optionName: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionItem {
        val binding = DataBindingUtil.inflate<MSubMenuTitleBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_sub_menu_title,
            parent,
            false
        )
        return CollectionItem(binding)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    override fun onBindViewHolder(holder: CollectionItem, position: Int) {
        var jsonObjectSub = collectionEdges.getJSONObject(position)

        val collectionSub = com.rasmishopping.app.collectionsection.models.Collection()
        val nameSub = jsonObjectSub.getString("title").substring(0, 1)
            .uppercase(Locale.getDefault()) + jsonObjectSub.getString("title").substring(1)
            .lowercase(Locale.getDefault())
        var jsonObject=collectionEdges.getJSONObject(position)

        val collection = com.rasmishopping.app.collectionsection.models.Collection()
        val name = jsonObject.getString("title").substring(0, 1)
            .uppercase(Locale.getDefault()) + jsonObject.getString("title").substring(1)
            .lowercase(Locale.getDefault())
        // collection.category_name = name
        collection.id = ID(jsonObject.getString("id"))
        holder.collectionbindingSubMenu.categorydata = collection
        holder.collectionbindingSubMenu.tvName.text = name
    }

    override fun getItemCount(): Int {
        return collectionEdges.length()
    }

}
