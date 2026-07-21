// ##### extgen :: Auto-generated file do not edit!! #####

#import <objc/runtime.h>
#import "core/GMExtUtils.h"
#import "GMAdMobInternal_ios.h"


extern "C" const char* extOptGetString(char* _ext, char* _opt);

// Adapter: matches const signature expected by the C++ API
static const char* ExtOptGetString(const char* ext, const char* opt)
{
    return extOptGetString(const_cast<char*>(ext), const_cast<char*>(opt));
}

static BOOL GMIsSubclassOf(Class cls, Class base)
{
    for (Class c = cls; c != Nil; c = class_getSuperclass(c)) {
        if (c == base) return YES;
    }
    return NO;
}

static void GMInjectSelectorsIntoSubclass(Class subclass, Class base)
{
    // Build set of methods already defined on subclass
    unsigned subCount = 0;
    Method *subList = class_copyMethodList(subclass, &subCount);

    CFMutableSetRef owned = CFSetCreateMutable(kCFAllocatorDefault, 0, NULL);
    for (unsigned i = 0; i < subCount; ++i) {
        CFSetAddValue(owned, method_getName(subList[i]));
    }

    // Walk base class methods
    unsigned baseCount = 0;
    Method *baseList = class_copyMethodList(base, &baseCount);

    for (unsigned i = 0; i < baseCount; ++i) {
        SEL sel = method_getName(baseList[i]);
        const char *name = sel_getName(sel);

        // Only inject extension selectors (methods prefixed with __EXT_NATIVE__)
        if (!name || strncmp(name, "__EXT_NATIVE__", 13) != 0) continue;

        // Add only if subclass doesn't already have it
        if (!CFSetContainsValue(owned, sel)) {
            IMP imp = method_getImplementation(baseList[i]);
            const char *types = method_getTypeEncoding(baseList[i]);
            if (class_addMethod(subclass, sel, imp, types)) {
                CFSetAddValue(owned, sel);
            }
        }
    }

    if (subList) free(subList);
    if (baseList) free(baseList);
    if (owned) CFRelease(owned);
}

@interface GMAdMobInternal ()
{
    gm::runtime::DispatchQueue __dispatch_queue;
    id<GMAdMobInterface> __impl;
}@end


@implementation GMAdMobInternal

+ (void)load
{
    // Find all loaded classes
    int num = objc_getClassList(NULL, 0);
    if (num <= 0) return;

    Class *classes = (Class *)malloc(sizeof(Class) * (unsigned)num);
    num = objc_getClassList(classes, num);

    Class base = [GMAdMobInternal class];

    for (int i = 0; i < num; ++i) {
        Class cls = classes[i];
        if (cls == base) continue;

        // We only care about direct or indirect subclasses
        if (GMIsSubclassOf(cls, base)) {
            GMInjectSelectorsIntoSubclass(cls, base);
        }
    }

    free(classes);

    gm::details::GMRTRunnerInterface ri{};
    ri.ExtOptGetString = &ExtOptGetString;
    GMExtensionInitialise(&ri, sizeof(ri));
}

- (instancetype)init
{
    self = [super init];
    if (self)
    {
        __impl = (id<GMAdMobInterface>)self;
    }
    return self;
}
- (double)__EXT_NATIVE__admob_initialize:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_initialize:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_set_test_device_id
{
    double __result = [__impl admob_set_test_device_id];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_events_on_paid_event:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: enabled, type: Bool
    bool enabled = gm::wire::codec::readValue<bool>(__br);

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    [__impl admob_events_on_paid_event:enabled callback:callback];

    return 0;
}

- (double)__EXT_NATIVE__admob_banner_set_ad_unit:(char*)ad_unit_id
{
    [__impl admob_banner_set_ad_unit:ad_unit_id];

    return 0;
}

- (double)__EXT_NATIVE__admob_banner_create:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: size, type: enum AdMobBannerSize
    gm_enums::AdMobBannerSize size = gm::wire::codec::readValue<gm_enums::AdMobBannerSize>(__br);

    // field: bottom, type: Bool
    bool bottom = gm::wire::codec::readValue<bool>(__br);

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_banner_create:size bottom:bottom callback:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_banner_create_ext:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: size, type: enum AdMobBannerSize
    gm_enums::AdMobBannerSize size = gm::wire::codec::readValue<gm_enums::AdMobBannerSize>(__br);

    // field: bottom, type: Bool
    bool bottom = gm::wire::codec::readValue<bool>(__br);

    // field: alignment, type: enum AdMobBannerAlignment
    gm_enums::AdMobBannerAlignment alignment = gm::wire::codec::readValue<gm_enums::AdMobBannerAlignment>(__br);

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_banner_create_ext:size bottom:bottom alignment:alignment callback:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_banner_get_width
{
    double __result = [__impl admob_banner_get_width];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_banner_get_height
{
    double __result = [__impl admob_banner_get_height];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_banner_move:(double)bottom
{
    [__impl admob_banner_move:bottom];

    return 0;
}

- (double)__EXT_NATIVE__admob_banner_show
{
    [__impl admob_banner_show];

    return 0;
}

- (double)__EXT_NATIVE__admob_banner_hide
{
    [__impl admob_banner_hide];

    return 0;
}

- (double)__EXT_NATIVE__admob_banner_remove
{
    [__impl admob_banner_remove];

    return 0;
}

- (double)__EXT_NATIVE__admob_interstitial_set_ad_unit:(char*)ad_unit_id
{
    [__impl admob_interstitial_set_ad_unit:ad_unit_id];

    return 0;
}

- (double)__EXT_NATIVE__admob_interstitial_free_loaded_instances:(double)count
{
    [__impl admob_interstitial_free_loaded_instances:count];

    return 0;
}

- (double)__EXT_NATIVE__admob_interstitial_max_instances:(double)value
{
    [__impl admob_interstitial_max_instances:value];

    return 0;
}

- (double)__EXT_NATIVE__admob_interstitial_load:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_interstitial_load:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_interstitial_show:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_interstitial_show:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_interstitial_is_loaded
{
    bool __result = [__impl admob_interstitial_is_loaded];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_interstitial_instances_count
{
    double __result = [__impl admob_interstitial_instances_count];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_server_side_verification_set:(char*)user_id arg1:(char*)custom_data
{
    [__impl admob_server_side_verification_set:user_id custom_data:custom_data];

    return 0;
}

- (double)__EXT_NATIVE__admob_server_side_verification_clear
{
    [__impl admob_server_side_verification_clear];

    return 0;
}

- (double)__EXT_NATIVE__admob_rewarded_video_set_ad_unit:(char*)ad_unit_id
{
    [__impl admob_rewarded_video_set_ad_unit:ad_unit_id];

    return 0;
}

- (double)__EXT_NATIVE__admob_rewarded_video_free_loaded_instances:(double)count
{
    [__impl admob_rewarded_video_free_loaded_instances:count];

    return 0;
}

- (double)__EXT_NATIVE__admob_rewarded_video_max_instances:(double)value
{
    [__impl admob_rewarded_video_max_instances:value];

    return 0;
}

- (double)__EXT_NATIVE__admob_rewarded_video_load:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_rewarded_video_load:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_rewarded_video_show:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_rewarded_video_show:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_rewarded_video_is_loaded
{
    bool __result = [__impl admob_rewarded_video_is_loaded];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_rewarded_video_instances_count
{
    double __result = [__impl admob_rewarded_video_instances_count];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_rewarded_interstitial_set_ad_unit:(char*)ad_unit_id
{
    [__impl admob_rewarded_interstitial_set_ad_unit:ad_unit_id];

    return 0;
}

- (double)__EXT_NATIVE__admob_rewarded_interstitial_free_loaded_instances:(double)count
{
    [__impl admob_rewarded_interstitial_free_loaded_instances:count];

    return 0;
}

- (double)__EXT_NATIVE__admob_rewarded_interstitial_max_instances:(double)value
{
    [__impl admob_rewarded_interstitial_max_instances:value];

    return 0;
}

- (double)__EXT_NATIVE__admob_rewarded_interstitial_load:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_rewarded_interstitial_load:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_rewarded_interstitial_show:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_rewarded_interstitial_show:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_rewarded_interstitial_is_loaded
{
    bool __result = [__impl admob_rewarded_interstitial_is_loaded];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_rewarded_interstitial_instances_count
{
    double __result = [__impl admob_rewarded_interstitial_instances_count];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_app_open_ad_set_ad_unit:(char*)ad_unit_id
{
    [__impl admob_app_open_ad_set_ad_unit:ad_unit_id];

    return 0;
}

- (double)__EXT_NATIVE__admob_app_open_ad_enable:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: orientation, type: Float64
    double orientation = gm::wire::codec::readValue<double>(__br);

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_app_open_ad_enable:orientation callback:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_app_open_ad_disable
{
    [__impl admob_app_open_ad_disable];

    return 0;
}

- (double)__EXT_NATIVE__admob_app_open_ad_is_enabled
{
    bool __result = [__impl admob_app_open_ad_is_enabled];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_app_open_ad_is_loaded
{
    bool __result = [__impl admob_app_open_ad_is_loaded];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_app_open_ad_load:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_app_open_ad_load:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_app_open_ad_show:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_app_open_ad_show:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_targeting_coppa:(double)coppa
{
    [__impl admob_targeting_coppa:coppa];

    return 0;
}

- (double)__EXT_NATIVE__admob_targeting_under_age:(double)under_age
{
    [__impl admob_targeting_under_age:under_age];

    return 0;
}

- (double)__EXT_NATIVE__admob_targeting_max_ad_content_rating:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: content_rating, type: enum AdMobMaxAdContentRating
    gm_enums::AdMobMaxAdContentRating content_rating = gm::wire::codec::readValue<gm_enums::AdMobMaxAdContentRating>(__br);

    [__impl admob_targeting_max_ad_content_rating:content_rating];

    return 0;
}

- (double)__EXT_NATIVE__admob_consent_request_info_update:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: debug_geography, type: enum AdMobConsentDebugGeography
    gm_enums::AdMobConsentDebugGeography debug_geography = gm::wire::codec::readValue<gm_enums::AdMobConsentDebugGeography>(__br);

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_consent_request_info_update:debug_geography callback:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_consent_get_status
{
    double __result = [__impl admob_consent_get_status];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_consent_get_type
{
    double __result = [__impl admob_consent_get_type];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_consent_is_form_available
{
    bool __result = [__impl admob_consent_is_form_available];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_consent_load:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_consent_load:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_consent_show:(char*)__arg_buffer arg1:(double)__arg_buffer_length
{
    gm::byteio::BufferReader __br{__arg_buffer, static_cast<size_t>(__arg_buffer_length)};

    // field: callback, type: Function
    gm::wire::GMFunction callback = gm::wire::codec::readFunction(__br, &__dispatch_queue);

    double __result = [__impl admob_consent_show:callback];

    return static_cast<double>(__result);
}

- (double)__EXT_NATIVE__admob_consent_reset
{
    [__impl admob_consent_reset];

    return 0;
}

- (double)__EXT_NATIVE__admob_consent_set_rdp:(double)enabled
{
    [__impl admob_consent_set_rdp:enabled];

    return 0;
}

- (double)__EXT_NATIVE__admob_settings_set_volume:(double)value
{
    [__impl admob_settings_set_volume:value];

    return 0;
}

- (double)__EXT_NATIVE__admob_settings_set_muted:(double)muted
{
    [__impl admob_settings_set_muted:muted];

    return 0;
}

// Internal function used for fetching dispatched function calls to GML
- (double)__EXT_NATIVE__GMAdMob_invocation_handler:(char*)__ret_buffer arg1:(double)__ret_buffer_length
{
    gm::byteio::BufferWriter __bw{ __ret_buffer, static_cast<size_t>(__ret_buffer_length) };
    return __dispatch_queue.fetch(__bw);
}

@end

