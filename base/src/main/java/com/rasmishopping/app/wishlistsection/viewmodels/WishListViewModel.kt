package com.rasmishopping.app.wishlistsection.viewmodels
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.shopify.graphql.support.ID
import com.rasmishopping.app.dbconnection.entities.CartItemData
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLMutateGraph
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.shopifyqueries.Mutation
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.GraphQLResponse
import com.rasmishopping.app.utils.Status
import com.rasmishopping.app.wishlistsection.models.WishListItem
import java.util.concurrent.Callable
import java.util.concurrent.Executors
class WishListViewModel(var repository: Repository) : ViewModel() {
    private val new_data = MutableLiveData<Storefront.Checkout>()
    val message = MutableLiveData<String>()
    private val changes = MutableLiveData<Boolean>()
    lateinit var context: Context
    val cartCount: Int
        get() {
            val count = intArrayOf(0)
            try {
                val executor = Executors.newSingleThreadExecutor()
                val callable = Callable {
                    if (repository.allCartItems.size > 0) {
                        count[0] = repository.allCartItems.size
                    }
                    count[0]
                }
                val future = executor.submit(callable)
                count[0] = future.get()
                executor.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return count[0]
        }
    fun getToastMessage(): MutableLiveData<String> {
        return message
    }
    val wishListCount: Int
        get() {
            val count = intArrayOf(0)
            try {
                val executor = Executors.newSingleThreadExecutor()
                val callable = Callable {
                    if (repository.wishListVariantData.size > 0) {
                        count[0] = repository.wishListVariantData.size
                    }
                    count[0]
                }
                val future = executor.submit(callable)
                count[0] = future.get()
                executor.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return count[0]
        }
    fun deleteData(variant_id: String) {
        try {
            val runnable = Runnable {
                try {
                    val data = repository.getSingleVariantData(variant_id)
                    repository.deleteSingleVariantData(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun addToCartFromWishlist(variantId: String, quantity: Int) {
        try {
            val runnable = Runnable {
                val data: CartItemData
                if (repository.getSingLeItem(variantId) == null) {
                    data = CartItemData()
                    data.variant_id = variantId
                    data.qty = quantity
                    data.selling_plan_id=""
                    data.offerName=""
                    repository.addSingLeItem(data)

                } else {
                    data = repository.getSingLeItem(variantId)
                    val qty = data.qty + quantity
                    data.qty = qty
                    repository.updateSingLeItem(data)
                }
                Log.i("MageNative", "CartCount : " + repository.allCartItems.size)
            }
            Thread(runnable).start()


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun removeFromWishlist(item: WishListItem) {
        try {
            val runnable = Runnable {
                if (repository.getSingleVariantData(item.variant_id!!) != null) {
                    val data = repository.getSingleVariantData(item.variant_id!!)
                    repository.deleteSingleVariantData(data)
                    prepareWishlist()
                }

            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun addToCart(variantId: String) {
        try {
            val runnable = Runnable {
                val data: CartItemData
                if (repository.getSingLeItem(variantId) == null) {
                    data = CartItemData()
                    data.variant_id = variantId
                    data.qty = 1
                    repository.addSingLeItem(data)
                } else {
                    data = repository.getSingLeItem(variantId)
                    val qty = data.qty + 1
                    data.qty = qty
                    repository.updateSingLeItem(data)
                }
                Log.i("MageNative", "CartCount : " + repository.allCartItems.size)
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun update(value: Boolean) {
        changes.value = value
    }
    private val lineItems: List<Storefront.CheckoutLineItemInput>
        get() {
            val checkoutLineItemInputs = ArrayList<Storefront.CheckoutLineItemInput>()
            try {
                var itemInput: Storefront.CheckoutLineItemInput? = null
                val dataList = repository.wishListVariantData
                val size = dataList.size
                for (i in 0 until size) {
                    itemInput = Storefront.CheckoutLineItemInput(
                        1,
                        ID(dataList[i].variant_id)
                    )
                    checkoutLineItemInputs.add(itemInput)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return checkoutLineItemInputs
        }

    fun WishListResponse(): MutableLiveData<Storefront.Checkout> {
        return new_data
    }
    fun prepareWishlist() {
        try {
            val runnable = object : Runnable {
                override fun run() {
                    val input = Storefront.CheckoutCreateInput()

                    input.lineItems = lineItems

                    try {
                        doGraphQLMutateGraph(
                            repository,
                            Mutation.createCheckout(input, Constant.internationalPricing(),"wishlist"),
                            customResponse = object : CustomResponse {
                                override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
                                    invoke(result)
                                }
                            },
                            context = context
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                private operator fun invoke(result: GraphCallResult<Storefront.Mutation>): Unit {
                    if (result is GraphCallResult.Success<*>) {
                        consumeResponse(GraphQLResponse.success(result as GraphCallResult.Success<*>))
                    } else {
                        consumeResponse(GraphQLResponse.error(result as GraphCallResult.Failure))
                    }
                    return Unit
                }
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun consumeResponse(response: GraphQLResponse) {
        try {
            when (response.status) {
                Status.SUCCESS -> {
                    val result =
                        (response.data as GraphCallResult.Success<Storefront.Mutation>).response
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
                        val payload = result.data!!.checkoutCreate
                        if (payload.checkoutUserErrors.size > 0) {
                            val iterator = payload.checkoutUserErrors.iterator()
                            var error: Storefront.CheckoutUserError? = null
                            while (iterator.hasNext()) {
                                error = iterator.next() as Storefront.CheckoutUserError
                                message.setValue(error.message)
                            }
                        } else {
                            val checkout = payload.checkout
                            new_data.setValue(checkout)
                        }
                    }
                }
                Status.ERROR -> message.setValue(response.error!!.error.message)
                else -> {
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
