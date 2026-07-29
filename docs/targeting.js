/**
 * @function admob_targeting_coppa
 * @desc Marks (or unmarks) ad requests as child-directed, for COPPA compliance. Must be called
 * **before** ${function.admob_initialize} - calling it after initialization has no effect.
 * @param {Bool} coppa `true` to tag requests as child-directed.
 * @function_end
 */

/**
 * @function admob_targeting_under_age
 * @desc Marks (or unmarks) the user as under the age of consent, for GDPR-related targeting rules.
 * Must be called **before** ${function.admob_initialize} - calling it after initialization has no
 * effect.
 * @param {Bool} under_age `true` to tag the user as under the age of consent.
 * @function_end
 */

/**
 * @function admob_targeting_max_ad_content_rating
 * @desc Sets the maximum content rating for ads returned by future ad requests. Must be called
 * **before** ${function.admob_initialize} - calling it after initialization has no effect.
 * @param {Enum.AdMobMaxAdContentRating} content_rating The maximum content rating to allow.
 * @function_end
 */

/**
 * @const AdMobMaxAdContentRating
 * @desc Extension-defined numeric mapping to Google's `G`/`PG`/`T`/`MA` content-rating constants,
 * used by ${function.admob_targeting_max_ad_content_rating}.
 * @member General Content suitable for a general audience.
 * @member ParentalGuidance Content suitable for most audiences with parental guidance.
 * @member Teen Content suitable for teen and older audiences.
 * @member MatureAudience Content suitable only for mature audiences.
 * @const_end
 */

/**
 * @module targeting
 * @title Targeting
 * @desc Functions for targeting ads to the right audience. All of these must be called before
 * ${function.admob_initialize}.
 *
 * @section_func
 * @ref admob_targeting_coppa
 * @ref admob_targeting_under_age
 * @ref admob_targeting_max_ad_content_rating
 * @section_end
 *
 * @section_const
 * @ref AdMobMaxAdContentRating
 * @section_end
 *
 * @module_end
 */
