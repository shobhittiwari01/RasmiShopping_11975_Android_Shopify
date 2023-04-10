package com.rasmishopping.app.maintenence_section
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.Splash
import com.rasmishopping.app.basesection.fragments.LeftMenu
import com.rasmishopping.app.basesection.viewmodels.LeftMenuViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.generated.callback.OnClickListener
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.ViewModelFactory
import kotlinx.android.synthetic.main.activity_maintenence.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class MaintenenceActivity : AppCompatActivity() {
    lateinit var leftmenu: LeftMenuViewModel
    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maintenence)
        (application as MyApplication).mageNativeAppComponent!!.doMaintenanceActivityInjection(this)
        leftmenu = ViewModelProvider(this, viewModelFactory).get(LeftMenuViewModel::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            if(leftmenu.repository.getPreviewData().isNotEmpty()){
                movetodemo.visibility= View.VISIBLE
                movetodemo.setOnClickListener {
                    val runnable = Runnable {
                        leftmenu.deletLocal()
                        leftmenu.deleteData()
                        MagePrefs.clearHomePageData()
                        leftmenu.repository.deletePreviewData()
                        leftmenu.logOut()
                        MagePrefs.clearCountry()
                        SplashViewModel.viewhashmap = HashMap<String, View>()
                        var intent = Intent(this@MaintenenceActivity, Splash::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        Constant.activityTransition(this@MaintenenceActivity)
                    }
                    Thread(runnable).start()
                }
            }
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity();
    }

}