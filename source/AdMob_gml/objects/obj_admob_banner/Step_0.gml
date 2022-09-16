/// @description Handle device flipping

// Inherit the parent event
event_inherited();

// When flipping the phone the banner doesn't get positioned in the correct place
// to solve that we need some special handling. The banner needs to be destroyed
// and created again for the correct dimensions to be applied.

// For handling with flipping vertical/horizontal position we check:
// 1) if the button was already pressed (means there is a banner on screen)
// 2) if the display height as changed (this happens when flipping)
if (pressed && displayHeight != display_get_height())
{
	// We remove the previous banner
	AdMob_Banner_Remove();
	
	// And create a new one (this one will have right dimensions)
	AdMob_Banner_Create(banner_type, bottom);
	
	// We refresh the display size
	displayHeight = display_get_height();
}