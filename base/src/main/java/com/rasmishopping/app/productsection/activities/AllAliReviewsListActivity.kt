package com.rasmishopping.app.productsection.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.databinding.ActivityAllReviewListBinding
import com.rasmishopping.app.productsection.adapters.AllReviewListAdapter
import com.rasmishopping.app.productsection.models.Review
import com.rasmishopping.app.productsection.viewmodels.ProductViewModel
import com.rasmishopping.app.utils.ApiResponse
import com.rasmishopping.app.utils.ViewModelFactory
import org.json.JSONObject
import javax.inject.Inject

class AllAliReviewsListActivity : NewBaseActivity() {
    private var binding: ActivityAllReviewListBinding? = null
    private var reviewList: ArrayList<Review>? = null
    private var productName: String? = null
    private var productID: String? = null
    private var ShopID: String? = null
    private var page: Int = 1
    private var isLoading: Boolean = true
    private val TAG = "AllAliReviewsListActivi"

    @Inject
    lateinit var reviewAdapter: AllReviewListAdapter

    @Inject
    lateinit var factory: ViewModelFactory
    private var model: ProductViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.activity_all_review_list,
            group,
            true
        )
        (application as MyApplication).mageNativeAppComponent!!.doAllAliReviewListInjection(this)
        model = ViewModelProvider(this, factory).get(ProductViewModel::class.java)
        model?.context = this
        showBackButton()
        model?.getAlireviewProduct?.observe(this, Observer { this.consumeAliReviews(it) })
        if (intent.hasExtra("reviewList") && intent.hasExtra("product_name") && intent.hasExtra("product_id") && intent.hasExtra(
                "shop_id"
            )
        ) {
            reviewList = intent.getSerializableExtra("reviewList") as ArrayList<Review>
            productName = intent.getStringExtra("product_name")
            productID = intent.getStringExtra("product_id")
            ShopID = intent.getStringExtra("shop_id")
            reviewAdapter.setData(reviewList,this)
            binding?.reviewList?.adapter = reviewAdapter
            showTittle(intent.getStringExtra("product_name") ?: "")
        }
        binding?.reviewList?.addOnScrollListener(recyclerViewOnScrollListener)
    }

    private fun consumeAliReviews(response: ApiResponse?) {
        var responseData = JSONObject(response?.data.toString())
        if (responseData.getBoolean("status")) {
            var reviews = responseData.getJSONObject("data").getJSONArray("data")
            reviewList = ArrayList<Review>()
            var review_model: Review? = null
            if (reviews.length() > 0) {
                for (i in 0 until reviews.length()) {
                    review_model = Review(
                        reviews.getJSONObject(i).getString("content"),
                        reviews.getJSONObject(i).getString("id"),
                        reviews.getJSONObject(i).getString("star"),
                        reviews.getJSONObject(i).getString("star"),
                        reviews.getJSONObject(i).getString("created_at"),
                        reviews.getJSONObject(i).getString("author"),
                        "","",ArrayList()
                        /*  reviews.getJSONObject(i).getString("title")*/
                    )
                    reviewList?.add(review_model)
                }
                reviewAdapter.reviwList?.addAll(reviewList!!)
                reviewAdapter.notifyDataSetChanged()
            } else {
                isLoading = false
            }
        }
    }

    private val recyclerViewOnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = recyclerView.layoutManager!!.childCount
            val totalItemCount = recyclerView.layoutManager!!.itemCount
            var firstVisibleItemPosition = 0
            if (recyclerView.layoutManager is LinearLayoutManager) {
                firstVisibleItemPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            } else if (recyclerView.layoutManager is GridLayoutManager) {
                firstVisibleItemPosition =
                    (recyclerView.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
            }
            if (!recyclerView.canScrollVertically(1)) {
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0
                    && totalItemCount >= reviewList!!.size && isLoading
                ) {
                    page++
                    model?.getAliReviewProduct(ShopID!!, productID!!, page)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }
}