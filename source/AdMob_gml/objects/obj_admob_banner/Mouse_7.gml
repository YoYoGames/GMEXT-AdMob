/// @description Banner create/move

if (pressed)
{
    bottom = !bottom;

    var _move_result =
        admob_banner_move(bottom);

    if (_move_result != AdMobError.Ok)
    {
        show_debug_message(
            "Banner move failed: " + string(_move_result)
        );
    }
}
else
{
    pressed = true;

    var _create_result =
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

                if (_data.event_type == AdMobBannerCallbackEvent.LoadFailed)
                {
                    show_debug_message(
                        "Banner failed: " + _data.error_message
                    );
                }
            }
        );

    if (_create_result != AdMobError.Ok)
    {
        show_debug_message(
            "Banner create failed immediately: "
            + string(_create_result)
        );
    }
}
