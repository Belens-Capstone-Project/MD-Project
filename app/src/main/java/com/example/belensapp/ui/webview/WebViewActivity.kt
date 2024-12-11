package com.example.belensapp.ui.webview

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.belensapp.R

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        // Get the data passed from the previous activity
        val url = intent.getStringExtra("url")
        val title = intent.getStringExtra("title")
        val imageUrl = intent.getStringExtra("imageUrl")

        // Set the title for the WebViewActivity
        supportActionBar?.title = title

        // Initialize WebView and load the URL
        val webView = findViewById<WebView>(R.id.webview)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient() // To open links inside WebView instead of browser
        webView.loadUrl(url ?: "https://www.dicoding.com") // Default URL in case of null
    }
}