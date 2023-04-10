package com.rasmishopping.app.cartsection.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.databinding.MDiscountlistingBinding

class DiscountItems : RecyclerView.ViewHolder {
    lateinit var binding: MDiscountlistingBinding
    constructor(binding: MDiscountlistingBinding) : super(binding.root) {
        this.binding = binding
    }
}
