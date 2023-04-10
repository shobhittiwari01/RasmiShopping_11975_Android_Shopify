package com.rasmishopping.app.cartsection.activities
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.ClipboardManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.cartdiscount.listing.DiscountListingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.shopify.graphql.support.ID
import com.shopify.rewardifyappmodule.ActiveDiscountList
import com.rasmishopping.app.FlitsDashboard.StoreCredits.StoreCreditsViewModel
import com.rasmishopping.app.FlitsDashboard.WishlistSection.FlitsWishlistViewModel
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel.Companion.featuresModel
import com.rasmishopping.app.cartsection.adapters.CartListAdapter
import com.rasmishopping.app.cartsection.adapters.CouponCodeAdapter
import com.rasmishopping.app.cartsection.adapters.DiscountListAdapter
import com.rasmishopping.app.cartsection.adapters.LocationListAdapter
import com.rasmishopping.app.cartsection.models.CartBottomData
import com.rasmishopping.app.cartsection.models.PricesModel
import com.rasmishopping.app.cartsection.viewmodels.CartListViewModel
import com.rasmishopping.app.checkoutsection.activities.CheckoutWeblink
import com.rasmishopping.app.customviews.MageNativeButton
import com.rasmishopping.app.databinding.MCartlistBinding
import com.rasmishopping.app.databinding.PopConfirmationBinding
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.network_transaction.customLoader
import com.rasmishopping.app.personalised.adapters.PersonalisedAdapter
import com.rasmishopping.app.personalised.viewmodels.PersonalisedViewModel
import com.rasmishopping.app.productsection.activities.ProductView
import com.rasmishopping.app.productsection.viewmodels.ProductViewModel
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.*
import com.shopify.zapietapp.ZapietViewModel
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.m_cartlist.*
import kotlinx.android.synthetic.main.m_cartlist_shimmer_layout_grid.*
import kotlinx.android.synthetic.main.m_newbaseactivity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
class CartList : NewBaseActivity(), DatePickerDialog.OnDateSetListener, OnMapReadyCallback {
    @Inject
    lateinit var factory: ViewModelFactory
    private var cartlist: RecyclerView? = null
    private var model: CartListViewModel? = null
    private var personamodel: PersonalisedViewModel? = null
    private var count: Int = 1
    private var checkoutID: ID? = null
    var checkout_disable: Boolean = false
    private val TAG = "CartList"
    var randomString: String? = null
    var date: String? = null
    var shipping: Boolean? = false
    var jsonarray: JsonArray? = null
    var variantJsonObject: JsonObject? = null
    var TotalDiscountFixedAmount: String? = null
    var TotalDiscountPercentage: String? = null
    var tagjsonarray: JsonArray? = null
    lateinit var delivery_param: HashMap<String, String>
    var DiscountedCheckoutUrl: String? = null
    var NormalDiscountedCheckoutUrl: String? = null
    var DiscountedCheckoutId: String? = null
    var NormalDiscountedCheckoutId: String? = null
    var checkoutdata: Storefront.Checkout? = null
    lateinit var response_data: Storefront.Checkout
    private var productmodel: ProductViewModel? = null
    val mincalender = Calendar.getInstance()
    var calender = Calendar.getInstance()
    val year = calender.get(Calendar.YEAR)
    var set_coupon: Boolean = false
    var set_normalcoupon: Boolean = false
    var wholesale_price_total: Double = 0.00
    var compare_At_price: Double = 0.00
    var price: Double = 0.00
    var Discount_Percentage: Double = 0.00
    val month = calender.get(Calendar.MONTH)
    val day = calender.get(Calendar.DAY_OF_MONTH)
    lateinit var dpd: DatePickerDialog
    var simpleDateFormat: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
    var dayFormat: SimpleDateFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
    var disabledates: ArrayList<Calendar>? = null
    var slots: JsonArray = JsonArray()
    lateinit var locations: JsonArray
    lateinit var daysOfWeek: JsonObject
    private var zapietviewmodel: ZapietViewModel? = null
    @Inject
    lateinit var couponCodeAdapter: CouponCodeAdapter
    lateinit var localdelivery_slots: ArrayList<String>
    var interval: Int = 0
    var selected_delivery: String = "delivery"
    var selected_slot: String? = null
    private lateinit var mMap: GoogleMap
    private var custom_attribute: JSONObject = JSONObject()
    private var priceModel: PricesModel? = null
    private var grandTotal: String? = null
    var RulesResponse: String? = null
    var wholesaleparams: JsonObject? = null
    var TagsArrayList: MutableList<String> = mutableListOf()
    private val bottomData = CartBottomData()
    private var cartWarning: HashMap<String, Boolean>? = hashMapOf()
    var tag: String = "noexpand"
    var discounttag: String = "noexpand"
    private var storecreditmodel: StoreCreditsViewModel? = null
    private var isSomethingLoading=false
    companion object {
        var flistwishmodel: FlitsWishlistViewModel? = null
    }
    @Inject
    lateinit var locationAdapter: LocationListAdapter
    @Inject
    lateinit var adapter: CartListAdapter
    @Inject
    lateinit var personalisedadapter: PersonalisedAdapter
    @Inject
    lateinit var padapter: PersonalisedAdapter
    private var binding: MCartlistBinding? = null
    var discountcode: String? = null
    var location_id: String? = null
    private var discountlistviewmodel: DiscountListingViewModel? = null
    @Inject
    lateinit var discountlistAdapter: DiscountListAdapter
    lateinit var checkout_id:ID
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_cartlist, group, true)
        //Shimmer
        shimmerStartGridCart()
        cartlist = setLayout(binding!!.cartlist, "vertical")
        cartlist!!.isNestedScrollingEnabled = false
        showCartText(resources.getString(R.string.yourcart), "")
        showBackButton()
        hidenavbottom()
        hidethemeselector()
        (application as MyApplication).mageNativeAppComponent!!.doCartListActivityInjection(this)
        model = ViewModelProvider(this, factory).get(CartListViewModel::class.java)
        model!!.context = this
        personamodel = ViewModelProvider(this, factory).get(PersonalisedViewModel::class.java)
        personamodel?.activity = this
        discountlistviewmodel = ViewModelProvider(this, factory).get(DiscountListingViewModel::class.java)
        discountlistviewmodel!!.context = this
        zapietviewmodel = ViewModelProvider(this, factory).get(ZapietViewModel::class.java)
        zapietviewmodel!!.context = this
        storecreditmodel = ViewModelProvider(this, factory).get(StoreCreditsViewModel::class.java)
        storecreditmodel!!.context = this
        binding!!.discountCodeEdt.setOnClickListener {
            binding!!.discountCodeEdt.requestFocus()
            nav_view.visibility = View.GONE
        }
        binding!!.giftcardEdt.setOnClickListener {
            binding!!.giftcardEdt.requestFocus()
            nav_view.visibility = View.GONE
        }
        flistwishmodel = ViewModelProvider(this, factory).get(FlitsWishlistViewModel::class.java)
        flistwishmodel!!.context = this
        ProductView.productmodel = ViewModelProvider(this, factory).get(ProductViewModel::class.java)
        ProductView.productmodel!!.context = this
        if (featuresModel.Enable_flits_App) {
            if (model!!.isLoggedIn) {
                storecreditmodel!!.spent_rules_data.observe(this, { consumestoreresponse(it) })
                storecreditmodel?.apply_discount?.observe(this, { consumeflistdiscount(it) })
            }
        }
        if (featuresModel.enableRewardify)
            binding?.redeem?.visibility = View.VISIBLE
        else binding?.redeem?.visibility = View.GONE
        productmodel = ViewModelProvider(this, factory).get(ProductViewModel::class.java)
        productmodel!!.context = this
        model?.removeDiscount()?.observe(this, Observer { this.consumeRemoveDiscount(it) })
        model!!.Response().observe(this, Observer<Storefront.Checkout> { this.consumeResponse(it) })
        binding!!.locationList.layoutManager = LinearLayoutManager(this)
        binding?.shippingContainer?.setOnClickListener {
            shipping = true
            binding?.deliveryDateTxt?.text = getString(R.string.click_here_to_select_delivery_date)
            binding!!.storetext.visibility = View.GONE
            zapietviewmodel!!.ShippingCalenderResponse(resources.getString(R.string.shop)).observe(this) { this.DeliveryStatus(it) }
        }
        model!!.message.observe(this, Observer<String> { this.showToast(it) })
        model!!.getGiftCard().observe(this, Observer<Storefront.Mutation> { this.consumeResponseGift(it) })
        model!!.getGiftCardRemove().observe(this, Observer<Storefront.Mutation> { this.consumeResponseGiftRemove(it) })
        model!!.getDiscount().observe(this, Observer<Storefront.Mutation> { this.consumeResponseDiscount(it) })
        model!!.getDiscountError().observe(this, Observer<Boolean> {
            if (it) {
                isSomethingLoading=false
                set_normalcoupon = false
            }
        })
        model!!.wholesale_data.observe(this, { consumewholesaleresponse(it) })
        giftDiscountExpandCollapse()
        binding!!.handler = ClickHandler()
        if (featuresModel.enablecartDiscountlisting) {
            discountlistviewmodel!!.FetchDiscountlistResponse(Urls(MyApplication.context).mid).observe(this@CartList) { this@CartList.discountlistFetchResponse(it) }
        } else {
            binding!!.discountlistsection.visibility = View.GONE
        }
        binding!!.viewallbutton.setOnClickListener {
            val intent = Intent(this@CartList, CouponsListActivity::class.java)
            startActivity(intent)
        }
        binding?.viewallbutton?.setBackgroundColor(Color.parseColor(themeColor))
        binding?.viewallbutton?.setTextColor(Color.parseColor(textColor))
    }
    private fun discountlistFetchResponse(it: com.shopify.apicall.ApiResponse) {
        if (it.data != null) {
            val jsondata = JSONObject(it.data.toString())
            if (jsondata.has("data")) {
                val discarr = jsondata.getJSONArray("data")
                binding!!.discountlistsection.visibility = View.VISIBLE
                discountlistAdapter = DiscountListAdapter()
                discountlistAdapter.setData(this, discarr)
                binding!!.couponlistrecycler.adapter = discountlistAdapter
            }
        } else {
            binding!!.discountlistsection.visibility = View.GONE
        }
    }
    // Tags for wholesale api
    fun CheckCustomerTags(checkout: Storefront.Checkout) {
        wholesaleparams = JsonObject()
        val strSplit: List<String> = MagePrefs.getCustomerTagslist().toString().removePrefix("[").removeSuffix("]").replace(" ", "").split(",")
        //this will store list of tags
        TagsArrayList = strSplit as MutableList<String>
        tagjsonarray = JsonArray()
        //this will loop tags upto thier size
        //creating jsondata and sending to whole sale api
        for (j in 0 until TagsArrayList.size) {
            jsonarray = JsonArray()
            for (i in 0 until checkout.lineItems.edges.size) {
                var variantJsonObject: JsonObject = JsonObject()
                val variant =
                    checkout.lineItems.edges.get(i).node.variant
                val variant_id =
                    variant.id.toString().replace("gid://shopify/ProductVariant/", "")
                        .split("?")[0]
                val product_id =
                    variant.product.id.toString().replace("gid://shopify/Product/", "")
                        .split("?")[0]
                variantJsonObject.addProperty("variant_id", variant_id)
                variantJsonObject.addProperty("product_id", product_id)
                variantJsonObject.addProperty(
                    "quantity",
                    checkout.lineItems.edges.get(i).node.quantity
                )
                variantJsonObject.addProperty(
                    "price",
                    checkout.lineItems.edges.get(i).node.variant.price.amount.toDouble()
                        .toInt()
                )
                if (checkout.lineItems.edges.get(i).node.variant.compareAtPrice != null) {
                    variantJsonObject.addProperty(
                        "compare_at_price",
                        checkout.lineItems.edges.get(i).node.variant.compareAtPrice.amount.toDouble()
                            .toInt()
                    )
                }
                jsonarray?.add(variantJsonObject)
            }
            wholesaleparams!!.add("items", jsonarray)
            tagjsonarray?.add(TagsArrayList[j])
            variantJsonObject = JsonObject()
            variantJsonObject?.add("tags", tagjsonarray)
            wholesaleparams!!.add("customer", variantJsonObject)
        }
        //hitting wholesale api
        model!!.getWholesalepricedata(
            Urls.Authorization,
            Urls.wholsesaleapikey,
            wholesaleparams!!
        )
        wholesale_price_total = 0.00
        compare_At_price = 0.00
        price = 0.00

        Log.d("taglist", "" + TagsArrayList)

    }
    fun consumewholesaleresponse(it: ApiResponse?) {
        if (it?.data != null) {
            var response = it.data.toString()
            for (i in 0 until (it.data as JsonObject).getAsJsonArray("items").size()) {
                var quantity = (it.data.getAsJsonArray("items")
                    .get(i) as JsonObject).get("quantity").toString().toDouble()
                wholesale_price_total = (it.data.getAsJsonArray("items")
                    .get(i) as JsonObject).get("wpd_price").toString()
                    .toDouble() * quantity + wholesale_price_total
                compare_At_price = (it.data.getAsJsonArray("items")
                    .get(i) as JsonObject).get("compare_at_price").toString()
                    .toDouble() * quantity + compare_At_price
                price = (it.data.getAsJsonArray("items")
                    .get(i) as JsonObject).get("price").toString()
                    .toDouble() * quantity + price
                Log.d("whoesaleprice", "" + wholesale_price_total)
            }
            Discount_Percentage = ((price.toInt() - wholesale_price_total.toInt()) / price) * 100
            adapter.setData(
                checkoutdata?.lineItems!!.edges,
                model,
                this,
                object : CartListAdapter.StockCallback {
                    override fun cartWarning(warning: HashMap<String, Boolean>) {
                        cartWarning = warning
                    }
                }, response
            )
            cartlist!!.adapter = adapter
            binding?.discountNote?.visibility = View.VISIBLE
            setBottomData(checkoutdata!!)
        }

    }

    fun giftDiscountExpandCollapse() {
        binding?.giftcardDropdown?.setOnClickListener {
            if (tag == "noexpand") {
                binding?.giftcardSection?.visibility = View.VISIBLE
                binding?.expandCollapse2?.visibility = View.VISIBLE
                binding?.expandCollapse?.visibility = View.GONE
                tag = "expand"
            } else if (tag == "expand") {
                binding?.giftcardSection?.visibility = View.GONE
                binding?.expandCollapse2?.visibility = View.GONE
                binding?.expandCollapse?.visibility = View.VISIBLE
                tag = "noexpand"
            }
        }
        binding?.discountDropdown?.setOnClickListener {
            if (discounttag == "noexpand") {
                binding?.discountcodeSection?.visibility = View.VISIBLE
                binding?.discountExpandCollapse2?.visibility = View.VISIBLE
                binding?.discountExpandCollape?.visibility = View.GONE
                discounttag = "expand"
            } else if (discounttag == "expand") {
                binding?.discountcodeSection?.visibility = View.GONE
                binding?.discountExpandCollapse2?.visibility = View.GONE
                binding?.discountExpandCollape?.visibility = View.VISIBLE
                discounttag = "noexpand"
            }
        }
    }

    private fun consumeRecommended(reponse: GraphQLResponse?) {
        try {
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
                        var recommendedList =
                            result.data!!.productRecommendations as ArrayList<Storefront.Product>?
                        if (recommendedList?.size!! > 0) {
                            Log.d(TAG, "consumeRecommended: " + recommendedList.size)
                            binding!!.shopifyrecommendedSection.visibility = View.VISIBLE
                            setLayout(binding!!.shopifyrecommendedList, "horizontal")
                            personalisedadapter = PersonalisedAdapter()
                            if (!personalisedadapter.hasObservers()) {
                                personalisedadapter.setHasStableIds(true)
                            }
                            var jsonobject: JSONObject = JSONObject()
                            jsonobject.put("item_shape", "rounded")
                            jsonobject.put("item_text_alignment", "center")
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
                else -> {

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun consumestoreresponse(response: com.shopify.apicall.ApiResponse?) {
        if (response?.status == com.shopify.apicall.Status.SUCCESS) {
            var Rule_Id: Int? = null


            if (JSONObject(response.data.toString()).has("code")) {
                val RulesResponse =
                    JSONObject(JSONObject(response.data.toString()).getString("code").toString())
                for (i in 0 until RulesResponse.getJSONArray("rules").length()) {
                    Rule_Id = ((RulesResponse.getJSONArray("rules")
                        .get(i) as JSONObject).getJSONObject("rule") as JSONObject).getInt("id")
                }
                if (Rule_Id != null) {
                    storecreditmodel?.ApplyStoreCredit(
                        Urls.X_Integration_App_Name!!,
                        Rule_Id,
                        MagePrefs.getCustomerID().toString(),
                        Urls.user_id,
                        Urls.token,
                        checkoutdata?.id.toString()
                    )
                }
            }

        }
    }

    fun consumeflistdiscount(
        response: com.shopify.apicall.ApiResponse
    ) {
        if (response.status == com.shopify.apicall.Status.SUCCESS) {
            if (JSONObject(response.data.toString()).has("code")) {
                RulesResponse = JSONObject(response.data.toString()).getString("code").toString()
                binding?.couponCode?.text = RulesResponse
                binding?.couponcodeList!!.visibility = View.VISIBLE
                binding?.couponcodeList!!.setOnClickListener({
                    val sdk = Build.VERSION.SDK_INT
                    if (sdk < Build.VERSION_CODES.HONEYCOMB) {
                        val clipboard =
                            getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.text = binding?.couponCode!!.text
                    } else {
                        val clipboard =
                            getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = ClipData.newPlainText("FlitsCoupon", binding?.couponCode!!.text)
                        clipboard.setPrimaryClip(clip)
                    }
                })
                // binding?.discountCodeBtn!!.performClick()
                Log.d("flitsdiscount", "" + RulesResponse)
            }
        }
    }

    private fun consumeRemoveDiscount(it: Storefront.Mutation?) {
        isSomethingLoading=false
        set_normalcoupon = false
        binding!!.discountCodeBtn.text = getString(R.string.apply)
        binding!!.discountCodeEdt.isEnabled = true
        binding!!.discountCodeEdt.text!!.clear()
        if (featuresModel.WholeSale_Pricing && model!!.isLoggedIn) {
            //if wholesale is enable then we fetch tags and setting them to wholesale api
            CheckCustomerTags(checkoutdata!!)
            set_coupon = false
        }
        Log.d(TAG, "consumeResponseDiscount: " + it!!.checkoutDiscountCodeRemove)
        try {
            customLoader?.dismiss()
            binding!!.discountCodeBtn.text = getString(R.string.apply)
              val bottomData = CartBottomData()
            bottomData.checkoutId = it.checkoutDiscountCodeRemove.checkout.id
            priceModel?.checkoutId = bottomData.checkoutId
            Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
            if (wholesale_price_total != 0.00) {
                bottomData.subtotaltext = resources.getString(R.string.bagtotal)
                priceModel?.subtotaltext = bottomData.subtotaltext
                bottomData.subtotal = CurrencyFormatter.setsymbol(
                    it.checkoutDiscountCodeRemove.checkout.lineItemsSubtotalPrice.amount,
                    it.checkoutDiscountCodeRemove.checkout.lineItemsSubtotalPrice.currencyCode.toString()
                )
                priceModel?.subtotal = bottomData.subtotal
            } else {
                bottomData.subtotaltext = resources.getString(R.string.bagtotal)
                priceModel?.subtotaltext = bottomData.subtotaltext
                bottomData.subtotal = CurrencyFormatter.setsymbol(
                    it.checkoutDiscountCodeRemove.checkout.lineItemsSubtotalPrice.amount,
                    it.checkoutDiscountCodeRemove.checkout.lineItemsSubtotalPrice.currencyCode.toString()
                )
                priceModel?.subtotal = bottomData.subtotal
            }
            if (it.checkoutDiscountCodeRemove.checkout.taxExempt!!) {
                binding!!.taxtext.visibility = View.VISIBLE
                binding!!.tax.visibility = View.VISIBLE
                bottomData.tax = CurrencyFormatter.setsymbol(
                    it.checkoutDiscountCodeRemove.checkout.totalTax.amount,
                    it.checkoutDiscountCodeRemove.checkout.totalTax.currencyCode.toString()
                )
                priceModel?.tax = bottomData.tax
            }
            if (wholesale_price_total != 0.00) {
                bottomData.grandtotal = CurrencyFormatter.setsymbol(
                    it.checkoutDiscountCodeRemove.checkout.totalPrice.amount,
                    it.checkoutDiscountCodeRemove.checkout.totalPrice.currencyCode.toString()
                )
                priceModel?.grandtotal = bottomData.grandtotal
            } else {
                bottomData.grandtotal = CurrencyFormatter.setsymbol(
                    it.checkoutDiscountCodeRemove.checkout.totalPrice.amount,
                    it.checkoutDiscountCodeRemove.checkout.totalPrice.currencyCode.toString()
                )
                priceModel?.grandtotal = bottomData.grandtotal
            }
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)
            binding?.DiscountedPrice?.visibility = View.GONE
            binding?.discountText?.visibility = View.GONE
            checkout_id=it.checkoutDiscountCodeRemove.checkout.id
            bottomData.checkouturl = it.checkoutDiscountCodeRemove.checkout.webUrl
            priceModel?.checkouturl = bottomData.checkouturl
            binding!!.bottomdata = bottomData
            binding!!.root.visibility = View.VISIBLE
            binding!!.discountCodeEdt.text?.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun consumeResponseDiscount(it: Storefront.Mutation?) {
        isSomethingLoading=false
        binding?.discountNote?.visibility = View.GONE
        Log.d(TAG, "consumeResponseDiscount: " + it!!.checkoutDiscountCodeApplyV2)
        set_normalcoupon = true
        binding!!.discountCodeBtn.text = getString(R.string.remove)
        binding!!.discountCodeEdt.isEnabled = false
        try {
            val bottomData = CartBottomData()
            bottomData.checkoutId = it.checkoutDiscountCodeApplyV2.checkout.id
            Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
            bottomData.subtotaltext =
                resources.getString(R.string.subtotaltext) + " ( " + model!!.cartCount + " ${
                    resources.getString(
                        R.string.items
                    )
                } )"
            bottomData.subtotal = CurrencyFormatter.setsymbol(
                it.checkoutDiscountCodeApplyV2.checkout.lineItemsSubtotalPrice.amount,
                it.checkoutDiscountCodeApplyV2.checkout.lineItemsSubtotalPrice.currencyCode.toString()
            )

            bottomData.subtotaltext = resources.getString(R.string.bagtotal)
            bottomData.subtotal = CurrencyFormatter.setsymbol(
                it.checkoutDiscountCodeApplyV2.checkout.lineItemsSubtotalPrice.amount,
                it.checkoutDiscountCodeApplyV2.checkout.lineItemsSubtotalPrice.currencyCode.toString()
            )
            if (it.checkoutDiscountCodeApplyV2.checkout.taxExempt!!) {
                binding!!.taxtext.visibility = View.VISIBLE
                binding!!.tax.visibility = View.VISIBLE
                bottomData.tax = CurrencyFormatter.setsymbol(
                    it.checkoutDiscountCodeApplyV2.checkout.totalTax.amount,
                    it.checkoutDiscountCodeApplyV2.checkout.totalTax.currencyCode.toString()
                )
            }

            bottomData.grandtotal = CurrencyFormatter.setsymbol(
                it.checkoutDiscountCodeApplyV2.checkout.totalPrice.amount,
                it.checkoutDiscountCodeApplyV2.checkout.totalPrice.currencyCode.toString()
            )
            binding?.total?.text = bottomData.grandtotal
            binding?.total?.paintFlags =
                binding?.total?.paintFlags!! and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            binding?.DiscountedTotal?.visibility = View.GONE
            bottomData.checkouturl = it.checkoutDiscountCodeApplyV2.checkout.webUrl

//                 var discountPercentageNode=it.checkoutDiscountCodeApplyV2.checkout.discountApplications.edges.get(0).node.value as Storefront.PricingPercentageValue
//                 var TotalDiscountPercentage=discountPercentageNode.percentage
            if (it.checkoutDiscountCodeApplyV2.checkout.discountApplications.edges.size > 0) {
                if (it.checkoutDiscountCodeApplyV2.checkout.discountApplications.edges[0].node.value is Storefront.PricingPercentageValue) {
                    var discountPriceNode =
                        it.checkoutDiscountCodeApplyV2.checkout.discountApplications.edges[0].node.value as Storefront.PricingPercentageValue
                    TotalDiscountPercentage = discountPriceNode.percentage.toString()
                    //discountname = it.checkoutDiscountCodeApplyV2.checkout.discountApplications.edges[0].node.allocationMethod.name
                    binding?.DiscountedPrice?.text = "-" + TotalDiscountPercentage + "%"
                } else {
                    var discountPriceNode =
                        it.checkoutDiscountCodeApplyV2.checkout.discountApplications.edges[0].node.value as Storefront.MoneyV2
                    TotalDiscountFixedAmount = CurrencyFormatter.setsymbol(
                        discountPriceNode.amount,
                        discountPriceNode.currencyCode.name
                    )

                    binding?.DiscountedPrice?.text = "-" + CurrencyFormatter.setsymbol(
                        discountPriceNode.amount,
                        discountPriceNode.currencyCode.name
                    )
                }
                binding?.DiscountedPrice?.visibility = View.VISIBLE
                binding?.discountText?.visibility = View.VISIBLE
            } else {
                showToast(getString(R.string.notapplicable))
                binding!!.discountCodeBtn.text = getString(R.string.apply)
                binding!!.discountCodeEdt.isEnabled = true
                binding!!.discountCodeEdt.text!!.clear()
                set_normalcoupon = false
            }
            binding!!.bottomdata = bottomData
            binding!!.root.visibility = View.VISIBLE
            checkout_id=it.checkoutDiscountCodeApplyV2.checkout.id
            NormalDiscountedCheckoutId = bottomData.checkoutId.toString()
            NormalDiscountedCheckoutUrl = bottomData.checkouturl
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun consumeResponseGiftRemove(it: Storefront.Mutation?) {
        binding!!.giftext.visibility = View.VISIBLE
        binding!!.gift.visibility = View.VISIBLE
        binding!!.applyGiftBut.text = getString(R.string.apply)
        val bottomData = CartBottomData()
        bottomData.checkoutId = it!!.checkoutGiftCardRemoveV2.checkout.id
        Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
        if (wholesale_price_total != 0.00) {
            bottomData.subtotaltext =
                resources.getString(R.string.bagtotal)

            bottomData.subtotal = CurrencyFormatter.setsymbol(
                wholesale_price_total.toString(),
                it.checkoutGiftCardRemoveV2.checkout.lineItemsSubtotalPrice.currencyCode.toString()
            )
        } else {
            bottomData.subtotaltext = resources.getString(R.string.bagtotal)
            bottomData.subtotal = CurrencyFormatter.setsymbol(
                it.checkoutGiftCardRemoveV2.checkout.lineItemsSubtotalPrice.amount,
                it.checkoutGiftCardRemoveV2.checkout.lineItemsSubtotalPrice.currencyCode.toString()
            )

        }

        if (it.checkoutGiftCardRemoveV2.checkout.taxExempt!!) {
            binding!!.taxtext.visibility = View.VISIBLE
            binding!!.tax.visibility = View.VISIBLE
            bottomData.tax = CurrencyFormatter.setsymbol(
                it.checkoutGiftCardRemoveV2.checkout.totalTax.amount,
                it.checkoutGiftCardRemoveV2.checkout.totalTax.currencyCode.toString()
            )
        }
        if (wholesale_price_total != 0.00) {
            bottomData.grandtotal = CurrencyFormatter.setsymbol(
                wholesale_price_total.toString(),
                it.checkoutGiftCardRemoveV2.checkout.totalPrice.currencyCode.toString()
            )
        } else {
            bottomData.grandtotal = CurrencyFormatter.setsymbol(
                it.checkoutGiftCardRemoveV2.checkout.totalPrice.amount,
                it.checkoutGiftCardRemoveV2.checkout.totalPrice.currencyCode.toString()
            )
        }
        bottomData.checkouturl = it.checkoutGiftCardRemoveV2.checkout.webUrl
        binding!!.bottomdata = bottomData
        binding!!.root.visibility = View.VISIBLE
        showToast(getString(R.string.gift_remove))
    }

    private fun consumeResponseGift(it: Storefront.Mutation?) {
        binding!!.applyGiftBut.text = getString(R.string.remove)
        val bottomData = CartBottomData()
        bottomData.giftcardID = it!!.checkoutGiftCardsAppend.checkout.appliedGiftCards[0].id
        bottomData.checkoutId = it.checkoutGiftCardsAppend.checkout.id
        if (it.checkoutGiftCardsAppend.checkout.appliedGiftCards.size > 0) {
            bottomData.gift = "-" + CurrencyFormatter.setsymbol(
                it.checkoutGiftCardsAppend.checkout.appliedGiftCards.get(0).amountUsed.amount,
                it.checkoutGiftCardsAppend.checkout.appliedGiftCards.get(0).amountUsed.currencyCode.toString()
            )
            binding!!.giftext.text =
                binding!!.giftext.text.toString() + ".." + it.checkoutGiftCardsAppend.checkout.appliedGiftCards.get(
                    0
                ).lastCharacters
            binding!!.giftext.visibility = View.VISIBLE
            binding!!.gift.visibility = View.VISIBLE
        }
        Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
        bottomData.subtotaltext = resources.getString(R.string.bagtotal)
        bottomData.subtotal = CurrencyFormatter.setsymbol(
            it.checkoutGiftCardsAppend.checkout.lineItemsSubtotalPrice.amount,
            it.checkoutGiftCardsAppend.checkout.lineItemsSubtotalPrice.currencyCode.toString()
        )
        if (it.checkoutGiftCardsAppend.checkout.taxExempt!!) {
            binding!!.taxtext.visibility = View.VISIBLE
            binding!!.tax.visibility = View.VISIBLE
            bottomData.tax = CurrencyFormatter.setsymbol(
                it.checkoutGiftCardsAppend.checkout.totalTax.amount,
                it.checkoutGiftCardsAppend.checkout.totalTax.currencyCode.toString()
            )
        }
        if (wholesale_price_total != 0.00) {
            bottomData.grandtotal = CurrencyFormatter.setsymbol(
                (wholesale_price_total - it.checkoutGiftCardsAppend.checkout.appliedGiftCards[0].amountUsedV2.amount.toDouble()).toString(),
                it.checkoutGiftCardRemoveV2.checkout.totalPrice.currencyCode.toString()
            )
        } else {
            bottomData.grandtotal = CurrencyFormatter.setsymbol(
                (it.checkoutGiftCardsAppend.checkout.totalPrice.amount.toDouble() - it.checkoutGiftCardsAppend.checkout.appliedGiftCards.get(
                    0
                ).amountUsedV2.amount.toDouble()).toString(),
                it.checkoutGiftCardsAppend.checkout.totalPrice.currencyCode.toString()
            )
        }
        bottomData.checkouturl = it.checkoutGiftCardsAppend.checkout.webUrl
        binding!!.bottomdata = bottomData
        binding!!.root.visibility = View.VISIBLE
        showToast(getString(R.string.gift_success))
    }

    private fun showToast(msg: String) {
        Toast.makeText(this@CartList, msg, Toast.LENGTH_LONG).show()
    }

    private fun consumeResponse(reponse: Storefront.Checkout) {
        checkout_id=reponse.id
        if (featuresModel.WholeSale_Pricing && model!!.isLoggedIn) {
            CheckCustomerTags(reponse)
        }
        if (reponse.lineItems.edges.size > 0) {
            showCartText(resources.getString(R.string.yourcart)," (" + reponse.lineItems.edges.size + " ${resources.getString(R.string.items)})")
            if (adapter.data != null) {
                adapter.data = reponse.lineItems.edges
                adapter.notifyDataSetChanged()
            } else {
                adapter.setData(
                    reponse.lineItems.edges,
                    model,
                    this,
                    object : CartListAdapter.StockCallback {
                        override fun cartWarning(warning: HashMap<String, Boolean>) {
                            cartWarning = warning
                        }
                    }, ""
                )
                cartlist!!.adapter = adapter
                shimmerStopGridCart()
                shimmer_view_container_grid_cart.visibility = View.GONE
                bottomsection.visibility = View.VISIBLE
                cvDetails.visibility = View.VISIBLE
            }
            setBottomData(reponse)
            delivery_param = model!!.fillDeliveryParam(reponse.lineItems.edges)
            response_data = reponse
            if (SplashViewModel.featuresModel.zapietEnable) {
                binding!!.zepietSection.visibility = View.VISIBLE
                binding!!.localContainer.performClick()
            } else {
                binding!!.zepietSection.visibility = View.GONE
            }
            invalidateOptionsMenu()

        }
    }


    private fun showShipping(reponse: Storefront.Checkout) {
        if (reponse.lineItems.edges.size > 0) {
            Log.d("showmsg", "showShipping: " + reponse.availableShippingRates.ready)
            Log.d("showmsg", "showShipping: " + reponse.availableShippingRates.shippingRates)
        } else {
            showToast(resources.getString(R.string.emptycart))
            finish()
        }
    }

    private fun hideload(pinalertDialog: SweetAlertDialog) {
        Handler().postDelayed({
            pinalertDialog.dismiss()
        }, 4000)
    }

    private fun showload(view: View) {
        var pinalertDialog = SweetAlertDialog(this@CartList, SweetAlertDialog.NORMAL_TYPE)
        pinalertDialog.titleText = view.context?.getString(R.string.note)
        pinalertDialog.contentText = view.context?.getString(R.string.loadings)
        pinalertDialog.show()
        hideload(pinalertDialog)
    }

    private fun checkzip() {
        if (!zipcodes.text.toString().isEmpty()) {
            /*model!!.validateDelivery(delivery_param).observe(
                this@CartList
            ) { this@CartList.validate_delivery(it, response_data.lineItems.edges) }*/
            zapietviewmodel!!.ZapietValidationResponse(resources.getString(R.string.shop)).observe(
                this@CartList
            ) { this@CartList.validate_delivery(it, response_data.lineItems.edges) }
        }
    }

    private fun validate_delivery(
        response: com.shopify.apicall.ApiResponse,
        edges: List<Storefront.CheckoutLineItemEdge>
    ) {
        try {
            Log.i("VALIDATIONAPI", "1 " + response.data)
            if (response.data != null) {
                val res = response.data
                if (res!!.asJsonObject!!.has("productsEligible")) {
                    if (res.asJsonObject.get("productsEligible").asBoolean) {
                        binding!!.zepietSection.visibility = View.VISIBLE
                        //val local_delivery_param = model!!.fillLocalDeliveryParam(edges, binding!!.zipcodes)
                        /*model!!.localDeliveryy(local_delivery_param)
                            .observe(this, { this.localDelivery(it) })*/
                        zapietviewmodel!!.ZapietLocalDeliveryResponse(
                            Urls(application as MyApplication).shopdomain,
                            binding!!.zipcodes.text.toString()
                        )
                            .observe(this, { this.localDelivery(it) })
                    } else {
                        binding!!.zepietSection.visibility = View.GONE
                        binding!!.bottomsection.visibility = View.VISIBLE
                    }
                } else {
                    showToast(resources.getString(R.string.noeligibility))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun storeDelivery(it: com.shopify.apicall.ApiResponse) {
        Log.d("LOCATIONDATAAAA", "" + it.data.toString())
        try {
            if (it.data != null) {
                val res = it.data
                /*if (binding!!.shipnote.visibility == View.GONE) {
                } else {
                    binding!!.deliveryDateTxt.visibility = View.GONE
                }*/
                locations = res!!.asJsonObject.getAsJsonArray("locations")
                if (locations.size() > 0) {
                    location_id = locations.asJsonArray.get(0).asJsonObject.get("id").toString()
                    Log.i("LOCATIONID", "1 " + location_id)
                    var zapiatID = "M=P&L=" + location_id
                    model?.setZepiatID(zapiatID)
                    zapietviewmodel!!.dynamiclocation_id(location_id)
                    zapietviewmodel!!.ZapietDatesResponse(resources.getString(R.string.shop))
                        .observe(this@CartList) { this@CartList.storeDates(it) }
                    locationAdapter.setData(this@CartList,
                        locations,
                        itemClick = object : LocationListAdapter.ItemClick {
                            override fun selectLocation(location_item: JsonObject) {
                                custom_attribute.put(
                                    "Pickup-Location-Id",
                                    location_item.get("id").asString
                                )
                                location_id = location_item.get("id").asString
                                Log.i("LOCATIONID", "2 " + location_id)
                                zapietviewmodel!!.dynamiclocation_id(location_id)
                                custom_attribute.put(
                                    "Pickup-Location-Company",
                                    location_item.get("company_name").asString
                                )
                                custom_attribute.put(
                                    "Pickup-Location-Address-Line-1",
                                    location_item.get("address_line_1").asString
                                )
                                custom_attribute.put(
                                    "Pickup-Location-City",
                                    location_item.get("city").asString
                                )
                                custom_attribute.put(
                                    "Pickup-Location-Postal-Code",
                                    location_item.get("postal_code").asString
                                )
                                custom_attribute.put(
                                    "Pickup-Location-Country",
                                    location_item.get("country").asString
                                )
                                /*custom_attribute.put(
                                    "Pickup-Location-Address-Line-2",
                                    location_item.get("address_line_2").asString
                                )*/
                                //custom_attribute.put("Pickup-Location-Region", location_item.get("region").asString)
                                //val sydney = LatLng(location_item.get("latitude").asDouble, location_item.get("longitude").asDouble)
                                /*if (marker == null) {
                                    var markerOptions = MarkerOptions().position(sydney)
                                        .title("I am here!")
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_MAGENTA));
                                    //marker = mMap.addMarker(markerOptions);
                                } else {
                                    marker!!.setPosition(sydney);
                                }
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(sydney))
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 18f))*/
                                zapietviewmodel!!.ZapietDatesResponse(resources.getString(R.string.shop))
                                    .observe(this@CartList) { this@CartList.storeDates(it) }
                            }
                        })
                    binding!!.locationList.adapter = locationAdapter
                }
                //daysOfWeek = calendar!!.getAsJsonObject("daysOfWeek")
                //interval = calendar.get("interval").asInt
                //loadCalendar(calendar, disabled)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun storeDates(apiResponse: com.shopify.apicall.ApiResponse) {
        Log.i("ZAPIETTTTDATESRESPONSE", "" + apiResponse.data.toString())
        val res: JsonObject = apiResponse.data as JsonObject
        if (res.size() > 0) {
            binding?.deliveryDateTxt?.visibility = View.VISIBLE
        }
        val disabled = res.getAsJsonArray("disabled")
        daysOfWeek = res.getAsJsonObject("daysOfWeek")
        Log.i("WEEKSSS", "" + daysOfWeek)
        interval = res.get("interval").asInt
        loadCalendar(res, disabled)
    }

    private fun DeliveryStatus(it: com.shopify.apicall.ApiResponse) {
        Log.i("ZAPIETTTTRESPONSE", "2 " + it.data)
        if (it.data != null) {
            val res = it.data?.asJsonObject
            if (res != null) {
                val disabled = res.getAsJsonArray("disabled")
                binding!!.deliveryDateTxt.visibility = View.VISIBLE
                //binding?.shipnote?.text = note.toString()
                binding?.shipnote?.text = "SHIPPING"
                binding?.shipnote?.visibility = View.VISIBLE
                binding!!.zipcode.visibility = View.GONE
                binding!!.locationList.visibility = View.GONE
                binding!!.pintext.visibility = View.GONE
                binding!!.deliverAreaTxt.visibility = View.GONE
                binding!!.deliveryTimeSpn.visibility = View.GONE
                binding!!.pintextrue.visibility = View.GONE
                binding!!.pintext.visibility = View.GONE
                /*val calendar = res.asJsonObject
                if(slots.size() > 0){
                    slots = calendar.getAsJsonArray("slots")
                }*/
                loadCalendar(res as JsonObject?, disabled)
                val sdk = Build.VERSION.SDK_INT
                if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                    binding?.shippingContainer?.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@CartList,
                            R.drawable.grey_border
                        )
                    )
                    binding!!.localContainer.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@CartList,
                            R.drawable.black_border
                        )
                    )
                    binding!!.storeContainer.background = ContextCompat.getDrawable(
                        this@CartList,
                        R.drawable.black_border
                    )
                } else {
                    binding?.shippingContainer?.background = ContextCompat.getDrawable(
                        this@CartList,
                        R.drawable.grey_border
                    )
                    binding!!.localContainer.background = ContextCompat.getDrawable(
                        this@CartList,
                        R.drawable.black_border
                    )
                    binding!!.storeContainer.background = ContextCompat.getDrawable(
                        this@CartList,
                        R.drawable.black_border
                    )
                }
            } else {
                binding!!.deliveryDateTxt.visibility = View.GONE
                binding!!.deliveryTimeSpn.visibility = View.GONE
                binding!!.deliverAreaTxt.visibility = View.GONE
                binding!!.zipcode.visibility = View.GONE
                binding!!.locationList.visibility = View.GONE
                binding!!.pintext.visibility = View.GONE
                binding!!.pintextrue.visibility = View.GONE
                showToast(resources.getString(R.string.noshipping))
                val sdk = Build.VERSION.SDK_INT
                if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                    binding?.shippingContainer?.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@CartList,
                            R.drawable.grey_border
                        )
                    )
                    binding!!.localContainer.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@CartList,
                            R.drawable.black_border
                        )
                    )
                    binding!!.storeContainer.background = ContextCompat.getDrawable(
                        this@CartList,
                        R.drawable.black_border
                    )
                } else {
                    binding?.shippingContainer?.background = ContextCompat.getDrawable(
                        this@CartList,
                        R.drawable.grey_border
                    )
                    binding!!.localContainer.background = ContextCompat.getDrawable(
                        this@CartList,
                        R.drawable.black_border
                    )
                    binding!!.storeContainer.background = ContextCompat.getDrawable(
                        this@CartList,
                        R.drawable.black_border
                    )
                }
            }
        } else {
            showToast(resources.getString(R.string.noshipping))
        }
    }

    private fun localDelivery(it: com.shopify.apicall.ApiResponse) {
        try {
            Log.i("VALIDATIONAPI", "2 " + it.data)
            if (it.data != null) {
                val res = it.data
                if (res!!.asJsonObject != null) {
                    if (res.asJsonObject.has("error")) {
                        binding!!.deliveryDateTxt.visibility = View.GONE
                        binding!!.pintext.visibility = View.VISIBLE
                        binding!!.proceedtocheck.visibility = View.GONE
                        binding!!.pintextrue.visibility = View.GONE
                        binding!!.deliveryTimeSpn.visibility = View.GONE
                    } else {
                        binding!!.proceedtocheck.visibility = View.VISIBLE
                        binding!!.pintext.visibility = View.GONE
                        binding!!.pintextrue.visibility = View.VISIBLE
                        var zapiatID = "M=D&L=" + JSONObject(res.toString()).getString("id")
                        model?.setZepiatID(zapiatID)
                        zapietviewmodel!!.ZapietLocalDeliverydatesResponse(Urls(application as MyApplication).shopdomain)
                            .observe(this, { this.localDeliverydates(it) })
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun localDeliverydates(it: com.shopify.apicall.ApiResponse) {
        Log.i("VALIDATIONAPI", "3 " + it.data)
        if (it.data != null) {
            val res = it.data
            if (res!!.asJsonObject != null) {
                binding!!.deliveryDateTxt.visibility = View.VISIBLE
                val calendar = res.asJsonObject
                slots = calendar.getAsJsonArray("slots")
                val disabled = calendar.getAsJsonArray("disabled")
                loadCalendar(calendar, disabled)
            }
        }
    }

    private fun loadCalendar(res: JsonObject?, disabled: JsonArray?) {
        disabledates = ArrayList<Calendar>()
        dpd = DatePickerDialog.newInstance(
            this,
            year, // Initial year selection
            month, // Initial month selection
            day
        )
        dpd.locale = Locale.getDefault()
        dpd.isThemeDark = !HomePageViewModel.isLightModeOn()
        dpd.showYearPickerFirst(false)
        dpd.version = DatePickerDialog.Version.VERSION_2
        var new_calendar: Calendar?
        for (j in 0..disabled!!.size() - 1) {
            if (disabled.isJsonArray) {
                if (disabled[j].toString() == "2") {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            (Calendar.MONDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + i)
                        )
                        disabledates?.add(new_calendar)
                        i += 7
                    }
                } else if (disabled[j].toString() == "3") {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            (Calendar.TUESDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + i)
                        )
                        disabledates?.add(new_calendar)
                        i += 7
                    }
                } else if (disabled[j].toString() == "4") {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            (Calendar.WEDNESDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + i)
                        )
                        disabledates?.add(new_calendar)
                        i += 7
                    }
                } else if (disabled[j].toString() == "5") {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            (Calendar.THURSDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + i)
                        )
                        disabledates?.add(new_calendar)
                        i += 7
                    }
                } else if (disabled[j].toString() == "6") {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            (Calendar.FRIDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + i)
                        )
                        disabledates?.add(new_calendar)
                        i += 7
                    }
                } else if (disabled[j].toString() == "7") {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            (Calendar.SATURDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + i)
                        )
                        disabledates?.add(new_calendar)
                        i += 7
                    }
                } else if (disabled[j].toString() == "1") {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            Calendar.SUNDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + 7 + i
                        )
                        disabledates?.add(new_calendar)
                        i += 7
                    }
                }
            }
        }
        val disabledDays1: Array<Calendar> =
            disabledates?.toArray(arrayOfNulls<Calendar>(disabledates?.size!!)) as Array<Calendar>
        dpd.disabledDays = disabledDays1
        val minDate = res!!.get("minDate").asString?.split("-")
        mincalender.set(Calendar.YEAR, minDate!![0].toInt())
        mincalender.set(Calendar.MONTH, minDate[1].toInt() - 1)
        mincalender.set(Calendar.DAY_OF_MONTH, minDate[2].toInt())
        dpd.minDate = mincalender
    }

    private fun consumeResponse(reponse: ApiResponse) {
        when (reponse.status) {
            Status.SUCCESS -> setYouMayPersonalisedData(reponse.data!!)
            Status.ERROR -> {
                reponse.error!!.printStackTrace()
                Log.i("PERSONAERROR", "" + reponse.error.toString())
            }
        }
    }
    private fun setYouMayPersonalisedData(data: JsonElement) {
        try {
            val jsondata = JSONObject(data.toString())
            Log.i("PERSONALDATA", "" + jsondata)
            if (jsondata.has("query1")) {
                binding!!.personalisedsection.visibility = View.VISIBLE
                setLayout(binding!!.personalised, "horizontal")
                personamodel!!.setPersonalisedData(
                    jsondata.getJSONObject("query1").getJSONArray("products"),
                    padapter,
                    binding!!.personalised
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun setBottomData(checkout: Storefront.Checkout) {
        try {
            val bottomData = CartBottomData()
            bottomData.checkoutId = checkout.id
            checkoutdata = checkout
            checkoutID = checkout.id
            Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
            bottomData.subtotaltext = resources.getString(R.string.bagtotal)
            bottomData.subtotal = CurrencyFormatter.setsymbol(checkout.lineItemsSubtotalPrice.amount, checkout.lineItemsSubtotalPrice.currencyCode.toString())
            if (checkout.taxExempt!!) {
                binding!!.taxtext.visibility = View.VISIBLE
                binding!!.tax.visibility = View.VISIBLE
                bottomData.tax = CurrencyFormatter.setsymbol(
                    checkout.totalTax.amount,
                    checkout.totalTax.currencyCode.toString()
                )
            }
            if (wholesale_price_total != 0.00 && wholesale_price_total != compare_At_price) {
                bottomData.grandtotal = CurrencyFormatter.setsymbol(checkout.totalPrice.amount, checkout.totalPrice.currencyCode.toString())
                var discountprice=checkout.totalPrice.amount.toString().toDouble() - wholesale_price_total.toString().toDouble()
                binding?.DiscountedPrice?.text = "-"+CurrencyFormatter.setsymbol(discountprice.toString(), checkout.totalPrice.currencyCode.toString()) + "(" + Discount_Percentage.toInt() + "%" + ")"

                //random whole sale discounted coupon
                randomString = GenerateRandomString.randomString(10)
                model?.GetWholeSaleDiscountCoupon(
                    Urls((application as MyApplication)).mid,
                    randomString!!,
                    "percentage",
                    "-" + Discount_Percentage.toString()
                )
                binding?.DiscountedTotal?.text = CurrencyFormatter.setsymbol(
                    (wholesale_price_total.toString().toDouble()).toString(),
                    checkout.totalPrice.currencyCode.toString()
                )
                binding?.DiscountedTotal?.setTextColor(resources.getColor(R.color.red))
                binding?.total?.paintFlags =
                    binding?.total?.paintFlags!! or Paint.STRIKE_THRU_TEXT_FLAG
                binding?.DiscountedPrice?.visibility = View.VISIBLE
                binding?.DiscountedTotal?.visibility = View.VISIBLE
                binding?.discountText?.visibility = View.VISIBLE
            } else {
                bottomData.grandtotal = CurrencyFormatter.setsymbol(checkout.totalPrice.amount, checkout.totalPrice.currencyCode.toString())
                binding?.total?.paintFlags = binding?.total?.paintFlags!! and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                if(checkout.discountApplications.edges!=null && checkout.discountApplications.edges.size>0 ){
                    binding?.DiscountedPrice?.visibility = View.VISIBLE
                    binding?.discountText?.visibility = View.VISIBLE
                    if (checkout.discountApplications.edges[0].node.value is Storefront.PricingPercentageValue) {
                        var discountPriceNode = checkout.discountApplications.edges[0].node.value as Storefront.PricingPercentageValue
                        TotalDiscountPercentage = discountPriceNode.percentage.toString()
                        binding?.DiscountedPrice?.text = "-" + TotalDiscountPercentage + "%"
                        binding?.DiscountedPrice?.text = "-" + TotalDiscountPercentage + "%"
                    } else {
                        var discountPriceNode = checkout.discountApplications.edges[0].node.value as Storefront.MoneyV2
                        TotalDiscountFixedAmount = CurrencyFormatter.setsymbol(discountPriceNode.amount, discountPriceNode.currencyCode.name)
                        binding?.DiscountedPrice?.text = "-" + CurrencyFormatter.setsymbol(discountPriceNode.amount, discountPriceNode.currencyCode.name)
                    }
                } else {
                    binding?.DiscountedPrice?.visibility = View.GONE
                    binding?.discountText?.visibility = View.GONE
                }
                binding?.DiscountedTotal?.visibility = View.GONE
            }
            if (featuresModel.Enable_flits_App && model!!.isLoggedIn) {
                storecreditmodel?.GetUnsubscribeCartSpentRules(
                    Urls.X_Integration_App_Name!!,
                    checkout,
                    MagePrefs.getCustomerID().toString(),
                    Urls.user_id,
                    Urls.token
                )
            }
            MagePrefs.setGrandTotal(bottomData.grandtotal ?: "")
            grandTotal = checkout.totalPrice.amount
            bottomData.checkouturl = checkout.webUrl
            binding!!.bottomdata = bottomData
            binding!!.root.visibility = View.VISIBLE
            if (set_normalcoupon == true) {
                binding!!.discountCodeBtn.text = getString(R.string.apply)
                if (binding!!.discountCodeEdt.text!!.isNotEmpty()) {
                    binding!!.discountCodeBtn.performClick()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    object GenerateRandomString {
        const val DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        var RANDOM = Random()
        fun randomString(len: Int): String {
            val sb = java.lang.StringBuilder(len)
            for (i in 0 until len) {
                sb.append(DATA[RANDOM.nextInt(DATA.length)])
            }
            return sb.toString()
        }
    }
    override fun onResume() {
        super.onResume()
        wholesale_price_total = 0.00
        compare_At_price = 0.00
        /* couponCodeList.clear()
         binding?.couponcodeList?.visibility = View.GONE*/
        set_coupon = false
        if (model!!.cartCount > 0) {
            binding!!.content.visibility = View.VISIBLE
            binding!!.nocartsection.visibility = View.GONE
            if (set_normalcoupon == true) {
                binding!!.discountCodeBtn.text = getString(R.string.apply)
                if (binding!!.discountCodeEdt.text!!.isNotEmpty()) {
                    binding!!.discountCodeBtn.performClick()
                }
            } else {
                model!!.prepareCart()
            }
        } else {
            shimmerStopGridCart()
            binding!!.nocartsection.visibility = View.VISIBLE
            binding!!.content.visibility = View.GONE
            //showToast(resources.getString(R.string.emptycart))
            //finish()
        }
        binding!!.continueShopping.setOnClickListener {
            var intentOne = Intent(this, HomePage::class.java)
            startActivity(intentOne)
            Constant.activityTransition(this)
        }
        invalidateOptionsMenu()
        count = 1
    }

    inner class ClickHandler {
        fun loadCheckout(view: View, data: CartBottomData) {
            NormalCheckout()
            try {
                if(!isSomethingLoading){
                    view.isEnabled = false
                    Log.d(TAG, "loadCheckout: " + cartWarning?.values)
                    if (cartWarning?.values?.contains(true) == true) {
                        var alertDialog = SweetAlertDialog(this@CartList, SweetAlertDialog.WARNING_TYPE)
                        alertDialog.titleText = view.context?.getString(R.string.warning_message)
                        alertDialog.contentText = view.context?.getString(R.string.cart_warning)
                        alertDialog.confirmText = view.context?.getString(R.string.dialog_ok)
                        alertDialog.setConfirmClickListener { sweetAlertDialog ->
                            sweetAlertDialog.dismissWithAnimation()
                        }
                        alertDialog.show()
                    } else {
                        val attributeInputs: MutableList<Storefront.AttributeInput> = ArrayList()
                        if (featuresModel.zapietEnable) {
                            if (binding?.deliveryDateTxt?.text == date) {
                                Log.d(TAG, "loadCheckout: 2" + custom_attribute)
                                if (custom_attribute.names()?.length()!! >0){
                                    val iter: Iterator<String> = custom_attribute.keys()
                                    var itemInput: Storefront.AttributeInput? = null
                                    while (iter.hasNext()) {
                                        val key = iter.next()
                                        val value: String = custom_attribute.getString(key)
                                        itemInput = Storefront.AttributeInput(key, value)
                                        attributeInputs.add(itemInput)
                                    }
                                    Log.i("attributeInputs", "cart $attributeInputs")
                                }
                                proceedWithCheckout(attributeInputs,data)
                            }else{
                                showToast(resources.getString(R.string.zip_ship_val))
                            }
                        }else{
                            proceedWithCheckout(attributeInputs,data)
                        }
                    }
                    view.isEnabled = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        fun proceedWithCheckout(
            attributeInputs: MutableList<Storefront.AttributeInput>,
            data: CartBottomData
        ) {
           // binding!!.orderNoteEdt.setText("Magenative")
            if (attributeInputs.size>0 ||!TextUtils.isEmpty(binding!!.orderNoteEdt.text.toString().trim())){
                model!!.prepareCartwithAttribute(attributeInputs, binding!!.orderNoteEdt.text.toString()+"",checkout_id)
            }else {
                if (set_normalcoupon == true) {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(1000)
                        val intent = Intent(this@CartList, CheckoutWeblink::class.java)
                        intent.putExtra("link", NormalDiscountedCheckoutUrl)
                        intent.putExtra("id", NormalDiscountedCheckoutId)
                        startActivity(intent)
                        Constant.activityTransition(this@CartList)
                    }
                }else{
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(1000)
                        val intent = Intent(this@CartList, CheckoutWeblink::class.java)
                        intent.putExtra("link", data.checkouturl)
                        intent.putExtra("id", data.checkoutId.toString())
                        startActivity(intent)
                        Constant.activityTransition(this@CartList)
                    }
                }
            }
        }

        fun LoadDiscount(view: View, bottomData: CartBottomData) {
            //this variable is used for managing normal discount ,because in case of flits we don't switch button to remove
            //so to manage that case we are using this variable
            set_normalcoupon = true
            if ((view as MageNativeButton).text == getString(R.string.apply)) {
                //validation for empty text
                if (TextUtils.isEmpty(binding!!.discountCodeEdt.text.toString().trim())) {
                    binding!!.discountCodeEdt.error = getString(R.string.discount_validation)
                }
                //first condition:In app discount will work if it is enable
                else if (featuresModel.appOnlyDiscount) {
                    isSomethingLoading=true
                    model?.NResponse(
                        Urls(application as MyApplication).mid,
                        binding?.discountCodeEdt?.text.toString()
                    )?.observe(
                        this@CartList,
                        Observer {
                            this.showData(
                                it,
                                bottomData,
                                binding?.discountCodeEdt?.text.toString()
                            )
                        })
                } else {
                    // second condition:Shopify discount will work ,if in app discount is disable
                    isSomethingLoading=true
                    model!!.applyDiscount(
                        bottomData.checkoutId,
                        binding!!.discountCodeEdt.text.toString().trim()
                    )
                }
            } else if (view.text == getString(R.string.remove)) {
                isSomethingLoading=true
                model!!.removeDiscount(bottomData.checkoutId)
                if (featuresModel.Enable_flits_App && model!!.isLoggedIn) {
                    storecreditmodel?.GetUnsubscribeCartSpentRules(
                        Urls.X_Integration_App_Name!!,
                        checkoutdata!!,
                        MagePrefs.getCustomerID().toString(),
                        Urls.user_id,
                        Urls.token
                    )
                }
            }
        }
        fun getResonse(it: Storefront.Checkout?) {
            if (set_coupon == true) {
                //condition for getting flits discounted web url and id
                customLoader?.dismiss()
                DiscountedCheckoutUrl = it?.webUrl
                DiscountedCheckoutId = it?.id.toString()
            } else {
                val bottomData = CartBottomData()
                bottomData.checkoutId = it!!.id
                Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
                bottomData.checkouturl = it.webUrl
                NormalDiscountedCheckoutId = it.id.toString()
                NormalDiscountedCheckoutUrl = it.webUrl
            }
        }
        fun loadpincode(view: View) {
            showload(view)
            checkzip()
        }
        //Flits Discount Code Integration
        fun FiltDiscountCode(view: View, bottomData: CartBottomData) {
            if(!isSomethingLoading){
                LoadDiscount(view, bottomData)
            }
        }
        fun applyGiftCard(view: View, bottomData: CartBottomData) {
            if ((view as MageNativeButton).text == getString(R.string.apply)) {
                if (TextUtils.isEmpty(binding!!.giftcardEdt.text.toString().trim())) {
                    binding!!.giftcardEdt.error = getString(R.string.giftcard_validation)
                } else {
                    model!!.applyGiftCard(
                        binding!!.giftcardEdt.text.toString().trim(),
                        bottomData.checkoutId
                    )
                }
            } else if (view.text == getString(R.string.remove)) {
                model!!.removeGiftCard(bottomData.giftcardID, bottomData.checkoutId)
            }
        }

        fun clearCart(view: View) {
            val alertDialog = SweetAlertDialog(view.context, SweetAlertDialog.WARNING_TYPE)
            var customeview = PopConfirmationBinding.inflate(LayoutInflater.from(view.context))
            customeview.textView.text = getString(R.string.warning_message)
            customeview.textView2.text = getString(R.string.delete_cart_warning)
            alertDialog.hideConfirmButton()
            customeview.okDialog.setOnClickListener {
                customeview.okDialog.isClickable = false
                customeview.textView.text = getString(R.string.done)
                customeview.textView2.text = getString(R.string.deleted)
                alertDialog.showCancelButton(false)
                alertDialog.setConfirmClickListener(null)
                alertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                Handler().postDelayed({
                    model!!.clearCartData()
                    startActivity(
                        Intent(
                            this@CartList,
                            HomePage::class.java
                        )
                    )
                    finish()
                }, 100)
                alertDialog.cancel()
            }
            customeview.noDialog.setOnClickListener {
                customeview.noDialog.isClickable = false
                alertDialog.cancel()
            }
            alertDialog.setCustomView(customeview.root)
            alertDialog.show()
        }

        fun showDiscountDialog(view: View) {
            if (model!!.isLoggedIn) {
                val intent = Intent(this@CartList, ActiveDiscountList::class.java)
                intent.putExtra(
                    "cid",
                    MagePrefs.getCustomerID()!!.replace("gid://shopify/Customer/", "")
                        .split("?")[0]
                )
                intent.putExtra("clientid", Urls.REWARDIFYCLIENTID)
                intent.putExtra("clientsecret", Urls.REWARDIFYCLIENTSECRET)
                startActivity(intent)
            } else {
                showToast(resources.getString(R.string.logginfirst))
            }
        }

        private fun NormalCheckout() {
            try {
                model!!.ResponseAtt().observe(this@CartList, Observer<Storefront.Checkout> {
                    //consumeResponse(it)
                    val bottomData = CartBottomData()
                    bottomData.checkoutId = it.id
                    Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
                    bottomData.checkouturl = it.webUrl
                    binding!!.bottomdata = bottomData
                    /* if (!featuresModel.native_checkout && checkout_disable == false) {*/
                    val intent = Intent(this@CartList, CheckoutWeblink::class.java)
                    intent.putExtra("link", bottomData.checkouturl)
                    intent.putExtra("id", bottomData.checkoutId.toString())
                    startActivity(intent)
                    Constant.activityTransition(this@CartList)
                    /*} else if (checkout_disable == false) {
                        setNativeCheckout(bottomData)
                    }*/
                })

//                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
//            var listdialog = Dialog(this@CartList, R.style.WideDialog)
//            listdialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
//            listdialog.window!!.setLayout(
//                Constraints.LayoutParams.MATCH_PARENT,
//                Constraints.LayoutParams.MATCH_PARENT
//            )
//            var discountCodeLayoutBinding = DataBindingUtil.inflate<DiscountCodeLayoutBinding>(
//                layoutInflater,
//                R.layout.discount_code_layout,
//                null,
//                false
//            )
//            listdialog.setContentView(discountCodeLayoutBinding.root)
//            discountCodeLayoutBinding!!.cancel.setOnClickListener {
//                listdialog.dismiss()
//            }
//            discountCodeLayoutBinding.noBut.setOnClickListener {
//                try {
//                    listdialog.dismiss()
//                    Log.d(TAG, "loadCheckout: 2" + custom_attribute)
//                    val iter: Iterator<String> = custom_attribute.keys()
//                    var itemInput: Storefront.AttributeInput? = null
//                    val attributeInputs: MutableList<Storefront.AttributeInput> =
//                        java.util.ArrayList()
//                    while (iter.hasNext()) {
//                        val key = iter.next()
//                        val value: String = custom_attribute.getString(key)
//                        itemInput = Storefront.AttributeInput(key, value)
//                        attributeInputs.add(itemInput)
//                    }
//                    Log.i("attributeInputs", "cart $attributeInputs")
//                    if (!TextUtils.isEmpty(binding!!.orderNoteEdt.text.toString().trim())) {
//                        model!!.prepareCartwithAttribute(
//                            attributeInputs,
//                            binding!!.orderNoteEdt.text.toString()
//                        )
//                    } else {
//                        model!!.prepareCartwithAttribute(attributeInputs, "")
//                    }
//                    model!!.ResponseAtt().observe(this@CartList, Observer<Storefront.Checkout> {
//                        //consumeResponse(it)
//                        val bottomData = CartBottomData()
//                        bottomData.checkoutId = it.id
//                        Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
//                        bottomData.checkouturl = it.webUrl
//                        binding!!.bottomdata = bottomData
//                        if (!featuresModel.native_checkout) {
//                            val intent = Intent(this@CartList, CheckoutWeblink::class.java)
//                            intent.putExtra("link", bottomData.checkouturl)
//                            intent.putExtra("id", bottomData.checkoutId)
//                            startActivity(intent)
//                            Constant.activityTransition(this@CartList)
//                        } else {
//
//                            setNativeCheckout(bottomData)
//                        }
//                    })
////                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//            discountCodeLayoutBinding.yesBut.setOnClickListener {
//                if (TextUtils.isEmpty(
//                        discountCodeLayoutBinding.discountCodeEdt.text.toString().trim()
//                    )
//                ) {
//                    discountCodeLayoutBinding.discountCodeEdt.error =
//                        getString(R.string.discount_validation)
//                } else {
//                    if (SplashViewModel.featuresModel.appOnlyDiscount) {
//                        model?.NResponse(
//                            Urls(application as MyApplication).mid,
//                            discountCodeLayoutBinding.discountCodeEdt.text.toString()
//                        )?.observe(
//                            this@CartList,
//                            Observer {
//                                this.showData(
//                                    it,
//                                    data,
//                                    discountCodeLayoutBinding.discountCodeEdt.text.toString()
//                                )
//                            })
//                    } else {
//                        model!!.applyDiscount(
//                            data.checkoutId,
//                            discountCodeLayoutBinding.discountCodeEdt.text.toString()
//                        )
//                    }
//
//                    listdialog.dismiss()
//                }
//            }
//            listdialog.show()
        /******************************** DICOUNTCODE SECTION ***************************************/

        private fun showData(response: ApiResponse?, data: CartBottomData, discountCode: String) {
            Log.i("COUPPNCODERESPONSE", "" + response?.data)
            couponCodeData(response?.data, data, discountCode)
        }

        private fun couponCodeData(
            data: JsonElement?,
            data1: CartBottomData,
            discountCode: String
        ) {
            if(data!=null){
                val jsondata = JSONObject(data.toString())
                if (jsondata.has("discount_code") && jsondata.getBoolean("success")) {
                    discountcode = jsondata.getString("discount_code")
                    Log.i("DICOUNTCODE", "" + discountcode)
                    Log.i("CHECKOUTID", "" + data1.checkoutId)
                    model!!.applyDiscount(
                        data1.checkoutId,
                        discountcode.toString()
                    )
                    MagePrefs.setCouponCode(discountCode)
                } else if (!jsondata.getBoolean("success")) {
                    model!!.applyDiscount(
                        data1.checkoutId,
                        discountCode
                    )
                    MagePrefs.setCouponCode(discountCode)
                }
            }else{
                model!!.applyDiscount(
                    data1.checkoutId,
                    discountCode
                )
                MagePrefs.setCouponCode(discountCode)
            }
        }
        /***********************************************************************************/


        var sdk = Build.VERSION.SDK_INT
        fun storeDeliveryClick(view: View) {
            binding?.deliveryDateTxt?.text = getString(R.string.click_here_to_select_delivery_date)
            shipping = false
            binding!!.storetext.visibility = View.VISIBLE
            custom_attribute = JSONObject()
            binding!!.orderNoteEdt.hint = resources.getString(R.string.order_note_hint)
            binding!!.deliveryTimeSpn.visibility = View.GONE
            binding!!.deliverAreaTxt.visibility = View.GONE
            binding!!.zipcode.visibility = View.GONE
            binding!!.pintext.visibility = View.GONE
            binding!!.shipnote.visibility = View.GONE
            binding!!.pintextrue.visibility = View.GONE
            binding!!.proceedtocheck.visibility = View.VISIBLE
            //var store_delivery_param = model!!.fillStoreDeliveryParam(response_data.lineItems.edges, binding!!.zipcodes)
            zapietviewmodel!!.ZapietResponseResponse(resources.getString(R.string.shop))
                .observe(this@CartList) { this@CartList.storeDelivery(it) }
            binding!!.deliverAreaTxt.text = resources.getString(R.string.withdrawal_day_and_time)
            //binding!!.mapContainer.visibility = View.GONE
            binding!!.locationList.visibility = View.VISIBLE
            selected_delivery = "pickup"
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        this@CartList,
                        R.drawable.grey_border
                    )
                )
                binding!!.localContainer.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        this@CartList,
                        R.drawable.black_border
                    )
                )
                binding!!.shippingContainer.background = ContextCompat.getDrawable(
                    this@CartList,
                    R.drawable.black_border
                )
            } else {
                view.background = ContextCompat.getDrawable(
                    this@CartList,
                    R.drawable.grey_border
                )
                binding!!.localContainer.background = ContextCompat.getDrawable(
                    this@CartList,
                    R.drawable.black_border
                )
                binding!!.shippingContainer.background = ContextCompat.getDrawable(
                    this@CartList,
                    R.drawable.black_border
                )
            }
            custom_attribute.put("Checkout-Method", selected_delivery)
        }


        fun localDeliveryClick(view: View) {
            /*if (!customLoader!!.isShowing) {
                customLoader!!.show()
            }*/
            binding?.deliveryDateTxt?.text = getString(R.string.click_here_to_select_delivery_date)
            shipping = false
            custom_attribute = JSONObject()
            binding!!.orderNoteEdt.hint = resources.getString(R.string.order_note_hint)
            binding!!.deliveryTimeSpn.visibility = View.GONE
            binding!!.deliveryDateTxt.visibility = View.GONE
            binding!!.storetext.visibility = View.GONE

            //model!!.validateDelivery(delivery_param).observe(this@CartList, Observer { this@CartList.validate_delivery(it, response_data.lineItems.edges) })
//            var store_delivery_param = model!!.fillStoreDeliveryParam(response_data.lineItems.edges,binding!!.zipcodes)
//            model!!.storeDelivery(store_delivery_param).observe(this@CartList, Observer { this@CartList.storeDelivery(it) })
            //binding!!.mapContainer.visibility = View.GONE

//            model!!.validateDelivery(delivery_param).observe(this@CartList, Observer { this@CartList.validate_delivery(it, response_data.lineItems.edges) })
            binding!!.locationList.visibility = View.GONE
            binding!!.zipcode.visibility = View.VISIBLE
            binding!!.deliverAreaTxt.visibility = View.VISIBLE
            binding!!.shipnote.visibility = View.GONE
            binding!!.deliverAreaTxt.text =
                resources.getString(R.string.please_enter_your_postal_code_to_find_out_if_we_deliver_to_this_area)
            selected_delivery = "delivery"
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        this@CartList,
                        R.drawable.grey_border
                    )
                )
                binding!!.storeContainer.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        this@CartList,
                        R.drawable.black_border
                    )
                )
                binding!!.shippingContainer.background = ContextCompat.getDrawable(
                    this@CartList,
                    R.drawable.black_border
                )


            } else {
                view.background = ContextCompat.getDrawable(
                    this@CartList,
                    R.drawable.grey_border
                )
                binding!!.storeContainer.background = ContextCompat.getDrawable(
                    this@CartList,
                    R.drawable.black_border
                )
                binding!!.shippingContainer.background = ContextCompat.getDrawable(
                    this@CartList,
                    R.drawable.black_border
                )
            }
            custom_attribute.put("Checkout-Method", selected_delivery)
        }

        fun deliveryDatePicker() {
            dpd.show(supportFragmentManager, "Datepickerdialog")
        }
    }


    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        if (binding?.shipnote?.isVisible == true) {
            date = "" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year
            binding!!.deliveryDateTxt.text = date
        } else {
            date = "" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year
            var new_date = simpleDateFormat.parse(date)
            var dayOfTheWeek = dayFormat.format(new_date)
            binding!!.deliveryDateTxt.text = date
            if (selected_delivery == "pickup") {
                custom_attribute.put("Pickup-Date", date.toString())
            } else {
                custom_attribute.put("Delivery-Date", date.toString())
            }
            localdelivery_slots = ArrayList()
            binding!!.deliveryTimeSpn.visibility = View.VISIBLE
            //   binding!!.orderNoteEdt.visibility = View.VISIBLE
            if (selected_delivery.equals("delivery")) {
                for (i in 0..slots.size() - 1) {
                    if (slots[i].asJsonObject.get("day_of_week").asString.equals(
                            dayOfTheWeek,
                            true
                        )
                    ) {
                        localdelivery_slots.add(
                            slots[i].asJsonObject.get("available_from").asString + " - " + slots[i].asJsonObject.get(
                                "available_until"
                            ).asString
                        )
                    }
                }
                binding!!.deliveryTimeSpn.adapter =
                    ArrayAdapter<String>(this, R.layout.spinner_item_layout, localdelivery_slots)
            } else if (selected_delivery.equals("pickup")) {
                val week_object: JsonObject =
                    daysOfWeek.getAsJsonObject(dayOfTheWeek.lowercase(Locale.getDefault()))
                val array = JSONArray()
                val min = week_object.getAsJsonObject("min")
                val min_hour = min.get("hour").asString
                val min_minute = min.get("minute").asString
                array.put(min_hour + ":" + min_minute)
                val max = week_object.getAsJsonObject("max")
                val max_hour = max.get("hour").asString
                val max_minute = max.get("minute").asString
                Log.i("THESETIMESLOTS", "1 $array")
                val df = SimpleDateFormat("HH:mm")
                val cal = Calendar.getInstance()
                var myTime = min_hour + ":" + min_minute
                CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        val d = df.parse(myTime)
                        cal.time = d
                        cal.add(Calendar.MINUTE, interval)
                        myTime = df.format(cal.time)
                        if (myTime != max_hour + ":" + max_minute) {
                            Log.i("THESETIMESLOTS", "loop $array")
                            array.put(myTime)
                        } else {
                            break
                        }
                    }
                }
                array.put(max_hour + ":" + max_minute)
                val array_display_slots: ArrayList<String> = ArrayList()
                for (x in 0 until array.length()) {
                    array_display_slots.add(convert(array.get(x).toString()))
                }
                binding!!.deliveryTimeSpn.adapter =
                    ArrayAdapter<String>(this, R.layout.spinner_item_layout, array_display_slots)
            }
            binding!!.deliveryTimeSpn.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        binding!!.bottomsection.visibility = View.VISIBLE
                        Log.d(TAG, "onItemSelected: " + parent?.selectedItem.toString())
                        selected_slot = parent?.selectedItem.toString()
                        if (selected_delivery == "pickup") {
                            custom_attribute.put("Pickup-Time", selected_slot)
                        } else {
                            custom_attribute.put("Delivery-Time", selected_slot)
                        }
                    }
                }
        }
    }

    fun convert(time: String): String {
        var convertedTime = ""
        try {
            val displayFormat = SimpleDateFormat("hh:mm")
            val parseFormat = SimpleDateFormat("HH:mm")
            val date = parseFormat.parse(time)
            convertedTime = displayFormat.format(date)
            println("convertedTime : $convertedTime")
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return convertedTime
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    fun setNativeCheckout(bottomData: CartBottomData) {

        /*var intent = Intent(this, NativeCheckoutAddressPage::class.java)
        intent.putExtra("checkout_id", bottomData.checkoutId.toString())
        intent.putExtra("grand_total", grandTotal.toString())
        startActivity(intent)*/
//        model!!.populateShipping(input, bottomData.checkoutId)
    }


    override fun onDestroy() {
        super.onDestroy()
        nav_view.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        nav_view.visibility = View.VISIBLE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.m_wish, menu)
        try {

            ////////////////Wishlist menu Item//////////////
            var wishitem = menu.findItem(R.id.wish_item)
            wishitem.setActionView(R.layout.m_wishcount)
            val wishview = wishitem.actionView
            val wishrelative = wishview?.findViewById<RelativeLayout>(R.id.back)
            val wishtext = wishview?.findViewById<TextView>(R.id.count)
            val wishicon = wishview?.findViewById<ImageView>(R.id.cart_icon)
            wishrelative?.backgroundTintList = ColorStateList.valueOf(
                Color.parseColor(
                    HomePageViewModel.count_color
                )
            )
            wishtext?.setTextColor(Color.parseColor(HomePageViewModel.count_textcolor))
            wishicon?.setColorFilter(Color.parseColor(HomePageViewModel.icon_color))

            wishtext!!.text = "" + leftMenuViewModel!!.wishListcount
            wishitem.isVisible = featuresModel.in_app_wishlist
            wishitem.actionView?.setOnClickListener {
                onOptionsItemSelected(wishitem)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

//    fun productCartClick(view: View,data: ListData) {
//        val productIntent = Intent(view.context, ProductView::class.java)
//    productIntent.putExtra("ID", data.product!!.id.toString())
//    productIntent.putExtra("tittle", data.textdata)
//    productIntent.putExtra("product", data.product)
//    productIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//    productIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//    productIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//    view.context.startActivity(productIntent)
//    Constant.activityTransition(view.context)
//
//    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        try{
            val intent = Intent(this, CartList::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            Constant.activityTransition(this)
        }catch (e:Exception){}

    }
}