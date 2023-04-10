package com.rasmishopping.app.basesection.adapters

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.collectionsection.models.Collection
import com.rasmishopping.app.collectionsection.viewholders.CollectionItem
import com.rasmishopping.app.databinding.MCollectionItemsBinding
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class CustomCollectionSliderAdapter @Inject
constructor() : RecyclerView.Adapter<CollectionItem>() {
    private var layoutInflater: LayoutInflater? = null
    lateinit var collectionEdges: JSONArray
    lateinit var jsonObject: JSONObject
    var activity: Activity? = null
        private set

    fun setData(collectionEdges: JSONArray, activity: Activity, jsonObject: JSONObject) {
        this.collectionEdges = collectionEdges
        this.activity = activity
        this.jsonObject = jsonObject
    }
    init {
        setHasStableIds(true)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionItem {
        val binding = DataBindingUtil.inflate<MCollectionItemsBinding>(LayoutInflater.from(parent.context), R.layout.m_collection_items, parent, false)
        try {
            when(jsonObject.getString("item_shape")){
                "square"->{
                    binding.card.radius=0f
                    binding.card.cardElevation=0f
                    binding.card.useCompatPadding=false
                }
            }
            when (jsonObject.getString("item_text_alignment")) {
                "left" -> {
                    binding.name.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                }
                "right" -> {
                    binding.name.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                }
            }
            when(jsonObject.getString("item_border")){
                "1"->{
                    val background = JSONObject(jsonObject.getString("item_border_color"))
                    binding.card.setCardBackgroundColor(Color.parseColor(background.getString("color")))
                    val params=binding.image.layoutParams as FrameLayout.LayoutParams
                    params.setMargins(3,3,3,3)
                    binding.image.layoutParams=params
                    binding.frame.layoutParams=params
                }
            }
            when (jsonObject.getString("item_title_font_weight")) {
                "bold" -> {
                    binding.name.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                }
                "light" -> {
                    binding.name.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                }
                "medium" -> {
                    binding.name.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                }
            }
            if (jsonObject.getString("item_title_font_style").equals("italic")) {
                binding.name.setTypeface(binding.name.typeface, Typeface.ITALIC)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return CollectionItem(binding)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: CollectionItem, position: Int) {
        try {
            if (collectionEdges.getJSONObject(position) != null) {
                val model = CommanModel()
                if (collectionEdges.getJSONObject(position).has("image_url")) {
//                    var fimageIds = intArrayOf(R.drawable.fcollslidefirst, R.drawable.fcollslidesec, R.drawable.fcollslidethird)
//                    var gimageIds = intArrayOf(R.drawable.gcollslidefirst, R.drawable.gcollslidesec, R.drawable.gcollslidethird)
//                    var himageIds = intArrayOf(R.drawable.hcollslidefirst, R.drawable.hcollslidesec, R.drawable.hcollslidethird)
                    /*model.imageurl = collectionEdges.getJSONObject(position)?.getString("image_url")
                    holder.collectionbinding.commondata = model*/
                    when(MagePrefs.getTheme()) {
                        "Grocery Theme" -> {
                            model.imageurl=collectionEdges.getJSONObject(position)?.getString("image_url")
//                            Glide.with(activity!!).load(collectionEdges.getJSONObject(position)?.getString("image_url")).placeholder(gimageIds[position]).into(holder.collectionbindings.image)
                        }
                        "Fashion Theme" -> {
                            model.imageurl=collectionEdges.getJSONObject(position)?.getString("image_url")
//                            Glide.with(activity!!).load(collectionEdges.getJSONObject(position)?.getString("image_url")).placeholder(fimageIds[position]).into(holder.collectionbindings.image)
                        }
                        "Home Theme" -> {
                            model.imageurl=collectionEdges.getJSONObject(position)?.getString("image_url")
//                            Glide.with(activity!!).load(collectionEdges.getJSONObject(position)?.getString("image_url")).placeholder(himageIds[position]).into(holder.collectionbindings.image)
                        }
                    }
                    holder.collectionbindings.commondata = model
                }
            }
            val collection = Collection()
            if (collectionEdges.getJSONObject(position).has("title")) {
                val name = collectionEdges.getJSONObject(position).getString("title")
                collection.category_name = name
                Constant.translateField(collection.category_name!!,holder.collectionbindings.name)
            }
            if (collectionEdges.getJSONObject(position).has("link_type")) {
                collection.type = collectionEdges.getJSONObject(position).getString("link_type")
            }
            if (collectionEdges.getJSONObject(position).has("link_value")) {
                collection.value = collectionEdges.getJSONObject(position).getString("link_value")
            }
            holder.collectionbindings.categorydata = collection
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        Log.i("MageNative", "GridSize" + collectionEdges.length())
        return collectionEdges.length()
    }

}