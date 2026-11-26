package com.awt.regain.patches

import com.awt.regain.patches.Constants.COMPATIBILITY_REGAIN
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch

/**
 * Patch: Pro User Unlock
 *
 * Patches [ai.blox100.feature_subscription.domain.model.SubscriptionState.isProUser]
 * so it always returns `true`, enabling Pro features for testing purposes.
 *
 * Original bytecode:
 *   .registers 2
 *   iget-boolean v0, p0, Lai/blox100/feature_subscription/domain/model/SubscriptionState;->isProUser:Z
 *   return v0
 *
 * Patched bytecode:
 *   .registers 1
 *   const/4 v0, 0x1
 *   return v0
 *
 * Strategy: Replace only instruction at index 0 (iget-boolean → const/4 0x1).
 * The existing `return v0` at index 1 is left untouched.
 */
@Suppress("unused")
val proUserUnlockPatch =
    bytecodePatch(
        name = "Pro user unlock",
        description = "Always returns true for isProUser() to enable Pro features during testing.",
    ) {
        compatibleWith(COMPATIBILITY_REGAIN)

        execute {
            // Replace the iget-boolean at index 0 with const/4 v0, 0x1
            // The `return v0` at index 1 remains unchanged.
            IsProUserFingerprint.method.replaceInstruction(0, "const/4 v0, 0x1")
        }
    }
