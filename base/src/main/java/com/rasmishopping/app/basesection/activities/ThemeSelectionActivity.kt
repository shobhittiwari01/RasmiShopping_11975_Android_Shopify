package com.rasmishopping.app.basesection.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.databinding.MThemeselectBinding
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import kotlinx.android.synthetic.main.m_themeselect.*

class ThemeSelectionActivity: NewBaseActivity() {
    private var binding: MThemeselectBinding? = null
    val items = listOf("All Stores", "Grocery Store", "Fashion Store", "Home Decor Store")
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_themeselect, group, true)
        hidenavbottom()
        hidethemeselector()
        supportActionBar!!.hide()
        (application as MyApplication).mageNativeAppComponent!!.doThemeselectionInjection(this)
        if(HomePageViewModel.isDarkModeOn()) {
            themecontainer.setBackgroundColor(Color.parseColor(resources.getString(R.color.white)))
            scrollcontainer.setBackgroundColor(Color.parseColor(resources.getString(R.color.white)))
            livetheme.setBackgroundColor(Color.parseColor(resources.getString(R.color.white)))
            grocerytheme.setBackgroundColor(Color.parseColor(resources.getString(R.color.white)))
            fashiontheme.setBackgroundColor(Color.parseColor(resources.getString(R.color.white)))
            hometheme.setBackgroundColor(Color.parseColor(resources.getString(R.color.white)))
            //electheme.setBackgroundColor(Color.parseColor(resources.getString(R.color.white)))
            separatorview.setBackgroundColor(Color.parseColor(resources.getString(R.color.white)))
            Glide.with(this).load(resources.getDrawable(R.drawable.ic_closeicon_dark)).into(closedialog)
        }
        closedialog.setOnClickListener {
            finish()
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding!!.themefilter.adapter = adapter
        binding!!.themefilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        binding!!.livetheme.visibility = View.VISIBLE
                        binding!!.grocerytheme.visibility = View.VISIBLE
                        binding!!.fashiontheme.visibility = View.VISIBLE
                        binding!!.hometheme.visibility = View.VISIBLE
                        //binding!!.electheme.visibility = View.VISIBLE
                    }
                    1 -> {
                        binding!!.livetheme.visibility = View.GONE
                        binding!!.grocerytheme.visibility = View.VISIBLE
                        binding!!.fashiontheme.visibility = View.GONE
                        binding!!.hometheme.visibility = View.GONE
                        //binding!!.electheme.visibility = View.GONE
                    }
                    2 -> {
                        binding!!.livetheme.visibility = View.GONE
                        binding!!.grocerytheme.visibility = View.GONE
                        binding!!.fashiontheme.visibility = View.VISIBLE
                        binding!!.hometheme.visibility = View.GONE
                        //binding!!.electheme.visibility = View.GONE
                    }
                    3 -> {
                        binding!!.livetheme.visibility = View.GONE
                        binding!!.grocerytheme.visibility = View.GONE
                        binding!!.fashiontheme.visibility = View.GONE
                        binding!!.hometheme.visibility = View.VISIBLE
                        //binding!!.electheme.visibility = View.GONE
                    }
                    /*4 -> {
                        binding!!.livetheme.visibility = View.GONE
                        binding!!.grocerytheme.visibility = View.GONE
                        binding!!.fashiontheme.visibility = View.GONE
                        binding!!.hometheme.visibility = View.GONE
                        binding!!.electheme.visibility = View.VISIBLE
                    }*/
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding?.livetheme?.setOnClickListener { onLiveThemeClicked() }
        setOnClickListeners(binding?.grocerytheme, binding?.fashiontheme, binding?.hometheme,binding?.electheme)
    }

    private fun onLiveThemeClicked() {
        val homepage = Intent(this@ThemeSelectionActivity, HomePage::class.java)
        startActivity(homepage)
        Constant.activityTransition(this)
        MagePrefs.saveTheme("Live Theme")
    }

    private fun setOnClickListeners(vararg buttons: CardView?) {
        buttons.forEach { button ->
            button?.setOnClickListener {
                when (button.id) {
                    R.id.grocerytheme -> loadJsonFile(R.raw.grocery, "Grocery Theme")
                    R.id.hometheme -> loadJsonFile(R.raw.homedecor, "Home Theme")
                    R.id.fashiontheme -> loadJsonFile(R.raw.fashion, "Fashion Theme")
                    //R.id.electheme -> loadJsonFile(R.raw.electronics, "Electronics Theme")
                }
            }
        }
    }

    private fun loadJsonFile(resourceId: Int, themeName: String) {
        try {
            val inputStream = resources.openRawResource(resourceId)
            val json = inputStream.bufferedReader().use { it.readText() }
            val demopage = Intent(this@ThemeSelectionActivity, DemoActivity::class.java)
            MagePrefs.saveDemoJson(json)
            demopage.putExtra("data", json)
            startActivity(demopage)
            Constant.activityTransition(this)
            MagePrefs.saveTheme(themeName)
            finishAffinity()
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_searchandcarts, menu)
        return true
    }
}
