package com.rasmishopping.app.cartsection.adapters

import android.graphics.Color
import android.util.Log
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
import com.rasmishopping.app.cartsection.viewholders.NewAddressViewHolder
import com.rasmishopping.app.databinding.MAddressitemBinding
import com.rasmishopping.app.databinding.NewAddressListBinding
import com.rasmishopping.app.productsection.adapters.SellingPlanGroupAdapter
import javax.inject.Inject

class NewAddressListAdapter @Inject
constructor() : RecyclerView.Adapter<NewAddressViewHolder>() {
    var data: MutableList<Storefront.MailingAddressEdge>? = null
    private var layoutInflater: LayoutInflater? = null
    private var model: AddressModel? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewAddressViewHolder {
        val binding = DataBindingUtil.inflate<NewAddressListBinding>(
            LayoutInflater.from(parent.context),
            R.layout.new_address_list,
            parent,
            false
        )
        return NewAddressViewHolder(binding)
    }
    companion object {
        var variantCallback: VariantCallback? = null
    }

    interface VariantCallback {
        fun clickVariant(variantName: Address)
    }
    override fun onBindViewHolder(holder: NewAddressViewHolder, position: Int) {
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
        address.phone = data?.get(position)?.node?.phone ?: ""
        address.zip = data?.get(position)?.node?.zip ?: ""
        address.province = data?.get(position)?.node?.province ?: ""
        holder.binding.handler = ClickHandler()
        holder.binding.address = address
        holder.binding.delete.setOnClickListener {


        }
        holder.binding.addressCard.setOnClickListener {
              variantCallback?.clickVariant(address)
            holder.binding.addressLayout.setBackgroundColor(Color.GRAY)
            holder.binding.address = address
            Log.d("Adresss",""+address)


        }
    }

    override fun getItemCount(): Int {
        return data!!.size
    }

    fun setData(data: MutableList<Storefront.MailingAddressEdge>, model: AddressModel?,variantCallback_:VariantCallback) {
        this.data = data
        this.model = model
        variantCallback = variantCallback_
    }

    inner class ClickHandler {
        fun deleteAddress(view: View, address: Address) {
            var alertDialog = SweetAlertDialog(view.context, SweetAlertDialog.NORMAL_TYPE)
            alertDialog.titleText =view.context.getString(R.string.confirmation)
            alertDialog.contentText = view.context.getString(R.string.update_msg)
            alertDialog.confirmText = view.context.getString(R.string.dialog_ok)
            alertDialog.cancelText = view.context.getString(R.string.no)
            alertDialog.setConfirmClickListener { sweetAlertDialog ->
                model!!.deleteAddress(view.resources.getString(R.string.deleteaddress), address)
                data!!.removeAt(address.position)
                notifyItemRemoved(address.position)
                notifyItemRangeChanged(address.position, data!!.size)
                sweetAlertDialog.dismissWithAnimation()
            }
            alertDialog.show()

        }

        fun editAddress(view: View, address: Address) {
            model!!.setSheet()
            model!!.setAddress(address)
        }
    }
}