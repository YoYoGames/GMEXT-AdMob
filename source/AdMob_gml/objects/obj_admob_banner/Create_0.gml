/// @description Initialize variables

banner_type = AdMobBannerSize.AnchoredAdaptive;
// banner_type = AdMobBannerSize.Banner;
// banner_type = AdMobBannerSize.SmartBanner;
// banner_type = AdMobBannerSize.FullBanner;
// banner_type = AdMobBannerSize.LargeBanner;
// banner_type = AdMobBannerSize.MediumRectangle;
// banner_type = AdMobBannerSize.Leaderboard;

event_inherited();
text = "Banner";

displayHeight = display_get_height();
alignment = AdMobBannerAlignment.Center;
pressed = false;
bottom = false;
