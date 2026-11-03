# Still Data

A native Android data-usage monitor and local firewall with an original, peaceful alpine interface. Built for Android 10–15+ and suitable for Galaxy S25 Ultra. No account, server, analytics, or Internet permission.

## Installable build

[Download the APK](downloads/StillData-1.0-release.apk) · [Quick setup guide](downloads/SETUP.md)

This is a first locally signed build. Compilation, APK signing, and 16 core logic checks passed. Android device behavior remains unverified because the available emulator could not complete startup. See [validation details](VALIDATION.md).

## What works

- Device and per-app mobile/Wi-Fi usage from Android NetworkStatsManager, with Today, seven-day, and billing-cycle views.
- Every installed app, including system apps; shared Android UIDs are grouped so usage is not double-counted. Search and progressive loading keep the app list responsive.
- Independent mobile and Wi-Fi blocking rules. A local VpnService captures only selected apps and discards their IPv4/IPv6 packets. Allowed apps bypass it and retain their connection.
- Quiet Mode blocks all apps on mobile data while leaving Wi-Fi rules independent.
- Persistent rules, a billing budget, real seven-day usage chart, blocked-packet counters, and a foreground notification with a stop action.
- System-wide domain-based ad/tracker blocking through Android Private DNS, with setup for AdGuard DNS or Control D. The app verifies the active provider using Android's network state; it cannot silently change that system setting.
- Mountain scenery and gentle snow that respect Android animation and battery settings.

## Install on your S25 Ultra

1. Transfer `StillData-1.0-release.apk` to your phone and open it. Allow installation from the browser or Files app if Android asks.
2. Open Still Data. Tap **Allow usage access**, enable Still Data in Android settings, then return.
3. In **Apps**, switch off Mobile access or Wi-Fi access for apps you want to block.
4. In **Guard**, tap **Enable firewall** and approve Android's VPN prompt. Allow notifications so the status and stop action are easy to reach.
5. For ads, tap **Set up ad blocking**, copy the hostname, choose **Private DNS provider hostname** in Android settings, paste it, and save. Return and tap **Check DNS status**.
6. If Samsung stops the foreground service, add Still Data to **Never auto sleeping apps** in Samsung Battery settings. No broad battery or device administrator permissions are requested.

## Practical limits

- Android allows one VPN at a time. The local firewall replaces another VPN while running. Private DNS is independent and can work alongside it.
- Android counters can be delayed and may differ from carrier billing. Usage is reported from Android, never estimated or fabricated. Some system/removed-app totals have no installed package to block.
- Dropped-packet totals include retries and measure traffic discarded by the VPN, not exact bytes saved or ads blocked.
- A billing budget is a visual guide; it does not automatically switch off mobile data.
- Private DNS blocks domains, not all ads. First-party ads (including many YouTube ads), cached content, and apps with their own encrypted DNS may bypass it. Your chosen DNS provider processes domain lookups; Still does not.
- Blocking is enforced while the foreground firewall service is running. Force-stopping the app, revoking VPN permission, or starting another VPN ends protection. Network transitions and Android background restrictions can introduce a gap. Always-on lockdown is intentionally unsupported for this selective drop-only VPN.
- Rules are preserved on this phone. The app does not upload or save packet contents, DNS requests, or browsing history.

## Build from source

Open this folder in Android Studio, install Android SDK 35, and use JDK 17 or newer. The included Gradle wrapper uses Gradle 8.11.1 and Android Gradle Plugin 8.9.1.

```sh
./gradlew :app:assembleDebug
```

The project has no runtime third-party dependencies. Java source targets Java 8 and uses only Android framework APIs.

The lightweight build used in this workspace is also included:

```sh
STILL_ANDROID_SDK=/path/to/android-sdk STILL_ECJ_JAR=/path/to/ecj-3.41.0.jar bash scripts/test.sh
STILL_ANDROID_SDK=/path/to/android-sdk STILL_ECJ_JAR=/path/to/ecj-3.41.0.jar bash scripts/build.sh release
```

It requires Android SDK Platform 35, Build Tools 35.0.0, Eclipse ECJ 3.41.0, Python 3, Java 17+, and keytool. The signing key stays in ignored `.signing/`. This APK is locally signed and is not a Play Store publication. For your own release distribution, generate and protect your own signing key.

## Verification

See `VALIDATION.md` for checks performed and device-testing limitations. Core checks cover network-specific rules, the empty-allowlist safeguard, Quiet Mode, billing windows across month/DST boundaries, and unavailable-vs-zero usage.
