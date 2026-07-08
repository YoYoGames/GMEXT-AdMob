// ##### extgen :: Auto-generated file do not edit!! #####

#pragma once
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
        NullViewHandler = -7
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
        Disabled = -1,
        EEA = 1,
        NotEEA = 2
    };

    enum class AdMobConsentStatus : std::int32_t
    {
        Unknown = 0,
        Required = 1,
        NotRequired = 2,
        Obtained = 3
    };

    enum class AdMobConsentType : std::int32_t
    {
        Unknown = 0,
        NonPersonalized = 1,
        Personalized = 2,
        Declined = 3
    };

    enum class AdMobInitializeCallbackEvent : std::int32_t
    {
        Initialized = 0,
        Failed = 1
    };

    enum class AdMobBannerCallbackEvent : std::int32_t
    {
        Loaded = 0,
        LoadFailed = 1,
        Opened = 2,
        Clicked = 3,
        Closed = 4
    };

    enum class AdMobInterstitialCallbackEvent : std::int32_t
    {
        Loaded = 0,
        LoadFailed = 1,
        FullyShown = 2,
        ShowFailed = 3,
        Dismissed = 4
    };

    enum class AdMobRewardedVideoCallbackEvent : std::int32_t
    {
        Loaded = 0,
        LoadFailed = 1,
        FullyShown = 2,
        ShowFailed = 3,
        Dismissed = 4,
        Reward = 5
    };

    enum class AdMobRewardedInterstitialCallbackEvent : std::int32_t
    {
        Loaded = 0,
        LoadFailed = 1,
        FullyShown = 2,
        ShowFailed = 3,
        Dismissed = 4,
        Reward = 5
    };

    enum class AdMobAppOpenAdCallbackEvent : std::int32_t
    {
        Loaded = 0,
        LoadFailed = 1,
        FullyShown = 2,
        ShowFailed = 3,
        Dismissed = 4
    };

    enum class AdMobConsentCallbackEvent : std::int32_t
    {
        RequestInfoUpdated = 0,
        RequestInfoUpdateFailed = 1,
        Loaded = 2,
        LoadFailed = 3,
        Shown = 4,
        ShowFailed = 5
    };

    enum class AdMobPaidEventCallbackEvent : std::int32_t
    {
        Paid = 0
    };

}


namespace gm_structs
{

}

namespace gm::wire::codec
{
}

namespace gm::wire::details
{
}

double admob_initialize(const gm::wire::GMFunction& callback);
double admob_set_test_device_id();
double admob_events_on_paid_event(bool enabled, const gm::wire::GMFunction& callback);
void admob_banner_set_ad_unit(std::string_view ad_unit_id);
double admob_banner_create(gm_enums::AdMobBannerSize size, bool bottom, const gm::wire::GMFunction& callback);
double admob_banner_create_ext(gm_enums::AdMobBannerSize size, bool bottom, gm_enums::AdMobBannerAlignment alignment, const gm::wire::GMFunction& callback);
double admob_banner_get_width();
double admob_banner_get_height();
double admob_banner_move(bool bottom);
double admob_banner_show();
double admob_banner_hide();
double admob_banner_remove();
void admob_interstitial_set_ad_unit(std::string_view ad_unit_id);
void admob_interstitial_free_loaded_instances(double count);
void admob_interstitial_max_instances(double value);
double admob_interstitial_load(const gm::wire::GMFunction& callback);
double admob_interstitial_show(const gm::wire::GMFunction& callback);
double admob_interstitial_is_loaded();
double admob_interstitial_instances_count();
double admob_server_side_verification_set(std::string_view user_id, std::string_view custom_data);
double admob_server_side_verification_clear();
void admob_rewarded_video_set_ad_unit(std::string_view ad_unit_id);
void admob_rewarded_video_free_loaded_instances(double count);
void admob_rewarded_video_max_instances(double value);
double admob_rewarded_video_load(const gm::wire::GMFunction& callback);
double admob_rewarded_video_show(const gm::wire::GMFunction& callback);
double admob_rewarded_video_is_loaded();
double admob_rewarded_video_instances_count();
void admob_rewarded_interstitial_set_ad_unit(std::string_view ad_unit_id);
void admob_rewarded_interstitial_free_loaded_instances(double count);
void admob_rewarded_interstitial_max_instances(double value);
double admob_rewarded_interstitial_load(const gm::wire::GMFunction& callback);
double admob_rewarded_interstitial_show(const gm::wire::GMFunction& callback);
double admob_rewarded_interstitial_is_loaded();
double admob_rewarded_interstitial_instances_count();
void admob_app_open_ad_set_ad_unit(std::string_view ad_unit_id);
double admob_app_open_ad_enable(double orientation, const gm::wire::GMFunction& callback);
void admob_app_open_ad_disable();
double admob_app_open_ad_is_enabled();
double admob_app_open_ad_is_loaded();
double admob_app_open_ad_load(const gm::wire::GMFunction& callback);
double admob_app_open_ad_show(const gm::wire::GMFunction& callback);
double admob_targeting_coppa(bool coppa);
double admob_targeting_under_age(bool under_age);
double admob_targeting_max_ad_content_rating(gm_enums::AdMobMaxAdContentRating content_rating);
double admob_consent_request_info_update(gm_enums::AdMobConsentDebugGeography debug_geography, const gm::wire::GMFunction& callback);
double admob_consent_get_status();
double admob_consent_get_type();
double admob_consent_is_form_available();
double admob_consent_load(const gm::wire::GMFunction& callback);
double admob_consent_show(const gm::wire::GMFunction& callback);
void admob_consent_reset();
void admob_consent_set_rdp(bool enabled);
void admob_settings_set_volume(double value);
void admob_settings_set_muted(bool muted);
