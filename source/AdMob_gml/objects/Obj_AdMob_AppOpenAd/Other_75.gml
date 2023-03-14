
show_debug_message("system async: " + json_encode(async_load))

if(async_load[?"event_type"] == "onResume")
{
	if(!AdMob_IsShowingAd()) AdMob_AppOpenAd_Show()
}
