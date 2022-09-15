package com.intelnet.omniwallet.ui.login

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.intelnet.omniwallet.BuildConfig
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseFragment
import com.intelnet.omniwallet.databinding.FragmentLoginLaterBinding
import com.intelnet.omniwallet.ui.home.HomeActivity
import com.intelnet.omniwallet.util.Status
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.util.concurrent.Executor


@AndroidEntryPoint
class LoginLaterFragment : BaseFragment<FragmentLoginLaterBinding, LoginLaterViewModel>() {

    override val viewModel: LoginLaterViewModel by viewModels()

    private var executor: Executor? = null
    private var biometricPrompt: BiometricPrompt? = null
    private var promptInfo: BiometricPrompt.PromptInfo? = null

    private val startHome:Boolean by lazy {
        requireActivity().intent.getBooleanExtra("start_home", false)
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentLoginLaterBinding.inflate(inflater, container, false)

    override fun initControl() {
        if(startHome){
            (requireActivity() as HomeActivity).startHandlerTimeout()
            requireActivity().intent.removeExtra("start_home")
            navigate(
                LoginLaterFragmentDirections.actionLoginLaterFragmentToHomeFragment()
            )
        }

        binding.btnUnlock.setOnClickListener {
            val pass = binding.edtPass.text.toString()
            if (validateForm(pass))
                viewModel.loadCredentials(pass)
        }

        binding.txtResetWallet.setOnClickListener {
            showAlertDialog(
                title = "",
                content = getString(R.string.content_reset_wallet),
                confirmButtonTitle = "Ok",
                cancelButtonTitle = "Hủy",
                confirmCallback = {

                    preferencesRepository.clearData()

                    if(deleteDir(File(requireActivity().filesDir, "")))
                        (requireActivity() as HomeActivity).navigateAddWalletActivity()

                },
                cancelCallback = {

                }
            )
        }

        binding.imgFinger.setOnClickListener {
            initBiometric()
            biometricPrompt?.authenticate(promptInfo!!)
        }

        binding.swDefault.isChecked = preferencesRepository.isRememberLogin()
    }

    private fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val childrens = dir.list() ?: return false
            for (i in childrens.indices) {
                val success = deleteDir(File(dir, childrens[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir.delete()
    }

    private fun initBiometric(){
        if(executor==null){
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

                        if (errorCode == 11) {
                            showAlertDialog(
                                title = "",
                                content = errString.toString(),
                                confirmButtonTitle = "setting",
                                cancelButtonTitle = "cancel",
                                confirmCallback = {
                                    launchFingerprint()
                                },
                                cancelCallback = {
                                    binding.swDefault.isChecked = false
                                    preferencesRepository.setRememberLogin(false)
                                }
                            )
                        } else{
                            showToast("$errString")
                            binding.swDefault.isChecked = false
                            preferencesRepository.setRememberLogin(false)
                        }
                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult,
                    ) {
                        super.onAuthenticationSucceeded(result)
                        Timber.d("Authentication succeeded!")
                        preferencesRepository.setRememberLogin(true)
                        (requireActivity() as HomeActivity).startHandlerTimeout()
                        navigate(
                            LoginLaterFragmentDirections.actionLoginLaterFragmentToHomeFragment()
                        )
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

    override fun initUI() {
        if (BuildConfig.DEBUG) {
            binding.edtPass.setText(getString(R.string.password_demo))
        }

        if (preferencesRepository.isRememberLogin()&&!startHome) {
            initBiometric()
            biometricPrompt?.authenticate(promptInfo!!)
        }
    }

    override fun initEvent() {
        viewModel.loginLiveData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { data ->
                when (data.responseType) {
                    Status.LOADING -> {
                        showLoadingDialog()
                    }
                    Status.SUCCESSFUL -> {
                        hideDialog()
                        data.data?.let { address ->

                            (requireActivity() as HomeActivity).startHandlerTimeout()
                            preferencesRepository.setRememberLogin(binding.swDefault.isChecked)
                            preferencesRepository.setAddress(address)

                            navigate(
                                LoginLaterFragmentDirections.actionLoginLaterFragmentToHomeFragment(
                                    address
                                )
                            )
                        }
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
    }

    override fun initConfig() {

    }

    private fun validateForm(pass: String): Boolean {
        if (pass.isBlank()) {
            showToast("Vui lòng nhập mật khẩu!")
            return false
        }
        if (pass.length < 8) {
            showToast("Mật khẩu phải ít nhất 8 ký tự!")
            return false
        }

        return true
    }


    override fun onDestroy() {
        biometricPrompt = null
        promptInfo = null
        executor = null
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("requestCode: $requestCode")
        Timber.d("resultCode: $resultCode")

        if (requestCode == 100 && (resultCode == Activity.DEFAULT_KEYS_SEARCH_LOCAL || resultCode == Activity.RESULT_FIRST_USER || resultCode == Activity.RESULT_OK)) {
            Timber.d("GOOD JOB")
            binding.swDefault.isChecked = true
        } else{
            binding.swDefault.isChecked = false
            preferencesRepository.setRememberLogin(false)
        }

    }

}