package com.rasmishopping.app.utils

//import com.shopify.mltranslation.Translation
import android.R
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.*
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import cn.pedant.SweetAlert.SweetAlertDialog
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.shopify.buy3.HttpCachePolicy
import com.shopify.buy3.Storefront
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.customviews.MageNativeTextView
import com.rasmishopping.app.dbconnection.entities.AppLocalData
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.models.VariantData
import com.rasmishopping.app.sharedprefsection.MagePrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.HashMap
import java.util.concurrent.TimeUnit


object Constant {
    var ispersonalisedEnable: Boolean = true
    var namespace: String = "my_fields"
    var key = "filedemo"
    var previous: VariantData? = null
    var flits_token = "b7e0df7341529fe4a782bc9ca3931f0a"
    var flits_userId = "24088"
    var current: VariantData? = null
     lateinit var firebaseAnalytics: FirebaseAnalytics
    var policy: HttpCachePolicy.ExpirePolicy =
        HttpCachePolicy.Default.CACHE_FIRST.expireAfter(5, TimeUnit.SECONDS)
    var appLocalData: AppLocalData = AppLocalData()
    var directive: MutableList<Storefront.InContextDirective>? = null
    fun getProgressDialog(context: Context, msg: String): ProgressDialog {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(msg)
        progressDialog.setCancelable(false)
        return progressDialog
    }

    fun activityTransition(context: Context) {
        (context as Activity).overridePendingTransition(
            R.anim.slide_in_left,
            R.anim.slide_out_right
        )
    }

    fun showDialog(
        context: Context, data: String, title: String,
        onSuccess: ((Boolean) -> Unit?)? = null
    ) {
        var pinalertDialog = SweetAlertDialog(context, SweetAlertDialog.NORMAL_TYPE)
        pinalertDialog.titleText = title
        pinalertDialog.contentText = data
        pinalertDialog.setConfirmButton("Copy", SweetAlertDialog.OnSweetClickListener {
            val clipboard: ClipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(title, data)
            clipboard.setPrimaryClip(clip)
            pinalertDialog.dismissWithAnimation()
            if (onSuccess != null)
                onSuccess(true)
        })
        pinalertDialog.show()
    }

    fun printHashKey(pContext: Context) {
        try {
            val info = pContext.packageManager.getPackageInfo(
                pContext.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey: String = String(Base64.encode(md.digest(), 0))
                Log.i(TAG, "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "printHashKey()", e)
        } catch (e: Exception) {
            Log.e(TAG, "printHashKey()", e)
        }
    }

    fun getCurrency(country: String): String {
        var json =
            "{\"AC\":\"USD\",\"AD\":\"EUR\",\"AE\":\"AED\",\"AF\":\"AFN\",\"AG\":\"XCD\",\"AI\":\"XCD\",\"AL\":\"ALL\",\"AM\":\"AMD\",\"AO\":\"AOA\",\"AQ\":\"\",\"AR\":\"ARS\",\"AS\":\"USD\",\"AT\":\"EUR\",\"AU\":\"AUD\",\"AW\":\"AWG\",\"AX\":\"EUR\",\"AZ\":\"AZN\",\"BA\":\"BAM\",\"BB\":\"BBD\",\"BD\":\"BDT\",\"BE\":\"EUR\",\"BF\":\"XOF\",\"BG\":\"BGN\",\"BH\":\"BHD\",\"BI\":\"BIF\",\"BJ\":\"XOF\",\"BL\":\"EUR\",\"BM\":\"BMD\",\"BN\":\"BND\",\"BO\":\"BOB\",\"BQ\":\"USD\",\"BR\":\"BRL\",\"BS\":\"BSD\",\"BT\":\"BTN\",\"BV\":\"NOK\",\"BW\":\"BWP\",\"BY\":\"BYR\",\"BZ\":\"BZD\",\"CA\":\"CAD\",\"CC\":\"AUD\",\"CD\":\"CDF\",\"CF\":\"XAF\",\"CG\":\"XAF\",\"CH\":\"CHF\",\"CI\":\"XOF\",\"CK\":\"NZD\",\"CL\":\"CLP\",\"CM\":\"XAF\",\"CN\":\"CNY\",\"CO\":\"COP\",\"CR\":\"CRC\",\"CU\":\"CUP\",\"CV\":\"CVE\",\"CW\":\"ANG\",\"CX\":\"AUD\",\"CY\":\"EUR\",\"CZ\":\"CZK\",\"DE\":\"EUR\",\"DJ\":\"DJF\",\"DK\":\"DKK\",\"DM\":\"XCD\",\"DO\":\"DOP\",\"DZ\":\"DZD\",\"EC\":\"USD\",\"EE\":\"EUR\",\"EG\":\"EGP\",\"EH\":\"MAD\",\"ER\":\"ERN\",\"ES\":\"EUR\",\"ET\":\"ETB\",\"FI\":\"EUR\",\"FJ\":\"FJD\",\"FK\":\"FKP\",\"FM\":\"USD\",\"FO\":\"DKK\",\"FR\":\"EUR\",\"GA\":\"XAF\",\"GB\":\"GBP\",\"GD\":\"XCD\",\"GE\":\"GEL\",\"GF\":\"EUR\",\"GG\":\"GBP\",\"GH\":\"GHS\",\"GI\":\"GIP\",\"GL\":\"DKK\",\"GM\":\"GMD\",\"GN\":\"GNF\",\"GP\":\"EUR\",\"GQ\":\"XAF\",\"GR\":\"EUR\",\"GS\":\"GBP\",\"GT\":\"GTQ\",\"GU\":\"USD\",\"GW\":\"XOF\",\"GY\":\"GYD\",\"HK\":\"HKD\",\"HM\":\"AUD\",\"HN\":\"HNL\",\"HR\":\"HRK\",\"HT\":\"HTG\",\"HU\":\"HUF\",\"ID\":\"IDR\",\"IE\":\"EUR\",\"IL\":\"ILS\",\"IM\":\"GBP\",\"IN\":\"INR\",\"IO\":\"USD\",\"IQ\":\"IQD\",\"IR\":\"IRR\",\"IS\":\"ISK\",\"IT\":\"EUR\",\"JE\":\"GBP\",\"JM\":\"JMD\",\"JO\":\"JOD\",\"JP\":\"JPY\",\"KE\":\"KES\",\"KG\":\"KGS\",\"KH\":\"KHR\",\"KI\":\"AUD\",\"KM\":\"KMF\",\"KN\":\"XCD\",\"KP\":\"KPW\",\"KR\":\"KRW\",\"KW\":\"KWD\",\"KY\":\"KYD\",\"KZ\":\"KZT\",\"LA\":\"LAK\",\"LB\":\"LBP\",\"LC\":\"XCD\",\"LI\":\"CHF\",\"LK\":\"LKR\",\"LR\":\"LRD\",\"LS\":\"LSL\",\"LT\":\"EUR\",\"LU\":\"EUR\",\"LV\":\"EUR\",\"LY\":\"LYD\",\"MA\":\"MAD\",\"MC\":\"EUR\",\"MD\":\"MDL\",\"ME\":\"EUR\",\"MF\":\"EUR\",\"MG\":\"MGA\",\"MH\":\"USD\",\"MK\":\"MKD\",\"ML\":\"XOF\",\"MM\":\"MMK\",\"MN\":\"MNT\",\"MO\":\"MOP\",\"MP\":\"USD\",\"MQ\":\"EUR\",\"MR\":\"MRO\",\"MS\":\"XCD\",\"MT\":\"EUR\",\"MU\":\"MUR\",\"MV\":\"MVR\",\"MW\":\"MWK\",\"MX\":\"MXN\",\"MY\":\"MYR\",\"MZ\":\"MZN\",\"NA\":\"NAD\",\"NC\":\"XPF\",\"NE\":\"XOF\",\"NF\":\"AUD\",\"NG\":\"NGN\",\"NI\":\"NIO\",\"NL\":\"EUR\",\"NO\":\"NOK\",\"NP\":\"NPR\",\"NR\":\"AUD\",\"NU\":\"NZD\",\"NZ\":\"NZD\",\"OM\":\"OMR\",\"PA\":\"PAB\",\"PE\":\"PEN\",\"PF\":\"XPF\",\"PG\":\"PGK\",\"PH\":\"PHP\",\"PK\":\"PKR\",\"PL\":\"PLN\",\"PM\":\"EUR\",\"PN\":\"NZD\",\"PR\":\"USD\",\"PS\":\"ILS\",\"PT\":\"EUR\",\"PW\":\"USD\",\"PY\":\"PYG\",\"QA\":\"QAR\",\"RE\":\"EUR\",\"RO\":\"RON\",\"RS\":\"RSD\",\"RU\":\"RUB\",\"RW\":\"RWF\",\"SA\":\"SAR\",\"SB\":\"SBD\",\"SC\":\"SCR\",\"SD\":\"SDG\",\"SE\":\"SEK\",\"SG\":\"SGD\",\"SH\":\"SHP\",\"SI\":\"EUR\",\"SJ\":\"NOK\",\"SK\":\"EUR\",\"SL\":\"SLL\",\"SM\":\"EUR\",\"SN\":\"XOF\",\"SO\":\"SOS\",\"SR\":\"SRD\",\"SS\":\"SSP\",\"ST\":\"STD\",\"SV\":\"USD\",\"SX\":\"ANG\",\"SY\":\"SYP\",\"SZ\":\"SZL\",\"TC\":\"USD\",\"TD\":\"XAF\",\"TF\":\"EUR\",\"TG\":\"XOF\",\"TH\":\"THB\",\"TJ\":\"TJS\",\"TK\":\"NZD\",\"TL\":\"USD\",\"TM\":\"TMT\",\"TN\":\"TND\",\"TO\":\"TOP\",\"TR\":\"TRY\",\"TT\":\"TTD\",\"TV\":\"AUD\",\"TW\":\"TWD\",\"TZ\":\"TZS\",\"UA\":\"UAH\",\"UG\":\"UGX\",\"UM\":\"USD\",\"US\":\"USD\",\"UY\":\"UYU\",\"UZ\":\"UZS\",\"VA\":\"EUR\",\"VC\":\"XCD\",\"VE\":\"VEF\",\"VG\":\"USD\",\"VI\":\"USD\",\"VN\":\"VND\",\"VU\":\"VUV\",\"WF\":\"XPF\",\"WS\":\"WST\",\"XK\":\"EUR\",\"YE\":\"YER\",\"YT\":\"EUR\",\"ZA\":\"ZAR\",\"ZM\":\"ZMW\",\"ZW\":\"ZWL\"}"
        val obj: JSONObject = JSONObject(json)
        return obj.getString(country)
    }

    fun logAddToWishlistEvent(
        contentData: String?,
        contentId: String?,
        contentType: String?,
        currency: String?,
        price: Double,
        context: Context
    ) {
        val logger = AppEventsLogger.newLogger(context)
        val params = Bundle()
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData)
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId)
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType)
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency)
        logger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_WISHLIST, price, params)
    }

    fun logAddToCartEvent(
        contentData: String?,
        contentId: String?,
        contentType: String?,
        currency: String?,
        price: Double,
        context: Context
    ) {
        val logger = AppEventsLogger.newLogger(context)
        val params = Bundle()
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData)
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId)
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType)
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency)
        logger.logEvent(AppEventsConstants.EVENT_NAME_ADDED_TO_CART, price, params)
    }

    fun logCompleteRegistrationEvent(registrationMethod: String?, context: Context) {
        val logger = AppEventsLogger.newLogger(context)
        val params = Bundle()
        params.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, registrationMethod)
        logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, params)
    }

    fun logViewContentEvent(
        contentType: String?,
        contentData: String?,
        contentId: String?,
        currency: String?,
        price: Double,
        context: Context
    ) {
        val logger = AppEventsLogger.newLogger(context)
        val params = Bundle()
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType)
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT, contentData)
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, contentId)
        params.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency)
        logger.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, price, params)
    }

    fun checkInternetConnection(context: Context): Boolean {
        val connectivity = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity == null) {
            return false
        } else {
            val info = connectivity.allNetworkInfo
            if (info != null) {
                for (i in info.indices) {
                    if (info[i].state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun internationalPricing(): List<Storefront.InContextDirective> {
        if(directive==null){
            try{
                directive = mutableListOf()
                val inContextDirective = Storefront.InContextDirective()
                if(!MagePrefs.getCountryCode().isNullOrEmpty()){
                    inContextDirective.country = Storefront.CountryCode.valueOf(MagePrefs.getCountryCode().toString())
                }
                if(MagePrefs.getLanguage()!=null) {
                    inContextDirective.language = Storefront.LanguageCode.valueOf(MagePrefs.getLanguage().toString().uppercase())
                }
                directive!!.add(inContextDirective)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
        return directive!!
    }

    fun hideKeyboard(context: Context) {
        val inputManager: InputMethodManager =
            context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.toggleSoftInput(0, 0)
    }
    fun imageConfiguration(viewType:String): Storefront.ImageTransformInput{
        var imageTransformInput= Storefront.ImageTransformInput()
        when(viewType){
            "homepage"->{
                imageTransformInput.maxWidth=300
                imageTransformInput.maxHeight=300
                imageTransformInput.preferredContentType= Storefront.ImageContentType.PNG
            }
            "productlist","wishlist","searchlist","colllection"->{
                imageTransformInput.maxWidth=512
                imageTransformInput.maxHeight=512
                imageTransformInput.preferredContentType= Storefront.ImageContentType.PNG
                imageTransformInput.scale= 3
            }
            "cartlist"->{
                imageTransformInput.maxWidth=150
                imageTransformInput.maxHeight=150
                imageTransformInput.preferredContentType= Storefront.ImageContentType.PNG
                imageTransformInput.scale= 3
            }
            "productview"->{
                imageTransformInput.maxWidth=700
                imageTransformInput.maxHeight=700
                imageTransformInput.preferredContentType= Storefront.ImageContentType.PNG
                imageTransformInput.scale= 3
            }

        }
        return imageTransformInput
    }
//    *************************ML Trasnslation ***************************************

    /*
    Do uncomment this code from here and homepage when you install ml translation module.


     */
    fun translateField(textvalue: String, mageNativeTextView: MageNativeTextView) {
//        if (SplashViewModel.featuresModel.mltranslation && !MagePrefs.getLanguage().equals("en")) {
//            CoroutineScope(Dispatchers.Main).launch {
//                delay(1000)
//                Translation.translatetext(
//                    textvalue,
//                    MagePrefs.getLanguage()!!,
//                    object : Translation.TranslationCallback {
//                        override fun transdata(textvalue: String) {
//                            mageNativeTextView.text = textvalue
//                        }
//                    })
//            }
//
//        }
    }

    fun translateField(textvalue: String, menuItem: MenuItem) {
//        if (SplashViewModel.featuresModel.mltranslation && !MagePrefs.getLanguage().equals("en")) {
//            CoroutineScope(Dispatchers.Main).launch {
//                delay(1000)
//                Translation.translatetext(
//                    textvalue,
//                    MagePrefs.getLanguage()!!,
//                    object : Translation.TranslationCallback {
//                        override fun transdata(textvalue: String) {
//                            menuItem.title = textvalue
//                        }
//                    })
//            }
//
//        }
    }
    fun applyColor(textview: MageNativeTextView,text:String){
        try {
            var drawable=textview.background as GradientDrawable
            textview.text=text
            var backcolor:String=NewBaseActivity.themeColor
            var textcolor:String=NewBaseActivity.textColor
            when(NewBaseActivity.themeColor){
                "#FFFFFF","#ffffff","#000000"->{
                    when(NewBaseActivity.textColor){
                        "#FFFFFF","#ffffff","#000000"->{
                            if(HomePageViewModel.isLightModeOn()){
                                backcolor="#000000"
                                textcolor="#FFFFFF"
                            }else{
                                backcolor="#FFFFFF"
                                textcolor="#000000"
                            }
                        }
                        else->{
                            backcolor=NewBaseActivity.textColor
                            textcolor=NewBaseActivity.themeColor
                        }
                    }
                }
            }
            drawable.setColor(Color.parseColor(backcolor))
            drawable.setStroke(2, Color.parseColor(backcolor))
            textview.setTextColor(Color.parseColor(textcolor))
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    fun getSearchPattern(keyword:String):String{
        var finalpattern=""
        var generalSearchEnable=true
        var includeTitle=false
        var includeDescription=false
        var includeProductType=false
        var includeProductTag=false
        try {
            if(generalSearchEnable)
                finalpattern+="$keyword OR "
            if (includeTitle)
                finalpattern+="title:$keyword OR "
            if (includeDescription)
                finalpattern+="description:$keyword OR "
            if (includeProductType)
                finalpattern+="product_type:$keyword OR "
            if (includeProductTag)
                finalpattern+="tag:$keyword"
        }catch (e:Exception){
            e.printStackTrace()
        }
        return finalpattern
    }
    fun initializeFeatures(){
        try {
                SplashViewModel.featuresModel.in_app_wishlist = false
                SplashViewModel.featuresModel.nativeOrderView = false
                SplashViewModel.featuresModel.rtl_support = false
                SplashViewModel.featuresModel.product_share = false
                SplashViewModel.featuresModel.multi_currency = false
                SplashViewModel.featuresModel.multi_language = false
                SplashViewModel.featuresModel.abandoned_cart_compaigns = false
                SplashViewModel.featuresModel.outOfStock = false
                SplashViewModel.featuresModel.reOrderEnabled = false
                SplashViewModel.featuresModel.recommendedProducts = false
                SplashViewModel.featuresModel.ardumented_reality = false
                SplashViewModel.featuresModel.addCartEnabled = false
                SplashViewModel.featuresModel.deep_linking = false
                SplashViewModel.featuresModel.qr_code_search_scanner = false
                SplashViewModel.featuresModel.showBottomNavigation = false
                SplashViewModel.featuresModel.socialloginEnable = false
                SplashViewModel.featuresModel.native_checkout = false

        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    fun initializeIntegrations(){
        try {
                SplashViewModel.featuresModel.yoptoLoyalty = false
                SplashViewModel.featuresModel.algoliasearch = false
                SplashViewModel.featuresModel.returnprime = false
                SplashViewModel.featuresModel.shipway_order_tracking = false
                SplashViewModel.featuresModel.Enable_flits_App = false
                SplashViewModel.featuresModel.aliReviews = false
                SplashViewModel.featuresModel.productReview = false
                SplashViewModel.featuresModel.judgemeProductReview = false
                SplashViewModel.featuresModel.sizeChartVisibility = false
                SplashViewModel.featuresModel.tidioChat = false
                SplashViewModel.featuresModel.zenDeskChat = false
                SplashViewModel.featuresModel.zapietEnable = false
                SplashViewModel.featuresModel.langify = false
                SplashViewModel.featuresModel.langshop = false
                SplashViewModel.featuresModel.weglot = false
                SplashViewModel.featuresModel.ai_product_reccomendaton = false
                SplashViewModel.featuresModel.WholeSale_Pricing=false
                SplashViewModel.featuresModel.shipway_order_tracking = false
                SplashViewModel.featuresModel.enablebackInStock = false
                SplashViewModel.featuresModel.enableRewardify = false
                SplashViewModel.featuresModel.liveSale = false
                SplashViewModel.featuresModel.whatsappChat = false
                SplashViewModel.featuresModel.fb_wt = false
                SplashViewModel.featuresModel.fbMessenger = false
                SplashViewModel.featuresModel.fb_wt = false
                SplashViewModel.featuresModel.enableInstafeed = false
                SplashViewModel.featuresModel.firstSale = false
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    private fun vibrateOnce(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vib = vibratorManager.defaultVibrator;
            vib.vibrate(VibrationEffect.createOneShot(100, 1))
        } else {
            val vib = context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
            vib.vibrate(500)
        }
    }

    fun SlideAnimation(context: Context, view: TextView) {

        val anim1 = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f)
        val anim2 = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f)
        anim1.interpolator = AccelerateInterpolator()
        anim2.interpolator = AccelerateInterpolator()
        anim1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                anim2.start()
            }
        })
        anim1.start()
        anim1.duration = 10L
        vibrateOnce(context)
    }

    fun WishlistAnimation(activity: Context, view: ImageView) {

        val animZoomIn = AnimationUtils.loadAnimation(
            activity,
            com.rasmishopping.app.R.anim.heart_new_anim
        )

        view.startAnimation(animZoomIn)
    }
//    ********************************************************************************

    fun isUsingNightModeResources(context: Context): Boolean {

        val currentNightMode: Int = (context.getResources().getConfiguration().uiMode
                and Configuration.UI_MODE_NIGHT_MASK)
        return when (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
    }


    fun FirebaseEvent_AddtoWishlist(product_id:String,quantity:String)
    {      firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_WISHLIST) {
            param(FirebaseAnalytics.Param.ITEM_ID, product_id)
            param(FirebaseAnalytics.Param.QUANTITY, quantity!!)
        }
    }
    fun FirebaseEvent_AddtoCart(product_id:String,quantity:String)
    {      firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_TO_CART) {
            param(FirebaseAnalytics.Param.ITEM_ID, product_id)
            param(FirebaseAnalytics.Param.QUANTITY, quantity.toString())
        }
    }
    fun FirebaseEvent_ViewItem(currency:String,price:String,title:String,handle:String)
    {
        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM) {
            param(FirebaseAnalytics.Param.CURRENCY, currency)
            param(FirebaseAnalytics.Param.PRICE, price)
            param(FirebaseAnalytics.Param.ITEM_NAME, title)
            param("Product_Handle", handle)

        }
    }
    fun FirebaseEvent_SearchTerm(keyword:String)
    {
        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH) {
            param(FirebaseAnalytics.Param.SEARCH_TERM, keyword)

        }
    }
    fun FirebaseEvent_CategoryClicked(category_title:String)
    {
        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent("Category Clicked") {
            param(FirebaseAnalytics.Param.ITEM_CATEGORY, category_title)

        }
    }
    fun FirebaseEvent_AppOpen(source:String)
    {
        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN) {
            param(FirebaseAnalytics.Param.SOURCE, source)

        }
    }
    fun FirebaseEvent_SignUp(email:String)
    {
        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
            param("UserEmail", email)

        }
    }
    fun FirebaseEvent_SignIn(email:String)
    {
        firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param("UserEmail", email)

        }
    }
    fun FirebaseEvent_Purchase(currency:String,price:String,trans_id:String,end_date:String)
    {   firebaseAnalytics = Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
            param(FirebaseAnalytics.Param.CURRENCY, currency)
            param(FirebaseAnalytics.Param.PRICE, price)
            param(FirebaseAnalytics.Param.TRANSACTION_ID, trans_id)
            param(FirebaseAnalytics.Param.END_DATE, end_date)
        }
    }


}
