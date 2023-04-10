package com.rasmishopping.app.dbconnection.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import java.io.Serializable

@Entity
class CartItemData : Serializable {
    @PrimaryKey
    @ColumnInfo(name = "variant_id")
    lateinit var variant_id: String

    @ColumnInfo(name = "qty")
    var qty: Int = 1
    @ColumnInfo(name = "selling_plan_id")
    lateinit var selling_plan_id: String
    @ColumnInfo(name = "offerName")
    lateinit var offerName: String
}
