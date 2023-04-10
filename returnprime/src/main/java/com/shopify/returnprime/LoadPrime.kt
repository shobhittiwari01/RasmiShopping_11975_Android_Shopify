package com.shopify.returnprime
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.utils.Urls

class LoadPrime : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(intent.getStringExtra("order_number")!=null){
            returnPrime(intent.getStringExtra("order_number") as String,
                intent.getStringExtra("customer_email") as String,
                intent.getStringExtra("shop_domain") as String
            )
        }

    }
    fun returnPrime( order_number: String,email:String,shop_domain:String) {
        val channel_id = Urls.Channel_id
        val returnprimeurl = "https://admin.returnprime.com/external/fetch-order?" +
                "order_number=$order_number&email=$email&store=$shop_domain&channel_id=$channel_id"
        openCustomTab(this, Uri.parse(returnprimeurl))
    }

    fun openCustomTab(activity: Context, uri: Uri) {
        val customTabsIntent=CustomTabsIntent.Builder().build()
        val packageName = "com.android.chrome"
        customTabsIntent.intent.setPackage(packageName)
        customTabsIntent.launchUrl(activity, uri)
    }
}