// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.enums;

public enum AdMobMaxAdContentRating
{
    General((int)0),
    ParentalGuidance((int)1),
    Teen((int)2),
    MatureAudience((int)3);

    private final int value;
    private AdMobMaxAdContentRating(int v)
    {
        this.value = v;
    }
    public int value()
    {
        return this.value;
    }
    public static AdMobMaxAdContentRating from(int v)
    {
        switch (v)
        {
            case 0:
                return AdMobMaxAdContentRating.General;
            case 1:
                return AdMobMaxAdContentRating.ParentalGuidance;
            case 2:
                return AdMobMaxAdContentRating.Teen;
            case 3:
                return AdMobMaxAdContentRating.MatureAudience;
            default:
                throw new IllegalArgumentException("Unknown AdMobMaxAdContentRating value: " + v);
        }
    }
}