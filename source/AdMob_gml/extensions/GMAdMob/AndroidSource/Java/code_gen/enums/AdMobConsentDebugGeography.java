// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobConsentDebugGeography
{
    Disabled((int)0),
    EEA((int)1),
    NotEEA((int)2),
    RegulatedUSState((int)3),
    Other((int)4);

    private final int value;
    private AdMobConsentDebugGeography(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobConsentDebugGeography from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobConsentDebugGeography.Disabled;
            case 1:
                return AdMobConsentDebugGeography.EEA;
            case 2:
                return AdMobConsentDebugGeography.NotEEA;
            case 3:
                return AdMobConsentDebugGeography.RegulatedUSState;
            case 4:
                return AdMobConsentDebugGeography.Other;
            default:
                throw new IllegalArgumentException("Unknown AdMobConsentDebugGeography value: " + v);
        }
    }
}