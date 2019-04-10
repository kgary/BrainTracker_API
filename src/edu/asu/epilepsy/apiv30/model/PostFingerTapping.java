package edu.asu.epilepsy.apiv30.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public class PostFingerTapping extends PostActivity{
	 private int patientPin; 
	 private HashMap<String,Integer> results;     
	 private int timeToTap;
	 private float screenWidth;
	 private float screenHeight;
	 private int timeToComplete;
	 private Timestamp _userSubmittedTimeStamp;
	 private String surveyResults;
	 private String parameters;
	 private double score;

	 
	public PostFingerTapping(String activityId, int activityInstanceId, 
			HashMap<String, Integer> results,String surveyResults,String parameters, int timeToTap, float screenWidth, float screenHeight,
			double score,int timeToComplete, Timestamp userSubmittedTimeStamp,int patintPin) {
		super();
		this._activityId = activityId;
		this._activityInstanceId = activityInstanceId;
		this.results = results;
		this.surveyResults=surveyResults;
		this.parameters=parameters;
		this.timeToTap = timeToTap;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.timeToComplete = timeToComplete;
		this._userSubmittedTimeStamp = userSubmittedTimeStamp;
		this.patientPin = patintPin;
		this.score=score;
	}
	public int getPatientPin() {
		return patientPin;
	}
	public void setPatientPin(int patientPin) {
		this.patientPin = patientPin;
	}
	public Timestamp get_userSubmittedTimeStamp() {
		return _userSubmittedTimeStamp;
	}
	public void set_userSubmittedTimeStamp(Timestamp _userSubmittedTimeStamp) {
		this._userSubmittedTimeStamp = _userSubmittedTimeStamp;
	}
	public HashMap<String, Integer> getResults() {
		return results;
	}
	public void setResults(HashMap<String, Integer> results) {
		this.results = results;
	}
	public int getTimeToTap() {
		return timeToTap;
	}
	public void setTimeToTap(int timeToTap) {
		this.timeToTap = timeToTap;
	}
	public float getScreenWidth() {
		return screenWidth;
	}
	public void setScreenWidth(float screenWidth) {
		this.screenWidth = screenWidth;
	}
	public float getScreenHeight() {
		return screenHeight;
	}
	public void setScreenHeight(float screenHeight) {
		this.screenHeight = screenHeight;
	}
	public int getTimeToComplete() {
		return timeToComplete;
	}
	public void setTimeToComplete(int timeToComplete) {
		this.timeToComplete = timeToComplete;
	}

	public String getSurveyResults() {
		return surveyResults;
	}

	public void setSurveyResults(String surveyResults) {
		this.surveyResults = surveyResults;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	 
}
