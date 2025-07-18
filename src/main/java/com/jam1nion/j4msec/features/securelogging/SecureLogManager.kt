package com.jam1nion.j4msec.features.securelogging

import android.content.Context
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel
import kotlinx.coroutines.CoroutineScope
import java.io.File

interface SecureLogManager {

    suspend fun logDeviceInfo()
    fun logAsync(tag: String, message: String, level : LoggingLevel = LoggingLevel.INFO)
    suspend fun log(tag: String, message: String, level : LoggingLevel = LoggingLevel.INFO)
    fun exportDecryptedAsync(onResult: (List<String>) -> Unit)
    suspend fun exportDecrypted() : List<String>
    fun exportFilteredAsync(levels: List<LoggingLevel>, onResult: (List<String>) -> Unit)
    suspend fun exportFiltered(levels: List<LoggingLevel>): List<String>
    fun verifyIntegrityAsync(onResult: (Boolean) -> Unit)
    suspend fun verifyIntegrity() : Boolean
    fun exportDecryptedSkippingCorruptedAsync(onResult: (List<String>) -> Unit)
    suspend fun exportDecryptedSkippingCorrupted() : List<String>
    fun shutdownLogger()
}