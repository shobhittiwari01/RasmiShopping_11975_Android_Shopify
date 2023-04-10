package com.shopify.livesale

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.livebroadcasterapi.LiveBroadcast
import com.livebroadcasterapi.LiveBroadcastConfig
import com.livebroadcasterapi.LiveBroadcastConstants
import com.livebroadcasterapi.model.LiveBroadcastError
import com.livebroadcasterapi.model.appIdModule.ExtractAppIdResponse
import com.livebroadcasterapi.model.loginModel.LoginResponse
import com.livebroadcasterapi.network.response.CompletionHandler
import com.livebroadcasterapi.util.LiveBroadcasterPreferences
import com.livebroadcasterui.LiveBroadcastUI
import com.livebroadcasterui.LiveBroadcastUIConfig
import com.livebroadcasterui.home.BroadcastListActivity
import com.livebroadcasterui.liveEvent.LiveEventActivity
import com.livebroadcasterui.liveEvent.product.IProductInfo
import com.shopify.livesale.databinding.LiveSaleBinding

class LiveSaleActivity : AppCompatActivity(), IProductInfo {
    private lateinit var binding: LiveSaleBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*binding = LiveSaleBinding.inflate(layoutInflater)
        setContentView(binding.root)*/
        if (intent.getStringExtra("publickey") != null) {
            initializeLiveBroadcast(intent.getStringExtra("publickey")!!)
        }

    }

    private fun initializeLiveBroadcast(key: String) {
        val liveBroadcasterConfig = LiveBroadcastConfig.Builder(this)
            .setAccessToken(LiveBroadcasterPreferences.getAccessToken(this) ?: "")
            .setPublicKey(key) //Add public key
            .setLoggingEnabled(true)
            .build()
        LiveBroadcast.initialize(liveBroadcasterConfig)
        val liveBroadcastUiConfig = LiveBroadcastUIConfig.Builder()
            .messageTextSize(resources.getDimensionPixelSize(R.dimen.dimen_14sp))
            .build()
        LiveBroadcastUI.initialize(liveBroadcastUiConfig)
        val accessToken = LiveBroadcast.getInstance().getAccessToken()
        callModulesApi()
    }

    override fun onProductClick(productId: String, productUrl: String) {
        TODO("Not yet implemented")
    }

    private fun callModulesApi() {
        LiveBroadcast.getApiInstance()
            .getAppId(object : CompletionHandler<ExtractAppIdResponse, LiveBroadcastError> {
                override fun onSuccess(result: ExtractAppIdResponse) {
                    if (result.settings[0].key == LiveBroadcastConstants.APP_ID) {
                        Log.e(TAG, "AppId: $result.settings[0].value")
                        LiveBroadcast.getInstance()
                            .setAppId(this@LiveSaleActivity, result.settings[0].value)
                        if (intent.getStringExtra("usertype") != null) {
                            when (intent.getStringExtra("usertype")) {
                                "user" -> {
                                    callLoginApi(
                                        intent.getStringExtra("id")!!,
                                        intent.getStringExtra("password")!!
                                    )
                                }
                                "guest" -> {
                                    LiveBroadcast.getInstance().setAccessToken("")
                                    LiveBroadcast.getInstance()
                                        .setUserRole(LiveBroadcastConstants.GUEST_USER)
                                    navigateHomeActivity()
                                }
                            }
                        }
                    }
                }

                override fun onError(error: LiveBroadcastError?) {
                    Log.e(TAG, "errorMessage: ${error?.error?.message}")
                }

            })
    }

    private fun callLoginApi(email: String, password: String) {
        LiveBroadcast.getApiInstance().loginWithEmailPassword(email, password, object :
            CompletionHandler<LoginResponse, LiveBroadcastError> {
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

    private fun navigateHomeActivity() {
        LiveEventActivity.iProductInfo = this
        val intent = Intent(this, BroadcastListActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }
}