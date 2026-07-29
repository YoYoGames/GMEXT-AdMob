event_inherited();

if (pressed && displayHeight != display_get_height())
{
    admob_banner_remove();

    admob_banner_create_ext(
        banner_type,
        bottom,
        alignment,
        function(_result, _type)
        {
            show_debug_message($"Banner callback: success={_result.success}, type={_type}, error={_result.error_message}");
        }
    );

    displayHeight = display_get_height();
}
