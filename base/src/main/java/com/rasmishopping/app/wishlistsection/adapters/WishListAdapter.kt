package com.rasmishopping.app.wishlistsection.adapters
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.databinding.MWishitemBinding
import com.rasmishopping.app.databinding.PopConfirmationBinding
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.wishlistsection.models.WishListItem
import com.rasmishopping.app.wishlistsection.viewholders.WishItem
import com.rasmishopping.app.wishlistsection.viewmodels.WishListViewModel
import javax.inject.Inject
class WishListAdapter @Inject
constructor() : RecyclerView.Adapter<WishItem>() {
    var data: MutableList<Storefront.CheckoutLineItemEdge>? = null
    private var context: Context? = null
    var VariantID: String? = null
    companion object { var variantCallback: variantCallback? = null }
    private var wish_model: WishListViewModel? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishItem {
        val binding = DataBindingUtil.inflate<MWishitemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_wishitem,
            parent,
            false
        )
        return WishItem(binding)
    }
    override fun onBindViewHolder(holder: WishItem, position: Int) {
        val item = WishListItem()
        val model = CommanModel()
        item.position = position
        item.product_id = data?.get(position)!!.node.variant.product.id.toString()
        item.variant_id = data?.get(position)!!.node.variant.id.toString()
        item.available_qty = data?.get(position)!!.node.variant.quantityAvailable.toString()
        VariantID = data?.get(position)!!.node.variant.id.toString()
        item.productname = data?.get(position)!!.node.variant.product.title
        val variant = data?.get(position)!!.node.variant
        var url=variant?.image?.url
        if(url!=null){
            model.imageurl = variant?.image?.url
            Log.i("SaifDev_WishlistList","URL"+variant?.image?.url)
            var width= variant?.image!!.width
            var height=variant?.image!!.height
            Log.i("SaifDev_WishlistList","widhth:${width}")
            Log.i("SaifDev_WishlistList","height:${height}")
            (holder.binding!!.imagesection.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,${variant?.image!!.width}:${variant?.image!!.height}"
        }
        holder.binding.commondata = model
        var drawable=holder.binding!!.movetocart.background as GradientDrawable
        drawable.setColor(Color.parseColor(NewBaseActivity.themeColor))
        drawable.setStroke(2,Color.parseColor(NewBaseActivity.themeColor))
        holder.binding!!.movetocart.background=drawable
        holder.binding!!.movetocart.text=context!!.resources.getString(R.string.movetocart)
        holder.binding!!.movetocart.setTextColor(Color.parseColor(NewBaseActivity.textColor))
        holder.binding.handler = ClickHandlers()
        if (variant.currentlyNotInStock == false) {
            if (variant.quantityAvailable <= 0 && !variant.availableForSale) {
                var drawable=holder.binding!!.movetocart.background as GradientDrawable
                drawable.setColor(Color.parseColor("#F0F0F0"))
                drawable.setStroke(2, Color.parseColor("#F0F0F0"))
                holder.binding!!.movetocart.background=drawable
                holder.binding!!.movetocart.setTextColor(Color.parseColor("#F43939"))
                holder.binding!!.movetocart.text=context!!.resources.getString(R.string.out_of_stock)
                if (SplashViewModel.featuresModel.outOfStock!!) {
                    holder.binding?.movetocart?.visibility = View.VISIBLE
                }else{
                    holder.binding?.movetocart?.visibility = View.GONE
                }
                holder.binding?.image?.alpha = 0.7f
                holder.binding!!.movetocart.isClickable=false
                item.available=false
            } else {
                if(SplashViewModel.featuresModel.addCartEnabled){
                    holder.binding?.movetocart?.visibility = View.VISIBLE
                }else{
                    holder.binding?.movetocart?.visibility = View.GONE
                }
                holder.binding?.image?.alpha = 1f
                holder.binding!!.movetocart.isClickable=true
                item.available=true
            }
        } else {
            if(SplashViewModel.featuresModel.addCartEnabled){
                holder.binding?.movetocart?.visibility = View.VISIBLE
            }else{
                holder.binding?.movetocart?.visibility = View.GONE
            }
            holder.binding?.image?.alpha = 1f
            holder.binding!!.movetocart.isClickable=true
            item.available=true
        }
        setVariants(item, holder, variant.selectedOptions)
    }
    private fun setVariants(
        item: WishListItem,
        holder: WishItem,
        selectedOptions: List<Storefront.SelectedOption>
    ) {
        try {
            val iterator1 = selectedOptions.iterator()
            var counter = 0
            var option: Storefront.SelectedOption
            while (iterator1.hasNext()) {
                counter = counter + 1
                option = iterator1.next()
                if (!option.value.equals("Default Title", true)) {
                    val finalvalue = option.name + " : " + option.value
                    if (counter == 1) {
                        item.variant_one = finalvalue
                    }
                    if (counter == 2) {
                        item.variant_two = finalvalue
                    }
                    if (counter == 3) {
                        item.variant_three = finalvalue
                    }
                    if (counter > 3) {
                        break
                    }
                }
            }
            holder.binding.variantdata = item
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    override fun getItemCount(): Int {
        return data!!.size
    }
    interface variantCallback {
        fun callback(variantSize: Int)
    }
    fun setData(
        data: MutableList<Storefront.CheckoutLineItemEdge>,
        context: Context,
        wish_model: WishListViewModel
    ) {
        this.data = data
        this.wish_model = wish_model
        this.context = context
        Log.i("MageNative", "wishcount 2 : " + this.data!!.size)
    }
    inner class ClickHandlers {
        fun removeWishList(view: View, item: WishListItem) {
            try {
                    val alertDialog = SweetAlertDialog(view.context, SweetAlertDialog.WARNING_TYPE)
                    var customeview = PopConfirmationBinding.inflate(LayoutInflater.from(view.context))
                    customeview.textView.text=context!!.resources.getString(R.string.warning_message)
                    customeview.textView2.text=context!!.resources.getString(R.string.delete_wishlist_warning)
                    alertDialog.hideConfirmButton()
                    customeview.okDialog.setOnClickListener{
                        customeview.okDialog.isClickable=false
                        customeview.textView.text=context!!.resources.getString(R.string.deleted)
                        customeview.textView2.text=context!!.resources.getString(R.string.wishlist_deleted_message)
                        alertDialog.showCancelButton(false)
                        alertDialog.setConfirmClickListener(null)
                        alertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                        wish_model!!.removeFromWishlist(item)
                        data!!.removeAt(item.position!!)
                        notifyItemRemoved(item.position!!)
                        notifyItemRangeChanged(item.position!!, data!!.size)
                        wish_model!!.update(true)
                        val handler = Handler()
                        handler.postDelayed({
                            alertDialog.cancel()
                        }, 1500)

                    }
                    customeview.noDialog.setOnClickListener{
                        customeview.noDialog.isClickable=false
                        alertDialog.cancel()
                    }
                    alertDialog.setCustomView(customeview.root)
                    alertDialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        fun productClick(view: View, data: WishListItem) {
            val productintent = Intent(view.context, ProductView::class.java)
            productintent.putExtra("ID", data.product_id)
            productintent.putExtra("tittle", data.productname)
            productintent.putExtra("Variant_ID", data.variant_id)
            productintent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            productintent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            productintent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            view.context.startActivity(productintent)
            Constant.activityTransition(view.context)
        }
        fun moveToCart(view: View, item: WishListItem) {
            if(item.available!!){
                wish_model?.addToCartFromWishlist(item.variant_id.toString(), 1)
                Toast.makeText(context, R.string.success_moved, Toast.LENGTH_SHORT).show()
                wish_model!!.removeFromWishlist(item)
                data!!.removeAt(item.position!!)
                notifyItemRemoved(item.position)
                notifyItemRangeChanged(item.position, data!!.size)
                wish_model!!.update(true)
            }
        }
    }
}
