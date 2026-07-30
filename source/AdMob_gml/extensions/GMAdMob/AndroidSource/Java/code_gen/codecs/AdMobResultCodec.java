// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.codecs;

import java.nio.ByteBuffer;

import ${YYAndroidPackageName}.GMExtWire;
import java.util.Optional;
import ${YYAndroidPackageName}.records.*;

public final class AdMobResultCodec {
    private AdMobResultCodec()
    {
    }
    public static AdMobResult read(ByteBuffer b)
    {
        boolean success = GMExtWire.readBool(b);

        java.util.Optional<String> error_message = java.util.Optional.empty();
        if (GMExtWire.readBool(b))
        {
            String __opt_error_message = GMExtWire.readString(b);
            error_message = java.util.Optional.of(__opt_error_message);
        }

        java.util.Optional<Integer> sdk_error_code = java.util.Optional.empty();
        if (GMExtWire.readBool(b))
        {
            int __opt_sdk_error_code = GMExtWire.readI32(b);
            sdk_error_code = java.util.Optional.of(__opt_sdk_error_code);
        }

        return new AdMobResult(success, error_message, sdk_error_code);
    }

    public static void write(GMExtWire.IByteWriter b, AdMobResult obj)
    {
        GMExtWire.writeBool(b, obj.success());

        GMExtWire.writeBool(b, obj.error_message() != null && obj.error_message().isPresent());
        if (obj.error_message() != null && obj.error_message().isPresent())
        {
            GMExtWire.writeString(b, obj.error_message().get());
        }

        GMExtWire.writeBool(b, obj.sdk_error_code() != null && obj.sdk_error_code().isPresent());
        if (obj.sdk_error_code() != null && obj.sdk_error_code().isPresent())
        {
            GMExtWire.writeI32(b, obj.sdk_error_code().get());
        }

    }
}