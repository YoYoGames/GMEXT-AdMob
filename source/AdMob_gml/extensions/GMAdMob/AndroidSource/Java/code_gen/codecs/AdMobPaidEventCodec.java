// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.codecs;

import java.nio.ByteBuffer;

import ${YYAndroidPackageName}.GMExtWire;
import java.util.Optional;
import ${YYAndroidPackageName}.enums.*;
import ${YYAndroidPackageName}.records.*;

public final class AdMobPaidEventCodec {
    private AdMobPaidEventCodec()
    {
    }
    public static AdMobPaidEvent read(ByteBuffer b)
    {
        AdMobAdType ad_type = AdMobAdType.from(GMExtWire.readI32(b));

        String ad_unit_id = GMExtWire.readString(b);

        double value_micros = GMExtWire.readF64(b);

        String currency_code = GMExtWire.readString(b);

        AdMobPrecisionType precision = AdMobPrecisionType.from(GMExtWire.readI32(b));

        String mediation_adapter_class_name = GMExtWire.readString(b);

        java.util.Optional<String> ad_source_name = java.util.Optional.empty();
        if (GMExtWire.readBool(b))
        {
            String __opt_ad_source_name = GMExtWire.readString(b);
            ad_source_name = java.util.Optional.of(__opt_ad_source_name);
        }

        java.util.Optional<String> ad_source_id = java.util.Optional.empty();
        if (GMExtWire.readBool(b))
        {
            String __opt_ad_source_id = GMExtWire.readString(b);
            ad_source_id = java.util.Optional.of(__opt_ad_source_id);
        }

        java.util.Optional<String> ad_source_instance_name = java.util.Optional.empty();
        if (GMExtWire.readBool(b))
        {
            String __opt_ad_source_instance_name = GMExtWire.readString(b);
            ad_source_instance_name = java.util.Optional.of(__opt_ad_source_instance_name);
        }

        java.util.Optional<String> ad_source_instance_id = java.util.Optional.empty();
        if (GMExtWire.readBool(b))
        {
            String __opt_ad_source_instance_id = GMExtWire.readString(b);
            ad_source_instance_id = java.util.Optional.of(__opt_ad_source_instance_id);
        }

        return new AdMobPaidEvent(ad_type, ad_unit_id, value_micros, currency_code, precision, mediation_adapter_class_name, ad_source_name, ad_source_id, ad_source_instance_name, ad_source_instance_id);
    }

    public static void write(GMExtWire.IByteWriter b, AdMobPaidEvent obj)
    {
        GMExtWire.writeI32(b, obj.ad_type().value());

        GMExtWire.writeString(b, obj.ad_unit_id());

        GMExtWire.writeF64(b, obj.value_micros());

        GMExtWire.writeString(b, obj.currency_code());

        GMExtWire.writeI32(b, obj.precision().value());

        GMExtWire.writeString(b, obj.mediation_adapter_class_name());

        GMExtWire.writeBool(b, obj.ad_source_name() != null && obj.ad_source_name().isPresent());
        if (obj.ad_source_name() != null && obj.ad_source_name().isPresent())
        {
            GMExtWire.writeString(b, obj.ad_source_name().get());
        }

        GMExtWire.writeBool(b, obj.ad_source_id() != null && obj.ad_source_id().isPresent());
        if (obj.ad_source_id() != null && obj.ad_source_id().isPresent())
        {
            GMExtWire.writeString(b, obj.ad_source_id().get());
        }

        GMExtWire.writeBool(b, obj.ad_source_instance_name() != null && obj.ad_source_instance_name().isPresent());
        if (obj.ad_source_instance_name() != null && obj.ad_source_instance_name().isPresent())
        {
            GMExtWire.writeString(b, obj.ad_source_instance_name().get());
        }

        GMExtWire.writeBool(b, obj.ad_source_instance_id() != null && obj.ad_source_instance_id().isPresent());
        if (obj.ad_source_instance_id() != null && obj.ad_source_instance_id().isPresent())
        {
            GMExtWire.writeString(b, obj.ad_source_instance_id().get());
        }

    }
}