package com.rasmishopping.app.basesection.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Notification {
    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("merchant_id")
    @Expose
    var merchant_id: String? = null

    @SerializedName("notification_data")
    @Expose
    var notification_data: String? = null

    @SerializedName("total_device_count")
    @Expose
    var total_device_count: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("total_success_count")
    @Expose
    var total_success_count: String? = null

    @SerializedName("created_at")
    @Expose
    var created_at: String? = null
    @SerializedName("updated_at")
    @Expose
    var updated_at: String? = null
}