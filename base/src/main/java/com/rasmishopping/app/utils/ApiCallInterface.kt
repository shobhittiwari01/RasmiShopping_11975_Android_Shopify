package com.rasmishopping.app.utils
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.rasmishopping.app.dbconnection.dependecyinjection.GPTBODY
import com.rasmishopping.app.utils.Urls.Data.BIRTHREWARDS
import com.rasmishopping.app.utils.Urls.Data.EARNREWARD
import com.rasmishopping.app.utils.Urls.Data.GETREWARDS
import com.rasmishopping.app.utils.Urls.Data.JUDGEME_REVIEWCOUNT
import com.rasmishopping.app.utils.Urls.Data.JUDGEME_REVIEWCREATE
import com.rasmishopping.app.utils.Urls.Data.JUDGEME_REVIEWINDEX
import com.rasmishopping.app.utils.Urls.Data.MYREWARDS
import com.rasmishopping.app.utils.Urls.Data.REDEEMPOINTS
import com.rasmishopping.app.utils.Urls.Data.SENDREFERRAL
import com.rasmishopping.app.utils.Urls.Data.SIZECHART
import com.rasmishopping.app.utils.Urls.Data.YOTPOAUTHENTICATE
import com.rasmishopping.app.utils.Urls.Data.YOTPOCREATEREVIEW
import com.squareup.okhttp.RequestBody
import io.reactivex.Single
import org.json.JSONObject
import retrofit2.http.*
interface ApiCallInterface {

    @GET(Urls.MENU)
    fun getMenus(@Query("mid") mid: String): Single<JsonElement>
    @GET(Urls.MENU)
    fun getMenus(@Query("mid") mid: String, @Query("code") code:String): Single<JsonElement>
    @GET(Urls.HOMEPAGE)
    fun getHomePage(@Query("mid") mid: String): Single<JsonElement>

    @GET(Urls.SETORDER)
    fun setOrder(
        @Query("mid") mid: String,
        @Query("checkout_token") checkout_token: String?
    ): Single<JsonElement>
    @GET(Urls.ORDERTAGS)
    fun OrderTags(
        @Query("mid") mid: String,
        @Query("order_id") id: String?,@Query("tags") tags: String?
    ): Single<JsonElement>

    @GET(Urls.SETDEVICES)
    fun setDevices(
        @Query("mid") mid: String,
        @Query("device_id") device_id: String,
        @Query("email") email: String,
        @Query("type") type: String,
        @Query("unique_id") unique_id: String
    ): Single<JsonElement>

    @Headers(Urls.HEADER) // Add the Domain-Name header
    @POST(Urls.RECOMMENDATION)
    fun getRecommendations(
        @Header("X-SHOP") shop: String,
        @Header("X-CLIENT") client: String,
        @Header("X-ACCESS-TOKEN") token: String,
        @Header("Content-Type") content_tyepe: String,
        @Body body: com.rasmishopping.app.dbconnection.dependecyinjection.Body
    ): Single<JsonElement>

    @GET("installedstatus")
    fun checkInstallStatusReviewApi(
        @Query("mid") mid: String?,
        @Query("product_id") productId: String?
    ): Single<JsonElement>

    @GET("index.php/shopifymobile/productreviewapi/badges")
    fun getBadges(
        @Query("mid") mid: String?,
        @Query("product_id") productId: String?
    ): Single<JsonElement>

    @GET("index.php/shopifymobile/productreviewapi/product")
    fun getReviewsList(
        @Query("mid") mid: String?,
        @Query("product_id") productId: String?,
        @Query("page") page: Int
    ): Single<JsonElement>

    @GET("index.php/shopifymobile/productreviewapi/create")
    fun createReview(
        @Query("mid") mid: String?,
        @Query("review[rating]") reviewRating: String?,
        @Query("product_id") productId: String?,
        @Query("review[author]") reviewAuthor: String?,
        @Query("review[email]") reviewEmail: String?,
        @Query("review[title]") reviewTitle: String?,
        @Query("review[body]") reviewBody: String?
    ): Single<JsonElement>


    @GET(SIZECHART)
    fun getSizeChart(
        @Query("shop") shop: String,
        @Query("source") source: String,
        @Query("product") productId: String?,
        @Query("tags") tags: String,
        @Query("vendor") vendor: String
    ): String

    @GET(JUDGEME_REVIEWCOUNT)
    fun getJudgemeReviewCount(
        @Query("api_token") api_token: String,
        @Query("shop_domain") shop_domain: String,
        @Query("product_id") product_id: String
    ): Single<JsonElement>

    @GET(JUDGEME_REVIEWINDEX)
    fun getJudgemeIndex(
        @Query("api_token") api_token: String,
        @Query("shop_domain") shop_domain: String,
        @Query("per_page") per_page: Int,
        @Query("page") page: Int,
        @Query("product_id") product_id: String
    ): Single<JsonElement>

    @POST(JUDGEME_REVIEWCREATE)
    fun createJudgemeReview(@Body params: JsonObject): Single<JsonElement>

    @GET
    fun getJudgemeProductID(
        @Url url: String,
        @Query("api_token") api_token: String,
        @Query("shop_domain") shop_domain: String,
        @Query("handle") handle: String
    ): Single<JsonElement>

    @GET()
    fun getAlireviewStatus(@Url fullpath:String): Single<JsonElement>

    @GET()
    fun getAliProductReview(
        @Url fullpath:String,
        @Query("shop_id") shop_id: String,
        @Query("product_id") product_id: String,
        @Query("currentPage") currentPage: Int
    ): Single<JsonElement>

    @GET(GETREWARDS)
    fun getrewards(
        @Header("x-guid") xguid: String,
        @Header("x-api-key") xapikey: String/*, @Query("customer_email") customer_email: String, @Query("customer_id") customer_id: String*/
    ): Single<JsonElement>

    @POST(REDEEMPOINTS)
    fun redeemPoints(
        @Header("x-guid") xguid: String,
        @Header("x-api-key") xapikey: String,
        @Query("customer_external_id") customer_external_id: String,
        @Query("customer_email") customer_email: String,
        @Query("redemption_option_id") redemption_option_id: String
    ): Single<JsonElement>

    @GET(EARNREWARD)
    fun earnRewards(
        @Header("x-guid") xguid: String,
        @Header("x-api-key") xapikey: String
    ): Single<JsonElement>

    @POST(BIRTHREWARDS)
    fun redeemBirthPoints(
        @Header("x-guid") xguid: String,
        @Header("x-api-key") xapikey: String,
        @Query("customer_email") customer_email: String,
        @Query("day") day: String,
        @Query("month") month: String,
        @Query("year") year: String
    ): Single<JsonElement>

    @GET(MYREWARDS)
    fun myrewards(
        @Header("x-guid") xguid: String,
        @Header("x-api-key") xapikey: String,
        @Query("customer_email") customer_email: String,
        @Query("customer_id") customer_id: String,
        @Query("with_referral_code") with_referral_code: Boolean,
        @Query("with_history") with_history: Boolean
    ): Single<JsonElement>

    @POST(SENDREFERRAL)
    fun referfriend(
        @Header("x-guid") xguid: String,
        @Header("x-api-key") xapikey: String,
        @Query("customer_id") customer_id: String,
        @Query("emails") emails: String
    ): Single<JsonElement>

    @POST(YOTPOAUTHENTICATE)
    fun yotpoauthentiate(
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("grant_type") grant_type: String
    ): Single<JsonElement>


    @GET(Urls.DISCOUNTCODEAPPLY)
    fun discountcodeapply(
        @Query("mid") mid: String?,
        @Query("customer_code") customer_code: String?
    ): Single<JsonElement>

    @POST(Urls.VALIDATE_DELIVERY)
    fun validateDelivery(@QueryMap params: HashMap<String, String>): Single<JsonObject>

    @POST(Urls.LOCAL_DELIVERY)
    fun localDelivery(@QueryMap params: HashMap<String, String>): Single<JsonObject>

    @POST(Urls.LOCAL_DELIVERYY)
    fun localDeliveryy(@QueryMap params: HashMap<String, String>): Single<JsonObject>

    @GET(Urls.DeliveryStatus)
    fun DeliveryStatus(@Query("mid") mid: String?): Single<JsonObject>

    @POST(Urls.LOCAL_DELIVERY)
    fun storeDelivery(@QueryMap params: HashMap<String, String>): Single<JsonObject>

    @GET(Urls.MENUCOLLECTION)
    fun getMenuCollection(
        @Query("mid") mid: String?,
        @Query("prop") prop: String?
    ): Single<JsonElement>

    @GET(Urls.FILTERTAGPRO)
    fun getCollectionProductsbyTags(
        @Query("mid") mid: String?, @Query("handle") handle: String?, @Query("sort") sort: String?,
        @Query("page") page: String?, @Query("tags") tags: String?
    ): Single<JsonElement>

    @GET("shopifymobile/shopifyapi/sociologincustomer")
    fun getuserLogin(
        @Query("mid") mid: String?,
        @Query("email") email: String?,
        @Query("firstname")firstname:String?,
        @Query("lastname")lastname:String?
    ): Single<JsonElement>
    @GET("shopifymobile/shopifyapi/sociologincustomer")
    fun checkFirstOrder(
        @Query("mid") mid: String?,
        @Query("email") email: String?,
        @Query("firstname")firstname:String?,
        @Query("lastname")lastname:String?
    ): Single<JsonElement>
    @GET("shopifymobilenew/shopifyapi/getrecords")
    fun getPlan(
        @Query("where[merchant]") mid: String?,
        @Query("where[customer_id]") email: String?
        ): Single<JsonElement>
    @POST
    fun SaveProfileData(
        @Url url: String,@Query ("token")token: String
    ): Single<JsonElement>

    @FormUrlEncoded
    @POST
    fun UpdateCustomerPassword(
        @Url url: String, @Query ("token")token: String, @Field("password") password:String,@Field("password_confirmation") confirm_password:String,
    ): Single<JsonElement>
    @GET
    fun getStoreCredits(
        @Url url: String, @Query ("token")token: String
    ): Single<JsonElement>
    @FormUrlEncoded
    @POST
    fun getSpentRules(
        @Url url: String, @Query ("token")token: String,@Field("cart") cart:String
    ): Single<JsonElement>
    @FormUrlEncoded
    @POST
    fun ApplyStoreCredit(
        @Url url: String, @Query ("token")token: String,@Field("data") cart_data:String,
        @Field("spent_rule_id") spentRuleId:Int,@Field("cart_token") cartToken:String
    ): Single<JsonElement>
    @FormUrlEncoded
    @POST
    fun SendWishlistData(
        @Url url: String, @Query ("token")token: String,@Field("customer_id") customer_id:String,@Field("customer_email")customer_email:String,
        @Field("product_id")product_id:String,@Field("product_handle")product_handle:String,
        @Field("wsl_product_count")wishlist_product_count:Int
    ): Single<JsonElement>
    @FormUrlEncoded
    @HTTP(method = "DELETE",hasBody = true)
    fun DeleteWishlistData(
        @Url url: String, @Query ("token")token: String,@Field("customer_id") customer_id:String,@Field("customer_email")customer_email:String,
        @Field("product_id")product_id:String,@Field("product_handle")product_handle:String,
        @Field("wsl_product_count")wishlist_product_count:Int
    ): Single<JsonElement>


    @POST("shopifymobile/shopifyapi/deletecustomer")
     fun deleteUserData(@Query ("mid")mid: String, @Query("cid") customer_id:String
    ):Single<JsonElement>

    @POST(Urls.WHOLESALEPRICEURL)
     fun Wholesalepricedata(@Header("Authorization")authorization:String, @Header("X-WH-API-KEY")apiKey:String, @Body params:JsonObject
    ): Single<JsonElement>
    @GET(Urls.WHOLESALEDISCOUNTCOUPONURL)
    fun GetWholeSaleDiscountCoupon(@Query("mid")mid:String, @Query("coupon")Coupon:String, @Query("type")type:String, @Query("value")discount_percentage:String): Single<JsonElement>

    @POST(YOTPOCREATEREVIEW)
    fun yotpocretereview(
        @Query("appkey") appkey: String,
        @Query("sku") sku: String,
        @Query("product_title") product_title: String,
        @Query("product_url") product_url: String,
        @Query("display_name") display_name: String,
        @Query("email") email: String,
        @Query("review_content") review_content: String,
        @Query("review_title") review_title: String,
        @Query("review_score") review_score: String
    ): Single<JsonElement>

    @GET(Urls.NOTIFICATIONCENTRE)
    fun notificationcentre(
        @Query("key") key: String?,
        @Query("mid") mid: String?,
        @Query("page") page: Int?
    ): Single<JsonElement>




    @POST(Urls.gpturl)
    @Headers("Content-Type: application/json")
    fun getChatGPT(
        @Header("Authorization") authorization: String,
        @Body body: GPTBODY
    ): Single<JsonElement>
}