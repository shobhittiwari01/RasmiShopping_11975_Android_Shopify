package com.rasmishopping.app.basesection.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Data {
    @SerializedName("send")
    @Expose
    var send: ArrayList<Notification>? = null

    @SerializedName("created")
    @Expose
    var created: ArrayList<Notification>? = null

    @SerializedName("scheduled")
    @Expose
    var scheduled: ArrayList<Notification>? = null
}