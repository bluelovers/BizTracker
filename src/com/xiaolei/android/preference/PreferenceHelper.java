package com.xiaolei.android.preference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.xiaolei.android.entity.Parameter;
import com.xiaolei.android.entity.ParameterUtils;
import com.xiaolei.android.service.DatabaseHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

public class PreferenceHelper {
	/**
	 * Get the shared preferences instance of the active user.
	 * 
	 * @param context
	 * @return
	 */
	public static SharedPreferences getActiveUserSharedPreferences(
			Context context) {
		SharedPreferences result = null;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (prefs != null) {
			String activeUserId = prefs.getString(PreferenceKeys.ActiveUserId,
					"");
			if (!TextUtils.isEmpty(activeUserId)) {
				String userConfigFileName = getUserConfigFileName(context,
						activeUserId);
				result = context.getSharedPreferences(userConfigFileName,
						Context.MODE_PRIVATE);
			}
		}

		return result;
	}

	public static String getActiveUserConfigFileName(Context context) {
		String activeUserId = getActiveUserId(context);
		return getUserConfigFileName(context, activeUserId);
	}

	/**
	 * Find the user preference file name in the application default preference
	 * file. If the preference key don't exist, then generate one base on the
	 * user id.
	 * 
	 * @param context
	 * @param userId
	 * @return
	 */
	public static String getUserConfigFileName(Context context, String userId) {
		String result = "";
		if (TextUtils.isEmpty(userId)) {
			return result;
		} else {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			if (prefs != null) {
				String key = getUserConfigFileNameKey(userId);
				result = prefs.getString(key, "");
				if (TextUtils.isEmpty(result)) {
					result = String.format("user-config-%s", userId);
				}
			}
		}

		return result;
	}

	/**
	 * Find the user database file name in the application default preference
	 * file. If the preference key don't exist, then generate one base on the
	 * user id.
	 * 
	 * @param context
	 * @param userId
	 * @return
	 */
	public static String getUserDatabaseFileName(Context context, String userId) {
		String result = "";
		if (TextUtils.isEmpty(userId)) {
			return result;
		} else {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			if (prefs != null) {
				String key = getUserDatabaseFileNameKey(userId);
				result = prefs.getString(key, "");
				if (TextUtils.isEmpty(result)) {
					result = String.format("user-db-%s.db", userId);
				}
			}
		}

		return result;
	}

	public static String getUserDatabaseFileNameKey(String userId) {
		return String.format("user-db-file-name-%s", userId);
	}

	public static String getUserConfigFileNameKey(String userId) {
		return String.format("user-config-file-name-%s", userId);
	}

	/**
	 * Generate a random user id.
	 * 
	 * @return
	 */
	public static String generateNewUserId() {
		Date date = new Date();
		return String.format("%s-%s", UUID.randomUUID().toString(),
				String.valueOf(date.getTime()));
	}

	/**
	 * Check whether the active user id is persisted in the default application
	 * preference file.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean hasActiveUserId(Context context) {
		boolean result = false;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (prefs != null) {
			String activeUserId = prefs.getString(PreferenceKeys.ActiveUserId,
					"");
			return !TextUtils.isEmpty(activeUserId);
		}

		return result;
	}

	public static String getActiveUserId(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (prefs != null) {
			String activeUserId = prefs.getString(PreferenceKeys.ActiveUserId,
					"");
			return activeUserId;
		} else {
			return "";
		}
	}

	/**
	 * If active user id is not configured in application preference file, then
	 * create a new user id and add related key values for it.
	 * 
	 * @param context
	 */
	public static void createActiveUserRelatedPreferencesIfNeeds(Context context) {
		if (hasActiveUserId(context) == false) {
			String newUserId = generateNewUserId();
			String userConfigKey = getUserConfigFileNameKey(newUserId);
			String userDbKey = getUserDatabaseFileNameKey(newUserId);
			String userConfigFileName = getUserConfigFileName(context,
					newUserId);
			String userDbFileName = DatabaseHelper.databaseName; // Use old db
																	// file

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			if (prefs != null) {
				Editor editor = prefs.edit();
				if (editor != null) {
					editor.putString("user-" + newUserId, newUserId);
					editor.putString(userConfigKey, userConfigFileName);
					editor.putString(userDbKey, userDbFileName);
					editor.putString(PreferenceKeys.ActiveUserId, newUserId);
				}

				editor.commit();
			}
		}
	}

	/**
	 * Write the active user id to the application preference file.
	 * 
	 * @param context
	 * @param userId
	 */
	public static void setActiveUser(Context context, String userId) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (prefs != null) {
			Editor editor = prefs.edit();
			if (editor != null) {
				editor.putString(PreferenceKeys.ActiveUserId, userId);
			}

			editor.commit();
		}
	}

	/**
	 * Create a new user id then write its related key values to application
	 * default preference file.
	 * 
	 * @param context
	 */
	public static void createNewUserPreference(Context context) {
		String newUserId = generateNewUserId();
		String userConfigKey = getUserConfigFileNameKey(newUserId);
		String userDbKey = getUserDatabaseFileNameKey(newUserId);
		String userConfigFileName = getUserConfigFileName(context, newUserId);
		String userDbFileName = getUserDatabaseFileName(context, newUserId);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (prefs != null) {
			Editor editor = prefs.edit();
			if (editor != null) {
				editor.putString("user-" + newUserId, newUserId);
				editor.putString(userConfigKey, userConfigFileName);
				editor.putString(userDbKey, userDbFileName);
			}

			editor.commit();
		}
	}

	/**
	 * Get all user id list from application default preference file.
	 * 
	 * @param context
	 * @return
	 */
	public static List<String> getAllUserIds(Context context) {
		List<String> result = new ArrayList<String>();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (prefs != null) {
			Set<String> keys = prefs.getAll().keySet();
			for (String key : keys) {
				if (key.startsWith("user-")) {
					String value = prefs.getString(key, "");
					if (!TextUtils.isEmpty(value)) {
						result.add(value);
					}
				}
			}
		}

		return result;
	}

	/**
	 * Migrate the old parameter values from sqlite DB to SharedPreference.
	 */
	public static void migrateOldPreferencesIfNeed(Context context) {
		SharedPreferences pref = PreferenceHelper
				.getActiveUserSharedPreferences(context);
		if (pref != null) {
			SharedPreferences defaultPrefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			if (defaultPrefs != null
					&& defaultPrefs
							.getBoolean(PreferenceKeys.IsMigrated, false) == false) {
				try {
					// Migrate volume value
					int volume = ParameterUtils.getIntParameterValue(context,
							Parameter.VOLUME, -1);
					if (volume != -1) {
						Editor editor = pref.edit();
						if (editor != null) {
							editor.putInt(PreferenceKeys.Volume, volume);
							editor.commit();
						}
					}

					// Migrate default currency code
					String defaultCurrencyCode = ParameterUtils
							.getParameterValue(context,
									PreferenceKeys.DefaultCurrencyCode, "");
					if (!TextUtils.isEmpty(defaultCurrencyCode)) {
						Editor editor = pref.edit();
						if (editor != null) {
							editor.putString(
									PreferenceKeys.DefaultCurrencyCode,
									defaultCurrencyCode);
							editor.commit();
						}
					}

					// Do not migrate the password, because the encrypt method
					// is changed.
				} catch (Exception ex) {
					Log.e("BizTracker", ex.getMessage());
				} finally {
					Editor editor = defaultPrefs.edit();
					if (editor != null) {
						editor.putBoolean(PreferenceKeys.IsMigrated, true);
						editor.commit();
					}
				}
			}
		}
	}
}
