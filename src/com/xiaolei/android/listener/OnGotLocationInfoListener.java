package com.xiaolei.android.listener;

import android.location.Location;

public interface OnGotLocationInfoListener {
	void onGotLocation(Location currentLocation);

	void onGotLocationAddress(Location currentLocation, String address);
}
