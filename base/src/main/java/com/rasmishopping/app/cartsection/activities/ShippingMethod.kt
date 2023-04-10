package com.rasmishopping.app.cartsection.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.cartsection.viewmodels.CartListViewModel
import com.rasmishopping.app.databinding.FragmentShippingMethodBinding
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.ViewModelFactory
import javax.inject.Inject


class ShippingMethod : NewBaseActivity() {
    @Inject
    lateinit var factory: ViewModelFactory
    var binding: FragmentShippingMethodBinding? = null
    var Shippingdata: Storefront.Checkout? = null
    var checkoutID: ID? = null
    var grandtotal: String? = null
    var Arraylist: ArrayList<Storefront.Checkout> = ArrayList()
    var List: ArrayList<String>? = null
    var handle: String? = null
    var price: String? = null

    var HashMap: HashMap<String, ArrayList<String>> = HashMap()
    private var cartmodel: CartListViewModel? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.fragment_shipping_method, group, true)


        (application as MyApplication).mageNativeAppComponent!!.doShippingPageInjection(this)
        showBackButton()
        showTittle(resources.getString(R.string.shippingmethod))
        if (intent.hasExtra("data")) {
            Shippingdata = intent.getSerializableExtra("data") as Storefront.Checkout

        }
        if (intent.hasExtra("checkoutID")) {
            checkoutID = ID(intent.getStringExtra("checkoutID"))

        }
        if (intent.hasExtra("grandtotal")) {
            grandtotal = intent.getStringExtra("grandtotal")

        }
//        if (intent.hasExtra("grandtotal")) {
//            grandtotal = intent.getStringExtra("grandtotal")
//
//        }
        cartmodel = ViewModelProvider(this, factory).get(CartListViewModel::class.java)
        cartmodel!!.context = this
        setShippingMethods(Shippingdata!!)
        cartmodel!!.ShippingLineResponse().observe(
            this@ShippingMethod,
            androidx.lifecycle.Observer {
                shippingLineupdate(
                    it
                )
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setShippingMethods(ShippingData: Storefront.Checkout) {
        val colorStateList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)), intArrayOf(
                resources.getColor(R.color.black)
            )
        )

        var radionbutton: RadioButton? = null

        for (i in 0 until ShippingData.availableShippingRates.shippingRates.size) {
            radionbutton = RadioButton(this)
            List = ArrayList()
            radionbutton.buttonTintList = colorStateList
            radionbutton.text = ShippingData.availableShippingRates.shippingRates.get(i).title
            List?.add(ShippingData.availableShippingRates.shippingRates.get(i).handle)
            List?.add(ShippingData.availableShippingRates.shippingRates.get(i).price.amount)
            HashMap.put(ShippingData.availableShippingRates.shippingRates.get(i).title, List!!)
            Log.d(
                "shippingcharge",
                "" + ShippingData.availableShippingRates.shippingRates.get(i).price.amount
            )
            radionbutton.typeface = resources.getFont(R.font.sarabold)
            binding?.radiogroup?.addView(radionbutton)
        }

        binding?.radiogroup!!.setOnCheckedChangeListener { radioGroup, i ->
            var radio_btnID: Int = binding?.radiogroup!!.checkedRadioButtonId
            radionbutton = findViewById<View>(radio_btnID) as RadioButton
            radionbutton?.text
            Log.d("newtext", "" + radionbutton?.text)
            Log.d("Map", "" + HashMap)
            HashMap.forEach({
                handle = HashMap.get(radionbutton?.text)!![0]
                price = HashMap.get(radionbutton?.text)!![1]

                Log.d("value", "" + HashMap.get(radionbutton?.text)!![0])
                Log.d("value", "" + HashMap.get(radionbutton?.text)!![1])
            })

//            cartmodel!!.shippingLineUpdate(
//                checkoutID,model.handle!!
//
//            )

            Log.d("delivery", "" + handle)
            Log.d("delivery", "" + price)
            Log.d("delivery", "" + checkoutID)
            cartmodel!!.shippingLineUpdate(
                checkoutID,
                handle!!
            )
        }

        binding?.login?.setOnClickListener {

            var intent = Intent(this@ShippingMethod, CardDataActivity::class.java)
            intent.putExtra("checkoutID", checkoutID.toString())
            intent.putExtra("grandtotal", grandtotal)
            intent.putExtra("price", price)
            intent.putExtra("handle", handle)
            intent.putExtra("data", Shippingdata)
            startActivity(intent)
        }


    }

    fun shippingLineupdate(
        reponse: Storefront.CheckoutShippingLineUpdatePayload
    ) {
        Log.d("showmsg", "id: " + reponse.responseData)

//         setCard()
    }

}