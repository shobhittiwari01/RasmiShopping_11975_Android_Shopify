package com.rasmishopping.app.dbconnection.dependecyinjection

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GPTBODY {
    @SerializedName("model")
    @Expose
    var model: String? = null

    @SerializedName("prompt")
    @Expose
    var prompt: String? = null

    @SerializedName("temperature")
    @Expose
    var temperature: Double? = null

    @SerializedName("max_tokens")
    @Expose
    var max_tokens: Int? = null

    @SerializedName("top_p")
    @Expose
    var top_p: Double? = null

    @SerializedName("frequency_penalty")
    @Expose
    var frequency_penalty: Double? = null

    @SerializedName("presence_penalty")
    @Expose
    var presence_penalty: Double? = null

}