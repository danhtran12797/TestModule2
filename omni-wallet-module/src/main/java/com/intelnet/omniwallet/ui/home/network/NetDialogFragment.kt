package com.intelnet.omniwallet.ui.home.network

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseDialogFragment
import com.intelnet.omniwallet.databinding.DialogNetBinding
import com.intelnet.omniwallet.ui.home.network.adapter.ItemNetwork
import com.intelnet.omniwallet.ui.home.network.adapter.NetworkAdapter

class NetDialogFragment(
    private var netItems: List<ItemNetwork>,
    private var chooseNetworkListener: (Int) -> Unit = { },
    private var addNetworkListener: () -> Unit = { },
) : BaseDialogFragment<DialogNetBinding>() {

    private val adapter: NetworkAdapter by lazy {
        NetworkAdapter(callBackNetwork = {
            chooseNetworkListener.invoke(it)
            this.dismiss()
        })
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogNetBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme_transparent)
    }

    override fun onStart() {
        super.onStart()
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.addAll(netItems)
        initAdapter()

        binding.imgClose.setOnClickListener {
            dismiss()
        }

        binding.btnImportNet.setOnClickListener {
            dismiss()
            addNetworkListener.invoke()
        }

        binding.imgClose.setOnClickListener {
            dismiss()
        }
    }

    private fun initAdapter(){
        binding.rvNetwork.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NetDialogFragment.adapter
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    companion object {
        fun newInstance(
            fm: FragmentManager,
            items: List<ItemNetwork>,
            chooseNetworkListener: (Int) -> Unit = { },
            addNetworkListener: () -> Unit = { }
        ) {
            val dialog = NetDialogFragment(items, chooseNetworkListener, addNetworkListener)
            dialog.show(fm, dialog.tag)
        }
    }
}