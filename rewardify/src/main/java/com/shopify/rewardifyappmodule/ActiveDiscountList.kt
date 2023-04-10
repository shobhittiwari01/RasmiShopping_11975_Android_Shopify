package com.shopify.rewardifyappmodule

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.shopify.apicall.ApiResponse
import org.json.JSONArray
import org.json.JSONObject
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.shopify.apicall.CustomLoader


class ActiveDiscountList : AppCompatActivity() {
    lateinit var cid:String
    lateinit var model:CustomerViewModel
    lateinit var acesstoken:String
    var customLoader:CustomLoader?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discounts)
        model = ViewModelProvider(this).get(CustomerViewModel::class.java)
        model.context = this
        if(intent.getStringExtra("cid")!=null)
            cid= intent.getStringExtra("cid")!!
        customLoader= CustomLoader(this)
        model.getToken(intent.getStringExtra("clientid").toString(),intent.getStringExtra("clientsecret").toString())
        customLoader?.show()
        model.access_token.observe(this, { consumeresponse(it) })
        model.discount_data.observe(this, { displaydata(it) })
        model.recoverdiscount.observe(this, { responsedata(it) })
    }

    fun consumeresponse(response: ApiResponse) {
        if(customLoader!!.isShowing)
            customLoader!!.dismiss()
        when (response.status) {
            com.shopify.apicall.Status.SUCCESS -> {
                val jsonObject: String = response.data.toString()
                val jsonObject1 = JSONObject(jsonObject)
                if (response.data!!.asJsonObject.has("access_token")) {
                    acesstoken = jsonObject1.getString("access_token")
                    customLoader?.show()
                    model.getDiscountList(cid, acesstoken, 1, 20)
                }
            }
            com.shopify.apicall.Status.ERROR -> Log.d("javed", "responsedata: " + response.error)
            else -> {
            }
        }
    }

    fun displaydata(response: ApiResponse) {
        if(customLoader!!.isShowing)
            customLoader!!.dismiss()
        when (response.status) {
            com.shopify.apicall.Status.SUCCESS -> {
                val jsonObject= JSONArray(response.data.toString())
                val len=jsonObject.length()
                val activedisclist = findViewById<LinearLayout>(R.id.activedisclist)
                if(len>0) {
                    for (i in 0 until len) {
                        val obj = jsonObject.getJSONObject(i)
                        val view = View.inflate(this, R.layout.list_discounts, null)
                        val discount_txt = view.findViewById<TextView>(R.id.discount_txt)
                        val discountcode = view.findViewById<TextView>(R.id.discountcode)
                        val recoverdiscounts = view.findViewById<TextView>(R.id.recoverdiscounts)
                        val copy = view.findViewById<TextView>(R.id.copy)
                        discountcode.text = obj.getString("code")
                        discount_txt.text =
                            "Discount code generated for " + obj.getString("currency") + obj.getString(
                                "amount"
                            )
                        activedisclist.addView(view)
                        recoverdiscounts.setOnClickListener {
                            val jsonObject = JsonObject()
                            customLoader?.show()
                            model.discountRecover(
                                obj.getString("shopifyId"),
                                acesstoken,
                                jsonObject
                            )
                        }
                        copy.setOnClickListener {
                            val clipboard: ClipboardManager =
                                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip =
                                ClipData.newPlainText("discount", discountcode.text.toString())
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(this, "Text Copied", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                else{
                    (findViewById<TextView>(R.id.nodiscount)).visibility= View.VISIBLE
                    activedisclist.visibility = View.GONE
                }
            }
            com.shopify.apicall.Status.ERROR -> Log.d("javed", "responsedata: "+response.error)
            else -> {
            }
        }
    }

    fun responsedata(response: ApiResponse) {
        if(customLoader!!.isShowing)
            customLoader!!.dismiss()
        when (response.status) {
            com.shopify.apicall.Status.SUCCESS -> {
                val jsonObject = JSONObject(response.data.toString())
                if (jsonObject.has("transactions")) {
                    Toast.makeText(this, "Discount Recover", Toast.LENGTH_LONG).show()
                    finish()
                    startActivity(intent)
                }
            }
            com.shopify.apicall.Status.ERROR -> Log.d("javed", "responsedata: "+response.error)
            else -> {
            }
        }
    }
}