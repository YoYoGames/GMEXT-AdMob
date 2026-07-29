// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName};
import ${YYAndroidPackageName}.GMExtWire.GMFunction;
import ${YYAndroidPackageName}.GMExtWire.GMValue;
import ${YYAndroidPackageName}.enums.*;
import ${YYAndroidPackageName}.records.*;

import java.util.Optional;

public interface GMAdMobInterface {
    public AdMobError admob_initialize(GMFunction callback);
    public AdMobError admob_set_test_device_id();
    public void admob_events_on_paid_event(boolean enabled, GMFunction callback);
    public void admob_banner_set_ad_unit(String ad_unit_id);
    public AdMobError admob_banner_create(AdMobBannerSize size, boolean bottom, GMFunction callback);
    public AdMobError admob_banner_create_ext(AdMobBannerSize size, boolean bottom, AdMobBannerAlignment alignment, GMFunction callback);
    public double admob_banner_get_width();
    public double admob_banner_get_height();
    public void admob_banner_move(boolean bottom);
    public void admob_banner_show();
    public void admob_banner_hide();
    public void admob_banner_remove();
    public void admob_interstitial_set_ad_unit(String ad_unit_id);
    public AdMobError admob_interstitial_load(GMFunction callback, java.util.Optional<String> ad_unit_id);
    public boolean admob_interstitial_is_valid(long handle);
    public void admob_interstitial_dispose(long handle);
    public AdMobError admob_interstitial_show(long handle, GMFunction callback);
    public void admob_server_side_verification_set(String user_id, String custom_data);
    public void admob_server_side_verification_clear();
    public void admob_rewarded_video_set_ad_unit(String ad_unit_id);
    public AdMobError admob_rewarded_video_load(GMFunction callback, java.util.Optional<String> ad_unit_id);
    public boolean admob_rewarded_video_is_valid(long handle);
    public void admob_rewarded_video_dispose(long handle);
    public AdMobError admob_rewarded_video_show(long handle, GMFunction callback);
    public void admob_rewarded_interstitial_set_ad_unit(String ad_unit_id);
    public AdMobError admob_rewarded_interstitial_load(GMFunction callback, java.util.Optional<String> ad_unit_id);
    public boolean admob_rewarded_interstitial_is_valid(long handle);
    public void admob_rewarded_interstitial_dispose(long handle);
    public AdMobError admob_rewarded_interstitial_show(long handle, GMFunction callback);
    public void admob_app_open_ad_set_ad_unit(String ad_unit_id);
    public AdMobError admob_app_open_ad_enable(double orientation, GMFunction callback);
    public void admob_app_open_ad_disable();
    public boolean admob_app_open_ad_is_enabled();
    public boolean admob_app_open_ad_is_loaded();
    public AdMobError admob_app_open_ad_load(GMFunction callback);
    public AdMobError admob_app_open_ad_show(GMFunction callback);
    public void admob_targeting_coppa(boolean coppa);
    public void admob_targeting_under_age(boolean under_age);
    public void admob_targeting_max_ad_content_rating(AdMobMaxAdContentRating content_rating);
    public AdMobError admob_consent_request_info_update(AdMobConsentDebugGeography debug_geography, GMFunction callback);
    public AdMobConsentStatus admob_consent_get_status();
    public AdMobConsentType admob_consent_get_type();
    public boolean admob_consent_is_form_available();
    public AdMobError admob_consent_load(GMFunction callback);
    public AdMobError admob_consent_show(GMFunction callback);
    public void admob_consent_reset();
    public void admob_consent_set_rdp(boolean enabled);
    public void admob_settings_set_volume(double value);
    public void admob_settings_set_muted(boolean muted);
}