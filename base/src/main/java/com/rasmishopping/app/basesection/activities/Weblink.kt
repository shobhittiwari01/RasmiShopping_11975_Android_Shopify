package com.rasmishopping.app.basesection.activities

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.databinding.DataBindingUtil
import com.rasmishopping.app.MyApplication
import com.rasmishopping.app.R
import com.rasmishopping.app.databinding.MWebpageBinding
import com.rasmishopping.app.loader_section.CustomLoader
import com.rasmishopping.app.utils.Urls

class Weblink : NewBaseActivity() {
    private var webView: WebView? = null
    private var currentUrl: String? = " "
    private var binding: MWebpageBinding? = null
    private var customLoader: CustomLoader? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val content = findViewById<ViewGroup>(R.id.container)
        layoutInflater.inflate(R.layout.m_webpage, content, true)
        binding = DataBindingUtil.inflate<MWebpageBinding>(
            layoutInflater,
            R.layout.m_webpage,
            content,
            true
        )
        webView = binding!!.webview
        showBackButton()
        if (intent.hasExtra("name") && intent.getStringExtra("name") != null) {
            showTittle(intent.getStringExtra("name") ?: "")
        }
        if(intent.hasExtra("link")&&!intent.getStringExtra("link").isNullOrEmpty()){
            if (intent.getStringExtra("link")!!.contains("https") || intent.getStringExtra("link")!!
                    .contains("http")
            ) {
                Log.i("MageNative", "Link :" + intent.getStringExtra("link")!!)
                currentUrl = intent.getStringExtra("link")
            } else {
                currentUrl =
                    "https://" + Urls(MyApplication.context).shopdomain + intent.getStringExtra("link")
                Log.i("MageNative", "Link :" + intent.getStringExtra("link")!!)
            }
            customLoader = CustomLoader(this)
            customLoader?.show()
            webView!!.settings.javaScriptEnabled = true
            webView!!.settings.loadWithOverviewMode = true
            webView!!.settings.useWideViewPort = true
            setUpWebViewDefaults(webView!!)
            webView!!.loadUrl(currentUrl!!.trim({ it <= ' ' }))
            webView!!.webChromeClient = WebChromeClient()
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebViewDefaults(webView: WebView) {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.i("URL", "" + description)
                if (this@Weblink.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                    return;
                }
                customLoader?.dismiss()

            }

            override fun onLoadResource(view: WebView, url: String) {
                Log.i("URL", "" + url)
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (this@Weblink.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                    return;
                }
                customLoader?.dismiss()
                Log.i("pageURL", "" + url)
                val javascript =
                    "javascript: document.getElementsByClassName('grid--table')[0].style.display = 'none' "
                val javascript1 =
                    "javascript: document.getElementsByClassName('site-header')[0].style.display = 'none' "
                val javascript2 =
                    "javascript: document.getElementsByClassName('site-footer')[0].style.display = 'none' "
                val javascript3 =
                    "javascript: document.getElementsByClassName('ui-admin-bar__content')[0].style.display = 'none' "
                val javascript4 =
                    "javascript: document.getElementsByClassName('sweettooth-launcher-container')[0].style.display = 'none' "
                val javascript5 =
                    "javascript: document.getElementsByClassName('ui-admin-bar__body')[0].style.display = 'none' "
                val javascript6 =
                    "javascript: document.getElementsByClassName('chat-toggle chat-toggle--#202a36 chat-toggle--text-button')[0].style.display = 'none' "
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(javascript, object : ValueCallback<String> {
                        override fun onReceiveValue(value: String) {
                            Log.i("pageVALUE1", "" + value)
                        }
                    })
                    webView.evaluateJavascript(javascript1, object : ValueCallback<String> {
                        override fun onReceiveValue(value: String) {
                            Log.i("pageVALUE1", "" + value)
                        }
                    })
                    webView.evaluateJavascript(javascript2, object : ValueCallback<String> {
                        override fun onReceiveValue(value: String) {
                            Log.i("pageVALUE1", "" + value)
                        }
                    })
                    webView.evaluateJavascript(javascript3, object : ValueCallback<String> {
                        override fun onReceiveValue(value: String) {
                            Log.i("pageVALUE1", "" + value)
                        }
                    })
                    webView.evaluateJavascript(javascript4, object : ValueCallback<String> {
                        override fun onReceiveValue(value: String) {
                            Log.i("pageVALUE1", "" + value)
                        }
                    })
                    webView.evaluateJavascript(javascript5, object : ValueCallback<String> {
                        override fun onReceiveValue(value: String) {
                            Log.i("pageVALUE1", "" + value)
                        }
                    })
                    webView.evaluateJavascript(javascript6, object : ValueCallback<String> {
                        override fun onReceiveValue(value: String) {
                            Log.i("pageVALUE1", "" + value)
                        }
                    })

                } else {
                    webView.loadUrl(javascript)
                    webView.loadUrl(javascript1)
                    webView.loadUrl(javascript2)
                    webView.loadUrl(javascript3)
                    webView.loadUrl(javascript4)
                    webView.loadUrl(javascript5)
                }
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError
            ) {
                super.onReceivedSslError(view, handler, error)
                Log.i("URL", "" + error.url)
                if (this@Weblink.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                    return;
                }
                customLoader?.dismiss()
            }
        }
    }

    override fun onBackPressed() {
        if (webView!!.canGoBack()) {
            webView!!.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
