// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.codecs;

import java.nio.ByteBuffer;

import ${YYAndroidPackageName}.GMExtWire;
import ${YYAndroidPackageName}.records.*;

public final class AdMobRewardCodec {
    private AdMobRewardCodec()
    {
    }
    public static AdMobReward read(ByteBuffer b)
    {
        double amount = GMExtWire.readF64(b);

        String type = GMExtWire.readString(b);

        return new AdMobReward(amount, type);
    }

    public static void write(GMExtWire.IByteWriter b, AdMobReward obj)
    {
        GMExtWire.writeF64(b, obj.amount());

        GMExtWire.writeString(b, obj.type());

    }
}