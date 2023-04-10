package com.rasmishopping.app.searchsection.adapters
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
import com.rasmishopping.app.databinding.MSearchitemBinding
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.productsection.viewholders.ProductItem
import com.rasmishopping.app.productsection.viewmodels.ProductListModel
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
class SearchGridAdapter @Inject
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
    override fun getItemViewType(position: Int): Int {
        var viewtype = 0
        if (!products[position].node.availableForSale) {
            viewtype = -1
        }
        return viewtype
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductItem {
        val binding = DataBindingUtil.inflate<MSearchitemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_searchitem,
            parent,
            false
        )
        return ProductItem(binding)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: ProductItem, position: Int) {
        val variant = this.products[position].node.variants.edges[0].node
        val data = ListData()
        Log.i("MageNative", "Product ID" + this.products[position].node.id)
        data.product = this.products[position].node
        data.textdata = variant.product.title
        if(!this.products[position].node.description.isNullOrEmpty()&&this.products[position].node.description.length>1){
            data.description = this.products[position].node.description
            holder.searchbinding!!.shortdescription.visibility=View.VISIBLE
        }else{
            holder.searchbinding!!.shortdescription.visibility=View.GONE
        }
        data.regularprice = CurrencyFormatter.setsymbol(
            variant.price.amount,
            variant.price.currencyCode.toString()
        )
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
                data.offertext = "("+getDiscount(special, regular).toString() +  "%${activity!!.resources.getString(
                    R.string.off
                )})"
                holder.searchbinding!!.regularprice.background=ContextCompat.getDrawable(activity!!,R.drawable.cross_line)
                // holder.searchbinding!!.regularprice.paintFlags = holder.searchbinding!!.regularprice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.searchbinding!!.specialprice.visibility = View.VISIBLE
                holder.searchbinding!!.offertext.visibility = View.VISIBLE
            }else{
                holder.searchbinding!!.specialprice.visibility = View.GONE
                holder.searchbinding!!.offertext.visibility = View.GONE
                holder.searchbinding!!.regularprice.background=ContextCompat.getDrawable(activity!!,R.drawable.no_cross_line)
            }

        } else {
            holder.searchbinding!!.specialprice.visibility = View.GONE
            holder.searchbinding!!.offertext.visibility = View.GONE
            holder.searchbinding!!.regularprice.background=ContextCompat.getDrawable(activity!!,R.drawable.no_cross_line)
        }
        Constant.applyColor(
            holder.searchbinding!!.cartIcon,
            activity!!.resources.getString(R.string.addtocart)
        )
        if (SplashViewModel.featuresModel.in_app_wishlist) {
            if (model?.isInwishList(data.product?.variants!!.edges[0].node.id.toString())!!) {
                data.addtowish = activity?.resources?.getString(R.string.alreadyinwish)
                Glide.with(activity!!)
                    .load(R.drawable.wishiconselected)
                    .into(holder.searchbinding?.wishlistBut!!)
            } else {
                data.addtowish = activity?.resources?.getString(R.string.addtowish)
                Glide.with(activity!!)
                    .load(R.drawable.wishicon)
                    .into(holder.searchbinding?.wishlistBut!!)
            }
        }else{
            holder.searchbinding?.wishlistBut?.visibility = View.GONE
        }
        holder.searchbinding!!.listdata = data
        val model = CommanModel()
        /*if (this.products[position].node.images.edges.size > 0) {
            model.imageurl = this.products[position].node.images.edges[0].node.url
        }*/
        if (this.products[position].node.images.edges.size > 0) {
            var url=this.products[position].node.images.edges[0].node.url
            if(url!=null){
                model.imageurl = this.products[position].node.images.edges[0].node.url
                Log.i("SaifDev_ProductList","URL"+this.products[position].node.images.edges[0].node.url)
                var width=this.products[position].node.images.edges[0].node.width
                var height=this.products[position].node.images.edges[0].node.height
                Log.i("SaifDev_ProductList","widhth:${width}")
                Log.i("SaifDev_ProductList","height:${height}")
                (holder.searchbinding!!.imagesection.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,${width}:${height}"
            }
        }
        holder.searchbinding?.features = SplashViewModel.featuresModel
        holder.searchbinding!!.commondata = model
        holder.searchbinding!!.clickproduct = Product(holder, position)
        if (!this.products[position].node.availableForSale) {
            var drawable=holder.searchbinding!!.cartIcon.background as GradientDrawable
            drawable.setColor(Color.parseColor("#F0F0F0"))
            drawable.setStroke(2,Color.parseColor("#F0F0F0"))
            holder.searchbinding!!.cartIcon.background=drawable
            holder.searchbinding!!.cartIcon.setTextColor(Color.parseColor("#F43939"))
            holder.searchbinding!!.cartIcon.text=activity!!.resources.getString(R.string.out_of_stock)
            holder.searchbinding?.cartIcon?.visibility = View.VISIBLE
            holder.searchbinding?.image?.alpha = 0.7f
        } else {
            if(SplashViewModel.featuresModel.addCartEnabled){
                if(this.products[position].node.requiresSellingPlan){
                    holder.searchbinding?.cartIcon?.text = activity!!.resources.getString(R.string.subscribe)
                    holder.searchbinding?.cartIcon?.tag = "subscribe"
                }else{
                    holder.searchbinding?.cartIcon?.text = activity!!.resources.getString(R.string.addtocart)
                    holder.searchbinding?.cartIcon?.tag = "addtocart"
                }
                holder.searchbinding?.cartIcon?.visibility = View.VISIBLE
            }else{
                holder.searchbinding?.cartIcon?.visibility = View.GONE
            }
            holder.searchbinding?.image?.alpha = 1f
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
            view.context.startActivity(productintent)
            Constant.activityTransition(view.context)
        }
        fun wishListAdd(view: View, data: ListData) {
            if (!model?.isInwishList(data.product?.variants!!.edges[0].node.id.toString())!!) {
                model?.AddtoWishVariant(data.product?.variants!!.edges[0].node.id.toString())
                Toast.makeText(
                    view.context,
                    view.context.resources.getString(R.string.successwish),
                    Toast.LENGTH_LONG
                ).show()
                if(SplashViewModel.featuresModel.Enable_flits_App) {
                    flitswishlistmodel?.SendWishlistData(Urls.X_Integration_App_Name!!,
                        data.product?.id.toString().replace("gid://shopify/Product/", "")!!
                            .split("?")[0],
                        data.product?.handle.toString(), MagePrefs.getCustomerID().toString(),MagePrefs.getCustomerEmail().toString()
                        , Urls.user_id!!,
                        Urls.token!! )
                }
                data.addtowish = view.context.resources.getString(R.string.alreadyinwish)
                Glide.with(view.context).load(R.drawable.wishiconselected)
                    .into(holder.searchbinding?.wishlistBut!!)
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
                    Constant.FirebaseEvent_AddtoWishlist(    data.product?.variants!!.edges[0].node.id.toString(),"1")
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
                Glide.with(view.context).load(R.drawable.wishicon)
                    .into(holder.searchbinding?.wishlistBut!!)
            }
        }
        fun addCart(view: View, data: ListData) {
            if (data.product!!.requiresSellingPlan){
                var intent = Intent(view.context, ProductView::class.java)
                intent.putExtra("ID", data.product!!.id.toString())
                intent.putExtra("tittle", data.textdata)
                view.context.startActivity(intent)
                Constant.activityTransition(view.context)
            }else{
                if (data.product!!.variants.edges[0].node.quantityAvailable > 0 || data.product!!.variants.edges[0].node.currentlyNotInStock || data.product!!.variants.edges[0].node.availableForSale) {
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