package com.rasmishopping.app.productsection.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.GraphQLResponse
import com.rasmishopping.app.utils.Status

class FilterModel(var repository: Repository) : ViewModel() {
    val message = MutableLiveData<String>()
    private var FilterResponseLiveData = MutableLiveData<MutableList<Storefront.Filter>>()
    lateinit var context: Context

    fun FilterProductsResponse(): MutableLiveData<MutableList<Storefront.Filter>> {
        getFilteredProducts()
        return FilterResponseLiveData
    }

    private fun consumeFilterResponse(reponse: GraphQLResponse) {
        when (reponse.status) {
            Status.SUCCESS -> {
                val result =
                    (reponse.data as GraphCallResult.Success<Storefront.QueryRoot>).response
                if (result.hasErrors) {
                    val errors = result.errors
                    val iterator = errors.iterator()
                    val errormessage = StringBuilder()
                    var error: Error? = null
                    while (iterator.hasNext()) {
                        error = iterator.next()
                        errormessage.append(error.message())
                    }
                    message.setValue(errormessage.toString())
                } else {
                    FilterResponseLiveData.setValue(result.data?.collection?.products?.filters)
                }
            }
            Status.ERROR -> message.setValue(reponse.error!!.error.message)
            else -> {
            }
        }
    }

     fun getFilteredProducts() {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.filterdetails(Constant.internationalPricing()),
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        invokeFilterData(result)
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun invokeFilterData(result: GraphCallResult<Storefront.QueryRoot>): Unit {
        if (result is GraphCallResult.Success<*>) {
            consumeFilterResponse(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            consumeFilterResponse(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
        return Unit
    }
}