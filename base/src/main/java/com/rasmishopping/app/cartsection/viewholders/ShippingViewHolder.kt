package com.rasmishopping.app.cartsection.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.databinding.FragmentShippingMethodBinding
import com.rasmishopping.app.databinding.NewAddressListBinding

class ShippingViewHolder: RecyclerView.ViewHolder {
    var binding: FragmentShippingMethodBinding

    constructor(binding: FragmentShippingMethodBinding) : super(binding.root) {
        this.binding = binding
    }
}

