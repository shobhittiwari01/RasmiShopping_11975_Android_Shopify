package com.rasmishopping.app.dbconnection.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import java.io.Serializable
import java.util.*

@Entity(tableName = "CustomerTokenData")
class CustomerTokenData(
    @field:ColumnInfo(name = "CustomerAccessToken")
    var customerAccessToken: String?, expireTime: String
) : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @ColumnInfo(name = "ExpireTime")
    var expireTime: String? = null
        get() = field!!.lowercase(Locale.getDefault())

    @ColumnInfo(name = "email")
    var email: String? = null

    init {
        this.expireTime = expireTime
    }
}
