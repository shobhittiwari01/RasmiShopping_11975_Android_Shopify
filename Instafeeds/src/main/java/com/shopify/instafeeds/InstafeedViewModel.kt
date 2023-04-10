package com.shopify.instafeeds

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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class InstafeedViewModel : ViewModel() {
    @SuppressLint("StaticFieldLeak")
    lateinit var context: Context
    lateinit var apiInterface: ApiCallInterface
    private val disposables = CompositeDisposable()
    val instafeedsdata = MutableLiveData<ApiResponse>()

    fun InstafeedResponse(
    ): MutableLiveData<ApiResponse> {
        return instafeedsdata
    }

    fun getInstafeeds(
        fields: String,
        access_token: String
    ) {
        var retrofit: Retrofit = Retrofit.Builder()//.client(provideOkHttpClient())
            .baseUrl("https://graph.instagram.com/me/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiInterface = retrofit.create(ApiCallInterface::class.java)
        doRetrofitCall(
            apiInterface.getInstafeeds(fields, access_token),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {

                    instafeedsdata.value = ApiResponse.success(result)
                }
                override fun onErrorRetrofit(error: Throwable) {
                    instafeedsdata.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }
    private fun provideOkHttpClient(): OkHttpClient? {
        //this is the part where you will see all the logs of retrofit requests
        //and responses
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient().newBuilder()
            .connectTimeout(500, TimeUnit.MILLISECONDS)
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .addInterceptor(logging)
            .build()
    }
}