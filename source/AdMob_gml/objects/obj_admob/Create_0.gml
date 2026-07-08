/// @description Initialize variables

#macro admob_handle_orientation_changes false

if (admob_handle_orientation_changes)
{
    orientation = display_get_orientation();
    alarm[0] = room_speed;
}

status = "UNKNOWN";
type = "UNKNOWN";

function admob_log(_data_json)
{
    var _data = json_parse(_data_json);
    show_debug_message("AdMob callback: " + json_stringify(_data));
}

function showDebugInfo()
{
    var _consent_type = admob_consent_get_type();

    switch (_consent_type)
    {
        case AdMobConsentType.Personalized:
            show_debug_message("GoogleMobileAds ConsentType: PERSONALIZED");
        break;

        case AdMobConsentType.NonPersonalized:
            show_debug_message("GoogleMobileAds ConsentType: NON_PERSONALIZED");
        break;

        case AdMobConsentType.Declined:
            show_debug_message("GoogleMobileAds ConsentType: DECLINED");
        break;

        case AdMobConsentType.Unknown:
            show_debug_message("GoogleMobileAds ConsentType: UNKNOWN");
        break;
    }
}

function updateConsentStrings()
{
    switch (admob_consent_get_status())
    {
        case AdMobConsentStatus.Unknown:
            status = "UNKNOWN";
        break;

        case AdMobConsentStatus.NotRequired:
            status = "NOT_REQUIRED";
        break;

        case AdMobConsentStatus.Required:
            status = "REQUIRED";
        break;

        case AdMobConsentStatus.Obtained:
            status = "OBTAINED";
        break;
    }

    switch (admob_consent_get_type())
    {
        case AdMobConsentType.Unknown:
            type = "UNKNOWN";
        break;

        case AdMobConsentType.NonPersonalized:
            type = "NON_PERSONALIZED";
        break;

        case AdMobConsentType.Personalized:
            type = "PERSONALIZED";
        break;

        case AdMobConsentType.Declined:
            type = "DECLINED";
        break;
    }
}

function loadAllAds()
{
	show_debug_message("loadAllAds()")
	
    admob_interstitial_load(function(data){
		show_debug_message($"admob_interstitial_load: {data}")
	});
	
    admob_rewarded_video_load(function(data){
		show_debug_message($"admob_rewarded_video_load: {data}")
	});
	
    admob_rewarded_interstitial_load(function(data){
		show_debug_message($"admob_rewarded_interstitial_load: {data}")
	});

    admob_app_open_ad_enable(
        display_landscape,function(data){
		show_debug_message($"admob_app_open_ad_enable: {data}")
	});
}

function onConsentShown(_data_json)
{
    var _data = json_parse(_data_json);
    show_debug_message("Consent show callback: " + json_stringify(_data));

    showDebugInfo();
    updateConsentStrings();
    loadAllAds();
}

function onConsentLoaded(_data_json)
{
    var _data = json_parse(_data_json);
    show_debug_message("Consent load callback: " + json_stringify(_data));

    if (_data.event_type == AdMobConsentCallbackEvent.Loaded)
    {
        admob_consent_show(onConsentShown);
    }
    else
    {
        loadAllAds();
    }
}

function onConsentInfoUpdated(_data_json)
{
    var _data = json_parse(_data_json);
    show_debug_message("Consent info callback: " + json_stringify(_data));

    updateConsentStrings();

    if (_data.event_type == AdMobConsentCallbackEvent.RequestInfoUpdated)
    {
        if (admob_consent_get_status() == AdMobConsentStatus.Required)
        {
            admob_consent_load(onConsentLoaded);
        }
        else
        {
            loadAllAds();
        }
    }
    else
    {
        loadAllAds();
    }
}

function onPaidEvent(_data_json)
{
    var _data = json_parse(_data_json);

    if (_data.event_type != AdMobPaidEventCallbackEvent.Paid)
    {
        return;
    }

    var _unit_id = variable_struct_exists(_data, "unit_id") ? _data.unit_id : "";
    var _ad_type = variable_struct_exists(_data, "ad_type") ? _data.ad_type : "";
    var _micros = variable_struct_exists(_data, "micros") ? _data.micros : 0;
    var _currency_code = variable_struct_exists(_data, "currency_code") ? _data.currency_code : "";
    var _precision = variable_struct_exists(_data, "precision") ? _data.precision : 0;
    var _ad_source_name = variable_struct_exists(_data, "ad_source_name") ? _data.ad_source_name : "";
    var _ad_source_id = variable_struct_exists(_data, "ad_source_id") ? _data.ad_source_id : "";
    var _ad_source_instance_name = variable_struct_exists(_data, "ad_source_instance_name") ? _data.ad_source_instance_name : "";
    var _ad_source_instance_id = variable_struct_exists(_data, "ad_source_instance_id") ? _data.ad_source_instance_id : "";
    var _mediation_adapter_class_name = variable_struct_exists(_data, "mediation_adapter_class_name") ? _data.mediation_adapter_class_name : "";

    show_debug_message(
        $"Paid event: {_ad_type} {_micros} {_currency_code}"
    );
}

// Development only. Call before admob_initialize().
admob_set_test_device_id();

// Optional paid event stream.
// admob_events_on_paid_event(true, onPaidEvent);

// Optional targeting examples:
// admob_targeting_max_ad_content_rating(AdMobMaxAdContentRating.General);
// admob_targeting_coppa(true);
// admob_targeting_under_age(true);
show_debug_message("admob_initialize GM")
var _init_result = admob_initialize(
    function(_data_json)
    {
		show_debug_message("admob_initialize CALLBACK: " + ": " + _data_json)
		
        var _data = json_parse(_data_json);

        if (!_data.success)
        {
            show_debug_message(
                "AdMob initialize failed: "
                + string(_data.code)
                + " "
                + _data.error_message
            );

            return;
        }

        if (_data.event_type == AdMobInitializeCallbackEvent.Initialized)
        {
            admob_consent_request_info_update(
                AdMobConsentDebugGeography.EEA,
                onConsentInfoUpdated
            );
        }
    }
);

if (_init_result != AdMobError.Ok)
{
    show_debug_message(
        "AdMob initialize call failed immediately: "
        + string(_init_result)
    );
}

// Ad unit IDs are loaded from extension options by the native extension.
// Override them here only if needed:
//
// admob_banner_set_ad_unit(BANNER_ID);
// admob_interstitial_set_ad_unit(INTERSTITIAL_ID);
// admob_rewarded_video_set_ad_unit(REWARDED_ID);
// admob_rewarded_interstitial_set_ad_unit(REWARDED_INTERSTITIAL_ID);
