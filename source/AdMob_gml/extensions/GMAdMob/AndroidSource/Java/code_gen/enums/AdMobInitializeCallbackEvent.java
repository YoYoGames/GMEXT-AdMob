// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobInitializeCallbackEvent
{
    Initialized((int)0),
    Failed((int)1);

    private final int value;
    private AdMobInitializeCallbackEvent(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobInitializeCallbackEvent from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobInitializeCallbackEvent.Initialized;
            case 1:
                return AdMobInitializeCallbackEvent.Failed;
            default:
                throw new IllegalArgumentException("Unknown AdMobInitializeCallbackEvent value: " + v);
        }
    }
}