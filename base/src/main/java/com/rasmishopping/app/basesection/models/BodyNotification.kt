package com.rasmishopping.app.basesection.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BodyNotification {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    @SerializedName("success")
    @Expose
    var success: Boolean? = null

}