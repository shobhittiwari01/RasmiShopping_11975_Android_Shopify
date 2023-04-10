package com.rasmishopping.app.homesection.adapters

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.CornerFamily
import com.google.gson.JsonElement
import com.rasmishopping.app.R
import com.rasmishopping.app.databinding.CategorySquareSliderBinding
import com.rasmishopping.app.homesection.models.CategoryCircle
import com.rasmishopping.app.homesection.models.SquareItem
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.utils.Constant
import org.json.JSONObject
import javax.inject.Inject


class CategorySquareAdapter  @Inject
constructor() : RecyclerView.Adapter<SquareItem>() {
    lateinit var collectionEdges: List<JsonElement>
    lateinit var jsonObject: JSONObject
    var activity: Activity? = null
    var shape="square"
    fun setData(collectionEdges: List<JsonElement>, activity: Activity, jsonObject: JSONObject) {
        this.collectionEdges = collectionEdges
        this.activity = activity
        this.jsonObject = jsonObject
    }
    init {
        setHasStableIds(true)
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SquareItem {
        val binding = DataBindingUtil.inflate<CategorySquareSliderBinding>(
            LayoutInflater.from(parent.context),
            R.layout.category_square_slider,
            parent,
            false
        )
        try {

            if (jsonObject.getString("item_title").equals("0")) {
                binding.catTextOne.visibility = View.GONE
            }
            if(jsonObject.getString("item_shape").equals("rectangle")){
                shape="rectangle"
                (binding.cardOne.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio="H,250:190"
                val newLayoutParams = binding.imageOne.getLayoutParams() as FrameLayout.LayoutParams
                newLayoutParams.setMargins(2,2,2,2)
                binding.imageOne.layoutParams=newLayoutParams
            }
            if (jsonObject.getString("item_border").equals("1")) {
                var bordercolor="#FFFFFF"
                if(HomePageViewModel.isLightModeOn()){
                    var item_border_color = JSONObject(jsonObject.getString("item_border_color"))
                    bordercolor=item_border_color.getString("color")
                }
                binding.cardOne.setCardBackgroundColor(Color.parseColor(bordercolor))
            } else {
                val newLayoutParams = binding.imageOne.getLayoutParams() as FrameLayout.LayoutParams
                newLayoutParams.setMargins(0,0,0,0)
                binding.imageOne.layoutParams=newLayoutParams
                binding.cardOne.setCardBackgroundColor(Color.parseColor("#0000ffff"))
            }
            if(jsonObject.has("corner_radius")) {
                var card=HomePageViewModel.getCornerRadius(jsonObject.getString("corner_radius"))
                var image=0f
                when(jsonObject.getString("corner_radius")) {
                    "4","8"->{
                        image=jsonObject.getString("corner_radius").toFloat()
                    }
                    "12"->{
                        image=20f
                    }
                    "16"->{
                        if (shape.equals("rectangle")){
                            image=25f
                        }else{
                            image=23f
                        }
                    }
                    "20"->{
                        if (shape.equals("rectangle")){
                            image=26f
                        }else{
                            image=32f
                        }
                    }
                }
                var carddp= HomePageViewModel.applyDimension(COMPLEX_UNIT_DIP,card,
                    activity!!.resources.displayMetrics
                )
                var imagedp= HomePageViewModel.applyDimension(COMPLEX_UNIT_DIP,image,
                    activity!!.resources.displayMetrics
                )
                binding.cardOne.radius=carddp
                binding.imageOne.setShapeAppearanceModel(
                binding.imageOne.getShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, imagedp)
                    .build()
                )
            }
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
            binding.mainContainer?.getLayoutParams()?.width = devicewidth
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return SquareItem(binding)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun onBindViewHolder(holder: SquareItem, position: Int) {
        try {
            val collection = CategoryCircle()
            if (collectionEdges.get(position).asJsonObject.has("title")) {
                val name = collectionEdges.get(position).asJsonObject.get("title").asString
                collection.cat_text_one = name
                Constant.translateField(collection.cat_text_one!!,holder.squarebinding.catTextOne)
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
            holder.squarebinding.category = collection
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    override fun getItemCount(): Int {
        Log.i("TEST", "" + collectionEdges.size)
        return collectionEdges.size
    }

}
