package edu.asu.heal.promisapiv3.apiv31.model;

import java.util.Date;

public final class PatientBadges {

	private String patientPin;
	private String badgeId;
	private String activityInstanceId;
	private Boolean badgeUsed;

	public PatientBadges(String pin, String badgeId, String activityInstanceId, Boolean badgeUsed){
		this.patientPin = pin;
		this.badgeId = badgeId;
		this.activityInstanceId = activityInstanceId;
		this.badgeUsed = badgeUsed;
	}

	public Boolean getBadgeUsed() {
		return badgeUsed;
	}

	public void setBadgeUsed(Boolean badgeUsed) {
		this.badgeUsed = badgeUsed;
	}

	public void setPatientPin(String patientPin) {
		this.patientPin = patientPin;
	}

	public void setBadgeId(String badgeId) {
		this.badgeId = badgeId;
	}

	public void setActivityInstanceId(String activityInstanceId) {
		this.activityInstanceId = activityInstanceId;
	}

	public String getBadgeId(){
		return badgeId;
	}

	public String getActivityInstanceId(){
		return activityInstanceId;
	}

	public String getPatientPin(){
		return patientPin;
	}
}
