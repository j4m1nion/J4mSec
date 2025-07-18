package com.jam1nion.j4msec.features.securelogging

import android.content.Context
import android.os.Build
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel
import com.jam1nion.j4msec.features.utils.CryptoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.crypto.SecretKey

internal class SecureLogManagerImpl : SecureLogManager {

    companion object{
        private const val ALGORITHM = "SHA-256"
        internal const val LOG_FILE_NAME = "__secure_log.txt"
        private const val LOG_MAX_SIZE = 1000
        private const val DELIMITER = "\u2023"
    }

    @Volatile
    private var logFile: File? = null
    @Volatile
    private var key : SecretKey? = null
    private var defaultLogScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var logScope: CoroutineScope ? = null

    override fun init(context: Context, logScope: CoroutineScope ?, logDirectory : File?, logFileName: String?){
        this.logScope = logScope ?: defaultLogScope
        logFile = File(logDirectory ?: context.filesDir, logFileName ?: LOG_FILE_NAME)
        key = CryptoUtils.getLoggingSecretKey()

        if(J4mSec.configuration.enableLogging){
            logScope?.launch {
                log(
                    "Logging init",
                    "____LOGGING START SESSION____",
                    LoggingLevel.DEBUG
                )
                logDeviceInfo()
            }
        }
    }

    override suspend fun logDeviceInfo(){

        val builder = StringBuilder().apply {
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("OS: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        }

        val result = builder.toString()

        J4mSec.secureLogManager?.log("DEVICE", result, LoggingLevel.INFO)
    }

    private fun getLoggingKey(): SecretKey{
        val currentKey = key
        return if(currentKey != null){
            currentKey
        }
        else{
            val loggingKey = CryptoUtils.getLoggingSecretKey()
            key = loggingKey
            loggingKey
        }
    }
    override fun logAsync(tag: String, message: String, level : LoggingLevel){
        logScope?.launch {
            log(tag, message, level)
        }
    }

    override suspend fun log(tag: String, message: String, level : LoggingLevel){

        val timestamp = System.currentTimeMillis()
        val entry = "$timestamp [${level.name}] [$tag] $message"
        val prevHash = getLastHashOrNull()
        val hash = computeHash(entry, prevHash ?: "")
        val secureLine = CryptoUtils.encrypt(getLoggingKey(), "$entry$DELIMITER$prevHash$DELIMITER$hash")
        appendLine(secureLine)

    }

    override fun exportDecryptedAsync(onResult: (List<String>) -> Unit){
        logScope?.launch {
            onResult.invoke(exportDecrypted())
        }
    }

    override suspend fun exportDecrypted() : List<String> = withContext(Dispatchers.IO) {
        if(logFile?.exists() == false) return@withContext emptyList()

        logFile?.readLines()?.mapNotNull {
            try {
                CryptoUtils.decrypt(getLoggingKey(), it)
            }
            catch (ex: Exception){
                null
            }
        } ?: emptyList()
    }

    override  fun exportFilteredAsync(levels: List<LoggingLevel>, onResult: (List<String>) -> Unit){
        logScope?.launch {
            onResult.invoke(exportFiltered(levels))
        }
    }

    override suspend fun exportFiltered(levels: List<LoggingLevel>): List<String> = withContext(Dispatchers.IO) {
        val log = exportDecrypted()
        return@withContext log.filter { line ->  levels.any{ line.contains("[$it]") } }
    }

    override fun verifyIntegrityAsync(onResult: (Boolean) -> Unit){
        logScope?.launch {
            onResult.invoke(verifyIntegrity())
        }
    }

    override suspend fun verifyIntegrity() : Boolean  = withContext(Dispatchers.IO) {
        val log : List<String> = exportDecrypted()
        if(log.size < 2) return@withContext true

        for (i in 1 until log.size){
            val prevLine = log[i-1].split(DELIMITER)
            val currentLine = log[i].split(DELIMITER)

            if(prevLine.size < 3 || currentLine.size < 3)
                return@withContext false

            val prevHash = prevLine.last()

            val currHash = currentLine.last()
            val currPrevHash = currentLine[currentLine.size - 2]
            val currEntry = currentLine.subList(0, currentLine.size -2).joinToString(DELIMITER)

            if(prevHash != currPrevHash)
                return@withContext false

            val recomputedHash = computeHash(currEntry, currPrevHash)

            if (recomputedHash != currHash) return@withContext false
        }

        return@withContext true

    }


    override fun exportDecryptedSkippingCorruptedAsync(onResult: (List<String>) -> Unit){
        logScope?.launch {
            onResult.invoke(exportDecryptedSkippingCorrupted())
        }
    }


    override suspend fun exportDecryptedSkippingCorrupted() : List<String>  = withContext(Dispatchers.IO) {
        val log : List<String> = exportDecrypted()
        val result = mutableListOf<String>()
        if(log.size < 2) return@withContext log

        for (i in 1 until log.size){
            val prevLine = log[i-1].split(DELIMITER)
            val currentLine = log[i].split(DELIMITER)

            val prevHash = prevLine.last()

            val currHash = currentLine.last()
            val currPrevHash = currentLine[currentLine.size - 2]
            val currEntry = currentLine.subList(0, currentLine.size -2).joinToString(DELIMITER)

            if(prevHash != currPrevHash)
                continue

            val recomputedHash = computeHash(currEntry, currPrevHash)

            if (recomputedHash == currHash) result.add(log[i])
        }

        return@withContext result

    }




    private suspend fun appendLine(line: String) = withContext(Dispatchers.IO) {
        logFile?.run {
            if(exists() == false) createNewFile()
            val lines = readLines().toMutableList()
            if(lines.size >= LOG_MAX_SIZE) {
                lines.remove(lines.first())
                lines.add(line)
                writeText(lines.joinToString("\n"))
            }
            else{
                appendText("$line\n")
            }
        }
    }

    private suspend fun getLastHashOrNull() : String? = withContext(Dispatchers.IO) {
        logFile?.run {
            if(exists() == false) return@withContext null
            val lastLine = readLines().lastOrNull() ?: return@withContext null
            try {
                val decrypted = CryptoUtils.decrypt(getLoggingKey(), lastLine)
                decrypted.split(DELIMITER).lastOrNull()
            }
            catch (ex: Exception){
                null
            }
        }
    }

    private fun hash(input: String): String{

        val digest = MessageDigest.getInstance(ALGORITHM)
        return android.util.Base64.encodeToString(digest.digest(input.toByteArray()), android.util.Base64.NO_WRAP)
    }

    private fun computeHash(entry: String, prevHash: String): String {
        return hash("$entry$DELIMITER$prevHash")
    }

    override fun shutdownLogger(){
        defaultLogScope.cancel()
        key = null
        logFile = null
    }


}