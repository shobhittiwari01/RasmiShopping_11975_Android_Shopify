package com.rasmishopping.app.basesection.fragments
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.google.android.play.core.tasks.OnCompleteListener
import com.google.android.play.core.tasks.Task
import com.google.gson.JsonElement
import com.google.zxing.integration.android.IntentIntegrator

import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.addresssection.activities.AddressList
import com.rasmishopping.app.basesection.activities.DemoActivity
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.activities.Splash
import com.rasmishopping.app.basesection.activities.Weblink
import com.rasmishopping.app.basesection.models.MenuData
import com.rasmishopping.app.basesection.viewmodels.LeftMenuViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel.Companion.featuresModel
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.cartsection.activities.SubscribeCartList
import com.rasmishopping.app.collectionsection.activities.CollectionList
import com.rasmishopping.app.dashboard.activities.AccountActivity
import com.rasmishopping.app.databinding.MDynamicmenuBinding
import com.rasmishopping.app.databinding.MDynamicsubmenuBinding
import com.rasmishopping.app.databinding.MLeftmenufragmentBinding
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.livepreviewsection.LivePreview
import com.rasmishopping.app.loginsection.activity.LoginActivity
import com.rasmishopping.app.ordersection.activities.OrderList
import com.rasmishopping.app.productsection.activities.ProductList
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.userprofilesection.activities.UserProfile
import com.rasmishopping.app.utils.*
import com.rasmishopping.app.utils.Urls.Data.fbusername
import com.rasmishopping.app.utils.Urls.Data.whatsappnumber
import com.rasmishopping.app.wishlistsection.activities.WishList
import com.rasmishopping.app.yotporewards.rewarddashboard.RewardDashboard
import com.rasmishopping.app.yotporewards.withoutlogin.RewardsPointActivity
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Runnable
import javax.inject.Inject


class LeftMenu : BaseFragment() {
    private var binding: MLeftmenufragmentBinding? = null
    companion object {
        lateinit var menuList: LinearLayoutCompat
        private var currentcontext: Context? = null
        lateinit var sdkmenuList: LinearLayoutCompat
        lateinit var leftmenu: LeftMenuViewModel
        private var menuData: MenuData? = null
        var leftmenucallback: LeftMenuCallback? = null
        fun setcallback(leftmenucallback: LeftMenuCallback) {
            this.leftmenucallback = leftmenucallback
        }
        interface LeftMenuCallback {
            fun getLeftMenu()
        }
        fun renderSuccessResponse(response: GraphQLResponse) {
            if(menuList.childCount >0){
                menuList.removeAllViews()
            }
            CoroutineScope(CoroutineName("menudata")).launch(Dispatchers.IO) {
                val menudata = (response.data as GraphCallResult.Success<Storefront.QueryRoot>).response
                if (menuList.childCount == 0) {
                    try {
                        val `object` = menudata
                        val array = `object`.data?.menu?.items
                        var binding: MDynamicmenuBinding? =null
                        if (array?.size!! > 0) {
                            for (i in 0 until array.size) {
                                try {
                                    val menuData = MenuData()
                                    if (array.get(i).resourceId != null) {
                                        menuData.id = array.get(i).resourceId.toString()
                                    }
                                    if (array.get(i).type != null) {
                                        menuData.type = array.get(i).type.toString()
                                    }
                                    if (array.get(i).title != null) {
                                        menuData.title = array.get(i).title.toString()
                                    }
                                    if (array.get(i).url != null) {
                                        menuData.url = array.get(i).url.toString()
                                    }
                                    if(array.get(i).items.size>0){
                                        menuData.menuitems= array.get(i).items as ArrayList<Storefront.MenuItem>?
                                    }
                                    runBlocking(Dispatchers.Main) {
                                        binding =
                                            DataBindingUtil.inflate(
                                                currentcontext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                                                R.layout.m_dynamicmenu,
                                                null,
                                                false
                                            )
                                        binding?.menudata = menuData
                                        binding?.clickdata = LeftMenu().ClickHandlers(currentcontext)
                                        if (array.get(i).items.size > 0) {
                                            binding?.root?.findViewById<View>(R.id.expand_collapse)?.visibility = View.VISIBLE
                                            binding?.root?.findViewById<View>(R.id.expand_collapse)?.tag="expand"
                                            updateMenu(array.get(i).items, binding?.root?.findViewById(R.id.submenus))
                                        }
                                        delay(100)
                                        menuList.addView(binding?.root)
                                    }

                                } catch (e: Exception) {
                                    Log.i("MageNative", "Error" + e.message)
                                    Log.i("MageNative", "Error" + e.cause)
                                    e.printStackTrace()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        private fun updateMenu(
            array: MutableList<Storefront.MenuItem>,
            menulist: LinearLayoutCompat?
        ) {
            CoroutineScope(CoroutineName("longRunning")).launch(Dispatchers.IO) {
                if (array.size > 0) {
                    var binding:MDynamicsubmenuBinding?=null
                    for (i in 0 until array.size) {
                        try {
                            val menuData = MenuData()
                            if (array.get(i).resourceId != null) {
                                menuData.id = array.get(i).resourceId.toString()
                            }
                            if (array.get(i).type != null) {
                                menuData.type = array.get(i).type.toString()
                            }
                            if (array.get(i).title != null) {
                                menuData.title = array.get(i).title.toString()
                                //      Constant.translateField(menuData.title!!,binding.catname)
                            }
                            if (array.get(i).url != null) {
                                menuData.url = array.get(i).url.toString()
                            }
                            if(array.get(i).items.size>0){
                                menuData.menuitems= array.get(i).items as ArrayList<Storefront.MenuItem>?
                            }
                            runBlocking(Dispatchers.Main) {
                                binding = DataBindingUtil.inflate<MDynamicsubmenuBinding>(
                                    currentcontext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                                    R.layout.m_dynamicsubmenu,
                                    null,
                                    false
                                )
                                binding?.menudata = menuData
                                binding?.clickdata = LeftMenu().ClickHandlers(currentcontext)
                                if (array.get(i).items.size > 0) {
                                    binding?.root?.findViewById<View>(R.id.expand_collapse)?.visibility = View.VISIBLE
                                    binding?.root?.findViewById<View>(R.id.expand_collapse)?.tag="expand"
                                    updateMenu(array.get(i).items, binding?.root?.findViewById(R.id.submenus))
                                }
                                delay(100)
                                menulist?.addView(binding?.root)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        fun renderSuccessResponse(data: JsonElement) {
            if(menuList.childCount >0){
                menuList.removeAllViews()
            }
            if (menuList.childCount == 0) {
                CoroutineScope(CoroutineName("menudata")).launch(Dispatchers.IO) {
                    try {
                        val `object` = JSONObject(data.toString())
                        if (`object`.getBoolean("success")) {
                            if (`object`.has("data")) {
                                val array = `object`.getJSONArray("data")
                                if (array.length() > 0) {
                                    for (i in 0 until array.length()) {
                                        val menuData = MenuData()
                                        if (array.getJSONObject(i).has("id")) {
                                            menuData.id = array.getJSONObject(i).getString("id")
                                        }
                                        if (array.getJSONObject(i).has("handle")) {
                                            menuData.handle = array.getJSONObject(i).getString("handle")
                                        }
                                        if (array.getJSONObject(i).has("type")) {
                                            menuData.type = array.getJSONObject(i).getString("type")
                                        }
                                        if (array.getJSONObject(i).has("title")) {
                                            menuData.title = array.getJSONObject(i).getString("title")
                                        }
                                        runBlocking(Dispatchers.Main) {
                                            try {
                                                val binding: MDynamicmenuBinding =
                                                    DataBindingUtil.inflate(
                                                        currentcontext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                                                        R.layout.m_dynamicmenu,
                                                        null,
                                                        false
                                                    )
                                                binding.menudata = menuData
                                                binding.clickdata = LeftMenu().ClickHandlers(currentcontext)
                                                if (array.getJSONObject(i).has("menus")) {
                                                    if (array.getJSONObject(i).getJSONArray("menus").length() > 0) {
                                                        binding.root.findViewById<View>(R.id.expand_collapse).visibility = View.VISIBLE
                                                        binding.root.findViewById<View>(R.id.expand_collapse).tag = "expand"
                                                        updateMenu(array.getJSONObject(i).getJSONArray("menus"), binding.root.findViewById(R.id.submenus))
                                                    }
                                                }
                                                delay(100)
                                                menuList.addView(binding.root)
                                            } catch (e: Exception) {
                                                Log.i("MageNative", "Error" + e.message)
                                                Log.i("MageNative", "Error" + e.cause)
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        private fun updateMenu(array: JSONArray, menuList: LinearLayoutCompat) {
            CoroutineScope(CoroutineName("longRunning")).launch(Dispatchers.IO) {
                    if (array.length() > 0) {
                        for (i in 0 until array.length()) {
                            try {
                                val menuData = MenuData()
                                if (array.getJSONObject(i).has("id")) {
                                    menuData.id = array.getJSONObject(i).getString("id")
                                }
                                if (array.getJSONObject(i).has("handle")) {
                                    menuData.handle = array.getJSONObject(i).getString("handle")
                                }
                                if (array.getJSONObject(i).has("title")) {
                                    menuData.title =
                                        array.getJSONObject(i).getString("title")
                                }
                                if (array.getJSONObject(i).has("type")) {
                                    menuData.type = array.getJSONObject(i).getString("type")
                                }
                                if (array.getJSONObject(i).has("url")) {
                                    menuData.url = array.getJSONObject(i).getString("url")
                                }

                                runBlocking(Dispatchers.Main) {
                                    val binding = DataBindingUtil.inflate<MDynamicsubmenuBinding>(
                                        currentcontext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                                        R.layout.m_dynamicsubmenu,
                                        null,
                                        false
                                    )
                                    binding.menudata = menuData
                                    binding.clickdata = LeftMenu().ClickHandlers(currentcontext)
                                    if (array.getJSONObject(i).has("menus") && array.getJSONObject(i).getJSONArray("menus").length() > 0) {
                                        binding.root.findViewById<View>(R.id.expand_collapse).visibility = View.VISIBLE
                                        binding.root.findViewById<View>(R.id.expand_collapse).tag = "expand"
                                        updateMenu(array.getJSONObject(i).getJSONArray("menus"), binding.root.findViewById(R.id.submenus))
                                    }
                                    delay(100)
                                    menuList.addView(binding.root)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    var currentactivity: Activity? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            R.layout.m_leftmenufragment,
            container,
            true
        )
        sdkmenuList = binding?.sdkmenulist!!
        menuList = binding?.menulist!!
        var pInfo: PackageInfo? = null
        try {
            pInfo =
                requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val version = pInfo!!.versionName
        val versioncode = pInfo.versionCode
        Log.i("MageNative", "LeftMenuResume 4")
        menuData = MenuData()
        val app_version = "App v. $version"
        menuData!!.appversion = app_version
        menuData!!.copyright = resources.getString(R.string.copy) + resources.getString(R.string.app_name)
        binding!!.features = featuresModel
        binding!!.menudata = menuData
        binding!!.clickdata = ClickHandlers(currentcontext, binding)
        (requireActivity().application as MyApplication).mageNativeAppComponent!!.doLeftMeuInjection(
            this
        )
        binding!!.closedrawer.setOnClickListener {
            currentactivity as HomePage
            Log.d("pd", "" + currentactivity)
            if (leftmenucallback != null)
                leftmenucallback?.getLeftMenu()
            closeSubMenus(currentcontext!!)
        }

        binding!!.seconddivision.setOnClickListener {
            if (leftmenu.isLoggedIn) {
                val accountActivity = Intent(context, AccountActivity::class.java)
                requireContext().startActivity(accountActivity)
                Constant.activityTransition(requireContext())
            } else {
                val loginActivity = Intent(context, LoginActivity::class.java)
                requireContext().startActivity(loginActivity)
                Constant.activityTransition(requireContext())
            }
            closeSubMenus(currentcontext!!)
        }
        leftmenu = ViewModelProvider(this, viewModelFactory).get(LeftMenuViewModel::class.java)
        leftmenu.setCurrentBinding(binding!!)
        leftmenu.context = requireActivity()
        leftmenu.getblogs()
        leftmenu.blogslivedata.observe(viewLifecycleOwner, androidx.lifecycle.Observer { this.consumeblogs(it) })
        val runnable = Runnable {
            if (leftmenu.repository.getPreviewData().isNotEmpty()) {
                binding!!.livepreview.text = resources.getString(R.string.movetodemostore)
                binding!!.previewsub.text = resources.getString(R.string.previewsubtxt)
                binding!!.scannerimg.visibility=View.GONE
            } else {
                binding!!.livepreview.text = resources.getString(R.string.Livepreview)
                binding!!.previewsub.text =
                    resources.getString(R.string.scan_for_see_your_store_in_phone)
                binding!!.scannerimg.visibility=View.VISIBLE
            }
        }
        Thread(runnable).start()
        //leftmenu.data.observe(requireActivity(), Observer<HashMap<String, String>> { this.consumeResponse(it) })
        return binding!!.root
    }

    private fun consumeResponse(hash: HashMap<String, String>) {
        var shortname = ""
        menuData!!.tag = hash.get("tag")
        menuData!!.username = hash["firstname"] + " " + hash["secondname"]
        binding?.signin?.text = hash["firstname"] + " " + hash["secondname"]
        if (hash["tag"] == "login") {
            binding?.profileicon?.visibility = View.GONE
            binding?.userName?.visibility = View.VISIBLE
            when(hash["firstname"]!!.length){
                0->{
                    shortname = " "
                }
                else->{
                    shortname = hash["firstname"]!!.substring(0, 1)
                }
            }
            when(hash["secondname"]!!.length){
                0->{
                    shortname = shortname+" "
                }
                else->{
                    shortname = shortname+hash["secondname"]!!.substring(0, 1)
                }
            }
            binding?.usernameShortForm?.text = shortname
            menuData!!.visible = View.VISIBLE
        } else {
            binding?.profileicon?.visibility = View.VISIBLE
            binding?.userName?.visibility = View.INVISIBLE
            binding?.usernameShortForm?.visibility = View.GONE
            binding?.signin?.text = context?.resources?.getString(R.string.SignIn)
            Constant.translateField(resources.getString(R.string.SignIn), binding!!.signin)
            binding?.seconddivision?.tag = "Sign In"
            menuData!!.visible = View.GONE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        currentcontext = context
        currentactivity = context as NewBaseActivity
    }

    private fun consumeblogs(response: GraphQLResponse?) {
        when (response?.status) {
            Status.SUCCESS -> {
                val result =
                    (response.data as GraphCallResult.Success<Storefront.QueryRoot>).response
                if (result.hasErrors) {
                    val errors = result.errors
                    val iterator = errors.iterator()
                    val errormessage = StringBuilder()
                    var error: Error? = null
                    while (iterator.hasNext()) {
                        error = iterator.next()
                        errormessage.append(error.message())
                    }
//                    Toast.makeText(this, "" + errormessage, Toast.LENGTH_SHORT).show()
                } else {
                    for (i in 0 until result.data?.blogs?.edges?.size!!) {
                        val binding: MDynamicmenuBinding =
                            DataBindingUtil.inflate(
                                currentcontext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                                R.layout.m_dynamicmenu,
                                null,
                                false
                            )
                        menuData = MenuData()
                        menuData?.title = (result.data?.blogs?.edges?.get(i)?.node?.title)
                        Constant.translateField(menuData?.title!!, binding.catname)
                        menuData?.type = "Blogs"
                        menuData?.url = result.data?.blogs?.edges?.get(i)?.node?.onlineStoreUrl
                        binding.menudata = menuData
                        binding.clickdata = ClickHandlers(currentcontext)
                        sdkmenuList.addView(binding.root)
                    }
                }
            }
        }
    }

    inner class ClickHandlers(
        internal var context: Context?,
        internal var binding: MLeftmenufragmentBinding? = null
    ) {
        private var open = false
        fun getMenu(view: View, menudata: MenuData) {
            Log.i("SaifDevMenu", "" + menudata.type!!.uppercase())
            closeSubMenus(currentcontext!!)
            when (menudata.type!!.uppercase()) {
                "HTTP" -> {
                    val blog = Intent(context, Weblink::class.java)
                    blog.putExtra("name", menudata.title)
                    blog.putExtra("link", menudata.url)
                    context!!.startActivity(blog)
                    Constant.activityTransition(context!!)
                }
                "FRONTPAGE" -> {
                    val blog = Intent(context, Weblink::class.java)
                    blog.putExtra("name", menudata.title)
                    blog.putExtra("link", menudata.url)
                    context!!.startActivity(blog)
                    Constant.activityTransition(context!!)
                }
                "COLLECTION" -> {
                    try {
                        val intent = Intent(context, ProductList::class.java)
                        if (menudata.id == null) {
                            intent.putExtra("handle", menudata.handle)
                        } else {
                            if(featuresModel.memuWithApi){
                                intent.putExtra("ID", "gid://shopify/Collection/"+menudata.id!!)
                            }else{
                                intent.putExtra("ID", menudata.id!!)
                            }
                            if (menudata.menuitems != null && menudata.menuitems!!.size > 0) {
                                intent.putExtra("menudata", menudata)
                            }
                        }
                        intent.putExtra("tittle", menudata.title)
                        context!!.startActivity(intent)
                        Constant.activityTransition(context!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                "PRODUCT" -> {
                    val productintent = Intent(context, ProductView::class.java)
                    if (menudata.id == null) {
                        productintent.putExtra("handle", menudata.handle)
                    } else {
                        try {
                            if(featuresModel.memuWithApi){
                                productintent.putExtra("ID", "gid://shopify/Product/"+menudata.id!!)
                            }else{
                                productintent.putExtra("ID", menudata.id!!)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    productintent.putExtra("tittle", menudata.title)
                    context!!.startActivity(productintent)
                    Constant.activityTransition(context!!)
                }
                "CATALOG" -> {
                    val product_all = Intent(context, ProductList::class.java)
                    product_all.putExtra("tittle", menudata.title)
                    context!!.startActivity(product_all)
                    Constant.activityTransition(context!!)
                }
                "COLLECTIONS" -> {
                    val collection_all = Intent(context, CollectionList::class.java)
                    context!!.startActivity(collection_all)
                    Constant.activityTransition(context!!)
                }
                "PAGE" -> {
                    val page = Intent(context, Weblink::class.java)
                    page.putExtra("name", menudata.title)
                    page.putExtra("link", menudata.url)
                    context!!.startActivity(page)
                    Constant.activityTransition(context!!)
                }
                "SHOP_POLICY" -> {
                    val blog = Intent(context, Weblink::class.java)
                    blog.putExtra("name", menudata.title)
                    blog.putExtra("link", menudata.url)
                    context!!.startActivity(blog)
                    Constant.activityTransition(context!!)

                }
                "BLOG" -> {
                    val blog = Intent(context, Weblink::class.java)
                    blog.putExtra("name", menudata.title)
                    blog.putExtra("link", menudata.url)
                    context!!.startActivity(blog)
                    Constant.activityTransition(context!!)
                }
                "BLOGS" -> {
                    val blog = Intent(context, Weblink::class.java)
                    blog.putExtra("name", menudata.title)
                    blog.putExtra("link", menudata.url)
                    context!!.startActivity(blog)
                    Constant.activityTransition(context!!)
                }
            }
        }

        fun expandMenu(view: View, menudata: MenuData, isfirstlevel: Boolean) {
            val constraintLayout = view.parent.parent as ConstraintLayout
            var level = 1
            if (isfirstlevel) {
                level = 2
            }
            val linearLayoutCompat = constraintLayout.getChildAt(level) as LinearLayoutCompat
            var image = view as AppCompatImageView
            if (view.tag != null && view.tag == "expand") {
                if (open) {
                    linearLayoutCompat.visibility = View.GONE
                    if (isfirstlevel) {
                        val backview = constraintLayout.getChildAt(1)
                        backview.visibility = View.GONE
                    }
                    open = false
                    when (isfirstlevel) {
                        true -> image.setImageResource(R.drawable.ic_forward)
                        false -> image.setImageResource(R.drawable.ic_plus)
                    }

                } else {
                    linearLayoutCompat.visibility = View.VISIBLE
                    if (isfirstlevel) {
                        if (!SplashViewModel.viewhashmap.containsKey("current")) {
                            SplashViewModel.viewhashmap.put("current", view)
                        } else {
                            if (SplashViewModel.viewhashmap.get("current")!! != view) {
                                hideOtherExpanded(SplashViewModel.viewhashmap.get("current")!!, 2)
                                SplashViewModel.viewhashmap.put("current", view)
                            }
                        }
                        val backview: View = constraintLayout.getChildAt(1)
                        var back = backview.background as GradientDrawable
                        back.setColor(Color.parseColor(NewBaseActivity.themeColor))
                        back.setStroke(2, Color.parseColor(NewBaseActivity.themeColor))
                        backview.background = back
                        backview.visibility = View.VISIBLE
                    } else {
                        if (!SplashViewModel.viewhashmap.containsKey("submenu")) {
                            SplashViewModel.viewhashmap.put("submenu", view)
                        } else {
                            if (SplashViewModel.viewhashmap.get("submenu")!! != view) {
                                hideOtherExpanded(SplashViewModel.viewhashmap.get("submenu")!!, 1)
                                SplashViewModel.viewhashmap.put("submenu", view)
                            }
                        }
                        when (isfirstlevel) {
                            true -> image.setImageResource(R.drawable.ic_down)
                            false -> image.setImageResource(R.drawable.ic_minus)
                        }
                        open = true
                    }
                    when (isfirstlevel) {
                        true -> image.setImageResource(R.drawable.ic_down)
                        false -> image.setImageResource(R.drawable.ic_minus)
                    }
                    open = true
                }
            } else {
                getMenu(view, menudata)

            }
        }

        fun hideOtherExpanded(view: View, level: Int) {
            val constraintLayout = view.parent.parent as ConstraintLayout
            if (constraintLayout.getChildAt(level) is LinearLayoutCompat) {
                val linearLayoutCompat = constraintLayout.getChildAt(level) as LinearLayoutCompat
                var image = view as AppCompatImageView
                if (view.tag != null && view.tag == "expand") {
                    linearLayoutCompat.visibility = View.GONE
                    when (level) {
                        1 -> image.setImageResource(R.drawable.ic_plus)
                        2 -> image.setImageResource(R.drawable.ic_forward)
                    }
                }
            }
        }

        fun navigationClicks(view: View) {
            Log.d("javed", "navigationClicks: " + view)
            closeSubMenus(currentcontext!!)
            when (view.tag as String) {
                "livepreview" -> {
                    val integrator = IntentIntegrator((context as NewBaseActivity))
                    integrator.setPrompt("Scan Your Barcode")
                    integrator.setCameraId(0) // Use a specific camera of the device
                    integrator.setOrientationLocked(true)
                    integrator.setBeepEnabled(true)
                    integrator.captureActivity = LivePreview::class.java
                    integrator.initiateScan()
                    val runnable = Runnable {
                        if (leftmenu.repository.getPreviewData().isNotEmpty()) {
                            val runnable = Runnable {
                                leftmenu.deletLocal()
                                leftmenu.deleteData()
                                MagePrefs.clearHomePageData()
                                leftmenu.repository.deletePreviewData()
                                leftmenu.logOut()
                                MagePrefs.clearCountry()
                                SplashViewModel.viewhashmap = HashMap<String, View>()
                                var intent = Intent(context, Splash::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context!!.startActivity(intent)
                                Constant.activityTransition(context!!)
                            }
                            Thread(runnable).start()
                        } else {
                            val integrator = IntentIntegrator((context as NewBaseActivity))
                            integrator.setPrompt("Scan Your Barcode")
                            integrator.setCameraId(0) // Use a specific camera of the device
                            integrator.setOrientationLocked(true)
                            integrator.setBeepEnabled(true)
                            integrator.captureActivity = LivePreview::class.java
                            integrator.initiateScan()
                        }
                    }
                    Thread(runnable).start()
                }

                "whatsapp" -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data =
                            Uri.parse("http://api.whatsapp.com/send?phone=$whatsappnumber")
                        context!!.startActivity(intent)
                    } catch (e: Exception) {
                        context!!.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")
                            )
                        )
                    }
                }

                "facebook" -> {
                    if (isPackageInstalled(context!!)) {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.setPackage("com.facebook.orca")
                        intent.data = Uri.parse(fbusername)
                        context!!.startActivity(intent)
                    } else {
                        try {
                            context!!.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=com.facebook.orca")
                                )
                            )
                        } catch (e: ActivityNotFoundException) {
                            context!!.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=com.facebook.orca")
                                )
                            )
                        }
                    }
                }
                "currencyswitcher" -> {
                    Log.i("MageNative", "currencyswitcher" + " : IN")
                    //  (context as NewBaseActivity).getCurrency()
                }
                /*"rateus" -> {
                      (context as NewBaseActivity).InAppReviewFlow()
                }*/
                "countrycodeswitcher" -> {
                    Log.i("MageNative", "currencyswitcher" + " : IN")
                    (context as NewBaseActivity).getCountryCode()
                }
                "languageswither" -> {
                    (context as NewBaseActivity).getLanguageCode()
                }
                "collections" -> {
                    val collection_all = Intent(context, CollectionList::class.java)
                    context!!.startActivity(collection_all)
                    Constant.activityTransition(context!!)
                }
                "Sign In" -> {
                    if (leftmenu.isLoggedIn) {
                        val accountActivity = Intent(context, AccountActivity::class.java)
                        context!!.startActivity(accountActivity)
                        Constant.activityTransition(context!!)
                    } else {
                        val loginActivity = Intent(context, LoginActivity::class.java)
                        context!!.startActivity(loginActivity)
                        Constant.activityTransition(context!!)
                    }
                }
                "mywishlist" -> {
                    context?.startActivity(
                        Intent(
                            context,
                            WishList::class.java
                        )
                    )
                    Constant.activityTransition(context!!)
                }
                "mycartlist" -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (leftmenu.repository.getSellingPlanData().selling_plan_id != null) {
                            context?.startActivity(Intent(context, SubscribeCartList::class.java))
                            Constant.activityTransition(context!!)
                        } else {
                            context?.startActivity(Intent(context, CartList::class.java))
                            Constant.activityTransition(context!!)
                        }
                    }

                }
                "invitefriends" -> {
                    val appPackageName =
                        view.context.packageName // getPackageName() from Context or Activity object
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(
                        Intent.EXTRA_SUBJECT,
                        view.context.resources.getString(R.string.app_name)
                    )
                    shareIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        "https://play.google.com/store/apps/details?id=$appPackageName"
                    )
                    view.context.startActivity(
                        Intent.createChooser(
                            shareIntent,
                            view.context.resources.getString(R.string.shareproduct)
                        )
                    )
                    Constant.activityTransition(view.context)
                }
                "autosearch" -> {
                    var activity: NewBaseActivity = context as NewBaseActivity
                    activity.moveToSearch(activity)
                }
                "earnrewards" -> {
                    if (leftmenu.isLoggedIn) {
                        val rewards = Intent(context, RewardDashboard::class.java)
                        context!!.startActivity(rewards)
                        Constant.activityTransition(context!!)
                    } else {
                        val rewards = Intent(context, RewardsPointActivity::class.java)
                        context!!.startActivity(rewards)
                        Constant.activityTransition(context!!)
                    }
                }
                "chats" -> {
                    val chats = Intent(context, Weblink::class.java)
                    chats.putExtra("name", context!!.resources.getString(R.string.chat))
                    chats.putExtra(
                        "link",
                        "https://shopifymobileapp.cedcommerce.com/shopifymobile/tidiolivechatapi/chatpanel?shop=${
                            Urls(
                                MyApplication.context
                            ).shopdomain
                        }"
                    )
                    context!!.startActivity(chats)
                    Constant.activityTransition(context!!)
                }
                "smilereward" -> {
                    if (leftmenu.isLoggedIn) {
                        val intent = Intent(context, Weblink::class.java)
                        intent.putExtra("name", "My Rewards")
                        intent.putExtra(
                            "link",
                            "https://shopifymobileapp.cedcommerce.com/shopifymobile/smilerewardapi/generateview?mid=${
                                Urls(
                                    MyApplication.context
                                ).mid
                            }&cid=" + MagePrefs.getCustomerID()
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        context!!.startActivity(intent)
                        Constant.activityTransition(context!!)
                    } else {
                        val rewards = Intent(context, LoginActivity::class.java)
                        context!!.startActivity(rewards)
                        Constant.activityTransition(context!!)
                    }
                }
                "logout" -> {
                    MagePrefs.clearUserData()
                    binding?.signin?.text = context?.resources?.getString(R.string.SignIn)
                    Constant.translateField(
                        context?.resources?.getString(R.string.SignIn)!!,
                        binding!!.signin
                    )
                    binding?.seconddivision?.tag = "Sign In"
                    Toast.makeText(
                        context,
                        context!!.resources.getString(R.string.successlogout),
                        Toast.LENGTH_LONG
                    ).show()
                    leftmenu.logOut()
                    MagePrefs.clearSocialKey()
                }
                "myprofile" -> if (leftmenu.isLoggedIn) {
                    val myprofile = Intent(context, UserProfile::class.java)
                    context!!.startActivity(myprofile)
                    Constant.activityTransition(context!!)
                } else {
                    Toast.makeText(
                        context,
                        context!!.resources.getString(R.string.logginfirst),
                        Toast.LENGTH_LONG
                    ).show()
                }
                "myorders" -> if (leftmenu.isLoggedIn) {
                    val myprofile = Intent(context, OrderList::class.java)
                    context!!.startActivity(myprofile)
                    Constant.activityTransition(context!!)
                } else {
                    Toast.makeText(
                        context,
                        context!!.resources.getString(R.string.logginfirst),
                        Toast.LENGTH_LONG
                    ).show()
                }
                "myaddress" -> if (leftmenu.isLoggedIn) {
                    val myaddress = Intent(context, AddressList::class.java)
                    context!!.startActivity(myaddress)
                    Constant.activityTransition(context!!)
                } else {
                    Toast.makeText(
                        context,
                        context!!.resources.getString(R.string.logginfirst),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }

        private fun isPackageInstalled(context: Context): Boolean {
            return try {
                val packageManager = context.packageManager
                packageManager.getPackageInfo("com.facebook.orca", 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i("MageNative", "LeftMenuResume")
        when (requireActivity().packageName) {
            "com.rasmishopping.app" -> {
                menuData!!.previewvislible = View.GONE
            }
            else -> {
                menuData!!.previewvislible = View.GONE
            }
        }
        leftmenu.context = currentcontext
        consumeResponse(leftmenu.fetchData())

    }

    fun closeSubMenus(context: Context) {
        if (SplashViewModel.viewhashmap.containsKey("current")) {
            LeftMenu().ClickHandlers(context)
                .hideOtherExpanded(SplashViewModel.viewhashmap.get("current")!!, 2)
        }
        if (SplashViewModel.viewhashmap.containsKey("submenu")) {
            LeftMenu().ClickHandlers(context)
                .hideOtherExpanded(SplashViewModel.viewhashmap.get("submenu")!!, 1)
        }
        if (binding != null) {
            binding!!.scroll.fullScroll(View.FOCUS_UP)
        }
    }
}
