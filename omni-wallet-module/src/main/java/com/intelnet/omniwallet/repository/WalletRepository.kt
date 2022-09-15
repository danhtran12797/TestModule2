package com.intelnet.omniwallet.repository

import android.content.Context
import io.reactivex.Observable
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import timber.log.Timber
import java.io.File
import javax.inject.Inject


class WalletRepository @Inject constructor(context: Context) {

    private val keyDir: File = File(context.filesDir, "keystore")
    private val passwordRepository = PasswordRepository(context)

    fun exportPrivateKey(pass: String): Observable<String> {
        return Observable.fromCallable {
            val childrens = keyDir.list() ?: throw Exception("Error list dir.")
            val credentials =
                WalletUtils.loadCredentials(pass, File(keyDir, childrens[0])) // first file
            val privateKey = credentials.ecKeyPair.privateKey.toString(16)
            Timber.d("privateKey: $privateKey")
            privateKey
        }
    }

    // shiver riot talk grief short wing spirit morning volcano garlic giraffe any
    fun loadCredentials(pass: String): Observable<Credentials> {
        return Observable.fromCallable {
            val childrens = keyDir.list() ?: throw Exception("Error list dir.")
            val credentials =
                WalletUtils.loadCredentials(pass, File(keyDir, childrens[0])) // first file
            Timber.d("address: ${credentials.address}")
            credentials
        }
    }

    fun loadCredentials2(address: String): Observable<Credentials> {
        Timber.d("ADDRESS $address")
        return passwordRepository.getPassword(address)
            .flatMap {
                Timber.d("PASS $it")
                Observable.fromCallable {
                    val childrens = keyDir.list() ?: throw Exception("Error list dir.")
                    val credentials =
                        WalletUtils.loadCredentials(it, File(keyDir, childrens[0])) // first file
                    Timber.d("address: ${credentials.address}")
                    credentials
                }
            }
    }

}