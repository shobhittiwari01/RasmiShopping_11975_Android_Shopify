package com.shopify.rewardifyappmodule

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class  TransactionModel(
    @SerializedName("id")
    val id: String?,
    @SerializedName("amount")
    val amount: String?,
    @SerializedName("customerOpenBalance")
    val customerOpenBalance: String?,
    @SerializedName("transactionType")
    val transactionType: String?,
    @SerializedName("effectiveAt")
    val effectiveAt: String?,
) : Serializable