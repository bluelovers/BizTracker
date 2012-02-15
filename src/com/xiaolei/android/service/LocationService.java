package com.xiaolei.android.service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

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

	private Context mContext;
	private static LocationService instance = null;
	private LocationManager locationManager = null;
	private LocationListener locationListener = null;
	private long minTime = 0;
	private float minDistance = 0;
	private boolean isListening = false;

	private boolean isNetworkProviderEnabled = false;
	private boolean isGpsProviderEnabled = false;

	private int networkStatus = -1;
	private int gpsStatus = -1;

	private Location lastKnownLocation = null;
	private Location currentBestLocation = null;
	private String currentBestLocationAddress = "";

	private LocationService(Context context) {
		mContext = context;
	}

	public static LocationService getInstance(Context context) {
		if (instance == null) {
			instance = new LocationService(context);
			instance.init();
		}

		return instance;
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

	/**
	 * Open the system location source settings activity.
	 */
	public void openLocationSourceSettingsActivity() {
		Intent intent = new Intent(
				android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		try {
			mContext.startActivity(intent);
		} catch (Exception ex) {
			// Do nothing
		}
	}

	private void getLocationAddressAsync(Location location) {
		if (location == null) {
			return;
		}

		AsyncTask<Location, Void, Void> task = new AsyncTask<Location, Void, Void>() {

			@Override
			protected Void doInBackground(Location... params) {
				if (params == null || params.length == 0 || params[0] == null) {
					return null;
				}
				Location location = params[0];
				Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
				try {
					List<Address> addresses = geocoder.getFromLocation(
							location.getLatitude(), location.getLongitude(), 1);
					if (addresses != null && addresses.size() > 0) {
						Address returnedAddress = addresses.get(0);
						StringBuilder strReturnedAddress = new StringBuilder();
						for (int i = 0; i < returnedAddress
								.getMaxAddressLineIndex(); i++) {
							strReturnedAddress.append(
									returnedAddress.getAddressLine(i)).append(
									"\n");
						}
						currentBestLocationAddress = strReturnedAddress
								.toString();
					}
				} catch (IOException e) {
					// Do nothing
				}

				return null;
			}

		};
		task.execute(currentBestLocation);
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
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				if (locationManager == null) {
					locationManager = (LocationManager) mContext
							.getSystemService(Context.LOCATION_SERVICE);
				}
				if (locationManager != null && locationListener != null) {
					isGpsProviderEnabled = locationManager
							.isProviderEnabled(LocationManager.GPS_PROVIDER);
					if (isGpsProviderEnabled) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER, minTime,
								minDistance, locationListener);
					}

					isNetworkProviderEnabled = locationManager
							.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
					if (isNetworkProviderEnabled) {
						locationManager.requestLocationUpdates(
								LocationManager.NETWORK_PROVIDER, minTime,
								minDistance, locationListener);
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

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				isListening = true;
			}

		};
		task.execute();
	}

	/**
	 * Stop listening the location changed event.
	 */
	public void stopListening() {
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
