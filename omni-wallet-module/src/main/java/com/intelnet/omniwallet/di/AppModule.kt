package com.intelnet.omniwallet.di

import android.content.Context
import android.content.SharedPreferences
import com.intelnet.omniwallet.BuildConfig
import com.intelnet.omniwallet.entity.NetworkInfo
import com.intelnet.omniwallet.util.Constants
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(httpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private class BasicAuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request: Request = chain.request()
            val newRequest = request.newBuilder()
//                .addHeader("Content-Type", "text/plain")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build()
            return chain.proceed(newRequest)
        }
    }

    @Provides
    @Singleton
    fun httpClient(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
        val clientBuilder = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            clientBuilder.addInterceptor(httpLoggingInterceptor)
        }
        clientBuilder.addInterceptor(BasicAuthInterceptor())
        clientBuilder.writeTimeout(60, TimeUnit.SECONDS)
        clientBuilder.readTimeout(60, TimeUnit.SECONDS)
        return clientBuilder.build()
    }

    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context {
        return context
    }


    @Provides
    @Singleton
    fun provideSharedPref(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.LOCAL_SHARED_PREF, Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideBaseNetworks() = listOf(
        NetworkInfo(
            name = Constants.BSC_TESTNET,
            rpcServerUrl = "https://data-seed-prebsc-1-s1.binance.org:8545/",
            chainId = 97,
            symbol = Constants.BSC_SYMBOL,
            scanUrl = "https://testnet.bscscan.com",
            backendUrl = "https://api-testnet.bscscan.com",
            apiKey = BuildConfig.BSC_API
        ),
        NetworkInfo(
            name = Constants.ROPSTEN_NETWORK_NAME,
            rpcServerUrl = "https://ropsten.infura.io/v3/5c74f1278e2a4c87ab5b46e3aa8cb30b",
            chainId = 3,
            symbol = Constants.ETH_SYMBOL,
            scanUrl = "https://ropsten.etherscan.io",
            backendUrl = "https://api-ropsten.etherscan.io",
            apiKey = BuildConfig.Etherscan_API
        )
    )



    @Provides
    fun provideKeyStoreFile(@ApplicationContext context: Context): File {
        val keydir = File(context.filesDir, "keystore")
        deleteDir(keydir) // đảm bảo folder keystore 1 file :))
        if (!keydir.exists())
            keydir.mkdirs()
        return keydir
    }

    private fun deleteDir(dir: File): Boolean {
        if (dir.isDirectory) {
            val childrens = dir.list() ?: return false
            for (i in childrens.indices) {
                val success = deleteDir(File(dir, childrens[i]))
                if (!success) {
                    return false
                }
            }
        }
        return dir.delete()
    }

}