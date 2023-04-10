package com.rasmishopping.app.searchsection.viewmodels
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.GraphQLResponse
import com.rasmishopping.app.utils.Status
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
class SearchListModel(val repository: Repository) : ViewModel() {
    private val disposables = CompositeDisposable()
    val message = MutableLiveData<String>()
    val noproduct = MutableLiveData<Boolean>()
    var thread: Job? = null
    var stop: Boolean? = false
    var searchkeyword:String=" "
    fun setKeyword(keyword: String){
        this.searchkeyword=keyword
    }
    fun getKeyword():String{
        return searchkeyword
    }
    var firstdataloaded:Boolean=false
    var productlist=MutableLiveData<ArrayList<Storefront.ProductEdge>>()
    fun getProducts():MutableLiveData<ArrayList<Storefront.ProductEdge>>{
        return productlist
    }
    var sortkey= Storefront.ProductSortKeys.BEST_SELLING
    fun setSort(sortkey:Storefront.ProductSortKeys){
        this.sortkey=sortkey
    }
    fun getSort():Storefront.ProductSortKeys{
        return sortkey
    }
    var direction= true
    fun setReverse(direction:Boolean){
        this.direction=direction
    }
    fun getReverse():Boolean{
        return direction
    }
    fun fetchProducts(cursor: String,number:Int,context:Context){
        try {
            CoroutineScope(Dispatchers.IO).launch {
                doGraphQLQueryGraph(
                    repository,
                    Query.getAllProductsForSearch(
                        cursor,
                        number,
                        Constant.internationalPricing(),
                        getKeyword(),
                        getSort(),
                        getReverse()
                    ),
                    customResponse = object : CustomResponse {
                        override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                            invokeAll(result,context,getKeyword())
                        }
                    },
                    context = context
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun invokeAll(result: GraphCallResult<Storefront.QueryRoot>,context:Context,keyword: String){
        if (result is GraphCallResult.Success<*>) {
            ResponseAll(GraphQLResponse.success(result as GraphCallResult.Success<*>),context,keyword)
        } else {
            ResponseAll(GraphQLResponse.error(result as GraphCallResult.Failure),context,keyword)
        }
    }
    private fun ResponseAll(reponse: GraphQLResponse,context:Context,keyword: String) {
        when (reponse.status) {
            Status.SUCCESS -> {
                val result = (reponse.data as GraphCallResult.Success<Storefront.QueryRoot>).response
                if (result.hasErrors) {
                    val errors = result.errors
                    val iterator = errors.iterator()
                    val errormessage = StringBuilder()
                    var error: Error? = null
                    while (iterator.hasNext()) {
                        error = iterator.next()
                        errormessage.append(error.message())
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        message.value=errormessage.toString()
                    }
                    Log.i("MageNative", "AllProuctsSaif" + errormessage)
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        if(result.data!!.products.edges!=null && result.data!!.products.edges.size>0){
                            filterProduct(result.data!!.products.edges)
                            firstdataloaded=true
                            CoroutineScope(Dispatchers.IO).launch {
                                fetchProducts(result.data!!.products.edges.get(result.data!!.products.edges.size!! - 1)!!.cursor,10,context)
                            }
                        }else{
                            productlist.value= ArrayList<Storefront.ProductEdge>()
                            if(!firstdataloaded){
                                message.value=context.resources.getString(R.string.noresultfound)+context.resources.getString(R.string.for_)+" "+keyword
                                noproduct.value=true
                            }
                        }
                    }
                }
            }
            Status.ERROR -> {
                Log.i("MageNative", "AllProuctsSaif" + reponse.error!!.error.message)
            }
            else -> {
            }
        }
    }
    fun filterProduct(list: MutableList<Storefront.ProductEdge>) {
        try {
            if (SplashViewModel.featuresModel.outOfStock!!) {
                disposables.add(repository.getProductList(list)
                    .subscribeOn(Schedulers.io())
                    .filter({ checkNode(node =it.node)})
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result ->
                        productlist.value= result as ArrayList<Storefront.ProductEdge>?

                    })
            } else {
                disposables.add(repository.getProductList(list)
                    .subscribeOn(Schedulers.io())
                    .filter { it.node.availableForSale && checkNode(node =it.node)}
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { result ->
                        productlist.value= result as ArrayList<Storefront.ProductEdge>?

                    })
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
    fun available(node:Storefront.Product):Boolean{
        var available=true
        if(node.variants.edges[0].node.currentlyNotInStock==false){
            if(node.variants.edges[0].node.quantityAvailable<=0){
                available=false
            }
        }
        return available
    }
}
