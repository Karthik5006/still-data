# Validation

- Native resources compiled and linked with Android Build Tools 35.0.0 against Android SDK 35.
- All Java sources compiled; D8 generated the installable DEX.
- 16 core checks passed for independent mobile/Wi-Fi rules, Quiet Mode, excluding the app itself and removed packages, the empty-allowlist safeguard, month/DST billing boundaries, and honest unavailable/zero usage labels.
- APK signature verified with Android apksigner (v3, supported by the minimum Android 10 version).
- Manifest audited: no Internet, contacts, location, storage, accessibility, or device administrator permission. Usage Access, VPN approval, notifications, and package visibility support the core features.
- No native binaries: the APK is architecture-independent and supports the S25 Ultra's ARM64 processor.

Device validation is recorded below. No physical Galaxy S25 Ultra was available in this workspace.

An Android 15 emulator was started without hardware acceleration. Its Android framework did not finish starting in this environment; the install attempt could not complete. UI behavior, real network blocking, mobile/Wi-Fi switching, Usage Access results, and Private DNS filtering remain unverified on a running Android device. Do not treat the core checks or successful APK build as proof of physical-device behavior. Test these on the S25 Ultra before relying on this build to enforce a data budget.
