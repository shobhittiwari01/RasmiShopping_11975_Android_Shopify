package com.rasmishopping.app.homesection.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.databinding.MSlideritemdynamicBinding

class ProductSliderDynamic : RecyclerView.ViewHolder {
    lateinit var binding: MSlideritemdynamicBinding
    constructor(binding: MSlideritemdynamicBinding) : super(binding.root) {
        this.binding = binding
    }
}
