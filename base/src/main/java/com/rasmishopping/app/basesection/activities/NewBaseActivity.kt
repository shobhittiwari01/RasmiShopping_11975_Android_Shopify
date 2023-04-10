package com.rasmishopping.app.basesection.activities

//import com.shopify.mltranslation.Translation
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.*
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.zxing.integration.android.IntentIntegrator
import com.shopify.buy3.Storefront
import com.rasmishopping.app.BuildConfig
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.ItemDecoration.GridSpacingItemDecoration
import com.rasmishopping.app.basesection.adapters.LanguageListAdapter
import com.rasmishopping.app.basesection.adapters.RecylerAdapter
import com.rasmishopping.app.basesection.adapters.RecylerCountryCodeAdapter
import com.rasmishopping.app.basesection.fragments.BaseFragment
import com.rasmishopping.app.basesection.fragments.LeftMenu
import com.rasmishopping.app.basesection.fragments.LeftMenu.Companion.LeftMenuCallback
import com.rasmishopping.app.basesection.fragments.LeftMenu.Companion.setcallback
import com.rasmishopping.app.basesection.models.FeaturesModel
import com.rasmishopping.app.basesection.viewmodels.LeftMenuViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel.Companion.featuresModel
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.cartsection.activities.SubscribeCartList
import com.rasmishopping.app.collectionsection.activities.CollectionList
import com.rasmishopping.app.collectionsection.activities.CollectionListMenu
import com.rasmishopping.app.customviews.MageNativeTextView
import com.rasmishopping.app.dashboard.activities.AccountActivity
import com.rasmishopping.app.databinding.CurrencyListLayoutBinding
import com.rasmishopping.app.databinding.CurrencycodeListLayoutBinding
import com.rasmishopping.app.databinding.LanguageDialogBinding
import com.rasmishopping.app.dbconnection.entities.CartItemData
import com.rasmishopping.app.dbconnection.entities.WishItemData
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.loginsection.activity.LoginActivity
import com.rasmishopping.app.maintenence_section.MaintenenceActivity
import com.rasmishopping.app.searchsection.activities.AutoSearch
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.AESEnDecryption
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.Urls
import com.rasmishopping.app.utils.Urls.Data.fbusername
import com.rasmishopping.app.utils.Urls.Data.whatsappnumber
import com.rasmishopping.app.utils.ViewModelFactory
import com.rasmishopping.app.wishlistsection.activities.WishList
import kotlinx.android.synthetic.main.language_item.view.*
import kotlinx.android.synthetic.main.m_cartlist_shimmer_layout_grid.*
import kotlinx.android.synthetic.main.m_newbaseactivity.*
import kotlinx.android.synthetic.main.shimmer_layout.*
import kotlinx.android.synthetic.main.shimmer_layout_grid.*
import kotlinx.android.synthetic.main.shimmer_layout_home.*
import kotlinx.android.synthetic.main.shimmer_layout_product_list.*
import kotlinx.android.synthetic.main.shimmer_layout_product_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import zendesk.chat.ChatEngine
import zendesk.messaging.Engine
import zendesk.messaging.MessagingActivity
import java.util.*
import javax.inject.Inject

open class NewBaseActivity : AppCompatActivity(), BaseFragment.OnFragmentInteractionListener {
    //Broadcast Receiver Instance
    //  lateinit var broadcastInternetReceiver: BroadcastInternetReceiver

    // @BindView(R2.id.toolbar)
    lateinit var toolbar: Toolbar
    lateinit var toolbarcontainer: LinearLayoutCompat

    //  @BindView(R2.id.toolimage)
    lateinit var toolimage: ImageView
    protected lateinit var demostoresss: ImageView
    protected lateinit var demostoresnew: ImageView

    //lateinit var demostores: ImageView
    //var demostores: ImageView? = null
    //public var demostores: ImageView? = null
    lateinit var shadow: View

    // @BindView(R2.id.tooltext)
    lateinit var tooltext: MageNativeTextView
    lateinit var toolcarttext: MageNativeTextView
    lateinit var toolcartsubtext: MageNativeTextView

    // @BindView(R2.id.searchsection)
    lateinit var searchsection: LinearLayoutCompat
    lateinit var autosearchsection: LinearLayoutCompat
    lateinit var advancesearch: ConstraintLayout
    lateinit var brcodesearch: ConstraintLayout
    lateinit var imagesearch: ConstraintLayout
    lateinit var auto_search: AppCompatEditText
    lateinit var auto_voicesearch: RelativeLayout

    //  @BindView(R2.id.drawer_layout)
    lateinit var drawer_layout: DrawerLayout

    // @BindView(R2.id.search)
    lateinit var search: MageNativeTextView
    var mDrawerToggle: ActionBarDrawerToggle? = null
    private var splashmodel: SplashViewModel? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    var leftMenuViewModel: LeftMenuViewModel? = null
    var textView: TextView? = null
    val message = MutableLiveData<String>()
    private val TAG = "NewBaseActivity"
    var algoliaSearch: Boolean = true

    companion object {
        var themeColor: String = "#000000"
        var textColor: String = "#FFFFFF"
        var internet:Boolean=true
    }

    @Inject
    lateinit var languageListAdapter: LanguageListAdapter

    @Inject
    lateinit var recylerAdapter: RecylerAdapter

    @Inject
    lateinit var recylerCountryCodeAdapter: RecylerCountryCodeAdapter
    private var listDialog: BottomSheetDialog? = null
    var cartCount: Int = 0
    var is_data:Boolean=false
    lateinit var networkRequest: NetworkRequest
    lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private var languages: HashMap<String, String>? = null
    init {
        if (MagePrefs.getLanguage() != null) {
            updateconfig(this)
        }
    }
    fun updateconfig(wrapper: ContextThemeWrapper){
        var dLocale = Locale(MagePrefs.getLanguage().toString())
        Locale.setDefault(dLocale)
        val configuration = Configuration()
        configuration.fontScale=1.0f
        configuration.setLocale(dLocale)
        wrapper.applyOverrideConfiguration(configuration)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.m_newbaseactivity)
        getMaintenanceMode()
        checkInternet()
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbarcontainer = findViewById<LinearLayoutCompat>(R.id.toolbarcontainer)
        toolimage = findViewById<ImageView>(R.id.toolimage)
        demostoresss = findViewById<ImageView>(R.id.demostoresss)
        demostoresnew = findViewById<ImageView>(R.id.demostoresnew)
        shadow = findViewById<View>(R.id.shadow)
        tooltext = findViewById<MageNativeTextView>(R.id.tooltext)
        toolcarttext = findViewById<MageNativeTextView>(R.id.toolcarttext)
        toolcartsubtext = findViewById<MageNativeTextView>(R.id.toolcartsubtext)
        searchsection = findViewById<LinearLayoutCompat>(R.id.searchsection)
        autosearchsection = findViewById<LinearLayoutCompat>(R.id.autosearchsection)
        advancesearch = findViewById<ConstraintLayout>(R.id.advancesearch)
        imagesearch = findViewById<ConstraintLayout>(R.id.imagesearch)
        brcodesearch = findViewById<ConstraintLayout>(R.id.brcodesearch)
        auto_search = findViewById<AppCompatEditText>(R.id.auto_search)
        auto_voicesearch = findViewById<RelativeLayout>(R.id.auto_voicesearch)
        drawer_layout = findViewById<DrawerLayout>(R.id.drawer_layout)
        search = findViewById<MageNativeTextView>(R.id.search)
        (application as MyApplication).mageNativeAppComponent!!.doBaseActivityInjection(this)
        leftMenuViewModel =
            ViewModelProvider(this, viewModelFactory).get(LeftMenuViewModel::class.java)
        leftMenuViewModel!!.context = this
        leftMenuViewModel!!.repository.allCartItemsCount.observe(
            this,
            { this.consumeCartCount(it) })
        leftMenuViewModel!!.repository.wishListDataCount.observe(this,
            Observer { this.consumeWishCount(it) })
        setSupportActionBar(toolbar)
        setToggle()
        val mSlidingView: View = findViewById(R.id.leftmenu)
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val params = mSlidingView.layoutParams as DrawerLayout.LayoutParams
        params.width = metrics.widthPixels
        mSlidingView.layoutParams = params
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayShowTitleEnabled(false)
        showHumburger()
        nav_view.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home_bottom -> {
                    startActivity(
                        Intent(
                            this, HomePage::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                    Constant.activityTransition(this)
                }
                R.id.search_bottom -> {
                    moveToSearch(this)
                }
                R.id.category_bottom -> {
                    if (featuresModel.collectionWithHandle) {
                        startActivity(
                            Intent(
                                this, CollectionListMenu::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        )
                        Constant.activityTransition(this)
                    } else {
                        startActivity(
                            Intent(
                                this, CollectionList::class.java
                            ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        )
                        Constant.activityTransition(this)
                    }

                }
                R.id.account_bottom -> {
                    if (leftMenuViewModel?.isLoggedIn!!) {
                        startActivity(Intent(this, AccountActivity::class.java))
                        Constant.activityTransition(this)
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                        Constant.activityTransition(this)
                    }
                }
                R.id.notification_bottom -> {
                    startActivity(Intent(this, NotificationActivity::class.java))
                    Constant.activityTransition(this)
                    MagePrefs.clearImageState()
                    MagePrefs.clearImagePosition()
                }
            }
            true
        }
        /*CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            translateDate()
        }*/

        if (this@NewBaseActivity is HomePage) {
            nav_view.menu.findItem(R.id.home_bottom).isChecked = true
                      nav_view.menu.findItem(R.id.home_bottom).icon =
               AppCompatResources.getDrawable(this, R.drawable.ic_home_active)
        } else if (this@NewBaseActivity is AutoSearch) {
            nav_view.menu.findItem(R.id.search_bottom).isChecked = true
            nav_view.menu.findItem(R.id.search_bottom).icon =
                AppCompatResources.getDrawable(this, R.drawable.ic_search_active)
        } else if (this@NewBaseActivity is CollectionList || this@NewBaseActivity is CollectionListMenu) {
            nav_view.menu.findItem(R.id.category_bottom).isChecked = true

            nav_view.menu.findItem(R.id.category_bottom).icon =
                AppCompatResources.getDrawable(this, R.drawable.ic_category_active)
        } else if (this@NewBaseActivity is AccountActivity) {
            nav_view.menu.findItem(R.id.account_bottom).isChecked = true
            nav_view.menu.findItem(R.id.account_bottom).icon =
                AppCompatResources.getDrawable(this, R.drawable.ic_profile_active)
        } else if (this@NewBaseActivity is NotificationActivity) {
            nav_view.menu.findItem(R.id.notification_bottom).isChecked = true
            nav_view.menu.findItem(R.id.notification_bottom).icon =
                AppCompatResources.getDrawable(this, R.drawable.ic_notificationactive_icon)
        }

        setFeatures()

        /********************************* Chat Options **************************************/

        if (this@NewBaseActivity !is HomePage) {
            chat_but.visibility = View.GONE
        }

        val chatEngine: Engine? = ChatEngine.engine()
        chat_but.setOnClickListener {
            MessagingActivity.builder().withEngines(chatEngine).show(this@NewBaseActivity)
        }

        whatsappchat.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("http://api.whatsapp.com/send?phone=$whatsappnumber")
                startActivity(intent)
            } catch (e: Exception) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")
                    )
                )
            }
        }

        messengerchat.setOnClickListener {
            if (isPackageInstalled(this)) {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.setPackage("com.facebook.orca")
                intent.data = Uri.parse(fbusername)
                startActivity(intent)
            } else {
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri.parse("market://details?id=com.facebook.orca")
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.facebook.orca")
                        )
                    )
                }
            }
        }
    }

    private fun checkInternet() {
        try {
            networkRequest = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                // network is available for use
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    if (!internet){
                        Log.i("InterNetStatus","AvailableNow")
                        internet=true
                        finish();
                        overridePendingTransition(0, 0);
                    }
                }

                // Network capabilities have changed for the network
                override fun onCapabilitiesChanged(
                    network: Network, networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val unmetered =
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

                }

                // lost network connection
                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.i("InterNetStatus","Lost")
                    startActivity(
                        Intent(this@NewBaseActivity, InternetActivity::class.java)
                    )
                    Constant.activityTransition(this@NewBaseActivity)
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    /*private fun initializeLiveBroadcast() {
        val liveBroadcasterConfig = LiveBroadcastConfig.Builder(this)
            .setAccessToken(LiveBroadcasterPreferences.getAccessToken(this)?:"")
            .setPublicKey("vBFfEYzWxwh62DMa") //Add public key
            .setLoggingEnabled(true)
            .build()

        LiveBroadcast.initialize(liveBroadcasterConfig)

        val liveBroadcastUiConfig = LiveBroadcastUIConfig.Builder()
            .messageTextSize(resources.getDimensionPixelSize(com.livebroadcasterui.R.dimen.dimen_14sp))
            .build()

        LiveBroadcastUI.initialize(liveBroadcastUiConfig)
        callModulesApi()
    }
    private fun callModulesApi() {
        LiveBroadcast.getApiInstance().getAppId(object :
            CompletionHandler<ExtractAppIdResponse, LiveBroadcastError> {
            override fun onSuccess(result: ExtractAppIdResponse) {
                if(result.settings[0].key== LiveBroadcastConstants.APP_ID){
                    Log.e(TAG, "AppId: $result.settings[0].value")
                    LiveBroadcast.getInstance().setAppId(this@NewBaseActivity,result.settings[0].value)
                    LiveBroadcast.getInstance().setAccessToken("")
                    LiveBroadcast.getInstance().setUserRole(LiveBroadcastConstants.ANONYMOUS)
                    navigateHomeActivity()
                }
            }

            override fun onError(error: LiveBroadcastError?) {
                Log.e(TAG, "errorMessage: ${error?.error?.message}")
            }

        })
    }
    private fun navigateHomeActivity() {
        val intent = Intent(this, BroadcastListActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }*/
    fun InitializeDrawerCallback() {
        setcallback(object : LeftMenuCallback {
            override fun getLeftMenu() {
                drawer_layout.closeDrawers()
            }
        })
    }

    /* fun translateDate() {
         val mbottomNavigationMenuView = findViewById<BottomNavigationView>(R.id.nav_view)
         Constant.translateField(
             resources.getString(R.string.home),
             (mbottomNavigationMenuView.menu.findItem(R.id.home_bottom))
         )
         Constant.translateField(
             resources.getString(R.string.searchtxt),
             (mbottomNavigationMenuView.menu.findItem(R.id.search_bottom))
         )
         Constant.translateField(
             resources.getString(R.string.category_text),
             (mbottomNavigationMenuView.menu.findItem(R.id.category_bottom))
         )
         Constant.translateField(
             resources.getString(R.string.account_txt),
             (mbottomNavigationMenuView.menu.findItem(R.id.account_bottom))
         )
     }*/


    private fun isPackageInstalled(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            packageManager.getPackageInfo("com.facebook.orca", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    fun consumeWishCount(it: List<WishItemData>?) {
        leftMenuViewModel!!.wishListcount = it?.size!!
        invalidateOptionsMenu()
    }

    fun consumeCartCount(it: List<CartItemData>?) {
        if (it?.size!! > 0) {
            cartCount = it.size
            invalidateOptionsMenu()
        }
    }
    /*fun unregisterNetworkChangess() {
        try {
            unregisterReceiver(broadcastInternetReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }*/


    fun hidenavbottom() {
        nav_view.visibility = View.GONE
    }

    fun hidethemeselector() {
        demostoresss.visibility = View.GONE
    }

    fun InAppReviewFlow() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener {}
            } else {

                    // There was some problem, log or handle the error code.
                task.getException()!!.printStackTrace()

                // The user is not logged in to the Google Play Store
                val builder = AlertDialog.Builder(this)
                builder.setTitle(resources.getString(R.string.loginplaystore))
                builder.setMessage(resources.getString(R.string.loginmessageplaystore))
                builder.setPositiveButton(resources.getString(R.string.dialog_ok), DialogInterface.OnClickListener { dialog, which ->
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://")))
                    } catch (e: ActivityNotFoundException) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store")
                            )
                        )
                    }
                })
                builder.show()
            }
        }
    }

    fun setToggle() {
        mDrawerToggle = object : ActionBarDrawerToggle(
            this@NewBaseActivity,
            drawer_layout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                Log.i("MageNativeSaif", "DrawerOPen")
                invalidateOptionsMenu()
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                Log.i("MageNativeSaif", "DrawerClose")
                invalidateOptionsMenu()
            }
        }
        mDrawerToggle!!.syncState()
    }

    protected fun showBackButton() {
        var upArrow = resources.getDrawable(R.drawable.ic_backarrow_25)
        upArrow.setColorFilter(
            Color.parseColor(HomePageViewModel.icon_color), PorterDuff.Mode.SRC_ATOP
        )
        supportActionBar!!.setHomeAsUpIndicator(upArrow)
        mDrawerToggle!!.isDrawerIndicatorEnabled = false
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mDrawerToggle!!.drawerArrowDrawable.color = resources.getColor(R.color.black)
        mDrawerToggle!!.drawerArrowDrawable.color = Color.parseColor(HomePageViewModel.icon_color)
        mDrawerToggle!!.toolbarNavigationClickListener = View.OnClickListener { onBackPressed() }
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mDrawerToggle!!.onDrawerStateChanged(DrawerLayout.STATE_IDLE)
        mDrawerToggle!!.isDrawerIndicatorEnabled = false
        mDrawerToggle!!.syncState()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.m_search, menu)
        try {
            ////////////////Search menu Item//////////////
            var item = menu.findItem(R.id.search_item)
            item.setActionView(R.layout.m_searchicon)
            val view = item.actionView
            val searchicon = view?.findViewById<ImageView>(R.id.cart_icon)
            searchicon?.setColorFilter(Color.parseColor(HomePageViewModel.icon_color))
            view!!.setOnClickListener {
                onOptionsItemSelected(item)
            }
            ////////////////Wishlist menu Item//////////////
            var wishitem = menu.findItem(R.id.wish_item)
            wishitem.setActionView(R.layout.m_wishcount)
            val wishview = wishitem.actionView
            val wishrelative = wishview?.findViewById<RelativeLayout>(R.id.back)
            val wishtext = wishview?.findViewById<TextView>(R.id.count)
            val wishicon = wishview?.findViewById<ImageView>(R.id.cart_icon)
            wishrelative?.backgroundTintList = ColorStateList.valueOf(
                Color.parseColor(
                    HomePageViewModel.count_color
                )
            )
            wishtext?.setTextColor(Color.parseColor(HomePageViewModel.count_textcolor))
            wishicon?.setColorFilter(Color.parseColor(HomePageViewModel.icon_color))
            wishtext!!.text = "" + leftMenuViewModel!!.wishListcount
            wishitem.isVisible = featuresModel.in_app_wishlist
            wishitem.actionView?.setOnClickListener {
                onOptionsItemSelected(wishitem)
            }
            ////////////////cart menu Item//////////////
            val cartitem = menu.findItem(R.id.cart_item)
            cartitem.setActionView(R.layout.m_count)
            val cartview = cartitem.actionView
            val cartrelative = cartview?.findViewById<RelativeLayout>(R.id.back)
            val carttext = cartview?.findViewById<TextView>(R.id.count)
            val carticon = cartview?.findViewById<ImageView>(R.id.cart_icon)
            cartrelative?.backgroundTintList = ColorStateList.valueOf(
                Color.parseColor(
                    HomePageViewModel.count_color
                )
            )
            carttext?.setTextColor(Color.parseColor(HomePageViewModel.count_textcolor))
            carticon?.setColorFilter(Color.parseColor(HomePageViewModel.icon_color))
            if (leftMenuViewModel?.cartCount!! > 0) {
                cartrelative?.visibility = View.VISIBLE
                carttext!!.text = "" + leftMenuViewModel?.cartCount
            }
            cartitem.actionView?.setOnClickListener {
                onOptionsItemSelected(cartitem)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.search_item -> {
                moveToSearch(this)
                true
            }
            R.id.wish_item -> {
                startActivity(Intent(this, WishList::class.java))
                Constant.activityTransition(this)
                true
            }
            R.id.cart_item -> {
                CoroutineScope(Dispatchers.IO).launch {
                    if (leftMenuViewModel?.repository?.getSellingPlanData()?.selling_plan_id != null) {
                        startActivity(Intent(this@NewBaseActivity, SubscribeCartList::class.java))
                        Constant.activityTransition(this@NewBaseActivity)
                    } else {
                        startActivity(Intent(this@NewBaseActivity, CartList::class.java))
                        Constant.activityTransition(this@NewBaseActivity)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showHumburger() {
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        mDrawerToggle!!.isDrawerIndicatorEnabled = true
        mDrawerToggle!!.toolbarNavigationClickListener = null
    }

    protected fun showTittle(tittle: String) {
        Objects.requireNonNull<MageNativeTextView>(tooltext).visibility = View.VISIBLE
        Objects.requireNonNull<ImageView>(toolimage).visibility = View.GONE
        tooltext.text = tittle
        tooltext.setTextColor(Color.parseColor(HomePageViewModel.icon_color))
        toolbar.setBackgroundColor(Color.parseColor(HomePageViewModel.panel_bg_color))
        toolbarcontainer.setBackgroundColor(Color.parseColor(HomePageViewModel.panel_bg_color))
    }

    protected fun showCartText(tittle: String, subtile: String) {
        Objects.requireNonNull<MageNativeTextView>(toolcarttext).visibility = View.VISIBLE
        if (subtile.isNotEmpty()) {
            Objects.requireNonNull<MageNativeTextView>(toolcartsubtext).visibility = View.VISIBLE
            toolcartsubtext.text = subtile
            toolcartsubtext.setTextColor(Color.parseColor(HomePageViewModel.icon_color))
        }
        Objects.requireNonNull<ImageView>(toolimage).visibility = View.GONE
        Objects.requireNonNull<View>(shadow).visibility = View.GONE
        toolcarttext.text = tittle
        toolcarttext.setTextColor(Color.parseColor(HomePageViewModel.icon_color))
        toolbar.setBackgroundColor(Color.parseColor(HomePageViewModel.panel_bg_color))
        toolbarcontainer.setBackgroundColor(Color.parseColor(HomePageViewModel.panel_bg_color))
    }

    fun hideShadow() {
        Objects.requireNonNull<View>(shadow).visibility = View.GONE
    }

    fun showShadow() {
        Objects.requireNonNull<View>(shadow).visibility = View.VISIBLE
    }

    override fun onFragmentInteraction(view: View) {}


    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    fun getCurrency() {
        leftMenuViewModel!!.currencyResponse()
            .observe(this, Observer<List<Storefront.CurrencyCode>> { this.preparePopUp(it) })
        leftMenuViewModel!!.message.observe(this, Observer<String> { this.showToast(it) })
    }

    /************************************************* International Pricing ***************************************************************/

    fun getCountryCode() {
        listDialog = BottomSheetDialog(this, R.style.WideDialogFull)
        var currencyBinding = DataBindingUtil.inflate<CurrencycodeListLayoutBinding>(
            LayoutInflater.from(this), R.layout.currencycode_list_layout, null, false
        )
        listDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        listDialog?.setContentView(currencyBinding.root)
        currencyBinding.countrycodeList.layoutManager = LinearLayoutManager(this)
        currencyBinding.closeBut.setOnClickListener {
            listDialog?.dismiss()
        }
        drawer_layout.closeDrawers()
        listDialog?.show()
        leftMenuViewModel!!.countryCodeResponse().observe(this, Observer<List<Storefront.Country>> {
                this.showCountrycodePopUp(
                    it, currencyBinding
                )
            })
        leftMenuViewModel!!.message.observe(this, Observer<String> { this.showToast(it) })
    }

    fun getLanguageCode() {
        if(HomePage.available_language.length()==0) {
            HomePage.available_language.put(MagePrefs.getLanguage()!!.lowercase())
        }
        val dialog = Dialog(this, R.style.WideDialog)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        val binding = DataBindingUtil.inflate<LanguageDialogBinding>(layoutInflater, R.layout.language_dialog, null, false)
        languageListAdapter.setData(HomePage.available_language,
            object : LanguageListAdapter.LanguageCallback {
                override fun selectedLanguage(language: String) {
                        MagePrefs.setLanguage(language+"#app")
                        MagePrefs.clearHomePageData()
                        leftMenuViewModel!!.deleteData()
                        dialog.dismiss()
                        val intent = Intent(this@NewBaseActivity, Splash::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        Constant.activityTransition(this@NewBaseActivity)
                }
            })
        binding.backButton.setOnClickListener {
            dialog.dismiss()
        }
        binding.languageList.adapter = languageListAdapter
        dialog.setContentView(binding.root)
        drawer_layout.closeDrawers()
        dialog.show()
        /*leftMenuViewModel!!.languageCodeResponse()
            .observe(this, Observer<List<Storefront.Language>> {
                this.showLanguageDialog(
                    it, dialog, binding
                )
            })
        leftMenuViewModel!!.message.observe(this, Observer<String> { this.showToast(it) })*/
    }

    /***********************************************************************************************************************************/
    fun showLanguageDialog(
        languageCode: List<Storefront.Language>,
        dialog: Dialog,
        binding: LanguageDialogBinding
    ) {
        /*val available_language=HomePage.available_language

        if (dialog.isShowing) {
            binding.spinkit.visibility = View.GONE
            languages = hashMapOf()
            for (i in 0 until languageCode.size) {
                Log.i("SaifDevlang", "" + languageCode.get(i).endonymName)
                if (featuresModel.rtl_support && languageCode.get(i).name == "Arabic") {
                    languages?.put(languageCode.get(i).name, languageCode.get(i).isoCode.name)
                } else if (languageCode.get(i).name != "Arabic") {
                    languages?.put(languageCode.get(i).name, languageCode.get(i).isoCode.name)
                }
            }

            if (languages?.keys?.toMutableList()?.size!! > 0)
                languageListAdapter.setData(
                    languages?.keys?.toMutableList(),
                    object : LanguageListAdapter.LanguageCallback {
                        override fun selectedLanguage(language: String) {
                            for(i in 0 until available_language.length())
                            {
                                if(available_language.get(i).equals(languages?.get(language)?.lowercase()))
                                {
                                    is_data=true
                                }

                            }
                            if(is_data==true)
                            {
                                MagePrefs.setLanguage(languages?.get(language)!!)
                                MagePrefs.clearHomePageData()
                                leftMenuViewModel!!.deleteData()
                                dialog.dismiss()
                                val intent = Intent(this@NewBaseActivity, Splash::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                Constant.activityTransition(this@NewBaseActivity)
                            }
                            else
                            {
                                showToast("No data available for this language")
                            }
                        }
                    })
            binding.backButton.setOnClickListener {
                dialog.dismiss()
            }
            binding.languageList.adapter = languageListAdapter
        }*/
    }
    private fun preparePopUp(currencyCodes: List<Storefront.CurrencyCode>) {
        if (listDialog == null) {
            showPopUp(currencyCodes)
        } else {
            if (!listDialog!!.isShowing) {
                showPopUp(currencyCodes)
            }
        }
    }

    /************************************************* International Pricing ***************************************************************/

    private fun showCountrycodePopUp(
        countries: List<Storefront.Country>, binding: CurrencycodeListLayoutBinding
    ) {
        try {
            binding.spinkit.visibility = View.GONE
            recylerCountryCodeAdapter.setData(
                countries, this@NewBaseActivity, leftMenuViewModel!!.repository, leftMenuViewModel!!
            )
            binding.countrycodeList.adapter = recylerCountryCodeAdapter
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /***********************************************************************************************************************************/

    private fun showPopUp(enabledPresentmentCurrencies: List<Storefront.CurrencyCode>) {
        try {
            listDialog = BottomSheetDialog(this, R.style.WideDialogFull)
            var currencyBinding = DataBindingUtil.inflate<CurrencyListLayoutBinding>(
                LayoutInflater.from(this), R.layout.currency_list_layout, null, false
            )
            listDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            listDialog?.setContentView(currencyBinding.root)
            currencyBinding.currencyList.layoutManager = LinearLayoutManager(this)
            recylerAdapter.setData(enabledPresentmentCurrencies, this@NewBaseActivity)
            currencyBinding.currencyList.adapter = recylerAdapter
            currencyBinding.closeBut.setOnClickListener {
                listDialog?.dismiss()
            }
            drawer_layout.closeDrawers()
            listDialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setLayout(view: RecyclerView, orientation: String): RecyclerView {
        view.setHasFixedSize(true)
        view.isNestedScrollingEnabled = false
        view.itemAnimator = DefaultItemAnimator()
        val manager = LinearLayoutManager(this)
        when (orientation) {
            "horizontal" -> {
                manager.orientation = RecyclerView.HORIZONTAL
                view.layoutManager = manager
                if (view.itemDecorationCount == 0) {
                    view.addItemDecoration(GridSpacingItemDecoration(1, dpToPx(1), true))
                }
            }
            "vertical" -> {
                manager.orientation = RecyclerView.VERTICAL
                view.layoutManager = manager
                if (view.itemDecorationCount == 0) {
                    view.addItemDecoration(GridSpacingItemDecoration(1, dpToPx(2), true))
                }
            }
            "collectionvertical" -> {
                manager.orientation = RecyclerView.VERTICAL
                view.layoutManager = manager
                if (view.itemDecorationCount == 0) {
                    view.addItemDecoration(GridSpacingItemDecoration(1, dpToPx(8), true))
                }
            }
            "grid" -> {
                view.layoutManager = GridLayoutManager(this, 2)
                if (view.itemDecorationCount == 0) {
                    view.addItemDecoration(GridSpacingItemDecoration(2, dpToPx(0), true))
                }
            }
            "autogrid" -> {
                var manager = GridLayoutManager(this, 2)
                // manager.setReverseLayout(true);
                //manager.setStackFromEnd(true);
                view.layoutManager = manager
                if (view.itemDecorationCount == 0) {
                    view.addItemDecoration(GridSpacingItemDecoration(2, dpToPx(0), true))
                }
            }
            "autogrid4" -> {
                var manager = GridLayoutManager(this, 4)
                // manager.setReverseLayout(true);
                //manager.setStackFromEnd(true);
                view.layoutManager = manager
                if (view.itemDecorationCount == 0) {
                    view.addItemDecoration(GridSpacingItemDecoration(2, dpToPx(0), true))
                }
            }
            "3grid" -> {
                view.layoutManager = GridLayoutManager(this, 3)
                if (view.itemDecorationCount == 0) {
                    view.addItemDecoration(GridSpacingItemDecoration(3, dpToPx(4), true))
                }
            }
            "4grid" -> {
                view.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.HORIZONTAL, false)
                if (view.itemDecorationCount == 0) {
                    view.addItemDecoration(GridSpacingItemDecoration(2, dpToPx(6), true))
                }
            }
            "customisablegrid" -> {
                view.layoutManager = GridLayoutManager(this, 3)
//                if (view.itemDecorationCount == 0) {
//                    view.addItemDecoration(GridSpacingItemDecoration(3, dpToPx(4), true))
//                }
            }
            "customisablegridwithtwoitem" -> {
                view.layoutManager = GridLayoutManager(this, 2)
            }
            "customisablelist" -> {
                manager.orientation = RecyclerView.VERTICAL
                view.layoutManager = manager
            }
            "instagrid" -> {
                view.layoutManager = GridLayoutManager(this, 3)
            }
            "instalist" -> {
                manager.orientation = RecyclerView.HORIZONTAL
                view.layoutManager = manager
            }
        }
        return view
    }

    fun closePopUp() {
        listDialog!!.dismiss()
    }

    private fun dpToPx(dp: Int): Int {
        val r = resources
        return Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics
            )
        )
    }


    override fun onResume() {
        super.onResume()
        cartCount = leftMenuViewModel!!.cartCount
      }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val connectivityManager =
                getSystemService(ConnectivityManager::class.java) as ConnectivityManager
            connectivityManager.requestNetwork(networkRequest, networkCallback)
        }
        // broadcastInternetReceiver = BroadcastInternetReceiver()
        // registerNetworkBroadcast()
    }

    fun showAutoSearch() {
        autosearchsection.visibility = View.VISIBLE
        val typeface = Typeface.createFromAsset(assets, "fonts/poplight.ttf")
        auto_search.typeface = typeface
        if (featuresModel.qr_code_search_scanner) {
            advancesearch.visibility = View.VISIBLE
        }
    }

    fun hideutoSearch() {
        autosearchsection.visibility = View.GONE
        val typeface = Typeface.createFromAsset(assets, "fonts/poplight.ttf")
        auto_search.typeface = typeface
        if (featuresModel.qr_code_search_scanner) {
            advancesearch.visibility = View.GONE
        }
    }

    fun hideKeyboard(activity: Activity) {
        try {
            val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setLogoImage(url: String) {
        if (!this.isDestroyed) {
            Log.i("MageNative", "Image URL" + url)
            Glide.with(this).load(url).thumbnail(0.5f).apply(
                    RequestOptions().placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder).dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                ).into(toolimage)
        }
    }

    fun setLogoImageDrawable(drawable: Drawable) {
        val originalColor = Color.parseColor(MagePrefs.getThemeColor())
        val alphaValue = 40
        val lighterColor = ColorUtils.setAlphaComponent(originalColor, alphaValue)
        val placeholdercolor = ColorDrawable(lighterColor)
        if (!this.isDestroyed) {
            Glide.with(this)
                .load(drawable)
                .thumbnail(0.5f)
                .apply(
                    RequestOptions().placeholder(placeholdercolor)
                        .error(placeholdercolor).dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(toolimage)
        }
    }

    fun setPanelBackgroundColor(color: String) {
        toolbar.setBackgroundColor(Color.parseColor(color.uppercase(Locale.getDefault())))
        toolbarcontainer.setBackgroundColor(Color.parseColor(color.uppercase(Locale.getDefault())))
    }


    fun setSearchOptions(searchback: String, searchtext: String, searhcborder: String) {
        var draw: GradientDrawable = searchsection.background as GradientDrawable
        draw.setColor(Color.parseColor(searchback))
        search.setTextColor(Color.parseColor(searchtext))
        search.setHintTextColor(Color.parseColor(searchtext))
        draw.setStroke(2, Color.parseColor(searhcborder))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(
                    this, "" + resources.getString(R.string.noresultfound), Toast.LENGTH_LONG
                ).show()
//                finish()
            } else {
                when (result.formatName) {
                    "QR_CODE" -> {
                        try {
                            AESEnDecryption().data()
                            var json = JSONObject(result.contents)
                            if (json.has("mid")) {
                                Log.i("MageNative", "Barcode" + result)
                                Log.i("MageNative", "Barcode" + result.contents)
                                try {
                                    //clearAppData()
                                    leftMenuViewModel!!.deletLocal()
                                    leftMenuViewModel!!.deleteData()
                                    MagePrefs.clearHomePageData()
                                    featuresModel = FeaturesModel()
                                    leftMenuViewModel!!.insertPreviewData(json)
                                    MagePrefs.clearCountry()
                                    leftMenuViewModel!!.logOut()
                                    var intent = Intent(this, Splash::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    Constant.activityTransition(this)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    //Shimmer

//    fun shimmerStart() {
//        shimmer_view_container.startShimmer()
//    }

    open fun shimmerStop() {
        shimmer_view_container.stopShimmer()
        shimmer_view_container.visibility = View.GONE
    }

    fun shimmerStartGrid() {
        shimmer_view_container_grid.startShimmer()
    }

    fun shimmerStopGrid() {
        shimmer_view_container_grid.stopShimmer()
        shimmer_view_container_grid.visibility = View.GONE
    }

    fun shimmerStartGridCart() {
        shimmer_view_container_grid_cart.startShimmer()
    }

    fun shimmerStopGridCart() {
        shimmer_view_container_grid_cart.stopShimmer()
        shimmer_view_container_grid_cart.visibility = View.GONE
    }

    fun shimmerStartGridProductView() {
        shimmer_view_container_grid_product_view.startShimmer()
    }

    fun shimmerStopGridProductView() {
        shimmer_view_container_grid_product_view.stopShimmer()
        shimmer_view_container_grid_product_view.visibility = View.GONE
    }

    fun shimmerStartGridProductList() {
        shimmer_view_container_product_list.startShimmer()
    }

    fun shimmerStopGridProductList() {
        shimmer_view_container_product_list.stopShimmer()
        shimmer_view_container_product_list.visibility = View.GONE
    }

    fun shimmerStartHome() {
        shimmer_view_container_home.startShimmer()
    }

    fun shimmerStopHome() {
        shimmer_view_container_home.stopShimmer()
        shimmer_view_container_home.visibility = View.GONE
    }

    //Module Installation on Demand
    private lateinit var manager: SplitInstallManager
    fun checkIfModuleInstall(modulename: String): Boolean {
        manager = SplitInstallManagerFactory.create(this)
        return manager.installedModules.contains(modulename)
    }

    fun installModule(modulename: String) {
        val request = SplitInstallRequest.newBuilder().addModule(modulename).build()
        manager.startInstall(request).addOnCompleteListener {
                showToast(resources.getString(R.string.installedmodule))
            }.addOnSuccessListener {
                showToast(resources.getString(R.string.loadingmodule))
            }.addOnFailureListener {
                showToast(resources.getString(R.string.errormodule))
            }
    }

    fun moveToSearch(reference: Context) {
        if (featuresModel.algoliasearch) {
            if (checkIfModuleInstall(getString(R.string.module_algoliasearch))) {
                val intent = Intent()
                intent.setClassName(BuildConfig.APPLICATION_ID, "com.shopify.algolia.MainActivity")
                val options = Bundle()
                intent.putExtra("app_id", Urls.AppID)
                intent.putExtra("apikey", Urls.ApiKey)
                intent.putExtra("index", Urls.IndexName)
                ContextCompat.startActivity(reference, intent, options)
            } else {
                installModule(getString(R.string.module_algoliasearch))
            }
        } else {
            val searchpage = Intent(reference, AutoSearch::class.java)
            startActivity(searchpage)
            if (reference is Activity) {
                Constant.activityTransition(reference)
            }
        }
    }

    /*fun registerNetworkBroadcast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(
                broadcastInternetReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerReceiver(
                broadcastInternetReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }*/

    //    override fun onStop() {
//        super.onStop()
//        unregisterReceiver(broadcastInternetReceiver)
//    }
    fun setFeatures() {
        if (featuresModel.showBottomNavigation) {
            nav_view.visibility = View.VISIBLE
        } else {
            nav_view.visibility = View.GONE
        }
        navigation_font(this, nav_view)
    }

    fun showBottomBar() {
        if (featuresModel!!.showBottomNavigation) {
            nav_view.visibility = View.VISIBLE
        } else {
            nav_view.visibility = View.GONE
        }
    }

    fun updateColorInBottomBar() {
        navigation_font(this, nav_view)
    }


    fun navigation_font(context: Context, v: View) {
        try {
            if (v is ViewGroup) {
                val vg = v
                for (i in 0 until vg.childCount) {
                    val child: View = vg.getChildAt(i)
                    navigation_font(context, child)
                }
            } else if (v is TextView) {
                v.typeface = Typeface.createFromAsset(
                    context.assets, "fonts/poplight.ttf"
                )
                v.textSize = 10f

            } else if (v is ImageView) {
                // v.setColorFilter(Color.parseColor(NewBaseActivity.themeColor), android.graphics.PorterDuff.Mode.MULTIPLY)
                when (MagePrefs.getTheme()) {
                    "Grocery Theme" -> {
                        v.setColorFilter(
                            Color.parseColor("#03AD53"),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                    "Fashion Theme" -> {
                        v.setColorFilter(
                            Color.parseColor("#9A583C"),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                    "Home Theme" -> {
                        v.setColorFilter(
                            Color.parseColor("#6096B4"),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                    else->{
                        when (themeColor) {
                            "#FFFFFF", "#ffffff", "#000000" -> {
                                when (textColor) {
                                    "#FFFFFF", "#ffffff", "#000000" -> {
                                        if (HomePageViewModel.isLightModeOn()) {
                                            applyColor("#000000", v)
                                        } else {
                                            applyColor("#FFFFFF", v)
                                        }
                                    }
                                    else -> {
                                        applyColor(textColor, v)
                                    }
                                }
                            }
                            else -> {
                                applyColor(themeColor, v)
                            }
                        }
                    }
                }

            }
        } catch (e: Exception) {
        }
    }

    open fun applyColor(color: String, v: ImageView) {
        try {
            v.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun closesubmenu() {
        LeftMenu().closeSubMenus(this)
    }

    fun getMaintenanceMode() {
        try {
            MyApplication.dataBaseReference?.child("additional_info")?.child("maintenance_mode")
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        try {
                            if (dataSnapshot.value != null) {
                                Log.d(TAG, "onDataChange: " + dataSnapshot)
                                val maintenance = dataSnapshot.value as Boolean
                                MagePrefs.setMaintenanceMode(maintenance)
                                if (maintenance) {
                                    var intent = Intent(this@NewBaseActivity, MaintenenceActivity::class.java)
                                    startActivity(intent)
                                    Constant.activityTransition(this@NewBaseActivity)
                                    finish()
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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

    fun getFeatures() {
        try {
            MyApplication.dataBaseReference?.child("features")
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.value != null) {
                            Log.i("FirebaseData_Saif:", "Features" + dataSnapshot.value as java.util.ArrayList<String>)
                            Constant.initializeFeatures()
                            val featuresList = dataSnapshot.value as java.util.ArrayList<String>
                            CoroutineScope(Dispatchers.IO).launch {
                                for (i in 0..featuresList.size - 1) {
                                    Log.d("REpo", "onDataChange: " + featuresList[i])
                                    when (featuresList[i]) {
                                        "in-app-whislist" -> {
                                            SplashViewModel.featuresModel.in_app_wishlist = true
                                        } // check done
                                        "native_order_view" -> {
                                            SplashViewModel.featuresModel.nativeOrderView = true
                                        } //check done
                                        "rtl-Support" -> {
                                            SplashViewModel.featuresModel.rtl_support = true
                                        } //check done
                                        "product-share" -> {
                                            SplashViewModel.featuresModel.product_share = true
                                        } //check done
                                        "multi-currency" -> {
                                            SplashViewModel.featuresModel.multi_currency = true
                                        } //check done
                                        "multi-language" -> {
                                            SplashViewModel.featuresModel.multi_language = true
                                        } // check done
                                        "abandoned-cart-campaigns" -> {
                                            SplashViewModel.featuresModel.abandoned_cart_compaigns =
                                                true
                                        }//check done
                                        "out_of_stock" -> {
                                            SplashViewModel.featuresModel.outOfStock = true
                                        } //check done
                                        "reorder" -> {
                                            SplashViewModel.featuresModel.reOrderEnabled = true
                                        } //check done
                                        "recommended_products" -> {
                                            SplashViewModel.featuresModel.recommendedProducts = true
                                        } //check done
                                        "augmented-reality" -> {
                                            SplashViewModel.featuresModel.ardumented_reality = true
                                        } // check done
                                        "add_to_cart" -> {
                                            SplashViewModel.featuresModel.addCartEnabled = true
                                        } // check done
                                        "deep-linking" -> {
                                            SplashViewModel.featuresModel.deep_linking = true
                                        } // check done
                                        "qr-code-search-scanner" -> {
                                            SplashViewModel.featuresModel.qr_code_search_scanner =
                                                true
                                        } //check done
                                        "show_bottom_navigation" -> {
                                            SplashViewModel.featuresModel.showBottomNavigation =
                                                true
                                        } //check done
                                        "social_login" -> {
                                            SplashViewModel.featuresModel.socialloginEnable = false
                                        }
                                        "native_checkout" -> {
                                            SplashViewModel.featuresModel.native_checkout = true
                                        }
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun getDefaultLang(){
        MyApplication.dataBaseReference?.child("default_locale")
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()){
                        if (dataSnapshot.getValue(String::class.java) != null) {
                            Log.i("FirebaseData_Saif:", "DefaultLang" + dataSnapshot.getValue(String::class.java))
                            val lang = dataSnapshot.getValue(String::class.java)!!
                            MagePrefs.setLanguage(lang+"#panel")
                            if (MagePrefs.getLanguage()?.lowercase() == "ar") {
                                featuresModel.rtl_support = true
                            }
                        }
                    }else{
                        getDefaultLangV1()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Log.i("DBConnectionError", "" + databaseError.details)
                    Log.i("DBConnectionError", "" + databaseError.message)
                    Log.i("DBConnectionError", "" + databaseError.code)
                }
            })

    }
    fun getDefaultLangV1(){
        MyApplication.dataBaseReference?.child("additional_info")!!.child("locale")
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()){
                        if (dataSnapshot.getValue(String::class.java) != null) {
                            Log.i("FirebaseData_Saif:", "DefaultLang" + dataSnapshot.getValue(String::class.java))
                            val lang = dataSnapshot.getValue(String::class.java)!!
                            MagePrefs.setLanguage(lang+"#panel")
                            if (MagePrefs.getLanguage()?.lowercase() == "ar") {
                                featuresModel.rtl_support = true
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
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        finish();
        overridePendingTransition(0, 0);
        if (HomePageViewModel.isLightModeOn()) {
            HomePageViewModel.count_color = HomePageViewModel.count_color_l
            HomePageViewModel.count_textcolor = HomePageViewModel.count_textcolor_l
            HomePageViewModel.icon_color = HomePageViewModel.icon_color_l
            HomePageViewModel.panel_bg_color = HomePageViewModel.panel_bg_color_l
        } else {
            HomePageViewModel.count_color = "#FFFFFF"
            HomePageViewModel.count_textcolor = "#000000"
            HomePageViewModel.icon_color = "#FFFFFF"
            HomePageViewModel.panel_bg_color = "#000000"
        }

        HomePageViewModel.themedContext = createConfigurationContext(newConfig)
        SweetAlertDialog.DARK_STYLE=!HomePageViewModel.isLightModeOn()
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    open fun clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                (getSystemService(ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData() // note: it has a return value!
            } else {
                val packageName = applicationContext.packageName
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear $packageName")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    fun setLogoCenter(flag: Boolean){
        val layoutParams = toolimage.layoutParams as Toolbar.LayoutParams
        when(flag){
            true-> layoutParams.gravity=Gravity.CENTER
            false-> layoutParams.gravity=Gravity.START
        }
        toolimage.setLayoutParams(layoutParams)
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val connectivityManager = getSystemService(ConnectivityManager::class.java) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}
