/// @description Interstitial load/show

if (!is_undefined(interstitial_handle))
{
    var _handle = interstitial_handle;
    interstitial_handle = undefined;

    var _show_result =
        admob_interstitial_show(
            _handle,
            function(_result, _type)
            {
                show_debug_message($"Interstitial show callback: success={_result.success}, type={_type}, error={_result.error_message}");

                if (!_result.success || _type == AdMobInterstitialShowEvent.Dismissed)
                {
                    admob_interstitial_load(function(_result, _handle)
                    {
                        show_debug_message($"Interstitial load callback: success={_result.success}, error={_result.error_message}");

                        if (_result.success)
                            interstitial_handle = _handle;
                    });
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
        function(_result, _handle)
        {
            show_debug_message($"Interstitial load callback: success={_result.success}, error={_result.error_message}");

            if (_result.success)
                interstitial_handle = _handle;
        }
    );

    show_message_async("Interstitial still loading, try again soon");
}
