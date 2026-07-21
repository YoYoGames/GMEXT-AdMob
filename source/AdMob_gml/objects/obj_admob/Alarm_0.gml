/// @description Handle Orientation Changes

alarm[0] = room_speed;

if (orientation != display_get_orientation())
{
    show_debug_message("[AdMob] Orientation Changed!!!!");
	
    orientation = display_get_orientation();
	
    var _interstitial_count = admob_interstitial_instances_count();

    admob_interstitial_free_loaded_instances(_interstitial_count);

    repeat (_interstitial_count)
        admob_interstitial_load(function(data)
			{
			    show_debug_message($"Interstitial Load: {json_stringify(data)}");
			});

    var _rewarded_interstitial_count = admob_rewarded_interstitial_instances_count();

    admob_rewarded_interstitial_free_loaded_instances(_rewarded_interstitial_count);

    repeat (_rewarded_interstitial_count)
        admob_rewarded_interstitial_load(function(data)
			{
			    show_debug_message($"Rewarded Interstitial Load: {json_stringify(data)}");
			});

    var _rewarded_video_count = admob_rewarded_video_instances_count();

    admob_rewarded_video_free_loaded_instances(_rewarded_video_count);

    repeat (_rewarded_video_count)
        admob_rewarded_video_load(function (data)
			{
			    show_debug_message($"Rewarded Video: {json_stringify(data)}");
			});

    admob_app_open_ad_disable();

    switch (orientation)
    {
        case display_landscape:
        case display_landscape_flipped:
            admob_app_open_ad_enable(
                display_landscape,
				function(data)
				{
				    show_debug_message("AppOpen Landscape: {json_stringify(data)}");
				}
            );
        break;

        case display_portrait:
        case display_portrait_flipped:
            admob_app_open_ad_enable(
                display_portrait,
				function admob_log(data)
				{
				    show_debug_message($"AppOpen Portail: {json_stringify(data)}");
				}
            );
        break;
    }
}
