package com.rasmishopping.app.productsection.adapters

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.databinding.AllReviewListItemBinding
import com.rasmishopping.app.productsection.models.Review
import javax.inject.Inject

class AllReviewListAdapter @Inject constructor() :
    RecyclerView.Adapter<AllReviewListAdapter.AllReviewListViewHolder>() {
    var reviwList: ArrayList<Review>? = null
    lateinit var reviewImageListAdapter: ReviewImageListAdapter
    lateinit var context: Activity
    fun setData(reviwList: ArrayList<Review>?,context: Activity) {
        this.reviwList = reviwList
        this.context=context
    }

    class AllReviewListViewHolder : RecyclerView.ViewHolder {
        var binding: AllReviewListItemBinding? = null
        constructor(itemBinding: AllReviewListItemBinding) : super(itemBinding.root) {
            this.binding = itemBinding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllReviewListViewHolder {
        var binding = DataBindingUtil.inflate<AllReviewListItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.all_review_list_item,
            parent,
            false
        )
        return AllReviewListViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return reviwList?.size!!
    }

    override fun onBindViewHolder(holder: AllReviewListViewHolder, position: Int) {
        holder.binding?.reviewList = reviwList?.get(position)
        if ((reviwList?.get(position)?.reviewerName?.contains(" ")!!)&&(reviwList?.get(position)?.reviewerName?.split(" ")!!.get(1) != "")) {
            holder.binding?.shortname?.text =
                reviwList?.get(position)?.reviewerName?.split(" ")?.get(0)
                    ?.substring(0, 1) + "" + reviwList?.get(position)?.reviewerName?.split(" ")
                    ?.get(1)?.substring(0, 1)
        } else {
            holder.binding?.shortname?.text =
                reviwList?.get(position)?.reviewerName?.substring(0, 1)
        }

        if (reviwList?.get(position)?.pictures?.size!! > 0) {
            reviewImageListAdapter=ReviewImageListAdapter()
            holder?.binding?.imageList?.visibility = View.VISIBLE
            reviewImageListAdapter.setData(reviwList?.get(position)?.pictures!!,context)
            holder?.binding?.imageList?.adapter=reviewImageListAdapter
        }else{
            holder?.binding?.imageList?.visibility = View.GONE
        }
    }
}