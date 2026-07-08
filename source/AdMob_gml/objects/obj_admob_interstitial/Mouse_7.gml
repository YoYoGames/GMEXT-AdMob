/// @description Interstitial load/show

if (admob_interstitial_is_loaded())
{
    var _show_result =
        admob_interstitial_show(
            function(_data_json)
            {
                var _data = json_parse(_data_json);

                show_debug_message(
                    "Interstitial show callback: "
                    + json_stringify(_data)
                );

                if (_data.event_type == AdMobInterstitialCallbackEvent.Dismissed
                ||  _data.event_type == AdMobInterstitialCallbackEvent.ShowFailed)
                {
                    admob_interstitial_load(Obj_AdMob.admob_log);
                }
            }
        );

    if (_show_result != AdMobError.Ok)
    {
        show_debug_message(
            "Interstitial show failed immediately: "
            + string(_show_result)
        );
    }
}
else
{
    admob_interstitial_load(
        function(_data_json)
        {
            var _data = json_parse(_data_json);

            show_debug_message(
                "Interstitial load callback: "
                + json_stringify(_data)
            );
        }
    );

    show_message_async(
        "Interstitial still loading, try again soon"
    );
}
