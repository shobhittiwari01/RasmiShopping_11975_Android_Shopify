package com.rasmishopping.app.basesection.viewmodels

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.BodyNotification
import com.rasmishopping.app.basesection.models.Notification
import com.rasmishopping.app.databinding.MLeftmenufragmentBinding
import com.rasmishopping.app.dbconnection.entities.LivePreviewData
import com.rasmishopping.app.dbconnection.entities.WishItemData
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.network_transaction.doRetrofitCall
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.userprofilesection.models.DeletUserResponse
import com.rasmishopping.app.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Runnable
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class LeftMenuViewModel(var repository: Repository) : ViewModel() {
    private val disposables = CompositeDisposable()
    private val responseLiveData = MutableLiveData<ApiResponse>()
    val message = MutableLiveData<String>()
    val data = MutableLiveData<HashMap<String, String>>()
    private val currencyResponseLiveData = MutableLiveData<List<Storefront.CurrencyCode>>()
    val blogslivedata = MutableLiveData<GraphQLResponse>()
    val menusLiveData = MutableLiveData<GraphQLResponse>()
    private val countrycodeResponseLiveData = MutableLiveData<List<Storefront.Country>>()
    private val languagecodeResponseLiveData = MutableLiveData<List<Storefront.Language>>()
    private val handler = Handler()
    var context: Context? = null
    private var binding: MLeftmenufragmentBinding? = null
    var notifcationpage=1
    var notificationkey="created-send-scheduled"
    var notificationcentreapi=MutableLiveData<ArrayList<Notification>>()
    fun setNotificationPage(notifcationpage: Int){
        this.notifcationpage=notifcationpage
    }
    fun getNotificationPage():Int{
        return notifcationpage
    }
    fun setNotificationKey(notificationkey: String){
        this.notificationkey=notificationkey
    }
    fun getNotificationKey():String{
        return notificationkey
    }
    fun setCurrentBinding(binding: MLeftmenufragmentBinding){
        this.binding=binding
    }
    fun getLeftBinding():MLeftmenufragmentBinding{
        return this.binding!!
    }
    val isLoggedIn: Boolean
        get() {
            var loggedin = runBlocking(Dispatchers.IO) {
                return@runBlocking repository.isLogin
            }
            return loggedin
        }

    fun Response(): MutableLiveData<ApiResponse> {
        getMenus()
        return responseLiveData
    }

    var deleteUserRes :LiveData<DeletUserResponse>
        get() = repository.deleteUserRes
        set(value) {}



    fun MenuResponse(): MutableLiveData<GraphQLResponse> {
        getMenuItems()
        return menusLiveData
    }

    var cartCount: Int = 0
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
    var wishListcount: Int = 0
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

    fun fetchUserData() {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val hashdata = HashMap<String, String>()
                if(repository.isLogin&&repository.allUserData!=null &&repository.allUserData.size>0){
                    val localData = repository.allUserData[0]
                    hashdata.put("firstname", localData.firstname!!)
                    hashdata.put("secondname", localData.lastname!!)
                    hashdata.put("tag", "login")
                    Log.i("MageNative", "LeftMenuResume 2" + localData.firstname!!)
                    Log.i("MageNative", "LeftMenuResume 2" + localData.lastname!!)
                }
                 else {
                    Log.i("MageNative", "LeftMenuResume 2" + "Sign")
                    hashdata["firstname"]  = context?.getString(R.string.sign_first)!!
                    hashdata["secondname"] = context?.getString(R.string.in_last)!!
                    hashdata["tag"] = "Sign In"
                }
                Log.i("MageNative", "LeftMenuResume 2" + "pushed")
                data.postValue(hashdata)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getMenus() {
        try {
            disposables.add(repository.getMenus(
                Urls(MyApplication.context).mid,
                MagePrefs.getLanguage()!!
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result -> responseLiveData.setValue(ApiResponse.success(result)) },
                    { throwable -> responseLiveData.setValue(ApiResponse.error(throwable)) }
                ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun fetchData():HashMap<String,String>{
        var hashdata = HashMap<String, String>()
        try {
            val executor = Executors.newSingleThreadExecutor()
            val callable = Callable {
                if(repository.isLogin&&repository.allUserData!=null &&repository.allUserData.size>0){
                    val localData = repository.allUserData[0]
                    hashdata.put("firstname", localData.firstname!!)
                    hashdata.put("secondname", localData.lastname!!)
                    hashdata.put("tag", "login")
                    Log.i("MageNative", "LeftMenuResume domain" + localData.firstname!!)
                    Log.i("MageNative", "LeftMenuResume domain" + localData.lastname!!)
                }
                else {
                    Log.i("MageNative", "LeftMenuResume 2" + "Sign")
                    hashdata["firstname"]  = context?.getString(R.string.sign_first)!!
                    hashdata["secondname"] = context?.getString(R.string.in_last)!!
                    hashdata["tag"] = "Sign In"
                }
                hashdata
            }
            val future = executor.submit(callable)
            hashdata = future.get()
            executor.shutdown()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.i("MageNative", "LeftMenuResume data" + hashdata)
        return hashdata
    }
    fun currencyResponse(): MutableLiveData<List<Storefront.CurrencyCode>> {
        getCurrency()
        return currencyResponseLiveData
    }

    private fun getCurrency() {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.shopDetails,
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        invoke(result)
                    }
                },
                context = context!!
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /************************************************* International Pricing ***************************************************************/

    fun countryCodeResponse(): MutableLiveData<List<Storefront.Country>> {
        getCountryCode()
        return countrycodeResponseLiveData
    }
    fun languageCodeResponse(): MutableLiveData<List<Storefront.Language>> {
        getLanguageCode()
        return languagecodeResponseLiveData
    }
    private fun getCountryCode() {
        // Toast.makeText(context, "getCountryCode", Toast.LENGTH_SHORT).show()
        try {
            doGraphQLQueryGraph(
                repository,
                Query.CountryCodeDetails,
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        invokeCountryCode(result)
                    }
                },
                context = context!!
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private fun getLanguageCode() {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.CountryCodeDetails,
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        invokeLanguageCode(result)
                    }
                },
                context = context!!
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private fun invokeLanguageCode(result: GraphCallResult<Storefront.QueryRoot>): Unit {
        if (result is GraphCallResult.Success<*>) {
            consumelanguageCodeResponse(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            consumelanguageCodeResponse(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
        return Unit
    }
    private fun consumelanguageCodeResponse(reponse: GraphQLResponse) {
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

                    languagecodeResponseLiveData.setValue(
                        Objects.requireNonNull<Storefront.QueryRoot>(
                            result.data
                        ).localization.availableLanguages
                    )
                }
            }
            Status.ERROR -> message.setValue(reponse.error!!.error.message)
            else -> {
            }
        }
    }


    private fun invokeCountryCode(result: GraphCallResult<Storefront.QueryRoot>): Unit {
        if (result is GraphCallResult.Success<*>) {
            consumeCountryCodeResponse(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            consumeCountryCodeResponse(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
        return Unit
    }

    private fun consumeCountryCodeResponse(reponse: GraphQLResponse) {
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
                    countrycodeResponseLiveData.setValue(
                        Objects.requireNonNull<Storefront.QueryRoot>(
                            result.data
                        ).localization.availableCountries
                    )
                }
            }
            Status.ERROR -> message.setValue(reponse.error!!.error.message)
            else -> {
            }
        }
    }

    /*****************************************************************************************************************************************/

    override fun onCleared() {
        disposables.clear()
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
//                MagePrefs.saveCountryCode()
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
                    currencyResponseLiveData.setValue(
                        Objects.requireNonNull<Storefront.QueryRoot>(
                            result.data
                        ).shop.paymentSettings.enabledPresentmentCurrencies
                    )
                    Objects.requireNonNull<Storefront.QueryRoot>(
                        result.data
                    ).shop.paymentSettings.countryCode
                    Log.d("RegionCode",""+Objects.requireNonNull<Storefront.QueryRoot>(result.data
                    ).shop.paymentSettings.countryCode)

                    Objects.requireNonNull<Storefront.QueryRoot>(
                        result.data
                    ).shop.paymentSettings.cardVaultUrl
                    Log.d("RegionCode",""+Objects.requireNonNull<Storefront.QueryRoot>(result.data
                    ).shop.paymentSettings.cardVaultUrl)
                }
            }
            Status.ERROR -> message.setValue(reponse.error!!.error.message)
            else -> {
            }
        }
    }

    fun setCurrencyData(currencyCode: String?) {
        val runnable = Runnable {
            val appLocalData = repository.localData[0]
            appLocalData.currencycode = currencyCode
            repository.updateData(appLocalData)
        }
        Thread(runnable).start()
    }

    fun logOut() {
        val runnable = Runnable {
            Log.i("MageNative", "LeftMenuResume 5")
            repository.deleteLocalData()
            repository.deletecart()
            repository.deleteWishListData()
            repository.deleteUserData()
            MagePrefs.clearRecent()
            //fetchUserData()
        }
        Thread(runnable).start()
    }

    fun deletLocal() {
        viewModelScope.launch(Dispatchers.IO){
            repository.deleteLocalData()
        }
    }
    fun deleteData(){
        viewModelScope.launch(Dispatchers.IO){
            repository.deleteAllHomePageProduct()
        }
    }
    fun deleteData(product_id: String) {
        try {
            val runnable = Runnable {
                try {
                    val data = repository.getSingleData(product_id)
                    repository.deleteSingleData(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            Thread(runnable).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun isInwishList(product_id: String): Boolean {
        val isadded = booleanArrayOf(false)
        try {
            val executor = Executors.newSingleThreadExecutor()
            val callable = Callable {
                if (repository.getSingleVariantData(product_id) != null) {

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
    fun getMenuItems() {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.getMenuByHandle("main-menu"),
                // Query.getMenuHandleData("main-menu"),
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        invokemenus(result)
                    }
                },
                context = context!!
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun getblogs() {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.queryForShopBlog(),
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        invokeblogs(result)
                    }
                },
                context = context!!
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun invokemenus(result: GraphCallResult<Storefront.QueryRoot>) {
        if (result is GraphCallResult.Success<*>) {
            menusLiveData.setValue(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            menusLiveData.setValue(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
    }

    private fun invokeblogs(result: GraphCallResult<Storefront.QueryRoot>) {
        if (result is GraphCallResult.Success<*>) {
            blogslivedata.setValue(GraphQLResponse.success(result as GraphCallResult.Success<*>))
        } else {
            blogslivedata.setValue(GraphQLResponse.error(result as GraphCallResult.Failure))
        }
    }

    fun setWishList(product_id: String): Boolean {
        val isadded = booleanArrayOf(false)
        try {
            val executor = Executors.newSingleThreadExecutor()
            val callable = Callable {
                if (repository.getSingleVariantData(product_id) == null) {

                    Log.i("MageNative", "WishListCount : " + repository.wishListData.size)
                    val data = WishItemData()
                    data.variant_id = product_id
                    data.selling_plan_id=""
                    repository.insertWishListVariantData(data)
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

    fun insertPreviewData(data: JSONObject) {
        val runnable = Runnable {
            var lpreview = repository.getPreviewData()
            if (lpreview.size == 0) {
                var preview = LivePreviewData(
                    data.getString("mid"),
                    data.getString("shopUrl"),
                    data.getString("token")
                )
                repository.insertPreviewData(preview)
            } else {
                var preview = lpreview.get(0)
                preview.mid = data.getString("mid")
                preview.shopurl = data.getString("shopUrl")
                preview.apikey = data.getString("token")
                repository.updatePreviewData(preview)
            }
        }
        Thread(runnable).start()
    }

    fun NotificationResponse(mid: String): MutableLiveData<ArrayList<Notification>> {
        notificationcentre(mid)
        return notificationcentreapi!!
    }

    fun notificationcentre(mid:String) {
        doRetrofitCall(
            repository.notificationcentre(getNotificationKey(), mid, getNotificationPage()),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    var BodyNotification=Gson().fromJson(result, BodyNotification::class.java)
                    parseNotification(BodyNotification,mid)
                }
                override fun onErrorRetrofit(error: Throwable) {
                    message.value = ApiResponse.error(error).error!!.message
                }
            },
            context = MyApplication.context
        )
    }
    var thread: Job?=null
    fun parseNotification(body: BodyNotification, mid: String){
        try{
            if (body.success!!){
                if(body.data!!.send!!.size>0 ){
                    notificationcentreapi.value= body.data!!.send!!
                    thread = GlobalScope.launch(Dispatchers.IO) {
                        setNotificationPage(getNotificationPage()+1)
                        notificationcentre(mid)
                    }
                }else{
                    notificationcentreapi.value=ArrayList<Notification>()
                }
            }else{
                notificationcentreapi.value= ArrayList<Notification>()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }


}
