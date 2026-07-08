/// @description Legacy Social Async event

// The GMAdMob Extension Generator port does not use Social Async events.
// Async AdMob operations now receive their own callback, for example:
//
// admob_interstitial_load(function(_data_json) {
//     var _data = json_parse(_data_json);
//
//     if (_data.event_type == AdMobInterstitialCallbackEvent.Loaded) {
//         // loaded
//     }
// });
//
// This event is intentionally left as a no-op because .yy files were not changed.
