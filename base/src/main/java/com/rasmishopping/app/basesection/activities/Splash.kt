package com.rasmishopping.app.basesection.activities
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.facebook.appevents.AppEventsLogger
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.MyApplication.Companion.context
import com.rasmishopping.app.MyApplication.Companion.dataBaseReference
import com.rasmishopping.app.MyApplication.Companion.firebaseapp
import com.rasmishopping.app.MyApplication.Companion.getmFirebaseSecondanyInstance
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel.Companion.featuresModel
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.cartsection.activities.SubscribeCartList
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.jobservicessection.UploadWorker
import com.rasmishopping.app.productsection.activities.ProductList
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.trialsection.activities.TrialExpired
import com.rasmishopping.app.utils.*
import com.rasmishopping.app.utils.Urls.Data.FirebaseEmail
import com.rasmishopping.app.utils.Urls.Data.FirebaseNewEmail
import com.rasmishopping.app.utils.Urls.Data.FirebaseNewPassword
import com.rasmishopping.app.utils.Urls.Data.FirebasePassword
import kotlinx.coroutines.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class Splash : NewBaseActivity() {
    //Broadcast Receiver Instance
    @Inject
    lateinit var viewModelFactory_spplash: ViewModelFactory
    private var splashmodel: SplashViewModel? = null
    private var product_id: String? = null
    private lateinit var auth: FirebaseAuth
    private var Trial: Boolean = false
    var intentdelay: Long = 2000
    var PERMISSION_REQUEST_CODE_NOTIFICATION = 201
    var source="direct"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        internet=true
        setContentView(R.layout.m_splash)
        AppEventsLogger.activateApp(application)
        Log.i("NotificationSaif", "" + MagePrefs.getnoti())
        (application as MyApplication).mageNativeAppComponent!!.doSplashInjection(this@Splash)
        splashmodel = ViewModelProvider(this@Splash, viewModelFactory_spplash).get(SplashViewModel::class.java)
        if (Build.VERSION.SDK_INT >= 33) {
            if (hasPermission(android.Manifest.permission.POST_NOTIFICATIONS)) {
                updateConfig()
            } else {
                requestPermissions(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    PERMISSION_REQUEST_CODE_NOTIFICATION
                )
            }
        } else {
            updateConfig()
        }
    }

    fun updateConfig() {
        performtask()
    }

    fun performtask() {
        try {
            auth = Firebase.auth(firebaseapp)
            auth.signInWithEmailAndPassword(FirebaseEmail, FirebasePassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        dataBaseReference = getmFirebaseSecondanyInstance().getReference(Urls(context).shopdomain.replace(".myshopify.com", ""))
                        splashmodel!!.version.observe(this@Splash, Observer<String> {
                            if (!it.equals("v1")){
                                dataBaseReference= dataBaseReference!!.child(it)
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                intializeObservers()
                            }
                            Handler(Looper.myLooper()!!).postDelayed({ checkforIntents() }, intentdelay)
                        })
                        splashmodel!!.getVersion()
                    }
                }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    private fun intializeObservers() {
        splashmodel!!.message.observe(this@Splash, Observer<String> { this@Splash.showToast(it) })
        splashmodel!!.trialdata.observe(
            this@Splash,
            Observer<LocalDbResponse> { this@Splash.ProcessTrial(it) })
        splashmodel!!.fireBaseResponseMutableLiveData.observe(
            this@Splash,
            Observer<FireBaseResponse> { this@Splash.consumeResponse(it) })
        splashmodel!!.errorMessageResponse.observe(
            this@Splash,
            Observer<String> { this@Splash.consumeErrorResponse(it) })
        CoroutineScope(Dispatchers.IO).launch {
            initializeFirebase()
        }
        CoroutineScope(Dispatchers.IO).launch {
            if (splashmodel!!.isLogin) {
                splashmodel!!.refreshTokenIfRequired()
            }
            Log.i("Stepwise", "RefresToken")
        }
        CoroutineScope(Dispatchers.IO).launch {
            @SuppressLint("HardwareIds") val deviceId = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            splashmodel!!.sendTokenToServer(deviceId)
            Log.i("Stepwise", "SendServerToken")
        }
    }

    fun initializeFirebase() {
        if(MagePrefs.getDefaultLanguage()==null || MagePrefs.getDefaultLanguage()!!.contains("#panel")){
            getDefaultLang()
        }
        getFeatures()
        if (Trial) {
            splashmodel!!.connectFirebaseForTrial(Urls(context).shopdomain)
        } else {
            proceedWithMainTasks()
        }

    }
    private fun proceedWithMainTasks() {
        if (MagePrefs.getCountryCode() == null) {
            CoroutineScope(Dispatchers.IO).launch {
                splashmodel!!.connectFireBaseForSplashData()
            }
        }
    }

    private fun checkforIntents() {
            var intentcollection = arrayOf<Intent?>()
            intentcollection = arrayOfNulls(1)
            if (intent != null) {
                if (featuresModel.deep_linking) {
                    if (intent.data != null) {
                        if (intent.data!!.getQueryParameter("pid") != null) {
                            source="Product Share"
                            product_id = intent.data!!.getQueryParameters("pid")[0]
                            Log.i("PID", "" + product_id)
                            intentcollection = arrayOfNulls(2)
                            val product = Intent(this@Splash, ProductView::class.java)
                            product.putExtra("ID", product_id)
                            intentcollection[1] = product
                        } else {
                            val link_array = intent.data!!.toString().split("/")
                            var type = link_array.get(link_array.size - 2)
                            var value = link_array.get(link_array.size - 1)
                            Log.i("SaifTest", "" + type)
                            Log.i("SaifTest", "" + value)
                            when (type) {
                                "products" -> {
                                    source="Deep Link"
                                    intentcollection = arrayOfNulls(2)
                                    val product = Intent(this@Splash, ProductView::class.java)
                                    product.putExtra("handle", value)
                                    intentcollection[1] = product
                                }
                                "collections" -> {
                                    source="Deep Link"
                                    intentcollection = arrayOfNulls(2)
                                    val collection = Intent(this@Splash, ProductList::class.java)
                                    collection.putExtra("handle", value)
                                    intentcollection[1] = collection
                                }
                            }
                        }
                    }
                }
                if (intent.extras != null && intent.extras!!.containsKey("data")) {
                    Log.i("SaifDev", "IntentCheck" + intent.extras!!.getString("data"))
                    var obje = JSONObject(intent.extras!!.getString("data")!!)
                    intent.putExtra("type", obje.getJSONObject("payload").getString("link_type"))
                    intent.putExtra("ID", obje.getJSONObject("payload").getString("link_id"))
                    intent.putExtra("link", obje.getJSONObject("payload").getString("link_id"))
                }

                if (intent.hasExtra("type")) {
                    source="Push Notification"
                    when (intent.getStringExtra("type")) {
                        "product" -> {
                            intentcollection = arrayOfNulls(2)
                            val product = Intent(this@Splash, ProductView::class.java)
                            product.putExtra(
                                "ID",
                                "gid://shopify/Product/" + intent.getStringExtra("ID")
                            )
                            intentcollection[1] = product
                        }
                        "collection" -> {
                            intentcollection = arrayOfNulls(2)
                            val product = Intent(this@Splash, ProductList::class.java)
                            product.putExtra(
                                "ID",
                                "gid://shopify/Collection/" + intent.getStringExtra("ID")
                            )
                            product.putExtra("tittle", intent.getStringExtra("tittle"))
                            intentcollection[1] = product
                        }
                        "weblink" -> {
                            intentcollection = arrayOfNulls(2)
                            val product = Intent(this@Splash, Weblink::class.java)
                            product.putExtra("link", intent.getStringExtra("link"))
                            product.putExtra("name", intent.getStringExtra("name"))
                            intentcollection[1] = product
                        }
                        "cart" -> {
                            intentcollection = arrayOfNulls(2)
                            var cartlist: Intent = Intent()
                            CoroutineScope(Dispatchers.IO).launch {
                                if (splashmodel?.repository?.getSellingPlanData()?.selling_plan_id != null) {
                                    cartlist = Intent(this@Splash, SubscribeCartList::class.java)
                                } else {
                                    cartlist = Intent(this@Splash, CartList::class.java)
                                }
                                intentcollection[1] = cartlist
                            }
                        }
                    }
                }
            }
            initializeWorker()
            Constant.FirebaseEvent_AppOpen(source)
            val homepage = Intent(this@Splash, HomePage::class.java)
            intentcollection[0] = homepage
            startActivities(intentcollection)
            Constant.activityTransition(this)
            MagePrefs.clearTheme()
            Log.i("Stepwise", "4")
            finish()
    }

    private fun initializeWorker() {
        if (featuresModel.abandoned_cart_compaigns) {
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "MageNative", ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<UploadWorker>(12, TimeUnit.HOURS).build()
            )
        } else {
            val statuses: ListenableFuture<List<WorkInfo>> =
                WorkManager.getInstance(this).getWorkInfosForUniqueWork("MageNative")
            if (statuses.get().size > 0) {
                WorkManager.getInstance(this).cancelUniqueWork("MageNative")
            }
        }
    }
    private fun showToast(it: String?) {
        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
    }
    private fun consumeErrorResponse(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
    }
    private fun ProcessTrial(reponse: LocalDbResponse) {
        when (reponse.status) {
            Status.SUCCESS -> {
                if (reponse.data!!.isIstrialexpire) {
                    Log.i("Stepwise", "2")
                    proceedWithMainTasks()
                } else {
                    var intent = Intent(this, TrialExpired::class.java)
                    startActivity(intent)
                    Constant.activityTransition(this)
                    finish()
                }
            }
            Status.ERROR -> {
                reponse.error!!.printStackTrace()
                consumeErrorResponse(resources.getString(R.string.errorString))
            }
        }

    }
    private fun consumeResponse(reponse: FireBaseResponse) {
        when (reponse.status) {
            Status.SUCCESS -> renderSuccessResponse(reponse.data!!)
            Status.ERROR -> {
                reponse.error!!.printStackTrace()
                consumeErrorResponse(resources.getString(R.string.errorString))
            }
        }
    }

    private fun renderSuccessResponse(data: Boolean) {
        try {
//                finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface CallBack {
        fun getlang(code: String)
        fun featureSet(code: Boolean)
    }

    private fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions(permission: String, code: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), code)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(permission), code)
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE_NOTIFICATION -> {
                // If request is cancelled, the result arrays are empty.
                    try {
                        updateConfig()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                return
            }
        }
    }
}
