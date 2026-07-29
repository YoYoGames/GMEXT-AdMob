// ##### extgen :: Auto-generated file do not edit!! #####

package ${YYAndroidPackageName}.records;

import ${YYAndroidPackageName}.GMExtWire;
import ${YYAndroidPackageName}.codecs.*;
import ${YYAndroidPackageName}.enums.*;

import java.nio.ByteBuffer;
import java.util.Optional;

public record AdMobPaidEvent(AdMobAdType ad_type, String ad_unit_id, double value_micros, String currency_code, AdMobPrecisionType precision, String mediation_adapter_class_name, java.util.Optional<String> ad_source_name, java.util.Optional<String> ad_source_id, java.util.Optional<String> ad_source_instance_name, java.util.Optional<String> ad_source_instance_id) implements GMExtWire.ITypedStruct
{
    public static final int CODEC_ID = 2;
    @Override
    public void encode(ByteBuffer b)
    {
        AdMobPaidEventCodec.write(b, this);
    }
}
