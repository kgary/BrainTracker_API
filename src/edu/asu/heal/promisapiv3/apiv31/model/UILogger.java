package edu.asu.heal.promisapiv3.apiv31.model;

import java.sql.Timestamp;

public final class UILogger {

	private String patientPin;
	private String eventName;
	private String metaData;
	private Timestamp eventTime;

	public UILogger(String patientPin, String eventName, String metaData, Timestamp eventTime){

		this.patientPin = patientPin;
		this.eventName = eventName;
		this.metaData = metaData;
		this.eventTime = eventTime;
	}

	public String getPatientPin() {
		return patientPin;
	}

	public String getEventName() {
		return eventName;
	}

	public String getMetaData() {
		return metaData;
	}

	public Timestamp getEventTime() {
		return eventTime;
	}

	public void setPatientPin(String patientPin) {
		this.patientPin = patientPin;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	public void setEventTime(Timestamp eventTime) {
		this.eventTime = eventTime;
	}
}
