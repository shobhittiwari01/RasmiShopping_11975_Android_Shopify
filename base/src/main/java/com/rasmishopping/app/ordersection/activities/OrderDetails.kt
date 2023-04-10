package com.rasmishopping.app.ordersection.activities
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.shopify.buy3.GraphCallResult
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.Error
import com.rasmishopping.app.FlitsDashboard.WishlistSection.FlitsWishlistViewModel
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.databinding.ActivityOrderviewBinding
import com.rasmishopping.app.ordersection.adapters.OrderDetailsListAdapter
import com.rasmishopping.app.ordersection.viewmodels.OrderDetailsViewModel
import com.rasmishopping.app.personalised.adapters.PersonalisedAdapter
import com.rasmishopping.app.productsection.viewmodels.ProductViewModel
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.CurrencyFormatter
import com.rasmishopping.app.utils.GraphQLResponse
import com.rasmishopping.app.utils.Status
import com.rasmishopping.app.utils.ViewModelFactory
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class OrderDetails : NewBaseActivity() {
    private var binding: ActivityOrderviewBinding? = null
    private var orderEdge: Storefront.Order? = null
    private val TAG = "OrderDetails"
    private var productmodel: ProductViewModel? = null
    var flistwishmodel: FlitsWishlistViewModel? = null
    @Inject
    lateinit var factory: ViewModelFactory
    private var model: OrderDetailsViewModel? = null

    @Inject
    lateinit var personalisedadapter: PersonalisedAdapter

    @Inject
    lateinit var orderDetailsListAdapter: OrderDetailsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try{
            val group = findViewById<ViewGroup>(R.id.container)
            binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_orderview, group, true)
            (application as MyApplication).mageNativeAppComponent!!.doOrderDetailsInjection(this)
            model = ViewModelProvider(this, factory).get(OrderDetailsViewModel::class.java)
            productmodel = ViewModelProvider(this, factory).get(ProductViewModel::class.java)
            model?.context = this
            productmodel?.context = this
            showBackButton()
            showTittle(resources.getString(R.string.OrderDetails))
            model!!.recommendedLiveData.observe(this, androidx.lifecycle.Observer { this.consumeRecommended(it) })
            binding?.orderedItems?.adapter = orderDetailsListAdapter
            if (intent.hasExtra("orderData")) {
                orderEdge = intent.getSerializableExtra("orderData") as Storefront.Order
                Log.d(TAG, "onCreate: " + orderEdge?.lineItems?.edges?.get(0)?.node?.variant?.product?.id.toString())
                flistwishmodel = ViewModelProvider(this, factory).get(FlitsWishlistViewModel::class.java)
                flistwishmodel!!.context = this
                model?.shopifyRecommended(orderEdge?.lineItems?.edges?.get(0)?.node?.variant?.product?.id.toString())
                bindData(orderEdge)
                if(MagePrefs.getLanguage()=="AR") {
                    binding?.customerName?.gravity = Gravity.RIGHT
                    binding?.customerName?.gravity = Gravity.RIGHT
                    binding?.shippingAddress?.gravity = Gravity.RIGHT
                    binding?.shippingAddress?.gravity = Gravity.RIGHT
                } else {
                    binding?.customerName?.gravity  = Gravity.LEFT
                    binding?.customerName?.gravity = Gravity.LEFT
                    binding?.shippingAddress?.gravity = Gravity.LEFT
                    binding?.shippingAddress?.gravity = Gravity.LEFT
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    private fun consumeRecommended(response: GraphQLResponse?) {
        when (response?.status) {
            Status.SUCCESS -> {
                val result =
                    (response.data as GraphCallResult.Success<Storefront.QueryRoot>).response
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
                    var recommendedList = result.data!!.productRecommendations as ArrayList<Storefront.Product>?
                    if (recommendedList?.size!! > 0) {
                        Log.d(TAG, "consumeRecommended: " + recommendedList.size)
                        setLayout(binding!!.shopifyrecommendedList, "horizontal")
                        personalisedadapter = PersonalisedAdapter()
                        if (!personalisedadapter.hasObservers()) {
                            personalisedadapter.setHasStableIds(true)
                        }
                        var jsonobject: JSONObject = JSONObject();
                        jsonobject.put("item_shape","rounded")
                        jsonobject.put("item_text_alignment","center")
                        jsonobject.put("item_border","0")
                        jsonobject.put("item_title","1")
                        jsonobject.put("item_price","1")
                        jsonobject.put("item_compare_at_price","1")
                        personalisedadapter.setData(  recommendedList, this,jsonobject,  model?.repository!!)
                        binding!!.shopifyrecommendedList.adapter = personalisedadapter
                    }
                }
            }
            Status.ERROR -> Toast.makeText(this, response.error!!.error.message, Toast.LENGTH_SHORT)
                .show()
            else -> {
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindData(orderEdge: Storefront.Order?) {
        binding?.orderId?.text = getString(R.string.order_id) + orderEdge?.orderNumber.toString()
        val sdf2 = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        if (orderEdge?.processedAt?.toLocalDateTime() != null) {

            val expiretime = sdf.parse(orderEdge.processedAt?.toLocalDateTime().toString())
            val time = sdf2.format(expiretime!!)
            binding?.orderDate?.text = getString(R.string.placedon) + " " + time
        }
        orderDetailsListAdapter.setData(orderEdge?.lineItems?.edges)
        binding?.orderedItemsIndicator?.setViewPager(binding?.orderedItems)
        binding?.orderedItemsIndicator?.tintIndicator(Color.parseColor(themeColor))
        binding?.customerName?.text = orderEdge?.shippingAddress?.firstName + " " + orderEdge?.shippingAddress?.lastName
        val shippingAddress = StringBuffer()
        shippingAddress.append(orderEdge?.shippingAddress?.address1)
        shippingAddress.append("\n")
        shippingAddress.append(orderEdge?.shippingAddress?.city)
        shippingAddress.append("\n")
        shippingAddress.append(orderEdge?.shippingAddress?.country)
        shippingAddress.append("\n")
        shippingAddress.append(orderEdge?.shippingAddress?.zip)
        binding?.shippingAddress?.text = shippingAddress

        binding?.subtotalPrice?.text =
            getString(R.string.subtotal_amt) + " " + CurrencyFormatter.setsymbol(
                orderEdge?.subtotalPrice?.amount
                    ?: "", orderEdge?.subtotalPrice?.currencyCode.toString()
            )
        binding?.shippingPrice?.text =
            getString(R.string.shipping_amt) + " " + CurrencyFormatter.setsymbol(
                orderEdge?.totalShippingPrice?.amount
                    ?: "", orderEdge?.totalShippingPrice?.currencyCode.toString()
            )
        binding?.taxPrice?.text = getString(R.string.tax_amt) + " " + CurrencyFormatter.setsymbol(
            orderEdge?.totalTax?.amount
                ?: "", orderEdge?.totalTax?.currencyCode.toString()
        )
        binding?.orderPrice?.text = CurrencyFormatter.setsymbol(
            orderEdge?.totalPrice?.amount
                ?: "", orderEdge?.totalPrice?.currencyCode.toString()
        )

        binding?.customerEmail?.text = orderEdge?.email ?: " N/A"
        binding?.customerMobile?.text = orderEdge?.phone ?: " N/A"
        binding?.paymentStatus?.text =
            getString(R.string.payment_status) + " " + orderEdge?.financialStatus.toString()
        if (orderEdge?.financialStatus.toString().equals("REFUNDED")) {

            if(orderEdge?.canceledAt!=null){

                val cancelled_at = sdf.parse(orderEdge?.canceledAt?.toLocalDateTime().toString())
                val cancelDate = sdf2.format(cancelled_at)
                binding?.cancelledAt?.text = getString(R.string.cancelled_at) + " " + cancelDate
                binding?.cancelledReason?.text =
                    getString(R.string.cancelled_reason) + " " + orderEdge?.cancelReason.toString()
                binding?.cancelledAt?.visibility = View.VISIBLE
                binding?.cancelledReason?.visibility = View.VISIBLE
            }

        }
        binding?.orderStatus?.text = orderEdge?.fulfillmentStatus.toString()
        if (orderEdge?.fulfillmentStatus.toString().equals("UNFULFILLED")) {
            binding?.orderStatusContainer?.setBackgroundColor(resources.getColor(R.color.red))
            binding?.orderStatusIcon?.setImageDrawable(resources.getDrawable(R.drawable.cross_icon))
        } else if (orderEdge?.fulfillmentStatus.toString().equals("FULFILLED")) {
            binding?.orderStatusContainer?.setBackgroundColor(resources.getColor(R.color.green))
            binding?.orderStatusIcon?.setImageDrawable(resources.getDrawable(R.drawable.tick))
        } else {
            binding?.orderStatusContainer?.setBackgroundColor(resources.getColor(R.color.orange))
            binding?.orderStatusIcon?.setImageDrawable(resources.getDrawable(R.drawable.order_history))
        }
    }
}