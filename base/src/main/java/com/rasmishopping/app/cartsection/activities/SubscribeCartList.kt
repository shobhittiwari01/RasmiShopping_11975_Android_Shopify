package com.rasmishopping.app.cartsection.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.rasmishopping.app.FlitsDashboard.StoreCredits.StoreCreditsViewModel
import com.rasmishopping.app.FlitsDashboard.WishlistSection.FlitsWishlistViewModel
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.cartsection.adapters.LocationListAdapter
import com.rasmishopping.app.cartsection.adapters.SubscribeCartListAdapter
import com.rasmishopping.app.cartsection.models.CartBottomData
import com.rasmishopping.app.cartsection.viewmodels.SubscribeCartListModel
import com.rasmishopping.app.checkoutsection.activities.CheckoutWeblink
import com.rasmishopping.app.customviews.MageNativeButton
import com.rasmishopping.app.databinding.SubscribeCartlistBinding
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.personalised.adapters.PersonalisedAdapter
import com.rasmishopping.app.personalised.viewmodels.PersonalisedViewModel
import com.rasmishopping.app.productsection.viewmodels.ProductViewModel
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.*
import com.rasmishopping.app.wishlistsection.activities.WishList
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.m_cartlist.*
import kotlinx.android.synthetic.main.m_cartlist_shimmer_layout_grid.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SubscribeCartList : NewBaseActivity(), DatePickerDialog.OnDateSetListener,
    OnMapReadyCallback {
    @Inject
    lateinit var factory: ViewModelFactory
    private var cartlist: RecyclerView? = null
    var date: String? = null
    var shipping: Boolean? = false
    private var model: SubscribeCartListModel? = null
    private var productmodel: ProductViewModel? = null
    var flistwishmodel: FlitsWishlistViewModel? = null
    var DiscounUrl: String? = null
    var DiscountId: String? = null
    var apply: Boolean? = false
    private var personamodel: PersonalisedViewModel? = null
    private var count: Int = 1
    private var storecreditmodel: StoreCreditsViewModel? = null
    private val TAG = "CartList"
    lateinit var delivery_param: HashMap<String, String>
    var DiscountCodes: MutableList<String> = mutableListOf()
    lateinit var response_data: Storefront.Cart
    private var marker: Marker? = null
    val mincalender = Calendar.getInstance()
    val maxcalender = Calendar.getInstance()
    var calender = Calendar.getInstance()
    val year = calender.get(Calendar.YEAR)
    val month = calender.get(Calendar.MONTH)
    val day = calender.get(Calendar.DAY_OF_MONTH)
    lateinit var dpd: DatePickerDialog
    var simpleDateFormat: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
    var dayFormat: SimpleDateFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
    var disabledates: ArrayList<Calendar>? = null
    lateinit var slots: JsonArray
    lateinit var locations: JsonArray
    lateinit var daysOfWeek: JsonObject
    lateinit var localdelivery_slots: ArrayList<String>
    var interval: Int = 0
    var selected_delivery: String = "delivery"
    var selected_slot: String? = null
    var params: JsonObject = JsonObject()
    var jsonarray: JsonArray = JsonArray()
    private lateinit var mMap: GoogleMap
    private var custom_attribute: JSONObject = JSONObject()
    private var grandTotal: String? = null
    private var cartWarning: HashMap<String, Boolean>? = hashMapOf()
    var set_coupon: Boolean = false

    @Inject
    lateinit var locationAdapter: LocationListAdapter

    @Inject
    lateinit var adapter: SubscribeCartListAdapter

    @Inject
    lateinit var personalisedadapter: PersonalisedAdapter

    @Inject
    lateinit var padapter: PersonalisedAdapter
    private var binding: SubscribeCartlistBinding? = null
    var discountcode: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.subscribe_cartlist, group, true)
        //Shimmer
        shimmerStartGridCart()
        cartlist = setLayout(binding!!.cartlist, "vertical")
        cartlist!!.isNestedScrollingEnabled = false
        showCartText(resources.getString(R.string.yourcart), "")
        showBackButton()
        hidenavbottom()
        hidethemeselector()
        (application as MyApplication).mageNativeAppComponent!!.doSubscribeCartListActivityInjection(
            this
        )
        model = ViewModelProvider(this, factory).get(SubscribeCartListModel::class.java)
        model!!.context = this
        personamodel = ViewModelProvider(this, factory).get(PersonalisedViewModel::class.java)
        personamodel?.activity = this
        flistwishmodel = ViewModelProvider(this, factory).get(FlitsWishlistViewModel::class.java)
        flistwishmodel!!.context = this
        model!!.Response().observe(
            this,
            androidx.lifecycle.Observer<Storefront.Cart> { this.consumeResponse(it) })
        binding!!.locationList.layoutManager = LinearLayoutManager(this)
        productmodel = ViewModelProvider(this, factory).get(ProductViewModel::class.java)
        productmodel!!.context = this
        storecreditmodel = ViewModelProvider(this, factory).get(StoreCreditsViewModel::class.java)
        storecreditmodel!!.context = this
        /* if (SplashViewModel.featuresModel.Enable_flits_App) {
             storecreditmodel!!.spent_rules_data.observe(this, { consumestoreresponse(it) })
         }*/

        binding?.shippingContainer?.setOnClickListener {
            shipping = true
            model!!.DeliveryStatus(Urls(application as MyApplication).mid)
                .observe(this, { this.DeliveryStatus(it) })
        }


        model!!.message.observe(this, { this.showToast(it) })
        model!!.getGiftCard()
            .observe(this, { this.consumeResponseGift(it) })
        model!!.getGiftCardRemove()
            .observe(this, { this.consumeResponseGiftRemove(it) })
        model!!.getDiscountedData()
            .observe(this, { this.consumeResponseDiscount(it) })
        //shopify recommentdation Observer
        model!!.recommendedLiveData.observe(this, { this.consumeRecommended(it) })
//        model!!.shopifyRecommended()
        binding?.localTxt?.textSize = 12f
        binding?.shipTxt?.textSize = 12f
        binding?.storeTxt?.textSize = 12f
        binding!!.subtotaltext.textSize = 12f
        binding!!.subtotal.textSize = 12f
        binding!!.taxtext.textSize = 12f
        binding!!.tax.textSize = 12f
        binding!!.proceedtocheck.textSize = 13f
        binding!!.handler = ClickHandler()
        giftDiscountExpandCollapse()
    }

    fun consumestoreresponse(response: com.shopify.apicall.ApiResponse?) {
        if (response?.status == com.shopify.apicall.Status.SUCCESS) {
            var Rule_Id: Int? = null
            val RulesResponse =
                JSONObject(JSONObject(response.data.toString()).getString("code").toString())
            for (i in 0 until RulesResponse.getJSONArray("rules").length()) {
                Rule_Id = ((RulesResponse.getJSONArray("rules").get(i) as JSONObject).getJSONObject(
                    "rule"
                ) as JSONObject).getInt("id")

            }
            if (Rule_Id != null) {
                storecreditmodel?.ApplyStoreCredit(
                    Urls.X_Integration_App_Name!!,
                    Rule_Id,
                    MagePrefs.getCustomerID().toString(),
                    Urls.user_id,
                    Urls.token,
                    ""
                )
                storecreditmodel?.apply_discount?.observe(this, { consumeflistdiscount(it) })
            }
        }
    }

    fun consumeflistdiscount(response: com.shopify.apicall.ApiResponse) {
        if (response.status == com.shopify.apicall.Status.SUCCESS) {
            val RulesResponse = JSONObject(response.data.toString()).getString("code").toString()
            Log.d("flitsdiscount", "" + RulesResponse)
        }
    }

    fun giftDiscountExpandCollapse() {
        binding?.expandCollapse?.setOnClickListener {
            when (it.tag) {
                "noexpand" -> {
                    binding?.giftcardSection?.visibility = View.VISIBLE
                    binding?.expandCollapse2?.visibility = View.VISIBLE
                    binding?.expandCollapse?.visibility = View.GONE
                }

            }
        }
        binding?.expandCollapse2?.setOnClickListener {
            when (it.tag) {
                "expand" -> {
                    binding?.giftcardSection?.visibility = View.GONE
                    binding?.expandCollapse2?.visibility = View.GONE
                    binding?.expandCollapse?.visibility = View.VISIBLE
                }
            }
        }
        binding?.discountExpandCollape?.setOnClickListener {
            when (it.tag) {
                "noexpand" -> {
                    binding?.discountcodeSection?.visibility = View.VISIBLE
                    binding?.discountExpandCollapse2?.visibility = View.VISIBLE
                    binding?.discountExpandCollape?.visibility = View.GONE
                }

            }
        }
        binding?.discountExpandCollapse2?.setOnClickListener {
            when (it.tag) {
                "expand" -> {
                    binding?.discountcodeSection?.visibility = View.GONE
                    binding?.discountExpandCollapse2?.visibility = View.GONE
                    binding?.discountExpandCollape?.visibility = View.VISIBLE
                }
            }
        }

    }

    private fun consumeResponseDiscount(it: Storefront.Cart?) {
        try {
            if (apply == true) {
                binding!!.discountCodeBtn.text = getString(R.string.apply)
                apply = false
            } else {
                binding!!.discountCodeBtn.text = getString(R.string.remove)
            }
            val bottomData = CartBottomData()
            DiscountId = it?.id.toString()
            Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
            Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
            Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
            bottomData.subtotaltext =
                resources.getString(R.string.subtotaltext) + " ( " + model!!.cartCount + resources.getString(
                    R.string.items
                ) + " ) "
            bottomData.subtotal = CurrencyFormatter.setsymbol(
                it?.cost?.subtotalAmount?.amount!!,
                it.cost?.subtotalAmount?.currencyCode.toString()
            )
            if (it.cost?.totalTaxAmount != null) {
                binding!!.taxtext.visibility = View.VISIBLE
                binding!!.tax.visibility = View.VISIBLE
                bottomData.tax = CurrencyFormatter.setsymbol(
                    it.cost.totalTaxAmount.amount,
                    it.cost.totalTaxAmount.currencyCode.toString()
                )
            }
            bottomData.grandtotal = CurrencyFormatter.setsymbol(
                it.cost.totalAmount.amount,
                it.cost.totalAmount.currencyCode.toString()
            )
            if (it.discountAllocations.size > 0) {
                var discountPriceNode =
                    it.discountAllocations.get(0).discountedAmount
                binding?.DiscountedPrice?.text = CurrencyFormatter.setsymbol(
                    discountPriceNode.amount,
                    discountPriceNode.currencyCode.name)
                binding?.DiscountedPrice?.visibility = View.VISIBLE
                binding?.discountText?.visibility = View.VISIBLE
                binding?.total?.text = bottomData.grandtotal
            } else {
                binding?.DiscountedPrice?.visibility = View.GONE
                binding?.discountText?.visibility = View.GONE
                binding?.total?.text = bottomData.grandtotal
            }
            DiscounUrl = it.checkoutUrl
            binding!!.bottomdata = bottomData
            binding!!.root.visibility = View.VISIBLE
            if (MagePrefs.getGrandTotal() != null) {
                bottomData.subtotaltext =
                    resources.getString(R.string.subtotaltext) + " ( " + model!!.cartCount + " items )"
                bottomData.subtotal = MagePrefs.getGrandTotal()!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
                    var recommendedList =
                        result.data!!.productRecommendations as ArrayList<Storefront.Product>?
                    /*if (recommendedList?.size!! > 0) {
                        Log.d(TAG, "consumeRecommended: " + recommendedList.size)
                        binding!!.shopifyrecommendedSection.visibility = View.VISIBLE
                        setLayout(binding!!.shopifyrecommendedList, "horizontal")
                        personalisedadapter = PersonalisedAdapter()
                        personalisedadapter.setData(
                            flistwishmodel!!,
                            productmodel!!,
                            recommendedList,
                            this,
                            personamodel?.repository!!
                        )
                        binding!!.shopifyrecommendedList.adapter = personalisedadapter
                    }*/
                }
            }
            else -> {

            }
        }
    }

    private fun consumeResponseGiftRemove(it: Storefront.Mutation?) {
        binding!!.applyGiftBut.text = getString(R.string.apply)
        val bottomData = CartBottomData()
        bottomData.checkoutId = it!!.checkoutGiftCardRemoveV2.checkout.id
        Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
        bottomData.subtotaltext =
            resources.getString(R.string.subtotaltext) + " ( " + model!!.cartCount + resources.getString(
                R.string.items
            ) + " ) "
        bottomData.subtotal = CurrencyFormatter.setsymbol(
            it.checkoutGiftCardRemoveV2.checkout.subtotalPrice.amount,
            it.checkoutGiftCardRemoveV2.checkout.subtotalPrice.currencyCode.toString()
        )
        if (it.checkoutGiftCardRemoveV2.checkout.taxExempt!!) {
            binding!!.taxtext.visibility = View.VISIBLE
            binding!!.tax.visibility = View.VISIBLE
            bottomData.tax = CurrencyFormatter.setsymbol(
                it.checkoutGiftCardRemoveV2.checkout.totalTax.amount,
                it.checkoutGiftCardRemoveV2.checkout.totalTax.currencyCode.toString()
            )
        }
        bottomData.grandtotal = CurrencyFormatter.setsymbol(
            it.checkoutGiftCardRemoveV2.checkout.totalPrice.amount,
            it.checkoutGiftCardRemoveV2.checkout.totalPrice.currencyCode.toString()
        )
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
        Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
        bottomData.subtotaltext =
            resources.getString(R.string.subtotaltext) + " ( " + model!!.cartCount + resources.getString(
                R.string.items
            ) + " ) "
        bottomData.subtotal = CurrencyFormatter.setsymbol(
            (it.checkoutGiftCardsAppend.checkout.subtotalPrice.amount.toDouble() - it.checkoutGiftCardsAppend.checkout.appliedGiftCards[0].amountUsed.amount.toDouble()).toString(),
            it.checkoutGiftCardsAppend.checkout.subtotalPrice.currencyCode.toString()
        )
        if (it.checkoutGiftCardsAppend.checkout.taxExempt!!) {
            binding!!.taxtext.visibility = View.VISIBLE
            binding!!.tax.visibility = View.VISIBLE
            bottomData.tax = CurrencyFormatter.setsymbol(
                it.checkoutGiftCardsAppend.checkout.totalTax.amount,
                it.checkoutGiftCardsAppend.checkout.totalTax.currencyCode.toString()
            )
        }
        bottomData.grandtotal = CurrencyFormatter.setsymbol(
            (it.checkoutGiftCardsAppend.checkout.totalPrice.amount.toDouble() - it.checkoutGiftCardsAppend.checkout.appliedGiftCards[0].amountUsed.amount.toDouble()).toString(),
            it.checkoutGiftCardsAppend.checkout.totalPrice.currencyCode.toString()
        )
        bottomData.checkouturl = it.checkoutGiftCardsAppend.checkout.webUrl
        binding!!.bottomdata = bottomData
        binding!!.root.visibility = View.VISIBLE
        showToast(getString(R.string.gift_success))
    }

    private fun showToast(msg: String) {
        Toast.makeText(this@SubscribeCartList, msg, Toast.LENGTH_LONG).show()
    }

    private fun consumeResponse(reponse: Storefront.Cart) {
        if (reponse.lines.edges.size > 0) {
            showCartText(
                resources.getString(R.string.yourcart),
                " (" + reponse.lines.edges.size + " ${resources.getString(R.string.items)})"
            )
            if (adapter.data != null) {
                adapter.data = reponse.lines.edges
                adapter.notifyDataSetChanged()
            } else {
                adapter.setData(
                    reponse.lines.edges,
                    model,
                    this,
                    object : SubscribeCartListAdapter.StockCallback {
                        override fun cartWarning(warning: HashMap<String, Boolean>) {
                            cartWarning = warning
                        }
                    })
                cartlist!!.adapter = adapter
            }
            shimmerStopGridCart()
            shimmer_view_container_grid_cart.visibility = View.GONE
            shimmerStopGridCart()
            shimmer_view_container_grid_cart.visibility = View.GONE
            bottomsection.visibility = View.VISIBLE
            cvDetails.visibility = View.VISIBLE
            setBottomData(reponse)
            delivery_param = model!!.fillDeliveryParam(reponse.lines.edges)
            response_data = reponse
            if (SplashViewModel.featuresModel.zapietEnable) {
                binding!!.zepietSection.visibility = View.VISIBLE
                binding!!.localContainer.performClick()
            } else {
                binding!!.zepietSection.visibility = View.GONE
            }
            invalidateOptionsMenu()
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
        var pinalertDialog = SweetAlertDialog(this@SubscribeCartList, SweetAlertDialog.NORMAL_TYPE)
        pinalertDialog.titleText = view.context?.getString(R.string.note)
        pinalertDialog.contentText = view.context?.getString(R.string.loadings)
        pinalertDialog.show()
        hideload(pinalertDialog)
    }

    private fun checkzip() {
        if (!zipcodes.text.toString().isEmpty()) {

            model!!.validateDelivery(delivery_param).observe(
                this@SubscribeCartList,
                { this@SubscribeCartList.validate_delivery(it, response_data.lines.edges) })

        }
    }

    private fun validate_delivery(
        response: ApiResponse?,
        edges: List<Storefront.CartLineEdge>
    ) {
        try {
            Log.d(TAG, "validate_delivery: " + response!!.data)
            if (response.data != null) {
                var res = response.data
                if (res.asJsonObject!!.has("productsEligible")) {
                    if (res.asJsonObject.get("success").asBoolean && res.asJsonObject.get("productsEligible").asBoolean) {
                        binding!!.zepietSection.visibility = View.VISIBLE
                        var local_delivery_param =
                            model!!.fillLocalDeliveryParam(edges, binding!!.zipcodes)
                        Log.d(TAG, "validate_delivery: " + local_delivery_param)

                        model!!.localDeliveryy(local_delivery_param)
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

    private fun storeDelivery(it: ApiResponse?) {

        /*if (customLoader!!.isShowing) {
            customLoader!!.dismiss()
        }*/
        try {
            if (it!!.data != null) {
//                binding!!.deliveryDateTxt.visibility = View.VISIBLE
                var res = it.data
                if (res!!.asJsonObject.get("success").asBoolean == true) {
                    if (binding!!.shipnote.visibility == View.GONE) {
//                        binding!!.deliveryDateTxt.visibility = View.VISIBLE
                    } else {
                        binding!!.deliveryDateTxt.visibility = View.GONE
                    }

                    var calendar = res.asJsonObject.getAsJsonObject("calendar")
                    var disabled = calendar.getAsJsonArray("disabled")
                    locations = res.asJsonObject.getAsJsonArray("locations")
                    if (locations.size() > 0) {
                        locationAdapter.setData(
                            this@SubscribeCartList,
                            locations,
                            itemClick = object : LocationListAdapter.ItemClick {
                                override fun selectLocation(location_item: JsonObject) {
                                    custom_attribute.put(
                                        "Pickup-Location-Id",
                                        location_item.get("id").asString
                                    )
                                    custom_attribute.put(
                                        "Pickup-Location-Company",
                                        location_item.get("company_name").asString
                                    )
                                    custom_attribute.put(
                                        "Pickup-Location-Address-Line-1",
                                        location_item.get("address_line_1").asString
                                    )
                                    /*custom_attribute.put(
                                        "Pickup-Location-Address-Line-2",
                                        location_item.get("address_line_2").asString
                                    )*/
                                    custom_attribute.put(
                                        "Pickup-Location-City",
                                        location_item.get("city").asString
                                    )
//
//                                    custom_attribute.put("Pickup-Location-Region", location_item.get("region").asString)
//

                                    custom_attribute.put(
                                        "Pickup-Location-Postal-Code",
                                        location_item.get("postal_code").asString
                                    )
                                    custom_attribute.put(
                                        "Pickup-Location-Country",
                                        location_item.get("country").asString
                                    )
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
                                }
                            })
                        binding!!.locationList.adapter = locationAdapter
                    }

                    daysOfWeek = calendar.getAsJsonObject("daysOfWeek")
                    interval = calendar.get("interval").asInt
                    loadCalendar(calendar, disabled)
                } else {
                    Toast.makeText(
                        this,
                        res.asJsonObject.get("err_msg").asString,
                        Toast.LENGTH_SHORT
                    ).show()
                    binding?.deliveryOption?.visibility = View.GONE

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun DeliveryStatus(it: ApiResponse?) {

        if (it!!.data != null) {
            val res = it.data
            if (res!!.asJsonObject.get("is_installed").asBoolean == true) {
                val note =
                    res.asJsonObject.get("data").asJsonObject.get("translations").asJsonObject.get(
                        "shipping"
                    ).asJsonObject.get("note").asString


                binding?.shipnote?.text = note.toString()
                binding?.shipnote?.visibility = View.VISIBLE
                binding!!.deliveryTimeSpn.visibility = View.GONE
                binding!!.deliverAreaTxt.visibility = View.GONE
                binding!!.deliveryDateTxt.visibility = View.GONE
                binding!!.zipcode.visibility = View.GONE
                binding!!.locationList.visibility = View.GONE
                binding!!.pintext.visibility = View.GONE
                var sdk = android.os.Build.VERSION.SDK_INT
                /*if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    binding?.shippingContainer?.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@SubscribeCartList,
                            R.drawable.grey_border
                        )
                    )
                    binding!!.localContainer.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@SubscribeCartList,
                            R.drawable.black_border
                        )
                    )
                    binding!!.storeContainer.background = ContextCompat.getDrawable(
                        this@SubscribeCartList,
                        R.drawable.black_border
                    )
                    shipping = true
                } else {
                    binding?.shippingContainer?.background = ContextCompat.getDrawable(
                        this@SubscribeCartList,
                        R.drawable.grey_border
                    )
                    binding!!.localContainer.background = ContextCompat.getDrawable(
                        this@SubscribeCartList,
                        R.drawable.black_border
                    )
                    binding!!.storeContainer.background = ContextCompat.getDrawable(
                        this@SubscribeCartList,
                        R.drawable.black_border
                    )
                }*/

                shipping = true
            } else {
                /**
                 * Developer:Prakhar Dubey
                 * Date:4/04/2022
                 * Fixed Zapiet Shipping option not clicking issue
                 */
                var sdk = android.os.Build.VERSION.SDK_INT
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    binding?.shippingContainer?.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@SubscribeCartList,
                            R.drawable.grey_border
                        )
                    )
                    binding!!.localContainer.setBackgroundDrawable(
                        ContextCompat.getDrawable(
                            this@SubscribeCartList,
                            R.drawable.black_border
                        )
                    )
                    binding!!.storeContainer.background = ContextCompat.getDrawable(
                        this@SubscribeCartList,
                        R.drawable.black_border
                    )
                } else {
                    binding?.shippingContainer?.background = ContextCompat.getDrawable(
                        this@SubscribeCartList,
                        R.drawable.grey_border
                    )
                    binding!!.localContainer.background = ContextCompat.getDrawable(
                        this@SubscribeCartList,
                        R.drawable.black_border
                    )
                    binding!!.storeContainer.background = ContextCompat.getDrawable(
                        this@SubscribeCartList,
                        R.drawable.black_border
                    )
                }

                binding!!.deliveryTimeSpn.visibility = View.GONE
                binding!!.deliverAreaTxt.visibility = View.GONE
                binding!!.deliveryDateTxt.visibility = View.GONE
                binding!!.zipcode.visibility = View.GONE
                binding!!.locationList.visibility = View.GONE
                binding!!.pintext.visibility = View.GONE
                showToast(resources.getString(R.string.noshipping))
            }

        } else {
            showToast(resources.getString(R.string.noshipping))
        }


    }

    private fun localDelivery(it: ApiResponse?) {
        try {
            /* if (customLoader!!.isShowing) {
                 customLoader!!.dismiss()
             }*/
            binding!!.deliveryDateTxt.visibility = View.GONE
            Log.i("ALLLLLDATAAAAA", "" + it!!.data)
            if (it.data != null) {
                binding!!.deliveryDateTxt.visibility = View.VISIBLE
                //binding!!.deliveryDateTxt.visibility = View.VISIBLE
                val res = it.data
                if (res.asJsonObject.get("success").asBoolean == true) {
                    binding!!.deliveryDateTxt.visibility = View.VISIBLE
                    binding!!.proceedtocheck.visibility = View.VISIBLE
                    binding!!.pintext.visibility = View.GONE
                    binding!!.pintextrue.visibility = View.VISIBLE
                    val calendar = res.asJsonObject.getAsJsonObject("calendar")
                    val disabled = calendar.getAsJsonArray("disabled")
                    slots = calendar.getAsJsonArray("slots")
                    loadCalendar(calendar, disabled)
                } else if (res.asJsonObject.get("success").asBoolean == false) {
                    binding!!.deliveryDateTxt.visibility = View.GONE
                    binding!!.pintext.visibility = View.VISIBLE
                    binding!!.proceedtocheck.visibility = View.GONE
                    binding!!.pintextrue.visibility = View.GONE
                    binding!!.deliveryTimeSpn.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadCalendar(calendar: JsonObject?, disabled: JsonArray?) {
        disabledates = ArrayList<Calendar>()
        dpd = DatePickerDialog.newInstance(
            this,
            year, // Initial year selection
            month, // Initial month selection
            day
        )
        dpd.locale = Locale.getDefault()
        dpd.isThemeDark = false
        dpd.showYearPickerFirst(false)
        dpd.version = DatePickerDialog.Version.VERSION_2
        var new_calendar: Calendar? = null
        for (j in 0..disabled!!.size() - 1) {
            if (disabled.isJsonArray) {
                if (disabled[j].toString().equals("2")) {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            (Calendar.MONDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + i)
                        )
                        disabledates?.add(new_calendar)
                        i = i + 7
                    }

                } else if (disabled[j].toString().equals("3")) {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            (Calendar.TUESDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + i)
                        )
                        disabledates?.add(new_calendar)
                        i = i + 7
                    }
                } else if (disabled[j].toString().equals("4")) {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            (Calendar.WEDNESDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + i)
                        )
                        disabledates?.add(new_calendar)
                        i = i + 7
                    }
                } else if (disabled[j].toString().equals("5")) {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            (Calendar.THURSDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + i)
                        )
                        disabledates?.add(new_calendar)
                        i = i + 7
                    }
                } else if (disabled[j].toString().equals("6")) {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            (Calendar.FRIDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + i)
                        )
                        disabledates?.add(new_calendar)
                        i = i + 7
                    }
                } else if (disabled[j].toString().equals("7")) {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            (Calendar.SATURDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + i)
                        )
                        disabledates?.add(new_calendar)
                        i = i + 7
                    }
                } else if (disabled[j].toString().equals("1")) {
                    val weeks = 5
                    var i = 0
                    while (i < weeks * 7) {
                        new_calendar = Calendar.getInstance()
                        new_calendar.add(
                            Calendar.DAY_OF_YEAR,
                            Calendar.SUNDAY - new_calendar.get(Calendar.DAY_OF_WEEK) + 7 + i
                        )
                        disabledates?.add(new_calendar)
                        i = i + 7
                    }
                }
            }
        }
        val maxDate = ""
        val minDate = calendar!!.get("minDate").asString?.split("-")
        val disabledDays1: Array<Calendar> =
            disabledates?.toArray(arrayOfNulls<Calendar>(disabledates?.size!!)) as Array<Calendar>
        dpd.disabledDays = disabledDays1
        mincalender.set(Calendar.YEAR, minDate!![0].toInt())
        mincalender.set(Calendar.MONTH, minDate[1].toInt() - 1)
        mincalender.set(Calendar.DAY_OF_MONTH, minDate[2].toInt())
        dpd.minDate = mincalender

        maxcalender.set(Calendar.YEAR, maxDate[0].code)
        maxcalender.set(Calendar.MONTH, maxDate[1].code - 1)
        maxcalender.set(Calendar.DAY_OF_MONTH, maxDate[2].code)
        dpd.maxDate = maxcalender

    }

    private fun consumeResponse(reponse: ApiResponse) {
        when (reponse.status) {
            Status.SUCCESS -> setPersonalisedData(reponse.data!!)
            Status.ERROR -> {
                reponse.error!!.printStackTrace()
                showToast(resources.getString(R.string.errorString))
            }
        }
    }

    private fun Response(reponse: ApiResponse) {
        when (reponse.status) {

            Status.ERROR -> {
                reponse.error!!.printStackTrace()
                showToast(resources.getString(R.string.errorString))
            }
        }
    }

    private fun setPersonalisedData(data: JsonElement) {
        try {
            val jsondata = JSONObject(data.toString())
            if (jsondata.has("query1")) {
                binding!!.personalisedsection.visibility = View.VISIBLE
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


    private fun setBottomData(checkout: Storefront.Cart) {
        try {
            val bottomData = CartBottomData()
            bottomData.checkoutId = checkout.id
            Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
            bottomData.subtotaltext =
                resources.getString(R.string.subtotaltext) + " ( " + model!!.cartCount + resources.getString(
                    R.string.items
                ) + " ) "
            bottomData.subtotal = CurrencyFormatter.setsymbol(
                checkout.cost.subtotalAmount.amount,
                checkout.cost.subtotalAmount.currencyCode.toString()
            )
            if (checkout.cost.totalTaxAmount != null) {
                binding!!.taxtext.visibility = View.VISIBLE
                binding!!.tax.visibility = View.VISIBLE
                bottomData.tax = CurrencyFormatter.setsymbol(
                    checkout.cost.totalTaxAmount.amount,
                    checkout.cost.totalTaxAmount.currencyCode.toString()
                )
            }
            if (checkout.discountAllocations.size > 0) {
                var discountPriceNode =
                    checkout.discountAllocations.get(0).discountedAmount
                binding?.DiscountedPrice?.text = CurrencyFormatter.setsymbol(
                    discountPriceNode.amount,
                    discountPriceNode.currencyCode.name

                )
                binding?.DiscountedPrice?.visibility = View.VISIBLE
                binding?.discountText?.visibility = View.VISIBLE
                binding?.total?.text = bottomData.grandtotal

            } else {
                binding?.DiscountedPrice?.visibility = View.GONE
                binding?.discountText?.visibility = View.GONE
                binding?.total?.text = bottomData.grandtotal
            }
            bottomData.grandtotal = CurrencyFormatter.setsymbol(
                checkout.cost.totalAmount.amount,
                checkout.cost.totalAmount.currencyCode.toString()
            )
//            storecreditmodel?.GetSubscribeCartSpentRules(checkout,MagePrefs.getCustomerID().toString())

            Log.d("cartencodedData", "" + getBase64Encode(params.toString()))

            Log.d("cartdata", "" + params)
            MagePrefs.setGrandTotal(bottomData.grandtotal ?: "")
            grandTotal = checkout.cost.totalAmount.amount
            binding?.DiscountedPrice?.visibility = View.GONE
            binding?.discountText?.visibility = View.GONE
            binding?.total?.text = bottomData.grandtotal
            bottomData.checkouturl = checkout.checkoutUrl
            binding!!.bottomdata = bottomData
            binding!!.root.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getBase64Encode(id: String): String {
        var id = id
        val data = Base64.encode(id.toByteArray(), Base64.DEFAULT)
        try {
            id = String(data, Charset.defaultCharset()).trim { it <= ' ' }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return id
    }


    override fun onResume() {
        super.onResume()
        set_coupon = false
        binding!!.discountCodeBtn.text = getString(R.string.apply)
        binding?.DiscountedPrice?.visibility = View.GONE
        binding?.discountText?.visibility = View.GONE
        if (model!!.cartCount > 0) {
            model!!.prepareCart()
        } else {
            showToast(resources.getString(R.string.emptycart))
            finish()
        }
        invalidateOptionsMenu()
        count = 1
    }

    inner class ClickHandler {
        fun loadCheckout(view: View, data: CartBottomData) {
            Log.d(TAG, "loadCheckout: " + cartWarning?.values)
            if (cartWarning?.values?.contains(true) == true) {
                var alertDialog =
                    SweetAlertDialog(this@SubscribeCartList, SweetAlertDialog.WARNING_TYPE)
                alertDialog.titleText = view.context?.getString(R.string.warning_message)
                alertDialog.contentText = view.context?.getString(R.string.cart_warning)
                alertDialog.confirmText = view.context?.getString(R.string.dialog_ok)
                alertDialog.setConfirmClickListener { sweetAlertDialog ->
                    sweetAlertDialog.dismissWithAnimation()
                }
                alertDialog.show()
            } else if (SplashViewModel.featuresModel.zapietEnable) {
                if (binding?.deliveryDateTxt?.text == date || binding?.shipnote?.visibility == View.VISIBLE) {
                    showApplyCouponDialog(data)
                } else {
                    if (shipping == false) {
                        showToast(resources.getString(R.string.zip_ship_val))
                    } else {
                        showApplyCouponDialog(data)
                    }
                }
            } else {
                showApplyCouponDialog(data)
            }
        }

        fun loadpincode(view: View) {
            showload(view)
            checkzip()
        }

        fun payWithGpay(view: View, data: CartBottomData) {
            val idempotencyKey = UUID.randomUUID().toString()
            val billingAddressInput: Storefront.MailingAddressInput =
                Storefront.MailingAddressInput()
            billingAddressInput.address1 = "3/446 Gomti Nagar Vishvash Khand Lucknow"
            billingAddressInput.address2 = "3/446 Gomti Nagar Vishvash Khand Lucknow"
            billingAddressInput.city = "Lucknow"
            billingAddressInput.company = ""
            billingAddressInput.country = "India"
            billingAddressInput.firstName = "Abhishek"
            billingAddressInput.lastName = "Dubey"
            billingAddressInput.zip = "226010"

            model?.doGooglePay(data.checkoutId, "100", idempotencyKey, billingAddressInput)
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
            var alertDialog =
                SweetAlertDialog(this@SubscribeCartList, SweetAlertDialog.WARNING_TYPE)
            alertDialog.titleText = getString(R.string.warning_message)
            alertDialog.contentText = getString(R.string.delete_cart_warning)
            alertDialog.confirmText = getString(R.string.yes_delete)
            alertDialog.cancelText = getString(R.string.no)
            alertDialog.showCancelButton(true)
            alertDialog.setConfirmClickListener { sweetAlertDialog ->
                sweetAlertDialog.setTitleText(getString(R.string.deleted))
                    .setContentText(getString(R.string.cart_deleted_message))
                    .setConfirmText(getString(R.string.done))
                    .showCancelButton(false)
                    .setConfirmClickListener(null)
                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                Handler().postDelayed({
                    model!!.clearCartData()
                    finish()
                }, 100)

            }
            alertDialog.show()
        }

        private fun showApplyCouponDialog(data: CartBottomData) {
//            var listdialog = Dialog(this@SubscribeCartList, R.style.WideDialog)
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

            try {
//                    listdialog.dismiss()

                Log.d(TAG, "loadCheckout: 2" + custom_attribute)
                val iter: Iterator<String> = custom_attribute.keys()
                var itemInput: Storefront.AttributeInput? = null
                val attributeInputs: MutableList<Storefront.AttributeInput> =
                    ArrayList()
                while (iter.hasNext()) {
                    val key = iter.next()
                    val value: String = custom_attribute.getString(key)
                    itemInput = Storefront.AttributeInput(key, value)
                    attributeInputs.add(itemInput)
                }
                Log.i("attributeInputs", "cart $attributeInputs")
                if (!TextUtils.isEmpty(binding!!.orderNoteEdt.text.toString().trim())) {
                    model!!.prepareCartwithAttribute(
                        attributeInputs,
                        binding!!.orderNoteEdt.text.toString()
                    )
                } else {
                    model!!.prepareCartwithAttribute(attributeInputs, "")
                }
                if (set_coupon == true) {
                    val intent = Intent(this@SubscribeCartList, CheckoutWeblink::class.java)
                    intent.putExtra("link", DiscounUrl)
                    intent.putExtra("id", DiscountId)
                    startActivity(intent)
                    Constant.activityTransition(this@SubscribeCartList)
                } else {
                    model!!.ResponseAtt().observe(this@SubscribeCartList, {
                        //consumeResponse(it)
                        val bottomData = CartBottomData()
                        bottomData.checkoutId = it.id
                        Log.d(TAG, "setBottomData: " + bottomData.checkoutId)
                        bottomData.checkouturl = it.checkoutUrl
                        binding!!.bottomdata = bottomData
                        val intent = Intent(this@SubscribeCartList, CheckoutWeblink::class.java)
                        intent.putExtra("link", bottomData.checkouturl)
                        intent.putExtra("id", bottomData.checkoutId)
                        startActivity(intent)
                        Constant.activityTransition(this@SubscribeCartList)
                    })

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
//
//            discountCodeLayoutBinding.yesBut.setOnClickListener {
//                if (TextUtils.isEmpty(discountCodeLayoutBinding.discountCodeEdt.text.toString().trim())) {
//                    discountCodeLayoutBinding.discountCodeEdt.error =
//                        getString(R.string.discount_validation)
//                } else {
//                    DiscountCodes.add(discountCodeLayoutBinding.discountCodeEdt.text.toString().trim())
//                    if (SplashViewModel.featuresModel.appOnlyDiscount) {
//                        model?.NResponse(
//                            Urls(application as MyApplication).mid,
//                            discountCodeLayoutBinding.discountCodeEdt.text.toString()
//                        )?.observe(
//                            this@SubscribeCartList,
//                            {
//                                this.showData(
//                                    it,
//                                    data,
//                                    discountCodeLayoutBinding.discountCodeEdt.text.toString()
//                                )
//                            })
//                    } else {
//
//
//                        model!!.applyDiscount(
//                            data.checkoutId,
//                            discountCodeLayoutBinding.discountCodeEdt.text.toString()
//                        )
//
////                        model?.prepareCartwithDiscount(DiscountCodes)
//                    }
//
//                    listdialog.dismiss()
//                }
//            }
//            listdialog.show()
        }

        /******************************** DICOUNTCODE SECTION ***************************************/
        fun LoadDiscount(view: View, bottomData: CartBottomData) {
            set_coupon = true
            //this variable is used for managing normal discount ,because in case of flits we don't switch button to remove
            //so to manage that case we are using this variable
            var discount_code: MutableList<String> = mutableListOf()
            discount_code.add(binding!!.discountCodeEdt.text.toString().trim())
            if ((view as MageNativeButton).text == getString(R.string.apply)) {
                //validation for empty text
                if (TextUtils.isEmpty(binding!!.discountCodeEdt.text.toString().trim())) {
                    binding!!.discountCodeEdt.error = getString(R.string.discount_validation)
                }
                //first condition:In app discount will work if it is enable
                else if (SplashViewModel.featuresModel.appOnlyDiscount) {
                    model?.NResponse(
                        Urls(application as MyApplication).mid,
                        binding?.discountCodeEdt?.text.toString()
                    )?.observe(
                        this@SubscribeCartList,
                        {
                            this.showData(
                                it,
                                bottomData,
                                binding?.discountCodeEdt?.text.toString()
                            )
                        })
                } else {
                    // second condition:Shopify discount will work ,if in app discount is disable
                    model!!.prepareCartwithDiscount(
                        discount_code

                    )
                }
            } else if (view.text == getString(R.string.remove)) {
                discount_code.clear()
                binding?.DiscountedPrice?.visibility = View.GONE
                binding?.discountText?.visibility = View.GONE
                model!!.prepareCartwithDiscount(
                    discount_code

                )
                binding?.discountCodeBtn?.text = getString(R.string.apply)
                apply = true
            }


        }

        private fun showData(response: ApiResponse?, data: CartBottomData, discountCode: String) {
            Log.i("COUPPNCODERESPONSE", "" + response?.data)
            couponCodeData(response?.data, data, discountCode)
        }

        private fun couponCodeData(
            data: JsonElement?,
            data1: CartBottomData,
            discountCode: String
        ) {
            var discount_code: MutableList<String> = mutableListOf()
            discount_code.add(binding!!.discountCodeEdt.text.toString().trim())
            val jsondata = JSONObject(data.toString())
            if (jsondata.has("discount_code") && jsondata.getBoolean("success")) {
                discount_code.clear()
                discountcode = jsondata.getString("discount_code")
                discount_code.add(discountcode.toString())
                Log.i("DICOUNTCODE", "" + discountcode)
                Log.i("CHECKOUTID", "" + data1.checkoutId)
                model!!.prepareCartwithDiscount(
                    discount_code
                )
                MagePrefs.setCouponCode(discountCode)
            } else if (!jsondata.getBoolean("success")) {
                model!!.prepareCartwithDiscount(
                    discount_code
                )

            }
        }

        /***********************************************************************************/

        fun getResonse(it: Storefront.Checkout?) {
            if (count == 1) {
                val intent = Intent(this@SubscribeCartList, CheckoutWeblink::class.java)
                intent.putExtra("link", it?.webUrl)
                intent.putExtra("id", it?.id.toString())
                startActivity(intent)
                Constant.activityTransition(this@SubscribeCartList)
                count++
            }
        }

        var sdk = android.os.Build.VERSION.SDK_INT
        fun storeDeliveryClick(view: View) {
            /*if (!customLoader!!.isShowing) {
                customLoader!!.show()
            }*/
            custom_attribute = JSONObject()
            binding?.deliveryDateTxt?.visibility = View.VISIBLE
            binding!!.deliveryDateTxt.text =
                resources.getString(R.string.click_here_to_select_delivery_date)
            binding!!.orderNoteEdt.hint = resources.getString(R.string.order_note_hint)
            binding!!.deliveryTimeSpn.visibility = View.GONE
            binding!!.deliverAreaTxt.visibility = View.GONE
            binding!!.zipcode.visibility = View.GONE
            binding!!.pintext.visibility = View.GONE
            binding!!.shipnote.visibility = View.GONE
            binding!!.pintextrue.visibility = View.GONE
            binding!!.proceedtocheck.visibility = View.VISIBLE

            var store_delivery_param =
                model!!.fillStoreDeliveryParam(response_data.lines.edges, binding!!.zipcodes)
            model!!.storeDelivery(store_delivery_param)
                .observe(this@SubscribeCartList, { this@SubscribeCartList.storeDelivery(it) })

            binding!!.deliverAreaTxt.text = resources.getString(R.string.withdrawal_day_and_time)

            //binding!!.mapContainer.visibility = View.GONE
            binding!!.locationList.visibility = View.VISIBLE
            selected_delivery = "pickup"
            if (++sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        this@SubscribeCartList,
                        R.drawable.grey_border
                    )
                )
                binding!!.localContainer.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        this@SubscribeCartList,
                        R.drawable.black_border
                    )
                )
                binding!!.shippingContainer.background = ContextCompat.getDrawable(
                    this@SubscribeCartList,
                    R.drawable.black_border
                )
                shipping = false
            } else {
                view.background = ContextCompat.getDrawable(
                    this@SubscribeCartList,
                    R.drawable.grey_border
                )
                binding!!.localContainer.background = ContextCompat.getDrawable(
                    this@SubscribeCartList,
                    R.drawable.black_border
                )
                binding!!.shippingContainer.background = ContextCompat.getDrawable(
                    this@SubscribeCartList,
                    R.drawable.black_border
                )
            }
            shipping = false
            custom_attribute.put("Checkout-Method", selected_delivery)
        }


        fun localDeliveryClick(view: View) {
            /*if (!customLoader!!.isShowing) {
                customLoader!!.show()
            }*/
            custom_attribute = JSONObject()
            binding!!.deliveryDateTxt.text =
                resources.getString(R.string.click_here_to_select_delivery_date)
            binding!!.orderNoteEdt.hint = resources.getString(R.string.order_note_hint)
            binding!!.deliveryTimeSpn.visibility = View.GONE
            binding!!.deliveryDateTxt.visibility = View.GONE
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
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                view.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        this@SubscribeCartList,
                        R.drawable.grey_border
                    )
                )
                binding!!.storeContainer.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        this@SubscribeCartList,
                        R.drawable.black_border
                    )
                )
                binding!!.shippingContainer.background = ContextCompat.getDrawable(
                    this@SubscribeCartList,
                    R.drawable.black_border
                )

                shipping = false
            } else {
                view.background = ContextCompat.getDrawable(
                    this@SubscribeCartList,
                    R.drawable.grey_border
                )
                binding!!.storeContainer.background = ContextCompat.getDrawable(
                    this@SubscribeCartList,
                    R.drawable.black_border
                )
                binding!!.shippingContainer.background = ContextCompat.getDrawable(
                    this@SubscribeCartList,
                    R.drawable.black_border
                )
            }
            shipping = false
            custom_attribute.put("Checkout-Method", selected_delivery)
        }

        fun deliveryDatePicker() {
            dpd.show(supportFragmentManager, "Datepickerdialog")
        }
    }


    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
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
                if (slots[i].asJsonObject.get("day_of_week").asString.equals(dayOfTheWeek, true)) {
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
            wishitem.isVisible = SplashViewModel.featuresModel.in_app_wishlist
            wishitem.actionView?.setOnClickListener {
                onOptionsItemSelected(wishitem)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }
}
