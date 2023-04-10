package com.rasmishopping.app.addresssection.adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.shopify.buy3.Storefront
import com.rasmishopping.app.R
import com.rasmishopping.app.addresssection.models.Address
import com.rasmishopping.app.addresssection.viewholders.AddressViewHolder
import com.rasmishopping.app.addresssection.viewmodels.AddressModel
import com.rasmishopping.app.databinding.MAddressitemBinding
import com.rasmishopping.app.databinding.PopConfirmationBinding
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
class AddressListAdapter @Inject
constructor() : RecyclerView.Adapter<AddressViewHolder>() {
    var data: MutableList<Storefront.MailingAddressEdge>? = null
    private var layoutInflater: LayoutInflater? = null
    private var model: AddressModel? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding = DataBindingUtil.inflate<MAddressitemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.m_addressitem,
            parent,
            false
        )
        return AddressViewHolder(binding)
    }
    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val address = Address()
        address.position = position
        address.address_id = data?.get(position)?.node?.id
        address.firstName = data?.get(position)?.node?.firstName
        address.lastName = data?.get(position)?.node?.lastName
        if (data?.get(position)?.node?.address1 != null) {
            address.address1 = data?.get(position)?.node?.address1
            holder.binding.address1.visibility = View.VISIBLE
        }
        if (data?.get(position)?.node?.address2 != null) {
            address.address2 = data?.get(position)?.node?.address2
            holder.binding.address2.visibility = View.VISIBLE
        }
        address.city = data?.get(position)?.node?.city ?: ""
        address.country = data?.get(position)?.node?.country ?: ""
        address.phone = data?.get(position)?.node?.phone ?: "" .replace(" ","")
        address.zip = data?.get(position)?.node?.zip ?: ""
        address.province = data?.get(position)?.node?.province ?: ""
        holder.binding.address = address
        holder.binding.handler = ClickHandler()


    }
    override fun getItemCount(): Int {
        return data!!.size
    }
    fun setData(data: MutableList<Storefront.MailingAddressEdge>, model: AddressModel?) {
        this.data = data
        this.model = model
    }

    inner class ClickHandler {
        fun deleteAddress(view: View, address: Address) {

                val alertDialog = SweetAlertDialog(view.context, SweetAlertDialog.NORMAL_TYPE)
                var customeview = PopConfirmationBinding.inflate(LayoutInflater.from(view.context))
                customeview.textView.text=view.context.getString(R.string.confirmation)
                customeview.textView2.text=view.context.getString(R.string.delete_msg)
                alertDialog.hideConfirmButton()
                customeview.okDialog.setOnClickListener{
                    customeview.okDialog.isClickable=false
                    customeview.textView.text=view.context.getString(R.string.done)
                    customeview.textView2.text=view.resources.getString(R.string.deleteaddress)
                    alertDialog.showCancelButton(false)
                    alertDialog.setConfirmClickListener(null)
                    alertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                    model!!.deleteAddress(view.resources.getString(R.string.deleteaddress), address)
                    data!!.removeAt(address.position)
                    notifyItemRemoved(address.position)
                    notifyItemRangeChanged(address.position, data!!.size)
                    alertDialog.dismissWithAnimation()
                    //alertDialog.cancel()
                }
                customeview.noDialog.setOnClickListener{
                    customeview.noDialog.isClickable=false
                    alertDialog.dismissWithAnimation()
                }
                alertDialog.setCustomView(customeview.root)
                alertDialog.show()


        }
        fun editAddress(view: View, address: Address) {
            model!!.setSheet()
            model!!.setAddress(address)
        }
    }
}
