package com.shopify.zapietapp

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.shopify.apicall.ApiCallInterface
import com.shopify.apicall.ApiResponse
import com.shopify.apicall.CustomResponse
import com.shopify.apicall.doRetrofitCall
import com.shopify.buy3.Storefront
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.HashMap

class ZapietViewModel() : ViewModel() {
    @SuppressLint("StaticFieldLeak")
    lateinit var context: Context
    lateinit var  apiInterface: ApiCallInterface
    private val disposables = CompositeDisposable()
    val delivery_status = MutableLiveData<ApiResponse>()
    val delivery_dates = MutableLiveData<ApiResponse>()
    val delivery_validation = MutableLiveData<ApiResponse>()
    val localdelivery = MutableLiveData<ApiResponse>()
    val localdeliverydates = MutableLiveData<ApiResponse>()
    val shippingcalender = MutableLiveData<ApiResponse>()
    var location_id: String? = null

    /***************************************************** Pickup Locations *************************************************/

    fun ZapietResponseResponse(
        shop: String
    ): MutableLiveData<ApiResponse> {
        DeliveryStatus(
            shop)
        return delivery_status
    }

    fun DeliveryStatus(shop: String) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api-us.zapiet.com/v1.0/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(
            apiInterface.DeliveryStatus(shop),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    delivery_status.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    delivery_status.value = ApiResponse.error(error)
                }
            },
            context = context!!
        )
    }

    /*********************************************************************************************************************/

    /*************************************************** Delivery Dates **************************************************/

    fun dynamiclocation_id(locationId: String?) {
        location_id = locationId
    }

    fun ZapietDatesResponse(
        shop: String
    ): MutableLiveData<ApiResponse> {
        DeliveryDates(
            shop)
        return delivery_dates
    }

    fun DeliveryDates(shop: String) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api-us.zapiet.com/v1.0/pickup/locations/"+location_id+"/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiInterface=retrofit.create(ApiCallInterface::class.java)
            doRetrofitCall(
                apiInterface.StoreDeliverydates(shop),
                disposables,
                customResponse = object : CustomResponse {
                    override fun onSuccessRetrofit(result: JsonElement) {
                        delivery_dates.value = ApiResponse.success(result)
                    }

                    override fun onErrorRetrofit(error: Throwable) {
                        delivery_dates.value = ApiResponse.error(error)
                    }
                },
                context = context!!
            )
    }

    /******************************************************************************************************************/

    /******************************************* Validation Api ***********************************************************/

    fun ZapietValidationResponse(
        shop: String
    ): MutableLiveData<ApiResponse> {
        validateDelivery(
            shop)
        return delivery_validation
    }

    fun validateDelivery(shop: String) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api-us.zapiet.com/v1.0/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(
            apiInterface.validateDelivery("magenative.myshopify.com"),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    delivery_validation.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    delivery_validation.value = ApiResponse.error(error)
                }
            },
            context = context!!
        )
    }

    /*************************************************************************************************************************/

    /***************************************************** Local Delivery ****************************************************/

    fun ZapietLocalDeliveryResponse(
        shop: String,geoSearchQuery: String
    ): MutableLiveData<ApiResponse> {
        LocalDelivery(shop,geoSearchQuery)
        return localdelivery
    }

    fun LocalDelivery(shop: String,geoSearchQuery: String) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api-us.zapiet.com/v1.0/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(
            apiInterface.LocalDelivery(shop,geoSearchQuery),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    localdelivery.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    localdelivery.value = ApiResponse.error(error)
                }
            },
            context = context!!
        )
    }

    /******************************************************************************************************************************/

    /************************************************ Local Delivery Dates *********************************************************/

    fun ZapietLocalDeliverydatesResponse(
        shop: String
    ): MutableLiveData<ApiResponse> {
        LocalDeliverydates(
            shop)
        return localdeliverydates
    }

    fun LocalDeliverydates(shop: String) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api-us.zapiet.com/v1.0/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(
            apiInterface.LocalDeliveryDates(shop),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    localdeliverydates.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    localdeliverydates.value = ApiResponse.error(error)
                }
            },
            context = context!!
        )
    }

    /*******************************************************************************************************************************/

    /************************************************ Shipping Calender ************************************************************/

    fun ShippingCalenderResponse(
        shop: String
    ): MutableLiveData<ApiResponse> {
        getShippingCalender(
            shop)
        return shippingcalender
    }

    fun getShippingCalender(shop: String) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api-us.zapiet.com/v1.0/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(
            apiInterface.getShippingCalender(shop),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    shippingcalender.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    shippingcalender.value = ApiResponse.error(error)
                }
            },
            context = context!!
        )
    }

    /*******************************************************************************************************************************/

    /********************************************* Validation Params **************************************************************/

    fun fillDeliveryParam(edges: List<Storefront.CheckoutLineItemEdge>): HashMap<String, String> {
        var param = HashMap<String, String>()
        for (i in 0..edges.size - 1) {
            param.put(
                "cart[$i][product_id]",
                edges[i].node.id.toString()
                    .replace("gid://shopify/CheckoutLineItem/", "").split("?")[0]
            )
            param.put(
                "cart[$i][variant_id]",
                edges[i].node.variant.id.toString().replace("gid://shopify/ProductVariant/", "")
            )
            param.put("cart[$i][quantity]", edges[i].node.quantity.toString())
        }
        param.put("shop", context.resources.getString(R.string.shop))
        param.put("type", "delivery")
        return param
    }

    /********************************************************************************************************************************/
}