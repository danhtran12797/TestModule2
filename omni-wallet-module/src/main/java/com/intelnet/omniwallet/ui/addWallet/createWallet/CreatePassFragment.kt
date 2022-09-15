package com.intelnet.omniwallet.ui.addWallet.createWallet

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
import com.intelnet.omniwallet.databinding.FragmentCreatePassBinding
import com.intelnet.omniwallet.ui.addWallet.AddWalletViewModel
import com.intelnet.omniwallet.util.Status
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.Executor

@AndroidEntryPoint
class CreatePassFragment : BaseFragment<FragmentCreatePassBinding, AddWalletViewModel>() {

    override val viewModel: AddWalletViewModel by viewModels()

    private var executor: Executor? = null
    private var biometricPrompt: BiometricPrompt? = null
    private var promptInfo: BiometricPrompt.PromptInfo? = null

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentCreatePassBinding.inflate(inflater, container, false)

    override fun initControl() {
        binding.btnCreatePass.setOnClickListener {
            val pass = binding.edtNewPass.text.toString().trim()
            val passConfirm = binding.edtConfirmPass.text.toString().trim()
            val remember = binding.swDefault.isChecked
            if (validateForm(pass, passConfirm))
                viewModel.createWallet(passConfirm, remember)
        }

        binding.swDefault.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                checkDeviceHasBiometric(
                    validateCallback = {
                        if (it.isNotEmpty()) {
                            showToast(it)
                            binding.swDefault.isChecked = false
                        } else
                            biometricPrompt?.authenticate(promptInfo!!)
                    }
                )
            }
        }
    }

    override fun initUI() {
        if(BuildConfig.DEBUG){
            binding.edtNewPass.setText(getString(R.string.password_demo))
            binding.edtConfirmPass.setText(getString(R.string.password_demo))
        }

        initBiometric()
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
                                }
                            )
                        } else{
                            showToast("$errString")
                            binding.swDefault.isChecked = false
                        }

                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult,
                    ) {
                        super.onAuthenticationSucceeded(result)
                        Timber.d("Authentication succeeded!")
                        showToast("Authentication succeeded!")
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

    override fun initEvent() {
        viewModel.phraseLiveData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { data ->
                when (data.responseType) {
                    Status.LOADING -> {
                        showLoadingDialog()
                    }
                    Status.SUCCESSFUL -> {
                        hideDialog()
                        data.data?.let { pair ->
                            showToast(pair.second)

                            navigate(
                                CreatePassFragmentDirections.actionCreatePassFragmentToMemorizePhraseFragment(
                                    wordPhrase = pair.second,
                                    address = pair.first
                                )
                            )
                        }
                    }
                    Status.ERROR -> {
                        hideDialog()
                    }
                }
            }
        }
    }

    override fun initConfig() {

    }

    private fun validateForm(pass: String, passConfirm: String): Boolean {
        if (pass.isBlank() || passConfirm.isBlank()) {
            showToast("Vui lòng nhập mật khẩu!")
            return false
        }
        if (pass.length < 8) {
            showToast("Mật khẩu phải ít nhất 8 ký tự!")
            return false
        }
        if (pass != passConfirm) {
            showToast("Xác nhận mật khẩu không đúng!")
            return false
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("requestCode: $requestCode")
        Timber.d("resultCode: $resultCode")

        if (requestCode == 100 && (resultCode == Activity.DEFAULT_KEYS_SEARCH_LOCAL || resultCode == Activity.RESULT_FIRST_USER || resultCode == Activity.RESULT_OK)) {
            Timber.d("GOOD JOB")
        } else
            binding.swDefault.isChecked = false
    }

    override fun onDestroy() {
        biometricPrompt = null
        promptInfo = null
        executor = null
        super.onDestroy()
    }

}