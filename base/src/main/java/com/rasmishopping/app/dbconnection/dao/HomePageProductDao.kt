package com.rasmishopping.app.dbconnection.dao
import androidx.room.*
import com.shopify.graphql.support.ID
import com.rasmishopping.app.dbconnection.entities.HomePageProduct
@Dao
interface HomePageProductDao {

    @Query("SELECT * FROM homepageproduct WHERE product_id = :id")
    fun getProduct(id: String): List<HomePageProduct>
    @Query("SELECT * FROM homepageproduct WHERE category_id = :id AND uniqueId = :unique_id")
    fun getProductsByCatId(id: String,unique_id:String): List<HomePageProduct>
    @Query("SELECT * FROM homepageproduct WHERE category_id = :id AND uniqueId = :unique_id AND product_id = :product_id")
    fun getProductsByCatId_product(id: String,unique_id:String,product_id:String): List<HomePageProduct>
    @Insert
    fun insert(data: HomePageProduct)

    @Delete
    fun delete(data: HomePageProduct)

    @Update
    fun update(data: HomePageProduct)

    @Query("SELECT * FROM homepageproduct")
    fun getProducts(): List<HomePageProduct>

    @Query("DELETE  FROM homepageproduct")
    fun deleteall()

    @Query("DELETE  FROM homepageproduct WHERE category_id = :id AND uniqueId = :unique_id")
    fun deleteCategoryProducts(id: String,unique_id:String)

}
