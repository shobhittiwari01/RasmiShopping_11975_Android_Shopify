package com.shopify.returnprime

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent


class ShipwayTrack  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(intent.getStringExtra("shipway_order_number")!=null){
            ShipwayTracking(intent.getStringExtra("shipway_order_number") as String, intent.getStringExtra("shop_domain") as String)
        }
   }

    fun ShipwayTracking(order_number: String,shop_domain:String)
    {
        var url= "https://${shop_domain}/apps/shipway_track?order_tracking=${order_number}"
        openCustomTab(this, Uri.parse(url))
    }
    fun openCustomTab(activity: Context, uri: Uri) {
        val customTabsIntent= CustomTabsIntent.Builder().build()
        val packageName = "com.android.chrome"
        customTabsIntent.intent.setPackage(packageName)
        customTabsIntent.launchUrl(activity, uri)
    }
}