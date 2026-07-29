// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobAdType
{
    Banner((int)0),
    Interstitial((int)1),
    RewardedVideo((int)2),
    RewardedInterstitial((int)3),
    AppOpen((int)4);

    private final int value;
    private AdMobAdType(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobAdType from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobAdType.Banner;
            case 1:
                return AdMobAdType.Interstitial;
            case 2:
                return AdMobAdType.RewardedVideo;
            case 3:
                return AdMobAdType.RewardedInterstitial;
            case 4:
                return AdMobAdType.AppOpen;
            default:
                throw new IllegalArgumentException("Unknown AdMobAdType value: " + v);
        }
    }
}