package com.intelnet.omniwallet.repository

import android.content.Context
import com.intelnet.omniwallet.manager.CipherManager
import io.reactivex.Observable
import io.reactivex.Single
import java.security.SecureRandom
import javax.inject.Inject

class PasswordRepository @Inject constructor(context: Context) {

    private val cipher = CipherManager(context)

    fun getPassword(address: String): Observable<String> {
        return Observable.fromCallable { String(cipher.get(address)) } // return password
    }

    fun setPassword(address: String, password: String): Observable<String> {
        return Observable.fromCallable { cipher.put(address, password) } // return address
    }

    fun setPassword2(address: String, password: String): String {
        return cipher.put(address, password)
    }

    fun isExists(address: String): Boolean {
        return cipher.isExists(address)
    }

    fun generatePassword(): Single<String> {
        return Single.fromCallable {
            val bytes = ByteArray(256)
            val random = SecureRandom()
            random.nextBytes(bytes)
            String(bytes)
        }
    }
}