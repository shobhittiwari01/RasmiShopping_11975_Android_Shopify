package com.rasmishopping.app.cartsection.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.cartsection.models.CartListItem
import com.rasmishopping.app.cartsection.viewholders.SubscribeCartItem
import com.rasmishopping.app.cartsection.viewmodels.SubscribeCartListModel
import com.rasmishopping.app.databinding.PopConfirmationBinding
import com.rasmishopping.app.databinding.SubscribeCartItemBinding
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.CurrencyFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import javax.inject.Inject

class SubscribeCartListAdapter @Inject constructor(private val repository: Repository) :
    RecyclerView.Adapter<SubscribeCartItem>() {
    var data: MutableList<Storefront.CartLineEdge>? = null
    private var layoutInflater: LayoutInflater? = null
    private var model: SubscribeCartListModel? = null
    var cartlistArray = JSONArray()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val TAG = "CartListAdapter"
    private var warningList: HashMap<String, Boolean> = hashMapOf()
    private var context: Context? = null
    private var stockCallback: StockCallback? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscribeCartItem {
        val binding = DataBindingUtil.inflate<SubscribeCartItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.subscribe_cart_item,
            parent,
            false
        )
        var back =binding.qtysection.background as GradientDrawable
        back.setStroke(2, Color.parseColor(NewBaseActivity.themeColor))
        back.setColor(Color.parseColor(NewBaseActivity.themeColor))
        binding.qtysection.background=back
        binding.quantity.setTextColor(Color.parseColor(NewBaseActivity.textColor))
        binding.decrese.setColorFilter(Color.parseColor(NewBaseActivity.textColor), android.graphics.PorterDuff.Mode.SRC_IN);
        binding.increase.setColorFilter(Color.parseColor(NewBaseActivity.textColor), android.graphics.PorterDuff.Mode.SRC_IN);
        return SubscribeCartItem(binding)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    interface StockCallback {
        fun cartWarning(warning: HashMap<String, Boolean>)
    }

    override fun onBindViewHolder(holder: SubscribeCartItem, position: Int) {
        val item = CartListItem()
        item.position = position
        var merchant = data?.get(position)!!.node.merchandise as Storefront.ProductVariant
        item.product_id = merchant.product.id.toString()
        GlobalScope.launch(Dispatchers.IO) {
            val dataList = repository.allCartItems
            holder.binding.offername.text = dataList[position].offerName
        }
        item.variant_id = merchant.id.toString()
        item.productname = merchant.product.title
        item.quantity_available = merchant.quantityAvailable
        val variant = merchant
        item.normalprice = CurrencyFormatter.setsymbol(
            variant.price.amount,
            variant.price.currencyCode.toString()
        )
        if (variant.compareAtPrice != null) {
            val special = java.lang.Double.valueOf(variant.compareAtPrice.amount)
            val regular = java.lang.Double.valueOf(variant.price.amount)
            if (BigDecimal.valueOf(special).compareTo(BigDecimal.valueOf(regular)) == 1) {
                item.normalprice = CurrencyFormatter.setsymbol(
                    variant.compareAtPrice.amount,
                    variant.compareAtPrice.currencyCode.toString()
                )
                item.specialprice = CurrencyFormatter.setsymbol(
                    variant.price.amount,
                    variant.price.currencyCode.toString()
                )
                item.offertext = getDiscount(special, regular).toString() + "%off"
                holder.binding!!.regularprice.background= ContextCompat.getDrawable(context!!,R.drawable.cross_line)
                holder.binding.specialprice.visibility = View.VISIBLE
                holder.binding.offertext.visibility = View.VISIBLE
            } else {
                holder.binding.specialprice.visibility = View.GONE
                holder.binding.offertext.visibility = View.GONE
                holder.binding!!.regularprice.background= ContextCompat.getDrawable(context!!,R.drawable.no_cross_line)
            }
        } else {
            holder.binding.specialprice.visibility = View.GONE
            holder.binding.offertext.visibility = View.GONE
            holder.binding!!.regularprice.background= ContextCompat.getDrawable(context!!,R.drawable.no_cross_line)
        }
        val model = CommanModel()
        model.imageurl = variant.image?.url
        holder.binding.commondata = model
        holder.binding.currencyCode = variant.price.currencyCode.toString()
        holder.binding.productPrice = variant.price.amount.toDouble()
        item.image = variant.image?.url
        item.qty = data?.get(position)!!.node.quantity!!.toString()
        if (SplashViewModel.featuresModel.in_app_wishlist) holder.binding.movetowish.visibility =
            View.VISIBLE
        else holder.binding.movetowish.visibility = View.GONE
        Log.d(TAG, "onBindViewHolder: " + variant.currentlyNotInStock)
        if (variant.currentlyNotInStock == true == false) {
            if (variant.quantityAvailable!! < data?.get(position)?.node?.quantity!! && variant.availableForSale == true
            ) {
                holder.binding.notinstock.visibility = View.VISIBLE
                holder.binding.notinstock.text =
                    holder.binding.notinstock.context.getString(R.string.avaibale_qty) + " " + variant.quantityAvailable!!
                holder.binding.increase.visibility = View.GONE
                holder.binding.decrese.visibility = View.VISIBLE
                warningList.put(variant.id.toString(), true)
                stockCallback?.cartWarning(warningList)
            } else if (variant.quantityAvailable == 0) {
                holder.binding.notinstock.visibility = View.VISIBLE
                holder.binding.increase.visibility = View.GONE
                holder.binding.decrese.visibility = View.GONE
                warningList.put(variant.id.toString(), true)
                stockCallback?.cartWarning(warningList)
            } else {
                holder.binding.notinstock.visibility = View.GONE
                holder.binding.increase.visibility = View.VISIBLE
                holder.binding.decrese.visibility = View.VISIBLE
                warningList.put(variant.id.toString(), false)
                stockCallback?.cartWarning(warningList)
            }
        } else {
            holder.binding.notinstock.visibility = View.GONE
            holder.binding.increase.visibility = View.VISIBLE
            holder.binding.decrese.visibility = View.VISIBLE
            warningList.put(variant.id.toString(), false)
            stockCallback?.cartWarning(warningList)
        }

        item.currentlyNotInStock = variant.currentlyNotInStock ?: false
        holder.binding.handlers = ClickHandlers()
        setVariants(item, holder, variant.selectedOptions)


        if (merchant.sellingPlanAllocations.edges.size > 0) {
            item.selling_price_id =
                merchant.sellingPlanAllocations.edges[0].node.sellingPlan.id.toString()

        } else {
            item.selling_price_id = ""
        }

    }


    private fun setVariants(
        item: CartListItem,
        holder: SubscribeCartItem,
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

    fun setData(
        data: MutableList<Storefront.CartLineEdge>,
        model: SubscribeCartListModel?,
        context: Context,
        stockCallback: StockCallback?
    ) {
        this.data = data
        this.model = model
        this.context = context
        this.stockCallback = stockCallback
        firebaseAnalytics = Firebase.analytics
    }

    fun getDiscount(regular: Double, special: Double): Int {
        return ((regular - special) / regular * 100).toInt()
    }

    inner class ClickHandlers {
        fun productCartClick(view: View, data: CartListItem) {
            val productintent = Intent(view.context, ProductView::class.java)
            productintent.putExtra("ID", data.product_id)
            productintent.putExtra("tittle", data.productname)
            view.context.startActivity(productintent)
            Constant.activityTransition(view.context)
        }

        fun moveToWishList(view: View, item: CartListItem, currencyCode: String, price: Double) {
            var cartlistData = JSONObject()
            cartlistData.put("id", item.product_id)
            cartlistData.put("quantity", item.qty)
            cartlistArray.put(cartlistData.toString())
            if (SplashViewModel.featuresModel.firebaseEvents) {
                Constant.FirebaseEvent_AddtoWishlist(item.product_id!!,item.qty!!)
            }
            Constant.logAddToWishlistEvent(
                cartlistArray.toString(), item.product_id, "product",
                currencyCode,
                price, context ?: Activity()
            )

            try {
                model!!.moveToWishList(item)
                data!!.removeAt(item.position)
                notifyItemRemoved(item.position)
                notifyItemRangeChanged(item.position, data!!.size)
                warningList.remove(item.variant_id.toString())
                stockCallback?.cartWarning(warningList)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun removeFromCart(view: View, item: CartListItem) {
            val alertDialog = SweetAlertDialog(view.context, SweetAlertDialog.WARNING_TYPE)
            var customeview = PopConfirmationBinding.inflate(LayoutInflater.from(view.context))
            customeview.textView.text=context!!.resources.getString(R.string.warning_message)
            customeview.textView2.text=context!!.resources.getString(R.string.delete_single_cart_warning)
            alertDialog.hideConfirmButton()
            customeview.okDialog.setOnClickListener{
                customeview.okDialog.isClickable=false
                customeview.textView.text=context!!.resources.getString(R.string.deleted)
                customeview.textView2.text=context!!.resources.getString(R.string.cart_single_delete_message)
                alertDialog.showCancelButton(false)
                alertDialog.setConfirmClickListener(null)
                alertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                model!!.removeFromCart(item)
                data!!.removeAt(item.position)
                notifyItemRemoved(item.position)
                notifyItemRangeChanged(item.position, data!!.size)
                warningList.remove(item.variant_id.toString())
                stockCallback?.cartWarning(warningList)
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
        }

        fun increase(view: View, item: CartListItem) {
            if (item.currentlyNotInStock) {
                item.qty = (Integer.parseInt(item.qty!!) + 1).toString()
                model!!.updateCart(item)
            } else {
                if (item.qty?.toInt() == item.quantity_available) {
                    Toast.makeText(
                        view.context,
                        view.context.getString(R.string.variant_quantity_warning),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    item.qty = (Integer.parseInt(item.qty!!) + 1).toString()
                    model!!.updateCart(item)
                }
            }
        }

        fun decrease(view: View, item: CartListItem) {
            if (Integer.parseInt(item.qty!!) == 1) {
                model!!.removeFromCart(item)
                data!!.removeAt(item.position)
                notifyItemRemoved(item.position)
                notifyItemRangeChanged(item.position, data!!.size)
            } else {
                item.qty = (Integer.parseInt(item.qty!!) - 1).toString()
                model!!.updateCart(item)
            }
        }
    }
}
