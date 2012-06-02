package com.xiaolei.android.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.xiaolei.android.common.Utility;
import com.xiaolei.android.listener.OnGotLocationInfoListener;

/**
 * Provide a service to get the current location. Refer to:
 * http://developer.android
 * .com/guide/topics/location/obtaining-user-location.html
 * 
 * @author xiaolei
 * 
 */
public class LocationService {
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private final String GoogleMapAPITemplate = "http://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&sensor=true&language=%s";

	private Context mContext;
	private String mLastErrorMessage = null;
	private boolean mIsPaused = false;
	private static LocationService instance = null;
	private LocationManager locationManager = null;
	private LocationListener locationListener = null;
	private long minTime = 30000;
	private float minDistance = 10;
	private boolean isListening = false;

	private boolean isNetworkProviderEnabled = false;
	private boolean isGpsProviderEnabled = false;

	private int networkStatus = -1;
	private int gpsStatus = -1;

	private Location lastKnownLocation = null;
	private Location currentBestLocation = null;
	private String currentBestLocationAddress = "";

	private OnGotLocationInfoListener mOnGotLocationInfoListener;

	private LocationService(Context context) {
		mContext = context;
	}

	public static LocationService getInstance(Context context) {
		if (instance == null) {
			instance = new LocationService(context);
		}

		return instance;
	}

	public void setOnGotLocationInfoListener(OnGotLocationInfoListener listener) {
		mOnGotLocationInfoListener = listener;
	}

	protected void onGotLocation(Location location) {
		if (!mIsPaused && mOnGotLocationInfoListener != null
				&& location != null) {
			mOnGotLocationInfoListener.onGotLocation(location);
		}
	}

	protected void onGotAddress(String errorMessage, Location location,
			String address) {
		if (!mIsPaused && mOnGotLocationInfoListener != null
				&& location != null) {
			mOnGotLocationInfoListener.onGotLocationAddress(errorMessage,
					location, address);
		}
	}

	/**
	 * Start to track the location changed event.
	 */
	public void start() {
		if (!isListening) {
			this.init();
		} else {
			// Do nothing.
		}
	}
	
	/**
	 * Detect if the location service is paused.
	 */
	public boolean isPaused(){
		return mIsPaused;
	}

	/**
	 * Pause notifying location and address changed event.
	 */
	public void pause() {
		mIsPaused = true;
	}

	/**
	 * Continue to notify location and address changed event.
	 */
	public void resume() {
		mIsPaused = false;
	}

	/**
	 * Get the current best location.
	 * 
	 * @return
	 */
	public Location getCurrentBestLocation() {
		return currentBestLocation;
	}

	/**
	 * Get the current best location address.
	 * 
	 * @return
	 */
	public String getLastBestLocationAddress() {
		return currentBestLocationAddress;
	}

	private void setCurrentBestLocation(Location location) {
		if (location != null) {
			this.onGotLocation(location);
		}
	}

	private void setCurrentBestLocationAddress(Location location, String address) {
		if (!TextUtils.isEmpty(address)
				&& !address.equals(currentBestLocationAddress)) {
			currentBestLocationAddress = address;
		}

		this.onGotAddress(mLastErrorMessage, location, address);
	}

	/**
	 * Open the system location source settings activity.
	 */
	public static void tryOpenLocationSourceSettingsActivity(Activity context,
			int requestCode) {
		Intent intent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		try {
			context.startActivityForResult(intent, requestCode);
		} catch (Exception ex) {
			// Do nothing
		}
	}

	private void getLocationAddressByGoogleMapAsync(Location location) {
		if (location == null || mIsPaused) {
			return;
		}

		AsyncTask<Location, Void, String> task = new AsyncTask<Location, Void, String>() {

			@Override
			protected String doInBackground(Location... params) {
				if (params == null || params.length == 0 || params[0] == null) {
					return null;
				}
				Location location = params[0];
				String address = "";
				mLastErrorMessage = "";

				String cachedAddress = DataService.GetInstance(mContext)
						.getAddressFormLocationCache(location.getLatitude(),
								location.getLongitude());
				if (!TextUtils.isEmpty(cachedAddress)) {
					address = cachedAddress;
				} else {
					StringBuilder jsonText = new StringBuilder();
					HttpClient client = new DefaultHttpClient();
					String url = String.format(GoogleMapAPITemplate,
							location.getLatitude(), location.getLongitude(),
							Utility.getCurrentLanguageCode());
					HttpGet httpGet = new HttpGet(url);
					try {
						HttpResponse response = client.execute(httpGet);
						StatusLine statusLine = response.getStatusLine();
						int statusCode = statusLine.getStatusCode();
						if (statusCode == 200) {
							HttpEntity entity = response.getEntity();
							InputStream content = entity.getContent();
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(content));
							String line;
							while ((line = reader.readLine()) != null) {
								jsonText.append(line);
							}

							JSONObject result = new JSONObject(
									jsonText.toString());
							String status = result
									.getString(GoogleMapStatusSchema.status);
							if (GoogleMapStatusCodes.OK.equals(status)) {
								JSONArray addresses = result
										.getJSONArray(GoogleMapStatusSchema.results);
								if (addresses.length() > 0) {
									address = addresses
											.getJSONObject(0)
											.getString(
													GoogleMapStatusSchema.formatted_address);
									if (!TextUtils
											.isEmpty(currentBestLocationAddress)) {
										DataService
												.GetInstance(mContext)
												.updateAddressToLocationCache(
														location.getLatitude(),
														location.getLongitude(),
														currentBestLocationAddress);
									}
								}
							} else if (GoogleMapStatusCodes.ZERO_RESULTS
									.equals(status)) {
								address = "";
								Log.e("Error", "Zero result fetched.");
							} else {
								address = "";
							}
						} else {
							mLastErrorMessage = "Failed to get address via google map API.";
							Log.e("Error",
									"Failed to get address via google map API.");
						}
					} catch (ClientProtocolException e) {
						e.printStackTrace();
						mLastErrorMessage = e.getMessage();
					} catch (IOException e) {
						e.printStackTrace();
						mLastErrorMessage = e.getMessage();
					} catch (JSONException e) {
						e.printStackTrace();
						mLastErrorMessage = e.getMessage();
					}
				}

				return address;
			}

			@Override
			protected void onPostExecute(String result) {
				setCurrentBestLocationAddress(currentBestLocation, result);
			}
		};

		task.execute(currentBestLocation);
	}

	private void getLocationAddressAsync(Location location) {
		if (location == null) {
			return;
		}

		getLocationAddressByGoogleMapAsync(location);
	}

	/**
	 * Check whether the given location provider is enabled in the system
	 * settings.
	 * 
	 * @param provider
	 *            Use LocationManager.xxx
	 * @return
	 */
	public boolean isProviderEnable(String provider) {
		if (locationManager == null) {
			locationManager = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);
		}

		if (locationManager != null) {
			return locationManager.isProviderEnabled(provider);
		}

		return false;
	}

	private void init() {
		// Define a listener that responds to location updates
		if (locationListener == null) {
			locationListener = new LocationListener() {

				@Override
				public void onLocationChanged(Location location) {
					lastKnownLocation = location;
					if (isBetterLocation(location, currentBestLocation)) {
						currentBestLocation = location;
						getLocationAddressAsync(currentBestLocation);
						setCurrentBestLocation(currentBestLocation);

						/*
						 * Toast.makeText( mContext, String.format("%f, %f",
						 * location.getLatitude(), location.getLongitude()),
						 * Toast.LENGTH_SHORT).show();
						 */
					}
				}

				@Override
				public void onStatusChanged(String provider, int status,
						Bundle extras) {
					if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider)) {
						gpsStatus = status;
					} else if (LocationManager.NETWORK_PROVIDER
							.equalsIgnoreCase(provider)) {
						networkStatus = status;
					}
				}

				@Override
				public void onProviderEnabled(String provider) {
					if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider)) {
						isGpsProviderEnabled = true;
					} else if (LocationManager.NETWORK_PROVIDER
							.equalsIgnoreCase(provider)) {
						isNetworkProviderEnabled = true;
					}
				}

				@Override
				public void onProviderDisabled(String provider) {
					if (LocationManager.GPS_PROVIDER.equalsIgnoreCase(provider)) {
						isGpsProviderEnabled = false;
					} else if (LocationManager.NETWORK_PROVIDER
							.equalsIgnoreCase(provider)) {
						isNetworkProviderEnabled = false;
					}
				}

			};
		}

		listener();
	}

	private void listener() {
		if (isListening) {
			return;
		}

		isListening = true;

		if (locationManager == null) {
			locationManager = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);
		}
		if (locationManager != null && locationListener != null) {
			isGpsProviderEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if (isGpsProviderEnabled) {
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, minTime, minDistance,
						locationListener);
			}

			isNetworkProviderEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (isNetworkProviderEnabled) {
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, minTime, minDistance,
						locationListener);
			}

			if (isNetworkProviderEnabled) {
				lastKnownLocation = locationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}

			if (lastKnownLocation == null && isGpsProviderEnabled) {
				lastKnownLocation = locationManager
						.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
		}
	}

	/**
	 * Stop tracking the location changed event.
	 */
	public void stop() {
		if (locationManager != null && locationListener != null) {
			locationManager.removeUpdates(locationListener);
			isListening = false;
		}
	}

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	protected boolean isBetterLocation(Location location,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	public int getGpsStatus() {
		return gpsStatus;
	}

	public int getNetworkStatus() {
		return networkStatus;
	}

	public boolean getIsGpsProviderEnabled() {
		return isGpsProviderEnabled;
	}

	public boolean getIsNetworkProviderEnabled() {
		return isNetworkProviderEnabled;
	}

	public boolean getIsListening() {
		return isListening;
	}

}
