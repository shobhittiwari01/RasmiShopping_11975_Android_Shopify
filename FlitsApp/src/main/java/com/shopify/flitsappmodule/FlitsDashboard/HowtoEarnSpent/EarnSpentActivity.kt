package com.shopify.flitsappmodule.FlitsDashboard.HowtoEarnSpent

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.internal.ToolbarUtils
import com.google.android.material.tabs.TabLayout
import com.shopify.apicall.ApiResponse
import com.shopify.flitsappmodule.R
import com.rasmishopping.app.FlitsDashboard.StoreCredits.StoreCreditsViewModel
import org.json.JSONObject
import java.util.*


class EarnSpentActivity : AppCompatActivity() {
    private val TAG = "PackageActivity"
    private val context: Context? = null
    private var toolbar: androidx.appcompat.widget.Toolbar? = null
    var cid:String?=null
    var userid:String?=null
    var token:String?=null
    var appname:String?=null
    var curencycode:String?=null
    var themecolor:String?=null
    var mDrawerToggle: ActionBarDrawerToggle? = null
    var themetextcolor:String?=null
    var Response:JSONObject?=null
    var title: TextView?=null
    private var model: EarnSpentViewModel? = null
    private var adapter: EarnSpentTabAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolbar)
        title = findViewById(R.id.title)
        if (getSupportActionBar() == null) {

            setSupportActionBar(toolbar)
        } else toolbar?.setVisibility(View.GONE)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);
        if(intent.getStringExtra("cid")!=null)
        {
            cid=intent.getStringExtra("cid")
        }
        if(intent.getStringExtra("token")!=null)
        {
            token=intent.getStringExtra("token")
        }
        if(intent.getStringExtra("userId")!=null)
        {
            userid=intent.getStringExtra("userId")
        }
        if(intent.getStringExtra("appname")!=null)
        {
            appname=intent.getStringExtra("appname")
        }
        if(intent.getStringExtra("currencycode")!=null)
        {
            curencycode=intent.getStringExtra("currencycode")
        }
        if(intent.getStringExtra("themecolor")!=null)
        {
            themecolor=intent.getStringExtra("themecolor")
        }
        if(intent.getStringExtra("themetextcolor")!=null)
        {
            themetextcolor=intent.getStringExtra("themetextcolor")
        }
        model = ViewModelProvider(this).get(EarnSpentViewModel::class.java)
        model!!.context = this
        model!!.getEarnSpentGuideData(appname!!,cid!!,userid!!,token!!)
        model!!.data.observe(this, {
            ConsumeReponse(it,curencycode!!)
        })
        var upArrow = resources.getDrawable(R.drawable.ic_backarrow_25)
        upArrow.setColorFilter(
            Color.parseColor(themetextcolor),
            PorterDuff.Mode.SRC_ATOP
        )
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setHomeAsUpIndicator(upArrow)
        viewPager = findViewById(R.id.viewPager) as ViewPager?
        viewPager!!.offscreenPageLimit = 2
        tabLayout = findViewById(R.id.tabLayout) as TabLayout?
        toolbar?.setBackgroundColor(Color.parseColor(themecolor))
        title?.setTextColor(Color.parseColor(themetextcolor))

    }

    private fun createTabFragment(response: JSONObject,currencycode:String) {
        adapter = EarnSpentTabAdapter(getSupportFragmentManager(), tabLayout!!,response!!,currencycode!!)
        viewPager!!.adapter = adapter
        tabLayout!!.setupWithViewPager(viewPager)
    }
    fun ConsumeReponse(response: ApiResponse,currencycode: String)
    {
        Response = JSONObject(response.data.toString())
        createTabFragment(Response!!,currencycode!!)
    }

override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
//    fun setToggle() {
//        mDrawerToggle = object : ActionBarDrawerToggle(
//            this,
//            toolbar,
//
//        ) {
//            override fun onDrawerOpened(drawerView: View) {
//                super.onDrawerOpened(drawerView)
//                Log.i("MageNativeSaif", "DrawerOPen")
//                invalidateOptionsMenu()
//            }
//
//            override fun onDrawerClosed(drawerView: View) {
//                super.onDrawerClosed(drawerView)
//                Log.i("MageNativeSaif", "DrawerClose")
//                invalidateOptionsMenu()
//            }
//
//        }
//        mDrawerToggle!!.syncState()
//    }

    companion object {
        var tabLayout: TabLayout? = null
        var viewPager: ViewPager? = null
    }
}
