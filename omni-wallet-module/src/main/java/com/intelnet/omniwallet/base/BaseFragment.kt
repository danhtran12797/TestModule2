package com.intelnet.omniwallet.base

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_ETHERNET
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
import android.provider.Settings
import android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.intelnet.omniwallet.repository.PreferencesRepository
import com.intelnet.omniwallet.view.OmniLoadingDialog
import dagger.hilt.android.internal.Contexts.getApplication
import org.web3j.protocol.core.methods.response.Transaction
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


abstract class BaseFragment<B : ViewBinding, VM : BaseViewModel> : Fragment() {

    var dialog: DialogFragment? = null

    protected abstract val viewModel: VM

    private var _binding: B? = null
    protected val binding get() = _binding!!

    protected var firstCallApi = false

    var callBackTransactionConfirmed: (Transaction) -> Unit = {}

    val fManager: FragmentManager by lazy {
        requireActivity().supportFragmentManager
    }

    @Singleton
    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private val nameFragmentCurrent: String by lazy {
        this.findNavController().currentDestination?.label.toString()
    }

    var addressWallet: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getFragmentBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initControl()
        initUI()
        initEvent()
        initConfig()

    }

    protected abstract fun initControl()

    protected abstract fun initUI()

    protected abstract fun initEvent()

    protected abstract fun initConfig()

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): B

    override fun onDestroyView() {
        dialog?.dismiss()
        dialog = null
        _binding = null
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.d("onActivityCreated: Fragment=>$nameFragmentCurrent")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addressWallet = preferencesRepository.getAddress()
        Timber.d("onCreate: Fragment=>$nameFragmentCurrent, address: $addressWallet")

    }

    fun getListRecentlyAddress() = preferencesRepository.getRecentListAddress()

    fun setItemRecentAddress(item: String) = preferencesRepository.setRecentAddress(item)

    override fun onDestroy() {
        Timber.d("onDestroy: Fragment=>$nameFragmentCurrent")
        super.onDestroy()
    }

    fun navigate(navDirection: NavDirections, navOptions: NavOptions? = null) {
        navOptions?.let {
            this.findNavController().navigate(navDirection, it)
        } ?: kotlin.run {
            this.findNavController().navigate(navDirection)
        }
    }

    fun showLoadingDialog() {
        if (dialog == null) {
            dialog = OmniLoadingDialog {
                dialog = null
            }
            dialog!!.show(fManager, "DSBC_LOADING_DIALOG")
        }
    }

    fun hideDialog() {
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun backToPrevious(desId: Int = -1, inclusive: Boolean = false) {
        if (desId != -1)
            this.findNavController().popBackStack(desId, inclusive)
        else
            this.findNavController().popBackStack()
    }

    fun hideKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Check if no view has focus
        val currentFocusedView = requireActivity().currentFocus
        currentFocusedView?.let {
            inputMethodManager.hideSoftInputFromWindow(
                currentFocusedView.applicationWindowToken, 0
            )
        }
    }

    fun forceHideKeyboard(activity: Activity) {
        val imm =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val f = activity.currentFocus
        if (null != f && null != f.windowToken && EditText::class.java.isAssignableFrom(f.javaClass)) imm.hideSoftInputFromWindow(
            f.windowToken,
            0
        ) else activity.window
            .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    fun showKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(
            InputMethodManager.SHOW_IMPLICIT,
            0
        )
    }

    open fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication(requireContext()).getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

    open fun copyToClipboard(data: String) {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("eth-address", data)
        clipboard.setPrimaryClip(clip)
    }

    open fun showAlertDialog(
        title: String,
        content: String,
        confirmButtonTitle: String,
        cancelButtonTitle: String,
        confirmCallback: () -> Unit,
        cancelCallback: () -> Unit
    ) {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(requireContext())

        val dialog = builder
            .setTitle(title)
            .setMessage(content)
            .setCancelable(false)
            .setPositiveButton(confirmButtonTitle) { dialog, _ ->
                confirmCallback.invoke()
            }
            .setNegativeButton(cancelButtonTitle) { _, _ ->
                cancelCallback.invoke()
            }
            .show()

/*        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveButton.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.button_in_alert_dialog_color
            )
        )
        negativeButton.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.button_in_alert_dialog_color
            )
        )*/
    }

    fun openUrlTandC(url: String = "https://www.freeprivacypolicy.com/live/3b3e2ec3-76b7-4da2-822b-e20b323a8212") {
        if (url.isEmpty()) return
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(browserIntent)
        } catch (e: Exception) {
            //update late avoid user delete browser
        }
    }

    fun checkDeviceHasBiometric(validateCallback: (String) -> Unit) {
        val biometricManager = BiometricManager.from(requireActivity())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.")
                validateCallback.invoke("")
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e("MY_APP_TAG", "No biometric features available on this device.")
                validateCallback.invoke("No biometric features available on this device.")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e("MY_APP_TAG", "Biometric features are currently unavailable.")
                validateCallback.invoke("Biometric features are currently unavailable.")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                }

                startActivityForResult(enrollIntent, 100)
            }
            else -> {
                Log.e("MY_APP_TAG", "Not Thing.")
                validateCallback.invoke("")
            }
        }
    }

    open fun launchFingerprint() {
        val intent: Intent = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                Intent(Settings.ACTION_BIOMETRIC_ENROLL).putExtra(
                    EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                Intent(Settings.ACTION_FINGERPRINT_ENROLL)
            }
            else -> {
                Intent(Settings.ACTION_SECURITY_SETTINGS)
            }
        }
        try {
            startActivityForResult(intent, 100)
        } catch (error: ActivityNotFoundException) {
            startActivityForResult(Intent(Settings.ACTION_SETTINGS), 100)
        }
    }

}