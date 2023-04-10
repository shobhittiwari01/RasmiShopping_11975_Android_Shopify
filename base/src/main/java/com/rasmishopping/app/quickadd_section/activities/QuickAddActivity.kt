package com.rasmishopping.app.quickadd_section.activities
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.models.ListData
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.databinding.ActivityQuickAddBinding
import com.rasmishopping.app.databinding.SwatchesListQuickcartBinding
import com.rasmishopping.app.dbconnection.entities.CartItemData
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.productsection.viewmodels.ProductViewModel
import com.rasmishopping.app.quickadd_section.adapter.QuickVariantAdapter
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.CurrencyFormatter
import com.rasmishopping.app.utils.Urls
import com.rasmishopping.app.wishlistsection.activities.WishList
import com.rasmishopping.app.wishlistsection.viewmodels.WishListViewModel
import kotlinx.android.synthetic.main.m_productmain.view.*
import kotlinx.android.synthetic.main.m_productview.*
import kotlinx.android.synthetic.main.swatches_list.view.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*

class QuickAddActivity(
    context: Context,
    var activity: Context? = null,
    theme: Int,
    var product_id: String,
    var repository: Repository,
    var wishListViewModel: WishListViewModel? = null,
    var position: Int? = null,
    var wishlistData: MutableList<Storefront.Product>? = null,
    ) : BottomSheetDialog(context, theme) {
    var product:Storefront.Product ?=null
    var callback:ProductVariantCallback ?= null
    var wish:Boolean=false
    var productViewModel:ProductViewModel?=null
    constructor(context: Context, activity: Context? = null, theme: Int, product_id: String, repository: Repository, wishListViewModel: WishListViewModel? = null, position: Int? = null, wishlistData: MutableList<Storefront.Product>? = null, product:Storefront.Product) :this(context,activity, theme = theme, product_id =product_id,repository,wishListViewModel,position,wishlistData) {
        this.product=product
        this.activity=context
    }
    constructor(context: Context, activity: Context? = null, theme: Int, product_id: String, repository: Repository, wishListViewModel: WishListViewModel? = null, position: Int? = null, wishlistData: MutableList<Storefront.Product>? = null, product:Storefront.Product,callback:ProductVariantCallback,wish:Boolean,productViewModel:ProductViewModel) :this(context,activity, theme = theme, product_id =product_id,repository,wishListViewModel,position,wishlistData ) {
        this.product=product
        this.activity=context
        this.callback=callback
        this.wish=wish
        this.productViewModel=productViewModel
    }
    var variantId_titleCombo= HashMap<String,ID>()
    var variantId_priceCombo= HashMap<String,String>()
    var variantId_specialCombo= HashMap<String,String>()
    var variantId_imageCombo= HashMap<String,String>()
    var variantId_outofstockCombo= HashMap<String,Boolean>()
    var binding: ActivityQuickAddBinding? = null
    lateinit var app: MyApplication
    var variant_id: String? = null
    var carttArray = JSONArray()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var product_price: Double = 0.0
    var bottomSheetDialog: BottomSheetDialog? = null
    var currency: String? = null
    var quantity: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_quick_add, null, false)
        setContentView(binding?.root!!)
        this.window?.setBackgroundDrawableResource(android.R.color.transparent)
        bottomSheetDialog = this
        firebaseAnalytics = Firebase.analytics
        if(wish){
            binding!!.bottomsection.visibility=View.VISIBLE
            binding!!.cartcontainer.visibility=View.GONE
            binding!!.wishlistsection.isVisible = SplashViewModel.featuresModel.in_app_wishlist
            binding!!.wishlistsection.background=ContextCompat.getDrawable(HomePageViewModel.themedContext,R.drawable.wishlist_round)
            binding?.cartsection?.background=ContextCompat.getDrawable(HomePageViewModel.themedContext,R.drawable.newcartround)
            binding?.cartsection?.getBackground()?.setColorFilter(Color.parseColor(NewBaseActivity.themeColor), PorterDuff.Mode.SRC_OVER)
//            var buynowback=binding!!.buynowsection.background as GradientDrawable
//            buynowback.setStroke(2,Color.parseColor(NewBaseActivity.themeColor))
//            buynowback.setColor(Color.parseColor(NewBaseActivity.themeColor))
//            buynowsection.background=buynowback
//            binding?.buynow?.setTextColor(Color.parseColor(NewBaseActivity.textColor))
        }
        initView()
    }

    fun initView() {
        val model = CommanModel()
        if (product!!.images.edges.size > 0) {
            model.imageurl = product!!.images.edges[0].node.url
        }

        binding!!.commondata = model
        val variant = product!!.variants.edges[0].node
        val data = ListData()
        data.textdata = product!!.title
        data.regularprice = CurrencyFormatter.setsymbol(variant.price.amount, variant.price.currencyCode.toString())
        if (variant.compareAtPrice != null) {
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
                binding!!.regularprice.background= ContextCompat.getDrawable(activity!!,R.drawable.cross_line)
                binding!!.specialprice.visibility = View.VISIBLE
            }

        } else {
            binding!!.specialprice.visibility = View.GONE
        }
        binding!!.listdata = data
        var drawable=binding!!.cartcontainer.background as GradientDrawable
        drawable.setColor(Color.parseColor(NewBaseActivity.themeColor))
        drawable.setStroke(2, Color.parseColor(NewBaseActivity.themeColor))
        binding!!.cartcontainer.background=drawable
        binding!!.cartcontainer.isClickable=true
        binding?.handler = VariantClickHandler()
        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0..product!!.variants.edges.size-1){
                variantId_titleCombo.put(product!!.variants.edges.get(i).node.title.trim().replace(" ",""),product!!.variants.edges.get(i).node.id)
                variantId_priceCombo.put(product!!.variants.edges.get(i).node.title.trim().replace(" ",""),CurrencyFormatter.setsymbol(product!!.variants.edges.get(i).node.price.amount, product!!.variants.edges.get(i).node.price.currencyCode.toString()))
                if (product!!.variants.edges.get(i).node.compareAtPrice != null) {
                    val special = java.lang.Double.valueOf(product!!.variants.edges.get(i).node.compareAtPrice.amount)
                    val regular = java.lang.Double.valueOf(product!!.variants.edges.get(i).node.price.amount)
                    if (BigDecimal.valueOf(special).compareTo(BigDecimal.valueOf(regular)) == 1) {
                        variantId_priceCombo.put(product!!.variants.edges.get(i).node.title.trim().replace(" ",""),CurrencyFormatter.setsymbol(product!!.variants.edges.get(i).node.compareAtPrice.amount, product!!.variants.edges.get(i).node.compareAtPrice.currencyCode.toString()))
                        variantId_specialCombo.put(product!!.variants.edges.get(i).node.title.trim().replace(" ",""),CurrencyFormatter.setsymbol(product!!.variants.edges.get(i).node.price.amount, product!!.variants.edges.get(i).node.price.currencyCode.toString()))
                    }
                }
                variantId_imageCombo.put(product!!.variants.edges.get(i).node.title.trim().replace(" ",""),product!!.variants.edges.get(i).node.image.url)
                if (product!!.variants.edges.get(i).node.currentlyNotInStock == false) {
                    if (product!!.variants.edges.get(i).node.quantityAvailable <= 0 && !product!!.variants.edges.get(i).node.availableForSale) {
                        variantId_outofstockCombo.put(product!!.variants.edges.get(i).node.title.trim().replace(" ",""),false)
                    } else {
                        variantId_outofstockCombo.put(product!!.variants.edges.get(i).node.title.trim().replace(" ",""),true)
                    }
                } else {
                    variantId_outofstockCombo.put(product!!.variants.edges.get(i).node.title.trim().replace(" ",""),true)
                }
                Log.i("vaoutofstockCombo",""+variantId_outofstockCombo)
            }
        }
        createOptionList(product!!.variants,product!!.options)
    }
    private fun createOptionList(variants: Storefront.ProductVariantConnection?, options: List<Storefront.ProductOption>) {
        try {
            var array= arrayOfNulls<String>(options.size)
            for (j in 0 until options.size) {
                var swatechView: SwatchesListQuickcartBinding = DataBindingUtil.inflate(layoutInflater, R.layout.swatches_list_quickcart, null, false)
                swatechView.variantTitle.text = options.get(j).name
                swatechView.variantListRecyclerView.visibility = View.VISIBLE
                var adapter = QuickVariantAdapter()
                adapter.setData(
                    j,
                    options.get(j).name,
                    options.get(j).values,
                    activity!!,
                    object : QuickVariantAdapter.VariantCallback {
                        override fun clickVariant(variantName: String, optionName: String,optionposition:Int,vposition: Int) {
                            if(callback!=null){
                                callback!!.clickVariant(variantName,optionName,optionposition,vposition)
                            }
                            array[optionposition]=variantName.replace(" ","")
                            proceedToCart(array)
                        }

                    })
                swatechView.root.variant_list_recyclerView.adapter = adapter
                binding?.optionlist?.addView(swatechView.root)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    interface ProductVariantCallback {
        fun clickVariant(variantName: String, optionName: String,optionposition:Int,variantposition:Int)
        fun buynow()
        fun wishClick()
    }
    private fun proceedToCart(array: Array<String?>) {
        var finalkey=array.joinToString(separator = "/")
        Log.i("SaifQuickCart_String",""+finalkey)
        if(variantId_titleCombo.containsKey(finalkey)){
            variant_id=variantId_titleCombo.get(finalkey).toString()
            if(productViewModel!=null){
                if (productViewModel?.isInwishList(variant_id!!)!!) {
                    Constant.WishlistAnimation(activity!!, binding?.wishenable!!)
                    Wish(true)
                } else {
                    Wish(false)
                }
            }
        }
        if(variantId_priceCombo.containsKey(finalkey)){
            binding!!.regularprice.text=variantId_priceCombo.get(finalkey)
            binding!!.specialprice.visibility = View.GONE
            binding!!.regularprice.background= ContextCompat.getDrawable(activity!!,R.drawable.no_cross_line)
        }
        if (variantId_specialCombo.containsKey(finalkey)){
            binding!!.specialprice.text=variantId_specialCombo.get(finalkey)
            binding!!.regularprice.background= ContextCompat.getDrawable(activity!!,R.drawable.cross_line)
            binding!!.specialprice.visibility = View.VISIBLE
        }
        if(variantId_imageCombo.containsKey(finalkey)){
            val model = CommanModel()
            model.imageurl = variantId_imageCombo.get(finalkey)
            binding!!.commondata = model
        }
        if(variantId_outofstockCombo.containsKey(finalkey)){
            when(variantId_outofstockCombo.get(finalkey)){
                true->{
                    var drawable=binding!!.cartcontainer.background as GradientDrawable
                    drawable.setColor(Color.parseColor(NewBaseActivity.themeColor))
                    drawable.setStroke(2, Color.parseColor(NewBaseActivity.themeColor))
                    binding!!.cartcontainer.background=drawable
//                    binding!!.cartsection.background=ContextCompat.getDrawable(HomePageViewModel.themedContext,R.drawable.round)
                    binding!!.cartcontainer.isClickable=true
                    binding!!.cartsection.isClickable=true
                    binding!!.addCart.text=activity!!.resources.getString(R.string.addtocart)
                    binding!!.addtocart.text=activity!!.resources.getString(R.string.addtocart)
                    binding!!.addCart.setTextColor(Color.parseColor(NewBaseActivity.textColor))
                    binding?.addtocart?.setTextColor(Color.parseColor(NewBaseActivity.textColor))
                    binding!!.bagicon.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
                    binding!!.buynowsection.visibility=View.VISIBLE
                }
                false->{
                    var drawable=binding!!.cartcontainer.background as GradientDrawable
                    drawable.setColor(Color.parseColor("#F0F0F0"))
                    drawable.setStroke(2,Color.parseColor("#F0F0F0"))
                    binding!!.cartcontainer.background=drawable
                    binding!!.cartsection.background=drawable
                    binding!!.cartcontainer.isClickable=false
                    binding!!.cartsection.isClickable=false
                    binding!!.addCart.text=activity!!.resources.getString(R.string.out_of_stock)
                    binding!!.addtocart.text=activity!!.resources.getString(R.string.out_of_stock)
                    binding!!.addCart.setTextColor(Color.parseColor("#F43939"))
                    binding!!.addtocart.setTextColor(Color.parseColor("#F43939"))
                    binding!!.bagicon.setColorFilter(Color.parseColor("#F43939"), android.graphics.PorterDuff.Mode.SRC_IN);
                    binding!!.buynowsection.visibility=View.GONE
                }
            }
        }

    }

    fun addToCart(variantId: String, quantity: Int) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val data: CartItemData
                if (repository.getSingLeItem(variantId) == null) {
                    data = CartItemData()
                    data.variant_id = variantId
                    data.qty = quantity
                    data.selling_plan_id=""
                    data.offerName=""
                    repository.addSingLeItem(data)
                } else {
                    data = repository.getSingLeItem(variantId)
                    val qt = data.qty
                    data.qty = qt + quantity
                    repository.updateSingLeItem(data)
                }
                Log.i("MageNative", "CartCount : " + repository.allCartItems.size)
            }
            CoroutineScope(Dispatchers.Main).launch {
                if (activity is NewBaseActivity) {
                    (activity as NewBaseActivity).invalidateOptionsMenu()
                    Toast.makeText(context,context.getString(R.string.successcart),Toast.LENGTH_SHORT).show()
                }
            }
            Constant.logAddToCartEvent(
                carttArray.toString(), product_id, "product", currency, product_price, activity
                    ?: Activity()
            )
            if (SplashViewModel.featuresModel.firebaseEvents) {
                Constant.FirebaseEvent_AddtoCart(product_id,quantity.toString())

            }
            if (wishListViewModel != null) {
                if (activity is WishList) {
                    wishListViewModel!!.deleteData(product_id)
                    wishlistData!!.removeAt(position!!)
                    (activity as WishList).adapter.notifyItemRemoved(position!!)
                    (activity as WishList).adapter.notifyItemRangeChanged(
                        position!!,
                        wishlistData!!.size
                    )
                    wishListViewModel!!.update(true)
                    (activity as WishList).invalidateOptionsMenu()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun wishListAdd(variantId: String,view: View) {
        if (!productViewModel!!.isInwishList(variantId)) {
            Constant.WishlistAnimation(activity!!, binding?.wishenable!!)
            Wish(true)
        } else {
            Wish(false)
        }
    }
    inner class VariantClickHandler {
        fun addcart(view: View) {
            if(variant_id==null){
                Toast.makeText(context, context.getString(R.string.selectvariant), Toast.LENGTH_LONG).show()
            }else{
                if(binding?.addCart!!.text==context.getString(R.string.go_to_bag)) {
                    var intent=Intent(context,CartList::class.java)
                    context.startActivity(intent)
                    bottomSheetDialog?.dismiss()
                }
                else {
                    addToCart(variant_id!!, quantity)
                    Constant.SlideAnimation(activity!!, binding?.addCart!!)
                    binding?.addCart!!.text = activity!!.resources.getString(R.string.go_to_bag)
                    binding?.addtocart!!.text = activity!!.resources.getString(R.string.go_to_bag)
                }
            }
        }
        fun addtowish(view: View) {
            if(variant_id==null){
                Toast.makeText(context, context.getString(R.string.selectvariant), Toast.LENGTH_LONG).show()
            }else{
                wishListAdd(variant_id!!,view)
                if(callback!=null){
                    callback!!.wishClick()
                }
            }
        }
        fun buynow(view: View) {
            if(variant_id==null){
                Toast.makeText(context, context.getString(R.string.selectvariant), Toast.LENGTH_LONG).show()
            }else{
                if(callback!=null){
                    callback!!.buynow()
                }
                bottomSheetDialog?.dismiss()
            }
        }
        fun closeDialog(view: View) {
            bottomSheetDialog?.dismiss()
        }



    }
    private fun Wish(flag:Boolean){
        if(flag){
            binding?.wishenable!!.isVisible=true
            Constant.WishlistAnimation(activity!!, binding?.wishenable!!)
            binding?.wishdisable!!.isVisible=false
        }else{
            binding?.wishenable?.clearAnimation()
            binding?.wishenable!!.isVisible=false
            binding?.wishdisable!!.isVisible=true
        }
    }
}