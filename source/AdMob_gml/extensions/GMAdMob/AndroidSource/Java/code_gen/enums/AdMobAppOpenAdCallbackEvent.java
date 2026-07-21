// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobAppOpenAdCallbackEvent
{
    Loaded((int)0),
    LoadFailed((int)1),
    Shown((int)2),
    ShowFailed((int)3),
    Dismissed((int)4);

    private final int value;
    private AdMobAppOpenAdCallbackEvent(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobAppOpenAdCallbackEvent from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobAppOpenAdCallbackEvent.Loaded;
            case 1:
                return AdMobAppOpenAdCallbackEvent.LoadFailed;
            case 2:
                return AdMobAppOpenAdCallbackEvent.Shown;
            case 3:
                return AdMobAppOpenAdCallbackEvent.ShowFailed;
            case 4:
                return AdMobAppOpenAdCallbackEvent.Dismissed;
            default:
                throw new IllegalArgumentException("Unknown AdMobAppOpenAdCallbackEvent value: " + v);
        }
    }
}