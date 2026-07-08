// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobBannerCallbackEvent
{
    Loaded((int)0),
    LoadFailed((int)1),
    Opened((int)2),
    Clicked((int)3),
    Closed((int)4);

    private final int value;
    private AdMobBannerCallbackEvent(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobBannerCallbackEvent from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobBannerCallbackEvent.Loaded;
            case 1:
                return AdMobBannerCallbackEvent.LoadFailed;
            case 2:
                return AdMobBannerCallbackEvent.Opened;
            case 3:
                return AdMobBannerCallbackEvent.Clicked;
            case 4:
                return AdMobBannerCallbackEvent.Closed;
            default:
                throw new IllegalArgumentException("Unknown AdMobBannerCallbackEvent value: " + v);
        }
    }
}