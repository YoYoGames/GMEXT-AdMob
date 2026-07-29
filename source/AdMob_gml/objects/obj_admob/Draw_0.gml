/// @description Draw debug information

// This is for demo purposes and only draws debug information to the screen
draw_set_font(Font_YoYo_15);
draw_set_valign(fa_top);
draw_set_halign(fa_left);


// The function 'admob_consent_is_form_available()' will return either true or false depending
// on whether there is a consent form available or not on this device.
draw_text(x, y + 70, admob_consent_is_form_available() ? "Consent available" : "Consent unavailable");


// This checks whether the currently held interstitial handle (see
// Obj_AdMob_Interstitial) still points at a live, unshown ad. This function
// needs to return true before we can correctly show the interstitial ad.
var _interstitial_handle = Obj_AdMob_Interstitial.interstitial_handle;
var interstitial_isLoaded = !is_undefined(_interstitial_handle) && admob_interstitial_is_valid(_interstitial_handle);
draw_text(x, y + 105, "Interstitial_isLoaded: " + string(interstitial_isLoaded));

// This checks whether the currently held rewarded video handle (see
// Obj_AdMob_Rewarded) still points at a live, unshown ad. "Multi-loaded"
// counts the separate pool built up via Obj_AdMob_Rewarded_Multi_Load.
var _rewarded_video_handle = Obj_AdMob_Rewarded.rewarded_video_handle;
var rewardedVideoAd_isLoaded = !is_undefined(_rewarded_video_handle) && admob_rewarded_video_is_valid(_rewarded_video_handle);
draw_text(x, y + 140, "RewardedVideoAd_isLoaded: " + string(rewardedVideoAd_isLoaded) + " Multi-loaded: #" + string(array_length(Obj_AdMob_Rewarded.rewarded_video_multi_handles)));

// This checks whether the currently held rewarded interstitial handle (see
// Obj_AdMob_RewardedInterstitial) still points at a live, unshown ad.
var _rewarded_interstitial_handle = Obj_AdMob_RewardedInterstitial.rewarded_interstitial_handle;
var rewardedInterstitialAd_isLoaded = !is_undefined(_rewarded_interstitial_handle) && admob_rewarded_interstitial_is_valid(_rewarded_interstitial_handle);
draw_text(x, y + 175, "RewardedInterstitialAd_isLoaded: " + string(rewardedInterstitialAd_isLoaded));

// These set of function also allow to get the dimensions of the banner being displayed.
// If no banner is being displayed the functions return 0
// Note that the returned value is in display pixels and needs to be converted by the user
// according to the rendering target.
var room_pixels_w = admob_banner_get_width() *room_width/display_get_width();
var room_pixels_h = admob_banner_get_height() *room_height/display_get_height();
draw_text(70, y + 210, "Banner size: W=" + string(room_pixels_w) + ", H=" + string(room_pixels_h));
