package com.rasmishopping.app.homesection.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.models.ListData
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.databinding.MMultiplegridBinding
import com.rasmishopping.app.homesection.viewholders.SliderItemTypeOne
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.CurrencyFormatter
import org.json.JSONObject
import java.math.BigDecimal
import javax.inject.Inject

class ProductSliderGridAdapter @Inject
constructor() : RecyclerView.Adapter<SliderItemTypeOne>() {
    private var products: List<Storefront.Product>? = null
    private var catproducts: List<Storefront.ProductEdge>? = null
    private var activity: Activity? = null
    var jsonObject: JSONObject? = null
    fun setData(products: List<Storefront.Product>?, activity: Activity, jsonObject: JSONObject) {
        this.products = products
        this.activity = activity
        this.jsonObject = jsonObject
    }
    fun set_Data(catproducts: List<Storefront.ProductEdge>?, catactivity: Activity, catjsonObject: JSONObject) {
        this.catproducts = catproducts
        this.activity = catactivity
        this.jsonObject = catjsonObject
    }
    init {
        setHasStableIds(true)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderItemTypeOne {
        var gridbinding = DataBindingUtil.inflate<MMultiplegridBinding>(LayoutInflater.from(parent.context), R.layout.m_multiplegrid, parent, false)
        when (jsonObject!!.getString("item_shape")) {
            "square" -> {
                gridbinding.productsection.radius = 0f
                gridbinding.productsection.cardElevation = 0f
                gridbinding.imagesection.radius = 0f
                gridbinding.imagesection.cardElevation = 0f
            }
        }
        var alignment: String
        if (jsonObject!!.has("item_text_alignment")) {
            alignment = jsonObject!!.getString("item_text_alignment")
        } else {
            alignment = jsonObject!!.getString("item_alignment")
        }
        when (alignment){
            "right" -> { gridbinding.itemDataSection.gravity=Gravity.END  }
            "center" -> { gridbinding.itemDataSection.gravity=Gravity.CENTER }
        }
        if (jsonObject!!.getString("item_border").equals("1")) {
            if(HomePageViewModel.isLightModeOn()){
                val item_border_color = JSONObject(jsonObject!!.getString("item_border_color"))
                gridbinding.productsection.setCardBackgroundColor(Color.parseColor(item_border_color.getString("color")))
                gridbinding.imagepart.setBackgroundColor(Color.parseColor(item_border_color.getString("color")))
                var panel_background_color = JSONObject(jsonObject!!.getString("cell_background_color")).getString("color")
                gridbinding.innerproductsection.setBackgroundColor(Color.parseColor(panel_background_color))
            }else{
                gridbinding.innerproductsection.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
                gridbinding.imagepart.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.black))
            }
        }else{
            val newLayoutParams = gridbinding.innerproductsection.getLayoutParams() as FrameLayout.LayoutParams
            newLayoutParams.setMargins(0,0,0,0)
            gridbinding.innerproductsection.setLayoutParams(newLayoutParams)
            if(HomePageViewModel.isLightModeOn()){
                var panel_background_color = JSONObject(jsonObject!!.getString("cell_background_color")).getString("color")
                gridbinding.innerproductsection.setBackgroundColor(Color.parseColor(panel_background_color))
                gridbinding.productsection.setCardBackgroundColor(Color.parseColor(panel_background_color))
            }else{
                gridbinding.innerproductsection.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
                gridbinding.productsection.setCardBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
            }
        }
        if (jsonObject!!.getString("item_title").equals("0")&&jsonObject!!.getString("item_price").equals("0")) {
            gridbinding.itemDataSection.visibility=View.GONE
        }
        if (jsonObject!!.getString("item_title").equals("1")){
            gridbinding.name.visibility=View.VISIBLE
            if(HomePageViewModel.isLightModeOn()){
                var item_title_color = JSONObject(jsonObject!!.getString("item_title_color"))
                gridbinding.name.setTextColor(Color.parseColor(item_title_color.getString("color")))
            }
            when (jsonObject!!.getString("item_title_font_weight")) {
                "bold" -> {
                    gridbinding.name.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                }
                "light" -> {
                    gridbinding.name.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                }
                "medium" -> {
                    gridbinding.name.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                }
            }
            if (jsonObject!!.getString("item_title_font_style").equals("italic")) {
                gridbinding.name.setTypeface(gridbinding.name.typeface, Typeface.ITALIC)
            }
        }
        if (jsonObject!!.getString("item_price").equals("1")){
            gridbinding.pricesection.visibility=View.VISIBLE
            if(HomePageViewModel.isLightModeOn()){
                var item_price_color = JSONObject(jsonObject!!.getString("item_price_color"))
                gridbinding.regularprice.setTextColor(Color.parseColor(item_price_color.getString("color")))
            }
            when (jsonObject!!.getString("item_price_font_weight")) {
                "bold" -> {
                    gridbinding.regularprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                }
                "light" -> {
                    gridbinding.regularprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                }
                "medium" -> {
                    gridbinding.regularprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                }
            }
            if (jsonObject!!.getString("item_price_font_style").equals("italic")) {
                gridbinding.regularprice.setTypeface(gridbinding.regularprice.typeface, Typeface.ITALIC)
            }
            if (jsonObject!!.getString("item_compare_at_price").equals("1")) {
                gridbinding.specialprice.visibility=View.VISIBLE
                if(HomePageViewModel.isLightModeOn()){
                    var item_compare_at_price_color = JSONObject(jsonObject!!.getString("item_compare_at_price_color"))
                    gridbinding.specialprice.setTextColor(Color.parseColor(item_compare_at_price_color.getString("color")))
                }
                when (jsonObject!!.getString("item_compare_at_price_font_weight")) {
                    "bold" -> {
                        gridbinding.specialprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                    }
                    "light" -> {
                        gridbinding.specialprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                    }
                    "medium" -> {
                        gridbinding.specialprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                    }
                }
                if (jsonObject!!.getString("item_compare_at_price_font_style").equals("italic")) {
                    gridbinding.specialprice.setTypeface(gridbinding.specialprice.typeface, Typeface.ITALIC)
                }
            }
        }
        if(HomePageViewModel.isLightModeOn()){
            val namepricesection = JSONObject(jsonObject!!.getString("cell_background_color"))
            gridbinding.itemDataSection.setBackgroundColor(Color.parseColor(namepricesection.getString("color")))
        }
        return SliderItemTypeOne(gridbinding)
    }

    override fun onBindViewHolder(item: SliderItemTypeOne, position: Int) {
        var variant: Storefront.ProductVariant? =null
        if(catproducts!=null){
            variant=catproducts!!.get(position).node.variants.edges.get(0).node
        }else{
            variant= products!!.get(position).variants.edges.get(0).node!!
        }
        var product: Storefront.Product? =null
        if(catproducts!=null){
            product=catproducts!!.get(position).node!!
        }else{
            product= products!!.get(position)
        }
        val data = ListData()
        data.product = product
        data.textdata = product.title.toString().trim()
        data.regularprice = CurrencyFormatter.setsymbol(variant!!.price.amount, variant.price.currencyCode.toString())
        if (variant.compareAtPrice != null) {
            if (jsonObject!!.getString("item_compare_at_price").equals("1")) {
                val special = java.lang.Double.valueOf(variant.compareAtPrice.amount)
                val regular = java.lang.Double.valueOf(variant.price.amount)
                if (BigDecimal.valueOf(special).compareTo(BigDecimal.valueOf(regular)) == 1) {
                    data.regularprice = CurrencyFormatter.setsymbol(variant.compareAtPrice.amount, variant.compareAtPrice.currencyCode.toString())
                    data.specialprice = CurrencyFormatter.setsymbol(variant.price.amount, variant.price.currencyCode.toString())
                    item.gridbinding.regularprice.paintFlags = item.gridbinding.regularprice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    item.gridbinding.specialprice.visibility = View.VISIBLE
                }else{
                    item.gridbinding.specialprice.visibility = View.GONE
                    // item.gridbinding.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
            else {
                item.gridbinding.specialprice.visibility = View.GONE
                // item.gridbinding.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        } else {
            item.gridbinding.specialprice.visibility = View.GONE
            // item.gridbinding.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        val model = CommanModel()
        model.imageurl=""
        if (product.images?.edges?.size!! > 0) {
            model.imageurl = product.images?.edges?.get(0)?.node?.url
            Log.i("SaifDev_SliderGrid","SliderGrid"+product.images?.edges?.get(0)?.node?.url)
            if (product.images?.edges!!.size > 0) {
                val height = product.images?.edges?.get(0)?.node?.height
                val width = product.images?.edges?.get(0)?.node?.width
                (item.gridbinding.imagesection.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,$width:$height"
                Log.i("SaifDev_SliderGrid","ImageWidth"+width +" : "+"ImageHeight"+height)
            }
        }
        item.gridbinding.listdata = data
        item.gridbinding.commondata = model
        item.gridbinding.clickproduct = ProductSliderGridAdapter().Product( activity!!)
        if (SplashViewModel.featuresModel.outOfStock!!) {
            if (!product.availableForSale) {
                item.gridbinding.outOfStock.visibility = View.VISIBLE
            } else {
                item.gridbinding.outOfStock.visibility = View.GONE
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        if(catproducts!=null){
            return catproducts!!.size
        }else{
            return products!!.size
        }

    }
    inner class Product( var activity: Activity) {
        fun productClick(view: View, data: ListData) {
            val productintent = Intent(view.context, ProductView::class.java)
            productintent.putExtra("ID", data.product!!.id.toString())
            productintent.putExtra("tittle", data.textdata)
            view.context.startActivity(productintent)
            Constant.activityTransition(view.context)
        }

    }

}