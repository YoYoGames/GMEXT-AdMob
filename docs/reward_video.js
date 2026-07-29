/**
 * @function admob_rewarded_video_set_ad_unit
 * @desc Sets the default ad unit ID used by ${function.admob_rewarded_video_load} when its own
 * `ad_unit_id` argument is omitted.
 * @param {String} ad_unit_id The rewarded video ad unit ID, from the AdMob dashboard.
 * @function_end
 */

/**
 * @function admob_rewarded_video_load
 * @desc Loads a rewarded video ad. Multiple rewarded videos can be in flight at once - each
 * successful load produces its own independent `handle`, so you can pre-load several and show them
 * later in any order.
 * @param {Function} callback The function to call once the load completes or fails.
 * @param {String} [ad_unit_id] The ad unit ID to load. If omitted, uses the unit set by
 * ${function.admob_rewarded_video_set_ad_unit}.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if the load request was accepted, an error
 * code otherwise.
 * @event callback
 * @desc Fires once, when the load completes or fails.
 * @member {Struct.AdMobResult} result The load outcome.
 * @member {Real} [handle] A handle identifying this loaded ad instance, for use with
 * ${function.admob_rewarded_video_show}/${function.admob_rewarded_video_is_valid}/
 * ${function.admob_rewarded_video_dispose}. Only present on success.
 * @event_end
 * @function_end
 */

/**
 * @function admob_rewarded_video_is_valid
 * @desc Checks whether a handle still refers to a loaded, unshown rewarded video ad instance.
 * @param {Real} handle A handle returned by ${function.admob_rewarded_video_load}.
 * @returns {Bool} `true` if the handle is still valid (loaded, not yet shown or disposed).
 * @function_end
 */

/**
 * @function admob_rewarded_video_dispose
 * @desc Releases a loaded rewarded video ad instance without showing it. Does nothing if the handle
 * is already invalid.
 * @param {Real} handle A handle returned by ${function.admob_rewarded_video_load}.
 * @function_end
 */

/**
 * @function admob_rewarded_video_show
 * @desc Shows a loaded rewarded video ad full-screen. `handle` is consumed by this call (whether it
 * succeeds or fails) - it cannot be shown again, even if `admob_rewarded_video_show` itself fails.
 * @param {Real} handle A handle returned by ${function.admob_rewarded_video_load}.
 * @param {Function} callback The function to call for the ad's show-lifecycle events.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if the show request was accepted,
 * ${constant.AdMobError}.InvalidHandle if `handle` is invalid, already shown, or already disposed, an
 * error code otherwise.
 * @event callback
 * @desc Fires once per show-lifecycle event on success; fires once with `success = false` and no
 * `type` if the ad fails to show.
 * @member {Struct.AdMobResult} result The event's result.
 * @member {Enum.AdMobRewardedVideoShowEvent} [type] Which show-lifecycle event this is. Absent when
 * `result.success` is `false`.
 * @member {Struct.AdMobReward} [reward] The reward the user earned. Only present when `type` is
 * ${constant.AdMobRewardedVideoShowEvent}.Reward.
 * @event_end
 * @example
 * ```gml
 * admob_rewarded_video_show(global.rewarded_handle, function(_result, _type = undefined, _reward = undefined)
 * {
 *     if (_type == AdMobRewardedVideoShowEvent.Reward)
 *         show_debug_message($"Earned {_reward.amount} {_reward.type}");
 * });
 * ```
 * @function_end
 */

/**
 * @const AdMobRewardedVideoShowEvent
 * @desc The show-lifecycle events ${function.admob_rewarded_video_show}'s callback can fire with on
 * success. Numeric values are extension-owned.
 * @member Shown The rewarded video was displayed.
 * @member Dismissed The user closed the rewarded video.
 * @member Clicked The user clicked the rewarded video.
 * @member Impression The rewarded video recorded an impression.
 * @member Reward The user earned a reward - the callback's `reward` argument is present for this
 * event only.
 * @const_end
 */

/**
 * @module reward_video
 * @title Rewarded Video
 * @desc Functions for loading and showing rewarded video ads - full-screen ads that grant the player
 * an in-game reward for watching to completion.
 *
 * @section_func
 * @ref admob_rewarded_video_set_ad_unit
 * @ref admob_rewarded_video_load
 * @ref admob_rewarded_video_is_valid
 * @ref admob_rewarded_video_dispose
 * @ref admob_rewarded_video_show
 * @section_end
 *
 * @section_const
 * @ref AdMobRewardedVideoShowEvent
 * @section_end
 *
 * @module_end
 */
