if (admob_app_open_ad_is_enabled())
{
    admob_app_open_ad_disable();
}
else
{
    admob_app_open_ad_enable(
        display_landscape,
        function(_data_json)
        {
            var _data = json_parse(_data_json);
            show_debug_message(
                "App open callback: " + json_stringify(_data)
            );
        }
    );
}
