package com.intelnet.omniwallet.ui.home.send.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseDialogFragment
import com.intelnet.omniwallet.databinding.DialogInforBinding

class InforDialogFragment(
    private var title: String,
    private var content: String
) : BaseDialogFragment<DialogInforBinding>() {

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogInforBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme_transparent)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtTitle.text = title
        binding.txtDesc.text = content

        binding.imgClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    companion object {
        fun newInstance(
            fm: FragmentManager,
            title: String,
            content: String
        ) {
            val dialog = InforDialogFragment(title, content)
            dialog.show(fm, dialog.tag)
        }
    }
}