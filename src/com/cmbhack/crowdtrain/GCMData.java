package com.cmbhack.crowdtrain;

import android.os.Bundle;

public class GCMData {
	private String mTrainID;
	private String mType;
	private String mName;
	private int mDelayTime;
	private Bundle mDataBundle;
	public String getTrainID() {
		return mTrainID;
	}
	private void setTrainID(String mTrainID) {
		this.mTrainID = mTrainID;
	}
	public String getType() {
		return mType;
	}
	private void setType(String mType) {
		this.mType = mType;
	}
	public String getName() {
		return mName;
	}
	private void setName(String mName) {
		this.mName = mName;
	}
	public int getDelayTime() {
		return mDelayTime;
	}
	private void setDelayTime(int mDelayTime) {
		this.mDelayTime = mDelayTime;
	}	
	
	public static GCMData parse(Bundle bundle){
		GCMData gcm =  new GCMData();
		gcm.setTrainID(bundle.getString("trainId"));
		gcm.setType(bundle.getString("type"));
		gcm.setDelayTime(bundle.getInt("delayTime"));
		gcm.setName(bundle.getString("trainDesc"));
		gcm.setDataBundle(bundle);
		return gcm;
	}
	public Bundle getDataBundle() {
		return mDataBundle;
	}
	private void setDataBundle(Bundle mDataBundle) {
		this.mDataBundle = mDataBundle;
	}
}
