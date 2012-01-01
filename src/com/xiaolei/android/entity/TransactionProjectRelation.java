/**
 * 
 */
package com.xiaolei.android.entity;

import java.util.Date;

/**
 * @author xiaolei
 * 
 */
public final class TransactionProjectRelation {
	private long _id;
	private long TransactionId;
	private long ProjectId;
	private Date CreatedTime = new Date();
	/**
	 * @param _id the _id to set
	 */
	public void set_id(long _id) {
		this._id = _id;
	}
	/**
	 * @return the _id
	 */
	public long get_id() {
		return _id;
	}
	/**
	 * @param transactionId the transactionId to set
	 */
	public void setTransactionId(long transactionId) {
		TransactionId = transactionId;
	}
	/**
	 * @return the transactionId
	 */
	public long getTransactionId() {
		return TransactionId;
	}
	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(long projectId) {
		ProjectId = projectId;
	}
	/**
	 * @return the projectId
	 */
	public long getProjectId() {
		return ProjectId;
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
}
