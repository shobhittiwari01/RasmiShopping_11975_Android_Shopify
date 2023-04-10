package com.rasmishopping.app.cartsection.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import com.shopify.graphql.support.ID

class PricesModel : BaseObservable() {
    @Bindable
    var subtotal: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.subtotal)
        }

    @Bindable
    var tax: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.tax)
        }

    @Bindable
    var grandtotal: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.grandtotal)
        }

    @get:Bindable
    var subtotaltext: String? = null
        set(subtotaltext) {
            field = subtotaltext
            notifyPropertyChanged(BR.subtotaltext)
        }

    @Bindable
    var checkoutId: ID? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.checkoutId)
        }

    @Bindable
    var checkouturl: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.checkouturl)
        }
    var giftcardID: ID? = null
}
