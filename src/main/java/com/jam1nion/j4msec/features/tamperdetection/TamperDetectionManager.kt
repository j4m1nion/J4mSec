package com.jam1nion.j4msec.features.tamperdetection

import android.content.Context

interface TamperDetectionManager {

    fun appTamperingDetection(
        context: Context,
        knownAppHash: String,
        onTampering: () -> Unit,
        onIntegrity: () -> Unit
    )

    fun isAppTampered(
        context: Context,
        knownAppHash: String,
    ): Boolean

    fun assetTampered(
        context: Context,
        assetName: String,
        expectedBase64Hash: String,
        onTampering: () -> Unit,
        onIntegrity: () -> Unit
    )

    fun isAssetTampered(
        context: Context,
        assetName: String,
        expectedBase64Hash : String,
    ): Boolean

    fun verifyAllAssets(
        context: Context,
        expectedHashes: Map<String, String>
    ): List<String>
}