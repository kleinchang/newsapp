package com.kc.newsapp.ui

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.*
import com.kc.newsapp.R
import com.kc.newsapp.util.hide
import com.kc.newsapp.util.show
import kotlinx.android.synthetic.main.activity_webview.*


/**
 * Created by changk on 1/19/18.
 */
class WebViewActivity : AppCompatActivity() {

    companion object {
        const val KEY_URL = "url"

        fun open(context: Context, url: String) {
            val repositoryDetailsIntent = Intent(context, WebViewActivity::class.java)
            repositoryDetailsIntent.putExtra(KEY_URL, url)
            context.startActivity(repositoryDetailsIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: no loading indicator at the moment
        setContentView(R.layout.activity_webview)

        val url = intent.extras.getString(KEY_URL)
        webview.webViewClient = object : WebViewClient() {

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return true
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progress.show()
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                progress.hide()
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                super.onReceivedSslError(view, handler, error)
                progress.hide()
            }
        }

        webview.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                log("Progress $newProgress")
                if (newProgress > 40)
                    progress.hide()
            }
        }

        webview.settings.apply {
            javaScriptEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
        }

        webview.loadUrl(url)
    }
}