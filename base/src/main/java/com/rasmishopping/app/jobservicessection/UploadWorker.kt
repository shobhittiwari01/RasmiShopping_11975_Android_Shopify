package com.rasmishopping.app.jobservicessection

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.cartsection.activities.CartList
import com.rasmishopping.app.cartsection.activities.SubscribeCartList
import com.rasmishopping.app.notificationsection.NotificationUtils
import com.rasmishopping.app.repositories.Repository
import javax.inject.Inject

class UploadWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    @Inject
    lateinit var repository: Repository
    override fun doWork(): Result {

        // Do the work here--in this case, upload the images.
        checkNotification()

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    fun checkNotification() {
        (applicationContext as MyApplication).mageNativeAppComponent!!.doWorkerInjection(this)
        val runnable = Runnable {
            if (repository.allCartItems.size > 0) {
                if (isAppIsInBackground(applicationContext)) {
                    showCartNotification()
                }
            } else {
                Log.i("MageNative", "No Cart")
            }
        }
        Thread(runnable).start()
    }

    private fun showCartNotification() {
        try {
            var tittle = ""
            if (repository.isLogin) {
                tittle =
                    applicationContext.resources.getString(R.string.heyuser) + " " + repository.allUserData[0].firstname + " " + repository.allUserData[0].lastname
            } else {
                tittle = applicationContext.resources.getString(R.string.app_name)
            }
            val intent = Intent(applicationContext, SubscribeCartList::class.java)
            val notificationUtils = NotificationUtils(applicationContext)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            notificationUtils.showNotificationMessage(
                tittle,
                applicationContext.resources.getString(R.string.somethingleftinyourcart),
                intent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses = am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo!!.packageName == context.packageName) {
                isInBackground = false
            }
        }
        return isInBackground
    }
}