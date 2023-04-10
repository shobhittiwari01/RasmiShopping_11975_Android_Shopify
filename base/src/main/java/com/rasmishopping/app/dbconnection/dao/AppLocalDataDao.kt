package com.rasmishopping.app.dbconnection.dao

import androidx.room.*
import com.rasmishopping.app.dbconnection.entities.AppLocalData
import com.rasmishopping.app.dbconnection.entities.CustomerTokenData
import com.rasmishopping.app.dbconnection.entities.UserLocalData

@Dao
interface AppLocalDataDao {

    @get:Query("SELECT * FROM applocaldata")
    val all: List<AppLocalData>

    /***
     *
     * @return UserLocalData
     */

    @get:Query("SELECT * FROM UserLocalData")
    val allUserData: List<UserLocalData>

    @get:Query("SELECT * FROM CustomerTokenData")
    val customerToken: List<CustomerTokenData>

    @Insert
    fun insert(appLocalData: AppLocalData)

    @Query("DELETE  FROM AppLocalData")
    fun delete()

    @Update
    fun update(appLocalData: AppLocalData)


    @Query("UPDATE AppLocalData SET currencycode=:code WHERE id= 1")
    fun currencyupdate(code:String)
    @Insert
    fun insertUserData(UserLocalData: UserLocalData)

    @Delete
    fun deleteUserData(UserLocalData: UserLocalData)

    @Update
    fun updateUserData(UserLocalData: UserLocalData)

    @Query("DELETE  FROM UserLocalData")
    fun deletealldata()

    /***
     *
     * @param customerTokenData
     */

    @Insert
    fun InsertCustomerToken(customerTokenData: CustomerTokenData)

    @Update
    fun UpdateCustomerToken(customerTokenData: CustomerTokenData)

    @Delete
    fun deleteCustomerToken(CustomerTokenData: CustomerTokenData)

    @Query("DELETE  FROM CustomerTokenData")
    fun deleteall()

}
