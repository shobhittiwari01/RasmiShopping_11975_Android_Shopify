package com.shopify.apicall
import android.content.Context
import com.google.gson.JsonElement
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

var customLoader: CustomLoader? = null
private val TAG = "ApiCall"
fun doRetrofitCall(
    postData: Single<JsonElement>,
    disposables: CompositeDisposable,
    customResponse: CustomResponse,
    context: Context
) {
    CoroutineScope(Dispatchers.Main).launch {
        //if (customLoader != null) {
//            customLoader!!.dismiss()
           // customLoader = null
        //}
  //      customLoader = CustomLoader(context)
//        customLoader!!.show()
    }
    disposables.add(postData
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { result ->
                //customLoader!!.dismiss()
                customResponse.onSuccessRetrofit(result)
            },
            { throwable ->
                //customLoader!!.dismiss()
                customResponse.onErrorRetrofit(throwable)
            }
        ))
}