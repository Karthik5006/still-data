# Still Data

A native Android data-usage monitor and local firewall with an original, peaceful alpine interface. Built for Android 10–15+ and suitable for Galaxy S25 Ultra. No account, server, analytics, or Internet permission.

## Installable build

[Download the APK](downloads/StillData-1.1-release.apk) · [Quick setup guide](downloads/SETUP.md)

This is a first locally signed build. Compilation, APK signing, and 37 core logic checks passed. Android device behavior remains unverified because the available emulator could not complete startup. See [validation details](VALIDATION.md).

## Features

- Device and per-app mobile/Wi-Fi usage from Android NetworkStatsManager, with Today, seven-day, and billing-cycle views.
- Every installed app, including system apps; shared Android UIDs are grouped so usage is not double-counted. Search and progressive loading keep the app list responsive.
- Independent mobile and Wi-Fi blocking rules. A local VpnService captures only selected apps and discards their IPv4/IPv6 packets. Allowed apps bypass it and retain their connection.
- Independent Mobile and Wi-Fi controls: pause/resume each rule set, block every app on either network, or allow every app on either network. Per-app switches and usage sorting follow the selected network.
- Persistent rules, a billing budget, real seven-day usage chart, blocked-packet counters, and a foreground notification with a stop action.
- System-wide domain-based ad/tracker blocking through Android Private DNS, with setup for AdGuard DNS or Control D. The app verifies the active provider using Android's network state; it cannot silently change that system setting.
- A visible, interactive alpine landscape with density-scaled snowfall, drifting clouds, a moving skier, gentle screen transitions, and touch ripples. Animation status explains app, Battery Saver, and system animation pauses.
- Enable all / Disable all for five local features: tracking, the app firewall, mobile rules, Wi-Fi rules, and animations. Saved app choices are retained. Private DNS remains a separate Android setting, with explicit enable and disable guides.

## Install on your S25 Ultra

1. Transfer `StillData-1.1-release.apk` to your phone and open it. Allow installation from the browser or Files app if Android asks.
2. Open Still Data. Tap **Allow usage access**, enable Still Data in Android settings, then return.
3. In **Apps**, choose **Mobile data** or **Wi-Fi**. Use that network’s rule switch, Block all / Allow all buttons, or individual app access switches.
4. In **Guard**, tap **Enable firewall** and approve Android's VPN prompt. Allow notifications so the status and stop action are easy to reach.
5. For ads, tap **Enable ads filter**, copy the hostname, choose **Private DNS provider hostname** in Android settings, paste it, and save. Return and tap **Check DNS status**.
6. If Samsung stops the foreground service, add Still Data to **Never auto sleeping apps** in Samsung Battery settings. No broad battery or device administrator permissions are requested.

## New in 1.1

- Master feature buttons on Overview, Guard, and Settings, plus individual feature switches.
- Separate Mobile and Wi-Fi usage totals, history charts, access rules, and all-app blocking controls.
- Pausing one network does not clear or affect the other network’s saved rules. Allow all clears only the selected network’s rules.
- Usage tracking can be disabled without disabling the firewall; Android’s own system counters continue.
- Animations are visible in a dedicated Overview panel rather than hidden behind cards. The scene can be dragged horizontally. Vertical scrolling still works over the panel.
- Android Private DNS has separate enable/disable actions because third-party apps cannot silently change the system’s DNS setting.

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

See `VALIDATION.md` for checks performed and device-testing limitations. Core checks cover master feature configuration, independent network switches and block-all modes, animation advance/pause/resume behavior, network-specific rules, the empty-allowlist safeguard, Quiet Mode, billing windows across month/DST boundaries, and unavailable-vs-zero usage.
