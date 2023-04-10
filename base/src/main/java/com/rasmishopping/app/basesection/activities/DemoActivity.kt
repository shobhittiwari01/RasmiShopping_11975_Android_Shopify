package com.rasmishopping.app.basesection.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.viewmodels.DemoThemeViewModel
import com.rasmishopping.app.databinding.MDemopageBinding
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.ViewModelFactory
import kotlinx.android.synthetic.main.m_newbaseactivity.*
import javax.inject.Inject

class DemoActivity: NewBaseActivity() {
    @Inject
    lateinit var factory: ViewModelFactory
    private var binding: MDemopageBinding? = null
    lateinit var homepage: LinearLayoutCompat
    private var homemodel: DemoThemeViewModel? = null
    private var isImageChanged = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        //Constant.themeConfiguration(this)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_demopage, group, true)
        shimmerStartHome()
        showHumburger()
        homepage = binding!!.homecontainer
        (application as MyApplication).mageNativeAppComponent!!.doDemoThemeInjection(this)
        homemodel = ViewModelProvider(this, factory).get(DemoThemeViewModel::class.java)
        homemodel!!.context = this
        homemodel!!.getHomePageData().observe(this@DemoActivity) { consumeResponse(it) }
        val milisecond=0
        Handler(Looper.myLooper()!!).postDelayed({
            homemodel!!.GetResponse(this, homepage)
        }, milisecond.toLong())
        Log.i("SELECTED THEME",""+MagePrefs.getTheme())
        when(MagePrefs.getTheme()) {
            "Grocery Theme" -> {
                (this@DemoActivity as NewBaseActivity).demostoresss.setBackgroundResource(R.drawable.ic_themefirstvuttongrocery)
            }
            "Home Theme" -> {
                (this@DemoActivity as NewBaseActivity).demostoresss.setBackgroundResource(R.drawable.ic_themefirstvuttonhome)
            }
            "Fashion Theme" -> {
                (this@DemoActivity as NewBaseActivity).demostoresss.setBackgroundResource(R.drawable.ic_themefirstvuttonfashion)
            }
        }
        binding!!.scrollview.viewTreeObserver.addOnScrollChangedListener {
            var scrollY: Int = binding!!.scrollview.scrollY
            if (scrollY > 0 && !isImageChanged) {
                (this@DemoActivity as NewBaseActivity).demostoresss.visibility = View.GONE
                (this@DemoActivity as NewBaseActivity).demostoresnew.visibility = View.VISIBLE
                when(MagePrefs.getTheme()) {
                    "Grocery Theme" -> {
                        (this@DemoActivity as NewBaseActivity).demostoresnew.setBackgroundResource(R.drawable.ic_themesecvuttongrocery)
                    }
                    "Home Theme" -> {
                        (this@DemoActivity as NewBaseActivity).demostoresnew.setBackgroundResource(R.drawable.ic_themesecvuttonhome)
                    }
                    "Fashion Theme" -> {
                        (this@DemoActivity as NewBaseActivity).demostoresnew.setBackgroundResource(R.drawable.ic_themesecvuttonfashion)
                    }
                }
                isImageChanged = true
            } else if (scrollY == 0 && isImageChanged) {
                (this@DemoActivity as NewBaseActivity).demostoresss.visibility = View.VISIBLE
                (this@DemoActivity as NewBaseActivity).demostoresnew.visibility = View.GONE
                when(MagePrefs.getTheme()) {
                    "Grocery Theme" -> {
                        (this@DemoActivity as NewBaseActivity).demostoresss.setBackgroundResource(R.drawable.ic_themefirstvuttongrocery)
                    }
                    "Home Theme" -> {
                        (this@DemoActivity as NewBaseActivity).demostoresss.setBackgroundResource(R.drawable.ic_themefirstvuttonhome)
                    }
                    "Fashion Theme" -> {
                        (this@DemoActivity as NewBaseActivity).demostoresss.setBackgroundResource(R.drawable.ic_themefirstvuttonfashion)
                    }
                }
                isImageChanged = false
            }
        }
    }

    private fun consumeResponse(data: LinkedHashMap<String, View>?) {
        var interator=data!!.keys.iterator()
        while (interator.hasNext()){
            var view =data.get(interator.next())
            homepage.addView(view)
        }
        shimmerStopHome()
    }
}