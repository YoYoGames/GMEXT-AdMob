/// @desc Request tracking


if (os_type != os_ios)
	room_goto(Room_AdMob);
else
{
	// Request tracking
	AppTrackingTransparency_request();
}



