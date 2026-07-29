// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobBannerSize
{
    Banner((int)0),
    LargeBanner((int)1),
    MediumRectangle((int)2),
    FullBanner((int)3),
    Leaderboard((int)4),
    SmartBanner((int)5),
    AnchoredAdaptive((int)6);

    private final int value;
    private AdMobBannerSize(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobBannerSize from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobBannerSize.Banner;
            case 1:
                return AdMobBannerSize.LargeBanner;
            case 2:
                return AdMobBannerSize.MediumRectangle;
            case 3:
                return AdMobBannerSize.FullBanner;
            case 4:
                return AdMobBannerSize.Leaderboard;
            case 5:
                return AdMobBannerSize.SmartBanner;
            case 6:
                return AdMobBannerSize.AnchoredAdaptive;
            default:
                throw new IllegalArgumentException("Unknown AdMobBannerSize value: " + v);
        }
    }
}