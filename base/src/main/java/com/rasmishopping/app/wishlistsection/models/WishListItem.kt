package com.rasmishopping.app.wishlistsection.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

class WishListItem : BaseObservable() {
    @get:Bindable
    var variant_id: String? = null
        set(variant_id) {
            field = variant_id
            notifyPropertyChanged(BR.variant_id)
        }
    var productname: String? = null
    var available: Boolean?=null
    var available_qty: String? = null
    var selling_price_id: String? = null
    var normalprice: String? = null
    var product_id: String? = null
    var specialprice: String? = null
    var variant_one: String? = null
    var variant_two: String? = null
    var variant_three: String? = null
    var isSet_strike: Boolean = false
    var isImage: String? = null
    var offertext: String? = null

    @get:Bindable
    var position: Int = 0
        set(position) {
            field = position
            notifyPropertyChanged(BR.position)
        }
}
