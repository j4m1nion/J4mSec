package com.jam1nion.j4msec.features.appsignatureverification

import android.content.Context

interface AppSignatureManager {

    fun appSignature(context: Context, certificateSignature: ByteArray, onAppSignatureValid: () -> Unit, onAppSignatureInvalid: () -> Unit)
    fun appSignature(context: Context, certificateSignature: String, onAppSignatureValid: () -> Unit, onAppSignatureInvalid: () -> Unit)
    fun appSignature(context: Context, certificateSignature: String, xorKey: Char = 0x5A.toChar(), reverse: Boolean = true, salt: String? = null, onAppSignatureValid: () -> Unit, onAppSignatureInvalid: () -> Unit)
    fun appSignature(context: Context, certificateSignature: ByteArray) : Boolean
    fun appSignature(context: Context, certificateSignature: String) : Boolean
    fun appSignature(context: Context, certificateSignature: String, xorKey: Char = 0x5A.toChar(), reverse: Boolean = true, salt: String? = null) : Boolean
}