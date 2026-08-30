package com.guardianapp.videoplayer

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class YouTubeActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube)

        webView = findViewById(R.id.webView)
        setupWebView()
    }

    private fun setupWebView() {
        webView.apply {
            webViewClient = WebViewClient()
            settings.apply {
                javaScriptEnabled = true
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                databaseEnabled = true
                domStorageEnabled = true
            }
            loadUrl("https://www.youtube.com")
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
