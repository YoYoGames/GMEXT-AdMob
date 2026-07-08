// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobInterstitialCallbackEvent
{
    Loaded((int)0),
    LoadFailed((int)1),
    FullyShown((int)2),
    ShowFailed((int)3),
    Dismissed((int)4);

    private final int value;
    private AdMobInterstitialCallbackEvent(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobInterstitialCallbackEvent from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobInterstitialCallbackEvent.Loaded;
            case 1:
                return AdMobInterstitialCallbackEvent.LoadFailed;
            case 2:
                return AdMobInterstitialCallbackEvent.FullyShown;
            case 3:
                return AdMobInterstitialCallbackEvent.ShowFailed;
            case 4:
                return AdMobInterstitialCallbackEvent.Dismissed;
            default:
                throw new IllegalArgumentException("Unknown AdMobInterstitialCallbackEvent value: " + v);
        }
    }
}