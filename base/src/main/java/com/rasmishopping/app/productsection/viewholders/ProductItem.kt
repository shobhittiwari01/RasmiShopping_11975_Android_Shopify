package com.rasmishopping.app.productsection.viewholders
import androidx.recyclerview.widget.RecyclerView
import com.rasmishopping.app.databinding.MPersonalisedBinding
import com.rasmishopping.app.databinding.MProductitemBinding
import com.rasmishopping.app.databinding.MRecentBinding
import com.rasmishopping.app.databinding.MSearchitemBinding
class ProductItem : RecyclerView.ViewHolder {
    var binding: MProductitemBinding? = null
    var personalbinding: MPersonalisedBinding? = null
    var searchbinding: MSearchitemBinding? = null
    var recentbinding: MRecentBinding? = null
    constructor(binding: MProductitemBinding) : super(binding.root) {
        this.binding = binding
    }
    constructor(personalbinding: MPersonalisedBinding) : super(personalbinding.root) {
        this.personalbinding = personalbinding
    }
    constructor(searchbinding: MSearchitemBinding) : super(searchbinding.root) {
        this.searchbinding = searchbinding
    }
    constructor(recentbinding: MRecentBinding) : super(recentbinding.root) {
        this.recentbinding = recentbinding
    }
}

