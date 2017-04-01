package edu.asu.heal.promisapiv3.apiv31.model;

import java.sql.Timestamp;

public final class PatientGamePlay {

	private String patientPin;
	private String gameId;
	private Timestamp startTime;
	private Timestamp endTime;

	public PatientGamePlay(String patientPin, String gameId, Timestamp startTime, Timestamp endTime){

		this.patientPin = patientPin;
		this.gameId = gameId;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public String getPatientPin() {
		return patientPin;
	}

	public void setPatientPin(String patientPin) {
		this.patientPin = patientPin;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}
}
