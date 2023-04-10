package com.rasmishopping.app.productsection.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.databinding.MPersonalisedBinding
import com.rasmishopping.app.databinding.MProductitemBinding
import com.rasmishopping.app.databinding.ProductListItemBinding

class ProductListItem : RecyclerView.ViewHolder {
    var binding: ProductListItemBinding? = null

    constructor(binding: ProductListItemBinding) : super(binding.root) {
        this.binding = binding
    }

}
