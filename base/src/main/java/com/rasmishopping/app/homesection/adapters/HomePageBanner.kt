package com.rasmishopping.app.homesection.adapters
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.databinding.MBannerlayoutBinding
import com.rasmishopping.app.databinding.MDynamicbannerlayoutBinding
import com.rasmishopping.app.homesection.models.Home
import com.rasmishopping.app.homesection.models.MageBanner
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import org.json.JSONArray
import org.json.JSONObject

class HomePageBanner(fm: FragmentManager, var context: Context, var items: ArrayList<MageBanner>, private var type: String, private var cornerradius: Float, private var marginbanner:Int,private var backcolor:String, private var ratio:String) :
    PagerAdapter() {
    private var binding: MBannerlayoutBinding? = null
    private var dynamicbinding: MDynamicbannerlayoutBinding? = null
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`

    }
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }
    override fun getCount(): Int {
        return items.size
    }
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        when(type){
            "topbarbanner"->{
                binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.m_bannerlayout, null, false)
            }
            "bannerslider"->{
                dynamicbinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.m_dynamicbannerlayout, null, false)
            }
        }
        (dynamicbinding!!.bannercard.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = ratio
        if (HomePageViewModel.isLightModeOn() && backcolor.isNotEmpty()){
            dynamicbinding!!.root.setBackgroundColor(Color.parseColor(backcolor))
        }
        val model = CommanModel()
        Log.i("MageNative-Banner", "Banner" + items.get(position).image_url)
        model.imageurl = items.get(position).image_url
        dynamicbinding!!.bannercard.radius = cornerradius
        val home = Home()
        Log.i("MageNative-Banner", "id" + items.get(position).link_value)
        home.id = items.get(position).link_value
        Log.i("MageNative-Banner", "link_to" + items.get(position).link_type)
        if(items.get(position).link_value!!.contains("www.youtube.com")) {
            dynamicbinding!!.playButton.visibility = View.VISIBLE
        } else {
            dynamicbinding!!.playButton.visibility = View.GONE
        }
        home.link_to = items.get(position).link_type
        val params=(dynamicbinding!!.bannercard.layoutParams as ConstraintLayout.LayoutParams)
        val value =HomePageViewModel.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginbanner.toFloat(), context!!.resources.displayMetrics)
        params.marginStart= (value ).toInt()
        params.marginEnd= (value).toInt()
        var view:View?=null
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
