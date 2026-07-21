// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobConsentCallbackEvent
{
    RequestInfoUpdated((int)0),
    RequestInfoUpdateFailed((int)1),
    Loaded((int)2),
    LoadFailed((int)3),
    Dismissed((int)4),
    ShowFailed((int)5);

    private final int value;
    private AdMobConsentCallbackEvent(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobConsentCallbackEvent from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobConsentCallbackEvent.RequestInfoUpdated;
            case 1:
                return AdMobConsentCallbackEvent.RequestInfoUpdateFailed;
            case 2:
                return AdMobConsentCallbackEvent.Loaded;
            case 3:
                return AdMobConsentCallbackEvent.LoadFailed;
            case 4:
                return AdMobConsentCallbackEvent.Dismissed;
            case 5:
                return AdMobConsentCallbackEvent.ShowFailed;
            default:
                throw new IllegalArgumentException("Unknown AdMobConsentCallbackEvent value: " + v);
        }
    }
}