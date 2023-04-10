package com.rasmishopping.app.FlitsDashboard.Profile.profile

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CustomerViewModel() : ViewModel() {
    lateinit var context: Context
    lateinit var  apiInterface: ApiCallInterface
    private val disposables = CompositeDisposable()
    val data = MutableLiveData<ApiResponse>()

    fun SaveProfileInfo(AppName:String,CustomerId:String,userId:String,token:String) = CoroutineScope(Dispatchers.IO).launch {
        val customer_id=  CustomerId.replace("gid://shopify/Customer/", "")!!.split("?")[0]
        var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://shopifymobileapp.cedcommerce.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(

            apiInterface!!.SaveProfileData(AppName,
                "https://app.getflits.com/api/1/$userId/$customer_id/profile_save",token

            ), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    data.postValue(ApiResponse.success(result))
                }

                override fun onErrorRetrofit(error: Throwable) {
                    data.postValue(ApiResponse.error(error))
                }
            }, context = context
        )
    }
    fun UpdatePassword(AppName: String,customer_id:String,Password:String,ConfirmPassword:String,userId:String,token:String) = CoroutineScope(Dispatchers.IO).launch {

        val user_customer_id=   customer_id?.replace("gid://shopify/Customer/", "")!!.split("?")[0]
        var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://shopifymobileapp.cedcommerce.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(

            apiInterface.UpdateCustomerPassword(AppName,
                "https://app.getflits.com/api/1/$userId/$user_customer_id/update_password",token

                , Password,ConfirmPassword), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    data.postValue(ApiResponse.success(result))
                }

                override fun onErrorRetrofit(error: Throwable) {
                    data.postValue(ApiResponse.error(error))
                }
            }, context = context
        )
    }
}