package com.intelnet.omniwallet.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

inline fun <reified T> fromJson(gson: Gson, json: String): T {
    return gson.fromJson(json, object : TypeToken<T>() {}.type)
}