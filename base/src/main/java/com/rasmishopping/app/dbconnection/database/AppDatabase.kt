package com.rasmishopping.app.dbconnection.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rasmishopping.app.dbconnection.dao.*
import com.rasmishopping.app.dbconnection.entities.*

@Database(
    entities = [HomePageProduct::class,AppLocalData::class, UserLocalData::class, CustomerTokenData::class, ItemData::class, CartItemData::class, LivePreviewData::class, WishItemData::class],
    version = 19
)
abstract class AppDatabase : RoomDatabase() {
    abstract val itemDataDao: ItemDataDao
    abstract val cartItemDataDao: CartItemDataDao
    abstract val appLocalDataDao: AppLocalDataDao
    abstract fun getLivePreviewDao(): LivePreviewDao
    abstract fun wishitemDataDao(): WishItemDataDao
    abstract fun HomePageProductDao(): HomePageProductDao
}
