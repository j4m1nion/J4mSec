package com.jam1nion.j4msec.features.tamperdetection

import android.content.Context
import com.jam1nion.j4msec.J4mSec
import com.jam1nion.j4msec.features.securelogging.models.LoggingLevel
import com.jam1nion.j4msec.features.utils.CryptoUtils.computeSha256Base64
import java.io.File
import java.security.MessageDigest

internal class TamperDetectionManagerImpl : TamperDetectionManager {

    companion object{
        private const val TAG = "TamperDetectionManager"
        private const val ALGORITHM = "SHA-256"
    }

    override fun appTamperingDetection(
        context: Context,
        knownAppHash: String,
        onTampering: () -> Unit,
        onIntegrity: () -> Unit
    ){
        if(isAppTampered(context, knownAppHash)){
            onTampering.invoke()
        }
        else{
            onIntegrity.invoke()
        }
    }

    override fun isAppTampered(
        context: Context,
        knownAppHash: String,
    ): Boolean {
        val apkFile = File(context.packageCodePath)
        val currentHash = computeSha256Base64(apkFile, ALGORITHM)
        val tampered = currentHash.trim() != knownAppHash.trim()

        if(J4mSec.configuration.enableLogging){
            J4mSec.secureLogManager?.logAsync(
                TAG,
                if(tampered) "App tamper detected! Hash mismatch." else "App integrity verified.",
                if(tampered) LoggingLevel.SECURITY else LoggingLevel.DEBUG
            )
        }
        return tampered
    }

    override fun assetTampered(
        context: Context,
        assetName: String,
        expectedBase64Hash: String,
        onTampering: () -> Unit,
        onIntegrity: () -> Unit
    ){
        if(isAssetTampered(context, assetName, expectedBase64Hash)){
            onTampering.invoke()
        }
        else{
            onIntegrity.invoke()
        }
    }

    override fun isAssetTampered(
        context: Context,
        assetName: String,
        expectedBase64Hash : String
    ): Boolean {
        return try {
            val input = context.assets.open(assetName)
            val actualHash = input.use {
                val bytes = it.readBytes()
                val digest = MessageDigest.getInstance(ALGORITHM).digest(bytes)
                android.util.Base64.encodeToString(digest, android.util.Base64.NO_WRAP)
            }
            val result = actualHash != expectedBase64Hash

            if(J4mSec.configuration.enableLogging){
                J4mSec.secureLogManager?.logAsync(
                    TAG,
                    if(result) "Asset $assetName corrupted." else "Asset $assetName valid.",
                    if(result) LoggingLevel.SECURITY else LoggingLevel.DEBUG
                )
            }
            result
        }
        catch (ex: Exception){
            true
        }
    }

    override fun verifyAllAssets(
        context: Context,
        expectedHashes: Map<String, String>
    ): List<String> {
        return expectedHashes.filter { (asset, hash) ->
            isAssetTampered(context, asset, hash)
        }.keys.toList()
    }


}