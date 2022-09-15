package com.intelnet.omniwallet.entity

data class NetworkInfo(
    val name: String,
    val rpcServerUrl: String,
    val chainId: Int,
    val symbol: String,
    val scanUrl: String,
    val backendUrl: String,
    val apiKey: String
)