// ##### extgen :: Auto-generated file do not edit!! #####

#pragma once
#import <Foundation/Foundation.h>

#include <cstdint>
#include <string_view>
#include <vector>
#include <array>
#include <optional>
#include "core/GMExtWire.h"

namespace gm_consts
{
}


namespace gm_enums
{
    enum class AdMobError : std::int32_t
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
    };

    enum class AdMobBannerAlignment : std::int32_t
    {
        Left = 0,
        Center = 1,
        Right = 2
    };

    enum class AdMobBannerSize : std::int32_t
    {
        Banner = 0,
        LargeBanner = 1,
        MediumRectangle = 2,
        FullBanner = 3,
        Leaderboard = 4,
        SmartBanner = 5,
        AnchoredAdaptive = 6
    };

    enum class AdMobMaxAdContentRating : std::int32_t
    {
        General = 0,
        ParentalGuidance = 1,
        Teen = 2,
        MatureAudience = 3
    };

    enum class AdMobConsentDebugGeography : std::int32_t
    {
        Disabled = 0,
        EEA = 1,
        NotEEA = 2,
        RegulatedUSState = 3,
        Other = 4
    };

    enum class AdMobConsentStatus : std::int32_t
    {
        Unknown = 0,
        NotRequired = 1,
        Required = 2,
        Obtained = 3
    };

    enum class AdMobConsentType : std::int32_t
    {
        Unknown = 0,
        NonPersonalized = 1,
        Personalized = 2,
        Declined = 3
    };

    enum class AdMobAdType : std::int32_t
    {
        Banner = 0,
        Interstitial = 1,
        RewardedVideo = 2,
        RewardedInterstitial = 3,
        AppOpen = 4
    };

    enum class AdMobPrecisionType : std::int32_t
    {
        Unknown = 0,
        Estimated = 1,
        PublisherProvided = 2,
        Precise = 3
    };

    enum class AdMobBannerCallbackEvent : std::int32_t
    {
        Loaded = 0,
        LoadFailed = 1,
        Opened = 2,
        Clicked = 3,
        Closed = 4,
        Impression = 5
    };

    enum class AdMobInterstitialShowEvent : std::int32_t
    {
        Shown = 0,
        Dismissed = 1,
        Clicked = 2,
        Impression = 3
    };

    enum class AdMobRewardedVideoShowEvent : std::int32_t
    {
        Shown = 0,
        Dismissed = 1,
        Clicked = 2,
        Impression = 3,
        Reward = 4
    };

    enum class AdMobRewardedInterstitialShowEvent : std::int32_t
    {
        Shown = 0,
        Dismissed = 1,
        Clicked = 2,
        Impression = 3,
        Reward = 4
    };

    enum class AdMobAppOpenAdShowEvent : std::int32_t
    {
        Shown = 0,
        Dismissed = 1,
        Clicked = 2,
        Impression = 3
    };

}


namespace gm_structs
{
    struct AdMobResult;
    struct AdMobReward;
    struct AdMobPaidEvent;

    struct AdMobResult
    {
        bool success;
        std::optional<std::string> error_message;
        std::optional<std::int32_t> sdk_error_code;
    };

    struct AdMobReward
    {
        double amount;
        std::string type;
    };

    struct AdMobPaidEvent
    {
        gm_enums::AdMobAdType ad_type;
        std::string ad_unit_id;
        double value_micros;
        std::string currency_code;
        gm_enums::AdMobPrecisionType precision;
        std::string mediation_adapter_class_name;
        std::optional<std::string> ad_source_name;
        std::optional<std::string> ad_source_id;
        std::optional<std::string> ad_source_instance_name;
        std::optional<std::string> ad_source_instance_id;
    };

}

namespace gm::wire::codec
{
    template<>
    inline void writeValue<gm_structs::AdMobResult>(gm::byteio::IByteWriter& _buf, const gm_structs::AdMobResult& obj)
    {
        gm::wire::codec::writeValue(_buf, obj.success);
        gm::wire::codec::writeValue(_buf, obj.error_message);
        gm::wire::codec::writeValue(_buf, obj.sdk_error_code);
    }

    template<>
    inline gm_structs::AdMobResult readValue<gm_structs::AdMobResult>(gm::byteio::BufferReader& _buf)
    {
        gm_structs::AdMobResult obj;
        obj.success = gm::wire::codec::readValue<bool>(_buf);
        obj.error_message = gm::wire::codec::readOptional<std::string>(_buf);
        obj.sdk_error_code = gm::wire::codec::readOptional<std::int32_t>(_buf);
        return obj;
    }

    template<>
    inline void writeValue<gm_structs::AdMobReward>(gm::byteio::IByteWriter& _buf, const gm_structs::AdMobReward& obj)
    {
        gm::wire::codec::writeValue(_buf, obj.amount);
        gm::wire::codec::writeValue(_buf, obj.type);
    }

    template<>
    inline gm_structs::AdMobReward readValue<gm_structs::AdMobReward>(gm::byteio::BufferReader& _buf)
    {
        gm_structs::AdMobReward obj;
        obj.amount = gm::wire::codec::readValue<double>(_buf);
        obj.type = gm::wire::codec::readValue<std::string>(_buf);
        return obj;
    }

    template<>
    inline void writeValue<gm_structs::AdMobPaidEvent>(gm::byteio::IByteWriter& _buf, const gm_structs::AdMobPaidEvent& obj)
    {
        gm::wire::codec::writeValue(_buf, obj.ad_type);
        gm::wire::codec::writeValue(_buf, obj.ad_unit_id);
        gm::wire::codec::writeValue(_buf, obj.value_micros);
        gm::wire::codec::writeValue(_buf, obj.currency_code);
        gm::wire::codec::writeValue(_buf, obj.precision);
        gm::wire::codec::writeValue(_buf, obj.mediation_adapter_class_name);
        gm::wire::codec::writeValue(_buf, obj.ad_source_name);
        gm::wire::codec::writeValue(_buf, obj.ad_source_id);
        gm::wire::codec::writeValue(_buf, obj.ad_source_instance_name);
        gm::wire::codec::writeValue(_buf, obj.ad_source_instance_id);
    }

    template<>
    inline gm_structs::AdMobPaidEvent readValue<gm_structs::AdMobPaidEvent>(gm::byteio::BufferReader& _buf)
    {
        gm_structs::AdMobPaidEvent obj;
        obj.ad_type = gm::wire::codec::readValue<gm_enums::AdMobAdType>(_buf);
        obj.ad_unit_id = gm::wire::codec::readValue<std::string>(_buf);
        obj.value_micros = gm::wire::codec::readValue<double>(_buf);
        obj.currency_code = gm::wire::codec::readValue<std::string>(_buf);
        obj.precision = gm::wire::codec::readValue<gm_enums::AdMobPrecisionType>(_buf);
        obj.mediation_adapter_class_name = gm::wire::codec::readValue<std::string>(_buf);
        obj.ad_source_name = gm::wire::codec::readOptional<std::string>(_buf);
        obj.ad_source_id = gm::wire::codec::readOptional<std::string>(_buf);
        obj.ad_source_instance_name = gm::wire::codec::readOptional<std::string>(_buf);
        obj.ad_source_instance_id = gm::wire::codec::readOptional<std::string>(_buf);
        return obj;
    }

}

namespace gm::wire::details
{
    template<>
    struct gm_struct_traits<gm_structs::AdMobResult>
    {
        static constexpr bool is_gm_struct = true;
        static constexpr std::uint32_t codec_id = 0;
    };

    template<>
    struct gm_struct_traits<gm_structs::AdMobReward>
    {
        static constexpr bool is_gm_struct = true;
        static constexpr std::uint32_t codec_id = 1;
    };

    template<>
    struct gm_struct_traits<gm_structs::AdMobPaidEvent>
    {
        static constexpr bool is_gm_struct = true;
        static constexpr std::uint32_t codec_id = 2;
    };

}

@protocol GMAdMobInterface <NSObject>
- (gm_enums::AdMobError)admob_initialize:(gm::wire::GMFunction)callback;
- (gm_enums::AdMobError)admob_set_test_device_id;
- (void)admob_events_on_paid_event:(bool)enabled callback:(gm::wire::GMFunction)callback;
- (void)admob_banner_set_ad_unit:(std::string_view)ad_unit_id;
- (gm_enums::AdMobError)admob_banner_create:(gm_enums::AdMobBannerSize)size bottom:(bool)bottom callback:(gm::wire::GMFunction)callback;
- (gm_enums::AdMobError)admob_banner_create_ext:(gm_enums::AdMobBannerSize)size bottom:(bool)bottom alignment:(gm_enums::AdMobBannerAlignment)alignment callback:(gm::wire::GMFunction)callback;
- (double)admob_banner_get_width;
- (double)admob_banner_get_height;
- (void)admob_banner_move:(bool)bottom;
- (void)admob_banner_show;
- (void)admob_banner_hide;
- (void)admob_banner_remove;
- (void)admob_interstitial_set_ad_unit:(std::string_view)ad_unit_id;
- (gm_enums::AdMobError)admob_interstitial_load:(gm::wire::GMFunction)callback ad_unit_id:(std::optional<std::string_view>)ad_unit_id;
- (bool)admob_interstitial_is_valid:(std::uint64_t)handle;
- (void)admob_interstitial_dispose:(std::uint64_t)handle;
- (gm_enums::AdMobError)admob_interstitial_show:(std::uint64_t)handle callback:(gm::wire::GMFunction)callback;
- (void)admob_server_side_verification_set:(std::string_view)user_id custom_data:(std::string_view)custom_data;
- (void)admob_server_side_verification_clear;
- (void)admob_rewarded_video_set_ad_unit:(std::string_view)ad_unit_id;
- (gm_enums::AdMobError)admob_rewarded_video_load:(gm::wire::GMFunction)callback ad_unit_id:(std::optional<std::string_view>)ad_unit_id;
- (bool)admob_rewarded_video_is_valid:(std::uint64_t)handle;
- (void)admob_rewarded_video_dispose:(std::uint64_t)handle;
- (gm_enums::AdMobError)admob_rewarded_video_show:(std::uint64_t)handle callback:(gm::wire::GMFunction)callback;
- (void)admob_rewarded_interstitial_set_ad_unit:(std::string_view)ad_unit_id;
- (gm_enums::AdMobError)admob_rewarded_interstitial_load:(gm::wire::GMFunction)callback ad_unit_id:(std::optional<std::string_view>)ad_unit_id;
- (bool)admob_rewarded_interstitial_is_valid:(std::uint64_t)handle;
- (void)admob_rewarded_interstitial_dispose:(std::uint64_t)handle;
- (gm_enums::AdMobError)admob_rewarded_interstitial_show:(std::uint64_t)handle callback:(gm::wire::GMFunction)callback;
- (void)admob_app_open_ad_set_ad_unit:(std::string_view)ad_unit_id;
- (gm_enums::AdMobError)admob_app_open_ad_enable:(double)orientation callback:(gm::wire::GMFunction)callback;
- (void)admob_app_open_ad_disable;
- (bool)admob_app_open_ad_is_enabled;
- (bool)admob_app_open_ad_is_loaded;
- (gm_enums::AdMobError)admob_app_open_ad_load:(gm::wire::GMFunction)callback;
- (gm_enums::AdMobError)admob_app_open_ad_show:(gm::wire::GMFunction)callback;
- (void)admob_targeting_coppa:(bool)coppa;
- (void)admob_targeting_under_age:(bool)under_age;
- (void)admob_targeting_max_ad_content_rating:(gm_enums::AdMobMaxAdContentRating)content_rating;
- (gm_enums::AdMobError)admob_consent_request_info_update:(gm_enums::AdMobConsentDebugGeography)debug_geography callback:(gm::wire::GMFunction)callback;
- (gm_enums::AdMobConsentStatus)admob_consent_get_status;
- (gm_enums::AdMobConsentType)admob_consent_get_type;
- (bool)admob_consent_is_form_available;
- (gm_enums::AdMobError)admob_consent_load:(gm::wire::GMFunction)callback;
- (gm_enums::AdMobError)admob_consent_show:(gm::wire::GMFunction)callback;
- (void)admob_consent_reset;
- (void)admob_consent_set_rdp:(bool)enabled;
- (void)admob_settings_set_volume:(double)value;
- (void)admob_settings_set_muted:(bool)muted;
@end


@interface GMAdMobInternal : NSObject
- (double)__EXT_NATIVE__admob_initialize:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_set_test_device_id:(char*)__ret_buffer arg1:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_events_on_paid_event:(char*)__arg_buffer arg1:(double)__arg_buffer_length;
- (double)__EXT_NATIVE__admob_banner_set_ad_unit:(char*)ad_unit_id;
- (double)__EXT_NATIVE__admob_banner_create:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_banner_create_ext:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_banner_get_width;
- (double)__EXT_NATIVE__admob_banner_get_height;
- (double)__EXT_NATIVE__admob_banner_move:(double)bottom;
- (double)__EXT_NATIVE__admob_banner_show;
- (double)__EXT_NATIVE__admob_banner_hide;
- (double)__EXT_NATIVE__admob_banner_remove;
- (double)__EXT_NATIVE__admob_interstitial_set_ad_unit:(char*)ad_unit_id;
- (double)__EXT_NATIVE__admob_interstitial_load:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_interstitial_is_valid:(char*)__arg_buffer arg1:(double)__arg_buffer_length;
- (double)__EXT_NATIVE__admob_interstitial_dispose:(char*)__arg_buffer arg1:(double)__arg_buffer_length;
- (double)__EXT_NATIVE__admob_interstitial_show:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_server_side_verification_set:(char*)user_id arg1:(char*)custom_data;
- (double)__EXT_NATIVE__admob_server_side_verification_clear;
- (double)__EXT_NATIVE__admob_rewarded_video_set_ad_unit:(char*)ad_unit_id;
- (double)__EXT_NATIVE__admob_rewarded_video_load:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_rewarded_video_is_valid:(char*)__arg_buffer arg1:(double)__arg_buffer_length;
- (double)__EXT_NATIVE__admob_rewarded_video_dispose:(char*)__arg_buffer arg1:(double)__arg_buffer_length;
- (double)__EXT_NATIVE__admob_rewarded_video_show:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_rewarded_interstitial_set_ad_unit:(char*)ad_unit_id;
- (double)__EXT_NATIVE__admob_rewarded_interstitial_load:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_rewarded_interstitial_is_valid:(char*)__arg_buffer arg1:(double)__arg_buffer_length;
- (double)__EXT_NATIVE__admob_rewarded_interstitial_dispose:(char*)__arg_buffer arg1:(double)__arg_buffer_length;
- (double)__EXT_NATIVE__admob_rewarded_interstitial_show:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_app_open_ad_set_ad_unit:(char*)ad_unit_id;
- (double)__EXT_NATIVE__admob_app_open_ad_enable:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_app_open_ad_disable;
- (double)__EXT_NATIVE__admob_app_open_ad_is_enabled;
- (double)__EXT_NATIVE__admob_app_open_ad_is_loaded;
- (double)__EXT_NATIVE__admob_app_open_ad_load:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_app_open_ad_show:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_targeting_coppa:(double)coppa;
- (double)__EXT_NATIVE__admob_targeting_under_age:(double)under_age;
- (double)__EXT_NATIVE__admob_targeting_max_ad_content_rating:(char*)__arg_buffer arg1:(double)__arg_buffer_length;
- (double)__EXT_NATIVE__admob_consent_request_info_update:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_consent_get_status:(char*)__ret_buffer arg1:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_consent_get_type:(char*)__ret_buffer arg1:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_consent_is_form_available;
- (double)__EXT_NATIVE__admob_consent_load:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_consent_show:(char*)__arg_buffer arg1:(double)__arg_buffer_length arg2:(char*)__ret_buffer arg3:(double)__ret_buffer_length;
- (double)__EXT_NATIVE__admob_consent_reset;
- (double)__EXT_NATIVE__admob_consent_set_rdp:(double)enabled;
- (double)__EXT_NATIVE__admob_settings_set_volume:(double)value;
- (double)__EXT_NATIVE__admob_settings_set_muted:(double)muted;
- (double)__EXT_NATIVE__GMAdMob_invocation_handler:(char*)__ret_buffer arg1:(double)__ret_buffer_length;
@end


