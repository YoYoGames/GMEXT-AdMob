// Demonstrates holding multiple outstanding rewarded video handles at once -
// there's no extension-owned cap on this, the caller just tracks what it holds.
admob_rewarded_video_load(
    function(_result, _handle)
    {
        show_debug_message($"Rewarded load callback: success={_result.success}, error={_result.error_message}");

        if (_result.success)
            array_push(Obj_AdMob_Rewarded.rewarded_video_multi_handles, _handle);
    }
);
