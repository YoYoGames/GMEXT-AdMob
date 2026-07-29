// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.records;

import ${YYAndroidPackageName}.GMExtWire;
import ${YYAndroidPackageName}.codecs.*;

import java.nio.ByteBuffer;

public record AdMobReward(double amount, String type) implements GMExtWire.ITypedStruct
{
    public static final int CODEC_ID = 1;
    @Override
    public void encode(ByteBuffer b)
    {
        AdMobRewardCodec.write(b, this);
    }
}
