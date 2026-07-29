// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobConsentStatus
{
    Unknown((int)0),
    NotRequired((int)1),
    Required((int)2),
    Obtained((int)3);

    private final int value;
    private AdMobConsentStatus(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobConsentStatus from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobConsentStatus.Unknown;
            case 1:
                return AdMobConsentStatus.NotRequired;
            case 2:
                return AdMobConsentStatus.Required;
            case 3:
                return AdMobConsentStatus.Obtained;
            default:
                throw new IllegalArgumentException("Unknown AdMobConsentStatus value: " + v);
        }
    }
}