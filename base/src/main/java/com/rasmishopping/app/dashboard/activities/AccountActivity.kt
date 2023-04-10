package com.rasmishopping.app.dashboard.activities

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.shopify.flitsappmodule.FlitsDashboard.HowtoEarnSpent.EarnSpentActivity
import com.shopify.flitsappmodule.FlitsDashboard.StoreCredits.StoreCredits
import com.shopify.rewardifyappmodule.CustomerActivity
import com.rasmishopping.app.FlitsDashboard.StoreCredits.StoreCreditsViewModel
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.addresssection.activities.AddressList
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.fragments.LeftMenu
import com.rasmishopping.app.basesection.viewmodels.LeftMenuViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.dashboard.viewmodels.DashBoardViewModel
import com.rasmishopping.app.databinding.ActivityNewaccountsPageBinding
import com.rasmishopping.app.databinding.DeleteDialogBoxBinding
import com.rasmishopping.app.databinding.PopConfirmationBinding
import com.rasmishopping.app.dbconnection.entities.UserLocalData
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.homesection.viewmodels.HomePageViewModel
import com.rasmishopping.app.ordersection.activities.OrderList
import com.rasmishopping.app.repositories.Repository
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.userprofilesection.activities.UserProfile
import com.rasmishopping.app.utils.*
import com.rasmishopping.app.wishlistsection.activities.WishList
import kotlinx.android.synthetic.main.m_newbaseactivity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

class AccountActivity : NewBaseActivity() {

    var binding: ActivityNewaccountsPageBinding? = null
    var data: UserLocalData? = null
    var page: Intent? = null
    var Response: JSONObject? = null
    lateinit var deleUserDialogLyt: DeleteDialogBoxBinding

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var factory: ViewModelFactory
    private var creditmodel: StoreCreditsViewModel? = null
    private var model: LeftMenuViewModel? = null
    private var dashboardmodel: DashBoardViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.activity_newaccounts_page, group, true)

        binding!!.features = SplashViewModel.featuresModel
        (application as MyApplication).mageNativeAppComponent!!.doAccountPageInjection(this)
        model = ViewModelProvider(this, factory).get(LeftMenuViewModel::class.java)
        model!!.context = this
        creditmodel = ViewModelProvider(this, factory).get(StoreCreditsViewModel::class.java)
        dashboardmodel = ViewModelProvider(this, factory).get(DashBoardViewModel::class.java)
        creditmodel!!.context = this
        showBackButton()
        showTittle(getString(R.string.myaccount))
        hidethemeselector()
        nav_view.visibility=View.VISIBLE
        dashboardmodel!!.getResponse().observe(this, { updateResponse(it) })
        if (SplashViewModel.featuresModel.Enable_flits_App) {
            binding?.myCredits?.visibility = View.VISIBLE
            binding?.manageCredits?.visibility = View.VISIBLE
        } else {
            binding?.myCredits?.visibility = View.GONE
            binding?.manageCredits?.visibility = View.GONE
        }
        if (SplashViewModel.featuresModel.enableRewardify) {
            binding?.rewardifysection?.visibility = View.VISIBLE
        } else {
            binding?.rewardifysection?.visibility = View.GONE
        }
        binding?.mywishlist!!.setOnClickListener {
            val intent = Intent(this, WishList::class.java)
            startActivity(intent)
        }
        binding?.rewardifysection?.setOnClickListener {
            if (model!!.isLoggedIn) {
                val intent = Intent(this, CustomerActivity::class.java)
                intent.putExtra("cid", MagePrefs.getCustomerID())
                intent.putExtra("clientid", Urls.REWARDIFYCLIENTID)
                intent.putExtra("clientsecret", Urls.REWARDIFYCLIENTSECRET)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this,
                    this.resources.getString(R.string.logginfirst),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding?.myprofile?.setOnClickListener {
            if (model!!.isLoggedIn) {
                val myprofile = Intent(this, UserProfile::class.java)
                startActivity(myprofile)
            } else {
                Toast.makeText(
                    this,
                    this.resources.getString(R.string.logginfirst),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding?.mydelete?.setOnClickListener {
            deleUserDialogLyt = DeleteDialogBoxBinding.inflate(LayoutInflater.from(this))
            deleteUser()
        }
        binding?.myCredits?.setOnClickListener {
            val credit = Intent(this, StoreCredits::class.java)
            credit.putExtra("cid", MagePrefs.getCustomerID())
            credit.putExtra("userId", Urls.user_id)
            credit.putExtra("token", Urls.token)
            credit.putExtra("appname", Urls.X_Integration_App_Name)
            credit.putExtra("currencycode", MagePrefs.getCurrency())
            credit.putExtra("themecolor", HomePageViewModel.panel_bg_color)
            credit.putExtra("themetextcolor", HomePageViewModel.icon_color)
            startActivity(credit)
        }
        binding?.manageCredits?.setOnClickListener {
            val credit = Intent(this, EarnSpentActivity::class.java)
            credit.putExtra("cid", MagePrefs.getCustomerID())
            credit.putExtra("userId", Urls.user_id)
            credit.putExtra("token", Urls.token)
            credit.putExtra("appname", Urls.X_Integration_App_Name)
            credit.putExtra("currencycode", MagePrefs.getCurrency())
            credit.putExtra("themecolor", HomePageViewModel.panel_bg_color)
            credit.putExtra("themetextcolor", HomePageViewModel.icon_color)
            startActivity(credit)
        }
        binding?.myaddress?.setOnClickListener {
            if (model!!.isLoggedIn) {
                val myprofile = Intent(this, AddressList::class.java)
                startActivity(myprofile)
            } else {
                Toast.makeText(
                    this,
                    this.resources.getString(R.string.logginfirst),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding?.myorders?.setOnClickListener {
            if (model!!.isLoggedIn) {
                val myprofile = Intent(this, OrderList::class.java)
                startActivity(myprofile)
            } else {
                Toast.makeText(
                    this,
                    this.resources.getString(R.string.logginfirst),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding?.logout?.setOnClickListener {
            val alertDialog = SweetAlertDialog(it.context, SweetAlertDialog.NORMAL_TYPE)
            var customeview = PopConfirmationBinding.inflate(LayoutInflater.from(it.context))
//            customeview.textView.text = getString(R.string.warning_message)
            customeview.textView2.text =getString(R.string.success_loggedout)
            alertDialog.hideConfirmButton()
            customeview.okDialog.setOnClickListener {
                customeview.okDialog.isClickable = false
                customeview.textView.text = getString(R.string.done)
                customeview.textView2.text = getString(R.string.loggedout_text)
                alertDialog.showCancelButton(false)
                alertDialog.setConfirmClickListener(null)
                alertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                alertDialog.cancel()
                MagePrefs.clearUserData()
                binding?.logout?.visibility = View.GONE
                model!!.logOut()
                MagePrefs.clearSocialKey()

                var intent = Intent(this, HomePage::class.java)
                startActivity(intent)
                Constant.activityTransition(this)
                finish()

            }
            customeview.noDialog.setOnClickListener {
                customeview.noDialog.isClickable = false
                alertDialog.cancel()
            }
            alertDialog.setCustomView(customeview.root)
            alertDialog.show()

        }
        if(!HomePageViewModel.isLightModeOn()){
            applyTint()
        }
    }

    private fun applyTint() {
        try {
            binding!!.orderIcon.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_IN)
            binding!!.wishIcon.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_IN)
            binding!!.addressIcon.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_IN)
            binding!!.deleteIcon.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_IN)
            binding!!.creditIcon.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_IN)
            binding!!.manageCreditIcon.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_IN)
            binding!!.rewardifyicon.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_IN)
            binding!!.logoutIcon.setColorFilter(Color.parseColor(themeColor), PorterDuff.Mode.SRC_IN)
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }

    }

    private fun updateResponse(it: ApiResponse?) {
        try {
            when (it!!.status) {
                Status.SUCCESS -> {
                    if (it.data != null) {
                        var data = JSONObject(it.data.toString())
                        Toast.makeText(
                            this@AccountActivity,
                            data.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                        if (data.getString("success").equals("true")) {
                            startActivity(Intent(this@AccountActivity, HomePage::class.java))
                            MagePrefs.clearUserData()
                            binding?.logout?.visibility = View.GONE
                            model!!.logOut()
                            finish()
                        }
                    }
                }
                Status.ERROR -> {
                    Toast.makeText(this@AccountActivity, it.error!!.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } catch (e: Exception) {
            Log.e("DeleteRes", "onResponse: ${e.message}")
        }
    }

    private fun deleteUser() {
        try {
            val alertDialog = SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
            var customeview = PopConfirmationBinding.inflate(LayoutInflater.from(this))
            customeview.textView.text=this.getString(R.string.areyousure)
            customeview.textView2.text=this.getString(R.string.message)
            alertDialog.hideConfirmButton()
            customeview.okDialog.setOnClickListener{
                customeview.okDialog.isClickable=false
                customeview.textView.text=this.getString(R.string.done)
                customeview.textView2.text=resources.getString(R.string.deleteaddress)
                alertDialog.showCancelButton(false)
                alertDialog.setConfirmClickListener(null)
                alertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
                val customer_id = MagePrefs.getCustomerID()?.replace("gid://shopify/Customer/", "")!!.split("?")[0]
                CoroutineScope(Dispatchers.IO).launch {
                    dashboardmodel!!.deleteAccount(customer_id, this@AccountActivity)
                    alertDialog.dismissWithAnimation()
                }
            }
            customeview.noDialog.setOnClickListener{
                customeview.noDialog.isClickable=false
                alertDialog.dismissWithAnimation()
            }
            alertDialog.setCustomView(customeview.root)
            alertDialog.show()
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
    }

    fun MyProfileName() {
        super.onResume()
        var name = ""
        var shortname = ""
        CoroutineScope(Dispatchers.IO).launch {
            name = repository.allUserData[0].firstname + " " + repository.allUserData[0].lastname
            if (TextUtils.isEmpty(name.trim())) {
                name = "N/A"
            }
            when(repository.allUserData[0].firstname!!.length){
                0->{
                    shortname = " "
                }
                else->{
                    shortname = repository.allUserData[0].firstname!!.substring(0, 1)
                }
            }
            when(repository.allUserData[0].lastname!!.length){
                0->{
                    shortname = shortname+" "
                }
                else->{
                    shortname = shortname+repository.allUserData[0].lastname!!.substring(0, 1)
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                binding?.signin?.text = name
                binding?.usernameShortForm?.text = shortname
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MyProfileName()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }

    fun darktheme() {
        binding?.addressIcon?.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        binding?.creditIcon?.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        binding?.deleteIcon?.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        binding?.logoutIcon?.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        binding?.orderIcon?.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        binding?.manageCreditIcon?.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        binding?.profIcon?.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        binding?.wishIcon?.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
        binding?.rewardifyicon?.backgroundTintList = ColorStateList.valueOf(Color.BLACK)


        binding?.addressIcon?.imageTintMode = PorterDuff.Mode.SRC_ATOP
        binding?.creditIcon?.imageTintMode = PorterDuff.Mode.SRC_ATOP
        binding?.deleteIcon?.imageTintMode = PorterDuff.Mode.SRC_ATOP
        binding?.logoutIcon?.imageTintMode = PorterDuff.Mode.SRC_ATOP
        binding?.orderIcon?.imageTintMode = PorterDuff.Mode.SRC_ATOP
        binding?.manageCreditIcon?.imageTintMode = PorterDuff.Mode.SRC_ATOP
        binding?.profIcon?.imageTintMode = PorterDuff.Mode.SRC_ATOP
        binding?.wishIcon?.imageTintMode = PorterDuff.Mode.SRC_ATOP
        binding?.rewardifyicon?.imageTintMode = PorterDuff.Mode.SRC_ATOP
    }

}