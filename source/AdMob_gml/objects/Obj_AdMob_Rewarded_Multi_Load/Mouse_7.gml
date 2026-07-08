admob_rewarded_video_load(
    function(_data_json)
    {
        var _data = json_parse(_data_json);

        show_debug_message(
            "Rewarded load callback: "
            + json_stringify(_data)
        );
    }
);
