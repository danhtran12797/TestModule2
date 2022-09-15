package com.intelnet.omniwallet.repository

import android.content.Context
import com.intelnet.omniwallet.util.Constants
import com.intelnet.omniwallet.util.fromJson
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject

class PreferencesRepository @Inject constructor(context: Context, val gson: Gson) {

    init {
        Timber.d("INIT PreferencesRepository")
    }

    private var prefs =
        context.getSharedPreferences(Constants.LOCAL_SHARED_PREF, Context.MODE_PRIVATE)

    fun getStartIntro(): Boolean {
        return prefs.getBoolean("start_intro", false)
    }

    fun setStartIntro(start: Boolean) {
        prefs.edit().putBoolean("start_intro", start).apply()
    }

    fun isRememberLogin(): Boolean {
        return prefs.getBoolean("is_remember_login", false)
    }

    fun setRememberLogin(remember: Boolean) {
        prefs.edit().putBoolean("is_remember_login", remember).apply()
    }

    fun getAddress(): String {
        return prefs.getString("address_wallet", "") ?: ""
    }

    fun setAddress(address: String) {
        prefs.edit().putString("address_wallet", address).apply()
    }

    fun getDefaultNetwork(): String? {
        return prefs.getString("default_network_name", null)
    }

    fun setDefaultNetwork(netName: String) {
        prefs.edit().putString("default_network_name", netName).apply()
    }

    fun getListTokenAddress(type: String) : List<String>{
        val json = prefs.getString(
            if (type == Constants.BSC_SYMBOL) "list_token_address_bnb" else "list_token_address_eth", null
        ) ?: return listOf()
//        val type1 = object : TypeToken<List<String?>?>() {}.type
        return fromJson(gson, json)
    }

    private fun setListTokenAddress(lstAddress: List<String>, type: String) {
        if (type == Constants.BSC_SYMBOL)
            prefs.edit().putString("list_token_address_bnb", gson.toJson(lstAddress)).apply()
        else
            prefs.edit().putString("list_token_address_eth", gson.toJson(lstAddress)).apply()
    }

    fun hideTokenAddress(index:Int, type:String){
        val lstToken = getListTokenAddress(type).toMutableList()
        lstToken.removeAt(index)
        setListTokenAddress(lstToken, type)
    }

    fun checkExistTokenAddress(address: String, type: String):Boolean{
        val lstTokenAddress = getListTokenAddress(type)
        val index = lstTokenAddress.indexOfFirst { it.equals(address, true) }
        return index!=-1
    }

    fun addTokenAddress(address: String, type: String){
        val lstTokenAddress = getListTokenAddress(type).toMutableList()
        lstTokenAddress.add(address)
        setListTokenAddress(lstTokenAddress, type)
    }

    fun getRecentListAddress(): List<String> {
        val json = prefs.getString("list_recently_address", null) ?: return listOf()
        return fromJson(gson, json)
    }

    private fun setRecentListAddress(lstAddress: List<String>) {
        prefs.edit().putString("list_recently_address", gson.toJson(lstAddress)).apply()
    }

    fun setRecentAddress(item: String) {
        val list = getRecentListAddress().toMutableList()

        if (list.any { it == item })
            return
        list.add(0, item)
        if (list.size > 5)
            list.removeLast()

        setRecentListAddress(list)
    }

    fun clearData() {
        prefs.edit().clear().apply()
    }


//    fun clearDataAddressWallet() {
//        prefs.edit().remove("is_remember_login").apply()
//        prefs.edit().remove("address_wallet").apply()
//    }

}