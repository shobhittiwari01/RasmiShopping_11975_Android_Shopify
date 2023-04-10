package com.rasmishopping.app.notificationsection

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.rasmishopping.app.basesection.activities.Splash
import com.rasmishopping.app.utils.Constant

class FirebaseInstanceIDService : FirebaseMessagingService() {
    override fun onNewToken(refreshedToken: String) {
        super.onNewToken(refreshedToken)
        Log.d("NEW_TOKEN", refreshedToken)

    }
}
