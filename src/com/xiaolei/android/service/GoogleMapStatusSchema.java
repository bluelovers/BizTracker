package com.xiaolei.android.service;

/**
 * Google map geocoding API responses Json format.
 * Refer to: https://developers.google.com/maps/documentation/geocoding/?hl=fr#StatusCodes
 * @author xiaolei
 *
 */
public final class GoogleMapStatusSchema {
	public static final String results = "results";
	public static final String status = "status";
	public static final String formatted_address = "formatted_address";
}
