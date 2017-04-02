package edu.asu.epilepsy.apiv30.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import edu.asu.epilepsy.apiv30.model.Patient;
import edu.asu.epilepsy.apiv30.model.PostActivity;
import edu.asu.epilepsy.apiv30.model.QuestionResult;
import edu.asu.epilepsy.apiv30.model.UILogger;
import edu.asu.epilepsy.apiv30.model.Patient.Trial;

/**
 * This interface defines the datastore methods for instantiating and persisting model objects
 * 
 * @author kevinagary
 *
 */
public interface DAO {
	/**
	 * Returns a model object based on the given ID, which should exist
	 * @param activityInstanceId
	 * @return a HashMap of name/value pairs that can be used to construct the target model object
	 * @throws DAOException if there is a problem creating the object
	 */
	public default ValueObject getActivityInstance(String activityInstanceId) throws DAOException {
		return null;
	}
	
	public default ValueObject getActivity(String activityId) throws DAOException {
		return null;
	}
	
	public default ValueObject getPainIntensity(String activityID) throws DAOException {
		return null;
	}
	
	public default ValueObject getPromisSurvey(String activityId, String SurveyBlockType) throws DAOException {
		return null;
	}
	
	public default ValueObject getJoinActivity(String parentActivityId) throws DAOException{
		return null ;
	}
	
	public default ValueObject getpatient(String patientPIN) throws DAOException{
		return null ;
	}
	
	public default ValueObject checkActivity(String patientPIN) throws DAOException{
		return null ;
	}
	
	public default ValueObject postActivityInstances(ArrayList<PostActivity> questionResults) throws SQLException, DAOException
	{
		return null;
	}
	public default ValueObject getQuestionOptionByText(String questionOptionText) throws DAOException
	{
		return null;
	}
	
	public default ValueObject createActivityInstance(String sequence,String patientPIN,String startTime,String endTime,Trial trialType,String activityID) throws DAOException
	{
		return null;
	}

	public default ValueObject getMedicalAdherence(String patientPIN) throws DAOException
	{
		return null;
	}

	public default ValueObject getAdaptive(String patientPIN) throws DAOException
	{
		return null;
	}
	
	public default ValueObject changeActivityInstanceState(int activityInstance,String state) throws DAOException
	{
		return null;
	}
	
	public default ValueObject postUILogger(ArrayList<UILogger> loggerResults) throws SQLException, DAOException{
		return null;
	}
	
	public default ValueObject getActivityMetaData(String activityID) throws DAOException{
		return null;
	}
	public default ValueObject enrollPatients(ArrayList<Patient.PatientEnroll> patientsInfos) throws SQLException,DAOException{
		return null;
	}
	public default ValueObject getTrialDuration(String trialName) throws SQLException,DAOException{
		return null;
	}
	public default ValueObject getFingerTapping(String activityID) throws SQLException,DAOException{
		return null;
	}
}
