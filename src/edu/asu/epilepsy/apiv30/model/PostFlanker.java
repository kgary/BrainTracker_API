package edu.asu.epilepsy.apiv30.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public class PostFlanker extends PostActivity {
	 private int patientPin; 
	 private ArrayList<String> results;     
	 private int totalTimeTaken;
	 private float screenWidth;
	 private float screenHeight;
	 private Timestamp _userSubmittedTimeStamp;
	 private String surveyResults;
	 private String parameters;
	 
	 public PostFlanker(String activityId, int activityInstanceId, ArrayList<String> results,String surveyResults,String parameters,
			 int totalTimeTaken, float screenWidth, float screenHeight, Timestamp userSubmittedTimeStamp,int patintPin){
		 this._activityId = activityId;
		 this._activityInstanceId = activityInstanceId;
		 this.results = results;
		 this.totalTimeTaken = totalTimeTaken;
		 this.screenHeight = screenHeight;
		 this.screenWidth = screenWidth;
		 this._userSubmittedTimeStamp = userSubmittedTimeStamp;
		 this.patientPin = patintPin;
		 this.surveyResults=surveyResults;
		 this.parameters=parameters;
	 }

	 public int getPatientPin() {
		return patientPin;
	}

	public void setPatientPin(int patientPin) {
		this.patientPin = patientPin;
	}

	public ArrayList<String> getResults() {
		return results;
	}

	public void setResults(ArrayList<String> results) {
		this.results = results;
	}

	public int getTotalTimeTaken() {
		return totalTimeTaken;
	}

	public void setTotalTimeTaken(int totalTimeTaken) {
		this.totalTimeTaken = totalTimeTaken;
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

	public Timestamp get_userSubmittedTimeStamp() {
		return _userSubmittedTimeStamp;
	}

	public void set_userSubmittedTimeStamp(Timestamp _userSubmittedTimeStamp) {
		this._userSubmittedTimeStamp = _userSubmittedTimeStamp;
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


}
