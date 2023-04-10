package com.rasmishopping.app.productsection.adapters

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.databinding.JudgemeImagesItemBinding
import com.rasmishopping.app.productsection.activities.ZoomActivity
import com.rasmishopping.app.productsection.models.MediaModel
import com.rasmishopping.app.productsection.models.Picture
import com.rasmishopping.app.utils.Constant


class ReviewImageListAdapter :
    RecyclerView.Adapter<ReviewImageListAdapter.ReviewImageListViewHolder>() {
    lateinit var picturesList: List<Picture>
    private val TAG = "ReviewImageListAdapter"
    var mediaList = mutableListOf<MediaModel>()
    lateinit var context: Activity
    fun setData(picturesList: List<Picture>,context: Activity) {
        this.picturesList = picturesList
        this.context=context
    }

    class ReviewImageListViewHolder : RecyclerView.ViewHolder {
        lateinit var judgemeImagesItemBinding: JudgemeImagesItemBinding

        constructor(itemView: JudgemeImagesItemBinding) : super(itemView.root) {
            this.judgemeImagesItemBinding = itemView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewImageListViewHolder {
        val judgemeImagesItemBinding: JudgemeImagesItemBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.judgeme_images_item,
                parent,
                false
            )
        return ReviewImageListViewHolder(judgemeImagesItemBinding)
    }

    override fun onBindViewHolder(holder: ReviewImageListViewHolder, position: Int) {
        val commanModel = CommanModel()
        commanModel.imageurl = picturesList.get(position).urls.small
        holder.judgemeImagesItemBinding.commanmodel = commanModel

        val mediaModel = MediaModel(
            "MediaImage",
            picturesList.get(position).urls.small,
            picturesList.get(position).urls.small
        )

        if (picturesList.get(position).hidden) {
            holder.judgemeImagesItemBinding.itemImage.visibility = View.GONE
        } else {
            holder.judgemeImagesItemBinding.itemImage.visibility = View.VISIBLE
        }
        mediaList.add(mediaModel)
        holder.judgemeImagesItemBinding.itemImage.setOnClickListener {
            var intent = Intent(context, ZoomActivity::class.java)
            intent.putExtra("images", picturesList.get(position).urls.small)
            intent.putExtra("imageslist", Gson().toJson(mediaList))
            context.startActivity(intent)
            Constant.activityTransition(context)
        }
    }

    override fun getItemCount(): Int {
        return picturesList.size
    }
}