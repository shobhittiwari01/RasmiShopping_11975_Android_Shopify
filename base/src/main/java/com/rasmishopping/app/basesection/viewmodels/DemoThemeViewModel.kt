package com.rasmishopping.app.basesection.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.shopify.graphql.support.ID
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.DemoActivity
import com.rasmishopping.app.basesection.adapters.*
import com.rasmishopping.app.basesection.models.CommanModel
import com.rasmishopping.app.databinding.*
import com.rasmishopping.app.dbconnection.entities.HomePageProduct
import com.rasmishopping.app.homesection.adapters.*
import com.rasmishopping.app.homesection.models.CategoryCircle
import com.rasmishopping.app.homesection.models.MageBanner
import com.rasmishopping.app.homesection.models.ProductSlider
import com.rasmishopping.app.homesection.models.StandAloneBanner
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.network_transaction.CustomResponse
import com.rasmishopping.app.network_transaction.doGraphQLQueryGraph
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.shopifyqueries.Query
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.GraphQLResponse
import com.rasmishopping.app.utils.Status
import com.rasmishopping.app.utils.Urls
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.m_homepage_modified.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.timerTask

class DemoThemeViewModel(var repository: Repository) : ViewModel() {
    val homepagedata = MutableLiveData<LinkedHashMap<String, View>>()
    var linkedHashMap = LinkedHashMap<String, View>()

    companion object {
        var count_color: String = "#000000"
        var count_textcolor: String = "#FFFFFF"
        var icon_color: String = "#000000"
        var panel_bg_color: String = "#FFFFFF"
    }

    @Inject
    lateinit var homeadapter: ProductSliderListAdapter

    @Inject
    lateinit var productListAdapter: ProductListSliderAdapter

    @Inject
    lateinit var category_adapter: CategoryCircleAdpater

    @Inject
    lateinit var circleadapter: CustomCircleCategoryAdapter

    @Inject
    lateinit var gridAdapter: ProductSliderGridAdapter

    @Inject
    lateinit var adapter: CustomCollectionGridAdapter

    @Inject
    lateinit var slideradapter: CustomCollectionSliderAdapter
    lateinit var context: DemoActivity
    fun getHomePageData(): MutableLiveData<LinkedHashMap<String, View>> {
        return homepagedata
    }

    var searchicon: Boolean = true
    fun setSearchIcon(searchicon: Boolean) {
        this.searchicon = searchicon
    }

    fun getSearchAsIcon(): Boolean {
        return searchicon
    }

    var wishicon: Boolean = false
    fun setWishIcon(wishicon: Boolean) {
        this.wishicon = wishicon
    }

    fun getWishlistIcon(): Boolean {
        return wishicon
    }

    fun GetResponse(context: DemoActivity, homepage: LinearLayoutCompat) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                (context.application as MyApplication).mageNativeAppComponent!!.doDemoModelInjection(
                    this@DemoThemeViewModel
                )
                try {
                    dowloadJson(MagePrefs.getDemoJson(), context)
                } catch (e: Exception) {
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dowloadJson(downloadlink: String?, context: DemoActivity) {
        viewModelScope.launch {
            try {
                parseResponse(downloadlink, context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseResponse(apiResponse: String?, context: DemoActivity) {
        this.context = context
        try {
            var obj = JSONObject(apiResponse)
            var names: JSONArray = obj.getJSONObject("sort_order").names()!!
            Log.i("SORTNAME", "" + names)
            for (data in 0..names.length() - 1) {
                var part = names[data].toString().split("_")
                var key: String = names[data].toString().replace(part.get(part.size - 1), "")
                Log.d("COMPKEYS", "parseResponse: " + key)
                when (key) {
                    "top-bar_", "top-bar-without-slider_" -> {
                        topbar(
                            obj.getJSONObject(names[data].toString()),
                            names[data].toString()
                        )
                    }
                    "banner-slider_" -> {
                        createBannerSlider(
                            obj.getJSONObject(names[data].toString()),
                            names[data].toString()
                        )
                    }
                    "category-circle_" -> {
                        createCircleSlider(
                            obj.getJSONObject(names[data].toString()),
                            names[data].toString()
                        )
                    }
                    "standalone-banner_" -> {
                        createStandAloneBanner(
                            obj.getJSONObject(names[data].toString()),
                            names[data].toString()
                        )
                    }
                    "product-list-slider_" -> {
                        createProductSlider(
                            //dummyJSON,
                            obj.getJSONObject(names[data].toString()),
                            true,
                            names[data].toString()
                        )
                    }
                    "category-square_" -> {
                        createCategorySquare(
                            obj.getJSONObject(names[data].toString()),
                            names[data].toString()
                        )
                    }
                    "fixed-customisable-layout_" -> {
                        createFixedCustomisableLayout(
                            obj.getJSONObject(names[data].toString()),
                            true,
                            names[data].toString()
                        )
                    }
                    "three-product-hv-layout_" -> {
                        createHvLayout(
                            obj.getJSONObject(names[data].toString()),
                            names[data].toString()
                        )
                    }
                    "collection-grid-layout_" -> {
                        createCollectionGrid(
                            obj.getJSONObject(names[data].toString()),
                            names[data].toString()
                        )
                    }
                    "collection-list-slider_" -> {
                        createCollectionListSlider(
                            obj.getJSONObject(names[data].toString()),
                            names[data].toString()
                        )
                    }
                }
            }
            homepagedata.value = linkedHashMap
            context.main_container.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createCollectionListSlider(jsonObject: JSONObject, key: String) {
        try {
            val binding: MCollectionsliderBinding = DataBindingUtil.inflate(
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.m_collectionslider,
                null,
                false
            )
            val productSlider = ProductSlider()
            if (jsonObject.getString("header").equals("1")) {
                binding.headerSection.visibility = View.VISIBLE
                productSlider.headertext = jsonObject.getString("header_title_text")
                //Constant.translateField(productSlider.headertext!!, binding.headertext)
                if(HomePageViewModel.isLightModeOn()){
                    var header_background_color = JSONObject(jsonObject.getString("header_background_color")).getString("color")
                    binding.headerSection.setBackgroundColor(Color.parseColor(header_background_color))
                    var header_title_color = JSONObject(jsonObject.getString("header_title_color"))
                    binding.headertext.setTextColor(Color.parseColor(header_title_color.getString("color")))
                }
                when (jsonObject.getString("header_title_font_weight")) {
                    "bold" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                    }
                    "light" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "medium" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                    }
                }
                if (jsonObject.getString("header_title_font_style").equals("italic")) {
                    binding.headertext.setTypeface(binding.headertext.typeface, Typeface.ITALIC)
                }
                if (jsonObject.getString("header_subtitle").equals("1")) {
                    binding.subheadertext.visibility = View.VISIBLE
                    productSlider.subheadertext = jsonObject.getString("header_subtitle_text")
                    //Constant.translateField(productSlider.subheadertext!!, binding.subheadertext)
                    if(HomePageViewModel.isLightModeOn()){
                        var header_subtitle_color = JSONObject(jsonObject.getString("header_subtitle_color"))
                        binding.subheadertext.setTextColor(Color.parseColor(header_subtitle_color.getString("color")))
                    }
                    when (jsonObject.getString("header_subtitle_font_weight")) {
                        "bold" -> {
                            binding.subheadertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        }
                        "light" -> {
                            binding.subheadertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        }
                        "medium" -> {
                            binding.subheadertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        }
                    }
                    if (jsonObject.getString("header_subtitle_font_style").equals("italic")) {
                        binding.subheadertext.setTypeface(
                            binding.subheadertext.typeface,
                            Typeface.ITALIC
                        )
                    }
                }
            }
            if(HomePageViewModel.isLightModeOn()){
                var panel_background_color = JSONObject(jsonObject.getString("panel_background_color")).getString("color")
                binding.panelbackgroundcolor.setBackgroundColor(Color.parseColor(panel_background_color))
                binding.root.setBackgroundColor(Color.parseColor(panel_background_color))
            }
            context.setLayout(binding.productdataCollectionslider, "horizontal")
            slideradapter = CustomCollectionSliderAdapter()
            slideradapter.setData(jsonObject.getJSONArray("items"), context, jsonObject)
            binding.productdataCollectionslider.adapter = slideradapter
            slideradapter.notifyDataSetChanged()
            binding.productslider = productSlider
            linkedHashMap.put(key, binding.root)
            // homepagedata.setValue( hashMapOf("collection-list-slider_" to binding.root))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun createCollectionGrid(jsonObject: JSONObject, key: String) {
        Log.i("SaifDevCustomgrid", "" + jsonObject)
        val binding: MCollectionlgridBinding = DataBindingUtil.inflate(
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            R.layout.m_collectionlgrid,
            null,
            false
        )
        context.setLayout(binding.categorylist, "customisablegrid")
        try {
            repository.getJSonArray(JsonParser().parse(jsonObject.getString("items")).asJsonArray)
                .subscribeOn(Schedulers.io())
                .filter { x -> x.asJsonObject.get("link_type").asString.isNotEmpty() }
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<List<JsonElement>> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onSuccess(list: List<JsonElement>) {
                        adapter = CustomCollectionGridAdapter()
                        adapter.setData(list, context, jsonObject)
                        binding.categorylist.adapter = adapter
                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if(HomePageViewModel.isLightModeOn()){
            var background =JSONObject(jsonObject.getString("panel_background_color"))
            binding.categorylist.setBackgroundColor(Color.parseColor(background.getString("color")))
            binding.root.setBackgroundColor(Color.parseColor(background.getString("color")))
            
        }
        linkedHashMap.put(key, binding.root)
    }

    private fun createHvLayout(jsonObject: JSONObject, key: String) {
        try {
            var binding: MProductHvLayoutsBinding = DataBindingUtil.inflate(
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.m_product_hv_layouts,
                null,
                false
            )
            var productSlider = ProductSlider()
            if (jsonObject.getString("header").equals("1")) {
                binding.headerSection.visibility = View.VISIBLE
                productSlider.headertext = jsonObject.getString("header_title_text")
                //Constant.translateField(productSlider.headertext!!, binding.headertext)
                if(HomePageViewModel.isLightModeOn()){
                    var header_background_color = JSONObject(jsonObject.getString("header_background_color"))
                    binding.headerSection.setBackgroundColor(Color.parseColor(header_background_color.getString("color")))
                    var header_title_color = JSONObject(jsonObject.getString("header_title_color"))
                    binding.headertext.setTextColor(Color.parseColor(header_title_color.getString("color")))
                }
                when (jsonObject.getString("header_title_font_weight")) {
                    "bold" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                    }
                    "light" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "medium" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                    }
                }
                if (jsonObject.getString("header_title_font_style").equals("italic")) {
                    binding.headertext.setTypeface(binding.headertext.typeface, Typeface.ITALIC)
                }
                if (jsonObject.getString("header_subtitle").equals("1")) {
                    binding.subheadertext.visibility = View.VISIBLE
                    productSlider.subheadertext = jsonObject.getString("header_subtitle_text")
                    //Constant.translateField(productSlider.subheadertext!!, binding.subheadertext)
                    if(HomePageViewModel.isLightModeOn()){
                        var header_subtitle_color = JSONObject(jsonObject.getString("header_subtitle_color"))
                        binding.subheadertext.setTextColor(Color.parseColor(header_subtitle_color.getString("color")))
                    }

                    when (jsonObject.getString("header_subtitle_font_weight")) {
                        "bold" -> {
                            binding.subheadertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        }
                        "light" -> {
                            binding.subheadertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        }
                        "medium" -> {
                            binding.subheadertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        }
                    }
                    if (jsonObject.getString("header_subtitle_font_style").equals("italic")) {
                        binding.subheadertext.setTypeface(
                            binding.subheadertext.typeface,
                            Typeface.ITALIC
                        )
                    }
                }
                if (jsonObject.getString("header_deal").equals("1")) {
                    binding.dealsection.deallayout.visibility = View.VISIBLE
                    productSlider.timertextmessage = jsonObject.getString("item_deal_message")
                    if(HomePageViewModel.isLightModeOn()){
                        var header_deal_color = JSONObject(jsonObject.getString("header_deal_color"))
                        binding.dealsection.timerMessage.setTextColor(Color.parseColor(header_deal_color.getString("color")))
                    }
                    var DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"
                    var sdf = SimpleDateFormat(DATE_FORMAT)
                    // sdf.timeZone = TimeZone.getTimeZone("UTC")
                    var item_deal_start_date = sdf.format(Date())
                    Log.i("MageNative", "item_deal_start_date " + item_deal_start_date)
                    var item_deal_end_date = jsonObject.getString("item_deal_end_date")
                    Log.i("MageNative", "item_deal_end_date " + item_deal_end_date)
                    var startdate: Date?
                    var enddate: Date?
                    try {
                        startdate = sdf.parse(item_deal_start_date)
                        enddate = sdf.parse(item_deal_end_date)
                        var oldLong = startdate.time
                        var NewLong = enddate.time
                        var diff = NewLong - oldLong
                        Log.i("MageNative", "Long" + diff)
                        if (diff > 0) {
                            var counter = HomePageViewModel.MyCount(diff, 1000, productSlider, ":")
                            counter.start()
                        } else {
                            productSlider.timericon = View.GONE
                            binding.dealsection.timer.visibility = View.GONE
                            binding.dealsection.deallayout.visibility = View.GONE
                        }
                    } catch (ex: ParseException) {
                        ex.printStackTrace()
                    }
                }
            }
            if(HomePageViewModel.isLightModeOn()){
                var panel_background_color = JSONObject(jsonObject.getString("panel_background_color")).getString("color")
                binding.panelbackgroundcolor.setBackgroundColor(Color.parseColor(panel_background_color))
                binding.root.setBackgroundColor(Color.parseColor(panel_background_color))
                
            }
            when (jsonObject.getString("item_text_alignment")) {
                "left" -> {
                    binding.hvnameone.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    binding.hvnametwo.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    binding.hvnamethree.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                }
                "right" -> {
                    binding.hvnameone.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    binding.hvnametwo.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    binding.hvnamethree.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                }
            }
            when (jsonObject.getString("item_shape")) {
                "square" -> {
                    //first product
                    binding.cardone.radius = 0f
                    binding.cardone.useCompatPadding = false
                    //second product
                    binding.cardtwo.radius = 0f
                    binding.cardtwo.useCompatPadding = false
                    //third product
                    binding.cardthree.radius = 0f
                    binding.cardthree.useCompatPadding = false
                }
            }
            when (jsonObject.getString("item_border")) {
                "1" -> {
                    var background = JSONObject(jsonObject.getString("item_border_color"))
                    binding.cardone.setCardBackgroundColor(Color.parseColor(background.getString("color")))
                    binding.cardtwo.setCardBackgroundColor(Color.parseColor(background.getString("color")))
                    binding.cardthree.setCardBackgroundColor(Color.parseColor(background.getString("color")))
                    var params = binding.hvimagOne.layoutParams as FrameLayout.LayoutParams
                    params.setMargins(2, 2, 2, 2)
                    binding.hvimagOne.layoutParams = params
                    binding.hvimagtwo.layoutParams = params
                    binding.hvimagthree.layoutParams = params

                }
            }
            var face: Typeface? = null
            when (jsonObject.getString("item_title_font_weight")) {
                "bold" -> {
                    face = Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                }
                "light" -> {
                    face = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                }
                "medium" -> {
                    face = Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                }
            }
            binding.hvnameone.typeface = face
            binding.hvnametwo.typeface = face
            binding.hvnamethree.typeface = face
            when (jsonObject.getString("item_title_font_style")) {
                "italic" -> {
                    binding.hvnameone.setTypeface(binding.hvnameone.typeface, Typeface.ITALIC)
                    binding.hvnametwo.setTypeface(binding.hvnametwo.typeface, Typeface.ITALIC)
                    binding.hvnamethree.setTypeface(binding.hvnamethree.typeface, Typeface.ITALIC)
                }
            }
            val model = CommanModel()

            /**************first hv***************************/
            //productSlider.hvimageone = jsonObject.getJSONArray("items").getJSONObject(0).getString("image_url")
            when(MagePrefs.getTheme()){
                "Grocery Theme" -> {
                    model.imageurl = jsonObject.getJSONArray("items").getJSONObject(0).getString("image_url")
//                    Glide.with(context).load()
////                        .placeholder(context.resources.getDrawable(R.drawable.groceryhvimagefirst))
//                        .into(binding.hvimagOne)
                }
                "Fashion Theme" -> {
                    model.imageurl = jsonObject.getJSONArray("items").getJSONObject(0).getString("image_url")
//                    Glide.with(context).load(jsonObject.getJSONArray("items").getJSONObject(0).getString("image_url"))
////                        .placeholder(context.resources.getDrawable(R.drawable.fhvfirst))
//                        .into(binding.hvimagOne)
                }
                "Home Theme" -> {
                    model.imageurl = jsonObject.getJSONArray("items").getJSONObject(0).getString("image_url")
//                    Glide.with(context).load(jsonObject.getJSONArray("items").getJSONObject(0).getString("image_url"))
////                        .placeholder(context.resources.getDrawable(R.drawable.hhvfirst))
//                        .into(binding.hvimagOne)
                }
            }
            // productSlider.hvimageone="https://images.unsplash.com/photo-1580748141549-71748dbe0bdc?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=334&q=80"
            productSlider.hvnameone =
                jsonObject.getJSONArray("items").getJSONObject(0).getString("title")
            productSlider.hvtypeone =
                jsonObject.getJSONArray("items").getJSONObject(0).getString("link_type")
            productSlider.hvvalueone =
                jsonObject.getJSONArray("items").getJSONObject(0).getString("link_value")
            Constant.translateField(productSlider.hvnameone!!, binding.hvnameone)
            /**************second hv***************************/
            when(MagePrefs.getTheme()){
                "Grocery Theme" -> {
                    model.imageurl = jsonObject.getJSONArray("items").getJSONObject(1).getString("image_url")
//                    Glide.with(context).load(jsonObject.getJSONArray("items").getJSONObject(1).getString("image_url"))
////                        .placeholder(context.resources.getDrawable(R.drawable.groceryhvimagesec))
//                        .into(binding.hvimagtwo)
                }
                "Fashion Theme" -> {
                    model.imageurl = jsonObject.getJSONArray("items").getJSONObject(1).getString("image_url")
//                    Glide.with(context).load(jsonObject.getJSONArray("items").getJSONObject(1).getString("image_url"))
////                        .placeholder(context.resources.getDrawable(R.drawable.fhvsec))
//                        .into(binding.hvimagtwo)
                }
                "Home Theme" -> {
                    model.imageurl = jsonObject.getJSONArray("items").getJSONObject(1).getString("image_url")
//                    Glide.with(context).load(jsonObject.getJSONArray("items").getJSONObject(1).getString("image_url"))
////                        .placeholder(context.resources.getDrawable(R.drawable.hhvsec))
//                        .into(binding.hvimagtwo)
                }
            }
            binding.commondata=model
            //productSlider.hvimagetwo = jsonObject.getJSONArray("items").getJSONObject(1).getString("image_url")
            //productSlider.hvimagetwo="https://images.unsplash.com/photo-1570589107939-54ebe3183842?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=719&q=80"
            productSlider.hvnametwo =
                jsonObject.getJSONArray("items").getJSONObject(1).getString("title")
            productSlider.hvtypetwo =
                jsonObject.getJSONArray("items").getJSONObject(1).getString("link_type")
            productSlider.hvvaluetwo =
                jsonObject.getJSONArray("items").getJSONObject(1).getString("link_value")
            Constant.translateField(productSlider.hvnametwo!!, binding.hvnametwo)
            /**************third hv***************************/
            when(MagePrefs.getTheme()){
                "Grocery Theme" -> {
                    model.imageurl = jsonObject.getJSONArray("items").getJSONObject(2).getString("image_url")
//                    Glide.with(context).load(jsonObject.getJSONArray("items").getJSONObject(2).getString("image_url"))
//                        .placeholder(context.resources.getDrawable(R.drawable.groceryhvimagethird))
//                        .into(binding.hvimagthree)
                }
                "Fashion Theme" -> {
                    model.imageurl = jsonObject.getJSONArray("items").getJSONObject(2).getString("image_url")
//                    Glide.with(context).load(jsonObject.getJSONArray("items").getJSONObject(2).getString("image_url"))
//                        .placeholder(context.resources.getDrawable(R.drawable.fhvthird))
//                        .into(binding.hvimagthree)
                }
                "Home Theme" -> {
                    model.imageurl = jsonObject.getJSONArray("items").getJSONObject(2).getString("image_url")
//                    Glide.with(context).load(jsonObject.getJSONArray("items").getJSONObject(2).getString("image_url"))
//                        .placeholder(context.resources.getDrawable(R.drawable.hhvthird))
//                        .into(binding.hvimagthree)
                }
            }
            binding.commondata=model
            //productSlider.hvimagethree = jsonObject.getJSONArray("items").getJSONObject(2).getString("image_url")
            //productSlider.hvimagethree="https://images.unsplash.com/photo-1570589107939-54ebe3183842?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=719&q=80"
            productSlider.hvnamethree =
                jsonObject.getJSONArray("items").getJSONObject(2).getString("title")
            productSlider.hvtypethree =
                jsonObject.getJSONArray("items").getJSONObject(2).getString("link_type")
            productSlider.hvvaluethree =
                jsonObject.getJSONArray("items").getJSONObject(2).getString("link_value")
            Constant.translateField(productSlider.hvnamethree!!, binding.hvnamethree)
            binding.productslider = productSlider
            linkedHashMap.put(key, binding.root)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createFixedCustomisableLayout(jsonObject: JSONObject, flag: Boolean, key: String) {
        try {
            var binding: MFixedcustomisableBinding = DataBindingUtil.inflate(
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.m_fixedcustomisable,
                null,
                false
            )
            var productSlider = ProductSlider()
            binding.root.visibility = View.GONE
            updateDataInRecylerView(
                binding.productdataFixedcustomisable,
                jsonObject.getJSONArray("items").getJSONObject(0).getJSONArray("product_value"),
                jsonObject,
                flag,
                binding.root
            )
            if (jsonObject.getString("header").equals("1")) {
                binding.headerSection.visibility = View.VISIBLE
                productSlider.headertext = jsonObject.getString("header_title_text")
                //Constant.translateField(productSlider.headertext!!, binding.headertext)
                if(HomePageViewModel.isLightModeOn()){
                    var header_background_color = JSONObject(jsonObject.getString("header_background_color")).getString("color")
                    binding.headerSection.setBackgroundColor(Color.parseColor(header_background_color))
                    var header_title_color = JSONObject(jsonObject.getString("header_title_color")).getString("color")
                    binding.headertext.setTextColor(Color.parseColor(header_title_color))
                }
                when (jsonObject.getString("header_title_font_weight")) {
                    "bold" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                    }
                    "light" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "medium" -> {
                        binding.headertext.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                    }
                }
                if (jsonObject.getString("item_header_font_style").equals("italic")) {
                    binding.headertext.setTypeface(binding.headertext.typeface, Typeface.ITALIC)
                }
                if (jsonObject.getString("header_subtitle").equals("1")) {
                    binding.subheadertext.visibility = View.VISIBLE
                    productSlider.subheadertext = jsonObject.getString("header_subtitle_text")
                    /*Constant.translateField(
                        productSlider.subheadertext!!,
                        binding.subheadertext
                    )*/
                    if(HomePageViewModel.isLightModeOn()){
                        var header_subtitle_color = JSONObject(jsonObject.getString("header_subtitle_color"))
                        binding.subheadertext.setTextColor(Color.parseColor(header_subtitle_color.getString("color")))
                    }
                    when (jsonObject.getString("header_subtitle_font_weight")) {
                        "bold" -> {
                            binding.subheadertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        }
                        "light" -> {
                            binding.subheadertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        }
                        "medium" -> {
                            binding.subheadertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        }
                    }
                    if (jsonObject.getString("header_subtitle_title_font_style")
                            .equals("italic")
                    ) {
                        binding.subheadertext.setTypeface(
                            binding.subheadertext.typeface,
                            Typeface.ITALIC
                        )
                    }
                }
                if (jsonObject.getString("header_action").equals("1")) {
                    binding.actiontext.visibility = View.VISIBLE
                    productSlider.action_id = jsonObject.getJSONArray("items").getJSONObject(0)
                        .getString("link_value")
                    productSlider.actiontext = jsonObject.getString("header_action_text")
                    Constant.translateField(productSlider.actiontext!!, binding.actiontext)
                    if(HomePageViewModel.isLightModeOn()){
                        var header_action_color = JSONObject(jsonObject.getString("header_action_color"))
                        var header_action_background_color = JSONObject(jsonObject.getString("header_action_background_color"))
                        binding.actiontext.setTextColor(Color.parseColor(header_action_color.getString("color")))
                        var gradientDrawable = binding.actiontext.background as GradientDrawable
                        gradientDrawable.setStroke(2, Color.parseColor(header_action_background_color.getString("color")))
                        gradientDrawable.setColor(Color.parseColor(header_action_background_color.getString("color")))
                    }
                    //binding.actiontext.setBackgroundColor(Color.parseColor(header_action_background_color.getString("color")))
                    when (jsonObject.getString("header_action_font_weight")) {
                        "bold" -> {
                            binding.actiontext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        }
                        "light" -> {
                            binding.actiontext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        }
                        "medium" -> {
                            binding.actiontext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        }
                    }
                    if (jsonObject.getString("header_action_title_font_style")
                            .equals("italic")
                    ) {
                        binding.actiontext.setTypeface(
                            binding.actiontext.typeface,
                            Typeface.ITALIC
                        )
                    }
                }
                if (jsonObject.getString("header_deal").equals("1")) {
                    binding.dealsection.deallayout.visibility = View.VISIBLE
                    productSlider.timertextmessage = jsonObject.getString("item_deal_message")
                    if(HomePageViewModel.isLightModeOn()){
                        var header_deal_color = JSONObject(jsonObject.getString("header_deal_color"))
                        binding.dealsection.timerMessage.setTextColor(Color.parseColor(header_deal_color.getString("color")))
                    }
                    var DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"
                    var sdf = SimpleDateFormat(DATE_FORMAT)
                    //  sdf.timeZone = TimeZone.getTimeZone("UTC")
                    var item_deal_start_date = sdf.format(Date())
                    Log.i("MageNative", "item_deal_start_date " + item_deal_start_date)
                    var item_deal_end_date = jsonObject.getString("item_deal_end_date")
                    Log.i("MageNative", "item_deal_end_date " + item_deal_end_date)
                    var startdate: Date?
                    var enddate: Date?
                    try {
                        startdate = sdf.parse(item_deal_start_date)
                        enddate = sdf.parse(item_deal_end_date)
                        var oldLong = startdate.time
                        var NewLong = enddate.time
                        var diff = NewLong - oldLong
                        Log.i("MageNative", "Long" + diff)
                        if (diff > 0) {
                            var counter = HomePageViewModel.MyCount(diff, 1000, productSlider, ":")
                            counter.start()
                        } else {
                            productSlider.timericon = View.GONE
                            binding.dealsection.timer.visibility = View.GONE
                            binding.dealsection.deallayout.visibility = View.GONE
                        }
                    } catch (ex: ParseException) {
                        ex.printStackTrace()
                    }
                }
            }
            if(HomePageViewModel.isLightModeOn()){
                var panel_background_color = JSONObject(jsonObject.getString("panel_background_color")).getString("color")
                binding.panelbackgroundcolor.setBackgroundColor(Color.parseColor(panel_background_color))
                binding.root.setBackgroundColor(Color.parseColor(panel_background_color))
                
            }
            binding.productslider = productSlider
            linkedHashMap.put(key, binding.root)
            // homepagedata.setValue(hashMapOf("fixed-customisable-layout_" to binding.root))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun createProductSlider(jsonObject: JSONObject, flag: Boolean, key: String) {
        try {
            val binding: MProductSlidersBinding = DataBindingUtil.inflate(
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.m_product_sliders,
                null,
                false
            )
            var productSlider = ProductSlider()

            //binding.root.visibility = View.GONE

            /*if(jsonObject.has("categoryID")) {
                Log.i("HAVECATIDDD",""+jsonObject.getString("categoryID"))
                updateDataInRecylerViewcatId(binding.productdataSlider,jsonObject.getString("categoryID"))
            } else {
                updateDataInRecylerView(
                    binding.productdataSlider,
                    jsonObject.getJSONArray("item_value"),
                    jsonObject,
                    flag,
                    binding.root
                )
            }*/
            updateDataInRecylerView(
                binding.productdataSlider,
                jsonObject.getJSONArray("item_value"),
                jsonObject,
                flag,
                binding.root
            )
            if (flag) {
                if (jsonObject.getString("header").equals("1")) {
                    binding.headerSection.visibility = View.VISIBLE
                    //productSlider.headertext = jsonObject.getString("header_title_text")
                    binding.headertext.text = jsonObject.getString("header_title_text")
                    //Constant.translateField(productSlider.headertext!!, binding.headertext)
                    if(HomePageViewModel.isLightModeOn()){
                        var header_action_color = JSONObject(jsonObject.getString("header_action_color"))
                        var header_action_background_color = JSONObject(jsonObject.getString("header_action_background_color"))
                        binding.actiontext.setTextColor(Color.parseColor(header_action_color.getString("color")))
                        var gradientDrawable = binding.actiontext.background as GradientDrawable
                        gradientDrawable.setStroke(2, Color.parseColor(header_action_background_color.getString("color")))
                        gradientDrawable.setColor(Color.parseColor(header_action_background_color.getString("color")))
                    }
                    when (jsonObject.getString("item_header_font_weight")) {
                        "bold" -> {
                            binding.headertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        }
                        "light" -> {
                            binding.headertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        }
                        "medium" -> {
                            binding.headertext.typeface =
                                Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        }
                    }
                    if (jsonObject.getString("item_header_font_style").equals("italic")) {
                        binding.headertext.setTypeface(binding.headertext.typeface, Typeface.ITALIC)
                    }
                    if (jsonObject.getString("header_subtitle").equals("1")) {
                        binding.subheadertext.visibility = View.VISIBLE
                        //productSlider.subheadertext = jsonObject.getString("header_subtitle_text")
                        binding.subheadertext.text = jsonObject.getString("header_subtitle_text")
                        /*Constant.translateField(
                            productSlider.subheadertext!!,
                            binding.subheadertext
                        )*/
                        if(HomePageViewModel.isLightModeOn()){
                            var header_subtitle_color = JSONObject(jsonObject.getString("header_subtitle_color"))
                            binding.subheadertext.setTextColor(Color.parseColor(header_subtitle_color.getString("color")))
                        }
                        when (jsonObject.getString("header_subtitle_font_weight")) {
                            "bold" -> {
                                binding.subheadertext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                            }
                            "light" -> {
                                binding.subheadertext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                            }
                            "medium" -> {
                                binding.subheadertext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                            }
                        }
                        if (jsonObject.getString("header_subtitle_title_font_style")
                                .equals("italic")
                        ) {
                            binding.subheadertext.setTypeface(
                                binding.subheadertext.typeface,
                                Typeface.ITALIC
                            )
                        }
                    }
                    if (jsonObject.getString("header_action").equals("1")) {
                        binding.actiontext.visibility = View.VISIBLE
                        /*productSlider.action_id =
                            getcategoryID(jsonObject.getString("item_link_action_value"))*/
                        productSlider.actiontext = jsonObject.getString("header_action_text")
                        //Constant.translateField(productSlider.actiontext!!, binding.actiontext)
                        if(HomePageViewModel.isLightModeOn()){
                            var header_action_color = JSONObject(jsonObject.getString("header_action_color"))
                            var header_action_background_color = JSONObject(jsonObject.getString("header_action_background_color"))
                            binding.actiontext.setTextColor(Color.parseColor(header_action_color.getString("color")))
                            var gradientDrawable = binding.actiontext.background as GradientDrawable
                            gradientDrawable.setStroke(2, Color.parseColor(header_action_background_color.getString("color")))
                            gradientDrawable.setColor(Color.parseColor(header_action_background_color.getString("color")))
                        }
                        //binding.actiontext.setBackgroundColor(Color.parseColor(header_action_background_color.getString("color")))
                        when (jsonObject.getString("header_action_font_weight")) {
                            "bold" -> {
                                binding.actiontext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                            }
                            "light" -> {
                                binding.actiontext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                            }
                            "medium" -> {
                                binding.actiontext.typeface =
                                    Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                            }
                        }
                        if (jsonObject.getString("header_action_font_style").equals("italic")) {
                            binding.actiontext.setTypeface(
                                binding.actiontext.typeface,
                                Typeface.ITALIC
                            )
                        }
                    }
                    if (jsonObject.getString("header_deal").equals("1")) {
                        binding.dealsection.deallayout.visibility = View.VISIBLE
                        productSlider.timertextmessage = jsonObject.getString("item_deal_message")
                        if(HomePageViewModel.isLightModeOn()){
                            var header_deal_color = JSONObject(jsonObject.getString("header_deal_color"))
                            binding.dealsection.timerMessage.setTextColor(Color.parseColor(header_deal_color.getString("color")))
                        }
                        var DATE_FORMAT = "MM/dd/yyyy HH:mm:ss"
                        var sdf = SimpleDateFormat(DATE_FORMAT)
                        // sdf.timeZone = TimeZone.getTimeZone("UTC")
                        var item_deal_start_date = sdf.format(Date())
                        Log.i("MageNative", "item_deal_start_date " + item_deal_start_date)
                        var item_deal_end_date = jsonObject.getString("item_deal_end_date")
                        Log.i("MageNative", "item_deal_end_date " + item_deal_end_date)
                        var startdate: Date?
                        var enddate: Date?
                        try {
                            startdate = sdf.parse(item_deal_start_date)
                            enddate = sdf.parse(item_deal_end_date)
                            var oldLong = startdate.time
                            var NewLong = enddate.time
                            var diff = NewLong - oldLong
                            Log.i("MageNative", "Long" + diff)
                            if (diff > 0) {
                                var counter =
                                    HomePageViewModel.MyCount(diff, 1000, productSlider, ":")
                                counter.start()
                            } else {
                                productSlider.timericon = View.GONE
                                binding.dealsection.timer.visibility = View.GONE
                                binding.dealsection.deallayout.visibility = View.GONE
                            }
                        } catch (ex: ParseException) {
                            ex.printStackTrace()
                        }
                    }
                }
                if(HomePageViewModel.isLightModeOn()){
                    var panel_background_color = JSONObject(jsonObject.getString("panel_background_color")).getString("color")
                    binding.panelbackgroundcolor.setBackgroundColor(Color.parseColor(panel_background_color))
                    binding.root.setBackgroundColor(Color.parseColor(panel_background_color))
                    
                }
                binding.productslider = productSlider
                linkedHashMap.put(key, binding.root)
                //homepagedata.setValue(hashMapOf("product-list-slider_" to binding.root))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun topbar(jsonObject: JSONObject, key: String) {
        try {
            var flag = false
            val binding: MTopbarBinding = DataBindingUtil.inflate(
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.m_topbar,
                null,
                false
            )
            HomePageViewModel.panel_bg_color =
                JSONObject(jsonObject.getString("panel_background_color")).getString("color")
            if (!HomePageViewModel.isLightModeOn()){
                HomePageViewModel.count_color ="#FFFFFF"
                HomePageViewModel.count_textcolor = "#000000"
                HomePageViewModel.icon_color =  "#FFFFFF"
                HomePageViewModel.panel_bg_color = "#000000"
            }
            var searchposition = jsonObject.getString("search_position")
            if (SplashViewModel.featuresModel.in_app_wishlist) {
                when (jsonObject.getString("wishlist")) {
                    "1" -> {
                        setWishIcon(true)
                    }
                    else -> {
                        setWishIcon(false)
                    }
                }
            }
            if (jsonObject.has("logo_image_url")) {
                when(MagePrefs.getTheme()) {
                    "Grocery Theme" -> {
                        context.setLogoImageDrawable(context.resources.getDrawable(R.drawable.grocerylogo))
                    }
                    "Fashion Theme" -> {
                        context.setLogoImageDrawable(context.resources.getDrawable(R.drawable.fashionlogo))
                    }
                    "Home Theme" -> {
                        context.setLogoImageDrawable(context.resources.getDrawable(R.drawable.homelogo))
                    }
                }
                //context.setLogoImage(jsonObject.getString("logo_image_url"))
            }
            context.setPanelBackgroundColor(
                HomePageViewModel.panel_bg_color
            )
            binding.root.setBackgroundColor(
                Color.parseColor(
                    HomePageViewModel.panel_bg_color
                )
            )
            when (searchposition) {
                "middle-width-search" -> {
                    context.toolimage.visibility = View.GONE
                    context.searchsection.visibility = View.VISIBLE
                    context.search.text = jsonObject.getString("search_placeholder")
                    context.search.setOnClickListener {
                        context.moveToSearch(it.context)
                        //Constant.activityTransition()
                    }
                    var draw: GradientDrawable =
                        context.searchsection.background as GradientDrawable
                    draw.setColor(
                        Color.parseColor(
                            HomePageViewModel.panel_bg_color
                        )
                    )
                    context.search.setTextColor(
                        Color.parseColor(
                            JSONObject(jsonObject.getString("search_text_color")).getString(
                                "color"
                            )
                        )
                    )
                    draw.setStroke(
                        2,
                        Color.parseColor(
                            JSONObject(jsonObject.getString("search_border_color")).getString("color")
                        )
                    )
                    setSearchIcon(false)
                }
                "full-width-search" -> {
                    flag = true
                    context.hideShadow()
                    context.toolimage.visibility = View.VISIBLE
                    context.searchsection.visibility = View.GONE
                    binding.fullsearchsection.visibility = View.VISIBLE
                    binding.fullsearch.text = jsonObject.getString("search_placeholder")
                    binding.fullsearchsection.setOnClickListener { context.moveToSearch(context) }
                    //binding.curvecard.setCardBackgroundColor(Color.parseColor(HomePageViewModel.panel_bg_color))
                    var draw: GradientDrawable =
                        binding.fullsearchsection.background as GradientDrawable
                    draw.setColor(
                        Color.parseColor(
                            JSONObject(jsonObject.getString("search_background_color")).getString(
                                "color"
                            )
                        )
                    )
                    binding.fullsearch.setTextColor(
                        Color.parseColor(
                            JSONObject(
                                jsonObject.getString(
                                    "search_text_color"
                                )
                            ).getString("color")
                        )
                    )
                    draw.setStroke(
                        2,
                        Color.parseColor(
                            JSONObject(jsonObject.getString("search_border_color")).getString("color")
                        )
                    )
                    setSearchIcon(false)
                }
                else -> {
                    context.toolimage.visibility = View.VISIBLE
                    context.searchsection.visibility = View.GONE
                    binding.fullsearchsection.visibility = View.GONE
                }
            }
            if (jsonObject.has("item_banner") && jsonObject.getString("item_banner").equals("1")) {
                flag = true
                var common = CommanModel()
                val listType: Type = object : TypeToken<List<MageBanner?>?>() {}.getType()
                var items:ArrayList<MageBanner> =Gson().fromJson(jsonObject.getJSONArray("items").toString(), listType)
                var adp = HomePageBanner(
                        context.supportFragmentManager,
                        context,
                    items,
                        "topbarbanner",
                        0f
                ,2,"","H,700:394")
                //binding.bannerss.adapter = adp
                adp.notifyDataSetChanged()
                /*binding.bannerss.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrollStateChanged(state: Int) {}
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                    }

                    override fun onPageSelected(position: Int) {
                        if (jsonObject.has("items")) {
                            if (jsonObject.get("items") is JSONArray) {
                                if (jsonObject.getJSONArray("items").length() > 0) {
                                    if (jsonObject.getJSONArray("items").getJSONObject(position)
                                            .has("image_url")
                                    ) {
                                        common.imageurl =
                                            jsonObject.getJSONArray("items").getJSONObject(position)
                                                .getString("image_url")
                                    }
                                }
                            }
                        }

                    }
                })*/
                /*binding.bannersection.backgroundTintList = ColorStateList.valueOf(
                    Color.parseColor(
                        HomePageViewModel.panel_bg_color
                    )
                )
                if (jsonObject.getString("shape").equals("square")) {
                    binding.card.radius = 0f
                    binding.card.useCompatPadding = false
                }*/
                /*if (jsonObject.getString("item_dots").equals("1")) {
                    var background = JSONObject(jsonObject.getString("active_dot_color"))
                    var strokebackground = JSONObject(jsonObject.getString("inactive_dot_color"))
                    binding.indicators.visibility = View.VISIBLE
                    binding.indicators.setDotIndicatorColor(Color.parseColor(background.getString("color")))
                    binding.indicators.setStrokeDotsIndicatorColor(
                        Color.parseColor(
                            strokebackground.getString(
                                "color"
                            )
                        )
                    )
                    binding.indicators.setViewPager(binding.bannerss)
                    var i = 0
                    val timer = Timer()
                    timer.scheduleAtFixedRate(timerTask {
                        CoroutineScope(Dispatchers.Main).launch {
                            binding.bannerss.currentItem = i++
                            if (i == jsonObject.getJSONArray("items").length()) {
                                i = 0
                            }
                        }
                    }, 3000, 3000)
                }*/
                common.imageurl =
                    jsonObject.getJSONArray("items").getJSONObject(0).getString("image_url")
                binding.commondata = common
                //binding.bannersection.visibility = View.VISIBLE
            }
            context.invalidateOptionsMenu()
            if (flag) {
                linkedHashMap.put(key, binding.root)
                //homepagedata.setValue(hashMapOf("top-bar_" to binding.root))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun createStandAloneBanner(jsonObject: JSONObject, key: String) {
        var binding: MStandlonebannerssBinding = DataBindingUtil.inflate(
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            R.layout.m_standlonebannerss,
            null,
            false
        )
        var stand = StandAloneBanner()
        val model = CommanModel()

        //  jsonObject.put("item_image_size","half")
        //  jsonObject.put("item_image_size","2x")
        //  jsonObject.put("item_image_size","3x")
        if (jsonObject.has("item_image_size")) {
            when (jsonObject.getString("item_image_size")) {
                "half" -> {
                    (binding.imageLayout.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                        "H,700:60"
                    var params = binding.imageLayout.layoutParams as ConstraintLayout.LayoutParams
                    params.topMargin = 0
                    params.leftMargin = 0
                    params.rightMargin = 0
                    params.bottomMargin = 0
                    binding.imageLayout.radius = 0f
                    binding.imageLayout.cardElevation = 0f
                    binding.imageLayout.layoutParams = params
                }
                "2x" -> {
                    (binding.imageLayout.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                        "H,375:281"
                }
                "3x" -> {
                    (binding.imageLayout.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                        "H,375:498"
                }
            }
        }
        Log.i("standalonebannerimage", jsonObject.getString("banner_url"))
//        if(Urls(MyApplication.context).mid == "18") {
            when(MagePrefs.getTheme()) {
                "Grocery Theme" -> {
                    model.imageurl =jsonObject.getString("banner_url")
//                    Glide.with(context).load().placeholder(context.resources.getDrawable(R.drawable.grocerybannersliderlaceholder)).into(binding.image)
                }
                "Fashion Theme" -> {
                    model.imageurl =jsonObject.getString("banner_url")
//                    Glide.with(context).load(jsonObject.getString("banner_url")).placeholder(context.resources.getDrawable(R.drawable.fashionstandaloneplaceholder)).into(binding.image)
                }
                "Home Theme" -> {
                    model.imageurl =jsonObject.getString("banner_url")
//                    Glide.with(context).load(jsonObject.getString("banner_url")).placeholder(context.resources.getDrawable(R.drawable.homestandaloneplaceholder)).into(binding.image)
                }
            }
            binding.commondata=model
//        }
        //stand.image = jsonObject.getString("banner_url")
        if (jsonObject.has("item_button_position")) {
            when (jsonObject.getString("item_button_position")) {
                "no-btn" -> {
                    binding.buttonsection.visibility = View.GONE
                    stand.bannerlink = jsonObject.getString("banner_link_value")
                    stand.bannertype = jsonObject.getString("banner_link_type")
                    binding.buttonsection.tag = "no-btn"
                }
                "bottom" -> {
                    val buttonsection =
                        (binding.buttonsection.layoutParams as ConstraintLayout.LayoutParams)
                    buttonsection.topToTop = ConstraintSet.GONE
                    binding.imageLayout.setOnClickListener(null)
                    binding.buttonsection.tag = "bottom"
                }
                "middle" -> {
                    binding.imageLayout.setOnClickListener(null)
                    binding.buttonsection.tag = "middle"
                }
            }
        }
        when (binding.buttonsection.tag) {
            "bottom", "middle" -> {
                when (jsonObject.getString("item_text_alignment")) {
                    "left" -> {
                        binding.buttonOne.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        binding.buttonTwo.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    }
                    "right" -> {
                        binding.buttonOne.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                        binding.buttonTwo.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    }
                }
                if (jsonObject.has("first_button_text")) {
                    stand.text_one = jsonObject.getString("first_button_text")
                    stand.text_two = jsonObject.getString("second_button_text")
                    Constant.translateField(stand.text_one!!, binding.buttonOne)
                    Constant.translateField(stand.text_two!!, binding.buttonTwo)
                }
                var background = JSONObject(jsonObject.getString("button_background_color"))
                binding.buttonOne.setBackgroundColor(Color.parseColor(background.getString("color")))
                binding.buttonTwo.setBackgroundColor(Color.parseColor(background.getString("color")))
                var button_border_color = JSONObject(jsonObject.getString("button_border_color"))
                binding.one.setCardBackgroundColor(Color.parseColor(button_border_color.getString("color")))
                binding.two.setCardBackgroundColor(Color.parseColor(button_border_color.getString("color")))
                var button_text_color = JSONObject(jsonObject.getString("button_text_color"))
                binding.buttonOne.setTextColor(Color.parseColor(button_text_color.getString("color")))
                binding.buttonTwo.setTextColor(Color.parseColor(button_text_color.getString("color")))
                when (jsonObject.getString("item_font_weight")) {
                    "light" -> {
                        binding.buttonOne.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        binding.buttonTwo.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    }
                    "medium" -> {
                        binding.buttonOne.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        binding.buttonTwo.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                    }
                    "bold" -> {
                        binding.buttonOne.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        binding.buttonTwo.typeface =
                            Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                    }
                }
                when (jsonObject.getString("item_font_style")) {
                    "italic" -> {
                        binding.buttonOne.setTypeface(binding.buttonOne.typeface, Typeface.ITALIC)
                        binding.buttonTwo.setTypeface(binding.buttonTwo.typeface, Typeface.ITALIC)
                    }
                }
                stand.buttononetype = jsonObject.getString("first_button_link_type")
                stand.buttononelink = jsonObject.getString("first_button_link_value")
                stand.buttontwotype = jsonObject.getString("second_button_link_type")
                stand.buttontwolink = jsonObject.getString("second_button_link_value")
            }
        }

        binding.stand = stand
        linkedHashMap.put(key, binding.root)
        // homepagedata.setValue(hashMapOf("standalone-banner_" to binding.root))
    }

    private fun createCircleSlider(jsonObject: JSONObject, key: String) {
        try {
            Log.i("", "")
            val categoryitem: CircleCategorySliderBinding = DataBindingUtil.inflate(
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.circle_category_slider,
                null,
                false
            )
            circleadapter = CustomCircleCategoryAdapter()
            categoryitem.circleadapter.visibility = View.VISIBLE
            categoryitem.sliderCircleList.visibility = View.GONE
            circleadapter.setData(jsonObject.getJSONArray("items"), context)
            Log.i("CIRCLEJSON", "1 " + jsonObject.getJSONArray("items"))
            categoryitem.circleadapter.adapter = circleadapter
            /*circlecatlinkedHashMap.put(key, categoryitem.root)
            var interator=circlecatlinkedHashMap.keys.iterator()
            while (interator.hasNext()){
                var view =circlecatlinkedHashMap.get(interator.next())
                homepage.addView(view)
            }*/
            linkedHashMap.put(key, categoryitem.root)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createBannerSlider(jsonObject: JSONObject, key: String) {
        try {
            var binding: MBannerSlidersBinding = DataBindingUtil.inflate(
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                R.layout.m_banner_sliders,
                null,
                false
            )
            if (jsonObject.has("item_image_size")) {
                when (jsonObject.getString("item_image_size")) {
                    "half" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                            "H,700:60"
                    }
                    "2x" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                            "H,375:200"
                    }
                    "3x" -> {
                        (binding.banners.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                            "H,375:498"
                    }
                }
            }
            binding.banners.adapter = CustomHomePageBanners(
                context.supportFragmentManager,
                context,
                jsonObject.getJSONArray("items"),
                "bannerslider"
            )
            //binding.banners.currentItem = 1
            binding.banners.clipChildren = false
            binding.banners.clipToPadding = false
            binding.banners.setPadding(85, 0, 85, 0)
            binding.banners.pageMargin = 0
            val paddingPx = 100
            val MIN_SCALE = 0.9f
            val MAX_SCALE = 1f
            binding.banners.setPageTransformer(false, object : ViewPager.PageTransformer {
                override fun transformPage(page: View, position: Float) {
                    val pagerWidthPx = (page.parent as ViewPager).width.toFloat()
                    val pageWidthPx: Float = pagerWidthPx - 1 * paddingPx
                    val maxVisiblePages = pagerWidthPx / pageWidthPx
                    val center = maxVisiblePages / 2f
                    var scale: Float
                    if (position + 0.5f < center - 0.5f || position > center) {
                        scale = MIN_SCALE
                    } else {
                        var coef: Float
                        if (position + 0.5f < center) {
                            coef = (position + 1 - center) / 0.5f
                        } else {
                            coef = (center - position) / 0.5f
                        }
                        scale = coef * (MAX_SCALE - MIN_SCALE) + MIN_SCALE
                    }
                    page.scaleX = scale
                    page.scaleY = scale
                }
            })
            if (jsonObject.getString("item_dots").equals("1")) {
                binding.indicator.visibility = View.VISIBLE
                var background = JSONObject(jsonObject.getString("active_dot_color"))
                var strokebackground = JSONObject(jsonObject.getString("inactive_dot_color"))
                binding.indicator.setDotIndicatorColor(Color.parseColor(background.getString("color")))
                binding.indicator.setStrokeDotsIndicatorColor(
                    Color.parseColor(
                        strokebackground.getString(
                            "color"
                        )
                    )
                )
                binding.indicator.setViewPager(binding.banners)
            }
            var i = 0
            val timer = Timer()
            timer.scheduleAtFixedRate(timerTask {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.banners.currentItem = i++
                    if (i == 4) {
                        i = 0
                    }
                }
            }, 5000, 5000)
            linkedHashMap.put(key, binding.root)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createCategorySquare(jsonObject: JSONObject, key: String) {
        var binding: MCategorySquaresBinding = DataBindingUtil.inflate(
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
            R.layout.m_category_squares,
            null,
            false
        )
        repository.getJSonArray(JsonParser().parse(jsonObject.getString("items")).asJsonArray)
            .subscribeOn(Schedulers.io())
            .filter { x -> x.asJsonObject.get("link_type").asString.isNotEmpty() }
            .toList()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<List<JsonElement>> {
                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("CheckResult")
                override fun onError(e: Throwable) {
                }

                override fun onSuccess(list: List<JsonElement>) {
                    val collection = CategoryCircle()
                    when (jsonObject.getString("item_shape")) {
                        "square" -> {
                            binding.cardOne.radius = 0f
                            binding.cardTwo.radius = 0f
                            binding.cardThree.radius = 0f
                            binding.cardFour.radius = 0f
                            binding.cardFive.radius = 0f
                        }
                    }
                    if (jsonObject.getString("item_title").equals("1")) {
                        collection.titlevisible = true
                    }
                    if (jsonObject.getString("item_border").equals("1")) {
                        var item_border_color =
                            JSONObject(jsonObject.getString("item_border_color"))
                        collection.bordercolor = item_border_color.getString("color")
                        binding.cardOne.setCardBackgroundColor(
                            Color.parseColor(
                                item_border_color.getString(
                                    "color"
                                )
                            )
                        )
                        var params = binding.imageOne.layoutParams as FrameLayout.LayoutParams
                        params.setMargins(4, 4, 4, 4)
                        binding.imageOne.layoutParams = params
                        binding.cardTwo.setCardBackgroundColor(
                            Color.parseColor(
                                item_border_color.getString(
                                    "color"
                                )
                            )
                        )
                        binding.imageTwo.layoutParams = params
                        binding.cardThree.setCardBackgroundColor(
                            Color.parseColor(
                                item_border_color.getString(
                                    "color"
                                )
                            )
                        )
                        binding.imageThree.layoutParams = params
                        binding.cardFour.setCardBackgroundColor(
                            Color.parseColor(
                                item_border_color.getString(
                                    "color"
                                )
                            )
                        )
                        binding.imageFour.layoutParams = params
                        binding.cardFive.setCardBackgroundColor(
                            Color.parseColor(
                                item_border_color.getString(
                                    "color"
                                )
                            )
                        )
                        binding.imageFive.layoutParams = params
                    }
                    val model = CommanModel()

                    var tittlecolor = JSONObject(jsonObject.getString("item_title_color"))
                    var face = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                    when (jsonObject.getString("item_font_weight")) {
                        "medium" -> {
                            face = Typeface.createFromAsset(context.assets, "fonts/popmedium.ttf")
                        }
                        "light" -> {
                            face = Typeface.createFromAsset(context.assets, "fonts/poplight.ttf")
                        }
                        "bold" -> {
                            face = Typeface.createFromAsset(context.assets, "fonts/popbold.ttf")
                        }
                    }
                    if (list.get(0).asJsonObject.has("title")) {
                        collection.cat_text_one = list.get(0).asJsonObject.get("title").asString
                        Constant.translateField(collection.cat_text_one!!, binding.catTextOne)
                        collection.cat_text_two = list.get(1).asJsonObject.get("title").asString
                        Constant.translateField(collection.cat_text_two!!, binding.catTextTwo)
                        collection.cat_text_three = list.get(2).asJsonObject.get("title").asString
                        Constant.translateField(collection.cat_text_three!!, binding.catTextThree)
                        collection.cat_text_four = list.get(3).asJsonObject.get("title").asString
                        Constant.translateField(collection.cat_text_four!!, binding.catTextFour)
                        collection.cat_text_five = list.get(4).asJsonObject.get("title").asString
                        Constant.translateField(collection.cat_text_five!!, binding.catTextFive)
                    }
                    if (list.get(0).asJsonObject.has("image_url")) {
                        /*collection.cat_image_one =
                            list.get(0).asJsonObject?.get("image_url")?.asString
                        collection.cat_image_two =
                            list.get(1).asJsonObject?.get("image_url")?.asString
                        collection.cat_image_three =
                            list.get(2).asJsonObject?.get("image_url")?.asString
                        collection.cat_image_four =
                            list.get(3).asJsonObject?.get("image_url")?.asString
                        collection.cat_image_five =
                            list.get(4).asJsonObject?.get("image_url")?.asString*/
                        model.imageurl =list.get(0).asJsonObject?.get("image_url")?.asString
                        model.imageurl =list.get(1).asJsonObject?.get("image_url")?.asString
                        model.imageurl =list.get(2).asJsonObject?.get("image_url")?.asString
                        model.imageurl =list.get(3).asJsonObject?.get("image_url")?.asString
                        model.imageurl =list.get(4).asJsonObject?.get("image_url")?.asString
//                        Glide.with(context).load(list.get(0).asJsonObject?.get("image_url")?.asString).placeholder(context.resources.getDrawable(R.drawable.hcatsquarefirst)).into(binding.imageOne)
//                        Glide.with(context).load(list.get(1).asJsonObject?.get("image_url")?.asString).placeholder(context.resources.getDrawable(R.drawable.hcatsquaresec)).into(binding.imageTwo)
//                        Glide.with(context).load(list.get(2).asJsonObject?.get("image_url")?.asString).placeholder(context.resources.getDrawable(R.drawable.hcatsquarethird)).into(binding.imageThree)
//                        Glide.with(context).load(list.get(3).asJsonObject?.get("image_url")?.asString).placeholder(context.resources.getDrawable(R.drawable.hcatsquarefourth)).into(binding.imageFour)
//                        Glide.with(context).load(list.get(4).asJsonObject?.get("image_url")?.asString).placeholder(context.resources.getDrawable(R.drawable.hcatsquarefive)).into(binding.imageFive)
                    }
                    binding.commondata=model
                    if (list.get(0).asJsonObject.has("link_type")) {
                        collection.cat_link_one = list.get(0).asJsonObject.get("link_type").asString
                        collection.cat_link_two = list.get(1).asJsonObject.get("link_type").asString
                        collection.cat_link_three =
                            list.get(2).asJsonObject.get("link_type").asString
                        collection.cat_link_four =
                            list.get(3).asJsonObject.get("link_type").asString
                        collection.cat_link_five =
                            list.get(4).asJsonObject.get("link_type").asString
                    }
                    if (list.get(0).asJsonObject.has("link_value")) {
                        collection.cat_value_one =
                            list.get(0).asJsonObject.get("link_value").asString
                        collection.cat_value_two =
                            list.get(1).asJsonObject.get("link_value").asString
                        collection.cat_value_three =
                            list.get(2).asJsonObject.get("link_value").asString
                        collection.cat_value_four =
                            list.get(3).asJsonObject.get("link_value").asString
                        if (list.get(4).asJsonObject.has("link_value")) {
                            collection.cat_value_five =
                                list.get(4).asJsonObject.get("link_value").asString
                        }
                    }
                    binding.catTextOne.setTextColor(Color.parseColor(tittlecolor.getString("color")))
                    binding.catTextTwo.setTextColor(Color.parseColor(tittlecolor.getString("color")))
                    binding.catTextThree.setTextColor(Color.parseColor(tittlecolor.getString("color")))
                    binding.catTextFour.setTextColor(Color.parseColor(tittlecolor.getString("color")))
                    binding.catTextFive.setTextColor(Color.parseColor(tittlecolor.getString("color")))
                    binding.catTextOne.typeface = face
                    binding.catTextTwo.typeface = face
                    binding.catTextThree.typeface = face
                    binding.catTextFour.typeface = face
                    binding.catTextFive.typeface = face
                    when (jsonObject.getString("item_font_style")) {
                        "italic" -> {
                            binding.catTextOne.setTypeface(
                                binding.catTextOne.typeface,
                                Typeface.ITALIC
                            )
                            binding.catTextTwo.setTypeface(
                                binding.catTextTwo.typeface,
                                Typeface.ITALIC
                            )
                            binding.catTextThree.setTypeface(
                                binding.catTextThree.typeface,
                                Typeface.ITALIC
                            )
                            binding.catTextFour.setTypeface(
                                binding.catTextFour.typeface,
                                Typeface.ITALIC
                            )
                            binding.catTextFive.setTypeface(
                                binding.catTextFive.typeface,
                                Typeface.ITALIC
                            )
                        }
                    }
                    binding.category = collection
                }
            })

        var background = JSONObject(jsonObject.getString("panel_background_color"))
        binding.squareCircleList.setBackgroundColor(Color.parseColor(background.getString("color")))
        binding.root.setBackgroundColor(Color.parseColor(background.getString("color")))
        
        linkedHashMap.put(key, binding.root)
        /*circleadapter = CircleCategoryAdapter()
        binding.circleadapter.visibility = View.VISIBLE
        binding.circleList.visibility = View.GONE
        circleadapter.setData(jsonObject.getJSONArray("items"), context)
        Log.i("CIRCLEJSON","1 " +jsonObject.getJSONArray("items"))
        binding.circleadapter.adapter = circleadapter
        *//*circlecatlinkedHashMap.put(key, categoryitem.root)
        var interator=circlecatlinkedHashMap.keys.iterator()
        while (interator.hasNext()){
            var view =circlecatlinkedHashMap.get(interator.next())
            homepage.addView(view)
        }*/

    }

    private fun updateDataInRecylerView(
        productdata: RecyclerView?,
        jsonArray: JSONArray,
        jsonObject: JSONObject,
        flag: Boolean, view: View
    ) {
        runBlocking(Dispatchers.IO) {
            try {
                val edges = mutableListOf<Storefront.Product>()
                val product_ids = ArrayList<ID>()
                for (i in 0..jsonArray.length() - 1) {
                    product_ids.add(ID(getProductID(jsonArray.getString(i))))
                }
                getProductsById(product_ids, productdata, jsonObject, edges, flag, view,"demopage")
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun getProductsById(
        id: ArrayList<ID>,
        productdata: RecyclerView?,
        jsonObject: JSONObject,
        edges: MutableList<Storefront.Product>, flag: Boolean, view: View, viewType: String
    ) {
        try {
            doGraphQLQueryGraph(
                repository,
                Query.getAllProductsByID(id, Constant.internationalPricing(),viewType),
                customResponse = object : CustomResponse {
                    override fun onSuccessQuery(result: GraphCallResult<Storefront.QueryRoot>) {
                        if (result is GraphCallResult.Success<*>) {
                            consumeResponse(
                                GraphQLResponse.success(result as GraphCallResult.Success<*>),
                                productdata,
                                jsonObject,
                                edges, flag, view
                            )
                        } else {
                            consumeResponse(
                                GraphQLResponse.error(result as GraphCallResult.Failure),
                                productdata,
                                jsonObject,
                                edges, flag, view
                            )
                        }
                    }
                },
                context = context
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun consumeResponse(
        reponse: GraphQLResponse,
        productdata: RecyclerView?,
        jsonObject: JSONObject,
        edges: MutableList<Storefront.Product>, flag: Boolean, view: View
    ) {
        when (reponse.status) {
            Status.SUCCESS -> {
                val result =
                    (reponse.data as GraphCallResult.Success<Storefront.QueryRoot>).response
                if (result.hasErrors) {
                    val errors = result.errors
                    val iterator = errors.iterator()
                    val errormessage = StringBuilder()
                    var error: Error? = null
                    while (iterator.hasNext()) {
                        error = iterator.next()
                        errormessage.append(error.message())
                    }
                    Log.i("MageNatyive", "ERROR" + errormessage.toString())
                } else {
                    try {
                        for (i in 0..result.data!!.nodes.size - 1) {
                            if (result.data!!.nodes[i] != null) {
                                var product = result.data?.nodes?.get(i)!! as Storefront.Product
                                edges.add(product)
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        var array: List<HomePageProduct> =
                                            repository.getHomePageProduct(result.data?.nodes?.get(i)!!.id.toString())
                                        Log.i("SaifDev_cache", "Product" + array.size)
                                        Log.i("SaifDev_cache", "Product" + array)
                                        if (array.size == 0) {
                                            var data = HomePageProduct()
                                            data.product_id =
                                                result.data?.nodes?.get(i)!!.id.toString()
                                            var ProductData =
                                                Gson().toJson(result.data?.nodes?.get(i)!! as Storefront.Product)
                                            data.product = ProductData
                                            repository.insertHomePageProduct(data)
                                            /*CoroutineScope((Dispatchers.Main)).launch {
                                                message.value="Saif"+ProductData
                                                message.value="Product Inserted"+result.data?.nodes?.get(i)!!.id.toString()
                                            }*/
                                        } else {
                                            var data = repository.getHomePageProduct(
                                                result.data?.nodes?.get(i)!!.id.toString()
                                            ).get(0)
                                            data.product_id =
                                                result.data?.nodes?.get(i)!!.id.toString()
                                            var ProductData =
                                                Gson().toJson(result.data?.nodes?.get(i)!! as Storefront.Product)
                                            data.product = ProductData
                                            repository.updateHomePageProduct(data)
                                            /*CoroutineScope((Dispatchers.Main)).launch {
                                                message.value="Product updated"+result.data?.nodes?.get(i)!!.id.toString()
                                            }*/
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }

                        if (flag) {
                            filterProduct(edges, productdata, jsonObject, view)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        when (context.packageName) {
                            "com.rasmishopping.app" -> {
                                //    Toast.makeText(context, "Please Provide Visibility to Products and Collections", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
            Status.ERROR -> {
                Log.i("MageNatyive", "ERROR-1" + reponse.error!!.error.message)
                //message.setValue(reponse.error.error.message)
            }
        }
    }

    private fun filterProduct(
        list: List<Storefront.Product>,
        productdata: RecyclerView?,
        jsonObject: JSONObject, view: View
    ) {
        try {
            if (SplashViewModel.featuresModel.outOfStock!!) {
                repository.getProductListSlider(list)
                    .subscribeOn(Schedulers.io())
                    .filter { x -> checkNode(node = x) }
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<List<Storefront.Product>> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onSuccess(list: List<Storefront.Product>) {
                            view.visibility = View.VISIBLE
                            when (jsonObject.getString("type")) {
                                "fixed-customisable-layout" -> {
                                    if (jsonObject.getString("item_layout_type").equals("list")) {
                                        productListAdapter = ProductListSliderAdapter()
                                        context.setLayout(productdata!!, "customisablelist")
                                        productListAdapter.setData(list, context, jsonObject)
                                        productdata.adapter = productListAdapter
                                    } else {
                                        when (jsonObject.getString("item_in_a_row")) {
                                            "2" -> {
                                                var productTwoGridAdapter = ProductTwoGridAdapter()
                                                context.setLayout(
                                                    productdata!!,
                                                    "customisablegridwithtwoitem"
                                                )
                                                productTwoGridAdapter.setData(
                                                    list,
                                                    context,
                                                    jsonObject,
                                                    repository
                                                )
                                                productdata.adapter = productTwoGridAdapter
                                            }
                                            "3" -> {
                                                gridAdapter = ProductSliderGridAdapter()
                                                context.setLayout(productdata!!, "customisablegrid")
                                                gridAdapter.setData(list, context, jsonObject)
                                                productdata.adapter = gridAdapter
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    Log.i("MageNatyive", "Data" + list.size)
                                    homeadapter = ProductSliderListAdapter()
                                    //context.setLayout(productdata!!, "4grid")
                                    if (list.size > 4) {
                                        productdata!!.layoutManager = GridLayoutManager(
                                            context,
                                            2,
                                            GridLayoutManager.HORIZONTAL,
                                            false
                                        )
                                    } else {
                                        productdata!!.layoutManager = LinearLayoutManager(
                                            context,
                                            LinearLayoutManager.HORIZONTAL, false
                                        )
                                    }
                                    homeadapter.setData(list, context, jsonObject, repository)
                                    productdata.adapter = homeadapter
                                }
                            }
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }
                    })
            } else {
                repository.getProductListSlider(list)
                    .subscribeOn(Schedulers.io())
                    .filter { x -> x.availableForSale && checkNode(node = x) }
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : SingleObserver<List<Storefront.Product>> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onSuccess(list: List<Storefront.Product>) {
                            view.visibility = View.VISIBLE
                            when (jsonObject.getString("type")) {
                                "fixed-customisable-layout" -> {
                                    if (jsonObject.getString("item_layout_type").equals("list")) {
                                        productListAdapter = ProductListSliderAdapter()
                                        context.setLayout(productdata!!, "customisablelist")
                                        productListAdapter.setData(list, context, jsonObject)
                                        productdata.adapter = productListAdapter
                                    } else {
                                        when (jsonObject.getString("item_in_a_row")) {
                                            "2" -> {
                                                var productTwoGridAdapter = ProductTwoGridAdapter()
                                                context.setLayout(
                                                    productdata!!,
                                                    "customisablegridwithtwoitem"
                                                )
                                                productTwoGridAdapter.setData(
                                                    list,
                                                    context,
                                                    jsonObject,
                                                    repository
                                                )
                                                productdata.adapter = productTwoGridAdapter
                                            }
                                            "3" -> {
                                                gridAdapter = ProductSliderGridAdapter()
                                                context.setLayout(productdata!!, "customisablegrid")
                                                gridAdapter.setData(list, context, jsonObject)
                                                productdata.adapter = gridAdapter
                                            }
                                        }
                                    }
                                }
                                else -> {
                                    Log.i("MageNatyive", "Data" + list.size)
                                    homeadapter = ProductSliderListAdapter()
                                    context.setLayout(productdata!!, "horizontal")
                                    homeadapter.setData(list, context, jsonObject, repository)
                                    productdata.adapter = homeadapter
                                    homeadapter = ProductSliderListAdapter()
                                    //context.setLayout(productdata!!, "4grid")
                                    /*if(list.size > 4) {
                                        productdata!!.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
                                    } else {
                                        productdata!!.layoutManager = LinearLayoutManager(context,
                                            LinearLayoutManager.HORIZONTAL, false)
                                    }
                                    homeadapter.setData(list, context, jsonObject, repository)
                                    productdata.adapter = homeadapter*/
                                }
                            }
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }
                    })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getProductID(id: String?): String? {
        var cat_id: String? = null
        try {
            val data = ("gid://shopify/Product/" + id!!)
            cat_id = data
            Log.i("MageNatyive", "ProductSliderID :$id " + cat_id)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cat_id
    }

    fun checkNode(node: Storefront.Product): Boolean {
        return !node.tags.contains("se_global")
    }
}