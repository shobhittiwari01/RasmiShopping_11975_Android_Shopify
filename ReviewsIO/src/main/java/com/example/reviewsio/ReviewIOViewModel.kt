package com.example.reviewsio

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.shopify.apicall.ApiCallInterface
import com.shopify.apicall.ApiResponse
import com.shopify.apicall.CustomResponse
import com.shopify.apicall.doRetrofitCall
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ReviewIOViewModel() : ViewModel() {
    @SuppressLint("StaticFieldLeak")
    lateinit var context: Context
    lateinit var  apiInterface: ApiCallInterface
    private val disposables = CompositeDisposable()
    val createIOreview = MutableLiveData<ApiResponse>()
    val fetchIOreview = MutableLiveData<ApiResponse>()

    fun ReviewIOResponse(shop: String,reviewioapikey: String,param:JsonObject): MutableLiveData<ApiResponse> {
        CreateReviewsIO(shop,reviewioapikey,param)
        return createIOreview
    }

    fun CreateReviewsIO(shop: String,reviewioapikey:String,param: JsonObject) {
        val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("https://api.reviews.io/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(
                apiInterface.CreateReviewIO(shop,reviewioapikey,param),
                disposables,
                customResponse = object : CustomResponse {
                    override fun onSuccessRetrofit(result: JsonElement) {
                        createIOreview.value = ApiResponse.success(result)
                    }

                    override fun onErrorRetrofit(error: Throwable) {
                        createIOreview.value = ApiResponse.error(error)
                    }
                },
                context = context!!
        )
    }

    fun FetchReviewIOResponse(shop: String,reviewioapikey: String,sku:String): MutableLiveData<ApiResponse> {
        FetchReviewsIO(shop,reviewioapikey,sku)
        return fetchIOreview
    }

    fun FetchReviewsIO(shop: String,reviewioapikey:String,sku:String) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.reviews.io/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiInterface=retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(
            apiInterface.GetReviewIOReviews(shop,reviewioapikey,sku),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    fetchIOreview.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    fetchIOreview.value = ApiResponse.error(error)
                }
            },
            context = context!!
        )
    }
}