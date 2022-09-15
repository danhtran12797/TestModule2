package com.intelnet.omniwallet.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Wallet(var id:Long=0, var name:String="Account 1", var address:String) : Parcelable