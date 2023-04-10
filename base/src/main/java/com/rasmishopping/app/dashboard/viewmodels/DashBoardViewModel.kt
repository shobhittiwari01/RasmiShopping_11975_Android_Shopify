package com.rasmishopping.app.dashboard.viewmodels
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doRetrofitCall
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.utils.ApiResponse
import com.rasmishopping.app.utils.Urls
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashBoardViewModel(val repository: Repository) : ViewModel() {
    private val disposables = CompositeDisposable()
    private var deleteresponse=MutableLiveData<ApiResponse>()
    fun getResponse() :MutableLiveData<ApiResponse>{
        return deleteresponse
    }
    fun deleteAccount(customer_id:String,context:Context) {
        doRetrofitCall(
            repository.deleteUserData( Urls(MyApplication.context).mid, customer_id),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    CoroutineScope(Dispatchers.Main).launch {
                        deleteresponse.value = ApiResponse.success(result)
                    }
                }
                override fun onErrorRetrofit(error: Throwable) {
                    CoroutineScope(Dispatchers.Main).launch {
                        deleteresponse.value = ApiResponse.error(error)
                    }
                }
            },
            context = context
        )
    }
}