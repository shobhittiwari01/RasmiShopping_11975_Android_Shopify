package com.rasmishopping.app.homesection.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.models.ListData
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.customviews.MageNativeTextView
import com.rasmishopping.app.databinding.ProductGridItemsBinding
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.CurrencyFormatter
import org.json.JSONObject
import java.math.BigDecimal

class ProductTwoGridAdapter : RecyclerView.Adapter<ProductTwoGridAdapter.ProductGridItems>() {
    private var layoutInflater: LayoutInflater? = null
    private var products: List<Storefront.Product>? = null
    private var catproducts: List<Storefront.ProductEdge>? = null
    private var activity: Activity? = null
    private val TAG = "ProductSliderListAdapte"
    var jsonObject: JSONObject? = null
    lateinit var repository: Repository
    fun setData(
        products: List<Storefront.Product>?,
        activity: Activity,
        jsonObject: JSONObject,
        repository: Repository
    ) {
        this.products = products
        this.activity = activity
        this.jsonObject = jsonObject
        this.repository = repository
    }
    fun set_Data(catproducts: List<Storefront.ProductEdge>?, catactivity: Activity, catjsonObject: JSONObject, catrepository: Repository) {
        this.catproducts = catproducts
        this.activity = catactivity
        this.jsonObject = catjsonObject
        this.repository = catrepository
    }
    init {
        setHasStableIds(true)
    }
    class ProductGridItems : RecyclerView.ViewHolder {
        var binding: ProductGridItemsBinding
        constructor(itemView: ProductGridItemsBinding) : super(itemView.root) {
            this.binding = itemView
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductGridItems {
        var binding = DataBindingUtil.inflate<ProductGridItemsBinding>(LayoutInflater.from(parent.context), R.layout.product_grid_items, parent, false) as ProductGridItemsBinding
        when (jsonObject!!.getString("item_shape")) {
            "square" -> {
                binding.productsection.radius = 0f
                binding.productsection.cardElevation = 0f
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
            "right" -> { binding.itemDataSection.gravity= Gravity.END }
            "center" -> { binding.itemDataSection.gravity= Gravity.CENTER }
        }
        if (jsonObject!!.getString("item_border").equals("1")) {
            if(HomePageViewModel.isLightModeOn()){
                val item_border_color = JSONObject(jsonObject!!.getString("item_border_color"))
                binding.productsection.setCardBackgroundColor(Color.parseColor(item_border_color.getString("color")))
                binding.imagepart.setBackgroundColor(Color.parseColor(item_border_color.getString("color")))
                var panel_background_color = JSONObject(jsonObject!!.getString("cell_background_color")).getString("color")
                binding.innerproductsection.setBackgroundColor(Color.parseColor(panel_background_color))
            }else{
                binding.innerproductsection.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
                binding.imagepart.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.black))
            }

        }else{
            val newLayoutParams = binding.innerproductsection.getLayoutParams() as FrameLayout.LayoutParams
            newLayoutParams.setMargins(0,0,0,0)
            binding.innerproductsection.setLayoutParams(newLayoutParams)
            if(HomePageViewModel.isLightModeOn()){
                var panel_background_color = JSONObject(jsonObject!!.getString("cell_background_color")).getString("color")
                binding.innerproductsection.setBackgroundColor(Color.parseColor(panel_background_color))
                binding.productsection.setCardBackgroundColor(Color.parseColor(panel_background_color))
            }else{
                binding.innerproductsection.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
                binding.productsection.setCardBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
            }
        }
        if (jsonObject!!.getString("item_title").equals("0")&&jsonObject!!.getString("item_price").equals("0")) {
            binding.itemDataSection.visibility=View.GONE
        }
        if (jsonObject!!.getString("item_title").equals("1")){
            binding.name.visibility=View.VISIBLE
            if(HomePageViewModel.isLightModeOn()){
                var item_title_color = JSONObject(jsonObject!!.getString("item_title_color"))
                binding.name.setTextColor(Color.parseColor(item_title_color.getString("color")))
            }
            when (jsonObject!!.getString("item_title_font_weight")) {
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
            if (jsonObject!!.getString("item_title_font_style").equals("italic")) {
                binding.name.setTypeface(binding.name.typeface, Typeface.ITALIC)
            }
        }
        if (jsonObject!!.getString("item_price").equals("1")){
            binding.pricesection.visibility=View.VISIBLE
            if(HomePageViewModel.isLightModeOn()){
                var item_price_color = JSONObject(jsonObject!!.getString("item_price_color"))
                binding.regularprice.setTextColor(Color.parseColor(item_price_color.getString("color")))
            }
            when (jsonObject!!.getString("item_price_font_weight")) {
                "bold" -> {
                    binding.regularprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                }
                "light" -> {
                    binding.regularprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                }
                "medium" -> {
                    binding.regularprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                }
            }
            if (jsonObject!!.getString("item_price_font_style").equals("italic")) {
                binding.regularprice.setTypeface(binding.regularprice.typeface, Typeface.ITALIC)
            }
            if (jsonObject!!.getString("item_compare_at_price").equals("1")) {
                binding.specialprice.visibility=View.VISIBLE
                if(HomePageViewModel.isLightModeOn()){
                    var item_compare_at_price_color = JSONObject(jsonObject!!.getString("item_compare_at_price_color"))
                    binding.specialprice.setTextColor(Color.parseColor(item_compare_at_price_color.getString("color")))
                }
                when (jsonObject!!.getString("item_compare_at_price_font_weight")) {
                    "bold" -> {
                        binding.specialprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                    }
                    "light" -> {
                        binding.specialprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                    }
                    "medium" -> {
                        binding.specialprice.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                    }
                }
                if (jsonObject!!.getString("item_compare_at_price_font_style").equals("italic")) {
                    binding.specialprice.setTypeface(binding.specialprice.typeface, Typeface.ITALIC)
                }
            }
        }
        if(HomePageViewModel.isLightModeOn()){
            val namepricesection = JSONObject(jsonObject!!.getString("cell_background_color"))
            binding.itemDataSection.setBackgroundColor(Color.parseColor(namepricesection.getString("color")))
        }
        return ProductGridItems(binding)
    }

    override fun onBindViewHolder(item: ProductGridItems, position: Int) {
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
                    item.binding.regularprice.paintFlags = item.binding.regularprice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    item.binding.specialprice.visibility = View.VISIBLE
                }else{
                    item.binding.specialprice.visibility = View.GONE
                    // item.binding.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
            else {
                item.binding.specialprice.visibility = View.GONE
                // item.binding.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        } else {
            item.binding.specialprice.visibility = View.GONE
            // item.binding.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
        val model = CommanModel()
        model.imageurl=""
        if (product.images?.edges?.size!! > 0) {
            model.imageurl = product.images?.edges?.get(0)?.node?.url
            Log.i("SaifDev_TwoGridAdapter","TwoGridAdapter"+product.images?.edges?.get(0)?.node?.url)
            if (product.images?.edges!!.size > 0) {
                val height = product.images?.edges?.get(0)?.node?.height
                val width = product.images?.edges?.get(0)?.node?.width
                (item.binding.imagesection.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,$width:$height"
                Log.i("SaifDev_TwoGridAdapter","ImageWidth"+width +" : "+"ImageHeight"+height)
            }
        }
        item.binding.listdata = data
        item.binding.commondata = model
        item.binding.clickproduct = ProductTwoGridAdapter().Product( activity!!)
        if (SplashViewModel.featuresModel.outOfStock!!) {
            if (!product.availableForSale) {
                item.binding.outOfStock.visibility = View.VISIBLE
            } else {
                item.binding.outOfStock.visibility = View.GONE
            }
        }
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
    inner class Product(var activity: Activity) {
        fun productClick(view: View, data: ListData) {
            val productintent = Intent(view.context, ProductView::class.java)
            productintent.putExtra("ID", data.product!!.id.toString())
            productintent.putExtra("tittle", data.textdata)
            view.context.startActivity(productintent)
            Constant.activityTransition(view.context)
        }
    }
}
