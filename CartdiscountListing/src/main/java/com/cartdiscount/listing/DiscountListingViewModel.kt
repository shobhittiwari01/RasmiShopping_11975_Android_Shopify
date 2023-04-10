package com.cartdiscount.listing
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
class DiscountListingViewModel: ViewModel() {
    lateinit var context: Context
    lateinit var  apiInterface: ApiCallInterface
    private val disposables = CompositeDisposable()
    val fetchdiscountlisting = MutableLiveData<ApiResponse>()
    var url:String="https://devshop.magenative.com/index.php/shopifymobilenew/"
    fun FetchDiscountlistResponse(mid: String): MutableLiveData<ApiResponse> {
        FetchDiscountList(mid)
        return fetchdiscountlisting
    }
    fun FetchDiscountList(mid: String) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(
            apiInterface.GetDiscountListing(mid),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    fetchdiscountlisting.value = ApiResponse.success(result)
                }
                override fun onErrorRetrofit(error: Throwable) {
                    fetchdiscountlisting.value = ApiResponse.error(error)
                }
            },
            context = context!!
        )
    }
}