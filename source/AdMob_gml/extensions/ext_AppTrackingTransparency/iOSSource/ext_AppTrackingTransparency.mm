
#import "ext_AppTrackingTransparency.h"

const int EVENT_OTHER_SOCIAL = 70;
extern int CreateDsMap( int _num, ... );
extern void CreateAsynEventWithDSMap(int dsmapindex, int event_index);
extern UIViewController *g_controller;
extern UIView *g_glView;
extern int g_DeviceWidth;
extern int g_DeviceHeight;

@implementation ext_AppTrackingTransparency

-(double) AppTrackingTransparency_available
{
    if(@available(iOS 14.0, *))
		return 1.0;
	else
		return 0.0;
}
		
-(void) AppTrackingTransparency_request
{
	if(!(@available(iOS 14.0, *)))
		return;
	
	[ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status)
	{
		int dsMapIndex = CreateDsMap(1,"type", 0.0, "AppTrackingTransparency");
			CreateAsynEventWithDSMap(dsMapIndex,EVENT_OTHER_SOCIAL);
	}];
}

-(double) AppTrackingTransparency_status
{
	if(!(@available(iOS 14.0, *)))
		return -4;
	
	ATTrackingManagerAuthorizationStatus status = [ATTrackingManager trackingAuthorizationStatus];
	switch(status)
	{
		case ATTrackingManagerAuthorizationStatusNotDetermined:
			// The user has not yet received an authorization request to authorize access to app-related data that can be used for tracking the user or the device.
			return 0;
		case ATTrackingManagerAuthorizationStatusAuthorized:
			// The user authorizes access to app-related data that can be used for tracking the user or the device.
			return 1;
		case ATTrackingManagerAuthorizationStatusDenied:
			// The user denies authorization to access app-related data that can be used for tracking the user or the device.
			return 2;
		case ATTrackingManagerAuthorizationStatusRestricted:
			// The authorization to access app-related data that can be used for tracking the user or the device is restricted.
			return 3;
	}		
}

@end
