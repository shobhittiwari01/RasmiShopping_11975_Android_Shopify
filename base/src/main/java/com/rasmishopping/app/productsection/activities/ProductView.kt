package com.rasmishopping.app.productsection.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.graphics.text.LineBreaker
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.animation.*
import android.webkit.WebSettings
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.backinstock.app.BackInStockViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.shopify.graphql.support.ID
import com.rasmishopping.app.FlitsDashboard.WishlistSection.FlitsWishlistViewModel
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.activities.Weblink
import com.rasmishopping.app.basesection.models.ListData
import com.rasmishopping.app.basesection.viewmodels.LeftMenuViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel.Companion.featuresModel
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.cartsection.activities.SubscribeCartList
import com.rasmishopping.app.checkoutsection.activities.CheckoutWeblink
import com.rasmishopping.app.customviews.MageNativeTextView
import com.rasmishopping.app.databinding.*
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.personalised.adapters.PersonalisedAdapter
import com.rasmishopping.app.personalised.viewmodels.PersonalisedViewModel
import com.rasmishopping.app.productsection.adapters.*
import com.rasmishopping.app.productsection.models.MediaModel
import com.rasmishopping.app.productsection.models.Picture
import com.rasmishopping.app.productsection.models.Review
import com.rasmishopping.app.productsection.models.ReviewModel
import com.rasmishopping.app.productsection.viewmodels.ProductViewModel
import com.rasmishopping.app.quickadd_section.activities.QuickAddActivity
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.*
import com.rasmishopping.app.wishlistsection.activities.WishList
import kotlinx.android.synthetic.main.m_productview.*
import kotlinx.android.synthetic.main.swatches_list.view.*
import kotlinx.android.synthetic.main.wishlist_animation.view.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

class ProductView : NewBaseActivity() {
    private var product_handle: String? = null
    private var productName: String? = null
    private var productsku: String? = null
    private var binding: MProductviewBinding? = null
    lateinit var subscriptionid: ID
    var subscribedvalue = "onetime"
    var group_plan_id: String? = null
    var offerName = "null"
    var availableqty: Int = 0
    @Inject
    lateinit var factory: ViewModelFactory
    private var model: ProductViewModel? = null
    private var reviewModel: ReviewModel? = null
    private val TAG = "ProductView"
    var productID = "noid"
    var group_data: MutableList<Storefront.SellingPlanGroupEdge>? = null
    var group_offer_data: MutableList<String>? = null
    var whishlistArray = JSONArray()
    var cartlistArray = JSONArray()
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var sizeChartUrl: String = ""
    private var singleVariant: Boolean = false
    private var variantEdge: Storefront.ProductVariant? = null
    private lateinit var quantitysection: LinearLayout
    private lateinit var qtyscroll: HorizontalScrollView
    private lateinit var closesheet: ImageView
    @Inject
    lateinit var reviewAdapter: ReviewListAdapter
    lateinit var sellingplans_adapter: SellingPlanGroupAdapter
    lateinit var offerplans_adapter: SellingGroupOfferAdapter
    private var data: ListData? = null
    private var personamodel: PersonalisedViewModel? = null
    private var inStock: Boolean = true
    //private var quantityselected = "notselectedyet"
    lateinit var slider: ImagSlider
    private var variantValidation: MutableMap<String, String> = mutableMapOf()
    private var external_id: String? = null
    private var judgeme_productid: String? = null
    private var AliProductId: String? = null
    private var AliShopId: String? = null
    private var reviewList: ArrayList<Review>? = null
    private var mediaList = mutableListOf<MediaModel>()
    protected lateinit var leftmenu: LeftMenuViewModel
    lateinit var mBottomSheetBehaviour: BottomSheetBehavior<ConstraintLayout>
    @Inject
    lateinit var arImagesAdapter: ArImagesAdapter
    @Inject
    lateinit var customadapter: CustomAdapters
    @Inject
    lateinit var personalisedadapter: PersonalisedAdapter
    private var backinstockviewmodel: BackInStockViewModel? = null
    private lateinit var  product:Storefront.Product
    companion object {
        var productmodel: ProductViewModel? = null
        @SuppressLint("StaticFieldLeak")
        var model: ProductViewModel? = null
        @SuppressLint("StaticFieldLeak")
        var flistwishmodel: FlitsWishlistViewModel? = null
        var WishlistVariantID = "noid"
        var variantId: ID? = null
        var VariantSellingID: HashMap<ID, ArrayList<ID>> = HashMap<ID, ArrayList<ID>>()
        var totalVariant: Int? = null
        lateinit var adapter: VariantAdapter
        var variant_data: MutableList<String>? = mutableListOf()
        var selectedVariants: MutableMap<String, String> = mutableMapOf()
        var selectedvariant_pair: MutableMap<String, String> = mutableMapOf()
        var varproductedge: Storefront.Product? = null
        var notavailablecombination = HashMap<String, ArrayList<String>>()
        var variant_pair: MutableMap<String, String> = mutableMapOf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Constant.previous = null
        Constant.current = null
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_productview, group, true)
        binding?.features = featuresModel
        showBackButton()
        hidenavbottom()
        hidethemeselector()
        showShadow()
        shimmerStartGridProductView()
        variant_data?.clear()
        selectedVariants.clear()
        val bottomSheetParent = findViewById<ConstraintLayout>(R.id.bottom_sheet_parent)
        mBottomSheetBehaviour = BottomSheetBehavior.from(bottomSheetParent)
        quantitysection = bottomSheetParent.findViewById<LinearLayout>(R.id.quantitysection)
        qtyscroll = bottomSheetParent.findViewById<HorizontalScrollView>(R.id.qtyscroll)
        closesheet = bottomSheetParent.findViewById<ImageView>(R.id.closesheet)
        leftmenu = ViewModelProvider(this, viewModelFactory).get(LeftMenuViewModel::class.java)
        backinstockviewmodel = ViewModelProvider(this, viewModelFactory).get(BackInStockViewModel::class.java)
        backinstockviewmodel!!.context = this
        (application as MyApplication).mageNativeAppComponent!!.doProductViewInjection(this)
        model = ViewModelProvider(this, factory).get(ProductViewModel::class.java)
        model!!.context = this
        productmodel = ViewModelProvider(this, factory).get(ProductViewModel::class.java)
        productmodel!!.context = this
        firebaseAnalytics = Firebase.analytics
        flistwishmodel = ViewModelProvider(this, factory).get(FlitsWishlistViewModel::class.java)
        flistwishmodel!!.context = this
        model?.createreviewResponse?.observe(this, Observer { this.createReview(it) })
        personamodel = ViewModelProvider(this, factory).get(PersonalisedViewModel::class.java)
        personamodel?.activity = this
        try {
            binding?.cartsection?.setBackgroundColor(Color.parseColor(themeColor))
            binding?.cartsection?.background=ContextCompat.getDrawable(HomePageViewModel.themedContext,R.drawable.newcartround)
            binding?.cartsection?.getBackground()?.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_OVER)
        //            binding?.buynow?.setTextColor(resources.getColor(R.color.black))
//            binding?.buynowsection?.background = (resources.getDrawable(R.drawable.round))
//            binding?.buynowsection?.setBackgroundColor(resources.getColor(R.color.white))
        } catch (e: Exception) {

        }
        if (intent.getStringExtra("handle") != null) {
            model!!.handle = intent.getStringExtra("handle")!!
            Log.i("DeveloperHandle", "" + model!!.handle)
        }
        if (intent.getStringExtra("ID") != null) {
            Log.i("DeveloperID", "" + intent.getStringExtra("ID"))
            model!!.id = intent.getStringExtra("ID")!!
            if (model!!.id.contains("/")) {
                val pid = model!!.id.split("/")
                productID = pid[pid.size - 1]
            } else productID = model!!.id
            Log.i("PID2", "" + productID)
        }
        if (intent.getStringExtra("Variant_ID") != null) {
            WishlistVariantID = intent.getStringExtra("Variant_ID")!!
        }

        if (featuresModel.productReview!!) {
            model?.getReviewBadges(Urls(application as MyApplication).mid, (productID))
                ?.observe(this, Observer { this.consumeBadges(it) })
            model?.getReviews(Urls(application as MyApplication).mid, productID, 1)
                ?.observe(this, Observer { this.consumeReview(it) })
        }
        if (featuresModel.sizeChartVisibility) {
            model?.sizeChartVisibility?.observe(
                this,
                Observer { this.consumeSizeChartVisibility(it) })
            model?.sizeChartUrl?.observe(this, Observer { this.consumeSizeChartURL(it) })
        }
        if (featuresModel.aliReviews) {
            model?.getAlireviewInstallStatus?.observe(
                this,
                Observer { this.consumeAliReviewStatus(it) })
            model?.getAlireviewProduct?.observe(this, Observer { this.consumeAliReviews(it) })
            model?.getAliReviewStatus()
        }
        data = ListData()
        if (featuresModel.ai_product_reccomendaton) {
            model!!.getApiResponse()
                .observe(this, Observer<ApiResponse> { this.consumeResponse(it) })
        }
        if (intent.getSerializableExtra("product") != null) {
            setProductData(intent.getSerializableExtra("product") as Storefront.Product)
        } else {
            model!!.Response().observe(this, Observer<GraphQLResponse> { this.consumeResponse(it) })
        }
        model!!.recommendedLiveData.observe(this, Observer { this.consumeRecommended(it) })
        model!!.shopifyRecommended()
        if (featuresModel.judgemeProductReview) {
            model?.getjudgeMeProductID?.observe(this, Observer { this.consumeJudgeMeProductID(it) })
            model?.getjudgeMeReviewCount?.observe(
                this,
                Observer { this.consumeJudgeMeReviewCount(it) })
            model?.getjudgeMeReviewIndex?.observe(this, Observer { this.consumeJudgeMeReview(it) })
        }
        binding?.yotpoWriteReviewBut?.setOnClickListener {
            if (yotporeviewsection.visibility == View.VISIBLE) {
                yotporeviewsection.visibility = View.GONE
            } else {
                yotporeviewsection.visibility = View.VISIBLE
            }
        }
        binding?.yotpoSubmitreview?.setOnClickListener {
            if (binding!!.yotpoName.text!!.toString().isEmpty()) {
                binding!!.yotpoName.error = resources.getString(R.string.empty)
                binding!!.yotpoName.requestFocus()
            } else if (binding!!.yotpoEmail.text!!.toString().isEmpty()) {
                binding!!.yotpoEmail.error = resources.getString(R.string.empty)
                binding!!.yotpoEmail.requestFocus()
            } else if (binding!!.yotpoReviewtitle.text!!.toString().isEmpty()) {
                binding!!.yotpoReviewtitle.error = resources.getString(R.string.empty)
                binding!!.yotpoReviewtitle.requestFocus()
            } else if (binding!!.yotpoReviewbody.text!!.toString().isEmpty()) {
                binding!!.yotpoReviewbody.error = resources.getString(R.string.empty)
                binding!!.yotpoReviewbody.requestFocus()
            } else {
                if (leftmenu.isLoggedIn) {
                    submityptporeview()
                } else {
                    val alertDialog: AlertDialog = AlertDialog.Builder(this@ProductView).create()
                    alertDialog.setTitle("NOTE!")
                    alertDialog.setMessage("Please create an account in the app to leave a review.")
                    alertDialog.setButton(
                        AlertDialog.BUTTON_NEUTRAL,
                        "OK"
                    ) { dialog, which -> dialog.dismiss() }
                    alertDialog.show()
                }
            }
        }
        if (binding?.addtocart!!.text.equals(getString(R.string.out_of_stock))) {
            binding?.addtocart?.setTextColor(resources.getColor(R.color.outofstockred))
            binding?.cartsection?.background=ContextCompat.getDrawable(HomePageViewModel.themedContext,R.drawable.newcartround)
            binding?.cartsection?.setBackgroundColor(resources.getColor(R.color.outofstock_background))
            binding?.cartsection?.getBackground()?.setColorFilter(resources.getColor(R.color.outofstock_background), PorterDuff.Mode.SRC_OVER)
        } else {
            binding?.addtocart?.setTextColor(Color.parseColor(NewBaseActivity.textColor))
//            binding?.carticon?.setColorFilter(Color.parseColor(NewBaseActivity.textColor))
        }

        binding?.threesixtyview?.setOnClickListener {
            arCoreButtonClicked()
        }
        model!!.message.observe(this, {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })
        model!!.data.observe(this, {
            buyNowCheckout(it)
        })
        onBackPressedDispatcher.addCallback(
            this /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    VariantSellingID = HashMap<ID, ArrayList<ID>>()
                    ProductView.variantId = null
                    finish()
                }
            })
    }

    private fun buyNowCheckout(checkout: Storefront.Checkout?) {
        val alertDialog = SweetAlertDialog(this@ProductView, SweetAlertDialog.WARNING_TYPE)
        var customeview = PopConfirmationBinding.inflate(LayoutInflater.from(this@ProductView))
        customeview.textView.text = getString(R.string.confirmation)
        customeview.textView2.text = getString(R.string.want_to_apply_discount_code)
        alertDialog.hideConfirmButton()
        customeview.okDialog.setOnClickListener {
            customeview.okDialog.isClickable = false
            customeview.textView.text = getString(R.string.done)
            customeview.textView2.text = productName + " " + getString(R.string.add_cart)
            alertDialog.showCancelButton(false)
            alertDialog.setConfirmClickListener(null)
            alertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
            model!!.addToCart(variantId.toString(), 1, "", "")
            invalidateOptionsMenu()
            alertDialog.cancel()
            val intent = Intent(this, CartList::class.java)
            startActivity(intent)
            Constant.activityTransition(this)
        }
        customeview.noDialog.setOnClickListener {
            customeview.noDialog.isClickable = false
            alertDialog.cancel()
            val intent = Intent(this, CheckoutWeblink::class.java)
            intent.putExtra("link", checkout!!.webUrl)
            intent.putExtra("id", checkout.id.toString())
            startActivity(intent)
            Constant.activityTransition(this)
        }
        alertDialog.setCustomView(customeview.root)
        alertDialog.show()
    }

    private fun consumeAliReviews(response: ApiResponse?) {
        var responseData = JSONObject(response?.data.toString())
        if (responseData.getBoolean("status")) {
            binding?.aliReviewSection?.visibility = View.VISIBLE
            binding?.reviewCard?.visibility = View.VISIBLE
            binding?.ratingvalue?.text = responseData.getString("avg")
            binding?.totalReview?.text = responseData.getString("total_review")
            var reviews = responseData.getJSONObject("data").getJSONArray("data")
            reviewList = ArrayList<Review>()
            var review_model: Review? = null
            for (i in 0 until reviews.length()) {
                review_model = Review(
                    reviews.getJSONObject(i).getString("content"),
                    reviews.getJSONObject(i).getString("id"),
                    reviews.getJSONObject(i).getString("star"),
                    reviews.getJSONObject(i).getString("star"),
                    reviews.getJSONObject(i).getString("created_at"),
                    reviews.getJSONObject(i).getString("author"),
                    "", "", ArrayList()
                    /*  reviews.getJSONObject(i).getString("title")*/
                )
                reviewList?.add(review_model)
            }
            if (reviewList?.size!! > 0) {
                binding?.aliNoReviews?.visibility = View.GONE
                binding?.aliReviewList?.visibility = View.VISIBLE
                binding?.aliViewAllBut?.visibility = View.VISIBLE
                binding?.aliRateProductBut?.visibility = View.VISIBLE
                reviewAdapter = ReviewListAdapter()
                reviewAdapter.setData(reviewList, this)
                binding?.aliReviewList?.adapter = reviewAdapter
            } else {
                binding?.aliNoReviews?.visibility = View.VISIBLE
                binding?.aliReviewList?.visibility = View.GONE
                binding?.aliRateProductBut?.visibility = View.VISIBLE
                binding?.aliViewAllBut?.visibility = View.GONE
            }
        } else {
            binding?.aliReviewSection?.visibility = View.GONE
            binding?.reviewCard?.visibility = View.GONE
            binding?.aliNoReviews?.visibility = View.GONE
            binding?.aliReviewList?.visibility = View.GONE
            binding?.aliRateProductBut?.visibility = View.GONE
            binding?.aliViewAllBut?.visibility = View.GONE
        }
    }

    private fun consumeAliReviewStatus(response: ApiResponse?) {
        Log.d(TAG, "consumeAliReviewStatus: " + response?.data)
        try {
            var responseData = JSONObject(response?.data.toString())
            if (responseData.get("status") is String && responseData.get("status")
                    .equals("error")
            ) {
                binding?.aliReviewSection?.visibility = View.GONE
            } else {
                if (responseData.getBoolean("status")) {
                    AliProductId = (productID)
                    AliShopId = responseData.getJSONObject("result").getString("shop_id")
                    model?.getAliReviewProduct(
                        responseData.getJSONObject("result").getString("shop_id"),
                        (productID),
                        1
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun consumeJudgeMeReview(response: ApiResponse?) {
        var responseData = JSONObject(response?.data.toString())
        var reviews = responseData.getJSONArray("reviews")
        Log.d("javed", "consumeJudgeMeReview: " + reviews)
        reviewList = ArrayList<Review>()
        var review_model: Review? = null
        var picture: Picture? = null
        var picture_array: JSONArray? = null
        var urls_obj: JSONObject? = null
        for (i in 0 until reviews.length()) {
            var picture_list: MutableList<Picture> = mutableListOf()
            picture_array = reviews.getJSONObject(i).getJSONArray("pictures")
            if (picture_array.length() > 0) {
                for (j in 0 until picture_array.length()) {
                    urls_obj = picture_array.getJSONObject(j).getJSONObject("urls")
                    picture = Picture(
                        picture_array.getJSONObject(j).getBoolean("hidden"),
                        com.rasmishopping.app.productsection.models.Urls(
                            urls_obj.getString("compact"),
                            urls_obj.getString("huge"),
                            urls_obj.getString("original"),
                            urls_obj.getString("small")
                        )
                    )
                    picture_list.add(picture)
                }
            }
            review_model = Review(
                reviews.getJSONObject(i).getString("body"),
                reviews.getJSONObject(i).getString("id"),
                reviews.getJSONObject(i).getString("rating"),
                reviews.getJSONObject(i).getString("rating"),
                reviews.getJSONObject(i).getString("created_at"),
                reviews.getJSONObject(i).getJSONObject("reviewer").getString("name"),
                reviews.getJSONObject(i).getString("title"),
                reviews.getJSONObject(i).getString("curated"),
                picture_list
            )
            if (reviews.getJSONObject(i).getString("curated").equals("ok")) {
                reviewList?.add(review_model)
            }
        }

        if (reviewList?.size!! > 0) {
            binding?.judgemeNoReviews?.visibility = View.GONE
            binding?.judgemeReviewList?.visibility = View.VISIBLE
            binding?.judgemeViewAllBut?.visibility = View.VISIBLE
            binding?.judgemeRateProductBut?.visibility = View.VISIBLE
            reviewAdapter = ReviewListAdapter()
            reviewAdapter.setData(reviewList, this)
            binding?.judgemeReviewList?.adapter = reviewAdapter
        }
    }

    private fun consumeJudgeMeReviewCount(response: ApiResponse?) {
        binding?.totalReview?.text = JSONObject(response?.data.toString()).getString("count") + " R"
        binding?.reviewCard?.visibility = View.VISIBLE
        binding?.ratingvalue?.visibility = View.GONE
    }

    private fun consumeJudgeMeProductID(response: ApiResponse?) {
        Log.d(TAG, "consumeJudgeMeProductID: " + response?.data)
        if (response?.data != null) {
            var responseData = JSONObject(response.data.toString())
            if (responseData.has("product")) {
                var product = responseData.getJSONObject("product")
                model?.judgemeReviewCount(
                    product.getString("id"),
                    Urls.JUDGEME_APITOKEN,
                    Urls(application as MyApplication).shopdomain
                )
                model?.judgemeReviewIndex(
                    product.getString("id"),
                    Urls.JUDGEME_APITOKEN,
                    Urls(application as MyApplication).shopdomain,
                    5,
                    1
                )
                external_id = product.getString("external_id")
                judgeme_productid = product.getString("id")
            }
        }
    }

    private fun consumeRecommended(reponse: GraphQLResponse?) {
        when (reponse?.status) {
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
                    Toast.makeText(this, "" + errormessage, Toast.LENGTH_SHORT).show()
                } else {
                    if (result.data!!.productRecommendations != null) {
                        var recommendedList =
                            result.data!!.productRecommendations as ArrayList<Storefront.Product>?
                        if (recommendedList?.size!! > 0) {
                            Log.d(TAG, "consumeRecommended: " + recommendedList.size)
                            binding!!.shopifyrecommendedSection.visibility = View.VISIBLE
                            shopifyrecommended_section.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
                            shopifyrecommended_title.setTextColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.normalgrey1text))
                            setLayout(binding!!.shopifyrecommendedList, "horizontal")
                            personalisedadapter = PersonalisedAdapter()
                            if (!personalisedadapter.hasObservers()) {
                                personalisedadapter.setHasStableIds(true)
                            }
                            var jsonobject: JSONObject = JSONObject();
                            jsonobject.put("item_shape", "rounded")
                            jsonobject.put("item_text_alignment", "left")
                            jsonobject.put("item_border", "0")
                            jsonobject.put("item_title", "1")
                            jsonobject.put("item_price", "1")
                            jsonobject.put("item_compare_at_price", "1")
                            personalisedadapter.setData(
                                recommendedList,
                                this,
                                jsonobject,
                                personamodel?.repository!!
                            )
                            binding!!.shopifyrecommendedList.adapter = personalisedadapter
                        }
                    }
                }
            }
            else -> {

            }
        }
    }

    private fun consumeSizeChartURL(it: String?) {
        sizeChartUrl = it!!
    }

    private fun consumeSizeChartVisibility(it: Boolean?) {
        if (it!!) {
            binding?.sizeChartSection?.visibility = View.VISIBLE
        } else {
            binding?.sizeChartSection?.visibility = View.GONE
        }
    }

    private fun createReview(response: ApiResponse?) {
        if (response?.data != null) {
            var data = JSONObject(response.data.toString())
            if (data.getBoolean("success")) {
                Toast.makeText(this, getString(R.string.review_submitted), Toast.LENGTH_SHORT)
                    .show()
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    model?.getProductReviews(
                        Urls(application as MyApplication).mid,
                        (productID),
                        1
                    )
                    model?.getbadgeReviews(
                        Urls(application as MyApplication).mid,
                        (productID)
                    )
                }
            }
        }
    }

    private fun consumeBadges(response: ApiResponse?) {
        Log.d("javed", "consumeBadges: " + response?.data)
        if (response?.data != null) {
            var data = JSONObject(response.data.toString()).getJSONObject("data")
            binding?.ratingvalue?.text =
                data.getJSONObject((productID)).getString("total-rating").substring(0, 3)
            binding?.totalReview?.text = data.getJSONObject((productID)).getString("total-reviews")
            binding?.reviewCard?.visibility = View.VISIBLE
        }
    }

    private fun consumeReview(response: ApiResponse?) {
        if (response?.data != null) {
            try {
                Log.d(TAG, "consumeReview: " + JSONObject(response.data.toString()))
                val obj = JSONObject(response.data.toString()).get("data")
                if (obj is JSONObject) {
                    if (JSONObject(response.data.toString()).getJSONObject("data").has("reviews")) {
                        reviewModel = Gson().fromJson<ReviewModel>(
                            response.data.toString(),
                            ReviewModel::class.java
                        ) as ReviewModel
                        if (reviewModel?.success!!) {
                            if (reviewModel?.data?.reviews?.size!! > 0) {
                                binding?.noReviews?.visibility = View.GONE
                                binding?.reviewList?.visibility = View.VISIBLE
                                binding?.viewAllBut?.visibility = View.VISIBLE
                                binding?.rateProductBut?.visibility = View.VISIBLE
                                reviewAdapter.setData(reviewModel?.data?.reviews, this)
                                binding?.reviewList?.adapter = reviewAdapter
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                binding?.noReviews?.visibility = View.VISIBLE
                binding?.reviewList?.visibility = View.GONE
                binding?.viewAllBut?.visibility = View.GONE
                binding?.rateProductBut?.visibility = View.VISIBLE
            }
        }
    }

    fun getBase64Decode(id: String?): String? {
        var data = id
        val datavalue = data!!.split("/".toRegex()).toTypedArray()
        val valueid = datavalue[datavalue.size - 1]
        val datavalue2 = valueid.split("key".toRegex()).toTypedArray()
        data = datavalue2[0]
        return data
    }

    private fun consumeResponse(reponse: GraphQLResponse) {
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
                    Toast.makeText(this, "" + errormessage, Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        var productedge: Storefront.Product? = null
                        if (!model!!.handle.isEmpty()) {
                            productedge = result.data!!.product
                        }
                        if (!model!!.id.isEmpty()) {
                            productedge = result.data!!.node as Storefront.Product
                        }
                        Log.i("MageNative", "Product_id" + productedge!!.id.toString())
                        setProductData(productedge)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            Status.ERROR -> Toast.makeText(this, reponse.error!!.error.message, Toast.LENGTH_SHORT)
                .show()
            else -> {
            }
        }
    }

    private fun consumeResponse(reponse: ApiResponse) {
        when (reponse.status) {
            Status.SUCCESS -> setPersonalisedData(reponse.data!!)
            Status.ERROR -> {
                reponse.error!!.printStackTrace()
                //Toast.makeText(this, resources.getString(R.string.errorString), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setPersonalisedData(data: JsonElement) {
        try {
            val jsondata = JSONObject(data.toString())
            if (jsondata.has("query1")) {
                binding!!.personalisedsection.visibility = View.VISIBLE
                binding!!.personalisedsection.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
                binding!!.personalisedyext.setTextColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.normalgrey1text))
                setLayout(binding!!.personalised, "horizontal")
                personamodel!!.setPersonalisedData(
                    jsondata.getJSONObject("query1").getJSONArray("products"),
                    personalisedadapter,
                    binding!!.personalised
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n", "LogNotTimber", "SetJavaScriptEnabled")
    private fun setProductData(productedge: Storefront.Product?) {
        try {
            checkChatGPT(productedge)
            product=productedge!!
            sellingplans_adapter = SellingPlanGroupAdapter()
            SubscriptionProductData(productedge)
            (binding!!.images.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                "H,3:4"
            varproductedge = productedge
//            cartsection.background=ContextCompat.getDrawable(HomePageViewModel.themedContext,R.drawable.round)
            try {
//                var buynowback = binding!!.buynowsection.background as GradientDrawable
//                buynowback.setStroke(2, Color.parseColor(themeColor))
//                buynowback.setColor(Color.parseColor(themeColor))
//                buynowsection.background = buynowback
//                binding?.buynowsection?.background = (resources.getDrawable(R.drawable.round))
            } catch (e: Exception) {
//                binding?.buynow?.setTextColor(resources.getColor(R.color.black))
//                binding?.buynowsection?.setBackgroundColor(resources.getColor(R.color.white))
//                binding?.buynowsection?.background = (resources.getDrawable(R.drawable.round))
            }
//            binding?.buynow?.setTextColor(resources.getColor(R.color.black))
            var mediaModel: MediaModel? = null
            for (i in 0..productedge!!.media.edges.size - 1) {
                var a: String = productedge.media.edges.get(i).node.graphQlTypeName
                if (a.equals("Model3d")) {
                    var d = productedge.media.edges.get(i).node as Storefront.Model3d
                    if (d.sources.get(0).url.contains(".glb")) {
                        data!!.arimage = d.sources.get(0).url
                        mediaModel = MediaModel(
                            d.graphQlTypeName,
                            d.previewImage.url,
                            d.sources.get(0).url
                        )
                        mediaList.add(mediaModel)
                        if (featuresModel.ardumented_reality) {
                            binding!!.aricon.visibility = View.VISIBLE
                        } else {
                            binding!!.aricon.visibility = View.GONE
                        }
                    }
                } else if (a.equals("Video")) {
                    val video = productedge.media.edges.get(i).node as Storefront.Video
                    mediaModel = MediaModel(
                        video.graphQlTypeName,
                        video.previewImage.url,
                        video.sources.get(0).url
                    )
                    mediaList.add(mediaModel)
                } else if (a.equals("ExternalVideo")) {
                    val externalVideo =
                        productedge.media.edges.get(i).node as Storefront.ExternalVideo
                    mediaModel = MediaModel(
                        externalVideo.graphQlTypeName,
                        externalVideo.previewImage.url,
                        externalVideo.embedUrl
                    )
                    mediaList.add(mediaModel)
                } else if (a.equals("MediaImage")) {
                    var mediaImage = productedge.media.edges.get(i).node as Storefront.MediaImage
                    mediaModel = MediaModel(
                        mediaImage.graphQlTypeName,
                        mediaImage.previewImage.url,
                        ""
                    )
                    mediaList.add(mediaModel)
                }
            }
            Log.d(TAG, "setProductData: " + mediaList)
            Log.d(TAG, "setProductData: " + productedge.handle)
            product_handle = productedge.handle
            if (featuresModel.judgemeProductReview) {
                model?.judgemeProductID(
                    Urls.JUDGEME_GETPRODUCTID + productedge.handle,
                    productedge.handle,
                    Urls.JUDGEME_APITOKEN,
                    Urls(application as MyApplication).shopdomain
                )
            }
            Log.d(TAG, "setProductData: " + productedge.id)
            var tags_data: StringBuilder = StringBuilder()
            if (productedge.tags.size > 0) {
                productedge.tags.forEach {
                    tags_data.append("$it,")
                }
                Log.d(TAG, "setProductData: " + tags_data.substring(0, tags_data.length - 1))
            } else {
                tags_data.append("")
            }
            if (featuresModel.sizeChartVisibility) {
                var collections: String? = null
                if (productedge.collections != null) {
                    if(productedge.collections?.edges==null){
                        if (productedge.collections?.nodes?.size!! > 0) {
                            var buffer = StringBuffer()
                            for (i in 0 until productedge.collections.nodes.size) {
                                buffer.append((productedge.collections.nodes.get(i).id.toString()))
                                    .append(",")
                            }
                            collections = buffer.substring(0, buffer.length - 1)
                        } else {
                            collections = (productedge.collections.nodes.get(0).id.toString())
                        }
                    }else{
                        if (productedge.collections?.edges?.size!! > 0) {
                            var buffer = StringBuffer()
                            for (i in 0 until productedge.collections.edges.size) {
                                buffer.append((productedge.collections.edges.get(i).node.id.toString()))
                                    .append(",")
                            }
                            collections = buffer.substring(0, buffer.length - 1)
                        } else {
                            collections = (productedge.collections.edges.get(0).node.id.toString())
                        }
                    }
                }

                model!!.getSizeChart(
                    Urls(application as MyApplication).shopdomain,
                    "magenative",
                    (productID),
                    tags_data.toString(),
                    productedge.vendor,
                    collections
                )
            }
            if (featuresModel.ai_product_reccomendaton) {
                model!!.getRecommendations(productedge.id.toString())
            }
            getString(R.string.avaibale_qty) + " " + productedge.totalInventory
            availableqty = productedge.totalInventory
            val variant = productedge.variants.edges[0].node
            /******************************* Local delivery and pickup work *************************************/
            if (featuresModel.localpickupEnable) {
                val alledges = variant.storeAvailability.edges
                Log.d(TAG, alledges.size.toString())
                if (alledges.size > 2) {
                    binding?.checkstoretext?.visibility = View.VISIBLE
                } else {
                    binding?.checkstoretext?.visibility = View.GONE
                }
                if (variant.storeAvailability.edges[0].node.available.toString().equals("true")) {
                    binding?.card?.visibility = View.VISIBLE
                    binding?.heading?.text = getString(R.string.pickupavailable)
                    binding?.checks?.setImageResource(R.drawable.checkmark)
                    binding?.addfirst?.text =
                        variant.storeAvailability.edges[0].node.location.address.city
                    binding?.addsecond?.text =
                        variant.storeAvailability.edges[0].node.location.address.address2 + " " + variant.storeAvailability.edges[0].node.location.address.address1 + " " +
                                variant.storeAvailability.edges[0].node.location.address.province + " " + variant.storeAvailability.edges[0].node.location.address.city + " " + variant.storeAvailability.edges[0].node.location.address.zip
                    binding?.pickuptime?.text = variant.storeAvailability.edges[0].node.pickUpTime
                    binding?.phonenumber?.text =
                        variant.storeAvailability.edges[0].node.location.address.phone
                } else if (variant.storeAvailability.edges[0].node.available.toString()
                        .equals("false")
                ) {
                    binding?.card?.visibility = View.VISIBLE
                    binding?.heading?.text = getString(R.string.pickupavailablenot)
                    binding?.checks?.setImageResource(R.drawable.cross)
                    binding?.addfirst?.text =
                        variant.storeAvailability.edges[0].node.location.address.city
                    binding?.addsecond?.text =
                        variant.storeAvailability.edges[0].node.location.address.address2 + " " + variant.storeAvailability.edges[0].node.location.address.address1 + " " +
                                variant.storeAvailability.edges[0].node.location.address.province + " " + variant.storeAvailability.edges[0].node.location.address.city + " " + variant.storeAvailability.edges[0].node.location.address.zip
                    binding?.pickuptime?.text = variant.storeAvailability.edges[0].node.pickUpTime
                    binding?.phonenumber?.text =
                        variant.storeAvailability.edges[0].node.location.address.phone
                }
                binding?.storerecycler?.setHasFixedSize(true)
                binding?.storerecycler?.layoutManager = LinearLayoutManager(
                    this,
                    LinearLayoutManager.VERTICAL,
                    false
                )
                binding?.storerecycler?.isNestedScrollingEnabled = false
                customadapter.setData(alledges, this)
                binding?.storerecycler!!.adapter = customadapter
                binding?.checkstoretext?.setOnClickListener {
                    if (storerecycler.visibility == View.VISIBLE) {
                        storerecycler.visibility = View.GONE
                    } else {
                        storerecycler.visibility = View.VISIBLE
                    }
                }
            }
            /***************************************************************************************************/
            slider = ImagSlider(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
            if (mediaList.size > 0) {
                slider.setData(mediaList)
            }
            data!!.product = productedge
            binding!!.images.adapter = slider
            binding!!.indicator.setViewPager(binding!!.images)
            data!!.textdata = productedge.title
            productName = productedge.title
            productsku = productedge.variants.edges[0].node.sku
            showTittle(" ")
            if (productedge.options.size > 3) {
                showVariantPage()
                variantId = productedge.variants.edges[0].node.id
            } else {
                if (featuresModel.Spinner_Varient)
                    filterSpinnerOptionList(
                        productedge.options,
                        productedge.variants.edges,
                        productedge
                    )
                else
                    filterOptionList(productedge.options, productedge.variants.edges, productedge)
            }
            var detailback=detailssection.background as GradientDrawable
            detailback.setStroke(2,Color.parseColor("#D1D1D1"))
            detailback.setColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
            detailssection.background=detailback
            binding?.description!!.text = productedge.description
            if(productedge.description.isNullOrBlank())
            {
               detailssection.visibility=View.GONE
                desc_section.visibility=View.GONE

            }
            else
            {
                detailssection.visibility=View.VISIBLE
                desc_section.visibility=View.VISIBLE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding?.description?.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
            }
            var textcolor:String="#000000"
            if(!HomePageViewModel.isLightModeOn()){
                textcolor="#FFFFFF"
            }
            var background:String="#FFFFFF"
            if(!HomePageViewModel.isLightModeOn()){
                background="#000000"
            }
            if(MagePrefs.getLanguage()!=null)
            {
                if (MagePrefs.getLanguage()!!.uppercase(Locale.getDefault()) == "AR") {
                    val pish = "<head><style>@font-face {font-family: 'Poppins';src: url('file:///android_asset/fonts/popnormal.ttf');}</style></head>"
                    var desc = "<html>" + pish + "<body style='font-family: Poppins;color: ${textcolor};background:${background}'> <div dir='rtl'>" + productedge.descriptionHtml.replace("<table","<table width=\"100%\"") + "</div></body></html>"
                    binding?.descriptionwebview?.loadDataWithBaseURL(null,desc, "text/html", "utf-8", null)
                }

                else {
                    val pish = "<head><style>@font-face {font-family: 'Poppins';src: url('file:///android_asset/fonts/popnormal.ttf');}</style></head>"
                    var desc = "<html>" + pish + "<body style='font-family: Poppins;color: ${textcolor};background:${background}'>" + productedge.descriptionHtml.replace("<table","<table width=\"100%\"") + "</body></html>"
                    binding?.descriptionwebview?.loadDataWithBaseURL(null, desc, "text/html", "utf-8", null)
                }
            }
            else
            {
                if (MagePrefs.getLanguage()!!.uppercase(Locale.getDefault()) == "AR") {
                    val pish = "<head><style>@font-face {font-family: 'Poppins';src: url('file:///android_asset/fonts/popnormal.ttf');}</style></head>"
                    var desc = "<html>" + pish + "<body style='font-family: Poppins;color: ${textcolor};background:${background}'> <div dir='rtl'>" + productedge.descriptionHtml.replace("<table","<table width=\"100%\"") + "</div></body></html>"
                    binding?.descriptionwebview?.loadDataWithBaseURL(null,desc, "text/html", "utf-8", null)
                }

                else {
                    val pish = "<head><style>@font-face {font-family: 'Poppins';src: url('file:///android_asset/fonts/popnormal.ttf');}</style></head>"
                    var desc = "<html>" + pish + "<body style='font-family: Poppins;color: ${textcolor};background:${background}'>" + productedge.descriptionHtml.replace("<table","<table width=\"100%\"") + "</body></html>"
                    binding?.descriptionwebview?.loadDataWithBaseURL(null, desc, "text/html", "utf-8", null)
                }
            }

            binding?.descriptionwebview?.settings?.loadWithOverviewMode = true
            binding?.descriptionwebview?.settings?.useWideViewPort = true
            binding?.descriptionwebview?.settings?.javaScriptEnabled = true
            val webSettings: WebSettings? = binding?.descriptionwebview?.settings
            webSettings?.defaultFontSize = 40
            var contentViewArray = JSONArray()
            var cartlistData = JSONObject()
            cartlistData.put("id", productedge.id.toString())
            cartlistData.put("quantity", 1)
            contentViewArray.put(cartlistData.toString())
            Constant.logViewContentEvent(
                "product", contentViewArray.toString(),
                productedge.id.toString(),
                productedge.variants.edges.get(0).node.price.currencyCode.toString(),
                productedge.variants.edges.get(0).node.price.amount.toDouble(),
                this
            )
            Constant.FirebaseEvent_ViewItem(MagePrefs.getCurrency()!!.toString(),productedge?.variants.edges.get(0).node.price.amount,productedge.title.toString(),productedge!!.handle.toString())
            var descriptionnormal = false
            binding?.parentsection!!.setOnClickListener {
                if (descriptionnormal) {
                    if (binding?.description!!.isVisible) {
                        binding?.description!!.visibility = View.GONE
                        expand_collapse.setImageResource(R.drawable.ic_forward)
                    } else {
                        binding?.description!!.visibility = View.VISIBLE
                        expand_collapse.setImageResource(R.drawable.ic_up)
                    }
                } else {
                    if (binding?.descriptionwebview!!.isVisible) {
                        binding?.descriptionwebview!!.visibility = View.GONE
                        expand_collapse.setImageResource(R.drawable.ic_forward)
                    } else {
                        binding?.descriptionwebview!!.visibility = View.VISIBLE
                        expand_collapse.setImageResource(R.drawable.ic_up)
                    }
                }
            }
            binding!!.shareicon.isVisible = featuresModel.product_share
            wishlistsection.isVisible = featuresModel.in_app_wishlist
            wishlistsection.background=ContextCompat.getDrawable(HomePageViewModel.themedContext,R.drawable.wishlist_round)
            binding!!.emailbutton.setOnClickListener {
                val email: String = binding!!.backstockemail.text.toString()
                if (email.trim().isNotEmpty()) {
                    backinstockviewmodel!!.BackInStockResponse(
                        email,
                        resources.getString(R.string.shop),
                        productID,
                        variantId.toString()
                    )
                        .observe(this@ProductView) { this@ProductView.backinstockalert(it) }
                } else {
                    Toast.makeText(
                        this@ProductView,
                        "Please enter a valid email first.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            if(productedge.variants.edges.size<=1) {
                if (model?.isInwishList(variantId.toString())!!) {

                    Wish(true)
                    Constant.WishlistAnimation(this, binding?.wishenable!!)

                } else {
                    Wish(false)
                }
            }
            spacer.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.divider))
            line1.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.divider))
            line2.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.divider))
            bottomsection.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
            setProductPrice(variant)
            binding!!.productdata = data
            binding!!.clickhandlers = ClickHandlers()
            shimmerStopGridProductView()
            binding!!.mainparentsection.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun SubscriptionProductData(productedge: Storefront.Product?) {
        if (productedge!!.requiresSellingPlan) {
            bottomsection.visibility = View.GONE
        }
        if (productedge?.sellingPlanGroups != null && productedge.sellingPlanGroups.edges.size > 0) {
            subscriptionsection.visibility = View.VISIBLE
            subscriptionsection.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.white))
            spacer_2.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.divider))
            subscribe_text.setTextColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.normalgrey1text))
            subscribedvalue = "plansAvailable"
            group_data = mutableListOf()
            group_offer_data = mutableListOf()
            for (i in 0 until productedge.sellingPlanGroups.edges.size) {
                group_data?.add(productedge.sellingPlanGroups.edges.get(i))
                var splans = productedge.sellingPlanGroups.edges.get(i).node.sellingPlans
                for (j in 0 until splans.edges.size) {
                    group_offer_data?.add(splans.edges.get(j).node.name)
                    subscriptionid = splans.edges.get(j).node.id
                }
            }
            sellingplans_adapter.setData(
                group_data!!, this,
                object : SellingPlanGroupAdapter.VariantCallback {
                    override fun clickVariant(
                        variantName: String,
                        optionName: MutableList<Storefront.SellingPlanEdge>
                    ) {
                        offerplans_adapter = SellingGroupOfferAdapter()
                        offerplans_adapter.setData(
                            optionName, this@ProductView,
                            object : SellingGroupOfferAdapter.VariantCallback {
                                override fun clickVariant(
                                    offername: String,
                                    offer_percentage: String,
                                    plan_id: String
                                ) {
                                    Log.i("OPTIONSELECTED", "" + offername)
                                    subscribesection.visibility = View.VISIBLE
                                    binding?.offerText?.text = offer_percentage
                                    binding?.offerText?.visibility = View.VISIBLE
                                    group_plan_id = plan_id
                                    offerName = offername
                                }
                            }
                        )
                        binding?.groupOfferRecycler?.adapter = offerplans_adapter
                        binding?.groupOfferRecycler?.adapter?.notifyDataSetChanged()
                        binding?.subscribeBtn?.setOnClickListener {
                            if (group_plan_id != null) {
                                model!!.addToCart(
                                    variantId.toString(),
                                    1,
                                    group_plan_id.toString(), offerName
                                )
                                Log.d("groupid", "" + group_plan_id.toString())
                                Toast.makeText(
                                    this@ProductView,
                                    productName + " " + resources.getString(R.string.add_cart),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@ProductView,
                                    resources.getString(R.string.noplan),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }, group_offer_data!!
            )
            binding?.subscribtionGroup?.adapter = sellingplans_adapter
            binding?.subscribtionGroup?.adapter?.notifyDataSetChanged()
        }
    }

    private fun showVariantPage() {
        binding!!.variantContainer.visibility = View.GONE
        binding!!.selectVariantPage.visibility = View.VISIBLE
        binding!!.dividertwo.visibility = View.VISIBLE
        binding!!.selectVariantPage.setOnClickListener {
            var varintent = Intent(this, VariationsActivity::class.java)
            startActivityForResult(varintent, 201)
        }
    }

    private fun filterOptionList(
        options: List<Storefront.ProductOption>,
        edges: MutableList<Storefront.ProductVariantEdge>,
        productedge: Storefront.Product
    ) {
        var variant_options: MutableList<Storefront.SelectedOption>? = null
        var name = "Name"
        var value = "Value"
        Log.d(TAG, "filterOptionList: " + options)
        Log.d(TAG, "filterOptionList: " + edges)
        var swatechView: SwatchesListBinding? = null
        var outofStockList: MutableList<String> = mutableListOf()
        if (edges.size > 1) {
            binding!!.variantContainer.visibility = View.VISIBLE
        } else {
            binding!!.variantContainer.visibility = View.GONE
            singleVariant = true
            variantId = edges.get(0).node.id
            variantValidation.put("title", variantId.toString())
            availableqty = edges.get(0).node.quantityAvailable
            val pid = variantId.toString().split("/")
            val vid = pid[pid.size - 1]
            Log.i("INVENTORY", "FIRST " + edges.get(0).node.quantityAvailable.toString())
            if (edges.get(0).node.currentlyNotInStock == false) {
                if (edges.get(0).node.quantityAvailable <= 0 && !edges.get(0).node.availableForSale) {
                    binding?.addtocart?.text = getString(R.string.out_of_stock)
                    binding?.cartsection?.setBackgroundColor(resources.getColor(R.color.outofstock_background))
                    binding?.cartsection?.background=ContextCompat.getDrawable(HomePageViewModel.themedContext,R.drawable.newcartround)
                    binding?.cartsection?.getBackground()?.setColorFilter(resources.getColor(R.color.outofstock_background), PorterDuff.Mode.SRC_OVER)
                    binding?.buynowsection?.alpha = 0.4f
//                    binding?.buynow?.setTextColor(resources.getColor(R.color.black))
//                    binding?.buynowsection?.setBackgroundColor(resources.getColor(R.color.white))
//                    binding?.buynowsection?.background = (resources.getDrawable(R.drawable.round))
//                    binding?.cartsection?.setBackgroundColor(resources.getColor(R.color.white))
                    binding?.addtocart?.setTextColor(resources.getColor(R.color.outofstockred))
                    if (featuresModel.enablebackInStock) {
                        binding!!.backinstock.visibility = View.VISIBLE
                    }
                    inStock = false
                } else {
                    binding?.buynowsection?.alpha = 1.0f
                    binding?.addtocart?.text = getString(R.string.addtocart)
                    binding?.addtocart?.setTextColor(Color.parseColor(NewBaseActivity.textColor))
//                    binding?.buynow?.setTextColor(resources.getColor(R.color.black))
//                    binding?.buynowsection?.setBackgroundColor(resources.getColor(R.color.white))
//                    binding?.buynowsection?.background = (resources.getDrawable(R.drawable.round))
                    binding?.cartsection?.setBackgroundColor(Color.parseColor(themeColor))
                    binding?.cartsection?.background=ContextCompat.getDrawable(HomePageViewModel.themedContext,R.drawable.newcartround)
                    binding?.cartsection?.getBackground()?.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_OVER)
                    inStock = true
                    if (featuresModel.enablebackInStock) {
                        binding!!.backinstock.visibility = View.GONE
                    }
                }
            } else {
                inStock = true
                binding?.buynowsection?.alpha = 1.0f
                binding?.addtocart?.text = getString(R.string.addtocart)
                binding?.addtocart?.setTextColor(Color.parseColor(NewBaseActivity.textColor))
//                binding?.buynow?.setTextColor(resources.getColor(R.color.black))
//                binding?.buynowsection?.setBackgroundColor(resources.getColor(R.color.white))
//                binding?.buynowsection?.background = (resources.getDrawable(R.drawable.round))
                binding?.cartsection?.setBackgroundColor(Color.parseColor(themeColor))
                binding?.cartsection?.background=ContextCompat.getDrawable(HomePageViewModel.themedContext,R.drawable.newcartround)
                binding?.cartsection?.getBackground()?.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_OVER)
                availableqty = 1;
                if (featuresModel.enablebackInStock) {
                    binding!!.backinstock.visibility = View.GONE
                }
            }
            setProductPrice(edges.get(0).node)
        }
        for (i in 0 until edges.size) {
            if (edges.get(i).node.sellingPlanAllocations != null) {
                if (edges.get(i).node.sellingPlanAllocations.edges.size > 0) {
                    var variant_id = edges.get(i).node.id
                    for (j in 0 until edges.get(i).node.sellingPlanAllocations.edges.size) {
                        var sellingplanid =
                            edges.get(i).node.sellingPlanAllocations.edges.get(j).node.sellingPlan.id
                        if (VariantSellingID.containsKey(sellingplanid)) {
                            var variantwithplans = VariantSellingID.get(sellingplanid)
                            variantwithplans!!.add(variant_id)
                            VariantSellingID?.put(sellingplanid, variantwithplans)
                        } else {
                            var variantwithplans = ArrayList<ID>()
                            variantwithplans.add(variant_id)
                            VariantSellingID?.put(sellingplanid, variantwithplans)
                        }
                    }
                }
            }
            if (!edges.get(i).node.availableForSale) {
                outofStockList.add(edges.get(i).node.title)
            }
            if (WishlistVariantID == edges[i].node.id.toString()) {
                variant_options = edges[i].node.selectedOptions
            }
        }
        totalVariant = options.size
        if (variant_options != null) {
            for (i in 0 until variant_options.size) {
                name = variant_options.get(i).name
                value = variant_options.get(i).value
                variant_data?.add(value)
            }
        } else {
            variant_data?.add("")
        }
        Log.d("javed", "filterOptionList1: " + variant_options)
        Log.d("javed", "filterOptionList2: " + variant_data)
        for (j in 0 until options.size) {
            var swatechView = SwatchesListBinding.inflate(LayoutInflater.from(this))
            swatechView.variantTitle.text = options.get(j).name
            swatechView.variantTitle.setTextColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.normalgrey1text))
            swatechView.back.setBackgroundColor(ContextCompat.getColor(HomePageViewModel.themedContext,R.color.divider))
            swatechView.variantList.visibility = View.GONE
            swatechView.variantListRecyclerView.visibility = View.VISIBLE
            adapter = VariantAdapter()
            var data = HashMap<String, String>()
            for (k in 0 until options.get(j).values.size) {
                data.put(options.get(j).values.get(k), "true")
            }
            adapter.setData(
                variant_data!!,
                options.get(j).name,
                options.get(j).values,
                outofStockList,
                this,
                object : VariantAdapter.VariantCallback {
                    override fun clickVariant(variantName: String, optionName: String) {
                        variant_pair.put(optionName, variantName)
                        if (totalVariant == variant_pair.size) {
                            sellingplans_adapter = SellingPlanGroupAdapter()
                            SubscriptionProductData(productedge = productedge)
                            variantFilter(variant_pair.values.toList(), edges, variant_pair)
                            selectedVariants = variant_pair
                        }
                        collapseBottomsheet()
                    }

                })
            swatechView.root.variant_list_recyclerView.adapter = adapter
            binding?.variantContainer?.addView(swatechView.root)
        }
    }

    private fun filterSpinnerOptionList(
        options: List<Storefront.ProductOption>,
        edges: MutableList<Storefront.ProductVariantEdge>,
        productedge: Storefront.Product
    ) {
        var variant_options: MutableList<Storefront.SelectedOption>? = null
        var name = "Name"
        var value = "Value"

        Log.d(TAG, "filterOptionList: " + options)
        var swatechView: SwatchesListBinding? = null
        var outofStockList: MutableList<String> = mutableListOf()
        if (edges.size > 1) {
            //binding!!.variantheading.visibility = View.VISIBLE
            binding!!.variantContainer.visibility = View.VISIBLE
        } else {
            // binding!!.variantheading.visibility = View.GONE
            binding!!.variantContainer.visibility = View.GONE

            singleVariant = true
            variantId = edges.get(0).node.id

            variantValidation.put("title", variantId.toString())

            availableqty = edges.get(0).node.quantityAvailable
            val pid = variantId.toString().split("/")
            val vid = pid[pid.size - 1]
            Log.i("INVENTORY", "Second last " + edges.get(0).node.quantityAvailable.toString())
            if (edges.get(0).node.currentlyNotInStock == false) {
                if (edges.get(0).node.quantityAvailable <= 0 && !edges.get(0).node.availableForSale) {
                    binding?.addtocart?.text = getString(R.string.out_of_stock)
                    binding?.cartsection?.setBackgroundColor(resources.getColor(R.color.outofstock_background))
                    binding?.cartsection?.background=ContextCompat.getDrawable(HomePageViewModel.themedContext,R.drawable.newcartround)
                    binding?.addtocart?.setTextColor(resources.getColor(R.color.outofstockred))
                    binding?.cartsection?.getBackground()?.setColorFilter(resources.getColor(R.color.outofstock_background), PorterDuff.Mode.SRC_OVER)
                    inStock = false
                    if (featuresModel.enablebackInStock) {
                        binding!!.backinstock.visibility = View.VISIBLE
                    }
                } else {
                    binding?.addtocart?.text = getString(R.string.addtocart)
                    binding?.addtocart?.setTextColor(Color.parseColor(NewBaseActivity.textColor))
                    inStock = true
                    if (featuresModel.enablebackInStock) {
                        binding!!.backinstock.visibility = View.GONE
                    }
                }
            } else {
                inStock = true
                binding?.addtocart?.text = getString(R.string.addtocart)
                binding?.addtocart?.setTextColor(Color.parseColor(NewBaseActivity.textColor))
                availableqty = 1;
                if (featuresModel.enablebackInStock) {
                    binding!!.backinstock.visibility = View.GONE
                }
            }
            setProductPrice(edges.get(0).node)
        }
        for (i in 0 until edges.size) {
            if (edges.get(i).node.sellingPlanAllocations != null) {
                if (edges.get(i).node.sellingPlanAllocations.edges.size > 0) {
                    var variant_id = edges.get(i).node.id
                    for (j in 0 until edges.get(i).node.sellingPlanAllocations.edges.size) {
                        var sellingplanid =
                            edges.get(i).node.sellingPlanAllocations.edges.get(j).node.sellingPlan.id
                        if (VariantSellingID.containsKey(sellingplanid)) {
                            var variantwithplans = VariantSellingID.get(sellingplanid)
                            variantwithplans!!.add(variant_id)
                            VariantSellingID?.put(sellingplanid, variantwithplans)
                        } else {
                            var variantwithplans = ArrayList<ID>()
                            variantwithplans.add(variant_id)
                            VariantSellingID?.put(sellingplanid, variantwithplans)
                        }
                    }
                }
            }
            if (!edges.get(i).node.availableForSale) {
                outofStockList.add(edges.get(i).node.title)
            }
            if (WishlistVariantID == edges[i].node.id.toString()) {
                variant_options = edges[i].node.selectedOptions
            }
        }
        totalVariant = options.size
        var variant_pair: MutableMap<String, String> = mutableMapOf()
        for (j in 0 until options.size) {
            swatechView =
                DataBindingUtil.inflate(layoutInflater, R.layout.swatches_list, null, false)
            swatechView.variantTitle.text = options.get(j).name
            swatechView.variantList.visibility = View.VISIBLE
            swatechView.variantListRecyclerView.visibility = View.GONE
            adapter = VariantAdapter()
            swatechView.variantList.tag = options.get(j).name
            swatechView.variantList.adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                options.get(j).values
            )
            swatechView.variantList.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        Log.d(
                            TAG,
                            "onItemSelected: " + (p1 as AppCompatTextView).text + "name : " + p0?.tag
                        )

                        variant_pair.put(p0?.tag.toString(), p1.text.toString())
                        if (totalVariant == variant_pair.size) {
                            sellingplans_adapter = SellingPlanGroupAdapter()
                            SubscriptionProductData(productedge = productedge)
                            variantFilter(variant_pair.values.toList(), edges, variant_pair)
                            selectedVariants = variant_pair

                        }
                        collapseBottomsheet()
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                }

            binding?.variantContainer?.addView(swatechView.root)

        }

    }

    private fun variantFilter(
        variantPair: List<String>,
        edges: MutableList<Storefront.ProductVariantEdge>,
        variantList: MutableMap<String, String>
    ) {
        val new_pair = StringBuilder()
        for (i in 0 until variantPair.size) {
            if (new_pair.length > 0) {
                new_pair.append(" / ")
            }
            new_pair.append(variantPair.get(i))
        }
        val new_pair_reverse = StringBuilder()
        for (i in variantPair.size - 1 downTo 0) {
            if (new_pair_reverse.length > 0) {
                new_pair_reverse.append(" / ")
            }
            new_pair_reverse.append(variantPair.get(i))
        }
        Log.d(TAG, "variantFilter: " + new_pair)
        Log.d(TAG, "variantFilter: " + new_pair_reverse)
        edges.forEach {

            if (it.node.title.equals(new_pair.toString()) || it.node.title.equals(new_pair_reverse.toString())) {
                variantId = it.node.id

                if (model?.isInwishList(variantId.toString())!!) {

                    Wish(true)
                    Constant.WishlistAnimation(this, binding?.wishenable!!)

                } else {
                    Wish(false)
                }
                invalidateOptionsMenu()
                variant_data?.clear()
                Log.d("javed", "variantFilter: " + it.node.quantityAvailable.toString())

                availableqty = it.node.quantityAvailable
                /********************************** Back In Stock Functionality *******************************************/

                val pid = variantId.toString().split("/")
                val vid = pid[pid.size - 1]

                Log.i("INVENTORY", "last " + it.node.quantityAvailable.toString())
                /***********************************************************************************************************/
                try {
                    if (slider!!.mediaList?.contains(
                            MediaModel(
                                "MediaImage",
                                it.node.image.url,
                                ""
                            )
                        ) == false
                    ) {
                        slider.mediaList?.add(MediaModel("MediaImage", it.node.image.url, ""))
                        binding!!.images.adapter!!.notifyDataSetChanged()
                    }
                    binding!!.images.setCurrentItem(
                        slider.mediaList!!.indexOf(
                            MediaModel(
                                "MediaImage",
                                it.node.image.url,
                                ""
                            )
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                setProductPrice(it.node)
                if (it.node.currentlyNotInStock == false) {
                    if (it.node.quantityAvailable <= 0 && !it.node.availableForSale) {
                        binding?.addtocart?.text = getString(R.string.out_of_stock)
                        binding?.buynowsection?.alpha = 0.4f
                        binding?.addtocart?.setTextColor(resources.getColor(R.color.outofstockred))
                        binding?.cartsection?.setBackgroundColor(resources.getColor(R.color.outofstock_background))
                        binding?.cartsection?.background=resources.getDrawable(R.drawable.newcartround)
                        binding?.cartsection?.getBackground()?.setColorFilter(resources.getColor(R.color.outofstock_background), PorterDuff.Mode.SRC_OVER)
//                        binding?.buynow?.setTextColor(resources.getColor(R.color.black)
//                        binding?.buynowsection?.background = (resources.getDrawable(R.drawable.round))
                        inStock = false
                        if (featuresModel.enablebackInStock) {
                            binding!!.backinstock.visibility = View.VISIBLE
                        }
                    } else {
                        binding?.buynowsection?.alpha = 1.0f
//                        binding?.buynowsection?.background = (resources.getDrawable(R.drawable.round))
                        binding?.addtocart?.text = getString(R.string.addtocart)
                        binding?.addtocart?.setTextColor(Color.parseColor(NewBaseActivity.textColor))
                        binding?.cartsection?.setBackgroundColor(Color.parseColor(themeColor))
                        binding?.cartsection?.background=resources.getDrawable(R.drawable.newcartround)
                        binding?.cartsection?.getBackground()?.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_OVER)
                        inStock = true
                        if (featuresModel.enablebackInStock) {
                            binding!!.backinstock.visibility = View.GONE
                        }
                    }
                } else {
                    inStock = true
                    binding?.buynowsection?.alpha = 1.0f
                    binding?.addtocart?.text = getString(R.string.addtocart)
                    binding?.addtocart?.setTextColor(Color.parseColor(NewBaseActivity.textColor))
                    binding?.cartsection?.setBackgroundColor(Color.parseColor(themeColor))
                    binding?.cartsection?.background=resources.getDrawable(R.drawable.newcartround)
                    binding?.cartsection?.getBackground()?.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_OVER)
                    availableqty = 1;
                    if (featuresModel.enablebackInStock) {
                        binding!!.backinstock.visibility = View.GONE
                    }
                }
                Log.d(TAG, "variantFilter: " + variantId)
                variantValidation = variantList
                return
            }
            else {
                binding?.addtocart?.text = getString(R.string.out_of_stock)
//                binding?.buynowsection?.background = (resources.getDrawable(R.drawable.round))
                binding?.addtocart?.setTextColor(resources.getColor(R.color.outofstockred))
                binding?.cartsection?.setBackgroundColor(resources.getColor(R.color.outofstock_background))

                binding?.cartsection?.background=resources.getDrawable(R.drawable.newcartround)

                binding?.cartsection?.getBackground()?.setColorFilter(resources.getColor(R.color.outofstock_background), PorterDuff.Mode.SRC_OVER)
                inStock = false
            }
        }

    }

    private fun backinstockalert(apiResponse: com.shopify.apicall.ApiResponse?) {
        try {
            val res = apiResponse!!.data
            if (res!!.asJsonObject!!.has("success")) {
                if (res.asJsonObject.get("success").asBoolean) {
                    val pinalertDialog =
                        SweetAlertDialog(this@ProductView, SweetAlertDialog.NORMAL_TYPE)
                    pinalertDialog.titleText = resources.getString(R.string.note)
                    pinalertDialog.contentText = resources.getString(R.string.stocknotify)
                    pinalertDialog.show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setProductPrice(variant: Storefront.ProductVariant?) {
        data!!.regularprice = CurrencyFormatter.setsymbol(
            variant?.price?.amount!!,
            variant.price?.currencyCode.toString()
        )
        if (variant?.compareAtPrice != null) {
            val special = java.lang.Double.valueOf(variant.compareAtPrice.amount)
            val regular = java.lang.Double.valueOf(variant.price.amount)
            if (BigDecimal.valueOf(special).compareTo(BigDecimal.valueOf(regular)) == 1) {
                data!!.regularprice = CurrencyFormatter.setsymbol(
                    variant.compareAtPrice.amount,
                    variant.compareAtPrice.currencyCode.toString()
                )
                data!!.specialprice = CurrencyFormatter.setsymbol(
                    variant.price.amount,
                    variant.price.currencyCode.toString()
                )
                data!!.offertext = "(" + getDiscount(
                    special,
                    regular
                ).toString() + "${resources.getString(R.string.off)})"
                data!!.isStrike = true
                binding!!.regularprice.paintFlags =
                    binding!!.regularprice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding!!.specialprice.visibility = View.VISIBLE
                binding!!.offertext.visibility = View.VISIBLE
            } else {
                data!!.isStrike = false
                binding!!.specialprice.visibility = View.GONE
                binding!!.offertext.visibility = View.GONE
                binding!!.regularprice.paintFlags =
                    binding!!.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        } else {
            data!!.isStrike = false
            binding!!.specialprice.visibility = View.GONE
            binding!!.offertext.visibility = View.GONE
            binding!!.regularprice.paintFlags =
                binding!!.regularprice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        try {
            binding!!.specialprice.setTextColor(resources.getColor(R.color.specialpricecolor))
            binding!!.regularprice.setTextColor(resources.getColor(R.color.black))
        } catch (e: Exception) {

        }

    }

    fun getDiscount(regular: Double, special: Double): Int {
        return ((regular - special) / regular * 100).toInt()
    }

    inner class ClickHandlers {
        fun buynow(view: View, data: ListData) {
            if (inStock) {
                if (!variantValidation.isEmpty() || totalVariant != null) {
                    if (variantValidation.size >= totalVariant!! || singleVariant) {
                        /*if (mBottomSheetBehaviour.state != BottomSheetBehavior.STATE_COLLAPSED) {
                            if (!quantityselected.equals("notselectedyet")) {*/
                        model!!.prepareCart(variantId.toString(), 1)
                        collapseBottomsheet()
                        /*} else {
                            Toast.makeText(
                                view.context,
                                resources.getString(R.string.select_quantity),
                                Toast.LENGTH_LONG
                            ).show()
                        }*/
                        if (binding!!.cartsection.visibility == View.GONE) {
                            binding!!.cartsection.visibility = View.VISIBLE
                        }
                        /*} else {
                            showQtyDialog()
                            binding!!.cartsection.visibility = View.GONE
                        }*/
                    } else {
                        openQuickOptions()
                    }
                } else {
                    openQuickOptions()
                }
            } else {
                Toast.makeText(
                    view.context,
                    getString(R.string.outofstock_warning),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun addtoCart(view: View, data: ListData) {
            if (inStock) {
                if (!variantValidation.isEmpty() || totalVariant != null) {
                    if (variantValidation.size >= totalVariant!! || singleVariant) {
                        if (binding?.addtocart!!.text == getString(R.string.go_to_bag)) {
                            val intent = Intent(this@ProductView, CartList::class.java)
                            startActivity(intent)
                        } else {

                            model!!.addToCart(
                                variantId.toString(),
                                1,
                                "", ""
                            )
                            Constant.SlideAnimation(this@ProductView, binding?.addtocart!!)
                            binding?.addtocart!!.text = getString(R.string.go_to_bag)
                            Toast.makeText(
                                view.context,
                                productName + " " + resources.getString(R.string.add_cart),
                                Toast.LENGTH_LONG
                            ).show()
                            //collapseBottomsheet()
                            invalidateOptionsMenu()
                            val cartlistData = JSONObject()
                            cartlistData.put("id", data.product?.id.toString())
                            cartlistData.put("quantity", 1)
                            cartlistArray.put(cartlistData.toString())
                            Constant.logAddToCartEvent(
                                cartlistArray.toString(), data.product?.id.toString(),
                                "product",
                                data.product?.variants?.edges?.get(0)?.node?.price?.currencyCode?.toString(),
                                data.product?.variants?.edges?.get(0)?.node?.price?.amount?.toDouble()
                                    ?: 0.0,
                                this@ProductView
                            )
                            if (featuresModel.firebaseEvents) {
                                Constant.FirebaseEvent_AddtoCart(data.product?.id.toString(),"1")
                            }
                            if (binding!!.buynowsection.visibility == View.GONE) {
//                                binding?.buynowsection?.background = (resources.getDrawable(R.drawable.round))
                                binding!!.buynowsection.visibility = View.VISIBLE
                            }
                        }


                        /*} else Toast.makeText(
                            view.context,
                            resources.getString(R.string.select_quantity),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        showQtyDialog()
                        binding!!.buynowsection.visibility = View.GONE
                    }*/
                    } else {
                        openQuickOptions()
                    }
                } else {
                    openQuickOptions()
                }

            } else {
                Toast.makeText(
                    view.context,
                    getString(R.string.outofstock_warning),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun showSizeChart(view: View, data: ListData) {
            val intent = Intent(this@ProductView, Weblink::class.java)
            intent.putExtra("name", resources.getString(R.string.size_chart))
            intent.putExtra("link", sizeChartUrl)
            startActivity(intent)
            Constant.activityTransition(this@ProductView)
        }

        fun addtoWish(view: View, data: ListData) {
//            if (inStock) {
            Log.i("MageNative", "In Wish")
            if (!variantValidation.isEmpty() || totalVariant != null) {
                if (variantValidation.size >= totalVariant!! || singleVariant) {
                    if (!model!!.isInwishList(variantId.toString())) {
                        model!!.AddtoWishVariant(
                            variantId.toString()
                        )
                        if (featuresModel.Enable_flits_App) {
                            flistwishmodel?.SendWishlistData(
                                Urls.X_Integration_App_Name!!,
                                productID,
                                product_handle.toString(),
                                MagePrefs.getCustomerID().toString(),
                                MagePrefs.getCustomerEmail().toString(),
                                Urls.user_id!!,
                                Urls.token!!
                            )
                        }
                        Wish(true)

                        var wishlistData = JSONObject()
                        wishlistData.put("id", variantId.toString())
                        wishlistData.put("quantity", 1)
                        whishlistArray.put(wishlistData.toString())
                        Constant.logAddToWishlistEvent(
                            whishlistArray.toString(),
                            variantId.toString(),
                            "product",
                            data.product?.variants?.edges?.get(0)?.node?.price?.currencyCode?.toString(),
                            data.product?.variants?.edges?.get(0)?.node?.price?.amount?.toDouble()
                                ?: 0.0,
                            this@ProductView
                        )
                        if (SplashViewModel.featuresModel.firebaseEvents) {
                            Constant.FirebaseEvent_AddtoWishlist(variantId.toString(),"1")
                        }

                    } else {
                        if (SplashViewModel.featuresModel.Enable_flits_App) {
                            flistwishmodel?.RemoveWishlistData(
                                Urls.X_Integration_App_Name!!,
                                data.product?.id.toString().replace("gid://shopify/Product/", "")
                                    .split("?")[0],
                                data.product?.handle.toString(),
                                MagePrefs.getCustomerID().toString(),
                                MagePrefs.getCustomerEmail().toString(),
                                Urls.user_id!!,
                                Urls.token!!
                            )
                        }
                        model!!.deleteData(variantId.toString())
                        Toast.makeText(
                            view.context,
                            resources.getString(R.string.removedwish),
                            Toast.LENGTH_SHORT
                        ).show()
                        Wish(false)

                    }
                } else {
                    openQuickOptions()
                }
            } else {
                openQuickOptions()
            }
            invalidateOptionsMenu()
        }

        fun viewAllReview(view: View) {
            var intent = Intent(this@ProductView, AllReviewListActivity::class.java)
            intent.putExtra("reviewList", reviewModel)
            intent.putExtra("product_name", productName)
            intent.putExtra("product_id", (productID))
            startActivity(intent)
            Constant.activityTransition(view.context)
        }

        fun viewAllJudgeMeReview(view: View) {
            var intent = Intent(this@ProductView, AllJudgeMeReviews::class.java)
            intent.putExtra("reviewList", reviewList)
            intent.putExtra("product_name", productName)
            intent.putExtra("product_id", judgeme_productid)
            startActivity(intent)
            Constant.activityTransition(view.context)
        }

        fun viewAllAliReview(view: View) {
            var intent = Intent(this@ProductView, AllAliReviewsListActivity::class.java)
            intent.putExtra("reviewList", reviewList)
            intent.putExtra("product_name", productName)
            intent.putExtra("product_id", AliProductId)
            intent.putExtra("shop_id", AliShopId)
            startActivity(intent)
            Constant.activityTransition(view.context)
        }

        fun shareProduct(view: View, data: ListData) {
            val shareString =
                resources.getString(R.string.hey) + "  " + data.product!!.title + "  " + resources.getString(
                    R.string.on
                ) + "  " + resources.getString(R.string.app_name) + "\n" + data.product!!.onlineStoreUrl + "?pid=" + data.product!!.id.toString()
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                view.context.resources.getString(R.string.app_name)
            )
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareString)
            view.context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    view.context.resources.getString(R.string.share)
                )
            )
            Constant.activityTransition(view.context)
        }


        fun showAR(view: View, data: ListData) {
            try {
                Log.d(TAG, "showAR: " + mediaList)
                var dialog = Dialog(this@ProductView, R.style.WideDialog)
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
                var dialogBinding = DataBindingUtil.inflate<ArimagesDialogBinding>(
                    layoutInflater,
                    R.layout.arimages_dialog,
                    null,
                    false
                )
                dialog.setContentView(dialogBinding.root)
                dialogBinding.closeBut.setOnClickListener {
                    dialog.dismiss()
                }
                model?.filterArModel(mediaList)?.observe(this@ProductView, Observer {
                    arImagesAdapter.setData(it)
                    dialogBinding.arList.adapter = arImagesAdapter
                    if (it.size == 1) {
                        try {
                            val sceneViewerIntent = Intent(Intent.ACTION_VIEW)
                            val intentUri: Uri =
                                Uri.parse("https://arvr.google.com/scene-viewer/1.1").buildUpon()
                                    .appendQueryParameter("file", data.arimage)
                                    .build()
                            sceneViewerIntent.data = intentUri
                            sceneViewerIntent.setPackage("com.google.ar.core")
                            startActivity(sceneViewerIntent)
                            Constant.activityTransition(view.context)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(
                                this@ProductView,
                                getString(R.string.ar_error_text),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        dialog.show()
                    }
                })

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun rateProduct(view: View, data: ListData) {
            Log.d("javed", "injudgerev: ")
            var bottomsheet = Dialog(this@ProductView, R.style.WideDialog)
            bottomsheet.window?.setBackgroundDrawableResource(android.R.color.transparent)
            bottomsheet.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            var reviewFormBinding = DataBindingUtil.inflate<ReviewFormBinding>(
                layoutInflater,
                R.layout.review_form,
                null,
                false
            )
            bottomsheet.setContentView(reviewFormBinding.root)
            reviewFormBinding.ratingBar.progressTintList =
                ColorStateList.valueOf(Color.parseColor("#FFD700"))
            bottomsheet.setCancelable(false)
            reviewFormBinding.closeBut.setOnClickListener {
                bottomsheet.dismiss()
            }
            reviewFormBinding.submitReview.setOnClickListener {
                if (TextUtils.isEmpty(reviewFormBinding.nameEdt.text.toString().trim())) {
                    reviewFormBinding.nameEdt.error = getString(R.string.name_validation)
                    reviewFormBinding.nameEdt.requestFocus()
                } else if (TextUtils.isEmpty(reviewFormBinding.titleEdt.text.toString().trim())) {
                    reviewFormBinding.titleEdt.error = getString(R.string.review_title_validation)
                    reviewFormBinding.titleEdt.requestFocus()
                } else if (TextUtils.isEmpty(reviewFormBinding.bodyEdt.text.toString().trim())) {
                    reviewFormBinding.bodyEdt.error = getString(R.string.review_validation)
                    reviewFormBinding.bodyEdt.requestFocus()
                } else if (TextUtils.isEmpty(reviewFormBinding.emailEdt.text.toString().trim())) {
                    reviewFormBinding.emailEdt.error = getString(R.string.email_validation)
                    reviewFormBinding.emailEdt.requestFocus()
                } else if (!model?.isValidEmail(
                        reviewFormBinding.emailEdt.text.toString().trim()
                    )!!
                ) {
                    reviewFormBinding.emailEdt.error = resources.getString(R.string.invalidemail)
                    reviewFormBinding.emailEdt.requestFocus()
                } else {
                    model?.getcreateReview(
                        Urls(application as MyApplication).mid,
                        reviewFormBinding.ratingBar.rating.toString(),
                        (productID),
                        reviewFormBinding.nameEdt.text.toString().trim(),
                        reviewFormBinding.emailEdt.text.toString().trim(),
                        reviewFormBinding.titleEdt.text.toString().trim(),
                        reviewFormBinding.bodyEdt.text.toString().trim()
                    )
                    bottomsheet.dismiss()
                }
            }
            bottomsheet.show()
        }

        fun rateProductJudgeMe(view: View, data: ListData) {
            var intent = Intent(this@ProductView, JudgeMeCreateReview::class.java)
            intent.putExtra("external_id", external_id)
            startActivityForResult(intent, 105)
            Constant.activityTransition(view.context)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 105) {
            if (featuresModel.judgemeProductReview) {
                model?.judgemeProductID(
                    Urls.JUDGEME_GETPRODUCTID + product_handle,
                    product_handle!!,
                    Urls.JUDGEME_APITOKEN,
                    Urls(application as MyApplication).shopdomain
                )
            }
        }

        if (requestCode == 201) {
            if (resultCode == RESULT_CANCELED) {

            } else if (resultCode == RESULT_OK) {

                val string = StringBuilder()
                for (i in 0 until selectedvariant_pair.values.toList().size) {
                    string.append(selectedvariant_pair.keys.toList().get(i))
                    string.append(" : ")
                    string.append(selectedvariant_pair.values.toList().get(i))
                    string.append("\n")
                }
                Log.d("javed", "string: " + string)
                binding!!.selectedvariant.visibility = View.VISIBLE
                binding!!.selectedvariant.text = string
                variantFilter(
                    selectedvariant_pair.values.toList(),
                    varproductedge!!.variants.edges,
                    selectedvariant_pair
                )
            } else {

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_searchandcart, menu)
        try {
            ////////////////Search menu Item//////////////
            var item = menu.findItem(R.id.search_item)
            item.setActionView(R.layout.m_searchicon)
            val view = item.actionView
            val searchicon = view?.findViewById<ImageView>(R.id.cart_icon)
            searchicon?.setColorFilter(Color.parseColor(HomePageViewModel.icon_color))
            view!!.setOnClickListener {
                onOptionsItemSelected(item)
            }
            ////////////////Share menu Item//////////////
            /*var shareitem = menu.findItem(R.id.share_item)
            shareitem.setActionView(R.layout.m_share)
            val shareview = shareitem.actionView
            val shareicon = shareview?.findViewById<ImageView>(R.id.cart_icon)
            shareicon?.setColorFilter(Color.parseColor(HomePageViewModel.icon_color))
            shareicon!!.setOnClickListener {
                binding!!.shareicon.performClick()
            }
            shareitem.isVisible = featuresModel.product_share*/
            ////////////////Wishlist menu Item//////////////
            var wishitem = menu.findItem(R.id.wish_item)
            wishitem.setActionView(R.layout.m_wishcount)
            val wishview = wishitem.actionView
            val wishrelative = wishview?.findViewById<RelativeLayout>(R.id.back)
            val wishtext = wishview?.findViewById<TextView>(R.id.count)
            val wishicon = wishview?.findViewById<ImageView>(R.id.cart_icon)
            wishrelative?.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor(HomePageViewModel.count_color))
            wishtext?.setTextColor(Color.parseColor(HomePageViewModel.count_textcolor))
            wishicon?.setColorFilter(Color.parseColor(HomePageViewModel.icon_color))
            wishtext!!.text = "" + leftMenuViewModel!!.wishListcount
            wishitem.isVisible = featuresModel.in_app_wishlist
            wishitem.actionView?.setOnClickListener {
                onOptionsItemSelected(wishitem)
            }
            ////////////////cart menu Item//////////////
            val cartitem = menu.findItem(R.id.cart_item)
            cartitem.setActionView(R.layout.m_count)
            val cartview = cartitem.actionView
            val cartrelative = cartview?.findViewById<RelativeLayout>(R.id.back)
            val carttext = cartview?.findViewById<TextView>(R.id.count)
            val carticon = cartview?.findViewById<ImageView>(R.id.cart_icon)
            cartrelative?.backgroundTintList = ColorStateList.valueOf(
                Color.parseColor(
                    HomePageViewModel.count_color
                )
            )
            carttext?.setTextColor(Color.parseColor(HomePageViewModel.count_textcolor))
            carticon?.setColorFilter(Color.parseColor(HomePageViewModel.icon_color))
            if (leftMenuViewModel?.cartCount!! > 0) {
                cartrelative?.visibility = View.VISIBLE
                carttext!!.text = "" + leftMenuViewModel?.cartCount
            }
            cartitem.actionView?.setOnClickListener {
                onOptionsItemSelected(cartitem)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.search_item -> {
                moveToSearch(this)
                true
            }
            R.id.wish_item -> {
                startActivity(Intent(this, WishList::class.java))
                Constant.activityTransition(this)
                true
            }
            R.id.cart_item -> {
                CoroutineScope(Dispatchers.IO).launch {
                    if (leftMenuViewModel?.repository?.getSellingPlanData()?.selling_plan_id != null) {
                        startActivity(Intent(this@ProductView, SubscribeCartList::class.java))
                        Constant.activityTransition(this@ProductView)
                    } else {
                        startActivity(Intent(this@ProductView, CartList::class.java))
                        Constant.activityTransition(this@ProductView)
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun submityptporeview() {
        model?.NResponse(
            "VuCs0uv4gPpRuMAMYS0msr1XozTDZunonCRRh6fC",
            productsku.toString(),
            productName.toString(),
            data?.product!!.onlineStoreUrl,
            MagePrefs.getCustomerFirstName().toString(),
            MagePrefs.getCustomerEmail().toString(),
            yotpo_reviewbody.text.toString(),
            yotpo_reviewtitle.text.toString(),
            yotpo_rating_bar.rating.toString()
        )?.observe(this, Observer { this.showData(it) })
    }

    private fun showData(response: ApiResponse?) {
        Log.i("RESPONSEGET", "" + response?.data)
        receiveReview(response?.data)
    }

    private fun receiveReview(data: JsonElement?) {
        val jsondata = JSONObject(data.toString())
        Log.i("messagereview", "" + jsondata)
        try {
            if (jsondata.getString("message").equals("ok")) {
                val alertDialog: AlertDialog = AlertDialog.Builder(this@ProductView).create()
                alertDialog.setTitle("Thank You")
                alertDialog.setMessage("Your review has been submitted successfully.")
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                alertDialog.show()
            }
            Handler().postDelayed({ finish() }, 2000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun arCoreButtonClicked() {
        val sceneViewerIntent = Intent(Intent.ACTION_VIEW)
        sceneViewerIntent.data =
            Uri.parse("https://arvr.google.com/scene-viewer/1.0?file=${data!!.arimage}")
        sceneViewerIntent.setPackage("com.google.android.googlequicksearchbox")
        startActivity(sceneViewerIntent)
    }


    override fun onResume() {
        if (binding?.addtocart!!.text.equals(getString(R.string.go_to_bag))) {
            binding?.addtocart!!.text = getString(R.string.addtocart)
        }

        super.onResume()
        invalidateOptionsMenu()
    }
    private fun collapseBottomsheet() {
        quantitysection.removeAllViews()
        if (mBottomSheetBehaviour.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    private fun Wish(flag: Boolean) {
        if (flag) {
            binding?.wishenable!!.isVisible = true
            Constant.WishlistAnimation(this, binding?.wishenable!!)
            binding?.wishdisable!!.isVisible = false
        } else {

            binding?.wishenable?.clearAnimation()
            binding?.wishenable?.isVisible = false
            binding?.wishdisable!!.isVisible = true

//            binding?.wishenable.playAnimation()
        }
    }
    private fun openQuickOptions() {
        var customQuickAddActivity = QuickAddActivity(
            context = this@ProductView,
            theme = R.style.WideDialogFull,
            product_id = product.id.toString(),
            repository = model!!.repository,
            product = product,
            callback = object :QuickAddActivity.ProductVariantCallback{
                override fun clickVariant(
                    variantName: String,
                    optionName: String,
                    position: Int,
                    vposition:Int
                ) {
                   var parent:ConstraintLayout= binding?.variantContainer!!.getChildAt(position) as ConstraintLayout
                   var recyler:RecyclerView=parent.getChildAt(2) as RecyclerView
                   var recylerChild:RelativeLayout=recyler.getChildAt(vposition) as RelativeLayout
                   var variantname: MageNativeTextView =recylerChild.getChildAt(0) as MageNativeTextView
                   variantname.performClick()
                }

                override fun buynow() {
                    binding!!.buynowsection.performClick()
                }

                override fun wishClick() {
                    binding!!.wishlistsection.performClick()
                }

            },
            wish=true,
            productViewModel = model!!
            )
        customQuickAddActivity.show()
    }

    fun chatGpt(chatgptresponse: String) {
        try{
            binding!!.chatgpt.visibility=View.VISIBLE
          //  var chatgpttext="Product information refers to the details, specifications, and features of a product that are provided to potential buyers or users. It includes information about the product's function, design, materials, size, warranty, and any other relevant details.In the applyTransformation() method, we add the next word to the TextView by getting the current text, appending a space and the next word, and setting the result as the new text of the TextView. We then increment the index and use postDelayed() to schedule another call to applyTransformation() after the specified delay."
            var chatgpttext=chatgptresponse
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding!!.chatgptdescription.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
            }
            binding!!.chatgpt.setOnClickListener {
                when(binding!!.chatgptdescriptioncontainer.isVisible){
                    true->{
                        binding!!.chatgpt.setImageResource(R.drawable.chatgpt_icon)
                        binding!!.chatgptdescription.text=""
                        binding!!.chatgptdescriptioncontainer.visibility=View.GONE
                    }
                    false->{
                        binding!!.chatgpt.setImageResource(R.drawable.chatgptcross)
                        binding!!.chatgptdescriptioncontainer.visibility=View.VISIBLE
                        val animation = TypewriterAnimation(chatgpttext,   binding!!.chatgptdescription)
                        animation.interpolator = AccelerateDecelerateInterpolator()
                        animation.duration = 5000
                        binding!!.chatgptdescription.startAnimation(animation)
                    }
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    fun checkChatGPT(productedge: Storefront.Product?){
        try{
            var desc="For the custom, please write note of you cats name along with photo on the cart page or email us at contact@catcurio.com Specifications:Custom Name And Portrait BraceletShape: RoundPendant: One, two, three piecesMetals Type: Stainless SteelGender: Unisex"
            model!!.getChatGPT(productedge!!.description.replace("\\","")).observe(this,{
            if(!it.isNullOrEmpty()){
                chatGpt(it)
            }
            })
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}
