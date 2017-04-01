package edu.asu.heal.promisapiv3.apiv31.model;

public class ActivatePatientBadge {

	private String patientPin;
	private String badgeId;
	private String activityInstanceId;
	private Boolean badgeUsed;
	private String powerupId;
	private int powerupCount;

	public ActivatePatientBadge(String patientPin, String badgeId, String activityInstanceId, Boolean badgeUsed,
			String powerupId, int powerupCount) {

		this.patientPin = patientPin;
		this.badgeId = badgeId;
		this.activityInstanceId = activityInstanceId;
		this.badgeUsed = badgeUsed;
		this.powerupId = powerupId;
		this.powerupCount = powerupCount;
	}

	public String getPatientPin() {
		return patientPin;
	}

	public void setPatientPin(String patientPin) {
		this.patientPin = patientPin;
	}

	public String getBadgeId() {
		return badgeId;
	}

	public void setBadgeId(String badgeId) {
		this.badgeId = badgeId;
	}

	public String getActivityInstanceId() {
		return activityInstanceId;
	}

	public void setActivityInstanceId(String activityInstanceId) {
		this.activityInstanceId = activityInstanceId;
	}

	public Boolean getBadgeUsed() {
		return badgeUsed;
	}

	public void setBadgeUsed(Boolean badgeUsed) {
		this.badgeUsed = badgeUsed;
	}

	public String getPowerupId() {
		return powerupId;
	}

	public void setPowerupId(String powerupId) {
		this.powerupId = powerupId;
	}

	public int getPowerupCount() {
		return powerupCount;
	}

	public void setPowerupCount(int powerupCount) {
		this.powerupCount = powerupCount;
	}
}
