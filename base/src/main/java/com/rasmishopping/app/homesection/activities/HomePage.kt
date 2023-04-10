package com.rasmishopping.app.homesection.activities
//import com.shopify.livesale.LiveSaleActivity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import com.google.gson.JsonElement
import com.shopify.instafeeds.InstafeedViewModel
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.activities.ThemeSelectionActivity
import com.rasmishopping.app.basesection.fragments.LeftMenu
import com.rasmishopping.app.basesection.viewmodels.LeftMenuViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel.Companion.featuresModel
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.cartsection.activities.SubscribeCartList
import com.rasmishopping.app.databinding.MHomepageModifiedBinding
import com.rasmishopping.app.databinding.PopConfirmationBinding
import com.rasmishopping.app.homesection.adapters.InstaFeedAdapters
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel.Companion.count_color
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel.Companion.count_textcolor
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel.Companion.icon_color
import com.rasmishopping.app.maintenence_section.MaintenenceActivity
import com.rasmishopping.app.personalised.adapters.PersonalisedAdapter
import com.rasmishopping.app.personalised.viewmodels.PersonalisedViewModel
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.*
import com.rasmishopping.app.wishlistsection.activities.WishList
import kotlinx.android.synthetic.main.m_leftmenufragment.*
import kotlinx.android.synthetic.main.m_newbaseactivity.*
import kotlinx.android.synthetic.main.m_newbaseactivity.chat_but
import kotlinx.android.synthetic.main.pop_confirmation.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class HomePage : NewBaseActivity() {
    private var binding: MHomepageModifiedBinding? = null
    @Inject
    lateinit var factory: ViewModelFactory
    private var homemodel: HomePageViewModel? = null
    protected lateinit var leftmenu: LeftMenuViewModel
    lateinit var homepage: LinearLayoutCompat
    var banner: Boolean? = false
    var collectionlist: Boolean? = false
    var productslider: Boolean? = false
    private var personamodel: PersonalisedViewModel? = null
    private var hasBanner: Boolean? = null
    private var hasFullSearch: Boolean = false
    private val MY_REQUEST_CODE = 105
    private var appUpdateManager: AppUpdateManager? = null
    @Inject
    lateinit var personalisedadapter: PersonalisedAdapter
    private var instafeedmodel: InstafeedViewModel? = null
    private var feedsrecycler: RecyclerView? = null
    @Inject
    lateinit var instafeed_adapters: InstaFeedAdapters
    @Inject
    lateinit var padapter: PersonalisedAdapter
    private var isImageChanged = false
    companion object {
        var available_language: JSONArray = JSONArray()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_homepage_modified, group, true)
        SweetAlertDialog.DARK_STYLE = !HomePageViewModel.isLightModeOn()
        shimmerStartHome()
        homepage = binding!!.homecontainer
        (application as MyApplication).mageNativeAppComponent!!.doHomePageInjection(this)
        leftmenu = ViewModelProvider(this, viewModelFactory).get(LeftMenuViewModel::class.java)
        appUpdateManager = AppUpdateManagerFactory.create(this)
        homemodel = ViewModelProvider(this, factory).get(HomePageViewModel::class.java)
        homemodel!!.context = this
        showHumburger()
        InitializeDrawerCallback()
        personamodel = ViewModelProvider(this, factory).get(PersonalisedViewModel::class.java)
        personamodel?.activity = this
        instafeedmodel = ViewModelProvider(this, factory).get(InstafeedViewModel::class.java)
        instafeedmodel!!.context = this
        homemodel!!.getThemeData(this@HomePage).observe(this@HomePage, { setFeature(it) })
        homemodel!!.getFeatureLiveData(this@HomePage).observe(this@HomePage, { setFeature(it) })
        loadHomePage()
        homemodel!!.getToastMessage()
            .observe(this@HomePage, Observer<String> { consumeResponse(it) })
        homemodel!!.getHomePageData()
            .observe(this@HomePage, Observer<LinkedHashMap<String, View>> { consumeResponse(it) })
        if (featuresModel.memuWithApi) {
            leftMenuViewModel!!.Response()
                .observe(this, Observer<ApiResponse> { this.leftmenuconsumeResponse(it) })
        } else {
            leftMenuViewModel!!.MenuResponse()
                .observe(this, Observer<GraphQLResponse> { this.leftmenuconsumeResponse(it) })
        }
        if (featuresModel.forceUpdate) {
            forceUpdate()
        }
        homemodel!!.feedtoken.observe(this, { it ->
            getFeeds(it)
        })
        instafeedmodel?.InstafeedResponse()
            ?.observe(this@HomePage) { this@HomePage.showfeedsData(it) }
        homemodel?.notifyZendesk?.observe(this, Observer {
            if (it) {
                chat_but.visibility = View.VISIBLE
            } else {
                chat_but.visibility = View.GONE
            }

        })
        livesale.setOnClickListener {
//              var intent=Intent(this, LiveSaleActivity::class.java)
//              intent.putExtra("publickey","vBFfEYzWxwh62DMa")
//              intent.putExtra("usertype","guest")
//              startActivity(intent)
        }
        homemodel?.notifyfeaturesModel?.observe(this, Observer {
            if (it) {
                if (featuresModel.whatsappChat) {
                    //whatsappchat.visibility = View.VISIBLE
                    whatsappsection.visibility = View.VISIBLE
                    social_login.visibility = View.VISIBLE
                    sixthdivision.visibility = View.VISIBLE
                } else {
                    whatsappchat.visibility = View.GONE
                    whatsappsection.visibility = View.GONE
                }
                if (featuresModel.fbMessenger) {
                    //messengerchat.visibility = View.VISIBLE
                    fbsection.visibility = View.VISIBLE
                    social_login.visibility = View.VISIBLE
                    sixthdivision.visibility = View.VISIBLE
                } else {
                    messengerchat.visibility = View.GONE
                    fbsection.visibility = View.GONE
                }
            }
        })

        /*if (Urls(application as MyApplication).mid == "18") {
            demostoresss.visibility = View.GONE
            ScrollImage()
            demostoresss.setOnClickListener {
                val homepage = Intent(this@HomePage, ThemeSelectionActivity::class.java)
                startActivity(homepage)
                Constant.activityTransition(this)
            }
            demostoresnew.setOnClickListener {
                val homepage = Intent(this@HomePage, ThemeSelectionActivity::class.java)
                startActivity(homepage)
                Constant.activityTransition(this)
            }
        } else {
            demostoresss.visibility = View.GONE
            demostoresnew.visibility = View.GONE
        }*/
        binding!!.pullToRefresh.setOnRefreshListener {
            homemodel!!.RefreshHomePage()
            homemodel!!.initializeHashMap()
            if(homepage.childCount>0){
                homepage.removeAllViews()
            }
            loadHomePage()
            performRefresh()
        }
    }

    private fun performRefresh() {
       // homemodel!!.connectFirebaseForHomePageData(this, homepage)
        if(binding!!.pullToRefresh.isRefreshing){
            binding!!.pullToRefresh.isRefreshing = false
        }}

    private fun ScrollImage() {
        (this@HomePage as NewBaseActivity).demostoresss.setBackgroundResource(R.drawable.ic_themefirstvutton)
        binding!!.scrollview.viewTreeObserver.addOnScrollChangedListener {
            var scrollY: Int = binding!!.scrollview.scrollY
            if (scrollY > 0 && !isImageChanged) {
                (this@HomePage as NewBaseActivity).demostoresss.visibility = View.GONE
                (this@HomePage as NewBaseActivity).demostoresnew.visibility = View.VISIBLE
                (this@HomePage as NewBaseActivity).demostoresnew.setBackgroundResource(R.drawable.ic_themesecvutton)
                isImageChanged = true
            } else if (scrollY == 0 && isImageChanged) {
                (this@HomePage as NewBaseActivity).demostoresss.visibility = View.VISIBLE
                (this@HomePage as NewBaseActivity).demostoresnew.visibility = View.GONE
                (this@HomePage as NewBaseActivity).demostoresss.setBackgroundResource(R.drawable.ic_themefirstvutton)
                isImageChanged = false
            }
        }
    }

    private fun loadPersonalised(it: Boolean?) {
        if (it == true) {
            homemodel!!.getApiResponse()
                .observe(this, Observer<ApiResponse> { this.consumeResponse(it) })
            homemodel!!.getBestApiResponse()
                .observe(this, Observer<ApiResponse> { this.consumeResponse(it) })
        }
    }

    private fun forceUpdate() {
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager!!.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)
            ) {
                startUpdateFlow(appUpdateInfo)
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdateFlow(appUpdateInfo)
            }
        }
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateInfo
            appUpdateManager!!.startUpdateFlowForResult(
                appUpdateInfo,
                IMMEDIATE,
                this,
                MY_REQUEST_CODE
            )
        } catch (e: SendIntentException) {
            e.printStackTrace()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(
                    applicationContext,
                    "Update canceled by user! Result Code: " + resultCode,
                    Toast.LENGTH_LONG
                ).show()
                finishAffinity()
            } else if (resultCode == RESULT_OK) {
                Toast.makeText(
                    applicationContext,
                    "Update success! Result Code: " + resultCode,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Update Failed! Result Code: " + resultCode,
                    Toast.LENGTH_LONG
                ).show()
                forceUpdate()
            }
        }
    }

    private fun consumeFullSearch(it: Boolean?) {
        hasFullSearch = it!!
    }

    private fun ConsumeBanner(it: Boolean?) {
        hasBanner = it
    }

    override fun onPause() {
        super.onPause()
        drawer_layout.closeDrawers()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.m_search, menu)
        try {
            ////////////////Search menu Item//////////////
            var item = menu.findItem(R.id.search_item)
            item.setActionView(R.layout.m_searchicon)
            item.isVisible = homemodel!!.getSearchAsIcon()
            val view = item.actionView
            val searchicon = view?.findViewById<ImageView>(R.id.cart_icon)
            searchicon?.setColorFilter(Color.parseColor(icon_color))
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
            wishrelative?.backgroundTintList = ColorStateList.valueOf(Color.parseColor(count_color))
            wishtext?.setTextColor(Color.parseColor(count_textcolor))
            wishicon?.setColorFilter(Color.parseColor(icon_color))
            wishtext!!.text = "" + leftMenuViewModel!!.wishListcount
            wishitem.isVisible = homemodel!!.getWishlistIcon()
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
            cartrelative?.backgroundTintList = ColorStateList.valueOf(Color.parseColor(count_color))
            carttext?.setTextColor(Color.parseColor(count_textcolor))
            carticon?.setColorFilter(Color.parseColor(icon_color))
            carttext!!.text = "" + homemodel?.cartCount
            if (homemodel?.cartCount!! > 0) {
                cartrelative?.visibility = View.VISIBLE
                carttext.text = "" + homemodel?.cartCount
            }
            cartitem.actionView?.setOnClickListener {
                onOptionsItemSelected(cartitem)
            }
            setHomeIconColors(
                icon_color,
            )
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
                startActivity(Intent(this@HomePage, WishList::class.java))
                Constant.activityTransition(this@HomePage)
                true
            }
            R.id.cart_item -> {
                CoroutineScope(Dispatchers.IO).launch {
                    if (leftMenuViewModel?.repository?.getSellingPlanData()?.selling_plan_id != null) {
                        startActivity(Intent(this@HomePage, SubscribeCartList::class.java))
                        Constant.activityTransition(this@HomePage)
                    } else {
                        startActivity(Intent(this@HomePage, CartList::class.java))
                        Constant.activityTransition(this@HomePage)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun setHomeIconColors(iconcolor: String) {
        mDrawerToggle!!.drawerArrowDrawable.color = Color.parseColor(iconcolor)
    }

    fun consumeResponse(data: String) {
        Toast.makeText(this, data, Toast.LENGTH_LONG).show()
    }

    fun consumeResponse(data: LinkedHashMap<String, View>) {
        var interator = data.keys.iterator()
        while (interator.hasNext()) {
            var view = data.get(interator.next())
            if (view!!.getParent() != null) {
                (view!!.getParent() as ViewGroup).removeView(view)
            }
            homepage.addView(view)
        }
        shimmerStopHome()
       /* if (Urls(application as MyApplication).mid == "18") {
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            if (sharedPreferences.getBoolean("isFirstRun", true)) {
                val homepage = Intent(this@HomePage, ThemeSelectionActivity::class.java)
                startActivity(homepage)
                Constant.activityTransition(this)
                sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
            }
        }*/
        invalidateOptionsMenu()
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
        if (MagePrefs.getMaintenanceMode()!!) {
            var intent = Intent(this, MaintenenceActivity::class.java)
            startActivity(intent)
            Constant.activityTransition(this)
            finish()
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isOpen) {
            closesubmenu()
            drawer_layout.closeDrawers()
        } else {
            super.onBackPressed()
            featuresModel.tidioChat = false
            featuresModel.zenDeskChat = false
            featuresModel.yoptoLoyalty = false
            featuresModel.smileIO = false
            featuresModel.multi_currency = false
            featuresModel.multi_language = false
            featuresModel.showBottomNavigation = false
            featuresModel.reOrderEnabled = false
        }
    }

    private fun leftmenuconsumeResponse(reponse: GraphQLResponse) {
        when (reponse.status) {
            Status.SUCCESS -> LeftMenu.renderSuccessResponse(reponse)
            Status.ERROR -> { reponse.error!! }
            else -> {
            }
        }
    }

    private fun leftmenuconsumeResponse(reponse: ApiResponse) {
        when (reponse.status) {
            Status.SUCCESS -> LeftMenu.renderSuccessResponse(reponse.data!!)
            Status.ERROR -> { reponse.error!! }
            else -> {
            }
        }
    }
    private fun consumeResponse(reponse: ApiResponse) {
        when (reponse.status) {
            Status.SUCCESS -> setPersonalisedData(reponse.data!!)
            Status.ERROR -> { reponse.error!!.printStackTrace()
            }
        }
    }
    private fun setPersonalisedData(data: JsonElement) {
        try {
            val jsondata = JSONObject(data.toString())
            Log.i("MageNative", "TrendingProducts" + jsondata)
            if (jsondata.has("trending")) {
                binding!!.personalisedsection.visibility = View.VISIBLE
                setLayout(binding!!.personalised, "horizontal")
                personamodel!!.setPersonalisedData(
                    jsondata.getJSONObject("trending").getJSONArray("products"),
                    personalisedadapter,
                    binding!!.personalised
                )
            }
            if (jsondata.has("bestsellers")) {
                binding!!.bestsellerpersonalisedsection.visibility = View.VISIBLE
                setLayout(binding!!.bestpersonalised, "horizontal")
                personamodel!!.setPersonalisedData(
                    jsondata.getJSONObject("bestsellers").getJSONArray("products"),
                    padapter,
                    binding!!.bestpersonalised
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    fun setFeature(it: String) {
        when (it) {
            "themecolor" -> {
                showBottomBar()
                if (featuresModel.enableInstafeed) {
                    homemodel!!.getFeedToken()
                }
                updateColorInBottomBar()
            }
        }
        setFeatures()
        invalidateOptionsMenu()
    }

    fun loadHomePage() {
        var milisecond = 0
        if (MagePrefs.getHomePageData() != null) {
            Log.i("PullToRefresh","1")
            milisecond = 1000
            homemodel!!.cachedData(MagePrefs.getHomePageData()!!, this@HomePage)
        }
        Handler(Looper.myLooper()!!).postDelayed({
            Log.i("PullToRefresh","2")
            homemodel!!.connectFirebaseForHomePageData(this, homepage)
        }, milisecond.toLong())
    }

    fun getFeeds(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            instafeedmodel?.getInstafeeds(resources.getString(R.string.instafields), token)
        }
    }
    private fun showfeedsData(apiResponse: com.shopify.apicall.ApiResponse) {
        try {
            val jsondata = JSONObject(apiResponse.data.toString())
            if (jsondata.has("data")) {
                var dataarray = jsondata.getJSONArray("data")
                if (dataarray.length() > 0) {
                    binding!!.feedsSection.visibility = View.VISIBLE
                    feedsrecycler = setLayout(
                        binding!!.root.findViewById(R.id.feedsrecycler),
                        "insta" + Urls.InstaView
                    )
                    binding!!.feedtittle.text = Urls.InstaTittle
                    binding!!.feedusername.text = " " + "@" + dataarray.getJSONObject(0).getString("username")
                    binding!!.feedusername.setTextColor(Color.parseColor(themeColor))
                    if (dataarray.length() > Urls.InstaRange) {
                        var datarray = JSONArray()
                        for (i in 0..Urls.InstaRange - 1) {
                            datarray.put(i, dataarray.get(i))
                        }
                        dataarray = datarray
                    }
                    instafeed_adapters.setData(this, dataarray)
                    binding!!.feedsrecycler.adapter = instafeed_adapters
                    instafeed_adapters.notifyDataSetChanged()
                } else {
                    binding!!.feedsSection.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}



