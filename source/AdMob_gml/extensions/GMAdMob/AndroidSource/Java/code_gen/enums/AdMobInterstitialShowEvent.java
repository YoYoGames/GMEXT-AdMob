// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobInterstitialShowEvent
{
    Shown((int)0),
    Dismissed((int)1),
    Clicked((int)2),
    Impression((int)3);

    private final int value;
    private AdMobInterstitialShowEvent(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobInterstitialShowEvent from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobInterstitialShowEvent.Shown;
            case 1:
                return AdMobInterstitialShowEvent.Dismissed;
            case 2:
                return AdMobInterstitialShowEvent.Clicked;
            case 3:
                return AdMobInterstitialShowEvent.Impression;
            default:
                throw new IllegalArgumentException("Unknown AdMobInterstitialShowEvent value: " + v);
        }
    }
}