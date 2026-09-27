# AutoSkip (Android)

Automatically taps the **"Skip ad" / "Saltar anuncio"** button in the YouTube app using an `AccessibilityService`.

[![Download APK](https://img.shields.io/github/v/release/Abelgs716/autoskip?label=Download%20APK)](https://github.com/Abelgs716/autoskip/releases/latest)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20the%20project-FF5E5B?logo=ko-fi&logoColor=white)](https://ko-fi.com/abelgs716)
[![MIT License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

## Download and install on your phone

1. Download `AutoSkip-1.1.apk` from [Releases](https://github.com/Abelgs716/autoskip/releases/latest) on your phone.
2. Open it and allow *Install unknown apps* for your browser or file manager when Android asks.
3. Open AutoSkip and follow the permissions screen: *Settings → Accessibility → AutoSkip → Enable*.
   - If you see **"Restricted setting"** (Android 13+): *Settings → Apps → AutoSkip → ⋮ → Allow restricted settings* and try again.
4. The status should show 🟢 **Active**. Open YouTube and AutoSkip will tap "Skip ad" as soon as it appears.

Requirements: Android 8.0+ and the official YouTube app.

### Install from PC via USB

With USB debugging enabled on your phone and [platform-tools](https://developer.android.com/tools/releases/platform-tools) installed in `%USERPROFILE%\AndroidDev\sdk`, run `instalar-en-movil.bat`. It installs `AutoSkip-1.1.apk` and enables the Accessibility service without going through Settings.

## Build

You need JDK 17 and the Android SDK (or [Android Studio](https://developer.android.com/studio), which includes both). Open the folder with *File → Open* or use the terminal:

```
gradlew test            # unit tests for text detection
gradlew assembleDebug   # test APK
gradlew assembleRelease # signed APK (requires keystore.properties)
```

To sign the release version, create `keystore.properties` at the root (it's in `.gitignore`, never committed):

```
storeFile=C:/path/to/autoskip-release.jks
storePassword=...
keyAlias=autoskip
keyPassword=...
```

All updates must be signed with **the same key**; if lost, users will need to uninstall to install a new version.

## How it works

| File | Purpose |
|---|---|
| `AutoSkipService.kt` | Background service. Only receives events from `com.google.android.youtube`, batches events (at most one scan per 250ms) and waits 1.5s before the next tap. |
| `SkipAdFinder.kt` | Finds the button: first by view ID (`skip_ad_button`, …; language independent) then by exact text match with "skip ad" phrases. |
| `SkipAdMatcher.kt` | "Skip ad" phrases in 30+ languages. Normalizes text (case, accents, punctuation). |
| `MainActivity.kt` | Status (🟢 / 🔴 / ⚠️), enable/disable toggle, and counter. |
| `PermissionActivity.kt` | Permission explanation and settings shortcuts. |

Safety measures to avoid tapping anything else:
- Bare words ("Skip", "Saltar") don't count; only full phrases or view IDs.
- Labels longer than 40 chars are ignored; node must be visible and enabled.
- If a tappable element is larger than 25% of the screen (e.g., the player), it's never tapped.
- No `INTERNET` permission: the app can't send data.

Works the same in portrait and landscape (uses absolute coordinates and total screen area).

## Tests run (Sep 26, 2026, Pixel 7 emulator, Android 15, YouTube 21.38.130)

| Test | Result |
|---|---|
| Portrait, YouTube English | ✅ 2 ads skipped |
| Landscape (fullscreen), English | ✅ 2 ads skipped, video plays normally |
| Portrait, YouTube Spanish | ✅ Skipped; "Saltar" / "Saltar anuncio" buttons |
| Text detection only (no IDs) | ✅ Skipped using "skip ad" phrases |
| Toggle disabled | ✅ 0 taps with button visible |
| Unskippable ads and banners | ✅ Leaves them alone |
| Reinstall/update | ✅ Service stays active |
| Signed release APK (R8) | ✅ Works like test version |

All taps logged were on `com.google.android.youtube:id/skip_ad_button` (logcat tag `AutoSkip`). Todo: test on real device.

## Maintenance

YouTube changes its UI frequently. If it stops working, enable *Developer options → Show layout bounds* or use *Layout Inspector* / `uiautomator dump` with an ad on screen to see the new button ID or text, then add it to `SKIP_VIEW_IDS` or `RAW_PHRASES` in `SkipAdMatcher.kt`.

## Google Play notice

Google Play policy prohibits apps that block or interfere with other apps' ads, and scrutinizes `AccessibilityService` use. Skipping ads may violate YouTube's Terms. That's why AutoSkip is distributed only as an APK on GitHub Releases, not on Play Store.

## iOS?

Not possible on iOS. Apple offers no API equivalent to `AccessibilityService`: each app lives in its own sandbox and can't read or tap another's UI. Shortcuts, Voice Control, or Switch Control can't automate this from third-party apps either, and the App Store won't allow it. The only official way to watch YouTube ad-free on iPhone is YouTube Premium.

## ❤️ Support the project

AutoSkip is **free, ad-free, open source and non-profit**. Donations on **[Ko-fi](https://ko-fi.com/abelgs716)** help us improve the organization and keep the project updated when YouTube changes its UI.

Giving the repository a ⭐ and sharing it with others also helps a lot.

## License

[MIT](LICENSE). AutoSkip is not affiliated with YouTube or Google.
