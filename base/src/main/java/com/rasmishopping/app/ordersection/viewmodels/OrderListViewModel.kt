package com.rasmishopping.app.ordersection.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.rasmishopping.app.R
import com.rasmishopping.app.dbconnection.entities.CartItemData
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.GraphQLResponse
import com.rasmishopping.app.utils.Status

class OrderListViewModel(private val repository: Repository) : ViewModel() {
    var cursor = "nocursor"
        set(cursor) {
            field = cursor
            fetchOrderData()
        }
    lateinit var context: Context
    private val response = MutableLiveData<Storefront.OrderConnection>()
    val errorResponse = MutableLiveData<String>()
    fun getResponse_(): MutableLiveData<Storefront.OrderConnection> {
        fetchOrderData()
        return response
    }

    private fun fetchOrderData() {
        try {
            val tokenData = repository.accessToken[0]
            doGraphQLQueryGraph(
                repository,
                Query.getOrderList(
                    tokenData.customerAccessToken,
                    cursor,
                    Constant.internationalPricing()
                ),
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        invokes(result)
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun invokes(graphCallResult: GraphCallResult<Storefront.QueryRoot>): Unit {
        if (graphCallResult is GraphCallResult.Success<*>) {
            consumeResponse(GraphQLResponse.success(graphCallResult as GraphCallResult.Success<*>))
        } else {
            consumeResponse(GraphQLResponse.error(graphCallResult as GraphCallResult.Failure))
        }
        return Unit
    }
    fun addToCart(variantId: String, quantity: Int) {
        try {
            val runnable = Runnable {
                val data: CartItemData
                if (repository.getSingLeItem(variantId) == null ) {
                    data = CartItemData()
                    data.variant_id = variantId
                    data.qty = quantity
                    data.selling_plan_id =""
                    data.offerName=""
                    repository.addSingLeItem(data)
                }
                else{
                    data = repository.getSingLeItem(variantId)
                    val qty = data.qty + quantity
                    data.qty = qty
                    data.selling_plan_id=""
                    data.offerName=""
                    repository.updateSingLeItem(data)
                }
                Log.i("MageNative", "CartCount : " + repository.allCartItems.size)
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun consumeResponse(reponse: GraphQLResponse) {
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
                    errorResponse.setValue(errormessage.toString())
                } else {
                    try {
                        Log.i("SaifDevorders", "Cursor : " + result.data!!.customer.orders.edges.size)
                        response.setValue(result.data!!.customer.orders)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            Status.ERROR -> errorResponse.setValue(reponse.error!!.error.message)
            else -> {
            }
        }
    }
}
