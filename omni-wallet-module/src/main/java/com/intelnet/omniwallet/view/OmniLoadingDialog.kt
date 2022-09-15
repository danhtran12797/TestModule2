package com.intelnet.omniwallet.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import com.intelnet.omniwallet.R

class OmniLoadingDialog(var callbackDismiss: (() -> Unit)?) : AppCompatDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // make white background transparent
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_loading_omni_wallet, container, false)
    }

/*    override fun onStart() {
        dialog?.let {
            val params = it.window?.attributes
            // Assign window properties to fill the parent
            params?.width = WindowManager.LayoutParams.MATCH_PARENT
            params?.height = WindowManager.LayoutParams.MATCH_PARENT
            it.window?.attributes = params as WindowManager.LayoutParams
            isCancelable = true
            super.onStart()
        } ?: kotlin.run {
            return
        }
    }*/

    override fun dismiss() {
        super.dismiss()
        callbackDismiss?.invoke()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.themeShowLoadingDialog)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callbackDismiss?.invoke()
        callbackDismiss=null
    }
}