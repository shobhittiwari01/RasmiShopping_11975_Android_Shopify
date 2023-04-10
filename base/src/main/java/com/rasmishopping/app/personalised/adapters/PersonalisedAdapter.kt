package com.rasmishopping.app.personalised.adapters

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
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.shopify.buy3.Storefront
import com.rasmishopping.app.FlitsDashboard.WishlistSection.FlitsWishlistViewModel
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.models.ListData
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.databinding.MPersonalisedBinding
import com.rasmishopping.app.databinding.MSimilarBinding
import com.rasmishopping.app.databinding.MSlideritemoneBinding
import com.rasmishopping.app.homesection.adapters.ProductSliderListAdapter
import com.rasmishopping.app.homesection.viewholders.SliderItemTypeOne
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.productsection.viewholders.ProductItem
import com.rasmishopping.app.productsection.viewmodels.ProductViewModel
import com.rasmishopping.app.quickadd_section.activities.QuickAddActivity
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.CurrencyFormatter
import com.rasmishopping.app.utils.Urls
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import javax.inject.Inject

class PersonalisedAdapter @Inject
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderItemTypeOne {
        var binding = DataBindingUtil.inflate<MSimilarBinding>(LayoutInflater.from(parent.context), R.layout.m_similar, parent, false)
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
            "right" -> { binding.itemDataSection.gravity=Gravity.END }
            "center" -> { binding.itemDataSection.gravity=Gravity.CENTER }
        }
        if (jsonObject!!.getString("item_border").equals("1")) {
            val item_border_color = JSONObject(jsonObject!!.getString("item_border_color"))
            binding.productsection.setCardBackgroundColor(Color.parseColor( item_border_color.getString("color")))
            val newLayoutParams = binding.innerproductsection.getLayoutParams() as FrameLayout.LayoutParams
            newLayoutParams.setMargins(4,4,4,4)
            binding.innerproductsection.setLayoutParams(newLayoutParams)
        }else{
            binding.productsection.radius = 0f
            binding.productsection.cardElevation = 0f
        }
        if (jsonObject!!.getString("item_title").equals("0")&&jsonObject!!.getString("item_price").equals("0")) {
            binding.itemDataSection.visibility=View.GONE
        }
        if (jsonObject!!.getString("item_title").equals("1")){
            binding.name.visibility=View.VISIBLE
        }
        if (jsonObject!!.getString("item_price").equals("1")){
            binding.pricesection.visibility=View.VISIBLE
            if (jsonObject!!.getString("item_compare_at_price").equals("1")) {
                binding.specialprice.visibility=View.VISIBLE
            }
        }
        binding.itemDataSection.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
        binding.name.setTextColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.black))
        binding.regularprice.setTextColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.normalgrey3text))
        binding.specialprice.setTextColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.black))
        return SliderItemTypeOne(binding)
    }

    override fun onBindViewHolder(item: SliderItemTypeOne, position: Int) {
        val variant = products?.get(position)!!.variants.edges.get(0).node
        val data = ListData()
        data.product = products?.get(position)
        data.textdata = products?.get(position)?.title.toString().trim()
        Constant.translateField(data.textdata!!,item.similarbinding.name)
        data.regularprice = CurrencyFormatter.setsymbol(variant!!.price.amount, variant.price.currencyCode.toString())
        if (variant.compareAtPrice != null) {
            if (jsonObject!!.getString("item_compare_at_price").equals("1")) {
                val special = java.lang.Double.valueOf(variant.compareAtPrice.amount)
                val regular = java.lang.Double.valueOf(variant.price.amount)
                if (BigDecimal.valueOf(special).compareTo(BigDecimal.valueOf(regular)) == 1) {
                    data.regularprice = CurrencyFormatter.setsymbol(variant.compareAtPrice.amount, variant.compareAtPrice.currencyCode.toString())
                    data.specialprice = CurrencyFormatter.setsymbol(variant.price.amount, variant.price.currencyCode.toString())
                    item.similarbinding.specialprice.visibility = View.VISIBLE
                    item.similarbinding.regularprice.setPaintFlags(item.similarbinding.regularprice.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
                }else{
                    item.similarbinding.specialprice.visibility = View.GONE
                    item.similarbinding.regularprice.setPaintFlags(item.similarbinding.regularprice.getPaintFlags() and Paint.STRIKE_THRU_TEXT_FLAG.inv())
                }
            }
            else {
                item.similarbinding.specialprice.visibility = View.GONE
            }
        } else {
            item.similarbinding.specialprice.visibility = View.GONE
        }
        val model = CommanModel()
        if (products?.get(position)?.images?.edges?.size!! > 0) {
            model.imageurl = products?.get(position)?.images?.edges?.get(0)?.node?.url
            val height=products?.get(0)?.images?.edges?.get(0)?.node?.height
            val width=products?.get(0)?.images?.edges?.get(0)?.node?.width
            //Log.i("SaifDev","ImageWidth"+width +" : "+"ImageHeight"+height)
           /* if (height==width){
                (item.similarbinding.imagesection.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,130:130"
            }*/
        }
        item.similarbinding.listdata = data
        item.similarbinding.commondata = model
        item.similarbinding.clickproduct = ProductSliderListAdapter().Product(repository, activity!!)
        if (SplashViewModel.featuresModel.outOfStock!!) {
            if (!products?.get(position)!!.availableForSale) {
                item.similarbinding.outOfStock.visibility = View.VISIBLE
            } else {
                item.similarbinding.outOfStock.visibility = View.GONE
            }
        }
        Constant.translateField(activity!!.resources.getString(R.string.out_of_stock)!!,item.similarbinding.outOfStock)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return products!!.size
    }
    inner class Product(var repository: Repository, var activity: Activity) {
        fun productClick(view: View, data: ListData) {
            val productintent = Intent(view.context, ProductView::class.java)
            productintent.putExtra("ID", data.product!!.id.toString())
            productintent.putExtra("tittle", data.textdata)
            productintent.putExtra("product", data.product)
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
            if(data.product!!.variants.edges.size>1){
                customQuickAddActivity.show()
            }else{
                customQuickAddActivity.addToCart(data.product!!.variants.edges.get(0).node.id.toString(),1)
            }
        }
    }
}
