package com.rasmishopping.app.basesection.activities
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.Menu
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import java.util.ArrayList
import java.util.HashMap

class InternetActivity : NewBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_internet_connection)
        internet=false
        onBackPressedDispatcher.addCallback(
            this /* lifecycle owner */,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    internet=true
                    finishAffinity();
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }
}