package edu.asu.heal.promisapiv3.apiv30.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is the model for the post of the promis survey.
 * @author Deepak S N
 */
public class PostPromisSurvey extends PostActivity{
	private HashMap<Integer,ArrayList<PostPromisSurvey.OptionToValue>> _questionToOptions;
	private Timestamp _userSubmittedTimeStamp;
	
	public PostPromisSurvey(){}
	public PostPromisSurvey(String activityId,int activityInstanceId,HashMap<Integer,ArrayList<PostPromisSurvey.OptionToValue>> questionToOptions,Timestamp userSubmittedTimeStamp)
	{
		_activityId = activityId;
		_activityInstanceId = activityInstanceId;
		_questionToOptions = questionToOptions;
		_userSubmittedTimeStamp = userSubmittedTimeStamp;
	}

	public int getActivityInstanceId() {
		return _activityInstanceId;
	}

	
	  public HashMap<Integer, ArrayList<PostPromisSurvey.OptionToValue>> getQuestionToOptions() {
		return _questionToOptions;
	}
	

	public Timestamp getUserSubmittedTimeStamp() {
		return _userSubmittedTimeStamp;
	}
	
	//This is an inner class that wraps the optionID and value for it if it exists. 
	public class OptionToValue
	{
		private int _optionID;
		private String _value;
		private String _dosage;
		
		public  OptionToValue(int optionID,String value,String dosage)
		{
			_optionID = optionID;
			_value = value;
			_dosage = dosage;
		}
		
		public int getOptionId()
		{
			return _optionID;
		}
		public String getValue()
		{
			return _value;
		}
		public String getDosage()
		{
			return _dosage;
		}
	}
}
