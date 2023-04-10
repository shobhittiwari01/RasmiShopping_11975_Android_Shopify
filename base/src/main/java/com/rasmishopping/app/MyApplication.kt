package com.rasmishopping.app
import android.content.Context
import com.facebook.FacebookSdk
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.*

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rasmishopping.app.dbconnection.dependecyinjection.DaggerMageNativeAppComponent

import com.rasmishopping.app.dbconnection.dependecyinjection.MageNativeAppComponent
import com.rasmishopping.app.dbconnection.dependecyinjection.UtilsModule
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Urls.Data.FirebaseApiKey_live
import com.rasmishopping.app.utils.Urls.Data.FirebaseApiKey_preview
import com.rasmishopping.app.utils.Urls.Data.FirebaseApplicationId_live
import com.rasmishopping.app.utils.Urls.Data.FirebaseApplicationId_preview
import com.rasmishopping.app.utils.Urls.Data.FirebaseDatabaseUrl_live
import com.rasmishopping.app.utils.Urls.Data.FirebaseDatabaseUrl_preview
import com.rasmishopping.app.utils.Urls.Data.FirebaseProjectId_live
import com.rasmishopping.app.utils.Urls.Data.FirebaseProjectId_preview
import net.danlew.android.joda.JodaTimeAndroid
class MyApplication : SplitCompatApplication() {
    var mageNativeAppComponent: MageNativeAppComponent? = null
    override fun onCreate() {
        super.onCreate()
        FacebookSdk.fullyInitialize()
        FacebookSdk.setAutoInitEnabled(true)
        JodaTimeAndroid.init(this)
        MagePrefs.getInstance(this)
        context = this

        mageNativeAppComponent = DaggerMageNativeAppComponent.builder().utilsModule(UtilsModule(this)).build()
        /*val options = FirebaseOptions.Builder()
            .setProjectId(FirebaseProjectId_preview)
            .setApplicationId(FirebaseApplicationId_preview) // Required for Analytics.
            .setApiKey(FirebaseApiKey_preview) // Required for Auth.
            .setDatabaseUrl(FirebaseDatabaseUrl_preview) // Required for RTDB.
            .build()*/
             /*val options = FirebaseOptions.Builder()
            .setProjectId("magenative-dev-server-preview")
            .setApplicationId("1:132710677937:ios:a284473bd9b353b8285190") // Required for Analytics.
            .setApiKey("AIzaSyDpn2RGoAybqWikv25ZvR5fi69oTgtKEr8") // Required for Auth.
            .setDatabaseUrl("https://magenative-dev-server-preview-default-rtdb.firebaseio.com/") // Required for RTDB.
            .build()*/

        val options = FirebaseOptions.Builder()
            .setProjectId(FirebaseProjectId_live)
            .setApplicationId(FirebaseApplicationId_live) // Required for Analytics.
            .setApiKey(FirebaseApiKey_live) // Required for Auth.
            .setDatabaseUrl(FirebaseDatabaseUrl_live) // Required for RTDB.
            .build()
        firebaseapp = FirebaseApp.initializeApp(this /* Context */, options, "MageNative")
       // FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        if(BuildConfig.DEBUG){
            FirebaseCrashlytics.getInstance().setUserId("Development")
            FirebaseCrashlytics.getInstance().log("Development")
        }else{
            FirebaseCrashlytics.getInstance().setUserId("Production")
            FirebaseCrashlytics.getInstance().log("Production")
        }
    }
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
    }
    companion object {

        lateinit var context: MyApplication
        lateinit var firebaseapp: FirebaseApp
        private var mFirebaseSecondanyInstance: FirebaseDatabase? = null
        var flag: Boolean = false
        fun getmFirebaseSecondanyInstance(): FirebaseDatabase {
            if (mFirebaseSecondanyInstance == null) {
                val secondary = FirebaseApp.getInstance("MageNative")
                mFirebaseSecondanyInstance = FirebaseDatabase.getInstance(secondary)
            }
            return mFirebaseSecondanyInstance as FirebaseDatabase
        }

        var dataBaseReference: DatabaseReference? = null

    }


}
