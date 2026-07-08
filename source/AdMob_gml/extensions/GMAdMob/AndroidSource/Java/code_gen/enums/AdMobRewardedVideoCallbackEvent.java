// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobRewardedVideoCallbackEvent
{
    Loaded((int)0),
    LoadFailed((int)1),
    FullyShown((int)2),
    ShowFailed((int)3),
    Dismissed((int)4),
    Reward((int)5);

    private final int value;
    private AdMobRewardedVideoCallbackEvent(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobRewardedVideoCallbackEvent from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobRewardedVideoCallbackEvent.Loaded;
            case 1:
                return AdMobRewardedVideoCallbackEvent.LoadFailed;
            case 2:
                return AdMobRewardedVideoCallbackEvent.FullyShown;
            case 3:
                return AdMobRewardedVideoCallbackEvent.ShowFailed;
            case 4:
                return AdMobRewardedVideoCallbackEvent.Dismissed;
            case 5:
                return AdMobRewardedVideoCallbackEvent.Reward;
            default:
                throw new IllegalArgumentException("Unknown AdMobRewardedVideoCallbackEvent value: " + v);
        }
    }
}