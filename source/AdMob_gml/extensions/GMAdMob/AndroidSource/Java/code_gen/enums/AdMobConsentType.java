// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobConsentType
{
    Unknown((int)0),
    NonPersonalized((int)1),
    Personalized((int)2),
    Declined((int)3);

    private final int value;
    private AdMobConsentType(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobConsentType from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobConsentType.Unknown;
            case 1:
                return AdMobConsentType.NonPersonalized;
            case 2:
                return AdMobConsentType.Personalized;
            case 3:
                return AdMobConsentType.Declined;
            default:
                throw new IllegalArgumentException("Unknown AdMobConsentType value: " + v);
        }
    }
}