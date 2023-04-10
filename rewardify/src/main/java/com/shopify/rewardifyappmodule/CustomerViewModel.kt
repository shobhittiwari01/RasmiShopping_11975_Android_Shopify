package com.shopify.rewardifyappmodule

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.shopify.apicall.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CustomerViewModel : ViewModel() {
    lateinit var context: Context
    lateinit var  apiInterface: ApiCallInterface
    private val disposables = CompositeDisposable()
    val access_token = MutableLiveData<ApiResponse>()
    val customer_data = MutableLiveData<ApiResponse>()
    val transaction_data = MutableLiveData<ApiResponse>()
    val redeem_data = MutableLiveData<ApiResponse>()
    val discount_data = MutableLiveData<ApiResponse>()
    val recoverdiscount = MutableLiveData<ApiResponse>()


    fun getToken(clientid:String, clientsecret:String) = CoroutineScope(Dispatchers.IO).launch {
        Log.d("javed", "getToken: "+clientid+" d "+clientsecret)
        doRetrofitCall(
            RetrofitManager().getRetrofit().getToken(
                "oauth/v2/token",
                clientid,
                clientsecret,
                "client_credentials"

            ), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    Log.d("javed", "onSuccessRetrofit: "+result)
                    access_token.value=ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    Log.d("javed", "onSuccessRetrofit: "+error)
                    access_token.value=ApiResponse.error(error)
                }
            }, context = context
        )
    }


    fun getCustomerInfo(customer_id:String,token:String) = CoroutineScope(Dispatchers.IO).launch {
        doRetrofitCall(
            RetrofitManager().getRetrofit().getCustomer(
                "customer/$customer_id/account",
                "Bearer "+token
            ), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    Log.d("javed", "onSuccessRetrofit: "+result)
                    customer_data.value=ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    Log.d("javed", "onSuccessRetrofit: "+error)
                    customer_data.value=ApiResponse.error(error)
                }
            }, context = context
        )
    }

    fun getTransactionInfo(customer_id:String,token:String,page:Int,itemperpage:Int) = CoroutineScope(Dispatchers.IO).launch {
        doRetrofitCall(
            RetrofitManager().getRetrofit().getTransactions(
                "customer/$customer_id/account/transactions",
                "Bearer "+token,
                page,itemperpage
            ), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    Log.d("javed", "onSuccessRetrofit: "+result)
                    transaction_data.value=ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    Log.d("javed", "onSuccessRetrofit: "+error)
                    transaction_data.value=ApiResponse.error(error)
                }
            }, context = context
        )
    }


    fun getRedeemInfo(customer_id:String,token:String,jsonObject: JsonObject) = CoroutineScope(Dispatchers.IO).launch {
        doRetrofitCall(
            RetrofitManager().getRetrofit().Redeem(
                "customer/$customer_id/account/redeem",
                "Bearer "+token,
                jsonObject
            ), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    Log.d("javed", "onSuccessRetrofit: "+result)
                    redeem_data.value=ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    Log.d("javed", "onSuccessRetrofit: "+error)
                    redeem_data.value=ApiResponse.error(error)
                }
            }, context = context
        )
    }

    fun getDiscountList(customer_id:String,token:String,page:Int,itemperpage:Int) = CoroutineScope(Dispatchers.IO).launch {
        doRetrofitCall(
            RetrofitManager().getRetrofit().getDiscounts(
                "customer/$customer_id/discounts",
                "Bearer "+token,
                page,itemperpage
            ), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    Log.d("javed", "onSuccessRetrofit: "+result)
                    discount_data.value=ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    Log.d("javed", "onSuccessRetrofit: "+error)
                    discount_data.value=ApiResponse.error(error)
                }
            }, context = context
        )
    }

    fun discountRecover(id:String,token:String,jsonObject: JsonObject) = CoroutineScope(Dispatchers.IO).launch {
        doRetrofitCall(
            RetrofitManager().getRetrofit().discountRecover(
                "/customer/discount/$id/recover",
                "Bearer "+token,jsonObject
            ), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    Log.d("javed", "onSuccessRetrofit: "+result)
                    recoverdiscount.value=ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    Log.d("javed", "onSuccessRetrofit: "+error)
                    recoverdiscount.value=ApiResponse.error(error)
                }
            }, context = context
        )
    }
}