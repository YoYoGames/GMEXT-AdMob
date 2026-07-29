// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobRewardedVideoShowEvent
{
    Shown((int)0),
    Dismissed((int)1),
    Clicked((int)2),
    Impression((int)3),
    Reward((int)4);

    private final int value;
    private AdMobRewardedVideoShowEvent(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobRewardedVideoShowEvent from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobRewardedVideoShowEvent.Shown;
            case 1:
                return AdMobRewardedVideoShowEvent.Dismissed;
            case 2:
                return AdMobRewardedVideoShowEvent.Clicked;
            case 3:
                return AdMobRewardedVideoShowEvent.Impression;
            case 4:
                return AdMobRewardedVideoShowEvent.Reward;
            default:
                throw new IllegalArgumentException("Unknown AdMobRewardedVideoShowEvent value: " + v);
        }
    }
}