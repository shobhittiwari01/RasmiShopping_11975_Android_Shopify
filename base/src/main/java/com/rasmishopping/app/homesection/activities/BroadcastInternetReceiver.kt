package com.rasmishopping.app.homesection.activities

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.view.LayoutInflater
import com.rasmishopping.app.databinding.NetworkStateLytBinding

class BroadcastInternetReceiver : BroadcastReceiver() {
    lateinit var binding : NetworkStateLytBinding

    override fun onReceive(context: Context?, intent: Intent?) {
        if(context!=null){
            if(!isOnline(context!!)){
                val alertDialog = AlertDialog.Builder(context).create()
                binding = NetworkStateLytBinding.inflate(LayoutInflater.from(context))
                binding.tryAgain.setOnClickListener {
                    if (isOnline(context!!)){
                        alertDialog.dismiss()
                    }
                }
                alertDialog.setView(binding.root)
                binding.txtNTitle.text = "please check your internet connection"
                alertDialog.show()
                alertDialog.setCancelable(false)
            }
        }
    }

    private fun isOnline(context: Context): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            netInfo != null && netInfo.isConnected
        } catch (e: NullPointerException) {
            e.printStackTrace()
            false
        }
    }
}