package com.shopify.instafeeds

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shopify.instafeeds.databinding.MInstafeeditemsBinding
import org.json.JSONArray

class InstaFeedAdapter : RecyclerView.Adapter<InstaFeedAdapter.InstaFeedItems>() {
    private var layoutInflater: LayoutInflater? = null
    var Instafeedarray = JSONArray()
    private var activity: Activity? = null

    fun setData(
        activity: Activity,
        Instafeedarray: JSONArray
    ) {
        this.activity = activity
        this.Instafeedarray = Instafeedarray
    }

    class InstaFeedItems(itemView: MInstafeeditemsBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: MInstafeeditemsBinding = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InstaFeedItems {
        layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<MInstafeeditemsBinding>(
            layoutInflater!!,
            R.layout.m_instafeeditems, parent, false
        )
        return InstaFeedItems(binding)
    }

    override fun getItemCount(): Int {
        return Instafeedarray.length()
    }

    override fun onBindViewHolder(holder: InstaFeedItems, position: Int) {
        Glide.with(activity!!).load(Instafeedarray.getJSONObject(position).getString("media_url"))
            .into(holder.binding.feedimage)
        holder.binding.feedcaption.text =
            Instafeedarray.getJSONObject(position).getString("caption")
        holder.binding.feedimage.setOnClickListener {
            val uri: Uri = Uri.parse(Instafeedarray.getJSONObject(position).getString("permalink"))
            val likeIng = Intent(Intent.ACTION_VIEW, uri)
            likeIng.setPackage("com.instagram.android")
            try {
                activity!!.startActivity(likeIng)
            } catch (e: ActivityNotFoundException) {
                activity!!.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(Instafeedarray.getJSONObject(position).getString("permalink"))
                    )
                )
            }
        }
    }
}