package com.rasmishopping.app.cartsection.adapters

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.cartsection.viewholders.DiscountItems
import com.rasmishopping.app.databinding.MDiscountlistingBinding
import org.json.JSONArray
import javax.inject.Inject

class AllDiscountListAdapter @Inject
constructor() : RecyclerView.Adapter<DiscountItems>() {
    private var activity: Activity? = null
    private var discarr: JSONArray?= JSONArray()
    private val checkString = "DiscountAutomatic"

    fun setData(activity: Activity, discarr: JSONArray) {
        this.activity = activity
        this.discarr = discarr
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountItems {
        var listbinding = DataBindingUtil.inflate<MDiscountlistingBinding>(LayoutInflater.from(parent.context), R.layout.m_discountlisting, parent, false)
        return DiscountItems(listbinding)
    }

    override fun getItemCount(): Int {
        return discarr!!.length()
    }

    override fun onBindViewHolder(holder: DiscountItems, position: Int) {
        if(discarr!!.getJSONObject(position).getJSONObject("discount").getString("__typename").contains(checkString)){
            holder.binding.copyimage.visibility = View.GONE
            holder.binding.nocoderequired.visibility = View.VISIBLE
        }else{
            holder.binding.copyimage.visibility = View.VISIBLE
            holder.binding.nocoderequired.visibility = View.GONE
        }
        holder.binding.coupontext.text = "CODE:" + " " +discarr!!.getJSONObject(position).getJSONObject("discount").getString("title")
        holder.binding.coupondesc.text = discarr!!.getJSONObject(position).getJSONObject("discount").getString("summary")
        holder.binding.copyimage.setOnClickListener {
            val textToCopy = discarr!!.getJSONObject(position).getJSONObject("discount").getString("title")
            val clipboardManager = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", textToCopy.uppercase())
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(activity, "Coupon code copied", Toast.LENGTH_SHORT).show()
        }
    }
}
