package com.rasmishopping.app.cartsection.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.shopify.buy3.CardClient
import com.shopify.buy3.CardVaultResult
import com.shopify.buy3.CreditCard
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.addresssection.models.Address
import com.rasmishopping.app.addresssection.models.CountryListResponse
import com.rasmishopping.app.addresssection.viewmodels.AddressModel
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.cartsection.adapters.NewAddressListAdapter
import com.rasmishopping.app.cartsection.fragment.ShippingMethod
import com.rasmishopping.app.cartsection.viewmodels.CartListViewModel
import com.rasmishopping.app.databinding.NewAddressPageBinding
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_shipping_method.view.*
import kotlinx.android.synthetic.main.m_addressbottomsheet.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.*
import javax.inject.Inject

class NativeCheckoutAddressPage : NewBaseActivity() {
    internal var binding: NewAddressPageBinding? = null
    private var addresslist: RecyclerView? = null
    private var model: AddressModel? = null
    private var cursor: String? = null
    var tag: String? = null
//    private lateinit var navController: NavController
    var ship_amount:String?=null
    private var grandTotal: String? = null
    var totalAmount: String? = null
    private var cartmodel: CartListViewModel? = null
    @Inject
    lateinit var factory: ViewModelFactory
    var checkoutID: ID? = null


    @Inject
    lateinit var adapter: NewAddressListAdapter

    private var address: Address? = null
    var ResponseObject:JSONObject= JSONObject()
    private var mailingAddressEdges: MutableList<Storefront.MailingAddressEdge>? = null
    private val recyclerViewOnScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val visibleItemCount = recyclerView.layoutManager!!.childCount
            val totalItemCount = recyclerView.layoutManager!!.itemCount
            val firstVisibleItemPosition =
                (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (!recyclerView.canScrollVertically(1)) {
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition > 0
                    && totalItemCount >= mailingAddressEdges!!.size
                ) {
                    Log.i("Magenative", "NEwAddress")
                    model!!.addresscursor = cursor.toString()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.new_address_page, group, true)
//        binding!!.handler = ClickHandler()


        val gson = Gson()
        val inputStream: String =
            assets.open("country_list.json").bufferedReader().use { it.readText() }
        val countryList = gson.fromJson("$inputStream", CountryListResponse::class.java)

        var countryName = mutableListOf<String>()
        var stateName = mutableListOf<String>()
        if (intent.hasExtra("checkout_id")) {
            checkoutID = ID(intent.getStringExtra("checkout_id"))
        }
        if (intent.hasExtra("grand_total")) {
            grandTotal = intent.getStringExtra("grand_total")
        }

//        country.setOnFocusChangeListener { v, hasFocus ->
//            if (hasFocus) {
//                country.showDropDown()
//            }
//        }
//        state.setOnFocusChangeListener { v, hasFocus ->
//            if (hasFocus) {
//                state.showDropDown()
//            }
//        }
//
//        val stateAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, stateName)
//        state.setAdapter(stateAdapter)
//        state.setOnItemClickListener { parent, view, position, id ->
//
//            val countryName = country.text.toString()
//
//            if (countryName == countryList.countries[position].country) {
//                for (i in countryList.countries[position].states) {
//                    stateName.add(i)
//                }
//            }
//        }
//
//
//        for (i in countryList.countries) {
//            countryName.add(i.country)
//        }
//
//        val countryAdapter =
//            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countryName)
//        country.apply {
//            setAdapter(countryAdapter)
//            setOnItemClickListener { parent, view, position, id ->
//
//                val countryName = country.text.toString()
//
//                if (countryName == countryList.countries[position].country) {
//                    for (i in countryList.countries[position].states) {
//                        stateName.add(i)
//                    }
//                }
//
//            }
//        }

        addresslist = setLayout(binding!!.addresslist, "horizontal")
        addresslist!!.addOnScrollListener(recyclerViewOnScrollListener)
        showTittle(resources.getString(R.string.myaddress))
        showBackButton()
        (application as MyApplication).mageNativeAppComponent!!.doNewAddressListInjection(this)
        model = ViewModelProviders.of(this, factory).get(AddressModel::class.java)
        model!!.context = this
        model!!.message.observe(this, Observer<String> { this.showToast(it) })
        model!!.addresses.observe(
            this,
            Observer<MutableList<Storefront.MailingAddressEdge>> { this.listAddress(it) })

        binding?.submit?.setOnClickListener {
            var alertDialog =
                SweetAlertDialog(this@NativeCheckoutAddressPage, SweetAlertDialog.NORMAL_TYPE)
            alertDialog.titleText = this.getString(R.string.confirmation)
            alertDialog.contentText = this.getString(R.string.update_msg)
            alertDialog.confirmText = this.getString(R.string.dialog_ok)
            alertDialog.cancelText = this.getString(R.string.no)
            alertDialog.setConfirmClickListener { sweetAlertDialog ->
                ClickHandler().SubmitAddress()
                sweetAlertDialog.dismissWithAnimation()
            }
            alertDialog.show()
        }
        cartmodel = ViewModelProvider(this, factory).get(CartListViewModel::class.java)
        cartmodel!!.context = this
        cartmodel!!.AddressResponse()
            .observe(this@NativeCheckoutAddressPage, Observer<Storefront.Checkout> { ClickHandler().setMailAddress(it) })

    }


    private fun listAddress(mailingAddressEdges: MutableList<Storefront.MailingAddressEdge>) {
        try {
            if (mailingAddressEdges.size > 0) {
                if (this.mailingAddressEdges == null) {
                    this.mailingAddressEdges = mailingAddressEdges
                    binding?.orText?.visibility=View.VISIBLE
                    adapter.setData(mailingAddressEdges, model,
                        object : NewAddressListAdapter.VariantCallback
                        {
                            override fun clickVariant(address: Address)
                            {
                                val input = Storefront.MailingAddressInput()
                                input.firstName = address.firstName
                                input.lastName = address.lastName
                                input.company = " "
                                input.address1 = address.address1
                                input.address2 = address.address2
                                input.city = address.city
                                input.country = address.country
                                input.province =address.province
                                input.zip = address.zip
                                input.phone = address.phone
                                cartmodel?.populateShipping(input,checkoutID)
                            }})
                    addresslist!!.adapter = adapter
                } else {
                    this.mailingAddressEdges!!.addAll(mailingAddressEdges)
                    adapter.notifyDataSetChanged()
                }
                cursor = this.mailingAddressEdges!![this.mailingAddressEdges!!.size - 1].cursor
                Log.i("MageNative", "Cursor : " + cursor!!)

            } else {
                showToast(resources.getString(R.string.noaddressfound))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun showToast(toast: String) {
        Toast.makeText(this@NativeCheckoutAddressPage, toast, Toast.LENGTH_LONG).show()
    }

    inner class ClickHandler {


        fun SubmitAddress() {
            if (binding!!.firstname.text!!.toString().isEmpty()) {
                binding!!.firstname.error = resources.getString(R.string.empty)
                binding!!.firstname.requestFocus()
            } else {
                if (binding!!.lastname.text!!.toString().isEmpty()) {
                    binding!!.lastname.error = resources.getString(R.string.empty)
                    binding!!.lastname.requestFocus()
                } else {
                    if (binding!!.address1.text!!.toString().isEmpty()) {
                        binding!!.address1.error =
                            resources.getString(R.string.empty)
                        binding!!.address1.requestFocus()
                    } else {
                        if (binding!!.address2.text!!.toString().isEmpty()) {
                            binding!!.address2.error =
                                resources.getString(R.string.empty)
                            binding!!.address2.requestFocus()
                        } else {
                            if (binding!!.city.text!!.toString().isEmpty()) {
                                binding!!.city.error =
                                    resources.getString(R.string.empty)
                                binding!!.city.requestFocus()
                            } else {
                                if (binding!!.state.text!!.toString().isEmpty()) {
                                    binding!!.state.error =
                                        resources.getString(R.string.empty)
                                    binding!!.state.requestFocus()
                                } else {
                                    if (binding!!.country.text!!.toString()
                                            .isEmpty()
                                    ) {
                                        binding!!.country.error =
                                            resources.getString(R.string.empty)
                                        binding!!.country.requestFocus()
                                    } else {
                                        if (binding!!.pincode.text!!.toString()
                                                .isEmpty()
                                        ) {
                                            binding!!.pincode.error =
                                                resources.getString(R.string.empty)
                                            binding!!.pincode.requestFocus()
                                        } else {
                                            if (binding!!.phone.text!!.toString()
                                                    .isEmpty()
                                            ) {
                                                binding!!.phone.error =
                                                    resources.getString(R.string.empty)
                                                binding!!.phone.requestFocus()
                                            } else {
                                                Proceed()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        fun setMailAddress(reponse: Storefront.Checkout) {
            var intent =Intent(this@NativeCheckoutAddressPage,ShippingMethod::class.java)
            intent.putExtra("data",  reponse)
            intent.putExtra("checkoutID",  checkoutID.toString())
            intent.putExtra("grandtotal",  grandTotal)
            startActivity(intent)
            finish()
        }
        private fun Proceed() {
            val input = Storefront.MailingAddressInput()
            input.firstName = binding!!.firstname.text!!.toString()
            input.lastName = binding!!.lastname.text!!.toString()
            input.company = " "
            input.address1 = binding!!.address1.text!!.toString()
            input.address2 = binding!!.address2.text!!.toString()
            input.city = binding!!.city.text!!.toString()
            input.country = binding!!.country.text!!.toString()
            input.province = binding!!.state.text!!.toString()
            input.zip = binding!!.pincode.text!!.toString()
            input.phone = binding!!.phone.text!!.toString()
            CoroutineScope(Dispatchers.IO).launch {
                  delay(1000)
                mailingAddressEdges = null
                model!!.addAddress(input)
                cartmodel!!.populateShipping(input,checkoutID)
            }
            binding!!.firstname.setText("")
            binding!!.lastname.setText("")
            binding!!.address1.setText("")
            binding!!.address2.setText("")
            binding!!.city.setText("")
            binding!!.country.setText("")
            binding!!.state.setText("")
            binding!!.pincode.setText("")
            binding!!.phone.setText("")
        }
    }

}