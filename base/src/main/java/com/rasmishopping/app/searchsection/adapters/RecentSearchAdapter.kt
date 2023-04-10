package com.rasmishopping.app.searchsection.adapters
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.databinding.MRecentBinding
import com.rasmishopping.app.productsection.viewholders.ProductItem
import com.rasmishopping.app.searchsection.interfaces.Click
import org.json.JSONArray
import javax.inject.Inject
class RecentSearchAdapter @Inject
constructor() : RecyclerView.Adapter<ProductItem>() {
    var recents=JSONArray()
    lateinit var activity:Activity
    lateinit var click: Click
    fun setData(recents:JSONArray,activity:Activity,click:Click) {
        this.recents = recents
        this.activity = activity
        this.click=click
    }
    init {
       // setHasStableIds(true)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductItem {
        val binding = DataBindingUtil.inflate<MRecentBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_recent,
            parent,
            false
        )
        return ProductItem(binding)
    }
    override fun onBindViewHolder(holder: ProductItem, position: Int) {
        holder.recentbinding!!.recentsearch.text= recents.getString(position)
        holder.recentbinding!!.recentsearch.setOnClickListener({
            click.click(holder.recentbinding!!.recentsearch.text.toString())
        })
    }
    override fun getItemCount(): Int {
        return recents.length()
    }
}