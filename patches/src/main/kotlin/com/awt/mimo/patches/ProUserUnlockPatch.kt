package com.awt.regain.patches

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.patch
import com.awt.regain.patches.Constants.COMPATIBILITY_MIMO

@Suppress("unused")
val mimoForcePremiumTierPatch = patch(
    name = "Force premium subscription tier",
    description = "Overwrites the SubscriptionTier parameter with the premium " +
        "enum constant in the Mimo IAP-model constructor.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_MIMO)

    execute {
        IapModelConstructorFingerprint.method.addInstructions(
            0,
            """
                sget-object p3, Lcom/getmimo/data/source/remote/iap/model/SubscriptionTier;->e:Lcom/getmimo/data/source/remote/iap/model/SubscriptionTier;
            """.trimIndent(),
        )
    }
}