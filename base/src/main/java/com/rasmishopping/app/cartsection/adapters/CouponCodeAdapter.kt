package com.rasmishopping.app.cartsection.adapters

import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.databinding.CouponcodeItemBinding
import kotlinx.android.synthetic.main.couponcode_item.view.*
import javax.inject.Inject

class CouponCodeAdapter @Inject constructor() : RecyclerView.Adapter<CouponCodeAdapter.CouponCodeViewHolder>() {
     var couponList: MutableSet<String>? = null
    private var removeCouponCallback: RemoveCouponCallback? = null
    fun setData(couponList: MutableSet<String>, removeCouponCallback: RemoveCouponCallback) {
        this.couponList = couponList
        this.removeCouponCallback = removeCouponCallback
    }

    class CouponCodeViewHolder : RecyclerView.ViewHolder {
        private var couponcodeItemBinding: CouponcodeItemBinding? = null

        constructor(itemView: CouponcodeItemBinding) : super(itemView.root) {
            this.couponcodeItemBinding = itemView
        }
    }

    interface RemoveCouponCallback {
        fun removeCoupon(couponcode: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponCodeViewHolder {
        var view = DataBindingUtil.inflate<CouponcodeItemBinding>(LayoutInflater.from(parent.context), R.layout.couponcode_item, parent, false)
        return CouponCodeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return couponList?.size ?: 0
    }

    override fun onBindViewHolder(holder: CouponCodeViewHolder, position: Int) {
        holder.itemView.coupon_code.text = couponList?.toList()?.get(position)
        holder.itemView.close_but.visibility=View.VISIBLE
        holder.itemView.close_but.setOnClickListener {
            removeCouponCallback?.removeCoupon(couponList?.toList()?.get(position) ?: "")
            holder.itemView.close_but.visibility=View.GONE
        }

    }
}