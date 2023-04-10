package com.rasmishopping.app.yotporewards.getrewards

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.databinding.ActivityGetRewardsBinding
import com.rasmishopping.app.databinding.RedeemdialogBinding
import com.rasmishopping.app.utils.ApiResponse
import com.rasmishopping.app.utils.ViewModelFactory
import com.rasmishopping.app.yotporewards.getrewards.adapter.GetRewardsAdapter
import com.rasmishopping.app.yotporewards.getrewards.model.GetRewardModel
import com.rasmishopping.app.yotporewards.getrewards.model.GetRewardModelItem
import org.json.JSONObject
import javax.inject.Inject

class GetRewardsActivity : NewBaseActivity() {
    private var binding: ActivityGetRewardsBinding? = null
    private var rewardsViewModel: GetRewardsViewModel? = null
    private val TAG = "GetRewardsActivity"
    var dialog: Dialog? = null

    @Inject
    lateinit var factory: ViewModelFactory

    @Inject
    lateinit var getRewardsAdapter: GetRewardsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.activity_get_rewards, group, true)
        (application as MyApplication).mageNativeAppComponent!!.doGetRewadsInjection(this)
        rewardsViewModel = ViewModelProvider(this, factory).get(GetRewardsViewModel::class.java)
        rewardsViewModel?.context = this
        showBackButton()
        showTittle(getString(R.string.get_rewards))
        rewardsViewModel?.getrewards?.observe(this, Observer { this.consumeRewards(it) })
        rewardsViewModel?.redeemPoints?.observe(this, Observer { this.consumeRedeemPoints(it) })
        rewardsViewModel?.getRewards()
    }

    private fun consumeRedeemPoints(response: ApiResponse?) {
        if (response!!.status.toString() == "SUCCESS") {
            var responseObj = JSONObject(response?.data?.toString())
            //if (responseObj.has("amount")) {
            Toast.makeText(
                this,
                "Thanks for redeeming! Here's your coupon code: " + responseObj.getString("reward_text"),
                Toast.LENGTH_LONG
            ).show()
            //}
        }
        if (response!!.status.toString() == "ERROR") {
            if (response.error.toString().contains("422")) {
                Toast.makeText(
                    this,
                    "You don't have enough points for this amount",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
    }

    private fun consumeRewards(response: ApiResponse?) {
        if (response?.data != null) {
            Log.d(TAG, "consumeRewards: " + response.data.toString())
            var rewardModel = Gson().fromJson<String>(
                response.data.toString(),
                GetRewardModel::class.java
            ) as GetRewardModel
            getRewardsAdapter.setData(rewardModel, object : GetRewardsAdapter.ClickCallback {
                override fun redeemPoint(rewardItem: GetRewardModelItem) {
                    dialog = Dialog(this@GetRewardsActivity, R.style.WideDialog)
                    dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
                    dialog?.window?.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    var redeemdialogBinding = DataBindingUtil.inflate<RedeemdialogBinding>(
                        layoutInflater,
                        R.layout.redeemdialog,
                        null,
                        false
                    )
                    redeemdialogBinding.headerPrice.text = rewardItem.name
                    redeemdialogBinding.description.text = rewardItem.description

                    dialog?.setContentView(redeemdialogBinding.root)
                    redeemdialogBinding.cancelBut.setOnClickListener {
                        dialog?.dismiss()
                    }
                    redeemdialogBinding.butRedeem.setOnClickListener {
                        rewardsViewModel?.redeemPoints(rewardItem.id.toString())
                    }
                    dialog?.show()
                }
            })
            binding?.rewadslist?.adapter = getRewardsAdapter
        }
    }
}