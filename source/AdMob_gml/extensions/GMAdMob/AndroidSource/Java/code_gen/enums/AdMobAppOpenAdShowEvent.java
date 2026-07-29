// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobAppOpenAdShowEvent
{
    Shown((int)0),
    Dismissed((int)1),
    Clicked((int)2),
    Impression((int)3);

    private final int value;
    private AdMobAppOpenAdShowEvent(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobAppOpenAdShowEvent from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobAppOpenAdShowEvent.Shown;
            case 1:
                return AdMobAppOpenAdShowEvent.Dismissed;
            case 2:
                return AdMobAppOpenAdShowEvent.Clicked;
            case 3:
                return AdMobAppOpenAdShowEvent.Impression;
            default:
                throw new IllegalArgumentException("Unknown AdMobAppOpenAdShowEvent value: " + v);
        }
    }
}