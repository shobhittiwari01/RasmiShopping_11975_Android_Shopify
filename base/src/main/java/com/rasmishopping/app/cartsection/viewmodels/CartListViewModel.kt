package com.rasmishopping.app.cartsection.viewmodels
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.cartsection.models.CartListItem
import com.rasmishopping.app.dbconnection.entities.CustomerTokenData
import com.rasmishopping.app.dbconnection.entities.WishItemData
import com.rasmishopping.app.dbconnection.dependecyinjection.Body
import com.rasmishopping.app.dbconnection.dependecyinjection.InnerData
import com.rasmishopping.app.network_transaction.*
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.shopifyqueries.Mutation
import com.rasmishopping.app.shopifyqueries.MutationQuery
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.json.JSONObject
import java.lang.Runnable
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.Executors
class CartListViewModel(private val repository: Repository) : ViewModel() {
    private val data = MutableLiveData<Storefront.Checkout>()
    private val shipdata = MutableLiveData<Storefront.Checkout>()
    private val Addressdata = MutableLiveData<Storefront.Checkout>()
    private val ShippingLinedata = MutableLiveData<Storefront.CheckoutShippingLineUpdatePayload>()
    private val Checkoutdata = MutableLiveData<Storefront.CheckoutCompleteWithCreditCardV2Payload>()
    private val giftcard = MutableLiveData<Storefront.Mutation>()
    private val giftcardRemove = MutableLiveData<Storefront.Mutation>()
    private var discount:MutableLiveData<Storefront.Mutation>?=null
    private var discounterror:MutableLiveData<Boolean>?=null
    var product_id=""
    private var zapiatId: String = ""
    private val api = MutableLiveData<ApiResponse>()
    private var dataAtt: MutableLiveData<Storefront.Checkout>? =null
    private val youmayapi = MutableLiveData<ApiResponse>()
    private val disposables = CompositeDisposable()
    val recommendedLiveData = MutableLiveData<GraphQLResponse>()
    private val validate_delivery = MutableLiveData<ApiResponse>()
    private val local_delivery = MutableLiveData<ApiResponse>()
    private val delivery_status = MutableLiveData<ApiResponse>()
    val wholesale_data = MutableLiveData<ApiResponse>()
    val wholesale_discount_coupon= MutableLiveData<ApiResponse>()
    private val store_delivery = MutableLiveData<ApiResponse>()
    lateinit var context: Context
    private val removediscount = MutableLiveData<Storefront.Mutation>()
    private val TAG = "CartListViewModel"
    private var responsedata :MutableLiveData<Storefront.Checkout>?=null
    var getdiscountcodeapplyapi=MutableLiveData<ApiResponse>()
    var customeraccessToken: CustomerTokenData
        get() {
            var customerToken = runBlocking(Dispatchers.IO) {
                return@runBlocking repository.accessToken[0]
            }
            return customerToken
        }
        set(value) {}
    val message = MutableLiveData<String>()
    val firstsale = MutableLiveData<Boolean>()
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
    lateinit var lineitemsArr:List<Storefront.CheckoutLineItemInput>
    fun setZepiatID(zapiat_id: String) {
                this.zapiatId = zapiat_id
            }
    private val lineItems: List<Storefront.CheckoutLineItemInput>
        get() {
            val checkoutLineItemInputs = ArrayList<Storefront.CheckoutLineItemInput>()
            try {
                var itemInput: Storefront.CheckoutLineItemInput? = null
                val dataList = repository.allCartItems
                val size = dataList.size
                for (i in 0 until size) {
                    itemInput = Storefront.CheckoutLineItemInput(dataList[i].qty, ID(dataList[i].variant_id))
                    .setCustomAttributes(listOf(Storefront.AttributeInput("_ZapietId",zapiatId)))

                    checkoutLineItemInputs.add(itemInput)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return checkoutLineItemInputs
        }
    fun removeDiscount(): MutableLiveData<Storefront.Mutation> {
        return removediscount
    }

        private val lineItems1: List<Storefront.CartLineInput>
        get() {
            val checkoutLineItemInputs = ArrayList<Storefront.CartLineInput>()
            try {
                var itemInput: Storefront.CartLineInput? = null
                val dataList = repository.allCartItems
                val size = dataList.size
                for (i in 0 until size) {
                    itemInput = Storefront.CartLineInput(
                        ID(dataList[i].variant_id)
                    )
                    itemInput.quantity = dataList[i].qty
                    checkoutLineItemInputs.add(itemInput)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return checkoutLineItemInputs
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

    fun Response(): MutableLiveData<Storefront.Checkout> {
        return data
    }

    fun ShipResponse(): MutableLiveData<Storefront.Checkout> {
        return shipdata
    }

    fun AddressResponse(): MutableLiveData<Storefront.Checkout> {
        return Addressdata
    }

    fun ShippingLineResponse(): MutableLiveData<Storefront.CheckoutShippingLineUpdatePayload> {
        return ShippingLinedata
    }

    fun checkoutResponse(): MutableLiveData<Storefront.CheckoutCompleteWithCreditCardV2Payload> {
        return Checkoutdata
    }

    fun getGiftCard(): MutableLiveData<Storefront.Mutation> {
        return giftcard
    }

    fun getDiscount(): MutableLiveData<Storefront.Mutation> {
        discount=MutableLiveData<Storefront.Mutation>()
        return discount!!
    }
    fun getDiscountError(): MutableLiveData<Boolean> {
        discounterror=MutableLiveData<Boolean>()
        return discounterror!!
    }

    fun ResponseAtt(): MutableLiveData<Storefront.Checkout> {
        dataAtt= MutableLiveData<Storefront.Checkout>()
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
        responsedata=MutableLiveData<Storefront.Checkout>()
        return responsedata!!
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
                } else {
                    responsedata?.setValue(
                        result.data!!.checkoutCustomerAssociateV2.checkout
                    )
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
                    val input = Storefront.CheckoutCreateInput()
                    input.lineItems = lineItems
                    lineitemsArr =lineItems
                    input.customAttributes
                    try {
                        doGraphQLMutateGraph(
                            repository,
                            Mutation.createCheckout(input, Constant.internationalPricing(),"cartlist"),
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
                        consumeResponse(GraphQLResponse.success(result as GraphCallResult.Success<*>),false)
                    } else {
                        consumeResponse(GraphQLResponse.error(result as GraphCallResult.Failure),false)
                    }
                    return Unit
                }
            }
            Thread(runnable).start()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun populateShipping(input: Storefront.MailingAddressInput, checkoutId: ID?) {
        try {
            doRetryGraphQLMutateGraph(
                repository,
                MutationQuery.populateShippingAddress(input, checkoutId),
                customResponse = object : CustomResponse {
                    override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
                        updateShippingAddressinvoke(result)
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun shippingLineUpdate(checkoutId: ID?,shippinghandle:String) {
        try {
            doGraphQLMutateGraph(
                repository,
                MutationQuery.checkoutShippingLineUpdate(checkoutId,shippinghandle),
                customResponse = object : CustomResponse {
                    override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
                            updateShippingLine(result)
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun completeCheckout(checkoutId: ID?,payment: Storefront.CreditCardPaymentInputV2) {
        try {
            doGraphQLMutateGraph(
                repository,
                MutationQuery.checkoutCompleteWithCreditCardV2(checkoutId,payment),
                customResponse = object : CustomResponse {
                    override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
                        updateCheckout(result)
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun updateEmail(checkoutId: ID?,email:String) {
        try {
            doGraphQLMutateGraph(
                repository,
                MutationQuery.checkoutEmailUpdate(checkoutId,email),
                customResponse = object : CustomResponse {
                    override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {

                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun updateCheckout(graphCallResult: GraphCallResult<Storefront.Mutation>): Unit {
        try {
            if (graphCallResult is GraphCallResult.Success<*>) {
                checkoutResponse(GraphQLResponse.success(graphCallResult as GraphCallResult.Success<*>))
            } else {
                checkoutResponse(GraphQLResponse.error(graphCallResult as GraphCallResult.Failure))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Unit
    }
    private fun checkoutResponse(reponse: GraphQLResponse) {
        when (reponse.status) {
            Status.SUCCESS -> {
                val result = (reponse.data as GraphCallResult.Success<Storefront.Mutation>).response
                if (result.hasErrors) {
                    val errors = result.errors
                    val iterator = errors.iterator()
                    val errormessage = StringBuilder()
                    var error: Error? = null
                    while (iterator.hasNext()) {
                        error = iterator.next()
                        errormessage.append(error.message())
                    }
                    Log.i("showmsg", "err1 " + errormessage.toString())
                } else {
                    Log.i("showmsg", "err3 " + reponse.data)
                    val errors = result.data!!.checkoutCompleteWithCreditCardV2.checkoutUserErrors
                    if (errors.size > 0) {
                        val iterator = errors.iterator()
                        var err = ""
                        while (iterator.hasNext()) {
                            val error = iterator.next() as Storefront.CheckoutUserError
                            err += error.message
                        }
                        Log.i("showmsg", "err2 " + err)
                    } else {
                        Log.i("showmsg", "err3 " + reponse.data)
                        Checkoutdata.setValue(
                            result.data!!.checkoutCompleteWithCreditCardV2)
                    }
                }
            }
            Status.ERROR -> {
                message.setValue(reponse.error!!.error.message)
                Log.i("showmsg", "err3 " + reponse.error)
            }
            else -> {
            }
        }
    }
    private fun updateShippingAddressinvoke(graphCallResult: GraphCallResult<Storefront.Mutation>): Unit {
        try {
            if (graphCallResult is GraphCallResult.Success<*>) {
                consumeUpdateAddressResponse(GraphQLResponse.success(graphCallResult as GraphCallResult.Success<*>))
            } else {
                consumeUpdateAddressResponse(GraphQLResponse.error(graphCallResult as GraphCallResult.Failure))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Unit
    }
    private fun updateShippingLine(graphCallResult: GraphCallResult<Storefront.Mutation>): Unit {
        try {
            if (graphCallResult is GraphCallResult.Success<*>) {
                consumeShippingLineResponse(GraphQLResponse.success(graphCallResult as GraphCallResult.Success<*>))
            } else {
                consumeShippingLineResponse(GraphQLResponse.error(graphCallResult as GraphCallResult.Failure))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Unit
    }
    private fun consumeUpdateAddressResponse(reponse: GraphQLResponse) {
        when (reponse.status) {
            Status.SUCCESS -> {
                val result = (reponse.data as GraphCallResult.Success<Storefront.Mutation>).response
                if (result.hasErrors) {
                    val errors = result.errors
                    val iterator = errors.iterator()
                    val errormessage = StringBuilder()
                    var error: Error? = null
                    while (iterator.hasNext()) {
                        error = iterator.next()
                        errormessage.append(error.message())
                    }
                    for(i in 0 until result.data!!.checkoutShippingAddressUpdateV2.checkoutUserErrors.size)
                    {
                        Toast.makeText(context,result.data!!.checkoutShippingAddressUpdateV2.checkoutUserErrors.get(i).message,Toast.LENGTH_SHORT).show()
                    }
                    Log.i("showmsg", "err1 " + errormessage.toString())
                } else {
                    Log.i("showmsg", "err3 " + reponse.data)
                    val errors = result.data!!.checkoutShippingAddressUpdateV2.checkoutUserErrors
                    for(i in 0 until result.data!!.checkoutShippingAddressUpdateV2.checkoutUserErrors.size) {
                        Toast.makeText(context,result.data!!.checkoutShippingAddressUpdateV2.checkoutUserErrors.get(i).message,Toast.LENGTH_SHORT).show()
                    }
                    if (errors.size > 0) {
                        val iterator = errors.iterator()
                        var err = ""
                        while (iterator.hasNext()) {
                            val error = iterator.next() as Storefront.CheckoutUserError
                            err += error.message
                        }
                        Log.i("showmsg", "err2 " + err)
                    } else {
                        Log.i("showmsg", "err3 " + reponse.data)
                        Addressdata.setValue(
                            result.data!!.checkoutShippingAddressUpdateV2.checkout)
                    }
                }
            }
            Status.ERROR -> {
                message.setValue(reponse.error!!.error.message)
                Log.i("showmsg", "err3 " + reponse.error)
            }
            else -> {
            }
        }
    }
    private fun consumeShippingLineResponse(reponse: GraphQLResponse) {
        when (reponse.status) {
            Status.SUCCESS -> {
                val result = (reponse.data as GraphCallResult.Success<Storefront.Mutation>).response
                if (result.hasErrors) {
                    val errors = result.errors
                    val iterator = errors.iterator()
                    val errormessage = StringBuilder()
                    var error: Error? = null
                    while (iterator.hasNext()) {
                        error = iterator.next()
                        errormessage.append(error.message())
                    }
                    Log.i("showmsg", "err1 " + errormessage.toString())
                } else {
                    Log.i("showmsg", "err3 " + reponse.data)
                    val errors = result.data!!.checkoutShippingLineUpdate.checkoutUserErrors
                    if (errors.size > 0) {
                        val iterator = errors.iterator()
                        var err = ""
                        while (iterator.hasNext()) {
                            val error = iterator.next() as Storefront.CheckoutUserError
                            err += error.message
                        }
                        Log.i("showmsg", "err2 " + err)
                    } else {
                        Log.i("showmsg", "err3 " + reponse.data)
                        ShippingLinedata.setValue(result.data!!.checkoutShippingLineUpdate)
                    }
                }
            }
            Status.ERROR -> {
                message.setValue(reponse.error!!.error.message)
                Log.i("showmsg", "err3 " + reponse.error)
            }
            else -> {
            }
        }
    }
    fun moveToWishList(item: CartListItem) {
        try {
            val runnable = Runnable {
                if (repository.getSingleVariantData(item.variant_id!!) == null) {
                    val data = WishItemData()
                    data.variant_id = item.variant_id!!
                    data.selling_plan_id=""
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
                if (cartCount > 0) {
                    prepareCart()
                } else {
                    var ref= context as CartList
                    ref.finish()
                    ref.startActivity(ref.intent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun updateCart(item: CartListItem) {
        try {
            val runnable = Runnable {
                if(item.qty!=null){
                    val data = repository.getSingLeItem(item.variant_id!!)
                    data.qty = Integer.parseInt(item.qty!!)
                    repository.updateSingLeItem(data)
                }
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
                        val payload = result.data!!.checkoutAttributesUpdateV2
                        if (payload.checkoutUserErrors.size > 0) {
                            val iterator = payload.checkoutUserErrors.iterator()
                            var error: Storefront.CheckoutUserError? = null
                            while (iterator.hasNext()) {
                                error = iterator.next() as Storefront.CheckoutUserError
                                message.setValue(error.message)
                            }
                        } else {
                            val checkout = payload.checkout
                            /*if (SplashViewModel.featuresModel.ai_product_reccomendaton) {
                                getRecommendations(checkout)
                            }*/
                            /*if (isLoggedIn){
                                checkFirstSale(checkout,dataAtt!!)
                            }else{*/
                                dataAtt?.setValue(checkout)
                           // }
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
            Log.d("testerror",""+list)
            var body = Body()
            body.queries = mutableListOf(query)
            Log.i("Body", "" + list)
            disposables.add(repository.getRecommendation(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result -> api.setValue(ApiResponse.success(result)) },
                    {
                            throwable -> api.setValue(ApiResponse.error(throwable))
                    Log.d("testerror",""+throwable)}

                ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun consumeResponse(response: GraphQLResponse,shipping: Boolean) {
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
                        if(result.data!!.checkoutCreate.checkout.lineItems.edges.size>0) {
                            product_id= result.data!!.checkoutCreate.checkout.lineItems.edges.get(0).node.variant.product.id.toString()
                        }
                        if (payload.checkoutUserErrors.size > 0) {
                            val iterator = payload.checkoutUserErrors.iterator()
                            if (payload.checkoutUserErrors[0].field[2] != null) {
                                val id = lineitemsArr?.get(payload.checkoutUserErrors[0].field[2].toInt())?.variantId.toString()
                                var cartitem=CartListItem()
                                cartitem.variant_id=id
                                removeFromCart(cartitem)
                            }else{
                                var error: Storefront.CheckoutUserError? = null
                                while (iterator.hasNext()) {
                                    error = iterator.next() as Storefront.CheckoutUserError
                                    message.setValue(error.message)
                                }
                            }
                        } else {
                            val checkout = payload.checkout
                           /* if (SplashViewModel.featuresModel.ai_product_reccomendaton) {
                                getRecommendations(checkout)
                            }*/
                            if (isLoggedIn){
                                checkFirstSale(checkout,data)
                            }else{
                                data.setValue(checkout)
                            }
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
    fun getWholesalepricedata(authorization:String,apikey:String,params:JsonObject): MutableLiveData<ApiResponse> {
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
    fun GetWholeSaleDiscountCoupon(mid:String,coupon:String,type:String,Discount_Price:String): MutableLiveData<ApiResponse> {
        doRetrofitCall(
            repository.getWholessaleDiscountCoupon(mid,coupon,type,Discount_Price),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    wholesale_discount_coupon.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    wholesale_discount_coupon.value = ApiResponse.error(error)
                }
            },
            context = context
        )
        return wholesale_discount_coupon
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
    private fun invokeDiscount(result: GraphCallResult<Storefront.Mutation>,checkout:Storefront.Checkout,data: MutableLiveData<Storefront.Checkout>) {
        if (result is GraphCallResult.Success<*>) {
            consumeResponseDiscount(GraphQLResponse.success(result as GraphCallResult.Success<*>),checkout,data)
        } else {
            consumeResponseDiscount(GraphQLResponse.error(result as GraphCallResult.Failure),checkout,data)
        }
    }
    fun removeDiscount(checkoutId: ID?) {
        doGraphQLMutateGraph(repository, MutationQuery.checkoutDiscountCodeRemove(checkoutId), customResponse = object : CustomResponse {
            override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
                invokeRemoveDiscount(result)
            }
        }, context = context)
    }
    private fun invokeRemoveDiscount(result: GraphCallResult<Storefront.Mutation>) {
        if (result is GraphCallResult.Success<*>) {
            consumeRemoveDiscount(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            consumeRemoveDiscount(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
    }

    private fun consumeRemoveDiscount(response: GraphQLResponse) {
        when (response.status) {
            Status.SUCCESS -> {
                val result = (response.data as GraphCallResult.Success<Storefront.Mutation>).response
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
                    val payload = result.data!!.checkoutDiscountCodeRemove
                    if (payload.checkoutUserErrors.size > 0) {
                        val iterator = payload.checkoutUserErrors.iterator()
                        var error: Storefront.CheckoutUserError? = null
                        while (iterator.hasNext()) {
                            error = iterator.next() as Storefront.CheckoutUserError
                            message.setValue(error.message)
                        }
                        discounterror!!.value=true
                    } else {
                        removediscount.setValue(result.data)
                    }
                }
            }
            /*Status.ERROR -> errormessage.setValue(response.error!!.error.message)*/
            else -> {
            }
        }
    }

    private fun consumeResponseDiscount(response: GraphQLResponse,checkout:Storefront.Checkout,data: MutableLiveData<Storefront.Checkout>) {
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
                    if (payload.checkoutUserErrors.size > 0) {
                        val iterator = payload.checkoutUserErrors.iterator()
                        var error: Storefront.CheckoutUserError? = null
                        while (iterator.hasNext()) {
                            error = iterator.next() as Storefront.CheckoutUserError
                           // message.setValue(error.message)
                        }
                        data.value=checkout
                    } else {
                        data.value=result.data!!.checkoutDiscountCodeApplyV2.checkout
                    }
                }
            }
            /*Status.ERROR -> errormessage.setValue(response.error!!.error.message)*/
            else -> {
            }
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
                    if (payload.checkoutUserErrors.size > 0) {
                        val iterator = payload.checkoutUserErrors.iterator()
                        var error: Storefront.CheckoutUserError? = null
                        while (iterator.hasNext()) {
                            error = iterator.next() as Storefront.CheckoutUserError
                            message.setValue(error.message)
                        }
                        discounterror!!.value=true
                    } else {
                        discount?.setValue(result.data!!)
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

    fun fillDeliveryParam(edges: List<Storefront.CheckoutLineItemEdge>): HashMap<String, String> {
        var param = HashMap<String, String>()
        for (i in 0..edges.size - 1) {
            param.put(
                "cart[$i][product_id]",
                        edges[i].node.id.toString()
                        .replace("gid://shopify/CheckoutLineItem/", "").split("?")[0]
            )
            param.put(
                "cart[$i][variant_id]",
                        edges[i].node.variant.id.toString().replace("gid://shopify/ProductVariant/", "")
            )
            param.put("cart[$i][quantity]", edges[i].node.quantity.toString())
        }
        param.put("shop", Urls(MyApplication.context).shopdomain)
        param.put("type", "pickup")
        return param
    }
    fun prepareCartwithAttribute(
        attributeInputs: MutableList<Storefront.AttributeInput>,
        order_note: String
    ,checkoutId: ID?
    ) {
        try {
            val runnable = object : Runnable {
                override fun run() {
                    try {
                        val input = Storefront.CheckoutAttributesUpdateV2Input()
                        input.setCustomAttributes(attributeInputs)
                        input.note = order_note
                        val call = repository.graphClient.mutateGraph(
                            Mutation.updateCheckout(
                                input,
                                Constant.internationalPricing()
                            ,"cartlist",checkoutId!!)
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
    fun checkFirstSale(checkout: Storefront.Checkout, data: MutableLiveData<Storefront.Checkout>) {
        try {
            if(SplashViewModel.featuresModel.firstSale){
                checkIfAnyORderPlacedFromApp(checkout,data)
            }else{
                data.value=checkout
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    fun checkIfAnyORderPlacedFromApp(
        checkout: Storefront.Checkout,
        data: MutableLiveData<Storefront.Checkout>
    ) {
        try {
            doRetrofitCall(
                repository.getFirstOrder(Urls(MyApplication.context).mid,MagePrefs.getCustomerID()!!.replace("gid://shopify/Customer/","")), disposables, customResponse = object : CustomResponse {
                    override fun onSuccessRetrofit(result: JsonElement) {
                        if (result!=null){
                            var json = result.asJsonObject.get("success").asBoolean
                            if (!json){
                                doGraphQLMutateGraph(
                                    repository,
                                    MutationQuery.checkoutDiscountCodeApply(
                                        checkout.id,
                                        Urls.firstsalecoupon,
                                        Constant.internationalPricing()
                                    ),
                                    customResponse = object : CustomResponse {
                                        override fun onSuccessMutate(result: GraphCallResult<Storefront.Mutation>) {
                                            invokeDiscount(result,checkout,data)
                                        }
                                    },
                                    context = context
                                )
                            }else{
                                CoroutineScope(Dispatchers.Main).launch {
                                    data.value = checkout
                                }
                            }
                        }else{
                            CoroutineScope(Dispatchers.Main).launch {
                                data.value = checkout
                            }
                        }

                    }
                    override fun onErrorRetrofit(error: Throwable) {
                        data.value = checkout
                    }
                }, context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}