package com.rasmishopping.app.yotporewards.referfriend

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

class ReferFriendViewModel(private val repository: Repository) : ViewModel() {
    private val disposables = CompositeDisposable()
    lateinit var context: Context
    var referfriend = MutableLiveData<ApiResponse>()


    fun sendReferral(emails: String) {
        doRetrofitCall(
            repository.referfriend(
                Urls.XGUID, Urls.X_API_KEY, MagePrefs.getCustomerID()
                    ?: "", emails
            ), disposables, object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    referfriend.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    referfriend.value = ApiResponse.error(error)
                }
            }, context = context
        )
    }

}