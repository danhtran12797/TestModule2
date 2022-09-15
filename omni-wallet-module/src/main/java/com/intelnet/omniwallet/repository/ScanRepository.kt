package com.intelnet.omniwallet.repository

import android.text.TextUtils
import com.intelnet.omniwallet.BuildConfig
import com.intelnet.omniwallet.entity.EtherPriceResponse
import com.intelnet.omniwallet.entity.EtherScanResponse
import com.intelnet.omniwallet.entity.NetworkInfo
import com.intelnet.omniwallet.util.Constants
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import timber.log.Timber
import javax.inject.Inject

class ScanRepository @Inject constructor(
    okHttpClient: OkHttpClient,
    val NETWORKS: List<NetworkInfo>,
    val preferencesRepository: PreferencesRepository
) {

    private var defaultNetwork = getByName(preferencesRepository.getDefaultNetwork()) ?: NETWORKS[0]
    private val type = defaultNetwork.symbol

    private val etherScanApiClient: EtherScanApiClient

/*    @Query("page") page: Int,
    @Query("offset") offset: Int,*/

    private interface EtherScanApiClient {
        @GET("/api?module=account&action=txlist")
        fun fetchTransaction(
            @Query("address") address: String,
            @Query("sort") sort: String,
            @Query("apikey") apiKey: String
        ): Observable<EtherScanResponse>

        @GET("/api?module=stats&action={type}")
        fun fetchEthPrice(
            @Query("apikey") apiKey: String,
            @Path("type") type: String
        ): Single<EtherPriceResponse>
    }


    init {
        Timber.d("INIT ScanRepository")


        etherScanApiClient = Retrofit.Builder()
            .baseUrl(defaultNetwork.backendUrl)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(EtherScanApiClient::class.java)
    }

    private fun getByName(name: String?): NetworkInfo? {
        if (!TextUtils.isEmpty(name))
            for (NETWORK in NETWORKS)
                if (name == NETWORK.name)
                    return NETWORK
        return null
    }

    fun fetchTransaction(address: String, page: Int=1, offset: Int=30): Observable<EtherScanResponse> {
        return etherScanApiClient.fetchTransaction(
            address,
            "DESC",
            if (type == Constants.BSC_SYMBOL) BuildConfig.BSC_API else BuildConfig.Etherscan_API
        )
    }

    fun fetchEthPrice(): Single<EtherPriceResponse> {
        return etherScanApiClient.fetchEthPrice(
            if (type == Constants.BSC_SYMBOL) BuildConfig.BSC_API else BuildConfig.Etherscan_API,
            if (type == Constants.BSC_SYMBOL) "bnbprice" else "ethprice"
        )
    }
}