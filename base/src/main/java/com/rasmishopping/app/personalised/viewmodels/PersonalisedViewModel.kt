package com.rasmishopping.app.personalised.viewmodels

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.personalised.adapters.PersonalisedAdapter
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.GraphQLResponse
import com.rasmishopping.app.utils.Status
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset

class PersonalisedViewModel(var repository: Repository) : ViewModel() {
    var activity: Activity? = null

    fun setPersonalisedData(
        data: JSONArray,
        adapter: PersonalisedAdapter,
        recyler: RecyclerView
    ) {
        try {
            val edges = mutableListOf<Storefront.Product>()
            var runnable = Runnable {
                for (i in 0..data.length() - 1) {
                    getProductById(
                        data.getJSONObject(i).getString("product_id"),
                        adapter,
                        recyler,
                        edges,
                        data
                    )
                }
            }
            Thread(runnable).start()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    fun getProductById(
        id: String,
        adapter: PersonalisedAdapter,
        recyler: RecyclerView,
        edges: MutableList<Storefront.Product>,
        data: JSONArray
    ) {
        try {
            val call =
                repository.graphClient.queryGraph(
                    Query.getProductById(
                        getID(id),
                        Constant.internationalPricing()
                    )
                )
            call.enqueue(Handler(Looper.getMainLooper())) { result ->
                if (result is GraphCallResult.Success<*>) {
                    consumeResponse(
                        GraphQLResponse.success(result as GraphCallResult.Success<*>),
                        adapter,
                        recyler,
                        edges,
                        data
                    )
                } else {
                    consumeResponse(
                        GraphQLResponse.error(result as GraphCallResult.Failure),
                        adapter,
                        recyler,
                        edges,
                        data
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun consumeResponse(
        reponse: GraphQLResponse,
        adapter: PersonalisedAdapter,
        recyler: RecyclerView,
        edges: MutableList<Storefront.Product>,
        data: JSONArray
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
                    // message.setValue(errormessage.toString())
                } else {
                    try {
                        val edge = result.data!!.node as Storefront.Product
                        edges.add(edge)
                        if (edges.size == data.length()) {
                            filterProduct(edges, recyler, adapter)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        when (MyApplication.context.packageName) {
                            "com.rasmishopping.app" -> {
                                //Toast.makeText(MyApplication.context, "Please Provide Visibility to Products and Collections", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
            Status.ERROR -> {
                Log.i("MageNatyive", "ERROR-1" + reponse.error!!.error.message)
            }
        }
    }

    private fun filterProduct(
        list: List<Storefront.Product>,
        productdata: RecyclerView?,
        adapter: PersonalisedAdapter
    ) {
        try {
            repository.getProductListSlider(list)
                .subscribeOn(Schedulers.io())
                .filter { x -> x.availableForSale && checkNode(node =x)}
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<List<Storefront.Product>> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onSuccess(list: List<Storefront.Product>) {
                        var data = ProductView

                        if (!adapter.hasObservers()) {
                            adapter.setHasStableIds(true)
                        }
                        var jsonobject:JSONObject= JSONObject();
                        jsonobject.put("item_shape","rounded")
                        jsonobject.put("item_text_alignment","left")
                        jsonobject.put("item_border","0")
                        jsonobject.put("item_title","1")
                        jsonobject.put("item_price","1")
                        jsonobject.put("item_compare_at_price","1")
                        adapter.setData(  list, activity ?: Activity(),jsonobject, repository)
                        productdata!!.adapter = adapter
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }
                })
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
    fun getID(id: String): String {
        val data = ("gid://shopify/Product/" + id)
        return data
    }
}