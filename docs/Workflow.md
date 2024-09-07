@title Workflow

# Workflow

This is the recommended workflow for using AdMob extension functions calls both on Android and iOS.

## iOS

* Import the **AppTrackingTransparency** extension from marketplace. This is required for personalized ads.
* Request app tracking in an initialization room before initializing AdMob.

## Android & iOS

* Set desired configurations: ${function.AdMob_SetTestDeviceId}, ${function.AdMob_Targeting_COPPA}, ${function.AdMob_Targeting_UnderAge} and ${function.AdMob_Targeting_MaxAdContentRating}.
* Initialize the extension: ${function.AdMob_Initialize}
* Wait for callback (success/failure)
  * Handle failure (don’t continue any further)
* Request for consent information update: ${function.AdMob_Consent_RequestInfoUpdate}
* Check status using ${function.AdMob_Consent_GetStatus}
* Proceed to loading and showing ${function.AdMob_Consent_Load} / ${function.AdMob_Consent_Show}
* Now you can finally init and load your ads using the function pairs:
  * ${function.AdMob_Banner_Init} / ${function.AdMob_Banner_Create}
  * ${function.AdMob_Interstitial_Init} / ${function.AdMob_Interstitial_Load}
  * ${function.AdMob_RewardedVideo_Init} / ${function.AdMob_RewardedVideo_Load}
  * ${function.AdMob_RewardedInterstitial_Init} / ${function.AdMob_RewardedInterstitial_Load}
* After loading is successful you can show your ads with the corresponding show function.
