package com.awt.regain.patches

import app.morphe.patcher.string
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.replaceStrings
import app.morphe.patcher.patch.resourceXml
import com.awt.regain.patches.Constants.COMPATIBILITY_REGAIN

private const val MICROG_VENDOR_GROUP_ID = "app.revanced"
private const val MICROG_GMS_PACKAGE = "$MICROG_VENDOR_GROUP_ID.android.gms"
private const val REGAIN_PACKAGE_NAME = "ai.regainapp"
private const val REGAIN_SIGNING_CERT_SHA1 = "f1d63dc9f6d5b7ac1091b6a7723b957664d36f60"

/**
 * Regain 76.1.2139 uses AndroidX Credential Manager with
 * GetSignInWithGoogleOption. MicroG-RE exposes the matching Google ID
 * CredentialProviderService under its app.revanced vendor namespace.
 *
 * Keep this patch opt-in: without MicroG-RE, the stock Google credential
 * provider does not advertise the app.revanced credential type.
 */
@Suppress("unused")
val microGReSupportPatch = patch(
    name = "MicroG-RE support",
    description = "Routes Regain's Google ID Credential Manager request to MicroG-RE and configures Regain package/signature spoofing metadata.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_REGAIN)

    execute {
        /*
         * MicroG-RE's credential provider advertises:
         * app.revanced.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
         *
         * Only this credential TYPE is namespaced. The Google ID BUNDLE_KEY_*
         * values and TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL subtype intentionally
         * retain their com.google.android.libraries.identity.googleid names;
         * MicroG-RE reads those original keys.
         */
        val dexStringReplacements = linkedMapOf(
            "com.google.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL" to
                "$MICROG_VENDOR_GROUP_ID.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL",

            // Package/authority strings consumed by the renamed MicroG-RE package.
            "com.google.android.gms" to MICROG_GMS_PACKAGE,
            "com.google.android.gms.phenotype" to "$MICROG_GMS_PACKAGE.phenotype",
            "com.google.android.gms.chimera" to "$MICROG_GMS_PACKAGE.chimera",
            "com.google.android.gms.fonts" to "$MICROG_GMS_PACKAGE.fonts",
            "content://com.google.android.gms.phenotype/" to
                "content://$MICROG_GMS_PACKAGE.phenotype/",

            // Firebase Cloud Messaging uses the namespaced C2DM endpoints in MicroG-RE.
            "com.google.android.c2dm.intent.RECEIVE" to "$MICROG_VENDOR_GROUP_ID.android.c2dm.intent.RECEIVE",
            "com.google.android.c2dm.intent.REGISTER" to "$MICROG_VENDOR_GROUP_ID.android.c2dm.intent.REGISTER",
            "com.google.android.c2dm.intent.REGISTRATION" to "$MICROG_VENDOR_GROUP_ID.android.c2dm.intent.REGISTRATION",
            "com.google.android.c2dm.permission.RECEIVE" to "$MICROG_VENDOR_GROUP_ID.android.c2dm.permission.RECEIVE",
            "com.google.android.c2dm.permission.SEND" to "$MICROG_VENDOR_GROUP_ID.android.c2dm.permission.SEND",
            "com.google.iid.TOKEN_REQUEST" to "$MICROG_VENDOR_GROUP_ID.iid.TOKEN_REQUEST",
            "com.google.android.gms.permission.AD_ID" to "$MICROG_GMS_PACKAGE.permission.AD_ID",
        )

        replaceStrings(
            dexStringReplacements.map { (fromString, toString) ->
                string(fromString) to toString
            }
        )

        resourceXml("AndroidManifest.xml").use { manifest ->
            /*
             * Redirect only manifest permission/action/authority values that
             * MicroG-RE actually publishes. Component class names such as
             * FirebaseInstanceIdReceiver and AppMeasurementService remain
             * inside Regain's APK and must not be renamed.
             */
            val manifestTransformations = mapOf(
                "com.google.android.c2dm" to "$MICROG_VENDOR_GROUP_ID.android.c2dm",
                "com.google.android.gms.permission.AD_ID" to "$MICROG_GMS_PACKAGE.permission.AD_ID",
                "com.google.android.gms.phenotype" to "$MICROG_GMS_PACKAGE.phenotype",
                "com.google.android.gms.chimera" to "$MICROG_GMS_PACKAGE.chimera",
                "com.google.android.gms.fonts" to "$MICROG_GMS_PACKAGE.fonts",
            )

            manifest.elements().forEach { element ->
                element.attributeNames.forEach attributes@{ attributeName ->
                    val value = element.attribute(attributeName) ?: return@attributes
                    var replaced = value

                    manifestTransformations.forEach { (fromString, toString) ->
                        replaced = replaced.replace(fromString, toString)
                    }

                    if (replaced != value) {
                        element.setAttribute(attributeName, replaced)
                    }
                }
            }

            /*
             * Let MicroG-RE answer package/signature checks using Regain's
             * original Play-distributed identity. SHA-1 was read from the
             * v2/v3 signing block of the supplied 76.1.2139 base APK.
             */
            val application = manifest.elements("application").first()
            val metadata = mapOf(
                "$MICROG_GMS_PACKAGE.SPOOFED_PACKAGE_NAME" to REGAIN_PACKAGE_NAME,
                "$MICROG_GMS_PACKAGE.SPOOFED_PACKAGE_SIGNATURE" to REGAIN_SIGNING_CERT_SHA1,
                "$MICROG_VENDOR_GROUP_ID.MICROG_PACKAGE_NAME" to MICROG_GMS_PACKAGE,
            )

            metadata.forEach { (name, value) ->
                val existing = application.elements("meta-data").firstOrNull {
                    it.attribute("android:name") == name
                }

                if (existing == null) {
                    application.appendChild("meta-data") {
                        setAttribute("android:name", name)
                        setAttribute("android:value", value)
                    }
                } else {
                    existing.setAttribute("android:value", value)
                }
            }
        }
    }
}
