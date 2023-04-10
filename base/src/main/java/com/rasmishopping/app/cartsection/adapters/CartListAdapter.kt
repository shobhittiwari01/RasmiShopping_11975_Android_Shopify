package com.rasmishopping.app.cartsection.adapters


import android.annotation.SuppressLint
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
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.rasmishopping.app.cartsection.viewholders.CartItem
import com.rasmishopping.app.cartsection.viewmodels.CartListViewModel
import com.rasmishopping.app.databinding.MCartitemBinding
import com.rasmishopping.app.databinding.PopConfirmationBinding
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.productsection.viewholders.ProductItem
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.CurrencyFormatter
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import javax.inject.Inject

class CartListAdapter @Inject constructor(private val repository: Repository) :
    RecyclerView.Adapter<CartItem>() {
    var data: MutableList<Storefront.CheckoutLineItemEdge>? = null
    private var layoutInflater: LayoutInflater? = null
    private var models: CartListViewModel? = null
    var cartlistArray = JSONArray()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val TAG = "CartListAdapter"
    private var warningList: HashMap<String, Boolean> = hashMapOf()
    private var context: Context? = null
    private var wholesaledata: String? = null
    private var stockCallback: StockCallback? = null
    var Discount_Percentage: Int? = null

    /*init {
        setHasStableIds(true)
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItem {
        val binding = DataBindingUtil.inflate<MCartitemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_cartitem,
            parent,
            false
        )
        var back =binding.qtysection.background as GradientDrawable
        back.setStroke(2,Color.parseColor(NewBaseActivity.themeColor))
        back.setColor(Color.parseColor(NewBaseActivity.themeColor))
        binding.qtysection.background=back
        binding.quantity.setTextColor(Color.parseColor(NewBaseActivity.textColor))
        binding.decrese.setColorFilter(Color.parseColor(NewBaseActivity.textColor), android.graphics.PorterDuff.Mode.SRC_IN);
        binding.increase.setColorFilter(Color.parseColor(NewBaseActivity.textColor), android.graphics.PorterDuff.Mode.SRC_IN);
        return CartItem(binding)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    interface StockCallback {
        fun cartWarning(warning: HashMap<String, Boolean>)
    }

    fun setData(
        data: MutableList<Storefront.CheckoutLineItemEdge>,
        model: CartListViewModel?,
        context: Context,
        stockCallback: StockCallback?, wholesaledata: String
    ) {
        this.data = data
        this.models = model
        this.wholesaledata = wholesaledata
        this.context = context
        this.stockCallback = stockCallback
        firebaseAnalytics = Firebase.analytics
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CartItem, position: Int) {
        val item = CartListItem()
        //val models = CartListViewModel()
        holder.binding.remove.tag=position
        item.position = position
        item.product_id = data?.get(position)!!.node.variant.product.id.toString()
        item.variant_id = data?.get(position)!!.node.variant.id.toString()
        item.productname = data?.get(position)!!.node.variant.product.title
        item.quantity_available = data?.get(position)!!.node.variant.quantityAvailable
        val variant = data?.get(position)!!.node.variant
        item.normalprice = CurrencyFormatter.setsymbol(variant.price.amount, variant.price.currencyCode.toString())
        if (!wholesaledata!!.equals("")) {
            val wholesaledata_obj = JSONObject(wholesaledata.toString())
            val wholesale_price = (wholesaledata_obj.getJSONArray("items").get(position) as JSONObject).getString("wpd_price")
            val compare_price = (wholesaledata_obj.getJSONArray("items").get(position) as JSONObject).getString("compare_at_price")
            if (!compare_price.isNullOrEmpty() && compare_price!=wholesale_price) {
                item.normalprice = CurrencyFormatter.setsymbol(compare_price, variant.price.currencyCode.toString())
                item.specialprice = CurrencyFormatter.setsymbol(wholesale_price, variant.price.currencyCode.toString())
                if (wholesale_price != variant.price.amount.toDouble().toInt().toString()) {
                    Discount_Percentage = (((variant.price.amount.toDouble() - wholesale_price.toDouble()) / variant.price.amount.toDouble()) * 100).toDouble().toInt()
                } else {
                    Discount_Percentage = (((variant.compareAtPrice.amount.toDouble() - wholesale_price.toDouble()) / variant.compareAtPrice.amount.toDouble()) * 100).toDouble().toInt()
                }
                if(wholesale_price!=  variant.price.amount.toDouble().toInt().toString()) {
                       Discount_Percentage = (((   variant.price.amount.toDouble()- wholesale_price.toDouble()) / variant.price.amount.toDouble()) * 100).toDouble().toInt()
                } else {
                       Discount_Percentage = (((   compare_price.toDouble()- wholesale_price.toDouble()) / compare_price.toDouble()) * 100).toDouble().toInt()
                }
                item.offertext = "("+Discount_Percentage.toString() + context!!.resources.getString(R.string.off)+")"
                holder.binding!!.regularprice.background= ContextCompat.getDrawable(context!!,R.drawable.cross_line)
                holder.binding.specialprice.visibility = View.VISIBLE
                holder.binding.offertext.visibility = View.VISIBLE

            } else {
                holder.binding.specialprice.visibility = View.GONE
                holder.binding.offertext.visibility = View.GONE
                holder.binding!!.regularprice.background= ContextCompat.getDrawable(context!!,R.drawable.no_cross_line)
            }
        } else if (variant.compareAtPrice != null) {
            val special = java.lang.Double.valueOf(variant.compareAtPrice.amount)
            val regular = java.lang.Double.valueOf(variant.price.amount)
            if (BigDecimal.valueOf(special).compareTo(BigDecimal.valueOf(regular)) == 1) {
                item.normalprice = CurrencyFormatter.setsymbol(variant.compareAtPrice.amount, variant.compareAtPrice.currencyCode.toString())
                item.specialprice = CurrencyFormatter.setsymbol(variant.price.amount, variant.price.currencyCode.toString())
                item.offertext = "("+getDiscount(special, regular).toString() + context!!.resources.getString(R.string.off)+")"
                holder.binding!!.regularprice.background= ContextCompat.getDrawable(context!!,R.drawable.cross_line)
                holder.binding.specialprice.visibility = View.VISIBLE
                holder.binding.offertext.visibility = View.VISIBLE
            } else {
                holder.binding.specialprice.visibility = View.GONE
                holder.binding.offertext.visibility = View.GONE
                holder.binding!!.regularprice.background=ContextCompat.getDrawable(context!!,R.drawable.no_cross_line)
            }
        } else {
            holder.binding.specialprice.visibility = View.GONE
            holder.binding.offertext.visibility = View.GONE
            holder.binding!!.regularprice.background=ContextCompat.getDrawable(context!!,R.drawable.no_cross_line)
        }
        val model = CommanModel()
        var url=variant?.image?.url
        if(url!=null){
            item.image = variant?.image?.url
            model.imageurl = variant?.image?.url
            Log.i("SaifDev_CartList","URL"+variant?.image?.url)
            var width=variant?.image?.width
            var height=variant?.image?.height
            Log.i("SaifDev_CartList","widhth:${width}")
            Log.i("SaifDev_CartList","height:${height}")
            (holder.binding!!.imagesection.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,${width}:${height}"
        }

        holder.binding.commondata = model
        holder.binding.currencyCode = variant.price.currencyCode.toString()
        holder.binding.productPrice = variant.price.amount.toDouble()

        if(data?.get(position)?.node?.variant?.quantityAvailable!!>0 && data!!.get(position).node.variant.availableForSale) {
            if(data?.get(position)!!.node.quantity!!>data?.get(position)?.node?.variant?.quantityAvailable!!){
                data?.get(position)!!.node.quantity=data?.get(position)?.node?.variant?.quantityAvailable
                item.qty = data?.get(position)?.node?.variant?.quantityAvailable.toString()
                models!!.updateCart(item)
            }
        }
        item.qty = data?.get(position)!!.node.quantity!!.toString()
        if (SplashViewModel.featuresModel.in_app_wishlist) holder.binding.movetowish.visibility =
            View.VISIBLE
        else holder.binding.movetowish.visibility = View.GONE
        Log.d(TAG, "onBindViewHolder: " + data?.get(position)?.node?.variant?.currentlyNotInStock)
        if (data?.get(position)?.node?.variant?.currentlyNotInStock == false) {
            if(data?.get(position)?.node?.variant?.quantityAvailable!!<=0 && !data!!.get(position).node.variant.availableForSale) {
                holder.binding.notinstock.visibility = View.VISIBLE
                holder.binding.notinstock.text = holder.binding.notinstock.context.getString(R.string.avaibale_qty) + " " + data?.get(position)?.node?.variant?.quantityAvailable!!
                holder.binding.increase.visibility = View.GONE
                holder.binding.decrese.visibility = View.VISIBLE
                warningList.put(data?.get(position)?.node?.variant?.id.toString(), true)
                stockCallback?.cartWarning(warningList)
            }else {
                holder.binding.notinstock.visibility = View.GONE
                holder.binding.increase.visibility = View.VISIBLE
                holder.binding.decrese.visibility = View.VISIBLE
                warningList.put(data?.get(position)?.node?.variant?.id.toString(), false)
                stockCallback?.cartWarning(warningList)
            }
        } else {
            holder.binding.notinstock.visibility = View.GONE
            holder.binding.increase.visibility = View.VISIBLE
            holder.binding.decrese.visibility = View.VISIBLE
            warningList.put(data?.get(position)?.node?.variant?.id.toString(), false)
            stockCallback?.cartWarning(warningList)
        }
        item.currentlyNotInStock = data?.get(position)?.node?.variant?.currentlyNotInStock ?: false
        holder.binding.handlers = ClickHandlers()
        setVariants(item, holder, variant.selectedOptions)
        holder.binding.increase.setOnClickListener {
            holder.binding.increase.isClickable=false
            Toast.makeText(
                context,
                context!!.getString(R.string.updating_quantity),
                Toast.LENGTH_LONG
            ).show()
            holder.binding.increase.isEnabled = false
            val handler = Handler()
            handler.postDelayed({
                holder.binding.increase.isEnabled = true
                Toast.makeText(
                    context,
                    context!!.getString(R.string.quantity_updated),
                    Toast.LENGTH_LONG
                ).show()
            }, 1500)
            if (item.currentlyNotInStock) {
                item.qty = (Integer.parseInt(item.qty!!) + 1).toString()
                models!!.updateCart(item)
            } else {
                if(item.quantity_available!!>0){
                    if (item.qty?.toInt()!! >=item.quantity_available!!) {
                        Toast.makeText(
                            context,
                            context!!.getString(R.string.variant_quantity_warning),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else {
                        item.qty = (Integer.parseInt(item.qty!!) + 1).toString()
                        models!!.updateCart(item)
                    }
                } else {
                    item.qty = (Integer.parseInt(item.qty!!) + 1).toString()
                    models!!.updateCart(item)
                }
            }
        }

        holder.binding.decrese.setOnClickListener {
            holder.binding.decrese.isClickable=false
            Toast.makeText(
                context,
                context!!.getString(R.string.updating_quantity),
                Toast.LENGTH_LONG
            ).show()
            holder.binding.decrese.isEnabled = false
            val handler = Handler()
            handler.postDelayed({
                holder.binding.decrese.isEnabled = true
                Toast.makeText(
                    context,
                    context!!.getString(R.string.quantity_updated),
                    Toast.LENGTH_LONG
                ).show()
            }, 1500)
            if (Integer.parseInt(item.qty!!) == 1) {
                models!!.removeFromCart(item)
                data!!.removeAt(item.position)
                notifyItemRemoved(item.position)
                notifyItemRangeChanged(item.position, data!!.size)
            } else {
                item.qty = (Integer.parseInt(item.qty!!) - 1).toString()
                models!!.updateCart(item)
            }
        }
    }

    private fun setVariants(
        item: CartListItem,
        holder: CartItem,
        selectedOptions: List<Storefront.SelectedOption>
    ) {
        try {
            val iterator1 = selectedOptions.iterator()
            var counter = 0
            var option: Storefront.SelectedOption
            while (iterator1.hasNext()) {
                counter += 1
                option = iterator1.next()
                if (!option.value.equals("Default Title", true)) {
                    val finalvalue = option.name + " : " + option.value
                    if (counter == 1) {
                        item.variant_one = finalvalue
                        holder.binding.variantOne.visibility=View.VISIBLE
                    }
                    if (counter == 2) {
                        item.variant_two = finalvalue
                        holder.binding.variantTwo.visibility=View.VISIBLE
                    }
                    if (counter == 3) {
                        item.variant_three = finalvalue
                        holder.binding.variantThree.visibility=View.VISIBLE
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
           // view.isClickable=false
            val cartlistData = JSONObject()
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
                if (data!!.size == 1) {
                    models!!.moveToWishList(item)
                    data!!.removeAt(item.position)
                    notifyItemRemoved(item.position)
                    notifyItemRangeChanged(item.position, data!!.size)
                    warningList.remove(item.variant_id.toString())
                    stockCallback?.cartWarning(warningList)
                    context!!.startActivity(
                        Intent(
                            context,
                            HomePage::class.java
                        )
                    )
                } else {
                    models!!.moveToWishList(item)
                    data!!.removeAt(item.position)
                    notifyItemRemoved(item.position)
                    notifyItemRangeChanged(item.position, data!!.size)
                    warningList.remove(item.variant_id.toString())
                    stockCallback?.cartWarning(warningList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun removeFromCart(view: View, item: CartListItem) {
            if (data!!.size == 1) {
                models!!.removeFromCart(item)
                data!!.removeAt(item.position)
                notifyItemRemoved(item.position)
                notifyItemRangeChanged(item.position, data!!.size)
                warningList.remove(item.variant_id.toString())
                stockCallback?.cartWarning(warningList)
                context!!.startActivity(
                    Intent(
                        context,
                        HomePage::class.java
                    )
                )
            } else {

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
                    models!!.removeFromCart(item)
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
        }

        /*fun increase(view: View, item: CartListItem) {
            if (item.currentlyNotInStock) {
                item.qty = (Integer.parseInt(item.qty!!) + 1).toString()
                models!!.updateCart(item)
                val handler = Handler()
                handler.postDelayed({
                }, 1500)
            } else {
                if (item.qty?.toInt() == item.quantity_available) {
                    Toast.makeText(
                        view.context,
                        view.context.getString(R.string.variant_quantity_warning),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    item.qty = (Integer.parseInt(item.qty!!) + 1).toString()
                    models!!.updateCart(item)
                }
            }
        }*/

        /*fun decrease(view: View, item: CartListItem) {
            if (Integer.parseInt(item.qty!!) == 1) {
                models!!.removeFromCart(item)
                data!!.removeAt(item.position)
                notifyItemRemoved(item.position)
                notifyItemRangeChanged(item.position, data!!.size)
            } else {
                item.qty = (Integer.parseInt(item.qty!!) - 1).toString()
                models!!.updateCart(item)
            }
        }*/
    }
}
