package ${YYAndroidPackageName};

import ${YYAndroidPackageName}.R;
import ${YYAndroidPackageName}.GMExtWire.GMFunction;
import ${YYAndroidPackageName}.enums.*;
import ${YYAndroidPackageName}.records.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

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

    private static final String LOG_TAG = "AdMob";

    // Loud-log threshold for an ad-handle map - not a hard cap, since evicting one would
    // silently invalidate a handle the caller may still legitimately hold.
    private static final int AD_HANDLE_WARN_THRESHOLD = 50;

    // WeakReference to Activity to prevent memory leaks
    private WeakReference<Activity> activityRef;

    // Root view to attach banner ads
    private volatile ViewGroup rootView;

    // AdMob settings
    private volatile boolean isInitialized = false;
    private volatile boolean isTestDevice = false;
    private volatile boolean isRdpEnabled = false;
    private boolean isShowingAd = false;

    // Targeting options
    private volatile boolean targetCOPPA = false;
    private volatile boolean targetUnderAge = false;
    private volatile String maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_G;

    // Banner ad variables
    private volatile String bannerAdUnitId = "";
    private volatile AdView bannerAdView = null;
    private volatile AdSize bannerSize = null;
    private int currentBannerAlignment = RelativeLayout.CENTER_HORIZONTAL;
    private volatile RelativeLayout bannerLayout = null;

    // Shared across interstitial/rewarded video/rewarded interstitial handles so a handle from
    // one ad type can never coincide with a live handle from another - a mismatched call site
    // (wrong variable passed to the wrong show()) fails loud as InvalidHandle instead of
    // potentially matching an unrelated ad by coincidence.
    private final AtomicLong nextAdHandle = new AtomicLong(1);

    // Interstitial ad variables
    private volatile String interstitialAdUnitId = "";
    private final Map<Long, InterstitialAd> interstitialAdHandles = new ConcurrentHashMap<>();

    // Server side verification variables
	private volatile String serverSideVerificationUserId = null;
	private volatile String serverSideVerificationCustomData = null;

    // Rewarded video ad variables
    private volatile String rewardedUnitId = "";
    private final Map<Long, RewardedAd> rewardedAdHandles = new ConcurrentHashMap<>();

    // Rewarded interstitial ad variables
    private volatile String rewardedInterstitialAdUnitId = "";
    private final Map<Long, RewardedInterstitialAd> rewardedInterstitialAdHandles = new ConcurrentHashMap<>();

    // App Open ad variables
    private volatile String appOpenAdUnitId = "";
    // volatile: written from AppOpenAd.load()'s completion callback (thread not documented by
    // Google), read from the game thread via is_loaded/show - same cross-thread pattern the
    // interstitial/rewarded handle maps guard with ConcurrentHashMap.
    private volatile int appOpenAdOrientation = Configuration.ORIENTATION_UNDEFINED;
    private volatile long appOpenAdLoadTime = 0;
    private volatile int appOpenAdExpirationTime = 4;
    private volatile AppOpenAd appOpenAd = null;

    private volatile boolean triggerOnPaidEvent = false;
    private volatile boolean triggerAppOpenAd = false;

    // Consent variables
    private volatile ConsentInformation consentInformation;
    private volatile ConsentForm consentFormInstance;

    private volatile GMFunction paidEventCallback = null;
    private volatile GMFunction bannerCallback = null;
    private volatile GMFunction appOpenEnableCallback = null;
    private volatile GMFunction appOpenLoadCallback = null;
    private volatile GMFunction appOpenShowCallback = null;
    // Reject a concurrent load/show instead of silently overwriting the pending
    // caller's callback, same as banner's bannerLoadPending guard.
    private volatile boolean appOpenLoadPending = false;
    private volatile boolean appOpenShowPending = false;

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
    public AdMobError admob_initialize(final GMFunction callback) {

		final String callingMethod = "admob_initialize";

        if (!validateNotInitialized(callingMethod)) return AdMobError.IllegalCall;

		if (!validateViewHandler(callingMethod)) return AdMobError.NullViewHandler;

        // Run initialization in a background thread
        new Thread(() -> {
            MobileAds.setRequestConfiguration(buildRequestConfiguration(callingMethod));

            try {
                Activity activity = getActivity(callingMethod);
                if (activity == null) {
                    invokeLoadCallback(callback, new AdMobResult(false, Optional.of("Activity reference is null."), Optional.empty()));
                    return;
                }

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

                    invokeLoadCallback(callback, new AdMobResult(true, Optional.empty(), Optional.empty()));
                });
            } catch (Exception e) {
                Log.i(LOG_TAG, "GoogleMobileAds Init Error: " + e.toString());
                invokeLoadCallback(callback, new AdMobResult(false, Optional.of(e.toString()), Optional.empty()));
            }
        }).start();

        return AdMobError.Ok;
    }

    private void initializeAdUnits() {
        bannerAdUnitId = getAdMobOptionString("Android_BANNER");
        interstitialAdUnitId = getAdMobOptionString("Android_INTERSTITIAL");
        rewardedUnitId = getAdMobOptionString("Android_REWARDED");
        rewardedInterstitialAdUnitId = getAdMobOptionString("Android_REWARDED_INTERSTITIAL");
        appOpenAdUnitId = getAdMobOptionString("Android_OPENAPPAD");
    }

    private String getAdMobOptionString(String optionName) {
        String value = GMExtUtils.GetExtensionOption("GMAdMob", optionName);

        return value != null ? value : "";
    }

    private String normalizeAdUnitId(String adUnitId) {
        return adUnitId != null ? adUnitId : "";
    }
    public AdMobError admob_set_test_device_id() {
        if (!validateNotInitialized("admob_set_test_device_id")) return AdMobError.IllegalCall;

        isTestDevice = true;
        return AdMobError.Ok;
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
    public AdMobError admob_banner_create(final AdMobBannerSize size, final boolean bottom, final GMFunction callback) {
        bannerCallback = callback;

        final String callingMethod = "admob_banner_create";

		if (!validateInitialized(callingMethod))
			return AdMobError.NotInitialized;

		if (!validateAdId(bannerAdUnitId, callingMethod))
			return AdMobError.InvalidAdId;

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;

		currentBannerAlignment = RelativeLayout.CENTER_HORIZONTAL;

		// Call the helper method with default horizontal alignment ("center")
		createBannerAdView(size.value(), bottom, currentBannerAlignment, callingMethod);

		return AdMobError.Ok;
    }
    public AdMobError admob_banner_create_ext(final AdMobBannerSize size, final boolean bottom, final AdMobBannerAlignment horizontalAlignment, final GMFunction callback) {
        bannerCallback = callback;

		final String callingMethod = "admob_banner_create_ext";

		if (!validateInitialized(callingMethod))
			return AdMobError.NotInitialized;

		if (!validateAdId(bannerAdUnitId, callingMethod))
			return AdMobError.InvalidAdId;

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;


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

		return AdMobError.Ok;
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
					AdapterResponseInfo loadedAdapterResponseInfo = bannerAdView.getResponseInfo()
							.getLoadedAdapterResponseInfo();
					if (loadedAdapterResponseInfo == null) return;
					onPaidEventHandler(adValue, bannerAdView.getAdUnitId(), AdMobAdType.Banner,
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
					invokeBannerEventCallback(new AdMobResult(true, Optional.empty(), Optional.empty()), AdMobBannerCallbackEvent.Loaded);
				}

				@Override
				public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
					invokeBannerEventCallback(
					    new AdMobResult(false, Optional.of(loadAdError.getMessage()), Optional.of(loadAdError.getCode())),
					    AdMobBannerCallbackEvent.LoadFailed
					);
				}

				@Override
				public void onAdOpened() {
					invokeBannerEventCallback(new AdMobResult(true, Optional.empty(), Optional.empty()), AdMobBannerCallbackEvent.Opened);
				}

				@Override
				public void onAdClicked() {
					invokeBannerEventCallback(new AdMobResult(true, Optional.empty(), Optional.empty()), AdMobBannerCallbackEvent.Clicked);
				}

				@Override
				public void onAdImpression() {
					invokeBannerEventCallback(new AdMobResult(true, Optional.empty(), Optional.empty()), AdMobBannerCallbackEvent.Impression);
				}

				@Override
				public void onAdClosed() {
					invokeBannerEventCallback(new AdMobResult(true, Optional.empty(), Optional.empty()), AdMobBannerCallbackEvent.Closed);
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
		// Already always called from the UI thread (ViewHandler.post/onDestroy), so clean up
		// synchronously here instead of via cleanAd()'s own post - that nested post would only
		// run after destroy() below, clearing the listeners after the view is already gone.
		cleanUpAd(bannerAdView);

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
    public AdMobError admob_interstitial_load(final GMFunction callback, final Optional<String> adUnitId) {

        final String callingMethod = "admob_interstitial_load";

        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized;

        final String resolvedAdUnitId = adUnitId.orElse(interstitialAdUnitId);

        if (!validateAdId(resolvedAdUnitId, callingMethod))
            return AdMobError.InvalidAdId;

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;

        loadInterstitialAd(resolvedAdUnitId, callback, callingMethod);

        return AdMobError.Ok;
    }
    public boolean admob_interstitial_is_valid(long handle) {
        return interstitialAdHandles.containsKey(handle);
    }
    public void admob_interstitial_dispose(long handle) {
        InterstitialAd ad = interstitialAdHandles.remove(handle);
        cleanAd(ad, this::cleanUpAd);
    }
    public AdMobError admob_interstitial_show(final long handle, final GMFunction callback) {

        final String callingMethod = "admob_interstitial_show";

        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized;

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;

        final InterstitialAd interstitialAdRef = interstitialAdHandles.remove(handle);
        if (interstitialAdRef == null) {
            Log.w(LOG_TAG, callingMethod + " :: Handle is invalid, already shown, or already disposed.");
            return AdMobError.InvalidHandle;
        }

        showInterstitialAd(interstitialAdRef, callback, callingMethod);

        return AdMobError.Ok;
    }

    private void loadInterstitialAd(final String adUnitId, final GMFunction callback, final String callingMethod) {
        RunnerActivity.ViewHandler.post(() -> {

			Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            // Use application context
            Context appContext = activity.getApplicationContext();

            InterstitialAd.load(appContext, adUnitId, buildAdRequest(), new InterstitialAdLoadCallback() {

                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                    if (triggerOnPaidEvent) {
                        interstitialAd.setOnPaidEventListener(adValue -> {
                            AdapterResponseInfo loadedAdapterResponseInfo = interstitialAd.getResponseInfo().getLoadedAdapterResponseInfo();
                            if (loadedAdapterResponseInfo == null) return;
                            onPaidEventHandler(adValue, interstitialAd.getAdUnitId(), AdMobAdType.Interstitial,
                                    loadedAdapterResponseInfo,
                                    interstitialAd.getResponseInfo().getMediationAdapterClassName());
                        });
                    }

                    long handle = nextAdHandle.getAndIncrement();
                    interstitialAdHandles.put(handle, interstitialAd);
                    warnIfAdHandleMapGrowing("interstitialAdHandles", interstitialAdHandles);

                    invokeLoadCallback(callback, new AdMobResult(true, Optional.empty(), Optional.empty()), handle);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    invokeLoadCallback(
                        callback,
                        new AdMobResult(false, Optional.of(loadAdError.getMessage()), Optional.of(loadAdError.getCode()))
                    );
                }
            });
        });
    }

    private void showInterstitialAd(final InterstitialAd interstitialAdRef, final GMFunction callback, final String callingMethod) {
        RunnerActivity.ViewHandler.post(() -> {

			Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            interstitialAdRef.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    isShowingAd = false; // Reset the flag

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
                	cleanAd(interstitialAdRef, ad -> cleanUpAd(ad));

                    invokeShowEventCallback(callback, AdMobInterstitialShowEvent.Dismissed);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    isShowingAd = false; // Reset the flag

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
                	cleanAd(interstitialAdRef, ad -> cleanUpAd(ad));

                    invokeShowFailedCallback(callback, adError);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    invokeShowEventCallback(callback, AdMobInterstitialShowEvent.Shown);
                }

                @Override
                public void onAdClicked() {
                    invokeShowEventCallback(callback, AdMobInterstitialShowEvent.Clicked);
                }

                @Override
                public void onAdImpression() {
                    invokeShowEventCallback(callback, AdMobInterstitialShowEvent.Impression);
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
    public AdMobError admob_rewarded_video_load(final GMFunction callback, final Optional<String> adUnitId) {

        final String callingMethod = "admob_rewarded_video_load";

        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized;

        final String resolvedAdUnitId = adUnitId.orElse(rewardedUnitId);

        if (!validateAdId(resolvedAdUnitId, callingMethod))
            return AdMobError.InvalidAdId;

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;

        loadRewardedAd(resolvedAdUnitId, callback, callingMethod);

        return AdMobError.Ok;
    }
    public boolean admob_rewarded_video_is_valid(long handle) {
        return rewardedAdHandles.containsKey(handle);
    }
    public void admob_rewarded_video_dispose(long handle) {
        RewardedAd ad = rewardedAdHandles.remove(handle);
        cleanAd(ad, this::cleanUpAd);
    }
    public AdMobError admob_rewarded_video_show(final long handle, final GMFunction callback) {

        final String callingMethod = "admob_rewarded_video_show";

        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized;

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;

        final RewardedAd rewardedAdRef = rewardedAdHandles.remove(handle);
        if (rewardedAdRef == null) {
            Log.w(LOG_TAG, callingMethod + " :: Handle is invalid, already shown, or already disposed.");
            return AdMobError.InvalidHandle;
        }

        showRewardedAd(rewardedAdRef, callback, callingMethod);

        return AdMobError.Ok;
    }

    private void loadRewardedAd(final String adUnitId, final GMFunction callback, final String callingMethod) {
        RunnerActivity.ViewHandler.post(() -> {

            Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            // Use application context
            Context appContext = activity.getApplicationContext();

            RewardedAd.load(appContext, adUnitId, buildAdRequest(), new RewardedAdLoadCallback() {

                @Override
                public void onAdLoaded(@NonNull RewardedAd rewardedAd) {

                    final String userId = serverSideVerificationUserId;
                    final String customData = serverSideVerificationCustomData;

					// Configure server-side verification using the helper method
                    configureServerSideVerification(rewardedAd, userId, customData);

                    if (triggerOnPaidEvent) {
                        rewardedAd.setOnPaidEventListener(adValue -> {
                            AdapterResponseInfo loadedAdapterResponseInfo = rewardedAd.getResponseInfo().getLoadedAdapterResponseInfo();
                            if (loadedAdapterResponseInfo == null) return;
                            onPaidEventHandler(adValue, rewardedAd.getAdUnitId(), AdMobAdType.RewardedVideo,
                                    loadedAdapterResponseInfo,
                                    rewardedAd.getResponseInfo().getMediationAdapterClassName());
                        });
                    }

                    long handle = nextAdHandle.getAndIncrement();
                    rewardedAdHandles.put(handle, rewardedAd);
                    warnIfAdHandleMapGrowing("rewardedAdHandles", rewardedAdHandles);

                    invokeLoadCallback(callback, new AdMobResult(true, Optional.empty(), Optional.empty()), handle);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    invokeLoadCallback(
                        callback,
                        new AdMobResult(false, Optional.of(loadAdError.getMessage()), Optional.of(loadAdError.getCode()))
                    );
                }
            });
        });
    }

    private void showRewardedAd(final RewardedAd rewardedAdRef, final GMFunction callback, final String callingMethod) {
        RunnerActivity.ViewHandler.post(() -> {

			Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            rewardedAdRef.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    isShowingAd = false; // Reset the flag

					// Use the generic cleanAd method with cleanUpAd as the cleaner
					cleanAd(rewardedAdRef, ad -> cleanUpAd(ad));

                    invokeShowEventCallback(callback, AdMobRewardedVideoShowEvent.Dismissed);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    isShowingAd = false; // Reset the flag

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
					cleanAd(rewardedAdRef, ad -> cleanUpAd(ad));

                    invokeShowFailedCallback(callback, adError);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    invokeShowEventCallback(callback, AdMobRewardedVideoShowEvent.Shown);
                }

                @Override
                public void onAdClicked() {
                    invokeShowEventCallback(callback, AdMobRewardedVideoShowEvent.Clicked);
                }

                @Override
                public void onAdImpression() {
                    invokeShowEventCallback(callback, AdMobRewardedVideoShowEvent.Impression);
                }
            });

            rewardedAdRef.show(activity, rewardItem -> {
                int rewardAmount = rewardItem.getAmount();
                String rewardType = rewardItem.getType();

                invokeRewardCallback(callback, AdMobRewardedVideoShowEvent.Reward.value(), new AdMobReward(rewardAmount, rewardType));
            });

            isShowingAd = true;
        });
    }

    // #endregion

    // #region Rewarded Interstitial
    public void admob_rewarded_interstitial_set_ad_unit(String adUnitId) {
        rewardedInterstitialAdUnitId = normalizeAdUnitId(adUnitId);
    }
    public AdMobError admob_rewarded_interstitial_load(final GMFunction callback, final Optional<String> adUnitId) {

        final String callingMethod = "admob_rewarded_interstitial_load";

        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized;

        final String resolvedAdUnitId = adUnitId.orElse(rewardedInterstitialAdUnitId);

        if (!validateAdId(resolvedAdUnitId, callingMethod))
            return AdMobError.InvalidAdId;

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;

        loadRewardedInterstitialAd(resolvedAdUnitId, callback, callingMethod);

        return AdMobError.Ok;
    }
    public boolean admob_rewarded_interstitial_is_valid(long handle) {
        return rewardedInterstitialAdHandles.containsKey(handle);
    }
    public void admob_rewarded_interstitial_dispose(long handle) {
        RewardedInterstitialAd ad = rewardedInterstitialAdHandles.remove(handle);
        cleanAd(ad, this::cleanUpAd);
    }
    public AdMobError admob_rewarded_interstitial_show(final long handle, final GMFunction callback) {

        final String callingMethod = "admob_rewarded_interstitial_show";

        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized;

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;

        final RewardedInterstitialAd rewardedInterstitialAdRef = rewardedInterstitialAdHandles.remove(handle);
        if (rewardedInterstitialAdRef == null) {
            Log.w(LOG_TAG, callingMethod + " :: Handle is invalid, already shown, or already disposed.");
            return AdMobError.InvalidHandle;
        }

        showRewardedInterstitialAd(rewardedInterstitialAdRef, callback, callingMethod);

        return AdMobError.Ok;
    }

    private void loadRewardedInterstitialAd(final String adUnitId, final GMFunction callback, final String callingMethod) {
        RunnerActivity.ViewHandler.post(() -> {

            Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            // Use application context
            Context appContext = activity.getApplicationContext();

            RewardedInterstitialAd.load(appContext, adUnitId, buildAdRequest(), new RewardedInterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {

                    final String userId = serverSideVerificationUserId;
                    final String customData = serverSideVerificationCustomData;

					// Configure server-side verification using the helper method
                    configureServerSideVerification(rewardedInterstitialAd, userId, customData);

                    if (triggerOnPaidEvent) {
                        rewardedInterstitialAd.setOnPaidEventListener(adValue -> {
                            AdapterResponseInfo loadedAdapterResponseInfo = rewardedInterstitialAd.getResponseInfo().getLoadedAdapterResponseInfo();
                            if (loadedAdapterResponseInfo == null) return;
                            onPaidEventHandler(adValue, rewardedInterstitialAd.getAdUnitId(), AdMobAdType.RewardedInterstitial,
                                    loadedAdapterResponseInfo,
                                    rewardedInterstitialAd.getResponseInfo().getMediationAdapterClassName());
                        });
                    }

                    long handle = nextAdHandle.getAndIncrement();
                    rewardedInterstitialAdHandles.put(handle, rewardedInterstitialAd);
                    warnIfAdHandleMapGrowing("rewardedInterstitialAdHandles", rewardedInterstitialAdHandles);

                    invokeLoadCallback(callback, new AdMobResult(true, Optional.empty(), Optional.empty()), handle);
                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    invokeLoadCallback(
                        callback,
                        new AdMobResult(false, Optional.of(loadAdError.getMessage()), Optional.of(loadAdError.getCode()))
                    );
                }
            });
        });
    }

    private void showRewardedInterstitialAd(final RewardedInterstitialAd rewardedInterstitialAdRef, final GMFunction callback, final String callingMethod) {
        RunnerActivity.ViewHandler.post(() -> {

			Activity activity = getActivity(callingMethod);
            if (activity == null) return;

            rewardedInterstitialAdRef.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    isShowingAd = false; // Reset the flag

					// Use the generic cleanAd method with cleanUpAd as the cleaner
					cleanAd(rewardedInterstitialAdRef, ad -> cleanUpAd(ad));

                    invokeShowEventCallback(callback, AdMobRewardedInterstitialShowEvent.Dismissed);
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    isShowingAd = false; // Reset the flag

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
					cleanAd(rewardedInterstitialAdRef, ad -> cleanUpAd(ad));

                    invokeShowFailedCallback(callback, adError);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    invokeShowEventCallback(callback, AdMobRewardedInterstitialShowEvent.Shown);
                }

                @Override
                public void onAdClicked() {
                    invokeShowEventCallback(callback, AdMobRewardedInterstitialShowEvent.Clicked);
                }

                @Override
                public void onAdImpression() {
                    invokeShowEventCallback(callback, AdMobRewardedInterstitialShowEvent.Impression);
                }
            });

            rewardedInterstitialAdRef.show(activity, rewardItem -> {
                int rewardAmount = rewardItem.getAmount();
                String rewardType = rewardItem.getType();

                invokeRewardCallback(callback, AdMobRewardedInterstitialShowEvent.Reward.value(), new AdMobReward(rewardAmount, rewardType));
            });

            isShowingAd = true;
        });
    }

    // #endregion

    // #region App Open Ad
    public void admob_app_open_ad_set_ad_unit(String adUnitId) {
        appOpenAdUnitId = normalizeAdUnitId(adUnitId);
    }
    public AdMobError admob_app_open_ad_enable(final GMFunction callback) {
        appOpenEnableCallback = callback;

        final String callingMethod = "admob_app_open_ad_enable";

        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized;

        if (!validateAdId(appOpenAdUnitId, callingMethod))
            return AdMobError.InvalidAdId;

        triggerAppOpenAd = true;

        if (!appOpenAdIsValid(callingMethod)) {
            admob_app_open_ad_load(appOpenEnableCallback);
        } else {
            invokeLoadCallback(callback, new AdMobResult(true, Optional.empty(), Optional.empty()));
        }

        return AdMobError.Ok;
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
    public AdMobError admob_app_open_ad_load(final GMFunction callback) {

        final String callingMethod = "admob_app_open_ad_load";

        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized;

        if (!validateAdId(appOpenAdUnitId, callingMethod))
            return AdMobError.InvalidAdId;

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;

        if (appOpenAdIsValid(callingMethod)) {
            invokeLoadCallback(callback, new AdMobResult(true, Optional.empty(), Optional.empty()));
            return AdMobError.Ok;
        }

        if (appOpenLoadPending)
            return AdMobError.IllegalCall;

        appOpenLoadPending = true;
        appOpenLoadCallback = callback;
        loadAppOpenAd(appOpenAdUnitId, callingMethod);

        return AdMobError.Ok;
    }
    public AdMobError admob_app_open_ad_show(final GMFunction callback) {

		final String callingMethod = "admob_app_open_ad_show";

        if (!validateInitialized(callingMethod))
            return AdMobError.NotInitialized;

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;

        if (!appOpenAdIsValid(callingMethod))
            return AdMobError.NoAdsLoaded;

        if (appOpenShowPending)
            return AdMobError.IllegalCall;

        appOpenShowPending = true;
        appOpenShowCallback = callback;
        showAppOpenAd(callingMethod);

        return AdMobError.Ok;
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
                                    onPaidEventHandler(adValue, appOpenAd.getAdUnitId(), AdMobAdType.AppOpen,
                                            loadedAdapterResponseInfo,
                                            appOpenAd.getResponseInfo().getMediationAdapterClassName());
                                });
                            }

                            GMFunction resolvedCallback = appOpenLoadCallback != null ? appOpenLoadCallback : appOpenEnableCallback;
                            appOpenLoadCallback = null;
                            appOpenLoadPending = false;
                            invokeLoadCallback(resolvedCallback, new AdMobResult(true, Optional.empty(), Optional.empty()));
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            appOpenAd = null;

                            GMFunction resolvedCallback = appOpenLoadCallback != null ? appOpenLoadCallback : appOpenEnableCallback;
                            appOpenLoadCallback = null;
                            appOpenLoadPending = false;
                            invokeLoadCallback(
                                resolvedCallback,
                                new AdMobResult(false, Optional.of(loadAdError.getMessage()), Optional.of(loadAdError.getCode()))
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
                    isShowingAd = false; // Reset the flag

                    // Use the generic cleanAd method with cleanUpAd as the cleaner
                	cleanAd(appOpenAd, ad -> cleanUpAd(ad));
                    appOpenAd = null;

                    GMFunction resolvedCallback = appOpenShowCallback != null ? appOpenShowCallback : appOpenEnableCallback;
                    appOpenShowCallback = null;
                    appOpenShowPending = false;
                    invokeShowEventCallback(resolvedCallback, AdMobAppOpenAdShowEvent.Dismissed);

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

                    GMFunction resolvedCallback = appOpenShowCallback != null ? appOpenShowCallback : appOpenEnableCallback;
                    appOpenShowCallback = null;
                    appOpenShowPending = false;
                    invokeShowFailedCallback(resolvedCallback, adError);

                    // If AppOpenAd is being automatically managed
                    if (triggerAppOpenAd) {
                        // Reload the App Open Ad after failure
					    admob_app_open_ad_load(appOpenEnableCallback);
                    }
				}

				@Override
				public void onAdShowedFullScreenContent() {
					GMFunction resolvedCallback = appOpenShowCallback != null ? appOpenShowCallback : appOpenEnableCallback;
					invokeShowEventCallback(resolvedCallback, AdMobAppOpenAdShowEvent.Shown);
				}

				@Override
				public void onAdClicked() {
					GMFunction resolvedCallback = appOpenShowCallback != null ? appOpenShowCallback : appOpenEnableCallback;
					invokeShowEventCallback(resolvedCallback, AdMobAppOpenAdShowEvent.Clicked);
				}

				@Override
				public void onAdImpression() {
					GMFunction resolvedCallback = appOpenShowCallback != null ? appOpenShowCallback : appOpenEnableCallback;
					invokeShowEventCallback(resolvedCallback, AdMobAppOpenAdShowEvent.Impression);
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
    public AdMobError admob_consent_request_info_update(AdMobConsentDebugGeography mode, final GMFunction callback) {

		final String callingMethod = "admob_consent_request_info_update";

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;

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
					() -> invokeLoadCallback(callback, new AdMobResult(true, Optional.empty(), Optional.empty())),
					formError -> invokeLoadCallback(
					    callback,
					    new AdMobResult(false, Optional.of(formError.getMessage()), Optional.of(formError.getErrorCode()))
					));
		});
        return AdMobError.Ok;
	}
    public AdMobConsentStatus admob_consent_get_status() {
		return consentInformation == null ? AdMobConsentStatus.Unknown : AdMobConsentStatus.from(consentInformation.getConsentStatus());
	}
    public AdMobConsentType admob_consent_get_type() {
		if (consentInformation == null)
			return AdMobConsentType.Unknown;

		if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.OBTAINED) {

			Context context = RunnerJNILib.ms_context;
			if (!canShowAds(context))
				return AdMobConsentType.Declined;

			return canShowPersonalizedAds(context) ? AdMobConsentType.Personalized : AdMobConsentType.NonPersonalized;
		}

		return AdMobConsentType.Unknown;
	}
    public boolean admob_consent_is_form_available() {
        return consentInformation != null
            && consentInformation.isConsentFormAvailable();
    }
    public AdMobError admob_consent_load(final GMFunction callback) {

		final String callingMethod = "admob_consent_load";

		Activity activity = getActivity(callingMethod);
        if (activity == null) return AdMobError.IllegalCall;

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;

		RunnerActivity.ViewHandler.post(() -> UserMessagingPlatform.loadConsentForm(activity,
				consentForm -> {
					consentFormInstance = consentForm;
					invokeLoadCallback(callback, new AdMobResult(true, Optional.empty(), Optional.empty()));
				},
				formError -> invokeLoadCallback(
				    callback,
				    new AdMobResult(false, Optional.of(formError.getMessage()), Optional.of(formError.getErrorCode()))
				)));
        return AdMobError.Ok;
	}
    public AdMobError admob_consent_show(final GMFunction callback) {

		final String callingMethod = "admob_consent_show";

		if (!validateViewHandler(callingMethod))
			return AdMobError.NullViewHandler;

		RunnerActivity.ViewHandler.post(() -> {
			Activity activity = getActivity(callingMethod);
            if (activity == null) return;

			final ConsentForm consentForm = consentFormInstance;
			if (consentForm != null) {
				consentForm.show(activity, formError -> {
					if (formError == null) {
						invokeLoadCallback(callback, new AdMobResult(true, Optional.empty(), Optional.empty()));
					} else {
						invokeLoadCallback(
						    callback,
						    new AdMobResult(false, Optional.of(formError.getMessage()), Optional.of(formError.getErrorCode()))
						);
					}
					// Nullify instance after use
					consentFormInstance = null;
				});
			} else {
				Log.i(LOG_TAG, "admob_consent_show :: There is no loaded consent form.");
			}
		});
        return AdMobError.Ok;
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
		Activity activity = RunnerActivity.CurrentActivity;
		activityRef = new WeakReference<>(activity);

		// Refresh rootView too - it holds a hard reference into the Activity's view
		// hierarchy, so surviving an Activity recreation without refreshing it would
		// attach new banners into a destroyed view tree and leak the old Activity.
		if (activity != null)
			rootView = activity.findViewById(android.R.id.content);
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
        }
    }

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Clean up Banner Ad
		if (bannerAdView != null) {
			deleteBannerAdView();
		}

		// Clear Interstitial Ads
		for (InterstitialAd ad : interstitialAdHandles.values())
			cleanAd(ad, this::cleanUpAd);
		interstitialAdHandles.clear();

		// Clear Rewarded Ads
		for (RewardedAd ad : rewardedAdHandles.values())
			cleanAd(ad, this::cleanUpAd);
		rewardedAdHandles.clear();

		// Clear Rewarded Interstitial Ads
		for (RewardedInterstitialAd ad : rewardedInterstitialAdHandles.values())
			cleanAd(ad, this::cleanUpAd);
		rewardedInterstitialAdHandles.clear();

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

	private void warnIfAdHandleMapGrowing(String mapName, Map<Long, ?> handles) {
		if (handles.size() == AD_HANDLE_WARN_THRESHOLD) {
			Log.w(LOG_TAG, mapName + " has " + handles.size()
					+ " outstanding loaded-but-undisposed handles - are load() calls missing a matching show()/dispose()?");
		}
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

    private void runOnUiThreadOrNow(Runnable action) {
        Activity activity = RunnerActivity.CurrentActivity;
        if (activity != null)
            activity.runOnUiThread(action);
        else
            action.run();
    }

    private void invokeLoadCallback(GMFunction callback, AdMobResult result, long handle) {
        runOnUiThreadOrNow(() -> {
            if (callback != null)
                callback.call(result, handle);
        });
    }

    private void invokeLoadCallback(GMFunction callback, AdMobResult result) {
        runOnUiThreadOrNow(() -> {
            if (callback != null)
                callback.call(result);
        });
    }

    private void invokeShowEventCallback(GMFunction callback, AdMobInterstitialShowEvent type) {
        runOnUiThreadOrNow(() -> {
            if (callback != null)
                callback.call(new AdMobResult(true, Optional.empty(), Optional.empty()), type.value());
        });
    }

    private void invokeShowEventCallback(GMFunction callback, AdMobAppOpenAdShowEvent type) {
        runOnUiThreadOrNow(() -> {
            if (callback != null)
                callback.call(new AdMobResult(true, Optional.empty(), Optional.empty()), type.value());
        });
    }

    private void invokeBannerEventCallback(AdMobResult result, AdMobBannerCallbackEvent type) {
        runOnUiThreadOrNow(() -> {
            if (bannerCallback != null)
                bannerCallback.call(result, type.value());
        });
    }

    private void invokeShowFailedCallback(GMFunction callback, AdError adError) {
        runOnUiThreadOrNow(() -> {
            if (callback != null)
                callback.call(new AdMobResult(false, Optional.of(adError.getMessage()), Optional.of(adError.getCode())));
        });
    }

    private void invokeShowEventCallback(GMFunction callback, AdMobRewardedVideoShowEvent type) {
        runOnUiThreadOrNow(() -> {
            if (callback != null)
                callback.call(new AdMobResult(true, Optional.empty(), Optional.empty()), type.value());
        });
    }

    private void invokeShowEventCallback(GMFunction callback, AdMobRewardedInterstitialShowEvent type) {
        runOnUiThreadOrNow(() -> {
            if (callback != null)
                callback.call(new AdMobResult(true, Optional.empty(), Optional.empty()), type.value());
        });
    }

    private void invokeRewardCallback(GMFunction callback, int type, AdMobReward reward) {
        runOnUiThreadOrNow(() -> {
            if (callback != null)
                callback.call(new AdMobResult(true, Optional.empty(), Optional.empty()), type, reward);
        });
    }

    private static String safeString(String value) {
        return value != null ? value : "";
    }

    private void onPaidEventHandler(AdValue adValue, String adUnitId, AdMobAdType adType,
                                    AdapterResponseInfo loadedAdapterResponseInfo, String mediationAdapterClassName) {

        Optional<String> adSourceName = Optional.empty();
        Optional<String> adSourceId = Optional.empty();
        Optional<String> adSourceInstanceName = Optional.empty();
        Optional<String> adSourceInstanceId = Optional.empty();

        if (loadedAdapterResponseInfo != null) {
            adSourceName = Optional.of(safeString(loadedAdapterResponseInfo.getAdSourceName()));
            adSourceId = Optional.of(safeString(loadedAdapterResponseInfo.getAdSourceId()));
            adSourceInstanceName = Optional.of(safeString(loadedAdapterResponseInfo.getAdSourceInstanceName()));
            adSourceInstanceId = Optional.of(safeString(loadedAdapterResponseInfo.getAdSourceInstanceId()));
        } else {
            Log.w(LOG_TAG, "LoadedAdapterResponseInfo is null.");
        }

        AdMobPaidEvent event = new AdMobPaidEvent(
            adType,
            safeString(adUnitId),
            adValue.getValueMicros(),
            safeString(adValue.getCurrencyCode()),
            AdMobPrecisionType.from(adValue.getPrecisionType()),
            safeString(mediationAdapterClassName),
            adSourceName,
            adSourceId,
            adSourceInstanceName,
            adSourceInstanceId
        );

        runOnUiThreadOrNow(() -> {
            if (paidEventCallback != null)
                paidEventCallback.call(event);
        });
    }

	private AdRequest buildAdRequest() {
		AdRequest.Builder builder = new AdRequest.Builder();
	
		// As per Google's request, set the request agent
		builder.setRequestAgent("gmext-admob-" + GMExtUtils.GetExtensionVersion("GMAdMob"));
	
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


    // #endregion
}
