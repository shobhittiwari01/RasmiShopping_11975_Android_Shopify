package com.rasmishopping.app.productsection.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.google.gson.GsonBuilder
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.cartsection.activities.SubscribeCartList
import com.rasmishopping.app.databinding.ActivityZoomBinding
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.productsection.adapters.ZoomImageAdapter
import com.rasmishopping.app.productsection.models.MediaModel
import com.rasmishopping.app.utils.Constant
import kotlinx.android.synthetic.main.m_newbaseactivity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ZoomActivity : NewBaseActivity() {
    private var binding: ActivityZoomBinding? = null
    private var images_list: MutableList<MediaModel>? = null
    private  val TAG = "ZoomActivity"

    @Inject
    lateinit var zoomImageAdapter: ZoomImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_zoom, group, true)
        (application as MyApplication).mageNativeAppComponent!!.doZoomActivityInjection(this)
        showBackButton()
        nav_view.visibility = View.GONE
        showTittle(" ")
        if (intent.hasExtra("imageslist") && intent.hasExtra("images")) {
            var mediaList = intent.getStringExtra("imageslist")
            var mediaImage = intent.getStringExtra("images")
            images_list = GsonBuilder().create().fromJson(mediaList, Array<MediaModel>::class.java)
                .toMutableList()
            zoomImageAdapter.setData(images_list, images_list?.get(0)?.previewUrl!!)
            binding?.imagesSlider?.adapter = zoomImageAdapter
            binding?.imagesSlider?.currentItem = if(getPosition(images_list!!, mediaImage!!)==-1){ 0 } else { getPosition(images_list!!, mediaImage!!) }


        }
        supportActionBar!!.hide()


    }


    fun getPosition(list : MutableList<MediaModel> , url: String): Int{
        for (i in 0 until list.size){
            if (url.equals(list.get(i).previewUrl,true)){
                return i
            }
        }
        return  -1
    }

}