package com.rasmishopping.app.basesection.activities
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.adapters.NotificationListAdapter
import com.rasmishopping.app.basesection.models.Notification
import com.rasmishopping.app.basesection.viewmodels.LeftMenuViewModel
import com.rasmishopping.app.databinding.MNotificationBinding
import com.rasmishopping.app.utils.Urls
import com.rasmishopping.app.utils.ViewModelFactory
import kotlinx.coroutines.*
import javax.inject.Inject
class NotificationActivity : NewBaseActivity() {
    @Inject
    lateinit var factory: ViewModelFactory
    private var model: LeftMenuViewModel? = null
    private var binding: MNotificationBinding? = null
    @Inject
    lateinit var notificationlistAdapter: NotificationListAdapter
    var thread: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_notification, group, true)
        showBackButton()
        hidethemeselector()
        showTittle(resources.getString(R.string.notification))
        (application as MyApplication).mageNativeAppComponent!!.doNotificationInjection(this)
        model = ViewModelProvider(this, factory).get(LeftMenuViewModel::class.java)
        model!!.context = this
        shimmerStartGridCart()
        model?.NotificationResponse(Urls(application as MyApplication).mid)?.observe(this@NotificationActivity, Observer { this.showData(it) })
        model?.message!!.observe(this@NotificationActivity, Observer { this.showError(it) })
    }

    private fun showError(it: String?) {
        try {
            Toast.makeText(this,it,Toast.LENGTH_LONG).show()
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun showData(data: ArrayList<Notification>) {
        if(data.size>0 ){
            when(binding!!.notificationrecycler.childCount){
                0->{
                    shimmerStopGridCart()
                    binding!!.nonotificationsection.visibility = View.GONE
                    binding!!.notificationrecycler.visibility = View.VISIBLE
                    notificationlistAdapter = NotificationListAdapter()
                    notificationlistAdapter.setData(this, data)
                    binding!!.notificationrecycler.adapter = notificationlistAdapter
                }
                else->{
                    notificationlistAdapter.discarr.addAll(data)
                    notificationlistAdapter.notifyDataSetChanged()
                }
            }
        }else{
            when(binding!!.notificationrecycler.childCount){
                0->{
                    shimmerStopGridCart()
                    binding!!.nonotificationsection.visibility = View.VISIBLE
                    binding!!.notificationrecycler.visibility = View.GONE
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_searchandcarts, menu)
        return true
    }
}