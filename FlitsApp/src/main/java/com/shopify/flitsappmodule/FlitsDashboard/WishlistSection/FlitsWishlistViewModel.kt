package com.rasmishopping.app.FlitsDashboard.WishlistSection

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

class FlitsWishlistViewModel() : ViewModel() {
    lateinit var context: Context
    lateinit var  apiinterface: ApiCallInterface
    private val disposables = CompositeDisposable()
    val wishlist_data = MutableLiveData<ApiResponse>()
    val remove_wishlist_data = MutableLiveData<ApiResponse>()

    fun SendWishlistData(AppName:String,product_id:String,product_handle:String,customer_id: String,email:String,userId:String,token:String) = CoroutineScope(Dispatchers.IO).launch {
        val customer_id =  customer_id.replace("gid://shopify/Customer/", "")!!.split("?")[0]
        var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://shopifymobileapp.cedcommerce.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        apiinterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(

            apiinterface!!.SendWishlistData(AppName,
                "https://app.getflits.com/api/1/$userId/wishlist/add_to_wishlist",token,customer_id,email,
                product_id,product_handle,1



            ), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    wishlist_data.value= ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    wishlist_data.value= ApiResponse.error(error)
                }
            }, context = context
        )
    }
    fun RemoveWishlistData(AppName: String,product_id:String,product_handle:String,customer_id:String,email: String,userId: String,token: String) = CoroutineScope(Dispatchers.IO).launch {
        val customer_id =   customer_id.replace("gid://shopify/Customer/", "")!!.split("?")[0]
        var retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://shopifymobileapp.cedcommerce.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        apiinterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(

            apiinterface!!.DeleteWishlistData(AppName,
                "https://app.getflits.com/api/1/$userId/wishlist/remove_from_wishlist",token,customer_id,email,
                product_id,product_handle,1



            ), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    remove_wishlist_data.value= ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    remove_wishlist_data.value= ApiResponse.error(error)
                }
            }, context = context
        )
    }
//    fun GetSpentRules() = CoroutineScope(Dispatchers.IO).launch {
//
//        val customer_id=   MagePrefs.getCustomerID()?.replace("gid://shopify/Customer/", "")!!.split("?")[0]
//        doRetrofitCall(
//
//            repository.GetSpentRules(
//                "https://app.getflits.com/api/1/24088/$customer_id/credit/get_spent_rules","b7e0df7341529fe4a782bc9ca3931f0a",MagePrefs.getcartdata().toString()), disposables, customResponse = object : CustomResponse {
//                override fun onSuccessRetrofit(result: JsonElement) {
//                    data.postValue(ApiResponse.success(result))
//                }
//
//                override fun onErrorRetrofit(error: Throwable) {
//                    data.postValue(ApiResponse.error(error))
//                }
//            }, context = context
//        )
//    }
}