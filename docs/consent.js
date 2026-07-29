/**
 * @function admob_consent_request_info_update
 * @desc Requests the user's current consent status from Google's User Messaging Platform (UMP). Call
 * this before any other function in this module - ${function.admob_consent_get_status}/
 * ${function.admob_consent_get_type}/${function.admob_consent_is_form_available} all report
 * `${constant.AdMobConsentStatus}.Unknown`/`${constant.AdMobConsentType}.Unknown`/`false` until this
 * has completed successfully at least once.
 * @param {Enum.AdMobConsentDebugGeography} debug_geography A debug geography to simulate (this
 * device is automatically registered as a test device for the simulation), or
 * ${constant.AdMobConsentDebugGeography}.Disabled for real, non-debug behavior.
 * @param {Function} callback The function to call once the request completes or fails.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if the request was accepted, an error code
 * otherwise.
 * @event callback
 * @desc Fires once, when the consent info request completes or fails.
 * @member {Struct.AdMobResult} result The request outcome.
 * @event_end
 * @function_end
 */

/**
 * @function admob_consent_get_status
 * @desc Gets the user's current consent status, as of the last
 * ${function.admob_consent_request_info_update} call.
 * @returns {Enum.AdMobConsentStatus} The current consent status.
 * @function_end
 */

/**
 * @function admob_consent_get_type
 * @desc Gets a coarse classification of the user's consent choice. This is an extension-defined
 * convenience derived from the platform consent APIs - current Google UMP does not itself expose a
 * personalized/non-personalized/declined classification, so treat this as a best-effort helper, not
 * an official Google value.
 * @returns {Enum.AdMobConsentType} The classified consent type.
 * @function_end
 */

/**
 * @function admob_consent_is_form_available
 * @desc Checks whether a consent form is available to load, as of the last
 * ${function.admob_consent_request_info_update} call.
 * @returns {Bool} `true` if a consent form is available.
 * @function_end
 */

/**
 * @function admob_consent_load
 * @desc Loads the consent form, so it's ready to present with ${function.admob_consent_show}.
 * @param {Function} callback The function to call once the load completes or fails.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if the load request was accepted, an error
 * code otherwise.
 * @event callback
 * @desc Fires once, when the load completes or fails.
 * @member {Struct.AdMobResult} result The load outcome.
 * @event_end
 * @function_end
 */

/**
 * @function admob_consent_show
 * @desc Presents the consent form previously loaded with ${function.admob_consent_load}.
 * [[Note: If no form is currently loaded, this silently does nothing - `callback` never fires. Always
 * wait for a successful ${function.admob_consent_load} callback first.]]
 * @param {Function} callback The function to call once the consent form is dismissed.
 * @returns {Enum.AdMobError} ${constant.AdMobError}.Ok if the show request was accepted, an error
 * code otherwise.
 * @event callback
 * @desc Fires once, when the user dismisses the consent form.
 * @member {Struct.AdMobResult} result The outcome.
 * @event_end
 * @function_end
 */

/**
 * @function admob_consent_reset
 * @desc Resets all locally-stored consent state, as if the app were freshly installed. Intended for
 * testing the consent flow.
 * @function_end
 */

/**
 * @function admob_consent_set_rdp
 * @desc Sets the "Restricted Data Processing" flag Google's SDK attaches to ad requests, for CCPA
 * compliance when a user has opted out of the sale of their personal information.
 * @param {Bool} enabled `true` to request restricted data processing on future ad requests.
 * @function_end
 */

/**
 * @const AdMobConsentDebugGeography
 * @desc A geography to simulate for consent-flow testing, used by
 * ${function.admob_consent_request_info_update}. Values match Android
 * `ConsentDebugSettings.DebugGeography` and iOS `UMPDebugGeography`.
 * @member Disabled No debug geography - use the device's real location.
 * @member EEA Simulate a location inside the European Economic Area.
 * @member NotEEA Simulate a location outside the EEA. Deprecated by Google - use `Other` instead.
 * @member RegulatedUSState Simulate a location in a regulated US state.
 * @member Other Simulate a location outside the EEA and not a regulated US state.
 * @const_end
 */

/**
 * @const AdMobConsentStatus
 * @desc The user's consent status, as returned by ${function.admob_consent_get_status}. Values follow
 * Android's `ConsentInformation.ConsentStatus` numbering.
 * @member Unknown Consent status is unknown - ${function.admob_consent_request_info_update} hasn't
 * completed successfully yet.
 * @member NotRequired Consent is not required for this user.
 * @member Required Consent is required but has not yet been given.
 * @member Obtained Consent has been given (or is not required and has been acknowledged).
 * @const_end
 */

/**
 * @const AdMobConsentType
 * @desc An extension-defined classification of the user's consent choice, returned by
 * ${function.admob_consent_get_type}. Current Google UMP does not expose this classification directly
 * - do not assume it is derived the same way on every platform.
 * @member Unknown The consent type could not be classified.
 * @member NonPersonalized The user consented to non-personalized ads only.
 * @member Personalized The user consented to personalized ads.
 * @member Declined The user declined to consent (ads cannot be shown).
 * @const_end
 */

/**
 * @module consent
 * @title Consent
 * @desc Functions for requesting and reading the user's consent status via Google's User Messaging
 * Platform (UMP), for GDPR/CCPA compliance.
 *
 * @section_func
 * @ref admob_consent_request_info_update
 * @ref admob_consent_get_status
 * @ref admob_consent_get_type
 * @ref admob_consent_is_form_available
 * @ref admob_consent_load
 * @ref admob_consent_show
 * @ref admob_consent_reset
 * @ref admob_consent_set_rdp
 * @section_end
 *
 * @section_const
 * @ref AdMobConsentDebugGeography
 * @ref AdMobConsentStatus
 * @ref AdMobConsentType
 * @section_end
 *
 * @module_end
 */
