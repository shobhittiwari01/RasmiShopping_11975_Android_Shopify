package com.rasmishopping.app.productsection.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.databinding.MPersonalisedBinding
import com.rasmishopping.app.databinding.MProductfilteritemBinding

class ProductFilterItem : RecyclerView.ViewHolder {
    var binding: MProductfilteritemBinding? = null
    var personalbinding: MPersonalisedBinding? = null

    constructor(binding: MProductfilteritemBinding) : super(binding.root) {
        this.binding = binding
    }

    constructor(personalbinding: MPersonalisedBinding) : super(personalbinding.root) {
        this.personalbinding = personalbinding
    }
}
