package com.rasmishopping.app.dbconnection.dao

import androidx.room.*
import com.rasmishopping.app.dbconnection.entities.LivePreviewData

@Dao
interface LivePreviewDao {

    @get:Query("SELECT * FROM LivePreviewData")
    val getPreviewDetails: List<LivePreviewData>

    @Insert
    fun insert(data: LivePreviewData)

    @Query("DELETE  FROM LivePreviewData")
    fun delete()

    @Update
    fun update(data: LivePreviewData)

}
