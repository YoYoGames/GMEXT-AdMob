# Workflow

This is the recommended workflow for using AdMob extension functions calls both on Android and iOS.

## iOS

* Import **AppTrackingTransparency** extension from marketplace. This is required for personalized ads.
* Request app tracking on an initialization room before initializing AdMob.

## Android & iOS

* Set desired configurations: [AdMob_SetTestDeviceId](General#admob_settestdeviceid), [AdMob_Targeting_COPPA](Targeting#admob_targeting_coppa), [AdMob_Targeting_UnderAge](Targeting#admob_targeting_underage) and [AdMob_Targeting_MaxAdContentRating](Targeting#admob_targeting_maxadcontentrating).
* Initialize the extension: [AdMob_Initialize](General#admob_initialize)
* Wait for callback (success/failure)
  * Handle failure (don’t continue any further)
* Request for consent information update: [AdMob_Consent_RequestInfoUpdate](Consent#admob_consent_requestinfoupdate-updated)
* Check status using [AdMob_Consent_GetStatus](Consent#admob_consent_getstatus)
* Proceed to loading and showing [AdMob_Consent_Load](Consent#admob_consent_load-updated) / [AdMob_Consent_Show](Consent#admob_consent_show)
* Now you can finally init and load your ads using the function pairs:
  * [AdMob_Banner_Init](Banner#admob_banner_init) / [AdMob_Banner_Create](Banner#admob_banner_create-updated)
  * [AdMob_Interstitial_Init](Interstitial#admob_interstitial_init) / [AdMob_Interstitial_Load](Interstitial#admob_interstitial_load-updated)
  * [AdMob_RewardedVideo_Init](Rewarded%20Video#AdMob_RewardedVideo_Init) / [AdMob_RewardedVideo_Load](Rewarded%20Video.md#AdMob_RewardedVideo_Load)
  * [AdMob_RewardedInterstitial_Init](Rewarded%20Interstitial#AdMob_RewardedInterstitial_Init) / [AdMob_RewardedInterstitial_Load](Rewarded%20Interstitial#AdMob_RewardedInterstitial_Load)
* After loading is successful you can show your ads with the corresponding show function.
