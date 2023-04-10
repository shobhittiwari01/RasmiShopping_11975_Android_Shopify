package com.rasmishopping.app.basesection.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.databinding.MBannerlayoutBinding
import com.rasmishopping.app.databinding.MDynamicbannerlayoutsBinding
import com.rasmishopping.app.homesection.models.Home
import com.rasmishopping.app.sharedprefsection.MagePrefs
import org.json.JSONArray

class CustomHomePageBanners(fm: FragmentManager, var context: Context, private var items: JSONArray, private var type:String) : PagerAdapter() {
    private var binding: MBannerlayoutBinding? = null
    private var dynamicbinding: MDynamicbannerlayoutsBinding? = null
//    var imageIds = intArrayOf(R.drawable.grocerybannersliderlaceholder, R.drawable.grocerybannerslidersec)
//    var fimageIds = intArrayOf(R.drawable.fbannersliderfirst, R.drawable.fbannersliderfirst)
//    var himageIds = intArrayOf(R.drawable.hbannersliderfirst, R.drawable.hbannerslidersec)
    override fun isViewFromObject(view: View, `object`: Any): Boolean {

            return view == `object`

    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    override fun getCount(): Int {
        return items.length()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        when(type){

            "topbarbanner"->{

                binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.m_bannerlayout, null, false)

            }
            "bannerslider"->{

                dynamicbinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.m_dynamicbannerlayouts, null, false)

            }
        }
        val model = CommanModel()
        Log.i("MageNative-Banner", "Banner" + items.getJSONObject(position).getString("image_url"))
        when(MagePrefs.getTheme()) {
            "Grocery Theme" -> {
                model.imageurl=items.getJSONObject(position)?.getString("image_url")
//                Glide.with(context).load(items.getJSONObject(position)?.getString("image_url")).placeholder(imageIds[position]).into(dynamicbinding!!.bannerimage)
            }
            "Fashion Theme" -> {
                model.imageurl=items.getJSONObject(position)?.getString("image_url")
//                Glide.with(context).load(fimageIds[position]).placeholder(fimageIds[position]).into(dynamicbinding!!.bannerimage)
            }
            "Home Theme" -> {
                model.imageurl=items.getJSONObject(position)?.getString("image_url")
//                Glide.with(context).load(himageIds[position]).placeholder(himageIds[position]).into(dynamicbinding!!.bannerimage)
            }

        }
        binding?.common=model
        dynamicbinding?.common=model
        //Glide.with(context).load(items.getJSONObject(position)?.getString("image_url")).placeholder(context.resources.getDrawable(R.drawable.grocerybannersliderplaceholder)).into(dynamicbinding!!.bannerimage)
        //model.imageurl = items.getJSONObject(position)?.getString("image_url")!!

        val home = Home()
        Log.i("MageNative-Banner", "id" + items.getJSONObject(position).getString("link_value"))
        home.id = items.getJSONObject(position).getString("link_value")
        Log.i("MageNative-Banner", "link_to" + items.getJSONObject(position).getString("link_type"))
        if(items.getJSONObject(position).getString("link_value").contains("youtube")) {
            dynamicbinding!!.playButton.visibility = View.VISIBLE
        } else {
            dynamicbinding!!.playButton.visibility = View.GONE
        }
        home.link_to = items.getJSONObject(position).getString("link_type")
        var view: View?=null
        when(type){
            "topbarbanner"->{
                binding!!.common = model
                binding!!.home = home
                container.addView(binding!!.root)
                view= binding!!.root
            }
            "bannerslider"->{
                dynamicbinding!!.common = model
                dynamicbinding!!.home = home
                container.addView(dynamicbinding!!.root)
                view= dynamicbinding!!.root
            }
        }
        return view!!
    }
}