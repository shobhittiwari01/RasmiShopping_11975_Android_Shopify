package com.rasmishopping.app.dbconnection.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class WishItemData {
    @PrimaryKey
    @ColumnInfo(name = "variant_id")
    lateinit var variant_id: String

    @ColumnInfo(name = "selling_plan_id")
    lateinit var selling_plan_id: String
}