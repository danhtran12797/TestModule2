package com.intelnet.omniwallet.util

import java.net.URL

fun String.isValidEmail() =
    isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.formatAddressWallet(start:Int=6) = if(isNotEmpty()) this.replace(this.substring(start, 38), "...") else ""

fun String.getStringAddressFromScan():String{
    val startPos = this.indexOf("0x")
    val endPos = this.indexOf("@")
    return if(endPos!=-1){
        this.substring(startPos, endPos)
    }else
        this.substring(startPos)
}

fun String.getHostName():String{
    val url = URL(this)
    return url.host
}

fun String.trimTrailingZero(): String {
    return if (this.indexOf(".") < 0) {
        this
    } else {
        this.replace("0*$".toRegex(), "").replace("\\.$".toRegex(), "")
    }
}