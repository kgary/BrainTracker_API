package edu.asu.heal.promisapiv3.apiv31.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import edu.asu.heal.promisapiv3.apiv31.model.ActivatePatientBadge;
import edu.asu.heal.promisapiv3.apiv31.model.Patient;
import edu.asu.heal.promisapiv3.apiv31.model.PostActivity;
import edu.asu.heal.promisapiv3.apiv31.model.QuestionResult;
import edu.asu.heal.promisapiv3.apiv31.model.UILogger;
import edu.asu.heal.promisapiv3.apiv31.model.Patient.Trial;
import edu.asu.heal.promisapiv3.apiv31.model.PatientBadges;
import edu.asu.heal.promisapiv3.apiv31.model.PatientGamePlay;
import edu.asu.heal.promisapiv3.apiv31.model.PatientPowerups;

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

	public default ValueObject getPatientBadges(String patientPIN) throws SQLException, DAOException
	{
		return null;
	}
	public default ValueObject getBadge(String badgeId) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject getBadgePowerup(String badgeId) throws SQLException, DAOException
	{
		return null;
	}

	public default ValueObject getPowerup(String powerupId) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject insertPatientPowerup(PatientPowerups patientPowerup) throws SQLException, DAOException{
		return null;
	}

	public default ValueObject updatePatientPowerup(PatientPowerups patientPowerup) throws SQLException, DAOException{
		return null;
	}

	public default ValueObject checkPatientPowerup(String patientPIN, String powerupId) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject getGameDetails(String gameId) throws SQLException, DAOException{
		return null;
	}

	public default ValueObject getPatGamePlay(String patientPIN) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject getPatientPowerups(String patientPIN) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject insertPatientGamePlay(PatientGamePlay patientGamePlay) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject updatePatientGamePlay(PatientGamePlay patientGamePlay) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject checkPatientBadge(PatientBadges patientBadge) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject activatePatientBadge(ActivatePatientBadge activatePatientBadge) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject getWeeklyActivityCount(String patientPIN, String activityInstanceId) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject getActivityBadges(String activityType, int activityCount) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject insertPatientBadge(PatientBadges patientBadge) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject getDailyActivityCount(String patientPIN, String activityInstanceId, Date dailyInstanceStartTime) throws SQLException, DAOException{
		return null;
	}
	public default ValueObject getPatientBadgeCount(String patientPIN, Boolean usedFlag)throws SQLException, DAOException{
		return null;
	}
}
