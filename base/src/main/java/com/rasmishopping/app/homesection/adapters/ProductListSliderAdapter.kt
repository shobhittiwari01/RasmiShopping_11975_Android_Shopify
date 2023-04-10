package com.rasmishopping.app.homesection.adapters

import android.app.Activity
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
import com.rasmishopping.app.databinding.MCustomisableListBinding
import com.rasmishopping.app.homesection.viewholders.SliderItemTypeOne
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.CurrencyFormatter
import org.json.JSONObject
import java.math.BigDecimal
import javax.inject.Inject

class

ProductListSliderAdapter @Inject
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
        var listbinding = DataBindingUtil.inflate<MCustomisableListBinding>(LayoutInflater.from(parent.context), R.layout.m_customisable_list, parent, false)
        when (jsonObject!!.getString("item_shape")) {
            "square" -> {
                listbinding.productsection.radius = 0f
                listbinding.productsection.cardElevation = 0f
                listbinding.imagesection.radius = 0f
                listbinding.imagesection.cardElevation = 0f
            }
        }
        var alignment: String
        if (jsonObject!!.has("item_text_alignment")) {
            alignment = jsonObject!!.getString("item_text_alignment")
        } else {
            alignment = jsonObject!!.getString("item_alignment")
        }
        when (alignment){
            "right" -> { listbinding.itemDataSection.gravity=Gravity.END or Gravity.CENTER_VERTICAL }
            "center" -> { listbinding.itemDataSection.gravity=Gravity.CENTER }
        }
        if (jsonObject!!.getString("item_border").equals("1")) {
            if(HomePageViewModel.isLightModeOn()){
                val item_border_color = JSONObject(jsonObject!!.getString("item_border_color"))
                listbinding.productsection.setCardBackgroundColor(Color.parseColor(item_border_color.getString("color")))
                listbinding.innerproductsection.setBackgroundColor(Color.parseColor(item_border_color.getString("color")))
            }else{
                listbinding.innerproductsection.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.black))
            }
        }else{
            val newLayoutParams = listbinding.innerproductsection.getLayoutParams() as FrameLayout.LayoutParams
            newLayoutParams.setMargins(0,0,0,0)
            listbinding.innerproductsection.setLayoutParams(newLayoutParams)
            if(HomePageViewModel.isLightModeOn()){
                val namepricesection = JSONObject(jsonObject!!.getString("cell_background_color"))
                listbinding.innerproductsection.setBackgroundColor(Color.parseColor(namepricesection.getString("color")))
            }else{
                listbinding.innerproductsection.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
            }
        }
        if (jsonObject!!.getString("item_title").equals("0")&&jsonObject!!.getString("item_price").equals("0")) {
            listbinding.itemDataSection.visibility=View.GONE
        }
        if (jsonObject!!.getString("item_title").equals("1")){
            listbinding.name.visibility=View.VISIBLE
            if(HomePageViewModel.isLightModeOn()){
                var item_title_color = JSONObject(jsonObject!!.getString("item_title_color"))
                listbinding.name.setTextColor(Color.parseColor(item_title_color.getString("color")))
            }
            when (jsonObject!!.getString("item_title_font_weight")) {
                "bold" -> {
                    listbinding.name.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                }
                "light" -> {
                    listbinding.name.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                }
                "medium" -> {
                    listbinding.name.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                }
            }
            if (jsonObject!!.getString("item_title_font_style").equals("italic")) {
                listbinding.name.setTypeface(listbinding.name.typeface, Typeface.ITALIC)
            }
        }
        if (jsonObject!!.getString("item_price").equals("1")){
            listbinding.pricesection.visibility=View.VISIBLE
            if(HomePageViewModel.isLightModeOn()){
                var item_price_color = JSONObject(jsonObject!!.getString("item_price_color"))
                listbinding.regularprice.setTextColor(Color.parseColor(item_price_color.getString("color")))
            }
            when (jsonObject!!.getString("item_price_font_weight")) {
                "bold" -> {
                    listbinding.regularprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                }
                "light" -> {
                    listbinding.regularprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                }
                "medium" -> {
                    listbinding.regularprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                }
            }
            if (jsonObject!!.getString("item_price_font_style").equals("italic")) {
                listbinding.regularprice.setTypeface(listbinding.regularprice.typeface, Typeface.ITALIC)
            }
            if (jsonObject!!.getString("item_compare_at_price").equals("1")) {
                listbinding.specialprice.visibility=View.VISIBLE
                if(HomePageViewModel.isLightModeOn()){
                    var item_compare_at_price_color = JSONObject(jsonObject!!.getString("item_compare_at_price_color"))
                    listbinding.specialprice.setTextColor(Color.parseColor(item_compare_at_price_color.getString("color")))
                }
                when (jsonObject!!.getString("item_compare_at_price_font_weight")) {
                    "bold" -> {
                        listbinding.specialprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                    }
                    "light" -> {
                        listbinding.specialprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                    }
                    "medium" -> {
                        listbinding.specialprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                    }
                }
                if (jsonObject!!.getString("item_compare_at_price_font_style").equals("italic")) {
                    listbinding.specialprice.setTypeface(listbinding.specialprice.typeface, Typeface.ITALIC)
                }
            }
        }
        if(HomePageViewModel.isLightModeOn()){
            val namepricesection = JSONObject(jsonObject!!.getString("cell_background_color"))
            listbinding.itemDataSection.setBackgroundColor(Color.parseColor(namepricesection.getString("color")))
        }
        return SliderItemTypeOne(listbinding)
    }

    override fun onBindViewHolder(item: SliderItemTypeOne, position: Int) {
        var variant: Storefront.ProductVariant? =null
        if(catproducts!=null){
            variant=catproducts!!.get(position)!!.node.variants.edges.get(0).node
        }else{
            variant= products!!.get(position)!!.variants.edges.get(0).node!!
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
                    item.listbinding.regularprice.paintFlags = item.listbinding.regularprice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    item.listbinding.specialprice.visibility = View.VISIBLE
                }else{
                    item.listbinding.specialprice.visibility = View.GONE
                    // item.listbinding.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
            else {
                item.listbinding.specialprice.visibility = View.GONE
                // item.listbinding.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        } else {
            item.listbinding.specialprice.visibility = View.GONE
            // item.listbinding.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        val model = CommanModel()
        model.imageurl=""
        if (product.images?.edges?.size!! > 0) {
            model.imageurl = product.images?.edges?.get(0)?.node?.url
            Log.i("SaifDev_SliderLIst","SliderList"+product.images?.edges?.get(0)?.node?.url)
            if (product.images?.edges!!.size > 0) {
                val height = product.images?.edges?.get(0)?.node?.height
                val width = product.images?.edges?.get(0)?.node?.width
              //  (item.listbinding.imagesection.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "W,$width:$height"
                Log.i("SaifDev_SliderLIst","ImageWidth"+width +" : "+"ImageHeight"+height)
            }
        }
        item.listbinding.listdata = data
        item.listbinding.commondata = model
        item.listbinding.clickproduct = ProductSliderGridAdapter().Product( activity!!)
        if (SplashViewModel.featuresModel.outOfStock!!) {
            if (!product.availableForSale) {
                item.listbinding.outOfStock.visibility = View.VISIBLE
            } else {
                item.listbinding.outOfStock.visibility = View.GONE
            }
        }
        Constant.translateField(activity!!.resources.getString(R.string.out_of_stock)!!,item.listbinding.outOfStock)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        if (catproducts!=null){
            return catproducts!!.size
        }else{
            return products!!.size
        }
    }
}
