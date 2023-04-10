package com.rasmishopping.app.userprofilesection.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.shopify.buy3.Storefront
import com.rasmishopping.app.FlitsDashboard.Profile.profile.CustomerViewModel
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.viewmodels.LeftMenuViewModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.databinding.MUserprofileBinding
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.userprofilesection.models.User
import com.rasmishopping.app.userprofilesection.viewmodels.UserProfileViewModel
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.Urls
import com.rasmishopping.app.utils.ViewModelFactory
import org.w3c.dom.Text
import javax.inject.Inject

class UserProfile : NewBaseActivity() {
    private var binding: MUserprofileBinding? = null

    @Inject
    lateinit var factory: ViewModelFactory
    private var model: UserProfileViewModel? = null
    private var user: User? = null
    private var customermodel: CustomerViewModel? = null
    private var leftmodel: LeftMenuViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_userprofile, group, true)
        showBackButton()
        hidenavbottom()
        showTittle(resources.getString(R.string.myprofile))
        (application as MyApplication).mageNativeAppComponent!!.doUserProfileInjection(this)
        user = User()
        binding!!.user = user
        binding!!.handler = ClickHandler()
        model = ViewModelProvider(this, factory).get(UserProfileViewModel::class.java)
        model!!.context = this
        customermodel = ViewModelProvider(this, factory).get(CustomerViewModel::class.java)
        customermodel?.context = this
        leftmodel = ViewModelProvider(this, factory).get(LeftMenuViewModel::class.java)
        leftmodel!!.context = this
        model!!.getResponse_().observe(this, Observer<Storefront.Customer> { this.consumeResponse(it) })
        model!!.getFlag().observe(this, Observer<Boolean> { this.consumeResponse(it) })
        model!!.passwordResponse.observe(this, Observer<String> { this.consumeResponse(it) })
        model!!.errorMessageResponse.observe(this, Observer<String> { this.showToast(it) })
        binding?.checkbox!!.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                binding?.passLyt?.visibility = View.VISIBLE
                binding?.passwordLyt?.visibility = View.VISIBLE
            } else {
                binding?.passLyt?.visibility = View.GONE
                binding?.passwordLyt?.visibility = View.GONE
            }
        }
        if(MagePrefs.getSocialKey() != null){
            binding!!.checkbox.visibility = View.GONE
        }else{
            binding!!.checkbox.visibility = View.VISIBLE
        }
    }

    private fun consumeResponse(customer: Storefront.Customer) {
        if(customer.firstName!=null){
            user!!.firstname = customer.firstName
        }else{
            user!!.firstname=" "
        }
        if(customer.lastName!=null){
            user!!.lastname = customer.lastName
        }
        else{
            user!!.lastname=" "
        }
        if(customer.email!=null){
            user!!.email = customer.email
        }
        model!!.saveUser(user!!)
    }

    private fun consumeResponse(password: String) {
        if (password!=null){
            user!!.password = password
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    inner class ClickHandler {
        fun updateProfile(view: View, user: User) {
            if (binding!!.firstname.text!!.toString().trim().isEmpty()) {
                binding!!.firstname.error = resources.getString(R.string.empty)
                binding!!.firstname.requestFocus()
            } else {
                if (binding!!.lastname.text!!.toString().trim().isEmpty()) {
                    binding!!.lastname.error = resources.getString(R.string.empty)
                    binding!!.lastname.requestFocus()
                } else {
                    if (binding!!.email.text!!.toString().trim().isEmpty()) {
                        binding!!.email.error = resources.getString(R.string.empty)
                        binding!!.email.requestFocus()
                    } else {
                        if (!model!!.isValidEmail(binding!!.email.text!!.toString())) {
                            binding!!.email.error = resources.getString(R.string.invalidemail)
                            binding!!.email.requestFocus()
                        } else {
                            if(binding!!.checkbox.isChecked){
                                if (binding!!.password.text!!.toString().trim().isEmpty()) {
                                    binding!!.password.error = resources.getString(R.string.empty)
                                    binding!!.password.requestFocus()
                                } else {
                                    if (binding!!.ConfirmPassword.text!!.toString().trim().isEmpty()) {
                                        binding!!.ConfirmPassword.error =
                                            resources.getString(R.string.empty)
                                        binding!!.ConfirmPassword.requestFocus()
                                    } else {
                                        if (binding!!.password.text!!.toString().trim() == binding!!.ConfirmPassword.text!!.toString().trim()) {
                                            proceedWithUpdate()
                                        } else {
                                            binding!!.password.error =
                                                resources.getString(R.string.passwordnotmatch)
                                            binding!!.ConfirmPassword.error =
                                                resources.getString(R.string.passwordnotmatch)
                                            binding!!.password.requestFocus()
                                        }
                                    }
                                }
                            }else{
                                proceedWithUpdate()
                            }
                        }
                    }
                }
            }
        }
    }
    fun proceedWithUpdate(){
        user!!.firstname = binding!!.firstname.text!!.toString()
        user!!.lastname = binding!!.lastname.text!!.toString()
        user!!.email = binding!!.email.text!!.toString()
        user!!.password = binding!!.password.text!!.toString()
        if(SplashViewModel.featuresModel.Enable_flits_App) {
            customermodel?.UpdatePassword(Urls.X_Integration_App_Name!!,MagePrefs.getCustomerID()!!,user!!.password!!,user!!.password!!,
                Urls.user_id!!,
                Urls.token!!)
        }
        model!!.updateDataonServer(user!!)
    }
    fun consumeResponse(flag:Boolean){
        if(flag){
            if(binding!!.checkbox.isChecked){
                MagePrefs.clearUserData()
                model!!.logOut()
                MagePrefs.clearSocialKey()
                var intent=Intent(this,HomePage::class.java)
                startActivity(intent)
                Constant.activityTransition(this)
                finish()
            }else{
                var intent=Intent(this@UserProfile,UserProfile::class.java)
                this@UserProfile.startActivity(intent)
                finish()
                Constant.activityTransition(this)
            }
        }
    }
}
