package com.awt.mimo.patches

import app.morphe.patcher.Fingerprint

/**
 * Targets the Mimo IAP-model constructor:
 *
 *   .method public constructor <init>(
 *       Ljava/util/List;
 *       Lcom/revenuecat/purchases/EntitlementInfo;
 *       Lcom/getmimo/data/source/remote/iap/model/SubscriptionTier;
 *       Lcom/revenuecat/purchases/EntitlementInfo;
 *   )V
 *       .registers 5
 *       ...
 */
internal object IapModelConstructorFingerprint : Fingerprint(
    definingClass = Constants.IAP_MODEL_CLASS,
    name = "<init>",
    returnType = "V",
    parameters = listOf(
        "Ljava/util/List;",
        "Lcom/revenuecat/purchases/EntitlementInfo;",
        "Lcom/getmimo/data/source/remote/iap/model/SubscriptionTier;",
        "Lcom/revenuecat/purchases/EntitlementInfo;",
    ),
)
