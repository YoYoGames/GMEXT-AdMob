@title Extension Options

# Extension Options

Open the AdMob extension's Extension Options from the Asset Browser (right-click the extension →
**Properties**, or double-click it) to fill in your app and ad unit IDs. Every ID below is a "test ad"
placeholder by default, from Google's own [test ad unit list](https://developers.google.com/admob/android/test-ads)
- replace every one with your real IDs (${page.setup}) before shipping.

## Android Options

| Option | Required | What it does |
|---|---|---|
| **Application ID** | Yes | Your app's AdMob Application ID (from the AdMob dashboard), used to initialize the SDK on Android. |
| **Banner Unit ID** | Only if using banners | The ad unit ID passed to ${function.admob_banner_create}/${function.admob_banner_create_ext} when you don't set one manually with ${function.admob_banner_set_ad_unit}. |
| **Interstitial Unit ID** | Only if using interstitials | The default interstitial ad unit ID. |
| **Rewarded Unit ID** | Only if using rewarded video | The default rewarded video ad unit ID. |
| **Rewarded Interstitial Unit ID** | Only if using rewarded interstitials | The default rewarded interstitial ad unit ID. |
| **App Open Ad ID** | Only if using app open ads | The default app open ad unit ID. |

## iOS Options

Same fields as Android, mirrored for the iOS platform: **Application ID**, **Banner Unit ID**,
**Interstitial Unit ID**, **Rewarded Unit ID**, **Rewarded Interstitial Unit ID**, **App Open Ad ID**.

## Extra Options

| Option | What it does |
|---|---|
| **Use Google Ad Manager** | Boolean, default off. Enable only if your app also integrates Google Ad Manager (`AdManagerAdView`/`AdManagerAdRequest`) alongside AdMob - leave disabled for a plain AdMob-only integration. Controls the `AD_MANAGER_APP`/`GADIsAdManagerApp` manifest and plist flags on Android and iOS respectively. |
| **Log Level** | A dropdown (`0`/`1`/`2`) controlling how much the extension logs to the native platform log (Logcat on Android, the device console on iOS). Higher is more verbose - raise it when diagnosing an issue, lower it for release builds. |

## Per-Ad-Unit vs. Per-Call Ad Units

Every ad family also has its own `admob_*_set_ad_unit` function (e.g.
${function.admob_interstitial_set_ad_unit}) that overrides the Extension Option value at runtime -
useful if you want to switch ad units without rebuilding, or if you manage multiple ad units per
family. The load functions for handle-based families (interstitial, rewarded video, rewarded
interstitial) also accept an optional `ad_unit_id` argument that overrides both the Extension Option
and any `set_ad_unit` call for that one load.
