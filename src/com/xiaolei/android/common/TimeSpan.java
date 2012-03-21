package com.xiaolei.android.common;

public class TimeSpan {

	private static final long MILLIES_IN_SEC = 1000;
	private static final long SECS_IN_MINUTE = 60;
	private static final long MINUTES_IN_HOUR = 60;
	private static final long HOURS_IN_DAY = 24;

	private long mMillies;
	private long mSeconds;
	private long mMinutes;
	private long mHours;
	private long mDays;

	public TimeSpan(long totalMillies) {
		mMillies = totalMillies % MILLIES_IN_SEC;
		totalMillies /= MILLIES_IN_SEC;
		mSeconds = totalMillies % SECS_IN_MINUTE;
		totalMillies /= SECS_IN_MINUTE;
		mMinutes = totalMillies % MINUTES_IN_HOUR;
		totalMillies /= MINUTES_IN_HOUR;
		mHours = totalMillies % HOURS_IN_DAY;
		totalMillies /= HOURS_IN_DAY;
		mDays = totalMillies;
	}

	public long getDays() {
		return mDays;
	}

	public long getHours() {
		return mHours;
	}

	public long getMinutes() {
		return mMinutes;
	}

	public long getSeconds() {
		return mSeconds;
	}

	public long getMillies() {
		return mMillies;
	}

	@Override
	public String toString() {
		return String.format("%02d:%02d:%02d", mHours, mMinutes, mSeconds);
	}
}