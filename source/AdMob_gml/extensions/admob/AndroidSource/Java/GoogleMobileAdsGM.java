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

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class GoogleMobileAdsGM extends RunnerSocial {

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

    private double AdMob_AppOpenAd_Load() {

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

	private double AdMob_AppOpenAd_Show() {

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
    public void onResume() {
		super.onResume();
        if (triggerAppOpenAd && !isShowingAd) {
            if (!appOpenAdIsValid("onResume")) {
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
        RunnerActivity.CurrentActivity.runOnUiThread(() -> {
            int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
            RunnerJNILib.DsMapAddString(dsMapIndex, "type", eventType);
            if (data != null) {
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        RunnerJNILib.DsMapAddString(dsMapIndex, key, (String) value);
                    } else if (value instanceof Double || value instanceof Integer || value instanceof Float || value instanceof Boolean) {
                        // Convert Boolean to double (1.0 or 0.0)
                        double doubleValue;
                        if (value instanceof Boolean) {
                            doubleValue = (Boolean) value ? 1.0 : 0.0;
                        } else if (value instanceof Integer) {
                            doubleValue = ((Integer) value).doubleValue();
                        } else if (value instanceof Float) {
                            doubleValue = ((Float) value).doubleValue();
                        } else { // Double
                            doubleValue = (Double) value;
                        }
                        RunnerJNILib.DsMapAddDouble(dsMapIndex, key, doubleValue);
                    } else if (value instanceof Long) {
                        long longValue = (Long) value;
                        if (Math.abs(longValue) <= MAX_DOUBLE_SAFE) {
                            RunnerJNILib.DsMapAddDouble(dsMapIndex, key, (double) longValue);
                        } else {
                            String formattedLong = String.format("@i64@%016x$i64$", longValue);
                            RunnerJNILib.DsMapAddString(dsMapIndex, key, formattedLong);
                        }
                    } else if (value instanceof Map) {
                        String jsonString = new JSONObject((Map) value).toString();
                        RunnerJNILib.DsMapAddString(dsMapIndex, key, jsonString);
                    } else if (value instanceof List) {
                        String jsonString = new JSONArray((List) value).toString();
                        RunnerJNILib.DsMapAddString(dsMapIndex, key, jsonString);
                    } else {
                        // Convert other types to String
                        RunnerJNILib.DsMapAddString(dsMapIndex, key, value.toString());
                    }
                }
            }
            RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
        });
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
