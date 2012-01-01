/**
 * 
 */
package com.xiaolei.android.entity;

import java.util.Date;

/**
 * @author xiaolei
 * 
 */
public class Project {
	private long Id;
	private String Name;
	private String Description;
	private Date CreatedTime;
	private Date LastUpdateTime;
	private Boolean IsActive;
	private String Tag;
	/**
	 * @param id the id to set
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
	 * @param name the name to set
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
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		Description = description;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return Description;
	}
	/**
	 * @param createdTime the createdTime to set
	 */
	public void setCreatedTime(Date createdTime) {
		CreatedTime = createdTime;
	}
	/**
	 * @return the createdTime
	 */
	public Date getCreatedTime() {
		return CreatedTime;
	}
	/**
	 * @param lastUpdateTime the lastUpdateTime to set
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
	 * @param isActive the isActive to set
	 */
	public void setIsActive(Boolean isActive) {
		IsActive = isActive;
	}
	/**
	 * @return the isActive
	 */
	public Boolean getIsActive() {
		return IsActive;
	}
	/**
	 * @param tag the tag to set
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
}
