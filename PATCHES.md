# Patches

> Generated from `patches-list.json` — **v1.2.0-dev.1** (`dev`) · **8 patches** across **2 apps** · back to [README](README.md)

---

## Mimo (com.getmimo)

**Supported versions:** `9.39`

| Patch | Details |
|---|---|
| **Force premium subscription tier** | Overwrites the SubscriptionTier parameter with the premium enum constant in the Mimo IAP-model constructor. |

---

## Regain (ai.regainapp)

**Supported versions:** `76.2.2151` `76.1.2139`

| Patch | Details |
|---|---|
| **MicroG-RE support** | Routes Regain Google ID Credential Manager, supported GMS service actions, and Firebase C2DM names to MicroG-RE; configures Regain package/signature metadata. |
| **Pro user unlock** | Always returns true for isProUser() to enable Pro features during testing. |

---

## Universal

| Patch | Details |
|---|---|
| **Change installer source** | Spoofs the installer source so the app appears to be installed from an app store.<br><sub>Options: Spoofed package installer name</sub> |
| **Clone app** | Changes the app package name to allow installing the same app multiple times. By default ".morphe" is appended to the package name. Each cloned install must use a unique package name. Cloning does not work with all apps and using this patch may cause app crashes or other unexpected behavior.<br><sub>Options: Package name, Update permissions, Update providers</sub> |
| **Disable Play Store updates** | Disables Play Store updates by setting the version code to the maximum allowed. This patch may cause unexpected issues with some apps and does not work if the app is installed by root mounting |
| **Override certificate pinning** | Overrides certificate pinning, allowing to inspect traffic via a proxy. |
| **Spoof signature** | Spoofs the package signature of the original APK. |
