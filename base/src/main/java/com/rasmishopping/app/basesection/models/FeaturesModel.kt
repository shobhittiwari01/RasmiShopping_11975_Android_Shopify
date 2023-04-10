package com.rasmishopping.app.basesection.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR

class FeaturesModel : BaseObservable() {
    @Bindable
    var Spinner_Varient: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.Spinner_Varient)
        }

    @Bindable
    var WholeSale_Pricing: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.WholeSale_Pricing)
        }

    @Bindable
    var Enable_flits_App: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.Enable_flits_App)
        }

    @Bindable
    var enableInstafeed: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.enableInstafeed)
        }

    @Bindable
    var enablebackInStock: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.enablebackInStock)
        }

    @Bindable
    var zapietEnable: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.zapietEnable)
        }

    @Bindable
    var enableRewardify: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.enableRewardify)
        }
    var liveSale: Boolean = true
    var firstOrderSale: Boolean = false
    var memuWithApi: Boolean = false
    var collectionWithHandle: Boolean = false

    @Bindable
    var kiwisize: Boolean = false
    var langify: Boolean = false
    var langshop: Boolean = false
    var weglot: Boolean = false
    var fb_wt: Boolean = false
    var returnprime: Boolean = false
    var shipway_order_tracking: Boolean = false
    var algoliasearch: Boolean = false

    @Bindable
    var socialloginEnable: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.socialloginEnable)
        }

    @Bindable
    var multipassEnabled: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.multipassEnabled)
        }


    @Bindable
    var filterEnable: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.filterEnable)
        }

    @Bindable
    var localpickupEnable: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.localpickupEnable)
        }

    @Bindable
    var smileIO: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.smileIO)
        }

    @Bindable
    var appOnlyDiscount: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.appOnlyDiscount)
        }

    @Bindable
    var whatsappChat: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.whatsappChat)
        }

    @Bindable
    var zenDeskChat: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.zenDeskChat)
        }

    @Bindable
    var fbMessenger: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.fbMessenger)
        }

    @Bindable
    var tidioChat: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.tidioChat)
        }

    @Bindable
    var yoptoLoyalty: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.yoptoLoyalty)
        }

    @Bindable
    var forceUpdate: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.forceUpdate)
        }

    @Bindable
    var productListEnabled: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.productListEnabled)
        }

    @Bindable
    var firebaseEvents: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.firebaseEvents)
        }

    @Bindable
    var aliReviews: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.aliReviews)
        }

    @Bindable
    var nativeOrderView: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.nativeOrderView)
        }

    @Bindable
    var recommendedProducts: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.recommendedProducts)
        }

    @Bindable
    var reOrderEnabled: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.reOrderEnabled)
        }

    @Bindable
    var addCartEnabled: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.addCartEnabled)
        }

    @Bindable
    var sizeChartVisibility: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.sizeChartVisibility)
        }

    @Bindable
    var productReview: Boolean? = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.productReview)
        }


    @Bindable
    var outOfStock: Boolean? = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.outOfStock)
        }

    @Bindable
    var showBottomNavigation: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.showBottomNavigation)
        }

    @Bindable
    var judgemeProductReview: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.judgemeProductReview)
        }

    @Bindable
    var in_app_wishlist: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.in_app_wishlist)
        }

    @Bindable
    var rtl_support: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.rtl_support)
        }


    @Bindable
    var product_share: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.product_share)
        }

    @Bindable
    var multi_currency: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.multi_currency)
        }

    @Bindable
    var multi_language: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.multi_language)
        }

    @Bindable
    var abandoned_cart_compaigns: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.abandoned_cart_compaigns)
        }

    @Bindable
    var ai_product_reccomendaton: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.ai_product_reccomendaton)
        }

    @Bindable
    var qr_code_search_scanner: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.qr_code_search_scanner)
        }

    @Bindable
    var ardumented_reality: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.ardumented_reality)
        }


    @Bindable
    var native_checkout: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.native_checkout)
        }
    var deep_linking: Boolean = false
    @Bindable
    var mltranslation: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.mltranslation)
        }

    @Bindable
    var enablecartDiscountlisting: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.enablecartDiscountlisting)
        }
    var darkMode: String = "auto"
    var firstSale: Boolean = false
}