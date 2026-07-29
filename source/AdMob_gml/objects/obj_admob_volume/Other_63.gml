/// @description Apply volume change

if (request_id == async_load[? "id"])
{
    if (async_load[? "status"] == 1)
    {
        var volume = async_load[? "value"];
        var shouldMute = volume == 0;

        admob_settings_set_muted(shouldMute);
        admob_settings_set_volume(volume);
    }
}
