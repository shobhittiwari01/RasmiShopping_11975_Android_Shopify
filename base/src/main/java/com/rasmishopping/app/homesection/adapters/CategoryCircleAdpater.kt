package com.rasmishopping.app.homesection.adapters
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonElement
import com.rasmishopping.app.R
import com.rasmishopping.app.databinding.CollectionCircleBinding
import com.rasmishopping.app.homesection.models.CategoryCircle
import com.rasmishopping.app.homesection.models.CategoryItem
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.utils.Constant
import org.json.JSONObject
import javax.inject.Inject
class CategoryCircleAdpater @Inject
constructor() : RecyclerView.Adapter<CategoryItem>() {
    lateinit var collectionEdges: List<JsonElement>
    lateinit var jsonObject: JSONObject
    var activity: Activity? = null
    fun setData(collectionEdges: List<JsonElement>, activity: Activity, jsonObject: JSONObject) {
        this.collectionEdges = collectionEdges
        this.activity = activity
        this.jsonObject = jsonObject
    }
    init {
        setHasStableIds(true)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryItem {
        val binding = DataBindingUtil.inflate<CollectionCircleBinding>(
            LayoutInflater.from(parent.context),
            R.layout.collection_circle,
            parent,
            false
        )
        try {
            if (jsonObject.getString("item_title").equals("0")) {
                binding.catTextOne.visibility = View.GONE
            }
            var item_border="#FFFFFF"
            if (jsonObject.getString("item_border").equals("1")) {
                if(HomePageViewModel.isLightModeOn()){
                    var item_border_color = JSONObject(jsonObject.getString("item_border_color"))
                    item_border = item_border_color.getString("color")
                }
            } else {
                if(HomePageViewModel.isLightModeOn()){
                    var cell_background_color = JSONObject(jsonObject.getString("panel_background_color"))
                    item_border = cell_background_color.getString("color")
                }
            }
            binding.imageOne.tag = item_border
            if(HomePageViewModel.isLightModeOn()){
                var background = JSONObject(jsonObject.getString("item_title_color"))
                binding.catTextOne.setTextColor(Color.parseColor(background.getString("color")))
            }
            var face = Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
            when(jsonObject.getString("item_font_weight")){
                "medium"->{
                    face = Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                }
                "light"->{
                    face = Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                }
                "bold"->{
                    face = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                }
            }
            binding.catTextOne.typeface = face
            when(jsonObject.getString("item_font_style")){
                "italic"->{
                    binding.catTextOne.setTypeface(binding.catTextOne.typeface, Typeface.ITALIC)
                }
            }
            val displaymetrics = DisplayMetrics()
            activity!!.windowManager.defaultDisplay.getMetrics(displaymetrics)
            val devicewidth: Int = (displaymetrics.widthPixels / 7 )+20
            binding?.maincat?.getLayoutParams()?.width = devicewidth
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return CategoryItem(binding)
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun onBindViewHolder(holder: CategoryItem, position: Int) {
        try {
            val collection = CategoryCircle()
            if (collectionEdges.get(position).asJsonObject.has("title")) {
                val name = collectionEdges.get(position).asJsonObject.get("title").asString
                collection.cat_text_one = name
                Constant.translateField(collection.cat_text_one!!,holder.circlebinding.catTextOne)
            }
            if (collectionEdges.get(position).asJsonObject.has("image_url")) {
                collection.cat_image_one =
                    collectionEdges.get(position).asJsonObject?.get("image_url")?.asString
            }
            if (collectionEdges.get(position).asJsonObject.has("link_type")) {
                collection.cat_link_one =
                    collectionEdges.get(position).asJsonObject.get("link_type").asString
            }
            if (collectionEdges.get(position).asJsonObject.has("link_value")) {
                collection.cat_value_one =
                    collectionEdges.get(position).asJsonObject.get("link_value").asString
            }
            holder.circlebinding.category = collection
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    override fun getItemCount(): Int {
        Log.i("TEST", "" + collectionEdges.size)
        return collectionEdges.size
    }
}
