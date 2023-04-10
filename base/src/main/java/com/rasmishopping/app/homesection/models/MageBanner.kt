package com.rasmishopping.app.homesection.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MageBanner {
    @SerializedName("link_type")
    @Expose
    var link_type: String? = null

    @SerializedName("link_value")
    @Expose
    var link_value: String? = null

    @SerializedName("image_url")
    @Expose
    var image_url: String? = null

    @SerializedName("title")
    @Expose
    var title: String? = null
}