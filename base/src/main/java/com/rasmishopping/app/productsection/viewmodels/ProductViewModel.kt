package com.rasmishopping.app.productsection.viewmodels

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.shopify.graphql.support.ID
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.dbconnection.entities.CartItemData
import com.rasmishopping.app.dbconnection.entities.ItemData
import com.rasmishopping.app.dbconnection.entities.WishItemData
import com.rasmishopping.app.dbconnection.dependecyinjection.Body
import com.rasmishopping.app.dbconnection.dependecyinjection.InnerData
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLMutateGraph
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.network_transaction.doRetrofitCall
import com.rasmishopping.app.productsection.models.MediaModel
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.shopifyqueries.Mutation
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.utils.*
import com.rasmishopping.app.utils.Urls.Data.SIZECHART
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.lang.Runnable
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.regex.Pattern

class ProductViewModel( val repository: Repository) : ViewModel() {
    var handle = ""
    var id = ""
    private val disposables = CompositeDisposable()
    private val responseLiveData = MutableLiveData<GraphQLResponse>()
    val recommendedLiveData = MutableLiveData<GraphQLResponse>()
    var reviewResponse: MutableLiveData<ApiResponse>? = null
    var reviewBadges: MutableLiveData<ApiResponse>? = MutableLiveData<ApiResponse>()
    var createreviewResponse = MutableLiveData<ApiResponse>()
    var getjudgeMeProductID = MutableLiveData<ApiResponse>()
    var getjudgeMeReviewCount = MutableLiveData<ApiResponse>()
    var getjudgeMeReviewCreate = MutableLiveData<ApiResponse>()
    var getjudgeMeReviewIndex = MutableLiveData<ApiResponse>()
    var getAlireviewInstallStatus = MutableLiveData<ApiResponse>()
    var getAlireviewProduct = MutableLiveData<ApiResponse>()
    var sizeChartVisibility = MutableLiveData<Boolean>()
    var sizeChartUrl = MutableLiveData<String>()
    var getyotpocreate = MutableLiveData<ApiResponse>()
    lateinit var context: Context
    private val TAG = "ProductViewModel"
    val filteredlist = MutableLiveData<List<Storefront.ProductVariantEdge>>()
    val data = MutableLiveData<Storefront.Checkout>()
    val message = MutableLiveData<String>()
    val chatgptresponse = MutableLiveData<String>()
    fun getChatGPT(description:String):MutableLiveData<String>{
        sendChatGPTRequest(description)
        return chatgptresponse
    }

    private fun sendChatGPTRequest(description: String) {
        doRetrofitCall(
            repository.chatGPT(description),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    Log.i("SaifDevResult",""+result)
                    try {
                        var data=JSONObject(ApiResponse.success(result).data.toString())
                        if(data.has("choices") && data.getJSONArray("choices").length()>0){
                            var text=data.getJSONArray("choices").getJSONObject(0).getString("text")
                            chatgptresponse.value=text
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }

                override fun onErrorRetrofit(error: Throwable) {
                    Log.i("Result",""+error.printStackTrace())
                    chatgptresponse.value = ""
                }
            },
            context = context
        )
    }

    fun getAliReviewStatus() {
        doRetrofitCall(
            repository.AliReviewInstallStatus(SplashViewModel.ALIREVIEW_INSTALLSTATUS),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    getAlireviewInstallStatus.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    getAlireviewInstallStatus.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }

    fun getAliReviewProduct(shop_id: String, product_id: String, currentPage: Int) {
        doRetrofitCall(
            repository.getAliProductReview(SplashViewModel.ALIREVIEW_PRODUCT,shop_id, product_id, currentPage),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    getAlireviewProduct.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    getAlireviewProduct.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }

    fun judgemeProductID(url: String, handle: String, apiToken: String, shopDomain: String) {
        Log.d("javed", "onSuccessRetrofit: "+url)
        Log.d("javed", "onSuccessRetrofit: "+handle)
        Log.d("javed", "onSuccessRetrofit: "+apiToken)
        Log.d("javed", "onSuccessRetrofit: "+shopDomain)
        doRetrofitCall(
            repository.judgemeProductID(url, handle, apiToken, shopDomain),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    getjudgeMeProductID.value = ApiResponse.success(result)
                    Log.d("javed", "onSuccessRetrofit: "+result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    getjudgeMeProductID.value = ApiResponse.error(error)

                }
            },
            context = context
        )
    }

    fun judgemeReviewCount(product_id: String, apiToken: String, shopDomain: String) {

        Log.d("javed", "onSuccessRetrofit: "+handle)
        Log.d("javed", "onSuccessRetrofit: "+apiToken)
        Log.d("javed", "onSuccessRetrofit: "+shopDomain)
        doRetrofitCall(
            repository.judgemeReviewCount(product_id, apiToken, shopDomain),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    getjudgeMeReviewCount.value = ApiResponse.success(result)
                    Log.d("javed", "onSuccessRetrofit: "+result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    getjudgeMeReviewCount.value = ApiResponse.error(error)
                    Log.d("javed", "eroor: "+error)
                }
            },
            context = context
        )
    }

    fun judgemeReviewCreate(params: JsonObject) {
        doRetrofitCall(
            repository.judgemeReviewCreate(params),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    getjudgeMeReviewCreate.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    getjudgeMeReviewCreate.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }

    fun judgemeReviewIndex(
        product_id: String,
        apiToken: String,
        shopDomain: String,
        per_page: Int,
        page: Int
    ) {
        doRetrofitCall(
            repository.judgemeReviewIndex(apiToken, shopDomain, per_page, page, product_id),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    getjudgeMeReviewIndex.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    getjudgeMeReviewIndex.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }

    fun getReviews(mid: String, product_id: String, page: Int): MutableLiveData<ApiResponse> {
        reviewResponse = MutableLiveData<ApiResponse>()
        getProductReviews(mid, product_id, page)
        return reviewResponse!!
    }

    fun getReviewBadges(mid: String, product_id: String): MutableLiveData<ApiResponse> {
        getbadgeReviews(mid, product_id)
        return reviewBadges!!
    }

    fun getProductReviews(mid: String, product_id: String, page: Int) {

        doRetrofitCall(
            repository.getProductReviews(mid, product_id, page),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    reviewResponse?.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    reviewResponse?.value = ApiResponse.error(error)
                }
            },
            context = context
        )

    }


    fun getbadgeReviews(mid: String, product_id: String) {

        doRetrofitCall(
            repository.getbadgeReviews(mid, product_id),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    reviewBadges?.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    reviewBadges?.value = ApiResponse.error(error)
                }
            },
            context = context
        )

    }

    fun getcreateReview(
        mid: String,
        reviewRating: String,
        product_id: String,
        reviewAuthor: String,
        reviewEmail: String,
        reviewTitle: String,
        reviewBody: String
    ) {
        doRetrofitCall(
            repository.getcreateReview(
                mid,
                reviewRating,
                product_id,
                reviewAuthor,
                reviewEmail,
                reviewTitle,
                reviewBody
            ),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    createreviewResponse.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    createreviewResponse.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }

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

    fun shopifyRecommended() {
        if (SplashViewModel.featuresModel.recommendedProducts) {
            getRecommendedProducts()
        }
    }

    fun Response(): MutableLiveData<GraphQLResponse> {
        if (!id.isEmpty()) {
            getProductsById()
        }
        if (!handle.isEmpty()) {
            getProductsByHandle()
        }
        return responseLiveData
    }

    private fun getRecommendedProducts() {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.recommendedProducts(id, Constant.internationalPricing()),
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

    private fun getProductsById() {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.getProductById(id, Constant.internationalPricing()),
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        invoke(result)
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getProductsByHandle() {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.getProductByHandle(handle, Constant.internationalPricing()),
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        invoke(result)
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private operator fun invoke(result: GraphCallResult<Storefront.QueryRoot>): Unit {
        if (result is GraphCallResult.Success<*>) {
            responseLiveData.setValue(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            responseLiveData.setValue(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
        return Unit
    }


    fun setWishList(product_id: String): Boolean {
        val isadded = booleanArrayOf(false)
        try {
            val executor = Executors.newSingleThreadExecutor()
            val callable = Callable {
                if (repository.getSingleData(product_id) == null) {
                    Log.i("MageNative", "WishListCount : " + repository.wishListData.size)
                    val data = ItemData()
                    data.product_id = product_id
                    repository.insertWishListData(data)
                    Log.i("MageNative", "WishListCount 2: " + repository.wishListData.size)
                    isadded[0] = true
                }
                isadded[0]
            }
            val future = executor.submit(callable)
            isadded[0] = future.get()
            executor.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return isadded[0]
    }

    fun AddtoWishVariant(variantId: String) {
        try {
            val runnable = Runnable {
                val data: WishItemData
                if (repository.getSingleVariantData(variantId) == null) {
                    data = WishItemData()
                    data.variant_id = variantId
                    data.selling_plan_id=""

                    repository.insertWishListVariantData(data)
                }
                Log.i("MageNative", "CartCount : " + repository.allCartItems.size)
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun isInwishList(variantId: String): Boolean {
        val isadded = booleanArrayOf(false)
        try {
            val executor = Executors.newSingleThreadExecutor()
            val callable = Callable {
                if (repository.getSingleVariantData(variantId) != null) {

                    Log.i("MageNative", "item already in wishlist : ")
                    isadded[0] = true
                }
                isadded[0]
            }
            val future = executor.submit(callable)
            isadded[0] = future.get()
            executor.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return isadded[0]
    }

    fun deleteData(product_id: String) {
        try {
          CoroutineScope(Dispatchers.IO).launch {
                try {
                    val data = repository.getSingleVariantData(product_id)
                    repository.deleteSingleVariantData(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun filterList(list: List<Storefront.ProductVariantEdge>) {
        try {
            disposables.add(repository.getList(list)
                .subscribeOn(Schedulers.io())
                //  .filter { x -> x.node.availableForSale }
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result -> filteredlist.setValue(result) })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCleared() {
        disposables.clear()
    }

//    public fun isInwishList(product_id: String): Boolean {
//        val isadded = booleanArrayOf(false)
//        try {
//            val executor = Executors.newSingleThreadExecutor()
//            val callable = Callable {
//                if (repository.getSingleData(product_id) != null) {
//
//                    Log.i("MageNative", "item already in wishlist : ")
//                    isadded[0] = true
//                }
//                isadded[0]
//            }
//            val future = executor.submit(callable)
//            isadded[0] = future.get()
//            executor.shutdown()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        return isadded[0]
//    }

    fun addToCart(variantId: String, quantity: Int, subscribe_id: String, offerName: String) {
        try {
            val runnable = Runnable {
                val data: CartItemData
                if (repository.getSingLeItem(variantId) == null ) {
                    data = CartItemData()
                    data.variant_id = variantId
                    data.qty = quantity
                    data.selling_plan_id = subscribe_id
                    data.offerName = offerName
                    repository.addSingLeItem(data)
                }
                else{
                    data = repository.getSingLeItem(variantId)
                    val qty = data.qty + quantity
                    data.qty = qty
                    data.selling_plan_id=subscribe_id
                    data.offerName = offerName
                    repository.updateSingLeItem(data)
                }
                Log.i("MageNative", "CartCount : " + repository.allCartItems.size)
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getQtyInCart(variantId: String): Int {
        var variant_qty = runBlocking(Dispatchers.IO) {
            if (repository.getSingLeItem(variantId) == null) {
                return@runBlocking 0
            } else {
                return@runBlocking repository.getSingLeItem(variantId).qty
            }
        }
        return variant_qty
    }

    private val api = MutableLiveData<ApiResponse>()
    fun getApiResponse(): MutableLiveData<ApiResponse> {
        return api
    }

    fun getRecommendations(id: String) {
        RetrofitUrlManager.getInstance().putDomain("douban", Urls.PERSONALISED)

        try {

            var query = InnerData()
            query.id = "query1"

            query.maxRecommendations = 8
            query.recommendationType = "similar_products"

            var list = mutableListOf<Long>()
            var s = id
            list.add(s.replace("gid://shopify/Product/", "").toLong())
            query.productIds = list
            Log.d("PrakharNew",""+list)
            var body = Body()
            body.queries = mutableListOf(query)
            Log.i("Body", "" + list)
            doRetrofitCall(
                repository.getRecommendation(body),
                disposables,
                customResponse = object : CustomResponse {
                    override fun onSuccessRetrofit(result: JsonElement) {
                        api.value = ApiResponse.success(result)

                    }

                    override fun onErrorRetrofit(error: Throwable) {
                        api.value = ApiResponse.error(error)
                        Log.d("PrakharNew",""+error)
                    }
                },
                context = context
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSizeChart(
        shop: String,
        source: String,
        product_id: String,
        tags: String,
        vendor: String,
        collections: String? = null
    ) {
        //   Log.d(TAG, "getSizeChart: "+collections)
        RetrofitUrlManager.getInstance().putDomain("douban", Urls.SIZECHART)
        var hashMap = HashMap<String, String>()
        hashMap.put("shop", shop)
        hashMap.put("source", source)
        hashMap.put("product", product_id)
        hashMap.put("tags", tags)
        hashMap.put("vendor", vendor)
        if (collections != null) {
            hashMap.put("collections", collections)
        }
        Log.d("OKHttp", "" + SIZECHART + "?" + getPostDataString(hashMap))
        sizeChartUrl.value = SIZECHART + "?" + getPostDataString(hashMap)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                  //  var result = async(Dispatchers.IO) {
                      val result=  URL(SIZECHART + "?" + getPostDataString(hashMap)).readText()
                   // }
                    parseResponse(result)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseResponse(await: String) {
        CoroutineScope(Dispatchers.Main).launch {
            sizeChartVisibility.value = await.length != 0
        }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getPostDataString(params: HashMap<String, String>): String? {
        val result = StringBuilder()
        var first = true
        for ((key, value) in params.entries) {
            if (first) first = false else result.append("&")
            result.append(URLEncoder.encode(key, "UTF-8"))
            result.append("=")
            result.append(URLEncoder.encode(value, "UTF-8"))
        }
        Log.i("POST_STRING", "" + result)
        return result.toString()
    }

    fun filterArModel(armodelList: MutableList<MediaModel>): MutableLiveData<MutableList<MediaModel>> {
        val ardatamodelList = MutableLiveData<MutableList<MediaModel>>()
        disposables.add(repository.getArModels(armodelList).subscribeOn(Schedulers.io())
            .filter { t -> t.typeName.equals("Model3d") }
            .observeOn(AndroidSchedulers.mainThread())
            .toList()
            .subscribe { result -> ardatamodelList.value = result }
        )
        return ardatamodelList
    }

    fun isValidEmail(target: String): Boolean {
        val emailPattern = Pattern.compile(
            "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
            Pattern.CASE_INSENSITIVE
        )
        return emailPattern.matcher(target).matches()
    }

    fun NResponse(
        appkey: String,
        sku: String,
        product_title: String,
        product_url: String,
        display_name: String,
        email: String,
        review_content: String,
        review_title: String,
        review_score: String
    ): MutableLiveData<ApiResponse> {
        yotpocretereview(
            appkey,
            sku,
            product_title,
            product_url,
            display_name,
            email,
            review_content,
            review_title,
            review_score
        )
        return getyotpocreate
    }

    fun yotpocretereview(
        appkey: String,
        sku: String,
        product_title: String,
        product_url: String,
        display_name: String,
        email: String,
        review_content: String,
        review_title: String,
        review_score: String
    ) {
        doRetrofitCall(
            repository.yotpocretereview(
                appkey,
                sku,
                product_title,
                product_url,
                display_name,
                email,
                review_content,
                review_title,
                review_score

            ),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    getyotpocreate.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    getyotpocreate.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }
    fun prepareCart(variantId: String, quantity: Int) {
        try {
            val runnable = object : Runnable {
                override fun run() {
                    val input = Storefront.CheckoutCreateInput()
                    var item= Storefront.CheckoutLineItemInput(quantity, ID(variantId))
                    val lineItems = ArrayList<Storefront.CheckoutLineItemInput>()
                    lineItems.add(item)
                    input.lineItems = lineItems
                    try {
                        doGraphQLMutateGraph(repository,
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
                        if (payload.checkoutUserErrors.size > 0) {
                            val iterator = payload.checkoutUserErrors.iterator()
                            var error: Storefront.CheckoutUserError? = null
                            while (iterator.hasNext()) {
                                error = iterator.next() as Storefront.CheckoutUserError
                                message.setValue(error.message)
                            }
                        } else {
                            data.setValue(payload.checkout)
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
