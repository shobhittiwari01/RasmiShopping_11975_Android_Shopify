package com.rasmishopping.app.productsection.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.R
import com.rasmishopping.app.databinding.MProductfilterkeyitemBinding
import com.rasmishopping.app.productsection.viewholders.FilterItem
import com.rasmishopping.app.sharedprefsection.MagePrefs
import org.json.JSONArray
import javax.inject.Inject


class FilterKeyAdapter @Inject
constructor() : RecyclerView.Adapter<FilterItem>() {
    private var layoutInflater: LayoutInflater? = null
    private var activity: Activity? = null
    var jsonArray: JSONArray? = null
    fun setData(activity: Activity, filterarray: JSONArray) {
        this.activity = activity
        this.jsonArray = filterarray
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterItem {
        val binding = DataBindingUtil.inflate<MProductfilterkeyitemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_productfilterkeyitem,
            parent,
            false
        )
        return FilterItem(binding)
    }

    override fun onBindViewHolder(holder: FilterItem, position: Int) {
        Log.i("WHOLEARRAY", "" + jsonArray)
        val jsonObject = jsonArray!!.getJSONObject(position)
        val responseData = jsonObject.getJSONObject("responseData")
        Log.i("ALlBBJECTS", "2 " + responseData)
        if (responseData.has("label")) {
            Log.i("ALLLABELS", "" + responseData.getString("label"))
            holder.binding.labeltext.text = responseData.getString("label")
        }
        if (responseData.has("type")) {
            Log.i("ALLType", "" + responseData.getString("type"))
            //holder.binding.labeltext.text = responseData.getString("label")

        }
        holder.binding.labeltext.setOnClickListener {
            Toast.makeText(activity, responseData.getString("type"), Toast.LENGTH_SHORT).show()
            MagePrefs.saveFilterType(responseData.getString("type"))
            /*if(responseData.getString("label").equals("Price")){
                val inflater = LayoutInflater
                    .from(activity)
                val view: View = inflater.inflate(R.layout.m_productfilterpriceitem, null)
                layoutToAdd.addView(view)
            }*/
            if (MagePrefs.getFilterType() != null) {
                selectFilter(MagePrefs.getFilterType())
            }
        }
    }

    private fun selectFilter(filterType: String?) {

    }

    override fun getItemCount(): Int {
        return jsonArray!!.length()
    }
}
