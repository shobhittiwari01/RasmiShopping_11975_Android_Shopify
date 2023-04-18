package com.rasmishopping.app.homesection.viewmodels
 import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.webkit.JavascriptInterface
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
 import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
 import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
 import androidx.viewpager.widget.ViewPager.GONE
 import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.android.material.shape.CornerFamily
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.shopify.graphql.support.ID
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.MyApplication.Companion.context
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.activities.Weblink
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.databinding.*
import com.rasmishopping.app.dbconnection.dependecyinjection.Body
import com.rasmishopping.app.dbconnection.dependecyinjection.InnerData
import com.rasmishopping.app.dbconnection.entities.AppLocalData
import com.rasmishopping.app.dbconnection.entities.HomePageProduct
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.homesection.adapters.*
import com.rasmishopping.app.homesection.models.CategoryCircle
import com.rasmishopping.app.homesection.models.MageBanner
import com.rasmishopping.app.homesection.models.ProductSlider
import com.rasmishopping.app.homesection.models.StandAloneBanner
import com.rasmishopping.app.loader_section.CustomLoader
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.network_transaction.doRetrofitCall
import com.rasmishopping.app.productsection.activities.ProductList
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.utils.*
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.m_homepage_modified.*
import kotlinx.android.synthetic.main.m_newbaseactivity.*
import kotlinx.coroutines.*
import me.jessyan.retrofiturlmanager.RetrofitUrlManager
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.timerTask


class HomePageViewModel(var repository: Repository) : ViewModel() {
    val message = MutableLiveData<String>()
    private val disposables = CompositeDisposable()
    var isData = true
    var homejson:String?=null
    var Bottom_Json=JSONObject()
    val homepagedata = MutableLiveData<LinkedHashMap<String, View>>()
    val hasBannerOnTop = MutableLiveData<Boolean>()
    val hasFullSearchOnTop = MutableLiveData<Boolean>()
    val currencyResponseLiveData = MutableLiveData<Storefront.CountryCode>()
    private val TAG = "HomePageViewModel"
    private var customLoader: CustomLoader? = null
    var appname: String? = null
    val notifyPersonalised: MutableLiveData<Boolean> = MutableLiveData<Boolean>()
    var getyotpoauthenticate = MutableLiveData<ApiResponse>()
    var notifyZendesk = MutableLiveData<Boolean>()
    var notifyfeaturesModel = MutableLiveData<Boolean>()
    var appLocalData: AppLocalData = AppLocalData()
    var notifyfeature = MutableLiveData<String>()
    var feedtoken = MutableLiveData<String>()
    var linkedHashMap = LinkedHashMap<String, View>()
    init {
        try {
            if(MyApplication.dataBaseReference!=null){
                MyApplication.dataBaseReference?.child("additional_info")?.child("dark_mode")
                    ?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.getValue(String::class.java) != null) {
                                    SplashViewModel.featuresModel.darkMode = dataSnapshot.getValue(String::class.java)!!
                                    Log.i("FirebaseData_Saif", "Dark_mode"+"${SplashViewModel.featuresModel.darkMode}")
                                    performThemeUpdate(SplashViewModel.featuresModel.darkMode)
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.i("DBConnectionError", "" + databaseError.details)
                            Log.i("DBConnectionError", "" + databaseError.message)
                            Log.i("DBConnectionError", "" + databaseError.code)
                        }
                    })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun initializeHashMap(){
        linkedHashMap=LinkedHashMap<String, View>()
    }
    companion object {
        var count_color: String ="#FFFFFF"
        var count_color_l: String ="#FFFFFF"
        var count_textcolor: String = "#000000"
        var count_textcolor_l: String = "#000000"
        var icon_color: String =  "#FFFFFF"
        var icon_color_l: String =  "#FFFFFF"
        var panel_bg_color: String = "#000000"
        var panel_bg_color_l: String = "#000000"
        var themedContext: Context= context.applicationContext.createConfigurationContext(context.getResources().getConfiguration())
        fun isDarkModeOn():Boolean{
            val nightModeFlags: Int = context.getResources().getConfiguration().uiMode and Configuration.UI_MODE_NIGHT_MASK
            when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> return true
                Configuration.UI_MODE_NIGHT_NO -> return false
                Configuration.UI_MODE_NIGHT_UNDEFINED -> return false
                else ->return false
            }
        }
        fun isLightModeOn():Boolean {
            var lightmode=false
            if (SplashViewModel.featuresModel.darkMode.equals("off") || (SplashViewModel.featuresModel.darkMode.equals("auto") && !isDarkModeOn())) {
                lightmode=true
            }
            return lightmode
        }
         fun performThemeUpdate(data: String?) {
            when (data) {
                "on" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                "off" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                "auto" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
        fun applyDimension(unit: Int, value: Float, metrics: DisplayMetrics): Float {
            when (unit) {
                TypedValue.COMPLEX_UNIT_PX -> return value
                TypedValue.COMPLEX_UNIT_DIP ->             // Just multiplying the value on screen density.
                    return value * metrics.density
                TypedValue.COMPLEX_UNIT_SP -> return value * metrics.scaledDensity
                TypedValue.COMPLEX_UNIT_PT -> return value * metrics.xdpi * (1.0f / 72)
                TypedValue.COMPLEX_UNIT_IN -> return value * metrics.xdpi
                TypedValue.COMPLEX_UNIT_MM -> return value * metrics.xdpi * (1.0f / 25.4f)
            }
            return 0f
        }
        fun getCornerRadius(radius:String):Float{
            var card=0f
            when(radius) {
                "4","8"->{
                    card=radius.toFloat()
                }
                "12"->{
                    card=20f
                }
                "16"->{
                    card=25f
                }
                "20"->{
                    card=40f
                }
            }
            return card
        }
        fun getPlaceHolder(): Drawable {
            var placecolor=NewBaseActivity.themeColor
            var alphaValue = 20
            when(NewBaseActivity.themeColor){
                "#FFFFFF","#00000"->{
                    placecolor="#F2F2F2"
                }
            }
            val originalColor = Color.parseColor(placecolor)
            val lighterColor = ColorUtils.setAlphaComponent(originalColor, alphaValue)
            val unwrappedDrawable: Drawable = AppCompatResources.getDrawable(context, R.drawable.blackplaceholder)!!
            val wrappedDrawable: Drawable = DrawableCompat.wrap(unwrappedDrawable)
            DrawableCompat.setTint(wrappedDrawable, lighterColor)
            return wrappedDrawable
        }
    }
    @Inject
    lateinit var homeadapter: ProductSliderListAdapter
    @Inject
    lateinit var productListAdapter: ProductListSliderAdapter
    @Inject
    lateinit var adapter: CollectionGridAdapter
    @Inject
    lateinit var category_adapter: CategoryCircleAdpater
    @Inject
    lateinit var category_square_adapter: CategorySquareAdapter

    @Inject
    lateinit var slideradapter: CollectionSliderAdapter

    @Inject
    lateinit var gridAdapter: ProductSliderGridAdapter

    lateinit var context: HomePage

    var sortKeys: Storefront.ProductCollectionSortKeys? =
        Storefront.ProductCollectionSortKeys.CREATED
    @Inject
    lateinit var homeadapters: ProductSliderListDynamicAdapter
    fun getHomePageData(): MutableLiveData<LinkedHashMap<String, View>> {
        return homepagedata
    }
    fun getFeatureLiveData(context: HomePage): MutableLiveData<String> {
        Log.i("FirebaseData_Saif:", "integrations->"+"0")
        getFireBaseData(context)
        return notifyfeature
    }
    fun getThemeData(context: HomePage): MutableLiveData<String> {
        getThemeColors(context)
        return notifyfeature
    }
    private fun getThemeColors(context: HomePage) {
        try {
            MyApplication.dataBaseReference?.child("additional_info")?.child("appthemecolor")
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.getValue(String::class.java) != null) {
                                var value = dataSnapshot.getValue(String::class.java)!!
                                if (!value.contains("#")) {
                                    value = "#" + value
                                }
                                NewBaseActivity.themeColor = value
                                MagePrefs.saveThemeColor(NewBaseActivity.themeColor)
                            } else {
                                NewBaseActivity.themeColor = "#000000"
                                MagePrefs.saveThemeColor(NewBaseActivity.themeColor)
                            }
                        } else {
                            NewBaseActivity.themeColor = "#000000"
                        }
                        MyApplication.dataBaseReference?.child("additional_info")
                            ?.child("text_color")
                            ?.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if (dataSnapshot.getValue(String::class.java) != null) {
                                            var value = dataSnapshot.getValue(String::class.java)!!
                                            if (!value.contains("#")) {
                                                value = "#" + value
                                            }
                                            NewBaseActivity.textColor = value
                                        } else {
                                            NewBaseActivity.textColor = "#FFFFFF"
                                        }
                                    } else {
                                        NewBaseActivity.textColor = "#FFFFFF"
                                    }
                                    notifyfeature.value = "themecolor"
                                    Log.i(
                                        "FirebaseData_Saif:",
                                        "TextColor" + NewBaseActivity.textColor
                                    )
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                }
                            })
                        Log.i("FirebaseData_Saif:", "ThemeColor" + NewBaseActivity.themeColor)

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
    fun currencyResponse(context: HomePage): MutableLiveData<Storefront.CountryCode> {
        getCountryCode()
        return currencyResponseLiveData
    }

    fun getToastMessage(): MutableLiveData<String> {
        return message
    }

    var searchicon: Boolean = true
    fun setSearchIcon(searchicon: Boolean) {
        this.searchicon = searchicon
    }

    fun getSearchAsIcon(): Boolean {
        return searchicon
    }

    var wishicon: Boolean = false
    fun setWishIcon(wishicon: Boolean) {
        this.wishicon = wishicon
    }

    fun getWishlistIcon(): Boolean {
        return wishicon
    }

    fun getFireBaseData(context: HomePage) {
        Log.i("FirebaseData_Saif:", "integrations->"+"1")
            this.context = context
            try {
                Log.i("FirebaseData_Saif:", "integrations->"+"2")
                Log.i("FirebaseData_Saif:", "integrations->"+"3"+MyApplication.dataBaseReference!!.key)
                MyApplication.dataBaseReference?.child("integrations")
                    ?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            Log.i("FirebaseData_Saif:", "integrations->"+dataSnapshot.exists())
                            if (dataSnapshot.exists()){
                                integrations(dataSnapshot)
                            }else{
                                getFireBaseDataV1(context)
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.i("DBConnectionError", "" + databaseError.details)
                            Log.i("DBConnectionError", "" + databaseError.message)
                            Log.i("DBConnectionError", "" + databaseError.code)
                        }

                    })

            }catch (e:Exception){
                e.printStackTrace()
            }
    }
    fun getFireBaseDataV1(context: HomePage) {
        Log.i("FirebaseData_Saif:", "integrations->"+"1")
        this.context = context
        try {
            Log.i("FirebaseData_Saif:", "integrations->"+"2")
            Log.i("FirebaseData_Saif:", "integrations->"+"3"+MyApplication.dataBaseReference!!.key)
            MyApplication.getmFirebaseSecondanyInstance().getReference(Urls(MyApplication.context).shopdomain.replace(".myshopify.com", "")).child("integrations")
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.i("FirebaseData_Saif:", "integrations->"+dataSnapshot.exists())
                        if (dataSnapshot.exists()){
                            integrations(dataSnapshot)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.i("DBConnectionError", "" + databaseError.details)
                        Log.i("DBConnectionError", "" + databaseError.message)
                        Log.i("DBConnectionError", "" + databaseError.code)
                    }

                })

        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    fun integrations(dataSnapshot: DataSnapshot){
        if (dataSnapshot.value != null) {
            Constant.initializeIntegrations()
            val integrations = dataSnapshot.value as ArrayList<HashMap<String, String>>
            Log.i("FirebaseData_Saif:", "Integrations" + integrations)
            for (i in 0..integrations.size - 1) {
                var id: String = integrations[i].get("id")!!
                when (id) {

                    "I12" -> {
                        SplashViewModel.featuresModel.yoptoLoyalty = true
                        val inputs =
                            integrations[i].get("inputs") as HashMap<String, String>
                        Urls.X_API_KEY = inputs.get("apiKey")!!
                        Urls.XGUID = inputs.get("guid")!!
                    }
                    "I15" -> {
                        SplashViewModel.featuresModel.algoliasearch = true
                        val inputs =
                            integrations[i].get("inputs") as HashMap<String, String>
                        Urls.AppID = inputs.get("appId")!!
                        Urls.ApiKey = inputs.get("apiKey")!!
                        Urls.IndexName = inputs.get("indexName")!!
                    }
                    "I16" -> {
                        SplashViewModel.featuresModel.returnprime = true
                        SplashViewModel.featuresModel.shipway_order_tracking = true
                        val inputs =
                            integrations[i].get("inputs") as HashMap<String, String>
                        Urls.Channel_id = inputs.get("channelId")!!
                    }
                    "I17" -> {
                        SplashViewModel.featuresModel.Enable_flits_App = true
                        val inputs =
                            integrations[i].get("inputs") as HashMap<String, String>
                        Urls.token = inputs.get("token")!!
                        Urls.user_id = inputs.get("userId")!!
                        if (context.resources.getString(R.string.app_name).contains(" ")) {
                            appname =
                                context.resources.getString(R.string.app_name).replace(" ", "_")
                            Urls.X_Integration_App_Name = appname
                        } else {
                            Urls.X_Integration_App_Name =
                                context.resources.getString(R.string.app_name)
                        }
                    }
                    "I13" -> {
                        SplashViewModel.featuresModel.aliReviews = true
                    }
                    "I01" -> {
                        SplashViewModel.featuresModel.productReview = true
                    }
                    "I03" -> {
                        SplashViewModel.featuresModel.judgemeProductReview = true
                        val inputs =
                            integrations[i].get("inputs") as HashMap<String, String>
                        Urls.JUDGEME_APITOKEN = inputs.get("apiKey")!!
                    }
                    "I04" -> {
                        SplashViewModel.featuresModel.sizeChartVisibility = true
                    }
                    "I06" -> {
                        SplashViewModel.featuresModel.tidioChat = true
                    }
                    "I05" -> {
                        SplashViewModel.featuresModel.zenDeskChat = true
                        val inputs =
                            integrations[i].get("inputs") as HashMap<String, String>
                        Urls.Zendesk_KEY = inputs.get("apiKey")!!
                        notifyZendesk.value = SplashViewModel.featuresModel.zenDeskChat
                    }
                    "I07" -> {
                        SplashViewModel.featuresModel.zapietEnable = true
                    }
                    "I10" -> {
                        SplashViewModel.featuresModel.langify = true
                    }
                    "I09" -> {
                        SplashViewModel.featuresModel.langshop = true
                    }
                    "I08" -> {
                        SplashViewModel.featuresModel.weglot = true
                    }
                    "I14" -> {
                        SplashViewModel.featuresModel.ai_product_reccomendaton = true
                    }
                    "I18"->{
                        /*SplashViewModel.featuresModel.WholeSale_Pricing=true
                        val inputs = integrations[i].get("inputs") as HashMap<String, String>
                        Urls.Authorization=inputs.get("authorization")!!
                        Urls.wholsesaleapikey=inputs.get("wholesaleKey")!!*/

                    }
                    "I19"->{
                        SplashViewModel.featuresModel.shipway_order_tracking = true
                    }
                    "I20"->{
                        SplashViewModel.featuresModel.enablebackInStock = true
                    }
                    "I21" -> {
                        SplashViewModel.featuresModel.enableRewardify = true
//                                            val inputs = integrations[i].get("inputs") as HashMap<String, String>
//                                            Urls.REWARDIFYCLIENTID = inputs.get("client_id")!!
//                                            Urls.REWARDIFYCLIENTSECRET = inputs.get("client_secret")!!
                    }
                    "I100"->{
                        SplashViewModel.featuresModel.liveSale=true
                        val inputs = integrations[i].get("inputs") as HashMap<String, String>
                        Urls.LiveSalePublicKey = inputs.get("public_key")!!
                    }
                    "" -> {
                        if (integrations[i].get("inputs") != null) {
                            var input = integrations[i].get("inputs") as HashMap<String, HashMap<String, HashMap<String, String>>>
                            for (i in 0..input.size - 1) {
                                if(input.containsKey("whatsapp")){
                                    SplashViewModel.featuresModel.whatsappChat = true
                                    Urls.whatsappnumber = input.get("whatsapp")?.get("inputs")?.get("mobile_no")!!
                                    SplashViewModel.featuresModel.fb_wt = true
                                }
                                if(input.containsKey("facebook-chat")){
                                    SplashViewModel.featuresModel.fbMessenger = true
                                    Urls.fbusername = input.get("facebook-chat")?.get("inputs")?.get("user_id")!!
                                    SplashViewModel.featuresModel.fb_wt = true
                                }
                                if(input.containsKey("first_order_discount")){
                                    SplashViewModel.featuresModel.firstSale = true
                                    Urls.firstsalecoupon = input.get("first_order_discount")?.get("inputs")?.get("coupon_code")!!
                                }
                            }
                        }
                        notifyfeaturesModel.value = SplashViewModel.featuresModel.fb_wt
                    }
                }
            }
        }
    }

    //********************************************NEW hompage changes*************************************************//
    internal fun connectFirebaseForHomePageData(context: HomePage, homepage: LinearLayoutCompat) {
        try {
            Log.i("PullToRefresh","4")
            CoroutineScope(Dispatchers.IO).launch {
                MyApplication.dataBaseReference?.child("homepage_component")
                    ?.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            Log.i("FirebaseData_Saif", "2"+dataSnapshot.exists())
                            Log.i("FirebaseData_Saif", "2"+dataSnapshot.getValue())
                            if(dataSnapshot.exists()){
                                if (dataSnapshot.getValue() != null) {
                                    if (dataSnapshot.getValue()!! is String){
                                        val downloadlink = dataSnapshot.getValue()!! as String
                                        Log.i("MageNative", "DownloadLink " + downloadlink)
                                        HomePage.available_language=JSONArray().put(MagePrefs.getLanguage()!!)
                                        Log.i("FirebaseData_Saif", "2")
                                        dowloadJson(downloadlink, context)
                                    }
                                    if(dataSnapshot.getValue() is HashMap<*, *>){
                                        val downloadlink = dataSnapshot.getValue()!! as HashMap<*, *>
                                        val gson = Gson()
                                        val json = gson.toJson(downloadlink)
                                        Log.i("MageNative", "DownloadLink " + json)
                                        var jsonobjecthome=JSONObject(json)
                                        HomePage.available_language=jsonobjecthome.names()
                                        if(jsonobjecthome.has(MagePrefs.getLanguage())){
                                            Log.i("FirebaseData_Saif", "2")
                                            Log.i("PullToRefresh","5")
                                            dowloadJson(jsonobjecthome.getString(MagePrefs.getLanguage()!!), context)
                                        }
                                    }
                                }
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.i("DBConnectionError", "" + databaseError.details)
                            Log.i("DBConnectionError", "" + databaseError.message)
                            Log.i("DBConnectionError", "" + databaseError.code)
                        }
                    })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun dowloadJson(downloadlink: String, context: HomePage) {
        try {
            homejson=downloadlink
            viewModelScope.launch {
                if (Constant.checkInternetConnection(context)){
                    customLoader = CustomLoader(context)
                    try{
                        var result = async(Dispatchers.IO) {
                            URL(downloadlink).readText()
                        }
                        var homedata=result.await()
                        if(!homedata.isNullOrEmpty()){
                            Log.i("PullToRefresh","6")
                            parseResponse(homedata, context)
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }

            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun cachedData(data: String, context: HomePage) {
        try {
            Log.i("PullToRefresh","3")
            parseResponse(data, context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun getCountryCode() {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.shopDetails,
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
//                    message.setValue(errormessage.toString())
                } else {
                   /* Log.i("SaifDevCountryCode", "111222 " + MagePrefs.getDefaultCountryCode().toString())
                    Log.i("SaifDevCountryCode", "111 " + result.data?.shop?.paymentSettings?.countryCode!!)
                    Log.i("SaifDevCountry", "" + result.data?.shop?.paymentSettings?.currencyCode.toString())*/
                    MagePrefs.saveCountryCode(result.data?.shop?.paymentSettings?.countryCode!!)
                    MagePrefs.setCurrency(result.data?.shop?.paymentSettings?.currencyCode.toString())
                    saveCurrency(result.data?.shop?.paymentSettings?.countryCode!!.toString())
//                  MagePrefs.saveCountryCode(Objects.requireNonNull<Storefront.QueryRoot>(result.data).getShop().getPaymentSettings().countryCode)
                }
            }
//            Status.ERROR -> message.setValue(reponse.error!!.error.message)
            else -> {
            }
        }
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
    fun homepageJsonDialog(){
        if(!homejson.isNullOrBlank())
        {
            val dialog = Dialog(context, R.style.WideDialog)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            val binding = DataBindingUtil.inflate<JsonpopupBinding>(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater, R.layout.jsonpopup, null, false)

            binding?.coupontext?.text=homejson.toString().lowercase()
            binding?.copyimage?.setOnClickListener {
                val textToCopy = homejson?.lowercase()
                val clipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("text", textToCopy)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(context, "json copied", Toast.LENGTH_SHORT).show()
            }
            dialog.setContentView(binding.root)
            dialog.show()
            binding.backButton.setOnClickListener {
                dialog.dismiss()
            }

        }
    }
    fun parseResponse(apiResponse: String, context: HomePage) {
        Log.i("PullToRefresh","7")
        MagePrefs.saveHomePageData(apiResponse)
        this.context = context
        if (context.homepage.childCount == 0) {
            try {
                Log.i("PullToRefresh","8")
                var obj = JSONObject(apiResponse)
                var names: JSONArray = obj.getJSONObject("sort_order").names()!!
                for (data in 0..names.length() - 1) {
                    var part = names[data].toString().split("_")
                    var key: String = names[data].toString().replace(part.get(part.size - 1), "")
                    Log.d(TAG, "parseResponse: " + key)
                    when (key) {
                        //ok tested
                        "top-bar_", "top-bar-without-slider_" -> {
                            topbar(obj.getJSONObject(names[data].toString()), names[data].toString())
                        }
                        //ok tested
                        "category-circle_"->{
                            createCircleCategory(  obj.getJSONObject(names[data].toString()),names[data].toString())
                        }
                        //ok tested
                        "category-slider_" -> {
                            createCircleSlider(obj.getJSONObject(names[data].toString()), names[data].toString())
                        }
                        //ok tested
                        "category-card_" -> {
                            createCategoryCard(obj.getJSONObject(names[data].toString()), names[data].toString())
                        }
                        //ok tested
                        "spacer_" -> {
                            Spacer(obj.getJSONObject(names[data].toString()), names[data].toString())
                        }
                        //ok tested
                        "banner-slider_" -> {
                            createBannerSlider(obj.getJSONObject(names[data].toString()), names[data].toString())
                        }
                        //ok tested
                        "product-list-slider_" -> {
                            createProductSlider(obj.getJSONObject(names[data].toString()), true, names[data].toString())
                        }
                        //ok tested
                        "category-square_" -> {
                            createCategorySquare(obj.getJSONObject(names[data].toString()), names[data].toString())
                        }
                        //ok tested
                        "standalone-banner_" -> {
                            createStandAloneBanner(obj.getJSONObject(names[data].toString()), names[data].toString())
                        }
                        //ok tested
                        //product grid layout
                        "fixed-customisable-layout_" -> {
                            createFixedCustomisableLayout(obj.getJSONObject(names[data].toString()), true, names[data].toString())
                        }
                        //ok tested
                        //3-product layout
                        "three-product-hv-layout_" -> {
                            createHvLayout(obj.getJSONObject(names[data].toString()), names[data].toString())
                        }
                        //ok tested
                        //collection grid layout
                        "collection-grid-layout_" -> {
                            createCollectionGrid(obj.getJSONObject(names[data].toString()), names[data].toString())
                        }
                        //ok tested
                        //collection slider
                        "collection-list-slider_" -> {
                            createCollectionListSlider(obj.getJSONObject(names[data].toString()), names[data].toString())
                        }
                        //ok tested
                        "announcement-bar_" -> {
                            createAnnouncementBar(obj.getJSONObject(names[data].toString()), names[data].toString())
                        }

                    }
                }
                homepagedata.value = linkedHashMap
                if (names.length() == context.homepage.childCount) {
                    //   Toast.makeText(context, "complete", Toast.LENGTH_SHORT).show()
                    if (customLoader != null && customLoader?.isShowing == true) {
                        customLoader?.dismiss()
                    }
                    context.main_container.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (customLoader != null && customLoader?.isShowing == true) {
                    customLoader?.dismiss()
                }
            }
        } else {
            Log.i("HomepageConttent_saif", "2-cache")
            try {
                var obj = JSONObject(apiResponse)
                var names: JSONArray = obj.getJSONObject("sort_order").names()!!
                for (data in 0..names.length() - 1) {
                    var part = names[data].toString().split("_")
                    var key: String = names[data].toString().replace(part.get(part.size - 1), "")
                    Log.d(TAG, "parseResponse: " + key)
                    when (key) {
                        "product-list-slider_" -> {
                            createProductSlider(
                                obj.getJSONObject(names[data].toString()),
                                false,
                                names[data].toString()
                            )
                        }
                        "fixed-customisable-layout_" -> {
                            createFixedCustomisableLayout(
                                obj.getJSONObject(names[data].toString()),
                                false,
                                names[data].toString()
                            )
                        }
                        else -> {
                            continue
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
//        homepageJsonDialog()
    }
//    fun setBottomNavigation()
//    {
//         Bottom_Json.put("icon_selected_color","#423878")
//         Bottom_Json.put("icon_default_color","#AB9AED")
//         Bottom_Json.put("icon_background_color","#000000")
//         Bottom_Json.put("text_default_color","#AB9AE0")
//         Bottom_Json.put("text_selected_color","#423B78")
//    }

    private fun topbar(jsonObject: JSONObject, key: String) {
        try {
            var flag = false
            val binding: MTopbarBinding = DataBindingUtil.inflate(context.getSystemService(
                LAYOUT_INFLATER_SERVICE
            ) as LayoutInflater, R.layout.m_topbar, null, false)
            panel_bg_color = JSONObject(jsonObject.getString("panel_background_color")).getString("color")
            panel_bg_color_l = JSONObject(jsonObject.getString("panel_background_color")).getString("color")
            icon_color = JSONObject(jsonObject.getString("icon_color")).getString("color")
            icon_color_l = JSONObject(jsonObject.getString("icon_color")).getString("color")
            count_color = JSONObject(jsonObject.getString("count_color")).getString("color")
            count_color_l = JSONObject(jsonObject.getString("count_color")).getString("color")
            count_textcolor = JSONObject(jsonObject.getString("count_textcolor")).getString("color")
            count_textcolor_l = JSONObject(jsonObject.getString("count_textcolor")).getString("color")
            if (!isLightModeOn()){
                count_color ="#FFFFFF"
                count_textcolor = "#000000"
                icon_color =  "#FFFFFF"
                panel_bg_color = "#000000"
            }
            var searchposition = jsonObject.getString("search_position")
            if (SplashViewModel.featuresModel.in_app_wishlist) {
                when (jsonObject.getString("wishlist")) {
                    "1" -> {
                        setWishIcon(true)
                    }
                    else -> {
                        setWishIcon(false)
                    }
                }
            }
            if (jsonObject.has("logo_image_url")) {
                context.setLogoImage(jsonObject.getString("logo_image_url"))
            }
            context.setPanelBackgroundColor(panel_bg_color)
            binding.root.setBackgroundColor(Color.parseColor(panel_bg_color))
            context.setLogoCenter(false)
            when (searchposition) {
                "middle-width-search" -> {
                    context.toolimage.visibility = View.GONE
                    context.searchsection.visibility = View.VISIBLE
                    context.search.text = jsonObject.getString("search_placeholder")
                    context.search.setOnClickListener { context.moveToSearch(it.context) }
                    if(isLightModeOn()){
                        var draw: GradientDrawable = context.searchsection.background as GradientDrawable
                        draw.setColor(Color.parseColor(JSONObject(jsonObject.getString("search_background_color")).getString("color")))
                        context.search.setTextColor(Color.parseColor(JSONObject(jsonObject.getString("search_text_color")).getString("color")))
                        draw.setStroke(2, Color.parseColor(JSONObject(jsonObject.getString("search_border_color")).getString("color")))
                    }
                    setSearchIcon(false)

                }
                "full-width-search" -> {
                    flag = true
                    context.hideShadow()
                    context.toolimage.visibility = View.VISIBLE
                    context.searchsection.visibility = View.GONE
                    binding.fullsearchsection.visibility = View.VISIBLE
                    binding.fullsearch.text = jsonObject.getString("search_placeholder")
                    binding.fullsearchsection.setOnClickListener { context.moveToSearch(context) }
                    if(isLightModeOn()){
                        var draw: GradientDrawable = binding.fullsearchsection.background as GradientDrawable
                        draw.setColor(Color.parseColor(JSONObject(jsonObject.getString("search_background_color")).getString("color")))
                        binding.fullsearch.setTextColor(Color.parseColor(JSONObject(jsonObject.getString("search_text_color")).getString("color")))
                        draw.setStroke(2, Color.parseColor(JSONObject(jsonObject.getString("search_border_color")).getString("color")))
                    }
                    setSearchIcon(false)
                }
                "middle-logo" -> {
                    setSearchIcon(true)
                    context.toolimage.visibility = View.VISIBLE
                    context.searchsection.visibility = View.GONE
                    binding.fullsearchsection.visibility = View.GONE
                    context.setLogoCenter(true)
                }
                else -> {
                    setSearchIcon(true)
                    context.toolimage.visibility = View.VISIBLE
                    context.searchsection.visibility = View.GONE
                    binding.fullsearchsection.visibility = View.GONE
                }
            }
            context.invalidateOptionsMenu()
            if (flag) {
                linkedHashMap.put(key, binding.root)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun createFixedCustomisableLayout(jsonObject: JSONObject, flag: Boolean, key: String) {
        try {
            var binding: MFixedcustomisableBinding = DataBindingUtil.inflate(
                context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.m_fixedcustomisable,
                null,
                false
            )
            var productSlider = ProductSlider()
            binding.root.visibility=View.GONE
            if(jsonObject.has("linking_type")){
                when(jsonObject.getString("linking_type")){
                    "newest_first"->{
                        updateDataInRecylerViewCatId(binding.productdataFixedcustomisable, jsonObject.getJSONArray("items").getJSONObject(0).getString("link_value"),jsonObject,flag,binding.root)
                    }
                    else->{
                        updateDataInRecylerView(binding.productdataFixedcustomisable, jsonObject.getJSONArray("items").getJSONObject(0).getJSONArray("product_value"),  jsonObject,flag,binding.root)
                    }
                }
            }else{
                updateDataInRecylerView(binding.productdataFixedcustomisable, jsonObject.getJSONArray("items").getJSONObject(0).getJSONArray("product_value"),  jsonObject,flag,binding.root)
            }

            if (flag){
                if (jsonObject.getString("header").equals("1")) {
                    binding.headerSection.visibility = View.VISIBLE
                    productSlider.headertext = jsonObject.getString("header_title_text")
                    //Constant.translateField(productSlider.headertext!!, binding.headertext)
                    if(isLightModeOn()){
                        var header_background_color = JSONObject(jsonObject.getString("header_background_color")).getString("color")
                        binding.headerSection.setBackgroundColor(Color.parseColor(header_background_color))
                        var header_title_color = JSONObject(jsonObject.getString("header_title_color")).getString("color")
                        binding.headertext.setTextColor(Color.parseColor(header_title_color))
                    }
                    when (jsonObject.getString("header_title_font_weight")) {
                        "bold" -> {
                            binding.headertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        }
                        "light" -> {
                            binding.headertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        }
                        "medium" -> {
                            binding.headertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        }
                    }
                    if (jsonObject.getString("item_header_font_style").equals("italic")) {
                        binding.headertext.setTypeface(binding.headertext.typeface, Typeface.ITALIC)
                    }
                    if (jsonObject.getString("header_subtitle").equals("1")) {
                        binding.subheadertext.visibility = View.VISIBLE
                        productSlider.subheadertext = jsonObject.getString("header_subtitle_text")
                       /* Constant.translateField(
                            productSlider.subheadertext!!,
                            binding.subheadertext
                        )*/
                        if(isLightModeOn()){
                            var header_subtitle_color = JSONObject(jsonObject.getString("header_subtitle_color"))
                            binding.subheadertext.setTextColor(Color.parseColor(header_subtitle_color.getString("color")))
                        }
                        when (jsonObject.getString("header_subtitle_font_weight")) {
                            "bold" -> {
                                binding.subheadertext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                            }
                            "light" -> {
                                binding.subheadertext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                            }
                            "medium" -> {
                                binding.subheadertext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                            }
                        }
                        if (jsonObject.getString("header_subtitle_title_font_style").equals("italic")) {
                            binding.subheadertext.setTypeface(binding.subheadertext.typeface, Typeface.ITALIC)
                        }
                    }else{
                        (binding.headertext.layoutParams as ConstraintLayout.LayoutParams).bottomToBottom = PARENT_ID
                    }
                    if (jsonObject.getString("header_action").equals("1")) {
                        binding.actiontext.visibility = View.VISIBLE
                        productSlider.action_id = jsonObject.getJSONArray("items").getJSONObject(0).getString("link_value")
                        productSlider.actiontext = jsonObject.getString("header_action_text")
                       // Constant.translateField(productSlider.actiontext!!, binding.actiontext)
                        if(isLightModeOn()){
                            var header_action_color = JSONObject(jsonObject.getString("header_action_color"))
                            var header_action_background_color = JSONObject(jsonObject.getString("header_action_background_color"))
                            binding.actiontext.setTextColor(Color.parseColor(header_action_color.getString("color")))
                            var gradientDrawable = binding.actiontext.background as GradientDrawable
                            gradientDrawable.setStroke(2, Color.parseColor(header_action_background_color.getString("color")))
                            gradientDrawable.setColor(Color.parseColor(header_action_background_color.getString("color")))
                        }
                        //binding.actiontext.setBackgroundColor(Color.parseColor(header_action_background_color.getString("color")))
                        when (jsonObject.getString("header_action_font_weight")) {
                            "bold" -> {
                                binding.actiontext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                            }
                            "light" -> {
                                binding.actiontext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                            }
                            "medium" -> {
                                binding.actiontext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                            }
                        }
                        if (jsonObject.getString("header_action_title_font_style").equals("italic")) {
                            binding.actiontext.setTypeface(binding.actiontext.typeface, Typeface.ITALIC)
                        }
                    }
                    if (jsonObject.getString("header_deal").equals("1")) {
                        binding.dealsection.deallayout.visibility = View.VISIBLE
                        productSlider.timertextmessage = jsonObject.getString("item_deal_message")
                        if(isLightModeOn()){
                            var header_deal_color = JSONObject(jsonObject.getString("header_deal_color"))
                            binding.dealsection.timerMessage.setTextColor(Color.parseColor(header_deal_color.getString("color")))
                        }
                        var DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"
                        var sdf = SimpleDateFormat(DATE_FORMAT)
                        //  sdf.timeZone = TimeZone.getTimeZone("UTC")
                        var item_deal_start_date = sdf.format(Date())
                        Log.i("MageNative", "item_deal_start_date " + item_deal_start_date)
                        var item_deal_end_date = jsonObject.getString("item_deal_end_date")
                        Log.i("MageNative", "item_deal_end_date " + item_deal_end_date)
                        var startdate: Date?
                        var enddate: Date?
                        try {
                            startdate = sdf.parse(item_deal_start_date)
                            enddate = sdf.parse(item_deal_end_date)
                            var oldLong = startdate.time
                            var NewLong = enddate.time
                            var diff = NewLong - oldLong
                            Log.i("MageNative", "Long" + diff)
                            if (diff > 0) {
                                var counter = MyCount(diff, 1000, productSlider, ":")
                                counter.start()
                            } else {
                                productSlider.timericon = View.GONE
                                binding.dealsection.timer.visibility = View.GONE
                                binding.dealsection.deallayout.visibility = View.GONE
                            }
                        } catch (ex: ParseException) {
                            ex.printStackTrace()
                        }
                    }
                }
                if(isLightModeOn()){
                    var panel_background_color = JSONObject(jsonObject.getString("panel_background_color")).getString("color")
                    binding.panelbackgroundcolor.setBackgroundColor(Color.parseColor(panel_background_color))
                    binding.root.setBackgroundColor(Color.parseColor(panel_background_color))
                }
                binding.productslider = productSlider
                linkedHashMap.put(key, binding.root)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun createCollectionListSlider(jsonObject: JSONObject, key: String) {
        try {
            val binding: MCollectionsliderBinding = DataBindingUtil.inflate(
                context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.m_collectionslider,
                null,
                false
            )
            val productSlider = ProductSlider()
            if (jsonObject.getString("header").equals("1")) {
                binding.headerSection.visibility = View.VISIBLE
                productSlider.headertext = jsonObject.getString("header_title_text")
                //Constant.translateField(productSlider.headertext!!, binding.headertext)
                if(isLightModeOn()){
                    var header_background_color = JSONObject(jsonObject.getString("header_background_color")).getString("color")
                    binding.headerSection.setBackgroundColor(Color.parseColor(header_background_color))
                    var header_title_color = JSONObject(jsonObject.getString("header_title_color"))
                    binding.headertext.setTextColor(Color.parseColor(header_title_color.getString("color")))
                }
                when (jsonObject.getString("header_title_font_weight")) {
                    "bold" -> {
                        binding.headertext.typeface = Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                    }
                    "light" -> {
                        binding.headertext.typeface = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "medium" -> {
                        binding.headertext.typeface = Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                    }
                }
                if (jsonObject.getString("header_title_font_style").equals("italic")) {
                    binding.headertext.setTypeface(binding.headertext.typeface, Typeface.ITALIC)
                }
                if (jsonObject.getString("header_subtitle").equals("1")) {
                    binding.subheadertext.visibility = View.VISIBLE
                    productSlider.subheadertext = jsonObject.getString("header_subtitle_text")
                    //Constant.translateField(productSlider.subheadertext!!, binding.subheadertext)
                    if(isLightModeOn()){
                        var header_subtitle_color = JSONObject(jsonObject.getString("header_subtitle_color"))
                        binding.subheadertext.setTextColor(Color.parseColor(header_subtitle_color.getString("color")))
                    }
                    when (jsonObject.getString("header_subtitle_font_weight")) {
                        "bold" -> {
                            binding.subheadertext.typeface = Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        }
                        "light" -> {
                            binding.subheadertext.typeface = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        }
                        "medium" -> {
                            binding.subheadertext.typeface = Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        }
                    }
                    if (jsonObject.getString("header_subtitle_font_style").equals("italic")) {
                        binding.subheadertext.setTypeface(binding.subheadertext.typeface, Typeface.ITALIC)
                    }
                }
            }
            if(isLightModeOn()){
                var panel_background_color = JSONObject(jsonObject.getString("panel_background_color")).getString("color")
                binding.panelbackgroundcolor.setBackgroundColor(Color.parseColor(panel_background_color))
                binding.root.setBackgroundColor(Color.parseColor(panel_background_color))
            }
            context.setLayout(binding.productdataCollectionslider, "horizontal")
            slideradapter = CollectionSliderAdapter()
            slideradapter.setData(jsonObject.getJSONArray("items"), context, jsonObject)
            binding.productdataCollectionslider.adapter = slideradapter
            slideradapter.notifyDataSetChanged()
            binding.productslider = productSlider
            linkedHashMap.put(key, binding.root)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    private fun createHvLayout(jsonObject: JSONObject, key: String) {
        try {
            var binding: MProductHvLayoutBinding = DataBindingUtil.inflate(
                context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.m_product_hv_layout,
                null,
                false
            )
            var productSlider = ProductSlider()
            if (jsonObject.getString("header").equals("1")) {
                binding.headerSection.visibility = View.VISIBLE
                productSlider.headertext = jsonObject.getString("header_title_text")
               // Constant.translateField(productSlider.headertext!!, binding.headertext)
                if(isLightModeOn()){
                    var header_background_color = JSONObject(jsonObject.getString("header_background_color"))
                    binding.headerSection.setBackgroundColor(Color.parseColor(header_background_color.getString("color")))
                    var header_title_color = JSONObject(jsonObject.getString("header_title_color"))
                    binding.headertext.setTextColor(Color.parseColor(header_title_color.getString("color")))
                }
                when (jsonObject.getString("header_title_font_weight")) {
                    "bold" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                    }
                    "light" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "medium" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                    }
                }
                if (jsonObject.getString("header_title_font_style").equals("italic")) {
                    binding.headertext.setTypeface(binding.headertext.typeface, Typeface.ITALIC)
                }
                if (jsonObject.getString("header_subtitle").equals("1")) {
                    binding.subheadertext.visibility = View.VISIBLE
                    productSlider.subheadertext = jsonObject.getString("header_subtitle_text")
                    //Constant.translateField(productSlider.subheadertext!!, binding.subheadertext)
                    if(isLightModeOn()){
                        var header_subtitle_color = JSONObject(jsonObject.getString("header_subtitle_color"))
                        binding.subheadertext.setTextColor(Color.parseColor(header_subtitle_color.getString("color")))
                    }
                    when (jsonObject.getString("header_subtitle_font_weight")) {
                        "bold" -> {
                            binding.subheadertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        }
                        "light" -> {
                            binding.subheadertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        }
                        "medium" -> {
                            binding.subheadertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        }
                    }
                    if (jsonObject.getString("header_subtitle_font_style").equals("italic")) {
                        binding.subheadertext.setTypeface(
                            binding.subheadertext.typeface,
                            Typeface.ITALIC
                        )
                    }
                }
                if (jsonObject.getString("header_deal").equals("1")) {
                    binding.dealsection.deallayout.visibility = View.VISIBLE
                    productSlider.timertextmessage = jsonObject.getString("item_deal_message")
                    if(isLightModeOn()){
                        var header_deal_color = JSONObject(jsonObject.getString("header_deal_color"))
                        binding.dealsection.timerMessage.setTextColor(Color.parseColor(header_deal_color.getString("color")))
                    }
                    var DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"
                    var sdf = SimpleDateFormat(DATE_FORMAT)
                    // sdf.timeZone = TimeZone.getTimeZone("UTC")
                    var item_deal_start_date = sdf.format(Date())
                    Log.i("MageNative", "item_deal_start_date " + item_deal_start_date)
                    var item_deal_end_date = jsonObject.getString("item_deal_end_date")
                    Log.i("MageNative", "item_deal_end_date " + item_deal_end_date)
                    var startdate: Date?
                    var enddate: Date?
                    try {
                        startdate = sdf.parse(item_deal_start_date)
                        enddate = sdf.parse(item_deal_end_date)
                        var oldLong = startdate.time
                        var NewLong = enddate.time
                        var diff = NewLong - oldLong
                        Log.i("MageNative", "Long" + diff)
                        if (diff > 0) {
                            var counter = MyCount(diff, 1000, productSlider, ":")
                            counter.start()
                        } else {
                            productSlider.timericon = View.GONE
                            binding.dealsection.timer.visibility = View.GONE
                            binding.dealsection.deallayout.visibility = View.GONE
                        }
                    } catch (ex: ParseException) {
                        ex.printStackTrace()
                    }
                }
            }
            if(isLightModeOn()){
                var panel_background_color = JSONObject(jsonObject.getString("panel_background_color")).getString("color")
                binding.panelbackgroundcolor.setBackgroundColor(Color.parseColor(panel_background_color))
                binding.root.setBackgroundColor(Color.parseColor(panel_background_color))
            }
            when (jsonObject.getString("item_text_alignment")) {
                "left" -> {
                    binding.hvnameone.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    binding.hvnametwo.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    binding.hvnamethree.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                }
                "right" -> {
                    binding.hvnameone.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    binding.hvnametwo.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    binding.hvnamethree.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                }
            }

            when (jsonObject.getString("item_shape")) {
                "square" -> {
                    //first product
                    binding.cardone.radius = 0f
                    binding.cardone.useCompatPadding = false
                    //second product
                    binding.cardtwo.radius = 0f
                    binding.cardtwo.useCompatPadding = false
                    //third product
                    binding.cardthree.radius = 0f
                    binding.cardthree.useCompatPadding = false
                }
            }

            when (jsonObject.getString("item_border")) {
                "1" -> {
                    if(isLightModeOn()){
                        var background = JSONObject(jsonObject.getString("item_border_color"))
                        binding.cardone.setCardBackgroundColor(Color.parseColor(background.getString("color")))
                        binding.cardtwo.setCardBackgroundColor(Color.parseColor(background.getString("color")))
                        binding.cardthree.setCardBackgroundColor(Color.parseColor(background.getString("color")))
                    }
                    var params = binding.hvimagOne.layoutParams as FrameLayout.LayoutParams
                    params.setMargins(3, 3, 3, 3)
                    binding.hvimagOne.layoutParams = params
                    binding.hvimagtwo.layoutParams = params
                    binding.hvimagthree.layoutParams = params

                }
            }
            var face: Typeface? = null
            when (jsonObject.getString("item_title_font_weight")) {
                "bold" -> {
                    face = Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                }
                "light" -> {
                    face = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                }
                "medium" -> {
                    face = Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                }
            }
            binding.hvnameone.typeface = face
            binding.hvnametwo.typeface = face
            binding.hvnamethree.typeface = face
            when (jsonObject.getString("item_title_font_style")) {
                "italic" -> {
                    binding.hvnameone.setTypeface(binding.hvnameone.typeface, Typeface.ITALIC)
                    binding.hvnametwo.setTypeface(binding.hvnametwo.typeface, Typeface.ITALIC)
                    binding.hvnamethree.setTypeface(binding.hvnamethree.typeface, Typeface.ITALIC)
                }
            }
            /**************first hv***************************/
            productSlider.hvimageone =
                jsonObject.getJSONArray("items").getJSONObject(0).getString("image_url")
            // productSlider.hvimageone="https://images.unsplash.com/photo-1580748141549-71748dbe0bdc?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=334&q=80"
            productSlider.hvnameone =
                jsonObject.getJSONArray("items").getJSONObject(0).getString("title")
            productSlider.hvtypeone =
                jsonObject.getJSONArray("items").getJSONObject(0).getString("link_type")
            productSlider.hvvalueone =
                jsonObject.getJSONArray("items").getJSONObject(0).getString("link_value")
            Constant.translateField(productSlider.hvnameone!!, binding.hvnameone)
            /**************second hv***************************/
            productSlider.hvimagetwo =
                jsonObject.getJSONArray("items").getJSONObject(1).getString("image_url")
            //productSlider.hvimagetwo="https://images.unsplash.com/photo-1570589107939-54ebe3183842?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=719&q=80"
            productSlider.hvnametwo =
                jsonObject.getJSONArray("items").getJSONObject(1).getString("title")
            productSlider.hvtypetwo =
                jsonObject.getJSONArray("items").getJSONObject(1).getString("link_type")
            productSlider.hvvaluetwo =
                jsonObject.getJSONArray("items").getJSONObject(1).getString("link_value")
            Constant.translateField(productSlider.hvnametwo!!, binding.hvnametwo)
            /**************third hv***************************/
            productSlider.hvimagethree =
                jsonObject.getJSONArray("items").getJSONObject(2).getString("image_url")
            //productSlider.hvimagethree="https://images.unsplash.com/photo-1570589107939-54ebe3183842?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=719&q=80"
            productSlider.hvnamethree =
                jsonObject.getJSONArray("items").getJSONObject(2).getString("title")
            productSlider.hvtypethree =
                jsonObject.getJSONArray("items").getJSONObject(2).getString("link_type")
            productSlider.hvvaluethree =
                jsonObject.getJSONArray("items").getJSONObject(2).getString("link_value")
            Constant.translateField(productSlider.hvnamethree!!, binding.hvnamethree)
            binding.productslider = productSlider
            linkedHashMap.put(key, binding.root)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /***************************************************** Announcement Bar Settings ********************************************/

    private fun createAnnouncementBar(jsonObject: JSONObject, key: String) {
        try {
            val announcementbinding: MAnnouncementbarBinding = DataBindingUtil.inflate(
                context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.m_announcementbar,
                null,
                false
            )
            var common=CommanModel()
            when (jsonObject.getString("bar_type")) {
                "image" -> {
                    announcementbinding.annnouncementimage.visibility = View.VISIBLE
                    announcementbinding.web.visibility = View.GONE
                    announcementbinding.webdata.visibility = View.GONE
                    common.imageurl=jsonObject.getString("image_link")
                    announcementbinding.annnouncementimage.setOnClickListener {
                        click(jsonObject.getString("image_link_type"),jsonObject.getString("image_link_value"))
                    }
                }
                "text" -> {
                    announcementbinding.webdata.setOnClickListener {
                        Log.i("ClickAnnouctment","True")
                        click(jsonObject.getString("text_link_type"),jsonObject.getString("text_link_value"))
                    }
                    common.imageurl=" "
                    announcementbinding.annnouncementimage.visibility = View.GONE
                    announcementbinding.web.visibility = View.VISIBLE
                    val background = JSONObject(jsonObject.getString("background_color"))
                    announcementbinding.web.setBackgroundColor(Color.parseColor(background.getString("color")))
                    val dynamicText = jsonObject.getString("bar_text")
                    val textcolor = JSONObject(jsonObject.getString("text_color"))
                    val dynamictextcolor = textcolor.getString("color")
                    val textAlignment = jsonObject.getString("bar_text_alignment")
                    var html=""
                    when (jsonObject.getString("bar_text_marquee")) {
                        "0" -> {
                            html = "<!DOCTYPE html><html><body><p style='margin: auto; width: 100%; text-align: ${textAlignment}; color: ${dynamictextcolor};'>${dynamicText}</p></body></html>"
                        }
                        "1" -> {
                            val scrollAmount = Integer.parseInt(jsonObject.getString("marquee_text_speed"))*2
                            val marqueeDirection = jsonObject.getString("marquee_text_direction")
                            val direction: String = if (marqueeDirection == "rtl") "left" else "right"
                            html = """
                               <!DOCTYPE html><html><body>
                               <marquee scrollamount=${scrollAmount} style="margin: auto; width: 100%;color:${dynamictextcolor};" direction=${direction}>${dynamicText}</marquee></body></html>
                           """.trimIndent()
                        }
                    }
                    announcementbinding.web.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                }
            }
            announcementbinding!!.image=common
            linkedHashMap.put(key, announcementbinding.root)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun click(type:String,value:String){
        when(type){
            "collections" -> {
                val collection = "gid://shopify/Collection/" + value
                val intent = Intent(context, ProductList::class.java)
                intent.putExtra("ID", (collection))
                intent.putExtra("tittle", " ")
                context.startActivity(intent)
                Constant.activityTransition(context)
            }
            "products" -> {
                val product = "gid://shopify/Product/" + value
                val prod_link = Intent(context, ProductView::class.java)
                prod_link.putExtra("ID", (product))
                context.startActivity(prod_link)
                Constant.activityTransition(context)
            }
            "web_url" -> {
                if (value.trim() != "#") {
                    val weblink = Intent(context, Weblink::class.java)
                    weblink.putExtra("link", value.trim())
                    weblink.putExtra("name", " ")
                    context.startActivity(weblink)
                    Constant.activityTransition(context)
                }
            }
        }
    }
    private fun createStandAloneBanner(jsonObject: JSONObject, key: String) {
        var binding: MStandlonebannerBinding = DataBindingUtil.inflate(
            context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            R.layout.m_standlonebanner,
            null,
            false
        )
        var stand = StandAloneBanner()
        if (jsonObject.has("item_image_size")) {
            when (jsonObject.getString("item_image_size")) {
                "half" -> {
                    (binding.image.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,70:6"
                     binding.buttonOne.textSize=10f
                     binding.buttonTwo.textSize=10f
                }
                "2x" -> {
                    (binding.image.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,7:5"
                }
                "3x" -> {
                    (binding.image.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,7:9"
                }
            }
        }
        Log.i("standalonebannerimage", jsonObject.getString("banner_url"))
        stand.image = jsonObject.getString("banner_url")
        if (jsonObject.has("item_button_position")) {
            when (jsonObject.getString("item_button_position")) {
                "no-btn" -> {
                    binding.buttonsection.visibility = View.GONE
                    stand.bannerlink = jsonObject.getString("banner_link_value")
                    stand.bannertype = jsonObject.getString("banner_link_type")
                    binding.buttonsection.tag = "no-btn"
                }
                "bottom" -> {
                   var params = (binding.buttonsection.layoutParams as ConstraintLayout.LayoutParams)
                    params.topToTop=UNSET
                    binding.buttonsection.layoutParams=params
                    binding.image.setOnClickListener(null)
                    binding.buttonsection.tag = "bottom"
                }
                "middle" -> {
                    binding.image.setOnClickListener(null)
                    binding.buttonsection.tag = "middle"
                }
            }
        }
        when (binding.buttonsection.tag) {
            "bottom", "middle" -> {
                when (jsonObject.getString("item_text_alignment")) {
                    "left" -> {
                        binding.buttonOne.gravity = Gravity.CENTER_VERTICAL or Gravity.START
                        binding.buttonTwo.gravity = Gravity.CENTER_VERTICAL or Gravity.START
                    }
                    "right" -> {
                        binding.buttonOne.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                        binding.buttonTwo.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    }
                }
                if (jsonObject.has("first_button_text")) {
                    stand.text_one = jsonObject.getString("first_button_text")
                    stand.text_two = jsonObject.getString("second_button_text")
                }
                if(isLightModeOn()){
                    var background = JSONObject(jsonObject.getString("button_background_color"))
                    var button_text_color = JSONObject(jsonObject.getString("button_text_color"))
                    binding.buttonOne.setBackgroundColor(Color.parseColor(background.getString("color")))
                    binding.buttonTwo.setBackgroundColor(Color.parseColor(background.getString("color")))
                    binding.buttonOne.setTextColor(Color.parseColor(button_text_color.getString("color")))
                    binding.buttonTwo.setTextColor(Color.parseColor(button_text_color.getString("color")))
                }
                when (jsonObject.getString("item_font_weight")) {
                    "light" -> {
                        binding.buttonOne.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        binding.buttonTwo.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "medium" -> {
                        binding.buttonOne.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        binding.buttonTwo.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                    }
                    "bold" -> {
                        binding.buttonOne.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        binding.buttonTwo.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                    }
                }
                when (jsonObject.getString("item_font_style")) {
                    "italic" -> {
                        binding.buttonOne.setTypeface(binding.buttonOne.typeface, Typeface.ITALIC)
                        binding.buttonTwo.setTypeface(binding.buttonTwo.typeface, Typeface.ITALIC)
                    }
                }
                stand.buttononetype = jsonObject.getString("first_button_link_type")
                stand.buttononelink = jsonObject.getString("first_button_link_value")
                stand.buttontwotype = jsonObject.getString("second_button_link_type")
                stand.buttontwolink = jsonObject.getString("second_button_link_value")
            }
        }
        binding.stand = stand
        linkedHashMap.put(key, binding.root)
    }

    private fun Spacer(jsonObject: JSONObject, key: String) {
        val spacerbinding: SpacerBinding = DataBindingUtil.inflate(
            context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            R.layout.spacer,
            null,
            false
        )
        val height = jsonObject.getString("height")
        if(jsonObject.has("background_color")) {
            var cell_background_color = JSONObject(jsonObject.getString("background_color"))
            spacerbinding.spacer.setBackgroundColor(Color.parseColor(cell_background_color.getString("color")))
        }
        spacerbinding.spacer.layoutParams.height = height.toInt() * 4
        linkedHashMap.put(key, spacerbinding.root)
    }

    private fun createCircleCategory(jsonObject: JSONObject, key: String) {
        try {
            val categoryitem: CircleCategorySliderBinding = DataBindingUtil.inflate(
                context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.circle_category_slider,
                null,
                false
            )
            repository.getJSonArray(JsonParser().parse(jsonObject.getString("items")).asJsonArray)
                .subscribeOn(Schedulers.io())
                .filter { x -> x.asJsonObject.get("link_type").asString.isNotEmpty() }
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<List<JsonElement>> {
                    override fun onSubscribe(d: Disposable) {}
                    override fun onSuccess(list: List<JsonElement>) {
                        val collection = CategoryCircle()
                        if (jsonObject.getString("item_title").equals("1")) {
                            collection.titlevisible = true
                        }
                        var item_border="#FFFFFF"
                        if (jsonObject.getString("item_border").equals("1")) {
                            if(isLightModeOn()){
                                var item_border_color = JSONObject(jsonObject.getString("item_border_color"))
                                item_border = item_border_color.getString("color")
                            }
                        } else {
                            if(isLightModeOn()){
                                var cell_background_color = JSONObject(jsonObject.getString("panel_background_color"))
                                item_border = cell_background_color.getString("color")
                            }
                        }
                        collection.bordercolor = item_border
                        var tittlecolor = JSONObject(jsonObject.getString("item_title_color")).getString("color")
                        var face = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        when (jsonObject.getString("item_font_weight")) {
                            "medium" -> { face = Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf") }
                            "light" -> { face = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf") }
                            "bold" -> { face = Typeface.createFromAsset(context.assets, "fonts/popbold.ttf") }
                        }
                        if (list.get(0).asJsonObject.has("title")) {
                            collection.cat_text_one = list.get(0).asJsonObject.get("title").asString
                            Constant.translateField(collection.cat_text_one!!, categoryitem.catTextOne)
                            collection.cat_text_two = list.get(1).asJsonObject.get("title").asString
                            Constant.translateField(
                                collection.cat_text_two!!,
                                categoryitem.catTextTwo
                            )
                            collection.cat_text_three =
                                list.get(2).asJsonObject.get("title").asString
                            Constant.translateField(
                                collection.cat_text_three!!,
                                categoryitem.catTextThree
                            )
                            collection.cat_text_four =
                                list.get(3).asJsonObject.get("title").asString
                            Constant.translateField(
                                collection.cat_text_four!!,
                                categoryitem.catTextFour
                            )
                            collection.cat_text_five =
                                list.get(4).asJsonObject.get("title").asString
                            Constant.translateField(
                                collection.cat_text_five!!,
                                categoryitem.catTextFive
                            )
                        }
                        if (list.get(0).asJsonObject.has("image_url")) {
                            collection.cat_image_one =
                                list.get(0).asJsonObject?.get("image_url")?.asString
                            collection.cat_image_two =
                                list.get(1).asJsonObject?.get("image_url")?.asString
                            collection.cat_image_three =
                                list.get(2).asJsonObject?.get("image_url")?.asString
                            collection.cat_image_four =
                                list.get(3).asJsonObject?.get("image_url")?.asString
                            collection.cat_image_five =
                                list.get(4).asJsonObject?.get("image_url")?.asString
                        }
                        if (list.get(0).asJsonObject.has("link_type")) {
                            collection.cat_link_one =
                                list.get(0).asJsonObject.get("link_type").asString
                            collection.cat_link_two =
                                list.get(1).asJsonObject.get("link_type").asString
                            collection.cat_link_three =
                                list.get(2).asJsonObject.get("link_type").asString
                            collection.cat_link_four =
                                list.get(3).asJsonObject.get("link_type").asString
                            collection.cat_link_five =
                                list.get(4).asJsonObject.get("link_type").asString
                        }
                        if (list.get(0).asJsonObject.has("link_value")) {
                            collection.cat_value_one =
                                list.get(0).asJsonObject.get("link_value").asString
                            collection.cat_value_two =
                                list.get(1).asJsonObject.get("link_value").asString
                            collection.cat_value_three =
                                list.get(2).asJsonObject.get("link_value").asString
                            collection.cat_value_four =
                                list.get(3).asJsonObject.get("link_value").asString
                            if (list.get(4).asJsonObject.has("link_value")) {
                                collection.cat_value_five =
                                    list.get(4).asJsonObject.get("link_value").asString
                            }
                        }
                        if(isLightModeOn()){
                            categoryitem.catTextOne.setTextColor(Color.parseColor(tittlecolor))
                            categoryitem.catTextTwo.setTextColor(Color.parseColor(tittlecolor))
                            categoryitem.catTextThree.setTextColor(Color.parseColor(tittlecolor))
                            categoryitem.catTextFour.setTextColor(Color.parseColor(tittlecolor))
                            categoryitem.catTextFive.setTextColor(Color.parseColor(tittlecolor))
                            var background = JSONObject(jsonObject.getString("panel_background_color")).getString("color")
                            categoryitem.sliderCircleList.setBackgroundColor(Color.parseColor(background))
                            categoryitem.mainContainer.setBackgroundColor(Color.parseColor(background))
                        }
                        categoryitem.catTextOne.typeface = face
                        categoryitem.catTextTwo.typeface = face
                        categoryitem.catTextThree.typeface = face
                        categoryitem.catTextFour.typeface = face
                        categoryitem.catTextFive.typeface = face
                        when (jsonObject.getString("item_font_style")) {
                            "italic" -> {
                                categoryitem.catTextOne.setTypeface(
                                    categoryitem.catTextOne.typeface,
                                    Typeface.ITALIC
                                )
                                categoryitem.catTextTwo.setTypeface(
                                    categoryitem.catTextTwo.typeface,
                                    Typeface.ITALIC
                                )
                                categoryitem.catTextThree.setTypeface(
                                    categoryitem.catTextThree.typeface,
                                    Typeface.ITALIC
                                )
                                categoryitem.catTextFour.setTypeface(
                                    categoryitem.catTextFour.typeface,
                                    Typeface.ITALIC
                                )
                                categoryitem.catTextFive.setTypeface(
                                    categoryitem.catTextFive.typeface,
                                    Typeface.ITALIC
                                )
                            }
                        }
                        categoryitem.category = collection
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }
                })
            linkedHashMap.put(key, categoryitem.root)
            // homepagedata.setValue(hashMapOf("category-circle_" to categoryitem.root))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createCircleSlider(jsonObject: JSONObject, key: String) {
        val binding: NewCircleSliderBinding = DataBindingUtil.inflate(
            context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            R.layout.new_circle_slider,
            null,
            false
        )
        context.setLayout(binding.newCircleList, "horizontal")
        try {
            repository.getJSonArray(JsonParser().parse(jsonObject.getString("items")).asJsonArray)
                .subscribeOn(Schedulers.io())
                .filter { x -> x.asJsonObject.get("link_type").asString.isNotEmpty() }
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<List<JsonElement>> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onSuccess(list: List<JsonElement>) {
                        category_adapter = CategoryCircleAdpater()
                        category_adapter.setData(list, context, jsonObject)
                        binding.newCircleList.adapter = category_adapter
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var background = JSONObject(jsonObject.getString("panel_background_color"))
        binding.newCircleList.setBackgroundColor(Color.parseColor(background.getString("color")))
        linkedHashMap.put(key, binding.root)
    }

    private fun createCategoryCard(jsonObject: JSONObject, key: String) {
        if (jsonObject.getString("item_total").toInt() > 5) {
            val binding: NewCircleSliderBinding = DataBindingUtil.inflate(
                context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.new_circle_slider,
                null,
                false
            )
            context.setLayout(binding.newCircleList, "horizontal")
            try {
                repository.getJSonArray(JsonParser().parse(jsonObject.getString("items")).asJsonArray)
                    .subscribeOn(Schedulers.io())
                    .filter { x -> x.asJsonObject.get("link_type").asString.isNotEmpty() }
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<List<JsonElement>> {
                        override fun onSubscribe(d: Disposable) {
                        }
                        override fun onSuccess(list: List<JsonElement>) {
                            var item_shape = jsonObject.getString("item_shape")
                            when (item_shape) {
                                "circle" -> {
                                    category_adapter = CategoryCircleAdpater()
                                    category_adapter.setData(list, context, jsonObject)
                                    binding.newCircleList.adapter = category_adapter
                                }
                                "square" -> {
                                    category_square_adapter = CategorySquareAdapter()
                                    category_square_adapter.setData(list, context, jsonObject)
                                    binding.newCircleList.adapter = category_square_adapter
                                }
                                "rectangle" -> {
                                    category_square_adapter = CategorySquareAdapter()
                                    category_square_adapter.setData(list, context, jsonObject)
                                    binding.newCircleList.adapter = category_square_adapter
                                }
                            }
                        }
                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if(isLightModeOn()){
                var background = JSONObject(jsonObject.getString("panel_background_color"))
                binding.newCircleList.setBackgroundColor(Color.parseColor(background.getString("color")))
                binding.root.setBackgroundColor(Color.parseColor(background.getString("color")))
            }
            linkedHashMap.put(key, binding.root)
        } else {
            var item_shape = jsonObject.getString("item_shape")
            when (item_shape) {
                "circle" -> {
                    createCircleCategory(jsonObject, key)
                }
                "square" -> {
                    createCategorySquare(jsonObject, key)
                }
                "rectangle" -> {
                    createCategorySquare(jsonObject, key)
                }
            }
        }
    }
    private fun createCollectionGrid(jsonObject: JSONObject, key: String) {
        Log.i("SaifDevCustomgrid", "" + jsonObject)
        val binding: MCollectionlgridBinding = DataBindingUtil.inflate(
            context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            R.layout.m_collectionlgrid,
            null,
            false
        )
        if(jsonObject.has("header")&&jsonObject.getString("header").equals("1")){
            binding.headertext.visibility=View.VISIBLE
            binding.headertext.text=jsonObject.getString("header_title_text")
            if(isLightModeOn()){
                if(jsonObject.has("header_background_color")){
                    binding.headertext.setBackgroundColor(Color.parseColor(JSONObject(jsonObject.getString("header_background_color")).getString("color")))
                }
                if(jsonObject.has("header_title_color")){
                    binding.headertext.setTextColor(Color.parseColor(JSONObject(jsonObject.getString("header_title_color")).getString("color")))
                }
            }
            if(jsonObject.has("header_title_font_weight")){
                when (jsonObject.getString("header_title_font_weight")) {
                    "bold" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                    }
                    "light" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "medium" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                    }
                }
            }

            if (jsonObject.has("header_title_font_style") && jsonObject.getString("header_title_font_style").equals("italic")) {
                binding.headertext.setTypeface(binding.headertext.typeface, Typeface.ITALIC)
            }

        }
        context.setLayout(binding.categorylist, "customisablegrid")
        try {
            repository.getJSonArray(JsonParser().parse(jsonObject.getString("items")).asJsonArray)
                .subscribeOn(Schedulers.io())
                .filter { x -> x.asJsonObject.get("link_type").asString.isNotEmpty() }
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<List<JsonElement>> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onSuccess(list: List<JsonElement>) {
                        adapter = CollectionGridAdapter()
                        adapter.setData(list, context, jsonObject)
                        binding.categorylist.adapter = adapter
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if(isLightModeOn()){
            var background =JSONObject(jsonObject.getString("panel_background_color"))
            binding.categorylist.setBackgroundColor(Color.parseColor(background.getString("color")))
            binding.root.setBackgroundColor(Color.parseColor(background.getString("color")))
        }
        linkedHashMap.put(key, binding.root)
    }


    private fun createCategorySquare(jsonObject: JSONObject, key: String) {
        var binding: MCategorySquareBinding = DataBindingUtil.inflate(
            context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            R.layout.m_category_square,
            null,
            false
        )
        var shape="square"
        repository.getJSonArray(JsonParser().parse(jsonObject.getString("items")).asJsonArray)
            .subscribeOn(Schedulers.io())
            .filter { x -> x.asJsonObject.get("link_type").asString.isNotEmpty() }
            .toList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<List<JsonElement>> {
                override fun onSubscribe(d: Disposable) {}
                override fun onSuccess(list: List<JsonElement>) {
                    val collection = CategoryCircle()
                    when (jsonObject.getString("item_shape")) {
                        "rectangle" -> {
                            shape="rectangle"
                            (binding.cardOne.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio="H,250:190"
                            (binding.cardTwo.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio="H,250:190"
                            (binding.cardThree.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio="H,250:190"
                            (binding.cardFour.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio="H,250:190"
                            (binding.cardFive.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio="H,250:190"
                            val newLayoutParams = binding.imageOne.getLayoutParams() as FrameLayout.LayoutParams
                            newLayoutParams.setMargins(2,2,2,2)
                            binding.imageOne.layoutParams=newLayoutParams
                            binding.imageTwo.layoutParams=newLayoutParams
                            binding.imageThree.layoutParams=newLayoutParams
                            binding.imageFour.layoutParams=newLayoutParams
                            binding.imageFive.layoutParams=newLayoutParams
                        }
                    }
                    if (jsonObject.getString("item_title").equals("1")) {
                        collection.titlevisible = true
                    }
                    if (jsonObject.getString("item_border").equals("1")) {
                        var bordercolor="#FFFFFF"
                        if(isLightModeOn()){
                            var item_border_color = JSONObject(jsonObject.getString("item_border_color"))
                            bordercolor=item_border_color.getString("color")
                        }
                        binding.cardOne.setCardBackgroundColor(Color.parseColor(bordercolor))
                        binding.cardTwo.setCardBackgroundColor(Color.parseColor(bordercolor))
                        binding.cardThree.setCardBackgroundColor(Color.parseColor(bordercolor))
                        binding.cardFour.setCardBackgroundColor(Color.parseColor(bordercolor))
                        binding.cardFive.setCardBackgroundColor(Color.parseColor(bordercolor))
                    }else {
                        val newLayoutParams = binding.imageOne.getLayoutParams() as FrameLayout.LayoutParams
                        newLayoutParams.setMargins(0,0,0,0)
                        binding.imageOne.layoutParams=newLayoutParams
                        binding.imageTwo.layoutParams=newLayoutParams
                        binding.imageThree.layoutParams=newLayoutParams
                        binding.imageFour.layoutParams=newLayoutParams
                        binding.imageFive.layoutParams=newLayoutParams
                        binding.cardOne.setCardBackgroundColor(Color.parseColor("#0000ffff"))
                        binding.cardTwo.setCardBackgroundColor(Color.parseColor("#0000ffff"))
                        binding.cardThree.setCardBackgroundColor(Color.parseColor("#0000ffff"))
                        binding.cardFour.setCardBackgroundColor(Color.parseColor("#0000ffff"))
                        binding.cardFive.setCardBackgroundColor(Color.parseColor("#0000ffff"))
                    }
                    if(jsonObject.has("corner_radius")) {
                        var card=getCornerRadius(jsonObject.getString("corner_radius"))
                        var image=0f
                        when(jsonObject.getString("corner_radius")) {
                            "4","8"->{
                                image=jsonObject.getString("corner_radius").toFloat()
                            }
                            "12"->{
                                image=20f
                            }
                            "16"->{
                                if (shape.equals("rectangle")){
                                    image=25f
                                }else{
                                    image=23f
                                }
                            }
                            "20"->{
                                if (shape.equals("rectangle")){
                                    image=27f
                                }else{
                                    image=33f
                                }
                            }
                        }
                        var carddp= applyDimension(TypedValue.COMPLEX_UNIT_DIP,card,
                            context!!.resources.displayMetrics
                        )
                        var imagedp= applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,image,
                            context!!.resources.displayMetrics
                        )
                        binding.cardOne.radius=carddp
                        binding.cardTwo.radius=carddp
                        binding.cardThree.radius=carddp
                        binding.cardFour.radius=carddp
                        binding.cardFive.radius=carddp
                        binding.imageOne.setShapeAppearanceModel(
                            binding.imageOne.getShapeAppearanceModel()
                                .toBuilder()
                                .setAllCorners(CornerFamily.ROUNDED, imagedp)
                                .build()
                        )
                        binding.imageTwo.setShapeAppearanceModel(
                            binding.imageTwo.getShapeAppearanceModel()
                                .toBuilder()
                                .setAllCorners(CornerFamily.ROUNDED, imagedp)
                                .build()
                        )
                        binding.imageThree.setShapeAppearanceModel(
                            binding.imageThree.getShapeAppearanceModel()
                                .toBuilder()
                                .setAllCorners(CornerFamily.ROUNDED, imagedp)
                                .build()
                        )
                        binding.imageFour.setShapeAppearanceModel(
                            binding.imageFour.getShapeAppearanceModel()
                                .toBuilder()
                                .setAllCorners(CornerFamily.ROUNDED, imagedp)
                                .build()
                        )
                        binding.imageFive.setShapeAppearanceModel(
                            binding.imageFive.getShapeAppearanceModel()
                                .toBuilder()
                                .setAllCorners(CornerFamily.ROUNDED, imagedp)
                                .build()
                        )
                    }
                    var tittlecolor = JSONObject(jsonObject.getString("item_title_color"))
                    var face = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    when (jsonObject.getString("item_font_weight")) {
                        "medium" -> {
                            face = Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        }
                        "light" -> {
                            face = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        }
                        "bold" -> {
                            face = Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        }
                    }
                    if (list.get(0).asJsonObject.has("title")) {
                        collection.cat_text_one = list.get(0).asJsonObject.get("title").asString
                        Constant.translateField(collection.cat_text_one!!, binding.catTextOne)
                        collection.cat_text_two = list.get(1).asJsonObject.get("title").asString
                        Constant.translateField(collection.cat_text_two!!, binding.catTextTwo)
                        collection.cat_text_three = list.get(2).asJsonObject.get("title").asString
                        Constant.translateField(collection.cat_text_three!!, binding.catTextThree)
                        collection.cat_text_four = list.get(3).asJsonObject.get("title").asString
                        Constant.translateField(collection.cat_text_four!!, binding.catTextFour)
                        collection.cat_text_five = list.get(4).asJsonObject.get("title").asString
                        Constant.translateField(collection.cat_text_five!!, binding.catTextFive)
                    }
                    if (list.get(0).asJsonObject.has("image_url")) {
                        collection.cat_image_one =
                            list.get(0).asJsonObject?.get("image_url")?.asString
                        collection.cat_image_two =
                            list.get(1).asJsonObject?.get("image_url")?.asString
                        collection.cat_image_three =
                            list.get(2).asJsonObject?.get("image_url")?.asString
                        collection.cat_image_four =
                            list.get(3).asJsonObject?.get("image_url")?.asString
                        collection.cat_image_five =
                            list.get(4).asJsonObject?.get("image_url")?.asString
                    }
                    if (list.get(0).asJsonObject.has("link_type")) {
                        collection.cat_link_one = list.get(0).asJsonObject.get("link_type").asString
                        collection.cat_link_two = list.get(1).asJsonObject.get("link_type").asString
                        collection.cat_link_three =
                            list.get(2).asJsonObject.get("link_type").asString
                        collection.cat_link_four =
                            list.get(3).asJsonObject.get("link_type").asString
                        collection.cat_link_five =
                            list.get(4).asJsonObject.get("link_type").asString
                    }
                    if (list.get(0).asJsonObject.has("link_value")) {
                        collection.cat_value_one =
                            list.get(0).asJsonObject.get("link_value").asString
                        collection.cat_value_two =
                            list.get(1).asJsonObject.get("link_value").asString
                        collection.cat_value_three =
                            list.get(2).asJsonObject.get("link_value").asString
                        collection.cat_value_four =
                            list.get(3).asJsonObject.get("link_value").asString
                        if (list.get(4).asJsonObject.has("link_value")) {
                            collection.cat_value_five =
                                list.get(4).asJsonObject.get("link_value").asString
                        }
                    }
                    if(isLightModeOn()){
                        binding.catTextOne.setTextColor(Color.parseColor(tittlecolor.getString("color")))
                        binding.catTextTwo.setTextColor(Color.parseColor(tittlecolor.getString("color")))
                        binding.catTextThree.setTextColor(Color.parseColor(tittlecolor.getString("color")))
                        binding.catTextFour.setTextColor(Color.parseColor(tittlecolor.getString("color")))
                        binding.catTextFive.setTextColor(Color.parseColor(tittlecolor.getString("color")))
                    }
                    binding.catTextOne.typeface = face
                    binding.catTextTwo.typeface = face
                    binding.catTextThree.typeface = face
                    binding.catTextFour.typeface = face
                    binding.catTextFive.typeface = face
                    when (jsonObject.getString("item_font_style")) {
                        "italic" -> {
                            binding.catTextOne.setTypeface(
                                binding.catTextOne.typeface,
                                Typeface.ITALIC
                            )
                            binding.catTextTwo.setTypeface(
                                binding.catTextTwo.typeface,
                                Typeface.ITALIC
                            )
                            binding.catTextThree.setTypeface(
                                binding.catTextThree.typeface,
                                Typeface.ITALIC
                            )
                            binding.catTextFour.setTypeface(
                                binding.catTextFour.typeface,
                                Typeface.ITALIC
                            )
                            binding.catTextFive.setTypeface(
                                binding.catTextFive.typeface,
                                Typeface.ITALIC
                            )
                        }
                    }
                    binding.category = collection
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }
            })
        if(isLightModeOn()){
            var background = JSONObject(jsonObject.getString("panel_background_color"))
            binding.mCircleList.setBackgroundColor(Color.parseColor(background.getString("color")))
            binding.root.setBackgroundColor(Color.parseColor(background.getString("color")))
        }
        linkedHashMap.put(key, binding.root)
    }

    private fun createProductSlider(jsonObject: JSONObject, flag: Boolean, key: String) {
        try {
            val binding: MProductSlidersBinding = DataBindingUtil.inflate(
                context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.m_product_sliders,
                null,
                false
            )
            var productSlider = ProductSlider()
            binding.root.visibility=View.GONE
            if(jsonObject.has("linking_type")){
                when(jsonObject.getString("linking_type")){
                    "newest_first"->{
                        updateDataInRecylerViewCatId(binding.productdataSlider, jsonObject.getString("item_link_action_value"),jsonObject,flag,binding.root)
                    }
                    else->{
                        updateDataInRecylerView(binding.productdataSlider, jsonObject.getJSONArray("item_value"),  jsonObject,flag,binding.root)
                    }
                }
            }else{
                updateDataInRecylerView(binding.productdataSlider, jsonObject.getJSONArray("item_value"),  jsonObject,flag,binding.root)
            }
            if(flag){
                if (jsonObject.getString("header").equals("1")) {
                    binding.headerSection.visibility = View.VISIBLE
                    productSlider.headertext = jsonObject.getString("header_title_text")
                    if(isLightModeOn()){
                        var header_background_color = JSONObject(jsonObject.getString("header_background_color"))
                        binding.headerSection.setBackgroundColor(Color.parseColor(header_background_color.getString("color")))
                        var header_title_color = JSONObject(jsonObject.getString("header_title_color"))
                        binding.headertext.setTextColor(Color.parseColor(header_title_color.getString("color")))
                    }
                    when (jsonObject.getString("item_header_font_weight")) {
                        "bold" -> {
                            binding.headertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        }
                        "light" -> {
                            binding.headertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        }
                        "medium" -> {
                            binding.headertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        }
                    }
                    if (jsonObject.getString("item_header_font_style").equals("italic")) {
                        binding.headertext.setTypeface(binding.headertext.typeface, Typeface.ITALIC)
                    }
                    if (jsonObject.getString("header_subtitle").equals("1")) {
                        binding.subheadertext.visibility = View.VISIBLE
                        productSlider.subheadertext = jsonObject.getString("header_subtitle_text")
                       // Constant.translateField(productSlider.subheadertext!!, binding.subheadertext)
                        if(isLightModeOn()){
                            var header_subtitle_color = JSONObject(jsonObject.getString("header_subtitle_color"))
                            binding.subheadertext.setTextColor(Color.parseColor(header_subtitle_color.getString("color")))
                        }
                        when (jsonObject.getString("header_subtitle_font_weight")) {
                            "bold" -> {
                                binding.subheadertext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                            }
                            "light" -> {
                                binding.subheadertext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                            }
                            "medium" -> {
                                binding.subheadertext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                            }
                        }
                        if (jsonObject.getString("header_subtitle_title_font_style")
                                .equals("italic")
                        ) {
                            binding.subheadertext.setTypeface(
                                binding.subheadertext.typeface,
                                Typeface.ITALIC
                            )
                        }
                    }else{
                        (binding.headertext.layoutParams as ConstraintLayout.LayoutParams).bottomToBottom = PARENT_ID
                    }

                    if (jsonObject.getString("header_action").equals("1")) {
                        binding.actiontext.visibility = View.VISIBLE
                        productSlider.action_id = getcategoryID(jsonObject.getString("item_link_action_value"))
                        productSlider.actiontext = jsonObject.getString("header_action_text")
                       // Constant.translateField(productSlider.actiontext!!, binding.actiontext)
                        if(isLightModeOn()){
                            var header_action_color = JSONObject(jsonObject.getString("header_action_color"))
                            var header_action_background_color = JSONObject(jsonObject.getString("header_action_background_color"))
                            binding.actiontext.setTextColor(Color.parseColor(header_action_color.getString("color")))
                            var gradientDrawable = binding.actiontext.background as GradientDrawable
                            gradientDrawable.setStroke(2, Color.parseColor(header_action_background_color.getString("color")))
                            gradientDrawable.setColor(Color.parseColor(header_action_background_color.getString("color")))
                        }
                        //binding.actiontext.setBackgroundColor(Color.parseColor(header_action_background_color.getString("color")))
                        when (jsonObject.getString("header_action_font_weight")) {
                            "bold" -> {
                                binding.actiontext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                            }
                            "light" -> {
                                binding.actiontext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                            }
                            "medium" -> {
                                binding.actiontext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                            }
                        }
                        if (jsonObject.getString("header_action_font_style").equals("italic")) {
                            binding.actiontext.setTypeface(binding.actiontext.typeface, Typeface.ITALIC)
                        }
                    }
                    if (jsonObject.getString("header_deal").equals("1")) {
                        binding.dealsection.deallayout.visibility = View.VISIBLE
                        productSlider.timertextmessage = jsonObject.getString("item_deal_message")
                        if(isLightModeOn()){
                            var header_deal_color = JSONObject(jsonObject.getString("header_deal_color"))
                            binding.dealsection.timerMessage.setTextColor(Color.parseColor(header_deal_color.getString("color")))
                        }
                        var DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"
                        var sdf = SimpleDateFormat(DATE_FORMAT)
                        // sdf.timeZone = TimeZone.getTimeZone("UTC")
                        var item_deal_start_date = sdf.format(Date())
                        Log.i("MageNative", "item_deal_start_date " + item_deal_start_date)
                        var item_deal_end_date = jsonObject.getString("item_deal_end_date")
                        Log.i("MageNative", "item_deal_end_date " + item_deal_end_date)
                        var startdate: Date?
                        var enddate: Date?
                        try {
                            startdate = sdf.parse(item_deal_start_date)
                            enddate = sdf.parse(item_deal_end_date)
                            var oldLong = startdate.time
                            var NewLong = enddate.time
                            var diff = NewLong - oldLong
                            Log.i("MageNative", "Long" + diff)
                            if (diff > 0) {
                                var counter = MyCount(diff, 1000, productSlider, ":")
                                counter.start()
                            } else {
                                productSlider.timericon = View.GONE
                                binding.dealsection.timer.visibility = View.GONE
                                binding.dealsection.deallayout.visibility = View.GONE
                            }
                        } catch (ex: ParseException) {
                            ex.printStackTrace()
                        }
                    }
                }
                if(isLightModeOn()){
                    var panel_background_color = JSONObject(jsonObject.getString("panel_background_color")).getString("color")
                    binding.panelbackgroundcolor.setBackgroundColor(Color.parseColor(panel_background_color))
                    binding.root.setBackgroundColor(Color.parseColor(panel_background_color))
                }
                binding.productslider = productSlider
                linkedHashMap.put(key, binding.root)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun updateDataInRecylerViewCatId(
        productdataSlider: RecyclerView,
        CatID: String,
        jsonObject: JSONObject,
        flag: Boolean,
        root: View
    ) {
        try {
            runBlocking(Dispatchers.IO) {
                try {
                    val edges = mutableListOf<Storefront.Product>()
                    var no=10
                    when (jsonObject.getString("type")) {
                        "fixed-customisable-layout" -> {
                            no=  Integer.parseInt(jsonObject.getString("item_in_a_row")) * Integer.parseInt(jsonObject.getString("item_row"))
                            Log.i("CategoryCache","actualsize"+no)
                            if(repository.getHomePageProductByCatID(CatID,jsonObject.getString("uniqueId")).size!=no){
                                repository.deleteCategoryProducts(CatID,jsonObject.getString("uniqueId"))
                            }
                            delay(100)
                        }
                    }
                    try{
                        if(repository.isHomePageProductsCached()&&flag){
                            if(repository.getHomePageProductByCatID(CatID,jsonObject.getString("uniqueId")).size>0){
                                for (i in 0 until repository.getHomePageProductByCatID(CatID,jsonObject.getString("uniqueId")).size){
                                    Log.i("CategoryCache",""+jsonObject.getString("uniqueId"))
                                    Log.i("CategoryCache",""+repository.getHomePageProductByCatID(CatID,jsonObject.getString("uniqueId")).size)
                                    var obj = JSONObject(repository.getHomePageProductByCatID(CatID,jsonObject.getString("uniqueId")).get(i).product)
                                    var cachejsonObject=JSONObject()
                                    var key=""
                                    if(obj.length()>0){
                                        for (n in 0 until obj.names()!!.length()){
                                            if (obj.has(obj.names()!!.getString(n)) && obj.getJSONObject(obj.names()!!.getString(n)).length()>0){
                                                cachejsonObject=  obj.getJSONObject(obj.names()!!.getString(n))
                                                key=obj.names()!!.getString(n)
                                                if(cachejsonObject.length()>0){
                                                    var product = getProduct(cachejsonObject,ID(repository.getHomePageProductByCatID(CatID,jsonObject.getString("uniqueId")).get(i).product_id),key)
                                                    edges.add(product)
                                                }
                                                Log.i("Product_aftercache", "${key}->" + cachejsonObject)
                                            }
                                        }
                                    }
                                }
                                Log.i("CategoryCache",""+edges.size)
                            }
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                    if (repository.isHomePageProductsCached() && flag && edges.size>0) {
                        filterProduct(edges, productdataSlider, jsonObject, root)
                    } else {
                        doGraphQLQueryGraph(
                            repository,
                            Query.getProductsById("gid://shopify/Collection/" + CatID, "nocursor", sortKeys, true,
                                no, Constant.internationalPricing(), getFilters()),
                            customResponse = object : CustomResponse {
                                override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                                    invokes(result, productdataSlider,jsonObject,flag,root,CatID)
                                }
                            },
                            context = context
                        )
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        } catch (e: Exception) {

        }
    }
    private fun invokes(
        result: GraphCallResult<Storefront.QueryRoot>,
        productdataSlider: RecyclerView,
        jsonObject: JSONObject,
        flag: Boolean,
        root: View,
        CatID: String
    ) {
        if (result is GraphCallResult.Success<*>) {
            consumeResponses(GraphQLResponse.success(result as GraphCallResult.Success<*>), productdataSlider,jsonObject,flag,root,CatID)
        } else {
            consumeResponses(GraphQLResponse.error(result as GraphCallResult.Failure), productdataSlider,jsonObject,flag,root,CatID)
        }
        return
    }

    private fun consumeResponses(
        reponse: GraphQLResponse,
        productdataSlider: RecyclerView,
        jsonObject: JSONObject,
        flag: Boolean,
        root: View,
        CatID: String
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
                        var edges: List<Storefront.ProductEdge>? = null
                        if (result.data!!.node != null) {
                            edges = (result.data?.node as Storefront.Collection).products.edges
                            for (i in 0..edges.size - 1) {
                                if (edges.get(i).node != null) {
                                    var product = edges.get(i).node as Storefront.Product
                                  //  Log.i("SaifDev_TestingCache_fixed", "Product" + product.id)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {

                                            var array:List<HomePageProduct> = repository.getHomePageProductByCatID_Product(CatID,jsonObject.getString("uniqueId"),product.id.toString())
                                            if(array.size==0){
                                                var data=HomePageProduct()
                                                data.product_id=product.id.toString()
                                                var ProductData = Gson().toJson(product)
                                                Log.i("SaifDev_fixed", "Product_caching_insert" + product.id.toString())
                                                Log.i("SaifDev_fixed", "Product_caching_insert" + jsonObject.getString("uniqueId"))
                                                data.product=ProductData
                                                data.category_id=CatID
                                                data.uniqueId=jsonObject.getString("uniqueId")
                                                repository.insertHomePageProduct(data)
                                            }else{
                                                var data=array.get(0)
                                                data.product_id=product.id.toString()
                                                Log.i("SaifDev_fixed", "Product_caching_update" + product.id.toString())
                                                Log.i("SaifDev_fixed", "Product_caching_insert" + jsonObject.getString("uniqueId"))
                                                var ProductData = Gson().toJson(product)
                                                data.product=ProductData
                                                data.category_id=CatID
                                                data.uniqueId=jsonObject.getString("uniqueId")
                                                repository.updateHomePageProduct(data)
                                            }
                                        }catch (e:Exception){
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                        if(flag && edges!=null){
                            filterProducts(edges, productdataSlider,jsonObject,flag,root)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            Status.ERROR -> {
                Log.i("MageNatyive", "ERROR-1" + reponse.error!!.error.message)
                message.setValue(reponse.error.error.message)
            }
        }
    }

    fun getFilters(): ArrayList<Storefront.ProductFilter> {
        val productFiltersarray = ArrayList<Storefront.ProductFilter>()
        if (!SplashViewModel.featuresModel.outOfStock!!){
            val instockFilter = Storefront.ProductFilter()
            instockFilter.available = true
            productFiltersarray.add(instockFilter)
        }
        System.out.println("FiltersFinalTobeSent" + productFiltersarray.toString())
        return productFiltersarray
    }

    private fun filterProducts(
        list: List<Storefront.ProductEdge>?,
        productdataSlider: RecyclerView,
        jsonObject: JSONObject,
        flag: Boolean,
        root: View
    ) {
        try {
            repository.getProductListSliders(list)
                .subscribeOn(Schedulers.io())
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<List<Storefront.ProductEdge>> {
                    override fun onSubscribe(d: Disposable) {
                    }
                    override fun onError(e: Throwable) {
                        Log.i("DYNAMICDATA", "ERROR " + e.message)
                    }
                    override fun onSuccess(list: List<Storefront.ProductEdge>) {
                        Log.i("DYNAMICDATA", "SUCCESSS 1" + list.size)
                        if(list.size>0){
                            root.visibility=View.VISIBLE
                            when (jsonObject.getString("type")) {
                                "fixed-customisable-layout" -> {
                                    if (jsonObject.getString("item_layout_type").equals("list")) {
                                        productListAdapter = ProductListSliderAdapter()
                                        context.setLayout(productdataSlider, "customisablelist")
                                        productListAdapter.set_Data(list, context, jsonObject)
                                        productdataSlider.adapter = productListAdapter
                                    } else {
                                        when (jsonObject.getString("item_in_a_row")) {
                                            "2" -> {
                                                var productTwoGridAdapter = ProductTwoGridAdapter()
                                                context.setLayout(productdataSlider!!, "customisablegridwithtwoitem")
                                                productTwoGridAdapter.set_Data(list, context, jsonObject, repository)
                                                productdataSlider.adapter = productTwoGridAdapter
                                            }
                                            "3" -> {
                                                gridAdapter = ProductSliderGridAdapter()
                                                context.setLayout(productdataSlider!!, "customisablegrid")
                                                gridAdapter.set_Data(list, context, jsonObject)
                                                productdataSlider.adapter = gridAdapter
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    homeadapters = ProductSliderListDynamicAdapter()
                                    if (jsonObject.has("number_of_rows")){
                                        when(jsonObject.getString("number_of_rows")){
                                            "1"->productdataSlider!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                            "2"-> productdataSlider!!.layoutManager = GridLayoutManager(context,2, GridLayoutManager.HORIZONTAL, false)
                                        }
                                    }else{
                                        productdataSlider!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                    }
                                    homeadapters.setData(list, context,jsonObject, repository)
                                    productdataSlider.adapter = homeadapters
                                }
                            }
                        }
                    }
                })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateDataInRecylerView(
        productdata: RecyclerView?,
        jsonArray: JSONArray,
        jsonObject: JSONObject,
        flag: Boolean, view: View
    ) {
        runBlocking(Dispatchers.IO) {
            try {
                val edges = mutableListOf<Storefront.Product>()
                val product_ids = ArrayList<ID>()
                for (i in 0..jsonArray.length() - 1) {
                    Log.d(TAG, "updateDataInRecylerView: " + ID(getProductID(jsonArray.getString(i))))
                    var cache = repository.isHomePageProductsCached()
                    try{
                        if(repository.isHomePageProductsCached()&&flag){
                            if(repository.getHomePageProduct(getProductID(jsonArray.getString(i))!!).size>0){
                                //Log.i("SaifDev_TestingCache", "Category_aftercache" + jsonObject.getString("header_title_text"))
                                var obj = JSONObject(repository.getHomePageProduct(getProductID(jsonArray.getString(i))!!).get(0).product)
                                var cachejsonObject=JSONObject()
                                var key=""
                                if(obj.length()>0){
                                    for (n in 0 until obj.names()!!.length()){
                                        if (obj.has(obj.names()!!.getString(n)) && obj.getJSONObject(obj.names()!!.getString(n)).length()>0){
                                            cachejsonObject=  obj.getJSONObject(obj.names()!!.getString(n))
                                            key=obj.names()!!.getString(n)
                                            if(cachejsonObject.length()>0){
                                                val product = getProduct(cachejsonObject,ID(getProductID(jsonArray.getString(i))),key)
                                                edges.add(product)
                                            }
                                            Log.i("Product_aftercache", "${key}->" + cachejsonObject)
                                        }
                                    }
                                }
                            }
                        }else{
                            product_ids.add(ID(getProductID(jsonArray.getString(i))))
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
                if (repository.isHomePageProductsCached() && flag && edges.size>0) {
                    filterProduct(edges, productdata, jsonObject, view)
                } else {
                    getProductsById(product_ids, productdata, jsonObject, edges, flag, view,"homepage")
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
    private fun getProduct(JsonObject: JSONObject, id: ID, key: String): Storefront.Product {
        var product = Storefront.Product(id)
        try {
            val imageNode = Storefront.Image()
            product.title = JsonObject.getString("title")
            val imageEdge: MutableList<Storefront.ImageEdge> = mutableListOf()
            if(JsonObject.getJSONObject("images")
                    .getJSONObject(key)
                    .getJSONArray("edges").length()>0){
                var imageobj=JsonObject.getJSONObject("images")
                    .getJSONObject(key)
                    .getJSONArray("edges")
                    .getJSONObject(0)
                    .getJSONObject(key)
                    .getJSONObject("node")
                    .getJSONObject(key)
                imageNode.url = imageobj.getString("url")
                imageNode.height = imageobj.getInt("height")
                imageNode.width = imageobj.getInt("width")
            }
            imageEdge.add(Storefront.ImageEdge().setNode(imageNode))
            product.images = Storefront.ImageConnection().setEdges(imageEdge)
            product.description = JsonObject.getString("description")
            product.availableForSale = JsonObject.getBoolean("availableForSale")
            product.tags = Gson().fromJson(JsonObject.getJSONArray("tags").toString(), MutableList::class.java) as MutableList<String>
            val VariantEdge: MutableList<Storefront.ProductVariantEdge> = mutableListOf()
            var variants=JsonObject.getJSONObject("variants").getJSONObject(key).getJSONArray("edges")
            var variantsdata=variants.getJSONObject(0).getJSONObject(key).getJSONObject("node").getJSONObject(key).getJSONObject("id")
            var variantId = ""
            if(variantsdata.has("id")){
                variantId=variantsdata.getString("id")
            }
            if(variantsdata.has(key)){
                variantId=variantsdata.getString(key)
            }
            var VariantNode=Storefront.ProductVariant()
            if (variantId.isNotEmpty()){
                VariantNode = Storefront.ProductVariant(ID(variantId))
            }
            var price=variants.getJSONObject(0).getJSONObject(key).getJSONObject("node").getJSONObject(key).getJSONObject("price").getJSONObject(key)
            VariantNode.setPrice(Storefront.MoneyV2().setAmount(price.getString("amount")).setCurrencyCode(Storefront.CurrencyCode.fromGraphQl(price.getString("currencyCode"))))
            var compareatprice=variants.getJSONObject(0).getJSONObject(key).getJSONObject("node").getJSONObject(key)
            if(compareatprice.has("compareAtPrice") && compareatprice.getJSONObject("compareAtPrice").has(key)){
                VariantNode.setCompareAtPrice(Storefront.MoneyV2().setAmount(compareatprice.getJSONObject("compareAtPrice").getJSONObject(key).getString("amount"))
                    .setCurrencyCode(Storefront.CurrencyCode.fromGraphQl(compareatprice.getJSONObject("compareAtPrice").getJSONObject(key).getString("currencyCode"))))
            }
            VariantEdge.add(Storefront.ProductVariantEdge().setNode(VariantNode))
            product.variants = Storefront.ProductVariantConnection().setEdges(VariantEdge)
        }catch (e:Exception){
            e.printStackTrace()
        }
        return product
    }
    fun getProductsById(
        id: ArrayList<ID>,
        productdata: RecyclerView?,
        jsonObject: JSONObject,
        edges: MutableList<Storefront.Product>, flag: Boolean, view: View,viewType:String
    ) {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.getAllProductsByID(id, Constant.internationalPricing(),viewType),
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        if (result is GraphCallResult.Success<*>) {
                            consumeResponse(
                                GraphQLResponse.success(result as GraphCallResult.Success<*>),
                                productdata,
                                jsonObject,
                                edges, flag, view
                            )
                        } else {
                            consumeResponse(
                                GraphQLResponse.error(result as GraphCallResult.Failure),
                                productdata,
                                jsonObject,
                                edges, flag, view
                            )
                        }
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    private fun getProductID(id: String?): String? {
        var cat_id: String? = null
        try {
            val data = ("gid://shopify/Product/" + id!!)
            cat_id = data
           // Log.i("MageNatyive", "ProductSliderID :$id " + cat_id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cat_id
    }

    private fun getcategoryID(id: String?): String? {
        var cat_id: String? = null
        try {
            val data = id
            cat_id = data
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cat_id
    }

    private fun consumeResponse(
        reponse: GraphQLResponse,
        productdata: RecyclerView?,
        jsonObject: JSONObject,
        edges: MutableList<Storefront.Product>, flag: Boolean, view: View
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
                            if (result.data!!.nodes[i] != null) {
                                var product = result.data?.nodes?.get(i)!! as Storefront.Product
                                 // Log.i("SaifDev_TestingCache", "Category" + jsonObject.getString("header_title_text"))
                                 Log.i("SaifDev_TestingCache", "Product" + product.id)
                                edges.add(product)
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                      //  Log.i("SaifDev_TestingCache", "Category_caching" + jsonObject.getString("header_title_text"))
                                        var array:List<HomePageProduct> = repository.getHomePageProduct(result.data?.nodes?.get(i)!!.id.toString())
                                        if(array.size==0){
                                            var data=HomePageProduct()
                                            data.product_id=result.data?.nodes?.get(i)!!.id.toString()
                                            var ProductData = Gson().toJson(result.data?.nodes?.get(i)!! as Storefront.Product)
                                            Log.i("SaifDev_TestingCache", "Product_caching_insert" + ProductData)
                                            Log.i("SaifDev_TestingCache", "Product_caching_insert" + result.data?.nodes?.get(i)!!.id.toString())
                                            data.product=ProductData
                                            repository.insertHomePageProduct(data)
                                            /*CoroutineScope((Dispatchers.Main)).launch {
                                                message.value="Saif"+ProductData
                                                message.value="Product Inserted"+result.data?.nodes?.get(i)!!.id.toString()
                                            }*/
                                        }else{
                                            var data=repository.getHomePageProduct(result.data?.nodes?.get(i)!!.id.toString()).get(0)
                                            data.product_id=result.data?.nodes?.get(i)!!.id.toString()
                                            var ProductData = Gson().toJson(result.data?.nodes?.get(i)!! as Storefront.Product)
                                            Log.i("SaifDev_TestingCache", "Product_caching_update" + ProductData)
                                            Log.i("SaifDev_TestingCache", "Product_caching_update" + result.data?.nodes?.get(i)!!.id.toString())
                                            data.product=ProductData
                                            repository.updateHomePageProduct(data)
                                            /*CoroutineScope((Dispatchers.Main)).launch {
                                                message.value="Product updated"+result.data?.nodes?.get(i)!!.id.toString()
                                            }*/
                                        }
                                    }catch (e:Exception){
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                        if(flag){
                            filterProduct(edges, productdata, jsonObject,view)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        when (context.packageName) {
                            "com.rasmishopping.app" -> {
                                //    Toast.makeText(context, "Please Provide Visibility to Products and Collections", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
            Status.ERROR -> {
                Log.i("MageNatyive", "ERROR-1" + reponse.error!!.error.message)
//                message.setValue(reponse.error.error.message)
            }
            else -> {}
        }
    }

    private fun filterProduct(
        list: List<Storefront.Product>,
        productdata: RecyclerView?,
        jsonObject: JSONObject, view: View
    ) {
        try {
            if (SplashViewModel.featuresModel.outOfStock!!) {
                repository.getProductListSlider(list)
                    .subscribeOn(Schedulers.io())
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<List<Storefront.Product>> {
                        override fun onSubscribe(d: Disposable) {
                        }
                        override fun onSuccess(list: List<Storefront.Product>) {
                            if(list.size>0){
                                view.visibility = View.VISIBLE
                                when (jsonObject.getString("type")) {
                                    "fixed-customisable-layout" -> {
                                        if (jsonObject.getString("item_layout_type").equals("list")) {
                                            productListAdapter = ProductListSliderAdapter()
                                            context.setLayout(productdata!!, "customisablelist")
                                            productListAdapter.setData(list, context, jsonObject)
                                            productdata.adapter = productListAdapter
                                        } else {
                                            when (jsonObject.getString("item_in_a_row")) {
                                                "2" -> {
                                                    var productTwoGridAdapter = ProductTwoGridAdapter()
                                                    context.setLayout(productdata!!, "customisablegridwithtwoitem")
                                                    productTwoGridAdapter.setData(list,context, jsonObject, repository)
                                                    productdata.adapter = productTwoGridAdapter
                                                }
                                                "3" -> {
                                                    gridAdapter = ProductSliderGridAdapter()
                                                    context.setLayout(productdata!!, "customisablegrid")
                                                    gridAdapter.setData(list, context, jsonObject)
                                                    productdata.adapter = gridAdapter
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        Log.i("MageNatyive", "Data" + list.size)
                                        homeadapter = ProductSliderListAdapter()
                                        if (jsonObject.has("number_of_rows")){
                                            when(jsonObject.getString("number_of_rows")){
                                                "1"->productdata!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                                "2"-> productdata!!.layoutManager = GridLayoutManager(context,2, GridLayoutManager.HORIZONTAL, false)
                                            }
                                        }else{
                                            productdata!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                        }
                                        homeadapter.setData(list, context, jsonObject, repository)
                                        productdata!!.adapter = homeadapter
                                    }
                                }
                            }
                        }
                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }
                    })
            } else {
                repository.getProductListSlider(list)
                    .subscribeOn(Schedulers.io())
                    .filter { x -> x.availableForSale  }
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<List<Storefront.Product>> {
                        override fun onSubscribe(d: Disposable) {
                        }
                        override fun onSuccess(list: List<Storefront.Product>) {
                            if(list.size>0){
                                view.visibility = View.VISIBLE
                                when (jsonObject.getString("type")) {
                                    "fixed-customisable-layout" -> {
                                        if (jsonObject.getString("item_layout_type").equals("list")) {
                                            productListAdapter = ProductListSliderAdapter()
                                            context.setLayout(productdata!!, "customisablelist")
                                            productListAdapter.setData(list, context, jsonObject)
                                            productdata.adapter = productListAdapter
                                        } else {
                                            when (jsonObject.getString("item_in_a_row")) {
                                                "2" -> {
                                                    var productTwoGridAdapter = ProductTwoGridAdapter()
                                                    context.setLayout(productdata!!, "customisablegridwithtwoitem")
                                                    productTwoGridAdapter.setData(list, context, jsonObject, repository)
                                                    productdata.adapter = productTwoGridAdapter
                                                }
                                                "3" -> {
                                                    gridAdapter = ProductSliderGridAdapter()
                                                    context.setLayout(productdata!!, "customisablegrid")
                                                    gridAdapter.setData(list, context, jsonObject)
                                                    productdata.adapter = gridAdapter
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        Log.i("MageNatyive", "Data" + list.size)
                                        homeadapter = ProductSliderListAdapter()
                                        if (jsonObject.has("number_of_rows")){
                                            when(jsonObject.getString("number_of_rows")){
                                                "1"->productdata!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                                "2"-> productdata!!.layoutManager = GridLayoutManager(context,2, GridLayoutManager.HORIZONTAL, false)
                                            }
                                        }else{
                                            productdata!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                        }

                                        homeadapter.setData(list, context, jsonObject, repository)
                                        productdata!!.adapter = homeadapter
                                    }
                                }
                            }
                        }
                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }
                    })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun setVariants(node: Storefront.Product): Boolean {
        var edges = ArrayList<Storefront.ProductVariantEdge>()
        for (i in 0 until node.variants.edges.size) {
            if (node.variants.edges.get(i).node.availableForSale) {
                edges.add(node.variants.edges.get(i))
            }
        }
        var variants: Storefront.ProductVariantConnection = Storefront.ProductVariantConnection()
        variants.edges = edges
        node.variants = variants
        return true
    }

    private fun checkProduct(jsonArray: JSONArray, productedge: Storefront.ProductEdge): Boolean {
        var flag: Boolean = false
        try {
            for (data in 0..jsonArray.length() - 1) {
                val data = data
                val id = data
                if (id.equals(productedge.node.id.toString())) {
                    flag = true
                    break
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return flag
    }

    private fun createBannerSlider(jsonObject: JSONObject, key: String) {
        try {
            var binding: MBannerSliderBinding = DataBindingUtil.inflate(context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.m_banner_slider,
                null,
                false
            )
            var backcolor=""
            if (isLightModeOn()&&jsonObject.has("panel_background_color")){
                backcolor=  JSONObject(jsonObject.getString("panel_background_color")).getString("color")
                binding.root.setBackgroundColor(Color.parseColor(backcolor))
            }
            var dotmargin=20
            var cornerradius = 0f
            var position=0
            var delaytime=5000
            var period=5000
            var padding=0
            var roundflag=false
            var ratio="H,700:394"
            if (jsonObject.has("banner_shape")) {
                if(jsonObject.has("container_padding")) {
                    padding=Integer.parseInt(jsonObject.getString("container_padding"))
                }
                var params=(binding.banners.layoutParams as ConstraintLayout.LayoutParams)
                when (jsonObject.getString("banner_shape")) {
                    "bs1-l1" -> {
                        params.dimensionRatio = "H,3:1"
                        ratio = "H,3:1"
                    }
                    "bs1-l2" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,3:1"
                         ratio = "H,3:1"
                         dotmargin=0
                         position=1
                         padding=4
                         roundflag=true
                         SecondbannerAnimation(binding.banners)
                    }
                    "bs1-l3" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,3:1"
                        ratio = "H,3:1"
                        dotmargin=0
                        position=1
                        padding=4
                        roundflag=true
                        bannerAnimation(binding.banners)
                    }
                    "bs2-l1" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,16:9"
                        ratio = "H,16:9"
                    }
                    "bs2-l2" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,16:9"
                        ratio = "H,16:9"
                        dotmargin=0
                        position=1
                        padding=4
                        roundflag=true
                        SecondbannerAnimation(binding.banners)
                    }
                    "bs2-l3" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,16:9"
                        ratio = "H,16:9"
                        dotmargin=0
                        position=1
                        padding=4
                        roundflag=true
                        bannerAnimation(binding.banners)
                    }
                    "bs3-l1" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,1:1"
                        ratio = "H,1:1"
                    }
                    "bs3-l2" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,1:1"
                        ratio = "H,1:1"
                        dotmargin=0
                        position=1
                        padding=4
                        roundflag=true
                        SecondbannerAnimation(binding.banners)
                    }
                    "bs3-l3" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,1:1"
                        ratio = "H,1:1"
                        dotmargin=0
                        position=1
                        padding=4
                        roundflag=true
                        bannerAnimation(binding.banners)
                    }
                    "bs4-l1" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,1.4:1"
                        ratio = "H,1.4:1"
                    }
                    "bs4-l2" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,1.4:1"
                        ratio = "H,1.4:1"
                        dotmargin=0
                        position=1
                        padding=4
                        roundflag=true
                        SecondbannerAnimation(binding.banners)
                    }
                    "bs4-l3" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,1.4:1"
                        ratio = "H,1.4:1"
                        dotmargin=0
                        position=1
                        padding=4
                        roundflag=true
                        bannerAnimation(binding.banners)
                    }
                    "bs5-l1" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,1.5:2"
                        ratio = "H,1.5:2"
                    }
                    "bs5-l2" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,1.5:2"
                        ratio = "H,1.5:2"
                        dotmargin=0
                        position=1
                        padding=4
                        roundflag=true
                        SecondbannerAnimation(binding.banners)
                    }
                    "bs5-l3" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = "H,1.5:2"
                        ratio = "H,1.5:2"
                        dotmargin=0
                        position=1
                        padding=4
                        roundflag=true
                        bannerAnimation(binding.banners)
                    }
                }
            }
            if(jsonObject.has("corner_radius")) {
                cornerradius  = applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, getCornerRadius(jsonObject.getString("corner_radius")),
                    context!!.resources.displayMetrics
                )
            }
            val listType: Type = object : TypeToken<List<MageBanner?>?>() {}.getType()
            var items:ArrayList<MageBanner> =Gson().fromJson(jsonObject.getJSONArray("items").toString(), listType)
            binding.banners.adapter = HomePageBanner(context.supportFragmentManager, context,
                Gson().fromJson(jsonObject.getJSONArray("items").toString(), listType)
                , "bannerslider",cornerradius!!,padding,backcolor,ratio)
            if (jsonObject.getString("item_dots").equals("1")) {
                var params=(binding.dotscontainer.layoutParams as ConstraintLayout.LayoutParams)
                params.topMargin=dotmargin
                binding.dotscontainer.layoutParams  = params
                var activecolor="#FFFFFF"
                var inactivecolor="#F2F2F2"
                if(isLightModeOn()){
                     activecolor=JSONObject(jsonObject.getString("active_dot_color")).getString("color")
                     inactivecolor=JSONObject(jsonObject.getString("inactive_dot_color")).getString("color")
                }
                binding.indicator.initDots(items.size,activecolor,inactivecolor)
                binding.dotscontainer.visibility = View.VISIBLE
            }
            binding.banners.postDelayed({  binding.banners.setCurrentItem(position,true) }, 10)
            binding.banners.addOnPageChangeListener(object :OnPageChangeListener{
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageSelected(position: Int) {
                    Log.i("BannerPosition",""+position)
                    var selection=position
                    if (roundflag){
                        if(position== (binding.banners.adapter as HomePageBanner).items.size-2){
                            (binding.banners.adapter as HomePageBanner).items.addAll(items)
                            (binding.banners.adapter as HomePageBanner).notifyDataSetChanged()
                        }
                        if (position>=items.size){
                            selection=position % items.size
                        }
                    }
                    if(binding.dotscontainer!!.isVisible){
                        binding.indicator.setDotSelection(selection)
                    }
                }
                override fun onPageScrollStateChanged(state: Int) {}
            })
            var i = position
            val timer = Timer()
            timer.scheduleAtFixedRate(timerTask {
                CoroutineScope(Dispatchers.Main).launch {
                    if (i< (binding.banners.adapter as HomePageBanner).items.size){
                            binding.banners.setCurrentItem( i++,true)
                            if (i == (binding.banners.adapter as HomePageBanner).items.size) {
                                i = 0
                            }
                    }
                }
            }, delaytime.toLong(), period.toLong())
            linkedHashMap.put(key, binding.root)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    private fun bannerAnimation(banners: ViewPager) {
        banners.clipChildren = false
        banners.clipToPadding = false
        banners.offscreenPageLimit=3
        banners.setPadding(100, 0, 100, 0)
        banners.pageMargin = 0
        banners.setPageTransformer(false) { page, position ->
            var r= 1-Math.abs(position)
            page.scaleY=0.85f + r * 0.15f
        }
    }

    private fun SecondbannerAnimation(banners: ViewPager) {
        banners.clipChildren = false
        banners.clipToPadding = false
        banners.offscreenPageLimit=3
        banners.setPadding(100, 0, 100, 0)
    }


    class MyCount : CountDownTimer {
        var productSlider: ProductSlider
        var format: String

        constructor(
            millisInFuture: Long,
            countDownInterval: Long,
            productSlider: ProductSlider,
            format: String
        ) : super(millisInFuture, countDownInterval) {
            this.productSlider = productSlider
            this.format = format
        }

        override fun onFinish() {
            productSlider.timericon = View.GONE
        }

        override fun onTick(millisUntilFinished: Long) {
            var millis = millisUntilFinished

            productSlider.day = "" + (TimeUnit.MILLISECONDS.toDays(millis))
            productSlider.hour =
                "" + (TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(
                    TimeUnit.MILLISECONDS.toDays(millis)
                ))
            productSlider.minute =
                "" + (TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(millis)
                ))
            productSlider.secs =
                "" + (TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(millis)
                ))
            if (productSlider.day!!.length == 1) {
                productSlider.day = "0" + productSlider.day
            }
            if (productSlider.hour!!.length == 1) {
                productSlider.hour = "0" + productSlider.hour
            }
            if (productSlider.minute!!.length == 1) {
                productSlider.minute = "0" + productSlider.minute
            }
            if (productSlider.secs!!.length == 1) {
                productSlider.secs = "0" + productSlider.secs
            }
            /*var hms =
                " " + (TimeUnit.MILLISECONDS.toDays(millis)) + " Day $format " + (TimeUnit.MILLISECONDS.toHours(
                    millis
                ) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis))) + " H $format " + (TimeUnit.MILLISECONDS.toMinutes(
                    millis
                ) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))) + " M $format " + (TimeUnit.MILLISECONDS.toSeconds(
                    millis
                ) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))) + " S"
            productSlider.timertext = hms*/
        }
    }

    private val api = MutableLiveData<ApiResponse>()
    private val bestapi = MutableLiveData<ApiResponse>()
    fun getApiResponse(): MutableLiveData<ApiResponse> {
        getRecommendations("trending", api)
        return api
    }

    fun getBestApiResponse(): MutableLiveData<ApiResponse> {
        getRecommendations("bestsellers", bestapi)
        return bestapi
    }

    fun getRecommendations(recommendationType: String, api: MutableLiveData<ApiResponse>) {
        RetrofitUrlManager.getInstance().putDomain("douban", Urls.PERSONALISED)
        try {
            var query = InnerData()
            query.id = recommendationType
            query.maxRecommendations = 12
            query.recommendationType = recommendationType
            var body = Body()
            body.queries = mutableListOf(query)

            disposables.add(repository.getRecommendation(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        api.setValue(ApiResponse.success(result))
                    },
                    { throwable ->
                        // customLoader!!.dismiss()
                        api.setValue(ApiResponse.error(throwable))
                    }
                ))

//            doRetrofitCall(repository.getRecommendation(body), disposables, customResponse = object : CustomResponse {
//                override fun onSuccessRetrofit(result: JsonElement) {
//                    api.setValue(ApiResponse.success(result))
//                }
//
//                override fun onErrorRetrofit(error: Throwable) {
//                    api.setValue(ApiResponse.error(error))
//                }
//            }, context = context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun NResponse(
        client_id: String,
        client_secret: String,
        grant_type: String
    ): MutableLiveData<ApiResponse> {
        yotpoauthenticateapi(client_id, client_secret, grant_type)
        return getyotpoauthenticate
    }

    fun yotpoauthenticateapi(client_id: String, client_secret: String, grant_type: String) {
        doRetrofitCall(
            repository.yotpoauthentiate(client_id, client_secret, grant_type),
            disposables,
            customResponse = object : CustomResponse {
                override fun onSuccessRetrofit(result: JsonElement) {
                    getyotpoauthenticate.value = ApiResponse.success(result)
                }

                override fun onErrorRetrofit(error: Throwable) {
                    getyotpoauthenticate.value = ApiResponse.error(error)
                }
            },
            context = context
        )
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun getFeedToken() {
        viewModelScope.launch {
            customLoader = CustomLoader(context)
            try {
                var result = async(Dispatchers.IO) {
                    URL(Urls.Instatokenurl + Urls(MyApplication.context).mid).readText()
                }
                var feed = result.await()
                Log.i("Instatokenurl", "" + Urls.Instatokenurl + Urls(MyApplication.context).mid)
                Log.i("Instatokenurl", "" + feed)
                var json = JSONObject(feed)
                when (json.getBoolean("success")) {
                    true -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            feedtoken.value = json.getString("data")
                        }
                    }
                    false -> {
                        Log.i("InstatokenError", "" + json.getString("msg"))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun RefreshHomePage(){
        try{
            MagePrefs.clearHomePageData()
            viewModelScope.launch(Dispatchers.IO){
                repository.deleteAllHomePageProduct()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}
