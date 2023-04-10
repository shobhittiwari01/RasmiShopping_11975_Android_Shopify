package com.rasmishopping.app.basesection.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.viewholders.CircleData
import com.rasmishopping.app.databinding.MCircleitemBinding
import com.rasmishopping.app.sharedprefsection.MagePrefs
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class CustomCircleCategoryAdapter @Inject
constructor() : RecyclerView.Adapter<CircleData>() {
    var activity: Activity? = null
    lateinit var jsonobject: JSONArray

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CircleData {
        val binding = DataBindingUtil.inflate<MCircleitemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_circleitem, parent, false)
        return CircleData(binding)
    }

    override fun getItemCount(): Int {
        return jsonobject.length()
    }

    override fun onBindViewHolder(holder: CircleData, position: Int) {
        Log.i("CIRCLEJSON","2 " +jsonobject)
        var model=CommanModel()
        val requestOptions: RequestOptions = RequestOptions().circleCrop()
        when(MagePrefs.getTheme()){
            "Grocery Theme" -> {
                model.imageurl=(jsonobject.get(position) as JSONObject).getString("image_url")
//                Glide.with(activity!!).load(imageIds[position]).placeholder(imageIds[position]).apply(requestOptions).into(holder.binding.catimage)
            }
            "Fashion Theme" -> {
                model.imageurl=(jsonobject.get(position) as JSONObject).getString("image_url")
//                Glide.with(activity!!).load(fimageIds[position]).placeholder(fimageIds[position]).apply(requestOptions).into(holder.binding.catimage)
            }
        }
        holder.binding.common=model
        //Glide.with(activity!!).load(imageIds[position]).placeholder(imageIds[position]).apply(requestOptions).into(holder.binding.catimage)
        if(jsonobject.getJSONObject(position).has("title")){
            holder.binding.cattext.text = jsonobject.getJSONObject(position).getString("title")
            holder.binding.cattext.visibility = View.VISIBLE
        } else {
            holder.binding.cattext.visibility = View.GONE
        }
    }

    fun setData(jsonObject: JSONArray, activity: Activity) {
        this.activity = activity
        this.jsonobject = jsonObject
    }
}
