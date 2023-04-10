package com.rasmishopping.app.FlitsDashboard.StoreCredits

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.shopify.apicall.ApiCallInterface
import com.shopify.apicall.ApiResponse
import com.shopify.apicall.CustomResponse
import com.shopify.apicall.doRetrofitCall
import com.shopify.buy3.Storefront
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class StoreCreditsViewModel() : ViewModel() {
    lateinit var context: Context
    lateinit var  apiInterface: ApiCallInterface
    private val disposables = CompositeDisposable()
    val credit_data = MutableLiveData<ApiResponse>()
    val spent_rules_data = MutableLiveData<ApiResponse>()
    val apply_discount = MutableLiveData<ApiResponse>()
    var cartdata:String?=null

    fun GetStoreCredit(AppName:String,CustomerId:String,userId:String,token:String) = CoroutineScope(Dispatchers.IO).launch {
        val customer_id=  CustomerId?.replace("gid://shopify/Customer/", "")!!.split("?")[0]
        var retrofit:Retrofit = Retrofit.Builder()
            .baseUrl("https://shopifymobileapp.cedcommerce.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        apiInterface=retrofit.create(ApiCallInterface::class.java)

        doRetrofitCall(

            apiInterface!!.getStoreCredits(AppName,
                "https://app.getflits.com/api/1/$userId/$customer_id/credit/get_credit",token

            ), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    credit_data.value= ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    credit_data.value=ApiResponse.error(error)
                }
            }, context = context
        )
    }
    fun GetSubscribeCartSpentRules(checkout: Storefront.Cart,CustomerId: String) = CoroutineScope(Dispatchers.IO).launch {
        var params: JsonObject = JsonObject()
        var jsonarray: JsonArray = JsonArray()
        params.addProperty("token", "b7e0df7341529fe4a782bc9ca3931f0a")
        params.addProperty("note", "")
        var attr: JsonObject = JsonObject()
        attr.addProperty("", "")
        params.add("attribute", attr)
        params.addProperty("total_discount", 0)
        params.addProperty("total_weight", 0.0)
        params.addProperty(
            "original_total_price",
            checkout.cost.totalAmount.amount.toDouble().toInt()
        )
        params.addProperty(
            "total_price",
            checkout.cost.totalAmount.amount.toDouble().toInt()
        )
        params.addProperty("item_count", checkout.lines.edges.size)
        for (i in 0 until checkout.lines.edges.size) {
            var variantJsonObject: JsonObject = JsonObject()

            val variant =
                checkout.lines.edges.get(i).node.merchandise as Storefront.ProductVariant
            val variant_id =
                variant.id.toString().replace("gid://shopify/ProductVariant/", "")
                    .split("?")[0]
            val product_id =
                variant.product.id.toString().replace("gid://shopify/Product/", "")
                    .split("?")[0]
            variantJsonObject.addProperty("product_id", product_id.toLong())
            variantJsonObject.addProperty("quantity", checkout.lines.edges.get(i).node.quantity)
            variantJsonObject.addProperty(
                "price",
                checkout.lines.edges.get(i).node.cost.totalAmount.amount.toDouble()
                    .toInt()
            )
            variantJsonObject.addProperty(
                "original_price",
                checkout.lines.edges.get(i).node.cost.totalAmount.amount.toDouble()
                    .toInt()
            )
            variantJsonObject.addProperty(
                "discounted_price",
                checkout.lines.edges.get(i).node.cost.totalAmount.amount.toDouble()
                    .toInt()
            )
            variantJsonObject.addProperty("line_price", checkout.lines.edges.get(i).node.cost.totalAmount.amount.toDouble().toInt())
            variantJsonObject.addProperty("vendor", variant.product.vendor)
            variantJsonObject.addProperty("texable", true)
            variantJsonObject.addProperty("image", variant.image.url)
            variantJsonObject.addProperty("original_line_price", checkout.lines.edges.get(i).node.cost.totalAmount.amount.toDouble().toInt())
            jsonarray.add(variantJsonObject)
        }

        params.add("items", jsonarray)
        cartdata= params.toString()

        Log.d("cartencodedData", "" + getBase64Encode(params.toString()))
        val customer_id= CustomerId.replace("gid://shopify/Customer/", "")!!.split("?")[0]
        var retrofit:Retrofit = Retrofit.Builder()
            .baseUrl("https://shopifymobileapp.cedcommerce.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(

            apiInterface!!.getSpentRules("",
                "https://app.getflits.com/api/1/24088/$customer_id/credit/get_spent_rules","b7e0df7341529fe4a782bc9ca3931f0a",getBase64Encode(params.toString()).toString()), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    spent_rules_data.postValue(ApiResponse.success(result))
                }

                override fun onErrorRetrofit(error: Throwable) {
                    spent_rules_data.postValue(ApiResponse.error(error))
                }
            }, context = context
        )
    }
    fun GetUnsubscribeCartSpentRules(AppName: String,checkout:Storefront.Checkout,CustomerId: String,userId: String,token: String) = CoroutineScope(Dispatchers.IO).launch {


        var params: JsonObject = JsonObject()
        var jsonarray: JsonArray = JsonArray()
        params.addProperty("token", token)
        params.addProperty("note", "")
        var attr: JsonObject = JsonObject()
        attr.addProperty("", "")
        params.add("attribute", attr)
        params.addProperty("total_discount", 0)
        params.addProperty("total_weight", 0.0)
        params.addProperty(
            "original_total_price",
            checkout.totalPrice.amount.toDouble().toInt()*100
        )
        params.addProperty(
            "total_price",
            checkout.totalPrice.amount.toDouble().toInt()*100
        )
        params.addProperty("item_count", checkout.lineItems.edges.size)
        for (i in 0 until checkout.lineItems.edges.size) {
            var variantJsonObject: JsonObject = JsonObject()

            val variant =
                checkout.lineItems.edges.get(i).node.variant
            val variant_id =
                variant.id.toString().replace("gid://shopify/ProductVariant/", "")
                    .split("?")[0]
            val product_id =
                variant.product.id.toString().replace("gid://shopify/Product/", "")
                    .split("?")[0]
            variantJsonObject.addProperty("id", variant_id.toLong())
            variantJsonObject.addProperty("key", "$product_id:c572b018c17d62853985e19b2b11a9a4")
            variantJsonObject.addProperty("properties", "")
            variantJsonObject.addProperty("variant_id", variant_id.toLong())
            variantJsonObject.addProperty("product_id", product_id.toLong())
            variantJsonObject.addProperty("quantity", checkout.lineItems.edges.get(i).node.quantity)
            variantJsonObject.addProperty(
                "price",
                checkout.lineItems.edges.get(i).node.variant.price.amount.toDouble()
                    .toInt()*100
            )
            variantJsonObject.addProperty(
                "original_price",
                checkout.lineItems.edges.get(i).node.variant.price.amount.toDouble()
                    .toInt()*100
            )
            variantJsonObject.addProperty(
                "discounted_price",
                checkout.lineItems.edges.get(i).node.variant.price.amount.toDouble()
                    .toInt()*100
            )
            variantJsonObject.addProperty(
                "line_price",
                checkout.lineItems.edges.get(i).node.variant.price.amount.toDouble()
                    .toInt()*100
            )
            variantJsonObject.addProperty("vendor", variant.product.vendor)
            variantJsonObject.addProperty("texable", true)


            variantJsonObject.addProperty("image", variant.image.url)
            variantJsonObject.addProperty(
                "original_line_price",
                checkout.lineItems.edges.get(i).node.variant.price.amount.toDouble()
                    .toInt()*100
            )
            jsonarray.add(variantJsonObject)
        }

        params.add("items", jsonarray)
        cartdata=getBase64Encode(params.toString())





        val customer_id=  CustomerId.replace("gid://shopify/Customer/", "")!!.split("?")[0]
        var retrofit:Retrofit = Retrofit.Builder()
            .baseUrl("https://shopifymobileapp.cedcommerce.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(

            apiInterface!!.getSpentRules(AppName,
                "https://app.getflits.com/api/1/$userId/$customer_id/credit/get_spent_rules",token,getBase64Encode(params.toString()).toString()), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    spent_rules_data.postValue(ApiResponse.success(result))
                }

                override fun onErrorRetrofit(error: Throwable) {
                    spent_rules_data.postValue(ApiResponse.error(error))
                }
            }, context = context
        )
    }
    fun ApplyStoreCredit(AppName: String,spent_rule_id:Int,CustomerId: String,userId: String,token: String,checkout_id:String) = CoroutineScope(Dispatchers.IO).launch {

        val customer_id=   CustomerId.replace("gid://shopify/Customer/", "")!!.split("?")[0]
        var retrofit:Retrofit = Retrofit.Builder()
            .baseUrl("https://shopifymobileapp.cedcommerce.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(

            apiInterface!!.ApplyStoreCredit(AppName,
                "https://app.getflits.com/api/1/$userId/$customer_id/credit/apply_credit",token,cartdata!!,spent_rule_id,md5(checkout_id)!!), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    apply_discount.postValue(ApiResponse.success(result))
                }

                override fun onErrorRetrofit(error: Throwable) {
                    apply_discount.postValue(ApiResponse.error(error))
                }
            }, context = context
        )
    }
    fun md5(s: String): String? {
        try {
            // Create MD5 Hash
            val digest: MessageDigest = MessageDigest.getInstance("MD5")
            digest.update(s.toByteArray())
            val messageDigest: ByteArray = digest.digest()

            // Create Hex String
            val hexString = StringBuffer()
            for (i in messageDigest.indices) hexString.append(
                Integer.toHexString(
                    0xFF and messageDigest[i]
                        .toInt()
                )
            )
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }
    private fun getBase64Encode(id: String): String {
        var id = id
        val data = Base64.encode(id.toByteArray(), Base64.DEFAULT)
        try {
            id = String(data, Charset.defaultCharset()).trim { it <= ' ' }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return id
    }
}