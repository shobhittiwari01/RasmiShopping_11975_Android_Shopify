package com.rasmishopping.app.yotporewards.myrewards

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

class MyRewardsViewModel(private val repository: Repository) : ViewModel() {
    private val disposables = CompositeDisposable()
    lateinit var context: Context
    var myrewards = MutableLiveData<ApiResponse>()

    fun getMyRewards() {
        doRetrofitCall(
            repository.myrewards(
                Urls.XGUID, Urls.X_API_KEY, MagePrefs.getCustomerEmail()
                    ?: "", MagePrefs.getCustomerID() ?: ""
            ), disposables, object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    myrewards.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    myrewards.value = ApiResponse.error(error)
                }
            }, context = context
        )
    }
}