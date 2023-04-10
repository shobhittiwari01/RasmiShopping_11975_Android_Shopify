package com.backinstock.app

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.shopify.apicall.ApiCallInterface
import com.shopify.apicall.ApiResponse
import com.shopify.apicall.CustomResponse
import com.shopify.apicall.doRetrofitCall
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BackInStockViewModel() : ViewModel() {
    @SuppressLint("StaticFieldLeak")
    lateinit var context: Context
    lateinit var  apiInterface: ApiCallInterface
    private val disposables = CompositeDisposable()
    val stockalert = MutableLiveData<ApiResponse>()

    fun BackInStockResponse(
        email: String,
        shop: String,
        product: String,
        variant: String
    ): MutableLiveData<ApiResponse> {
        StockAlert(
            email,shop,product,variant)
        return stockalert
    }

    fun StockAlert(email: String,shop: String,product: String,variant: String) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://shopifymobileapp.cedcommerce.com/index.php/shopifymobile/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(
            apiInterface.InStockAlert(email,shop,product,variant),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    stockalert.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    stockalert.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }
}