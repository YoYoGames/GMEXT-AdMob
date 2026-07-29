# GMEXT-AdMob
Repository for GameMaker's AdMob Extension

This repository was created with the intent of presenting users with the latest version available of the extension (even previous to marketplace updates) and also provide a way for the community to contribute with bug fixes and feature implementation.

This extension wraps Google's [Mobile Ads SDK](https://developers.google.com/admob) (Android `play-services-ads` 25.4.0 + `user-messaging-platform` 4.0.0, iOS `Google-Mobile-Ads-SDK` 12.0.0), letting you show Banner, Interstitial, Rewarded, Rewarded Interstitial and App Open ads, target ad content to an audience, and manage GDPR consent. It will work both with Android and iOS platform exports. However note that iOS version will require [installing CocoaPods](https://help.yoyogames.com/hc/en-us/articles/360008958858-iOS-and-tvOS-Using-CocoaPods) in your system (**on later versions of the IDE installing cocoapods is no longer necessary**)

> [!IMPORTANT]
> **Version 2.0.0 is a breaking change.** The extension has been fully rewritten onto the current Google Mobile Ads SDK with a new callback-based API (snake_case `admob_*` functions that take a GML callback function directly, replacing the old `AdMob_*` functions and their `async_load`-based social events). Projects upgrading from an earlier (1.x) version will need to adjust their code. Check [the documentation](../../wiki) for the current API.

ANDROID SOURCE:
`source/AdMob_gml/extensions/GMAdMob/AndroidSource/Java/`

IOS SOURCE:
`source/AdMob_gml/extensions/GMAdMob/source/src/ios/`

This repository also bundles 5 mediation sub-extensions alongside the main `GMAdMob` extension:
`AdMobAppLovin`, `AdMobIronSource`, `AdMobMeta`, `AdMobPangle`, and `AdMobUnityAds`.

---

## Important

Do not download from the **main branch** this branch is a work in place branch and probably has features that might be broken or not working properly, please download from the releases panel (right side instead).

---

## Documentation

* Check [the documentation](../../wiki)

The online documentation is regularly updated to ensure it contains the most current information. For those who prefer a different format, we also offer a HTML version. This HTML is directly converted from the GitHub Wiki content, ensuring consistency, although it may follow slightly behind in updates.

We encourage users to refer primarily to the GitHub Wiki for the latest information and updates. The HTML version, included with the extension and within the demo project's data files, serves as a secondary, static reference.

Additionally, if you're contributing new features through PR (Pull Requests), we kindly ask that you also provide accompanying documentation for these features, to maintain the comprehensiveness and usefulness of our resources.

---
