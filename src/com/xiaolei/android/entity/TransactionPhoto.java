/**
 * 
 */
package com.xiaolei.android.entity;

import java.util.Date;

/**
 * @author xiaolei
 * 
 */
public final class TransactionPhoto {
	private long id;
	private long BizLogId;
	private String FileName;
	private byte[] Thumbnail;
	private Date CreatedTime = new Date();
	private String Comment;
	private String Name;
	private String Tag;
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param bizLogId the bizLogId to set
	 */
	public void setBizLogId(long bizLogId) {
		BizLogId = bizLogId;
	}
	/**
	 * @return the bizLogId
	 */
	public long getBizLogId() {
		return BizLogId;
	}
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		FileName = fileName;
	}
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return FileName;
	}
	/**
	 * @param thumbnail the thumbnail to set
	 */
	public void setThumbnail(byte[] thumbnail) {
		Thumbnail = thumbnail;
	}
	/**
	 * @return the thumbnail
	 */
	public byte[] getThumbnail() {
		return Thumbnail;
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
	 * @param comment the comment to set
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
