package com.rasmishopping.app.basesection.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import com.shopify.buy3.Storefront
import java.io.Serializable

class MenuData : BaseObservable() , Serializable {

    var menuitems: ArrayList<Storefront.MenuItem>? = null
        get() = field
        set(value) {
            field = value
        }
    var id: String? = null
    var url: String? = null
    var type: String? = null
    var handle: String? = null
    var appversion: String? = null
    var copyright: String? = null

    @get:Bindable
    var username: String? = null
        set(username) {
            field = username
            notifyPropertyChanged(BR.username)
        }

    @get:Bindable
    var title: String? = null
        set(title) {
            field = title
            notifyPropertyChanged(BR.title)
        }

    @get:Bindable
    var tag: String? = null
        set(tag) {
            field = tag
            notifyPropertyChanged(BR.tag)
        }

    @get:Bindable
    var visible: Int = 0
        set(visible) {
            field = visible
            notifyPropertyChanged(BR.visible)
        }

    @get:Bindable
    var previewvislible: Int = 0
        set(previewvislible) {
            field = previewvislible
            notifyPropertyChanged(BR.previewvislible)
        }

    var menuItems: ArrayList<Storefront.MenuItem>? = null
       get()=field
      set(value) {
        field = value
       }
}
