/// @description Banner create/move

// If button was pressed
if(pressed)
{
	// We flip the bottom flag
	bottom = !bottom;
	
	// We change the position to the bottom flag
	// 1: places banner at bottom
	// 0: places banner at top
	AdMob_Banner_Move(real(bottom));
}
else
{
	// This was the first press
	pressed = true;

	// Create banner with selected type at the button of the screen
	AdMob_Banner_Create(banner_type, real(true));
}
