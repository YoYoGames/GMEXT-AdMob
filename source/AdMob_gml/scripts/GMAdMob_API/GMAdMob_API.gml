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
    NullViewHandler = -7,
    InvalidHandle = -8
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

enum AdMobAdType
{
    Banner = 0,
    Interstitial = 1,
    RewardedVideo = 2,
    RewardedInterstitial = 3,
    AppOpen = 4
}

enum AdMobPrecisionType
{
    Unknown = 0,
    Estimated = 1,
    PublisherProvided = 2,
    Precise = 3
}

enum AdMobBannerCallbackEvent
{
    Loaded = 0,
    LoadFailed = 1,
    Opened = 2,
    Clicked = 3,
    Closed = 4,
    Impression = 5
}

enum AdMobInterstitialShowEvent
{
    Shown = 0,
    Dismissed = 1,
    Clicked = 2,
    Impression = 3
}

enum AdMobRewardedVideoShowEvent
{
    Shown = 0,
    Dismissed = 1,
    Clicked = 2,
    Impression = 3,
    Reward = 4
}

enum AdMobRewardedInterstitialShowEvent
{
    Shown = 0,
    Dismissed = 1,
    Clicked = 2,
    Impression = 3,
    Reward = 4
}

enum AdMobAppOpenAdShowEvent
{
    Shown = 0,
    Dismissed = 1,
    Clicked = 2,
    Impression = 3
}

// #####################################################################
// # Constructors
// #####################################################################

/**
 * @returns {Struct.AdMobResult}
 */
function AdMobResult() constructor
{
    /**
     * Internally generated hash for quick validation
     * @ignore
     */
    static __uid = 1608421935;

    self.success = undefined;
    self.error_message = undefined;
    self.sdk_error_code = undefined;

}

/**
 * @returns {Struct.AdMobReward}
 */
function AdMobReward() constructor
{
    /**
     * Internally generated hash for quick validation
     * @ignore
     */
    static __uid = 2987584937;

    self.amount = undefined;
    self.type = undefined;

}

/**
 * @returns {Struct.AdMobPaidEvent}
 */
function AdMobPaidEvent() constructor
{
    /**
     * Internally generated hash for quick validation
     * @ignore
     */
    static __uid = 587962964;

    self.ad_type = undefined;
    self.ad_unit_id = undefined;
    self.value_micros = undefined;
    self.currency_code = undefined;
    self.precision = undefined;
    self.mediation_adapter_class_name = undefined;
    self.ad_source_name = undefined;
    self.ad_source_id = undefined;
    self.ad_source_instance_name = undefined;
    self.ad_source_instance_id = undefined;

}

// #####################################################################
// # Codecs
// #####################################################################

/**
 * @func __AdMobResult_encode(_inst, _buffer, _offset, _where)
 * @param {Struct.AdMobResult} _inst
 * @param {Id.Buffer} _buffer
 * @param {Real} _offset
 * @param {String} _where
 * @ignore
 */
function __AdMobResult_encode(_inst, _buffer, _offset, _where = _GMFUNCTION_)
{
    buffer_seek(_buffer, buffer_seek_start, _offset);
    with (_inst)
    {
        // field: success, type: Bool
        if (!is_bool(self.success)) show_error($"{_where} :: self.success expected bool", true);
        buffer_write(_buffer, buffer_bool, self.success);

        // field: error_message, type: optional<String>
        if (is_undefined(self.error_message))
        {
            buffer_write(_buffer, buffer_bool, false);
        }
        else
        {
            buffer_write(_buffer, buffer_bool, true);
            if (!is_string(self.error_message)) show_error($"{_where} :: self.error_message expected string", true);
            buffer_write(_buffer, buffer_u32, string_byte_length(self.error_message));
            buffer_write(_buffer, buffer_string, self.error_message);
        }

        // field: sdk_error_code, type: optional<Int32>
        if (is_undefined(self.sdk_error_code))
        {
            buffer_write(_buffer, buffer_bool, false);
        }
        else
        {
            buffer_write(_buffer, buffer_bool, true);
            if (!is_numeric(self.sdk_error_code)) show_error($"{_where} :: self.sdk_error_code expected number", true);
            buffer_write(_buffer, buffer_s32, self.sdk_error_code);
        }

    }
}

/**
 * @func __AdMobResult_decode(_buffer, _offset)
 * @param {Id.Buffer} _buffer
 * @param {Real} _offset
 * @returns {Struct.AdMobResult}
 * @ignore
 */
function __AdMobResult_decode(_buffer, _offset)
{
    buffer_seek(_buffer, buffer_seek_start, _offset);

    _inst = new AdMobResult();
    with (_inst)
    {
        // field: success, type: Bool
        self.success = buffer_read(_buffer, buffer_bool);

        // field: error_message, type: optional<String>
        if (buffer_read(_buffer, buffer_bool))
        {
            buffer_read(_buffer, buffer_u32);
            self.error_message = buffer_read(_buffer, buffer_string);
        }
        else
        {
            self.error_message = undefined;
        }

        // field: sdk_error_code, type: optional<Int32>
        if (buffer_read(_buffer, buffer_bool))
        {
            self.sdk_error_code = buffer_read(_buffer, buffer_s32);
        }
        else
        {
            self.sdk_error_code = undefined;
        }

    }

    return _inst;
}

/**
 * @func __AdMobReward_encode(_inst, _buffer, _offset, _where)
 * @param {Struct.AdMobReward} _inst
 * @param {Id.Buffer} _buffer
 * @param {Real} _offset
 * @param {String} _where
 * @ignore
 */
function __AdMobReward_encode(_inst, _buffer, _offset, _where = _GMFUNCTION_)
{
    buffer_seek(_buffer, buffer_seek_start, _offset);
    with (_inst)
    {
        // field: amount, type: Float64
        if (!is_numeric(self.amount)) show_error($"{_where} :: self.amount expected number", true);
        buffer_write(_buffer, buffer_f64, self.amount);

        // field: type, type: String
        if (!is_string(self.type)) show_error($"{_where} :: self.type expected string", true);
        buffer_write(_buffer, buffer_u32, string_byte_length(self.type));
        buffer_write(_buffer, buffer_string, self.type);

    }
}

/**
 * @func __AdMobReward_decode(_buffer, _offset)
 * @param {Id.Buffer} _buffer
 * @param {Real} _offset
 * @returns {Struct.AdMobReward}
 * @ignore
 */
function __AdMobReward_decode(_buffer, _offset)
{
    buffer_seek(_buffer, buffer_seek_start, _offset);

    _inst = new AdMobReward();
    with (_inst)
    {
        // field: amount, type: Float64
        self.amount = buffer_read(_buffer, buffer_f64);

        // field: type, type: String
        buffer_read(_buffer, buffer_u32);
        self.type = buffer_read(_buffer, buffer_string);

    }

    return _inst;
}

/**
 * @func __AdMobPaidEvent_encode(_inst, _buffer, _offset, _where)
 * @param {Struct.AdMobPaidEvent} _inst
 * @param {Id.Buffer} _buffer
 * @param {Real} _offset
 * @param {String} _where
 * @ignore
 */
function __AdMobPaidEvent_encode(_inst, _buffer, _offset, _where = _GMFUNCTION_)
{
    buffer_seek(_buffer, buffer_seek_start, _offset);
    with (_inst)
    {
        // field: ad_type, type: enum AdMobAdType

        if (!is_numeric(self.ad_type)) show_error($"{_where} :: self.ad_type expected number", true);
        buffer_write(_buffer, buffer_s32, self.ad_type);

        // field: ad_unit_id, type: String
        if (!is_string(self.ad_unit_id)) show_error($"{_where} :: self.ad_unit_id expected string", true);
        buffer_write(_buffer, buffer_u32, string_byte_length(self.ad_unit_id));
        buffer_write(_buffer, buffer_string, self.ad_unit_id);

        // field: value_micros, type: Float64
        if (!is_numeric(self.value_micros)) show_error($"{_where} :: self.value_micros expected number", true);
        buffer_write(_buffer, buffer_f64, self.value_micros);

        // field: currency_code, type: String
        if (!is_string(self.currency_code)) show_error($"{_where} :: self.currency_code expected string", true);
        buffer_write(_buffer, buffer_u32, string_byte_length(self.currency_code));
        buffer_write(_buffer, buffer_string, self.currency_code);

        // field: precision, type: enum AdMobPrecisionType

        if (!is_numeric(self.precision)) show_error($"{_where} :: self.precision expected number", true);
        buffer_write(_buffer, buffer_s32, self.precision);

        // field: mediation_adapter_class_name, type: String
        if (!is_string(self.mediation_adapter_class_name)) show_error($"{_where} :: self.mediation_adapter_class_name expected string", true);
        buffer_write(_buffer, buffer_u32, string_byte_length(self.mediation_adapter_class_name));
        buffer_write(_buffer, buffer_string, self.mediation_adapter_class_name);

        // field: ad_source_name, type: optional<String>
        if (is_undefined(self.ad_source_name))
        {
            buffer_write(_buffer, buffer_bool, false);
        }
        else
        {
            buffer_write(_buffer, buffer_bool, true);
            if (!is_string(self.ad_source_name)) show_error($"{_where} :: self.ad_source_name expected string", true);
            buffer_write(_buffer, buffer_u32, string_byte_length(self.ad_source_name));
            buffer_write(_buffer, buffer_string, self.ad_source_name);
        }

        // field: ad_source_id, type: optional<String>
        if (is_undefined(self.ad_source_id))
        {
            buffer_write(_buffer, buffer_bool, false);
        }
        else
        {
            buffer_write(_buffer, buffer_bool, true);
            if (!is_string(self.ad_source_id)) show_error($"{_where} :: self.ad_source_id expected string", true);
            buffer_write(_buffer, buffer_u32, string_byte_length(self.ad_source_id));
            buffer_write(_buffer, buffer_string, self.ad_source_id);
        }

        // field: ad_source_instance_name, type: optional<String>
        if (is_undefined(self.ad_source_instance_name))
        {
            buffer_write(_buffer, buffer_bool, false);
        }
        else
        {
            buffer_write(_buffer, buffer_bool, true);
            if (!is_string(self.ad_source_instance_name)) show_error($"{_where} :: self.ad_source_instance_name expected string", true);
            buffer_write(_buffer, buffer_u32, string_byte_length(self.ad_source_instance_name));
            buffer_write(_buffer, buffer_string, self.ad_source_instance_name);
        }

        // field: ad_source_instance_id, type: optional<String>
        if (is_undefined(self.ad_source_instance_id))
        {
            buffer_write(_buffer, buffer_bool, false);
        }
        else
        {
            buffer_write(_buffer, buffer_bool, true);
            if (!is_string(self.ad_source_instance_id)) show_error($"{_where} :: self.ad_source_instance_id expected string", true);
            buffer_write(_buffer, buffer_u32, string_byte_length(self.ad_source_instance_id));
            buffer_write(_buffer, buffer_string, self.ad_source_instance_id);
        }

    }
}

/**
 * @func __AdMobPaidEvent_decode(_buffer, _offset)
 * @param {Id.Buffer} _buffer
 * @param {Real} _offset
 * @returns {Struct.AdMobPaidEvent}
 * @ignore
 */
function __AdMobPaidEvent_decode(_buffer, _offset)
{
    buffer_seek(_buffer, buffer_seek_start, _offset);

    _inst = new AdMobPaidEvent();
    with (_inst)
    {
        // field: ad_type, type: enum AdMobAdType
        self.ad_type = buffer_read(_buffer, buffer_s32);

        // field: ad_unit_id, type: String
        buffer_read(_buffer, buffer_u32);
        self.ad_unit_id = buffer_read(_buffer, buffer_string);

        // field: value_micros, type: Float64
        self.value_micros = buffer_read(_buffer, buffer_f64);

        // field: currency_code, type: String
        buffer_read(_buffer, buffer_u32);
        self.currency_code = buffer_read(_buffer, buffer_string);

        // field: precision, type: enum AdMobPrecisionType
        self.precision = buffer_read(_buffer, buffer_s32);

        // field: mediation_adapter_class_name, type: String
        buffer_read(_buffer, buffer_u32);
        self.mediation_adapter_class_name = buffer_read(_buffer, buffer_string);

        // field: ad_source_name, type: optional<String>
        if (buffer_read(_buffer, buffer_bool))
        {
            buffer_read(_buffer, buffer_u32);
            self.ad_source_name = buffer_read(_buffer, buffer_string);
        }
        else
        {
            self.ad_source_name = undefined;
        }

        // field: ad_source_id, type: optional<String>
        if (buffer_read(_buffer, buffer_bool))
        {
            buffer_read(_buffer, buffer_u32);
            self.ad_source_id = buffer_read(_buffer, buffer_string);
        }
        else
        {
            self.ad_source_id = undefined;
        }

        // field: ad_source_instance_name, type: optional<String>
        if (buffer_read(_buffer, buffer_bool))
        {
            buffer_read(_buffer, buffer_u32);
            self.ad_source_instance_name = buffer_read(_buffer, buffer_string);
        }
        else
        {
            self.ad_source_instance_name = undefined;
        }

        // field: ad_source_instance_id, type: optional<String>
        if (buffer_read(_buffer, buffer_bool))
        {
            buffer_read(_buffer, buffer_u32);
            self.ad_source_instance_id = buffer_read(_buffer, buffer_string);
        }
        else
        {
            self.ad_source_instance_id = undefined;
        }

    }

    return _inst;
}

// #####################################################################
// # Functions
// #####################################################################

/**
 * @param {Function} _callback
 * @returns {Enum.AdMobError}
 */
function admob_initialize(_callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_initialize(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

/**
 * @returns {Enum.AdMobError}
 */
function admob_set_test_device_id()
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_set_test_device_id(buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

/**
 * @param {Bool} _enabled
 * @param {Function} _callback
 */
function admob_events_on_paid_event(_enabled, _callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _enabled, type: Bool
    if (!is_bool(_enabled)) show_error($"{_GMFUNCTION_} :: _enabled expected bool", true);
    buffer_write(__args_buffer, buffer_bool, _enabled);

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __return_value__ = __admob_events_on_paid_event(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return __return_value__;
}

// Skipping function admob_banner_set_ad_unit (no wrapper is required)


/**
 * @param {Enum.AdMobBannerSize} _size
 * @param {Bool} _bottom
 * @param {Function} _callback
 * @returns {Enum.AdMobError}
 */
function admob_banner_create(_size, _bottom, _callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _size, type: enum AdMobBannerSize

    if (!is_numeric(_size)) show_error($"{_GMFUNCTION_} :: _size expected number", true);
    buffer_write(__args_buffer, buffer_s32, _size);

    // param: _bottom, type: Bool
    if (!is_bool(_bottom)) show_error($"{_GMFUNCTION_} :: _bottom expected bool", true);
    buffer_write(__args_buffer, buffer_bool, _bottom);

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_banner_create(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

/**
 * @param {Enum.AdMobBannerSize} _size
 * @param {Bool} _bottom
 * @param {Enum.AdMobBannerAlignment} _alignment
 * @param {Function} _callback
 * @returns {Enum.AdMobError}
 */
function admob_banner_create_ext(_size, _bottom, _alignment, _callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

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
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_banner_create_ext(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

// Skipping function admob_banner_get_width (no wrapper is required)


// Skipping function admob_banner_get_height (no wrapper is required)


// Skipping function admob_banner_move (no wrapper is required)


// Skipping function admob_banner_show (no wrapper is required)


// Skipping function admob_banner_hide (no wrapper is required)


// Skipping function admob_banner_remove (no wrapper is required)


// Skipping function admob_interstitial_set_ad_unit (no wrapper is required)


/**
 * @param {Function} _callback
 * @param {String} _ad_unit_id
 * @returns {Enum.AdMobError}
 */
function admob_interstitial_load(_callback, _ad_unit_id)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    // param: _ad_unit_id, type: optional<String>
    if (is_undefined(_ad_unit_id))
    {
        buffer_write(__args_buffer, buffer_bool, false);
    }
    else
    {
        buffer_write(__args_buffer, buffer_bool, true);
        if (!is_string(_ad_unit_id)) show_error($"{_GMFUNCTION_} :: _ad_unit_id expected string", true);
        buffer_write(__args_buffer, buffer_u32, string_byte_length(_ad_unit_id));
        buffer_write(__args_buffer, buffer_string, _ad_unit_id);
    }

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_interstitial_load(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

/**
 * @param {Real} _handle
 * @returns {Bool}
 */
function admob_interstitial_is_valid(_handle)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _handle, type: UInt64
    if (!is_numeric(_handle)) show_error($"{_GMFUNCTION_} :: _handle expected number", true);
    buffer_write(__args_buffer, buffer_u64, _handle);

    var __return_value__ = __admob_interstitial_is_valid(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return __return_value__;
}

/**
 * @param {Real} _handle
 */
function admob_interstitial_dispose(_handle)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _handle, type: UInt64
    if (!is_numeric(_handle)) show_error($"{_GMFUNCTION_} :: _handle expected number", true);
    buffer_write(__args_buffer, buffer_u64, _handle);

    var __return_value__ = __admob_interstitial_dispose(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return __return_value__;
}

/**
 * @param {Real} _handle
 * @param {Function} _callback
 * @returns {Enum.AdMobError}
 */
function admob_interstitial_show(_handle, _callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _handle, type: UInt64
    if (!is_numeric(_handle)) show_error($"{_GMFUNCTION_} :: _handle expected number", true);
    buffer_write(__args_buffer, buffer_u64, _handle);

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_interstitial_show(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

// Skipping function admob_server_side_verification_set (no wrapper is required)


// Skipping function admob_server_side_verification_clear (no wrapper is required)


// Skipping function admob_rewarded_video_set_ad_unit (no wrapper is required)


/**
 * @param {Function} _callback
 * @param {String} _ad_unit_id
 * @returns {Enum.AdMobError}
 */
function admob_rewarded_video_load(_callback, _ad_unit_id)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    // param: _ad_unit_id, type: optional<String>
    if (is_undefined(_ad_unit_id))
    {
        buffer_write(__args_buffer, buffer_bool, false);
    }
    else
    {
        buffer_write(__args_buffer, buffer_bool, true);
        if (!is_string(_ad_unit_id)) show_error($"{_GMFUNCTION_} :: _ad_unit_id expected string", true);
        buffer_write(__args_buffer, buffer_u32, string_byte_length(_ad_unit_id));
        buffer_write(__args_buffer, buffer_string, _ad_unit_id);
    }

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_rewarded_video_load(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

/**
 * @param {Real} _handle
 * @returns {Bool}
 */
function admob_rewarded_video_is_valid(_handle)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _handle, type: UInt64
    if (!is_numeric(_handle)) show_error($"{_GMFUNCTION_} :: _handle expected number", true);
    buffer_write(__args_buffer, buffer_u64, _handle);

    var __return_value__ = __admob_rewarded_video_is_valid(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return __return_value__;
}

/**
 * @param {Real} _handle
 */
function admob_rewarded_video_dispose(_handle)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _handle, type: UInt64
    if (!is_numeric(_handle)) show_error($"{_GMFUNCTION_} :: _handle expected number", true);
    buffer_write(__args_buffer, buffer_u64, _handle);

    var __return_value__ = __admob_rewarded_video_dispose(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return __return_value__;
}

/**
 * @param {Real} _handle
 * @param {Function} _callback
 * @returns {Enum.AdMobError}
 */
function admob_rewarded_video_show(_handle, _callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _handle, type: UInt64
    if (!is_numeric(_handle)) show_error($"{_GMFUNCTION_} :: _handle expected number", true);
    buffer_write(__args_buffer, buffer_u64, _handle);

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_rewarded_video_show(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

// Skipping function admob_rewarded_interstitial_set_ad_unit (no wrapper is required)


/**
 * @param {Function} _callback
 * @param {String} _ad_unit_id
 * @returns {Enum.AdMobError}
 */
function admob_rewarded_interstitial_load(_callback, _ad_unit_id)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    // param: _ad_unit_id, type: optional<String>
    if (is_undefined(_ad_unit_id))
    {
        buffer_write(__args_buffer, buffer_bool, false);
    }
    else
    {
        buffer_write(__args_buffer, buffer_bool, true);
        if (!is_string(_ad_unit_id)) show_error($"{_GMFUNCTION_} :: _ad_unit_id expected string", true);
        buffer_write(__args_buffer, buffer_u32, string_byte_length(_ad_unit_id));
        buffer_write(__args_buffer, buffer_string, _ad_unit_id);
    }

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_rewarded_interstitial_load(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

/**
 * @param {Real} _handle
 * @returns {Bool}
 */
function admob_rewarded_interstitial_is_valid(_handle)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _handle, type: UInt64
    if (!is_numeric(_handle)) show_error($"{_GMFUNCTION_} :: _handle expected number", true);
    buffer_write(__args_buffer, buffer_u64, _handle);

    var __return_value__ = __admob_rewarded_interstitial_is_valid(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return __return_value__;
}

/**
 * @param {Real} _handle
 */
function admob_rewarded_interstitial_dispose(_handle)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _handle, type: UInt64
    if (!is_numeric(_handle)) show_error($"{_GMFUNCTION_} :: _handle expected number", true);
    buffer_write(__args_buffer, buffer_u64, _handle);

    var __return_value__ = __admob_rewarded_interstitial_dispose(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return __return_value__;
}

/**
 * @param {Real} _handle
 * @param {Function} _callback
 * @returns {Enum.AdMobError}
 */
function admob_rewarded_interstitial_show(_handle, _callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _handle, type: UInt64
    if (!is_numeric(_handle)) show_error($"{_GMFUNCTION_} :: _handle expected number", true);
    buffer_write(__args_buffer, buffer_u64, _handle);

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_rewarded_interstitial_show(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

// Skipping function admob_app_open_ad_set_ad_unit (no wrapper is required)


/**
 * @param {Function} _callback
 * @returns {Enum.AdMobError}
 */
function admob_app_open_ad_enable(_callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_app_open_ad_enable(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

// Skipping function admob_app_open_ad_disable (no wrapper is required)


// Skipping function admob_app_open_ad_is_enabled (no wrapper is required)


// Skipping function admob_app_open_ad_is_loaded (no wrapper is required)


/**
 * @param {Function} _callback
 * @returns {Enum.AdMobError}
 */
function admob_app_open_ad_load(_callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_app_open_ad_load(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

/**
 * @param {Function} _callback
 * @returns {Enum.AdMobError}
 */
function admob_app_open_ad_show(_callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_app_open_ad_show(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

// Skipping function admob_targeting_coppa (no wrapper is required)


// Skipping function admob_targeting_under_age (no wrapper is required)


/**
 * @param {Enum.AdMobMaxAdContentRating} _content_rating
 */
function admob_targeting_max_ad_content_rating(_content_rating)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _content_rating, type: enum AdMobMaxAdContentRating

    if (!is_numeric(_content_rating)) show_error($"{_GMFUNCTION_} :: _content_rating expected number", true);
    buffer_write(__args_buffer, buffer_s32, _content_rating);

    var __return_value__ = __admob_targeting_max_ad_content_rating(buffer_get_address(__args_buffer), buffer_tell(__args_buffer));

    return __return_value__;
}

/**
 * @param {Enum.AdMobConsentDebugGeography} _debug_geography
 * @param {Function} _callback
 * @returns {Enum.AdMobError}
 */
function admob_consent_request_info_update(_debug_geography, _callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _debug_geography, type: enum AdMobConsentDebugGeography

    if (!is_numeric(_debug_geography)) show_error($"{_GMFUNCTION_} :: _debug_geography expected number", true);
    buffer_write(__args_buffer, buffer_s32, _debug_geography);

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_consent_request_info_update(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

/**
 * @returns {Enum.AdMobConsentStatus}
 */
function admob_consent_get_status()
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_consent_get_status(buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

/**
 * @returns {Enum.AdMobConsentType}
 */
function admob_consent_get_type()
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_consent_get_type(buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

// Skipping function admob_consent_is_form_available (no wrapper is required)


/**
 * @param {Function} _callback
 * @returns {Enum.AdMobError}
 */
function admob_consent_load(_callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_consent_load(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

/**
 * @param {Function} _callback
 * @returns {Enum.AdMobError}
 */
function admob_consent_show(_callback)
{
    var __available__ = __GMAdMob_is_available();
    if (!__available__) return;

    var __dispatcher__ = __GMAdMob_get_dispatcher();

    var __args_buffer = __ext_core_get_args_buffer();

    // param: _callback, type: Function
    if (!is_callable(_callback)) show_error($"{_GMFUNCTION_} :: _callback expected callable type", true);
    var _callback_handle = __ext_core_function_register(_callback, __dispatcher__);
    buffer_write(__args_buffer, buffer_u64, _callback_handle);

    var __ret_buffer = __ext_core_get_ret_buffer();

    var __return_value__ = __admob_consent_show(buffer_get_address(__args_buffer), buffer_tell(__args_buffer), buffer_get_address(__ret_buffer), buffer_get_size(__ret_buffer));

    var __result__ = undefined;
    __result__ = buffer_read(__ret_buffer, buffer_s32);
    return __result__;
}

// Skipping function admob_consent_reset (no wrapper is required)


// Skipping function admob_consent_set_rdp (no wrapper is required)


// Skipping function admob_settings_set_volume (no wrapper is required)


// Skipping function admob_settings_set_muted (no wrapper is required)


/// @ignore
function __GMAdMob_get_decoders()
{
    static __decoders__ = [
        __AdMobResult_decode,
        __AdMobReward_decode,
        __AdMobPaidEvent_decode
    ];
    return __decoders__;
}
/// @ignore
function __GMAdMob_get_dispatcher()
{
    static __dispatcher__ = new __GMNativeFunctionDispatcher(__GMAdMob_invocation_handler, __GMAdMob_get_decoders());
    return __dispatcher__;
}
/// @ignore
function __GMAdMob_is_available()
{
    static __available__ = extension_exists("GMAdMob");
    return __available__;
}
