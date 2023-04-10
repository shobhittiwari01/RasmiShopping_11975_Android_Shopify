package com.rasmishopping.app.wishlistsection.viewmodels

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.shopify.graphql.support.ID
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.dbconnection.entities.CartItemData
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLMutateGraph
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.shopifyqueries.Mutation
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.GraphQLResponse
import com.rasmishopping.app.utils.Status
import com.rasmishopping.app.utils.WishListDbResponse
import com.rasmishopping.app.wishlistsection.models.WishListItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class SubscribeWishListVIewModel(var repository: Repository) : ViewModel() {
    private val data = MutableLiveData<WishListDbResponse>()
    private val wishListData = MutableLiveData<MutableList<Storefront.Product>>()
    private val new_data = MutableLiveData<Storefront.Cart>()

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

    fun Response(): MutableLiveData<MutableList<Storefront.Product>> {
        FetchData()
        return wishListData
    }

    fun updateResponse(): MutableLiveData<Boolean> {
        return changes
    }



    private fun FetchData() {
        try {
            val runnable = Runnable {
                if (repository.wishListData.size > 0) {
                    Log.i("MageNative", "inwish")
                    Log.i("MageNative", "wish count 3 : " + repository.wishListData.size)
                    var product_ids = ArrayList<ID>()
                    val edges = mutableListOf<Storefront.Product>()
                    for (i in 0..repository.wishListData.size - 1) {
                        product_ids.add(ID(repository.wishListData[i].product_id))
                    }
                    getAllProductsById(product_ids, edges)
                } else {
                    Log.i("MageNative", "nowish")
                    GlobalScope.launch(Dispatchers.Main) {
                        message.value = "No Data in WishList"
                    }
                }
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAllProductsById(
        productIds: ArrayList<ID>,
        edges: MutableList<Storefront.Product>
    ) {
//        val directive: MutableList<Storefront.InContextDirective> = mutableListOf()
//        var inContextDirective = Storefront.InContextDirective()
//        inContextDirective.country=Storefront.CountryCode.AU
//        directive.add(inContextDirective)

        doGraphQLQueryGraph(
            repository,
            Query.getAllProductsByID(productIds, Constant.internationalPricing(),"wishlist"),
            customResponse = object : CustomResponse {
                override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                    if (result is GraphCallResult.Success<*>) {
                        consumeResponse(
                            GraphQLResponse.success(result as GraphCallResult.Success<*>),
                            edges,
                            productIds
                        )
                    } else {
                        consumeResponse(
                            GraphQLResponse.error(result as GraphCallResult.Failure),
                            edges,
                            productIds
                        )
                    }
                }
            },
            context = context
        )
    }

    private fun consumeResponse(
        reponse: GraphQLResponse,
        edges: MutableList<Storefront.Product>,
        productIds: ArrayList<ID>
    ) {
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
                    Log.i("MageNatyive", "ERROR" + errormessage.toString())
                    message.setValue(errormessage.toString())
                } else {
                    try {
                        for (i in 0..result.data!!.nodes.size - 1) {
                            edges.add(result.data!!.nodes[i] as Storefront.Product)
                        }
                        if (edges.size == productIds.size) {
                            filterProduct(edges)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        when (context.packageName) {
                            "com.rasmishopping.app" -> {
                                //   Toast.makeText(context, "Please Provide Visibility to Products and Collections", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
            Status.ERROR -> {
                Log.i("MageNatyive", "ERROR-1" + reponse.error!!.error.message)
                message.setValue(reponse.error.error.message)
            }
        }
    }

    private fun filterProduct(edges: MutableList<Storefront.Product>) {
        if (SplashViewModel.featuresModel.outOfStock!!) {
            repository.getProductListSlider(edges)
                .subscribeOn(Schedulers.io())
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result -> wishListData.value = result }
        } else {
            repository.getProductListSlider(edges)
                .subscribeOn(Schedulers.io())
                .filter { x -> x.availableForSale }
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result -> wishListData.value = result }
        }

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

    fun addToCartFromWishlist(variantId: String, quantity: Int,selling_price_id:String) {
        try {
            val runnable = Runnable {
                val data: CartItemData
                if (repository.getSingLeItem(variantId) == null) {
                    data = CartItemData()
                    data.variant_id = variantId
                    data.qty = quantity
                    data.selling_plan_id = selling_price_id
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

    private val lineItems1: List<Storefront.CartLineInput>
        get() {
            val checkoutLineItemInputs = ArrayList<Storefront.CartLineInput>()
            try {

                    var itemInput: Storefront.CartLineInput? = null
                    val dataList = repository.wishListVariantData
                    val size = dataList.size
                    for (i in 0 until size) {
                        if (dataList[i].selling_plan_id != "") {
                            itemInput = Storefront.CartLineInput(ID(dataList[i].variant_id))
//                  itemInput?.merchandiseId=ID("Z2lkOi8vc2hvcGlmeS9Qcm9kdWN0VmFyaWFudC80MjcwMDM5NDgyMzkzNw==")
                            itemInput?.quantity = 1
                            itemInput?.sellingPlanId = ID(dataList[i].selling_plan_id)

                            checkoutLineItemInputs.add(itemInput!!)
                        }
                        else
                        {
                            itemInput = Storefront.CartLineInput(ID(dataList[i].variant_id))
//                  itemInput?.merchandiseId=ID("Z2lkOi8vc2hvcGlmeS9Qcm9kdWN0VmFyaWFudC80MjcwMDM5NDgyMzkzNw==")
                            itemInput?.quantity = 1
                            checkoutLineItemInputs.add(itemInput!!)

                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return checkoutLineItemInputs
        }
    fun WishListResponse(): MutableLiveData<Storefront.Cart> {
        return new_data
    }

    fun prepareWishlist() {
        try {
            val runnable = object : Runnable {
                override fun run() {
                    val input1 = Storefront.CartInput()
                    input1.lines = lineItems1
//                    val input = Storefront.CheckoutCreateInput()
////                    input.lineItems = lineItems1
                    try {
//
//                        doGraphQLMutateGraph(
//                            repository,
//                            Mutation.createCheckout(input, Constant.internationalPricing()),
//                            customResponse = object : CustomResponse {
//                                override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
//                                    invoke(result)
//                                }
//                            },
//                            context = context
//                        )


//                        val cartlineInput= Storefront.CartLineInput(ID(""))
//                        if (isLoggedIn) {
//                            input1.buyerIdentity = Storefront.CartBuyerIdentityInput()
//                                .setCustomerAccessToken()
//                        }


//                        val directive: MutableList<Storefront.InContextDirective> = mutableListOf()
//                        var inContextDirective = Storefront.InContextDirective()
//                        inContextDirective.country= Storefront.CountryCode.US
//                        directive.add(inContextDirective)

                        doGraphQLMutateGraph(
                            repository,
                            Mutation.cartCreation(input1,Constant.internationalPricing()),
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
                    Log.d("response",""+response.data)
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
                        val payload = result.data!!.cartCreate
                        if (payload.userErrors.size > 0) {
                            val iterator = payload.userErrors.iterator()
                            var error: Storefront.UserError? = null
                            while (iterator.hasNext()) {
                                error = iterator.next() as Storefront.UserError
                                message.setValue(error.message)
                            }
                        } else {
                            val checkout = payload.cart
//                            getRecommendations(checkout)
//                            getYouMayRecommendations(checkout)
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

    private fun getProductID(id: String?): String? {
        var cat_id: String? = null
        try {
            val data =
                Base64.encode(("gid://shopify/Product/" + id!!).toByteArray(), Base64.DEFAULT)
            cat_id = String(data, Charset.defaultCharset()).trim { it <= ' ' }
            Log.i("MageNatyive", "ProductSliderID :$id " + cat_id)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return cat_id
    }
}
