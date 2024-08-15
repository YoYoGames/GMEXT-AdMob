@title Workflow

# Workflow

This is the recommended workflow for using AdMob extension functions calls both on Android and iOS.

## iOS

* Import **AppTrackingTransparency** extension from marketplace. This is required for personalized ads.
* Request app tracking on an initialization room before initializing AdMob.

## Android & iOS

* Set desired configurations: [AdMob_SetTestDeviceId](general#admob_settestdeviceid), [AdMob_Targeting_COPPA](targeting#admob_targeting_coppa), [AdMob_Targeting_UnderAge](targeting#admob_targeting_underage) and [AdMob_Targeting_MaxAdContentRating](targeting#admob_targeting_maxadcontentrating).
* Initialize the extension: [AdMob_Initialize](general#admob_initialize)
* Wait for callback (success/failure)
  * Handle failure (don’t continue any further)
* Request for consent information update: [AdMob_Consent_RequestInfoUpdate](consent#admob_consent_requestinfoupdate)
* Check status using [AdMob_Consent_GetStatus](consent#admob_consent_getstatus)
* Proceed to loading and showing [AdMob_Consent_Load](consent#admob_consent_load) / [AdMob_Consent_Show](consent#admob_consent_show)
* Now you can finally init and load your ads using the function pairs:
  * [AdMob_Banner_Init](banner#admob_banner_init) / [AdMob_Banner_Create](banner#admob_banner_create)
  * [AdMob_Interstitial_Init](interstitial#admob_interstitial_init) / [AdMob_Interstitial_Load](interstitial#admob_interstitial_load)
  * [AdMob_RewardedVideo_Init](reward_video#admob_rewardedvideo_init) / [AdMob_RewardedVideo_Load](reward_video#admob_rewardedvideo_load)
  * [AdMob_RewardedInterstitial_Init](reward_interstitial#admob_rewardedinterstitial_init) / [AdMob_RewardedInterstitial_Load](reward_interstitial#admob_rewardedinterstitial_load)
* After loading is successful you can show your ads with the corresponding show function.
