// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobError
{
    Ok((int)0),
    NotInitialized((int)-1),
    InvalidAdId((int)-2),
    AdLimitReached((int)-3),
    NoAdsLoaded((int)-4),
    NoActiveBannerAd((int)-5),
    IllegalCall((int)-6),
    NullViewHandler((int)-7);

    private final int value;
    private AdMobError(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobError from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobError.Ok;
            case -1:
                return AdMobError.NotInitialized;
            case -2:
                return AdMobError.InvalidAdId;
            case -3:
                return AdMobError.AdLimitReached;
            case -4:
                return AdMobError.NoAdsLoaded;
            case -5:
                return AdMobError.NoActiveBannerAd;
            case -6:
                return AdMobError.IllegalCall;
            case -7:
                return AdMobError.NullViewHandler;
            default:
                throw new IllegalArgumentException("Unknown AdMobError value: " + v);
        }
    }
}