package com.intelnet.omniwallet.ui.home.network

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.intelnet.omniwallet.base.BaseFragment
import com.intelnet.omniwallet.databinding.FragmentAddNetworkBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddNetworkFragment : BaseFragment<FragmentAddNetworkBinding, AddNetworkViewModel>() {

    override val viewModel: AddNetworkViewModel by viewModels()

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAddNetworkBinding.inflate(inflater, container, false)

    override fun initControl() {
        binding.imgBack.setOnClickListener {
            backToPrevious()
        }
    }

    override fun initUI() {

    }

    override fun initEvent() {

    }

    override fun initConfig() {

    }

}