// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobPaidEventCallbackEvent
{
    Paid((int)0);

    private final int value;
    private AdMobPaidEventCallbackEvent(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobPaidEventCallbackEvent from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobPaidEventCallbackEvent.Paid;
            default:
                throw new IllegalArgumentException("Unknown AdMobPaidEventCallbackEvent value: " + v);
        }
    }
}