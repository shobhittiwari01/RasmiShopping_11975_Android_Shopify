package com.rasmishopping.app.homesection.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.JsonReader
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
import com.rasmishopping.app.basesection.activities.NewBaseActivity.Companion.themeColor
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.models.ListData
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.databinding.MSlideritemoneBinding
import com.rasmishopping.app.homesection.viewholders.SliderItemTypeOne
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.quickadd_section.activities.QuickAddActivity
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.CurrencyFormatter
import org.json.JSONObject
import java.math.BigDecimal
import javax.inject.Inject


class ProductSliderListAdapter @Inject
constructor() : RecyclerView.Adapter<SliderItemTypeOne>() {
    private var layoutInflater: LayoutInflater? = null
    private var products: List<Storefront.Product>? = null
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

    /*init {
        setHasStableIds(true)
    }*/


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderItemTypeOne {
        var binding = DataBindingUtil.inflate<MSlideritemoneBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_slideritemone,
            parent,
            false
        )
        var backproduct=binding.inner.background as GradientDrawable
        when (jsonObject!!.getString("item_shape")) {
            "square" -> {
                binding.productsection.radius = 0f
                binding.productsection.cardElevation = 0f
                binding.imagesection.radius = 0f
                binding.imagesection.cardElevation = 0f
                backproduct.cornerRadius=0f
            }
        }
        var alignment: String
        if (jsonObject!!.has("item_text_alignment")) {
            alignment = jsonObject!!.getString("item_text_alignment")
        } else {
            alignment = jsonObject!!.getString("item_alignment")
        }
        when (alignment) {
            "right" -> {
                binding.itemDataSection.gravity = Gravity.END
            }
            "center" -> {
                binding.itemDataSection.gravity = Gravity.CENTER
            }
        }
        /*jsonObject!!.put("item_border","0")
        var json=JSONObject()
        json.put("color","#5EFF33")
        jsonObject!!.put("item_border_color",json)*/
        if (jsonObject!!.getString("item_border").equals("1")) {
            if(HomePageViewModel.isLightModeOn()){
                val item_border_color = JSONObject(jsonObject!!.getString("item_border_color"))
                binding.productsection.setCardBackgroundColor(Color.parseColor(item_border_color.getString("color")))
                backproduct.setStroke(1,Color.parseColor(item_border_color.getString("color")))
                if(jsonObject!!.has("cell_background_color")){
                    val namepricesection = JSONObject(jsonObject!!.getString("cell_background_color"))
                    backproduct.setColor(Color.parseColor(namepricesection.getString("color")))
                    binding.inner.background=backproduct
                }
            }
        } else {
            val newLayoutParams = binding.inner.layoutParams as FrameLayout.LayoutParams
            newLayoutParams.setMargins(0, 0, 0, 0)
            binding.inner.layoutParams = newLayoutParams
            binding.productsection.setCardBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
            backproduct.setStroke(0,ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
            if(HomePageViewModel.isLightModeOn()){
                if(jsonObject!!.has("cell_background_color")){
                    var panel_background_color = JSONObject(jsonObject!!.getString("cell_background_color")).getString("color")
                    backproduct.setColor(Color.parseColor(panel_background_color))
                }
            }else{
                backproduct.setColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
            }
        }
        if (jsonObject!!.getString("item_title").equals("0") && jsonObject!!.getString("item_price").equals("0")) {
            binding.itemDataSection.visibility = View.GONE
        }
        if (jsonObject!!.getString("item_title").equals("1")){
            binding.name.visibility=View.VISIBLE
            if(HomePageViewModel.isLightModeOn()){
                try{
                    var item_title_color = JSONObject(jsonObject!!.getString("item_title_color")).getString("color")
                     binding.name.setTextColor(Color.parseColor(item_title_color))
                }catch (e:Exception){
                    binding.name.setTextColor(Color.parseColor(themeColor))
                }
            }
            when (jsonObject!!.getString("item_title_font_weight")) {
                "bold" -> {
                    binding.name.typeface =
                        Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                }
                "light" -> {
                    binding.name.typeface =
                        Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                }
                "medium" -> {
                    binding.name.typeface =
                        Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                }
            }
            if (jsonObject!!.getString("item_title_font_style").equals("italic")) {
                binding.name.setTypeface(binding.name.typeface, Typeface.ITALIC)
            }
        }
        if (jsonObject!!.getString("item_price").equals("1")) {
            binding.pricesection.visibility = View.VISIBLE
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
                binding.specialprice.visibility = View.VISIBLE
                if(HomePageViewModel.isLightModeOn()){
                    var item_compare_at_price_color = JSONObject(jsonObject!!.getString("item_compare_at_price_color"))
                    binding.specialprice.setTextColor(Color.parseColor(item_compare_at_price_color.getString("color")))
                }
                when (jsonObject!!.getString("item_compare_at_price_font_weight")) {
                    "bold" -> {
                        binding.specialprice.typeface =
                            Typeface.createFromAsset(activity!!.assets, "fonts/popbold.ttf")
                    }
                    "light" -> {
                        binding.specialprice.typeface =
                            Typeface.createFromAsset(activity!!.assets, "fonts/poplight.ttf")
                    }
                    "medium" -> {
                        binding.specialprice.typeface =
                            Typeface.createFromAsset(activity!!.assets, "fonts/popmedium.ttf")
                    }
                }
                if (jsonObject!!.getString("item_compare_at_price_font_style").equals("italic")) {
                    binding.specialprice.setTypeface(binding.specialprice.typeface, Typeface.ITALIC)
                }
            }
        }
        if(HomePageViewModel.isLightModeOn()){
            if(jsonObject!!.has("cell_background_color")){
                val namepricesection = JSONObject(jsonObject!!.getString("cell_background_color"))
                binding.itemDataSection.setBackgroundColor(Color.parseColor(namepricesection.getString("color")))
            }
        }
        return SliderItemTypeOne(binding)
    }

    override fun onBindViewHolder(item: SliderItemTypeOne, position: Int) {
        val variant = products?.get(position)!!.variants.edges.get(0).node
        val data = ListData()
        data.product = products?.get(position)
        data.textdata = products?.get(position)?.title.toString().trim()
        Constant.translateField(data.textdata!!, item.binding.name)
        data.regularprice = CurrencyFormatter.setsymbol(
            variant!!.price.amount,
            variant.price.currencyCode.toString()
        )
        if (variant.compareAtPrice != null) {
            if (jsonObject!!.getString("item_compare_at_price").equals("1")) {
                val special = java.lang.Double.valueOf(variant.compareAtPrice.amount)
                val regular = java.lang.Double.valueOf(variant.price.amount)
                if (BigDecimal.valueOf(special).compareTo(BigDecimal.valueOf(regular)) == 1) {
                    data.regularprice = CurrencyFormatter.setsymbol(
                        variant.compareAtPrice.amount,
                        variant.compareAtPrice.currencyCode.toString()
                    )
                    data.specialprice = CurrencyFormatter.setsymbol(
                        variant.price.amount,
                        variant.price.currencyCode.toString()
                    )
                    item.binding.specialprice.visibility = View.VISIBLE
                    item.binding.regularprice.paintFlags = item.binding.regularprice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    item.binding.specialprice.visibility = View.GONE
                    item.binding.regularprice.paintFlags = item.binding.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            } else {
                item.binding.specialprice.visibility = View.GONE
            }
        } else {
            item.binding.specialprice.visibility = View.GONE
        }
        val model = CommanModel()
        if (products?.get(position)?.images?.edges?.size!! > 0) {
            var url=products?.get(position)?.images?.edges?.get(0)?.node?.url
            if(url!=null){
                model.imageurl = products?.get(position)?.images?.edges?.get(0)?.node?.url
                Log.i("SaifDev_ImageQuery","URL"+products?.get(position)?.images?.edges?.get(0)?.node?.url)
                val height = products?.get(position)?.images?.edges?.get(0)?.node?.height
                val width = products?.get(position)?.images?.edges?.get(0)?.node?.width
                (item.binding.imagesection.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,1:1.25"
            }
        }
        item.binding.listdata = data
        item.binding.commondata = model
        item.binding.clickproduct = ProductSliderListAdapter().Product(repository, activity!!)
        if (SplashViewModel.featuresModel.outOfStock!!) {
            if (!products?.get(position)!!.availableForSale) {
                item.binding.outOfStock.visibility = View.VISIBLE
            } else {
                item.binding.outOfStock.visibility = View.GONE
            }
        }
        Constant.translateField(
            activity!!.resources.getString(R.string.out_of_stock),
            item.binding.outOfStock
        )
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return products!!.size
    }

    inner class Product(var repository: Repository, var activity: Activity) {
        fun productClick(view: View, data: ListData) {
            Log.i("PRODUCTID",""+data.product!!.id.toString())
            val productintent = Intent(view.context, ProductView::class.java)
            productintent.putExtra("ID", data.product!!.id.toString())
            productintent.putExtra("tittle", data.textdata)
            view.context.startActivity(productintent)
            Constant.activityTransition(view.context)
        }

        fun addCart(view: View, data: ListData) {
            var customQuickAddActivity = QuickAddActivity(
                context = activity,
                theme = R.style.WideDialogFull,
                product_id = data.product!!.id.toString(),
                repository = repository,
                product = data.product!!
            )
            if (data.product!!.variants.edges.size > 1) {
                customQuickAddActivity.show()
            } else {
                customQuickAddActivity.addToCart(
                    data.product!!.variants.edges.get(0).node.id.toString(),
                    1
                )
            }
        }
    }
}
