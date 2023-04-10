package com.rasmishopping.app.sharedprefsection

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.shopify.buy3.Storefront
import org.json.JSONObject

@SuppressLint("StaticFieldLeak")
object MagePrefs {
    private var context: Context? = null
    private var sharedPreference: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private val HOMEDATA = "home_data"
    private val LANGUAGE = "language"
    private val AVAILABLELANGUAGE = "availablelanguage"
    private val DEFAULTLANGUAGE = "defaultlanguage"
    private val PREF_NAME = "MagenativeShopify"
    private val HOME_PRODUCTS = "home_products"
    private val CART_AMOUNT = "cart_amount"
    private const val CARTDATA = "cartdata"
    private val APPCURRENCY = "currency"
    private val SYMBOL = "symbol"
    private const val CUSTOMERID = "customerId"
    private const val ACCESSTOKEN = "accesstoken"
    private const val CUSTOMEREMAIL = "customerEmail"
    private const val MAINTENANCE = "maintenancemode"
    private const val FIRSTNAME = "firstname"
    private const val LASTNAME = "lastname"
    private const val TAGSLIST = "tagslist"
    private const val COUPONCODE = "coupon_code"
    private const val PLANNAME = "plan_name"

    fun getInstance(context: Context) {
        this.context = context
        sharedPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = sharedPreference?.edit()
    }




    fun setAvailableLanguage(language: ArrayList<String>) {
        editor?.putString(AVAILABLELANGUAGE, language.toString())
        editor?.commit()
    }
    fun getAvailableLanguage(): String? {
        return sharedPreference?.getString(AVAILABLELANGUAGE, null)
    }
    fun savenotification(language: String) {
        editor?.putString("NOTI", language)
        editor?.commit()
    }
    fun getnoti(): String? {
        return sharedPreference?.getString("NOTI", null)
    }
    fun setLanguage(language: String) {
        editor?.putString(LANGUAGE, language)
        editor?.commit()
    }
    fun getLanguage(): String? {
        var data = sharedPreference?.getString(LANGUAGE, null)
        if (data==null){
            return data
        }else{
            return data.split("#").get(0)
        }
    }
    fun setDefaultLanguage(defaultlanguage: String) {
        editor?.putString(DEFAULTLANGUAGE, defaultlanguage)
        editor?.commit()
    }
    fun getDefaultLanguage(): String? {
        return sharedPreference?.getString(LANGUAGE, null)
    }

    fun setGrandTotal(grand_total: String) {
        editor?.putString(CART_AMOUNT, grand_total)
        editor?.commit()
    }

    fun getGrandTotal(): String? {
        return sharedPreference?.getString(CART_AMOUNT, null)
    }

    fun setCurrency(currency: String) {
        editor?.putString(APPCURRENCY, currency)
        editor?.commit()
    }

    fun getCurrency(): String? {
        return sharedPreference?.getString(APPCURRENCY, null)
    }
    fun setSymbol(symbol: String) {
        editor?.putString(SYMBOL, symbol)
        editor?.commit()
    }

    fun getSymbol(): String? {
        return sharedPreference?.getString(SYMBOL, null)
    }
    fun clearHomeData() {
        editor?.remove(HOMEDATA)
        editor?.commit()
    }
    fun saveCartdata(cartdata: String) {
        editor?.putString(CARTDATA, cartdata)
        editor?.commit()
    }
    fun saveRecent(recent:String){
        var jsonArray= getRecent()
        if(jsonArray.length()==7){
            jsonArray.put(recent,recent)
            jsonArray.remove(jsonArray.names()!!.getString(0))
        }else{
            jsonArray.put(recent,recent)
        }
        editor?.putString("RECENT", jsonArray.toString())
        editor?.commit()
        Log.i("SaifRecentSize",""+jsonArray.length())
    }
    fun getRecent():JSONObject{
        if(sharedPreference?.getString("RECENT", null)!=null){
            return JSONObject(sharedPreference?.getString("RECENT", null))
        }else{
            return JSONObject()
        }
    }
    fun clearRecent(){
        editor?.remove("RECENT")
        editor?.commit()
    }
    fun getcartdata(): String? {
        return sharedPreference?.getString(CARTDATA, null)
    }

    fun setCustomerId(customerID: String) {
        editor?.putString(CUSTOMERID, customerID)
        editor?.commit()
    }

    fun getCustomerID(): String? {

        return sharedPreference?.getString(CUSTOMERID, null)
    }

    fun setCustomerFirstName(firstname: String) {
        editor?.putString(FIRSTNAME, firstname)
        editor?.commit()
    }

    fun getCustomerFirstName(): String? {
        return sharedPreference?.getString(FIRSTNAME, null)
    }

    fun setCustomerLastName(lastname: String) {
        editor?.putString(LASTNAME, lastname)
        editor?.commit()
    }

    fun getCustomerTagslist(): String? {
        return sharedPreference?.getString(TAGSLIST, null)
    }
    fun setCustomerTagslist(tagslist: String) {
        editor?.putString(TAGSLIST, tagslist)
        editor?.commit()
    }

    fun getCustomerLastName(): String? {
        return sharedPreference?.getString(LASTNAME, null)
    }



    fun setCustomerEmail(customerEmail: String) {
        editor?.putString(CUSTOMEREMAIL, customerEmail)
        editor?.commit()
    }

    fun getaccessToken(): String? {
        return sharedPreference?.getString(ACCESSTOKEN, null)
    }

    fun saveaccessToken(accesstoken: String) {
        editor?.putString(ACCESSTOKEN, accesstoken)
        editor?.commit()
    }

    fun getCustomerEmail(): String? {
        return sharedPreference?.getString(CUSTOMEREMAIL, null)
    }

    fun clearUserData() {
        editor?.remove(CUSTOMERID)
        editor?.remove(CUSTOMEREMAIL)
        editor?.commit()
    }

    fun setMaintenanceMode(maintenance: Boolean) {
        editor?.putBoolean(MAINTENANCE, maintenance)
        editor?.commit()
    }

    fun getMaintenanceMode(): Boolean? {
        return sharedPreference?.getBoolean(MAINTENANCE, false)
    }

    fun setCouponCode(couponCode: String) {
        editor?.putString(COUPONCODE, couponCode)
        editor?.commit()
    }

    fun getCouponCode(): String? {
        return sharedPreference?.getString(COUPONCODE, null)
    }

    fun clearCouponCode() {
        editor?.remove(COUPONCODE)
        editor?.commit()
    }

    fun setPlanName(planName: String) {
        editor?.putString(PLANNAME, planName)
        editor?.commit()
    }

    fun getPlanName(): String? {
        return sharedPreference?.getString(PLANNAME, null)
    }

    fun saveCountryCode(countrycode: Storefront.CountryCode) {
        editor?.putString("COUNTRYCODE", countrycode.toString())
        editor?.commit()
    }

    fun getCountryCode(): String? {
        return sharedPreference?.getString("COUNTRYCODE", null)
    }
    fun clearCountry(){
        editor?.remove("COUNTRYCODE")
        editor?.remove(APPCURRENCY)
        editor?.remove(LANGUAGE)
        editor?.commit()
    }
    fun saveHandle(handle: String?) {
        editor?.putString("HANDLE", handle)
        editor?.commit()
    }

    fun getHandle(): String? {
        return sharedPreference?.getString("HANDLE", null)
    }

    fun saveFilterType(filtertype: String) {
        editor?.putString("FILTERTYPE", filtertype)
        editor?.commit()
    }

    fun getFilterType(): String? {
        return sharedPreference?.getString("FILTERTYPE", null)
    }


    fun clearavailablity() {
        editor?.remove("instock")
        editor?.remove("outstock")
        editor?.commit()
    }

    fun saveMinvalue(minvalue: String) {
        editor?.putString("MINVALUE", minvalue)
        editor?.commit()
    }

    fun getminvalue(): String? {
        return sharedPreference?.getString("MINVALUE", null)
    }

    fun saveMaxvalue(maxvalue: String) {
        editor?.putString("MAXVALUE", maxvalue)
        editor?.commit()
    }

    fun getmaxvalue(): String? {
        return sharedPreference?.getString("MAXVALUE", null)
    }

    fun color(): String? {
        return sharedPreference?.getString("color", null)
    }
    fun colorSave(colorSave :String) {
        editor?.putString("color", colorSave)
        editor?.commit()
    }

    fun colorLable(): String? {
        return sharedPreference?.getString("colorLable", null)
    }
    fun colorLableSave(colorLableSave :String) {
        editor?.putString("colorLable", colorLableSave)
        editor?.commit()
    }
    fun sizeLable(): String? {
        return sharedPreference?.getString("sizeLable", null)
    }
    fun sizeLableSave(sizeLableSave :String) {
        editor?.putString("sizeLable", sizeLableSave)
        editor?.commit()
    }
    fun size(): String? {
        return sharedPreference?.getString("size", null)
    }
    fun rating(): String? {
        return sharedPreference?.getString("rating", null)
    }
    fun sizeSave(sizeSave :String) {
        editor?.putString("size", sizeSave)
        editor?.commit()
    }

    fun brandsValue(): String? {
        return sharedPreference?.getString("brandsValue", null)
    }
    fun brandsValueSave(brandsValueSave :String) {
        editor?.putString("brandsValue", brandsValueSave)
        editor?.commit()
    }

    fun productTypeValue(): String? {
        return sharedPreference?.getString("productType", null)
    }
    fun productTypeValueSave(productTypeValueSave :String) {
        editor?.putString("productType", productTypeValueSave)
        editor?.commit()
    }


    fun clearPriceFilter() {
        editor?.remove("MINVALUE")
        editor?.remove("MAXVALUE")
        editor?.commit()
    }

    fun clearSizeFilter() {
        editor?.remove("size")
        editor?.remove("sizeLable")
        editor?.commit()
    }

    fun clearRatingFilter() {
        editor?.remove("rating")
        editor?.remove("ratingLable")
        editor?.commit()
    }
    fun clearColorFilter() {
        editor?.remove("color")
        editor?.remove("colorLable")
        editor?.commit()
    }
    fun clearBrandFilter() {
        editor?.remove("brandsValue")
        editor?.commit()
    }
    fun clearproductTypeSave() {
        editor?.remove("productType")
        editor?.commit()
    }

    fun saveDefaultCountryCode(countryCode: Storefront.CountryCode) {
        editor?.putString("DEFAULTCOUNTRYCODE", countryCode.toString())
        editor?.commit()
    }

    fun getDefaultCountryCode(): String? {
        return sharedPreference?.getString("DEFAULTCOUNTRYCODE", null)
    }

    fun saveSocialKey(sociallogin: String) {
        editor?.putString("SOCIALLOGIN", sociallogin)
        editor?.commit()
    }

    fun getSocialKey(): String? {
        return sharedPreference?.getString("SOCIALLOGIN", null)
    }

    fun clearSocialKey() {
        editor?.remove("SOCIALLOGIN")
        editor?.commit()
    }
    fun saveHomePageData(json:String){
        editor?.putString("HomePageData",json)
        editor?.commit()
    }
    fun getHomePageData():String?{
        return sharedPreference?.getString("HomePageData",null)
    }
    fun clearHomePageData(){
        editor?.remove("HomePageData")
        editor?.commit()
    }

    fun setDarkTheme(isDark: String) {
        editor?.putString("darkmode", isDark)
        editor?.commit()
    }

    fun getDarkTheme(): String? {
        return sharedPreference?.getString("darkmode",null)
    }
    fun removeDarkModeSetting(){
        editor?.remove("darkmode")
        editor?.commit()
    }

    fun saveDemoJson(demojson: String) {
        editor?.putString("DemoJSON",demojson)
        editor?.commit()
    }

    fun getDemoJson():String? {
        return sharedPreference?.getString("DemoJSON",null)
    }

    fun saveTheme(theme: String) {
        editor?.putString("THEME",theme)
        editor?.commit()
    }

    fun getTheme():String? {
        return sharedPreference?.getString("THEME",null)
    }

    fun clearTheme() {
        editor?.remove("THEME")
        editor?.commit()
    }

    fun saveThemeColor(themeColor: String) {
        editor?.putString("THEMECOLOR",themeColor)
        editor?.commit()
    }

    fun getThemeColor():String? {
        return sharedPreference?.getString("THEMECOLOR",null)
    }

    fun saveImageState(tag: String) {
        editor?.putString("IMAGESTATE",tag)
        editor?.commit()
    }

    fun getImageState():String? {
        return sharedPreference?.getString("IMAGESTATE",null)
    }

    fun saveImagePosition(position: String) {
        editor?.putString("POSITION",position)
        editor?.commit()
    }
    fun getImagePosition():String? {
        return sharedPreference?.getString("POSITION",null)
    }

    fun clearImageState() {
        editor?.remove("IMAGESTATE")
        editor?.commit()
    }

    fun clearImagePosition() {
        editor?.remove("POSITION")
        editor?.commit()
    }
}