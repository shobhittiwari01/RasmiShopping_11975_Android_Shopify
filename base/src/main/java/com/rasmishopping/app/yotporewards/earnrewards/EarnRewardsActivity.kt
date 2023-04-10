package com.rasmishopping.app.yotporewards.earnrewards

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.databinding.ActivityEarnRewardsBinding
import com.rasmishopping.app.databinding.RedeemdialogBinding
import com.rasmishopping.app.utils.ApiResponse
import com.rasmishopping.app.utils.Constant
import com.rasmishopping.app.utils.ViewModelFactory
import com.rasmishopping.app.yotporewards.earnrewards.adapter.EarnRewardAdapter
import com.rasmishopping.app.yotporewards.earnrewards.model.EarnRewardModelItem
import com.rasmishopping.app.yotporewards.referfriend.ReferFriendActivity
import java.lang.reflect.Type
import java.util.*
import javax.inject.Inject
import org.joda.time.DateTimeFieldType.dayOfMonth

import org.joda.time.DateTimeFieldType.monthOfYear

class EarnRewardsActivity : NewBaseActivity() {
    private var binding: ActivityEarnRewardsBinding? = null
    private var earnRewardsViewModel: EarnRewardsViewModel? = null
    var dialog:Dialog?=null
    @Inject
    lateinit var earnRewardAdapter: EarnRewardAdapter

    @Inject
    lateinit var factory: ViewModelFactory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.activity_earn_rewards, group, true)
        (application as MyApplication).mageNativeAppComponent!!.doEarnRewadsInjection(this)
        showBackButton()
        showTittle(getString(R.string.earn_points))
        earnRewardsViewModel =
            ViewModelProvider(this, factory).get(EarnRewardsViewModel::class.java)
        earnRewardsViewModel?.context = this
        earnRewardsViewModel?.earnrewards?.observe(this, Observer { this.consumeEarnRewards(it) })
        earnRewardsViewModel?.earnBirthrewards?.observe(this, Observer { this.consumeEarnBirthRewards(it) })
        earnRewardsViewModel?.earnRewards()
    }

    private fun consumeEarnRewards(response: ApiResponse?) {
        if (response?.data != null) {
            val collectionType: Type = object : TypeToken<List<EarnRewardModelItem>>() {}.type
            var earnrewardModel =
                Gson().fromJson<List<EarnRewardModelItem>>(response.data.toString(), collectionType)
                    .toList()
            var redeemdialogBinding: RedeemdialogBinding? = null
            earnRewardAdapter.setData(
                earnrewardModel,
                object : EarnRewardAdapter.ClickEarnCallback, DatePickerDialog.OnDateSetListener {
                    override fun earnRewardCallback(earnRewardModelItem: EarnRewardModelItem) {
                        dialog = Dialog(this@EarnRewardsActivity, R.style.WideDialog)
                        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
                        dialog?.window?.setLayout(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT
                        )
                        redeemdialogBinding = DataBindingUtil.inflate<RedeemdialogBinding>(
                            layoutInflater,
                            R.layout.redeemdialog,
                            null,
                            false
                        )
                        redeemdialogBinding?.headerPrice?.text = earnRewardModelItem.title
                        redeemdialogBinding?.description?.text = earnRewardModelItem.details
                        if (TextUtils.isEmpty(earnRewardModelItem.ctaText)) {
                            redeemdialogBinding?.butRedeem?.visibility = View.GONE
                        } else {
                            redeemdialogBinding?.butRedeem?.visibility = View.VISIBLE
                            redeemdialogBinding?.butRedeem?.text = earnRewardModelItem.ctaText
                        }

                        dialog?.setContentView(redeemdialogBinding?.root!!)
                        if (earnRewardModelItem.type.equals("BirthdayCampaign")) {
                            redeemdialogBinding?.selectdate?.visibility = View.VISIBLE
                            redeemdialogBinding?.selectdate?.setOnClickListener {
                                val mCalendar = Calendar.getInstance()
                                val dialog = DatePickerDialog(
                                    this@EarnRewardsActivity, this,
                                    mCalendar[Calendar.YEAR], mCalendar[Calendar.MONTH],
                                    mCalendar[Calendar.DAY_OF_MONTH]
                                )

                                dialog.datePicker.maxDate = Date().time
                                dialog.show()
                            }
                        }

                        redeemdialogBinding?.butRedeem?.setOnClickListener {
                            if (earnRewardModelItem.type.equals("ReferralCampaign")) {
                                startActivity(
                                    Intent(
                                        this@EarnRewardsActivity,
                                        ReferFriendActivity::class.java
                                    )
                                )
                                Constant.activityTransition(this@EarnRewardsActivity)
                                finish()
                            }
                            if (earnRewardModelItem.type.equals("BirthdayCampaign")) {
                                if (redeemdialogBinding!!.selectdate.text.toString() == resources.getString(
                                        R.string.select_date
                                    )
                                ) {
                                    Toast.makeText(
                                        this@EarnRewardsActivity,
                                        getString(R.string.pleaseselectbirth),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val date = redeemdialogBinding!!.selectdate.text.split("-")
                                    earnRewardsViewModel?.earnBirthRewards(
                                        date[0],
                                        date[1],
                                        date[2]
                                    )
                                }
                            }
                        }
                        redeemdialogBinding?.cancelBut?.setOnClickListener {
                            dialog?.dismiss()
                        }
                        dialog?.show()
                    }

                    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
                        val month: Int = p2 + 1
                        var fm = "" + month
                        var fd = "" + p3
                        if (month < 10)
                            fm = "0$month"
                        if (p3 < 10)
                            fd = "0$p3"
                        val date = "$fd-$fm-$p1"
                        redeemdialogBinding?.selectdate?.text = date
                    }
                })
            binding?.earnrewardList?.adapter = earnRewardAdapter
        }
    }

    private fun consumeEarnBirthRewards(response: ApiResponse?) {
        Toast.makeText(
            this@EarnRewardsActivity,
            "Success",
            Toast.LENGTH_SHORT
        ).show()
        if(dialog!!.isShowing)
            dialog!!.dismiss()
    }
}