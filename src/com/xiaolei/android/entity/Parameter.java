/**
 * 
 */
package com.xiaolei.android.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.TextUtils;

/**
 * @author xiaolei
 * 
 */
public final class Parameter {
	public static final String VOLUME = "Volume";

	private String Key;

	private String Value;

	private int ValueType;

	private int Id;

	public Boolean isEmpty() {
		return TextUtils.isEmpty(Key);
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		Key = key;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return Key;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		Value = value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return Value;
	}

	/**
	 * @param lastUpdateTime
	 *            the lastUpdateTime to set
	 */
	public void setLastUpdateTime(Date lastUpdateTime) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		LastUpdateTime = format.format(lastUpdateTime);
	}

	/**
	 * @return the lastUpdateTime
	 */
	public String getLastUpdateTime() {
		return LastUpdateTime;
	}

	/**
	 * @param tag
	 *            the tag to set
	 */
	public void setTag(String tag) {
		Tag = tag;
	}

	/**
	 * @return the tag
	 */
	public String getTag() {
		return Tag;
	}

	/**
	 * @param valueType
	 *            the valueType to set
	 */
	public void setValueType(int valueType) {
		ValueType = valueType;
	}

	/**
	 * @return the valueType
	 */
	public int getValueType() {
		return ValueType;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		Id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return Id;
	}

	private String Tag;

	private String LastUpdateTime;

	public int getIntegerValue(int defaultValue) {
		try {
			return Integer.parseInt(Value);
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public double getDoubleValue(double defaultValue) {
		try {
			return Double.valueOf(Value);
		} catch (Exception ex) {
			return defaultValue;
		}
	}
	
	public float getFloatValue(float defaultValue) {
		try {
			return Float.valueOf(Value);
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public Boolean getBooleanValue(Boolean defaultValue) {
		try {
			return Boolean.getBoolean(Value);
		} catch (Exception ex) {
			return defaultValue;
		}
	}
}
