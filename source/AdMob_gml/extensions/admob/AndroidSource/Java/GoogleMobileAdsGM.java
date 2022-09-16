package ${YYAndroidPackageName};

import ${YYAndroidPackageName}.R;
import com.yoyogames.runner.RunnerJNILib;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import android.widget.AbsoluteLayout;
import android.view.ViewGroup;
import android.widget.Toast;
import java.lang.Exception;
import java.net.URL;
import android.provider.Settings;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.mediation.MediationAdapter;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.RequestConfiguration;

import com.google.ads.mediation.admob.AdMobAdapter;

import com.google.android.ump.*;//UserMessagingPlatform;
import androidx.annotation.Nullable;

import android.widget.RelativeLayout;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.Gravity;
import androidx.annotation.NonNull;
import android.widget.FrameLayout;

import android.util.Log;

import java.util.List;

import android.util.DisplayMetrics;
import android.view.Display;

public class GoogleMobileAdsGM extends RunnerSocial {
	private static final int EVENT_OTHER_SOCIAL = 70;

	public static Activity activity;

	public GoogleMobileAdsGM() {
		activity = RunnerActivity.CurrentActivity;
	}

	//////////////////////////////////////////////////// GoogleMobileAds
	//////////////////////////////////////////////////// ////////////////////////////////////////////////////

	public void AdMob_Initialize() {
		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {
				MobileAds.setRequestConfiguration(requestConfigurationBuilder());

				try {
					// MobileAds.initialize(activity);
					MobileAds.initialize(activity, new OnInitializationCompleteListener() {
						@Override
						public void onInitializationComplete(InitializationStatus initializationStatus) {
							Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
							for (String adapterClass : statusMap.keySet()) {
								AdapterStatus status = statusMap.get(adapterClass);
								Log.d("yoyo", String.format("Adapter name: %s, Description: %s, Latency: %d",
										adapterClass, status.getDescription(), status.getLatency()));
							}

							int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
							RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_OnInitialized");
							RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);

							// Initialize ad types using extension options
							AdMob_Banner_Init(RunnerJNILib.extOptGetString("AdMob", "Android_BANNER"));
							AdMob_Interstitial_Init(RunnerJNILib.extOptGetString("AdMob", "Android_INTERSTITIAL"));
							AdMob_RewardedVideo_Init(RunnerJNILib.extOptGetString("AdMob", "Android_REWARDED"));
							AdMob_RewardedInterstitial_Init(RunnerJNILib.extOptGetString("AdMob", "Android_REWARDED_INTERSTITIAL"));
						}
					});
				} catch (Exception e) {
					Log.i("yoyo", "GoogleMobileAds Init Error: " + e.toString());
					Log.i("yoyo", e.toString());
				}
			}
		});
	}

	public String testDeviceID;

	public void AdMob_SetTestDeviceId() {
		testID_on = true;
	}

	///// BANNER
	///// //////////////////////////////////////////////////////////////////////////////////////

	private AdView adView = null;
	private String bannerID;
	private RelativeLayout layout;

	public void AdMob_Banner_Init(String adUnitId) {
		bannerID = adUnitId;
	}

	public void AdMob_Banner_Create(final double size, final double bottom) {
		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {
				if (adView != null) {
					layout.removeView(adView);
					adView.destroy();
					adView = null;

					final ViewGroup rootView = activity.findViewById(android.R.id.content);
					rootView.removeView(layout);
					// layout.destroy();
					layout = null;
				}

				layout = new RelativeLayout(activity);

				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.CENTER_HORIZONTAL);
				if (bottom > 0.5)
					params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				else
					params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

				adView = new AdView(activity);

				layout.addView((View) adView, params);

				final ViewGroup rootView = activity.findViewById(android.R.id.content);
				rootView.addView((View) layout);

				adView.setAdListener(new AdListener() {
					@Override
					public void onAdLoaded() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Banner_OnLoaded");
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdFailedToLoad(LoadAdError loadAdError) {
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

				bannerSize = banner_size(size);
				adView.setAdSize(bannerSize);
				adView.setAdUnitId(bannerID);
				adView.requestLayout();
				adView.setVisibility(View.VISIBLE);

				adView.loadAd(AdMob_AdRequest());
			}
		});
	}

	private AdSize bannerSize = null;
	
	public double AdMob_Banner_GetWidth() {
		if (bannerSize == null)
			return 0;

		int w = bannerSize.getWidthInPixels(RunnerJNILib.ms_context);
		return w;
	}

	public double AdMob_Banner_GetHeight() {
		if (bannerSize == null)
			return 0;

		int h = bannerSize.getHeightInPixels(RunnerJNILib.ms_context);
		if (bannerSize == AdSize.SMART_BANNER) {
			DisplayMetrics displayMetrics = (RunnerJNILib.ms_context).getResources().getDisplayMetrics();

			int screenHeightInDP = Math.round(displayMetrics.heightPixels / displayMetrics.density);
			int density = Math.round(displayMetrics.density);
			if (screenHeightInDP < 400)
				h = 32 * density;
			else if (screenHeightInDP <= 720)
				h = 50 * density;
			else
				h = 90 * density;
		}
		return h;
	}

	public void AdMob_Banner_Move(final double bottom) {
		if (adView != null) {
			RunnerActivity.ViewHandler.post(new Runnable() {
				public void run() {
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					params.addRule(RelativeLayout.CENTER_HORIZONTAL);
					if (bottom > 0.5)
						params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					else
						params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

					adView.setLayoutParams(params);
				}
			});
		}
	}

	public void AdMob_Banner_Show() {
		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {
				if (adView != null)
					adView.setVisibility(View.VISIBLE);
			}
		});
	}

	public void AdMob_Banner_Hide() {
		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {
				if (adView != null)
					adView.setVisibility(View.GONE);
			}
		});
	}

	public void AdMob_Banner_Remove() {
		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {
				if (adView != null) {
					AdSize bannerSize = null;

					layout.removeView(adView);
					adView.destroy();
					adView = null;

					final ViewGroup rootView = activity.findViewById(android.R.id.content);
					rootView.removeView(layout);
					// layout.destroy();
					layout = null;
				}
			}
		});
	}

	private AdSize banner_size(double size) {
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

	///// INTERSTITIAL
	///// ////////////////////////////////////////////////////////////////////////////////

	private InterstitialAd mInterstitialAd = null;
	private String mInterstitialID = null;

	public void AdMob_Interstitial_Init(String adUnitId) {
		mInterstitialID = adUnitId;
	}

	public void AdMob_Interstitial_Load() {
		if (mInterstitialAd == null)
			RunnerActivity.ViewHandler.post(new Runnable() {
				public void run() {
					InterstitialAd.load(activity, mInterstitialID, AdMob_AdRequest(), new InterstitialAdLoadCallback() {
						@Override
						public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
							mInterstitialAd = interstitialAd;

							int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
							RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Interstitial_OnLoaded");
							RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
						}

						@Override
						public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
							mInterstitialAd = null;

							int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
							RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Interstitial_OnLoadFailed");
							RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", loadAdError.getMessage());
							RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", loadAdError.getCode());
							RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
						}
					});
				}
			});
	}

	public void AdMob_Interstitial_Show() {
		if (mInterstitialAd == null)
			return;

		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {
				mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
					@Override
					public void onAdDismissedFullScreenContent() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Interstitial_OnDismissed");
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdFailedToShowFullScreenContent(AdError adError) {
						mInterstitialAd = null;
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Interstitial_OnShowFailed");
						RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", adError.getMessage());
						RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", adError.getCode());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdShowedFullScreenContent() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Interstitial_OnFullyShown");
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}
				});

				mInterstitialAd.show(activity);
				mInterstitialAd = null;
			}
		});
	}

	public double AdMob_Interstitial_IsLoaded() {
		if (mInterstitialAd == null)
			return 0.0;

		return 1.0;
	}

	///// REWARDED VIDEO
	///// //////////////////////////////////////////////////////////////////////////////

	public RewardedAd mRewardedAd = null;
	public String mRewardedAdID = null;

	public void AdMob_RewardedVideo_Init(String adUnitId) {
		mRewardedAdID = adUnitId;
	}

	public void AdMob_RewardedVideo_Load() {
		if (mRewardedAd == null)
			RunnerActivity.ViewHandler.post(new Runnable() {
				public void run() {
					RewardedAd.load(activity, mRewardedAdID, AdMob_AdRequest(), new RewardedAdLoadCallback() {
						@Override
						public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
							mRewardedAd = null;

							int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
							RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedVideo_OnLoadFailed");
							RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", loadAdError.getMessage());
							RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", loadAdError.getCode());
							RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
						}

						@Override
						public void onAdLoaded(@NonNull RewardedAd rewardedAd_) {
							mRewardedAd = rewardedAd_;

							int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
							RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedVideo_OnLoaded");
							RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
						}
					});
				}
			});
	}

	public void AdMob_RewardedVideo_Show() {
		if (mRewardedAd == null)
			return;

		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {
				mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
					@Override
					public void onAdDismissedFullScreenContent() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedVideo_OnDismissed");
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdFailedToShowFullScreenContent(AdError adError) {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedVideo_OnShowFailed");
						RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", adError.getMessage());
						RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", adError.getCode());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdShowedFullScreenContent() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedVideo_OnFullyShown");
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}
				});

				mRewardedAd.show(activity, new OnUserEarnedRewardListener() {
					@Override
					public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
						int rewardAmount = rewardItem.getAmount();
						String rewardType = rewardItem.getType();

						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedVideo_OnReward");
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}
				});
				mRewardedAd = null;
			}
		});
	}

	public double AdMob_RewardedVideo_IsLoaded() {
		if (mRewardedAd == null)
			return 0.0;

		return 1.0;
	}

	///// REWARDED INTESTITIAL
	///// ////////////////////////////////////////////////////////////////////////

	public RewardedInterstitialAd mRewardedInterstitialAd = null;
	public String mRewardedInterstitialAdID = null;

	public void AdMob_RewardedInterstitial_Init(String adUnitId) {
		mRewardedInterstitialAdID = adUnitId;
	}

	public void AdMob_RewardedInterstitial_Load() {
		if (mRewardedInterstitialAd == null)
			RunnerActivity.ViewHandler.post(new Runnable() {
				public void run() {
					RewardedInterstitialAd.load(activity, mRewardedInterstitialAdID, AdMob_AdRequest(),
							new RewardedInterstitialAdLoadCallback() {
								@Override
								public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
									mRewardedInterstitialAd = null;

									int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
									RunnerJNILib.DsMapAddString(dsMapIndex, "type",
											"AdMob_RewardedInterstitial_OnLoadFailed");
									RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", loadAdError.getMessage());
									RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", loadAdError.getCode());
									RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
								}

								@Override
								public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedInterstitialAd_) {
									mRewardedInterstitialAd = rewardedInterstitialAd_;

									int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
									RunnerJNILib.DsMapAddString(dsMapIndex, "type",
											"AdMob_RewardedInterstitial_OnLoaded");
									RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
								}
							});
				}
			});
	}

	public void AdMob_RewardedInterstitial_Show() {
		if (mRewardedInterstitialAd == null)
			return;

		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {
				mRewardedInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
					@Override
					public void onAdDismissedFullScreenContent() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedInterstitial_OnDismissed");
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdFailedToShowFullScreenContent(AdError adError) {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedInterstitial_OnShowFailed");
						RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", adError.getMessage());
						RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", adError.getCode());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}

					@Override
					public void onAdShowedFullScreenContent() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedInterstitial_OnFullyShown");
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}
				});

				mRewardedInterstitialAd.show(activity, new OnUserEarnedRewardListener() {
					@Override
					public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
						int rewardAmount = rewardItem.getAmount();
						String rewardType = rewardItem.getType();

						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_RewardedInterstitial_OnReward");
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}
				});
				mRewardedInterstitialAd = null;
			}
		});
	}

	public double AdMob_RewardedInterstitial_IsLoaded() {
		if (mRewardedInterstitialAd == null)
			return 0.0;

		return 1.0;
	}

	///// TARGETING
	///// ///////////////////////////////////////////////////////////////////////////////////

	public void AdMob_Targeting_COPPA(double COPPA) {
		targetCOPPA = COPPA > 0.5;
	}

	public void AdMob_Targeting_UnderAge(double underAge) {
		targetUnderAge = underAge >= 0.5;
	}

	public void AdMob_Targeting_MaxAdContentRating(double contentRating) {
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
	}

	private boolean testID_on = false;
	private boolean targetCOPPA = false;
	private boolean targetUnderAge = false;
	private String maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_G;

	private RequestConfiguration requestConfigurationBuilder() {
		RequestConfiguration.Builder mRequestConfiguration = new RequestConfiguration.Builder();

		if (testID_on) {
			List<String> testDeviceIds = Arrays.asList(getDeviceID());
			mRequestConfiguration = mRequestConfiguration.setTestDeviceIds(testDeviceIds);
		}

		if (targetCOPPA)
			mRequestConfiguration = mRequestConfiguration
					.setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE);

		if (targetUnderAge)
			mRequestConfiguration = mRequestConfiguration
					.setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE);

		return mRequestConfiguration.build();
	}

	///// UTILS
	///// ///////////////////////////////////////////////////////////////////////////////////////

	public void AdMob_NonPersonalizedAds_Set(double value) {
		NPA = value >= 0.5;
	}

	public boolean NPA = false;

	private AdRequest AdMob_AdRequest() {
		AdRequest.Builder builder = new AdRequest.Builder();

		if (NPA) {
			Bundle extras = new Bundle();
			extras.putString("npa", "1");
			builder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
		}

		return builder.build();
	}

	private String getDeviceID() {
		String android_id = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
		String deviceId = MD5(android_id).toUpperCase();
		return deviceId;
	}

	// https://stackoverflow.com/questions/4846484/md5-hashing-in-android/21333739#21333739
	private String MD5(String md5) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes("UTF-8"));
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

	///// CONSENT
	///// /////////////////////////////////////////////////////////////////////////////////////

	// EU Consent: https://developers.google.com/admob/android/eu-consent
	public ConsentInformation consentInformation;

	public void AdMob_Consent_RequestInfoUpdate(double mode) {
		ConsentRequestParameters.Builder builder = new ConsentRequestParameters.Builder();
		builder.setTagForUnderAgeOfConsent(targetUnderAge);

		if (mode != 3) {
			ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
					.setDebugGeography((int) mode)// ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
					.addTestDeviceHashedId(getDeviceID())
					.build();

			builder = builder.setConsentDebugSettings(debugSettings);
		}

		ConsentRequestParameters params = builder.build();

		consentInformation = UserMessagingPlatform.getConsentInformation(activity);
		consentInformation.requestConsentInfoUpdate(activity, params,
				new ConsentInformation.OnConsentInfoUpdateSuccessListener() {
					@Override
					public void onConsentInfoUpdateSuccess() {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Consent_OnRequestInfoUpdated");
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}
				},
				new ConsentInformation.OnConsentInfoUpdateFailureListener() {
					@Override
					public void onConsentInfoUpdateFailure(FormError formError) {
						int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
						RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Consent_OnRequestInfoUpdateFailed");
						RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", formError.getMessage());
						RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", formError.getErrorCode());
						RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
					}
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

	public ConsentForm consentForm;

	public void AdMob_Consent_Load() {
		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {
				UserMessagingPlatform.loadConsentForm(activity,
						new UserMessagingPlatform.OnConsentFormLoadSuccessListener() {
							@Override
							public void onConsentFormLoadSuccess(ConsentForm consent_form) {
								consentForm = consent_form;
								int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
								RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Consent_OnLoaded");
								RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
							}
						},
						new UserMessagingPlatform.OnConsentFormLoadFailureListener() {
							@Override
							public void onConsentFormLoadFailure(FormError formError) {
								int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
								RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Consent_OnLoadFailed");
								RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", formError.getMessage());
								RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", formError.getErrorCode());
								RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
							}
						});
			}
		});
	}

	public void AdMob_Consent_Show() {
		RunnerActivity.ViewHandler.post(new Runnable() {
			public void run() {
				consentForm.show(activity, new ConsentForm.OnConsentFormDismissedListener() {
					@Override
					public void onConsentFormDismissed(@Nullable FormError formError) {
						if (formError == null) {
							int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
							RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Consent_OnShown");
							RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
						} else {
							int dsMapIndex = RunnerJNILib.jCreateDsMap(null, null, null);
							RunnerJNILib.DsMapAddString(dsMapIndex, "type", "AdMob_Consent_OnShowFailed");
							RunnerJNILib.DsMapAddString(dsMapIndex, "errorMessage", formError.getMessage());
							RunnerJNILib.DsMapAddDouble(dsMapIndex, "errorCode", formError.getErrorCode());
							RunnerJNILib.CreateAsynEventWithDSMap(dsMapIndex, EVENT_OTHER_SOCIAL);
						}
					}
				});
			}
		});
	}

	public void AdMob_Consent_Reset() {
		if (consentInformation != null)
			consentInformation.reset();
	}

	///// SETTINGS
	///// ////////////////////////////////////////////////////////////////////////////////////

	public void AdMob_Settings_SetVolume(double value) {
		MobileAds.setAppVolume((float) value);
		AdsSoundReLoad();
	}

	public void AdMob_Settings_SetMuted(double value) {
		MobileAds.setAppMuted(value >= 0.5);
		AdsSoundReLoad();
	}

	private void AdsSoundReLoad() {
		if (mInterstitialID != null) {
			mInterstitialAd = null;
			AdMob_Interstitial_Load();
		}

		if (mRewardedAdID != null) {
			mRewardedAd = null;
			AdMob_RewardedVideo_Load();
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
				Log.e("yoyo", "hasConsentFor: denied for purpose #" + p);
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
				Log.e("yoyo", "hasConsentOrLegitimateInterestFor: denied for #" + p);
				return false;
			}
		}
		return true;
	}

}