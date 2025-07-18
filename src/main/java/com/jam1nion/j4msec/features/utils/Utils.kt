package com.jam1nion.j4msec.features.utils

import android.util.Base64

fun String.getProp() : String? {
    return try {
        val process = Runtime.getRuntime().exec("getprop $this")
        process.inputStream.bufferedReader().use { it.readLine() }
    }
    catch (ex : Exception){
        null
    }
}

fun String.isBase64Encoded() : Boolean {
    return try {
        Base64.decode(this, Base64.NO_WRAP)
        true
    }
    catch (ex: IllegalArgumentException){
        false
    }
}

 fun String.containsAny(keywords: List<String>) : Boolean{
    return keywords.any { this.contains(it, ignoreCase = true) }
}