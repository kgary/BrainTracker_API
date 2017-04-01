package edu.asu.heal.promisapiv3.apiv31.model;

public final class PatientPowerups {

	private String patientPin;
	private String powerupId;
	private int count;

	public PatientPowerups(String patientPin, String powerupId, int count) {
		this.patientPin = patientPin;
		this.powerupId = powerupId;
		this.count = count;
	}

	public String getPatientPin() {
		return patientPin;
	}

	public void setPatientPin(String patientPin) {
		this.patientPin = patientPin;
	}

	public String getPowerupId() {
		return powerupId;
	}

	public void setPowerupId(String powerupId) {
		this.powerupId = powerupId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
