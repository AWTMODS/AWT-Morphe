package com.awt.mimo.patches

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_MIMO =
        Compatibility(
            name = "Mimo",
            packageName = "com.getmimo",
            apkFileType = ApkFileType.APK,
            appIconColor = 0x9C6BFF,
            targets = listOf(AppTarget(version = "9.39")),
        )

    const val IAP_MODEL_CLASS =
        "Lcom/getmimo/data/source/remote/iap/model/SubscriptionModel;"
    const val SUBSCRIPTION_TIER_CLASS =
        "Lcom/getmimo/data/source/remote/iap/model/SubscriptionTier;"
    const val SIGNATURE_SPOOF_CLASS =
        "Lapp/morphe/extension/signature/SignatureSpoof;"
}
