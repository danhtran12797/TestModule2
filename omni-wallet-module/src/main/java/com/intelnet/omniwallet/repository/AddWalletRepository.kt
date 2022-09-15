package com.intelnet.omniwallet.repository

import android.content.Context
import io.reactivex.Observable
import org.bouncycastle.util.encoders.Hex
import org.web3j.crypto.Bip44WalletUtils
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.WalletUtils
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/*
* Timber: Log
*
*
*
* */


class AddWalletRepository @Inject constructor(
    context: Context,
    private val keyStoreFile: File
) {

    private val passwordRepository = PasswordRepository(context)

    fun createWallet(pass: String): Observable<Pair<String, String>> {
        return Observable.fromCallable {
            val bip39Wallet = Bip44WalletUtils.generateBip44Wallet(pass, keyStoreFile) // lưu file .json xuống thiết bị - web3j làm
            Timber.d("bip39Wallet: $bip39Wallet")
            val credentials = Bip44WalletUtils.loadBip44Credentials("", bip39Wallet.mnemonic) // load credentials từ mnemonic
            Timber.d("address: ${credentials.address}")
            Timber.d("privateKey: ${credentials.ecKeyPair.privateKey.toString(16)}") // Log xem thử private key :))

            passwordRepository.setPassword2(credentials.address, pass) // đồng thời encrypt pass dựa vào address public và lưu file xuống thiết bị

            Pair(
                credentials.address, // địa chỉ public
                bip39Wallet.mnemonic // 12 từ
            )
        }
    }

    fun importPrivateKey(prvKey: String, pass: String): Observable<String> {
        return Observable.fromCallable {
            val keys = ECKeyPair.create(Hex.decode(prvKey))
            val nameFile = WalletUtils.generateWalletFile(pass, keys, keyStoreFile, false) // lưu file .json xuống thiết bị - web3j làm
            Timber.d("nameFile: $nameFile")
            val credentials = WalletUtils.loadCredentials(pass, File(keyStoreFile, nameFile)) // load credentials từ file
            Timber.d("address: ${credentials.address}")

            credentials.address
        }.flatMap {
            passwordRepository.setPassword(it, pass)  // đồng thời encrypt pass dựa vào address public và lưu file xuống thiết bị
            // passwordRepository.setPassword(it, pass) phát ra địa chỉ public
        }
    }

    fun importMnemonic(mnemonic: String, pass: String): Observable<String> {
        return Observable.fromCallable {
            val credentials = Bip44WalletUtils.loadBip44Credentials("", mnemonic) // load credentials từ mnemonic
            val nameFile =
                WalletUtils.generateWalletFile(pass, credentials.ecKeyPair, keyStoreFile, false)  // lưu file .json xuống thiết bị - web3j làm
            Timber.d("address: ${credentials.address}")
            Timber.d("nameFile: $nameFile")

            credentials.address
        }.flatMap {
            passwordRepository.setPassword(it, pass) // đồng thời encrypt pass dựa vào address public và lưu file xuống thiết bị
            // passwordRepository.setPassword(it, pass) phát ra địa chỉ public
        }
    }

}