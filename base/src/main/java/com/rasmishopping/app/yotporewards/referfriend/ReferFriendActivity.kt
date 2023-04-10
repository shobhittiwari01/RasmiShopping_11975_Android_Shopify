package com.rasmishopping.app.yotporewards.referfriend

import android.app.Dialog
import android.content.ClipData
import android.content.Context
import android.os.Bundle
import android.text.ClipboardManager
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.basesection.activities.NewBaseActivity
import com.rasmishopping.app.databinding.ActivityReferFriendBinding
import com.rasmishopping.app.databinding.ReferfriendDialogBinding
import com.rasmishopping.app.utils.ApiResponse
import com.rasmishopping.app.utils.ViewModelFactory
import kotlinx.android.synthetic.main.activity_refer_friend.*
import javax.inject.Inject

class ReferFriendActivity : NewBaseActivity() {
    private var binding: ActivityReferFriendBinding? = null
    private var model: ReferFriendViewModel? = null
    var dialog: Dialog? = null

    @Inject
    lateinit var factory: ViewModelFactory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val group = findViewById<ViewGroup>(R.id.container)
        binding =
            DataBindingUtil.inflate(layoutInflater, R.layout.activity_refer_friend, group, true)
        (application as MyApplication).mageNativeAppComponent!!.doReferFriendInjection(this)
        model = ViewModelProvider(this, factory).get(ReferFriendViewModel::class.java)
        model?.context = this
        showBackButton()
        showTittle(getString(R.string.refer_friends))
        model?.referfriend?.observe(this, Observer { this.consumeSendReferral(it) })
        binding?.emailBut?.setOnClickListener {
            openEmailDialog()
        }
    }
    fun copyText(v:View){
        clipBoard(referral_link.text.toString())
    }

    private fun clipBoard(text: CharSequence){
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText("Copy Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(applicationContext, "Copied", Toast.LENGTH_SHORT).show()

    }

    private fun consumeSendReferral(response: ApiResponse?) {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
            Toast.makeText(this, getString(R.string.mail_sent), Toast.LENGTH_SHORT).show()
        }

    }

    private fun openEmailDialog() {
        dialog = Dialog(this, R.style.WideDialog)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        var referfriendDialogBinding = DataBindingUtil.inflate<ReferfriendDialogBinding>(
            layoutInflater,
            R.layout.referfriend_dialog,
            null,
            false
        )
        dialog?.setContentView(referfriendDialogBinding.root)
        referfriendDialogBinding.sendemailBut.setOnClickListener {
            if (TextUtils.isEmpty(referfriendDialogBinding.emailEdt.text.toString().trim())) {
                referfriendDialogBinding.emailEdt.error = getString(R.string.email_validation)
                referfriendDialogBinding.emailEdt.requestFocus()
            } else {
                model?.sendReferral(referfriendDialogBinding.emailEdt.text.toString().trim())
            }
        }
        referfriendDialogBinding.closeBut.setOnClickListener {
            dialog?.dismiss()
        }
        dialog?.show()

    }
}