package ${YYAndroidPackageName};

import ${YYAndroidPackageName}.R;
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

public class GoogleMobileAdsGM extends RunnerSocial {

    // Constants
    private static final int EVENT_OTHER_SOCIAL = 70;

    private static final int ADMOB_ERROR_NOT_INITIALIZED = -1;
    private static final int ADMOB_ERROR_INVALID_AD_ID = -2;
    private static final int ADMOB_ERROR_AD_LIMIT_REACHED = -3;
    private static final int ADMOB_ERROR_NO_ADS_LOADED = -4;
    private static final int ADMOB_ERROR_NO_ACTIVE_BANNER_AD = -5;
    private static final int ADMOB_ERROR_ILLEGAL_CALL = -6;
    private static final int ADMOB_ERROR_NULL_VIEW_HANDLER = -7;

	public static final int ADMOB_BANNER_ALIGNMENT_LEFT = 0;
	public static final int ADMOB_BANNER_ALIGNMENT_CENTER = 1;
	public static final int ADMOB_BANNER_ALIGNMENT_RIGHT = 2;

    private static final String LOG_TAG = "AdMob";

    // WeakReference to Activity to prevent memory leaks
    private WeakReference<Activity> activityRef;

    // Root view to attach banner ads
    private final ViewGroup rootView;

    // Initialization flag
    private boolean isInitialized = false;

    // AdMob settings
    private boolean isTestDevice = false;
    private boolean triggerOnPaidEvent = false;
	private boolean enableRDP = false;

    // Targeting options
    private boolean targetCOPPA = false;
    private boolean targetUnderAge = false;
    private String maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_G;

    // Ad units
    private String bannerAdUnitId = "";
    private String interstitialAdUnitId = "";
    private String rewardedVideoAdUnitId = "";
    private String rewardedInterstitialAdUnitId = "";
    private String appOpenAdUnitId = "";

    // Banner ad variables
    private AdView bannerAdView = null;
    private AdSize bannerSize = null;
    private RelativeLayout bannerLayout = null;

    // Interstitial ad variables
    private int interstitialMaxLoadedInstances = 1;
    private final ConcurrentLinkedQueue<InterstitialAd> loadedInterstitialQueue = new ConcurrentLinkedQueue<>();

    // Rewarded video ad variables
    private int rewardedVideoMaxLoadedInstances = 1;
    private final ConcurrentLinkedQueue<RewardedAd> loadedRewardedVideoQueue = new ConcurrentLinkedQueue<>();

	private String ssvUserId = null;
	private String ssvCustomData = null;

    // Rewarded interstitial ad variables
    private int rewardedInterstitialMaxLoadedInstances = 1;
    private final ConcurrentLinkedQueue<RewardedInterstitialAd> loadedRewardedInterstitialQueue = new ConcurrentLinkedQueue<>();

    // App Open ad variables
    private boolean isShowingAd = false;
    private boolean isAppOpenAdEnabled = false;
    private AppOpenAd appOpenAdInstance = null;
    private long appOpenAdLoadTime = 0;
    private int appOpenAdOrientation = AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE;

    // Consent variables
    private ConsentInformation consentInformation;
    private ConsentForm consentFormInstance;

    public GoogleMobileAdsGM() {
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

    public double AdMob_Initialize() {

		final String callingMethod = "AdMob_Initialize";

        if (!validateNotInitialized(callingMethod)) return ADMOB_ERROR_ILLEGAL_CALL;

		if (!validateViewHandler(callingMethod)) return ADMOB_ERROR_NULL_VIEW_HANDLER;

        // Ensure running from main thread
        RunnerActivity.ViewHandler.post(() -> {

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
        });

        return 0;
    }

    private void initializeAdUnits() {
        bannerAdUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_BANNER");
        interstitialAdUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_INTERSTITIAL");
        rewardedVideoAdUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_REWARDED");
        rewardedInterstitialAdUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_REWARDED_INTERSTITIAL");
        appOpenAdUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_OPENAPPAD");
    }

    public double AdMob_SetTestDeviceId() {
        if (!validateNotInitialized("AdMob_SetTestDeviceId")) return ADMOB_ERROR_ILLEGAL_CALL;

        isTestDevice = true;
        return 0;
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

		// Call the helper method with default horizontal alignment ("center")
		createBannerAdView(size, isBottom, ADMOB_BANNER_ALIGNMENT_CENTER, callingMethod);

		return 0;
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
		int alignment = (int)horizontalAlignment;
	
		if (alignment != ADMOB_BANNER_ALIGNMENT_LEFT &&
			alignment != ADMOB_BANNER_ALIGNMENT_CENTER &&
			alignment != ADMOB_BANNER_ALIGNMENT_RIGHT) {
			Log.w(LOG_TAG, callingMethod + " :: Invalid horizontal alignment parameter. Defaulting to CENTER.");
			alignment = ADMOB_BANNER_ALIGNMENT_CENTER;
		}
	
		// Call the helper method with the specified horizontal alignment
		createBannerAdView(size, isBottom, alignment, callingMethod);
	
		return 0;
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

        if (!validateActiveBannerAd(callingMethod))
            return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        RunnerActivity.ViewHandler.post(() -> {

            if (!validateActiveBannerAd(callingMethod))
                return;

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.addRule(bottom > 0.5 ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
            bannerAdView.setLayoutParams(params);
        });
        return 0;
    }

    public double AdMob_Banner_Show() {

        final String callingMethod = "AdMob_Banner_Show";

        if (!validateActiveBannerAd(callingMethod))
            return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        RunnerActivity.ViewHandler.post(() -> {

            if (!validateActiveBannerAd(callingMethod))
                return;

            bannerAdView.setVisibility(View.VISIBLE);
        });
        return 0;
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
	
			AdSize bannerSize = getAdSize(size, callingMethod);
			if (bannerSize == null) return;

			bannerLayout = new RelativeLayout(activity);
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
	
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT
			);
	
			// Set horizontal alignment based on the double value
			if (horizontalAlignment == ADMOB_BANNER_ALIGNMENT_LEFT) {
				params.addRule(RelativeLayout.ALIGN_PARENT_START);
			} else if (horizontalAlignment == ADMOB_BANNER_ALIGNMENT_RIGHT) {
				params.addRule(RelativeLayout.ALIGN_PARENT_END);
			} else {
				// Default to center if not specified or unrecognized
				params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			}
	
			// Set vertical alignment
			params.addRule(isBottom ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
	
			bannerLayout.addView(bannerAdView, params);
			rootView.addView(bannerLayout);
	
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
			});
	
			bannerAdView.setAdSize(bannerSize);
			bannerAdView.setAdUnitId(bannerAdUnitId);
			bannerAdView.requestLayout();
			bannerAdView.setVisibility(View.VISIBLE);
	
			bannerAdView.loadAd(buildAdRequest());
		});
	}

    private void deleteBannerAdView() {
		cleanAd(bannerAdView, ad -> cleanUpAd(ad));

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
		freeLoadedInstances(loadedInterstitialQueue, count, ad -> cleanUpAd(ad));
    }

    public void Admob_Interstitial_Max_Instances(double value) {
        interstitialMaxLoadedInstances = (int) value;
		trimLoadedAdsQueue(loadedInterstitialQueue, interstitialMaxLoadedInstances, ad -> cleanUpAd(ad));
    }

    public double AdMob_Interstitial_Load() {

        final String callingMethod = "AdMob_Interstitial_Load";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(interstitialAdUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

        if (!validateLoadedAdsLimit(loadedInterstitialQueue, interstitialMaxLoadedInstances, callingMethod))
            return ADMOB_ERROR_AD_LIMIT_REACHED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        loadInterstitialAd(interstitialAdUnitId, loadedInterstitialQueue, interstitialMaxLoadedInstances, callingMethod);

        return 0;
    }

    public double AdMob_Interstitial_Show() {

        final String callingMethod = "AdMob_Interstitial_Show";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdLoaded(loadedInterstitialQueue, callingMethod))
            return ADMOB_ERROR_NO_ADS_LOADED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        showInterstitialAd(loadedInterstitialQueue, callingMethod);

        return 0;
    }

    public double AdMob_Interstitial_IsLoaded() {
        return AdMob_Interstitial_Instances_Count() > 0 ? 1.0 : 0.0;
    }

    public double AdMob_Interstitial_Instances_Count() {
        return loadedInterstitialQueue.size();
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

                    adQueue.add(interstitialAd);

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

    // #region Rewarded Video

    public void AdMob_RewardedVideo_Set_AdUnit(String adUnitId) {
        rewardedVideoAdUnitId = adUnitId;
    }

	public void AdMob_RewardedVideo_Set_SSV_Options(final String userId, final String customData) {
        final String callingMethod = "AdMob_RewardedVideo_Set_SSV_Options";
    
		if (!validateInitialized(callingMethod))
			return;

		if (!validateViewHandler(callingMethod))
			return;
		
		RunnerActivity.ViewHandler.post(() -> {
			// Store the SSV options for later use when loading the ad
			ssvUserId = userId;
			ssvCustomData = customData;
		});
    }

    public void AdMob_RewardedVideo_Free_Loaded_Instances(double count) {
		freeLoadedInstances(loadedRewardedVideoQueue, count, ad -> cleanUpAd(ad));
    }

    public void AdMob_RewardedVideo_Max_Instances(double value) {
        rewardedVideoMaxLoadedInstances = (int) value;
        trimLoadedAdsQueue(loadedRewardedVideoQueue, rewardedVideoMaxLoadedInstances, ad -> cleanUpAd(ad));
    }

    public double AdMob_RewardedVideo_Load() {

        final String callingMethod = "AdMob_RewardedVideo_Load";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(rewardedVideoAdUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

        if (!validateLoadedAdsLimit(loadedRewardedVideoQueue, rewardedVideoMaxLoadedInstances, callingMethod))
            return ADMOB_ERROR_AD_LIMIT_REACHED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        loadRewardedAd(rewardedVideoAdUnitId, loadedRewardedVideoQueue, rewardedVideoMaxLoadedInstances, callingMethod);

        return 0;
    }

    public double AdMob_RewardedVideo_Show() {

        final String callingMethod = "AdMob_RewardedVideo_Show";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdLoaded(loadedRewardedVideoQueue, callingMethod))
            return ADMOB_ERROR_NO_ADS_LOADED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        showRewardedAd(loadedRewardedVideoQueue, callingMethod);

        return 0;
    }

    public double AdMob_RewardedVideo_IsLoaded() {
        return AdMob_RewardedVideo_Instances_Count() > 0 ? 1.0 : 0.0;
    }

    public double AdMob_RewardedVideo_Instances_Count() {
        return loadedRewardedVideoQueue.size();
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

					// Set ServerSideVerificationOptions if available
					if (ssvUserId != null || ssvCustomData != null) {
						ServerSideVerificationOptions.Builder ssvBuilder = new ServerSideVerificationOptions.Builder();
						if (ssvUserId != null) {
							ssvBuilder.setUserId(ssvUserId);
						}
						if (ssvCustomData != null) {
							ssvBuilder.setCustomData(ssvCustomData);
						}
						rewardedAd.setServerSideVerificationOptions(ssvBuilder.build());
					}

                    adQueue.offer(rewardedAd);

					// Reset SSV variables
					ssvUserId = null;
					ssvCustomData = null;

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
		freeLoadedInstances(loadedRewardedInterstitialQueue, count, ad -> cleanUpAd(ad));
    }

    public void AdMob_RewardedInterstitial_Max_Instances(double value) {
        rewardedInterstitialMaxLoadedInstances = (int) value;
        trimLoadedAdsQueue(loadedRewardedInterstitialQueue, rewardedInterstitialMaxLoadedInstances, ad -> cleanUpAd(ad));
    }

    public double AdMob_RewardedInterstitial_Load() {

        final String callingMethod = "AdMob_RewardedInterstitial_Load";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdId(rewardedInterstitialAdUnitId, callingMethod))
            return ADMOB_ERROR_INVALID_AD_ID;

        if (!validateLoadedAdsLimit(loadedRewardedInterstitialQueue, rewardedInterstitialMaxLoadedInstances, callingMethod))
            return ADMOB_ERROR_AD_LIMIT_REACHED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        loadRewardedInterstitialAd(rewardedInterstitialAdUnitId, loadedRewardedInterstitialQueue, rewardedInterstitialMaxLoadedInstances, callingMethod);

        return 0;
    }

    public double AdMob_RewardedInterstitial_Show() {

        final String callingMethod = "AdMob_RewardedInterstitial_Show";

        if (!validateInitialized(callingMethod))
            return ADMOB_ERROR_NOT_INITIALIZED;

        if (!validateAdLoaded(loadedRewardedInterstitialQueue, callingMethod))
            return ADMOB_ERROR_NO_ADS_LOADED;

		if (!validateViewHandler(callingMethod))
			return ADMOB_ERROR_NULL_VIEW_HANDLER;

        showRewardedInterstitialAd(loadedRewardedInterstitialQueue, callingMethod);

        return 0;
    }

    public double AdMob_RewardedInterstitial_IsLoaded() {
        return AdMob_RewardedInterstitial_Instances_Count() > 0 ? 1.0 : 0.0;
    }

    public double AdMob_RewardedInterstitial_Instances_Count() {
        return loadedRewardedInterstitialQueue.size();
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

                    adQueue.add(rewardedInterstitialAd);

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

        appOpenAdInstance = null;
        isAppOpenAdEnabled = true;
        appOpenAdOrientation = (orientation == 0) ? AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE
                : AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT;
        loadAppOpenAd();

        return 0;
    }

    public void AdMob_AppOpenAd_Disable() {
        appOpenAdInstance = null;
        isAppOpenAdEnabled = false;
    }

    public double AdMob_AppOpenAd_IsEnabled() {
        return isAppOpenAdEnabled ? 1.0 : 0.0;
    }

	private boolean appOpenAdIsValid(int expirationTimeHours, String callingMethod) {
		if (appOpenAdInstance == null) {
			Log.w(LOG_TAG, callingMethod + " :: There is no app open ad loaded.");
			return false;
		}
	
		if (appOpenAdInstance.getResponseInfo() == null) {
			Log.w(LOG_TAG, callingMethod + " :: Ad's ResponseInfo is null.");
			return false;
		}
	
		long dateDifference = (new Date()).getTime() - appOpenAdLoadTime;
		boolean expired = dateDifference >= (3600000L * expirationTimeHours);
	
		if (expired) {
			Log.w(LOG_TAG, callingMethod + " :: The loaded app open ad expired, reloading...");
			return false;
		}
	
		return true;
	}

    private void loadAppOpenAd() {

        final String callingMethod = "loadAppOpenAd";

        if (!isAppOpenAdEnabled)
            return;

        if (!validateInitialized(callingMethod))
            return;

        if (!validateAdId(appOpenAdUnitId, callingMethod))
            return;

		if (!validateViewHandler(callingMethod))
			return;

        RunnerActivity.ViewHandler.post(() -> {

            final String adUnitId = appOpenAdUnitId;

            Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            // Use application context
            Context appContext = activity.getApplicationContext();

            AppOpenAd.load(appContext, appOpenAdUnitId, buildAdRequest(), appOpenAdOrientation,
                    new AppOpenAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {

                            appOpenAdLoadTime = (new Date()).getTime();
                            appOpenAdInstance = appOpenAd;

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
                            appOpenAdInstance = null;

                            Map<String, Object> data = new HashMap<>();
                            data.put("unit_id", adUnitId);
                            data.put("errorMessage", loadAdError.getMessage());
                            data.put("errorCode", (double) loadAdError.getCode());
                            sendAsyncEvent("AdMob_AppOpenAd_OnLoadFailed", data);
                        }
                    });
        });
    }

	private void showAppOpenAd() {
		if (!isAppOpenAdEnabled)
			return;
	
		final String callingMethod = "showAppOpenAd";
	
		if (!validateInitialized(callingMethod))
			return;
	
		if (!appOpenAdIsValid(4, callingMethod)) {
			appOpenAdInstance = null;
			loadAppOpenAd();
			return;
		}
	
		if (!validateViewHandler(callingMethod))
			return;
	
		RunnerActivity.ViewHandler.post(() -> {
			// Check if the App Open ad instance is still valid
			if (appOpenAdInstance == null)
				return;
	
			// Get the Activity reference inside the Runnable
			Activity activity = getActivity(callingMethod);
			if (activity == null) return;
	
			// Set the full-screen content callback
			appOpenAdInstance.setFullScreenContentCallback(new FullScreenContentCallback() {
				@Override
				public void onAdDismissedFullScreenContent() {
					appOpenAdInstance = null;
					sendAsyncEvent("AdMob_AppOpenAd_OnDismissed", null);
					loadAppOpenAd();
				}
	
				@Override
				public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
					isShowingAd = false;
	
					appOpenAdInstance = null;
					Map<String, Object> data = new HashMap<>();
					data.put("errorMessage", adError.getMessage());
					data.put("errorCode", (double) adError.getCode());
					sendAsyncEvent("AdMob_AppOpenAd_OnShowFailed", data);
					loadAppOpenAd();
				}
	
				@Override
				public void onAdShowedFullScreenContent() {
					appOpenAdInstance = null;
					sendAsyncEvent("AdMob_AppOpenAd_OnFullyShown", null);
					loadAppOpenAd();
				}
			});
	
			// Update the isShowingAd flag and show the ad
			isShowingAd = true;
			appOpenAdInstance.show(activity);
			appOpenAdInstance = null;
		});
	}

    // #endregion

	// #region Targeting

	public double AdMob_Targeting_COPPA(double COPPA) {

		if (!validateNotInitialized("AdMob_Targeting_COPPA")) return ADMOB_ERROR_ILLEGAL_CALL;

		targetCOPPA = COPPA > 0.5;
		return 0;
	}

	public double AdMob_Targeting_UnderAge(double underAge) {

		if (!validateNotInitialized("AdMob_Targeting_UnderAge")) return ADMOB_ERROR_ILLEGAL_CALL;

		targetUnderAge = underAge >= 0.5;
		return 0;
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

			if (mode != 3) {
				ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
						.setDebugGeography((int) mode)
						.addTestDeviceHashedId(getDeviceID(callingMethod))
						.build();

				builder.setConsentDebugSettings(debugSettings);
			}

			ConsentRequestParameters params = builder.build();

			consentInformation = UserMessagingPlatform.getConsentInformation(activity);
			consentInformation.requestConsentInfoUpdate(activity, params,
					() -> sendAsyncEvent("AdMob_Consent_OnRequestInfoUpdated", null),
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

	private boolean canShowAds(Context context) {
		// https://stackoverflow.com/questions/69307205/mandatory-consent-for-admob-user-messaging-platform
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

	public void AdMob_Settings_RDP(double enabled) {
		enableRDP = enabled > 0.5;
	}

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
    public void onResume() {
		super.onResume();
        if (isAppOpenAdEnabled && !isShowingAd) {
            showAppOpenAd();
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
		freeLoadedInstances(loadedInterstitialQueue, -1, ad -> cleanUpAd(ad)); // Free all instances
		loadedInterstitialQueue.clear();

		// Clear Rewarded Ads
		freeLoadedInstances(loadedRewardedVideoQueue, -1, ad -> cleanUpAd(ad)); // Free all instances
		loadedRewardedVideoQueue.clear();

		// Clear Rewarded Interstitial Ads
		freeLoadedInstances(loadedRewardedInterstitialQueue, -1, ad -> cleanUpAd(ad)); // Free all instances
		loadedRewardedInterstitialQueue.clear();

		// Nullify App Open Ad
		if (appOpenAdInstance != null) {
			cleanAd(appOpenAdInstance, ad -> cleanUpAd(ad));
			appOpenAdInstance = null;
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
        int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
        RunnerJNILib.DsMapAddString(dsMapIndex, "type", eventType);
        if (data != null) {
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String) {
                    RunnerJNILib.DsMapAddString(dsMapIndex, key, (String) value);
                } else if (value instanceof Double) {
                    RunnerJNILib.DsMapAddDouble(dsMapIndex, key, (Double) value);
                } else if (value instanceof Integer) {
                    RunnerJNILib.DsMapAddDouble(dsMapIndex, key, ((Integer) value).doubleValue());
                } else if (value instanceof Boolean) {
                    RunnerJNILib.DsMapAddDouble(dsMapIndex, key, (Boolean) value ? 1.0 : 0.0);
                }
            }
        }
        RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
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
		if (enableRDP) {
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
