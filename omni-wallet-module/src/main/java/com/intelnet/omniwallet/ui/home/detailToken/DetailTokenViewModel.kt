package com.intelnet.omniwallet.ui.home.detailToken

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.intelnet.omniwallet.base.BaseViewModel
import com.intelnet.omniwallet.entity.EtherScanResponse
import com.intelnet.omniwallet.entity.Transaction
import com.intelnet.omniwallet.repository.ScanRepository
import com.intelnet.omniwallet.ui.home.adapter.ItemToken
import com.intelnet.omniwallet.ui.home.detailToken.adapter.ItemHistoryTokenAdapter
import com.intelnet.omniwallet.ui.home.detailToken.adapter.ItemTransaction
import com.intelnet.omniwallet.util.BalanceUtil
import com.intelnet.omniwallet.util.Data
import com.intelnet.omniwallet.util.Event
import com.intelnet.omniwallet.util.Status
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.web3j.abi.TypeDecoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import timber.log.Timber
import java.lang.reflect.Method
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


typealias EventTransaction = Event<Data<EtherScanResponse>>

@HiltViewModel
class DetailTokenViewModel @Inject constructor(
    private val scanRepository: ScanRepository
) : BaseViewModel() {

    val lstItemTransaction = mutableListOf<ItemTransaction>()

    private val _transactionLiveData: MutableLiveData<EventTransaction> = MutableLiveData()
    val transactionLiveData: LiveData<EventTransaction> = _transactionLiveData


    fun loadTransaction(walletAddress: String) {
        val disposable =
            scanRepository.fetchTransaction(
                walletAddress
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    _transactionLiveData.value = Event(Data(responseType = Status.LOADING))
                }
                .doOnComplete {
                    Timber.d("Close waitting dialog")
                }
                .subscribe(
                    { response ->
                        Timber.d("On Next Called: $response")

                        _transactionLiveData.value =
                            Event(
                                Data(
                                    responseType = Status.SUCCESSFUL,
                                    data = response
                                )
                            )

                    }, { error ->
                        Timber.e("On Error Called, ${error.message}")
                        _transactionLiveData.value =
                            Event(Data(Status.ERROR, null, error = Error(error.message)))
                    }, {
                        Timber.d("On Complete Called: transfer")
                    }
                )
        addDisposable(disposable)
    }

    private fun decodeInput(item: Transaction): Pair<String, String> {

        val dt = getShortDate(item.timeStamp.toLong())
        Timber.d("dt: $dt")

        val inputData = item.input

        val method = inputData.substring(0, 10)
        Timber.d("method: $method")
        val to = inputData.substring(10, 74)
        val value = inputData.substring(74)
        val refMethod: Method = TypeDecoder::class.java.getDeclaredMethod(
            "decode",
            String::class.java,
            Int::class.javaPrimitiveType,
            Class::class.java
        )
        refMethod.isAccessible = true
        val address = refMethod.invoke(
            null, to, 0,
            Address::class.java
        ) as Address
        Timber.d("address: ${address.value}")
        val amount = refMethod.invoke(null, value, 0, Uint256::class.java) as Uint256
        Timber.d("amount: ${amount.value}")

        val amountEth = BalanceUtil.weiToEth(amount.value)

        return Pair(address.value, amountEth.toPlainString())
    }

    private fun getShortDate(ts: Long): String {
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.ENGLISH)
        return simpleDateFormat.format(ts * 1000L)
    }


    fun parseToItemTransaction(
        response: EtherScanResponse,
        lstToken: List<ItemToken>,
        walletAddress: String,
        symbolDefault: String,
        contractAddress: String
    ): List<ItemTransaction> {

        val lstHead =  lstItemTransaction.take(1)
        lstItemTransaction.clear()
        lstItemTransaction.addAll(lstHead)

        val lstFilter = response.result.toMutableList().filter {
            if (contractAddress.isNotEmpty()) it.to.equals(
                contractAddress,
                true
            ) else if(it.functionName.contains("transfer")) lstToken.any { token-> token.address.equals(it.to, true) } else true
        }
        val lstTemp = lstFilter.map { trans ->
            val dateFormat = getShortDate(trans.timeStamp.toLong())
            var symbol = symbolDefault
            var to: String
            var amount: String
            val estimate = BalanceUtil.convertTogEstimateGasEth(
                BigInteger(trans.gasPrice),
                BigInteger(trans.gas)
            ).toPlainString()

            if (trans.functionName.contains("transfer")) { // send token
                symbol =
                    lstToken.find { it.address.equals(trans.to, true) }?.symbol ?: symbolDefault
                decodeInput(trans).run {
                    to = first
                    amount = second
                }
            } else {
                to = trans.to
                amount = BalanceUtil.weiToEth(BigInteger(trans.value)).toPlainString()
            }

            val status = when{
                trans.from == trans.to -> ItemHistoryTokenAdapter.SELF
                trans.to == walletAddress -> ItemHistoryTokenAdapter.RECEIVED
                else -> trans.isError.toInt()
            }

            ItemTransaction(
                hash = trans.hash,
                nonce = trans.nonce,
                symbol = symbol,
                symbolNative = symbolDefault,
                amount = amount,
                estimate = estimate,
                from = trans.from,
                to = to,
                formatDateTime = dateFormat,
                status = status,
                type = ItemHistoryTokenAdapter.ITEM_DATA
            )
        }.take(29).toMutableList().apply {
            if(size>0)
                add(
                    ItemTransaction.generateItemFooter()
                )
            else
                add(
                    ItemTransaction.generateItemEmpty()
                )
        }

        lstItemTransaction.addAll(lstTemp)

        return lstItemTransaction
    }


}