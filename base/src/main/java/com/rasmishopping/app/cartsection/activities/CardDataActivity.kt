package com.rasmishopping.app.cartsection.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.shopify.buy3.CardClient
import com.shopify.buy3.CardVaultResult
import com.shopify.buy3.CreditCard
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.fragments.LeftMenu
import com.rasmishopping.app.basesection.viewmodels.LeftMenuViewModel
import com.rasmishopping.app.cartsection.viewmodels.CartListViewModel
import com.rasmishopping.app.checkoutsection.activities.OrderSuccessActivity
import com.rasmishopping.app.checkoutsection.viewmodels.CheckoutWebLinkViewModel
import com.rasmishopping.app.databinding.FragmentCartDataBinding
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.Urls
import com.rasmishopping.app.utils.ViewModelFactory
import java.util.*
import javax.inject.Inject


class CardDataActivity : NewBaseActivity() {
    @Inject
    lateinit var factory: ViewModelFactory
    var binding: FragmentCartDataBinding? = null
    var checkoutid: ID? = null
    var grandtotal: String? = null
    var shippinddata: Storefront.Checkout? = null
    var price: String? = null
    var totalAmount: String? = null

    var handle: String? = null
    private var model: CheckoutWebLinkViewModel? = null
    private var cartmodel: CartListViewModel? = null
    private var leftmenu: LeftMenuViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_cart_data, group, true)


        (application as MyApplication).mageNativeAppComponent!!.doCardPageInjection(this)
        showBackButton()
        showTittle(resources.getString(R.string.carddetails))

        if (intent.hasExtra("checkoutID")) {
            checkoutid = ID(intent.getStringExtra("checkoutID"))
        }
        if (intent.hasExtra("grandtotal")) {
            grandtotal = intent.getStringExtra("grandtotal")
        }
        if (intent.hasExtra("data")) {
            shippinddata = intent.getSerializableExtra("data") as Storefront.Checkout
        }
        if (intent.hasExtra("price")) {
            price = intent.getStringExtra("price")
        }
        binding?.login?.setOnClickListener {
            setCard()
        }
        cartmodel = ViewModelProvider(this, factory).get(CartListViewModel::class.java)
        cartmodel!!.context = this
        model = ViewModelProviders.of(this, factory).get(CheckoutWebLinkViewModel::class.java)
        model!!.context = this
        leftmenu = ViewModelProvider(this, viewModelFactory).get(LeftMenuViewModel::class.java)
       leftmenu!!.context = this
        cartmodel!!.checkoutResponse().observe(
            this,
            { this.completeCheckout(it) })
    }

    fun shippingLineupdate(
        reponse: Storefront.CheckoutShippingLineUpdatePayload
    ) {
        Log.d("showmsg", "id: " + reponse.responseData)

//         setCard()
    }

    fun completeCheckout(reponse: Storefront.CheckoutCompleteWithCreditCardV2Payload) {
        Log.d("showmsg", "id: " + reponse.responseData)
        model!!.setOrder(Urls((application as MyApplication)).mid,  (reponse.responseData.get("payment") as Storefront.Payment).id.toString())

        startActivity(
            Intent(
                this@CardDataActivity,
                OrderSuccessActivity::class.java
            )
        )
        finishAffinity()
        Constant.activityTransition(this@CardDataActivity)
    }

    fun setCard() {
        leftmenu?.currencyResponse()
        val cardVaultUrl = "https://deposit.us.shopifycs.com/sessions"
        val cardClient: CardClient = CardClient()
        val creditCard: CreditCard = CreditCard(
            binding?.cardNumber?.text.toString(),
            binding?.firstname?.text.toString(),
            binding?.lastname?.text.toString(),
            binding?.month?.text.toString(),
            binding?.yy?.text.toString(),
            binding?.cvv?.text.toString()
        )
                   
        cardClient.vault(creditCard, cardVaultUrl).enqueue { result: CardVaultResult ->

            if (result is CardVaultResult.Failure) {
                Log.d("showmsg", "card: " + result.exception)
                Log.d("showmsg", "card: " + result.exception.message)
                Log.d(
                    "showmsg",
                    "card: " + result.exception.localizedMessage
                )
                Log.d(
                    "showmsg",
                    "card: " + result.exception.stackTrace
                )
                Log.d("showmsg", "card: " + result.exception.cause)
            } else if (result is CardVaultResult.Success) {
                cartmodel!!.updateEmail(checkoutid, MagePrefs.getCustomerEmail()!!)
                val vaultid = result.token

                val input = Storefront.MailingAddressInput()
                input.zip = shippinddata?.shippingAddress?.zip
                input.phone = shippinddata?.shippingAddress?.phone
                input.country = shippinddata?.shippingAddress?.country
                input.firstName = shippinddata?.shippingAddress?.firstName
                input.address1 = shippinddata?.shippingAddress?.address1
                input.city = shippinddata?.shippingAddress?.city
                input.address2 = shippinddata?.shippingAddress?.address2
                input.province = shippinddata?.shippingAddress?.province
                input.lastName = shippinddata?.shippingAddress?.lastName
                input.company = shippinddata?.shippingAddress?.company
                val uniqueId = UUID.randomUUID().toString()
//                totalAmount = (grandtotal?.toDouble()!! + price!!.toDouble()).toString()
                totalAmount = (grandtotal?.toDouble()!! + price!!.toDouble()).toString()
                val moneyInput = Storefront.MoneyInput(
                    totalAmount,
                    Storefront.CurrencyCode.valueOf(Constant.getCurrency(MagePrefs.getCountryCode()!!))
                )
                val checkoutitems =
                    Storefront.CreditCardPaymentInputV2(moneyInput, uniqueId, input, vaultid)
                cartmodel!!.completeCheckout(checkoutid, checkoutitems)
            } else {
                Log.d("showmsg", "else: ")
            }
        }
    }
}