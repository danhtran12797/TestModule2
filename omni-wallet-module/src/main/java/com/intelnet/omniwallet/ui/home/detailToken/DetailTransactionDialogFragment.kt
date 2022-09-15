package com.intelnet.omniwallet.ui.home.detailToken

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseDialogFragment
import com.intelnet.omniwallet.databinding.DialogTransactionDetailBinding
import com.intelnet.omniwallet.ui.home.detailToken.adapter.ItemHistoryTokenAdapter
import com.intelnet.omniwallet.ui.home.detailToken.adapter.ItemTransaction
import com.intelnet.omniwallet.util.BalanceUtil
import com.intelnet.omniwallet.util.formatAddressWallet
import java.math.BigDecimal

class DetailTransactionDialogFragment(
    private var item: ItemTransaction,
    private var viewListener: () -> Unit = { }
) : BaseDialogFragment<DialogTransactionDetailBinding>() {

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogTransactionDetailBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme_transparent)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initControl()
        initUI()
    }

    private fun initUI() {

        binding.txtTotalAmount.isSelected = true

        val pair = if (item.status != ItemHistoryTokenAdapter.FAILED)
            Pair(getString(R.string.confirmed), R.color.green_1)
        else
            Pair(getString(R.string.failed), R.color.red1)

        binding.txtStatus.text = pair.first
        binding.txtStatus.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                pair.second
            )
        )

        binding.txtChooseNetwork.text =
            if (item.status != ItemHistoryTokenAdapter.RECEIVED) getString(
                R.string.sent_to,
                item.symbol
            ) else getString(R.string.receive_from, item.symbol)

        binding.txtDateTime.text = item.formatDateTime

        binding.txtFrom.text = item.from.formatAddressWallet()
        binding.txtTo.text = item.to.formatAddressWallet()

        binding.txtNonce.text = StringBuilder("#").append(item.nonce)

        binding.txtAmount.text = BalanceUtil.formatBalanceWithSymbol(item.amount, item.symbol)

        binding.txtGas.text = BalanceUtil.formatBalanceWithSymbol(item.estimate, item.symbolNative)

        binding.txtTotalAmount.text = if (item.symbol == item.symbolNative) {
            val total = BigDecimal(item.amount) + BigDecimal(item.estimate)
            BalanceUtil.formatBalanceWithSymbol(total.toPlainString(), item.symbolNative)
        } else {
            StringBuilder(item.amount).append(" ${item.symbol}").append(" / ").append(item.estimate)
                .append(" ${item.symbolNative}")
        }

        binding.group.isVisible = item.status != ItemHistoryTokenAdapter.RECEIVED
    }


    private fun initControl() {
        binding.btnViewTestnet.setOnClickListener {
            viewListener.invoke()
            dismiss()
        }

        binding.imgClose.setOnClickListener {
            dismiss()
        }
    }


    companion object {
        fun newInstance(
            fm: FragmentManager,
            item: ItemTransaction,
            viewListener: () -> Unit = { }
        ) {
            val dialog = DetailTransactionDialogFragment(item, viewListener)
            dialog.show(fm, dialog.tag)
        }
    }
}