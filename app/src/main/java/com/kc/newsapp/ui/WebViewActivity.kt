package com.kc.newsapp.ui

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kc.newsapp.R
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
        }

        webview.loadUrl(url)
    }
}