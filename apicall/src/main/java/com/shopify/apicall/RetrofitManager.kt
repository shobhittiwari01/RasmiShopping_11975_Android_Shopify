package com.shopify.apicall

import android.util.Log
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class  RetrofitManager{
    companion object {
         var apiInterface: ApiCallInterface?=null
    }
    fun getRetrofit() : ApiCallInterface{
        if(apiInterface==null) {
            var retrofit= Retrofit.Builder()
                .baseUrl("https://api.rewardify.ca/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiInterface=retrofit.create(ApiCallInterface::class.java)
        }
        return apiInterface!!
    }
}