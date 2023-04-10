package com.rasmishopping.app.basesection.viewmodels

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.basesection.activities.Splash
import com.rasmishopping.app.basesection.models.FeaturesModel
import com.rasmishopping.app.dbconnection.entities.AppLocalData
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class SplashViewModel(val repository: Repository) : ViewModel() {
    private val disposables = CompositeDisposable()
    val trialdata = MutableLiveData<LocalDbResponse>()
    val apiresponseLiveData = MutableLiveData<ApiResponse>()
    val fireBaseResponseMutableLiveData = MutableLiveData<FireBaseResponse>()
    val errorMessageResponse = MutableLiveData<String>()
    var filteredproducts: MutableLiveData<MutableList<Storefront.ProductEdge>>? =
        MutableLiveData<MutableList<Storefront.ProductEdge>>()
    var appLocalData: AppLocalData = AppLocalData()
    val message = MutableLiveData<String>()
    val version = MutableLiveData<String>()
    var firebaseAnalytics: FirebaseAnalytics
    init {
        firebaseAnalytics = Firebase.analytics
    }
    companion object {
        var viewhashmap: HashMap<String, View> = HashMap<String, View>()
        var featuresModel: FeaturesModel = FeaturesModel()
        var filterfinaldata = HashMap<String, ArrayList<String>>()
        var filterinputformat = HashMap<String, String>()
        var ALIREVIEW_BASEURL: String = "https://alireviews.fireapps.io/"
        var ALIREVIEW_INSTALLSTATUS: String =
            ALIREVIEW_BASEURL + "api/shops/${Urls(MyApplication.context).shopdomain}"
        var ALIREVIEW_PRODUCT: String = ALIREVIEW_BASEURL + "comment/get_review"
    }
    private val TAG = "SplashViewModel"
    val isLogin: Boolean
        get() {
            var loggedin = runBlocking(Dispatchers.IO) {
                return@runBlocking repository.isLogin
            }
            return loggedin
        }

    private fun getCountry() {
        try {
            var call = repository.graphClient.queryGraph(Query.shopDetails)
            call.enqueue { result: GraphCallResult<Storefront.QueryRoot> -> currencyResponse(result) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun invokeProduct(result: GraphCallResult<Storefront.QueryRoot>) {
        if (result is GraphCallResult.Success<*>) {
            consumeProductResponse(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            consumeProductResponse(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
    }

    private fun consumeProductResponse(reponse: GraphQLResponse) {
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
                    Log.i("MageNative", "1" + errormessage)
                    message.setValue(errormessage.toString())
                } else {
                    filterProduct(result.data!!.products.edges)
                }
            }
            Status.ERROR -> {
                Log.i("MageNative", "2" + reponse.error!!.error.message)
                message.setValue(reponse.error.error.message)
            }
            else -> {
            }
        }
    }

    fun filterProduct(list: MutableList<Storefront.ProductEdge>) {
        try {
            disposables.add(repository.getProductList(list)
                .subscribeOn(Schedulers.io())
//                     { x -> x.node.availableForSale }
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result -> filteredproducts!!.value = result })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun currencyResponse(result: GraphCallResult<Storefront.QueryRoot>) {
        if (result is GraphCallResult.Success<*>) {
            consumeResponseCurrency(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            consumeResponseCurrency(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
    }

    private fun consumeResponseCurrency(reponse: GraphQLResponse) {
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
                    message.postValue(errormessage.toString())
                } else {
                    Log.i("SaifDevCountry", "" + result.data?.shop?.paymentSettings?.countryCode!!)
                    Log.i(
                        "SaifDevCountry",
                        "" + result.data?.shop?.paymentSettings?.currencyCode.toString()
                    )
                    // Log.i("SaifDevCountryShip",""+result.data?.shop?.shipsToCountries!!.get(0).toString())
                    MagePrefs.saveCountryCode(result.data?.shop?.paymentSettings?.countryCode!!)
                    MagePrefs.setCurrency(result.data?.shop?.paymentSettings?.currencyCode.toString())
                    saveCurrency(result.data?.shop?.paymentSettings?.countryCode!!.toString())
                    fireBaseResponseMutableLiveData.postValue(FireBaseResponse.success(true))
                }
            }
            Status.ERROR -> message.postValue(reponse.error!!.error.message)
            else -> {
            }
        }
    }

    fun connectFirebaseForTrial(shop: String) {
        try {
            MyApplication.dataBaseReference?.child("additional_info")?.child("validity")
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val value = dataSnapshot.getValue(Boolean::class.java)!!
                        appLocalData.isIstrialexpire = value
                        trialdata.value = LocalDbResponse.success(appLocalData)
                        val runnable = Runnable {
                            Log.i("MageNative:", "TrialExpired$value")
                            Log.i("MageNative:", "LocalData" + repository.localData)
                            repository.updateData(appLocalData)
                        }
                        Thread(runnable).start()
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.i("DBConnectionError", "" + databaseError.details)
                        Log.i("DBConnectionError", "" + databaseError.message)
                        Log.i("DBConnectionError", "" + databaseError.code)
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    fun getVersion() {
        try {
            MyApplication.dataBaseReference?.child("version")
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var value ="v1"
                        if (dataSnapshot.exists()){
                            value = dataSnapshot.getValue(String::class.java)!!
                        }
                        version.value=value
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.i("DBConnectionError", "" + databaseError.details)
                        Log.i("DBConnectionError", "" + databaseError.message)
                        Log.i("DBConnectionError", "" + databaseError.code)
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun connectFireBaseForSplashData() {
        try {
            Log.i("SaifDev", "CountryCode" + MagePrefs.getCountryCode())
            if (MagePrefs.getCountryCode() == null) {
                val runnable = Runnable { getCountry() }
                Thread(runnable).start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun refreshTokenIfRequired() {
        val runnable = Runnable {
            if (repository.accessToken[0].expireTime != null) {
                Log.i("Magenative", "ExpireTime" + repository.accessToken[0].expireTime)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                var expiretime: Date? = null
                try {
                    expiretime = sdf.parse(
                        repository.accessToken[0].expireTime!!.split("t".toRegex())
                            .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                    )
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                val currentDate = Date()
                val diff = expiretime!!.time - currentDate.time
                val seconds = diff / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24
                Log.i("Magenative", "Days$days")
                if (days == 0L) {
                    renewToken(repository.accessToken[0].customerAccessToken)
                }
            }
        }
        Thread(runnable).start()
    }

    private fun renewToken(customerAccessToken: String?) {
        try {
            val call =
                repository.graphClient.mutateGraph(MutationQuery.renewToken(customerAccessToken))
            call.enqueue(Handler(Looper.getMainLooper())) { graphCallResult: GraphCallResult<Storefront.Mutation> ->
                this.invoke(
                    graphCallResult
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private operator fun invoke(graphCallResult: GraphCallResult<Storefront.Mutation>): Unit {
        if (graphCallResult is GraphCallResult.Success<*>) {
            consumeResponse(GraphQLResponse.success(graphCallResult as GraphCallResult.Success<*>))
        } else {
            consumeResponse(GraphQLResponse.error(graphCallResult as GraphCallResult.Failure))
        }
        return
    }

    private fun consumeResponse(reponse: GraphQLResponse) {
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
                    errorMessageResponse.value = errormessage.toString()
                    Log.i("MageNative", "" + errormessage)
                } else {
                    val errors = result.data!!.customerAccessTokenRenew.userErrors
                    if (errors.size > 0) {
                        val iterator = errors.iterator()
                        var err = ""
                        while (iterator.hasNext()) {
                            val error = iterator.next() as Storefront.UserError
                            err += error.message
                        }
                        errorMessageResponse.value = err
                        Log.i("MageNative", "" + err)
                    } else {
                        viewModelScope.launch(Dispatchers.IO) {
                            val token = result.data!!.customerAccessTokenRenew.customerAccessToken
                            val data = repository.accessToken[0]
                            data.customerAccessToken = token.accessToken
                            data.expireTime = token.expiresAt.toString()
                            repository.updateAccessToken(data)
                        }
                    }
                }
            }
            Status.ERROR -> {
                errorMessageResponse.value = reponse.error!!.error.message
                Log.i("MageNative", "" + reponse.error.error.message)
            }
            else -> {
            }
        }
    }

    fun sendTokenToServer(unique_id: String) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.i("MageNative", "token_error : " + task.exception!!)
                    return@OnCompleteListener
                }
                val token = task.result!!
                val params = Bundle()
                params.putString("device_token", token)
                firebaseAnalytics.logEvent("android_custom_log", params)
                Log.i("MageNative", "token$token")
                if (Urls(MyApplication.context).mid.equals("18")) {
                    FirebaseMessaging.getInstance().subscribeToTopic("18")
                } else {
                    FirebaseMessaging.getInstance().subscribeToTopic("magenativeANDROID")
                }
                disposables.add(repository.setDevice(
                    Urls(MyApplication.context).mid,
                    token,
                    MagePrefs.getCustomerEmail() ?: " ",
                    "android",
                    unique_id
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { result -> apiresponseLiveData.setValue(ApiResponse.success(result)) },
                        { throwable -> apiresponseLiveData.setValue(ApiResponse.error(throwable)) }
                    ))
            })
    }

    fun saveCurrency(countryCode: String) {
        appLocalData.currencycode = Constant.getCurrency(countryCode)
        Log.i("COUNTRYCODES", Constant.getCurrency(countryCode))
        if (repository.localData.size == 0) {
            repository.insertData(appLocalData)
        } else {
            repository.updateData(appLocalData)
        }
    }

    fun getDefaultLanguageCode(lang: Splash.CallBack) {
        try {
            Log.i("DebugSaif1", "step1")
            var call = repository.graphClient.queryGraph(Query.CountryCodeDetails)
            call.enqueue { result: GraphCallResult<Storefront.QueryRoot> ->
                invokeLanguageCode(result, lang)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun invokeLanguageCode(
        result: GraphCallResult<Storefront.QueryRoot>,
        lang: Splash.CallBack
    ): Unit {
        Log.i("DebugSaif1", "step2")
        if (result is GraphCallResult.Success<*>) {
            consumedefaultlanguageCodeResponse(
                GraphQLResponse.success(result as GraphCallResult.Success<*>),
                lang
            )
        } else {
            consumedefaultlanguageCodeResponse(
                GraphQLResponse.error(result as GraphCallResult.Failure),
                lang
            )
        }
        return
    }

    private fun consumedefaultlanguageCodeResponse(
        reponse: GraphQLResponse,
        lang: Splash.CallBack
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
                    CoroutineScope(Dispatchers.Main).launch {
                        message.setValue(errormessage.toString())
                    }
                } else {
                    var DefaultLanguage =
                        Objects.requireNonNull<Storefront.QueryRoot>(result.data).localization.language.isoCode.name
                    //MagePrefs.setLanguage(DefaultLanguage)
                    lang.getlang(DefaultLanguage)
                }
            }
            Status.ERROR -> {
                CoroutineScope(Dispatchers.Main).launch {
                    message.setValue(reponse.error!!.error.message)
                }
            }
            else -> {
            }
        }
    }

    fun getProductsById(id: String, context: Context) {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.getProductById(id, Constant.internationalPricing()),
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        var data = result
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


