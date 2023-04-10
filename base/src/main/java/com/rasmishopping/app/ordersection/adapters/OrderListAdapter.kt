package com.rasmishopping.app.ordersection.adapters

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.shopify.buy3.Storefront
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.Weblink
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.databinding.MOrderitemBinding
import com.rasmishopping.app.databinding.PopConfirmationBinding
import com.rasmishopping.app.ordersection.activities.OrderList
import com.rasmishopping.app.ordersection.models.Order
import com.rasmishopping.app.ordersection.viewholders.OrderItem
import com.rasmishopping.app.ordersection.viewmodels.OrderListViewModel
import com.rasmishopping.app.utils.CurrencyFormatter
import com.rasmishopping.app.utils.Urls
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Handler
import javax.inject.Inject

class OrderListAdapter @Inject
constructor() : RecyclerView.Adapter<OrderItem>() {
    var data: MutableList<Storefront.OrderEdge>? = null
    private var model: OrderListViewModel? = null
    private val TAG = "OrderListAdapter"
    private var reference:OrderList?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItem {
        val binding = DataBindingUtil.inflate<MOrderitemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_orderitem,
            parent,
            false
        )
        binding.orderdetails.textSize = 11f
       // binding.orderdetails.setTextColor(binding.root.context.resources.getColor(R.color.colorPrimaryDark))
        binding.orderno.textSize = 11f
        binding.name.textSize = 11f
        binding.placedontext.textSize = 11f
        binding.date.textSize = 11f
        binding.totalspendingtext.textSize = 11f
        binding.ordernoheading.textSize = 11f
        binding.boughtforheading.textSize = 11f
        binding.boughtfor.textSize = 11f
        binding.ordernoheading.textSize = 11f
        binding.totalspending.textSize = 11f
        return OrderItem(binding)
    }

    override fun onBindViewHolder(holder: OrderItem, position: Int) {
        try {
            val order = Order()
            order.activityreference=reference
            order.orderEdge = data?.get(position)!!.node
            order.ordernumber = data?.get(position)!!.node.orderNumber!!.toString()
            order.name = data?.get(position)!!.node.name
            val sdf2 = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val expiretime =
                sdf.parse(data?.get(position)!!.node.processedAt.toLocalDateTime().toString())
            val time = sdf2.format(expiretime!!)
            order.date = time
            order.price = CurrencyFormatter.setsymbol(
                data?.get(position)!!.node.totalPrice.amount,
                data?.get(position)!!.node.totalPrice.currencyCode.toString()
            )
            order.status = data?.get(position)!!.node.statusUrl
            if (data?.get(position)!!.node.shippingAddress != null) {
                holder.binding.boughtfor.visibility = View.VISIBLE
                holder.binding.boughtforheading.visibility = View.VISIBLE
                order.boughtfor =
                    data?.get(position)!!.node.shippingAddress.firstName + " " + data?.get(position)!!.node.shippingAddress.lastName
            } else {
                holder.binding.boughtfor.visibility = View.GONE
                holder.binding.boughtforheading.visibility = View.GONE
            }
            if (data?.get(position)!!.node.fulfillmentStatus.name.equals("FULFILLED")) {
                holder.binding.trackBtn.visibility=View.VISIBLE
            }
            else{
                holder.binding.trackBtn.visibility=View.GONE
            }
            holder.binding.reorderBut.setOnClickListener {
                val alertDialog = SweetAlertDialog(holder.binding.reorderBut.context, SweetAlertDialog.NORMAL_TYPE)
                var customeview = PopConfirmationBinding.inflate(LayoutInflater.from(holder.binding.reorderBut.context))
                customeview.textView.text = holder.binding.reorderBut.context?.getString(R.string.confirmation)
                customeview.textView2.text = holder.binding.reorderBut.context?.getString(R.string.reorder_confirmation)
                alertDialog.hideConfirmButton()
                customeview.okDialog.setOnClickListener{
                    customeview.okDialog.isClickable=false
                    customeview.textView.text=holder.binding.reorderBut.context?.getString(R.string.done)
                    customeview.textView2.text=holder.binding.reorderBut.context?.getString(R.string.reorder_success_msg)
                    for (i in 0 until data?.get(position)?.node?.lineItems?.edges?.size!!) {
                        Log.d(TAG, "onBindViewHolder: " + data?.get(position)?.node?.lineItems?.edges?.get(i)?.node?.variant?.id)
                        Log.d(TAG, "onBindViewHolder: " + data?.get(position)?.node?.lineItems?.edges?.get(i)?.node?.quantity)
                        model?.addToCart(data?.get(position)?.node?.lineItems?.edges?.get(i)?.node?.variant?.id.toString(), data?.get(position)?.node?.lineItems?.edges?.get(i)?.node?.quantity?.toInt() ?: 0)
                    }
                    alertDialog.showCancelButton(false)
                    alertDialog.setConfirmClickListener(null)
                    alertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                    android.os.Handler().postDelayed({ alertDialog.cancel() },1000)
                }
                customeview.noDialog.setOnClickListener{
                    customeview.noDialog.isClickable=false
                    alertDialog.cancel()
                }
                alertDialog.setCustomView(customeview.root)
                alertDialog.show()
            }
            if (SplashViewModel.featuresModel.reOrderEnabled) {
                holder.binding.reorderBut.visibility = View.VISIBLE
            } else {
                holder.binding.reorderBut.visibility = View.GONE
            }
            holder.binding.trackBtn.setOnClickListener {

//                openCustomTab(it.context, Uri.parse(url))
            }
            //////////////Return Prime//////////////
            if (SplashViewModel.featuresModel.returnprime) {
                if (data?.get(position)!!.node.fulfillmentStatus.name.equals("FULFILLED")) {
                    order.returnorder = true
                }
            }

            //////////////Return Prime//////////////


            //////////////Shipway Order Tracking//////////////
            if (SplashViewModel.featuresModel.shipway_order_tracking) {
                if (data?.get(position)!!.node.fulfillmentStatus.name.equals("FULFILLED")) {
                    order.shipwaytrack = true
                }
            }

            holder.binding.features = SplashViewModel.featuresModel
            holder.binding.order = order
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun openCustomTab(activity: Context, uri: Uri) {
        val customTabsIntent= CustomTabsIntent.Builder().build()
        val packageName = "com.android.chrome"
        customTabsIntent.intent.setPackage(packageName)
        customTabsIntent.launchUrl(activity, uri)
    }
    override fun getItemCount(): Int {
        return data!!.size
    }

    fun setData(data: MutableList<Storefront.OrderEdge>?, model: OrderListViewModel?,reference:OrderList?) {
        this.data = data
        this.model = model
        this.reference=reference
    }
}
