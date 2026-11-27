package com.awt.mimo.patches

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.patch
import com.awt.mimo.patches.Constants.COMPATIBILITY_MIMO

@Suppress("unused")
val mimoForcePremiumTierPatch = patch(
    name = "Force premium subscription tier",
    description = "Overwrites the SubscriptionTier parameter with the premium " +
        "enum constant in the Mimo IAP-model constructor.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_MIMO)

    execute {
        val method = IapModelConstructorFingerprint.methodOrNull
            ?: error(
                "Constructor not found in ${Constants.IAP_MODEL_CLASS}. " +
                    "Verify the class is still Lzg/e; in Mimo 9.39.",
            )

        method.addInstructions(
            0,
            """
                sget-object p3, Lcom/getmimo/data/source/remote/iap/model/SubscriptionTier;->e:Lcom/getmimo/data/source/remote/iap/model/SubscriptionTier;
            """.trimIndent(),
        )
    }
}