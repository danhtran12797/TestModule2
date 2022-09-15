package com.intelnet.omniwallet.repository

import android.text.TextUtils
import com.intelnet.omniwallet.base.Erc20TokenWrapper
import com.intelnet.omniwallet.entity.NetworkInfo
import com.intelnet.omniwallet.util.BalanceUtil
import com.intelnet.omniwallet.util.BalanceUtil.convertTogEstimateGasEth
import io.reactivex.Observable
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.TransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.tx.response.NoOpProcessor
import org.web3j.tx.response.PollingTransactionReceiptProcessor
import org.web3j.tx.response.TransactionReceiptProcessor
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import timber.log.Timber
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import javax.inject.Inject


class NetworkRepository @Inject constructor(
    val preferencesRepository: PreferencesRepository,
    val NETWORKS: List<NetworkInfo>
) {

    private var defaultNetwork = getByName(preferencesRepository.getDefaultNetwork()) ?: NETWORKS[0]

    private var web3j = Web3j.build(HttpService(getDefaultNetwork().rpcServerUrl))

    fun getNetworkList() = NETWORKS

    fun setDefaultNetworkInfo(pos: Int) {
        web3j.shutdown()

        defaultNetwork = NETWORKS[pos]
        preferencesRepository.setDefaultNetwork(defaultNetwork.name)

        web3j = Web3j.build(HttpService(getDefaultNetwork().rpcServerUrl))

    }

    fun getDefaultNetwork(): NetworkInfo {
        return defaultNetwork
    }

    private fun getByName(name: String?): NetworkInfo? {
        if (!TextUtils.isEmpty(name))
            for (NETWORK in NETWORKS)
                if (name == NETWORK.name)
                    return NETWORK
        return null
    }

    fun getWalletBalance(address: String): Observable<BigInteger> {
        return Observable.fromCallable {
            web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).sendAsync().get().balance
        }
    }


    fun getListToken(credentials: Credentials): Observable<List<Map<String, String>>> {
        val lstAddress = preferencesRepository.getListTokenAddress(defaultNetwork.symbol)

        val transactionReceiptProcessor: TransactionReceiptProcessor = NoOpProcessor(web3j)

        val transactionManager: TransactionManager = RawTransactionManager(
            web3j,
            credentials,
            defaultNetwork.chainId.toLong(),
            transactionReceiptProcessor
        )

        if (lstAddress.isEmpty())
           return Observable.just(mutableListOf<Map<String, String>>())
        val lstObservable = mutableListOf<Observable<Map<String, String>>>()
        lstAddress.forEach {
            val contract = Erc20TokenWrapper.load(
                it, web3j,
                transactionManager, BigInteger.ZERO, BigInteger.ZERO
            )
            lstObservable.add(generateContractObservable(contract, credentials.address, it))
        }

        return Observable.zip(lstObservable) {
            it.toMutableList() as MutableList<Map<String, String>>
        }
    }


    fun getInforToken(
        credentials: Credentials,
        contractAddress: String
    ): Observable<Map<String, String>> {

        val transactionReceiptProcessor: TransactionReceiptProcessor = NoOpProcessor(web3j)

        val transactionManager: TransactionManager = RawTransactionManager(
            web3j,
            credentials,
            defaultNetwork.chainId.toLong(),
            transactionReceiptProcessor
        )

        return Observable.fromCallable {
            val contract = Erc20TokenWrapper.load(
                contractAddress, web3j,
                transactionManager, BigInteger.ZERO, BigInteger.ZERO
            )
            mapOf(
                "symbol" to contract.symbol().value,
                "decimals" to contract.decimals().value.toString(),
                "contractAddress" to contract.contractAddress
            )
        }
    }


    private fun generateContractObservable(
        contract: Erc20TokenWrapper,
        address: String,
        contractAddress: String
    ): Observable<Map<String, String>> {
        return Observable.fromCallable {
            mapOf(
                "name" to contract.name().value,
                "symbol" to contract.symbol().value,
                "address" to contractAddress,
                "balance" to BalanceUtil.subunitToBase(contract.balanceOf(Address(address)).value)
                    .toString()
            )
        }
    }

    fun getEstimateGas(
        fromAddress: String,
        toAddress: String,
        amount: String,
        contractAddress: String,
    ): Observable<Pair<BigInteger, BigInteger>> {

        return Observable.fromCallable {

            val weiValue: BigInteger = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()

            val function =
                Function("transfer", listOf(Address(toAddress), Uint256(weiValue)), emptyList())
            val txData: String = FunctionEncoder.encode(function)

            val gasPrice = web3j.ethGasPrice().send().gasPrice

            val toAddress2 = contractAddress.takeIf { it.isNotEmpty() } ?: toAddress

            val transaction = Transaction.createFunctionCallTransaction(
                fromAddress,
                BigInteger.ONE,
                gasPrice,
                DefaultGasProvider.GAS_LIMIT,
                toAddress2,
                BigInteger.ZERO,
                txData
            )

            val estimateGas = web3j.ethEstimateGas(transaction).sendAsync().get().amountUsed

            Timber.d("ethGasPrice: $gasPrice")
            Timber.d("ethEstimateGas: $estimateGas")

            val amountEstimateEth =
                convertTogEstimateGasEth(gasPrice, estimateGas)

            Timber.d("amountEstimateEth: $amountEstimateEth")

            Pair(gasPrice, estimateGas)
        }
    }

    @Throws(IOException::class)
    private fun getNonce(walletAddress: String): BigInteger {
        val ethGetTransactionCount = web3j.ethGetTransactionCount(
            walletAddress, DefaultBlockParameterName.PENDING
        ).send()
        return ethGetTransactionCount.transactionCount
    }

    fun transactionFlowable() = web3j.transactionFlowable()

    fun sendEther(
        credentials: Credentials,
        toAddress: String,
        amount: String,
        gasPrice: BigInteger,
        gasLimit: BigInteger
    ): Observable<String> {

        return Observable.fromCallable {
            val weiValue: BigDecimal = Convert.toWei(amount, Convert.Unit.ETHER)

            val nonce: BigInteger = getNonce(credentials.address)

            val rawTransaction = RawTransaction.createEtherTransaction(
                nonce, gasPrice, gasLimit, toAddress, weiValue.toBigIntegerExact()
            )
            val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
            val hexValue = Numeric.toHexString(signedMessage)

            val ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get()

            val transactionHash = ethSendTransaction.transactionHash

            Timber.d("transactionHash: $transactionHash")

            transactionHash
        }
    }

    fun sendToken(
        credentials: Credentials,
        toAddress: String,
        amount: String,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        tokenAddress: String
    ): Observable<String> {

        return Observable.fromCallable {

            val weiValue: BigDecimal = Convert.toWei(amount, Convert.Unit.ETHER)

            val ethGetTransactionCount = web3j.ethGetTransactionCount(
                credentials.address, DefaultBlockParameterName.LATEST
            ).send()
            val nonce: BigInteger = ethGetTransactionCount.transactionCount

            val function = Function(
                "transfer",
                Arrays.asList(
                    Address(toAddress),
                    Uint256(weiValue.toBigInteger())
                ) as List<Type<Any>>?, emptyList()
            )
            val txData: String = FunctionEncoder.encode(function)

            val rawTransaction = RawTransaction.createTransaction(
                nonce, gasPrice, gasLimit, tokenAddress, txData
            )

            // sign the transaction
            val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)
            val hexValue = Numeric.toHexString(signedMessage)

            // Send transaction
            val ethSendTransaction: EthSendTransaction =
                web3j.ethSendRawTransaction(hexValue).send()

            val transactionHash = ethSendTransaction.transactionHash

            Timber.d("transactionHash: $transactionHash")

            transactionHash
        }
    }


    fun sendToken2(
        credentials: Credentials,
        toAddress: String,
        amount: String,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        tokenAddress: String
    ): Observable<String> {

        return Observable.fromCallable {

            val weiValue: BigDecimal = Convert.toWei(amount, Convert.Unit.ETHER)
            val transactionReceiptProcessor: TransactionReceiptProcessor =
                PollingTransactionReceiptProcessor(web3j, 3000, 40)
            val transactionManager: TransactionManager = RawTransactionManager(
                web3j,
                credentials,
                defaultNetwork.chainId.toLong(),
                transactionReceiptProcessor
            )
            val contract = Erc20TokenWrapper.load(
                tokenAddress,
                web3j,
                transactionManager,
                gasPrice,
                gasLimit
            )
            val mReceipt =
                contract.transfer(Address(toAddress), Uint256(weiValue.toBigInteger()))
            val transactionHash = mReceipt.transactionHash

            Timber.d("transactionHash: $transactionHash")

            transactionHash
        }
    }


}