package com.rasmishopping.app.cartsection.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.databinding.MAddressitemBinding
import com.rasmishopping.app.databinding.NewAddressListBinding

class NewAddressViewHolder : RecyclerView.ViewHolder {
    var binding: NewAddressListBinding

    constructor(binding: NewAddressListBinding) : super(binding.root) {
        this.binding = binding
    }
}

