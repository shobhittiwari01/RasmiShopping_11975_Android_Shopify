package com.rasmishopping.app.notificationsection
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.basesection.activities.Splash
import com.rasmishopping.app.utils.Urls
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.util.*
class FirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService() {
    private var notificationUtils: NotificationUtils? = null
    private  var firebaseAnalytics: FirebaseAnalytics
    init {
        firebaseAnalytics = Firebase.analytics
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            val params = Bundle()
            params.putString("notification", "Recieved")
            firebaseAnalytics.logEvent("NotificationRecived",params)
            if(remoteMessage.notification!=null){
                createNotification(remoteMessage.data.toString())
            }else{
                createNotification(remoteMessage.data.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun createNotification(dataload:String){
        Log.i("DeveloperSaif", "" + dataload)
        val `object` = JSONObject(dataload)
        val data = `object`.getJSONObject("data")
        val title = data.getString("title")
        val merchant_id = data.getString("merchant_id")
        val mesg = data.getString("message")
        val link_type = data.getJSONObject("payload").getString("link_type")
        val link_id = data.getJSONObject("payload").getString("link_id")
        var resultIntent: Intent? = null
        when (link_type) {
            "product" -> {
                val product_id = link_id
                resultIntent = Intent(applicationContext, Splash::class.java)
                resultIntent.putExtra("ID", product_id)
                resultIntent.putExtra("type", "product")
            }
            "collection" -> {
                val s1 = link_id
                resultIntent = Intent(applicationContext, Splash::class.java)
                resultIntent.putExtra("ID",(s1))
                resultIntent.putExtra("tittle", title)
                resultIntent.putExtra("type", "collection")
            }
            "web_address","web_link" -> {
                resultIntent = Intent(applicationContext, Splash::class.java)
                resultIntent.putExtra("link", link_id)
                resultIntent.putExtra("name", " ")
                resultIntent.putExtra("type", "weblink")
            }
        }
        if (merchant_id.equals(Urls((application as MyApplication)).mid)) {
            if (data.has("image")) {
                showNotificationMessageWithBigImage(
                    applicationContext,
                    title,
                    mesg,
                    Objects.requireNonNull<Intent>(resultIntent),
                    data.getString("image")
                )
            } else {
                showNotificationMessage(
                    applicationContext,
                    title,
                    mesg,
                    Objects.requireNonNull<Intent>(resultIntent)
                )
            }
        }
    }
    private fun showNotificationMessage(
        context: Context,
        title: String,
        message: String,
        intent: Intent
    ) {
        notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils!!.showNotificationMessage(title, message, intent)
    }

    private fun showNotificationMessageWithBigImage(
        context: Context,
        title: String,
        message: String,
        intent: Intent,
        imageUrl: String
    ) {
        notificationUtils = NotificationUtils(context)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        notificationUtils!!.showNotificationMessageWithImage(title, message, intent, imageUrl)
    }
    companion object {
        private val TAG = "FirebaseMessageService"
    }
}