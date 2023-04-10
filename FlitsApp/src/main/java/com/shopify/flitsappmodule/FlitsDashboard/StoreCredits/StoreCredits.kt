package com.shopify.flitsappmodule.FlitsDashboard.StoreCredits


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopify.apicall.ApiResponse
import com.shopify.apicall.CurrencyFormatter
import com.shopify.apicall.StoreCredits.StoreCreditTransactionAdapter
import com.shopify.flitsappmodule.FlitsDashboard.HowtoEarnSpent.EarnSpentActivity
import com.shopify.flitsappmodule.R
import com.shopify.flitsappmodule.databinding.ActivityMycreditsdataBinding
import com.rasmishopping.app.FlitsDashboard.StoreCredits.StoreCreditsViewModel
import org.json.JSONObject

class StoreCredits: AppCompatActivity() {
    private var binding: ActivityMycreditsdataBinding? = null
    private var model: StoreCreditsViewModel? = null
    var Response:JSONObject?=null
    var cid:String?=null
    var userid:String?=null
    var token:String?=null
    var appname:String?=null
    var currency_code:String?=null
    var context: Context?=null
    var title: TextView?=null
    private var toolbar: androidx.appcompat.widget.Toolbar? = null
    var themecolor:String?=null
    var themetextcolor:String?=null
//
//    @Inject
//    lateinit var factory: ViewModelFactory

    var transaction_adapter: StoreCreditTransactionAdapter= StoreCreditTransactionAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_mycreditsdata)
//        model = ViewModelProvider(this, factory).get(StoreCreditsViewModel::class.java)
//        model!!.context = this
        toolbar = findViewById(R.id.flitstoolbar)
        title = findViewById(R.id.title)

        if (getSupportActionBar() == null) {

            setSupportActionBar(toolbar)
        } else toolbar?.setVisibility(View.GONE)
        getSupportActionBar()?.setDisplayShowTitleEnabled(false);
//        getSupportActionBar()?.title.
        context=this
        model = ViewModelProvider(this).get(StoreCreditsViewModel::class.java)
        model!!.context = this
        if(intent.getStringExtra("cid")!=null)
        {
            cid=intent.getStringExtra("cid")
        }
        if(intent.getStringExtra("userId")!=null)
        {
            userid=intent.getStringExtra("userId")
        }
        if(intent.getStringExtra("token")!=null)
        {
            token=intent.getStringExtra("token")
        }
        if(intent.getStringExtra("appname")!=null)
        {
            appname=intent.getStringExtra("appname")
        }
        if(intent.getStringExtra("currencycode")!=null)
        {
            currency_code=intent.getStringExtra("currencycode")
        }
        if(intent.getStringExtra("themecolor")!=null)
        {
            themecolor=intent.getStringExtra("themecolor")
        }
        if(intent.getStringExtra("themetextcolor")!=null)
        {
            themetextcolor=intent.getStringExtra("themetextcolor")
        }
        toolbar?.setBackgroundColor(Color.parseColor(themecolor))
       title?.setTextColor(Color.parseColor(themetextcolor))

        model!!.GetStoreCredit(appname!!,cid!!,userid!!,token!!)
        model!!.credit_data.observe(this, {
            consumeresponse(it,currency_code!!)
        })
        binding?.balancetitle?.setOnClickListener {
            val intent=Intent(context,EarnSpentActivity::class.java)
            intent.putExtra("cid",cid)
            intent.putExtra("appname",appname)
            intent.putExtra("userId",userid)
            intent.putExtra("token",token)
            intent.putExtra("currencycode",currency_code)
            context!!.startActivity(intent)
        }
        var upArrow = resources.getDrawable(R.drawable.ic_backarrow_25)
        upArrow.setColorFilter(
            Color.parseColor(themetextcolor),
            PorterDuff.Mode.SRC_ATOP
        )
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        getSupportActionBar()?.setHomeAsUpIndicator(upArrow)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun consumeresponse(response: ApiResponse, currency_code:String) {
        if(JSONObject(response.data.toString()).has("customer")) {
            if (!JSONObject(response.data.toString()).getString("customer").equals("null")) {
                Response = JSONObject(
                    JSONObject(response.data.toString()).getString("customer").toString()
                )
                val earned_credits = Response?.getString("total_earned_credits")
                binding?.earnedCredit?.text = CurrencyFormatter.setsymbol(
                    (earned_credits?.toDouble()!! / 100).toString(),
                    currency_code
                )
                val spent_credits = Response?.getString("total_spent_credits")
                binding?.spentCredit?.text = CurrencyFormatter.setsymbol(
                    (spent_credits?.toDouble()!! / 100).toString(),
                    currency_code
                )
                var spent_credit = Response?.getJSONArray("spent_credits")
                var earned_credit = Response?.getJSONArray("earned_credits")
                var credit_log = Response?.getJSONArray("credit_log")

                transaction_adapter?.setData(credit_log!!, context!!, currency_code)
                binding?.historyRecycler?.adapter = transaction_adapter
                binding?.historyRecycler?.layoutManager = LinearLayoutManager(this)
                for (i in 0 until Response?.getJSONArray("credit_log")!!.length()) {
                    var current_credit = (Response!!.getJSONArray("credit_log")
                        .get(0) as JSONObject).getString("current_credits")
                    binding?.currentCredit?.text = CurrencyFormatter.setsymbol(
                        (current_credit?.toDouble()!! / 100).toString(),
                        currency_code
                    )
                }
            }
        }


    }
}
