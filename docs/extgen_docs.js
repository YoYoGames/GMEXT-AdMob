/**
 * @function_partial admob_initialize
 * @param {Function} callback
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_set_test_device_id
 * @returns {Real}
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
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_banner_create_ext
 * @param {Enum.AdMobBannerSize} size
 * @param {Bool} bottom
 * @param {Enum.AdMobBannerAlignment} alignment
 * @param {Function} callback
 * @returns {Real}
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
 * @function_partial admob_interstitial_free_loaded_instances
 * @param {Real} count
 * @function_end
 */

/**
 * @function_partial admob_interstitial_max_instances
 * @param {Real} value
 * @function_end
 */

/**
 * @function_partial admob_interstitial_load
 * @param {Function} callback
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_interstitial_show
 * @param {Function} callback
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_interstitial_is_loaded
 * @returns {Bool}
 * @function_end
 */

/**
 * @function_partial admob_interstitial_instances_count
 * @returns {Real}
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
 * @function_partial admob_rewarded_video_free_loaded_instances
 * @param {Real} count
 * @function_end
 */

/**
 * @function_partial admob_rewarded_video_max_instances
 * @param {Real} value
 * @function_end
 */

/**
 * @function_partial admob_rewarded_video_load
 * @param {Function} callback
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_rewarded_video_show
 * @param {Function} callback
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_rewarded_video_is_loaded
 * @returns {Bool}
 * @function_end
 */

/**
 * @function_partial admob_rewarded_video_instances_count
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_rewarded_interstitial_set_ad_unit
 * @param {String} ad_unit_id
 * @function_end
 */

/**
 * @function_partial admob_rewarded_interstitial_free_loaded_instances
 * @param {Real} count
 * @function_end
 */

/**
 * @function_partial admob_rewarded_interstitial_max_instances
 * @param {Real} value
 * @function_end
 */

/**
 * @function_partial admob_rewarded_interstitial_load
 * @param {Function} callback
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_rewarded_interstitial_show
 * @param {Function} callback
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_rewarded_interstitial_is_loaded
 * @returns {Bool}
 * @function_end
 */

/**
 * @function_partial admob_rewarded_interstitial_instances_count
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_app_open_ad_set_ad_unit
 * @param {String} ad_unit_id
 * @function_end
 */

/**
 * @function_partial admob_app_open_ad_enable
 * @param {Real} orientation
 * @param {Function} callback
 * @returns {Real}
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
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_app_open_ad_show
 * @param {Function} callback
 * @returns {Real}
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
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_consent_get_status
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_consent_get_type
 * @returns {Real}
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
 * @returns {Real}
 * @function_end
 */

/**
 * @function_partial admob_consent_show
 * @param {Function} callback
 * @returns {Real}
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
 * @enum_partial AdMobError
 * @member Ok
 * @member NotInitialized
 * @member InvalidAdId
 * @member AdLimitReached
 * @member NoAdsLoaded
 * @member NoActiveBannerAd
 * @member IllegalCall
 * @member NullViewHandler
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
 * @member Required
 * @member NotRequired
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
 * @enum_partial AdMobInitializeCallbackEvent
 * @member Initialized
 * @member Failed
 * @enum_end
 */

/**
 * @enum_partial AdMobBannerCallbackEvent
 * @member Loaded
 * @member LoadFailed
 * @member Opened
 * @member Clicked
 * @member Closed
 * @enum_end
 */

/**
 * @enum_partial AdMobInterstitialCallbackEvent
 * @member Loaded
 * @member LoadFailed
 * @member Shown
 * @member ShowFailed
 * @member Dismissed
 * @enum_end
 */

/**
 * @enum_partial AdMobRewardedVideoCallbackEvent
 * @member Loaded
 * @member LoadFailed
 * @member Shown
 * @member ShowFailed
 * @member Dismissed
 * @member Reward
 * @enum_end
 */

/**
 * @enum_partial AdMobRewardedInterstitialCallbackEvent
 * @member Loaded
 * @member LoadFailed
 * @member Shown
 * @member ShowFailed
 * @member Dismissed
 * @member Reward
 * @enum_end
 */

/**
 * @enum_partial AdMobAppOpenAdCallbackEvent
 * @member Loaded
 * @member LoadFailed
 * @member Shown
 * @member ShowFailed
 * @member Dismissed
 * @enum_end
 */

/**
 * @enum_partial AdMobConsentCallbackEvent
 * @member RequestInfoUpdated
 * @member RequestInfoUpdateFailed
 * @member Loaded
 * @member LoadFailed
 * @member Dismissed
 * @member ShowFailed
 * @enum_end
 */

/**
 * @enum_partial AdMobPaidEventCallbackEvent
 * @member Paid
 * @enum_end
 */

/**
 * @const_partial macros
 * @const_end
 */

