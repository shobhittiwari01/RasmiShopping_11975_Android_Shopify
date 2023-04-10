package com.rasmishopping.app.repositories

import android.content.Intent
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.shopify.buy3.GraphClient
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.MyApplication.Companion.context
import com.rasmishopping.app.dbconnection.database.AppDatabase
import com.rasmishopping.app.dbconnection.entities.*
import com.rasmishopping.app.dbconnection.dependecyinjection.Body
import com.rasmishopping.app.dbconnection.dependecyinjection.GPTBODY
import com.rasmishopping.app.productsection.models.MediaModel
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.userprofilesection.models.DeletUserResponse
import com.rasmishopping.app.utils.ApiCallInterface
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.Urls
import com.squareup.okhttp.RequestBody
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit


class Repository {
    private val TAG = "Repository"
    private var apiCallInterface: ApiCallInterface
    private val appdatabase: AppDatabase
    
    val deleteUserRes :MutableLiveData<DeletUserResponse> = MutableLiveData()
    /*fun setUp() {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .build()

        var graphClient = GraphClient.build(context = context, shopDomain = Urls(MyApplication.context).shopdomain, Urls(MyApplication.context).apikey, configure = {
            httpClient = okHttpClient
            httpCache(file)
        })
    }*/
    fun  getFile():File{
        val file = File(
            Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOCUMENTS).toString() + "/" + File.separator + "cache.txt"
        )
        file.createNewFile()
        return file
    }

    val graphClient: GraphClient
        get() {
            return GraphClient.build(
                context,
                Urls(MyApplication.context).shopdomain,
                Urls(MyApplication.context).apikey,
                {
                    httpClient = requestHeader
                   /* httpCache(getFile(), {
                        cacheMaxSizeBytes = 1024 * 1024 * 10
                        defaultCachePolicy = Constant.policy
                        Unit
                    })*/
                    Unit
                },
                MagePrefs.getLanguage().toString().toLowerCase()
            )
        }
    internal val requestHeader: OkHttpClient
        get() {
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder().build()
                chain.proceed(request)
            }
                .connectTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
            return httpClient.build()
        }

    constructor(apiCallInterface: ApiCallInterface, appdatabase: AppDatabase) {
        this.apiCallInterface = apiCallInterface
        this.appdatabase = appdatabase
    }

    val localData: List<AppLocalData>
        get() = appdatabase.appLocalDataDao.all
    val wishListData: List<ItemData>
        get() = appdatabase.itemDataDao.all
//
//    val wishListDataCount: LiveData<List<ItemData>>
//        get() = appdatabase.itemDataDao.wish_count

    val allCartItems: List<CartItemData>
        get() = appdatabase.cartItemDataDao.all

    val allCartItemsCount: LiveData<List<CartItemData>>
        get() = appdatabase.cartItemDataDao.cart_count

    val allUserData: List<UserLocalData>
        get() = appdatabase.appLocalDataDao.allUserData
    val isLogin: Boolean
        get() = appdatabase.appLocalDataDao.customerToken.size > 0
    val accessToken: List<CustomerTokenData>
        get() {
            var customerToken = runBlocking(Dispatchers.IO) {
                return@runBlocking appdatabase.appLocalDataDao.customerToken
            }
            return customerToken
        }
    //   get() = appdatabase.appLocalDataDao.customerToken

    fun getMenus(mid: String, code: String): Single<JsonElement> {
        return apiCallInterface.getMenus(mid,code.lowercase())
    }

    fun getRecommendation(body: Body): Single<JsonElement> {
        Log.i("MageNative", "Cross-sell-3" + body)
        return apiCallInterface.getRecommendations(
            Urls(context).shopdomain,
            Urls.CLIENT,
            Urls.TOKEN,
            "application/json",
            body
        )
    }

    fun getHomePage(mid: String): Single<JsonElement> {
        return apiCallInterface.getHomePage(mid)
    }

    fun setDevice(
        mid: String,
        device_id: String,
        email: String,
        type: String,
        unique_id: String
    ): Single<JsonElement> {
        return apiCallInterface.setDevices(mid, device_id, email, type, unique_id)
    }

    fun setOrder(mid: String, checkout_token: String?): Single<JsonElement> {
        return apiCallInterface.setOrder(mid, checkout_token)
    }
    fun OrderTags(mid: String, id: String?,tags:String): Single<JsonElement> {
        return apiCallInterface.OrderTags(mid, id,tags)
    }


    fun getList(list: List<Storefront.ProductVariantEdge>): Observable<Storefront.ProductVariantEdge> {
        return Observable.fromIterable(list)
    }

    fun getProductList(list: List<Storefront.ProductEdge>): Observable<Storefront.ProductEdge> {
        return Observable.fromIterable(list)
    }

    fun getProductListSlider(list: List<Storefront.Product>): Observable<Storefront.Product> {
        return Observable.fromIterable(list)
    }

    fun getArModels(list: MutableList<MediaModel>): Observable<MediaModel> {
        return Observable.fromIterable(list)
    }

    fun getJSonArray(list: JsonArray): Observable<JsonElement> {
        return Observable.fromIterable(list)
    }

    fun insertData(data: AppLocalData) {
        appdatabase.appLocalDataDao.insert(data)
    }

    fun updateData(data: AppLocalData) {
        appdatabase.appLocalDataDao.update(data)
    }
    fun currencyupdate(data: String) {
        appdatabase.appLocalDataDao.currencyupdate(data)
    }

    fun deleteLocalData() {
        appdatabase.appLocalDataDao.delete()
    }

    fun insertWishListData(data: ItemData) {
        appdatabase.itemDataDao.insert(data)
    }

    fun getSingleData(id: String): ItemData {
        return appdatabase.itemDataDao.getSingleData(id)
    }

    fun deleteSingleData(data: ItemData) {
        appdatabase.itemDataDao.delete(data)
    }

    fun getSingle(data: AppLocalData): Single<AppLocalData> {
        return Single.just(data)
    }

    fun getSingLeItem(id: String): CartItemData {
        return appdatabase.cartItemDataDao.getSingleData(id)
    }

    fun getSellingPlanData(): CartItemData {
        return appdatabase.cartItemDataDao.getSellingPlanData()
    }

    fun addSingLeItem(data: CartItemData) {

        appdatabase.cartItemDataDao.insert(data)
    }

    fun updateSingLeItem(data: CartItemData) {
        appdatabase.cartItemDataDao.update(data)
    }

    fun deleteSingLeItem(data: CartItemData) {
        appdatabase.cartItemDataDao.delete(data)
    }

    fun deletecart() {
        appdatabase.cartItemDataDao.deleteCart()
    }

    fun insertUserData(data: UserLocalData) {
        appdatabase.appLocalDataDao.insertUserData(data)
    }

    fun updateUserData(data: UserLocalData) {
        appdatabase.appLocalDataDao.updateUserData(data)
    }

    fun saveaccesstoken(token: CustomerTokenData) {
        appdatabase.appLocalDataDao.InsertCustomerToken(token)

    }

    fun updateAccessToken(data: CustomerTokenData) {
        appdatabase.appLocalDataDao.UpdateCustomerToken(data)
    }

    fun deleteWishListData() {
        appdatabase.wishitemDataDao().deleteall()
    }

    fun deleteUserData() {
        appdatabase.appLocalDataDao.deletealldata()
        appdatabase.appLocalDataDao.deleteall()
    }

    fun insertPreviewData(data: LivePreviewData) {
        appdatabase.getLivePreviewDao().insert(data)
    }

    fun updatePreviewData(data: LivePreviewData) {
        appdatabase.getLivePreviewDao().update(data)
    }

    fun getPreviewData(): List<LivePreviewData> {
        return appdatabase.getLivePreviewDao().getPreviewDetails
    }

    fun deletePreviewData() {
        return appdatabase.getLivePreviewDao().delete()
    }

    fun getProductReviews(mid: String, product_id: String, page: Int): Single<JsonElement> {
        return apiCallInterface.getReviewsList(mid, product_id, page)
    }

    fun getbadgeReviews(mid: String, product_id: String): Single<JsonElement> {
        return apiCallInterface.getBadges(mid, product_id)
    }

    fun getcreateReview(
        mid: String,
        reviewRating: String,
        product_id: String,
        reviewAuthor: String,
        reviewEmail: String,
        reviewTitle: String,
        reviewBody: String
    ): Single<JsonElement> {
        return apiCallInterface.createReview(
            mid,
            reviewRating,
            product_id,
            reviewAuthor,
            reviewEmail,
            reviewTitle,
            reviewBody
        )
    }

    fun sizeChart(
        shop: String,
        source: String,
        product_id: String,
        tags: String,
        vendor: String
    ): String {
        return apiCallInterface.getSizeChart(shop, source, product_id, tags, vendor)
    }

    fun judgemeReviewCount(
        product_id: String,
        apiToken: String,
        shopDomain: String
    ): Single<JsonElement> {
        return apiCallInterface.getJudgemeReviewCount(apiToken, shopDomain, product_id)
    }

    fun judgemeReviewIndex(
        apiToken: String,
        shopDomain: String,
        per_page: Int,
        page: Int,
        product_id: String
    ): Single<JsonElement> {
        return apiCallInterface.getJudgemeIndex(apiToken, shopDomain, per_page, page, product_id)
    }

    fun judgemeReviewCreate(params: JsonObject): Single<JsonElement> {
        return apiCallInterface.createJudgemeReview(params)
    }

    fun judgemeProductID(
        url: String,
        handle: String,
        apiToken: String,
        shopDomain: String
    ): Single<JsonElement> {
        return apiCallInterface.getJudgemeProductID(url, apiToken, shopDomain, handle)
    }

    fun AliReviewInstallStatus(url: String): Single<JsonElement> {
        return apiCallInterface.getAlireviewStatus(url)
    }
    fun chatGPT(description: String): Single<JsonElement> {
        var body=GPTBODY()
        body.model=Urls.model
        body.prompt=description
        body.temperature=Urls.temperature
        body.max_tokens=Urls.max_tokens
        body.top_p=Urls.top_p
        body.frequency_penalty=Urls.frequency_penalty
        body.presence_penalty=Urls.presence_penalty
        return apiCallInterface.getChatGPT(Urls.authtoken, body)
    }
    fun getAliProductReview(url:String,
        shop_id: String,
        product_id: String,
        currentPage: Int
    ): Single<JsonElement> {
        return apiCallInterface.getAliProductReview(url,shop_id, product_id, currentPage)
    }

    fun getRewards(
        x_guid: String,
        x_api_key: String,
        customer_email: String,
        customer_id: String
    ): Single<JsonElement> {
        return apiCallInterface.getrewards(x_guid, x_api_key/*, customer_email, customer_id*/)
    }

    fun redeemPoints(
        x_guid: String,
        x_api_key: String,
        customer_external_id: String,
        customer_email: String,
        redemption_option_id: String
    ): Single<JsonElement> {
        return apiCallInterface.redeemPoints(
            x_guid,
            x_api_key,
            customer_external_id,
            customer_email,
            redemption_option_id
        )
    }

    fun earnRewards(x_guid: String, x_api_key: String): Single<JsonElement> {
        return apiCallInterface.earnRewards(x_guid, x_api_key)
    }

    fun earnBirthRewards(x_guid: String, x_api_key: String,email: String,day: String,month: String,year: String): Single<JsonElement> {
        return apiCallInterface.redeemBirthPoints(x_guid, x_api_key,email,day,month,year)
    }

    fun myrewards(
        x_guid: String,
        x_api_key: String,
        customer_email: String,
        customer_id: String
    ): Single<JsonElement> {
        return apiCallInterface.myrewards(
            x_guid,
            x_api_key,
            customer_email,
            customer_id,
            true,
            true
        )
    }

    fun referfriend(
        x_guid: String,
        x_api_key: String,
        customer_id: String,
        emails: String
    ): Single<JsonElement> {
        return apiCallInterface.referfriend(x_guid, x_api_key, customer_id, emails)
    }

    fun validateDelivery(jsonObject: HashMap<String, String>): Single<JsonObject> {
        return apiCallInterface.validateDelivery(jsonObject)
    }

    fun localDelivery(jsonObject: HashMap<String, String>): Single<JsonObject> {
        return apiCallInterface.localDelivery(jsonObject)
    }

    fun yotpoauthentiate(
        client_id: String,
        client_secret: String,
        grant_type: String
    ): Single<JsonElement> {
        return apiCallInterface.yotpoauthentiate(client_id, client_secret, grant_type)
    }

    fun yotpocretereview(
        appkey: String,
        sku: String,
        product_title: String,
        product_url: String,
        display_name: String,
        email: String,
        review_content: String,
        review_title: String,
        review_score: String
    ): Single<JsonElement> {
        return apiCallInterface.yotpocretereview(
            appkey,
            sku,
            product_title,
            product_url,
            display_name,
            email,
            review_content,
            review_title,
            review_score
        )
    }

    fun discountcodeapply(mid: String, customer_code: String): Single<JsonElement> {
        return apiCallInterface.discountcodeapply(mid, customer_code)
    }

    fun notificationcentre(key: String, mid: String, page: Int): Single<JsonElement> {
        return apiCallInterface.notificationcentre(key, mid, page)
    }


    fun localDeliveryy(jsonObject: HashMap<String, String>): Single<JsonObject> {
        return apiCallInterface.localDeliveryy(jsonObject)
    }



    fun DeliveryStatus(mid: String): Single<JsonObject> {
        return apiCallInterface.DeliveryStatus(mid)
    }

    fun storeDelivery(jsonObject: HashMap<String, String>): Single<JsonObject> {
        return apiCallInterface.localDelivery(jsonObject)
    }

    fun menuCollection(mid: String, tags: String): Single<JsonElement> {
        return apiCallInterface.getMenuCollection(mid, tags)

    }

    fun getCcollectionProductsbyTags(
        mid: String,
        handle: String,
        sort: String,
        page: String,
        tags: String
    ): Single<JsonElement> {
        return apiCallInterface.getCollectionProductsbyTags(mid, handle, sort, page, tags)

    }

    fun getUserLogin(mid: String, email: String,firstname:String,lastname:String): Single<JsonElement> {
        return apiCallInterface.getuserLogin(mid, email,firstname,lastname)
    }

    fun getFirstOrder(mid: String,customer_id: String): Single<JsonElement> {
        return apiCallInterface.getPlan(mid,customer_id)
    }

    fun deleteSingleVariantData(data: WishItemData) {
        appdatabase.wishitemDataDao().delete(data)
    }

    fun getSingleVariantData(id: String): WishItemData {
        return appdatabase.wishitemDataDao().getSingleVariantData(id)
    }

    fun insertWishListVariantData(data: WishItemData) {
        appdatabase.wishitemDataDao().insert(data)
    }

    val wishListVariantData: List<WishItemData>
        get() = appdatabase.wishitemDataDao().all
    val wishListDataCount: LiveData<List<WishItemData>>
        get() = appdatabase.wishitemDataDao().wish_count

     fun WholeSalePriceData(authorization: String, apikey:String, params: JsonObject): Single<JsonElement> {
        return apiCallInterface.Wholesalepricedata(authorization,apikey,params)
    }
    fun getWholessaleDiscountCoupon(mid:String,coupon:String,type:String,Discount_Price:String): Single<JsonElement> {
        return apiCallInterface.GetWholeSaleDiscountCoupon(mid,coupon,type,Discount_Price)
    }
    fun deleteUserData(mid: String,customer_Id: String): Single<JsonElement> {
        return apiCallInterface.deleteUserData(mid,customer_Id)
    }
    fun insertHomePageProduct(data:HomePageProduct){
        appdatabase.HomePageProductDao().insert(data)
    }
    fun updateHomePageProduct(data:HomePageProduct){
        appdatabase.HomePageProductDao().update(data)
    }
    fun deleteHomePageProduct(data:HomePageProduct){
        appdatabase.HomePageProductDao().delete(data)
    }
    fun deleteAllHomePageProduct(){
        appdatabase.HomePageProductDao().deleteall()
    }
    fun getHomePageProduct(id: String): List<HomePageProduct> {
        return appdatabase.HomePageProductDao().getProduct(id)
    }
    fun getHomePageProductByCatID(id: String,unique_id: String): List<HomePageProduct> {
        return appdatabase.HomePageProductDao().getProductsByCatId(id,unique_id)
    }
    fun getHomePageProductByCatID_Product(id: String,unique_id: String,product_id: String): List<HomePageProduct> {
        return appdatabase.HomePageProductDao().getProductsByCatId_product(id,unique_id,product_id)
    }
    fun deleteCategoryProducts(id: String,unique_id: String){
         appdatabase.HomePageProductDao().deleteCategoryProducts(id,unique_id)
    }
    fun isHomePageProductsCached():Boolean{
        if(appdatabase.HomePageProductDao().getProducts().size>0){
            return true
        }else{
            return false
        }
    }

    fun getProductListSliders(list: List<Storefront.ProductEdge>?): Observable<Storefront.ProductEdge> {
        return Observable.fromIterable(list)
    }
}
