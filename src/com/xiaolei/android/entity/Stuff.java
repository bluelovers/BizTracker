/**
 * 
 */
package com.xiaolei.android.entity;

import java.util.Date;

/**
 * @author xiaolei
 * 
 */
public class Stuff {
	private int _id;
	private String Name;
	private String Tag;
	private String Picture;
	private Date LastUpdateTime;
	private Date LastUsedTime;

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		Name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return Name;
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
	 * @param picture
	 *            the picture to set
	 */
	public void setPicture(String picture) {
		Picture = picture;
	}

	/**
	 * @return the picture
	 */
	public String getPicture() {
		return Picture;
	}

	/**
	 * @param lastUpdateTime
	 *            the lastUpdateTime to set
	 */
	public void setLastUpdateTime(Date lastUpdateTime) {
		LastUpdateTime = lastUpdateTime;
	}

	/**
	 * @return the lastUpdateTime
	 */
	public Date getLastUpdateTime() {
		return LastUpdateTime;
	}

	/**
	 * @param _id the _id to set
	 */
	public void setId(int _id) {
		this._id = _id;
	}

	/**
	 * @return the _id
	 */
	public int getId() {
		return _id;
	}

	/**
	 * @param lastUsedTime the lastUsedTime to set
	 */
	public void setLastUsedTime(Date lastUsedTime) {
		LastUsedTime = lastUsedTime;
	}

	/**
	 * @return the lastUsedTime
	 */
	public Date getLastUsedTime() {
		return LastUsedTime;
	}
}
