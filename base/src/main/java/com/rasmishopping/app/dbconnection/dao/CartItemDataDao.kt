package com.rasmishopping.app.dbconnection.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rasmishopping.app.dbconnection.entities.CartItemData

@Dao
interface  CartItemDataDao {

    @get:Query("SELECT * FROM cartitemdata")
    val all: List<CartItemData>

    @get:Query("SELECT * FROM cartitemdata")
    val cart_count: LiveData<List<CartItemData>>


    @Query("SELECT * FROM cartitemdata WHERE variant_id = :id")
    fun getSingleData(id: String): CartItemData

    @Query("SELECT * FROM cartitemdata WHERE selling_plan_id  <> ''")
    fun getSellingPlanData(): CartItemData

    @Insert
    fun insert(data: CartItemData)

    @Delete
    fun delete(data: CartItemData)

    @Update
    fun update(data: CartItemData)

    @Query("DELETE  FROM cartitemdata")
    fun deleteCart()

}
