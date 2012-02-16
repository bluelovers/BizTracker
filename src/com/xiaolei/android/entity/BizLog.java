/**
 * 
 */
package com.xiaolei.android.entity;

import java.util.Date;

/**
 * @author xiaolei
 * 
 */
public class BizLog {
	private long Id;
	private int StuffId;
	private double Cost;
	private Date LastUpdateTime;
	private String CurrencyCode;
	private Boolean Star;
	private String Comment;
	private String stuffName;
	private String locationName;
	private String location;

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		Id = id;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return Id;
	}

	/**
	 * @param stuffId
	 *            the stuffId to set
	 */
	public void setStuffId(int stuffId) {
		StuffId = stuffId;
	}

	/**
	 * @return the stuffId
	 */
	public int getStuffId() {
		return StuffId;
	}

	/**
	 * @param cost
	 *            the cost to set
	 */
	public void setCost(double cost) {
		Cost = cost;
	}

	/**
	 * @return the cost
	 */
	public double getCost() {
		return Cost;
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
	 * @param currenceCode
	 *            the currenceCode to set
	 */
	public void setCurrencyCode(String currenceCode) {
		CurrencyCode = currenceCode;
	}

	/**
	 * @return the currenceCode
	 */
	public String getCurrencyCode() {
		return CurrencyCode != null ? CurrencyCode : "";
	}

	/**
	 * @param star
	 *            the star to set
	 */
	public void setStar(Boolean star) {
		Star = star;
	}

	/**
	 * @return the star
	 */
	public Boolean getStar() {
		return Star;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		Comment = comment;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return Comment;
	}

	/**
	 * @param stuffName the stuffName to set
	 */
	public void setStuffName(String stuffName) {
		this.stuffName = stuffName;
	}

	/**
	 * @return the stuffName
	 */
	public String getStuffName() {
		return stuffName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}


}
