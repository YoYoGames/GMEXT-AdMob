/// @description Initialize variables


#macro admob_consent_enabled true



#macro admob_handle_orientation_changes false
if (admob_handle_orientation_changes)
{
    orientation = display_get_orientation();
    alarm[0] = room_speed;
}


// Development only. Call before admob_initialize().
admob_set_test_device_id();

function start_ads_init_flow()
{
	show_debug_message("admob_initialize GM");
	var _init_result = admob_initialize(function(data)
	        {
	            show_debug_message($"admob_initialize callback: {json_stringify(data)}");

	            if (!data.success)
	            {
	                show_debug_message($"AdMob initialize failed: {data.code} {data.error_message}");
	                return;
	            }
			
				// Load all formats:
			
			    admob_interstitial_load(
			        function(data)
			        {
			            show_debug_message($"admob_interstitial_load: {json_stringify(data)}");
			        }
			    );

			    admob_rewarded_video_load(
			        function(data)
			        {
			            show_debug_message($"admob_rewarded_video_load: {json_stringify(data)}");
			        }
			    );

			    admob_rewarded_interstitial_load(
			        function(data)
			        {
			            show_debug_message($"admob_rewarded_interstitial_load: {json_stringify(data)}");
			        }
			    );

			    admob_app_open_ad_enable(
			        display_landscape,
			        function(data)
			        {
			            show_debug_message($"admob_app_open_ad_enable: {json_stringify(data)}");
			        }
			    );
	        }
	    );

	if (_init_result != AdMobError.Ok)
	{
	    show_debug_message($"AdMob initialize call failed immediately: {_init_result}");
	}



	// Ad unit IDs are loaded from extension options by the native extension.
	// Override them here only if needed:
	// NOTE: Shold be after admob_initialize()
	// admob_banner_set_ad_unit(BANNER_ID);
	// admob_interstitial_set_ad_unit(INTERSTITIAL_ID);
	// admob_rewarded_video_set_ad_unit(REWARDED_ID);
	// admob_rewarded_interstitial_set_ad_unit(REWARDED_INTERSTITIAL_ID);




	// Optional paid event stream. This is now unit/void.
	//admob_events_on_paid_event(true, function(data)
	//	{
		//    if (data.event_type != AdMobPaidEventCallbackEvent.Paid)
		//    {
		//        return;
		//    }

		//    var _unit_id =
		//        variable_struct_exists(data, "unit_id")
		//            ? data.unit_id
		//            : "";

		//    var _ad_type =
		//        variable_struct_exists(data, "ad_type")
		//            ? data.ad_type
		//            : "";

		//    var _micros =
		//        variable_struct_exists(data, "micros")
		//            ? data.micros
		//            : 0;

		//    var _currency_code =
		//        variable_struct_exists(data, "currency_code")
		//            ? data.currency_code
		//            : "";

		//    var _precision =
		//        variable_struct_exists(data, "precision")
		//            ? data.precision
		//            : 0;

		//    var _ad_source_name =
		//        variable_struct_exists(data, "ad_source_name")
		//            ? data.ad_source_name
		//            : "";

		//    var _ad_source_id =
		//        variable_struct_exists(data, "ad_source_id")
		//            ? data.ad_source_id
		//            : "";

		//    var _ad_source_instance_name =
		//        variable_struct_exists(data, "ad_source_instance_name")
		//            ? data.ad_source_instance_name
		//            : "";

		//    var _ad_source_instance_id =
		//        variable_struct_exists(data, "ad_source_instance_id")
		//            ? data.ad_source_instance_id
		//            : "";

		//    var _mediation_adapter_class_name =
		//        variable_struct_exists(data, "mediation_adapter_class_name")
		//            ? data.mediation_adapter_class_name
		//            : "";

		//    show_debug_message(
		//        $"Paid event: {_ad_type} {_micros} {_currency_code}"
		//    );
		//});
}




if(admob_consent_enabled)
{
	instance_create_depth(x,y,0,Obj_AdMob_Consent,{owner: id})
}
else
	start_ads_init_flow()


