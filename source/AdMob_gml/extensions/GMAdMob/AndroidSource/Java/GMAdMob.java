package ${YYAndroidPackageName};

import ${YYAndroidPackageName}.R;
import ${YYAndroidPackageName}.GMExtWire;
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

    private static final String LOG_TAG = "AdMob";

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


    // #region Setup
    public double admob_initialize(final GMFunction callback) {
        initializeCallback = callback;

		final String callingMethod = "admob_initialize";

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

                    // Initialize ad units from extension options before firing callback.
                    initializeAdUnits();
                    isInitialized = true;

                    sendAsyncEvent("AdMob_OnInitialized", null);
                });
            } catch (Exception e) {
                Log.i(LOG_TAG, "GoogleMobileAds Init Error: " + e.toString());
                Log.i(LOG_TAG, e.toString());
            }
        }).start();

        return ADMOB_OK;
    }

    private void initializeAdUnits() {
        bannerAdUnitId = getAdMobOptionString("Android_BANNER");
        interstitialAdUnitId = getAdMobOptionString("Android_INTERSTITIAL");
        rewardedUnitId = getAdMobOptionString("Android_REWARDED");
        rewardedInterstitialAdUnitId = getAdMobOptionString("Android_REWARDED_INTERSTITIAL");
        appOpenAdUnitId = getAdMobOptionString("Android_OPENAPPAD");
    }

    private String getAdMobOptionString(String optionName) {
        String value = RunnerJNILib.extOptGetString("GMAdMob", optionName);

        if (value == null || value.isEmpty())
            value = RunnerJNILib.extOptGetString("AdMob", optionName);

        return value != null ? value : "";
    }

    private String normalizeAdUnitId(String adUnitId) {
        return adUnitId != null ? adUnitId : "";
    }
    public double admob_set_test_device_id() {
        if (!validateNotInitialized("admob_set_test_device_id")) return ADMOB_ERROR_ILLEGAL_CALL;

        isTestDevice = true;
        return ADMOB_OK;
    }
    public void admob_events_on_paid_event(boolean enabled, final GMFunction callback) {
        triggerOnPaidEvent = enabled;
        paidEventCallback = enabled ? callback : null;
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
    public void admob_banner_set_ad_unit(String adUnitId) {
        bannerAdUnitId = normalizeAdUnitId(adUnitId);
    }
    public double admob_banner_create(final AdMobBannerSize size, final boolean bottom, final GMFunction callback) {
        bannerCallback = callback;

        final String callingMethod = "admob_banner_create";

		if (!validateInitialized(callingMethod))
			return ADMOB_ERROR_NOT_INITIALIZED;

		if (!validateAdId(bannerAdUnitId, callingMethod))
			return ADMOB_ERROR_INVALID_AD_ID;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

		currentBannerAlignment = RelativeLayout.CENTER_HORIZONTAL;

		// Call the helper method with default horizontal alignment ("center")
		createBannerAdView(size.value(), bottom, currentBannerAlignment, callingMethod);

		return ADMOB_OK;
    }
    public double admob_banner_create_ext(final AdMobBannerSize size, final boolean bottom, final AdMobBannerAlignment horizontalAlignment, final GMFunction callback) {
        bannerCallback = callback;

		final String callingMethod = "admob_banner_create_ext";
	
		if (!validateInitialized(callingMethod))
			return ADMOB_ERROR_NOT_INITIALIZED;
	
		if (!validateAdId(bannerAdUnitId, callingMethod))
			return ADMOB_ERROR_INVALID_AD_ID;
	
		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;
	
		
		// Validate horizontalAlignment parameter
		switch ((int) horizontalAlignment.value()) {
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
		createBannerAdView(size.value(), bottom, currentBannerAlignment, callingMethod);
	
		return ADMOB_OK;
	}
    public double admob_banner_get_width() {
        if (bannerAdView == null) return 0;
        return bannerSize.getWidthInPixels(RunnerJNILib.ms_context);
    }
    public double admob_banner_get_height() {
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
    public double admob_banner_move(final boolean bottom) {

        final String callingMethod = "admob_banner_move";
    
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
            params.addRule(bottom ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
    
            bannerAdView.setLayoutParams(params);
        });
    
        return ADMOB_OK;
    }
    public double admob_banner_show() {

        final String callingMethod = "admob_banner_show";

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
    public double admob_banner_hide() {

        final String callingMethod = "admob_banner_hide";

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
    public double admob_banner_remove() {

        final String callingMethod = "admob_banner_remove";

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
    public void admob_interstitial_set_ad_unit(String adUnitId) {
        interstitialAdUnitId = normalizeAdUnitId(adUnitId);
    }
    public void admob_interstitial_free_loaded_instances(double count) {
		freeLoadedInstances(interstitialAdQueue, count, this::cleanUpAd);
    }
    public void admob_interstitial_max_instances(double value) {
        interstitialAdQueueCapacity = (int) value;
		trimLoadedAdsQueue(interstitialAdQueue, interstitialAdQueueCapacity, this::cleanUpAd);
    }
    public double admob_interstitial_load(final GMFunction callback) {

        final String callingMethod = "admob_interstitial_load";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(interstitialAdUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

        if (!validateLoadedAdsLimit(interstitialAdQueue, interstitialAdQueueCapacity, callingMethod))
            return ADMOB_ERROR_AD_LIMIT_REACHED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        interstitialLoadCallbacks.offer(callback);
        loadInterstitialAd(interstitialAdUnitId, interstitialAdQueue, interstitialAdQueueCapacity, callingMethod);

        return ADMOB_OK;
    }
    public double admob_interstitial_show(final GMFunction callback) {

        final String callingMethod = "admob_interstitial_show";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdLoaded(interstitialAdQueue, callingMethod))
            return ADMOB_ERROR_NO_ADS_LOADED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        interstitialShowCallback = callback;
        showInterstitialAd(interstitialAdQueue, callingMethod);

        return ADMOB_OK;
    }
    public boolean admob_interstitial_is_loaded() {
        return admob_interstitial_instances_count() > 0;
    }
    public double admob_interstitial_instances_count() {
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
    public void admob_server_side_verification_set(final String userId, final String customData) {
        serverSideVerificationUserId = userId;
        serverSideVerificationCustomData = customData;
    }
    public void admob_server_side_verification_clear() {
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
    public void admob_rewarded_video_set_ad_unit(String adUnitId) {
        rewardedUnitId = normalizeAdUnitId(adUnitId);
    }
    public void admob_rewarded_video_free_loaded_instances(double count) {
		freeLoadedInstances(rewardedAdQueue, count, this::cleanUpAd);
    }
    public void admob_rewarded_video_max_instances(double value) {
        rewardedAdQueueCapacity = (int) value;
        trimLoadedAdsQueue(rewardedAdQueue, rewardedAdQueueCapacity, this::cleanUpAd);
    }
    public double admob_rewarded_video_load(final GMFunction callback) {

        final String callingMethod = "admob_rewarded_video_load";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(rewardedUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

        if (!validateLoadedAdsLimit(rewardedAdQueue, rewardedAdQueueCapacity, callingMethod))
            return ADMOB_ERROR_AD_LIMIT_REACHED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        rewardedVideoLoadCallbacks.offer(callback);
        loadRewardedAd(rewardedUnitId, rewardedAdQueue, rewardedAdQueueCapacity, callingMethod);

        return ADMOB_OK;
    }
    public double admob_rewarded_video_show(final GMFunction callback) {

        final String callingMethod = "admob_rewarded_video_show";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdLoaded(rewardedAdQueue, callingMethod))
            return ADMOB_ERROR_NO_ADS_LOADED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        rewardedVideoShowCallback = callback;
        showRewardedAd(rewardedAdQueue, callingMethod);

        return ADMOB_OK;
    }
    public boolean admob_rewarded_video_is_loaded() {
        return admob_rewarded_video_instances_count() > 0;
    }
    public double admob_rewarded_video_instances_count() {
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
    public void admob_rewarded_interstitial_set_ad_unit(String adUnitId) {
        rewardedInterstitialAdUnitId = normalizeAdUnitId(adUnitId);
    }
    public void admob_rewarded_interstitial_free_loaded_instances(double count) {
		freeLoadedInstances(rewardedInterstitialAdQueue, count, this::cleanUpAd);
    }
    public void admob_rewarded_interstitial_max_instances(double value) {
        rewardedAdInterstitialQueueCapacity = (int) value;
        trimLoadedAdsQueue(rewardedInterstitialAdQueue, rewardedAdInterstitialQueueCapacity, this::cleanUpAd);
    }
    public double admob_rewarded_interstitial_load(final GMFunction callback) {

        final String callingMethod = "admob_rewarded_interstitial_load";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(rewardedInterstitialAdUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

        if (!validateLoadedAdsLimit(rewardedInterstitialAdQueue, rewardedAdInterstitialQueueCapacity, callingMethod))
            return ADMOB_ERROR_AD_LIMIT_REACHED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        rewardedInterstitialLoadCallbacks.offer(callback);
        loadRewardedInterstitialAd(rewardedInterstitialAdUnitId, rewardedInterstitialAdQueue, rewardedAdInterstitialQueueCapacity, callingMethod);

        return ADMOB_OK;
    }
    public double admob_rewarded_interstitial_show(final GMFunction callback) {

        final String callingMethod = "admob_rewarded_interstitial_show";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdLoaded(rewardedInterstitialAdQueue, callingMethod))
            return ADMOB_ERROR_NO_ADS_LOADED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        rewardedInterstitialShowCallback = callback;
        showRewardedInterstitialAd(rewardedInterstitialAdQueue, callingMethod);

        return ADMOB_OK;
    }
    public boolean admob_rewarded_interstitial_is_loaded() {
        return admob_rewarded_interstitial_instances_count() > 0;
    }
    public double admob_rewarded_interstitial_instances_count() {
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
    public void admob_app_open_ad_set_ad_unit(String adUnitId) {
        appOpenAdUnitId = normalizeAdUnitId(adUnitId);
    }
    public double admob_app_open_ad_enable(double orientation, final GMFunction callback) {

        final String callingMethod = "admob_app_open_ad_enable";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(appOpenAdUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

        triggerAppOpenAd = true;

        if (!appOpenAdIsValid(callingMethod)) {
            admob_app_open_ad_load(appOpenEnableCallback);
        }

        return ADMOB_OK;
    }
    public void admob_app_open_ad_disable() {
        triggerAppOpenAd = false;
    }
    public boolean admob_app_open_ad_is_enabled() {
        return triggerAppOpenAd;
    }
    public boolean admob_app_open_ad_is_loaded() {
        return appOpenAdIsValid("admob_app_open_ad_is_loaded");
    }
    public double admob_app_open_ad_load(final GMFunction callback) {

        final String callingMethod = "admob_app_open_ad_load";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(appOpenAdUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        if (appOpenAdIsValid(callingMethod))
            return ADMOB_OK;

        appOpenLoadCallback = callback;
        loadAppOpenAd(appOpenAdUnitId, callingMethod);

        return ADMOB_OK;
    }
    public double admob_app_open_ad_show(final GMFunction callback) {

		final String callingMethod = "admob_app_open_ad_show";
	
        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;
	
        if (!appOpenAdIsValid(callingMethod))
            return ADMOB_ERROR_NO_ADS_LOADED;

        appOpenShowCallback = callback;
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
					    admob_app_open_ad_load(appOpenEnableCallback);
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
					    admob_app_open_ad_load(appOpenEnableCallback);
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
    public double admob_targeting_coppa(boolean COPPA) {

		if (!validateNotInitialized("admob_targeting_coppa")) return ADMOB_ERROR_ILLEGAL_CALL;

		targetCOPPA = COPPA;
		return ADMOB_OK;
	}
    public double admob_targeting_under_age(boolean underAge) {

		if (!validateNotInitialized("admob_targeting_under_age")) return ADMOB_ERROR_ILLEGAL_CALL;

		targetUnderAge = underAge;
		return ADMOB_OK;
	}
    public double admob_targeting_max_ad_content_rating(AdMobMaxAdContentRating contentRating) {
		
		if (!validateNotInitialized("admob_targeting_max_ad_content_rating")) return ADMOB_ERROR_ILLEGAL_CALL;

		switch ((int) contentRating.value()) {
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
    public double admob_consent_request_info_update(AdMobConsentDebugGeography mode, final GMFunction callback) {
        consentRequestInfoUpdateCallback = callback;

		final String callingMethod = "admob_consent_request_info_update";

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

		RunnerActivity.ViewHandler.post(() -> {

			Activity activity = getActivity(callingMethod);
            if (activity == null) return;

			ConsentRequestParameters.Builder builder = new ConsentRequestParameters.Builder();
			builder.setTagForUnderAgeOfConsent(targetUnderAge);

			if (mode.value() >= 0) {
				ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
						.setDebugGeography((int) mode.value())
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
        return ADMOB_OK;
	}
    public double admob_consent_get_status() {
		return consentInformation == null ? 0 : (double) consentInformation.getConsentStatus();
	}
    public double admob_consent_get_type() {
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
    public boolean admob_consent_is_form_available() {
        return consentInformation != null
            && consentInformation.isConsentFormAvailable();
    }
    public double admob_consent_load(final GMFunction callback) {
        consentLoadCallback = callback;

		final String callingMethod = "admob_consent_load";

		Activity activity = getActivity(callingMethod);
        if (activity == null) return ADMOB_ERROR_ILLEGAL_CALL;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

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
        return ADMOB_OK;
	}
    public double admob_consent_show(final GMFunction callback) {
        consentShowCallback = callback;

		final String callingMethod = "admob_consent_show";

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

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
				Log.i(LOG_TAG, "admob_consent_show :: There is no loaded consent form.");
			}
		});
        return ADMOB_OK;
	}
    public void admob_consent_reset() {
		if (consentInformation != null)
			consentInformation.reset();
	}
    public void admob_consent_set_rdp(boolean enabled) {
		isRdpEnabled = enabled;
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
    public void admob_settings_set_volume(double value) {
		MobileAds.setAppVolume((float) value);
	}
    public void admob_settings_set_muted(boolean value) {
		MobileAds.setAppMuted(value);
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
                admob_app_open_ad_load(appOpenEnableCallback);
                return;
            }

            admob_app_open_ad_show(appOpenEnableCallback);
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
            GMExtWire.StructStream payload = eventPayload(normalizedEventType, data);
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

    private static GMExtWire.StructStream streamStruct() {
        return new GMExtWire.StructStream(4096);
    }

    private static GMExtWire.ArrayStream streamArray() {
        return new GMExtWire.ArrayStream(4096);
    }

    private GMExtWire.StructStream eventPayload(String eventType, Map<String, Object> data) {
        int callbackEventType = callbackEventTypeForName(eventType);

        boolean failed =
            eventType.endsWith("failed")
            || eventType.endsWith("load_failed")
            || eventType.endsWith("show_failed")
            || eventType.endsWith("request_info_update_failed");

        double code = failed ? -100.0 : ADMOB_OK;
        String errorMessage = "";

        if (data != null) {
            Object errorCodeValue = data.get("errorCode");
            if (errorCodeValue == null)
                errorCodeValue = data.get("error_code");

            if (errorCodeValue instanceof Number)
                code = ((Number)errorCodeValue).doubleValue();

            Object errorMessageValue = data.get("errorMessage");
            if (errorMessageValue == null)
                errorMessageValue = data.get("error_message");

            if (errorMessageValue != null)
                errorMessage = safeString(errorMessageValue.toString());

            if (!errorMessage.isEmpty())
                failed = true;
        }

        GMExtWire.StructStream payload = streamStruct()
            .kv("success", !failed)
            .kv("event_type", callbackEventType)
            .kv("code", code)
            .kv("error_message", errorMessage);

        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = toSnakeCase(entry.getKey());

                if ("error_message".equals(key))
                    continue;

                addValue(payload, key, entry.getValue());
            }
        }

        return payload;
    }

    private static void addValue(
        GMExtWire.StructStream stream,
        String key,
        Object value) {
        if (value == null) {
            stream.kv(key, "");
            return;
        }

        if (value instanceof Boolean) {
            stream.kv(key, (Boolean)value);
            return;
        }

        if (value instanceof Integer) {
            stream.kv(key, (Integer)value);
            return;
        }

        if (value instanceof Long) {
            stream.kv(key, (Long)value);
            return;
        }

        if (value instanceof Float) {
            stream.kv(key, ((Float)value).doubleValue());
            return;
        }

        if (value instanceof Double) {
            stream.kv(key, (Double)value);
            return;
        }

        if (value instanceof Number) {
            stream.kv(key, ((Number)value).doubleValue());
            return;
        }

        if (value instanceof Map) {
            GMExtWire.StructStream nested = streamStruct();

            for (Object rawEntryObject : ((Map)value).entrySet()) {
                Map.Entry rawEntry = (Map.Entry)rawEntryObject;
                if (rawEntry.getKey() != null)
                    addValue(
                        nested,
                        toSnakeCase(rawEntry.getKey().toString()),
                        rawEntry.getValue()
                    );
            }

            stream.kv(key, nested);
            return;
        }

        if (value instanceof List) {
            GMExtWire.ArrayStream array = streamArray();

            for (Object item : (List)value)
                addArrayValue(array, item);

            stream.kv(key, array);
            return;
        }

        stream.kv(key, value.toString());
    }

    private static void addArrayValue(
        GMExtWire.ArrayStream array,
        Object value) {
        if (value == null) {
            array.add("");
            return;
        }

        if (value instanceof Boolean) {
            array.add((Boolean)value);
            return;
        }

        if (value instanceof Integer) {
            array.add((Integer)value);
            return;
        }

        if (value instanceof Long) {
            array.add((Long)value);
            return;
        }

        if (value instanceof Float) {
            array.add(((Float)value).doubleValue());
            return;
        }

        if (value instanceof Double) {
            array.add((Double)value);
            return;
        }

        if (value instanceof Number) {
            array.add(((Number)value).doubleValue());
            return;
        }

        if (value instanceof Map) {
            GMExtWire.StructStream nested = streamStruct();

            for (Object rawEntryObject : ((Map)value).entrySet()) {
                Map.Entry rawEntry = (Map.Entry)rawEntryObject;
                if (rawEntry.getKey() != null)
                    addValue(
                        nested,
                        toSnakeCase(rawEntry.getKey().toString()),
                        rawEntry.getValue()
                    );
            }

            array.add(nested);
            return;
        }

        array.add(value.toString());
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
        GMExtWire.StructStream payload = streamStruct()
            .kv("success", success)
            .kv("event_type", eventType)
            .kv("code", code)
            .kv(
                "error_message",
                success ? "" : errorMessageForCode((int)code)
            );

        invokeCallback(callback, payload);
    }

    private void invokeCallback(GMFunction callback, GMExtWire.StructStream payload) {
        if (callback != null)
            callback.call(payload);
    }

    private static String safeString(String value) {
        return value != null ? value : "";
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
        if (adUnitId == null || adUnitId.trim().isEmpty()) {
            Log.w(LOG_TAG, callingMethod + " :: Ad unit ID is null or empty.");
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
