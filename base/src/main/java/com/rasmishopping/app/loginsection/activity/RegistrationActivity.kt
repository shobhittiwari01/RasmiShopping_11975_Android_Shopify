package com.rasmishopping.app.loginsection.activity

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.shopify.buy3.Storefront
import com.rasmishopping.app.FlitsDashboard.Profile.profile.CustomerViewModel
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.basesection.models.FeaturesModel
import com.rasmishopping.app.basesection.viewmodels.SplashViewModel
import com.rasmishopping.app.databinding.MRegistrationpageBinding
import com.rasmishopping.app.homesection.activities.HomePage
import com.rasmishopping.app.loginsection.viewmodels.RegistrationViewModel
import com.rasmishopping.app.sharedprefsection.MagePrefs
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.Urls
import com.rasmishopping.app.utils.ViewModelFactory
import kotlinx.android.synthetic.main.m_newbaseactivity.*
import javax.inject.Inject

class RegistrationActivity : NewBaseActivity() {
    private var binding: MRegistrationpageBinding? = null
    private var customermodel: CustomerViewModel? = null
    @Inject
    lateinit var factory: ViewModelFactory
    private var model: RegistrationViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.m_registrationpage, group, true)
        nav_view.visibility = View.GONE
//        showTittle(resources.getString(R.string.signupwithustext))
        showBackButton()
        (application as MyApplication).mageNativeAppComponent!!.doRegistrationActivityInjection(this)
        model = ViewModelProvider(this, factory).get(RegistrationViewModel::class.java)
        model!!.context = this
        model!!.Response().observe(this, Observer<Storefront.Customer> { this.consumeResponse(it) })
        model!!.LoginResponse().observe(this, Observer<Storefront.CustomerAccessToken> { this.consumeLoginResponse(it) })
        customermodel = ViewModelProviders.of(this, factory).get(CustomerViewModel::class.java)
        customermodel?.context = this
        model!!.message.observe(this, Observer<String> { this.showToast(it) })
        var hand = MyClickHandlers(this)
        binding!!.handlers = hand
        binding!!.includedlregistartion!!.textView.textSize=22f
//        themeDefine()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun consumeLoginResponse(token: Storefront.CustomerAccessToken) {
        showToast(resources.getString(R.string.successfullogin))
        model!!.savetoken(token)
        if(SplashViewModel.featuresModel.Enable_flits_App) {
            customermodel!!.SaveProfileInfo(Urls.X_Integration_App_Name!!,MagePrefs.getCustomerID().toString(),
                Urls.user_id!!,
                Urls.token!!)
        }
        val intent = Intent(this@RegistrationActivity, HomePage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        Constant.activityTransition(this)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }

    private fun consumeResponse(customer: Storefront.Customer) {
        MagePrefs.setCustomerId((customer.id.toString())!!)
        MagePrefs.setCustomerEmail((customer.email.toString())!!)
        Constant.FirebaseEvent_SignUp(customer.email)
        model!!.insertUserData(customer)
    }


    inner class MyClickHandlers(private val context: Context) : BaseObservable() {

        @get:Bindable
        var image: String? = null
            set(image) {
                field = image
                notifyPropertyChanged(BR.image)
            }

        fun RegistrationRequest(view: View) {
            if (binding!!.includedlregistartion.firstname.text!!.toString().trim().isEmpty()) {
                binding!!.includedlregistartion.firstname.error =
                    resources.getString(R.string.empty)
                binding!!.includedlregistartion.firstname.requestFocus()
            } else {
                if (binding!!.includedlregistartion.lastname.text!!.toString().trim().isEmpty()) {
                    binding!!.includedlregistartion.lastname.error =
                        resources.getString(R.string.empty)
                    binding!!.includedlregistartion.lastname.requestFocus()
                }
                else {
                    if (binding!!.includedlregistartion.email.text!!.toString().trim().isEmpty()) {
                        binding!!.includedlregistartion.email.error =
                            resources.getString(R.string.empty)
                        binding!!.includedlregistartion.email.requestFocus()
                    }
                    else {
                        if (!model!!.isValidEmail(binding!!.includedlregistartion.email.text!!.toString().trim()
                            )) {
                            binding!!.includedlregistartion.email.error =
                                resources.getString(R.string.invalidemail)
                            binding!!.includedlregistartion.email.requestFocus()
                        } else {
                            if (binding!!.includedlregistartion.password.text!!.toString().trim().isEmpty()) {
                                binding!!.includedlregistartion.password.error =
                                    resources.getString(R.string.empty)
                                binding!!.includedlregistartion.password.requestFocus()
                            }
//                            else if (!model!!.isStrongPassword(binding!!.includedlregistartion.password.text!!.toString())){
//                                binding!!.includedlregistartion.password.error =
//                                    resources!!.getString(R.string.enter_valid_password)
//                                binding!!.includedlregistartion.password.requestFocus()
//                            }
                            else {
                                if (binding!!.includedlregistartion.ConfirmPassword.text!!.toString().trim()
                                        .isEmpty()
                                ) {
                                    binding!!.includedlregistartion.ConfirmPassword.error =
                                        resources.getString(R.string.empty)
                                    binding!!.includedlregistartion.ConfirmPassword.requestFocus()
                                }
//                                else if (!model!!.isStrongPassword(binding!!.includedlregistartion.ConfirmPassword.text!!.toString())){
//                                    binding!!.includedlregistartion.ConfirmPassword.error =
//                                        resources!!.getString(R.string.passwordnotmatch)
//                                    binding!!.includedlregistartion.ConfirmPassword.requestFocus()
//                                }

                                else {
                                    if (binding!!.includedlregistartion.password.text!!.toString().trim() == binding!!.includedlregistartion.ConfirmPassword.text!!.trim().toString()) {
                                        model!!.getRegistrationDetails(
                                            binding!!.includedlregistartion.firstname.text!!.toString(),
                                            binding!!.includedlregistartion.lastname.text!!.toString(),
                                            binding!!.includedlregistartion.email.text!!.toString(),
                                            binding!!.includedlregistartion.password.text!!.toString()
                                        )
                                    } else {
                                        binding!!.includedlregistartion.password.error =
                                            resources.getString(R.string.passwordnotmatch)
                                        binding!!.includedlregistartion.ConfirmPassword.error =
                                            resources.getString(R.string.passwordnotmatch)
                                        binding!!.includedlregistartion.password.requestFocus()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
