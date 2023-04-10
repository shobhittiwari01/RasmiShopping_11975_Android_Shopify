package com.rasmishopping.app.cartsection.viewmodels

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.shopify.graphql.support.ID
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.cartsection.models.CartListItem
import com.rasmishopping.app.dbconnection.entities.CustomerTokenData
import com.rasmishopping.app.dbconnection.entities.WishItemData
import com.rasmishopping.app.dbconnection.dependecyinjection.Body
import com.rasmishopping.app.dbconnection.dependecyinjection.InnerData
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLMutateGraph
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.network_transaction.doRetrofitCall
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.shopifyqueries.Mutation
import com.rasmishopping.app.shopifyqueries.MutationQuery
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.json.JSONObject
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class SubscribeCartListModel (private val repository: Repository) : ViewModel() {
    private val data = MutableLiveData<Storefront.Cart>()
    private val discountdata = MutableLiveData<Storefront.Cart>()
    private val giftcard = MutableLiveData<Storefront.Mutation>()
    private val giftcardRemove = MutableLiveData<Storefront.Mutation>()
    private val discount = MutableLiveData<Storefront.Mutation>()
    private val api = MutableLiveData<ApiResponse>()
    private var dataAtt:MutableLiveData<Storefront.Cart>?=null
    private val youmayapi = MutableLiveData<ApiResponse>()
    private val disposables = CompositeDisposable()
    var product_id=""
    private val validate_delivery = MutableLiveData<ApiResponse>()
    val wholesale_data = MutableLiveData<ApiResponse>()
    private val local_delivery = MutableLiveData<ApiResponse>()
    private val delivery_status = MutableLiveData<ApiResponse>()
    private val store_delivery = MutableLiveData<ApiResponse>()
    lateinit var context: Context
    val recommendedLiveData = MutableLiveData<GraphQLResponse>()
    private val TAG = "CartListViewModel"
    private val responsedata = MutableLiveData<Storefront.Checkout>()
    var getdiscountcodeapplyapi:MutableLiveData<ApiResponse>?=null
    var customeraccessToken: CustomerTokenData
        get() {
            var customerToken = runBlocking(Dispatchers.IO) {
                return@runBlocking repository.accessToken[0]
            }
            return customerToken
        }
        set(value) {}
    val message = MutableLiveData<String>()
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
//    private val lineItems: List<Storefront.CheckoutLineItemInput>
//        get() {
//            val checkoutLineItemInputs = ArrayList<Storefront.CheckoutLineItemInput>()
//            try {
//                var itemInput: Storefront.CheckoutLineItemInput? = null
//                val dataList = repository.allCartItems
//                val size = dataList.size
//                for (i in 0 until size) {
//                    itemInput = Storefront.CheckoutLineItemInput(
//                        dataList[i].qty,
//                        ID(dataList[i].variant_id)
//                    )
//                    checkoutLineItemInputs.add(itemInput)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//            return checkoutLineItemInputs
//        }

    private val lineItems1: List<Storefront.CartLineInput>
        get() {
            val checkoutLineItemInputs = ArrayList<Storefront.CartLineInput>()
            try {

                var itemInput: Storefront.CartLineInput? = null
                val dataList = repository.allCartItems
                val size = dataList.size



                    for (i in 0 until size) {
                        if (dataList[i].selling_plan_id != "") {
                            itemInput = Storefront.CartLineInput(ID(dataList[i].variant_id))
//                  itemInput?.merchandiseId=ID("Z2lkOi8vc2hvcGlmeS9Qcm9kdWN0VmFyaWFudC80MjcwMDM5NDgyMzkzNw==")
                            itemInput?.quantity = dataList[i].qty
                            itemInput?.sellingPlanId = ID(dataList[i].selling_plan_id)

                            checkoutLineItemInputs.add(itemInput!!)

                        } else {
                            itemInput = Storefront.CartLineInput(ID(dataList[i].variant_id))
//                  itemInput?.merchandiseId=ID("Z2lkOi8vc2hvcGlmeS9Qcm9kdWN0VmFyaWFudC80MjcwMDM5NDgyMzkzNw==")
                            itemInput?.quantity = dataList[i].qty
                            checkoutLineItemInputs.add(itemInput!!)

                        }
                        Log.d("hit", "hit")

                    }


            } catch (e: Exception) {
                e.printStackTrace()
            }

            return checkoutLineItemInputs
        }
    fun getWholesalepricedata(authorization:String,apikey:String,params: JsonObject): MutableLiveData<ApiResponse> {
        doRetrofitCall(
            repository.WholeSalePriceData(authorization,apikey,params),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    wholesale_data.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    wholesale_data.value = ApiResponse.error(error)
                }
            },
            context = context
        )
        return wholesale_data
    }

    val wishListcount: Int
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
    val isLoggedIn: Boolean
        get() {
            var loggedin = runBlocking(Dispatchers.IO) {
                return@runBlocking repository.isLogin
            }
            return loggedin
        }

    fun Response(): MutableLiveData<Storefront.Cart> {
        return data
    }

    fun getGiftCard(): MutableLiveData<Storefront.Mutation> {
        return giftcard
    }

    fun getDiscount(): MutableLiveData<Storefront.Mutation> {
        return discount
    }
    fun getDiscountedData(): MutableLiveData<Storefront.Cart> {
        return discountdata
    }

    fun ResponseAtt(): MutableLiveData<Storefront.Cart> {
        dataAtt=MutableLiveData<Storefront.Cart>()
        return dataAtt!!
    }

    fun getGiftCardRemove(): MutableLiveData<Storefront.Mutation> {
        return giftcardRemove
    }

    fun getApiResponse(): MutableLiveData<ApiResponse> {
        return api
    }

    fun getYouMAyAPiResponse(): MutableLiveData<ApiResponse> {
        return youmayapi
    }

    fun getassociatecheckoutResponse(): MutableLiveData<Storefront.Checkout> {
        return responsedata
    }
    fun shopifyRecommended() {
        if (SplashViewModel.featuresModel.recommendedProducts) {
//            prepareCart()
//            getRecommendedProducts()
        }
    }
    private fun getRecommendedProducts() {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.recommendedProducts(product_id, Constant.internationalPricing()),
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        invokeRecommended(result)
                    }
                },
                context = context
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

    fun associatecheckout(checkoutId: ID?, customerAccessToken: String?) {
        try {
            doGraphQLMutateGraph(
                repository,
                MutationQuery.checkoutCustomerAssociateV2(
                    checkoutId,
                    customerAccessToken,
                    Constant.internationalPricing()
                ),
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

    private fun invoke(graphCallResult: GraphCallResult<Storefront.Mutation>) {
        if (graphCallResult is GraphCallResult.Success<*>) {
            consumeResponseassociate(GraphQLResponse.success(graphCallResult as GraphCallResult.Success<*>))
        } else {
            consumeResponseassociate(GraphQLResponse.error(graphCallResult as GraphCallResult.Failure))
        }
        return Unit
    }

    private fun consumeResponseassociate(response: GraphQLResponse) {
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
                    /*this.errormessage.setValue(errormessage.toString())*/
                } else {
                    /* val payload = result.data!!.checkoutCreate
                     if (payload.checkoutUserErrors.size > 0) {
                         val iterator = payload.checkoutUserErrors.iterator()
                         var error: Storefront.CheckoutUserError? = null
                         while (iterator.hasNext()) {
                             error = iterator.next() as Storefront.CheckoutUserError
                             message.setValue(error.message)
                         }
                         *//*errormessage.setValue(err)*//*
                    } else {*/
                    responsedata.setValue(
                        result.data!!.checkoutCustomerAssociateV2.checkout
                    )
                    /*}*/
                }
            }
            /*Status.ERROR -> errormessage.setValue(response.error!!.error.message)*/
            else -> {
            }
        }
    }

    fun prepareCart() {
        try {
            val runnable = object : Runnable {
                override fun run() {
                    val input1 = Storefront.CartInput()
                    input1.lines = lineItems1
                    try {

                        if (isLoggedIn) {
                            input1.buyerIdentity = Storefront.CartBuyerIdentityInput()
                                .setCountryCode(Constant.internationalPricing()[0].country)
                                .setEmail(MagePrefs.getCustomerEmail())

                        }
                        else{
                            input1.buyerIdentity = Storefront.CartBuyerIdentityInput()
                                .setCountryCode(Constant.internationalPricing()[0].country)

                        }

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

    fun prepareCartwithDiscount(discount_code: MutableList<String>) {
        try {
            val runnable = object : Runnable {
                override fun run() {

                    val input1 = Storefront.CartInput()
                    input1.lines = lineItems1
                    input1.discountCodes=discount_code

                    try {

                        if (isLoggedIn) {
                            input1.buyerIdentity = Storefront.CartBuyerIdentityInput()
                                .setCountryCode(Constant.internationalPricing()[0].country)
                                .setEmail(MagePrefs.getCustomerEmail())

                        }
                        else{
                            input1.buyerIdentity = Storefront.CartBuyerIdentityInput()
                                .setCountryCode(Constant.internationalPricing()[0].country)

                        }

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
                        consumeDiscountResponse(GraphQLResponse.success(result as GraphCallResult.Success<*>))
                    } else {
                        consumeDiscountResponse(GraphQLResponse.error(result as GraphCallResult.Failure))
                    }
                    return Unit
                }
            }
            Thread(runnable).start()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun moveToWishList(item: CartListItem) {
        try {
            val runnable = Runnable {
                if (repository.getSingleVariantData(item.variant_id!!) == null) {
                    val data = WishItemData()
                    data.variant_id = item.variant_id!!
                    data.selling_plan_id = item.selling_price_id!!

                    repository.insertWishListVariantData(data)
                    Log.i("MageNative", "WishListCount : " + repository.wishListData.size)
                }
                removeFromCart(item)
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun removeFromCart(item: CartListItem) {
        try {
            runBlocking(Dispatchers.IO) {

                val data = repository.getSingLeItem(item.variant_id!!)
                repository.deleteSingLeItem(data)

                prepareCart()


            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun updateCart(item: CartListItem) {
        try {
            val runnable = Runnable {
                val data = repository.getSingLeItem(item.variant_id!!)
                data.qty = Integer.parseInt(item.qty!!)
                repository.updateSingLeItem(data)
                prepareCart()
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun consumeResponseAtt(response: GraphQLResponse) {
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
                            dataAtt?.setValue(checkout)
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
    private fun consumeDiscountResponse(response: GraphQLResponse) {
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
                        var variant= result.data!!.cartCreate.cart.lines.edges.get(0).node.merchandise as Storefront.ProductVariant
                        product_id= variant.product.id.toString()
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
                        //    getRecommendedProducts()
//                            getYouMayRecommendations(checkout)
                            discountdata.setValue(checkout)
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
                        if(result.data!!.cartCreate.cart.lines.edges.size>0)
                        {
                            var variant= result.data!!.cartCreate.cart.lines.edges.get(0).node.merchandise as Storefront.ProductVariant
                            product_id= variant.product.id.toString()
                        }


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
                          getRecommendedProducts()
//                            getYouMayRecommendations(checkout)
                            data.setValue(checkout)
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

    fun getYouMayRecommendations(checkout: Storefront.Checkout) {
        RetrofitUrlManager.getInstance().putDomain("douban", Urls.PERSONALISED)
        try {
            var query = InnerData()
            query.id = "query1"
            query.maxRecommendations = 8
            query.recommendationType = "cross_sell"
            var list = mutableListOf<Long>()
            for (i in 0..checkout.lineItems.edges.size - 1) {
                var s =
                    checkout.lineItems.edges.get(i).node.variant.product.id.toString()

                list.add(s.replace("gid://shopify/Product/", "").toLong())
            }
            query.productIds = list
            var body = Body()
            body.queries = mutableListOf(query)
            Log.i("Body", "" + list)
            disposables.add(repository.getRecommendation(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result -> youmayapi.setValue(ApiResponse.success(result)) },
                    { throwable -> youmayapi.setValue(ApiResponse.error(throwable)) }
                ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getRecommendations(checkout: Storefront.Checkout) {
        RetrofitUrlManager.getInstance().putDomain("douban", Urls.PERSONALISED)
        try {
            var query = InnerData()
            query.id = "query1"
            query.maxRecommendations = 8
            query.recommendationType = "bought_together"
            var list = mutableListOf<Long>()
            for (i in 0..checkout.lineItems.edges.size - 1) {
                var s =
                        checkout.lineItems.edges.get(i).node.variant.product.id.toString()

                list.add(s.replace("gid://shopify/Product/", "").toLong())
            }
            query.productIds = list
            var body = Body()
            body.queries = mutableListOf(query)
            Log.i("Body", "" + list)
            disposables.add(repository.getRecommendation(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result -> api.setValue(ApiResponse.success(result)) },
                    { throwable -> api.setValue(ApiResponse.error(throwable)) }
                ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun clearCartData() {
        try {
            val runnable = Runnable {
                repository.deletecart()
                prepareCart()
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun applyGiftCard(gift_card: String, checkoutId: ID?) {
        var list = ArrayList<String>()
        list.add(gift_card)
        doGraphQLMutateGraph(
            repository,
            MutationQuery.checkoutGiftCardsAppend(
                checkoutId,
                list,
                Constant.internationalPricing()
            ),
            customResponse = object : CustomResponse {
                override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
                    invokeGift(result)
                }
            },
            context = context
        )

    }

    fun doGooglePay(
        checkoutId: ID?,
        totalPrice: String,
        idempotencyKey: String,
        billingAddressInput: Storefront.MailingAddressInput
    ) {
        /*var paymentData = JSONObject()
        paymentData.put("type", "google_pay")

        val input = Storefront.TokenizedPaymentInputV3(
            Storefront.MoneyInput(totalPrice, Storefront.CurrencyCode.valueOf(presentCurrency)),
            idempotencyKey,
            billingAddressInput,
            paymentData.toString(),
            Storefront.PaymentTokenType.GOOGLE_PAY
        )
        //input.test = true
        doGraphQLMutateGraph(
            repository,
            Mutation.checkoutWithGpay(checkoutId!!, input, Constant.internationalPricing()),
            customResponse = object : CustomResponse {
                override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
                    invokeGooglePay(result)
                }
            },
            context = context
        )*/
    }

    private fun invokeGooglePay(result: GraphCallResult<Storefront.Mutation>) {
        if (result is GraphCallResult.Success<*>) {
            consumeResponseGooglePay(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            consumeResponseGooglePay(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
        return Unit
    }

    private fun consumeResponseGooglePay(response: GraphQLResponse) {
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
                        val payload = result.data!!.checkoutCompleteWithTokenizedPaymentV3
                        doGraphQLQueryGraph(
                            repository,
                            Query.pollCheckoutCompletion(
                                payload.checkout.id,
                                Constant.internationalPricing()
                            ),
                            customResponse = object : CustomResponse {
                                override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                                    invokePollCompletion(result)
                                }
                            }, context = context
                        )
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

    private fun invokePollCompletion(result: GraphCallResult<Storefront.QueryRoot>) {
        if (result is GraphCallResult.Success<*>) {
            consumeResponsePollCompletion(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            consumeResponsePollCompletion(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
    }

    private fun consumeResponsePollCompletion(reponse: GraphQLResponse) {
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
                    val payment = result.data?.node as Storefront.Checkout

//                    if (payment.errorMessage == null || payment.errorMessage.isEmpty()) {
//                        val checkout = payment.checkout
//                        val orderId = checkout.order.id.toString()
//                    } else {
//                        val errorMessage = payment.errorMessage
//                    }
                }
            }
            Status.ERROR -> message.setValue(reponse.error!!.error.message)
            else -> {
            }
        }
    }


    fun invokeGift(result: GraphCallResult<Storefront.Mutation>): Unit {
        if (result is GraphCallResult.Success<*>) {
            consumeResponseGift(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            consumeResponseGift(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
        return Unit
    }

    private fun consumeResponseGift(response: GraphQLResponse) {
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
                } else {
                    val payload = result.data!!.checkoutGiftCardsAppend
                    if (payload.checkoutUserErrors.size > 0) {
                        val iterator = payload.checkoutUserErrors.iterator()
                        var error: Storefront.CheckoutUserError? = null
                        while (iterator.hasNext()) {
                            error = iterator.next() as Storefront.CheckoutUserError
                            message.setValue(error.message)
                        }
                    } else {
                        giftcard.setValue(result.data!!)

                    }
                }
            }
            /*Status.ERROR -> errormessage.setValue(response.error!!.error.message)*/
            else -> {
            }
        }
    }

    fun validateDelivery(param: HashMap<String, String>): MutableLiveData<ApiResponse> {
        disposables.add(repository.validateDelivery(param)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> validate_delivery.setValue(ApiResponse.success(result)) },
                { throwable -> validate_delivery.setValue(ApiResponse.error(throwable)) }
            ))
        return validate_delivery
    }

    fun localDelivery(param: HashMap<String, String>): MutableLiveData<ApiResponse> {
        disposables.add(repository.localDelivery(param)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> local_delivery.setValue(ApiResponse.success(result)) },
                { throwable -> local_delivery.setValue(ApiResponse.error(throwable)) }
            ))
        return local_delivery
    }

    fun localDeliveryy(param: HashMap<String, String>): MutableLiveData<ApiResponse> {
        disposables.add(repository.localDeliveryy(param)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> local_delivery.setValue(ApiResponse.success(result)) },
                { throwable -> local_delivery.setValue(ApiResponse.error(throwable)) }
            ))
        return local_delivery
    }

    fun DeliveryStatus(mid: String): MutableLiveData<ApiResponse> {
        disposables.add(repository.DeliveryStatus(mid)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> delivery_status.setValue(ApiResponse.success(result)) },
                { throwable -> delivery_status.setValue(ApiResponse.error(throwable)) }
            ))
        return delivery_status
    }


    fun storeDelivery(param: HashMap<String, String>): MutableLiveData<ApiResponse> {
        disposables.add(repository.storeDelivery(param)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> store_delivery.setValue(ApiResponse.success(result)) },
                { throwable -> store_delivery.setValue(ApiResponse.error(throwable)) }
            ))
        return store_delivery
    }

    fun removeGiftCard(giftcardID: ID?, checkoutId: ID?) {
        doGraphQLMutateGraph(
            repository,
            MutationQuery.checkoutGiftCardsRemove(
                giftcardID,
                checkoutId,
                Constant.internationalPricing()
            ),
            customResponse = object : CustomResponse {
                override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
                    invokeGiftRemove(result)
                }
            },
            context = context
        )
    }

    private fun invokeGiftRemove(result: GraphCallResult<Storefront.Mutation>) {
        if (result is GraphCallResult.Success<*>) {
            consumeResponseGiftRemove(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            consumeResponseGiftRemove(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
    }

    private fun consumeResponseGiftRemove(response: GraphQLResponse) {
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
                } else {
                    val payload = result.data!!.checkoutGiftCardRemoveV2
                    if (payload.checkoutUserErrors.size > 0) {
                        val iterator = payload.checkoutUserErrors.iterator()
                        var error: Storefront.CheckoutUserError? = null
                        while (iterator.hasNext()) {
                            error = iterator.next() as Storefront.CheckoutUserError
                            message.setValue(error.message)
                        }
                    } else {
                        giftcardRemove.setValue(result.data!!)

                    }
                }
            }
            /*Status.ERROR -> errormessage.setValue(response.error!!.error.message)*/
            else -> {
            }
        }
    }

    fun applyDiscount(checkoutId: ID?, discount_code: String) {
        doGraphQLMutateGraph(
            repository,
            MutationQuery.checkoutDiscountCodeApply(
                checkoutId,
                discount_code,
                Constant.internationalPricing()
            ),
            customResponse = object : CustomResponse {
                override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
                    invokeDiscount(result)
                }
            },
            context = context
        )
    }

    private fun invokeDiscount(result: GraphCallResult<Storefront.Mutation>) {
        if (result is GraphCallResult.Success<*>) {
            consumeResponseDiscount(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            consumeResponseDiscount(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
    }

    private fun consumeResponseDiscount(response: GraphQLResponse) {
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
                } else {
                    val payload = result.data!!.checkoutDiscountCodeApplyV2
                    if (payload.userErrors.size > 0) {
                        val iterator = payload.userErrors.iterator()
                        var error: Storefront.UserError? = null
                        while (iterator.hasNext()) {
                            error = iterator.next() as Storefront.UserError
                            message.setValue(error.message)
                        }
                    } else {
                        discount.setValue(result.data!!)
                    }
                }
            }
            /*Status.ERROR -> errormessage.setValue(response.error!!.error.message)*/
            else -> {
            }
        }
    }

    fun NResponse(mid: String, customer_code: String): MutableLiveData<ApiResponse> {
        discountcodeapplyapi(mid, customer_code)
        getdiscountcodeapplyapi=MutableLiveData<ApiResponse>()
        return getdiscountcodeapplyapi!!
    }

    fun discountcodeapplyapi(mid: String, customer_code: String) {
        doRetrofitCall(
            repository.discountcodeapply(mid, customer_code),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    getdiscountcodeapplyapi?.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    getdiscountcodeapplyapi?.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }

    fun fillDeliveryParam(edges: List<Storefront.CartLineEdge>): HashMap<String, String> {
        var param = HashMap<String, String>()
        for (i in 0..edges.size - 1) {
             var merchant=edges.get(i).node.merchandise as Storefront.ProductVariant
            param.put(
                "cart[$i][product_id]",
                        edges[i].node.id.toString().replace("gid://shopify/CheckoutLineItem/", "").split("?")[0]
            )
            param.put(
                "cart[$i][variant_id]",
                        merchant.id.toString().replace("gid://shopify/ProductVariant/", "")
            )
            param.put("cart[$i][quantity]", edges[i].node.quantity.toString())
            Log.d(
                TAG,
                "product_id: " +
                        edges[i].node.id.toString().replace("gid://shopify/CheckoutLineItem/", "").split("?")[0]
            )
            Log.d(
                TAG,
                "variant_id: " +
                        merchant.id.toString().replace("gid://shopify/ProductVariant/", "")
            )
        }
        param.put("shop", Urls(MyApplication.context).shopdomain)
        param.put("type", "pickup")
        return param
    }

    fun fillLocalDeliveryParam(
        edges: List<Storefront.CartLineEdge>,
        zipcodes: EditText
    ): HashMap<String, String> {
        var param = HashMap<String, String>()
        for (i in 0..edges.size - 1) {
            var merchant=edges.get(i).node.merchandise as Storefront.ProductVariant
            param.put(
                "cart[$i][product_id]",
                        edges[i].node.id.toString().replace("gid://shopify/CheckoutLineItem/", "").split("?")[0]
            )
            param.put(
                "cart[$i][variant_id]",
                        merchant.id.toString().replace("gid://shopify/ProductVariant/", "")
            )
            param.put("cart[$i][quantity]", edges[i].node.quantity.toString())
            Log.d(
                TAG,
                "product_id: " +
                        edges[i].node.id.toString().replace("gid://shopify/CheckoutLineItem/", "").split("?")[0]
            )
            Log.d(
                TAG,
                "variant_id: " +
                        merchant.id.toString().replace("gid://shopify/ProductVariant/", "")
            )
        }
        param.put("shop", Urls(MyApplication.context).shopdomain)
        param.put("type", "delivery")
        param.put("zipcode", zipcodes.text.toString())
        return param
    }

    fun fillStoreDeliveryParam(
        edges: List<Storefront.CartLineEdge>,
        zipcodes: EditText
    ): HashMap<String, String> {
        var param = HashMap<String, String>()
        for (i in 0..edges.size - 1) {
            var merchant=edges.get(i).node.merchandise as Storefront.ProductVariant
            param.put(
                "cart[$i][product_id]",
                        edges[i].node.id.toString().replace("gid://shopify/CheckoutLineItem/", "").split("?")[0]
            )
            param.put(
                "cart[$i][variant_id]",

                        merchant.id.toString().replace("gid://shopify/ProductVariant/", "")
            )
            param.put("cart[$i][quantity]", edges[i].node.quantity.toString())
            Log.d(
                TAG,
                "product_id: " +
                        edges[i].node.id.toString().replace("gid://shopify/CheckoutLineItem/", "").split("?")[0]
            )
            Log.d(
                TAG,
                "variant_id: " +
                        merchant.id.toString().replace("gid://shopify/ProductVariant/", "")
            )
        }
        param.put("shop", Urls(MyApplication.context).shopdomain)
        param.put("type", "pickup")
        param.put("zipcode", zipcodes.text.toString())
        return param
    }

    fun prepareCartwithAttribute(
        attributeInputs: MutableList<Storefront.AttributeInput>,
        order_note: String
    ) {
        try {
            val runnable = object : Runnable {
                override fun run() {
                    val input = Storefront.CartInput()
                    input.attributes = attributeInputs
                    input.lines = lineItems1
                    try {
//                        val directive: MutableList<Storefront.InContextDirective> = mutableListOf()
//                        var inContextDirective = Storefront.InContextDirective()
//                        inContextDirective.country= Storefront.CountryCode.IN
//                        directive.add(inContextDirective)
                        input.note = order_note
                        val call = repository.graphClient.mutateGraph(
                            Mutation.cartCreation(
                                input,
                                Constant.internationalPricing()
                            )
                        )
                        call.enqueue(Handler(Looper.getMainLooper())) { result: GraphCallResult<Storefront.Mutation> ->
                            this.invoke(
                                result
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                private operator fun invoke(result: GraphCallResult<Storefront.Mutation>): Unit {
                    if (result is GraphCallResult.Success<*>) {
                        consumeResponseAtt(GraphQLResponse.success(result as GraphCallResult.Success<*>))
                    } else {
                        consumeResponseAtt(GraphQLResponse.error(result as GraphCallResult.Failure))
                    }
                    return Unit
                }
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
