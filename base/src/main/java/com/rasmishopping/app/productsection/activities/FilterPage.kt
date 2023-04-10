package com.rasmishopping.app.productsection.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.customviews.MageNativeTextView
import com.rasmishopping.app.databinding.FilterPageSecondBinding
import com.rasmishopping.app.productsection.viewmodels.FilterModel
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.ViewModelFactory
import kotlinx.android.synthetic.main.filter_page_second.*
import kotlinx.android.synthetic.main.m_newbaseactivity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

class FilterPage : NewBaseActivity() {
    private var filterpagesecondBinding: FilterPageSecondBinding? = null
    var count = 0
    var TAG = "filterresponse"
    var filterModel: FilterModel? = null
    var firstTime: Boolean = true
    var product_id: String? = null
    var c: String = "0"
    var list: LinkedHashMap<Long, Long> = LinkedHashMap()
    var linkedList: LinkedList<Long> = LinkedList()

    @Inject
    lateinit var factory: ViewModelFactory

    @SuppressLint("LogNotTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        filterpagesecondBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.filter_page_second, group, true)
        showBackButton()
        hidethemeselector()
        showTittle(resources.getString(R.string.apply_filter))
        (application as MyApplication).mageNativeAppComponent!!.doFilterPageInjection(this)
        Log.d("REpo", "basecurr " + MagePrefs.getCurrency())
        Log.d("REpo", "prescur: " + Constant.getCurrency(MagePrefs.getCountryCode()!!))
        nav_view.visibility = View.GONE
        filterModel = ViewModelProvider(this, factory).get(FilterModel::class.java)
        filterModel!!.context = this
        filterModel!!.FilterProductsResponse().observe(this, Observer<List<Storefront.Filter>> { this.prepareFilterData(it) })
        if (intent.getStringExtra("ID") != null) {
            product_id = intent.getStringExtra("ID")
        }
        if (SplashViewModel.filterfinaldata.size > 0) {
            btn_reset.visibility = View.VISIBLE
            btn_apply.visibility = View.VISIBLE
            btn_reset.isEnabled = true
            btn_apply.isEnabled = true
            btn_reset.alpha = 1.0f
            btn_apply.alpha = 1.0f
        } else {
            btn_reset.alpha = 0.4f
            btn_apply.alpha = 0.4f
            btn_reset.isEnabled = false
            btn_apply.isEnabled = false
        }
        btn_apply.setOnClickListener {
            setResult(RESULT_OK)
            finish()
        }
        btn_reset.setOnClickListener {
            Toast.makeText(this, resources.getString(R.string.clearfilter), Toast.LENGTH_SHORT)
                .show()
            SplashViewModel.filterfinaldata = HashMap<String, java.util.ArrayList<String>>()
            SplashViewModel.filterinputformat = HashMap<String, String>()
            lienar_main_cat_filter.removeAllViews()
            lienar_sub_cat_filter.removeAllViews()
            firstTime = true
            filterModel!!.getFilteredProducts()
            btn_reset.alpha = 0.4f
            btn_apply.alpha = 0.4f
            btn_reset.isEnabled = false
            btn_apply.isEnabled = false
        }
        onBackPressedDispatcher.addCallback(
            this /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    SplashViewModel.filterfinaldata = HashMap<String, java.util.ArrayList<String>>()
                    SplashViewModel.filterinputformat = HashMap<String, String>()
                    finish()
                }
            })
    }

    @SuppressLint("LogNotTimber")
    private fun prepareFilterData(productfilter: List<Storefront.Filter>?) {
        btn_apply.text =
            this.resources.getString(R.string.apply_filter) + " " + SplashViewModel.filterfinaldata.size.toString()
        if (productfilter != null) {
            for (element in productfilter) {
                Log.i("IDS", "" + element.id)
                Log.i("IDS", "" + element.type)
                var filtertype = element.id.split(".")
                Log.i("IDS", "" + filtertype[2])
                when (filtertype[2]) {
                    "availability", "product_type", "vendor", "option", "m" -> {
                        addFilter(element, filtertype[2])
                    }
                    "price" -> {
                        if (MagePrefs.getCurrency()
                                .equals(Constant.getCurrency(MagePrefs.getCountryCode()!!))
                        )
                        {
                            addFilter(element, filtertype[2])
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ResourceType")
    private fun addFilter(responseData: Storefront.Filter, filtertype: String) {
        var main_filter_head = layoutInflater.inflate(R.layout.main_filtersection, null)
        var text_mainheading = main_filter_head.findViewById(R.id.text_mainheading) as TextView
        var line = main_filter_head.findViewById(R.id.line) as TextView
        var line2 = main_filter_head.findViewById(R.id.line2) as TextView
        text_mainheading.setTextColor(this.resources.getColor(R.color.black))
        var filter_count = main_filter_head.findViewById(R.id.filtercount) as TextView
//        var line = main_filter_head.findViewById(R.id.line2) as TextView
        if (firstTime) {
//            line.tag="selected"
            text_mainheading.setTextColor(this.resources.getColor(R.color.black))
            text_mainheading.backgroundTintList = this.resources.getColorStateList(R.color.white)
            line.backgroundTintList = this.resources.getColorStateList(R.color.colorPrimary)
            line2.backgroundTintList = this.resources.getColorStateList(R.color.white)
//            line.visibility=View.VISIBLE
            filter_count.visibility = View.VISIBLE
            filterpagesecondBinding
            text_mainheading.tag = "current"
            line.tag = "selected"
            selectFilter(filtertype, responseData, filter_count)
        }
        text_mainheading.text = responseData.label.toString()
        CoroutineScope(Dispatchers.IO).launch {
            main_filter_head!!.setOnClickListener {
                var child = lienar_main_cat_filter.childCount


                for (i in 0 until child) {

                    val view = lienar_main_cat_filter.getChildAt(i) as ConstraintLayout
                    var textView = view.getChildAt(0) as MageNativeTextView
                    var textView2 = view.getChildAt(1) as MageNativeTextView
                    var textView1 = view.getChildAt(2) as MageNativeTextView
                    var textView3 = view.getChildAt(3) as MageNativeTextView



                    if (textView.tag.equals("current")) {
                        text_mainheading.tag = "notcurrent"


                        textView.setTextColor(resources.getColor(R.color.black))
//                    line.visibility=View.GONE

                        textView.backgroundTintList = resources.getColorStateList(R.color.lightgray)
                        textView2.backgroundTintList =
                            resources.getColorStateList(R.color.lightgray)
//                        textView3.visibility=View.VISIBLE


                    }
                    if (textView1.tag.equals("selected")) {

                        textView1.tag = "unselected"

//                line.visibility=View.GONE
                        textView1.backgroundTintList =
                            resources.getColorStateList(R.color.lightgray)
                    }


                }

                text_mainheading.tag = "current"
//        line.visibility=View.VISIBLE
                line.tag = "selected"
//            line.visibility=View.VISIBLE
                text_mainheading.setTextColor(resources.getColor(R.color.black))
                text_mainheading.backgroundTintList = resources.getColorStateList(R.color.white)
                line2.backgroundTintList = resources.getColorStateList(R.color.white)
                line.backgroundTintList = resources.getColorStateList(R.color.colorPrimary)
                Log.d("pd", "added")
//            line.backgroundTintList=this.resources.getColorStateList(R.color.blue)
                MagePrefs.saveFilterType(responseData.id.toString())

                filter_count.visibility = View.VISIBLE
                Log.d("testtt", "" + responseData.id.toString())
                selectFilter(filtertype, responseData, filter_count)

            }
        }


        lienar_main_cat_filter?.addView(main_filter_head)
    }

    @SuppressLint("LogNotTimber", "SetTextI18n")
    private fun selectFilter(
        filterType: String?,
        jsonob: Storefront.Filter,
        filter_count: TextView
    ) {
        btn_apply.text =
            this.resources.getString(R.string.apply_filter) + " " + "(" + SplashViewModel.filterfinaldata.size.toString() + ")"
        try {
            when (filterType) {
                "availability", "product_type", "vendor" -> {
                    createProductFilters(filterType, jsonob, "nonamespace", filter_count)
                    firstTime = false
                }
                "price" -> {
                    createPriceFilter(filterType, jsonob, filter_count)
                    firstTime = false

                }
                "option" -> {
                    var filtertype = jsonob.id.split(".")
                    createProductFilters(filtertype[3], jsonob, "nonamespace", filter_count)
                    firstTime = false
                }
                "m" -> {
                    var filtertype = jsonob.id.split(".")
                    createProductFilters(filtertype[4], jsonob, filtertype[3], filter_count)
                    firstTime = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingInflatedId")
    fun createProductFilters(
        filtertype: String,
        filter: Storefront.Filter,
        namespace: String,
        filter_count: TextView
    ) {

        if (lienar_sub_cat_filter.childCount > 0) {
            lienar_sub_cat_filter.removeAllViews()
        }
//        btn_apply.text =
//            this.resources.getString(R.string.apply_filter) + " " + SplashViewModel.filterfinaldata.size.toString()


        filter.values.forEach { it ->
            val brandList = layoutInflater.inflate(R.layout.m_brand_name, null)
            val checkBox = brandList.findViewById<CheckBox>(R.id.brandCheckbox)
            var checkBox_text = brandList.findViewById<MageNativeTextView>(R.id.checkbox_text)
            var count = brandList.findViewById<MageNativeTextView>(R.id.count)
            checkBox_text.text = it.label
            count.text = it.count.toString()


            if (SplashViewModel.filterfinaldata.containsKey(filtertype)) {
                if (SplashViewModel.filterfinaldata.get(filtertype)!!.contains(it.label)) {
//                    btn_apply.text =
//                        this.resources.getString(R.string.apply_filter) + " " + SplashViewModel.filterfinaldata.size.toString()
                    checkBox.isChecked = true

                }
            }
            System.out.println("FiltersSaif" + SplashViewModel.filterfinaldata)

            if (filtertype.equals("availability")) {
                SplashViewModel.filterinputformat.put(it.label, it.input)
            }
            if (!namespace.equals("nonamespace")) {
                SplashViewModel.filterinputformat.put(filtertype, namespace)
                SplashViewModel.filterinputformat.put("metafield", filtertype)
            }

            checkBox.setOnCheckedChangeListener { compoundButton, b ->

                if (b) {

                    Log.i("itinput", "${it.label}")
                    if (SplashViewModel.filterfinaldata.containsKey(filtertype)) {
                        var list = SplashViewModel.filterfinaldata.get(filtertype)
                        if (!SplashViewModel.filterfinaldata.get(filtertype)!!.contains(it.label)) {
                            list!!.add(it.label)
                            SplashViewModel.filterfinaldata.put(filtertype, list)
                            if (list.size > 0) {
                                filter_count.text = list.size.toString()
                                filter_count.visibility = View.VISIBLE
                            } else {
                                filter_count.visibility = View.GONE
                            }
                            btn_apply.text =
                                this.resources.getString(R.string.apply_filter) + " " + "(" + SplashViewModel.filterfinaldata.size.toString() + ")"
                        }
                    } else {
                        var list = ArrayList<String>()
                        list.add(it.label)
                        SplashViewModel.filterfinaldata.put(filtertype, list)
                        btn_apply.text =
                            this.resources.getString(R.string.apply_filter) + " " + "(" + SplashViewModel.filterfinaldata.size.toString() + ")"
                        if (list.size > 0) {
                            filter_count.text = list.size.toString()
                            filter_count.visibility = View.VISIBLE
                        } else {
                            filter_count.visibility = View.GONE
                        }
                    }
                } else {

                    if (SplashViewModel.filterfinaldata.get(filtertype)!!.contains(it.label)) {
                        var list = SplashViewModel.filterfinaldata.get(filtertype)
                        list!!.remove(it.label)
                        SplashViewModel.filterfinaldata.put(filtertype, list)
                        btn_apply.text =
                            this.resources.getString(R.string.apply_filter) + " " + "(" + SplashViewModel.filterfinaldata.size.toString() + ")"
                        if (list.size > 0) {
                            filter_count.text = list.size.toString()
                            filter_count.visibility = View.VISIBLE
                        } else {
                            filter_count.visibility = View.GONE
                        }
                    }
                    if (SplashViewModel.filterfinaldata.get(filtertype)!!.size == 0) {
                        SplashViewModel.filterfinaldata.remove(filtertype)
                        btn_apply.text =
                            this.resources.getString(R.string.apply_filter) + " " + "(" + SplashViewModel.filterfinaldata.size.toString() + ")"

                    }
                }
                if (SplashViewModel.filterfinaldata.size > 0) {
                    btn_reset.visibility = View.VISIBLE
                    btn_apply.visibility = View.VISIBLE
                    btn_reset.isEnabled = true
                    btn_apply.isEnabled = true
                    btn_reset.alpha = 1.0f
                    btn_apply.alpha = 1.0f
                } else {
                    btn_reset.alpha = 0.4f
                    btn_apply.alpha = 0.4f
                    btn_reset.isEnabled = false
                    btn_apply.isEnabled = false
                }
                System.out.println("FiltersSaifInput" + it.input)
                System.out.println("FiltersSaif" + SplashViewModel.filterfinaldata)
            }
            lienar_sub_cat_filter.addView(brandList)
        }
    }

    fun price_interval(min: Long, max: Long, nbIntervalls: Long): HashMap<Long, Long> {
        val data = max - min  // --------------------------> subtract min
        var size = Math.round(((data) / nbIntervalls).toDouble())
        for (i in 0 until nbIntervalls) {
            var inf = i + i * size
            var sup: Long
            if (inf + size < data) {
                sup = inf + size
            } else {
                sup = data
            }

            var minvalue = inf + min
            var maxvalue = sup + min
            list.put(minvalue, maxvalue)
            linkedList.add(minvalue)// --------------------> add again min
            if (inf >= data || sup >= data) break
        }
        return list
    }

    @SuppressLint("SetTextI18n")
    fun createPriceFilter(filtertype: String, jsonob: Storefront.Filter, filter_count: TextView) {

        btn_apply.text =
            this.resources.getString(R.string.apply_filter) + " " + SplashViewModel.filterfinaldata.size.toString()
        if (lienar_sub_cat_filter.childCount > 0) {
            lienar_sub_cat_filter.removeAllViews()
        }

        val valuesarray = jsonob.values[0].input
        val priceobj = (JSONObject(valuesarray)).getJSONObject("price")
//        seekbar.tickEnd = priceobj.getString("max").toFloat()
//        seekbar.tickStart = priceobj.getString("min").toFloat()
        price_interval(
            priceobj.getString("min").toFloat().toLong(),
            priceobj.getString("max").toFloat().toLong(),
            5
        )

        for (i in 0 until list.size) {
            val sub_filter_head_range =
                layoutInflater.inflate(R.layout.m_productfilterpriceitem, null)
            val checkBox = sub_filter_head_range.findViewById<CheckBox>(R.id.price_button)
            val price: TextView = sub_filter_head_range.findViewById(R.id.pricelist)
            Log.d("pddddddddd", "" + linkedList[i] + "-" + list.get(linkedList[i]))
            var min = linkedList[i]
            var max = list.get(linkedList[i])

            price.text = MagePrefs.getSymbol()+" "+ min.toString() + " - " + MagePrefs.getSymbol()+" "+max
            lienar_sub_cat_filter.addView(sub_filter_head_range)
            if (SplashViewModel.filterfinaldata.containsKey(filtertype)) {
                if (SplashViewModel.filterfinaldata.get(filtertype)!!.get(0)
                        .contains(min.toString())
                ) {
//                    btn_apply.text =
//                        this.resources.getString(R.string.apply_filter) + " " + SplashViewModel.filterfinaldata.size.toString()
                    checkBox.isChecked = true
                    checkBox.tag="selected"
                    filter_count.text = "1"
                    filter_count.visibility = View.VISIBLE

                }
            }
            checkBox.setOnCheckedChangeListener { compoundButton, b ->

                if (b) {
                    var child = lienar_sub_cat_filter.childCount
                    for (j in 0 until child) {
                        val view = lienar_sub_cat_filter.getChildAt(j) as ConstraintLayout
                        var checkview = view.getChildAt(0) as CheckBox
                        var checkview1 = view.getChildAt(1) as MageNativeTextView
                        if (checkview.tag.equals("selected")) {
                            Log.d("pddddd", "notselected" + checkview1.text)
                            checkview.tag = "notselected"
                            checkview.isChecked = false
                        }
                    }
                    Log.d("pddddd", "selected" + price.text)
                    checkBox.tag = "selected"
                    checkBox.isChecked = true
                    var value = price.text.split(" - ").toTypedArray()
                    val minvalue: String =value[0].split(" ").get(1)
                    val maxvalue: String = value[1].split(" ").get(1)
                    var list = ArrayList<String>()
                    list.add(0, minvalue)
                    list.add(1, maxvalue)
                    SplashViewModel.filterfinaldata.put(filtertype, list)
                    if (SplashViewModel.filterfinaldata.size > 0) {
                        btn_reset.visibility = View.VISIBLE
                        btn_apply.visibility = View.VISIBLE
                        btn_reset.isEnabled = true
                        btn_apply.isEnabled = true
                        btn_reset.alpha = 1.0f
                        btn_apply.alpha = 1.0f
                    } else {
                        btn_reset.alpha = 0.4f
                        btn_apply.alpha = 0.4f
                        btn_reset.isEnabled = false
                        btn_apply.isEnabled = false
                    }
                    System.out.println("FiltersSaif" + SplashViewModel.filterfinaldata)
                    if (list.size > 0) {
                        filter_count.text = "1"
                        filter_count.visibility = View.VISIBLE
                    } else {
                        filter_count.visibility = View.GONE
                    }
                    btn_apply.text =
                        resources.getString(R.string.apply_filter) + " " + "(" + SplashViewModel.filterfinaldata.size.toString() + ")"
                } else {
                    if (SplashViewModel.filterfinaldata.get(filtertype)!!.get(0)
                            .contains(min.toString())
                    ) {
                        var list = SplashViewModel.filterfinaldata.get(filtertype)
                        list?.clear()
                        SplashViewModel.filterfinaldata.put(filtertype, list!!)
                        btn_apply.text =
                            this.resources.getString(R.string.apply_filter) + " " + "(" + SplashViewModel.filterfinaldata.size.toString() + ")"
                        if (list.size > 0) {
                            filter_count.text = list.size.toString()
                            filter_count.visibility = View.VISIBLE
                        } else {
                            filter_count.visibility = View.GONE
                        }
                    }
                    if (SplashViewModel.filterfinaldata.get(filtertype)!!.size == 0) {
                        SplashViewModel.filterfinaldata.remove(filtertype)
                        btn_apply.text =
                            this.resources.getString(R.string.apply_filter) + " " + "(" + SplashViewModel.filterfinaldata.size.toString() + ")"

                    }
                }


            }

//            price.setText())
        }
    }


    //        for (key in list.keys) {
//
//            val sub_filter_head_range =
//                View.inflate(this@FilterPage, R.layout.m_productfilterpriceitem, null)
//            val price: TextView = sub_filter_head_range.findViewById(R.id.price_data)
//
//Log.d("pddddddd",""+key +""+list[key])
////            price.append(list.values.add(0).toString())
//            lienar_sub_cat_filter.addView(sub_filter_head_range)
//        }

//        seekbar.setPinTextFormatter { value -> value }
//        System.out.println("FiltersSaif" + SplashViewModel.filterfinaldata)
//        seekbar.setOnRangeBarChangeListener(object : OnRangeBarChangeListener {
//            override fun onRangeChangeListener(
//                rangeBar: RangeBar,
//                leftPinIndex: Int,
//                rightPinIndex: Int,
//                leftPinValue: String,
//                rightPinValue: String
//            ) {
//            }
//
//            override fun onTouchEnded(rangeBar: RangeBar) {
//
//                val minvalue: String = rangeBar.leftPinValue.toString()
//                val maxvalue: String = rangeBar.rightPinValue.toString()
//                var list = ArrayList<String>()
//                list.add(0, minvalue)
//                list.add(1, maxvalue)
//                SplashViewModel.filterfinaldata.put(filtertype, list)
//                if (SplashViewModel.filterfinaldata.size > 0) {
//                    btn_reset.visibility = View.VISIBLE
//                    btn_apply.visibility = View.VISIBLE
//                    btn_reset.isEnabled = true
//                    btn_apply.isEnabled = true
//                    btn_reset.alpha = 1.0f
//                    btn_apply.alpha = 1.0f
//                } else {
//                    btn_reset.alpha = 0.4f
//                    btn_apply.alpha = 0.4f
//                    btn_reset.isEnabled = false
//                    btn_apply.isEnabled = false
//                }
//                System.out.println("FiltersSaif" + SplashViewModel.filterfinaldata)
////                if(list.size>0) {
////                    filter_count.text = list.size.toString()
////                    filter_count.visibility=View.VISIBLE
////                }
////                else
////                {
////                    filter_count.visibility=View.GONE
////                }
//                btn_apply.text =
//                    resources.getString(R.string.apply_filter) + " " + "(" + SplashViewModel.filterfinaldata.size.toString() + ")"
//            }
//
//            override fun onTouchStarted(rangeBar: RangeBar) {}
//        })

    override fun onDestroy() {
        super.onDestroy()
        nav_view.visibility = View.VISIBLE
    }


}
