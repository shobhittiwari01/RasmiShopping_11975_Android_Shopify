package com.rasmishopping.app.dbconnection.entities
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import java.io.Serializable

@Entity
class HomePageProduct : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    @ColumnInfo(name = "product_id")
    @TypeConverters
    lateinit var product_id: String
    @TypeConverters
    @ColumnInfo(name = "product")
    lateinit var product: String
    @TypeConverters
    @ColumnInfo(name = "category_id")
     var category_id: String=""
    @TypeConverters
    @ColumnInfo(name = "uniqueId")
     var uniqueId: String =""
}
