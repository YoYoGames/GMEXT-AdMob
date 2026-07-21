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

        if (!validateNotInitialized(callingMethod)) return AdMobError.IllegalCall.value();

		if (!validateViewHandler(callingMethod)) return AdMobError.NullViewHandler.value();

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

                    sendAsyncEvent("AdMob_OnInitialized", eventPayload("AdMob_OnInitialized"));
                });
            } catch (Exception e) {
                Log.i(LOG_TAG, "GoogleMobileAds Init Error: " + e.toString());
                Log.i(LOG_TAG, e.toString());
            }
        }).start();

        return AdMobError.Ok.value();
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
        if (!validateNotInitialized("admob_set_test_device_id")) return AdMobError.IllegalCall.value();

        isTestDevice = true;
        return AdMobError.Ok.value();
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
			return AdMobError.NotInitialized.value();

		if (!validateAdId(bannerAdUnitId, callingMethod))
			return AdMobError.InvalidAdId.value();

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();

		currentBannerAlignment = RelativeLayout.CENTER_HORIZONTAL;

		// Call the helper method with default horizontal alignment ("center")
		createBannerAdView(size.value(), bottom, currentBannerAlignment, callingMethod);

		return AdMobError.Ok.value();
    }
    public double admob_banner_create_ext(final AdMobBannerSize size, final boolean bottom, final AdMobBannerAlignment horizontalAlignment, final GMFunction callback) {
        bannerCallback = callback;

		final String callingMethod = "admob_banner_create_ext";
	
		if (!validateInitialized(callingMethod))
			return AdMobError.NotInitialized.value();
	
		if (!validateAdId(bannerAdUnitId, callingMethod))
			return AdMobError.InvalidAdId.value();
	
		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();
	
		
		// Validate horizontalAlignment parameter
		switch (horizontalAlignment) {
            case Left:
                currentBannerAlignment = RelativeLayout.ALIGN_PARENT_LEFT;
                break;
            case Center:
                currentBannerAlignment = RelativeLayout.CENTER_HORIZONTAL;
                break;
            case Right:
                currentBannerAlignment = RelativeLayout.ALIGN_PARENT_RIGHT;
                break;
            default:
                Log.w(LOG_TAG, callingMethod + " :: Invalid horizontal alignment parameter. Defaulting to CENTER.");
                currentBannerAlignment = RelativeLayout.CENTER_HORIZONTAL;
        }
	
		// Call the helper method with the specified horizontal alignment
		createBannerAdView(size.value(), bottom, currentBannerAlignment, callingMethod);
	
		return AdMobError.Ok.value();
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
    public void admob_banner_move(final boolean bottom) {

        final String callingMethod = "admob_banner_move";

        if (!validateInitialized(callingMethod))
            return;

        if (!validateActiveBannerAd(callingMethod))
            return;

        if (!validateViewHandler(callingMethod))
            return;

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
    }
    public void admob_banner_show() {

        final String callingMethod = "admob_banner_show";

		if (!validateInitialized(callingMethod))
			return;

        if (!validateActiveBannerAd(callingMethod))
            return;

		if (!validateViewHandler(callingMethod))
			return;

        RunnerActivity.ViewHandler.post(() -> {

            if (!validateActiveBannerAd(callingMethod))
                return;

            bannerAdView.setVisibility(View.VISIBLE);
        });
    }
    public void admob_banner_hide() {

        final String callingMethod = "admob_banner_hide";

        if (!validateActiveBannerAd(callingMethod))
            return;

		if (!validateViewHandler(callingMethod))
			return;

        RunnerActivity.ViewHandler.post(() -> {

            if (!validateActiveBannerAd(callingMethod))
                return;

            bannerAdView.setVisibility(View.GONE);
        });
    }
    public void admob_banner_remove() {

        final String callingMethod = "admob_banner_remove";

        if (!validateActiveBannerAd(callingMethod))
            return;

		if (!validateViewHandler(callingMethod))
			return;

        RunnerActivity.ViewHandler.post(() -> {

            if (!validateActiveBannerAd(callingMethod))
                return;

            deleteBannerAdView();
        });
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
					sendAsyncEvent("AdMob_Banner_OnLoaded", eventPayload("AdMob_Banner_OnLoaded"));
				}
	
				@Override
				public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
					sendAsyncEvent(
					    "AdMob_Banner_OnLoadFailed",
					    eventPayload(
					        "AdMob_Banner_OnLoadFailed",
					        (double) loadAdError.getCode(),
					        loadAdError.getMessage()
					    )
					);
				}
				
				@Override
				public void onAdOpened() {
					sendAsyncEvent("AdMob_Banner_OnOpened", eventPayload("AdMob_Banner_OnOpened"));
				}

				@Override
				public void onAdClicked() {
					sendAsyncEvent("AdMob_Banner_OnClicked", eventPayload("AdMob_Banner_OnClicked"));
				}

				@Override
				public void onAdClosed() {
					sendAsyncEvent("AdMob_Banner_OnClosed", eventPayload("AdMob_Banner_OnClosed"));
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
            return AdMobError.NotInitialized.value();

        if (!validateAdId(interstitialAdUnitId, callingMethod))
            return AdMobError.InvalidAdId.value();

        if (!validateLoadedAdsLimit(interstitialAdQueue, interstitialAdQueueCapacity, callingMethod))
            return AdMobError.AdLimitReached.value();

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();

        interstitialLoadCallbacks.offer(callback);
        loadInterstitialAd(interstitialAdUnitId, interstitialAdQueue, interstitialAdQueueCapacity, callingMethod);

        return AdMobError.Ok.value();
    }
    public double admob_interstitial_show(final GMFunction callback) {

        final String callingMethod = "admob_interstitial_show";

        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized.value();

        if (!validateAdLoaded(interstitialAdQueue, callingMethod))
            return AdMobError.NoAdsLoaded.value();

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();

        interstitialShowCallback = callback;
        showInterstitialAd(interstitialAdQueue, callingMethod);

        return AdMobError.Ok.value();
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

					sendAsyncEvent(
					    "AdMob_Interstitial_OnLoaded",
					    eventPayload("AdMob_Interstitial_OnLoaded")
					        .kv("unit_id", adUnitId)
					);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    sendAsyncEvent(
                        "AdMob_Interstitial_OnLoadFailed",
                        eventPayload(
                            "AdMob_Interstitial_OnLoadFailed",
                            (double) loadAdError.getCode(),
                            loadAdError.getMessage()
                        )
                            .kv("unit_id", adUnitId)
                    );
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

					sendAsyncEvent(
					    "AdMob_Interstitial_OnDismissed",
					    eventPayload("AdMob_Interstitial_OnDismissed")
					        .kv("unit_id", interstitialAdRef.getAdUnitId())
					);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    isShowingAd = false; // Reset the flag

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
                	cleanAd(interstitialAdRef, ad -> cleanUpAd(ad));

                    sendAsyncEvent(
                        "AdMob_Interstitial_OnShowFailed",
                        eventPayload(
                            "AdMob_Interstitial_OnShowFailed",
                            (double) adError.getCode(),
                            adError.getMessage()
                        )
                            .kv("unit_id", interstitialAdRef.getAdUnitId())
                    );
                }

                @Override
                public void onAdShowedFullScreenContent() {

					sendAsyncEvent(
					    "AdMob_Interstitial_OnFullyShown",
					    eventPayload("AdMob_Interstitial_OnFullyShown")
					        .kv("unit_id", interstitialAdRef.getAdUnitId())
					);
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
            return AdMobError.NotInitialized.value();

        if (!validateAdId(rewardedUnitId, callingMethod))
            return AdMobError.InvalidAdId.value();

        if (!validateLoadedAdsLimit(rewardedAdQueue, rewardedAdQueueCapacity, callingMethod))
            return AdMobError.AdLimitReached.value();

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();

        rewardedVideoLoadCallbacks.offer(callback);
        loadRewardedAd(rewardedUnitId, rewardedAdQueue, rewardedAdQueueCapacity, callingMethod);

        return AdMobError.Ok.value();
    }
    public double admob_rewarded_video_show(final GMFunction callback) {

        final String callingMethod = "admob_rewarded_video_show";

        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized.value();

        if (!validateAdLoaded(rewardedAdQueue, callingMethod))
            return AdMobError.NoAdsLoaded.value();

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();

        rewardedVideoShowCallback = callback;
        showRewardedAd(rewardedAdQueue, callingMethod);

        return AdMobError.Ok.value();
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

					sendAsyncEvent(
					    "AdMob_RewardedVideo_OnLoaded",
					    eventPayload("AdMob_RewardedVideo_OnLoaded")
					        .kv("unit_id", adUnitId)
					);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    sendAsyncEvent(
                        "AdMob_RewardedVideo_OnLoadFailed",
                        eventPayload(
                            "AdMob_RewardedVideo_OnLoadFailed",
                            (double) loadAdError.getCode(),
                            loadAdError.getMessage()
                        )
                            .kv("unit_id", adUnitId)
                    );
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

					sendAsyncEvent(
					    "AdMob_RewardedVideo_OnDismissed",
					    eventPayload("AdMob_RewardedVideo_OnDismissed")
					        .kv("unit_id", rewardedAdRef.getAdUnitId())
					);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    isShowingAd = false; // Reset the flag

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
					cleanAd(rewardedAdRef, ad -> cleanUpAd(ad));

                    sendAsyncEvent(
                        "AdMob_RewardedVideo_OnShowFailed",
                        eventPayload(
                            "AdMob_RewardedVideo_OnShowFailed",
                            (double) adError.getCode(),
                            adError.getMessage()
                        )
                            .kv("unit_id", rewardedAdRef.getAdUnitId())
                    );
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    sendAsyncEvent(
                        "AdMob_RewardedVideo_OnFullyShown",
                        eventPayload("AdMob_RewardedVideo_OnFullyShown")
                            .kv("unit_id", rewardedAdRef.getAdUnitId())
                    );
                }
            });

            rewardedAdRef.show(activity, rewardItem -> {
                int rewardAmount = rewardItem.getAmount();
                String rewardType = rewardItem.getType();

                sendAsyncEvent(
                    "AdMob_RewardedVideo_OnReward",
                    eventPayload("AdMob_RewardedVideo_OnReward")
                        .kv("unit_id", rewardedAdRef.getAdUnitId())
                        .kv("reward_amount", (double) rewardAmount)
                        .kv("reward_type", rewardType)
                );
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
            return AdMobError.NotInitialized.value();

        if (!validateAdId(rewardedInterstitialAdUnitId, callingMethod))
            return AdMobError.InvalidAdId.value();

        if (!validateLoadedAdsLimit(rewardedInterstitialAdQueue, rewardedAdInterstitialQueueCapacity, callingMethod))
            return AdMobError.AdLimitReached.value();

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();

        rewardedInterstitialLoadCallbacks.offer(callback);
        loadRewardedInterstitialAd(rewardedInterstitialAdUnitId, rewardedInterstitialAdQueue, rewardedAdInterstitialQueueCapacity, callingMethod);

        return AdMobError.Ok.value();
    }
    public double admob_rewarded_interstitial_show(final GMFunction callback) {

        final String callingMethod = "admob_rewarded_interstitial_show";

        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized.value();

        if (!validateAdLoaded(rewardedInterstitialAdQueue, callingMethod))
            return AdMobError.NoAdsLoaded.value();

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();

        rewardedInterstitialShowCallback = callback;
        showRewardedInterstitialAd(rewardedInterstitialAdQueue, callingMethod);

        return AdMobError.Ok.value();
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

					sendAsyncEvent(
					    "AdMob_RewardedInterstitial_OnLoaded",
					    eventPayload("AdMob_RewardedInterstitial_OnLoaded")
					        .kv("unit_id", adUnitId)
					);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    sendAsyncEvent(
                        "AdMob_RewardedInterstitial_OnLoadFailed",
                        eventPayload(
                            "AdMob_RewardedInterstitial_OnLoadFailed",
                            (double) loadAdError.getCode(),
                            loadAdError.getMessage()
                        )
                            .kv("unit_id", adUnitId)
                    );
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

					sendAsyncEvent(
					    "AdMob_RewardedInterstitial_OnDismissed",
					    eventPayload("AdMob_RewardedInterstitial_OnDismissed")
					        .kv("unit_id", rewardedInterstitialAdRef.getAdUnitId())
					);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    isShowingAd = false; // Reset the flag

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
					cleanAd(rewardedInterstitialAdRef, ad -> cleanUpAd(ad));

                    sendAsyncEvent(
                        "AdMob_RewardedInterstitial_OnShowFailed",
                        eventPayload(
                            "AdMob_RewardedInterstitial_OnShowFailed",
                            (double) adError.getCode(),
                            adError.getMessage()
                        )
                            .kv("unit_id", rewardedInterstitialAdRef.getAdUnitId())
                    );
                }

                @Override
                public void onAdShowedFullScreenContent() {
					sendAsyncEvent(
					    "AdMob_RewardedInterstitial_OnFullyShown",
					    eventPayload("AdMob_RewardedInterstitial_OnFullyShown")
					        .kv("unit_id", rewardedInterstitialAdRef.getAdUnitId())
					);
                }
            });

            rewardedInterstitialAdRef.show(activity, rewardItem -> {
                int rewardAmount = rewardItem.getAmount();
                String rewardType = rewardItem.getType();

                sendAsyncEvent(
                    "AdMob_RewardedInterstitial_OnReward",
                    eventPayload("AdMob_RewardedInterstitial_OnReward")
                        .kv("unit_id", rewardedInterstitialAdRef.getAdUnitId())
                        .kv("reward_amount", (double) rewardAmount)
                        .kv("reward_type", rewardType)
                );
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
            return AdMobError.NotInitialized.value();

        if (!validateAdId(appOpenAdUnitId, callingMethod))
            return AdMobError.InvalidAdId.value();

        triggerAppOpenAd = true;

        if (!appOpenAdIsValid(callingMethod)) {
            admob_app_open_ad_load(appOpenEnableCallback);
        }

        return AdMobError.Ok.value();
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
            return AdMobError.NotInitialized.value();

        if (!validateAdId(appOpenAdUnitId, callingMethod))
            return AdMobError.InvalidAdId.value();

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();

        if (appOpenAdIsValid(callingMethod))
            return AdMobError.Ok.value();

        appOpenLoadCallback = callback;
        loadAppOpenAd(appOpenAdUnitId, callingMethod);

        return AdMobError.Ok.value();
    }
    public double admob_app_open_ad_show(final GMFunction callback) {

		final String callingMethod = "admob_app_open_ad_show";
	
        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized.value();

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();
	
        if (!appOpenAdIsValid(callingMethod))
            return AdMobError.NoAdsLoaded.value();

        appOpenShowCallback = callback;
        showAppOpenAd(callingMethod);

        return AdMobError.Ok.value();
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

							sendAsyncEvent(
							    "AdMob_AppOpenAd_OnLoaded",
							    eventPayload("AdMob_AppOpenAd_OnLoaded")
							        .kv("unit_id", adUnitId)
							);
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            appOpenAd = null;

                            sendAsyncEvent(
                                "AdMob_AppOpenAd_OnLoadFailed",
                                eventPayload(
                                    "AdMob_AppOpenAd_OnLoadFailed",
                                    (double) loadAdError.getCode(),
                                    loadAdError.getMessage()
                                )
                                    .kv("unit_id", adUnitId)
                            );
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
					
                    sendAsyncEvent("AdMob_AppOpenAd_OnDismissed", eventPayload("AdMob_AppOpenAd_OnDismissed"));

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

					sendAsyncEvent(
					    "AdMob_AppOpenAd_OnShowFailed",
					    eventPayload(
					        "AdMob_AppOpenAd_OnShowFailed",
					        (double) adError.getCode(),
					        adError.getMessage()
					    )
					);
					
                    // If AppOpenAd is being automatically managed
                    if (triggerAppOpenAd) {
                        // Reload the App Open Ad after failure
					    admob_app_open_ad_load(appOpenEnableCallback);
                    }
				}
	
				@Override
				public void onAdShowedFullScreenContent() {
					sendAsyncEvent("AdMob_AppOpenAd_OnFullyShown", eventPayload("AdMob_AppOpenAd_OnFullyShown"));
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
    public void admob_targeting_coppa(boolean COPPA) {

		if (!validateNotInitialized("admob_targeting_coppa")) return;

		targetCOPPA = COPPA;
	}
    public void admob_targeting_under_age(boolean underAge) {

		if (!validateNotInitialized("admob_targeting_under_age")) return;

		targetUnderAge = underAge;
	}
    public void admob_targeting_max_ad_content_rating(AdMobMaxAdContentRating contentRating) {

		if (!validateNotInitialized("admob_targeting_max_ad_content_rating")) return;

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
	}

	//#endregion

	// #region Consent Management
    public double admob_consent_request_info_update(AdMobConsentDebugGeography mode, final GMFunction callback) {
        consentRequestInfoUpdateCallback = callback;

		final String callingMethod = "admob_consent_request_info_update";

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();

		RunnerActivity.ViewHandler.post(() -> {

			Activity activity = getActivity(callingMethod);
            if (activity == null) return;

			ConsentRequestParameters.Builder builder = new ConsentRequestParameters.Builder();
			builder.setTagForUnderAgeOfConsent(targetUnderAge);

			if (mode.value() > 0) {
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
						sendAsyncEvent("AdMob_Consent_OnRequestInfoUpdated", eventPayload("AdMob_Consent_OnRequestInfoUpdated"));
					},
					formError -> {
						sendAsyncEvent(
						    "AdMob_Consent_OnRequestInfoUpdateFailed",
						    eventPayload(
						        "AdMob_Consent_OnRequestInfoUpdateFailed",
						        (double) formError.getErrorCode(),
						        formError.getMessage()
						    )
						);
					});
		});
        return AdMobError.Ok.value();
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
        if (activity == null) return AdMobError.IllegalCall.value();

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();

		RunnerActivity.ViewHandler.post(() -> UserMessagingPlatform.loadConsentForm(activity,
				consentForm -> {
					consentFormInstance = consentForm;
					sendAsyncEvent("AdMob_Consent_OnLoaded", eventPayload("AdMob_Consent_OnLoaded"));
				},
				formError -> {
					sendAsyncEvent(
					    "AdMob_Consent_OnLoadFailed",
					    eventPayload(
					        "AdMob_Consent_OnLoadFailed",
					        (double) formError.getErrorCode(),
					        formError.getMessage()
					    )
					);
				}));
        return AdMobError.Ok.value();
	}
    public double admob_consent_show(final GMFunction callback) {
        consentShowCallback = callback;

		final String callingMethod = "admob_consent_show";

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler.value();

		RunnerActivity.ViewHandler.post(() -> {
			Activity activity = getActivity(callingMethod);
            if (activity == null) return;
	
			final ConsentForm consentForm = consentFormInstance;
			if (consentForm != null) {
				consentForm.show(activity, formError -> {
					if (formError == null) {
						sendAsyncEvent("AdMob_Consent_OnShown", eventPayload("AdMob_Consent_OnShown"));
					} else {
						sendAsyncEvent(
						    "AdMob_Consent_OnShowFailed",
						    eventPayload(
						        "AdMob_Consent_OnShowFailed",
						        (double) formError.getErrorCode(),
						        formError.getMessage()
						    )
						);
					}
					// Nullify instance after use
					consentFormInstance = null;
				});
			} else {
				Log.i(LOG_TAG, "admob_consent_show :: There is no loaded consent form.");
			}
		});
        return AdMobError.Ok.value();
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

    private void sendAsyncEvent(String eventType, GMExtWire.StructStream payload) {
        Activity activity = RunnerActivity.CurrentActivity;
        if (activity == null)
            return;

        activity.runOnUiThread(() -> {
            String normalizedEventType = toSnakeCase(eventType);
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

    private GMExtWire.StructStream eventPayload(String eventType) {
        return eventPayload(eventType, AdMobError.Ok.value(), "");
    }

    private GMExtWire.StructStream eventPayload(
        String eventType,
        double code,
        String errorMessage) {
        String normalizedEventType = toSnakeCase(eventType);
        boolean failed =
            code != AdMobError.Ok.value()
            || normalizedEventType.endsWith("failed")
            || normalizedEventType.endsWith("load_failed")
            || normalizedEventType.endsWith("show_failed")
            || normalizedEventType.endsWith("request_info_update_failed");

        String safeError = safeString(errorMessage);
        if (!safeError.isEmpty())
            failed = true;

        return streamStruct()
            .kv("success", !failed)
            .kv("event_type", callbackEventTypeForName(normalizedEventType))
            .kv("code", code)
            .kv("error_code", code)
            .kv("error_message", safeError);
    }

    private int callbackEventTypeForName(String eventType) {
        switch (eventType) {
            case "admob_on_initialized":
                return AdMobInitializeCallbackEvent.Initialized.value();

            case "admob_banner_on_loaded":
                return AdMobBannerCallbackEvent.Loaded.value();
            case "admob_banner_on_load_failed":
                return AdMobBannerCallbackEvent.LoadFailed.value();
            case "admob_banner_on_opened":
                return AdMobBannerCallbackEvent.Opened.value();
            case "admob_banner_on_clicked":
                return AdMobBannerCallbackEvent.Clicked.value();
            case "admob_banner_on_closed":
                return AdMobBannerCallbackEvent.Closed.value();

            case "admob_interstitial_on_loaded":
            case "admob_rewarded_video_on_loaded":
            case "admob_rewarded_interstitial_on_loaded":
            case "admob_app_open_ad_on_loaded":
                return AdMobInterstitialCallbackEvent.Loaded.value();

            case "admob_interstitial_on_load_failed":
            case "admob_rewarded_video_on_load_failed":
            case "admob_rewarded_interstitial_on_load_failed":
            case "admob_app_open_ad_on_load_failed":
                return AdMobInterstitialCallbackEvent.LoadFailed.value();

            case "admob_interstitial_on_fully_shown":
            case "admob_rewarded_video_on_fully_shown":
            case "admob_rewarded_interstitial_on_fully_shown":
            case "admob_app_open_ad_on_fully_shown":
                return AdMobInterstitialCallbackEvent.Shown.value();

            case "admob_interstitial_on_show_failed":
            case "admob_rewarded_video_on_show_failed":
            case "admob_rewarded_interstitial_on_show_failed":
            case "admob_app_open_ad_on_show_failed":
                return AdMobInterstitialCallbackEvent.ShowFailed.value();

            case "admob_interstitial_on_dismissed":
            case "admob_rewarded_video_on_dismissed":
            case "admob_rewarded_interstitial_on_dismissed":
            case "admob_app_open_ad_on_dismissed":
                return AdMobInterstitialCallbackEvent.Dismissed.value();

            case "admob_rewarded_video_on_reward":
            case "admob_rewarded_interstitial_on_reward":
                return AdMobRewardedVideoCallbackEvent.Reward.value();

            case "admob_consent_on_request_info_updated":
                return AdMobConsentCallbackEvent.RequestInfoUpdated.value();
            case "admob_consent_on_request_info_update_failed":
                return AdMobConsentCallbackEvent.RequestInfoUpdateFailed.value();
            case "admob_consent_on_loaded":
                return AdMobConsentCallbackEvent.Loaded.value();
            case "admob_consent_on_load_failed":
                return AdMobConsentCallbackEvent.LoadFailed.value();
            case "admob_consent_on_shown":
                return AdMobConsentCallbackEvent.Dismissed.value();
            case "admob_consent_on_show_failed":
                return AdMobConsentCallbackEvent.ShowFailed.value();

            case "admob_on_paid_event":
                return AdMobPaidEventCallbackEvent.Paid.value();

            default:
                return -1;
        }
    }

    private void callbackResult(GMFunction callback, int eventType, double code) {
        callbackResult(callback, eventType, code == AdMobError.Ok.value(), code);
    }

    private void callbackResult(GMFunction callback, int eventType, boolean success, double code) {
        GMExtWire.StructStream payload = streamStruct()
            .kv("success", success)
            .kv("event_type", eventType)
            .kv("code", code)
            .kv("error_code", code)
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
        switch (AdMobError.from(code)) {
            case Ok:
                return "";
            case NotInitialized:
                return "AdMob SDK is not initialized.";
            case InvalidAdId:
                return "The AdMob ad unit ID is invalid or empty.";
            case AdLimitReached:
                return "The loaded ad instance limit was reached.";
            case NoAdsLoaded:
                return "There are no ads loaded.";
            case NoActiveBannerAd:
                return "There is no active banner ad.";
            case IllegalCall:
                return "This call is not valid in the current AdMob state.";
            case NullViewHandler:
                return "RunnerActivity.ViewHandler is null.";
            default:
                return "Unknown AdMob error.";
        }
    }


    private void onPaidEventHandler(AdValue adValue, String adUnitId, String adType,
                                    AdapterResponseInfo loadedAdapterResponseInfo, String mediationAdapterClassName) {

        GMExtWire.StructStream payload =
            eventPayload("AdMob_OnPaidEvent")
                .kv("mediation_adapter_class_name", safeString(mediationAdapterClassName))
                .kv("unit_id", safeString(adUnitId))
                .kv("ad_type", safeString(adType))
                .kv("micros", adValue.getValueMicros())
                .kv("currency_code", safeString(adValue.getCurrencyCode()))
                .kv("precision", (double) adValue.getPrecisionType());

        if (loadedAdapterResponseInfo != null) {
            payload
                .kv("ad_source_name", safeString(loadedAdapterResponseInfo.getAdSourceName()))
                .kv("ad_source_id", safeString(loadedAdapterResponseInfo.getAdSourceId()))
                .kv("ad_source_instance_name", safeString(loadedAdapterResponseInfo.getAdSourceInstanceName()))
                .kv("ad_source_instance_id", safeString(loadedAdapterResponseInfo.getAdSourceInstanceId()));
        } else {
            Log.w(LOG_TAG, "LoadedAdapterResponseInfo is null.");
        }

        sendAsyncEvent("AdMob_OnPaidEvent", payload);
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
