package com.rasmishopping.app.userprofilesection.models


import com.google.gson.annotations.SerializedName

data class DeletUserResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)