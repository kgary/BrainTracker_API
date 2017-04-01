package edu.asu.heal.promisapiv3.apiv30.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is the model for the painIntensity for submitSurvey.
 * @author Deepak S N
 */
public class PostPainIntensity extends PostActivity{
	
	 private HashMap<String,Integer> _questionIds;     
	 private Integer _bodyPainLocation;
	 private Integer _bodyPainIntensity;
	 private ArrayList<Integer> _generalizedPainInensity;
	 
	 public ArrayList<Integer> get_generalizedPainInensity() {
		return _generalizedPainInensity;
	}

	public void setGeneralizedPainInensity(ArrayList<Integer> generalizedPainInensity) {
		this._generalizedPainInensity = generalizedPainInensity;
	}

	public Integer getBodyPainLocation() {
		return _bodyPainLocation;
	}

	public Integer getBodyPainIntensity() {
		return _bodyPainIntensity;
	}

	private Timestamp _userSubmittedTimeStamp;
	 
	 public PostPainIntensity(String activityId,HashMap<String,Integer> questionIds,int activityInstanceId,Timestamp userSubmittedTimeStamp,Integer bodyPainLocation,Integer intensity,ArrayList<Integer> generalizedPainInensity)
	 {
		 _activityId = activityId;
		 _questionIds = questionIds;
		 _activityInstanceId = activityInstanceId;
		 _userSubmittedTimeStamp = userSubmittedTimeStamp; 
		 _bodyPainIntensity = intensity;
		 _bodyPainLocation = bodyPainLocation;
		 _generalizedPainInensity = generalizedPainInensity;
	 }

	public HashMap<String,Integer> getQuestionIds() {
		return _questionIds;
	}

	public int getActivityInstanceId() {
		return _activityInstanceId;
	}

	public Timestamp getUserSubmittedTimeStamp() {
		return _userSubmittedTimeStamp;
	}
	 

}
