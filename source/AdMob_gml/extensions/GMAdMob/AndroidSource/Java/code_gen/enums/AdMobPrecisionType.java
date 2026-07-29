// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobPrecisionType
{
    Unknown((int)0),
    Estimated((int)1),
    PublisherProvided((int)2),
    Precise((int)3);

    private final int value;
    private AdMobPrecisionType(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobPrecisionType from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobPrecisionType.Unknown;
            case 1:
                return AdMobPrecisionType.Estimated;
            case 2:
                return AdMobPrecisionType.PublisherProvided;
            case 3:
                return AdMobPrecisionType.Precise;
            default:
                throw new IllegalArgumentException("Unknown AdMobPrecisionType value: " + v);
        }
    }
}