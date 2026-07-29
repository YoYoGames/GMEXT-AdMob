/**
 * @struct AdMobResult
 * @desc The uniform success/failure envelope delivered to every async callback in this extension.
 * `error_message` and `sdk_error_code` are both absent on success. `sdk_error_code` carries Google's
 * own `LoadAdError`/`AdError` code verbatim when the failure came from the ad SDK itself; it is absent
 * when the extension rejected the call before ever reaching Google (see ${constant.AdMobError} instead).
 * @member {Bool} success Whether the operation succeeded.
 * @member {String} [error_message] The SDK's own error message. Only present on failure.
 * @member {Real} [sdk_error_code] Google's raw `LoadAdError`/`AdError` code. Only present when the
 * failure came from the SDK (not from this extension rejecting the call outright).
 * @struct_end
 */

/**
 * @struct AdMobReward
 * @desc The reward earned from a rewarded video or rewarded interstitial ad. Mirrors Google's
 * `RewardItem.getAmount()`/`getType()` directly.
 * @member {Real} amount The reward amount.
 * @member {String} type The reward type, as configured for the ad unit in the AdMob dashboard.
 * @struct_end
 */

/**
 * @struct AdMobPaidEvent
 * @desc Delivered by Google's `OnPaidEventListener` - always a success signal (the underlying SDK
 * event has no failure case), so unlike ${struct.AdMobResult} there is no `success`/`error_message`
 * pair. `ad_type` and `ad_unit_id` are enrichment this extension adds so a single shared subscription
 * (see ${function.admob_events_on_paid_event}) can tell which ad family and unit the impression came
 * from; the rest mirrors Google's `AdValue`/`AdapterResponseInfo` directly.
 * @member {Enum.AdMobAdType} ad_type Which ad family the impression came from.
 * @member {String} ad_unit_id The ad unit ID the impression came from.
 * @member {Real} value_micros The estimated earned value, in micro-units of `currency_code`.
 * @member {String} currency_code The ISO 4217 currency code (e.g. `"USD"`).
 * @member {Enum.AdMobPrecisionType} precision How precisely `value_micros` is known.
 * @member {String} mediation_adapter_class_name The class name of the mediation adapter that served
 * the ad, or the AdMob network's own class name if no mediation was involved.
 * @member {String} [ad_source_name] The name of the ad source (network) that served the ad. Only
 * present when Google's `AdapterResponseInfo` was available for this impression.
 * @member {String} [ad_source_id] The ID of the ad source that served the ad.
 * @member {String} [ad_source_instance_name] The name of the specific ad source instance/placement.
 * @member {String} [ad_source_instance_id] The ID of the specific ad source instance/placement.
 * @struct_end
 */

/**
 * @function admob_initialize
 * @desc Initializes the Google Mobile Ads SDK. Call this once, before any other AdMob function
 * (other than ${function.admob_set_test_device_id}, which must be called *before* this one if you
 * need test ads on the current device). Every other function in this extension fails with
 * ${constant.AdMobError}.NotInitialized until this has completed successfully.
 * @param {Function} callback The function to call once initialization completes or fails.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if the initialization request was accepted,
 * an error code if it was rejected outright (e.g. already initialized).
 * @event callback
 * @desc Fires once, when the SDK finishes starting up (or fails to).
 * @member {Struct.AdMobResult} result The initialization outcome.
 * @event_end
 * @example
 * ```gml
 * admob_initialize(function(_result)
 * {
 *     if (_result.success)
 *         show_debug_message("AdMob initialized");
 *     else
 *         show_debug_message($"AdMob failed to initialize: {_result.error_message}");
 * });
 * ```
 * @function_end
 */

/**
 * @function admob_set_test_device_id
 * @desc Marks the current device as a test device, so every ad request from this session returns a
 * Google test ad instead of a live ad. Must be called **before** ${function.admob_initialize} -
 * calling it after initialization returns ${constant.AdMobError}.IllegalCall.
 * [[Important: Never ship a build with this enabled - it must only be used during development.]]
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok on success,
 * ${constant.AdMobError}.IllegalCall if the SDK is already initialized.
 * @function_end
 */

/**
 * @function admob_events_on_paid_event
 * @desc Subscribes to (or unsubscribes from) Google's paid-event stream, shared across every ad
 * family this extension wraps. Once subscribed, `callback` fires once per ad impression that earns
 * revenue, for every banner/interstitial/rewarded video/rewarded interstitial/app open ad shown -
 * check ${struct.AdMobPaidEvent}.ad_type to tell which family it came from.
 * @param {Bool} enabled `true` to subscribe, `false` to unsubscribe (in which case `callback` is
 * ignored and can be `undefined`).
 * @param {Function} callback The function to call for every paid impression.
 * @event callback
 * @desc Fires once per ad impression that earns revenue, across every ad family.
 * @member {Struct.AdMobPaidEvent} event The paid-event details.
 * @event_end
 * @example
 * ```gml
 * admob_events_on_paid_event(true, function(_event)
 * {
 *     show_debug_message($"Earned {_event.value_micros / 1000000} {_event.currency_code} from {_event.ad_type}");
 * });
 * ```
 * @function_end
 */

/**
 * @function admob_server_side_verification_set
 * @desc Configures the custom data Google's [Server-Side Verification](https://developers.google.com/admob/android/ssv)
 * feature attaches to the reward callback your backend receives when a rewarded video or rewarded
 * interstitial ad is completed. Applies to every rewarded ad loaded after this call.
 * @param {String} user_id An opaque identifier for the current user, echoed back to your server in
 * the SSV callback.
 * @param {String} custom_data Arbitrary custom data, echoed back to your server in the SSV callback.
 * @function_end
 */

/**
 * @function admob_server_side_verification_clear
 * @desc Clears the server-side-verification data previously set with
 * ${function.admob_server_side_verification_set}. Rewarded ads loaded after this call carry no SSV
 * custom data.
 * @function_end
 */

/**
 * @const AdMobError
 * @desc Extension-defined error codes returned synchronously by most functions in this extension.
 * These are **not** raw Google Mobile Ads SDK error codes - a Google SDK failure is instead reported
 * asynchronously via ${struct.AdMobResult}.sdk_error_code in the relevant callback.
 * @member Ok The call succeeded.
 * @member NotInitialized ${function.admob_initialize} has not completed successfully yet.
 * @member InvalidAdId No ad unit ID has been set for this ad family (or it is empty).
 * @member AdLimitReached Reserved for future use.
 * @member NoAdsLoaded The requested ad instance has not been loaded (or has already been shown/disposed).
 * @member NoActiveBannerAd No banner has been created yet.
 * @member IllegalCall The call is not valid in the SDK's current state (e.g. calling
 * ${function.admob_set_test_device_id} after ${function.admob_initialize}).
 * @member NullViewHandler The game's view is not available yet (too early in the app lifecycle).
 * @member InvalidHandle The handle passed does not refer to a currently-loaded ad instance - it may
 * already have been shown, disposed, or never existed.
 * @const_end
 */

/**
 * @const AdMobAdType
 * @desc Distinguishes which ad family a shared ${function.admob_events_on_paid_event} callback fired
 * for. Extension-owned - Google's own `AdValue` type carries no ad-family concept.
 * @member Banner
 * @member Interstitial
 * @member RewardedVideo
 * @member RewardedInterstitial
 * @member AppOpen
 * @const_end
 */

/**
 * @const AdMobPrecisionType
 * @desc How precisely ${struct.AdMobPaidEvent}.value_micros is known. Mirrors Android
 * `AdValue.PrecisionType`/iOS `GADAdValuePrecision` directly - both platforms already use these same
 * ordinals, so no translation is needed.
 * @member Unknown
 * @member Estimated
 * @member PublisherProvided
 * @member Precise
 * @const_end
 */

/**
 * @const macros
 * @const_end
 */

/**
 * @module general
 * @title General
 * @desc Initialization, the shared paid-event stream, server-side verification, and the shared result
 * types every other module's callbacks use.
 *
 * @section_func
 * @desc Initialization and cross-family functions.
 * @ref admob_initialize
 * @ref admob_set_test_device_id
 * @ref admob_events_on_paid_event
 * @ref admob_server_side_verification_set
 * @ref admob_server_side_verification_clear
 * @section_end
 *
 * @section_struct
 * @desc Shared data types used across every module.
 * @ref AdMobResult
 * @ref AdMobReward
 * @ref AdMobPaidEvent
 * @section_end
 *
 * @section_const
 * @ref AdMobError
 * @ref AdMobAdType
 * @ref AdMobPrecisionType
 * @section_end
 *
 * @module_end
 */
