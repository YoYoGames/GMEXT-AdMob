@title Getting Started

# Getting Started

This guide walks through the recommended call order for the AdMob extension, from initialization
through showing your first ad. See ${page.setup} first if you haven't yet set up your AdMob dashboard
account or configured the extension's Extension Options, and ${page.extension_options} for what each
option does.

## Prerequisites

* An AdMob account with your app and ad units already created (${page.setup}).
* The extension's Extension Options filled in with your **Application ID**s and **Ad Unit ID**s
(${page.extension_options}).
* **iOS only:** if you intend to show personalized ads, import the separate **AppTrackingTransparency**
extension from the Marketplace and request tracking permission (in an initialization room, before
${function.admob_initialize}) - this is required by Apple, not by AdMob itself.

## 1. Configure targeting (optional, before initialization)

If you need COPPA/under-age/max-content-rating targeting, or test ads on the current device, set them
up now - all of these must be called **before** ${function.admob_initialize}:

```gml
admob_set_test_device_id(); // development only - never ship this call
admob_targeting_coppa(false);
admob_targeting_under_age(false);
admob_targeting_max_ad_content_rating(AdMobMaxAdContentRating.General);
```

## 2. Initialize

```gml
admob_initialize(function(_result)
{
    if (!_result.success)
    {
        show_debug_message($"AdMob failed to initialize: {_result.error_message}");
        return;
    }

    // Safe to request consent info / load ads from here on.
    admob_consent_request_info_update(AdMobConsentDebugGeography.Disabled, function(_consent_result)
    {
        if (_consent_result.success && admob_consent_get_status() == AdMobConsentStatus.Required)
        {
            admob_consent_load(function(_load_result)
            {
                if (_load_result.success)
                    admob_consent_show(function(_show_result) { /* consent flow finished */ });
            });
        }
    });
});
```

See ${module.consent} for the full consent flow, including debug geographies for testing.

## 3. Load and show ads

Every ad family follows the same pattern: set the ad unit, load, then show. Interstitial, rewarded
video, and rewarded interstitial ads use a **handle** returned by their load callback - hold onto it
and pass it to the matching show function:

```gml
admob_interstitial_set_ad_unit(""); // your Ad Unit ID, or leave the Extension Option value in place

admob_interstitial_load(function(_result, _handle)
{
    if (_result.success)
        global.interstitial_handle = _handle;
});

// ...later, e.g. at a natural transition point in your game...

admob_interstitial_show(global.interstitial_handle, function(_result, _type = undefined)
{
    if (_type == AdMobInterstitialShowEvent.Dismissed)
        show_debug_message("Interstitial dismissed - resume gameplay");
});
```

Banner and app open ads don't use handles - there's only ever one instance of each. See
${module.banner} and ${module.app_open} for their specifics.

## 4. Handling callbacks

Every async function in this extension delivers its outcome through a callback carrying a
${struct.AdMobResult} (or, for show callbacks, an additional event-type argument). Always check
`result.success` first:

```gml
function(_result, _type = undefined)
{
    if (!_result.success)
    {
        show_debug_message($"Failed: {_result.error_message} (SDK code {_result.sdk_error_code})");
        return;
    }
    // handle _type
}
```

## 5. Cleanup

Dispose of any loaded-but-unshown ad instances you no longer need with
${function.admob_interstitial_dispose}/${function.admob_rewarded_video_dispose}/
${function.admob_rewarded_interstitial_dispose}, and remove an active banner with
${function.admob_banner_remove} when you're done with it. Ads that have already been shown are
consumed automatically - no cleanup call is needed for them.

## Testing notes

Call ${function.admob_set_test_device_id} before ${function.admob_initialize} during development so
every ad request returns a Google test ad instead of a live one - **never ship a build with this
call left in**. Use ${constant.AdMobConsentDebugGeography} with
${function.admob_consent_request_info_update} to test the GDPR consent flow as if the device were in
a specific region, without needing to actually be there.
