/// @description Handle Orientation Changes

alarm[0] = room_speed;

if (orientation != display_get_orientation())
{
    show_debug_message("[AdMob] Orientation Changed!!!!");
	
    orientation = display_get_orientation();
	
    if (!is_undefined(Obj_AdMob_Interstitial.interstitial_handle))
    {
        admob_interstitial_dispose(Obj_AdMob_Interstitial.interstitial_handle);
        Obj_AdMob_Interstitial.interstitial_handle = undefined;

        admob_interstitial_load(function(_result, _handle)
			{
			    show_debug_message($"Interstitial Load: success={_result.success}, error={_result.error_message}");

			    if (_result.success)
			        Obj_AdMob_Interstitial.interstitial_handle = _handle;
			});
    }

    if (!is_undefined(Obj_AdMob_RewardedInterstitial.rewarded_interstitial_handle))
    {
        admob_rewarded_interstitial_dispose(Obj_AdMob_RewardedInterstitial.rewarded_interstitial_handle);
        Obj_AdMob_RewardedInterstitial.rewarded_interstitial_handle = undefined;

        admob_rewarded_interstitial_load(function(_result, _handle)
			{
			    show_debug_message($"Rewarded Interstitial Load: success={_result.success}, error={_result.error_message}");

			    if (_result.success)
			        Obj_AdMob_RewardedInterstitial.rewarded_interstitial_handle = _handle;
			});
    }

    if (!is_undefined(Obj_AdMob_Rewarded.rewarded_video_handle))
    {
        admob_rewarded_video_dispose(Obj_AdMob_Rewarded.rewarded_video_handle);
        Obj_AdMob_Rewarded.rewarded_video_handle = undefined;

        admob_rewarded_video_load(function(_result, _handle)
			{
			    show_debug_message($"Rewarded Video Load: success={_result.success}, error={_result.error_message}");

			    if (_result.success)
			        Obj_AdMob_Rewarded.rewarded_video_handle = _handle;
			});
    }

    admob_app_open_ad_disable();

    switch (orientation)
    {
        case display_landscape:
        case display_landscape_flipped:
            admob_app_open_ad_enable(
                display_landscape,
				function(_result, _type = undefined)
				{
				    show_debug_message($"AppOpen Landscape: success={_result.success}, type={_type}, error={_result.error_message}");
				}
            );
        break;

        case display_portrait:
        case display_portrait_flipped:
            admob_app_open_ad_enable(
                display_portrait,
				function(_result, _type = undefined)
				{
				    show_debug_message($"AppOpen Portrait: success={_result.success}, type={_type}, error={_result.error_message}");
				}
            );
        break;
    }
}
