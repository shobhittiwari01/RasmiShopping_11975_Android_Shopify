package com.rasmishopping.app.productsection.adapters
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.shopify.buy3.Storefront
import com.rasmishopping.app.FlitsDashboard.WishlistSection.FlitsWishlistViewModel
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.models.ListData
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.databinding.MProductitemBinding
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.productsection.viewholders.ProductItem
import com.rasmishopping.app.productsection.viewmodels.ProductListModel
import com.rasmishopping.app.quickadd_section.activities.QuickAddActivity
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.CurrencyFormatter
import com.rasmishopping.app.utils.Urls
import kotlinx.android.synthetic.main.wishlist_animation.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import javax.inject.Inject


class ProductRecylerGridAdapter @Inject
constructor() : RecyclerView.Adapter<ProductItem>() {
    lateinit var products: MutableList<Storefront.ProductEdge>
    private var activity: Activity? = null
    private var repository: Repository? = null
    private var model: ProductListModel? = null
    private var flitswishlistmodel: FlitsWishlistViewModel? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var whilistArray = JSONArray()
    fun setData(
        flitswishlistmodel:FlitsWishlistViewModel,
        products: List<Storefront.ProductEdge>?,
        model: ProductListModel?,
        activity: Activity,
        repository: Repository
    ) {
        this.products = products as MutableList<Storefront.ProductEdge>
        this.activity = activity
        this.flitswishlistmodel=flitswishlistmodel
        this.model = model
        this.repository = repository
        firebaseAnalytics = Firebase.analytics
    }
    init {
        setHasStableIds(true)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductItem {
        val binding = DataBindingUtil.inflate<MProductitemBinding>(LayoutInflater.from(parent.context), R.layout.m_productitem, parent, false)
        return ProductItem(binding)
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun onBindViewHolder(holder: ProductItem, position: Int) {
        val variant = this.products[position].node.variants.edges[0].node
        val data = ListData()
        Log.i("MageNative", "SaifTest" + variant.availableForSale)
        Log.i("MageNative", "SaifTest" + variant.quantityAvailable)
        Log.i("MageNative", "Product ID" + this.products[position].node.id)
        Log.i("SaifDevTesting", "Product Tags" + this.products[position].node.tags)
        data.product = this.products[position].node
        data.textdata = this.products[position].node.title
        if(!this.products[position].node.description.isNullOrEmpty()&&this.products[position].node.description.length>1){
            data.description = this.products[position].node.description
            holder.binding!!.shortdescription.visibility=View.VISIBLE
        }else{
            holder.binding!!.shortdescription.visibility=View.GONE
        }
        data.regularprice = CurrencyFormatter.setsymbol(variant.price.amount, variant.price.currencyCode.toString())
        if (variant.compareAtPrice != null) {
            val special = java.lang.Double.valueOf(variant.compareAtPrice.amount)
            val regular = java.lang.Double.valueOf(variant.price.amount)
            if (BigDecimal.valueOf(special).compareTo(BigDecimal.valueOf(regular)) == 1) {
                data.regularprice = CurrencyFormatter.setsymbol(variant.compareAtPrice.amount, variant.compareAtPrice.currencyCode.toString())
                data.specialprice = CurrencyFormatter.setsymbol(variant.price.amount, variant.price.currencyCode.toString())
                data.offertext = "("+getDiscount(special, regular).toString() + "%${activity!!.resources.getString(
                        R.string.off
                    )})"
                holder.binding!!.regularprice.background=ContextCompat.getDrawable(HomePageViewModel.themedContext!!,R.drawable.cross_line)
                holder.binding!!.specialprice.setTextColor(Color.parseColor(NewBaseActivity.themeColor))
                holder.binding!!.specialprice.visibility = View.VISIBLE
                holder.binding!!.offertext.visibility = View.VISIBLE
            } else {
                holder.binding!!.specialprice.visibility = View.GONE
                holder.binding!!.offertext.visibility = View.GONE
                holder.binding!!.regularprice.background=ContextCompat.getDrawable(HomePageViewModel.themedContext!!,R.drawable.no_cross_line)
            }
        } else {
            holder.binding!!.specialprice.visibility = View.GONE
            holder.binding!!.offertext.visibility = View.GONE
            holder.binding!!.regularprice.background=ContextCompat.getDrawable(HomePageViewModel.themedContext!!,R.drawable.no_cross_line)
        }
        Constant.applyColor(
            holder.binding!!.cartIcon,
            activity!!.resources.getString(R.string.addtocart)
        )
        if (SplashViewModel.featuresModel.in_app_wishlist) {
            if (model?.isInwishList(data.product?.variants!!.edges[0].node.id.toString())!!) {
                data.addtowish = activity?.resources?.getString(R.string.alreadyinwish)
                holder?.binding?.wishEnable?.isVisible=false
                holder?.binding?.wishenable?.isVisible=true
                holder?.binding?.wishEnable?.clearAnimation()

            } else {
                data.addtowish = activity?.resources?.getString(R.string.addtowish)
                holder?.binding?.wishenable?.isVisible=false
                holder?.binding?.wishEnable?.isVisible=false
                holder?.binding?.wishEnable?.clearAnimation()
                Glide.with(activity!!)
                    .load(R.drawable.wishicon)
                    .into(holder.binding?.wishlistBut!!)
            }
        }
        holder.binding!!.listdata = data
        val model = CommanModel()
        model.imageurl=""
        if (this.products[position].node.images.edges.size > 0) {
            var url=this.products[position].node.images.edges[0].node.url
            if(url!=null){
                model.imageurl = this.products[position].node.images.edges[0].node.url
                Log.i("SaifDev_ProductList","URL"+this.products[position].node.images.edges[0].node.url)
                var width=this.products[position].node.images.edges[0].node.width
                var height=this.products[position].node.images.edges[0].node.height
                Log.i("SaifDev_ProductList","widhth:${width}")
                Log.i("SaifDev_ProductList","height:${height}")
                (holder.binding!!.imagesection.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,${width}:${height}"
            }
        }
        holder.binding?.features = SplashViewModel.featuresModel
        holder.binding!!.commondata = model
        holder.binding!!.clickproduct = Product(holder, position)
        if (!this.products[position].node.availableForSale) {
            var drawable=holder.binding!!.cartIcon.background as GradientDrawable
            drawable.setColor(Color.parseColor("#F0F0F0"))
            drawable.setStroke(2,Color.parseColor("#F0F0F0"))
            holder.binding!!.cartIcon.background=drawable
            holder.binding!!.cartIcon.setTextColor(Color.parseColor("#F43939"))
            holder.binding!!.cartIcon.text=activity!!.resources.getString(R.string.out_of_stock)
            holder.binding?.cartIcon?.visibility = View.VISIBLE
            holder.binding?.image?.alpha = 0.7f
        } else {
            if(SplashViewModel.featuresModel.addCartEnabled){
                if(this.products[position].node.requiresSellingPlan){
                    holder.binding?.cartIcon?.text = activity!!.resources.getString(R.string.subscribe)
                    holder.binding?.cartIcon?.tag = "subscribe"
                }else{
                    holder.binding?.cartIcon?.text = activity!!.resources.getString(R.string.addtocart)
                    holder.binding?.cartIcon?.tag = "addtocart"
                }
                holder.binding?.cartIcon?.visibility = View.VISIBLE
            }else{
                holder.binding?.cartIcon?.visibility = View.GONE
            }
            holder.binding?.image?.alpha = 1f
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    fun getDiscount(regular: Double, special: Double): Int {
        return ((regular - special) / regular * 100).toInt()
    }

    inner class Product(var holder: ProductItem, var position: Int) {

        fun productClick(view: View, data: ListData) {
            val productintent = Intent(view.context, ProductView::class.java)
            productintent.putExtra("ID", data.product!!.id.toString())
            productintent.putExtra("tittle", data.textdata)
            productintent.putExtra("product", data.product)
            productintent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            productintent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            productintent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            val bundle= ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
            view.context.startActivity(productintent)
            Constant.activityTransition(view.context)
        }

        fun wishListAdd(view: View, data: ListData) {
            if (!model?.isInwishList(data.product?.variants!!.edges[0].node.id.toString())!!) {
                model?.AddtoWishVariant(data.product?.variants!!.edges[0].node.id.toString())
//                Toast.makeText(
//                    view.context,
//                    view.context.resources.getString(R.string.successwish),
//                    Toast.LENGTH_LONG
//                ).show()
                if(SplashViewModel.featuresModel.Enable_flits_App) {
                    flitswishlistmodel?.SendWishlistData(Urls.X_Integration_App_Name!!,
                        data.product?.id.toString().replace("gid://shopify/Product/", "")!!
                            .split("?")[0],
                        data.product?.handle.toString(), MagePrefs.getCustomerID().toString(),MagePrefs.getCustomerEmail().toString()
                        , Urls.user_id!!,
                        Urls.token!! )
                }
                data.addtowish = view.context.resources.getString(R.string.alreadyinwish)
//                Glide.with(view.context).load(R.drawable.wishiconselected)
//                    .into(holder.binding?.wishlistBut!!)



                holder?.binding?.wishEnable?.isVisible=true
               Constant.WishlistAnimation(activity!!,holder?.binding?.wishEnable!!)

                var wishlistData = JSONObject()
                wishlistData.put("id", data.product?.variants!!.edges[0].node.id.toString())
                wishlistData.put("quantity", 1)
                whilistArray.put(wishlistData.toString())
                data.addtowish = activity?.resources?.getString(R.string.alreadyinwish)

                Constant.logAddToWishlistEvent(
                    whilistArray.toString(),
                    data.product?.variants!!.edges[0].node.id.toString(),
                    "product",
                    data.product?.variants?.edges?.get(0)?.node?.price?.currencyCode?.toString(),
                    data.product?.variants?.edges?.get(0)?.node?.price?.amount?.toDouble()
                        ?: 0.0,
                    activity ?: Activity()
                )
                if (SplashViewModel.featuresModel.firebaseEvents) {
                    Constant.FirebaseEvent_AddtoWishlist(data.product?.variants!!.edges[0].node.id.toString(),1.toString())
                }
            } else {
                if(SplashViewModel.featuresModel.Enable_flits_App) {
                    flitswishlistmodel?.RemoveWishlistData(Urls.X_Integration_App_Name!!,
                        data.product?.id.toString().replace("gid://shopify/Product/", "")!!
                            .split("?")[0],
                        data.product?.handle.toString(),MagePrefs.getCustomerID().toString(),MagePrefs.getCustomerEmail().toString()

                        , Urls.user_id!!,Urls.token!! )
                }
                model!!.deleteData(data.product?.variants!!.edges[0].node.id.toString())

                Toast.makeText(
                    view.context,
                    view.context.resources.getString(R.string.removedwish),
                    Toast.LENGTH_SHORT
                ).show()
                data!!.addtowish =  view.context.resources.getString(R.string.addtowish)
                holder?.binding?.wishenable?.isVisible=false
                holder?.binding?.wishEnable?.isVisible=false
                holder?.binding?.wishEnable?.clearAnimation()

                Glide.with(view.context).load(R.drawable.wishicon)
                    .into(holder.binding?.wishlistBut!!)

            }
        }

        fun addCart(view: View, data: ListData) {
            if (data.product!!.requiresSellingPlan) {
                var intent = Intent(view.context, ProductView::class.java)
                intent.putExtra("ID", data.product!!.id.toString())
                intent.putExtra("tittle", data.textdata)
                view.context.startActivity(intent)
                Constant.activityTransition(view.context)
            } else {
                if (data.product!!.availableForSale) {
                    var customQuickAddActivity = QuickAddActivity(
                        context = activity!!,
                        theme = R.style.WideDialogFull,
                        product_id = data.product!!.id.toString(),
                        repository = repository!!,
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
    }
}