package com.intelnet.omniwallet.ui.addWallet.createWallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseFragment
import com.intelnet.omniwallet.base.EmptyViewModel
import com.intelnet.omniwallet.databinding.FragmentMemorizePhraseBinding
import com.intelnet.omniwallet.ui.addWallet.AddWalletActivity
import com.intelnet.omniwallet.ui.addWallet.createWallet.adapter.MemorizePhraseAdapter
import com.intelnet.omniwallet.util.dpToPx
import com.intelnet.omniwallet.view.GridSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.regex.Pattern


@AndroidEntryPoint
class MemorizePhraseFragment :
    BaseFragment<FragmentMemorizePhraseBinding, EmptyViewModel>() {

    override val viewModel: EmptyViewModel by viewModels()

    private val args: MemorizePhraseFragmentArgs by navArgs()

    private val adapter = MemorizePhraseAdapter()

    private val lstWord: List<String> by lazy {
        Pattern.compile(" ").split(args.wordPhrase).toList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if ((requireActivity() as AddWalletActivity).deleteDir(
                        File(
                            requireContext().filesDir,
                            ""
                        )
                    )
                ) {
                    backToPrevious()
                }
            }
        })
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMemorizePhraseBinding.inflate(inflater, container, false)

    override fun initControl() {
        binding.btnContinue.setOnClickListener {
            navigate(
                MemorizePhraseFragmentDirections.actionMemorizePhraseFragmentToConfirmPhraseFragment(
                    seedCode = args.wordPhrase,
                    address = args.address
                )
            )
        }

        binding.txtCopyPhraseWord.setOnClickListener {
            copyToClipboard(args.wordPhrase)
            showToast(getString(R.string.toast_word_phrase_copied))
        }
    }

    override fun initUI() {
        binding.rvMemorizePhrase.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            if (itemDecorationCount == 0)
                addItemDecoration(GridSpacingItemDecoration(2, 16.dpToPx, true, 0))
            adapter = this@MemorizePhraseFragment.adapter.also {
                it.addAll(
                    lstWord
                )
            }
        }
    }

    override fun initEvent() {

    }

    override fun initConfig() {

    }

    override fun onDestroyView() {
        binding.rvMemorizePhrase.adapter = null
        super.onDestroyView()
    }

}