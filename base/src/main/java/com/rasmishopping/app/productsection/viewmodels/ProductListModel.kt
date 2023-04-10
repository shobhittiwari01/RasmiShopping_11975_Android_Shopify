package com.rasmishopping.app.productsection.viewmodels
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.GraphClient
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel.Companion.featuresModel
import com.rasmishopping.app.dbconnection.entities.WishItemData
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Runnable
import java.util.concurrent.Callable
import java.util.concurrent.Executors
class ProductListModel(var repository: Repository) : ViewModel() {
    private var categoryID = ""
    var shopID = ""
    var tags_ = ""
    var nextcursor = "nocursor"
    var prevcursor = "prevcursor"
    var thread: Job?=null
    var stop:Boolean?=false
    var collectionData: MutableLiveData<Storefront.Collection> = MutableLiveData<Storefront.Collection>()
    var categoryHandle = ""
    var cursor = "nocursor"
    var isDirection = true
    var sortKeys: Storefront.ProductCollectionSortKeys? = Storefront.ProductCollectionSortKeys.CREATED
    var keys: Storefront.ProductSortKeys? = Storefront.ProductSortKeys.CREATED_AT
    var number = 10
    private val disposables = CompositeDisposable()
    val message = MutableLiveData<String>()
    val filteredproducts = MutableLiveData<MutableList<Storefront.ProductEdge>>()
    lateinit var context: Context
    private val filerapiResponseData = MutableLiveData<ApiResponse>()
    fun getcategoryID(): String {
        return categoryID
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

    fun setcategoryID(categoryID: String) {
        this.categoryID = categoryID
    }

    fun getcategoryHandle(): String {
        return categoryHandle
    }

    fun setcategoryHandle(categoryHandle: String) {
        this.categoryHandle = categoryHandle
    }

    fun Response(sortcursors: String) {
        if (!getcategoryID().isEmpty()) {
            getProductsById(sortcursors)
        }
        if (!getcategoryHandle().isEmpty()) {
            getProductsByHandle(sortcursors)
        }
        if (!shopID.isEmpty()) {
            getAllProducts()
        }
        //getCollectionTags()
    }

    private fun getAllProducts() {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.getAllProducts(
                    cursor,
                    keys,
                    isDirection,
                    number,
                    Constant.internationalPricing(),getFilters()
                ),
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

    private fun getProductsById(sortcursors:String) {
        try {

            doGraphQLQueryGraph(
                repository,
                Query.getProductsById(
                    getcategoryID(),
                    sortcursors,
                    sortKeys,
                    isDirection,
                    number,
                    Constant.internationalPricing(), getFilters()
                ),
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

    private fun getProductsByHandle(sortcursors: String) {
        try {

            doGraphQLQueryGraph(
                repository,
                Query.getProductsByHandle(
                    getcategoryHandle(),
                    sortcursors,
                    sortKeys,
                    isDirection,
                    number,
                    Constant.internationalPricing(), getFilters()
                ),
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
            consumeResponse(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            consumeResponse(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
        return Unit
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
                    message.setValue(errormessage.toString())
                } else {
                    var edges: List<Storefront.ProductEdge>? = null
                    if (!getcategoryHandle().isEmpty()) {
                        if (result.data!!.collection!= null) {
                            edges = result.data!!.collection.products.edges
//                            filteredproducts.value=edges
                            ( result.data!!.collection).also { collectionData.value = it }
                            if ( edges.isNotEmpty()){
                                cursor = edges.get(edges.size - 1).cursor
                            }
                            thread = GlobalScope.launch(Dispatchers.IO) {
                                getallproductsbyhandle(repository.graphClient, number, cursor)
                            }

                        }

                    }
                    if (!getcategoryID().isEmpty()) {
                        if (result.data!!.node != null) {
                            edges = (result.data?.node as Storefront.Collection).products.edges
//                            filteredproducts.value=edges
                            (result.data?.node as Storefront.Collection).also { collectionData.value = it }
                            if ( edges.isNotEmpty()){
                                cursor = edges.get(edges.size - 1).cursor
                            }
                            thread = GlobalScope.launch(Dispatchers.IO) {
                                    getallproducts(repository.graphClient, number, cursor)
                            }

                        }
                    }
                   if (!shopID.isEmpty()) {
                        edges = result.data!!.products.edges
                       if ( edges.isNotEmpty()){
                           cursor = edges.get(edges.size - 1).cursor
                       }
                       thread = GlobalScope.launch(Dispatchers.IO) {
                           getAllProductsData(repository.graphClient, number, cursor)
                       }
                   }
                    filterProduct(edges)
                }
            }
            Status.ERROR -> CoroutineScope(Dispatchers.Main).launch {
                message.setValue(reponse.error!!.error.message)
            }
            else -> {
            }
        }
    }

    fun getAllProductsData(graph: GraphClient, number: Int, cursors: String){
        thread=  CoroutineScope(Dispatchers.IO).launch {
            try {
                var edges: MutableList<Storefront.ProductEdge>? = null
                if (nextcursor != prevcursor) {
                    Log.d("Threading","background thread")
                    var call = graph.queryGraph(
                        Query.getAllProducts(
                            cursors,
                            keys,
                            isDirection,
                            number,
                            Constant.internationalPricing(),getFilters()
                        )
                    )
                    call.enqueue {
                            result: GraphCallResult<Storefront.QueryRoot> ->
                        if (result is GraphCallResult.Success<*>) {
                            prevcursor = cursors
                            val output =
                                (result as GraphCallResult.Success<Storefront.QueryRoot>).response
                            if(output.data!!.products.edges!=null)
                            {
                                if (output.data!!.products.edges.size > 0) {
                                    nextcursor = output.data!!.products.edges.get(output.data!!.products.edges.size!! - 1)!!.cursor
                                    edges = output.data!!.products.edges
                                    cursor = nextcursor
                                    getAllProductsData(graph, number, nextcursor)
                                }
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                if(edges!=null){
                                    filterProduct(edges)
                                }
                            }
                        } else {
                            consumeResponse(GraphQLResponse.error(result as GraphCallResult.Failure))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun getFilters() : ArrayList<Storefront.ProductFilter>{
        val productFiltersarray = ArrayList<Storefront.ProductFilter>()
        if(SplashViewModel.filterfinaldata.size>0){
            var iterator=SplashViewModel.filterfinaldata.keys.iterator()
            while (iterator.hasNext()){
                var key=iterator.next()
                val list=SplashViewModel.filterfinaldata.get(key)
                when(key){
                    "availability"->{
                        val avalibityiterator=list!!.iterator()
                        while (avalibityiterator.hasNext()){
                            var availkey=avalibityiterator.next()
                            var value=JSONObject(SplashViewModel.filterinputformat.get(availkey)).getBoolean("available")
                            val instockFilter = Storefront.ProductFilter()
                            instockFilter.available = value
                            productFiltersarray.add(instockFilter)
                        }
                    }
                    "product_type" ->{
                        val producttype=list!!.iterator()
                        while (producttype.hasNext()){
                            var value=producttype.next()
                            var productTypeFilter= Storefront.ProductFilter()
                            productTypeFilter.productType = value
                            productFiltersarray.add(productTypeFilter)
                        }
                    }
                    "vendor" -> {
                        val productvendor=list!!.iterator()
                        while (productvendor.hasNext()){
                            var value=productvendor.next()
                            var productVendorFilter= Storefront.ProductFilter()
                            productVendorFilter.productVendor = value
                            productFiltersarray.add(productVendorFilter)
                        }
                    }
                    "price"->{
                        val pricefilter = Storefront.ProductFilter()
                        val priceRangeFilter = Storefront.PriceRangeFilter()
                        priceRangeFilter.min = list!!.get(0).toDouble()
                        priceRangeFilter.max = list.get(1).toDouble()
                        pricefilter.price=priceRangeFilter
                        productFiltersarray.add(pricefilter)
                    }else->{
                    if(SplashViewModel.filterinputformat.containsKey(key)){
                        val productmeta=list!!.iterator()
                        while (productmeta.hasNext()){
                            var value=productmeta.next()
                            var metafieldfilter = Storefront.ProductFilter()
                            var meta =  Storefront.MetafieldFilter(SplashViewModel.filterinputformat.get(key),key,value)
                            metafieldfilter.productMetafield=meta
                           // metafieldfilter.variantMetafield=meta
                            productFiltersarray.add(metafieldfilter)
                        }
                    }else{
                        val productoption=list!!.iterator()
                        while (productoption.hasNext()){
                            var value=productoption.next()
                            var variantionfilter = Storefront.ProductFilter()
                            var variation = Storefront.VariantOptionFilter(key,value)
                            variantionfilter.variantOption=variation
                            productFiltersarray.add(variantionfilter)
                        }
                    }
                }
                }
            }
        }
        System.out.println("FiltersFinalTobeSent"+productFiltersarray.toString())
        return productFiltersarray
    }


    fun getallproducts(graph: GraphClient, number: Int, cursors: String) {
//        if(stop==false)
//        {
            thread=  CoroutineScope(Dispatchers.IO).launch {

                try {

                    var edges: MutableList<Storefront.ProductEdge>? = null
                    if (nextcursor != prevcursor) {
                        Log.d("Threading","background thread")
                        var call = graph.queryGraph(
                            Query.getProductsById(
                                getcategoryID(),
                                cursors,
                                sortKeys,
                                isDirection,
                                number,
                                Constant.internationalPricing(),getFilters()
                            )
                        )

                        call.enqueue { result: GraphCallResult<Storefront.QueryRoot> ->
                            if (result is GraphCallResult.Success<*>) {
                                prevcursor = cursors
                                val output =
                                    (result as GraphCallResult.Success<Storefront.QueryRoot>).response
                                if(output.data?.node!=null)
                                {
                                    if ((output.data?.node as Storefront.Collection).products.edges.size > 0) {
                                        nextcursor = (output.data?.node as Storefront.Collection).products.edges.get((output.data?.node as Storefront.Collection).products.edges.size!! - 1)!!.cursor
                                        edges = (output.data?.node as Storefront.Collection).products.edges
                                        cursor = nextcursor
                                        getallproducts(graph, number, nextcursor)
                                    }
                                }
                                CoroutineScope(Dispatchers.Main).launch {
                                    if(edges!=null){
                                        filterProduct(edges)
                                    }
                                }
                            } else {
                                consumeResponse(GraphQLResponse.error(result as GraphCallResult.Failure))
                            }

                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
//        }
//
//   else
//        {
//            thread?.cancel()
//        }


    }
    fun getallproductsbyhandle(graph: GraphClient, number: Int, cursors: String) {
        thread=  CoroutineScope(Dispatchers.IO).launch {
            try {
                var edges: MutableList<Storefront.ProductEdge>? = null
                if (nextcursor != prevcursor) {
                    Log.d("Threading","background thread")
                    var call = graph.queryGraph(
                        Query.getProductsByHandle(
                            getcategoryHandle(),
                            cursors,
                            sortKeys,
                            isDirection,
                            number,
                            Constant.internationalPricing(),getFilters()
                        )
                    )
                    call.enqueue {
                            result: GraphCallResult<Storefront.QueryRoot> ->
                        prevcursor = cursors
                        if (result is GraphCallResult.Success<*>) {
                            val output = (result as GraphCallResult.Success<Storefront.QueryRoot>).response
                            if ((output.data?.collection)?.products?.edges?.size!! > 0) {
                                nextcursor = (output.data?.collection)?.products?.edges?.get((output.data?.collection)?.products?.edges?.size!! - 1)!!.cursor
                                edges = (output.data?.collection)!!.products.edges
                                CoroutineScope(Dispatchers.Main).launch {
                                    ( output.data!!.collection).also { collectionData.value = it }
                                }

                                cursor = nextcursor
                                getallproductsbyhandle(graph, number, nextcursor)
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                if(edges!=null){
                                    filterProduct(edges)
                                }
                            }
                        } else {
                            consumeResponse(GraphQLResponse.error(result as GraphCallResult.Failure))
                        }

                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun filterProduct(list: List<Storefront.ProductEdge>?) {
        try {
            if (featuresModel.outOfStock!!) {
                disposables.add(repository.getProductList(list!!)
                    .subscribeOn(Schedulers.io())
                    .filter{x->checkNode(node =x.node) }
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result -> filteredproducts.setValue(result) })
            } else {
                disposables.add(repository.getProductList(list!!)
                    .subscribeOn(Schedulers.io())
                    .filter { x -> x.node.availableForSale && checkNode(node =x.node)}
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result -> filteredproducts.setValue(result) })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun checkNode(node:Storefront.Product):Boolean{
        if(node.tags.contains("se_global")){
            return false
        }else{
            return true
        }

    }
    fun setVariants(node:Storefront.Product):Boolean{
        var edges=ArrayList<Storefront.ProductVariantEdge>()
        for (i in 0 until node.variants.edges.size) {
            if(node.variants.edges.get(i).node.availableForSale){
                edges.add(0,node.variants.edges.get(i))
            }else{
                edges.add(node.variants.edges.get(i))
            }
        }
        var variants: Storefront.ProductVariantConnection= Storefront.ProductVariantConnection()
        variants.setEdges(edges)
        node.setVariants(variants)
        return  true
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


    //    fun setWishList(product_id: String): Boolean {
//        val isadded = booleanArrayOf(false)
//        try {
//            val executor = Executors.newSingleThreadExecutor()
//            val callable = Callable {
//                if (repository.getSingleData(product_id) == null) {
//                    Log.i("MageNative", "WishListCount : " + repository.wishListData.size)
//                    val data = ItemData()
//                    data.product_id = product_id
//                    repository.insertWishListData(data)
//                    Log.i("MageNative", "WishListCount 2: " + repository.wishListData.size)
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
    fun AddtoWishVariant(variantId: String) {

        try {
            val runnable = Runnable {
                val data: WishItemData
                if (repository.getSingleVariantData(variantId) == null) {
                    data = WishItemData()
                    data.variant_id = variantId
                    data.selling_plan_id = ""

                    repository.insertWishListVariantData(data)

                }
                Log.i("MageNative", "CartCount : " + repository.allCartItems.size)
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    override fun onCleared() {
        disposables.clear()
    }

   /* private fun getCollectionTags() {
        doRetrofitCall(
            repository.menuCollection(Urls(MyApplication.context).mid, "tags"),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    collectionTags.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    collectionTags.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }*/

    fun getFilterProducts() {
        disposables.add(repository.getCcollectionProductsbyTags(
            Urls(MyApplication.context).mid, categoryHandle, "best-selling", "1", tags_
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result -> filerapiResponseData.setValue(ApiResponse.success(result)) },
                { throwable -> filerapiResponseData.setValue(ApiResponse.error(throwable)) }
            ))
    }

    fun ResponseApiFilterProducts(): MutableLiveData<ApiResponse> {
        getFilterProducts()
        return filerapiResponseData
    }
}
