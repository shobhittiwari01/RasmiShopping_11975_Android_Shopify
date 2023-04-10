package com.rasmishopping.app.network_transaction

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.GraphCallResultCallback
import com.shopify.buy3.RetryHandler
import com.shopify.buy3.Storefront
import com.rasmishopping.app.loader_section.CustomLoader
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.utils.GraphQLResponse
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


var customLoader: CustomLoader? = null
private val TAG = "ApiCall"

fun doGraphQLMutateGraph(
    repository: Repository,
    query: Storefront.MutationQuery,
    customResponse: CustomResponse,
    context: Context
) {
//    Log.d(TAG, "doGraphQLMutateGraph: " + query)
//    GlobalScope.launch(Dispatchers.Main) {
//        if (customLoader != null) {
//            customLoader!!.dismiss()
//            customLoader = null
//        }
//        customLoader = CustomLoader(context)
//        customLoader!!.show()
//    }

    var call = repository.graphClient.mutateGraph(query)
    call.enqueue { result: GraphCallResult<Storefront.Mutation> ->
        CoroutineScope(Dispatchers.Main).launch {
            if (!(context as Activity).isDestroyed) {
//                customLoader!!.dismiss()
            }
            customResponse.onSuccessMutate(result)
        }
    }
}

fun doRetryGraphQLMutateGraph(
    repository: Repository,
    query: Storefront.MutationQuery,
    customResponse: CustomResponse,
    context: Context
) {
    var data:Boolean?=null
    var call = repository.graphClient.mutateGraph(query)
    call.enqueue(null,RetryHandler.build(200,TimeUnit.MILLISECONDS,configure = {maxAttempts(5);retryWhen { value: GraphCallResult<Storefront.Mutation> ->


        ((value as GraphCallResult.Success<Storefront.Mutation>).response.data!!.checkoutShippingAddressUpdateV2.checkout.availableShippingRates !=null)
                && ((value as GraphCallResult.Success<Storefront.Mutation>).response.data!!.checkoutShippingAddressUpdateV2.checkout.availableShippingRates.ready == false)

    }})
        ,object :GraphCallResultCallback<Storefront.Mutation>{
            override fun invoke(result: GraphCallResult<Storefront.Mutation>) {

                CoroutineScope(Dispatchers.Main).launch {
                    if (!(context as Activity).isDestroyed) {
//                customLoader!!.dismiss()
                    }
                    customResponse.onSuccessMutate(result)

                }

            }
        })
}

fun ViewModel.doGraphQLQueryGraph(
    repository: Repository,
    query: Storefront.QueryRootQuery,
    customResponse: CustomResponse,
    context: Context
) {
    Log.d(TAG, "doGraphQLQueryGraph: " + query)
    CoroutineScope(Dispatchers.Main).launch {
//        if (customLoader != null) {
//            customLoader!!.dismiss()
//            customLoader = null
//        }
//        customLoader = CustomLoader(context)
//        if (!(context as Activity).isDestroyed) {
//            customLoader!!.show()
//        }
    }
    var call = repository.graphClient.queryGraph(query)
    call.enqueue { result: GraphCallResult<Storefront.QueryRoot> ->
        CoroutineScope(Dispatchers.Main).launch {
//            customLoader!!.dismiss()
            customResponse.onSuccessQuery(result)

        }
    }
}

fun doGraphQLQueryGraph(
    repository: Repository,
    query: Storefront.QueryRootQuery,
    customResponse: CustomResponse,
    context: Context
) {
    Log.d(TAG, "doGraphQLQueryGraph: " + query)
    CoroutineScope(Dispatchers.Main).launch {
        if (customLoader != null) {
            customLoader!!.dismiss()
            customLoader = null
        }
        customLoader = CustomLoader(context)
        customLoader!!.show()
    }
    var call = repository.graphClient.queryGraph(query)
    call.enqueue { result: GraphCallResult<Storefront.QueryRoot> ->
        CoroutineScope(Dispatchers.Main).launch {
            customLoader!!.dismiss()
            customResponse.onSuccessQuery(result)

        }
    }
}

fun doRetrofitCall(
    postData: Single<JsonElement>,
    disposables: CompositeDisposable,
    customResponse: CustomResponse,
    context: Context
) {
    CoroutineScope(Dispatchers.Main).launch {
        if (customLoader != null) {
            customLoader!!.dismiss()
            customLoader = null
        }
        customLoader = CustomLoader(context)
        //customLoader!!.show()
    }
    disposables.add(postData
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            { result ->
                customLoader!!.dismiss()
                customResponse.onSuccessRetrofit(result)
            },
            { throwable ->
                customLoader!!.dismiss()
                customResponse.onErrorRetrofit(throwable)
            }
        ))
}