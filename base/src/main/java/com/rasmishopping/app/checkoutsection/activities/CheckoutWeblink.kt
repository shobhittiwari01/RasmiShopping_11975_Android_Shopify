package com.rasmishopping.app.checkoutsection.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.checkoutsection.viewmodels.CheckoutWebLinkViewModel
import com.rasmishopping.app.databinding.MWebpageBinding
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.loader_section.CustomLoader
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.*
import org.json.JSONObject
import javax.inject.Inject

class CheckoutWeblink : NewBaseActivity() {
    private var webView: WebView? = null
    private var binding: MWebpageBinding? = null
    private var currentUrl: String? = null
    private var id: String? = null
    private var postData: String? = null
    private var customLoader: CustomLoader? = null
    private val TAG = "CheckoutWeblink"
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    @Inject
    lateinit var factory: ViewModelFactory
    private var model: CheckoutWebLinkViewModel? = null
    private var count: Int = 1
    private  var flag=false
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_webpage, content, true)
        (application as MyApplication).mageNativeAppComponent!!.doCheckoutWeblinkActivityInjection(this)
        firebaseAnalytics = Firebase.analytics
        model = ViewModelProvider(this, factory).get(CheckoutWebLinkViewModel::class.java)
        model!!.responseLiveData.observe(this,{
            updatePurchase(it)
        })
        model!!.context = this
        showTittle(resources.getString(R.string.checkout))
        showBackButton()
        customLoader = CustomLoader(this)
        customLoader?.show()
        webView = binding!!.webview
        currentUrl = intent.getStringExtra("link")
        id = intent.getStringExtra("id")
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.loadWithOverviewMode = true
        webView!!.settings.useWideViewPort = true
        webView!!.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        } else {
            val cookieSyncMngr = CookieSyncManager.createInstance(this)
            cookieSyncMngr.startSync()
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookie()
            cookieManager.removeSessionCookie()
            cookieSyncMngr.stopSync()
            cookieSyncMngr.sync()
        }
        webView!!.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
            var hr = view as WebView
            Log.i("MageNative.Saif",""+hr.hitTestResult.extra)//string on which clicked
            Log.i("MageNative.Saif",""+hr.hitTestResult.type) //html element which is clicked
            return@OnTouchListener false
        })
        setUpWebViewDefaults(webView!!)
        if (model!!.isLoggedIn) {
            try {

                /* String parts[]=  currentUrl.split("/");
                String checkouturl="https://"+getResources().getString(R.string.shopdomain)+"/account/login";
                postData = "form_type=" + URLEncoder.encode("customer_login", "UTF-8")
                        + "&customer[email]=" + URLEncoder.encode(data.getEmail(), "UTF-8")
                        + "&order=" + URLEncoder.encode(parts[parts.length-1], "UTF-8");
                webView.postUrl(checkouturl,postData.getBytes());*/

                val map = HashMap<String, String?>()
                map.put(
                    "X-Shopify-Customer-Access-Token",
                    model?.customeraccessToken?.customerAccessToken
                )
                /* val checkouturl = "https://" + resources.getString(R.string.shopdomain) + "/account/login"
                 postData = ("checkout_url=" + URLEncoder.encode(currentUrl!!.replace("https://" + resources.getString(R.string.shopdomain), ""), "UTF-8") +
                         "&form_type=" + URLEncoder.encode("customer_login", "UTF-8")
                         + "&customer[email]=" + URLEncoder.encode("asd@asd.com", "UTF-8")
                         + "&customer[password]=" + URLEncoder.encode("asdcxzasd", "UTF-8"))

                 Log.i("checkout", checkouturl)
                 Log.i("checkout", postData)

                 webView!!.postUrl(checkouturl, postData!!.toByteArray())*/
                webView!!.loadUrl(currentUrl!!,map)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            webView!!.loadUrl(currentUrl!!)
        }
        webView!!.webChromeClient = WebChromeClient()
    }

    private fun updatePurchase(it: ApiResponse?) {
        try {
            when(it!!.status){
                Status.SUCCESS->{
                    var obj= JSONObject(it?.data.toString())
                    if(obj.has("success")&&obj.getBoolean("success")){
                        if(obj.has("data")){
                            var orderid=obj.getJSONObject("data").getString("id")
                            var price=obj.getJSONObject("data").getString("total_price")
                            var currency=obj.getJSONObject("data").getString("currency")
                            var time=obj.getJSONObject("data").getString("created_at")
                            if (SplashViewModel.featuresModel.firebaseEvents) {
                                Constant.FirebaseEvent_Purchase(currency,price,orderid,time)
                            }
                        }
                    }

                }
                Status.ERROR->{
                    it.error!!.printStackTrace()
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebViewDefaults(webView: WebView) {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.i("URL", "" + description)
                customLoader?.dismiss()
            }

            override fun onLoadResource(view: WebView, url: String) {
                Log.i("URL", "" + url)
                if (url.contains("thank_you")) {
                    model!!.setOrder(Urls((application as MyApplication)).mid, id)
                    model!!.deleteCart()
                    MagePrefs.clearCouponCode()
                    flag=true
                    InAppReviewFlow()
                    //InAppReviewFlow()
                   /* var isLocalUrl = false
                    try {
                        val givenUrl = URL(url)
                        val host: String = givenUrl.getHost()
                        if (host.contains("myapp.com")) isLocalUrl = true
                    } catch (e: MalformedURLException) {
                    }

                    return if (isLocalUrl) super.shouldOverrideUrlLoading(view, url) else {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        true
                    }*/
                   /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Handler().postDelayed({
                            if (count == 1) {
                                startActivity(
                                    Intent(
                                        this@CheckoutWeblink,
                                        OrderSuccessActivity::class.java
                                    )
                                )
                                finishAffinity()
                                Constant.activityTransition(this@CheckoutWeblink)
                                MagePrefs.clearCouponCode()
                            }
                            count++
                        }, 3000, 10000)
                    }*/
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                customLoader?.dismiss()
                Log.i("pageURL", "" + url)
                if (url.contains("thank_you")) {
                    model!!.setOrder(Urls((application as MyApplication)).mid, id)
                    model!!.deleteCart()
                    MagePrefs.clearCouponCode()
                    InAppReviewFlow()
                    flag=true
                }
                val javascript =
                    "javascript: document.getElementsByClassName('section__header')[0].style.display = 'none' "
//                val javascript1 =
//                    "javascript: document.getElementsByClassName('logged-in-customer-information')[0].style.display = 'none' "
                val javascript2 =
                    "var length = document.querySelectorAll(\".reduction-code__text\").length;\n" +
                            "for(var i=0; i<length; i++){\n" +
                            "    (document.querySelectorAll(\".reduction-code__text\")[i]).innerHTML = \"${MagePrefs.getCouponCode()}\";\n" +
                            "}"
               // val paypalbutton = "javascript: document.getElementsByClassName('dynamic-checkout')[0].style.display = 'none' "
               // val paypalbutton_2 = "javascript: document.getElementsByClassName('alternative-payment-separator')[0].style.display = 'none' "
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                   /* webView.evaluateJavascript(paypalbutton) { value ->
                        Log.i(
                            "pageVALUE1",
                            "" + value
                        )
                    }
                    webView.evaluateJavascript(paypalbutton_2) { value ->
                        Log.i(
                            "pageVALUE1",
                            "" + value
                        )
                    }*/
                    webView.evaluateJavascript(javascript) { value ->
                        Log.i(
                            "pageVALUE1",
                            "" + value
                        )
                    }
//                    webView.evaluateJavascript(javascript1) { value ->
//                        Log.i(
//                            "pageVALUE1",
//                            "" + value
//                        )
//                    }
                    if (MagePrefs.getCouponCode() != null) {
                        webView.evaluateJavascript(javascript2) { value ->
                            Log.i(
                                "pageVALUE1",
                                "" + value
                            )
                        }
                    }

                } else {
                    webView.loadUrl(javascript)
                    //webView.loadUrl(paypalbutton)
                 //   webView.loadUrl(paypalbutton_2)
//                    webView.loadUrl(javascript1)
                    if (MagePrefs.getCouponCode() != null) {
                        webView.loadUrl(javascript2)
                    }
                }
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                super.onReceivedSslError(view, handler, error)
                Log.i("URL", "" + error.url)
                customLoader?.dismiss()
            }
        }
    }

    override fun onBackPressed() {
        if(flag){
            MagePrefs.clearCouponCode()
            var intent=Intent(this@CheckoutWeblink,HomePage::class.java)
            startActivity(intent)
            Constant.activityTransition(this)
        }else{
            if (webView!!.canGoBack()) {
                webView!!.goBack()
            } else {
                super.onBackPressed()
                MagePrefs.clearCouponCode()
            }
        }
    }
}
