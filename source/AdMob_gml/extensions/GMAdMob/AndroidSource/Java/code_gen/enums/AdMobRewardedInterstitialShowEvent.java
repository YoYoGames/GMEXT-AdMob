// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobRewardedInterstitialShowEvent
{
    Shown((int)0),
    Dismissed((int)1),
    Clicked((int)2),
    Impression((int)3),
    Reward((int)4);

    private final int value;
    private AdMobRewardedInterstitialShowEvent(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobRewardedInterstitialShowEvent from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobRewardedInterstitialShowEvent.Shown;
            case 1:
                return AdMobRewardedInterstitialShowEvent.Dismissed;
            case 2:
                return AdMobRewardedInterstitialShowEvent.Clicked;
            case 3:
                return AdMobRewardedInterstitialShowEvent.Impression;
            case 4:
                return AdMobRewardedInterstitialShowEvent.Reward;
            default:
                throw new IllegalArgumentException("Unknown AdMobRewardedInterstitialShowEvent value: " + v);
        }
    }
}