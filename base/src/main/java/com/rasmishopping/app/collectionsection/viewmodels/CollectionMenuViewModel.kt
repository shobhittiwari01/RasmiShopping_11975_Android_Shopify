package com.rasmishopping.app.collectionsection.viewmodels
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doRetrofitCall
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.ApiResponse
import com.rasmishopping.app.utils.Urls
import io.reactivex.disposables.CompositeDisposable
class CollectionMenuViewModel(private val repository: Repository) : ViewModel() {
    private val responseLiveData = MutableLiveData<ApiResponse>()
    val message = MutableLiveData<String>()
    private val disposables = CompositeDisposable()
    var context: Context? = null
    val isLoggedIn: Boolean get() = repository.isLogin
    fun Response(): MutableLiveData<ApiResponse> {
        getMenus()
        return responseLiveData
    }
    private fun getMenus() {
        try {
            doRetrofitCall(
                repository.getMenus(Urls(MyApplication.context).mid, MagePrefs.getLanguage()!!),
                disposables,
                customResponse = object : CustomResponse {
                    override fun onSuccessRetrofit(result: JsonElement) {
                        responseLiveData.value = ApiResponse.success(result)
                    }

                    override fun onErrorRetrofit(error: Throwable) {
                        responseLiveData.value = ApiResponse.error(error)
                    }
                },
                context = context!!
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}