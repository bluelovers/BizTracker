package com.xiaolei.android.entity;

import java.util.Date;

public class VoiceNote {
	private long Id;
	private long TransactionId;
	private String FileName;
	private long Duration;
	private String Title;
	private String Summary;
	private String Tag;
	private Date LastUpdatedTime;

	public long getId() {
		return Id;
	}

	public void setId(long id) {
		Id = id;
	}

	public long getTransactionId() {
		return TransactionId;
	}

	public void setTransactionId(long transactionId) {
		TransactionId = transactionId;
	}

	public String getFileName() {
		return FileName;
	}

	public void setFileName(String fileName) {
		FileName = fileName;
	}

	public long getDuration() {
		return Duration;
	}

	public void setDuration(long duration) {
		Duration = duration;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public String getSummary() {
		return Summary;
	}

	public void setSummary(String summary) {
		Summary = summary;
	}

	public String getTag() {
		return Tag;
	}

	public void setTag(String tag) {
		Tag = tag;
	}

	public Date getLastUpdatedTime() {
		return LastUpdatedTime;
	}

	public void setLastUpdatedTime(Date lastUpdatedTime) {
		LastUpdatedTime = lastUpdatedTime;
	}
}
