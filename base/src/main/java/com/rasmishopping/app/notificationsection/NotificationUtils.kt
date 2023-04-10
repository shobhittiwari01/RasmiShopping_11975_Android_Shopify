package com.rasmishopping.app.notificationsection
import android.app.*
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import java.util.*
class NotificationUtils {
    private val mContext: Context
    constructor(mContext: Context) {
        this.mContext = mContext
    }
    @JvmOverloads
    fun showNotificationMessage(title: String, message: String, intent: Intent) {
        try
        {
            if (TextUtils.isEmpty(message))
                return
            val icon = R.mipmap.ic_launcher
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val resultPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            var mBuilder: NotificationCompat.Builder? =null
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
                mBuilder= NotificationCompat.Builder(mContext,"MageNative")
            }else{
                mBuilder== NotificationCompat.Builder(mContext)
            }
            val alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.packageName + "/raw/notification")
            showSmallNotification(mBuilder!!, icon, title, message, resultPendingIntent, alarmSound)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    @JvmOverloads
    fun showNotificationMessageWithImage(
        title: String,
        message: String,
        intent: Intent,
        imageUrl: String
    ) {
        try
        {
            if (TextUtils.isEmpty(message))
                return
            val icon = R.mipmap.ic_launcher
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            val resultPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            }
            var mBuilder: NotificationCompat.Builder? =null
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
                mBuilder= NotificationCompat.Builder(mContext,"MageNative")
            }else{
                mBuilder== NotificationCompat.Builder(mContext)
            }
            val alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.packageName + "/raw/notification")
            showBigNotification(imageUrl,mBuilder!!, icon, title, message, resultPendingIntent, alarmSound)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private fun showSmallNotification(
        mBuilder: NotificationCompat.Builder,
        icon: Int,
        title: String,
        message: String,
        resultPendingIntent: PendingIntent,
        alarmSound: Uri
    ) {
        try {
            mBuilder.setSmallIcon(icon)
                .setColor(Color.parseColor(NewBaseActivity.themeColor))
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setContentIntent(resultPendingIntent)
            val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "MageNative"
                val descriptionText = "Notification Services"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel("MageNative", name, importance).apply {
                    description = descriptionText
                }
                notificationManager.createNotificationChannel(channel)
            }
            val random = Random()
            val m = random.nextInt(9998 - 1000) + 1000
            notificationManager.notify(m, mBuilder.build())
        } catch (e: Exception) {
            Log.i("exception", e.toString())
        }

    }
    private fun showBigNotification(
        bitmap: String,
        mBuilder: NotificationCompat.Builder,
        icon: Int,
        title: String,
        message: String,
        resultPendingIntent: PendingIntent,
        alarmSound: Uri
    ) {
        try {
            Glide.with(mContext)
                .asBitmap()
                .load(bitmap)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        mBuilder.setSmallIcon(icon)
                            .setColor(Color.parseColor(NewBaseActivity.themeColor))
                            .setLargeIcon(resource)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(resource))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                            .setSound(alarmSound)
                            .setContentIntent(resultPendingIntent)
                        val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val name = "MageNative"
                            val descriptionText = "Notification Services"
                            val importance = NotificationManager.IMPORTANCE_DEFAULT
                            val channel = NotificationChannel("MageNative", name, importance).apply {
                                description = descriptionText
                            }
                            notificationManager.createNotificationChannel(channel)
                        }
                        val random = Random()
                        val m = random.nextInt(9998 - 1000) + 1000
                        notificationManager.notify(m, mBuilder.build())
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
        } catch (e: Exception) {
            Log.i("exception", e.toString())
        }
    }
}