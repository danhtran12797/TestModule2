package com.intelnet.omniwallet.ui.home.send.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.intelnet.omniwallet.R
import com.intelnet.omniwallet.base.BaseBottomSheetFragment
import com.intelnet.omniwallet.databinding.DialogEditGasBinding
import com.intelnet.omniwallet.util.BalanceUtil
import com.intelnet.omniwallet.util.trimTrailingZero
import org.web3j.utils.Convert
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger

class EditGasDialogFragment(
    private val symbol: String,
    private var gasPrice: BigInteger,
    private var gasLimit: BigInteger,
    private var callbackSave: (BigInteger, BigInteger) -> Unit
) : BaseBottomSheetFragment<DialogEditGasBinding>() {

    private val weiFactor: BigDecimal by lazy {
        Convert.Unit.GWEI.weiFactor
    }

    private val countDown: BigInteger by lazy {
        BigInteger("1000")
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = DialogEditGasBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initControl()
        initUI()
    }

    private fun initControl() {
        binding.imgClose.setOnClickListener {
            dismiss()
        }

        binding.txtLabelEstimateGas.setOnClickListener {
            InforDialogFragment.newInstance(
                fm = childFragmentManager,
                title = getString(R.string.estimate_gas),
                content = getString(R.string.learn_more_gas_limit)
            )
        }

        binding.txtLabelPriceGas.setOnClickListener {
            InforDialogFragment.newInstance(
                fm = childFragmentManager,
                title = getString(R.string.gas_price),
                content = getString(R.string.learn_more_gas_price)
            )
        }

        binding.imgRemove.setOnClickListener {
            if (gasLimit.minus(countDown) < GAS_LIMIT) {
                return@setOnClickListener
            }
            gasLimit = gasLimit.minus(countDown)
            loadData()
        }

        binding.imgAdd.setOnClickListener {
            gasLimit = gasLimit.plus(countDown)
            loadData()
        }

        binding.imgRemove2.setOnClickListener {
            if (gasPrice.minus(weiFactor.toBigInteger()) < BigInteger.ZERO) {
                return@setOnClickListener
            }
            gasPrice = gasPrice.minus(weiFactor.toBigInteger())
            loadData()
        }

        binding.imgAdd2.setOnClickListener {
            gasPrice = gasPrice.plus(weiFactor.toBigInteger())
            loadData()
        }

        binding.btnSave.setOnClickListener {
            callbackSave.invoke(
                gasPrice,
                gasLimit
            )
            dismiss()
        }
    }

    private fun initUI() {
        loadData()
    }


    private fun loadData() {
        val amountEstimateEth =
            BalanceUtil.convertTogEstimateGasEth(gasPrice, gasLimit).toString().trimTrailingZero()
        val estimateAmountEth = BalanceUtil.formatBalanceWithSymbol(
            amountEstimateEth,
            symbol
        )
        binding.txtAmount.text = estimateAmountEth
        binding.txtEstimateGas.text = gasLimit.toString()
        binding.txtPriceGas.text = StringBuilder(BalanceUtil.weiToGwei(gasPrice)).append(" GWEI")

        Timber.d("ethGasPrice: $gasPrice")
        Timber.d("ethEstimateGas: $gasLimit")
        Timber.d("amountEstimateEth: $amountEstimateEth")
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    companion object {
        val GAS_LIMIT: BigInteger = BigInteger.valueOf(21000)
        fun newInstance(
            fm: FragmentManager,
            symbol:String,
            gasPrice: BigInteger,
            gasLimit: BigInteger,
            callbackAction: (BigInteger, BigInteger) -> Unit
        ) {
            val dialog = EditGasDialogFragment(symbol, gasPrice, gasLimit, callbackAction)
            dialog.show(fm, dialog.tag)
        }
    }
}