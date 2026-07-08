event_inherited();

if (pressed && displayHeight != display_get_height())
{
    admob_banner_remove();

    admob_banner_create_ext(
        banner_type,
        bottom,
        alignment,
        function(_data_json)
        {
            var _data = json_parse(_data_json);
            show_debug_message(
                "Banner callback: " + json_stringify(_data)
            );
        }
    );

    displayHeight = display_get_height();
}
