package com.rasmishopping.app.dbconnection.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rasmishopping.app.dbconnection.entities.CartItemData
import com.rasmishopping.app.dbconnection.entities.WishItemData

@Dao
interface WishItemDataDao {
    @get:Query("SELECT * FROM wishitemdata")
    val all: List<WishItemData>

    @get:Query("SELECT * FROM wishitemdata")
    val wish_count: LiveData<List<WishItemData>>

    @Query("SELECT * FROM wishitemdata WHERE variant_id = :id")
    fun getSingleVariantData(id: String): WishItemData
    @Query("SELECT * FROM wishitemdata WHERE selling_plan_id = :selling_plan_id")
    fun getSellingPlanData(selling_plan_id: String): WishItemData

    @Insert
    fun insert(data: WishItemData)

    @Delete
    fun delete(data: WishItemData)

    @Update
    fun update(data: WishItemData)

    @Query("DELETE  FROM wishitemdata")
    fun deleteall()
}