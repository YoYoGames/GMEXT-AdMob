// ##### extgen :: Auto-generated file do not edit!! #####

#include "GMAdMobInternal_native.h"
#include "GMAdMobInternal_exports.h"

using namespace gm_structs;
using namespace gm::wire::codec;

static gm::runtime::DispatchQueue __dispatch_queue;

// Internal function used for fetching dispatched function calls to GML
GMEXPORT double __EXT_NATIVE__GMAdMob_invocation_handler(char* __ret_buffer, double __ret_buffer_length)
{
    gm::byteio::BufferWriter __bw{ __ret_buffer, static_cast<size_t>(__ret_buffer_length) };
    return __dispatch_queue.fetch(__bw);
}

GMEXPORT double __EXT_NATIVE__admob_initialize(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_initialize(callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_set_test_device_id()
{
    auto&& __result = admob_set_test_device_id();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_events_on_paid_event(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: enabled, type: Bool
    bool enabled = gm::wire::codec::readValue<bool>(__br);

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    admob_events_on_paid_event(enabled, callback);
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_banner_set_ad_unit(char* ad_unit_id)
{
    admob_banner_set_ad_unit(ad_unit_id);
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_banner_create(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: size, type: enum AdMobBannerSize
    gm_enums::AdMobBannerSize size = gm::wire::codec::readValue<gm_enums::AdMobBannerSize>(__br);

    // field: bottom, type: Bool
    bool bottom = gm::wire::codec::readValue<bool>(__br);

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_banner_create(size, bottom, callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_banner_create_ext(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: size, type: enum AdMobBannerSize
    gm_enums::AdMobBannerSize size = gm::wire::codec::readValue<gm_enums::AdMobBannerSize>(__br);

    // field: bottom, type: Bool
    bool bottom = gm::wire::codec::readValue<bool>(__br);

    // field: alignment, type: enum AdMobBannerAlignment
    gm_enums::AdMobBannerAlignment alignment = gm::wire::codec::readValue<gm_enums::AdMobBannerAlignment>(__br);

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_banner_create_ext(size, bottom, alignment, callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_banner_get_width()
{
    auto&& __result = admob_banner_get_width();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_banner_get_height()
{
    auto&& __result = admob_banner_get_height();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_banner_move(double bottom)
{
    auto&& __result = admob_banner_move(static_cast<bool>(bottom));
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_banner_show()
{
    auto&& __result = admob_banner_show();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_banner_hide()
{
    auto&& __result = admob_banner_hide();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_banner_remove()
{
    auto&& __result = admob_banner_remove();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_interstitial_set_ad_unit(char* ad_unit_id)
{
    admob_interstitial_set_ad_unit(ad_unit_id);
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_interstitial_free_loaded_instances(double count)
{
    admob_interstitial_free_loaded_instances(static_cast<double>(count));
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_interstitial_max_instances(double value)
{
    admob_interstitial_max_instances(static_cast<double>(value));
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_interstitial_load(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_interstitial_load(callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_interstitial_show(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_interstitial_show(callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_interstitial_is_loaded()
{
    auto&& __result = admob_interstitial_is_loaded();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_interstitial_instances_count()
{
    auto&& __result = admob_interstitial_instances_count();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_server_side_verification_set(char* user_id, char* custom_data)
{
    admob_server_side_verification_set(user_id, custom_data);
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_server_side_verification_clear()
{
    admob_server_side_verification_clear();
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_video_set_ad_unit(char* ad_unit_id)
{
    admob_rewarded_video_set_ad_unit(ad_unit_id);
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_video_free_loaded_instances(double count)
{
    admob_rewarded_video_free_loaded_instances(static_cast<double>(count));
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_video_max_instances(double value)
{
    admob_rewarded_video_max_instances(static_cast<double>(value));
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_video_load(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_rewarded_video_load(callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_video_show(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_rewarded_video_show(callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_video_is_loaded()
{
    auto&& __result = admob_rewarded_video_is_loaded();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_video_instances_count()
{
    auto&& __result = admob_rewarded_video_instances_count();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_interstitial_set_ad_unit(char* ad_unit_id)
{
    admob_rewarded_interstitial_set_ad_unit(ad_unit_id);
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_interstitial_free_loaded_instances(double count)
{
    admob_rewarded_interstitial_free_loaded_instances(static_cast<double>(count));
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_interstitial_max_instances(double value)
{
    admob_rewarded_interstitial_max_instances(static_cast<double>(value));
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_interstitial_load(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_rewarded_interstitial_load(callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_interstitial_show(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_rewarded_interstitial_show(callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_interstitial_is_loaded()
{
    auto&& __result = admob_rewarded_interstitial_is_loaded();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_rewarded_interstitial_instances_count()
{
    auto&& __result = admob_rewarded_interstitial_instances_count();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_app_open_ad_set_ad_unit(char* ad_unit_id)
{
    admob_app_open_ad_set_ad_unit(ad_unit_id);
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_app_open_ad_enable(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: orientation, type: Float64
    double orientation = gm::wire::codec::readValue<double>(__br);

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_app_open_ad_enable(orientation, callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_app_open_ad_disable()
{
    admob_app_open_ad_disable();
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_app_open_ad_is_enabled()
{
    auto&& __result = admob_app_open_ad_is_enabled();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_app_open_ad_is_loaded()
{
    auto&& __result = admob_app_open_ad_is_loaded();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_app_open_ad_load(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_app_open_ad_load(callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_app_open_ad_show(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_app_open_ad_show(callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_targeting_coppa(double coppa)
{
    auto&& __result = admob_targeting_coppa(static_cast<bool>(coppa));
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_targeting_under_age(double under_age)
{
    auto&& __result = admob_targeting_under_age(static_cast<bool>(under_age));
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_targeting_max_ad_content_rating(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: content_rating, type: enum AdMobMaxAdContentRating
    gm_enums::AdMobMaxAdContentRating content_rating = gm::wire::codec::readValue<gm_enums::AdMobMaxAdContentRating>(__br);

    auto&& __result = admob_targeting_max_ad_content_rating(content_rating);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_consent_request_info_update(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: debug_geography, type: enum AdMobConsentDebugGeography
    gm_enums::AdMobConsentDebugGeography debug_geography = gm::wire::codec::readValue<gm_enums::AdMobConsentDebugGeography>(__br);

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_consent_request_info_update(debug_geography, callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_consent_get_status()
{
    auto&& __result = admob_consent_get_status();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_consent_get_type()
{
    auto&& __result = admob_consent_get_type();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_consent_is_form_available()
{
    auto&& __result = admob_consent_is_form_available();
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_consent_load(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_consent_load(callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_consent_show(char* __arg_buffer, double __arg_buffer_length)
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    auto&& __result = admob_consent_show(callback);
    return static_cast<double>(__result);
}

GMEXPORT double __EXT_NATIVE__admob_consent_reset()
{
    admob_consent_reset();
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_consent_set_rdp(double enabled)
{
    admob_consent_set_rdp(static_cast<bool>(enabled));
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_settings_set_volume(double value)
{
    admob_settings_set_volume(static_cast<double>(value));
    return 0;
}

GMEXPORT double __EXT_NATIVE__admob_settings_set_muted(double muted)
{
    admob_settings_set_muted(static_cast<bool>(muted));
    return 0;
}

