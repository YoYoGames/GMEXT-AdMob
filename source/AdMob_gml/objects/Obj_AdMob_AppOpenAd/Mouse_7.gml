
if (admob_app_open_ad_is_enabled())
{
    admob_app_open_ad_disable();
}
else
{
    admob_app_open_ad_enable(
        display_landscape,
        function(data)
        {
            show_debug_message($"App open callback: {json_stringify(data)}");
        }
    );
}
