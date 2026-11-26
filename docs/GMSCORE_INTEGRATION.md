# Regain GmsCore / microG integration

Status: research and integration groundwork only. No GmsCore patch is enabled yet.

Target from this repository:
- App: Regain
- Original package: `ai.regainapp`
- Target version: `76.1.2139`
- APK package/identity is configured in `patches/src/main/kotlin/com/awt/regain/patches/Constants.kt`.

## Findings

### Existing Morphe implementation to use as the reference

The upstream Morphe patch project has a shared GmsCore implementation:

- `patches/src/main/kotlin/app/morphe/patches/shared/misc/gms/GmsCoreSupportPatch.kt`
- `patches/src/main/kotlin/app/morphe/patches/shared/misc/gms/Fingerprints.kt`
- `extensions/shared-youtube/library/src/main/java/app/morphe/extension/shared/patches/GmsCoreSupportPatch.java`

It is not a drop-in patch. The upstream code depends on Morphe's clone-app patch, package-query patch, resource patch, settings preferences, and shared extension runtime. Its target-app adapters also supply fingerprints for startup and Google Play Services checks. Those dependencies must either be ported or reimplemented for this template.

Reference revision inspected: `MorpheApp/morphe-patches@d38d9d841a965953751ef1f733e308bcde46a5a7`.

### Regain login evidence

A reverse-engineering report for Regain 75.4.2131 records this application flow:

1. Google sign-in obtains a Google token.
2. Regain sends the token and `regain_email` to `GET /auth/google_sign_in`.
3. Regain's API returns its own JWT and refresh token.

The same report identifies the app's broad Firebase stack, but it does **not** prove which Google credential API the sign-in screen calls. The exact auth API must be identified from the target 76.1.2139 DEX before selecting a microG compatibility strategy.

The observed API endpoint is Regain's own API:
- Host: `api.regainapp.ai`
- Endpoint: `GET /auth/google_sign_in`

### Signing certificate

For Regain `ai.regainapp` version `76.1.2139`, the published certificate SHA-1 is:

```text
F1D63DC9F6D5B7AC1091B6A7723B957664D36F60
```

The published SHA-256 is:

```text
83BCE5D85F10817652CE823AB828EB23960FED90090F1EED1EE754B74CFCDEF4
```

This SHA-256 matches the certificate SHA-256 recorded by the separate Regain APK analysis report. The SHA-1 value is the one required by the upstream Morphe GmsCore patch's `spoofedPackageSignature` parameter.

These values must be rechecked against the exact original APK used for patching before release.



## Exact 76.1.2139 XAPK inspection (2026-11-26)

The supplied `regain-76-1-2139.xapk` was opened and the main APK extracted. It contains:
- `ai.regainapp.apk` (79,497,291 bytes)
- `config.arm64_v8a.apk`
- `config.armeabi_v7a.apk`
- density splits for mdpi through xxxhdpi, including tvdpi

The main APK contains four DEX files. DEX string/type-table inspection confirms that the sign-in implementation is **not the old legacy GoogleSignInClient-only flow**.

### Confirmed authentication stack

The exact build contains:
- `androidx.credentials.CredentialManager`
- `androidx.credentials.playservices.CredentialProviderPlayServicesImpl`
- `com.google.android.libraries.identity.googleid`
- `GetSignInWithGoogleOption`
- `GoogleIdTokenParsingException`
- `TYPE_GOOGLE_ID_TOKEN_CREDENTIAL`
- `BUNDLE_KEY_ID_TOKEN`

DEX log strings explicitly include:
- `Created CredentialManager instance`
- `Built GetCredentialRequest`
- `Created GetSignInWithGoogleOption`
- `Calling getCredential() - waiting for Google account selector to appear`

This is the AndroidX Credential Manager + Google ID library path, specifically the Sign in with Google option. AndroidX's Play Services credential provider delegates to Google Play services. This is materially different from Firebase Auth's legacy `GoogleSignInClient` flow.

The backend endpoint string `/auth/google_sign_in` remains present. The sign-in result therefore still has to provide a Google ID token that Regain can submit to its own backend.

### Implication for GmsCore / MicroG-RE

A generic package-name/manifest GmsCore patch does not prove this credential flow will work. The target calls the Credential Manager Play Services provider and requests a Google ID token. We must verify that the selected MicroG-RE release implements the specific Identity/Credential Manager service path used by the bundled AndroidX provider.

Do not classify this as "Firebase GoogleSignIn" solely because Firebase is bundled; the exact APK evidence identifies Credential Manager + Google ID token as the sign-in mechanism.

### Next technical tasks

- Decode the binary manifest and inspect package queries, provider declarations, app metadata, and GMS-related permissions.
- Resolve the obfuscated app class/method that constructs `GetSignInWithGoogleOption` and calls `CredentialManager.getCredential()`.
- Inspect `CredentialProviderPlayServicesImpl` and its GMS service/action dependencies in the exact DEX.
- Inspect the APK signing block directly and verify the signer SHA-1 against the published metadata before using it in a patch.
- Check current MicroG-RE source for the exact Identity/Credential Manager service interfaces, not just its generic Google sign-in or Firebase feature list.
- Implement an opt-in GmsCore support patch with verified app-specific fingerprints; retain the original patch as default-safe until the build and device test pass.

## Planned implementation

1. Inspect the exact 76.1.2139 base APK and its split set.
2. Identify the Google auth API in DEX:
   - legacy `GoogleSignInClient` / `GoogleSignInOptions`;
   - Google Identity Services;
   - Credential Manager / `GetGoogleIdOption`;
   - Firebase Auth `GoogleAuthProvider`;
   - or a custom browser/token flow.
3. Extract exact fingerprints for the app's `HomePageActivity.onCreate`, Google Play Services availability check, and any GMS package-name assumptions.
4. Port only the necessary upstream Morphe shared code and its runtime/resource dependencies. Avoid importing the entire YouTube extension library.
5. Add a GmsCore Support patch with:
   - a dedicated patched package name;
   - the original package and SHA-1 signature spoofing metadata;
   - GMS permissions, actions, authorities and package-visibility declarations required by the target;
   - app-specific DEX hooks, only where confirmed by fingerprints;
   - a MicroG settings entry if the target has an appropriate settings screen.
6. Build the .mpp bundle, patch the exact target APK, install it alongside the original if supported, and test:
   - app launch;
   - Google account chooser;
   - ID-token acquisition;
   - Regain `/auth/google_sign_in` success;
   - JWT refresh and account persistence;
   - FCM token registration (separate from sign-in).
7. Publish only after successful device testing. A successful patch-bundle build is not proof that Google sign-in works.

## Compatibility warning

microG's current published implementation matrix marks Firebase Auth as partial: anonymous, email/password, phone and custom-token methods are supported, while GoogleSignIn and FirebaseUI are listed as unsupported. Account authentication itself is also marked partial. Do not claim support until the exact Regain sign-in implementation is known and the token flow has been tested. If Regain uses an unsupported API, GmsCore package/manifest wiring alone will not create a working Google ID token.

For this Morphe patch repository, the practical runtime target is **Morphe MicroG-RE**, the Morphe-maintained GmsCore fork. Its patched package namespace is `app.revanced`, with the GmsCore package exposed as `app.revanced.android.gms`. The stock microG project and MicroG-RE should not be treated as interchangeable installation targets.

## Next required artifact

The exact original `ai.regainapp 76.1.2139` base APK (plus its split APKs, if patching the Play split set) is required to derive stable app-specific fingerprints. A decompiled APK containing DEX/smali and the decoded manifest is also acceptable. Do not use the already-analyzed 75.4.2131 build as the final fingerprint source.


## Reference sources

- Morphe shared GmsCore patch: https://github.com/MorpheApp/morphe-patches/blob/d38d9d841a965953751ef1f733e308bcde46a5a7/patches/src/main/kotlin/app/morphe/patches/shared/misc/gms/GmsCoreSupportPatch.kt
- Morphe GmsCore runtime: https://github.com/MorpheApp/morphe-patches/blob/d38d9d841a965953751ef1f733e308bcde46a5a7/extensions/shared-youtube/library/src/main/java/app/morphe/extension/shared/patches/GmsCoreSupportPatch.java
- Morphe MicroG-RE: https://github.com/MorpheApp/MicroG-RE
- microG API implementation status: https://github.com/microg/GmsCore/wiki/Implementation-status
- Google documentation on Google Sign-In certificate SHA-1 requirements: https://developers.google.com/android/guides/client-auth
- Regain 76.1.2139 APK metadata and signer fingerprint: https://apkbog.com/en/apk/regain-study-timer-for-focus/76.1.2139
- Regain 75.4.2131 reverse-engineering notes: https://github.com/mitunmanav/apps-autopsy/tree/main/apps/regain/report
