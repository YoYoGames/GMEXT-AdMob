event_inherited();

if (pressed && displayHeight != display_get_height())
{
    admob_banner_remove();

    admob_banner_create_ext(
        banner_type,
        bottom,
        alignment,
        function(data)
        {
            show_debug_message($"Banner callback: {json_stringify(data)}");
        }
    );

    displayHeight = display_get_height();
}
