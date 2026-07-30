// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName};

import java.nio.ByteBuffer;
import java.util.*;
import ${YYAndroidPackageName}.GMExtWire;
import ${YYAndroidPackageName}.GMExtWire.GMFunction;
import ${YYAndroidPackageName}.GMExtWire.GMValue;
import ${YYAndroidPackageName}.records.*;
import ${YYAndroidPackageName}.codecs.*;
import ${YYAndroidPackageName}.enums.*;

public abstract class GMAdMobInternal extends RunnerSocial implements GMAdMobInterface {

    private final GMExtWire.DispatchQueue __dispatch_queue = new GMExtWire.DispatchQueue();
    public double __EXT_NATIVE__GMAdMob_invocation_handler(ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        return __dispatch_queue.fetch(__ret_buffer);
    }

    public double __EXT_NATIVE__admob_initialize(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        AdMobError __result = admob_initialize(callback);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_set_test_device_id(ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        AdMobError __result = admob_set_test_device_id();

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_events_on_paid_event(ByteBuffer __arg_buffer, double __arg_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: enabled, type: Bool
        boolean enabled = GMExtWire.readBool(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        admob_events_on_paid_event(enabled, callback);
        return 0;
    }

    public double __EXT_NATIVE__admob_banner_set_ad_unit(String ad_unit_id)
    {
        admob_banner_set_ad_unit(ad_unit_id);
        return 0;
    }

    public double __EXT_NATIVE__admob_banner_create(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: size, type: enum AdMobBannerSize
        AdMobBannerSize size = AdMobBannerSize.from(GMExtWire.readI32(__arg_buffer));

        // field: bottom, type: Bool
        boolean bottom = GMExtWire.readBool(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        AdMobError __result = admob_banner_create(size, bottom, callback);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_banner_create_ext(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: size, type: enum AdMobBannerSize
        AdMobBannerSize size = AdMobBannerSize.from(GMExtWire.readI32(__arg_buffer));

        // field: bottom, type: Bool
        boolean bottom = GMExtWire.readBool(__arg_buffer);

        // field: alignment, type: enum AdMobBannerAlignment
        AdMobBannerAlignment alignment = AdMobBannerAlignment.from(GMExtWire.readI32(__arg_buffer));

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        AdMobError __result = admob_banner_create_ext(size, bottom, alignment, callback);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_banner_get_width()
    {
        double __result = admob_banner_get_width();
        return (double)__result;
    }

    public double __EXT_NATIVE__admob_banner_get_height()
    {
        double __result = admob_banner_get_height();
        return (double)__result;
    }

    public double __EXT_NATIVE__admob_banner_move(double bottom)
    {
        admob_banner_move(bottom != 0);
        return 0;
    }

    public double __EXT_NATIVE__admob_banner_show()
    {
        admob_banner_show();
        return 0;
    }

    public double __EXT_NATIVE__admob_banner_hide()
    {
        admob_banner_hide();
        return 0;
    }

    public double __EXT_NATIVE__admob_banner_remove()
    {
        admob_banner_remove();
        return 0;
    }

    public double __EXT_NATIVE__admob_interstitial_set_ad_unit(String ad_unit_id)
    {
        admob_interstitial_set_ad_unit(ad_unit_id);
        return 0;
    }

    public double __EXT_NATIVE__admob_interstitial_load(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        // field: ad_unit_id, type: optional<String>
        java.util.Optional<String> ad_unit_id = java.util.Optional.empty();
        if (GMExtWire.readBool(__arg_buffer))
        {
            String __opt_ad_unit_id = GMExtWire.readString(__arg_buffer);
            ad_unit_id = java.util.Optional.of(__opt_ad_unit_id);
        }

        AdMobError __result = admob_interstitial_load(callback, ad_unit_id);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_interstitial_is_valid(ByteBuffer __arg_buffer, double __arg_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: handle, type: UInt64
        long handle = GMExtWire.readI64(__arg_buffer);

        boolean __result = admob_interstitial_is_valid(handle);
        return __result ? 1.0 : 0.0;
    }

    public double __EXT_NATIVE__admob_interstitial_dispose(ByteBuffer __arg_buffer, double __arg_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: handle, type: UInt64
        long handle = GMExtWire.readI64(__arg_buffer);

        admob_interstitial_dispose(handle);
        return 0;
    }

    public double __EXT_NATIVE__admob_interstitial_show(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: handle, type: UInt64
        long handle = GMExtWire.readI64(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        AdMobError __result = admob_interstitial_show(handle, callback);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_server_side_verification_set(String user_id, String custom_data)
    {
        admob_server_side_verification_set(user_id, custom_data);
        return 0;
    }

    public double __EXT_NATIVE__admob_server_side_verification_clear()
    {
        admob_server_side_verification_clear();
        return 0;
    }

    public double __EXT_NATIVE__admob_rewarded_video_set_ad_unit(String ad_unit_id)
    {
        admob_rewarded_video_set_ad_unit(ad_unit_id);
        return 0;
    }

    public double __EXT_NATIVE__admob_rewarded_video_load(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        // field: ad_unit_id, type: optional<String>
        java.util.Optional<String> ad_unit_id = java.util.Optional.empty();
        if (GMExtWire.readBool(__arg_buffer))
        {
            String __opt_ad_unit_id = GMExtWire.readString(__arg_buffer);
            ad_unit_id = java.util.Optional.of(__opt_ad_unit_id);
        }

        AdMobError __result = admob_rewarded_video_load(callback, ad_unit_id);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_rewarded_video_is_valid(ByteBuffer __arg_buffer, double __arg_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: handle, type: UInt64
        long handle = GMExtWire.readI64(__arg_buffer);

        boolean __result = admob_rewarded_video_is_valid(handle);
        return __result ? 1.0 : 0.0;
    }

    public double __EXT_NATIVE__admob_rewarded_video_dispose(ByteBuffer __arg_buffer, double __arg_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: handle, type: UInt64
        long handle = GMExtWire.readI64(__arg_buffer);

        admob_rewarded_video_dispose(handle);
        return 0;
    }

    public double __EXT_NATIVE__admob_rewarded_video_show(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: handle, type: UInt64
        long handle = GMExtWire.readI64(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        AdMobError __result = admob_rewarded_video_show(handle, callback);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_rewarded_interstitial_set_ad_unit(String ad_unit_id)
    {
        admob_rewarded_interstitial_set_ad_unit(ad_unit_id);
        return 0;
    }

    public double __EXT_NATIVE__admob_rewarded_interstitial_load(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        // field: ad_unit_id, type: optional<String>
        java.util.Optional<String> ad_unit_id = java.util.Optional.empty();
        if (GMExtWire.readBool(__arg_buffer))
        {
            String __opt_ad_unit_id = GMExtWire.readString(__arg_buffer);
            ad_unit_id = java.util.Optional.of(__opt_ad_unit_id);
        }

        AdMobError __result = admob_rewarded_interstitial_load(callback, ad_unit_id);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_rewarded_interstitial_is_valid(ByteBuffer __arg_buffer, double __arg_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: handle, type: UInt64
        long handle = GMExtWire.readI64(__arg_buffer);

        boolean __result = admob_rewarded_interstitial_is_valid(handle);
        return __result ? 1.0 : 0.0;
    }

    public double __EXT_NATIVE__admob_rewarded_interstitial_dispose(ByteBuffer __arg_buffer, double __arg_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: handle, type: UInt64
        long handle = GMExtWire.readI64(__arg_buffer);

        admob_rewarded_interstitial_dispose(handle);
        return 0;
    }

    public double __EXT_NATIVE__admob_rewarded_interstitial_show(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: handle, type: UInt64
        long handle = GMExtWire.readI64(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        AdMobError __result = admob_rewarded_interstitial_show(handle, callback);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_app_open_ad_set_ad_unit(String ad_unit_id)
    {
        admob_app_open_ad_set_ad_unit(ad_unit_id);
        return 0;
    }

    public double __EXT_NATIVE__admob_app_open_ad_enable(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        AdMobError __result = admob_app_open_ad_enable(callback);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_app_open_ad_disable()
    {
        admob_app_open_ad_disable();
        return 0;
    }

    public double __EXT_NATIVE__admob_app_open_ad_is_enabled()
    {
        boolean __result = admob_app_open_ad_is_enabled();
        return __result ? 1.0 : 0.0;
    }

    public double __EXT_NATIVE__admob_app_open_ad_is_loaded()
    {
        boolean __result = admob_app_open_ad_is_loaded();
        return __result ? 1.0 : 0.0;
    }

    public double __EXT_NATIVE__admob_app_open_ad_load(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        AdMobError __result = admob_app_open_ad_load(callback);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_app_open_ad_show(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        AdMobError __result = admob_app_open_ad_show(callback);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_targeting_coppa(double coppa)
    {
        admob_targeting_coppa(coppa != 0);
        return 0;
    }

    public double __EXT_NATIVE__admob_targeting_under_age(double under_age)
    {
        admob_targeting_under_age(under_age != 0);
        return 0;
    }

    public double __EXT_NATIVE__admob_targeting_max_ad_content_rating(ByteBuffer __arg_buffer, double __arg_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: content_rating, type: enum AdMobMaxAdContentRating
        AdMobMaxAdContentRating content_rating = AdMobMaxAdContentRating.from(GMExtWire.readI32(__arg_buffer));

        admob_targeting_max_ad_content_rating(content_rating);
        return 0;
    }

    public double __EXT_NATIVE__admob_consent_request_info_update(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: debug_geography, type: enum AdMobConsentDebugGeography
        AdMobConsentDebugGeography debug_geography = AdMobConsentDebugGeography.from(GMExtWire.readI32(__arg_buffer));

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        AdMobError __result = admob_consent_request_info_update(debug_geography, callback);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_consent_get_status(ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        AdMobConsentStatus __result = admob_consent_get_status();

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobConsentStatus
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_consent_get_type(ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        AdMobConsentType __result = admob_consent_get_type();

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobConsentType
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_consent_is_form_available()
    {
        boolean __result = admob_consent_is_form_available();
        return __result ? 1.0 : 0.0;
    }

    public double __EXT_NATIVE__admob_consent_load(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        AdMobError __result = admob_consent_load(callback);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_consent_show(ByteBuffer __arg_buffer, double __arg_buffer_length, ByteBuffer __ret_buffer, double __ret_buffer_length)
    {
        GMExtWire.order(__arg_buffer);

        // field: callback, type: Function
        GMFunction callback = GMExtWire.readGMFunction(__arg_buffer, __dispatch_queue);

        AdMobError __result = admob_consent_show(callback);

        GMExtWire.order(__ret_buffer);
        GMExtWire.IByteWriter __ret_buffer_writer = new GMExtWire.GMBufferWriter(__ret_buffer);
        // return: __result, type: enum AdMobError
        GMExtWire.writeI32(__ret_buffer_writer, __result.value());

        return 0;
    }

    public double __EXT_NATIVE__admob_consent_reset()
    {
        admob_consent_reset();
        return 0;
    }

    public double __EXT_NATIVE__admob_consent_set_rdp(double enabled)
    {
        admob_consent_set_rdp(enabled != 0);
        return 0;
    }

    public double __EXT_NATIVE__admob_settings_set_volume(double value)
    {
        admob_settings_set_volume((double)value);
        return 0;
    }

    public double __EXT_NATIVE__admob_settings_set_muted(double muted)
    {
        admob_settings_set_muted(muted != 0);
        return 0;
    }

}