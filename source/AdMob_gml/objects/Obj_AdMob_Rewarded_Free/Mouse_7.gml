// Disposes every handle currently held from Obj_AdMob_Rewarded_Multi_Load's
// pool (see admob_rewarded_video_dispose - explicit early release of a
// loaded-but-unshown handle).
var _handles = Obj_AdMob_Rewarded.rewarded_video_multi_handles;

for (var _i = 0; _i < array_length(_handles); _i++)
    admob_rewarded_video_dispose(_handles[_i]);

Obj_AdMob_Rewarded.rewarded_video_multi_handles = [];
