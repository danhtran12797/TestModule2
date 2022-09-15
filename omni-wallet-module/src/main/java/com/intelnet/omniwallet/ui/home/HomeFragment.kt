package com.intelnet.omniwallet.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intelnet.omniwallet.view.Identicon
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseFragment
import com.intelnet.omniwallet.databinding.FragmentHomeBinding
import com.intelnet.omniwallet.ui.AnyOrientationCaptureActivity
import com.intelnet.omniwallet.ui.home.adapter.ItemToken
import com.intelnet.omniwallet.ui.home.adapter.ItemTokenAdapter
import com.intelnet.omniwallet.ui.home.detailToken.ChooseMenuDialogFragment
import com.intelnet.omniwallet.ui.home.detailToken.adapter.ItemMenu
import com.intelnet.omniwallet.ui.home.network.NetDialogFragment
import com.intelnet.omniwallet.util.*
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import org.web3j.crypto.WalletUtils
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.Executor


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override val viewModel: HomeViewModel by activityViewModels()

    private val args: HomeFragmentArgs by navArgs()

    private var address: String? = null

    private var count = 0

    private var executor: Executor? = null
    private var biometricPrompt: BiometricPrompt? = null
    private var promptInfo: BiometricPrompt.PromptInfo? = null

    private var listener: CompoundButton.OnCheckedChangeListener =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(!buttonView.isPressed)
                return@OnCheckedChangeListener
            count++
            if (isChecked) {
                if (count > 1) {
                    showToast(getString(R.string.please_login_again))
                    binding.swDefault.isChecked = false
                } else {
                    checkDeviceHasBiometric(
                        validateCallback = {
                            if (it.isNotEmpty()) {
                                showToast(it)
                                count--
                                binding.swDefault.isChecked = false
                            } else
                                biometricPrompt?.authenticate(promptInfo!!)
                        }
                    )
                }
            } else {
                showAlertDialog(
                    title = "",
                    content = getString(R.string.turn_off_biometric),
                    confirmButtonTitle = "ok",
                    cancelButtonTitle = "cancel",
                    confirmCallback = {
                        preferencesRepository.setRememberLogin(false)
                    },
                    cancelCallback = {
                        count--
                        binding.swDefault.isChecked = true
                    }
                )
            }
        }

    private val callBackToken: (Int, ItemToken) -> Unit = { i, data ->
        Timber.d("Item Click $i")
        navigate(
            HomeFragmentDirections.actionHomeFragmentToDetailTokenFragment(i)
        )
    }
    private val callBackItemLongClick: (Int, ItemToken) -> Unit = { i, data ->
        Timber.d("Item Long Click $i")
        if (i != 0) {
            ChooseMenuDialogFragment.newInstance(
                fManager,
                listOf(ItemMenu(getString(R.string.hide_token), ItemMenu.ACTION_HIDE_TOKEN)),
                callbackAction = {
                    when (it) {
                        ItemMenu.ACTION_HIDE_TOKEN -> {
                            preferencesRepository.hideTokenAddress(
                                i - 1,
                                viewModel.getSymbolNetworkDefault()
                            )
                            viewModel.refresh()
                        }
                        else -> {}
                    }
                }
            )
        }
    }
    private val callBackImportToken: () -> Unit = {
        navigate(
            HomeFragmentDirections.actionHomeFragmentToAddTokenFragment()
        )
    }

    private val tokenAdapter: ItemTokenAdapter by lazy {
        ItemTokenAdapter(
            mutableListOf(),
            callBackToken,
            callBackImportToken,
            callBackItemLongClick
        )
    }

    // Register the launcher and result handler
    private val qrcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            showToast("Cancelled")
        } else {
            Timber.d("contents: ${result.contents}")
            val addressFormat = result.contents.getStringAddressFromScan()
            if (WalletUtils.isValidAddress(addressFormat))
                navigate(
                    HomeFragmentDirections.actionHomeFragmentToSendTokenFragment(0, addressFormat)
                )
            else
                showToast(getString(R.string.toast_no_address_public))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        address = args.address ?: preferencesRepository.getAddress()
        address?.let {
            viewModel.loadBalance(it)
        }

    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    override fun initControl() {

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.txtAddress.setOnClickListener {
            address?.let {
                copyToClipboard(it)
                showToast(getString(R.string.toast_address_copied))
            }
        }

        // fade click anim
/*        binding.txtAddress.setOnTouchListener { v, action ->
            when (action.action) {
                MotionEvent.ACTION_UP -> {
                    binding.txtAddress.alpha = 1f
                    false
                }
                MotionEvent.ACTION_DOWN -> {
                    binding.txtAddress.alpha = 0.5f
                    false
                }
                else -> false
            }
        }*/

        binding.viewClickNetWork.setOnClickListener {
            NetDialogFragment.newInstance(
                fManager,
                viewModel.lstItemNetwork,
                chooseNetworkListener = {
                    if (it == -1)
                        return@newInstance
                    viewModel.setDefaultNetworkInfo(it)
                    setUiDefaultNetWork()
                    viewModel.refresh()
                },
                addNetworkListener = {
                    navigate(
                        HomeFragmentDirections.actionHomeFragmentToAddNetworkFragment()
                    )
                }
            )
        }

        binding.viewReceive.setOnClickListener {
            navigate(
                HomeFragmentDirections.actionHomeFragmentToReceiveTokenDialogFragment(address ?: "")
            )
        }

        binding.viewSend.setOnClickListener {
            navigate(
                HomeFragmentDirections.actionHomeFragmentToSendTokenFragment(indexToken = 0)
            )
        }

        binding.viewSwap.setOnClickListener {
            showToast("Action Swap")
        }

        binding.imgScan.setOnClickListener {
            qrcodeLauncher.launch(ScanOptions().apply {
                captureActivity = AnyOrientationCaptureActivity::class.java
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("đang quét...")
                setBeepEnabled(false)
                setOrientationLocked(false)
            })
        }

        binding.txtLock.setOnClickListener {
            showAlertDialog(
                title = "",
                content = getString(R.string.lock_title),
                confirmButtonTitle = "CÓ",
                cancelButtonTitle = "KHÔNG",
                confirmCallback = {
                    preferencesRepository.setRememberLogin(false)
                    (requireActivity() as HomeActivity).backToLoginScreen()
                },
                cancelCallback = {

                }
            )
        }

        binding.swDefault.setCustomChecked(preferencesRepository.isRememberLogin(), listener)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("requestCode: $requestCode")
        Timber.d("resultCode: $resultCode")

        if (requestCode == 100 && (resultCode == Activity.DEFAULT_KEYS_SEARCH_LOCAL||resultCode == Activity.RESULT_FIRST_USER||resultCode == Activity.RESULT_OK)){
            Timber.d("GOOD JOB")
            preferencesRepository.setRememberLogin(true)
        }
        else{
            count--
            binding.swDefault.setCustomChecked(false, listener)
        }
    }

    override fun initUI() {
        initAddressWallet()
        initBalanceWallet(viewModel.balanceETH)
        setUiDefaultNetWork()

//        initBiometric()

        binding.rvToken.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tokenAdapter
            addItemDecoration(
                DividerItemDecoration(requireContext(), RecyclerView.VERTICAL)
            )
        }

        binding.swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.blue500
            )
        )

    }

    private fun initBiometric() {
        if (executor == null) {
            executor = ContextCompat.getMainExecutor(requireContext())
            biometricPrompt = BiometricPrompt(this, executor!!,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence,
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        Timber.d("Error code: $errorCode")
                        Timber.d("Authentication error: $errString")

                        if (errorCode != 10 && errorCode != 13) {
                            if(errorCode==11){
                                showAlertDialog(
                                    title = "",
                                    content = errString.toString(),
                                    confirmButtonTitle = "setting",
                                    cancelButtonTitle = "cancel",
                                    confirmCallback = {
                                        launchFingerprint()
                                    },
                                    cancelCallback = {
                                        count--
                                        binding.swDefault.setCustomChecked(false, listener)
                                    }
                                )
                            }else{
                                showToast("$errString")
                                count--
                                binding.swDefault.setCustomChecked(false, listener)
                            }
                        }else{
                            count--
                            binding.swDefault.setCustomChecked(false, listener)
                        }
                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult,
                    ) {
                        super.onAuthenticationSucceeded(result)
                        Timber.d("Authentication succeeded!")
                        showToast("Authentication succeeded!")
                        preferencesRepository.setRememberLogin(true)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Timber.d("Authentication failed")
                    }
                })

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build()
        }
    }

    private fun setUiDefaultNetWork() {
        val itemNetwork = viewModel.getItemNetworkDefault()
        itemNetwork?.let {
            binding.txtDot.setBackgroundColor(it.color)
            binding.txtNet.text = it.name
        }
    }

    private fun initAddressWallet() {
        address?.let { it ->
            Timber.d("Address home: $it")

            binding.txtAddress.text = it.formatAddressWallet()
            Identicon(binding.imgAvatarWallet, it)

        }
    }

    private fun initBalanceWallet(ethBalance: BigDecimal) {
        binding.txtAmount.text = BalanceUtil.formatBalanceWithSymbol(
            ethBalance.toString().trimTrailingZero(),
            viewModel.getSymbolNetworkDefault()
        )
    }

    override fun initEvent() {
        viewModel.fetchLiveData.observe(viewLifecycleOwner) {
            when (it.responseType) {
                Status.ERROR -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                }

                Status.LOADING -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                }

                Status.SUCCESSFUL -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        viewModel.addressLiveData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { data ->
                when (data.responseType) {
                    Status.LOADING -> {
                        showLoadingDialog()
                    }
                    Status.SUCCESSFUL -> {
                        hideDialog()
//                        initAddressWallet()
                    }
                    Status.ERROR -> {
                        hideDialog()
                        data.error?.message?.let {
                            showToast(it)
                        }
                    }
                }
            }
        }

        viewModel.balanceLiveData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { data ->
                initBalanceWallet(data)
            }
        }

        viewModel.lstTokenLiveData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { lstItemToken ->
                tokenAdapter.addAll(lstItemToken)
            }
        }
    }

    override fun initConfig() {
        getNavigationResult<Int>(
            R.id.homeFragment,
            "network_change"
        ) {
            tokenAdapter.clearAll()
            viewModel.refresh()
        }
    }

    override fun onDestroyView() {
        binding.rvToken.adapter = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        biometricPrompt = null
        promptInfo = null
        executor = null
        super.onDestroy()
    }

}