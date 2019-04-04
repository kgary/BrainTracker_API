package edu.asu.epilepsy.apiv30.model;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This is a model object that should support JAXB bindings for our REST services
 * @author kevinagary
 *
 */
public abstract class Activity {
	protected String _activityId;

	protected Activity(String activityId) {
		_activityId = activityId;  // a factory needs to inject this later
	}
	// getters, setters, etc. below
	public String getActivity(){
		return this._activityId;
	}
	public void setActivity(String _activityId){
		this._activityId=_activityId;
	}
	
	public abstract String generateJSON() throws JsonProcessingException;
}
