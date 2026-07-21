// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobRewardedInterstitialCallbackEvent
{
    Loaded((int)0),
    LoadFailed((int)1),
    Shown((int)2),
    ShowFailed((int)3),
    Dismissed((int)4),
    Reward((int)5);

    private final int value;
    private AdMobRewardedInterstitialCallbackEvent(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobRewardedInterstitialCallbackEvent from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobRewardedInterstitialCallbackEvent.Loaded;
            case 1:
                return AdMobRewardedInterstitialCallbackEvent.LoadFailed;
            case 2:
                return AdMobRewardedInterstitialCallbackEvent.Shown;
            case 3:
                return AdMobRewardedInterstitialCallbackEvent.ShowFailed;
            case 4:
                return AdMobRewardedInterstitialCallbackEvent.Dismissed;
            case 5:
                return AdMobRewardedInterstitialCallbackEvent.Reward;
            default:
                throw new IllegalArgumentException("Unknown AdMobRewardedInterstitialCallbackEvent value: " + v);
        }
    }
}