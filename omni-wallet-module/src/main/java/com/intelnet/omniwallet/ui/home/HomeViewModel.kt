package com.intelnet.omniwallet.ui.home

import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.intelnet.omniwallet.base.BaseViewModel
import com.intelnet.omniwallet.repository.NetworkRepository
import com.intelnet.omniwallet.repository.PreferencesRepository
import com.intelnet.omniwallet.repository.WalletRepository
import com.intelnet.omniwallet.ui.home.adapter.ItemToken
import com.intelnet.omniwallet.ui.home.network.adapter.ItemNetwork
import com.intelnet.omniwallet.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.web3j.crypto.Credentials
import org.web3j.protocol.core.methods.response.Transaction
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import javax.inject.Inject

typealias EventAddress = Event<Data<String>>
typealias EventToken = Event<Data<MutableMap<String, String>>>
typealias EventEstimate = Event<Data<String>>
typealias EventTransfer = Event<Data<String>>
typealias EventTransaction = Event<Data<Transaction>>

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val networkRepository: NetworkRepository,
    private val preferencesRepository: PreferencesRepository
) :
    BaseViewModel() {

    init {
        Timber.d("INIT HOME VIEWMODEL")
    }

    lateinit var gasPrice: BigInteger
    lateinit var gasLimit: BigInteger

    var credentials: Credentials? = null

    var balanceETH = BigDecimal("0")

    var lstToken = mutableListOf<ItemToken>()

    private val _addressLiveData: MutableLiveData<EventAddress> = MutableLiveData()
    val addressLiveData: LiveData<EventAddress> = _addressLiveData

    private val _balanceLiveData: MutableLiveData<Event<BigDecimal>> = MutableLiveData()
    val balanceLiveData: LiveData<Event<BigDecimal>> = _balanceLiveData

    private val _lstTokenLiveData: MutableLiveData<Event<List<ItemToken>>> = MutableLiveData()
    val lstTokenLiveData: LiveData<Event<List<ItemToken>>> = _lstTokenLiveData

    private val _tokenLiveData: MutableLiveData<EventToken> = MutableLiveData()
    val tokenLiveData: LiveData<EventToken> = _tokenLiveData

    private val _estimateLiveData: MutableLiveData<EventEstimate> = MutableLiveData()
    val estimateLiveData: LiveData<EventEstimate> = _estimateLiveData

    private val _transferLiveData: MutableLiveData<EventTransfer> = MutableLiveData()
    val transferLiveData: LiveData<EventTransfer> = _transferLiveData

    // loading liveData
    private val _listenLiveData = MutableLiveData<EventTransaction>()
    val listenLiveData: LiveData<EventTransaction> = _listenLiveData

    val lstItemNetwork: MutableList<ItemNetwork> by lazy {
        getNetWorkItemList().toMutableList()
    }

    private fun getNetWorkItemList() = networkRepository.getNetworkList().map {
        val random = Random()
        val network = networkRepository.getDefaultNetwork()
        ItemNetwork(
            1, it.name, Color.argb(
                255, random.nextInt(256),
                random.nextInt(256), random.nextInt(256)
            ),
            network.name == it.name
        )
    }

    fun getItemNetworkDefault(): ItemNetwork? {
        return lstItemNetwork.find { it.isChecked }
    }

    fun setDefaultNetworkInfo(pos: Int) {
        lstItemNetwork.forEachIndexed { index, itemNetwork ->
            itemNetwork.isChecked = pos == index
        } // update list item network
        networkRepository.setDefaultNetworkInfo(pos)
    }

    fun loadCredentials(address: String) {
        val disposable = walletRepository.loadCredentials2(address)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
//                _addressLiveData.value = Event(Data(responseType = Status.LOADING))
            }
            .doOnComplete {
                Timber.d("Close waitting dialog")
            }
            .subscribe(
                { response ->
                    Timber.d("On Next Called: ${response.address}")
                    credentials = response

                    loadListToken()

                    _addressLiveData.value =
                        Event(
                            Data(
                                responseType = Status.SUCCESSFUL,
                                data = response.address
                            )
                        )
                }, { error ->
                    Timber.e("On Error Called, ${error.message}")
                    _addressLiveData.value =
                        Event(Data(Status.ERROR, null, error = Error(error.message)))
                }, {
                    Timber.d("On Complete Called")
                }
            )
        addDisposable(disposable)
    }

    private fun loadListToken() {
        val disposable = networkRepository.getListToken(credentials!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _fetchLiveData.value = Data(responseType = Status.LOADING)
            }
            .doOnComplete {
                Timber.d("Close waitting dialog")
            }
            .subscribe(
                { response ->
                    Timber.d("On Next Called")

                    Timber.d("SIZE: ${response.size}")
                    for (item in response) {
                        val xxx = item as HashMap<String, String>
                        xxx.forEach { (k, v) ->
                            Timber.d("$k : $v")
                        }
                        Timber.d("----------------------------")
                    }

                    val lstTokenItem = parseMapToItemToken(response)

                    _lstTokenLiveData.value = Event(lstTokenItem)

                    _fetchLiveData.value = Data(responseType = Status.SUCCESSFUL)

                }, { error ->
                    Timber.d("On Error Called, ${error.message}")
                    _fetchLiveData.value =
                        Data(Status.ERROR, null, error = Error(error.message))
                }, {
                    Timber.d("On Complete Called: loadListToken")
                }
            )
        addDisposable(disposable)
    }


    private fun parseMapToItemToken(lstMap: List<Map<String, String>>): MutableList<ItemToken> {
        lstToken.clear()
        val lstTemp = lstMap.mapIndexed { i, map ->
            val symbol = map["symbol"] ?: ""
            val balance = map["balance"] ?: ""
            val address = map["address"] ?: ""
            ItemToken(symbol, balance.trimTrailingZero(), address, ItemToken.ITEM_DATA)
        }.toMutableList().also {
            it.add(
                0,
                ItemToken.generateHeadItem(
                    getSymbolNetworkDefault(),
                    balanceETH.toString().trimTrailingZero()
                )
            )
        }
        lstToken.addAll( // set list token
            lstTemp
        )
        return lstTemp.also {
            it.add(
                ItemToken.generateFooterItem()
            )
        }
    }


    fun loadBalance(address: String) {
        val disposable = networkRepository.getWalletBalance(address)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _addressLiveData.value = Event(Data(responseType = Status.LOADING))
            }
            .doOnComplete {
                Timber.d("Close waitting dialog")
            }
            .subscribe(
                { response ->
                    Timber.d("On Next Called: $response")
                    loadCredentials(address)
                    _balanceLiveData.value =
                        Event(convertBalanceETH(response))

                }, { error ->
                    Timber.e("On Error Called, ${error.message}")
                    _addressLiveData.value =
                        Event(Data(Status.ERROR, null, error = Error(error.message)))
                }, {
                    Timber.d("On Complete Called: loadBalance")
                }
            )
        addDisposable(disposable)
    }

    fun getSymbolNetworkDefault() = networkRepository.getDefaultNetwork().symbol
    fun getScanUrlNetworkDefault() = networkRepository.getDefaultNetwork().scanUrl

    private fun convertBalanceETH(response: BigInteger): BigDecimal {
        balanceETH = BalanceUtil.subunitToBase(response)
        return balanceETH
    }

    fun refresh() {
        if (credentials == null)
            return
        val disposable = Observable.zip(
            networkRepository.getWalletBalance(credentials!!.address),
            networkRepository.getListToken(credentials!!),
            BiFunction { t1, t2 ->
                Pair(t1, t2)
            }
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _fetchLiveData.value = Data(responseType = Status.LOADING)
            }
            .doOnComplete {
                Timber.d("Close waitting dialog")
            }
            .subscribe(
                { response ->
                    Timber.d("On Next Called: $response")

                    val balanceETH = convertBalanceETH(response.first)
                    val lstTokenItem = parseMapToItemToken(response.second)

                    _balanceLiveData.value = Event(balanceETH)
                    _lstTokenLiveData.value = Event(lstTokenItem)

                    _fetchLiveData.value = Data(responseType = Status.SUCCESSFUL)
                }, { error ->
                    Timber.e("On Error Called, ${error.message}")
                    _fetchLiveData.value =
                        Data(Status.ERROR, null, error = Error(error.message))
                }, {
                    Timber.d("On Complete Called: refresh")
                }
            )
        addDisposable(disposable)
    }

    fun loadInforToken(address: String) {
        val disposable = networkRepository.getInforToken(credentials!!, address)
            .map {
                val isExist =
                    preferencesRepository.checkExistTokenAddress(address, getSymbolNetworkDefault())
                if (isExist)
                    throw Exception("token_address_existed")
                it.toMutableMap()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _tokenLiveData.value = Event(Data(responseType = Status.LOADING))
            }
            .doOnComplete {
                Timber.d("Close waitting dialog")
            }
            .subscribe(
                { response ->
                    Timber.d("On Next Called: $response")

                    _tokenLiveData.value =
                        Event(
                            Data(
                                responseType = Status.SUCCESSFUL,
                                data = response
                            )
                        )

                }, { error ->
                    Timber.e("On Error Called, ${error.message}")
                    _tokenLiveData.value =
                        Event(Data(Status.ERROR, null, error = Error(error.message)))
                }, {
                    Timber.d("On Complete Called: loadInforToken")
                }
            )
        addDisposable(disposable)
    }

    fun getEstimateGas(
        fromAddress: String,
        toAddress: String,
        amount: String,
        contractAddress: String
    ) {
        val disposable =
            networkRepository.getEstimateGas(fromAddress, toAddress, amount, contractAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    _estimateLiveData.value = Event(Data(responseType = Status.LOADING))
                }
                .doOnComplete {
                    Timber.d("Close waitting dialog")
                }
                .subscribe(
                    { response ->
                        Timber.d("On Next Called: $response")

                        gasPrice = response.first
                        gasLimit = response.second

                        _estimateLiveData.value =
                            Event(
                                Data(
                                    responseType = Status.SUCCESSFUL,
                                    data = "success"
                                )
                            )

                    }, { error ->
                        Timber.e("On Error Called, ${error.message}")
                        _estimateLiveData.value =
                            Event(Data(Status.ERROR, null, error = Error(error.message)))
                    }, {
                        Timber.d("On Complete Called: getEstimateGas")
                    }
                )
        addDisposable(disposable)
    }

    fun transfer(
        toAddress: String,
        amount: String,
        tokenAddress: String = ""
    ) {
        val disposable = if (tokenAddress.isNotEmpty()) {
            networkRepository.sendToken2(
                credentials!!,
                toAddress,
                amount,
                gasPrice,
                gasLimit,
                tokenAddress
            )
        } else {
            networkRepository.sendEther(credentials!!, toAddress, amount, gasPrice, gasLimit)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _transferLiveData.value = Event(Data(responseType = Status.LOADING))
            }
            .doOnComplete {
                Timber.d("Close waitting dialog")
            }
            .subscribe(
                { response ->
                    Timber.d("On Next Called: $response")

//                    listenerTransfer()

                    _transferLiveData.value =
                        Event(
                            Data(
                                responseType = Status.SUCCESSFUL,
                                data = response
                            )
                        )

                }, { error ->
                    Timber.e("On Error Called, ${error.message}")
                    _transferLiveData.value =
                        Event(Data(Status.ERROR, null, error = Error(error.message)))
                }, {
                    Timber.d("On Complete Called: transfer")
                }
            )
        addDisposable(disposable)
    }

    fun listenerTransfer() {
        val disposable = networkRepository.transactionFlowable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _listenLiveData.value = Event(Data(responseType = Status.LOADING))
            }
            .doOnComplete {
                Timber.d("Close waitting dialog")
            }
            .subscribe(
                { response ->
//                    Timber.d("On Next Called: $response")

                    _listenLiveData.value =
                        Event(
                            Data(
                                responseType = Status.SUCCESSFUL,
                                data = response
                            )
                        )

                }, { error ->
                    Timber.e("On Error Called, ${error.message}")
                    _listenLiveData.value =
                        Event(Data(Status.ERROR, null, error = Error(error.message)))
                }, {
                    Timber.d("On Complete Called: listenerTransfer")
                }
            )
        addDisposable(disposable)
    }

    fun addContractAddressToPref(address: String) {
        preferencesRepository.addTokenAddress(
            address = address,
            type = getSymbolNetworkDefault()
        )
    }

}