
/**
 * @module home
 * @title AdMob
 *
 * @section Extension's Features
 * @desc
 *
 * * Initialize the Google Mobile Ads SDK
 * * Show banner, interstitial, rewarded video, rewarded interstitial, and app open ads
 * * Request and read the user's consent status (GDPR/CCPA) via Google's User Messaging Platform
 * * Target ads by COPPA/under-age/max content rating
 * * Track ad revenue via a shared paid-event stream
 * * Configure server-side reward verification and app-wide ad volume
 *
 * @section_end
 *
 * @section Introduction
 *
 * @desc
 *
 * This extension wraps Google's [Mobile Ads SDK](https://developers.google.com/admob) for **Android
 * and iOS**. Call ${function.admob_initialize} once, before any other function (other than the
 * targeting functions and ${function.admob_set_test_device_id}, which must be called *before*
 * initialization). Every function that talks to Google's servers reports its outcome asynchronously
 * through a callback carrying a ${struct.AdMobResult} - check `result.success` before trusting the
 * rest of the payload.
 *
 * Interstitial, rewarded video, and rewarded interstitial ads share the same load/show shape: loading
 * produces a handle you hold onto, then showing consumes that handle. Multiple ads can be pre-loaded
 * at once by holding several handles. Banner and app open ads work differently - there is only ever
 * one instance of each, so no handles are involved.
 *
 * @section_end
 *
 * @section Guides
 * @desc Guides for the AdMob extension.
 * @reference page.getting_started
 * @reference page.extension_options
 * @section_end
 *
 * @section Modules
 * @desc The following are the available modules for the AdMob extension:
 *
 * @reference module.general
 * @reference module.banner
 * @reference module.interstitial
 * @reference module.reward_video
 * @reference module.reward_interstitial
 * @reference module.app_open
 * @reference module.targeting
 * @reference module.consent
 * @reference module.settings
 *
 * @section_end
 *
 * @module_end
 */
