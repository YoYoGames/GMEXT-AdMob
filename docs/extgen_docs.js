/**
 * @function_partial admob_initialize
 * @param {Function} callback
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_set_test_device_id
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_events_on_paid_event
 * @param {Bool} enabled
 * @param {Function} callback
 * @function_end
 */

/**
 * @function_partial admob_banner_set_ad_unit
 * @param {String} ad_unit_id
 * @function_end
 */

/**
 * @function_partial admob_banner_create
 * @param {Enum.AdMobBannerSize} size
 * @param {Bool} bottom
 * @param {Function} callback
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_banner_create_ext
 * @param {Enum.AdMobBannerSize} size
 * @param {Bool} bottom
 * @param {Enum.AdMobBannerAlignment} alignment
 * @param {Function} callback
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_banner_get_width
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_banner_get_height
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_banner_move
 * @param {Bool} bottom
 * @function_end
 */

/**
 * @function_partial admob_banner_show
 * @function_end
 */

/**
 * @function_partial admob_banner_hide
 * @function_end
 */

/**
 * @function_partial admob_banner_remove
 * @function_end
 */

/**
 * @function_partial admob_interstitial_set_ad_unit
 * @param {String} ad_unit_id
 * @function_end
 */

/**
 * @function_partial admob_interstitial_load
 * @param {Function} callback
 * @param {String} [ad_unit_id]
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_interstitial_is_valid
 * @param {Real} handle
 * @returns {Bool}
 * @function_end
 */

/**
 * @function_partial admob_interstitial_dispose
 * @param {Real} handle
 * @function_end
 */

/**
 * @function_partial admob_interstitial_show
 * @param {Real} handle
 * @param {Function} callback
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_server_side_verification_set
 * @param {String} user_id
 * @param {String} custom_data
 * @function_end
 */

/**
 * @function_partial admob_server_side_verification_clear
 * @function_end
 */

/**
 * @function_partial admob_rewarded_video_set_ad_unit
 * @param {String} ad_unit_id
 * @function_end
 */

/**
 * @function_partial admob_rewarded_video_load
 * @param {Function} callback
 * @param {String} [ad_unit_id]
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_rewarded_video_is_valid
 * @param {Real} handle
 * @returns {Bool}
 * @function_end
 */

/**
 * @function_partial admob_rewarded_video_dispose
 * @param {Real} handle
 * @function_end
 */

/**
 * @function_partial admob_rewarded_video_show
 * @param {Real} handle
 * @param {Function} callback
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_rewarded_interstitial_set_ad_unit
 * @param {String} ad_unit_id
 * @function_end
 */

/**
 * @function_partial admob_rewarded_interstitial_load
 * @param {Function} callback
 * @param {String} [ad_unit_id]
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_rewarded_interstitial_is_valid
 * @param {Real} handle
 * @returns {Bool}
 * @function_end
 */

/**
 * @function_partial admob_rewarded_interstitial_dispose
 * @param {Real} handle
 * @function_end
 */

/**
 * @function_partial admob_rewarded_interstitial_show
 * @param {Real} handle
 * @param {Function} callback
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_app_open_ad_set_ad_unit
 * @param {String} ad_unit_id
 * @function_end
 */

/**
 * @function_partial admob_app_open_ad_enable
 * @param {Function} callback
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_app_open_ad_disable
 * @function_end
 */

/**
 * @function_partial admob_app_open_ad_is_enabled
 * @returns {Bool}
 * @function_end
 */

/**
 * @function_partial admob_app_open_ad_is_loaded
 * @returns {Bool}
 * @function_end
 */

/**
 * @function_partial admob_app_open_ad_load
 * @param {Function} callback
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_app_open_ad_show
 * @param {Function} callback
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_targeting_coppa
 * @param {Bool} coppa
 * @function_end
 */

/**
 * @function_partial admob_targeting_under_age
 * @param {Bool} under_age
 * @function_end
 */

/**
 * @function_partial admob_targeting_max_ad_content_rating
 * @param {Enum.AdMobMaxAdContentRating} content_rating
 * @function_end
 */

/**
 * @function_partial admob_consent_request_info_update
 * @param {Enum.AdMobConsentDebugGeography} debug_geography
 * @param {Function} callback
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_consent_get_status
 * @returns {Enum.AdMobConsentStatus}
 * @function_end
 */

/**
 * @function_partial admob_consent_get_type
 * @returns {Enum.AdMobConsentType}
 * @function_end
 */

/**
 * @function_partial admob_consent_is_form_available
 * @returns {Bool}
 * @function_end
 */

/**
 * @function_partial admob_consent_load
 * @param {Function} callback
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_consent_show
 * @param {Function} callback
 * @returns {Enum.AdMobError}
 * @function_end
 */

/**
 * @function_partial admob_consent_reset
 * @function_end
 */

/**
 * @function_partial admob_consent_set_rdp
 * @param {Bool} enabled
 * @function_end
 */

/**
 * @function_partial admob_settings_set_volume
 * @param {Real} value
 * @function_end
 */

/**
 * @function_partial admob_settings_set_muted
 * @param {Bool} muted
 * @function_end
 */

/**
 * @struct_partial AdMobResult
 * @member {Bool} success
 * @member {String} [error_message]
 * @member {Real} [sdk_error_code]
 * @struct_end
 */

/**
 * @struct_partial AdMobReward
 * @member {Real} amount
 * @member {String} type
 * @struct_end
 */

/**
 * @struct_partial AdMobPaidEvent
 * @member {Enum.AdMobAdType} ad_type
 * @member {String} ad_unit_id
 * @member {Real} value_micros
 * @member {String} currency_code
 * @member {Enum.AdMobPrecisionType} precision
 * @member {String} mediation_adapter_class_name
 * @member {String} [ad_source_name]
 * @member {String} [ad_source_id]
 * @member {String} [ad_source_instance_name]
 * @member {String} [ad_source_instance_id]
 * @struct_end
 */

/**
 * @enum_partial AdMobError
 * @member Ok
 * @member NotInitialized
 * @member InvalidAdId
 * @member AdLimitReached
 * @member NoAdsLoaded
 * @member NoActiveBannerAd
 * @member IllegalCall
 * @member NullViewHandler
 * @member InvalidHandle
 * @enum_end
 */

/**
 * @enum_partial AdMobBannerAlignment
 * @member Left
 * @member Center
 * @member Right
 * @enum_end
 */

/**
 * @enum_partial AdMobBannerSize
 * @member Banner
 * @member LargeBanner
 * @member MediumRectangle
 * @member FullBanner
 * @member Leaderboard
 * @member SmartBanner
 * @member AnchoredAdaptive
 * @enum_end
 */

/**
 * @enum_partial AdMobMaxAdContentRating
 * @member General
 * @member ParentalGuidance
 * @member Teen
 * @member MatureAudience
 * @enum_end
 */

/**
 * @enum_partial AdMobConsentDebugGeography
 * @member Disabled
 * @member EEA
 * @member NotEEA
 * @member RegulatedUSState
 * @member Other
 * @enum_end
 */

/**
 * @enum_partial AdMobConsentStatus
 * @member Unknown
 * @member NotRequired
 * @member Required
 * @member Obtained
 * @enum_end
 */

/**
 * @enum_partial AdMobConsentType
 * @member Unknown
 * @member NonPersonalized
 * @member Personalized
 * @member Declined
 * @enum_end
 */

/**
 * @enum_partial AdMobAdType
 * @member Banner
 * @member Interstitial
 * @member RewardedVideo
 * @member RewardedInterstitial
 * @member AppOpen
 * @enum_end
 */

/**
 * @enum_partial AdMobPrecisionType
 * @member Unknown
 * @member Estimated
 * @member PublisherProvided
 * @member Precise
 * @enum_end
 */

/**
 * @enum_partial AdMobBannerCallbackEvent
 * @member Loaded
 * @member LoadFailed
 * @member Opened
 * @member Clicked
 * @member Closed
 * @member Impression
 * @enum_end
 */

/**
 * @enum_partial AdMobInterstitialShowEvent
 * @member Shown
 * @member Dismissed
 * @member Clicked
 * @member Impression
 * @enum_end
 */

/**
 * @enum_partial AdMobRewardedVideoShowEvent
 * @member Shown
 * @member Dismissed
 * @member Clicked
 * @member Impression
 * @member Reward
 * @enum_end
 */

/**
 * @enum_partial AdMobRewardedInterstitialShowEvent
 * @member Shown
 * @member Dismissed
 * @member Clicked
 * @member Impression
 * @member Reward
 * @enum_end
 */

/**
 * @enum_partial AdMobAppOpenAdShowEvent
 * @member Shown
 * @member Dismissed
 * @member Clicked
 * @member Impression
 * @enum_end
 */

/**
 * @const_partial macros
 * @const_end
 */

