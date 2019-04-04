package edu.asu.epilepsy.apiv30.model;

import java.util.Date;

/**
 * The ActivityInstance model object represents 
 * @author kevinagary
 *
 */
public final class ActivityInstance {

	private final String activityInstanceId;
	private final Date startTime;
	private final Date endTime;
	private final Date userSubmissionTime;
	private final Date actualSubmissionTime;
	private final String state;
	private final String sequence; // JSON right?
	private final String activityTitle;
	private final String description;	
	private final String patientPin;	
	
	/* package scope on purpose */
	public ActivityInstance(String aid, Date startTime, Date endTime, Date userSubmissionTime, Date actualSubmissionTime, String state, String sequence,String activityTitle, String description,String patientPin) throws ModelException {
		// Initialize existing one from DAO
		activityInstanceId = aid;
		this.startTime = startTime;
		this.endTime = endTime;
		this.userSubmissionTime = userSubmissionTime;
		this.actualSubmissionTime = actualSubmissionTime;
		this.state = state;
		this.sequence = sequence;
		this.activityTitle = activityTitle;
		this.description = description;
		this.patientPin = patientPin;
	}
	
	public String getActivityTitle() {
		return activityTitle;
	}

	public String getDescription() {
		return description;
	}

	public String getActivityInstanceId() {
		return activityInstanceId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public Date getUserSubmissionTime() {
		return userSubmissionTime;
	}

	public Date getActualSubmissionTime() {
		return actualSubmissionTime;
	}

	public String getState() {
		return state;
	}

	public String getSequence() {
		return sequence;
	}

	public String getPatientPin() {
		return patientPin;
	}
	
}
