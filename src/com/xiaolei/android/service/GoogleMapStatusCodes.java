package com.xiaolei.android.service;

/**
 * Google map API status codes. Refer to:
 * https://developers.google.com/maps/documentation/geocoding/?hl=fr#StatusCodes
 * 
 * @author xiaolei
 * 
 */
public final class GoogleMapStatusCodes {
	/**
	 * Indicates that no errors occurred; the address was successfully parsed
	 * and at least one geocode was returned.
	 */
	public static final String OK = "OK";

	/**
	 * Indicates that the geocode was successful but returned no results. This
	 * may occur if the geocode was passed a non-existent address or a latlng in
	 * a remote location.
	 */
	public static final String ZERO_RESULTS = "ZERO_RESULTS";

	/**
	 * Indicates that you are over your quota.
	 */
	public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";

	/**
	 * Indicates that your request was denied, generally because of lack of a
	 * sensor parameter.
	 */
	public static final String REQUEST_DENIED = "REQUEST_DENIED";

	/**
	 * Generally indicates that the query (address or latlng) is missing.
	 */
	public static final String INVALID_REQUEST = "INVALID_REQUEST";
}
