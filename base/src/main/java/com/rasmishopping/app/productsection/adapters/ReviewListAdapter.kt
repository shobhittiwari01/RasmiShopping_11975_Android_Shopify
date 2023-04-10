package com.rasmishopping.app.productsection.adapters

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.databinding.ReviewListItemBinding
import com.rasmishopping.app.productsection.models.Review
import io.grpc.Context
import javax.inject.Inject

class ReviewListAdapter @Inject
constructor() : RecyclerView.Adapter<ReviewListAdapter.ReviewListViewHolder>() {
    var reviwList: List<Review>? = null
    lateinit var reviewImageListAdapter: ReviewImageListAdapter
    lateinit var context: Activity
    fun setData(reviwList: List<Review>?,context: Activity) {
        this.reviwList = reviwList
        this.context=context
    }
    class ReviewListViewHolder : RecyclerView.ViewHolder {
        var binding: ReviewListItemBinding? = null

        constructor(itemBinding: ReviewListItemBinding) : super(itemBinding.root) {
            this.binding = itemBinding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewListViewHolder {
        var binding = DataBindingUtil.inflate<ReviewListItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.review_list_item,
            parent,
            false
        )
        return ReviewListViewHolder(binding)
    }


    override fun getItemCount(): Int {
        if (reviwList?.size!! <= 5)
            return reviwList?.size!!
        else
            return 5
    }

    override fun onBindViewHolder(holder: ReviewListViewHolder, position: Int) {
        holder.binding?.reviewList = reviwList?.get(position)
        if (reviwList?.get(position)?.reviewerName?.contains(" ")!!) {
            holder.binding?.shortname?.text =
                if (reviwList?.get(position)?.reviewerName?.split(" ")?.get(1) != "") {
                    reviwList?.get(position)?.reviewerName?.split(" ")?.get(0)
                        ?.substring(0, 1) + "" + reviwList?.get(position)?.reviewerName?.split(" ")
                        ?.get(1)?.substring(0, 1)
                } else {
                    reviwList?.get(position)?.reviewerName?.split(" ")?.get(0)?.substring(0, 1)
                }
        } else {
            holder.binding?.shortname?.text =
                reviwList?.get(position)?.reviewerName?.substring(0, 1)
        }
        if(reviwList?.get(position)?.pictures!=null){
            if (reviwList?.get(position)?.pictures?.size!! > 0) {
                reviewImageListAdapter = ReviewImageListAdapter()
                holder?.binding?.imageList?.visibility = View.VISIBLE
                reviewImageListAdapter.setData(reviwList?.get(position)?.pictures!!,context)
                holder?.binding?.imageList?.adapter = reviewImageListAdapter
            } else {
                holder?.binding?.imageList?.visibility = View.GONE
            }
        }
    }
}