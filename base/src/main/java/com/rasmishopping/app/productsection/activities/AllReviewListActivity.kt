package com.rasmishopping.app.productsection.activities

import android.os.Bundle
import android.view.Menu
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.cartsection.activities.SubscribeCartList
import com.rasmishopping.app.databinding.ActivityAllReviewListBinding
import com.rasmishopping.app.productsection.adapters.AllReviewListAdapter
import com.rasmishopping.app.productsection.models.Review
import com.rasmishopping.app.productsection.models.ReviewModel
import com.rasmishopping.app.productsection.viewmodels.ProductViewModel
import com.rasmishopping.app.utils.ApiResponse
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.Urls
import com.rasmishopping.app.utils.ViewModelFactory
import info.androidhive.fontawesome.FontTextView
import org.json.JSONObject
import javax.inject.Inject

class AllReviewListActivity : NewBaseActivity() {
    @Inject
    lateinit var reviewAdapter: AllReviewListAdapter
    private var reviewBinding: ActivityAllReviewListBinding? = null
    private var reviewList: ArrayList<Review>? = ArrayList<Review>()
    private var product_id: String? = null
    private var page: Int = 1
    private var isLoading: Boolean = true
    @Inject
    lateinit var factory: ViewModelFactory
    private var model: ProductViewModel? = null


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
                    model?.getReviews(Urls(application as MyApplication).mid, product_id!!, page)
                        ?.observe(
                            this@AllReviewListActivity,
                            Observer { this@AllReviewListActivity.consumeReview(it) })
                }
            }
        }
    }

    private fun consumeReview(response: ApiResponse?) {
        try {
            val objec = JSONObject(response?.data.toString()).get("data")
            if (objec is JSONObject) {
                if (JSONObject(response?.data.toString()).getJSONObject("data").has("reviews")) {
                    if (JSONObject(response?.data.toString()).getJSONObject("data")
                            .getJSONArray("reviews").length() > 0
                    ) {
                        var reviewModel = Gson().fromJson<ReviewModel>(
                            response?.data.toString(),
                            ReviewModel::class.java
                        ) as ReviewModel
                        if (reviewModel.success!!) {
                            if (reviewModel.data?.reviews?.size!! > 0) {
                                reviewAdapter.reviwList?.addAll(reviewModel.data?.reviews!!)
                                reviewAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            isLoading = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        reviewBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.activity_all_review_list, group, true)
        (application as MyApplication).mageNativeAppComponent!!.doReviewListInjection(this)
        model = ViewModelProvider(this, factory).get(ProductViewModel::class.java)
        model!!.context = this
        showBackButton()
        hidenavbottom()
        hidethemeselector()
        if (intent.hasExtra("reviewList")) {
            var reviewModel = intent.getSerializableExtra("reviewList") as ReviewModel
            reviewList = reviewModel.data?.reviews as ArrayList<Review>?
            product_id = intent.getStringExtra("product_id")
            reviewAdapter.setData(reviewList,this)
            reviewBinding?.reviewList?.adapter = reviewAdapter
            showTittle(intent.getStringExtra("product_name") ?: "")
        }
        reviewBinding?.reviewList?.addOnScrollListener(recyclerViewOnScrollListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }
}