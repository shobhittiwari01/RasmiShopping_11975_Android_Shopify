package com.rasmishopping.app.cartsection.activities
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.cartdiscount.listing.DiscountListingViewModel
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.cartsection.adapters.AllDiscountListAdapter
import com.rasmishopping.app.databinding.MCouponlistBinding
import com.rasmishopping.app.utils.Urls
import com.rasmishopping.app.utils.ViewModelFactory
import org.json.JSONObject
import javax.inject.Inject
class CouponsListActivity : NewBaseActivity() {
    private var binding: MCouponlistBinding? = null
    private var discountlistviewmodel: DiscountListingViewModel? = null
    @Inject
    lateinit var factory: ViewModelFactory
    @Inject
    lateinit var discountlistAdapter: AllDiscountListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_couponlist, group, true)
        binding!!.content.visibility = View.VISIBLE
        showTittle(resources.getString(R.string.coupons))
        showBackButton()
        hidenavbottom()
        hidethemeselector()
        (application as MyApplication).mageNativeAppComponent!!.doCouponsListActivityInjection(this)
        discountlistviewmodel = ViewModelProvider(this, factory).get(DiscountListingViewModel::class.java)
        discountlistviewmodel!!.context = this
        discountlistviewmodel!!.FetchDiscountlistResponse(Urls(MyApplication.context).mid)
            .observe(this@CouponsListActivity) {
                this@CouponsListActivity.discountlistFetchResponse(
                    it
                )
            }
        shimmerStartGridCart()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_searchandcarts, menu)
        return true
    }
    private fun discountlistFetchResponse(it: com.shopify.apicall.ApiResponse) {
        try {
            val jsondata = JSONObject(it.data.toString())
            if (jsondata.has("data")) {
                shimmerStopGridCart()
                binding!!.couponlistrecycler.visibility = View.VISIBLE
                val discarr = jsondata.getJSONArray("data")
                discountlistAdapter = AllDiscountListAdapter()
                discountlistAdapter.setData(this, discarr)
                binding!!.couponlistrecycler.adapter = discountlistAdapter
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}