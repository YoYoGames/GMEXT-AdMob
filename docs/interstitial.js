/**
 * @function admob_interstitial_set_ad_unit
 * @desc Sets the default ad unit ID used by ${function.admob_interstitial_load} when its own
 * `ad_unit_id` argument is omitted.
 * @param {String} ad_unit_id The interstitial ad unit ID, from the AdMob dashboard.
 * @function_end
 */

/**
 * @function admob_interstitial_load
 * @desc Loads an interstitial ad. Multiple interstitials can be in flight at once - each successful
 * load produces its own independent `handle`, so you can pre-load several and show them later in any
 * order.
 * @param {Function} callback The function to call once the load completes or fails.
 * @param {String} [ad_unit_id] The ad unit ID to load. If omitted, uses the unit set by
 * ${function.admob_interstitial_set_ad_unit}.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if the load request was accepted, an error
 * code otherwise.
 * @event callback
 * @desc Fires once, when the load completes or fails.
 * @member {Struct.AdMobResult} result The load outcome.
 * @member {Real} [handle] A handle identifying this loaded ad instance, for use with
 * ${function.admob_interstitial_show}/${function.admob_interstitial_is_valid}/
 * ${function.admob_interstitial_dispose}. Only present on success.
 * @event_end
 * @example
 * ```gml
 * admob_interstitial_load(function(_result, _handle)
 * {
 *     if (_result.success)
 *         global.interstitial_handle = _handle;
 * });
 * ```
 * @function_end
 */

/**
 * @function admob_interstitial_is_valid
 * @desc Checks whether a handle still refers to a loaded, unshown interstitial ad instance.
 * @param {Real} handle A handle returned by ${function.admob_interstitial_load}.
 * @returns {Bool} `true` if the handle is still valid (loaded, not yet shown or disposed).
 * @function_end
 */

/**
 * @function admob_interstitial_dispose
 * @desc Releases a loaded interstitial ad instance without showing it. Does nothing if the handle is
 * already invalid.
 * @param {Real} handle A handle returned by ${function.admob_interstitial_load}.
 * @function_end
 */

/**
 * @function admob_interstitial_show
 * @desc Shows a loaded interstitial ad full-screen. `handle` is consumed by this call (whether it
 * succeeds or fails) - it cannot be shown again, even if `admob_interstitial_show` itself fails.
 * @param {Real} handle A handle returned by ${function.admob_interstitial_load}.
 * @param {Function} callback The function to call for the ad's show-lifecycle events.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if the show request was accepted,
 * ${constant.AdMobError}.InvalidHandle if `handle` is invalid, already shown, or already disposed, an
 * error code otherwise.
 * @event callback
 * @desc Fires once per show-lifecycle event on success; fires once with `success = false` and no
 * `type` if the ad fails to show.
 * @member {Struct.AdMobResult} result The event's result.
 * @member {Enum.AdMobInterstitialShowEvent} [type] Which show-lifecycle event this is. Absent when
 * `result.success` is `false`.
 * @event_end
 * @example
 * ```gml
 * admob_interstitial_show(global.interstitial_handle, function(_result, _type = undefined)
 * {
 *     if (!_result.success)
 *         show_debug_message($"Interstitial failed to show: {_result.error_message}");
 *     else if (_type == AdMobInterstitialShowEvent.Dismissed)
 *         show_debug_message("Interstitial dismissed");
 * });
 * ```
 * @function_end
 */

/**
 * @const AdMobInterstitialShowEvent
 * @desc The show-lifecycle events ${function.admob_interstitial_show}'s callback can fire with on
 * success. Numeric values are extension-owned.
 * @member Shown The interstitial was displayed.
 * @member Dismissed The user closed the interstitial.
 * @member Clicked The user clicked the interstitial.
 * @member Impression The interstitial recorded an impression.
 * @const_end
 */

/**
 * @module interstitial
 * @title Interstitial
 * @desc Functions for loading and showing interstitial ads - full-screen ads shown at natural
 * transition points in your game.
 *
 * @section_func
 * @ref admob_interstitial_set_ad_unit
 * @ref admob_interstitial_load
 * @ref admob_interstitial_is_valid
 * @ref admob_interstitial_dispose
 * @ref admob_interstitial_show
 * @section_end
 *
 * @section_const
 * @ref AdMobInterstitialShowEvent
 * @section_end
 *
 * @module_end
 */
