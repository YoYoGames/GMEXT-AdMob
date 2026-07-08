package ${YYAndroidPackageName};

import ${YYAndroidPackageName}.R;
import ${YYAndroidPackageName}.GMExtWire.GMFunction;
import ${YYAndroidPackageName}.enums.*;
import com.yoyogames.runner.RunnerJNILib;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.View;
import android.app.Activity;
import android.view.ViewGroup;

import java.lang.Exception;
import java.lang.ref.WeakReference;

import android.provider.Settings;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.RequestConfiguration;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;

import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback;

import com.google.android.gms.ads.FullScreenContentCallback;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdListener;

import com.google.android.gms.ads.LoadAdError;

import com.google.android.ump.*;

import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;

import androidx.annotation.NonNull;
import android.util.Log;

import android.util.DisplayMetrics;
import android.view.Display;

import java.util.Date;

import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdapterResponseInfo;

import android.os.Bundle;
import com.google.ads.mediation.admob.AdMobAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GMAdMob extends GMAdMobInternal {

    // Constants
    private static final long MAX_DOUBLE_SAFE = 9007199254740992L; // 2^53
    private static final int EVENT_OTHER_SOCIAL = 70;

    private static final int ADMOB_OK = 0;
    private static final int ADMOB_ERROR_NOT_INITIALIZED = -1;
    private static final int ADMOB_ERROR_INVALID_AD_ID = -2;
    private static final int ADMOB_ERROR_AD_LIMIT_REACHED = -3;
    private static final int ADMOB_ERROR_NO_ADS_LOADED = -4;
    private static final int ADMOB_ERROR_NO_ACTIVE_BANNER_AD = -5;
    private static final int ADMOB_ERROR_ILLEGAL_CALL = -6;
    private static final int ADMOB_ERROR_NULL_VIEW_HANDLER = -7;

    private static final int ADMOB_INIT_EVENT_INITIALIZED = 0;
    private static final int ADMOB_INIT_EVENT_FAILED = 1;

    private static final int ADMOB_BANNER_EVENT_LOADED = 0;
    private static final int ADMOB_BANNER_EVENT_LOAD_FAILED = 1;
    private static final int ADMOB_BANNER_EVENT_OPENED = 2;
    private static final int ADMOB_BANNER_EVENT_CLICKED = 3;
    private static final int ADMOB_BANNER_EVENT_CLOSED = 4;

    private static final int ADMOB_FULLSCREEN_EVENT_LOADED = 0;
    private static final int ADMOB_FULLSCREEN_EVENT_LOAD_FAILED = 1;
    private static final int ADMOB_FULLSCREEN_EVENT_FULLY_SHOWN = 2;
    private static final int ADMOB_FULLSCREEN_EVENT_SHOW_FAILED = 3;
    private static final int ADMOB_FULLSCREEN_EVENT_DISMISSED = 4;
    private static final int ADMOB_FULLSCREEN_EVENT_REWARD = 5;

    private static final int ADMOB_CONSENT_EVENT_REQUEST_INFO_UPDATED = 0;
    private static final int ADMOB_CONSENT_EVENT_REQUEST_INFO_UPDATE_FAILED = 1;
    private static final int ADMOB_CONSENT_EVENT_LOADED = 2;
    private static final int ADMOB_CONSENT_EVENT_LOAD_FAILED = 3;
    private static final int ADMOB_CONSENT_EVENT_SHOWN = 4;
    private static final int ADMOB_CONSENT_EVENT_SHOW_FAILED = 5;

    private static final int ADMOB_PAID_EVENT_PAID = 0;

	public static final int ADMOB_BANNER_ALIGNMENT_LEFT = 0;
	public static final int ADMOB_BANNER_ALIGNMENT_CENTER = 1;
	public static final int ADMOB_BANNER_ALIGNMENT_RIGHT = 2;

    private static final String LOG_TAG = "yoyo";//"AdMob";

    // WeakReference to Activity to prevent memory leaks
    private WeakReference<Activity> activityRef;

    // Root view to attach banner ads
    private final ViewGroup rootView;

    // AdMob settings
    private boolean isInitialized = false;
    private boolean isTestDevice = false;
    private boolean isRdpEnabled = false;
    private boolean isShowingAd = false;

    // Targeting options
    private boolean targetCOPPA = false;
    private boolean targetUnderAge = false;
    private String maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_G;

    // Banner ad variables
    private String bannerAdUnitId = "";
    private AdView bannerAdView = null;
    private AdSize bannerSize = null;
    private int currentBannerAlignment = RelativeLayout.CENTER_HORIZONTAL;
    private RelativeLayout bannerLayout = null;

    // Interstitial ad variables
    private String interstitialAdUnitId = "";
    private int interstitialAdQueueCapacity = 1;
    private final ConcurrentLinkedQueue<InterstitialAd> interstitialAdQueue = new ConcurrentLinkedQueue<>();

    // Server side verification variables
	private String serverSideVerificationUserId = null;
	private String serverSideVerificationCustomData = null;

    // Rewarded video ad variables
    private String rewardedUnitId = "";
    private int rewardedAdQueueCapacity = 1;
    private final ConcurrentLinkedQueue<RewardedAd> rewardedAdQueue = new ConcurrentLinkedQueue<>();

    // Rewarded interstitial ad variables
    private String rewardedInterstitialAdUnitId = "";
    private int rewardedAdInterstitialQueueCapacity = 1;
    private final ConcurrentLinkedQueue<RewardedInterstitialAd> rewardedInterstitialAdQueue = new ConcurrentLinkedQueue<>();

    // App Open ad variables
    private String appOpenAdUnitId = "";
    private int appOpenAdOrientation = Configuration.ORIENTATION_UNDEFINED;
    private long appOpenAdLoadTime = 0;
    private int appOpenAdExpirationTime = 4;
    private AppOpenAd appOpenAd = null;

    private boolean triggerOnPaidEvent = false;
    private boolean triggerAppOpenAd = false;

    // Consent variables
    private ConsentInformation consentInformation;
    private ConsentForm consentFormInstance;

    private GMFunction initializeCallback = null;
    private GMFunction paidEventCallback = null;
    private GMFunction bannerCallback = null;
    private GMFunction interstitialShowCallback = null;
    private GMFunction rewardedVideoShowCallback = null;
    private GMFunction rewardedInterstitialShowCallback = null;
    private GMFunction appOpenEnableCallback = null;
    private GMFunction appOpenLoadCallback = null;
    private GMFunction appOpenShowCallback = null;
    private GMFunction consentRequestInfoUpdateCallback = null;
    private GMFunction consentLoadCallback = null;
    private GMFunction consentShowCallback = null;

    private final ConcurrentLinkedQueue<GMFunction> interstitialLoadCallbacks = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<GMFunction> rewardedVideoLoadCallbacks = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<GMFunction> rewardedInterstitialLoadCallbacks = new ConcurrentLinkedQueue<>();

    public GMAdMob() {
		Activity activity = RunnerActivity.CurrentActivity;
		if (activity == null) {
			Log.w(LOG_TAG, "Activity reference is null in constructor.");
			activityRef = new WeakReference<>(null);
			rootView = null;
		} else {
			activityRef = new WeakReference<>(activity);
			rootView = activity.findViewById(android.R.id.content);
		}
    }


    // -------------------------------------------------------------------------
    // Extension Generator API
    //
    // Sync functions return values directly.
    // Async SDK operations return an immediate AdMobError code and later invoke
    // their callback with a JSON payload containing numeric event_type enums.
    // -------------------------------------------------------------------------

    public double admob_initialize(final GMFunction callback) {
		Log.i("yoyo","admob_initialize EXT");
        initializeCallback = callback;
        double code = AdMob_Initialize();
        if (code != ADMOB_OK) {
            initializeCallback = null;
            callbackResult(callback, ADMOB_INIT_EVENT_FAILED, code);
        }
        return code;
    }

    public double admob_set_test_device_id() {
        return AdMob_SetTestDeviceId();
    }

    public double admob_events_on_paid_event(boolean enabled, final GMFunction callback) {
        triggerOnPaidEvent = enabled;
        paidEventCallback = enabled ? callback : null;
        AdMob_Events_OnPaidEvent(enabled ? 1.0 : 0.0);
        return ADMOB_OK;
    }

    public void admob_banner_set_ad_unit(String ad_unit_id) {
        AdMob_Banner_Set_AdUnit(ad_unit_id);
    }

    public double admob_banner_create(AdMobBannerSize size, boolean bottom, final GMFunction callback) {
        bannerCallback = callback;
        double code = AdMob_Banner_Create(size.value(), bottom ? 1.0 : 0.0);
        if (code != ADMOB_OK) {
            bannerCallback = null;
            callbackResult(callback, ADMOB_BANNER_EVENT_LOAD_FAILED, code);
        }
        return code;
    }

    public double admob_banner_create_ext(
        AdMobBannerSize size,
        boolean bottom,
        AdMobBannerAlignment alignment,
        final GMFunction callback) {
        bannerCallback = callback;
        double code = AdMob_Banner_Create_Ext(
            size.value(),
            bottom ? 1.0 : 0.0,
            alignment.value()
        );
        if (code != ADMOB_OK) {
            bannerCallback = null;
            callbackResult(callback, ADMOB_BANNER_EVENT_LOAD_FAILED, code);
        }
        return code;
    }

    public double admob_banner_get_width() {
        return AdMob_Banner_GetWidth();
    }

    public double admob_banner_get_height() {
        return AdMob_Banner_GetHeight();
    }

    public double admob_banner_move(boolean bottom) {
        return AdMob_Banner_Move(bottom ? 1.0 : 0.0);
    }

    public double admob_banner_show() {
        return AdMob_Banner_Show();
    }

    public double admob_banner_hide() {
        return AdMob_Banner_Hide();
    }

    public double admob_banner_remove() {
        double code = AdMob_Banner_Remove();
        if (code == ADMOB_OK) bannerCallback = null;
        return code;
    }

    public void admob_interstitial_set_ad_unit(String ad_unit_id) {
        AdMob_Interstitial_Set_AdUnit(ad_unit_id);
    }

    public void admob_interstitial_free_loaded_instances(double count) {
        Admob_Interstitial_Free_Loaded_Instances(count);
    }

    public void admob_interstitial_max_instances(double value) {
        Admob_Interstitial_Max_Instances(value);
    }

    public double admob_interstitial_load(final GMFunction callback) {
        interstitialLoadCallbacks.offer(callback);
        double code = AdMob_Interstitial_Load();
        if (code != ADMOB_OK) {
            interstitialLoadCallbacks.remove(callback);
            callbackResult(callback, ADMOB_FULLSCREEN_EVENT_LOAD_FAILED, code);
        }
        return code;
    }

    public double admob_interstitial_show(final GMFunction callback) {
        interstitialShowCallback = callback;
        double code = AdMob_Interstitial_Show();
        if (code != ADMOB_OK) {
            interstitialShowCallback = null;
            callbackResult(callback, ADMOB_FULLSCREEN_EVENT_SHOW_FAILED, code);
        }
        return code;
    }

    public double admob_interstitial_is_loaded() {
        return AdMob_Interstitial_IsLoaded();
    }

    public double admob_interstitial_instances_count() {
        return AdMob_Interstitial_Instances_Count();
    }

    public double admob_server_side_verification_set(String user_id, String custom_data) {
        if (!isInitialized) return ADMOB_ERROR_NOT_INITIALIZED;
        AdMob_ServerSideVerification_Set(user_id, custom_data);
        return ADMOB_OK;
    }

    public double admob_server_side_verification_clear() {
        if (!isInitialized) return ADMOB_ERROR_NOT_INITIALIZED;
        AdMob_ServerSideVerification_Clear();
        return ADMOB_OK;
    }

    public void admob_rewarded_video_set_ad_unit(String ad_unit_id) {
        AdMob_RewardedVideo_Set_AdUnit(ad_unit_id);
    }

    public void admob_rewarded_video_free_loaded_instances(double count) {
        AdMob_RewardedVideo_Free_Loaded_Instances(count);
    }

    public void admob_rewarded_video_max_instances(double value) {
        AdMob_RewardedVideo_Max_Instances(value);
    }

    public double admob_rewarded_video_load(final GMFunction callback) {
        rewardedVideoLoadCallbacks.offer(callback);
        double code = AdMob_RewardedVideo_Load();
        if (code != ADMOB_OK) {
            rewardedVideoLoadCallbacks.remove(callback);
            callbackResult(callback, ADMOB_FULLSCREEN_EVENT_LOAD_FAILED, code);
        }
        return code;
    }

    public double admob_rewarded_video_show(final GMFunction callback) {
        rewardedVideoShowCallback = callback;
        double code = AdMob_RewardedVideo_Show();
        if (code != ADMOB_OK) {
            rewardedVideoShowCallback = null;
            callbackResult(callback, ADMOB_FULLSCREEN_EVENT_SHOW_FAILED, code);
        }
        return code;
    }

    public double admob_rewarded_video_is_loaded() {
        return AdMob_RewardedVideo_IsLoaded();
    }

    public double admob_rewarded_video_instances_count() {
        return AdMob_RewardedVideo_Instances_Count();
    }

    public void admob_rewarded_interstitial_set_ad_unit(String ad_unit_id) {
        AdMob_RewardedInterstitial_Set_AdUnit(ad_unit_id);
    }

    public void admob_rewarded_interstitial_free_loaded_instances(double count) {
        AdMob_RewardedInterstitial_Free_Loaded_Instances(count);
    }

    public void admob_rewarded_interstitial_max_instances(double value) {
        AdMob_RewardedInterstitial_Max_Instances(value);
    }

    public double admob_rewarded_interstitial_load(final GMFunction callback) {
        rewardedInterstitialLoadCallbacks.offer(callback);
        double code = AdMob_RewardedInterstitial_Load();
        if (code != ADMOB_OK) {
            rewardedInterstitialLoadCallbacks.remove(callback);
            callbackResult(callback, ADMOB_FULLSCREEN_EVENT_LOAD_FAILED, code);
        }
        return code;
    }

    public double admob_rewarded_interstitial_show(final GMFunction callback) {
        rewardedInterstitialShowCallback = callback;
        double code = AdMob_RewardedInterstitial_Show();
        if (code != ADMOB_OK) {
            rewardedInterstitialShowCallback = null;
            callbackResult(callback, ADMOB_FULLSCREEN_EVENT_SHOW_FAILED, code);
        }
        return code;
    }

    public double admob_rewarded_interstitial_is_loaded() {
        return AdMob_RewardedInterstitial_IsLoaded();
    }

    public double admob_rewarded_interstitial_instances_count() {
        return AdMob_RewardedInterstitial_Instances_Count();
    }

    public void admob_app_open_ad_set_ad_unit(String ad_unit_id) {
        AdMob_AppOpenAd_Set_AdUnit(ad_unit_id);
    }

    public double admob_app_open_ad_enable(double orientation, final GMFunction callback) {
        appOpenEnableCallback = callback;
        double code = AdMob_AppOpenAd_Enable(orientation);
        if (code != ADMOB_OK) {
            appOpenEnableCallback = null;
            callbackResult(callback, ADMOB_FULLSCREEN_EVENT_LOAD_FAILED, code);
        }
        return code;
    }

    public void admob_app_open_ad_disable() {
        AdMob_AppOpenAd_Disable();
        appOpenEnableCallback = null;
    }

    public double admob_app_open_ad_is_enabled() {
        return AdMob_AppOpenAd_IsEnabled();
    }

    public double admob_app_open_ad_is_loaded() {
        return AdMob_AppOpenAd_IsLoaded();
    }

    public double admob_app_open_ad_load(final GMFunction callback) {
        appOpenLoadCallback = callback;
        double code = AdMob_AppOpenAd_Load();
        if (code != ADMOB_OK) {
            appOpenLoadCallback = null;
            callbackResult(callback, ADMOB_FULLSCREEN_EVENT_LOAD_FAILED, code);
        }
        return code;
    }

    public double admob_app_open_ad_show(final GMFunction callback) {
        appOpenShowCallback = callback;
        double code = AdMob_AppOpenAd_Show();
        if (code != ADMOB_OK) {
            appOpenShowCallback = null;
            callbackResult(callback, ADMOB_FULLSCREEN_EVENT_SHOW_FAILED, code);
        }
        return code;
    }

    public double admob_targeting_coppa(boolean coppa) {
        return AdMob_Targeting_COPPA(coppa ? 1.0 : 0.0);
    }

    public double admob_targeting_under_age(boolean under_age) {
        return AdMob_Targeting_UnderAge(under_age ? 1.0 : 0.0);
    }

    public double admob_targeting_max_ad_content_rating(AdMobMaxAdContentRating content_rating) {
        return AdMob_Targeting_MaxAdContentRating(content_rating.value());
    }

    public double admob_consent_request_info_update(
        AdMobConsentDebugGeography debug_geography,
        final GMFunction callback) {
        if (!validateViewHandler("admob_consent_request_info_update")) {
            callbackResult(callback, ADMOB_CONSENT_EVENT_REQUEST_INFO_UPDATE_FAILED, ADMOB_ERROR_NULL_VIEW_HANDLER);
            return ADMOB_ERROR_NULL_VIEW_HANDLER;
        }
        consentRequestInfoUpdateCallback = callback;
        AdMob_Consent_RequestInfoUpdate(debug_geography.value());
        return ADMOB_OK;
    }

    public double admob_consent_get_status() {
        return AdMob_Consent_GetStatus();
    }

    public double admob_consent_get_type() {
        return AdMob_Consent_GetType();
    }

    public double admob_consent_is_form_available() {
        return AdMob_Consent_IsFormAvailable();
    }

    public double admob_consent_load(final GMFunction callback) {
        if (!validateViewHandler("admob_consent_load")) {
            callbackResult(callback, ADMOB_CONSENT_EVENT_LOAD_FAILED, ADMOB_ERROR_NULL_VIEW_HANDLER);
            return ADMOB_ERROR_NULL_VIEW_HANDLER;
        }
        consentLoadCallback = callback;
        AdMob_Consent_Load();
        return ADMOB_OK;
    }

    public double admob_consent_show(final GMFunction callback) {
        if (!validateViewHandler("admob_consent_show")) {
            callbackResult(callback, ADMOB_CONSENT_EVENT_SHOW_FAILED, ADMOB_ERROR_NULL_VIEW_HANDLER);
            return ADMOB_ERROR_NULL_VIEW_HANDLER;
        }
        if (consentFormInstance == null) {
            callbackResult(callback, ADMOB_CONSENT_EVENT_SHOW_FAILED, ADMOB_ERROR_NO_ADS_LOADED);
            return ADMOB_ERROR_NO_ADS_LOADED;
        }
        consentShowCallback = callback;
        AdMob_Consent_Show();
        return ADMOB_OK;
    }

    public void admob_consent_reset() {
        AdMob_Consent_Reset();
    }

    public void admob_consent_set_rdp(boolean enabled) {
        AdMob_Consent_Set_RDP(enabled ? 1.0 : 0.0);
    }

    public void admob_settings_set_volume(double value) {
        AdMob_Settings_SetVolume(value);
    }

    public void admob_settings_set_muted(boolean muted) {
        AdMob_Settings_SetMuted(muted ? 1.0 : 0.0);
    }

    // #region Setup

    public double AdMob_Initialize() {

		final String callingMethod = "AdMob_Initialize";

        if (!validateNotInitialized(callingMethod)) return ADMOB_ERROR_ILLEGAL_CALL;

		if (!validateViewHandler(callingMethod)) return ADMOB_ERROR_NULL_VIEW_HANDLER;

        // Run initialization in a background thread
        new Thread(() -> {
            MobileAds.setRequestConfiguration(buildRequestConfiguration(callingMethod));

            try {
                Activity activity = getActivity(callingMethod);
                if (activity == null) return;
                

                MobileAds.initialize(activity, initializationStatus -> {

                    Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                    for (String adapterClass : statusMap.keySet()) {
                        AdapterStatus status = statusMap.get(adapterClass);
                        assert status != null;
                        Log.d(LOG_TAG, String.format("Adapter name: %s, Description: %s, Latency: %d",
                                adapterClass, status.getDescription(), status.getLatency()));
                    }

                    sendAsyncEvent("AdMob_OnInitialized", null);

                    // Initialize ad units from extension options if available
                    initializeAdUnits();

                    isInitialized = true;
                });
            } catch (Exception e) {
                Log.i(LOG_TAG, "GoogleMobileAds Init Error: " + e.toString());
                Log.i(LOG_TAG, e.toString());
            }
        }).start();

        return ADMOB_OK;
    }

    private void initializeAdUnits() {
        bannerAdUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_BANNER");
        interstitialAdUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_INTERSTITIAL");
        rewardedUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_REWARDED");
        rewardedInterstitialAdUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_REWARDED_INTERSTITIAL");
        appOpenAdUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_OPENAPPAD");
    }

    public double AdMob_SetTestDeviceId() {
        if (!validateNotInitialized("AdMob_SetTestDeviceId")) return ADMOB_ERROR_ILLEGAL_CALL;

        isTestDevice = true;
        return ADMOB_OK;
    }

    public void AdMob_Events_OnPaidEvent(double enabled) {
        triggerOnPaidEvent = enabled >= 0.5;
    }

    private RequestConfiguration buildRequestConfiguration(final String callingMethod) {

        RequestConfiguration.Builder requestConfigurationBuilder = MobileAds.getRequestConfiguration().toBuilder();

        if (isTestDevice) {
            List<String> testDeviceIds = Collections.singletonList(getDeviceID(callingMethod));
            requestConfigurationBuilder.setTestDeviceIds(testDeviceIds);
        }

        if (targetCOPPA)
            requestConfigurationBuilder.setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE);

        if (targetUnderAge)
            requestConfigurationBuilder.setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE);

        requestConfigurationBuilder.setMaxAdContentRating(maxAdContentRating);

        return requestConfigurationBuilder.build();
    }

    // #endregion

    // #region Banner

    public void AdMob_Banner_Set_AdUnit(String adUnitId) {
        bannerAdUnitId = adUnitId;
    }

    public double AdMob_Banner_Create(final double size, final double bottom) {

        final String callingMethod = "AdMob_Banner_Create";

		if (!validateInitialized(callingMethod))
			return ADMOB_ERROR_NOT_INITIALIZED;

		if (!validateAdId(bannerAdUnitId, callingMethod))
			return ADMOB_ERROR_INVALID_AD_ID;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

		boolean isBottom = bottom > 0.5;
        currentBannerAlignment = RelativeLayout.CENTER_HORIZONTAL;

		// Call the helper method with default horizontal alignment ("center")
		createBannerAdView(size, isBottom, currentBannerAlignment, callingMethod);

		return ADMOB_OK;
    }

	public double AdMob_Banner_Create_Ext(final double size, final double bottom, final double horizontalAlignment) {

		final String callingMethod = "AdMob_Banner_Create_Ext";
	
		if (!validateInitialized(callingMethod))
			return ADMOB_ERROR_NOT_INITIALIZED;
	
		if (!validateAdId(bannerAdUnitId, callingMethod))
			return ADMOB_ERROR_INVALID_AD_ID;
	
		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;
	
		boolean isBottom = bottom > 0.5;
	
		// Validate horizontalAlignment parameter
		switch ((int) horizontalAlignment) {
            case ADMOB_BANNER_ALIGNMENT_LEFT:
                currentBannerAlignment = RelativeLayout.ALIGN_PARENT_LEFT;
                break;
            case ADMOB_BANNER_ALIGNMENT_CENTER:
                currentBannerAlignment = RelativeLayout.CENTER_HORIZONTAL;
                break;
            case ADMOB_BANNER_ALIGNMENT_RIGHT:
                currentBannerAlignment = RelativeLayout.ALIGN_PARENT_RIGHT;
                break;
            default:
                Log.w(LOG_TAG, callingMethod + " :: Invalid horizontal alignment parameter. Defaulting to CENTER.");
                currentBannerAlignment = RelativeLayout.CENTER_HORIZONTAL;
        }
	
		// Call the helper method with the specified horizontal alignment
		createBannerAdView(size, isBottom, currentBannerAlignment, callingMethod);
	
		return ADMOB_OK;
	}

    public double AdMob_Banner_GetWidth() {
        if (bannerAdView == null) return 0;
        return bannerSize.getWidthInPixels(RunnerJNILib.ms_context);
    }

    public double AdMob_Banner_GetHeight() {
        if (bannerAdView == null) return 0;
        int height = bannerSize.getHeightInPixels(RunnerJNILib.ms_context);
            if (bannerSize == AdSize.SMART_BANNER) {
            DisplayMetrics displayMetrics = RunnerJNILib.ms_context.getResources().getDisplayMetrics();
            int screenHeightInDP = Math.round(displayMetrics.heightPixels / displayMetrics.density);
            int density = Math.round(displayMetrics.density);
                if (screenHeightInDP < 400)
                height = 32 * density;
            else if (screenHeightInDP <= 720)
                height = 50 * density;
            else
                height = 90 * density;
        }
        return height;
    }

    public double AdMob_Banner_Move(final double bottom) {

        final String callingMethod = "AdMob_Banner_Move";
    
        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;
    
        if (!validateActiveBannerAd(callingMethod))
            return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;
    
        if (!validateViewHandler(callingMethod))
            return ADMOB_ERROR_NULL_VIEW_HANDLER;
    
        RunnerActivity.ViewHandler.post(() -> {
    
            if (!validateActiveBannerAd(callingMethod))
                return;
    
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
    
            // Reuse the stored horizontal alignment from banner creation
            params.addRule(currentBannerAlignment);
            // Update the vertical alignment based on the 'bottom' parameter
            params.addRule(bottom > 0.5 ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
    
            bannerAdView.setLayoutParams(params);
        });
    
        return ADMOB_OK;
    }

    public double AdMob_Banner_Show() {

        final String callingMethod = "AdMob_Banner_Show";

		if (!validateInitialized(callingMethod))
			return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateActiveBannerAd(callingMethod))
            return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        RunnerActivity.ViewHandler.post(() -> {

            if (!validateActiveBannerAd(callingMethod))
                return;

            bannerAdView.setVisibility(View.VISIBLE);
        });
        return ADMOB_OK;
    }

    public double AdMob_Banner_Hide() {

        final String callingMethod = "AdMob_Banner_Hide";

        if (!validateActiveBannerAd(callingMethod))
            return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        RunnerActivity.ViewHandler.post(() -> {

            if (!validateActiveBannerAd(callingMethod))
                return;

            bannerAdView.setVisibility(View.GONE);
        });
        return 0;
    }

    public double AdMob_Banner_Remove() {

        final String callingMethod = "AdMob_Banner_Remove";

        if (!validateActiveBannerAd(callingMethod))
            return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        RunnerActivity.ViewHandler.post(() -> {

            if (!validateActiveBannerAd(callingMethod))
                return;

            deleteBannerAdView();
        });
        return 0;
    }

	private void createBannerAdView(final double size, final boolean isBottom, final int horizontalAlignment, final String callingMethod) {
		RunnerActivity.ViewHandler.post(() -> {
			if (bannerAdView != null) {
				deleteBannerAdView();
			}
				
			Activity activity = getActivity(callingMethod);
			if (activity == null) return;
	
			bannerSize = getAdSize(size, callingMethod);
			if (bannerSize == null) return;

			bannerLayout = new RelativeLayout(activity);
            ViewCompat.setOnApplyWindowInsetsListener(bannerLayout, (v, insets) -> {
                Insets sysBars = insets.getInsets(
                    WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout()
                );
                int topPad = isBottom ? 0 : sysBars.top; // uniquement quand ancrée en haut
                v.setPadding(0, topPad, 0, 0);
                return insets; // on ne consomme pas, on laisse propager
            });
			bannerAdView = new AdView(activity);
	
			if (triggerOnPaidEvent) {
				bannerAdView.setOnPaidEventListener(adValue -> {
					AdapterResponseInfo loadedAdapterResponseInfo = Objects.requireNonNull(bannerAdView.getResponseInfo())
							.getLoadedAdapterResponseInfo();
					if (loadedAdapterResponseInfo == null) return;
					onPaidEventHandler(adValue, bannerAdView.getAdUnitId(), "Banner",
							loadedAdapterResponseInfo,
							bannerAdView.getResponseInfo().getMediationAdapterClassName());
				});
			}

            // Define layout parameters for bannerAdView
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, // Width set to WRAP_CONTENT
                RelativeLayout.LayoutParams.WRAP_CONTENT  // Height set to WRAP_CONTENT
            ); 

            // Set horizontal alignment based on the provided parameter
            params.addRule(currentBannerAlignment);

            // Set vertical alignment
            params.addRule(isBottom ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);

            // Add the AdView to bannerLayout with the defined layout parameters
            bannerLayout.addView(bannerAdView, params);

			// Define layout parameters for bannerLayout to span the parent width
            RelativeLayout.LayoutParams bannerLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            );

            // Add bannerLayout to rootView with the defined layout parameters
            rootView.addView(bannerLayout, bannerLayoutParams);
            ViewCompat.requestApplyInsets(bannerLayout);
			bannerAdView.setAdListener(new AdListener() {
	
				@Override
				public void onAdLoaded() {
					sendAsyncEvent("AdMob_Banner_OnLoaded", null);
				}
	
				@Override
				public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
					Map<String, Object> data = new HashMap<>();
					data.put("errorMessage", loadAdError.getMessage());
					data.put("errorCode", (double) loadAdError.getCode());
					sendAsyncEvent("AdMob_Banner_OnLoadFailed", data);
				}
				
				@Override
				public void onAdOpened() {
					sendAsyncEvent("AdMob_Banner_OnOpened", null);
				}

				@Override
				public void onAdClicked() {
					sendAsyncEvent("AdMob_Banner_OnClicked", null);
				}

				@Override
				public void onAdClosed() {
					sendAsyncEvent("AdMob_Banner_OnClosed", null);
				}
			});

			bannerAdView.setAdSize(bannerSize);
			bannerAdView.setAdUnitId(bannerAdUnitId);
			bannerAdView.requestLayout();
			bannerAdView.setVisibility(View.VISIBLE);
	
			bannerAdView.loadAd(buildAdRequest());
		});
	}

    private void deleteBannerAdView() {
		cleanAd(bannerAdView, this::cleanUpAd);

        bannerLayout.removeView(bannerAdView);
        bannerAdView.destroy();
        bannerAdView = null;
        rootView.removeView(bannerLayout);
        bannerLayout = null;
        bannerSize = null;
    }

    private AdSize getAdSize(double size, final String callingMethod) {
        Activity activity = getActivity(callingMethod);
        if (activity == null) return null;
		
		switch ((int) size) {
            case 0:
                return AdSize.BANNER;
            case 1:
                return AdSize.LARGE_BANNER;
            case 2:
                return AdSize.MEDIUM_RECTANGLE;
            case 3:
                return AdSize.FULL_BANNER;
            case 4:
                return AdSize.LEADERBOARD;
            case 5:
                return AdSize.SMART_BANNER;
            case 6:
                Display display = activity.getWindowManager().getDefaultDisplay();
                DisplayMetrics outMetrics = new DisplayMetrics();
                display.getMetrics(outMetrics);

                float widthPixels = outMetrics.widthPixels;
                float density = outMetrics.density;

                int adWidth = (int) (widthPixels / density);

                return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
            default:
				Log.w(LOG_TAG, callingMethod + " :: Invalid banner size.");
                return null;
        }
    }

    // #endregion

    // #region Interstitial

    public void AdMob_Interstitial_Set_AdUnit(String adUnitId) {
        interstitialAdUnitId = adUnitId;
    }

    public void Admob_Interstitial_Free_Loaded_Instances(double count) {
		freeLoadedInstances(interstitialAdQueue, count, this::cleanUpAd);
    }

    public void Admob_Interstitial_Max_Instances(double value) {
        interstitialAdQueueCapacity = (int) value;
		trimLoadedAdsQueue(interstitialAdQueue, interstitialAdQueueCapacity, this::cleanUpAd);
    }

    public double AdMob_Interstitial_Load() {

        final String callingMethod = "AdMob_Interstitial_Load";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(interstitialAdUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

        if (!validateLoadedAdsLimit(interstitialAdQueue, interstitialAdQueueCapacity, callingMethod))
            return ADMOB_ERROR_AD_LIMIT_REACHED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        loadInterstitialAd(interstitialAdUnitId, interstitialAdQueue, interstitialAdQueueCapacity, callingMethod);

        return ADMOB_OK;
    }

    public double AdMob_Interstitial_Show() {

        final String callingMethod = "AdMob_Interstitial_Show";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdLoaded(interstitialAdQueue, callingMethod))
            return ADMOB_ERROR_NO_ADS_LOADED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        showInterstitialAd(interstitialAdQueue, callingMethod);

        return ADMOB_OK;
    }

    public double AdMob_Interstitial_IsLoaded() {
        return AdMob_Interstitial_Instances_Count() > 0 ? 1.0 : 0.0;
    }

    public double AdMob_Interstitial_Instances_Count() {
        return interstitialAdQueue.size();
    }

    private void loadInterstitialAd(final String adUnitId, final ConcurrentLinkedQueue<InterstitialAd> adQueue, final int maxInstances, final String callingMethod) {
        RunnerActivity.ViewHandler.post(() -> {
            
			Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            // Use application context
            Context appContext = activity.getApplicationContext();

            InterstitialAd.load(appContext, adUnitId, buildAdRequest(), new InterstitialAdLoadCallback() {

                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                    if (adQueue.size() >= maxInstances) {
                        Log.i(LOG_TAG, callingMethod + " :: Maximum number of loaded ads reached.");
                        return;
                    }

                    adQueue.offer(interstitialAd);

                    if (triggerOnPaidEvent) {
                        interstitialAd.setOnPaidEventListener(adValue -> {
                            AdapterResponseInfo loadedAdapterResponseInfo = interstitialAd.getResponseInfo().getLoadedAdapterResponseInfo();
                            if (loadedAdapterResponseInfo == null) return;
                            onPaidEventHandler(adValue, interstitialAd.getAdUnitId(), "Interstitial",
                                    loadedAdapterResponseInfo,
                                    interstitialAd.getResponseInfo().getMediationAdapterClassName());
                        });
                    }

					Map<String, Object> data = new HashMap<>();
					data.put("unit_id", adUnitId);
					sendAsyncEvent("AdMob_Interstitial_OnLoaded", data);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("unit_id", adUnitId);
                    data.put("errorMessage", loadAdError.getMessage());
                    data.put("errorCode", (double) loadAdError.getCode());
                    sendAsyncEvent("AdMob_Interstitial_OnLoadFailed", data);
                }
            });
        });
    }
	
    private void showInterstitialAd(final ConcurrentLinkedQueue<InterstitialAd> adQueue, final String callingMethod) {

        final InterstitialAd interstitialAdRef = adQueue.poll();
        RunnerActivity.ViewHandler.post(() -> {

            if (interstitialAdRef == null) return;

			Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            interstitialAdRef.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
                	cleanAd(interstitialAdRef, ad -> cleanUpAd(ad));

					Map<String, Object> data = new HashMap<>();
					data.put("unit_id", interstitialAdRef.getAdUnitId());
					sendAsyncEvent("AdMob_Interstitial_OnDismissed", data);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    isShowingAd = false; // Reset the flag

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
                	cleanAd(interstitialAdRef, ad -> cleanUpAd(ad));

                    Map<String, Object> data = new HashMap<>();
                    data.put("unit_id", interstitialAdRef.getAdUnitId());
                    data.put("errorMessage", adError.getMessage());
                    data.put("errorCode", (double) adError.getCode());
                    sendAsyncEvent("AdMob_Interstitial_OnShowFailed", data);
                }

                @Override
                public void onAdShowedFullScreenContent() {

					Map<String, Object> data = new HashMap<>();
					data.put("unit_id", interstitialAdRef.getAdUnitId());
					sendAsyncEvent("AdMob_Interstitial_OnFullyShown", data);
                }
            });

            interstitialAdRef.show(activity);
            isShowingAd = true;
        });
    }

    // #endregion

    // #region Server Side Verification

    public void AdMob_ServerSideVerification_Set(final String userId, final String customData) {
        final String callingMethod = "AdMob_ServerSideVerification_Set";
    
		if (!validateInitialized(callingMethod))
			return;

        serverSideVerificationUserId = userId;
        serverSideVerificationCustomData = customData;
    }

    public void AdMob_ServerSideVerification_Clear() {
        final String callingMethod = "AdMob_ServerSideVerification_Clear";
    
		if (!validateInitialized(callingMethod))
			return;

        serverSideVerificationUserId = null;
        serverSideVerificationCustomData = null;
    }

    private void configureServerSideVerification(Object ad, String userId, String customData) {
        if (ad == null) {
            Log.e(LOG_TAG, "Ad instance is null. Cannot configure server-side verification.");
            return;
        }
    
        if (userId != null && !userId.isEmpty() || customData != null && !customData.isEmpty()) {
            ServerSideVerificationOptions.Builder ssvBuilder = new ServerSideVerificationOptions.Builder();
    
            if (userId != null && !userId.isEmpty()) {
                ssvBuilder.setUserId(userId);
            }
    
            if (customData != null && !customData.isEmpty()) {
                ssvBuilder.setCustomData(customData);
            }
    
            ServerSideVerificationOptions ssvOptions = ssvBuilder.build();
    
            if (ad instanceof RewardedAd) {
                ((RewardedAd) ad).setServerSideVerificationOptions(ssvOptions);
            } else if (ad instanceof RewardedInterstitialAd) {
                ((RewardedInterstitialAd) ad).setServerSideVerificationOptions(ssvOptions);
            } else {
                Log.e(LOG_TAG, "Unsupported ad type for server-side verification.");
            }
        }
    }

    // #endregion

    // #region Rewarded

    public void AdMob_RewardedVideo_Set_AdUnit(String adUnitId) {
        rewardedUnitId = adUnitId;
    }

    public void AdMob_RewardedVideo_Free_Loaded_Instances(double count) {
		freeLoadedInstances(rewardedAdQueue, count, this::cleanUpAd);
    }

    public void AdMob_RewardedVideo_Max_Instances(double value) {
        rewardedAdQueueCapacity = (int) value;
        trimLoadedAdsQueue(rewardedAdQueue, rewardedAdQueueCapacity, this::cleanUpAd);
    }

    public double AdMob_RewardedVideo_Load() {

        final String callingMethod = "AdMob_RewardedVideo_Load";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(rewardedUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

        if (!validateLoadedAdsLimit(rewardedAdQueue, rewardedAdQueueCapacity, callingMethod))
            return ADMOB_ERROR_AD_LIMIT_REACHED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        loadRewardedAd(rewardedUnitId, rewardedAdQueue, rewardedAdQueueCapacity, callingMethod);

        return ADMOB_OK;
    }

    public double AdMob_RewardedVideo_Show() {

        final String callingMethod = "AdMob_RewardedVideo_Show";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdLoaded(rewardedAdQueue, callingMethod))
            return ADMOB_ERROR_NO_ADS_LOADED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        showRewardedAd(rewardedAdQueue, callingMethod);

        return ADMOB_OK;
    }

    public double AdMob_RewardedVideo_IsLoaded() {
        return AdMob_RewardedVideo_Instances_Count() > 0 ? 1.0 : 0.0;
    }

    public double AdMob_RewardedVideo_Instances_Count() {
        return rewardedAdQueue.size();
    }

    private void loadRewardedAd(final String adUnitId, final ConcurrentLinkedQueue<RewardedAd> adQueue, final int maxInstances, final String callingMethod) {
        RunnerActivity.ViewHandler.post(() -> {

            Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            // Use application context
            Context appContext = activity.getApplicationContext();

            RewardedAd.load(appContext, adUnitId, buildAdRequest(), new RewardedAdLoadCallback() {

                @Override
                public void onAdLoaded(@NonNull RewardedAd rewardedAd) {

					if (adQueue.size() >= maxInstances) {
                        Log.i(LOG_TAG, callingMethod + " :: Maximum number of loaded ads reached.");
                        return;
                    }

                    final String userId = serverSideVerificationUserId;
                    final String customData = serverSideVerificationCustomData;

					// Configure server-side verification using the helper method
                    configureServerSideVerification(rewardedAd, userId, customData);

                    adQueue.offer(rewardedAd);

                    if (triggerOnPaidEvent) {
                        rewardedAd.setOnPaidEventListener(adValue -> {
                            AdapterResponseInfo loadedAdapterResponseInfo = rewardedAd.getResponseInfo().getLoadedAdapterResponseInfo();
                            if (loadedAdapterResponseInfo == null) return;
                            onPaidEventHandler(adValue, rewardedAd.getAdUnitId(), "RewardedVideo",
                                    loadedAdapterResponseInfo,
                                    rewardedAd.getResponseInfo().getMediationAdapterClassName());
                        });
                    }

					Map<String, Object> data = new HashMap<>();
					data.put("unit_id", adUnitId);
					sendAsyncEvent("AdMob_RewardedVideo_OnLoaded", data);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("unit_id", adUnitId);
                    data.put("errorMessage", loadAdError.getMessage());
                    data.put("errorCode", (double) loadAdError.getCode());
                    sendAsyncEvent("AdMob_RewardedVideo_OnLoadFailed", data);
                }
            });
        });
    }

    private void showRewardedAd(final ConcurrentLinkedQueue<RewardedAd> adQueue, final String callingMethod) {

        if (!validateAdLoaded(adQueue, callingMethod))
            return;

        final RewardedAd rewardedAdRef = adQueue.poll();
        RunnerActivity.ViewHandler.post(() -> {

            if (rewardedAdRef == null) return;

			Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            rewardedAdRef.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {

					// Use the generic cleanAd method with cleanUpAd as the cleaner
					cleanAd(rewardedAdRef, ad -> cleanUpAd(ad));

					Map<String, Object> data = new HashMap<>();
					data.put("unit_id", rewardedAdRef.getAdUnitId());
					sendAsyncEvent("AdMob_RewardedVideo_OnDismissed", data);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    isShowingAd = false; // Reset the flag

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
					cleanAd(rewardedAdRef, ad -> cleanUpAd(ad));

                    Map<String, Object> data = new HashMap<>();
                    data.put("unit_id", rewardedAdRef.getAdUnitId());
                    data.put("errorMessage", adError.getMessage());
                    data.put("errorCode", (double) adError.getCode());
                    sendAsyncEvent("AdMob_RewardedVideo_OnShowFailed", data);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    Map<String, Object> data = new HashMap<>();
					data.put("unit_id", rewardedAdRef.getAdUnitId());
					sendAsyncEvent("AdMob_RewardedVideo_OnFullyShown", data);
                }
            });

            rewardedAdRef.show(activity, rewardItem -> {
                int rewardAmount = rewardItem.getAmount();
                String rewardType = rewardItem.getType();

                Map<String, Object> data = new HashMap<>();
                data.put("unit_id", rewardedAdRef.getAdUnitId());
                data.put("reward_amount", (double) rewardAmount);
                data.put("reward_type", rewardType);
                sendAsyncEvent("AdMob_RewardedVideo_OnReward", data);
            });

            isShowingAd = true;
        });
    }

    // #endregion

    // #region Rewarded Interstitial

    public void AdMob_RewardedInterstitial_Set_AdUnit(String adUnitId) {
        rewardedInterstitialAdUnitId = adUnitId;
    }

    public void AdMob_RewardedInterstitial_Free_Loaded_Instances(double count) {
		freeLoadedInstances(rewardedInterstitialAdQueue, count, this::cleanUpAd);
    }

    public void AdMob_RewardedInterstitial_Max_Instances(double value) {
        rewardedAdInterstitialQueueCapacity = (int) value;
        trimLoadedAdsQueue(rewardedInterstitialAdQueue, rewardedAdInterstitialQueueCapacity, this::cleanUpAd);
    }

    public double AdMob_RewardedInterstitial_Load() {

        final String callingMethod = "AdMob_RewardedInterstitial_Load";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(rewardedInterstitialAdUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

        if (!validateLoadedAdsLimit(rewardedInterstitialAdQueue, rewardedAdInterstitialQueueCapacity, callingMethod))
            return ADMOB_ERROR_AD_LIMIT_REACHED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        loadRewardedInterstitialAd(rewardedInterstitialAdUnitId, rewardedInterstitialAdQueue, rewardedAdInterstitialQueueCapacity, callingMethod);

        return ADMOB_OK;
    }

    public double AdMob_RewardedInterstitial_Show() {

        final String callingMethod = "AdMob_RewardedInterstitial_Show";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdLoaded(rewardedInterstitialAdQueue, callingMethod))
            return ADMOB_ERROR_NO_ADS_LOADED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        showRewardedInterstitialAd(rewardedInterstitialAdQueue, callingMethod);

        return ADMOB_OK;
    }

    public double AdMob_RewardedInterstitial_IsLoaded() {
        return AdMob_RewardedInterstitial_Instances_Count() > 0 ? 1.0 : 0.0;
    }

    public double AdMob_RewardedInterstitial_Instances_Count() {
        return rewardedInterstitialAdQueue.size();
    }

    private void loadRewardedInterstitialAd(final String adUnitId, final ConcurrentLinkedQueue<RewardedInterstitialAd> adQueue, final int maxInstances, final String callingMethod) {
        RunnerActivity.ViewHandler.post(() -> {

            Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            // Use application context
            Context appContext = activity.getApplicationContext();

            RewardedInterstitialAd.load(appContext, adUnitId, buildAdRequest(), new RewardedInterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {

                    if (adQueue.size() >= maxInstances) {
                        Log.i(LOG_TAG, callingMethod + " :: Maximum number of loaded ads reached.");
                        return;
                    }

                    final String userId = serverSideVerificationUserId;
                    final String customData = serverSideVerificationCustomData;

					// Configure server-side verification using the helper method
                    configureServerSideVerification(rewardedInterstitialAd, userId, customData);

                    adQueue.offer(rewardedInterstitialAd);

                    if (triggerOnPaidEvent) {
                        rewardedInterstitialAd.setOnPaidEventListener(adValue -> {
                            AdapterResponseInfo loadedAdapterResponseInfo = rewardedInterstitialAd.getResponseInfo().getLoadedAdapterResponseInfo();
                            if (loadedAdapterResponseInfo == null) return;
                            onPaidEventHandler(adValue, rewardedInterstitialAd.getAdUnitId(), "RewardedInterstitial",
                                    loadedAdapterResponseInfo,
                                    rewardedInterstitialAd.getResponseInfo().getMediationAdapterClassName());
                        });
                    }

					Map<String, Object> data = new HashMap<>();
                    data.put("unit_id", adUnitId);
                    sendAsyncEvent("AdMob_RewardedInterstitial_OnLoaded", data);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("unit_id", adUnitId);
                    data.put("errorMessage", loadAdError.getMessage());
                    data.put("errorCode", (double) loadAdError.getCode());
                    sendAsyncEvent("AdMob_RewardedInterstitial_OnLoadFailed", data);
                }
            });
        });
    }

    private void showRewardedInterstitialAd(final ConcurrentLinkedQueue<RewardedInterstitialAd> adQueue, final String callingMethod) {
        if (!validateAdLoaded(adQueue, callingMethod))
            return;

        final RewardedInterstitialAd rewardedInterstitialAdRef = adQueue.poll();
        RunnerActivity.ViewHandler.post(() -> {

            if (rewardedInterstitialAdRef == null) return;

			Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            rewardedInterstitialAdRef.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {

					// Use the generic cleanAd method with cleanUpAd as the cleaner
					cleanAd(rewardedInterstitialAdRef, ad -> cleanUpAd(ad));

					Map<String, Object> data = new HashMap<>();
                    data.put("unit_id", rewardedInterstitialAdRef.getAdUnitId());
                    sendAsyncEvent("AdMob_RewardedInterstitial_OnDismissed", data);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    isShowingAd = false; // Reset the flag

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
					cleanAd(rewardedInterstitialAdRef, ad -> cleanUpAd(ad));

                    Map<String, Object> data = new HashMap<>();
                    data.put("unit_id", rewardedInterstitialAdRef.getAdUnitId());
                    data.put("errorMessage", adError.getMessage());
                    data.put("errorCode", (double) adError.getCode());
                    sendAsyncEvent("AdMob_RewardedInterstitial_OnShowFailed", data);
                }

                @Override
                public void onAdShowedFullScreenContent() {
					Map<String, Object> data = new HashMap<>();
                    data.put("unit_id", rewardedInterstitialAdRef.getAdUnitId());
                    sendAsyncEvent("AdMob_RewardedInterstitial_OnFullyShown", data);
                }
            });

            rewardedInterstitialAdRef.show(activity, rewardItem -> {
                int rewardAmount = rewardItem.getAmount();
                String rewardType = rewardItem.getType();

                Map<String, Object> data = new HashMap<>();
                data.put("unit_id", rewardedInterstitialAdRef.getAdUnitId());
                data.put("reward_amount", (double) rewardAmount);
                data.put("reward_type", rewardType);
                sendAsyncEvent("AdMob_RewardedInterstitial_OnReward", data);
            });

            isShowingAd = true;
        });
    }

    // #endregion

    // #region App Open Ad

    public void AdMob_AppOpenAd_Set_AdUnit(String adUnitId) {
        appOpenAdUnitId = adUnitId;
    }

    public double AdMob_AppOpenAd_Enable(double orientation) {

        final String callingMethod = "AdMob_AppOpenAd_Enable";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(appOpenAdUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

        triggerAppOpenAd = true;

        if (!appOpenAdIsValid(callingMethod)) {
            AdMob_AppOpenAd_Load();
        }

        return ADMOB_OK;
    }

    public void AdMob_AppOpenAd_Disable() {
        triggerAppOpenAd = false;
    }

    public double AdMob_AppOpenAd_IsEnabled() {
        return triggerAppOpenAd ? 1.0 : 0.0;
    }

    public double AdMob_AppOpenAd_IsLoaded() {
        return appOpenAdIsValid("AdMob_AppOpenAd_IsLoaded") ? 1.0 : 0.0;
    }

    public double AdMob_AppOpenAd_Load() {

        final String callingMethod = "AdMob_AppOpenAd_Load";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(appOpenAdUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        if (appOpenAdIsValid(callingMethod))
            return ADMOB_OK;

        loadAppOpenAd(appOpenAdUnitId, callingMethod);

        return ADMOB_OK;
    }

	public double AdMob_AppOpenAd_Show() {

		final String callingMethod = "AdMob_AppOpenAd_Show";
	
        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;
	
        if (!appOpenAdIsValid(callingMethod))
            return ADMOB_ERROR_NO_ADS_LOADED;

        showAppOpenAd(callingMethod);

        return ADMOB_OK;
	}

    private void loadAppOpenAd(final String adUnitId, final String callingMethod) {
        RunnerActivity.ViewHandler.post(() -> {
            Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            // Use application context
            Context appContext = activity.getApplicationContext();

            Configuration config = activity.getResources().getConfiguration();
            appOpenAdOrientation = config.orientation;
            AppOpenAd.load(appContext, appOpenAdUnitId, buildAdRequest(),
                    new AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull AppOpenAd loadedAd) {

                            appOpenAdLoadTime = (new Date()).getTime();
                            appOpenAd = loadedAd;

                            if (triggerOnPaidEvent) {

                                appOpenAd.setOnPaidEventListener(adValue -> {
                                    AdapterResponseInfo loadedAdapterResponseInfo = appOpenAd.getResponseInfo()
                                            .getLoadedAdapterResponseInfo();
                                    if (loadedAdapterResponseInfo == null) return;
                                    onPaidEventHandler(adValue, appOpenAd.getAdUnitId(), "AppOpen",
                                            loadedAdapterResponseInfo,
                                            appOpenAd.getResponseInfo().getMediationAdapterClassName());
                                });
                            }

							Map<String, Object> data = new HashMap<>();
                            data.put("unit_id", adUnitId);
                            sendAsyncEvent("AdMob_AppOpenAd_OnLoaded", data);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            appOpenAd = null;

                            Map<String, Object> data = new HashMap<>();
                            data.put("unit_id", adUnitId);
                            data.put("errorMessage", loadAdError.getMessage());
                            data.put("errorCode", (double) loadAdError.getCode());
                            sendAsyncEvent("AdMob_AppOpenAd_OnLoadFailed", data);
                        }
                    });
        });
    }

    private void showAppOpenAd(final String callingMethod) {
        RunnerActivity.ViewHandler.post(() -> {
			// Check if the App Open ad instance is still valid
			if (appOpenAd == null)
				return;
	
			// Get the Activity reference inside the Runnable
			Activity activity = getActivity(callingMethod);
			if (activity == null) return;
	
			// Set the full-screen content callback
			appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
				@Override
				public void onAdDismissedFullScreenContent() {

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
                	cleanAd(appOpenAd, ad -> cleanUpAd(ad));
                    appOpenAd = null;
					
                    sendAsyncEvent("AdMob_AppOpenAd_OnDismissed", null);

                    // If AppOpenAd is being automatically managed
                    if (triggerAppOpenAd) {
                        // Load the App Open Ad again
					    AdMob_AppOpenAd_Load();
                    }
				}
	
				@Override
				public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
					isShowingAd = false; // Reset the flag

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
                	cleanAd(appOpenAd, ad -> cleanUpAd(ad));
                    appOpenAd = null;

					Map<String, Object> data = new HashMap<>();
					data.put("errorMessage", adError.getMessage());
					data.put("errorCode", (double) adError.getCode());
					sendAsyncEvent("AdMob_AppOpenAd_OnShowFailed", data);
					
                    // If AppOpenAd is being automatically managed
                    if (triggerAppOpenAd) {
                        // Reload the App Open Ad after failure
					    AdMob_AppOpenAd_Load();
                    }
				}
	
				@Override
				public void onAdShowedFullScreenContent() {
					sendAsyncEvent("AdMob_AppOpenAd_OnFullyShown", null);
				}
			});
	
			// Update the isShowingAd flag and show the ad
			isShowingAd = true;
			appOpenAd.show(activity);
		});
    }

	private boolean appOpenAdIsValid(String callingMethod) {
		// Check if is loaded
        if (appOpenAd == null) {
			Log.w(LOG_TAG, callingMethod + " :: There is no app open ad loaded.");
			return false;
		}
	
		if (appOpenAd.getResponseInfo() == null) {
			Log.w(LOG_TAG, callingMethod + " :: Ad's ResponseInfo is null.");
			return false;
		}
	
        // Check if is expired
		long dateDifference = (new Date()).getTime() - appOpenAdLoadTime;
		boolean expired = dateDifference >= (3600000L * appOpenAdExpirationTime);
		if (expired) {
			Log.w(LOG_TAG, callingMethod + " :: The loaded app open ad expired.");
			return false;
		}

        // Check if is correct orientation
        int currentOrientation = Configuration.ORIENTATION_UNDEFINED;
        Activity activity = getActivity(callingMethod);
        if (activity != null) {
            Configuration config = activity.getResources().getConfiguration();
            currentOrientation = config.orientation;
        }

        if (currentOrientation != appOpenAdOrientation) {
            Log.w(LOG_TAG, callingMethod + " :: The loaded app open ad has incorrect orientation.");
			return false;
        }
	
		return true;
	}

    // #endregion

	// #region Targeting

	public double AdMob_Targeting_COPPA(double COPPA) {

		if (!validateNotInitialized("AdMob_Targeting_COPPA")) return ADMOB_ERROR_ILLEGAL_CALL;

		targetCOPPA = COPPA > 0.5;
		return ADMOB_OK;
	}

	public double AdMob_Targeting_UnderAge(double underAge) {

		if (!validateNotInitialized("AdMob_Targeting_UnderAge")) return ADMOB_ERROR_ILLEGAL_CALL;

		targetUnderAge = underAge >= 0.5;
		return ADMOB_OK;
	}

	public double AdMob_Targeting_MaxAdContentRating(double contentRating) {
		
		if (!validateNotInitialized("AdMob_Targeting_MaxAdContentRating")) return ADMOB_ERROR_ILLEGAL_CALL;

		switch ((int) contentRating) {
			case 0:
				maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_G;
				break;
			case 1:
				maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_PG;
				break;
			case 2:
				maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_T;
				break;
			case 3:
				maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_MA;
				break;
		}
		return 0;
	}

	//#endregion

	// #region Consent Management

	public void AdMob_Consent_RequestInfoUpdate(double mode) {

		final String callingMethod = "AdMob_Consent_RequestInfoUpdate";

		if (!validateViewHandler(callingMethod))
			return;

		RunnerActivity.ViewHandler.post(() -> {

			Activity activity = getActivity(callingMethod);
            if (activity == null) return;

			ConsentRequestParameters.Builder builder = new ConsentRequestParameters.Builder();
			builder.setTagForUnderAgeOfConsent(targetUnderAge);

			if (mode >= 0) {
				ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
						.setDebugGeography((int) mode)
						.addTestDeviceHashedId(getDeviceID(callingMethod))
						.build();

				builder.setConsentDebugSettings(debugSettings);
			}

			ConsentRequestParameters params = builder.build();

			consentInformation = UserMessagingPlatform.getConsentInformation(activity);
			consentInformation.requestConsentInfoUpdate(activity, params,
					() -> 
					{
						sendAsyncEvent("AdMob_Consent_OnRequestInfoUpdated", null);
					},
					formError -> {
						Map<String, Object> data = new HashMap<>();
						data.put("errorMessage", formError.getMessage());
						data.put("errorCode", (double) formError.getErrorCode());
						sendAsyncEvent("AdMob_Consent_OnRequestInfoUpdateFailed", data);
					});
		});
	}

	public double AdMob_Consent_GetStatus() {
		return consentInformation == null ? 0 : (double) consentInformation.getConsentStatus();
	}

	public double AdMob_Consent_GetType() {
		if (consentInformation == null)
			return 0; // AdMob_Consent_Type_UNKNOWN

		if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.OBTAINED) {

			Context context = RunnerJNILib.ms_context;
			if (!canShowAds(context))
				return 3.0; // AdMob_Consent_Type_DECLINED

			return canShowPersonalizedAds(context) ? 2.0 : 1.0;

		}

		return 0.0; // AdMob_Consent_Type_UNKNOWN
	}

	public double AdMob_Consent_IsFormAvailable() {
		return consentInformation == null ? 0.0 : (consentInformation.isConsentFormAvailable() ? 1.0 : 0.0);
	}

	public void AdMob_Consent_Load() {

		final String callingMethod = "AdMob_Consent_Load";

		Activity activity = getActivity(callingMethod);
        if (activity == null) return;

		if (!validateViewHandler(callingMethod))
			return;

		RunnerActivity.ViewHandler.post(() -> UserMessagingPlatform.loadConsentForm(activity,
				consentForm -> {
					consentFormInstance = consentForm;
					sendAsyncEvent("AdMob_Consent_OnLoaded", null);
				},
				formError -> {
					Map<String, Object> data = new HashMap<>();
					data.put("errorMessage", formError.getMessage());
					data.put("errorCode", (double) formError.getErrorCode());
					sendAsyncEvent("AdMob_Consent_OnLoadFailed", data);
				}));
	}

	public void AdMob_Consent_Show() {

		final String callingMethod = "AdMob_Consent_Show";

		if (!validateViewHandler(callingMethod))
			return;

		RunnerActivity.ViewHandler.post(() -> {
			Activity activity = getActivity(callingMethod);
            if (activity == null) return;
	
			final ConsentForm consentForm = consentFormInstance;
			if (consentForm != null) {
				consentForm.show(activity, formError -> {
					if (formError == null) {
						sendAsyncEvent("AdMob_Consent_OnShown", null);
					} else {
						Map<String, Object> data = new HashMap<>();
						data.put("errorMessage", formError.getMessage());
						data.put("errorCode", (double) formError.getErrorCode());
						sendAsyncEvent("AdMob_Consent_OnShowFailed", data);
					}
					// Nullify instance after use
					consentFormInstance = null;
				});
			} else {
				Log.i(LOG_TAG, "AdMob_Consent_Show :: There is no loaded consent form.");
			}
		});
	}

	public void AdMob_Consent_Reset() {
		if (consentInformation != null)
			consentInformation.reset();
	}

	public void AdMob_Consent_Set_RDP(double enabled) {
		isRdpEnabled = enabled > 0.5;
	}

    // https://stackoverflow.com/questions/69307205/mandatory-consent-for-admob-user-messaging-platform
	private boolean canShowAds(Context context) {
	
		SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() + "_preferences",
				Context.MODE_PRIVATE);
		String purposeConsent = prefs.getString("IABTCF_PurposeConsents", "");
		String vendorConsent = prefs.getString("IABTCF_VendorConsents", "");
		String vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "");
		String purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "");

		int googleId = 755;
		boolean hasGoogleVendorConsent = hasAttribute(vendorConsent, googleId);
		boolean hasGoogleVendorLI = hasAttribute(vendorLI, googleId);

		List<Integer> indexes = new ArrayList<>();
		indexes.add(1);

		List<Integer> indexesLI = new ArrayList<>();
		indexesLI.add(2);
		indexesLI.add(7);
		indexesLI.add(9);
		indexesLI.add(10);

		return hasConsentFor(indexes, purposeConsent, hasGoogleVendorConsent)
				&& hasConsentOrLegitimateInterestFor(indexesLI, purposeConsent, purposeLI, hasGoogleVendorConsent,
						hasGoogleVendorLI);

	}

	private boolean canShowPersonalizedAds(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() + "_preferences",
				Context.MODE_PRIVATE);
		String purposeConsent = prefs.getString("IABTCF_PurposeConsents", "");
		String vendorConsent = prefs.getString("IABTCF_VendorConsents", "");
		String vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "");
		String purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "");

		int googleId = 755;
		boolean hasGoogleVendorConsent = hasAttribute(vendorConsent, googleId);
		boolean hasGoogleVendorLI = hasAttribute(vendorLI, googleId);

		List<Integer> indexes = new ArrayList<>();
		indexes.add(1);
		indexes.add(3);
		indexes.add(4);

		List<Integer> indexesLI = new ArrayList<>();
		indexesLI.add(2);
		indexesLI.add(7);
		indexesLI.add(9);
		indexesLI.add(10);

		return hasConsentFor(indexes, purposeConsent, hasGoogleVendorConsent)
				&& hasConsentOrLegitimateInterestFor(indexesLI, purposeConsent, purposeLI, hasGoogleVendorConsent,
						hasGoogleVendorLI);

	}

	private boolean hasAttribute(String input, int index) {
		if (input == null)
			return false;
		return input.length() >= index && input.charAt(index - 1) == '1';
	}

	private boolean hasConsentFor(List<Integer> indexes, String purposeConsent, boolean hasVendorConsent) {
		for (Integer p : indexes) {
			if (!hasAttribute(purposeConsent, p)) {
				Log.e(LOG_TAG, "hasConsentFor: denied for purpose #" + p);
				return false;
			}
		}
		return hasVendorConsent;
	}

	private boolean hasConsentOrLegitimateInterestFor(List<Integer> indexes, String purposeConsent, String purposeLI,
			boolean hasVendorConsent, boolean hasVendorLI) {
		for (Integer p : indexes) {
			boolean purposeAndVendorLI = hasAttribute(purposeLI, p) && hasVendorLI;
			boolean purposeConsentAndVendorConsent = hasAttribute(purposeConsent, p) && hasVendorConsent;
			boolean isOk = purposeAndVendorLI || purposeConsentAndVendorConsent;
			if (!isOk) {
				Log.e(LOG_TAG, "hasConsentOrLegitimateInterestFor: denied for #" + p);
				return false;
			}
		}
		return true;
	}

	// #endregion

	// #region Settings

	public void AdMob_Settings_SetVolume(double value) {
		MobileAds.setAppVolume((float) value);
	}

	public void AdMob_Settings_SetMuted(double value) {
		MobileAds.setAppMuted(value >= 0.5);
	}

	// #endregion

	// #region Activity Lifecycle Methods

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Update activity reference
		activityRef = new WeakReference<>(RunnerActivity.CurrentActivity);
	}

	@Override
    public void onStart() {
		super.onStart();
        if (triggerAppOpenAd && !isShowingAd) {
            if (!appOpenAdIsValid("onStart")) {
                AdMob_AppOpenAd_Load();
                return;
            }

            AdMob_AppOpenAd_Show();
            return;
        }
        isShowingAd = false;
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Clean up Banner Ad
		if (bannerAdView != null) {
			deleteBannerAdView();
		}

		// Clear Interstitial Ads
		freeLoadedInstances(interstitialAdQueue, -1, this::cleanUpAd); // Free all instances
		interstitialAdQueue.clear();

		// Clear Rewarded Ads
		freeLoadedInstances(rewardedAdQueue, -1, this::cleanUpAd); // Free all instances
		rewardedAdQueue.clear();

		// Clear Rewarded Interstitial Ads
		freeLoadedInstances(rewardedInterstitialAdQueue, -1, this::cleanUpAd); // Free all instances
		rewardedInterstitialAdQueue.clear();

		// Nullify App Open Ad
		if (appOpenAd != null) {
			cleanAd(appOpenAd, this::cleanUpAd);
			appOpenAd = null;
		}

		// Nullify Consent Form
		consentFormInstance = null;

		// Nullify Consent Information
		consentInformation = null;

		// Clear WeakReference to Activity
		if (activityRef != null) {
			activityRef.clear();
		}
	}

	// #endregion

    // #region Helper Methods

	@FunctionalInterface
	public interface AdCleaner<T> {
		void clean(T ad);
	}

	private <T> void cleanAd(T ad, AdCleaner<T> cleaner) {
		if (ad != null) {
			RunnerActivity.ViewHandler.post(() -> {
				cleaner.clean(ad);
			});
		}
	}

	private void cleanUpAd(AdView ad) {
		ad.setAdListener(null);
		ad.setOnPaidEventListener(null);		
		// Additional BannerAd-specific cleanup if needed
	}

	private void cleanUpAd(InterstitialAd ad) {
		ad.setFullScreenContentCallback(null);
		ad.setOnPaidEventListener(null);
		// Additional InterstitialAd-specific cleanup if needed
	}
	
	private void cleanUpAd(RewardedAd ad) {
		ad.setFullScreenContentCallback(null);
		ad.setOnPaidEventListener(null);
		// Additional RewardedAd-specific cleanup if needed
	}
	
	private void cleanUpAd(RewardedInterstitialAd ad) {
		ad.setFullScreenContentCallback(null);
		ad.setOnPaidEventListener(null);
		// Additional RewardedInterstitialAd-specific cleanup if needed
	}
	
	private void cleanUpAd(AppOpenAd ad) {
		ad.setFullScreenContentCallback(null);
		ad.setOnPaidEventListener(null);
		// Additional AppOpenAd-specific cleanup if needed
	}

	private <T> void freeLoadedInstances(Queue<T> queue, final double count, AdCleaner<T> cleaner) {
		RunnerActivity.ViewHandler.post(() -> {
			synchronized (queue) {
				double localCount = count;
				if (count < 0) {
					localCount = queue.size();
				}
		
				while (localCount > 0 && !queue.isEmpty()) {
					T ad = queue.poll();
					if (ad != null) {
						cleaner.clean(ad);
					}
					localCount--;
				}
			}
		});
	}

    private <T> void trimLoadedAdsQueue(Queue<T> queue, int maxSize, AdCleaner<T> cleaner) {
        int size = queue.size();
        if (size <= maxSize) return;

        freeLoadedInstances(queue, size - maxSize, cleaner);
    }

    private void sendAsyncEvent(String eventType, Map<String, Object> data) {
        Activity activity = RunnerActivity.CurrentActivity;
        if (activity == null)
            return;

        activity.runOnUiThread(() -> {
            String normalizedEventType = toSnakeCase(eventType);
            JSONObject payload = eventPayload(normalizedEventType, data);
            GMFunction callback = null;
            boolean clearShowCallback = false;

            switch (normalizedEventType) {
                case "admob_on_initialized":
                    callback = initializeCallback;
                    initializeCallback = null;
                    break;

                case "admob_banner_on_loaded":
                case "admob_banner_on_load_failed":
                case "admob_banner_on_opened":
                case "admob_banner_on_clicked":
                case "admob_banner_on_closed":
                    callback = bannerCallback;
                    break;

                case "admob_interstitial_on_loaded":
                case "admob_interstitial_on_load_failed":
                    callback = interstitialLoadCallbacks.poll();
                    break;

                case "admob_interstitial_on_fully_shown":
                    callback = interstitialShowCallback;
                    break;

                case "admob_interstitial_on_dismissed":
                case "admob_interstitial_on_show_failed":
                    callback = interstitialShowCallback;
                    clearShowCallback = true;
                    break;

                case "admob_rewarded_video_on_loaded":
                case "admob_rewarded_video_on_load_failed":
                    callback = rewardedVideoLoadCallbacks.poll();
                    break;

                case "admob_rewarded_video_on_fully_shown":
                case "admob_rewarded_video_on_reward":
                    callback = rewardedVideoShowCallback;
                    break;

                case "admob_rewarded_video_on_dismissed":
                case "admob_rewarded_video_on_show_failed":
                    callback = rewardedVideoShowCallback;
                    clearShowCallback = true;
                    break;

                case "admob_rewarded_interstitial_on_loaded":
                case "admob_rewarded_interstitial_on_load_failed":
                    callback = rewardedInterstitialLoadCallbacks.poll();
                    break;

                case "admob_rewarded_interstitial_on_fully_shown":
                case "admob_rewarded_interstitial_on_reward":
                    callback = rewardedInterstitialShowCallback;
                    break;

                case "admob_rewarded_interstitial_on_dismissed":
                case "admob_rewarded_interstitial_on_show_failed":
                    callback = rewardedInterstitialShowCallback;
                    clearShowCallback = true;
                    break;

                case "admob_app_open_ad_on_loaded":
                case "admob_app_open_ad_on_load_failed":
                    callback = appOpenLoadCallback != null ? appOpenLoadCallback : appOpenEnableCallback;
                    appOpenLoadCallback = null;
                    break;

                case "admob_app_open_ad_on_fully_shown":
                    callback = appOpenShowCallback != null ? appOpenShowCallback : appOpenEnableCallback;
                    break;

                case "admob_app_open_ad_on_dismissed":
                case "admob_app_open_ad_on_show_failed":
                    callback = appOpenShowCallback != null ? appOpenShowCallback : appOpenEnableCallback;
                    appOpenShowCallback = null;
                    break;

                case "admob_consent_on_request_info_updated":
                case "admob_consent_on_request_info_update_failed":
                    callback = consentRequestInfoUpdateCallback;
                    consentRequestInfoUpdateCallback = null;
                    break;

                case "admob_consent_on_loaded":
                case "admob_consent_on_load_failed":
                    callback = consentLoadCallback;
                    consentLoadCallback = null;
                    break;

                case "admob_consent_on_shown":
                case "admob_consent_on_show_failed":
                    callback = consentShowCallback;
                    consentShowCallback = null;
                    break;

                case "admob_on_paid_event":
                    callback = paidEventCallback;
                    break;
            }

            invokeCallback(callback, payload);

            if (clearShowCallback) {
                if (normalizedEventType.startsWith("admob_interstitial_"))
                    interstitialShowCallback = null;
                else if (normalizedEventType.startsWith("admob_rewarded_video_"))
                    rewardedVideoShowCallback = null;
                else if (normalizedEventType.startsWith("admob_rewarded_interstitial_"))
                    rewardedInterstitialShowCallback = null;
            }
        });
    }

    private JSONObject eventPayload(String eventType, Map<String, Object> data) {
        JSONObject payload = new JSONObject();

        try {
            int callbackEventType = callbackEventTypeForName(eventType);
            boolean failed =
                eventType.endsWith("failed")
                || eventType.endsWith("load_failed")
                || eventType.endsWith("show_failed")
                || eventType.endsWith("request_info_update_failed");

            payload.put("success", !failed);
            payload.put("event_type", callbackEventType);
            payload.put("code", failed ? -100.0 : ADMOB_OK);
            payload.put("error_message", "");

            if (data != null) {
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    payload.put(toSnakeCase(entry.getKey()), normalizeJsonValue(entry.getValue()));
                }
            }

            if (payload.has("error_code"))
                payload.put("code", payload.optDouble("error_code", -100.0));

            String errorMessage = payload.optString("error_message", "");
            if (errorMessage != null && !errorMessage.isEmpty())
                payload.put("success", false);
        }
        catch (JSONException ignored) {
        }

        return payload;
    }

    private int callbackEventTypeForName(String eventType) {
        switch (eventType) {
            case "admob_on_initialized":
                return ADMOB_INIT_EVENT_INITIALIZED;

            case "admob_banner_on_loaded":
                return ADMOB_BANNER_EVENT_LOADED;
            case "admob_banner_on_load_failed":
                return ADMOB_BANNER_EVENT_LOAD_FAILED;
            case "admob_banner_on_opened":
                return ADMOB_BANNER_EVENT_OPENED;
            case "admob_banner_on_clicked":
                return ADMOB_BANNER_EVENT_CLICKED;
            case "admob_banner_on_closed":
                return ADMOB_BANNER_EVENT_CLOSED;

            case "admob_interstitial_on_loaded":
            case "admob_rewarded_video_on_loaded":
            case "admob_rewarded_interstitial_on_loaded":
            case "admob_app_open_ad_on_loaded":
                return ADMOB_FULLSCREEN_EVENT_LOADED;

            case "admob_interstitial_on_load_failed":
            case "admob_rewarded_video_on_load_failed":
            case "admob_rewarded_interstitial_on_load_failed":
            case "admob_app_open_ad_on_load_failed":
                return ADMOB_FULLSCREEN_EVENT_LOAD_FAILED;

            case "admob_interstitial_on_fully_shown":
            case "admob_rewarded_video_on_fully_shown":
            case "admob_rewarded_interstitial_on_fully_shown":
            case "admob_app_open_ad_on_fully_shown":
                return ADMOB_FULLSCREEN_EVENT_FULLY_SHOWN;

            case "admob_interstitial_on_show_failed":
            case "admob_rewarded_video_on_show_failed":
            case "admob_rewarded_interstitial_on_show_failed":
            case "admob_app_open_ad_on_show_failed":
                return ADMOB_FULLSCREEN_EVENT_SHOW_FAILED;

            case "admob_interstitial_on_dismissed":
            case "admob_rewarded_video_on_dismissed":
            case "admob_rewarded_interstitial_on_dismissed":
            case "admob_app_open_ad_on_dismissed":
                return ADMOB_FULLSCREEN_EVENT_DISMISSED;

            case "admob_rewarded_video_on_reward":
            case "admob_rewarded_interstitial_on_reward":
                return ADMOB_FULLSCREEN_EVENT_REWARD;

            case "admob_consent_on_request_info_updated":
                return ADMOB_CONSENT_EVENT_REQUEST_INFO_UPDATED;
            case "admob_consent_on_request_info_update_failed":
                return ADMOB_CONSENT_EVENT_REQUEST_INFO_UPDATE_FAILED;
            case "admob_consent_on_loaded":
                return ADMOB_CONSENT_EVENT_LOADED;
            case "admob_consent_on_load_failed":
                return ADMOB_CONSENT_EVENT_LOAD_FAILED;
            case "admob_consent_on_shown":
                return ADMOB_CONSENT_EVENT_SHOWN;
            case "admob_consent_on_show_failed":
                return ADMOB_CONSENT_EVENT_SHOW_FAILED;

            case "admob_on_paid_event":
                return ADMOB_PAID_EVENT_PAID;

            default:
                return -1;
        }
    }

    private void callbackResult(GMFunction callback, int eventType, double code) {
        callbackResult(callback, eventType, code == ADMOB_OK, code);
    }

    private void callbackResult(GMFunction callback, int eventType, boolean success, double code) {
        JSONObject payload = new JSONObject();

        try {
            payload.put("success", success);
            payload.put("event_type", eventType);
            payload.put("code", code);
            payload.put("error_message", success ? "" : errorMessageForCode((int)code));
        }
        catch (JSONException ignored) {
        }

        invokeCallback(callback, payload);
    }

    private void invokeCallback(GMFunction callback, JSONObject payload) {
        if (callback != null)
            callback.call(payload.toString());
    }

    private static Object normalizeJsonValue(Object value) {
        if (value == null)
            return JSONObject.NULL;
        if (value instanceof Map)
            return new JSONObject((Map)value);
        if (value instanceof List)
            return new JSONArray((List)value);
        if (value instanceof Long) {
            long longValue = (Long)value;
            if (Math.abs(longValue) > MAX_DOUBLE_SAFE)
                return String.format("@i64@%016x$i64$", longValue);
        }
        return value;
    }

    private static String toSnakeCase(String value) {
        if (value == null || value.isEmpty())
            return "";

        String normalized = value
            .replace("AdMob_", "admob_")
            .replace("AdMob", "admob");

        normalized = normalized.replaceAll("([a-z0-9])([A-Z])", "$1_$2");
        return normalized.replace("__", "_").toLowerCase(Locale.US);
    }

    private static String errorMessageForCode(int code) {
        switch (code) {
            case ADMOB_OK:
                return "";
            case ADMOB_ERROR_NOT_INITIALIZED:
                return "AdMob SDK is not initialized.";
            case ADMOB_ERROR_INVALID_AD_ID:
                return "The AdMob ad unit ID is invalid or empty.";
            case ADMOB_ERROR_AD_LIMIT_REACHED:
                return "The loaded ad instance limit was reached.";
            case ADMOB_ERROR_NO_ADS_LOADED:
                return "There are no ads loaded.";
            case ADMOB_ERROR_NO_ACTIVE_BANNER_AD:
                return "There is no active banner ad.";
            case ADMOB_ERROR_ILLEGAL_CALL:
                return "This call is not valid in the current AdMob state.";
            case ADMOB_ERROR_NULL_VIEW_HANDLER:
                return "RunnerActivity.ViewHandler is null.";
            default:
                return "Unknown AdMob error.";
        }
    }


    private void onPaidEventHandler(AdValue adValue, String adUnitId, String adType,
                                    AdapterResponseInfo loadedAdapterResponseInfo, String mediationAdapterClassName) {

        Map<String, Object> data = new HashMap<>();
        data.put("mediation_adapter_class_name", mediationAdapterClassName);
        data.put("unit_id", adUnitId);
        data.put("ad_type", adType);
        data.put("micros", adValue.getValueMicros());
        data.put("currency_code", adValue.getCurrencyCode());
        data.put("precision", (double) adValue.getPrecisionType());

        if (loadedAdapterResponseInfo != null) {
            data.put("ad_source_name", loadedAdapterResponseInfo.getAdSourceName());
            data.put("ad_source_id", loadedAdapterResponseInfo.getAdSourceId());
            data.put("ad_source_instance_name", loadedAdapterResponseInfo.getAdSourceInstanceName());
            data.put("ad_source_instance_id", loadedAdapterResponseInfo.getAdSourceInstanceId());
        } else {
            Log.w(LOG_TAG, "LoadedAdapterResponseInfo is null.");
        }

        sendAsyncEvent("AdMob_OnPaidEvent", data);
    }

	private AdRequest buildAdRequest() {
		AdRequest.Builder builder = new AdRequest.Builder();
	
		// As per Google's request, set the request agent
		builder.setRequestAgent("gmext-admob-" + RunnerJNILib.extGetVersion("AdMob"));
	
		// Handle CCPA compliance by adding the "rdp" parameter if the user has opted out
		if (isRdpEnabled) {
			Bundle extras = new Bundle();
			extras.putInt("rdp", 1);
			builder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
		}

		// No longer add the deprecated "npa" parameter
		// The UMP SDK handles user consent and ad personalization
	
		return builder.build();
	}

    private String getDeviceID(final String callingMethod) {

        Activity activity = getActivity(callingMethod);
        if (activity == null) return "";

        String androidId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

		String deviceIdHash = computeMD5(androidId);
		if (deviceIdHash == null) {
			Log.w(LOG_TAG, "Failed to generate MD5 hash of ANDROID_ID.");
			return "";
		}
		return deviceIdHash.toUpperCase();
    }

    private String computeMD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    // #endregion

    // #region Validations

	private Activity getActivity(String callingMethod) {
		Activity activity = activityRef.get();
		if (activity == null) {
			Log.w(LOG_TAG, callingMethod + " :: Activity reference is null.");
		}
		return activity;
	}

    private boolean validateNotInitialized(String callingMethod) {
        if (isInitialized) {
            Log.w(LOG_TAG, callingMethod + " :: Method cannot be called after initialization.");
        }
        return !isInitialized;
    }

    private boolean validateInitialized(String callingMethod) {
        if (!isInitialized) {
            Log.w(LOG_TAG, callingMethod + " :: Extension was not initialized.");
        }
        return isInitialized;
    }

	private boolean validateViewHandler(String callingMethod) {
		if (RunnerActivity.ViewHandler == null) {
			Log.w(LOG_TAG, callingMethod + " :: ViewHandler is null, cannot post to main thread.");
			return false;
		}
		return true;
	}

    private boolean validateActiveBannerAd(String callingMethod) {
        if (bannerAdView == null) {
            Log.w(LOG_TAG, callingMethod + " :: There is no active banner ad.");
            return false;
        }
        return true;
    }

    private boolean validateAdId(String adUnitId, String callingMethod) {
        if (adUnitId.isEmpty()) {
            Log.w(LOG_TAG, callingMethod + " :: Ad unit ID is empty.");
            return false;
        }
        return true;
    }

    private <T> boolean validateLoadedAdsLimit(Queue<T> queue, int maxSize, String callingMethod) {
        if (queue.size() >= maxSize) {
            Log.w(LOG_TAG, callingMethod + " :: Maximum number of loaded ads reached.");
            return false;
        }
        return true;
    }

    private <T> boolean validateAdLoaded(Queue<T> queue, String callingMethod) {
        if (queue.isEmpty()) {
            Log.w(LOG_TAG, callingMethod + " :: There is no loaded ad in queue.");
            return false;
        }
        return true;
    }

    // #endregion
}
