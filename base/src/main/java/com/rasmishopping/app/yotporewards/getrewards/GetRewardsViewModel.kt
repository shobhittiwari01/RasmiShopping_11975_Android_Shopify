package com.rasmishopping.app.yotporewards.getrewards

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

class GetRewardsViewModel(private val repository: Repository) : ViewModel() {
    private val disposables = CompositeDisposable()
    var getrewards = MutableLiveData<ApiResponse>()
    var redeemPoints = MutableLiveData<ApiResponse>()
    lateinit var context: Context

    fun getRewards() {
        doRetrofitCall(
            repository.getRewards(
                Urls.XGUID, Urls.X_API_KEY, MagePrefs.getCustomerEmail()
                    ?: "", MagePrefs.getCustomerID()
                    ?: ""
            ), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    getrewards.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    getrewards.value = ApiResponse.error(error)
                }
            }, context = context
        )
    }

    fun redeemPoints(redeemptionId: String) {
        doRetrofitCall(
            repository.redeemPoints(
                Urls.XGUID, Urls.X_API_KEY, MagePrefs.getCustomerID()
                    ?: "", MagePrefs.getCustomerEmail()
                    ?: "", redeemptionId
            ), disposables, customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    redeemPoints.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    redeemPoints.value = ApiResponse.error(error)
                }
            }, context = context
        )
    }
}