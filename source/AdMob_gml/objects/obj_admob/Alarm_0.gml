/// @description Handle Orientation Changes

alarm[0] = room_speed;

if (orientation != display_get_orientation())
{
    show_debug_message("[AdMob] Orientation Changed!!!!");

    orientation = display_get_orientation();

    var _interstitial_count =
        admob_interstitial_instances_count();

    admob_interstitial_free_loaded_instances(_interstitial_count);

    repeat (_interstitial_count)
        admob_interstitial_load(Obj_AdMob.admob_log);

    var _rewarded_interstitial_count =
        admob_rewarded_interstitial_instances_count();

    admob_rewarded_interstitial_free_loaded_instances(
        _rewarded_interstitial_count
    );

    repeat (_rewarded_interstitial_count)
        admob_rewarded_interstitial_load(Obj_AdMob.admob_log);

    var _rewarded_video_count =
        admob_rewarded_video_instances_count();

    admob_rewarded_video_free_loaded_instances(
        _rewarded_video_count
    );

    repeat (_rewarded_video_count)
        admob_rewarded_video_load(Obj_AdMob.admob_log);

    admob_app_open_ad_disable();

    switch (orientation)
    {
        case display_landscape:
        case display_landscape_flipped:
            admob_app_open_ad_enable(
                display_landscape,
                Obj_AdMob.admob_log
            );
        break;

        case display_portrait:
        case display_portrait_flipped:
            admob_app_open_ad_enable(
                display_portrait,
                Obj_AdMob.admob_log
            );
        break;
    }
}
