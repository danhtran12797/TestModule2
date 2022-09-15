package com.intelnet.omniwallet.ui.addWallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.intelnet.omniwallet.base.BaseFragment
import com.intelnet.omniwallet.base.EmptyViewModel
import com.intelnet.omniwallet.databinding.FragmentAddWalletBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddWalletFragment : BaseFragment<FragmentAddWalletBinding, EmptyViewModel>() {

    override val viewModel: EmptyViewModel by viewModels()

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAddWalletBinding.inflate(inflater, container, false)

    override fun initControl() {
        binding.btnAddWallet.setOnClickListener {
            navigate(
                AddWalletFragmentDirections.actionAddWalletFragmentToCreatePassFragment()
            )
        }

        binding.btnImportPrivate.setOnClickListener {
            navigate(
                AddWalletFragmentDirections.actionAddWalletFragmentToImportKeyFragment()
            )
        }

        binding.btnImportPhrase.setOnClickListener {
            navigate(
                AddWalletFragmentDirections.actionAddWalletFragmentToImportPhraseFragment()
            )
        }

        binding.txtTAndC.setOnClickListener {
            openUrlTandC()
        }
    }

    override fun initUI() {

    }

    override fun initEvent() {

    }

    override fun initConfig() {

    }

}