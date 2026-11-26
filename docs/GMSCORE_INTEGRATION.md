# Regain MicroG-RE integration

## Status

An opt-in Morphe patch is implemented at:

`patches/src/main/kotlin/com/awt/regain/patches/MicroGReSupportPatch.kt`

- Target: Regain `ai.regainapp`, version `76.1.2139`
- MicroG runtime: Morphe MicroG-RE, vendor namespace `app.revanced`
- GmsCore package: `app.revanced.android.gms`
- Patch default: **off** (select it explicitly in Morphe)

The patch source is committed to this repository branch. The patch bundle has **not** been built here, and no installed-device or backend sign-in test has been completed. Morphe can handle the bundle build as requested; a successful build alone will not establish that sign-in works.

## Why this uses MicroG-RE

The supplied Regain 76.1.2139 XAPK was inspected. Its DEX files show that the Google sign-in screen uses:

- AndroidX `CredentialManager`
- AndroidX `CredentialProviderPlayServicesImpl`
- Google Identity Services `GetSignInWithGoogleOption`
- `TYPE_GOOGLE_ID_TOKEN_CREDENTIAL` and `BUNDLE_KEY_ID_TOKEN`

The app waits for the Credential Manager account selector, obtains a Google ID token, and sends it to Regain's own `/auth/google_sign_in` endpoint.

MicroG-RE's current source contains a matching Android Credential Provider implementation:

- [GoogleIdService.kt](https://github.com/MorpheApp/MicroG-RE/blob/main/play-services-core/src/main/kotlin/org/microg/gms/auth/credentials/provider/GoogleIdService.kt) handles `TYPE_GOOGLE_ID_TOKEN_CREDENTIAL`.
- [SignInProxyActivity.kt](https://github.com/MorpheApp/MicroG-RE/blob/main/play-services-core/src/main/kotlin/org/microg/gms/auth/credentials/provider/SignInProxyActivity.kt) requests a Google ID token and returns an AndroidX custom credential containing it.
- [credentials_provider_google_id.xml](https://github.com/MorpheApp/MicroG-RE/blob/main/play-services-core/src/main/res/xml/credentials_provider_google_id.xml) advertises the MicroG-RE credential type.

This provider requires Android API 34 or newer. The app's AndroidX request and MicroG-RE's provider use the same credential family, so this is a more direct match than relying on the older Firebase GoogleSignIn compatibility matrix.

## What the patch changes

The patch is intentionally target-specific and does not rename Regain's own package or local component classes.

1. **Credential type namespace.** Rewrites only the Google ID token credential type from the Google namespace to MicroG-RE's advertised type:

   `com.google.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL`

   becomes:

   `app.revanced.android.libraries.identity.googleid.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL`

   The Google ID `BUNDLE_KEY_*` names and the `TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL` subtype remain unchanged. MicroG-RE reads those original keys when it parses the request.

2. **GmsCore routing.** Remaps the relevant GmsCore package/authority strings and service actions that are exposed by MicroG-RE, including Identity sign-in, Sign-In, credentials, measurement, telemetry, FIDO2, and Phenotype actions.

3. **Firebase/C2DM namespace.** Remaps Regain's C2DM receive/register actions, permissions, and IID token request to the `app.revanced` namespace. Push delivery is not yet device-tested.

4. **Manifest metadata.** Adds or updates:
   - `app.revanced.android.gms.SPOOFED_PACKAGE_NAME = ai.regainapp`
   - `app.revanced.android.gms.SPOOFED_PACKAGE_SIGNATURE = f1d63dc9f6d5b7ac1091b6a7723b957664d36f60`
   - `app.revanced.MICROG_PACKAGE_NAME = app.revanced.android.gms`

   The SHA-1 is from the signer certificate in the supplied 76.1.2139 base APK's APK signing block.

5. **Optional selection.** The patch is `default = false`. This is deliberate: the rewritten credential type is intended for a device with the matching MicroG-RE provider installed and enabled. Applying it without that provider can leave Regain without a provider that recognizes the credential type.

## Device setup and verification

1. Install a compatible Morphe MicroG-RE build whose GmsCore package is `app.revanced.android.gms`.
2. Complete MicroG-RE setup and add the Google account that should be used for sign-in.
3. On Android 14 or newer, enable MicroG-RE's Google ID credential provider in the system Credential Manager / password and passkey provider settings. The exact Settings menu wording depends on the device.
4. In Morphe, apply this repository's **MicroG-RE support** patch to Regain `76.1.2139`. Leave the patch off on devices that should use normal Google Play services.
5. Test the complete flow: provider selection, Google account selection, ID-token return to Regain, success from `/auth/google_sign_in`, and session persistence.

Keep the original Regain package name `ai.regainapp`; this integration does not require app cloning.

## Verification still outstanding

- Morphe patch-bundle compilation and patch application.
- Confirming Android Credential Manager actually selects the MicroG-RE provider on the target device.
- Confirming a non-empty Google ID token is returned to Regain and accepted by `api.regainapp.ai`.
- Confirming login persistence / refresh-token behavior.
- Firebase FCM registration and push delivery.
- Other optional GmsCore-dependent features such as Advertising ID.

The source-level match is promising, but it is not an end-to-end login guarantee. If the Credential Manager provider does not appear or Regain rejects the token, inspect the device logs and the actual OAuth client configuration before adding broader package or signature spoofing.

## Reference implementation

The general Morphe GmsCore implementation was reviewed at commit
`MorpheApp/morphe-patches@d38d9d841a965953751ef1f733e308bcde46a5a7`:

- [Shared GmsCore patch](https://github.com/MorpheApp/morphe-patches/blob/d38d9d841a965953751ef1f733e308bcde46a5a7/patches/src/main/kotlin/app/morphe/patches/shared/misc/gms/GmsCoreSupportPatch.kt)
- [Shared GmsCore fingerprints](https://github.com/MorpheApp/morphe-patches/blob/d38d9d841a965953751ef1f733e308bcde46a5a7/patches/src/main/kotlin/app/morphe/patches/shared/misc/gms/Fingerprints.kt)

This Regain implementation is a narrow port of the parts supported by the inspected target and MicroG-RE manifest. It does not import Morphe's YouTube extension, clone-app preferences, or unrelated Google service hooks.
