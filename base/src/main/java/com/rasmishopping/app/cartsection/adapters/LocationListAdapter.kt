package com.rasmishopping.app.cartsection.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.rasmishopping.app.R
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.databinding.LocationListItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocationListAdapter @Inject constructor() :
    RecyclerView.Adapter<LocationListAdapter.LocationListViewHolder>() {
    var selectedposition : Int = -1
    private var layoutInflater: LayoutInflater? = null
    var location_list: JsonArray? = null
    var activity: Activity? = null

    companion object {
        lateinit var itemClick: ItemClick
        private var selectedPosition: Int = 0
    }

    fun setData(activity: Activity, location_list: JsonArray, itemClick: ItemClick) {
        this.location_list = location_list
        LocationListAdapter.itemClick = itemClick
        this.activity = activity
    }

    interface ItemClick {
        fun selectLocation(location_item: JsonObject)
    }

    class LocationListViewHolder : RecyclerView.ViewHolder {
        var locationListItemBinding: LocationListItemBinding? = null

        constructor(itemView: LocationListItemBinding) : super(itemView.root) {
            this.locationListItemBinding = itemView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationListViewHolder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }
        val binding = DataBindingUtil.inflate<LocationListItemBinding>(
            layoutInflater!!,
            R.layout.location_list_item,
            parent,
            false
        )
        return LocationListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return location_list?.size()!!
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LocationListViewHolder, position: Int) {
        holder.locationListItemBinding!!.locationChk.text =
            location_list?.get(position)?.asJsonObject?.get("company_name")?.asString + "\n" + location_list?.get(
                position
            )?.asJsonObject?.get("address_line_1")?.asString + "\n" + location_list?.get(position)?.asJsonObject?.get(
                "city"
            )?.asString + "," + location_list?.get(position)?.asJsonObject?.get("postal_code")?.asString
            holder.locationListItemBinding!!.locationChk.setChecked(position == selectedPosition)
            holder.locationListItemBinding!!.locationChk.setOnCheckedChangeListener { compoundButton, b ->
                if(b){
                    //selectedPosition = position
                    selectedPosition = holder.adapterPosition
                    itemClick.selectLocation(location_list?.get(position)?.asJsonObject!!)
                    holder.locationListItemBinding!!.locationChk.buttonTintList= ColorStateList.valueOf(activity!!.resources.getColor(R.color.black))
                    GlobalScope.launch(Dispatchers.Main) {
                        notifyDataSetChanged()
                    }
                }
            }
    }
}