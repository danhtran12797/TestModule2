package com.intelnet.omniwallet.ui.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.intelnet.omniwallet.base.BaseFragment
import com.intelnet.omniwallet.base.EmptyViewModel
import com.intelnet.omniwallet.databinding.FragmentWebviewBinding
import com.intelnet.omniwallet.util.getHostName
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WebViewFragment : BaseFragment<FragmentWebviewBinding, EmptyViewModel>() {

    override val viewModel: EmptyViewModel by viewModels()

    private val args:WebViewFragmentArgs by navArgs()

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentWebviewBinding.inflate(inflater, container, false)

    override fun initControl() {
        binding.imgBack.setOnClickListener {
            backToPrevious()
        }

        binding.imgShare.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, args.url)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun initUI() {
        binding.txtTitleToolbar.text = args.url.getHostName()

        showLoadingDialog()
        binding.webView.apply {
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    hideDialog()
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    url: String
                ): Boolean {
                    return false
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    hideDialog()
                }
            }
        }
        binding.webView.loadUrl(args.url)
    }

    override fun initEvent() {

    }

    override fun initConfig() {

    }

}