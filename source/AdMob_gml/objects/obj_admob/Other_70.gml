/// @description Legacy Social Async event

// The GMAdMob Extension Generator port does not use Social Async events.
// Async AdMob operations now receive their own callback with a struct payload.
//
// Example:
//
// admob_interstitial_load(function(data) {
//     if (data.event_type == AdMobInterstitialCallbackEvent.Loaded) {
//         // loaded
//     }
// });
//
// This event is intentionally left as a no-op because .yy files were not changed.
