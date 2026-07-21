/// @description Draw debug information

// This is for demo purposes and only draws debug information to the screen
draw_set_font(Font_YoYo_15);
draw_set_valign(fa_top);
draw_set_halign(fa_left);


var status = "UNKNOWN";
var type = "UNKNOWN";

switch (admob_consent_get_status())
{
    case AdMobConsentStatus.Unknown:
        status = "UNKNOWN";
    break;

    case AdMobConsentStatus.NotRequired:
        status = "NOT_REQUIRED";
    break;

    case AdMobConsentStatus.Required:
        status = "REQUIRED";
    break;

    case AdMobConsentStatus.Obtained:
        status = "OBTAINED";
    break;
}

switch (admob_consent_get_type())
{
    case AdMobConsentType.Unknown:
        type = "UNKNOWN";
    break;

    case AdMobConsentType.NonPersonalized:
        type = "NON_PERSONALIZED";
    break;

    case AdMobConsentType.Personalized:
        type = "PERSONALIZED";
    break;

    case AdMobConsentType.Declined:
        type = "DECLINED";
    break;
}


draw_text(x, y, "Consent Status: " + status);
draw_text(x, y + 35, "Consent Type: " + type);

// The function 'admob_consent_is_form_available()' will return either true or false depending
// on whether there is a consent form available or not on this device.
draw_text(x, y + 70, admob_consent_is_form_available() ? "Consent available" : "Consent unavailable");
