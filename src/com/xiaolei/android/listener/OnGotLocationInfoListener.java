package com.xiaolei.android.listener;

import android.location.Location;

public interface OnGotLocationInfoListener {
	void onGotLocation(Location currentLocation);

	void onGotLocationAddress(String errorMessage, Location currentLocation, String address);
}
