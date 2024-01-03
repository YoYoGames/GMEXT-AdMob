package ${YYAndroidPackageName};

import ${YYAndroidPackageName}.R;
import com.yoyogames.runner.RunnerJNILib;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.app.Activity;
import android.view.ViewGroup;

import java.lang.Exception;

import android.provider.Settings;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.ArrayList;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.RequestConfiguration;

import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback;

import com.google.android.ump.*;

import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;

import androidx.annotation.NonNull;

import android.util.Log;

import java.util.List;

import android.util.DisplayMetrics;
import android.view.Display;

import java.util.Date;

import android.os.Process;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import com.google.android.gms.ads.OnPaidEventListener;
import com.google.android.gms.ads.AdValue;
import com.google.android.gms.ads.AdapterResponseInfo;

import android.os.Bundle;
import com.google.ads.mediation.admob.AdMobAdapter;

public class GoogleMobileAdsGM extends RunnerSocial {
	private static final int EVENT_OTHER_SOCIAL = 70;
	private static final int ADMOB_ERROR_NOT_INITIALIZED = -1;
	private static final int ADMOB_ERROR_INVALID_AD_ID = -2;
	private static final int ADMOB_ERROR_AD_LIMIT_REACHED = -3;
	private static final int ADMOB_ERROR_NO_ADS_LOADED = -4;
	private static final int ADMOB_ERROR_NO_ACTIVE_BANNER_AD = -5;
	private static final int ADMOB_ERROR_ILLEGAL_CALL = -6;

	private static final String LOG_TAG = "AdMob";

	private final Activity activity;
	private final ViewGroup rootView;

	public GoogleMobileAdsGM() {
		activity = RunnerActivity.CurrentActivity;
		rootView = activity.findViewById(android.R.id.content);
	}

	// #region Setup

	private ExecutorService executorService = createExecutorService(500);

	private ExecutorService createExecutorService(int keepAliveTime) {
		int numberOfCores = Runtime.getRuntime().availableProcessors();
		TimeUnit keepAliveTimeUnit = TimeUnit.MILLISECONDS;
		BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();

		executorService = new ThreadPoolExecutor(
				numberOfCores,
				numberOfCores * 2,
				keepAliveTime,
				keepAliveTimeUnit,
				taskQueue,
				new BackgroundThreadFactory());

		return executorService;
	}

	private static class BackgroundThreadFactory implements ThreadFactory {
		private static int sTag = 1;

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable);
			thread.setName("AdmobInitThread" + (sTag++));
			thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
			thread.setUncaughtExceptionHandler((aThread, ex) -> Log.e(LOG_TAG, aThread.getName() + " encountered an error: " + ex.getMessage()));
			return thread;
		}
	}

	Boolean isInitialized = false;

	public double AdMob_Initialize() {

		if (!validateNotInitialized("AdMob_Initialize")) return ADMOB_ERROR_ILLEGAL_CALL;

		// ThreadPoolExecutor
		executorService.execute(new Runnable() {
			public void run() {
				MobileAds.setRequestConfiguration(buildRequestConfiguration());

				try {
					MobileAds.initialize(activity, initializationStatus -> {
						Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
						for (String adapterClass : statusMap.keySet()) {
							AdapterStatus status = statusMap.get(adapterClass);
							assert status != null;
							Log.d(LOG_TAG, String.format("Adapter name: %s, Description: %s, Latency: %d",
									adapterClass, status.getDescription(), status.getLatency()));
						}

						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_OnInitialized");
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);

						// Initialize ad types using extension options (if they are not empty)
						String bannerUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_BANNER");
						if (!bannerUnitId.isEmpty())
							AdMob_Banner_Set_AdUnit(bannerUnitId);

						String interstitialUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_INTERSTITIAL");
						if (!interstitialUnitId.isEmpty())
							AdMob_Interstitial_Set_AdUnit(interstitialUnitId);

						String rewardedVideoUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_REWARDED");
						if (!rewardedVideoUnitId.isEmpty())
							AdMob_RewardedVideo_Set_AdUnit(rewardedVideoUnitId);

						String rewardedInterstitialUnitId = RunnerJNILib.extOptGetString("AdMob",
								"Android_REWARDED_INTERSTITIAL");
						if (!rewardedInterstitialUnitId.isEmpty())
							AdMob_RewardedInterstitial_Set_AdUnit(rewardedInterstitialUnitId);

						String appOpenUnitId = RunnerJNILib.extOptGetString("AdMob", "Android_OPENAPPAD");
						if (!appOpenUnitId.isEmpty())
							AdMob_AppOpenAd_Set_AdUnit(appOpenUnitId);

						isInitialized = true;
					});
				} catch (Exception e) {
					Log.i(LOG_TAG, "GoogleMobileAds Init Error: " + e.toString());
					Log.i(LOG_TAG, e.toString());
				}
			}
		});

		return 0;
	}

	private boolean isTestDevice = false;

	public double AdMob_SetTestDeviceId() {
		if (!validateNotInitialized("AdMob_SetTestDeviceId")) return ADMOB_ERROR_ILLEGAL_CALL;

		isTestDevice = true;
		return 0;
	}

	boolean triggerOnPaidEvent = false;

	public void AdMob_Events_OnPaidEvent(double enabled) {
		triggerOnPaidEvent = enabled >= 0.5;
	}

	private RequestConfiguration buildRequestConfiguration() {

		RequestConfiguration.Builder requestConfigurationBuilder = MobileAds.getRequestConfiguration().toBuilder();

		if (isTestDevice) {
			List<String> testDeviceIds = Collections.singletonList(getDeviceID());
			requestConfigurationBuilder.setTestDeviceIds(testDeviceIds);
		}

		if (targetCOPPA)
			requestConfigurationBuilder.setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE);

		if (targetUnderAge)
			requestConfigurationBuilder.setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE);

		requestConfigurationBuilder.setMaxAdContentRating(maxAdContentRating);

		return requestConfigurationBuilder.build();
	}

	public void onPaidEventHandler(AdValue adValue, String adUnitId, String adType,
			AdapterResponseInfo loadedAdapterResponseInfo, String mediationAdapterClassName) {


		int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
		RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_OnPaidEvent");

		RunnerJNILib.DsMapAddString(dsMapIndex, "mediation_adapter_class_name", mediationAdapterClassName);

		RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", adUnitId);
		RunnerJNILib.DsMapAddString(dsMapIndex, "ad_type", adType);
		RunnerJNILib.DsMapAddDouble(dsMapIndex, "micros", adValue.getValueMicros());
		RunnerJNILib.DsMapAddString(dsMapIndex, "currency_code", adValue.getCurrencyCode());
		RunnerJNILib.DsMapAddDouble(dsMapIndex, "precision", adValue.getPrecisionType());

		RunnerJNILib.DsMapAddString(dsMapIndex, "ad_source_name", loadedAdapterResponseInfo.getAdSourceName());
		RunnerJNILib.DsMapAddString(dsMapIndex, "ad_source_id", loadedAdapterResponseInfo.getAdSourceId());
		RunnerJNILib.DsMapAddString(dsMapIndex, "ad_source_instance_name",
				loadedAdapterResponseInfo.getAdSourceInstanceName());
		RunnerJNILib.DsMapAddString(dsMapIndex, "ad_source_instance_id",
				loadedAdapterResponseInfo.getAdSourceInstanceId());

		RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
	}

	boolean nonPersonalizedAds = false;
	public void AdMob_NonPersonalizedAds_Set(double value) {
		nonPersonalizedAds = value >= 0.5;
	}

	// #endregion

	// #region Banner

	private AdView bannerAdView = null;
	private AdSize bannerSize = null;
	private RelativeLayout bannerLayout = null;

	private String bannerAdUnitId = "";

	public void AdMob_Banner_Set_AdUnit(String adUnitId) {
		bannerAdUnitId = adUnitId;
	}

	public double AdMob_Banner_Create(final double size, final double bottom) {

		final String callingMethod = "AdMob_Banner_Create";

		if (!validateInitialized(callingMethod))
			return ADMOB_ERROR_NOT_INITIALIZED;

		if (!validateAdId(bannerAdUnitId, callingMethod))
			return ADMOB_ERROR_INVALID_AD_ID;

		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {
				if (bannerAdView != null) {
					deleteBannerAdView();
				}

				bannerLayout = new RelativeLayout(activity);
				bannerAdView = new AdView(activity);

				if (triggerOnPaidEvent) {
					final AdView bannerAdRef = bannerAdView;
					bannerAdView.setOnPaidEventListener(adValue -> {
						AdapterResponseInfo loadedAdapterResponseInfo = Objects.requireNonNull(bannerAdRef.getResponseInfo())
								.getLoadedAdapterResponseInfo();
						if (loadedAdapterResponseInfo == null) return;
						onPaidEventHandler(adValue, bannerAdRef.getAdUnitId(), "Banner", loadedAdapterResponseInfo,
								bannerAdRef.getResponseInfo().getMediationAdapterClassName());
					});
				}

				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);

				params.addRule(RelativeLayout.CENTER_HORIZONTAL);
				params.addRule((bottom > 0.5) ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
				bannerLayout.addView((View) bannerAdView, params);
				rootView.addView((View) bannerLayout);

				bannerAdView.setAdListener(new AdListener() {

					@Override
					public void onAdLoaded() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Banner_OnLoaded");
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Banner_OnLoadFailed");
						RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", loadAdError.getMessage());
						RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", loadAdError.getCode());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdOpened() {
						// Code to be executed when an ad opens an overlay that
						// covers the screen.
					}

					@Override
					public void onAdClicked() {
						// Code to be executed when the user clicks on an ad.
					}

					@Override
					public void onAdClosed() {
						// Code to be executed when the user is about to return
						// to the app after tapping on an ad.
					}
				});

				bannerSize = getBannerSize(size);

				assert bannerSize != null;
				bannerAdView.setAdSize(bannerSize);
				bannerAdView.setAdUnitId(bannerAdUnitId);
				bannerAdView.requestLayout();
				bannerAdView.setVisibility(View.VISIBLE);

				bannerAdView.loadAd(buildAdRequest());
			}
		});

		return 0;
	}

	public double AdMob_Banner_GetWidth() {
		// If there is no active banner ad, return 0
		if (bannerAdView == null) return 0;

		// Get the width of the banner in pixels
		return bannerSize.getWidthInPixels(RunnerJNILib.ms_context);
	}

	public double AdMob_Banner_GetHeight() {
		// If there is no active banner ad, return 0
		if (bannerAdView == null) return 0;

		// Get the height of the banner in pixels
		int h = bannerSize.getHeightInPixels(RunnerJNILib.ms_context);

		// Special handling for SMART_BANNER to adjust height based on screen size and
		// density
		if (bannerSize == AdSize.SMART_BANNER) {
			DisplayMetrics displayMetrics = (RunnerJNILib.ms_context).getResources().getDisplayMetrics();

			// Calculate screen height in density-independent pixels (DP)
			int screenHeightInDP = Math.round(displayMetrics.heightPixels / displayMetrics.density);

			// Get screen density
			int density = Math.round(displayMetrics.density);

			// Adjust height based on screen height in DP
			if (screenHeightInDP < 400)
				h = 32 * density; // Smaller screens
			else if (screenHeightInDP <= 720)
				h = 50 * density; // Medium screens
			else
				h = 90 * density; // Larger screens
		}
		return h;
	}

	public double AdMob_Banner_Move(final double bottom) {

		final String callingMethod = "AdMob_Banner_Move";

		// If there is no active banner ad, return 0
		if (!validateActiveBannerAd(callingMethod))
			return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;

		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {

				if (!validateActiveBannerAd(callingMethod))
					return;

				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.CENTER_HORIZONTAL);
				params.addRule(bottom > 0.5 ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
				bannerAdView.setLayoutParams(params);
			}
		});
		return 0;
	}

	public double AdMob_Banner_Show() {

		final String callingMethod = "AdMob_Banner_Show";

		if (!validateActiveBannerAd(callingMethod))
			return ADMOB_ERROR_NO_ACTIVE_BANNER_AD;

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

		RunnerActivity.ViewHandler.post(() -> {

			if (!validateActiveBannerAd(callingMethod))
				return;

			deleteBannerAdView();
		});
		return 0;
	}

	private void deleteBannerAdView() {
		// Remove bannerAdView from bannerLayout
		bannerLayout.removeView(bannerAdView);
		bannerAdView.destroy();
		bannerAdView = null;
		// Remove bannerLayout from rootView
		rootView.removeView(bannerLayout);
		bannerLayout = null;
		// Reset bannerSize
		bannerSize = null;
	}

	private AdSize getBannerSize(double size) {
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
		}

		return null;
	}

	//#endregion

	// #region Interstitial

	private int interstitialMaxLoadedInstances = 1;
	private final ConcurrentLinkedQueue<InterstitialAd> loadedInterstitialQueue = new ConcurrentLinkedQueue<>();

	private String interstitialAdUnitId = "";

	public void AdMob_Interstitial_Set_AdUnit(String adUnitId) {
		interstitialAdUnitId = adUnitId;
	}

	public void Admob_Interstitial_Free_Loaded_Instances(double count) {
		if (count < 0) {
			count = loadedInterstitialQueue.size();
		}

		while (count > 0 && loadedInterstitialQueue.size() > 0) {
			loadedInterstitialQueue.poll();
			count--;
		}
	}

	public void Admob_Interstitial_Max_Instances(double value) {
		interstitialMaxLoadedInstances = (int) value;

		int size = loadedInterstitialQueue.size();
		if (size <= value) return;
		
		Admob_Interstitial_Free_Loaded_Instances(size - value);
	}

	public double AdMob_Interstitial_Load() {

		final String callingMethod = "AdMob_Interstitial_Load";

		if (!validateInitialized(callingMethod))
			return ADMOB_ERROR_NOT_INITIALIZED;

		if (!validateAdId(interstitialAdUnitId, callingMethod))
			return ADMOB_ERROR_INVALID_AD_ID;

		if (!validateLoadedAdsLimit(loadedInterstitialQueue, interstitialMaxLoadedInstances, callingMethod))
			return ADMOB_ERROR_AD_LIMIT_REACHED;

		RunnerActivity.ViewHandler.post(new Runnable() {

			final String adUnitId = interstitialAdUnitId;

			public void run() {
				InterstitialAd.load(activity, adUnitId, buildAdRequest(), new InterstitialAdLoadCallback() {

					@Override
					public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

						if (!validateLoadedAdsLimit(loadedInterstitialQueue, interstitialMaxLoadedInstances,
								callingMethod))
							return;

						loadedInterstitialQueue.add(interstitialAd);

						if (triggerOnPaidEvent) {
							final InterstitialAd interstitialAdRef = interstitialAd;
							interstitialAd.setOnPaidEventListener(adValue -> {
								AdapterResponseInfo loadedAdapterResponseInfo = interstitialAdRef.getResponseInfo()
										.getLoadedAdapterResponseInfo();
								if(loadedAdapterResponseInfo == null) return;
								onPaidEventHandler(adValue, interstitialAdRef.getAdUnitId(), "Interstitial",
										loadedAdapterResponseInfo,
										interstitialAdRef.getResponseInfo().getMediationAdapterClassName());
							});
						}

						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Interstitial_OnLoaded");
						RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", adUnitId);
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Interstitial_OnLoadFailed");
						RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", adUnitId);
						RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", loadAdError.getMessage());
						RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", loadAdError.getCode());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}
				});
			}
		});
		return 0;
	}

	public double AdMob_Interstitial_Show() {

		final String callingMethod = "AdMob_Interstitial_Show";

		if (!validateInitialized(callingMethod))
			return ADMOB_ERROR_NOT_INITIALIZED;

		if (!validateAdLoaded(loadedInterstitialQueue, callingMethod))
			return ADMOB_ERROR_NO_ADS_LOADED;

		final InterstitialAd interstitialAdRef = loadedInterstitialQueue.poll();
		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {
				assert interstitialAdRef != null;
				interstitialAdRef.setFullScreenContentCallback(new FullScreenContentCallback() {
					@Override
					public void onAdDismissedFullScreenContent() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Interstitial_OnDismissed");
						RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", interstitialAdRef.getAdUnitId());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Interstitial_OnShowFailed");
						RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", interstitialAdRef.getAdUnitId());
						RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", adError.getMessage());
						RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", adError.getCode());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdShowedFullScreenContent() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Interstitial_OnFullyShown");
						RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", interstitialAdRef.getAdUnitId());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}
				});

				interstitialAdRef.show(activity);
				isShowingAd = true;
			}
		});
		return 0;
	}

	public double AdMob_Interstitial_IsLoaded() {
		return AdMob_Interstitial_Instances_Count() > 0 ? 1.0 : 0.0;
	}

	public double AdMob_Interstitial_Instances_Count() {
		return loadedInterstitialQueue.size();
	}

	// #endregion

	// #region Rewarded Video

	private int rewardedVideoMaxLoadedInstances = 1;
	private final ConcurrentLinkedQueue<RewardedAd> loadedRewardedVideoQueue = new ConcurrentLinkedQueue<>();

	private String rewardedVideoAdUnitId = "";

	public void AdMob_RewardedVideo_Set_AdUnit(String adUnitId) {
		rewardedVideoAdUnitId = adUnitId;
	}

	public void AdMob_RewardedVideo_Free_Loaded_Instances(double count) {
		if (count < 0) {
			count = loadedRewardedVideoQueue.size();
		}

		while (count > 0 && loadedRewardedVideoQueue.size() > 0) {
			loadedRewardedVideoQueue.poll();
			count--;
		}
	}

	public void AdMob_RewardedVideo_Max_Instances(double value) {
		rewardedVideoMaxLoadedInstances = (int) value;

		int size = loadedRewardedVideoQueue.size();
		if (size <= value) return;
		
		AdMob_RewardedVideo_Free_Loaded_Instances(size - value);
	}

	public double AdMob_RewardedVideo_Load() {

		final String callingMethod = "AdMob_RewardedVideo_Load";

		if (!validateInitialized(callingMethod))
			return ADMOB_ERROR_NOT_INITIALIZED;

		if (!validateAdId(rewardedVideoAdUnitId, callingMethod))
			return ADMOB_ERROR_INVALID_AD_ID;

		if (!validateLoadedAdsLimit(loadedRewardedVideoQueue, rewardedVideoMaxLoadedInstances, callingMethod))
			return ADMOB_ERROR_AD_LIMIT_REACHED;

		RunnerActivity.ViewHandler.post(new Runnable() {

			final String adUnitId = rewardedVideoAdUnitId;

			public void run() {
				RewardedAd.load(activity, adUnitId, buildAdRequest(), new RewardedAdLoadCallback() {

					@Override
					public void onAdLoaded(@NonNull RewardedAd rewardedAd) {

						if (!validateLoadedAdsLimit(loadedRewardedVideoQueue, rewardedVideoMaxLoadedInstances,
								callingMethod))
							return;

						loadedRewardedVideoQueue.add(rewardedAd);

						if (triggerOnPaidEvent) {
							final RewardedAd rewardedAdRef = rewardedAd;
							rewardedAd.setOnPaidEventListener(new OnPaidEventListener() {
								@Override
								public void onPaidEvent(@NonNull AdValue adValue) {
									AdapterResponseInfo loadedAdapterResponseInfo = rewardedAdRef.getResponseInfo()
											.getLoadedAdapterResponseInfo();
									if (loadedAdapterResponseInfo == null) return;
									onPaidEventHandler(adValue, rewardedAdRef.getAdUnitId(), "RewardedVideo",
											loadedAdapterResponseInfo,
											rewardedAdRef.getResponseInfo().getMediationAdapterClassName());
								}
							});
						}

						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedVideo_OnLoaded");
						RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", adUnitId);
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedVideo_OnLoadFailed");
						RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", adUnitId);
						RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", loadAdError.getMessage());
						RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", loadAdError.getCode());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}
				});
			}
		});
		return 0;
	}

	public double AdMob_RewardedVideo_Show() {

		final String callingMethod = "AdMob_RewardedVideo_Show";

		if (!validateInitialized(callingMethod))
			return ADMOB_ERROR_NOT_INITIALIZED;

		if (!validateAdLoaded(loadedRewardedVideoQueue, callingMethod))
			return ADMOB_ERROR_NO_ADS_LOADED;

		final RewardedAd rewardedAd = loadedRewardedVideoQueue.poll();
		RunnerActivity.ViewHandler.post(() -> {

			assert rewardedAd != null;
			rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
				@Override
				public void onAdDismissedFullScreenContent() {
					int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
					RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedVideo_OnDismissed");
					RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", rewardedAd.getAdUnitId());
					RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
				}

				@Override
				public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
					int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
					RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedVideo_OnShowFailed");
					RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", rewardedAd.getAdUnitId());
					RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", adError.getMessage());
					RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", adError.getCode());
					RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
				}

				@Override
				public void onAdShowedFullScreenContent() {
					int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
					RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedVideo_OnFullyShown");
					RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", rewardedAd.getAdUnitId());
					RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
				}
			});

			rewardedAd.show(activity,
					rewardItem -> {
						int rewardAmount = rewardItem.getAmount();
						String rewardType = rewardItem.getType();

						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedVideo_OnReward");
						RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", rewardedAd.getAdUnitId());
						RunnerJNILib.DsMapAddDouble(dsMapIndex, "reward_amount", rewardAmount);
						RunnerJNILib.DsMapAddString(dsMapIndex, "reward_type", rewardType);
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					});

			isShowingAd = true;
		});
		return 0;
	}

	public double AdMob_RewardedVideo_IsLoaded() {
		return AdMob_RewardedVideo_Instances_Count() > 0 ? 1.0 : 0.0;
	}

	public double AdMob_RewardedVideo_Instances_Count() {
		return loadedRewardedVideoQueue.size();
	}

	// #endregion

	// #region Rewarded Interstitial

	private int rewardedInterstitialMaxLoadedInstances = 1;
	private final ConcurrentLinkedQueue<RewardedInterstitialAd> loadedRewardedInterstitialQueue = new ConcurrentLinkedQueue<>();

	private String rewardedInterstitialAdUnitId = "";

	public void AdMob_RewardedInterstitial_Set_AdUnit(String adUnitId) {
		rewardedInterstitialAdUnitId = adUnitId;
	}

	public void AdMob_RewardedInterstitial_Free_Loaded_Instances(double count) {
		if (count < 0) {
			count = loadedRewardedInterstitialQueue.size();
		}

		while (count > 0 && loadedRewardedInterstitialQueue.size() > 0) {
			loadedRewardedInterstitialQueue.poll();
			count--;
		}
	}

	public void AdMob_RewardedInterstitial_Max_Instances(double value) {
		rewardedInterstitialMaxLoadedInstances = (int) value;

		int size = loadedRewardedInterstitialQueue.size();
		if (size <= value) return;
		
		AdMob_RewardedInterstitial_Free_Loaded_Instances(size - value);
	}

	public double AdMob_RewardedInterstitial_Load() {

		final String callingMethod = "AdMob_RewardedInterstitial_Load";

		if (!validateInitialized(callingMethod))
			return ADMOB_ERROR_NOT_INITIALIZED;

		if (!validateAdId(rewardedInterstitialAdUnitId, callingMethod))
			return ADMOB_ERROR_INVALID_AD_ID;

		if (!validateLoadedAdsLimit(loadedRewardedInterstitialQueue, rewardedInterstitialMaxLoadedInstances,
				callingMethod))
			return ADMOB_ERROR_AD_LIMIT_REACHED;

		RunnerActivity.ViewHandler.post(new Runnable() {

			final String adUnitId = rewardedInterstitialAdUnitId;

			public void run() {
				RewardedInterstitialAd.load(activity, rewardedInterstitialAdUnitId, buildAdRequest(),
						new RewardedInterstitialAdLoadCallback() {
							@Override
							public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {

								if (!validateLoadedAdsLimit(loadedRewardedInterstitialQueue,
										rewardedInterstitialMaxLoadedInstances, callingMethod))
									return;

								loadedRewardedInterstitialQueue.add(rewardedInterstitialAd);

								if (triggerOnPaidEvent) {
									final RewardedInterstitialAd rewardedInterstitialAdRef = rewardedInterstitialAd;

									rewardedInterstitialAd.setOnPaidEventListener(new OnPaidEventListener() {
										@Override
										public void onPaidEvent(@NonNull AdValue adValue) {
											AdapterResponseInfo loadedAdapterResponseInfo = rewardedInterstitialAdRef
													.getResponseInfo().getLoadedAdapterResponseInfo();
											if (loadedAdapterResponseInfo == null) return;
											onPaidEventHandler(adValue, rewardedInterstitialAdRef.getAdUnitId(),
													"RewardedInterstitial", loadedAdapterResponseInfo,
													rewardedInterstitialAdRef.getResponseInfo()
															.getMediationAdapterClassName());
										}
									});
								}

								int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
								RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedInterstitial_OnLoaded");
								RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", adUnitId);
								RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
							}

							@Override
							public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
								int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
								RunnerJNILib.DsMapAddString(dsMapIndex, "type",
										"AdMob_RewardedInterstitial_OnLoadFailed");
								RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", adUnitId);
								RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", loadAdError.getMessage());
								RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", loadAdError.getCode());
								RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
							}
						});
			}
		});
		return 0;
	}

	public double AdMob_RewardedInterstitial_Show() {

		final String callingMethod = "AdMob_RewardedInterstitial_Show";

		if (!validateInitialized(callingMethod))
			return ADMOB_ERROR_NOT_INITIALIZED;

		if (!validateAdLoaded(loadedRewardedInterstitialQueue, callingMethod))
			return ADMOB_ERROR_NO_ADS_LOADED;

		final RewardedInterstitialAd rewardedInterstitialAd = loadedRewardedInterstitialQueue.poll();
		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {

				assert rewardedInterstitialAd != null;
				rewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
					@Override
					public void onAdDismissedFullScreenContent() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedInterstitial_OnDismissed");
						RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", rewardedInterstitialAd.getAdUnitId());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {

						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedInterstitial_OnShowFailed");
						RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", rewardedInterstitialAd.getAdUnitId());
						RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", adError.getMessage());
						RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", adError.getCode());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdShowedFullScreenContent() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedInterstitial_OnFullyShown");
						RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", rewardedInterstitialAd.getAdUnitId());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}
				});

				rewardedInterstitialAd.show(activity, rewardItem -> {
					int rewardAmount = rewardItem.getAmount();
					String rewardType = rewardItem.getType();

					int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
					RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedInterstitial_OnReward");
					RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", rewardedInterstitialAd.getAdUnitId());
					RunnerJNILib.DsMapAddDouble(dsMapIndex, "reward_amount", rewardAmount);
					RunnerJNILib.DsMapAddString(dsMapIndex, "reward_type", rewardType);
					RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
				});

				isShowingAd = true;
			}
		});
		return 0;
	}

	public double AdMob_RewardedInterstitial_IsLoaded() {
		return AdMob_RewardedInterstitial_Instances_Count() > 0 ? 1.0 : 0.0;
	}

	public double AdMob_RewardedInterstitial_Instances_Count() {
		return loadedRewardedInterstitialQueue.size();
	}

	// #endregion

	// #region App Open

	private Boolean isShowingAd = false;
	private Boolean isAppOpenAdEnabled = false;
	private AppOpenAd appOpenAdInstance = null;

	private long appOpenAdLoadTime = 0;
	private int appOpenAdOrientation = AppOpenAd.APP_OPEN_AD_ORIENTATION_LANDSCAPE;

	private String appOpenAdUnitId = "";

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

	public void onResume() {
		if (!isShowingAd) {
			showAppOpenAd();
		}
		isShowingAd = false;
	}

	private void loadAppOpenAd() {

		final String callingMethod = "__AdMob_AppOpenAd_Load";

		if (!isAppOpenAdEnabled)
			return;

		if (!validateInitialized(callingMethod))
			return;

		if (!validateAdId(appOpenAdUnitId, callingMethod))
			return;

		RunnerActivity.ViewHandler.post(() -> {

			final String adUnitId = appOpenAdUnitId;

			AppOpenAd.load(activity, appOpenAdUnitId, buildAdRequest(), appOpenAdOrientation,
					new AppOpenAdLoadCallback() {
						@Override
						public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {

							appOpenAdLoadTime = (new Date()).getTime();
							appOpenAdInstance = appOpenAd;

							if (triggerOnPaidEvent) {

								final AppOpenAd appOpenAdRef = appOpenAd;
								appOpenAd.setOnPaidEventListener(adValue -> {
									AdapterResponseInfo loadedAdapterResponseInfo = appOpenAdRef
											.getResponseInfo()
											.getLoadedAdapterResponseInfo();
									if(loadedAdapterResponseInfo == null) return;
									onPaidEventHandler(adValue, appOpenAdRef.getAdUnitId(), "AppOpen",
											loadedAdapterResponseInfo,
											appOpenAdRef.getResponseInfo().getMediationAdapterClassName());
								});
							}

							int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
							RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_AppOpenAd_OnLoaded");
							RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", adUnitId);
							RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
						}

						@Override
						public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
							appOpenAdInstance = null;

							int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
							RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_AppOpenAd_OnLoadFailed");
							RunnerJNILib.DsMapAddString(dsMapIndex, "unit_id", adUnitId);
							RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", loadAdError.getMessage());
							RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", loadAdError.getCode());
							RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
						}
					});
		});
	}

	private void showAppOpenAd() {
		if (!isAppOpenAdEnabled)
			return;

		final String callingMethod = "__AdMob_AppOpenAd_Show";

		if (!validateInitialized(callingMethod))
			return;

		if (!appOpenAdIsValid(4, callingMethod)) {
			appOpenAdInstance = null;
			loadAppOpenAd();
			return;
		}

		RunnerActivity.ViewHandler.post(() -> {
			if (appOpenAdInstance == null)
				return;

			appOpenAdInstance.setFullScreenContentCallback(new FullScreenContentCallback() {
				@Override
				public void onAdDismissedFullScreenContent() {

					appOpenAdInstance = null;
					int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
					RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_AppOpenAd_OnDismissed");
					RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					loadAppOpenAd();
				}

				@Override
				public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {

					appOpenAdInstance = null;
					int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
					RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_AppOpenAd_OnShowFailed");
					RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", adError.getMessage());
					RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", adError.getCode());
					RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					loadAppOpenAd();
				}

				@Override
				public void onAdShowedFullScreenContent() {
					appOpenAdInstance = null;
					int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
					RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_AppOpenAd_OnFullyShown");
					RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					loadAppOpenAd();
				}
			});

			isShowingAd = true;
			appOpenAdInstance.show(activity);
			appOpenAdInstance = null;
		});
	}

	private Boolean appOpenAdIsValid(int expirationTimeHours, String callingMethod) {

		if (appOpenAdInstance == null) {
			Log.i(LOG_TAG, callingMethod + " :: There is no app open ad loaded.");
			return false;
		}

		long dateDifference = (new Date()).getTime() - appOpenAdLoadTime;
		boolean expired = dateDifference >= (3600000L * expirationTimeHours);

		if (expired) {
			Log.i(LOG_TAG, callingMethod + " :: The loaded app open ad expired, reloading...");
			return false;
		}

		return true;
	}

	// #endregion

	// #region Targeting

	private boolean targetCOPPA = false;
	public double AdMob_Targeting_COPPA(double COPPA) {

		if (!validateNotInitialized("AdMob_Targeting_COPPA")) return ADMOB_ERROR_ILLEGAL_CALL;

		targetCOPPA = COPPA > 0.5;
		return 0;
	}

	private boolean targetUnderAge = false;
	public double AdMob_Targeting_UnderAge(double underAge) {

		if (!validateNotInitialized("AdMob_Targeting_UnderAge")) return ADMOB_ERROR_ILLEGAL_CALL;

		targetUnderAge = underAge >= 0.5;
		return 0;
	}

	private String maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_G;
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

	// #region Consent

	// EU Consent: https://developers.google.com/admob/android/eu-consent
	public ConsentInformation consentInformation;

	public void AdMob_Consent_RequestInfoUpdate(double mode) {
		ConsentRequestParameters.Builder builder = new ConsentRequestParameters.Builder();
		builder.setTagForUnderAgeOfConsent(targetUnderAge);

		if (mode != 3) {
			ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
					.setDebugGeography((int) mode)
					.addTestDeviceHashedId(getDeviceID())
					.build();

			builder.setConsentDebugSettings(debugSettings);
		}

		ConsentRequestParameters params = builder.build();

		consentInformation = UserMessagingPlatform.getConsentInformation(activity);
		consentInformation.requestConsentInfoUpdate(activity, params,
				() -> {
					int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
					RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Consent_OnRequestInfoUpdated");
					RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
				},
				formError -> {
					int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
					RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Consent_OnRequestInfoUpdateFailed");
					RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", formError.getMessage());
					RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", formError.getErrorCode());
					RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
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

	public ConsentForm consentFormInstance;

	public void AdMob_Consent_Load() {
		RunnerActivity.ViewHandler.post(() -> UserMessagingPlatform.loadConsentForm(activity,
				consentForm -> {
					consentFormInstance = consentForm;
					int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
					RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Consent_OnLoaded");
					RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
				},
				formError -> {
					int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
					RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Consent_OnLoadFailed");
					RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", formError.getMessage());
					RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", formError.getErrorCode());
					RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
				}));
	}

	public void AdMob_Consent_Show() {
		RunnerActivity.ViewHandler.post(() -> consentFormInstance.show(activity, formError -> {
			int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
			if (formError == null) {
				RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Consent_OnShown");
			} else {
				RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Consent_OnShowFailed");
				RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", formError.getMessage());
				RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", formError.getErrorCode());
			}
			RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
		}));
	}

	public void AdMob_Consent_Reset() {
		if (consentInformation != null)
			consentInformation.reset();
	}

	//#endregion

	// #region Settings

	public void AdMob_Settings_SetVolume(double value) {
		MobileAds.setAppVolume((float) value);
	}

	public void AdMob_Settings_SetMuted(double value) {
		MobileAds.setAppMuted(value >= 0.5);
	}

	//#endregion

	// #region Internals

	private AdRequest buildAdRequest() {
		AdRequest.Builder builder = new AdRequest.Builder();

		// As per Google's request
		builder.setRequestAgent("gmext-admob-" + RunnerJNILib.extGetVersion("AdMob"));

		// This is deprectated and shouldn't be used keeping it for the sake of compatibility
		if (nonPersonalizedAds) {
			Bundle extras = new Bundle();
			extras.putString("npa", "1");
			builder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
		}

		return builder.build();
	}

	private String getDeviceID() {
		String android_id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
		return Objects.requireNonNull(MD5(android_id)).toUpperCase();
	}

	// https://stackoverflow.com/questions/4846484/md5-hashing-in-android/21333739#21333739
	private String MD5(String md5) {
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

	//#endregion

	// #region Validations

	private Boolean validateNotInitialized(String callingMethod) {
		if (isInitialized) {
			Log.i(LOG_TAG, callingMethod + " :: Method cannot be called after initialization.");
		}
		return !isInitialized;
	}

	private Boolean validateInitialized(String callingMethod) {
		if (!isInitialized) {
			Log.i(LOG_TAG, callingMethod + " :: Extension was not initialized.");
		}
		return isInitialized;
	}

	private Boolean validateActiveBannerAd(String callingMethod) {
		if (bannerAdView == null) {
			Log.i(LOG_TAG, callingMethod + " :: There is no active banner ad.");
			return false;
		}
		return true;
	}

	private Boolean validateAdId(String adUnitId, String callingMethod) {
		if (adUnitId.isEmpty()) {
			Log.i(LOG_TAG, callingMethod + " :: Extension was not initialized.");
			return false;
		}
		return true;
	}

	private <T> Boolean validateLoadedAdsLimit(Queue<T> queue, int maxSize, String callingMethod) {
		if (queue.size() >= maxSize) {
			Log.i(LOG_TAG, callingMethod + " :: Maximum number of loaded ads reached.");
			return false;
		}
		return true;
	}

	private <T> Boolean validateAdLoaded(Queue<T> queue, String callingMethod) {
		if (queue.size() == 0) {
			Log.i(LOG_TAG, callingMethod + " :: There is no loaded ad in queue.");
			return false;
		}
		return true;
	}

	// #endregion
}
