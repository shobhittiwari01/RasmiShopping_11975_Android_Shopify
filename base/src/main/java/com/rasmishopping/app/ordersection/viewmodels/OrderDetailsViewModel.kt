package com.rasmishopping.app.ordersection.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.GraphQLResponse
import io.reactivex.disposables.CompositeDisposable

class OrderDetailsViewModel(var repository: Repository) : ViewModel() {
    private val disposables = CompositeDisposable()
    val recommendedLiveData = MutableLiveData<GraphQLResponse>()
    lateinit var context: Context


    fun shopifyRecommended(productID: String) {
        getRecommendedProducts(productID)
    }

    private fun getRecommendedProducts(productID: String) {
        try {
            doGraphQLQueryGraph(repository, Query.recommendedProducts(
                productID,
                Constant.internationalPricing()
            ), customResponse = object : CustomResponse {
                override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                    invokeRecommended(result)
                }
            }, context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun invokeRecommended(result: GraphCallResult<Storefront.QueryRoot>) {
        if (result is GraphCallResult.Success<*>) {
            recommendedLiveData.setValue(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            recommendedLiveData.setValue(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
    }
}