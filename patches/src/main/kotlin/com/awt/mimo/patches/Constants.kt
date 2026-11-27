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

    // Real obfuscated class that holds the IAP constructor:
    //   .class public final Lzg/e;
    //   .method public constructor <init>(
    //       Ljava/util/List;
    //       Lcom/revenuecat/purchases/EntitlementInfo;
    //       Lcom/getmimo/data/source/remote/iap/model/SubscriptionTier;
    //       Lcom/revenuecat/purchases/EntitlementInfo;
    //   )V
    const val IAP_MODEL_CLASS = "Lzg/e;"

    const val SUBSCRIPTION_TIER_CLASS =
        "Lcom/getmimo/data/source/remote/iap/model/SubscriptionTier;"

    const val SIGNATURE_SPOOF_CLASS =
        "Lapp/morphe/extension/signature/SignatureSpoof;"
}