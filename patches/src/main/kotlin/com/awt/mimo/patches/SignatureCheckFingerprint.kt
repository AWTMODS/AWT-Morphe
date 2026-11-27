package com.awt.mimo.patches

import app.morphe.patcher.Fingerprint

/**
 * Targets the Mimo method that calls:
 *
 *   invoke-virtual {pm, name, flags}, Landroid/content/pm/PackageManager;
 *       ->getPackageInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;
 *
 * The `strings` anchor narrows the search to methods that reference the
 * package name "com.getmimo" — Mimo's signature/license checkers always do.
 *
 * If the fingerprint stops matching after an app update, inspect the APK
 * and refine with `definingClass` + `name` + `parameters`.
 */
internal object SignatureCheckFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("com.getmimo"),
)