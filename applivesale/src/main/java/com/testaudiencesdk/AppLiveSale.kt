package com.testaudiencesdk

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.livebroadcasterapi.LiveBroadcast
import com.livebroadcasterapi.LiveBroadcastConfig
import com.livebroadcasterapi.LiveBroadcastConstants
import com.livebroadcasterapi.model.BroadcastItemResponse
import com.livebroadcasterapi.model.LiveBroadcastError
import com.livebroadcasterapi.model.appIdModule.ExtractAppIdResponse
import com.livebroadcasterapi.model.loginModel.LoginResponse
import com.livebroadcasterapi.network.response.CompletionHandler
import com.livebroadcasterapi.util.LiveBroadcasterPreferences
import com.livebroadcasterui.LiveBroadcastUI
import com.livebroadcasterui.LiveBroadcastUIConfig
import com.livebroadcasterui.Utils
import com.livebroadcasterui.home.BroadcastListActivity
import com.livebroadcasterui.liveEvent.LiveEventActivity
import com.livebroadcasterui.liveEvent.product.IProductInfo

import com.testaudiencesdk.databinding.ActivitySaleBinding

class AppLiveSale : AppCompatActivity(), IProductInfo {
    private val TAG = AppLiveSale::class.java.simpleName
    private lateinit var eventListResponse : BroadcastItemResponse
    private lateinit var binding: ActivitySaleBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        LiveEventActivity.iProductInfo = this
        initializeLiveBroadcast()


        binding.btnHome.setOnClickListener {
            LiveBroadcast.getInstance().setAccessToken("")
            LiveBroadcast.getInstance().setUserRole(LiveBroadcastConstants.ANONYMOUS)
            navigateHomeActivity()
        }

        binding.btnAlrea.setOnClickListener {
            navigateHomeActivity()
        }

        binding.btnEvent.setOnClickListener{
            callBroadcastDetailApi(binding.etEvent.text.toString().trim())
        }

        binding.btnTestLogin.setOnClickListener{
            callLoginApi("rajat.g@bigsteptech.com","123456")
        }

        binding.btnLoginUser.setOnClickListener{
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if(email.isNotBlank() && password.isNotBlank()){
                callLoginApi(email,password)
            }else{
                Utils.showToastMessage(this,getString(R.string.please_enter_email_and_password_to_login))
            }
        }

        binding.btnLogout.setOnClickListener{
            LiveBroadcast.getInstance().setAccessToken("")
            LiveBroadcasterPreferences.clearSharedPreferences(this)
            LiveBroadcast.getInstance().setUserRole(LiveBroadcastConstants.ANONYMOUS)
            binding.clLogin.visibility = View.VISIBLE
            binding.btnAlrea.visibility = View.GONE
            binding.btnLogout.visibility = View.GONE
        }
    }

    private fun navigateHomeActivity() {
        val intent = Intent(this, BroadcastListActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    /**
     * Call BroadcastDetailApi(
     */
    private fun callBroadcastDetailApi(broadcastId:String) {
        LiveBroadcast.getApiInstance().getBroadcastDetail(broadcastId,object : CompletionHandler<BroadcastItemResponse,LiveBroadcastError> {
            override fun onSuccess(result: BroadcastItemResponse) {
                eventListResponse = result
                if(eventListResponse.status==LiveBroadcastConstants.LIVE){ //Show Live Broadcast
                        if(System.currentTimeMillis()<Utils.getMilliFromDate(eventListResponse.endTime)) {
                            navigateToLiveBroadcast()
                        }else {
                            //If event end time is past the current time but event not ended
                            Utils.showAlert(this@AppLiveSale,getString(R.string.event_finished),
                            getString(R.string.event_not_available),false)
                        }
                    }else if(eventListResponse.status==LiveBroadcastConstants.UPCOMING){ //Show Upcoming Broadcast
                        navigateToPreviewBroadcast()
                    }else if(eventListResponse.status==LiveBroadcastConstants.COMPLETED){ // Show Broadcast Recording
                        navigateToBroadcastRecording()
                    }
                }

            override fun onError(error: LiveBroadcastError?) {
                Log.e(TAG, "errorMessage: ${error?.error?.message}")
            }
        })
    }

    private fun navigateToLiveBroadcast() {
        Utils.showLiveBroadcast(this, eventListResponse, false)
    }

    private fun navigateToPreviewBroadcast() {
        Utils.showBroadcastPreview(this,eventListResponse)
        finishAffinity()
    }

    private fun navigateToBroadcastRecording() {
        Utils.showBroadcastRecording(this,eventListResponse)
        finishAffinity()
    }


    /**
     * Call login api
     */
    private fun callLoginApi(email:String, password:String) {
        LiveBroadcast.getApiInstance().loginWithEmailPassword(email,password, object : CompletionHandler<LoginResponse,LiveBroadcastError> {
            override fun onSuccess(result: LoginResponse) {
                LiveBroadcast.getInstance().setAccessToken(result.id)
                LiveBroadcast.getInstance().setUserRole(LiveBroadcastConstants.USER)
                navigateHomeActivity()
            }

            override fun onError(error: LiveBroadcastError?) {
                Log.e(TAG, "errorMessage: ${error?.error?.message}")
            }
        })
    }

    /**
     * App Id Api call
     */
    private fun callModulesApi() {
        LiveBroadcast.getApiInstance().getAppId(object : CompletionHandler<ExtractAppIdResponse,LiveBroadcastError>{
            override fun onSuccess(result: ExtractAppIdResponse) {
                if(result.settings[0].key== LiveBroadcastConstants.APP_ID){
                    Log.e(TAG, "AppId: $result.settings[0].value")
                    LiveBroadcast.getInstance().setAppId(this@AppLiveSale,result.settings[0].value)
                }
            }

            override fun onError(error: LiveBroadcastError?) {
                Log.e(TAG, "errorMessage: ${error?.error?.message}")
            }

        })
    }

    override fun onProductClick(productId: String, productUrl: String) {
        Utils.showToastMessage(this,"$productId")
        Utils.showToastMessage(this,"$productUrl")
        Log.i("ProductSaif",""+productId)
        Log.i("ProductSaif",""+productUrl)
    }
    private fun initializeLiveBroadcast() {
        val liveBroadcasterConfig = LiveBroadcastConfig.Builder(this)
            .setAccessToken(LiveBroadcasterPreferences.getAccessToken(this)?:"")
            .setPublicKey("vBFfEYzWxwh62DMa") //Add public key
            .setLoggingEnabled(true)
            .build()

        LiveBroadcast.initialize(liveBroadcasterConfig)

        val liveBroadcastUiConfig = LiveBroadcastUIConfig.Builder()
            .messageTextSize(resources.getDimensionPixelSize(com.livebroadcasterui.R.dimen.dimen_14sp))
            .build()

        LiveBroadcastUI.initialize(liveBroadcastUiConfig)
        val accessToken = LiveBroadcast.getInstance().getAccessToken()
        Log.e(TAG, "LiveBroadcasterPreferences accessToken: $accessToken")
        if(!TextUtils.isEmpty(accessToken)){
            binding.clLogin.visibility = View.GONE
            binding.btnAlrea.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.VISIBLE
        }else{
            binding.clLogin.visibility = View.VISIBLE
            binding.btnAlrea.visibility = View.GONE
            binding.btnLogout.visibility = View.GONE
        }

        //  call modules api
        callModulesApi()
    }
}

