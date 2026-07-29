## IMPORTANT

- This extension is to be used with GM 2023.11 and future releases (compatible with LTSr2)
- Requires CocoaPods when used on iOS platforms (is not required on newer versions of IDE)
- Requires the [App Tracking Transparency](https://github.com/YoYoGames/GMEXT-AppTrackingTransparency) extension on iOS platforms.
- Works with Android and iOS
- **Version 2.0.0 is a breaking change**: the extension has been fully rewritten onto the current Google Mobile Ads SDK with a new callback-based API (snake_case `admob_*` functions that take a GML callback function directly, replacing the old `AdMob_*` functions and their `async_load`-based social events). Projects upgrading from an earlier (1.x) version will need to adjust their code. Check the documentation for the current API.

## DESCRIPTION

This extension wraps Google's Mobile Ads SDK, allowing users to add and control Ads inside their application/game (AKA AdMob) - Banner, Interstitial, Rewarded, Rewarded Interstitial and App Open - it also includes General Data Protection (GDPR) consent handling via Google's User Messaging Platform.

## FEATURES

- Creating, removing, moving, hiding and showing Banner ads.
- Loading, showing and disposing Interstitial, Rewarded Video and Rewarded Interstitial ads via caller-held handles, with optional server-side reward verification.
- Loading, showing, enabling and auto-showing App Open ads.
- Requesting info update, checking status/type, loading, showing and resetting GDPR consent, including Restricted Data Processing.
- Targeting ads content by COPPA / under-age / max ad content rating.
- Controlling mute and volume settings.
- Allows for AdUnitId switching mid run.
- Allows for loading multiple ads ahead of time (each tracked by its own handle).
- Subscribing to paid-event (ad revenue) callbacks.

## CHANGES SINCE ${releaseOldVersion}

https://github.com/YoYoGames/GMEXT-AdMob/compare/${releaseOldVersion}...${releaseNewVersion}

## DOCUMENTATION

The full documentation of the API is included in the extension asset (included files).

## NOTES

For testing you just need to compile and run the project (it uses default Google Dummy AppID)
