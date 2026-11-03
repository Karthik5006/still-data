# Validation

- Native resources compiled and linked with Android Build Tools 35.0.0 against Android SDK 35.
- All Java sources compiled; D8 generated the installable DEX.
- 37 core checks passed for master feature states, independent mobile/Wi-Fi enable/disable and block-all rules, visible scene position changes, pause/resume behavior and reasons, excluding the app itself and removed packages, the empty-allowlist safeguard, month/DST billing boundaries, and honest unavailable/zero usage labels.
- APK signature verified with Android apksigner (v3, supported by the minimum Android 10 version).
- Manifest audited: no Internet, contacts, location, storage, accessibility, or device administrator permission. Usage Access, VPN approval, notifications, and package visibility support the core features.
- No native binaries: the APK is architecture-independent and supports the S25 Ultra's ARM64 processor.

Device validation is recorded below. No physical Galaxy S25 Ultra was available in this workspace.

An Android 15 emulator was started without hardware acceleration. Its Android framework did not finish starting in this environment; the install attempt could not complete. UI behavior, real network blocking, mobile/Wi-Fi switching, Usage Access results, and Private DNS filtering remain unverified on a running Android device. Do not treat the core checks or successful APK build as proof of physical-device behavior. Test these on the S25 Ultra before relying on this build to enforce a data budget.

Version 1.1 uses versionCode 2 and the same signing certificate as 1.0, allowing an in-place APK update without clearing settings. The animation tests exercise the same scene clock and position functions used by Canvas; they are not a visual or physical-device verification.
