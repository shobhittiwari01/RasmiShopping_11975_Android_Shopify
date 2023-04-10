package com.rasmishopping.app.yotporewards.earnrewards

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doRetrofitCall
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.ApiResponse
import com.rasmishopping.app.utils.Urls
import io.reactivex.disposables.CompositeDisposable

class EarnRewardsViewModel(private val repository: Repository) : ViewModel() {
    private val disposables = CompositeDisposable()
    lateinit var context: Context
    var earnrewards = MutableLiveData<ApiResponse>()
    var earnBirthrewards = MutableLiveData<ApiResponse>()

    fun earnRewards() {
        doRetrofitCall(
            repository.earnRewards(Urls.XGUID, Urls.X_API_KEY),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    earnrewards.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    earnrewards.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }

    fun earnBirthRewards(day:String,month:String,year:String) {
        doRetrofitCall(
            repository.earnBirthRewards(Urls.XGUID, Urls.X_API_KEY,
                MagePrefs.getCustomerEmail()
                    ?: "",day,month,year),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    earnBirthrewards.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    earnBirthrewards.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }
}