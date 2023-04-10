package com.rasmishopping.app.collectionsection.adapters
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.collectionsection.models.Collection
import com.rasmishopping.app.collectionsection.viewholders.CollectionItem
import com.rasmishopping.app.databinding.MCategoryitemBinding
import java.util.*
import javax.inject.Inject
class CollectionRecylerAdapter @Inject
constructor() : RecyclerView.Adapter<CollectionItem>() {
    lateinit var collectionEdges: List<Storefront.CollectionEdge>
    var activity: Activity? = null
    fun setData(collectionEdges: List<Storefront.CollectionEdge>, activity: Activity) {
        this.collectionEdges = collectionEdges
        this.activity = activity
    }
    init {
        setHasStableIds(true)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionItem {
        val binding = DataBindingUtil.inflate<MCategoryitemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_categoryitem,
            parent,
            false
        )
        return CollectionItem(binding)
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun onBindViewHolder(holder: CollectionItem, position: Int) {
        if (collectionEdges[position].node.image != null) {
            val model = CommanModel()
            model.imageurl = collectionEdges[position].node.image.url
            holder.binding.commondata = model
        }
        val collection = Collection()
        val name = collectionEdges[position].node.title.substring(0, 1)
            .uppercase(Locale.getDefault()) + collectionEdges[position].node.title.substring(1)
            .lowercase(Locale.getDefault())
        collection.category_name = name
        collection.id = collectionEdges[position].node.id
        holder.binding.categorydata = collection
    }
    override fun getItemCount(): Int {
        return collectionEdges.size
    }
}
