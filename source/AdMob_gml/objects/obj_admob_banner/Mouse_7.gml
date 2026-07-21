/// @description Banner create/move

if (pressed)
{
    bottom = !bottom;

    var _move_result =
        admob_banner_move(bottom);

    if (_move_result != AdMobError.Ok)
    {
        show_debug_message($"Banner move failed: {_move_result}");
    }
}
else
{
    pressed = true;

    var _create_result = admob_banner_create_ext(
            banner_type,
            bottom,
            alignment,
            function(data)
            {
                show_debug_message($"Banner callback: {json_stringify(data)}");

                if (data.event_type == AdMobBannerCallbackEvent.LoadFailed)
                {
                    show_debug_message($"Banner failed: {data.error_message}");
                }
            }
        );

    if (_create_result != AdMobError.Ok)
    {
        show_debug_message($"Banner create failed immediately: {_create_result}");
    }
}
