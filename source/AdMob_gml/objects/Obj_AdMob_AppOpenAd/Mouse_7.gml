
if (admob_app_open_ad_is_enabled())
{
    admob_app_open_ad_disable();
}
else
{
    admob_app_open_ad_enable(
        function(_result, _type = undefined)
        {
            show_debug_message($"App open callback: success={_result.success}, type={_type}, error={_result.error_message}");
        }
    );
}
