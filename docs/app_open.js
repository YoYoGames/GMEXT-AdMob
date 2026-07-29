/**
 * @function admob_app_open_ad_set_ad_unit
 * @desc Sets the ad unit ID used by ${function.admob_app_open_ad_load}/
 * ${function.admob_app_open_ad_enable}.
 * @param {String} ad_unit_id The app open ad unit ID, from the AdMob dashboard.
 * @function_end
 */

/**
 * @function admob_app_open_ad_enable
 * @desc Turns on automatic app open ads: every time the app returns to the foreground, this
 * automatically loads (if needed) and shows an app open ad, using `callback` for both the load
 * outcome and the show-lifecycle events - unless ${function.admob_app_open_ad_load}/
 * ${function.admob_app_open_ad_show} are called manually with their own callback in the meantime.
 * This models Google's own `AppOpenAdManager` sample pattern for a cold-start/splash ad. There is
 * only ever one app open ad instance - no handles, unlike the interstitial/rewarded families.
 * @param {Function} callback The function to call for both the automatic load outcome and the
 * automatic show-lifecycle events.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if enabling succeeded, an error code
 * otherwise.
 * @event callback
 * @desc Fires once per automatic load outcome, then once per automatic show-lifecycle event whenever
 * the app returns to the foreground. Same argument shape as ${function.admob_app_open_ad_load}'s and
 * ${function.admob_app_open_ad_show}'s callbacks respectively.
 * @member {Struct.AdMobResult} result The event's result.
 * @member {Enum.AdMobAppOpenAdShowEvent} [type] Present only for a show-lifecycle event (absent for a
 * load outcome, or when `result.success` is `false`).
 * @event_end
 * @function_end
 */

/**
 * @function admob_app_open_ad_disable
 * @desc Turns off automatic app open ads, previously enabled with
 * ${function.admob_app_open_ad_enable}. The app no longer auto-loads or auto-shows an app open ad
 * when returning to the foreground.
 * @function_end
 */

/**
 * @function admob_app_open_ad_is_enabled
 * @desc Checks whether automatic app open ads are currently turned on.
 * @returns {Bool} `true` if ${function.admob_app_open_ad_enable} was called and
 * ${function.admob_app_open_ad_disable} hasn't been called since.
 * @function_end
 */

/**
 * @function admob_app_open_ad_is_loaded
 * @desc Checks whether an app open ad is currently loaded and ready to show.
 * @returns {Bool} `true` if an app open ad is loaded.
 * @function_end
 */

/**
 * @function admob_app_open_ad_load
 * @desc Manually loads an app open ad. There is only ever one app open ad instance - loading again
 * while one is already loaded is a no-op that immediately reports success.
 * @param {Function} callback The function to call once the load completes or fails.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if the load request was accepted, an error
 * code otherwise.
 * @event callback
 * @desc Fires once, when the load completes or fails.
 * @member {Struct.AdMobResult} result The load outcome.
 * @event_end
 * @function_end
 */

/**
 * @function admob_app_open_ad_show
 * @desc Manually shows the currently-loaded app open ad full-screen.
 * @param {Function} callback The function to call for the ad's show-lifecycle events.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if the show request was accepted,
 * ${constant.AdMobError}.NoAdsLoaded if no app open ad is currently loaded, an error code otherwise.
 * @event callback
 * @desc Fires once per show-lifecycle event on success; fires once with `success = false` and no
 * `type` if the ad fails to show.
 * @member {Struct.AdMobResult} result The event's result.
 * @member {Enum.AdMobAppOpenAdShowEvent} [type] Which show-lifecycle event this is. Absent when
 * `result.success` is `false`.
 * @event_end
 * @function_end
 */

/**
 * @const AdMobAppOpenAdShowEvent
 * @desc The show-lifecycle events ${function.admob_app_open_ad_show}'s (and, when auto-showing,
 * ${function.admob_app_open_ad_enable}'s) callback can fire with on success. Numeric values are
 * extension-owned.
 * @member Shown The app open ad was displayed.
 * @member Dismissed The user closed the app open ad.
 * @member Clicked The user clicked the app open ad.
 * @member Impression The app open ad recorded an impression.
 * @const_end
 */

/**
 * @module app_open
 * @title App Open Ads
 * @desc Functions for app open ads - a special format intended for monetizing your app's loading
 * screen. App open ads can be closed at any time and are designed to be shown when your users bring
 * your app to the foreground.
 *
 * @section_func
 * @ref admob_app_open_ad_set_ad_unit
 * @ref admob_app_open_ad_enable
 * @ref admob_app_open_ad_disable
 * @ref admob_app_open_ad_is_enabled
 * @ref admob_app_open_ad_is_loaded
 * @ref admob_app_open_ad_load
 * @ref admob_app_open_ad_show
 * @section_end
 *
 * @section_const
 * @ref AdMobAppOpenAdShowEvent
 * @section_end
 *
 * @module_end
 */
