package com.rasmishopping.app.addresssection.activities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.shopify.buy3.Storefront
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.addresssection.adapters.AddressListAdapter
import com.rasmishopping.app.addresssection.models.Address
import com.rasmishopping.app.addresssection.viewmodels.AddressModel
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.databinding.MAddresslistBinding
import com.rasmishopping.app.databinding.PopConfirmationBinding
import com.rasmishopping.app.utils.ViewModelFactory
import org.json.JSONObject
import javax.inject.Inject
open class AddressList : NewBaseActivity() {
    internal var binding: MAddresslistBinding? = null
    private var model: AddressModel? = null
    @Inject
    lateinit var factory: ViewModelFactory
    private var addresslist: RecyclerView? = null
    private val TAG = "AddressList"
    @Inject
    lateinit var adapter: AddressListAdapter
    private var cursor: String? = null
    private var mailingAddressEdges: MutableList<Storefront.MailingAddressEdge>? = null
    private var sheet: BottomSheetBehavior<*>? = null
    var tag: String? = null
    var state: String? = null
    var country: String? = null
    private var address: Address? = null
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
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_addresslist, group, true)
        binding!!.handler = ClickHandler()
        addresslist = setLayout(binding!!.mainlist.addresslist, "vertical")
        addresslist!!.addOnScrollListener(recyclerViewOnScrollListener)
        showTittle(resources.getString(R.string.myaddress))
        showBackButton()
        (application as MyApplication).mageNativeAppComponent!!.doAddressListInjection(this)
        sheet = BottomSheetBehavior.from(binding!!.mainbottomsheet.bottomSheet)
        sheet!!.state = BottomSheetBehavior.STATE_COLLAPSED
        model = ViewModelProvider(this, factory).get(AddressModel::class.java)
        model!!.context = this
        model!!.message.observe(this, Observer<String> { this.showToast(it) })
        model!!.flag.observe(this, Observer<Boolean> {
            if(it){
                if (binding!!.mainlist.addresslist.childCount==0){
                   binding!!.noaddres.nocartsection.visibility=View.VISIBLE
                   binding!!.mainlist.addresslist.visibility=View.GONE
                }
            }
        })
        model!!.addresses.observe(this, Observer<MutableList<Storefront.MailingAddressEdge>> { this.listAddress(it) })
        model!!.editaddress.observe(this, Observer<Address> { this.editAddress(it) })
        sheet!!.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    sheet!!.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        binding!!.noaddres.continueShopping.setOnClickListener{
            Log.i("ContinueClicked","True")
            finish()
        }
        binding?.mainbottomsheet!!.submit.setOnClickListener {
            Log.d("javed", "onCreate: " + tag)
            when(tag){
                "add"->{
                    ClickHandler().SubmitAddress()
                }
                "edit"->{
                    val alertDialog = SweetAlertDialog(this@AddressList, SweetAlertDialog.WARNING_TYPE)
                    var customeview = PopConfirmationBinding.inflate(LayoutInflater.from(this@AddressList))
                    customeview.textView.text=getString(R.string.confirmation)
                    customeview.textView2.text=getString(R.string.areyousure_update_address)
                    alertDialog.hideConfirmButton()
                    customeview.okDialog.setOnClickListener{
                        customeview.okDialog.isClickable=false
                        customeview.textView.text=getString(R.string.done)
                        customeview.textView2.text=getString(R.string.update_msg)
                        alertDialog.showCancelButton(false)
                        alertDialog.setConfirmClickListener(null)
                        alertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                        ClickHandler().SubmitAddress()
                        alertDialog.cancel()
                    }
                    customeview.noDialog.setOnClickListener{
                        customeview.noDialog.isClickable=false
                        alertDialog.cancel()
                    }
                    alertDialog.setCustomView(customeview.root)
                    alertDialog.show()
                }
            }
        }
    }
    fun SelectCountryAndState() {
        var countryName = mutableListOf<String>()
        when(tag){
            "edit"->{
                if (this.address!=null){
                    if(this.address!!.country!=null){
                        countryName.add(this.address!!.country.toString())
                    }else{
                        countryName.add(resources.getString(R.string.pleasechoosecountry))
                    }
                    if(this.address!!.province!=null){
                        state=this.address!!.province
                    }else{
                        state=resources.getString(R.string.state)
                    }
                }
            }
            "add"->{
                countryName.add(resources.getString(R.string.pleasechoosecountry))
            }
        }
        val inputStream: String = assets.open("country_list.json").bufferedReader().use { it.readText() }
        val country_json = JSONObject(inputStream)
        val countryData = country_json.names()
        for (i in 0 until countryData.length()) {
            countryName.add(countryData.getString(i))
        }
        binding?.mainbottomsheet?.country?.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countryName)
        binding!!.mainbottomsheet.country.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    Log.d(TAG, "onItemSelected: " + parent?.selectedItem.toString())
                    var selected_country = parent?.selectedItem.toString()
                    if (selected_country.equals(resources.getString(R.string.pleasechoosecountry))){
                        var stateName = mutableListOf<String>()
                        stateName.add(resources.getString(R.string.state))
                        binding?.mainbottomsheet?.state?.adapter = ArrayAdapter(this@AddressList, android.R.layout.simple_spinner_dropdown_item, stateName)
                    } else {
                        if(country_json.has(selected_country.trim())){
                            var stateName = mutableListOf<String>()
                            val json_arr = country_json.getJSONArray(selected_country.trim())
                            Log.d("javed", "json_arr: " + json_arr)
                            stateName.add(state!!)
                            for (i in 0 until json_arr.length()) {
                                stateName.add(json_arr.getString(i))
                            }
                            binding?.mainbottomsheet?.state?.adapter = ArrayAdapter(this@AddressList, android.R.layout.simple_spinner_dropdown_item, stateName)
                        }else{
                            binding?.mainbottomsheet!!.country.visibility=View.GONE
                            binding?.mainbottomsheet!!.countryTextSection.visibility=View.VISIBLE
                            binding?.mainbottomsheet!!.stateTextSection.visibility=View.VISIBLE
                            binding?.mainbottomsheet!!.countrytext.setText(selected_country)
                            binding?.mainbottomsheet!!.statetext.setText(state)
                            binding?.mainbottomsheet!!.state.visibility=View.GONE
                        }
                    }
                }
            }

        try {
            val popup = Spinner::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true

            // Get private mPopup member variable and try cast to ListPopupWindow
            val popupWindow = popup[binding?.mainbottomsheet?.country] as ListPopupWindow

            // Set popupWindow height to 500px
            popupWindow.height = 500
        } catch (e: NoClassDefFoundError) {
            // silently fail...
        } catch (e: ClassCastException) {
        } catch (e: NoSuchFieldException) {
        } catch (e: IllegalAccessException) {
        }
    }
    private fun editAddress(address: Address) {
        tag = "edit"
        this.address = address
        binding!!.mainbottomsheet.firstname.setText(address.firstName)
        binding!!.mainbottomsheet.lastname.setText(address.lastName)
        binding!!.mainbottomsheet.address1.setText(address.address1)
        binding!!.mainbottomsheet.address2.setText(address.address2)
        binding!!.mainbottomsheet.city.setText(address.city)
        binding!!.mainbottomsheet.pincode.setText(address.zip)
        binding!!.mainbottomsheet.phone.setText(address.phone)
        SelectCountryAndState()
        ClickHandler().openSheet()
    }
    private fun listAddress(mailingAddressEdges: MutableList<Storefront.MailingAddressEdge>) {
        try {
            if (mailingAddressEdges.size > 0) {
                if (this.mailingAddressEdges == null) {
                    binding!!.noaddres.nocartsection.visibility=View.GONE
                    binding!!.mainlist.addresslist.visibility=View.VISIBLE
                    this.mailingAddressEdges = mailingAddressEdges
                    adapter.setData(mailingAddressEdges, model)
                    addresslist!!.adapter = adapter
                } else {
                    this.mailingAddressEdges!!.addAll(mailingAddressEdges)
                    adapter.notifyDataSetChanged()
                }
                cursor = this.mailingAddressEdges!![this.mailingAddressEdges!!.size - 1].cursor
                Log.i("MageNative", "Cursor : " + cursor!!)

            } else {
                if (this.mailingAddressEdges == null) {
                   binding!!.noaddres.nocartsection.visibility=View.VISIBLE
                    binding!!.mainlist.addresslist.visibility=View.GONE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun showToast(toast: String) {
        Toast.makeText(this@AddressList, toast, Toast.LENGTH_LONG).show()
    }

    inner class ClickHandler {
        fun continueShopping(view: View){
           finish()
        }
        fun addAddress(view: View) {
            tag = "add"
            binding!!.mainbottomsheet.firstname.text!!.clear()
            binding!!.mainbottomsheet.lastname.text!!.clear()
            binding!!.mainbottomsheet.address1.text!!.clear()
            binding!!.mainbottomsheet.address2.text!!.clear()
            binding!!.mainbottomsheet.city.text!!.clear()
            state=resources.getString(R.string.state)
            binding!!.mainbottomsheet.pincode.text!!.clear()
            binding!!.mainbottomsheet.phone.text!!.clear()
            binding?.mainbottomsheet!!.country.visibility=View.VISIBLE
            binding?.mainbottomsheet!!.state.visibility=View.VISIBLE
            binding?.mainbottomsheet!!.countryTextSection.visibility=View.GONE
            binding?.mainbottomsheet!!.stateTextSection.visibility=View.GONE
            binding?.mainbottomsheet!!.countrytext.text!!.clear()
            binding?.mainbottomsheet!!.statetext.text!!.clear()
            SelectCountryAndState()
            openSheet()
        }
        fun openSheet() {
            if (sheet!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
                sheet!!.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        fun cancelAction(view: View) {
            closeSheet()
        }
        fun closeSheet() {
            if (sheet!!.state == BottomSheetBehavior.STATE_EXPANDED) {
                sheet!!.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        fun SubmitAddress() {
            if (binding!!.mainbottomsheet.firstname.text!!.toString().isEmpty()) {
                binding!!.mainbottomsheet.firstname.error = resources.getString(R.string.empty)
                binding!!.mainbottomsheet.firstname.requestFocus()
            } else {
                if (binding!!.mainbottomsheet.lastname.text!!.toString().isEmpty()) {
                    binding!!.mainbottomsheet.lastname.error = resources.getString(R.string.empty)
                    binding!!.mainbottomsheet.lastname.requestFocus()
                } else {
                    if (binding!!.mainbottomsheet.address1.text!!.toString().isEmpty()) {
                        binding!!.mainbottomsheet.address1.error =
                            resources.getString(R.string.empty)
                        binding!!.mainbottomsheet.address1.requestFocus()
                    } else {
                        if (binding!!.mainbottomsheet.address2.text!!.toString().isEmpty()) {
                            binding!!.mainbottomsheet.address2.error =
                                resources.getString(R.string.empty)
                            binding!!.mainbottomsheet.address2.requestFocus()
                        } else {
                            if(binding!!.mainbottomsheet.country.isVisible){
                                if (binding!!.mainbottomsheet.country.selectedItem!!.toString().equals(resources.getString(R.string.pleasechoosecountry))) {
                                    Toast.makeText(this@AddressList,resources.getString(R.string.pleasechoosecountry),Toast.LENGTH_LONG).show()
                                }
                                if(binding!!.mainbottomsheet.state.isVisible){
                                    if (binding!!.mainbottomsheet.state.selectedItem!!.toString().equals(resources.getString(R.string.state))) {
                                        Toast.makeText(this@AddressList,resources.getString(R.string.pleasechoosestate),Toast.LENGTH_LONG).show()
                                    }else{
                                        checkFurther()
                                    }
                                }
                            }else{
                                if (binding!!.mainbottomsheet.countrytext.text.isEmpty()) {
                                    binding!!.mainbottomsheet.countrytext.error = resources.getString(R.string.empty)
                                    binding!!.mainbottomsheet.countrytext.requestFocus()
                                }else{
                                    if (binding!!.mainbottomsheet.statetext.text.isEmpty()) {
                                        binding!!.mainbottomsheet.statetext.error = resources.getString(R.string.empty)
                                        binding!!.mainbottomsheet.statetext.requestFocus()
                                    }else{
                                        checkFurther()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        private fun checkFurther(){
            if (binding!!.mainbottomsheet.city.text!!.toString().isEmpty()) {
                binding!!.mainbottomsheet.city.error =
                    resources.getString(R.string.empty)
                binding!!.mainbottomsheet.city.requestFocus()
            }
            else {
                if (binding!!.mainbottomsheet.pincode.text!!.toString()
                        .isEmpty()
                ) {
                    binding!!.mainbottomsheet.pincode.error =
                        resources.getString(R.string.empty)
                    binding!!.mainbottomsheet.pincode.requestFocus()
                } else {
                    if (binding!!.mainbottomsheet.phone.text!!.toString()
                            .isEmpty()
                    ) {
                        binding!!.mainbottomsheet.phone.error =
                            resources.getString(R.string.empty)
                        binding!!.mainbottomsheet.phone.requestFocus()
                    } else {
                        closeSheet()
                        Proceed()
                    }
                }
            }
        }
        private fun Proceed() {
            val input = Storefront.MailingAddressInput()
            input.firstName = binding!!.mainbottomsheet.firstname.text!!.toString()
            input.lastName = binding!!.mainbottomsheet.lastname.text!!.toString()
            input.company = " "
            input.address1 = binding!!.mainbottomsheet.address1.text!!.toString()
            input.address2 = binding!!.mainbottomsheet.address2.text!!.toString()
            input.city = binding!!.mainbottomsheet.city.text!!.toString()
            if(binding!!.mainbottomsheet.country.isVisible){
                input.country = binding!!.mainbottomsheet.country.selectedItem.toString()
            }else{
                input.country = binding!!.mainbottomsheet.countrytext.text.toString()
            }
            if(binding!!.mainbottomsheet.state.isVisible){
                input.province = binding!!.mainbottomsheet.state.selectedItem.toString()
            }else{
                input.province = binding!!.mainbottomsheet.statetext.text.toString()
            }
            input.zip = binding!!.mainbottomsheet.pincode.text!!.toString()
            input.phone = binding!!.mainbottomsheet.phone.text!!.toString()
            when (tag) {
                "add" -> {
                    mailingAddressEdges = null
                    model!!.addAddress(input)
                }
                "edit" -> model!!.updateAddress(input, address!!.address_id)
            }
            binding!!.mainbottomsheet.firstname.text!!.clear()
            binding!!.mainbottomsheet.lastname.text!!.clear()
            binding!!.mainbottomsheet.address1.text!!.clear()
            binding!!.mainbottomsheet.address2.text!!.clear()
            binding!!.mainbottomsheet.city.text!!.clear()
//            binding!!.mainbottomsheet.country.setText("")
//            binding!!.mainbottomsheet.state.setText("")
            binding!!.mainbottomsheet.pincode.text!!.clear()
            binding!!.mainbottomsheet.phone.text!!.clear()
            //hideKeyboard(this@AddressList)
        }
    }

    override fun onBackPressed() {
        if (sheet!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheet!!.state = BottomSheetBehavior.STATE_COLLAPSED
        }else{
            super.onBackPressed()
        }
    }
}
