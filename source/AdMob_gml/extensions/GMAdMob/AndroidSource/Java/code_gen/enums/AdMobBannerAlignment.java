// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobBannerAlignment
{
    Left((int)0),
    Center((int)1),
    Right((int)2);

    private final int value;
    private AdMobBannerAlignment(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobBannerAlignment from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobBannerAlignment.Left;
            case 1:
                return AdMobBannerAlignment.Center;
            case 2:
                return AdMobBannerAlignment.Right;
            default:
                throw new IllegalArgumentException("Unknown AdMobBannerAlignment value: " + v);
        }
    }
}