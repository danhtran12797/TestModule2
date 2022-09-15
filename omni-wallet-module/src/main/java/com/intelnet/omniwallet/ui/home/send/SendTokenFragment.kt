package com.intelnet.omniwallet.ui.home.send

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intelnet.omniwallet.view.Identicon
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseFragment
import com.intelnet.omniwallet.databinding.FragmentSendTokenBinding
import com.intelnet.omniwallet.ui.AnyOrientationCaptureActivity
import com.intelnet.omniwallet.ui.home.HomeViewModel
import com.intelnet.omniwallet.ui.home.send.adapter.AddressRecentlyAdapter
import com.intelnet.omniwallet.util.formatAddressWallet
import com.intelnet.omniwallet.util.getStringAddressFromScan
import com.google.android.material.textfield.TextInputLayout
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import org.web3j.crypto.WalletUtils
import timber.log.Timber


@AndroidEntryPoint
class SendTokenFragment : BaseFragment<FragmentSendTokenBinding, HomeViewModel>(),
    View.OnClickListener {

    //    override val viewModel: SendTokenViewModel by viewModels()
    override val viewModel: HomeViewModel by activityViewModels()

    private val args: SendTokenFragmentArgs by navArgs()

    private var toAddress: String? = null

    private val adapter: AddressRecentlyAdapter by lazy {
        AddressRecentlyAdapter(
            callBackItemClick = {
                setTextInputFromAddress(it)
            }
        )
    }

    // Register the launcher and result handler
    private val qrcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            showToast("Cancelled")
        } else {
            Timber.d("content: ${result.contents}")
            val address = result.contents.getStringAddressFromScan()
            Timber.d("format: $address")
            if (WalletUtils.isValidAddress(address)) {
                setTextInputFromAddress(address)
            } else
                showToast("Not Address")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toAddress = args.address
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSendTokenBinding.inflate(inflater, container, false)

    override fun initControl() {

        binding.imgBack.setOnClickListener {
            backToPrevious()
        }

        binding.btnContinue.setOnClickListener {
            if(toAddress==null)
                return@setOnClickListener
            setItemRecentAddress(toAddress!!)
            navigate(
                SendTokenFragmentDirections.actionSendTokenFragmentToAmountFragment(
                    toAddress!!,
                    args.indexToken
                )
            )
        }

        binding.edtSearchAddress.addTextChangedListener { text: Editable? ->
            if (text == null)
                return@addTextChangedListener
            if (text.isEmpty()) {
                binding.tiySearchAddress.apply {
                    endIconMode = TextInputLayout.END_ICON_CUSTOM
                    endIconDrawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_baseline_qr_code_scanner_24
                    )
                    setEndIconOnClickListener(null)
                    setEndIconOnClickListener(this@SendTokenFragment)
                }
            } else {
                binding.tiySearchAddress.apply {
                    endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                }
                val txt = text.toString().trim()
                if (WalletUtils.isValidAddress(txt)) {
                    initUiToAddress(txt)
                }
            }
        }

    }

    private fun setTextInputFromAddress(text: String) {
        binding.edtSearchAddress.setText(text)
    }

    override fun initUI() {
        initUiFromAddress()
        initUiToAddress(toAddress)
        initDefaultNetwork()

        binding.tiySearchAddress.apply {
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            endIconDrawable = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_baseline_qr_code_scanner_24
            )
            setEndIconOnClickListener(this@SendTokenFragment)
        }

        binding.rvRecently.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SendTokenFragment.adapter.also {
                it.addAll(getListRecentlyAddress())
            }
            addItemDecoration(
                DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
            )
        }

        binding.imgDeleteFill.setOnClickListener {
            initUiToAddress(null)
        }
    }

    private fun initDefaultNetwork() {
        val itemNetwork = viewModel.getItemNetworkDefault()
        itemNetwork?.let {
            binding.txtDot.setBackgroundColor(it.color)
            binding.txtNet.text = it.name
        }
    }

    private fun initUiFromAddress() {
        val balanceFormat = StringBuilder().append(viewModel.balanceETH)
            .append(" ${viewModel.getSymbolNetworkDefault()}").toString()
        binding.txtBalance.text = getString(R.string.content_balance_from_send, balanceFormat)
        Identicon(
            binding.imgAvatarFrom,
            addressWallet
        )
    }

    private fun initUiToAddress(address: String?) {
        toAddress = address // set toAddress
        toAddress?.run {
            Identicon(binding.imgAvatarTo, this)
            binding.layoutFill.isVisible = true
            binding.tiySearchAddress.visibility = View.INVISIBLE
            binding.txtAddressFill.text = this.formatAddressWallet(12)
            binding.btnContinue.isEnabled = true
        } ?: kotlin.run {
            binding.edtSearchAddress.setText("")
            binding.layoutFill.isVisible = false
            binding.tiySearchAddress.visibility = View.VISIBLE
            binding.btnContinue.isEnabled = false
        }
    }

    override fun initEvent() {

    }

    override fun initConfig() {

    }

    override fun onDestroyView() {
        binding.rvRecently.adapter = null
        super.onDestroyView()
    }

    override fun onClick(p0: View?) {
        qrcodeLauncher.launch(ScanOptions().apply {
            captureActivity = AnyOrientationCaptureActivity::class.java
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt("đang quét...")
            setBeepEnabled(false)
            setOrientationLocked(false)
        })
    }

}