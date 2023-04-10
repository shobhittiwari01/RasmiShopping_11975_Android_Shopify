package com.rasmishopping.app.ordersection.models
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import com.shopify.buy3.Storefront
import com.rasmishopping.app.BuildConfig
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.Weblink
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.ordersection.activities.OrderDetails
import com.rasmishopping.app.ordersection.activities.OrderList
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.Urls


class Order : BaseObservable() {
    var ordernumber: String? = null
    var name: String? = null
    var date: String? = null
    var price: String? = null
    var status: String? = null
    var boughtfor: String? = null
    var orderEdge: Storefront.Order? = null
    var activityreference: OrderList? =null

    @get:Bindable
    var returnorder: Boolean = false
        set(returnorder) {
            field = returnorder
            notifyPropertyChanged(BR.returnorder)
        }
    @get:Bindable
    var shipwaytrack: Boolean = false
        set(returnorder) {
            field = returnorder
            notifyPropertyChanged(BR.returnorder)
        }
    fun orderView(view: View, order: Order) {
        if (SplashViewModel.featuresModel.nativeOrderView) {
            Log.i("MageNative", "SaifDev" + order.orderEdge!!.customerUrl)
            Log.i("MageNative", "SaifDev" + order.orderEdge!!.statusUrl)
            val intent = Intent(view.context, OrderDetails::class.java)
            intent.putExtra("name", order.name)
            intent.putExtra("orderData", order.orderEdge)
            view.context.startActivity(intent)
            Constant.activityTransition(view.context)
        } else {
            val intent = Intent(view.context, Weblink::class.java)
            intent.putExtra("name", order.name)
            intent.putExtra("link", order.orderEdge!!.statusUrl)
            view.context.startActivity(intent)
            Constant.activityTransition(view.context)
        }
    }

    fun returnPrime(view: View, order: Order) {
        if(order.activityreference!!.checkIfModuleInstall( activityreference?.getString(R.string.module_returnprime)!!))
        {
            val order_number = order.ordernumber
            val customer_email = order.orderEdge!!.email
            val shop_domain = Urls(MyApplication.context).shopdomain
            val options = Bundle()
            val intent=Intent()
            intent.setClassName(BuildConfig.APPLICATION_ID, "com.shopify.returnprime.LoadPrime")
            intent.putExtra("order_number",order_number)
            intent.putExtra("customer_email",customer_email)
            intent.putExtra("shop_domain",shop_domain)
            startActivity(view.context,intent,options)

        }else{
            order.activityreference!!.installModule(activityreference?.getString(R.string.module_returnprime)!!)
        }

    }
    fun ShipwayTrack(view: View, order: Order)
    {
        if(order.activityreference!!.checkIfModuleInstall( activityreference?.getString(R.string.module_returnprime)!!))
        {

            val order_number = order.ordernumber
            val shop_domain = Urls(MyApplication.context).shopdomain
            val options = Bundle()
            val intent=Intent()
            intent.setClassName(BuildConfig.APPLICATION_ID, "com.shopify.returnprime.ShipwayTrack")
            intent.putExtra("shipway_order_number",order_number)
            intent.putExtra("shop_domain",shop_domain)
            startActivity(view.context,intent,options)

        }else{
            order.activityreference!!.installModule(activityreference?.getString(R.string.module_returnprime)!!)
        }
    }


}
