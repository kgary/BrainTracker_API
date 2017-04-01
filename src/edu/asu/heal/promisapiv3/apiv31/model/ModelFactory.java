package edu.asu.heal.promisapiv3.apiv31.model;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.asu.heal.promisapiv3.apiv31.dao.DAO;
import edu.asu.heal.promisapiv3.apiv31.dao.DAOException;
import edu.asu.heal.promisapiv3.apiv31.dao.DAOFactory;
import edu.asu.heal.promisapiv3.apiv31.dao.ValueObject;
import edu.asu.heal.promisapiv3.apiv31.model.MedicalAdherence.MedicationInfo;
import edu.asu.heal.promisapiv3.apiv31.model.Patient.Trial;

/**
 * The ModelFactory knows how to create each type of model object. Client classes should
 * call this, not the constructors of the model objects directly.
 *
 * @author kevinagary
 *
 */
public final class ModelFactory {
	static Logger log = LogManager.getLogger(ModelFactory.class);
	private DAO __theDAO = null;

	public ModelFactory() throws ModelException {
		try {
			__theDAO = DAOFactory.getTheDAO();
		} catch (DAOException de) {
			log.error(de);
			throw new ModelException("Unable to initialize the DAO", de);
		}
	}

	public ActivityInstance getActivityInstance(String activityInstanceId) throws ModelException {
		try {
			// Has to take a ValueObject and create an ActivityInstance
			ValueObject vo = __theDAO.getActivityInstance(activityInstanceId);
			if(vo == null)
				return null;
			else
			{
				return new ActivityInstance(activityInstanceId,
						(Date)vo.getAttribute("StartTime"),
						(Date)vo.getAttribute("EndTime"),
						(Date)vo.getAttribute("UserSubmissionTime"),
						(Date)vo.getAttribute("ActualSubmissionTime"),
						(String)vo.getAttribute("State"),  // what is this again?
						(String)vo.getAttribute("Sequence"), // assuming this is JSON?
						(String)vo.getAttribute("activityTitle"),
						(String)vo.getAttribute("description"),
						(String)vo.getAttribute("patientPin"));
			}

		} catch (DAOException de) {
			de.printStackTrace();
			log.error(de);
			throw new ModelException("Unable to create Model Object", de);
		}
	}


	public Activity getActivity(String activityId,String patientPIN) throws ModelException {
		try {
			//ValueObject vo = __theDAO.getActivity(activityId);
			ValueObject vo = null;
			vo = __theDAO.getJoinActivity(activityId);
			ContainerActivityMapping containerActvtyRef = (ContainerActivityMapping)vo.getAttribute("containerActivityReference");
			ArrayList<String> childActivities = containerActvtyRef._childActivities;
			//The check to find if the activity is a child activity or parent activity.
			if(childActivities.size() > 0)
			{
				ArrayList<Activity> activities = new ArrayList<Activity>();
				for(String childactivityId : childActivities)
				{
					Activity activity = getActivity(childactivityId,patientPIN);
					activities.add(activity);
				}
				if(activities.size() > 2)
				{
					return new ContainerActivity(activityId,activities,containerActvtyRef._sequence);
				}
				else if(activities.size() == 2)
				{
					return new ContainerActivity(activityId,activities.get(0),activities.get(1),containerActvtyRef._sequence);
				}
			}
			else
			{
				if(activityId.equals("PI_DAILY") || activityId.equals("PI_WEEKLY")){
					vo = __theDAO.getPainIntensity(activityId);
					List<Question> questions = (List<Question>) vo.getAttribute("Question");
					HashMap<Integer,String> questionOptionsMap = new HashMap<Integer,String>();
					for(Question question:questions)
					{
						int questionOptionType = question.getQuestionOptionType();
						if(questionOptionType != -1)
						{
							String questionOptions = ReadAnsOptProp.getQuestionOption(Integer.toString(questionOptionType));
							questionOptionsMap.put((Integer.parseInt(question.getQuestionId())), questionOptions);
						}
					}

					return new PainIntensity(activityId, (List<Question>) vo.getAttribute("Question"), questionOptionsMap,(String)vo.getAttribute("QuestionId"));
				}
				else if(activityId.equals("MA")){
					vo = __theDAO.getMedicalAdherence(patientPIN);
					ArrayList<MedicationInfo> medicationInfo = (ArrayList<MedicationInfo>)vo.getAttribute("medicationInfo");
					ArrayList<Question> questions = null;
					HashMap<Integer,String> questionOptionsMap = null;
					if(vo.getAttribute("Questions") != null && vo.getAttribute("medicationInfo") != null)
					{
						questionOptionsMap = new HashMap<Integer,String>();
						questions = (ArrayList<Question>)vo.getAttribute("Questions");
						int questionOptionType = -1;
						for(Question question :questions)
						{
							questionOptionType = question.getQuestionOptionType();
							System.out.println("questionOptionType::"+questionOptionType);
							if(questionOptionType != -1)
							{
								if(!questionOptionsMap.containsKey(questionOptionType))
								{
									String questionOptions = ReadAnsOptProp.getQuestionOption(Integer.toString(questionOptionType));
									questionOptionsMap.put(questionOptionType, questionOptions);
								}
							}
						}
					}
					System.out.println("map::"+questionOptionsMap);
					System.out.println("ArrayList::"+medicationInfo);
					QuestionOption otherOption = getOptionByText("Other");
					return new MedicalAdherence(activityId, (List<Question>) vo.getAttribute("Questions"), questionOptionsMap,medicationInfo,otherOption);
				}
				else if(activityId.equals("CAT")){

					vo = __theDAO.getAdaptive(patientPIN);
					//Looping through the questions.
					ArrayList<Question> questions = null;
					HashMap<Integer,String> questionOptionsMap = null;
					if(vo.getAttribute("Questions") != null && vo.getAttribute("catquestionmapping") != null)
					{
						questionOptionsMap = new HashMap<Integer,String>();
						questions = (ArrayList<Question>) vo.getAttribute("Questions");
						int questionOptionType = -1;
						for(Question question : questions)
						{
							questionOptionType = question.getQuestionOptionType();
							if(!questionOptionsMap.containsKey(questionOptionType))
							{
								String questionOptions = ReadAnsOptProp.getQuestionOption(Integer.toString(questionOptionType));
								questionOptionsMap.put(questionOptionType, questionOptions);
							}
						}
						return new ComputerAdaptiveTesting(activityId, (List<Question>) vo.getAttribute("Questions"), questionOptionsMap,(HashMap<Integer,ArrayList<Integer>>)vo.getAttribute("catquestionmapping"));
					}
					else
					{
						throw new ModelException("Unable to create adaptive question obj");
					}
				}
				else if(activityId.equals("PR_Anxiety") || activityId.equals("PR_Fatigue") || activityId.equals("PR_PainInt") || activityId.equals("PR_PhysFuncMob")) {

					vo = __theDAO.getPromisSurvey(activityId, patientPIN); //We are harcoding it as parent proxy for now,not sure whether we need to get it from the app or we should have our own logic from DB.
					//Looping through the questions.
					ArrayList<Question> questions = null;
					HashMap<Integer,String> questionOptionsMap = null;
					if(vo.getAttribute("Questions") != null)
					{
						questionOptionsMap = new HashMap<Integer,String>();
						questions = (ArrayList<Question>) vo.getAttribute("Questions");
						int questionOptionType = -1;
						for(Question question : questions)
						{
							questionOptionType = question.getQuestionOptionType();
							if(!questionOptionsMap.containsKey(questionOptionType))
							{
								String questionOptions = ReadAnsOptProp.getQuestionOption(Integer.toString(questionOptionType));
								questionOptionsMap.put(questionOptionType, questionOptions);
							}
						}
					}
					//String questionOptions = ReadAnsOptProp.getQuestionOption(Integer.toString(((Question)((ArrayList)vo.getAttribute("Questions")).get(0)).getQuestionOptionType()));

					return new PromisSurvey(activityId, (List<Question>) vo.getAttribute("Questions"), questionOptionsMap , (String) vo.getAttribute("sequencing"));
				}
				else
				{
					log.warn("Cannot create model object for activity:"+activityId);//Need to have a better way for handling exception,moving forward quickly,will do it later
					return null;
				}
			}
		} catch (DAOException e) {
			log.error("Cannot create model object.");//Need to have a better way for handling exception,moving forward quickly,will do it later
			e.printStackTrace();
			throw new ModelException("Unable to create model obj");

		}
		return null;
	}

	public Patient getPatient(String patientPIN) throws ModelException
	{
		ValueObject vo = null;
		try
		{
			System.out.println(patientPIN);
			vo = __theDAO.getpatient(patientPIN);
		}
		catch(DAOException e)
		{
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model obj: "+e.getMessage());
		}

		return (Patient) (vo.getAttribute("Patient"));
	}

	public ArrayList<ActivityInstance> checkActivityInstance(String patientPIN) throws ModelException
	{
		ValueObject vo = null;
		try
		{
			vo = __theDAO.checkActivity(patientPIN);
			return (ArrayList<ActivityInstance>) vo.getAttribute("ActivityInstances"); // assuming this is JSON?
		} catch (DAOException de) {
			de.printStackTrace();
			log.error(de);
			throw new ModelException("Unable to create Model Object", de);
		}
	}

	public QuestionOption getOptionByText(String optionText) throws ModelException
	{
		ValueObject vo = null;
		try
		{
			vo = __theDAO.getQuestionOptionByText(optionText);
			return (QuestionOption) vo.getAttribute("questionOption");
		} catch (DAOException de) {
			de.printStackTrace();
			log.error(de);
			throw new ModelException("Unable to create Model Object", de);
		}
	}

	public boolean postActivityInstance(ArrayList<PostActivity> result) throws SQLException, DAOException
	{
		ValueObject vo = __theDAO.postActivityInstances(result);

		return (boolean) vo.getAttribute("result");
	}

	public String generateSequence(Activity activity) throws ModelException
	{
		ContainerActivity contAct = null;
		if(activity.getClass().getName().endsWith("ContainerActivity"))
			contAct  = (ContainerActivity) activity;
		else
			return activity._activityId; //If the activity is a leaf activity,we just return the activity id.

		StringBuilder sequence = new StringBuilder("");

		//The if logic must be tested.
		if(contAct.getSequencing().name().equals("INTERLEAVE_ORDERED") && contAct.getSubActivities().get(0).getClass().getName().equals("ContainerActivity") && contAct.getSubActivities().get(1).getClass().getName().equals("ContainerActivity"))
		{
			//This logic needs to be tested by adding sufficient data.

			String ChildSequence1 = generateSequence(contAct.getSubActivities().get(0));
			String ChildSequence2 = generateSequence(contAct.getSubActivities().get(1));
			String[] sequence1 = ChildSequence1.split(",");
			String[] sequence2 = ChildSequence2.split(",");
			int i=0;
			int j=0;
			while(true)
			{
				if(sequence1[i] != null && sequence2[j] != null)
				{
					sequence.append(sequence1[i++]+","+sequence2[j++]+",");
				}
				else if(sequence1[i] != null)
				{
					sequence.append(sequence1[i++]);
				}
				else if(sequence2[j] != null)
				{
					sequence.append(sequence2[j++]);
				}
				else
				{
					break;
				}
			}
		}
		else
		{
			//If the Container Activity is RANDOM,We are randomizing the blocks.
			if(contAct.getSequencing().name().equals("RANDOM"))
			{
				Collections.shuffle(contAct.getSubActivities());
			}

			for(Activity atvt:contAct.getSubActivities())
			{

				if(atvt.getClass().getName().endsWith("ContainerActivity"))
				{

					sequence.append(generateSequence(atvt)+",");
				}
				else
				{

					sequence.append(atvt._activityId+",");
				}
			}
		}
		if(sequence.toString().endsWith(","))
		{
			sequence.deleteCharAt(sequence.length() -1);
		}
		return sequence.toString();
	}

	//Inserting the generated sequence in the database.
	public boolean createActivityInstance(String sequence,String patientPIN,Trial trialType,String startTime,String endTime,String activityID) throws ModelException, DAOException
	{
		ValueObject vo = __theDAO.createActivityInstance(sequence, patientPIN, startTime,endTime, trialType,activityID);

		return (boolean) vo.getAttribute("result");
	}

	public boolean changeActivityInsState(int activityInstance,String state) throws DAOException
	{
		ValueObject vo = __theDAO.changeActivityInstanceState(activityInstance, state);

		return (boolean) vo.getAttribute("result");
	}
	public final class ContainerActivityMapping
	{
		public ContainerActivity.Sequencing _sequence;
		public ArrayList<String>_childActivities;

	}

	public boolean postUILoggerResults(ArrayList<UILogger> loggerResults) throws SQLException, DAOException
	{
		ValueObject vo = __theDAO.postUILogger(loggerResults);

		return (boolean) vo.getAttribute("result");
	}

	public String getActivityMetaData(String activityId) throws DAOException
	{
		ValueObject vo = __theDAO.getActivityMetaData(activityId);

		return (String) vo.getAttribute("metadata");
	}
	/**
	 * This function will call the DAO for the enrolling the patients
	 * @param patientsInfos An ArrayList of patients Info
	 * @return The newly enrolled patient pin
	 * @throws DAOException
	 * @throws SQLException
	 */
	public String enrollPatients(ArrayList<Patient.PatientEnroll> patientsInfos) throws DAOException, SQLException
	{
		ValueObject vo = __theDAO.enrollPatients(patientsInfos);
		System.out.println("The result is"+vo.getAttribute("result"));
		return Integer.toString((int)(vo.getAttribute("result")));
	}
	/**
	 * This method will return the trial name
	 * @param trialName
	 * @return integer - The duration of the trial
	 * @throws DAOException
	 * @throws SQLException
	 */
	public int getTrialDuration(String trialName) throws DAOException, SQLException
	{
		ValueObject vo = __theDAO.getTrialDuration(trialName);
		String startTime = (String) vo.getAttribute("startTime");
		String endTime = (String) vo.getAttribute("endTime");
		int duration = (int) vo.getAttribute("Duration");

		return duration;
	}

	public ArrayList<PatientBadges> getPatientBadges(String patientPIN) throws ModelException
	{
		System.out.println("Inside ModelFactory method getPatientBadges");
		ValueObject vo = null;
		try{
			vo = __theDAO.getPatientBadges(patientPIN);
			return (ArrayList<PatientBadges>) vo.getAttribute("PatientBadges");
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Unable to create Model Object for patient_badges");
		}
	}

	public Badge getBadge(String badgeId) throws ModelException{
		ValueObject vo = null;

		try{
			System.out.println(badgeId);
			vo = __theDAO.getBadge(badgeId);
			return (Badge) (vo.getAttribute("Badge"));

		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model getBadge");
		}
	}

	public BagdePowerups getBadgePowerup(String badgeId) throws ModelException
	{
		ValueObject vo = null;
		try{
			vo = __theDAO.getBadgePowerup(badgeId);
			return(BagdePowerups) vo.getAttribute("BadgePowerup");
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Unable to create Model Object for badge_powerups");
		}
	}

	public Powerups getPowerup(String powerupId) throws ModelException
	{
		ValueObject vo = null;
		try{
			vo = __theDAO.getPowerup(powerupId);
			return (Powerups) (vo.getAttribute("Powerup"));
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model getPowerups");
		}
	}

	public boolean insertPatientPowerup(PatientPowerups patientPowerup) throws ModelException
	{
		System.out.println("Inside insertPatientPowerup");
		ValueObject vo = null;
		try{
			vo = __theDAO.insertPatientPowerup(patientPowerup);
			return (boolean) vo.getAttribute("result");
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model insertPatientPowerup");
		}
	}

	public boolean updatePatientPowerup(PatientPowerups patientPowerup) throws ModelException
	{
		System.out.println("Inside updatePatientPowerup");
		ValueObject vo = null;
		try{
			vo = __theDAO.updatePatientPowerup(patientPowerup);
			return (boolean) vo.getAttribute("result");
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model updatePatientPowerup");
		}
	}

	public PatientPowerups checkPatientPowerup(String patientPIN, String powerupId) throws ModelException
	{
		System.out.println("Inside checkPatientPowerup");
		ValueObject vo = null;
		try{
			vo = __theDAO.checkPatientPowerup(patientPIN, powerupId);
			return (PatientPowerups) (vo.getAttribute("PatientPowerup"));
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model for checkPatientPowerup");
		}
	}

	public Games getGameDetails(String gameId) throws ModelException{

		System.out.println("Inside getGameDetails");
		ValueObject vo = null;
		try{
			vo = __theDAO.getGameDetails(gameId);
			return(Games) vo.getAttribute("GameInfo");
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model for getGameDetails");
		}
	}

	public PatientGamePlay getPatGamePlay(String patientPIN) throws ModelException{

		System.out.println("Inside getPatGamePlay Model");
		ValueObject vo = null;

		try{
			vo = __theDAO.getPatGamePlay(patientPIN);
			return (PatientGamePlay) vo.getAttribute("PatientGamePlay");
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model for getGameDetails");
		}
	}

	public PatientPowerups getPatientPowerups(String patientPIN) throws ModelException
	{
		System.out.println("Inside getPatientPowerups");
		ValueObject vo = null;
		try{
			vo = __theDAO.getPatientPowerups(patientPIN);
			return (PatientPowerups) (vo.getAttribute("PatientPowerupDetails"));
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model for getPatientPowerups");
		}
	}

	public Boolean insertPatGamePlay(PatientGamePlay patientGamePlay) throws ModelException
	{
		System.out.println("Inside insertPatGamePlay ModelFactory");
		ValueObject vo = null;
		try{
			vo = __theDAO.insertPatientGamePlay(patientGamePlay);
			return (Boolean) (vo.getAttribute("result"));
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model for insertPatGamePlay");
		}
	}

	public Boolean updatePatGamePlay(PatientGamePlay patientGamePlay) throws ModelException
	{
		System.out.println("Inside updatePatGamePlay ModelFactory");
		ValueObject vo = null;
		try{
			vo = __theDAO.updatePatientGamePlay(patientGamePlay);
			return (Boolean) (vo.getAttribute("result"));
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model for updatePatGamePlay");
		}
	}

	public Boolean checkPatientBadge(PatientBadges patientBadge) throws ModelException{
		System.out.println("Inside checkPatientBadge ModelFactory");
		ValueObject vo = null;
		try{
			vo = __theDAO.checkPatientBadge(patientBadge);
			return (Boolean) (vo.getAttribute("result"));
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model for checkPatientBadge");
		}
	}

	public Boolean activatePatientBadge(ActivatePatientBadge activatePatientBadge) throws ModelException{
		System.out.println("Inside model for activatePatientBadge");
		ValueObject vo = null;
		try{
			vo = __theDAO.activatePatientBadge(activatePatientBadge);
			return (Boolean) (vo.getAttribute("result"));
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model for activatePatientBadge");
		}
	}

	public int getWeeklyActivityCompletionCount(String patientPIN, String activityInstanceId) throws ModelException{
		System.out.println("Inside model for getWeeklyActivityCompletionCount");
		ValueObject vo = null;
		try{
			vo = __theDAO.getWeeklyActivityCount(patientPIN, activityInstanceId);
			return (int) (vo.getAttribute("WeeklyActivityCount"));
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model for getWeeklyActivityCompletionCount");
		}
	}

	public String getActivityBadges(String activityType, int activityCount) throws ModelException{

		System.out.println("Inside model for getActivityBadges");
		ValueObject vo = null;
		try{
			vo = __theDAO.getActivityBadges(activityType, activityCount);
			System.out.println("Vo received");
			return (String) (vo.getAttribute("BadgeId"));
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model for getActivityBadges");
		}
	}

	public Boolean insertPatientBadge(PatientBadges patientBadge) throws ModelException{

		System.out.println("Inside model for insertPatientBadge");
		ValueObject vo = null;
		try{
			vo = __theDAO.insertPatientBadge(patientBadge);
			return (Boolean) (vo.getAttribute("result"));
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model for insertPatientBadge");
		}
	}

	public int getDailyActivityCompletionCount(String patientPIN, String activityInstanceId, Date dailyInstanceStartTime) throws ModelException{
		System.out.println("Inside model for getDailyActivityCompletionCount");
		ValueObject vo = null;
		try{
			vo = __theDAO.getDailyActivityCount(patientPIN, activityInstanceId, dailyInstanceStartTime);
			return (int) (vo.getAttribute("DailyActivityCount"));
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Error in creating model for getDailyActivityCompletionCount");
		}
	}

	public int getPatientBadgeCount(String patientPIN, Boolean usedFlag) throws ModelException{
		System.out.println("Inside model for getPatientBadgeCount");
		ValueObject vo = null;
		try{
			vo = __theDAO.getPatientBadgeCount(patientPIN, usedFlag);
			return (int) (vo.getAttribute("BadgeCount"));
		}catch(Exception e){
			e.printStackTrace();
			log.error(e);
			throw new ModelException("Inside model for getPatientBadgeCount");
		}
	}
}
