package com.rasmishopping.app.utils

import android.util.Log
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.MyApplication.Companion.context
import com.rasmishopping.app.R
import com.rasmishopping.app.repositories.Repository
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.inject.Inject

class Urls {
    @Inject
    lateinit var repository: Repository
    var app: MyApplication

    constructor(app: MyApplication) {
        this.app = app
        app.mageNativeAppComponent!!.doURlInjection(this)
    }

    companion object Data {
        const val BASE_URL: String =
            "https://shopifymobileapp.cedcommerce.com/"//put your base url here
        const val PERSONALISED: String =
            "https://recommendations.loopclub.io/api/v1/"//put your base url here
        const val MENU: String =
            "shop-mobile/shopifyapi/getnewcategorymenus"//put your end point here
        const val SETORDER: String = "shopifymobile/shopifyapi/setorder"
        const val ORDERTAGS: String = "shop-mobile/shopifyapi/ordertags"
        const val SETDEVICES: String = "shopifymobile/shopifyapi/setdevices"
        const val RECOMMENDATION: String = "recommendations/"
        const val HEADER: String = "Domain-Name: douban"
        const val HOMEPAGE: String = "shopifymobile/shopifyapi/homepagedata"
        const val CLIENT: String = "magenative"
        const val TOKEN: String = "a2ds21R!3rT#R@R23r@#3f3ef"
        var REWARDIFYCLIENTID: String = "490_6b4mnyhzm2o0wkkg084ccg8wgc800o0w84s4cwc0kk0gw8cg8k"
        var REWARDIFYCLIENTSECRET: String = "4flwkzielyucccgwww44o8gc0gscks404408kcg80oks48kogg"
        const val MulipassSecret: String = "1f4237c87f31090e5763feaa34962b72"
        const val SIZECHART: String = "https://app.kiwisizing.com/size"
        const val JUDGEME_BASEURL: String = "https://judge.me/api/v1/"
        const val JUDGEME_REVIEWCOUNT: String = JUDGEME_BASEURL + "reviews/count/"
        const val JUDGEME_REVIEWINDEX: String = JUDGEME_BASEURL + "reviews"
        const val JUDGEME_REVIEWCREATE: String = JUDGEME_BASEURL + "reviews"
        const val JUDGEME_GETPRODUCTID: String = JUDGEME_BASEURL + "products/"
        var JUDGEME_APITOKEN: String = "R8kqByFI_qHiHHQj6ZV1yWCYveQ"

        const val YOTPOBASE_URL = "https://loyalty.yotpo.com/api/v2/"
        const val GETREWARDS = YOTPOBASE_URL + "redemption_options"
        const val REDEEMPOINTS = YOTPOBASE_URL + "redemptions"
        const val EARNREWARD = YOTPOBASE_URL + "campaigns"
        const val MYREWARDS = YOTPOBASE_URL + "customers"
        const val BIRTHREWARDS = YOTPOBASE_URL + "customer_birthdays"
        const val SENDREFERRAL = YOTPOBASE_URL + "referral/share"
        const val VALIDATE_DELIVERY: String =
            "shopifymobile/zapietstorepickupapi/validatedeliverynpickup"
        const val LOCAL_DELIVERY: String = "shopifymobile/zapietstorepickupapi/getdeliverynpickup"
        const val LOCAL_DELIVERYY: String =
            "shopifymobilenew/zapietstorepickupapi/getdeliverynpickup"
        const val DeliveryStatus: String =
            "https://shopifymobileapp.cedcommerce.com/shopifymobile/zapietstorepickupapi/installedstatus?"
        var XGUID = "oyeoRDurwhul3WK-zN5ScA"
        var X_API_KEY = "FCCVWdq07tgQCkq8Bw8ctQtt"
        var fbusername = "MageNative"
        var whatsappnumber = "+916393417500"
        var Zendesk_KEY = "+916393417500"
        const val MENUCOLLECTION: String =
            "http://shopifymobileapp.cedcommerce.com/shopifymobile/shopifyapi/getcollectionproperties"
        const val FILTERTAGPRO: String =
            "https://shopifymobileapp.cedcommerce.com/shopifymobile/shopifyapi/getcollectionproductsbytags"

        const val deleteUrls: String =
            "https://shopifymobileapp.cedcommerce.com/shopifymobile/shopifyapi/deletecustomer"

        /************************** Yotpo Rewards Integration ***************************/

        const val YOTPOAUTHENTICATE = "https://api.yotpo.com/oauth/token"
        const val YOTPOCREATEREVIEW = "https://api.yotpo.com/v1/widget/reviews"
        const val WHOLESALEPRICEURL = "https://api.wholesalehelper.io/api/v1/prices"
        const val WHOLESALEDISCOUNTCOUPONURL = "https://shopmobileapp.cedcommerce.com/shopifymobile/shopifycoupon/shopifycouponcode"

        /********************************************************************************/
        /********************************** DICOUNTCODE *********************************/

        const val DISCOUNTCODEAPPLY: String =
            "https://shopifymobileapp.cedcommerce.com/shopifymobilenew/discountpaneldataapi/getdiscountcodes/"

        /********************************************************************************/
        /*********************ISNtagram*******************/
        var InstaRange=6
        var InstaTittle="Instagram"
        var InstaView="grid"
        var Instatokenurl="https://mobileappbuilder.magenative.com/shopifymobilenew/instagramfeedsapi/getinstafeeds?mid="
        var devInstatokenurl="https://dev-mobileappnew.cedcommerce.com/shopifymobilenew/instagramfeedsapi/getinstafeeds?mid="
            /*********************ISNtagram*******************/
        /*********************LiveSAle*******************/
        var LiveSalePublicKey="vBFfEYzWxwh62DMa"
        /*********************LiveSAle*******************/

        /*********************Notification centre*******************/
        const val NOTIFICATIONCENTRE: String =
            "https://shopmobileapp.cedcommerce.com/shopifymobilenew/paneldataapi/notificationrecords"
        /************************************************************/

        /////////////algolia keys/////
        var AppID = "XEPLJ4LUDO"
        var ApiKey = "3ba73174525e37c13d94e9fced1f4125"
        var IndexName = "shopify_products"
        ////////////////////////
        /////////returnprime////
        var Channel_id="97860"
        ////////////
        ////////////flitsapp key//////
        var user_id="24752"
        var token="268c7946247c11a462970ec8ee0b195b"
        var X_Integration_App_Name:String?= context.resources.getString(R.string.app_name)
        var Authorization="Bearer 967a3937-e75f-494c-9855-ead304e78e15|tNzHDTXifaGvTWqcYoOxWVG7i4sZn5scb2i8RgK6"
        var wholsesaleapikey="6289587803094e6a02418e58ddcc861d"
        ///////////////////////////
        ////// First Sale /////
        var firstsalecoupon="Extra55"
        ///////////////////////////
        /////////// FireBase Key Preview//////
        var FirebaseProjectId_preview="shopify-dev-project-2f51e"
        var FirebaseApplicationId_preview="1:445702503308:android:f8dee0b320adfdaa68b4a9"
        var FirebaseApiKey_preview="AIzaSyD0GhHgrwqVQC7m3LBOkoxVzVefP6EQAZw"
        var FirebaseDatabaseUrl_preview="https://shopify-dev-project-2f51e.firebaseio.com/"
        /////////// FireBase Key Preview//////
        /////////// FireBase Key Live//////
        var FirebaseProjectId_live="live-shopify-project"
        var FirebaseApplicationId_live="1:322600045606:android:ccd0e8d87b47235fab6ae7"
        var FirebaseApiKey_live="AIzaSyC1LTEUGgrKWBDVRV0VMQJOCN2O-UyVKr4"
        var FirebaseDatabaseUrl_live="https://live-shopify-project.firebaseio.com/"
        /////////// FireBase Key Live//////
        /////////// FireBase Authentication//////
        var FirebaseEmail="sudhanshshah@magenative.com"
        var FirebasePassword="asdcxzasd"
        var FirebaseNewEmail="manoharsinghrawat@magenative.com"
        var FirebaseNewPassword="59Xp47nIt"
        /////////// FireBase Authentication//////
        //////chatgpt////
        const val gpturl="https://api.openai.com/v1/completions"
        var authtoken="Bearer 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"
        var content_gpt="application/json"
        var model="text-davinci-003"
        var temperature=0.7
        var max_tokens=64
        var top_p=1.0
        var frequency_penalty=0.0
        var presence_penalty=0.0
        ///chatgpt////
    }
    val shopdomain: String
        get() {
            var domain ="bayan-electronic.myshopify.com"
            try {
                val executor = Executors.newSingleThreadExecutor()
                val callable = Callable {
                    if (repository.getPreviewData().size > 0) {
                        domain = repository.getPreviewData().get(0).shopurl!!
                    }
                    domain
                }
                val future = executor.submit(callable)
                domain = future.get()
                executor.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.i("MageNative", "SaifDevdomain" + domain)
            return domain
        }

    val mid: String
        get() {
            var domain = "11975"
            try {
                val executor = Executors.newSingleThreadExecutor()
                val callable = Callable {
                    if (repository.getPreviewData().size > 0) {
                        domain = repository.getPreviewData().get(0).mid!!
                    }
                    domain
                }
                val future = executor.submit(callable)
                domain = future.get()
                executor.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.i("MageNative", "SaifDevdmid" + domain)
            return domain
        }
    val apikey: String
        get() {
            var key ="8038b87ff9c3d1049fef4b84e64babff"//"4f575dd7d40ec87298ef97bb0682a2ff"////"" //63893d2330e639632e2eab540e9d2d75
            try {
                val executor = Executors.newSingleThreadExecutor()
                val callable = Callable {
                    if (repository.getPreviewData().size > 0) {
                        key = repository.getPreviewData().get(0).apikey!!
                    }
                    key
                }
                val future = executor.submit(callable)
                key = future.get()
                executor.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.i("MageNative", "SaifDevakikey" + key)
            return key
        }

    /*Please put access token here from magenative panel*/
    val accessToken: String = "31fc1f41196628610bef9a4c73e2949c"
}
