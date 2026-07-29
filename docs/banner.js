/**
 * @function admob_banner_set_ad_unit
 * @desc Sets the ad unit ID used by ${function.admob_banner_create}/${function.admob_banner_create_ext}.
 * Must be called before either of those.
 * @param {String} ad_unit_id The banner ad unit ID, from the AdMob dashboard.
 * @function_end
 */

/**
 * @function admob_banner_create
 * @desc Creates and loads a banner ad, horizontally centered, anchored to the top or bottom of the
 * screen. Only one banner exists at a time - calling this again replaces the previous banner.
 * @param {Enum.AdMobBannerSize} size The banner size to request.
 * @param {Bool} bottom `true` to anchor the banner to the bottom of the screen, `false` for the top.
 * @param {Function} callback The function to call for every lifecycle event on this banner, for as
 * long as it exists.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if the create request was accepted, an error
 * code otherwise.
 * @event callback
 * @desc Fires once per lifecycle event, for as long as this banner exists (not a one-shot
 * load-then-show pair like the other ad families - a banner has one ongoing callback for its whole
 * lifecycle).
 * @member {Struct.AdMobResult} result The event's result. `success` is `false` only for
 * ${constant.AdMobBannerCallbackEvent}.LoadFailed.
 * @member {Enum.AdMobBannerCallbackEvent} type Which lifecycle event this is.
 * @event_end
 * @example
 * ```gml
 * admob_banner_set_ad_unit("ca-app-pub-3940256099942544/6300978111");
 * admob_banner_create(AdMobBannerSize.Banner, true, function(_result, _type)
 * {
 *     if (_type == AdMobBannerCallbackEvent.LoadFailed)
 *         show_debug_message($"Banner failed to load: {_result.error_message}");
 *     else
 *         show_debug_message($"Banner event: {_type}");
 * });
 * ```
 * @function_end
 */

/**
 * @function admob_banner_create_ext
 * @desc Same as ${function.admob_banner_create}, with an explicit horizontal alignment instead of
 * always centering.
 * @param {Enum.AdMobBannerSize} size The banner size to request.
 * @param {Bool} bottom `true` to anchor the banner to the bottom of the screen, `false` for the top.
 * @param {Enum.AdMobBannerAlignment} alignment The horizontal alignment for the banner.
 * @param {Function} callback The function to call for every lifecycle event on this banner, for as
 * long as it exists.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if the create request was accepted, an error
 * code otherwise.
 * @event callback
 * @desc Fires once per lifecycle event, for as long as this banner exists. Same shape as
 * ${function.admob_banner_create}'s callback.
 * @member {Struct.AdMobResult} result The event's result. `success` is `false` only for
 * ${constant.AdMobBannerCallbackEvent}.LoadFailed.
 * @member {Enum.AdMobBannerCallbackEvent} type Which lifecycle event this is.
 * @event_end
 * @function_end
 */

/**
 * @function admob_banner_get_width
 * @desc Gets the width, in pixels, of the current banner ad.
 * @returns {Real} The banner's width in pixels, or `0` if no banner exists.
 * @function_end
 */

/**
 * @function admob_banner_get_height
 * @desc Gets the height, in pixels, of the current banner ad.
 * @returns {Real} The banner's height in pixels, or `0` if no banner exists.
 * @function_end
 */

/**
 * @function admob_banner_move
 * @desc Moves the current banner ad to the opposite edge of the screen.
 * @param {Bool} bottom `true` to anchor the banner to the bottom of the screen, `false` for the top.
 * @function_end
 */

/**
 * @function admob_banner_show
 * @desc Makes a previously-hidden banner ad visible again. Does nothing if no banner exists.
 * @function_end
 */

/**
 * @function admob_banner_hide
 * @desc Hides the current banner ad without destroying it - it keeps its loaded state and can be
 * shown again with ${function.admob_banner_show}. Does nothing if no banner exists.
 * @function_end
 */

/**
 * @function admob_banner_remove
 * @desc Destroys the current banner ad entirely. Call ${function.admob_banner_create}/
 * ${function.admob_banner_create_ext} again to create a new one.
 * @function_end
 */

/**
 * @const AdMobBannerAlignment
 * @desc Extension-defined horizontal banner placement values, used by
 * ${function.admob_banner_create_ext}.
 * @member Left
 * @member Center
 * @member Right
 * @const_end
 */

/**
 * @const AdMobBannerSize
 * @desc Extension-defined numeric mapping to Google's banner-size APIs.
 * @member Banner A standard 320x50 banner.
 * @member LargeBanner A 320x100 banner.
 * @member MediumRectangle A 300x250 banner.
 * @member FullBanner A 468x60 banner.
 * @member Leaderboard A 728x90 banner.
 * @member SmartBanner A screen-width, auto-height banner. Deprecated by Google in favor of adaptive
 * banners - kept for backward compatibility.
 * @member AnchoredAdaptive A screen-width banner with a height Google optimizes for the current
 * device and orientation. The recommended choice for new integrations.
 * @const_end
 */

/**
 * @const AdMobBannerCallbackEvent
 * @desc The lifecycle events ${function.admob_banner_create}/${function.admob_banner_create_ext}'s
 * callback can fire with. Numeric values are extension-owned.
 * @member Loaded The banner finished loading and is ready to show.
 * @member LoadFailed The banner failed to load. ${struct.AdMobResult}.success is `false` for this
 * event only.
 * @member Opened The banner was clicked and an overlay was presented.
 * @member Clicked The user clicked the banner.
 * @member Closed The overlay presented after a click was closed.
 * @member Impression The banner recorded an impression.
 * @const_end
 */

/**
 * @module banner
 * @title Banner
 * @desc Functions for creating and managing banner ads - a small ad that stays anchored to the top or
 * bottom of the screen.
 *
 * @section_func
 * @ref admob_banner_set_ad_unit
 * @ref admob_banner_create
 * @ref admob_banner_create_ext
 * @ref admob_banner_get_width
 * @ref admob_banner_get_height
 * @ref admob_banner_move
 * @ref admob_banner_show
 * @ref admob_banner_hide
 * @ref admob_banner_remove
 * @section_end
 *
 * @section_const
 * @ref AdMobBannerAlignment
 * @ref AdMobBannerSize
 * @ref AdMobBannerCallbackEvent
 * @section_end
 *
 * @module_end
 */
