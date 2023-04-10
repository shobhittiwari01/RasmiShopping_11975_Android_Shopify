package com.rasmishopping.app.homesection.adapters
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonElement
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.collectionsection.models.Collection
import com.rasmishopping.app.collectionsection.viewholders.CollectionItem
import com.rasmishopping.app.databinding.MCategorygriditemBinding
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.utils.Constant
import org.json.JSONObject
import javax.inject.Inject
class CollectionGridAdapter @Inject
constructor() : RecyclerView.Adapter<CollectionItem>() {
    lateinit var collectionEdges: List<JsonElement>
    lateinit var jsonObject: JSONObject
    var activity: Activity? = null
        private set
    fun setData(collectionEdges: List<JsonElement>, activity: Activity, jsonObject: JSONObject) {
        this.collectionEdges = collectionEdges
        this.activity = activity
        this.jsonObject = jsonObject
    }
    init {
        setHasStableIds(true)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionItem {
        val binding = DataBindingUtil.inflate<MCategorygriditemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_categorygriditem,
            parent,
            false
        )
        try {
            when (jsonObject.getString("item_shape")) {
                "square" -> {
                    binding.main.radius = 0f
                    binding.main.cardElevation = 0f
                    binding.imagesection.radius = 0f
                    binding.imagesection.cardElevation = 0f
                }
            }
            var alignment: String
            if (jsonObject!!.has("item_text_alignment")) {
                alignment = jsonObject!!.getString("item_text_alignment")
            } else {
                alignment = jsonObject!!.getString("item_alignment")
            }
            when (alignment){
                "right" -> { binding.itemDataSection.gravity=Gravity.END or Gravity.CENTER_VERTICAL}
                "center" -> { binding.itemDataSection.gravity=Gravity.CENTER }
            }
            when(jsonObject.getString("item_title")){
                "1"->{
                    binding.itemDataSection.visibility=View.VISIBLE
                    if(HomePageViewModel.isLightModeOn()){
                        var background = JSONObject(jsonObject.getString("item_title_color"))
                        binding.name.setTextColor(Color.parseColor(background.getString("color")))
                    }
                }
            }
            if (jsonObject!!.getString("item_border").equals("1")) {
                if(HomePageViewModel.isLightModeOn()){
                    val item_border_color = JSONObject(jsonObject!!.getString("item_border_color"))
                    binding.main.setCardBackgroundColor(Color.parseColor(item_border_color.getString("color")))
                    binding.imagepart.setBackgroundColor(Color.parseColor(item_border_color.getString("color")))
                    var panel_background_color = JSONObject(jsonObject!!.getString("cell_background_color")).getString("color")
                    binding.innerproductsection.setBackgroundColor(Color.parseColor(panel_background_color))
                }else{
                    binding.innerproductsection.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
                    binding.imagepart.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.black))
                }
            }else{
                if(HomePageViewModel.isLightModeOn()){
                    var panel_background_color = JSONObject(jsonObject!!.getString("cell_background_color")).getString("color")
                    binding.innerproductsection.setBackgroundColor(Color.parseColor(panel_background_color))
                    binding.main.setCardBackgroundColor(Color.parseColor(panel_background_color))
                }else{
                    binding.innerproductsection.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
                    binding.main.setCardBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
                }
                val newLayoutParams = binding.innerproductsection.getLayoutParams() as FrameLayout.LayoutParams
                newLayoutParams.setMargins(0,0,0,0)
                binding.innerproductsection.setLayoutParams(newLayoutParams)
            }
            if(HomePageViewModel.isLightModeOn()){
                var background = JSONObject(jsonObject.getString("cell_background_color"))
                binding.itemDataSection.setBackgroundColor(Color.parseColor(background.getString("color")))
            }
            when(jsonObject.getString("item_font_weight")){
                "bold"->{
                    binding.name.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                }
                "medium"->{
                    binding.name.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                }
                "light"->{
                    binding.name.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                }
            }
            if (jsonObject.getString("item_font_style").equals("italic")) {
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
            if (collectionEdges.get(position) != null) {
                val model = CommanModel()
                if (collectionEdges.get(position).asJsonObject.has("image_url")) {
                    Log.i("CAME", "NOTINGROCERY")
                    model.imageurl =
                        collectionEdges.get(position).asJsonObject?.get("image_url")?.asString
                    holder.gridbinding.commondata = model
                }
            }
            val collection = Collection()
            if (collectionEdges.get(position).asJsonObject.has("title")) {
                val name = collectionEdges.get(position).asJsonObject.get("title").asString
                collection.category_name = name
                Constant.translateField(collection.category_name!!,holder.gridbinding.name)
            }
            if (collectionEdges.get(position).asJsonObject.has("link_type")) {
                collection.type =
                    collectionEdges.get(position).asJsonObject.get("link_type").asString
            }
            if (collectionEdges.get(position).asJsonObject.has("link_value")) {
                collection.value =
                    collectionEdges.get(position).asJsonObject.get("link_value").asString
            }
            holder.gridbinding.categorydata = collection
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        Log.i("TEST", "" + collectionEdges.size)
        return collectionEdges.size

    }

}
