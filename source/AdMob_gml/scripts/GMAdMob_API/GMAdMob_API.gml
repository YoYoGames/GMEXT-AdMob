// ##### extgen :: Auto-generated file do not edit!! #####

// #####################################################################
// # Macros
// #####################################################################

// #####################################################################
// # Enums
// #####################################################################

enum AdMobError
{
    Ok = 0,
    NotInitialized = -1,
    InvalidAdId = -2,
    AdLimitReached = -3,
    NoAdsLoaded = -4,
    NoActiveBannerAd = -5,
    IllegalCall = -6,
    NullViewHandler = -7
}

enum AdMobBannerAlignment
{
    Left = 0,
    Center = 1,
    Right = 2
}

enum AdMobBannerSize
{
    Banner = 0,
    LargeBanner = 1,
    MediumRectangle = 2,
    FullBanner = 3,
    Leaderboard = 4,
    SmartBanner = 5,
    AnchoredAdaptive = 6
}

enum AdMobMaxAdContentRating
{
    General = 0,
    ParentalGuidance = 1,
    Teen = 2,
    MatureAudience = 3
}

enum AdMobConsentDebugGeography
{
    Disabled = 0,
    EEA = 1,
    NotEEA = 2,
    RegulatedUSState = 3,
    Other = 4
}

enum AdMobConsentStatus
{
    Unknown = 0,
    NotRequired = 1,
    Required = 2,
    Obtained = 3
}

enum AdMobConsentType
{
    Unknown = 0,
    NonPersonalized = 1,
    Personalized = 2,
    Declined = 3
}

enum AdMobInitializeCallbackEvent
{
    Initialized = 0,
    Failed = 1
}

enum AdMobBannerCallbackEvent
{
    Loaded = 0,
    LoadFailed = 1,
    Opened = 2,
    Clicked = 3,
    Closed = 4
}

enum AdMobInterstitialCallbackEvent
{
    Loaded = 0,
    LoadFailed = 1,
    Shown = 2,
    ShowFailed = 3,
    Dismissed = 4
}

enum AdMobRewardedVideoCallbackEvent
{
    Loaded = 0,
    LoadFailed = 1,
    Shown = 2,
    ShowFailed = 3,
    Dismissed = 4,
    Reward = 5
}

enum AdMobRewardedInterstitialCallbackEvent
{
    Loaded = 0,
    LoadFailed = 1,
    Shown = 2,
    ShowFailed = 3,
    Dismissed = 4,
    Reward = 5
}

enum AdMobAppOpenAdCallbackEvent
{
    Loaded = 0,
    LoadFailed = 1,
    Shown = 2,
    ShowFailed = 3,
    Dismissed = 4
}

enum AdMobConsentCallbackEvent
{
    RequestInfoUpdated = 0,
    RequestInfoUpdateFailed = 1,
    Loaded = 2,
    LoadFailed = 3,
    Dismissed = 4,
    ShowFailed = 5
}

enum AdMobPaidEventCallbackEvent
{
    Paid = 0
}

// #####################################################################
// # Constructors
// #####################################################################

// #####################################################################
// # Codecs
// #####################################################################

// #####################################################################
// # Functions
// #####################################################################

/**
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_initialize(_callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_initialize(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

// Skipping function admob_set_test_device_id (no wrapper is required)


/**
 * @param {Bool} _enabled
 * @param {Function} _callback
 */
function admob_events_on_paid_event(_enabled, _callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _enabled, type: Bool
    if (!is_bool(_enabled)) show_error($"{_GMFUNCTION_} :: _enabled expected bool", true);
    buffer_write(__args_buffer, buffer_bool, _enabled);

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_events_on_paid_event(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

// Skipping function admob_banner_set_ad_unit (no wrapper is required)


/**
 * @param {Enum.AdMobBannerSize} _size
 * @param {Bool} _bottom
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_banner_create(_size, _bottom, _callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _size, type: enum AdMobBannerSize

    if (!is_numeric(_size)) show_error($"{_GMFUNCTION_} :: _size expected number", true);
    buffer_write(__args_buffer, buffer_s32, _size);

    // param: _bottom, type: Bool
    if (!is_bool(_bottom)) show_error($"{_GMFUNCTION_} :: _bottom expected bool", true);
    buffer_write(__args_buffer, buffer_bool, _bottom);

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_banner_create(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

/**
 * @param {Enum.AdMobBannerSize} _size
 * @param {Bool} _bottom
 * @param {Enum.AdMobBannerAlignment} _alignment
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_banner_create_ext(_size, _bottom, _alignment, _callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _size, type: enum AdMobBannerSize

    if (!is_numeric(_size)) show_error($"{_GMFUNCTION_} :: _size expected number", true);
    buffer_write(__args_buffer, buffer_s32, _size);

    // param: _bottom, type: Bool
    if (!is_bool(_bottom)) show_error($"{_GMFUNCTION_} :: _bottom expected bool", true);
    buffer_write(__args_buffer, buffer_bool, _bottom);

    // param: _alignment, type: enum AdMobBannerAlignment

    if (!is_numeric(_alignment)) show_error($"{_GMFUNCTION_} :: _alignment expected number", true);
    buffer_write(__args_buffer, buffer_s32, _alignment);

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_banner_create_ext(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

// Skipping function admob_banner_get_width (no wrapper is required)


// Skipping function admob_banner_get_height (no wrapper is required)


// Skipping function admob_banner_move (no wrapper is required)


// Skipping function admob_banner_show (no wrapper is required)


// Skipping function admob_banner_hide (no wrapper is required)


// Skipping function admob_banner_remove (no wrapper is required)


// Skipping function admob_interstitial_set_ad_unit (no wrapper is required)


// Skipping function admob_interstitial_free_loaded_instances (no wrapper is required)


// Skipping function admob_interstitial_max_instances (no wrapper is required)


/**
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_interstitial_load(_callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_interstitial_load(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

/**
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_interstitial_show(_callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_interstitial_show(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

// Skipping function admob_interstitial_is_loaded (no wrapper is required)


// Skipping function admob_interstitial_instances_count (no wrapper is required)


// Skipping function admob_server_side_verification_set (no wrapper is required)


// Skipping function admob_server_side_verification_clear (no wrapper is required)


// Skipping function admob_rewarded_video_set_ad_unit (no wrapper is required)


// Skipping function admob_rewarded_video_free_loaded_instances (no wrapper is required)


// Skipping function admob_rewarded_video_max_instances (no wrapper is required)


/**
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_rewarded_video_load(_callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_rewarded_video_load(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

/**
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_rewarded_video_show(_callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_rewarded_video_show(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

// Skipping function admob_rewarded_video_is_loaded (no wrapper is required)


// Skipping function admob_rewarded_video_instances_count (no wrapper is required)


// Skipping function admob_rewarded_interstitial_set_ad_unit (no wrapper is required)


// Skipping function admob_rewarded_interstitial_free_loaded_instances (no wrapper is required)


// Skipping function admob_rewarded_interstitial_max_instances (no wrapper is required)


/**
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_rewarded_interstitial_load(_callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_rewarded_interstitial_load(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

/**
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_rewarded_interstitial_show(_callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_rewarded_interstitial_show(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

// Skipping function admob_rewarded_interstitial_is_loaded (no wrapper is required)


// Skipping function admob_rewarded_interstitial_instances_count (no wrapper is required)


// Skipping function admob_app_open_ad_set_ad_unit (no wrapper is required)


/**
 * @param {Real} _orientation
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_app_open_ad_enable(_orientation, _callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _orientation, type: Float64
    if (!is_numeric(_orientation)) show_error($"{_GMFUNCTION_} :: _orientation expected number", true);
    buffer_write(__args_buffer, buffer_f64, _orientation);

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_app_open_ad_enable(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

// Skipping function admob_app_open_ad_disable (no wrapper is required)


// Skipping function admob_app_open_ad_is_enabled (no wrapper is required)


// Skipping function admob_app_open_ad_is_loaded (no wrapper is required)


/**
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_app_open_ad_load(_callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_app_open_ad_load(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

/**
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_app_open_ad_show(_callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_app_open_ad_show(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

// Skipping function admob_targeting_coppa (no wrapper is required)


// Skipping function admob_targeting_under_age (no wrapper is required)


/**
 * @param {Enum.AdMobMaxAdContentRating} _content_rating
 */
function admob_targeting_max_ad_content_rating(_content_rating)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _content_rating, type: enum AdMobMaxAdContentRating

    if (!is_numeric(_content_rating)) show_error($"{_GMFUNCTION_} :: _content_rating expected number", true);
    buffer_write(__args_buffer, buffer_s32, _content_rating);

    var _return_value = __admob_targeting_max_ad_content_rating(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

/**
 * @param {Enum.AdMobConsentDebugGeography} _debug_geography
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_consent_request_info_update(_debug_geography, _callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _debug_geography, type: enum AdMobConsentDebugGeography

    if (!is_numeric(_debug_geography)) show_error($"{_GMFUNCTION_} :: _debug_geography expected number", true);
    buffer_write(__args_buffer, buffer_s32, _debug_geography);

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_consent_request_info_update(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

// Skipping function admob_consent_get_status (no wrapper is required)


// Skipping function admob_consent_get_type (no wrapper is required)


// Skipping function admob_consent_is_form_available (no wrapper is required)


/**
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_consent_load(_callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_consent_load(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

/**
 * @param {Function} _callback
 * @returns {Real}
 */
function admob_consent_show(_callback)
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var _return_value = __admob_consent_show(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return _return_value;
}

// Skipping function admob_consent_reset (no wrapper is required)


// Skipping function admob_consent_set_rdp (no wrapper is required)


// Skipping function admob_settings_set_volume (no wrapper is required)


// Skipping function admob_settings_set_muted (no wrapper is required)


/// @ignore
function __GMAdMob_get_decoders()
{
    static __decoders = [];
    return __decoders;
}
/// @ignore
function __GMAdMob_get_dispatcher()
{
    static __available = __GMAdMob_is_available();
    if (!__available) return;

    static __dispatcher = new __GMNativeFunctionDispatcher(__GMAdMob_invocation_handler, __GMAdMob_get_decoders());
    return __dispatcher;
}
/// @ignore
function __GMAdMob_is_available()
{
    static __available = extension_exists("GMAdMob");
    return __available;
}
