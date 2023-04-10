package com.shopify.rewardifyappmodule

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.shopify.apicall.ApiResponse
import com.shopify.apicall.CustomLoader
import org.json.JSONObject

class CustomerActivity : AppCompatActivity() {
    var cid:String?=null
    lateinit var model:CustomerViewModel
    var customLoader:CustomLoader?=null
    lateinit var acesstoken:String
    var totalamount:TextView? =null
    var totalspent:TextView? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         totalamount = findViewById<TextView>(R.id.totalamount)
         totalspent = findViewById<TextView>(R.id.totalspent)
        model = ViewModelProvider(this).get(CustomerViewModel::class.java)
        model.context = this
        customLoader= CustomLoader(this)
        if(intent.getStringExtra("cid")!=null)
        { val customer_id=intent.getStringExtra("cid")
            cid=  customer_id?.replace("gid://shopify/Customer/", "")!!.split("?")[0]
        }
        customLoader?.show()
        model.getToken(intent.getStringExtra("clientid").toString(),intent.getStringExtra("clientsecret").toString())
        model.access_token.observe(this, { consumeresponse(it) })
        model.customer_data.observe(this, { displaydata(it) })
        model.transaction_data.observe(this, { displayTransactionData(it) })
        model.redeem_data.observe(this, { displayRedeemCode(it) })
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
                    model.getCustomerInfo(cid!!, jsonObject1.getString("access_token"))
                    model.getTransactionInfo(cid!!, jsonObject1.getString("access_token"), 1, 20)
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
                try{
                    val res = response.data.toString()
                    var jsonObject = JSONObject(res)
                    if(totalamount!=null){
                        totalamount!!.setText(jsonObject.getString("amount"))
                    }
                    if(totalspent!=null){
                        totalspent!!.setText(jsonObject.getJSONObject("customer").getString("totalSpent"))
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }
            com.shopify.apicall.Status.ERROR -> Log.d("javed", "responsedata: " + response.error)
            else -> {}
        }
    }

    fun displayTransactionData(response: ApiResponse) {
        if(customLoader!!.isShowing)
            customLoader!!.dismiss()
        when (response.status) {
            com.shopify.apicall.Status.SUCCESS -> {
                var transactionrecycler = findViewById<RecyclerView>(R.id.transactionrecycler)
                var transactionAdapter: TransactionAdapter = TransactionAdapter()
                if(response.data!=null) {
                    var transactionModel = Gson().fromJson<GetTransactionModel>(
                        response.data.toString(),
                        GetTransactionModel::class.java
                    ) as GetTransactionModel
                    transactionrecycler.adapter = transactionAdapter
                    transactionAdapter.setData(transactionModel)
                }
            }
            com.shopify.apicall.Status.ERROR -> Log.d("javed", "responsedata: " + response.error)
            else -> {
            }
        }
    }

    fun displayRedeemCode(response: ApiResponse) {
        if(customLoader!!.isShowing)
            customLoader!!.dismiss()
        when (response.status) {
            com.shopify.apicall.Status.SUCCESS -> {
                val jsonObject = JSONObject(response.data.toString())
                val redeemcode = findViewById<TextView>(R.id.redeemcode)
                val copy = findViewById<TextView>(R.id.copy)
                redeemcode.visibility = View.VISIBLE
                copy.visibility = View.VISIBLE
                redeemcode.text = jsonObject.getJSONObject("discount").getString("code")
                copy.setOnClickListener {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("discount", redeemcode.text.toString())
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Text Copied", Toast.LENGTH_LONG).show()
                }
            }
            com.shopify.apicall.Status.ERROR -> {
                Toast.makeText(this,"Unable to Generate Discount Code for the Amount",Toast.LENGTH_LONG).show()
                Log.d("javed", "responsedata: " + response.error)}
            else -> {
            }
        }
    }

    fun showDiscountDialog(view : View){
        val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
        val discountview=View.inflate(this,R.layout.dialog_generatediscount,null)
        val submitbtn=discountview.findViewById<Button>(R.id.submitbtn)
        val canclebtn=discountview.findViewById<Button>(R.id.canclebtn)
        val memo=discountview.findViewById<EditText>(R.id.memo)
        val amount=discountview.findViewById<EditText>(R.id.amount)
        canclebtn.setOnClickListener{
            alertDialog.dismiss()
        }
        submitbtn.setOnClickListener{
            if(amount.text.toString().isNotEmpty()) {
                alertDialog.dismiss()
                val jsonObject = JsonObject()
                jsonObject.addProperty("amount", amount.text.toString())
                jsonObject.addProperty("memo", memo.text.toString())
                jsonObject.addProperty("currency", "USD")
                customLoader?.show()
                model.getRedeemInfo(cid!!, acesstoken, jsonObject)
            }
            else
                Toast.makeText(this,"PLease Enter Amount",Toast.LENGTH_SHORT).show()
        }
        alertDialog.setView(discountview)
        alertDialog.show()
    }

    fun showDiscountList(view : View){
        val discintent= Intent(this,ActiveDiscountList::class.java)
        discintent.putExtra("cid",cid)
        discintent.putExtra("clientid",intent.getStringExtra("clientid").toString())
        discintent.putExtra("clientsecret",intent.getStringExtra("clientsecret").toString())
        startActivity(discintent)
        finish()
    }
}