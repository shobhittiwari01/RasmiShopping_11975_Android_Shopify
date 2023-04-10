package com.rasmishopping.app.homesection.adapters

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.rasmishopping.app.R
import com.rasmishopping.app.databinding.MInstafeeditemBinding
import com.rasmishopping.app.homesection.viewholders.InstaFeedItems
import com.rasmishopping.app.utils.Urls
import org.json.JSONArray
import javax.inject.Inject


class InstaFeedAdapters @Inject
constructor() : RecyclerView.Adapter<InstaFeedItems>() {
    var Instafeedarray = JSONArray()
    private var activity: Activity? = null

    fun setData(
        activity: Activity,
        Instafeedarray: JSONArray
    ) {
        this.activity = activity
        this.Instafeedarray = Instafeedarray
    }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstaFeedItems {
        val binding = DataBindingUtil.inflate<MInstafeeditemBinding>(LayoutInflater.from(parent.context), R.layout.m_instafeeditem, parent, false)
        return InstaFeedItems(binding)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return Instafeedarray.length()
    }

    override fun onBindViewHolder(holder: InstaFeedItems, position: Int) {
        try {
           when(Urls.InstaView){
               "grid"->{
                   holder.binding!!.list.visibility=View.GONE
                   holder.binding!!.grid.visibility=View.VISIBLE
                   Glide.with(activity!!).load(Instafeedarray.getJSONObject(position).getString("media_url")).into(holder.binding!!.feedimage)
               }
               "list"->{
                   holder.binding!!.grid.visibility=View.GONE
                   holder.binding!!.list.visibility=View.VISIBLE
                   Glide.with(activity!!).load(Instafeedarray.getJSONObject(position).getString("media_url")).into(holder.binding!!.feedimagelist)
               }
           }

            holder.binding!!.feedimage.setOnClickListener {
                val uri: Uri = Uri.parse(Instafeedarray.getJSONObject(position).getString("permalink"))
                val likeIng = Intent(Intent.ACTION_VIEW, uri)
                likeIng.setPackage("com.instagram.android")
                try {
                    activity!!.startActivity(likeIng)
                } catch (e: ActivityNotFoundException) {
                    activity!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Instafeedarray.getJSONObject(position).getString("permalink"))))
                }
            }
            holder.binding!!.feedimagelist.setOnClickListener {
                val uri: Uri = Uri.parse(Instafeedarray.getJSONObject(position).getString("permalink"))
                val likeIng = Intent(Intent.ACTION_VIEW, uri)
                likeIng.setPackage("com.instagram.android")
                try {
                    activity!!.startActivity(likeIng)
                } catch (e: ActivityNotFoundException) {
                    activity!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Instafeedarray.getJSONObject(position).getString("permalink"))))
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}