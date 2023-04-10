package com.shopify.apicall

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.reactivex.Single
import org.json.JSONObject
import retrofit2.http.*

interface ApiCallInterface {
    @GET
    fun getCustomer(
        @Url url: String,
        @Header("Authorization") token: String
    ): Single<JsonElement>

    @GET
    fun getTransactions(
        @Url url: String,
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("itemsPerPage") itemsPerPage: Int,
    ): Single<JsonElement>

    @GET
    fun getDiscounts(
        @Url url: String,
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("itemsPerPage") itemsPerPage: Int,
    ): Single<JsonElement>

    @Headers("Content-Type: application/json")
    @PATCH
    fun discountRecover(
        @Url url: String,
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Single<JsonElement>

    @FormUrlEncoded
    @POST
    fun getToken(
        @Url url: String,
        @Field("client_id") clientid: String,
        @Field("client_secret") clientsecret: String,
        @Field("grant_type") grant_type: String
    ): Single<JsonElement>

    @Headers("Content-Type: application/json")
    @POST
    fun Redeem(
        @Url url: String,
        @Header("Authorization") token: String,
        @Body body: JsonObject
    ): Single<JsonElement>

    @GET("media")
    fun getInstafeeds(
        @Query("fields") fields: String,
        @Query("access_token") access_token: String?
    ): Single<JsonElement>

    @GET("shopifymobile/shopifyapi/sociologincustomer")
    fun getuserLogin(
        @Query("mid") mid: String?,
        @Query("email") email: String?
    ): Single<JsonElement>

    @GET
    fun getPlan(
        @Url url: String,
        @Header("X-Shopify-Access-Token") token: String
    ): Single<JsonElement>

    @POST
    fun SaveProfileData(
        @Header("x-integration-app-name") AppName: String,
        @Url url: String, @Query("token") token: String
    ): Single<JsonElement>

    @FormUrlEncoded
    @POST
    fun UpdateCustomerPassword(
        @Header("x-integration-app-name") AppName: String,
        @Url url: String,
        @Query("token") token: String,
        @Field("password") password: String,
        @Field("password_confirmation") confirm_password: String,
    ): Single<JsonElement>

    @GET
    fun getStoreCredits(
        @Header("x-integration-app-name") AppName: String,
        @Url url: String, @Query("token") token: String
    ): Single<JsonElement>

    @FormUrlEncoded
    @POST
    fun getSpentRules(
        @Header("x-integration-app-name") AppName: String,
        @Url url: String, @Query("token") token: String, @Field("cart") cart: String
    ): Single<JsonElement>

    @FormUrlEncoded
    @POST
    fun getEarnSpentGuideRules(
        @Header("x-integration-app-name") AppName: String,
        @Url url: String, @Field("token") token: String
    ): Single<JsonElement>

    @FormUrlEncoded
    @POST
    fun ApplyStoreCredit(
        @Header("x-integration-app-name") AppName: String,
        @Url url: String, @Field("token") token: String, @Field("data") cart_data: String,
        @Field("spent_rule_id") spentRuleId: Int, @Field("cart_token") cartToken: String
    ): Single<JsonElement>

    @FormUrlEncoded
    @POST
    fun SendWishlistData(
        @Header("x-integration-app-name") AppName: String,
        @Url url: String,
        @Query("token") token: String,
        @Field("customer_id") customer_id: String,
        @Field("customer_email") customer_email: String,
        @Field("product_id") product_id: String,
        @Field("product_handle") product_handle: String,
        @Field("wsl_product_count") wishlist_product_count: Int
    ): Single<JsonElement>

    @FormUrlEncoded
    @HTTP(method = "DELETE", hasBody = true)
    fun DeleteWishlistData(
        @Header("x-integration-app-name") AppName: String,
        @Url url: String,
        @Query("token") token: String,
        @Field("customer_id") customer_id: String,
        @Field("customer_email") customer_email: String,
        @Field("product_id") product_id: String,
        @Field("product_handle") product_handle: String,
        @Field("wsl_product_count") wishlist_product_count: Int
    ): Single<JsonElement>

    @POST("pickup/locations")
    fun DeliveryStatus(@Query("shop") shop: String): Single<JsonElement>

    @POST("calendar")
    fun StoreDeliverydates(@Query("shop") shop: String): Single<JsonElement>

    @POST("delivery/validate")
    fun validateDelivery(@Query("shop") shop: String): Single<JsonElement>

    @POST("delivery/locations")
    fun LocalDelivery(
        @Query("shop") shop: String,
        @Query("geoSearchQuery") geoSearchQuery: String
    ): Single<JsonElement>

    @POST("delivery/locations/204196/calendar")
    fun LocalDeliveryDates(@Query("shop") shop: String): Single<JsonElement>

    @GET("shipping/calendar")
    fun getShippingCalender(@Query("shop") shop: String?): Single<JsonElement>

    @POST("alertmerestockalertsapi/alertsubscriptionforemail")
    fun InStockAlert(
        @Query("email") email: String,
        @Query("shop") shop: String,
        @Query("product") product: String,
        @Query("variant") variant: String
    ): Single<JsonElement>

    @GET("shopifyapi/coupons")
    fun GetDiscountListing(@Query("mid") mid: String): Single<JsonElement>
}