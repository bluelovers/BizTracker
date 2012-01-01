/**
 * 
 */
package com.xiaolei.android.entity;

import android.content.Intent;

/**
 * @author xiaolei
 *
 */
public class ControlPanelItem {

	private String Name;
	private int IconResourceId;
	private Intent intent;
	
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
	 * @param iconResourceId the iconResourceId to set
	 */
	public void setIconResourceId(int iconResourceId) {
		IconResourceId = iconResourceId;
	}
	/**
	 * @return the iconResourceId
	 */
	public int getIconResourceId() {
		return IconResourceId;
	}
	/**
	 * @param intent the intent to set
	 */
	public void setIntent(Intent intent) {
		this.intent = intent;
	}
	/**
	 * @return the intent
	 */
	public Intent getIntent() {
		return intent;
	}
}
