package com.rasmishopping.app.checkoutsection.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.shopify.buy3.Storefront
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.dbconnection.entities.CustomerTokenData
import com.rasmishopping.app.dbconnection.entities.UserLocalData
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.utils.ApiResponse
import com.rasmishopping.app.utils.Urls
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class CheckoutWebLinkViewModel(private val repository: Repository) : ViewModel() {

    private val disposables = CompositeDisposable()
     val responseLiveData = MutableLiveData<ApiResponse>()
    private val ordertagLiveData = MutableLiveData<ApiResponse>()
    lateinit var context: Context
    var customeraccessToken: CustomerTokenData
        get() {
            val customerToken = runBlocking(Dispatchers.IO) {
                return@runBlocking repository.accessToken[0]
            }
            return customerToken
        }
        set(value) {}
    val isLoggedIn: Boolean
        get() {
            val loggedin = runBlocking(Dispatchers.IO) {
                return@runBlocking repository.isLogin
            }
            return loggedin
        }
    val data: UserLocalData?
        get() {
            val user = arrayOf<UserLocalData>()
            try {
                val executor = Executors.newSingleThreadExecutor()
                val callable = Callable {
                    user[0] = repository.allUserData[0]
                    user[0]
                }
                val future = executor.submit(callable)
                user[0] = future.get()
                executor.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return user[0]
        }

    fun setOrder(mid: String, checkout_token: String?) {
        try {
            val postData = repository.setOrder(mid, checkout_token)


            disposables.add(postData
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        responseLiveData.value = ApiResponse.success(result)
                        Log.d("newwwdata",""+result)
                    //OrderTags(Urls((this.getApplication() as MyApplication)).mid,(result as JsonObject).getAsJsonObject("data").get("id").toString(),"Mobile App Order")
                    },
                    { throwable ->
                        responseLiveData.value = ApiResponse.error(throwable)
                    }
                ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun OrderTags(mid: String, id: String?,tags:String) {
        try {
            val postData = repository.OrderTags(mid, id,tags)
//            doRetrofitCall(postData, disposables, customResponse = object : CustomResponse {
//                override fun onSuccessRetrofit(result: JsonElement) {
//                    responseLiveData.setValue(ApiResponse.success(result))
//                }
//
//                override fun onErrorRetrofit(error: Throwable) {
//                    responseLiveData.setValue(ApiResponse.error(error))
//                }
//            }, context = context)

            disposables.add(postData
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        ordertagLiveData.value = ApiResponse.success(result)
                    },
                    { throwable ->
                        ordertagLiveData.value = ApiResponse.error(throwable)
                    }
                ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun deleteCart() {
        try {
            val runnable = Runnable { repository.deletecart() }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCleared() {
        disposables.clear()
    }
}
