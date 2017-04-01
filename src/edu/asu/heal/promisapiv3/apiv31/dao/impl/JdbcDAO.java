package edu.asu.heal.promisapiv3.apiv31.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.asu.heal.promisapiv3.apiv31.dao.DAO;
import edu.asu.heal.promisapiv3.apiv31.dao.DAOException;
import edu.asu.heal.promisapiv3.apiv31.dao.DAOFactory;
import edu.asu.heal.promisapiv3.apiv31.dao.ValueObject;
import edu.asu.heal.promisapiv3.apiv31.helper.APIConstants;
import edu.asu.heal.promisapiv3.apiv31.model.ActivatePatientBadge;
import edu.asu.heal.promisapiv3.apiv31.model.ActivityInstance;
import edu.asu.heal.promisapiv3.apiv31.model.Badge;
import edu.asu.heal.promisapiv3.apiv31.model.BagdePowerups;
import edu.asu.heal.promisapiv3.apiv31.model.ContainerActivity;
import edu.asu.heal.promisapiv3.apiv31.model.Games;
import edu.asu.heal.promisapiv3.apiv31.model.MedicalAdherence;
import edu.asu.heal.promisapiv3.apiv31.model.MedicalAdherence.MedicationInfo;
import edu.asu.heal.promisapiv3.apiv31.model.ModelFactory;
import edu.asu.heal.promisapiv3.apiv31.model.Patient;
import edu.asu.heal.promisapiv3.apiv31.model.Patient.PatientEnroll;
import edu.asu.heal.promisapiv3.apiv31.model.Patient.Trial;
import edu.asu.heal.promisapiv3.apiv31.model.Patient.medicationInfo;
import edu.asu.heal.promisapiv3.apiv31.model.PatientBadges;
import edu.asu.heal.promisapiv3.apiv31.model.PatientGamePlay;
import edu.asu.heal.promisapiv3.apiv31.model.PatientPowerups;
import edu.asu.heal.promisapiv3.apiv31.model.PostActivity;
import edu.asu.heal.promisapiv3.apiv31.model.PostPainIntensity;
import edu.asu.heal.promisapiv3.apiv31.model.PostPromisSurvey;
import edu.asu.heal.promisapiv3.apiv31.model.PostPromisSurvey.OptionToValue;
import edu.asu.heal.promisapiv3.apiv31.model.Powerups;
import edu.asu.heal.promisapiv3.apiv31.model.Question;
import edu.asu.heal.promisapiv3.apiv31.model.Question.Type;
import edu.asu.heal.promisapiv3.apiv31.model.QuestionOption;
import edu.asu.heal.promisapiv3.apiv31.model.UILogger;

public abstract class JdbcDAO implements DAO {
	static Logger log = LogManager.getLogger(JdbcDAO.class);
	private String __jdbcDriver;
	protected String _jdbcUser;
	protected String _jdbcPasswd;
	protected String _jdbcUrl;

	public JdbcDAO(Properties props) throws DAOException {

		// For MySQL we expect the JDBC Driver, user, password, and the URI. Maybe more in the future.
        _jdbcUrl    = props.getProperty("jdbc.url");
        _jdbcUser   = props.getProperty("jdbc.user");
        _jdbcPasswd = props.getProperty("jdbc.passwd");
        __jdbcDriver = props.getProperty("jdbc.driver");

        try {
        		Class.forName(__jdbcDriver); // ensure the driver is loaded
        }
        catch (ClassNotFoundException cnfe) {
        		throw new DAOException("*** Cannot find the JDBC driver " + __jdbcDriver, cnfe);
        }
        catch (Throwable t) {
        		throw new DAOException(t);
        }
	}

	/**
	 * We really should implement some simple wrapper and pooling YYY
	 * @return database Connection
	 * @throws DAOException
	 */
	protected Connection getConnection() throws DAOException {
		try {
			return DriverManager.getConnection(_jdbcUrl, _jdbcUser, _jdbcPasswd);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DAOException("Unable to get connection to database", e);
		}
	}

	/**
	 * Get an activity instance from the backing store
	 */
	public ValueObject getActivityInstance(String activityInstanceId) throws DAOException {
		ValueObject vo = null; // need to fill this up
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			String query = DAOFactory.getDAOProperties().getProperty("sql.activityInstance");
			ps = connection.prepareStatement(query);
			ps.setString(1, activityInstanceId);
			rs = ps.executeQuery();
			if(rs.next())
			{
				vo = new ValueObject();
				Date startTime =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("StartTime"));
				Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("EndTime"));
				Date userSubmissionTime = null;
				if(!(rs.getString("UserSubmissionTime") == null || rs.getString("UserSubmissionTime").isEmpty()))
					userSubmissionTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("UserSubmissionTime"));
				Date actualSubmissionTime = null;
				if(!(rs.getString("ActualSubmissionTime") == null || rs.getString("UserSubmissionTime").isEmpty()))
					actualSubmissionTime =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("ActualSubmissionTime"));
				String state = rs.getString("State");
				String sequence = rs.getString("Sequence");
				String activityTitle = rs.getString("activityTitle");
				String description = rs.getString("description");
				String patientPin = rs.getString("PatientPinFK");


				//Filling the value objects.
				vo.putAttribute("StartTime", startTime);
				vo.putAttribute("EndTime", endTime);
				vo.putAttribute("UserSubmissionTime", userSubmissionTime);
				vo.putAttribute("ActualSubmissionTime", actualSubmissionTime);
				vo.putAttribute("State", state);
				vo.putAttribute("Sequence", sequence);
				vo.putAttribute("activityTitle", activityTitle);
				vo.putAttribute("description", description);
				vo.putAttribute("patientPin", patientPin);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new DAOException("Unable to process results from query sql.actvty");
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
				if (connection != null) connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
				// YYY need a logging facility, but this does not have to be rethrown
			}
		}
		return vo;
	}

	/**
	 * Retrieve an activity from the backing store
	 */


	@Override
	public ValueObject getPainIntensity(String activityID) throws DAOException {
		ValueObject vo = new ValueObject(); // need to fill this up
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			String query = DAOFactory.getDAOProperties().getProperty("sql.pi");
			ps = connection.prepareStatement(query);
			if(activityID.equals(APIConstants.dailyActivityID_PI))
				ps.setString(1, "bodyPain_daily");
			else if(activityID.equals(APIConstants.weeklyActivityID_PI))
				ps.setString(1, "bodyPain_weekly");
			rs = ps.executeQuery();
			List<Question> questions = new ArrayList<Question>();
			while(rs.next())
			{
				String questionId = rs.getString("QuestionId");
				String questionText = rs.getString("QuestionText");
				int questionOption = rs.getInt("QuestionOptionType");
				String questionType = rs.getString("QuestionType");
				Question.Type questionTypeEnum = null;
				for(Question.Type type :Question.Type.values())
				{
					if(type.name().equalsIgnoreCase(questionType))
					{
						questionTypeEnum = type;
					}
				}
				Question question = new Question(questionId,questionTypeEnum,questionText,"",questionOption,"");
				questions.add(question);
			}
			vo.putAttribute("Question",questions);
		} catch (Throwable t) {
			throw new DAOException("Unable to process results from query sql.pi");
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
				if (connection != null) connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
				// YYY need a logging facility, but this does not have to be rethrown
				log.error(se);
			}
		}

		return vo;
	}

	/*
	 * INCORRECT/ I am not sure what this method is doing, but it has several errors. The SQL is wrong.
	 * It again mixes Activity and ActivityInstance. It had resource leaks. It had a null pointer error.
	 * @see edu.asu.heal.promisapiv3.dao.DAO#getPromisSurvey(java.lang.String, java.lang.String)*/

	@Override
	public ValueObject getPromisSurvey(String activityId, String patientPIN) throws DAOException {
		ValueObject vo = new ValueObject(); // need to fill this up
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String patientType = null;
		String sequencing = null;
		String surveyBlockId = null;

		try	{
			//Need to get the surveyBlockType from patients table
			String patientQuery = DAOFactory.getDAOProperties().getProperty("sql.patientType");
			ps = connection.prepareStatement(patientQuery);
			ps.setString(1, patientPIN);
			rs = ps.executeQuery();
			if(rs.next())
			{
				patientType =  rs.getString("type");

			}

			rs.close();
			ps.close();

			// get the sequencing - whether random or not from activity table
			String querySeq = DAOFactory.getDAOProperties().getProperty("sql.seq");
			ps = connection.prepareStatement(querySeq);
			ps.setString(1, activityId);
			rs = ps.executeQuery();
			if(rs.next())
			{
				sequencing =  rs.getString("canonicalOrder");


			}
			rs.close();
			ps.close();

			vo.putAttribute("sequencing", sequencing);


			//get survey block id
			String queryBlockId = DAOFactory.getDAOProperties().getProperty("sql.surveyBlockId");
			ps = connection.prepareStatement(queryBlockId);
			ps.setString(1, activityId);
			ps.setString(2, patientType);
			rs = ps.executeQuery();
			if(rs.next())
			{
				surveyBlockId =  rs.getString("SurveyBlockId");


			}
			rs.close();
			ps.close();

			//get questions based on survey block id
			String queryForQues = (sequencing.equals("RANDOM"))?"sql.questions.rand":"sql.questions";
			String queryQues = DAOFactory.getDAOProperties().getProperty(queryForQues);

			ps = connection.prepareStatement(queryQues);
			ps.setString(1, surveyBlockId);
			rs = ps.executeQuery();
			List<Question> qs = new ArrayList<Question>() ;
			String quesOptionType = null;

			while(rs.next())
			{
				String quesId =  rs.getString("QuestionId");
				String quesText =  rs.getString("QuestionText");
				quesOptionType =  rs.getString("QuestionOptionType");
				String shortForm = rs.getString("ShortForm");
				Question ques = new Question(quesId, Question.Type.LIKERT5, quesText, "", Integer.parseInt(quesOptionType),shortForm);

				qs.add(ques);
			}

			vo.putAttribute("Questions", qs);


		} catch (Throwable t) {
			t.printStackTrace();
			throw new DAOException("Unable to process results from query sql.actvty");
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
				if (connection != null) connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
				// YYY need a logging facility, but this does not have to be rethrown
				log.error(se);
			}
		}
		return vo;
	}

	@Override
	public ValueObject getJoinActivity(String parentActivityId) throws DAOException
	{
		ValueObject vo = new ValueObject(); // need to fill this up
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			String query = DAOFactory.getDAOProperties().getProperty("sql.ja");
			ps = connection.prepareStatement(query);
			ps.setString(1, parentActivityId);
			rs = ps.executeQuery();
			ArrayList<String> childActivities = new ArrayList<String>();
			String canonicalOrder = "";
			while(rs.next())
			{
				String childActivityId = rs.getString("ChildActivityId");
				System.out.println("The child activity::"+childActivityId);
				canonicalOrder = rs.getString("canonicalOrder");
				childActivities.add(childActivityId);
			}

			ModelFactory.ContainerActivityMapping actvtToOrderingMapping = new ModelFactory().new ContainerActivityMapping();
			actvtToOrderingMapping._childActivities = childActivities;
			//We have to do the seq mapping,only if the canonical ordering is not not empty.
			if(!canonicalOrder.isEmpty())
			{
				for(ContainerActivity.Sequencing seq : ContainerActivity.Sequencing.values())
				{
					if(seq.name().equals(canonicalOrder))
					{
						actvtToOrderingMapping._sequence = seq;
						break;
					}
				}
			}
			vo.putAttribute("containerActivityReference",actvtToOrderingMapping);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new DAOException("Unable to process results from query sql.ja");
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
				if (connection != null) connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
				// YYY need a logging facility, but this does not have to be rethrown
				log.error(se);
			}
		}

		return vo;
	}

	@Override
	public ValueObject getpatient(String patientPIN) throws DAOException
	{
		ValueObject vo = new ValueObject(); // need to fill this up
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			String query = DAOFactory.getDAOProperties().getProperty("sql.pa");
			ps = connection.prepareStatement(query);
			System.out.println("The patient pin is::"+patientPIN);
			ps.setString(1, patientPIN);
			rs = ps.executeQuery();
			Patient patient = null;
			if(rs.next())
			{
				String stageID = rs.getString("StageIdFK");
				String childPIN = rs.getString("ParentPinFK");
				String type = rs.getString("type");
				Boolean enhancedContent = rs.getBoolean("EnhancedContent");
				patient = new Patient(patientPIN,stageID,childPIN,type,enhancedContent);
			}

			vo.putAttribute("Patient",patient);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new DAOException("Unable to process results from query sql.ja");
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
				if (connection != null) connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
				// YYY need a logging facility, but this does not have to be rethrown
				log.error(se);
			}
		}

		return vo;
	}

	@Override
	public ValueObject checkActivity(String patientPIN) throws DAOException
	{
		ValueObject vo = new ValueObject(); // need to fill this up
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			String query = DAOFactory.getDAOProperties().getProperty("sql.chkActvyIns");
			ps = connection.prepareStatement(query);
			ps.setString(1, patientPIN);
			rs = ps.executeQuery();
			ArrayList<ActivityInstance> activityInstances = new ArrayList<ActivityInstance>();
			while(rs.next())
			{
				Date startTime =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("StartTime"));
				Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("EndTime"));
				Date userSubmissionTime = null;
				if(!(rs.getString("UserSubmissionTime") == null || rs.getString("UserSubmissionTime").isEmpty()))
					userSubmissionTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("UserSubmissionTime"));
				Date actualSubmissionTime = null;
				if(!(rs.getString("ActualSubmissionTime") == null || rs.getString("UserSubmissionTime").isEmpty()))
					actualSubmissionTime =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("ActualSubmissionTime"));
				String state = rs.getString("State");
				String sequence = rs.getString("Sequence");
				String activityInstanceId = rs.getString("ActivityInstanceId");
				String activityTitle = rs.getString("activityTitle");
				String description = rs.getString("description");
				String patientPin = rs.getString("PatientPinFK");



				ActivityInstance activityIns = new ActivityInstance(activityInstanceId,startTime,endTime, userSubmissionTime, actualSubmissionTime, state, sequence,activityTitle, description,patientPin) ;
				activityInstances.add(activityIns);

			}
			vo.putAttribute("ActivityInstances",activityInstances);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new DAOException("Unable to process results from query sql.chkActvyIns");
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
				if (connection != null) connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
				// YYY need a logging facility, but this does not have to be rethrown
				log.error(se);
			}
		}

		return vo;
	}

	//It is work in progress and needs to be changed accordingly depending upon the models that we are going to marshal. The arguments needs to be changed and correspondingly the logic needs to be modified.
	@Override
    public  ValueObject postActivityInstances(ArrayList<PostActivity> questionResults) throws SQLException, DAOException
    {
        ValueObject vo = new ValueObject(); // need to fill this up
        int updateCount = -1;
        if(questionResults != null)
        {
            //questionResults = subSurvey.questionResult;
            Connection connection = getConnection();
            try
            {
                //This boolean is set to false for performing the SQL transactions semantics.
                connection.setAutoCommit(false);
                String query = DAOFactory.getDAOProperties().getProperty("sql.submitSurvy");
                Timestamp userSubmissionTime = null;
                int activityInstanceId = -1;
                PreparedStatement ps = null;
                ps = connection.prepareStatement(query);
                for(PostActivity activity : questionResults)
                {
                    if(activity.getActivityId().equals("PI_DAILY")||activity.getActivityId().equals("PI_WEEKLY"))
                    {
                        PostPainIntensity painIntensity = (PostPainIntensity)activity;
                        userSubmissionTime = painIntensity.getUserSubmittedTimeStamp();
                        activityInstanceId = painIntensity.getActivityInstanceId();
                        try
                        {
                        	//This insert is for the bodypain intensity.
                        	if(painIntensity.getBodyPainLocation() != null)
                        	{
                        		 //This insert is for the bodypain location.
                                ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                                ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                                ps.setInt(3,painIntensity.getQuestionIds().get("bodyPain"));
                                ps.setInt(4, painIntensity.getBodyPainLocation());
                                //ps.setInt(4, painIntensity.getLocation());
                                ps.setInt(5,painIntensity.getActivityInstanceId());
                                ps.setString(6, "");
                                ps.setString(7, "");
                                ps.addBatch();
                        	}

                        	//This insert is for the bodypain intensity.
                            if(painIntensity.getBodyPainIntensity() != null)
                            {

                                ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                                ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                                ps.setInt(3,painIntensity.getQuestionIds().get("bodyPain"));
                                ps.setInt(4, painIntensity.getBodyPainIntensity());
                                ps.setInt(5,painIntensity.getActivityInstanceId());
                                ps.setString(7, "");
                                ps.addBatch();
                            }

                            //This insert is for generalized pain intensity.
                            if(painIntensity.get_generalizedPainInensity() != null)
                            {
                            	for(int genralizedPainOption : painIntensity.get_generalizedPainInensity())
                                {
                            		System.out.println("The timeStamp is::"+new Timestamp(new Date().getTime()));
                                	ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                                    ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                                    ps.setInt(3,painIntensity.getQuestionIds().get("generalizedPain"));
                                    ps.setInt(4, genralizedPainOption);
                                    ps.setInt(5,painIntensity.getActivityInstanceId());
                                    ps.setString(7, "");
                                    ps.addBatch();
                                }
                            }


                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                            if(ps != null)
                                ps.close();
                            connection.rollback();
                            throw e;
                        }
                    }
                    else if(activity.getActivityId().equals("CAT")|| activity.getActivityId().equals("MA") ||activity.getActivityId().equals("PR_Anxiety") || activity.getActivityId().equals("PR_Fatigue")|| activity.getActivityId().equals("PR_PainInt") || activity.getActivityId().equals("PR_PhysFuncMob"))
                    {
                        PostPromisSurvey promisSurvey = (PostPromisSurvey) activity;
                        userSubmissionTime = promisSurvey.getUserSubmittedTimeStamp();
                        activityInstanceId = promisSurvey.getActivityInstanceId();
                        HashMap<Integer,ArrayList<PostPromisSurvey.OptionToValue>> questionToOption = promisSurvey.getQuestionToOptions();
                        for(int questionId : questionToOption.keySet())
                        {
                        	ArrayList<PostPromisSurvey.OptionToValue> listOfOptions = questionToOption.get(questionId);
                        	for(OptionToValue eachValue:listOfOptions)
                        	{
                        		try
                                {
                                    ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                                    ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                                    ps.setInt(3,questionId);
                                    ps.setInt(4, eachValue.getOptionId());
                                    ps.setInt(5,promisSurvey.getActivityInstanceId());
                                    ps.setString(6, eachValue.getValue());
                                    ps.setString(7, eachValue.getDosage());
                                    ps.addBatch();
                                }
                                catch(Exception e)
                                {
                                    e.printStackTrace();
                                    if(ps != null)
                                        ps.close();
                                    connection.rollback();
                                    throw e;
                                }
                        	}

                        }
                    }
                }

                int[] executeResult = ps.executeBatch();
                ps.close();
                //If the Question Result Table is not inserted then we have to abort the transaction.

                if(executeResult.length <= 0 || (executeResult.length > 0 && executeResult[0] < 0))
                {
                    connection.rollback();
                    log.info("The batch processing failed::Rollingback Transactions");
                    vo.putAttribute("result", false);
                    return vo;
                }
                ps = null;
                Date date = new Date();
                Timestamp currentTime = new Timestamp(date.getTime());
                try
                {
                    query = DAOFactory.getDAOProperties().getProperty("sql.subSurvyActvyIns");
                    ps = connection.prepareStatement(query);
                    ps.setTimestamp(1,userSubmissionTime);
                    ps.setTimestamp(2,currentTime);
                    ps.setString(3,"completed");
                    ps.setInt(4,activityInstanceId);

                    updateCount = ps.executeUpdate();
                    ps.close();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    if(ps != null)
                        ps.close();
                    connection.rollback();
                    throw e;
                }
                //Only if the insert into question_result and the update in the survey_instance succeed we have to commit the transaction.
                if(updateCount > 0)
                    connection.commit();
                else
                    connection.rollback();
            }
            catch(Exception e)
            {
                connection.rollback();
                e.printStackTrace();
                throw e;
            }
            finally
            {
                try
                {
                    connection.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        vo.putAttribute("result",(updateCount>0) ? true:false);
        return vo;
    }
	@Override
	public ValueObject getQuestionOptionByText(String questionOptionText) throws DAOException
	{
		ValueObject vo = new ValueObject(); // need to fill this up
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			String query = DAOFactory.getDAOProperties().getProperty("sql.quesOpt");
			ps = connection.prepareStatement(query);
			ps.setString(1, questionOptionText);
			rs = ps.executeQuery();
			QuestionOption questionOpt = null;
			if(rs.next())
			{
				int questionOptionId =  rs.getInt("QuestionOptionId");
				int questionOptionOrder = rs.getInt("OptionOrder");
				int questionOptionType = rs.getInt("QuestionOptionType");

				questionOpt = new QuestionOption(questionOptionId,questionOptionText,questionOptionOrder,questionOptionType);
			}
			vo.putAttribute("questionOption",questionOpt);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new DAOException("Unable to process results from query sql.quesOpt");
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
				if (connection != null) connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
				// YYY need a logging facility, but this does not have to be rethrown
				log.error(se);
			}
		}
		return vo;
	}

	@Override
	//Create an activity instance.
	public ValueObject createActivityInstance(String sequence,String patientPIN,String startTime,String endTime,Trial trialType,String activityID) throws DAOException
	{
		ValueObject vo = new ValueObject(); // need to fill this up
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			String query = DAOFactory.getDAOProperties().getProperty("sql.createActvyIns");
			ps = connection.prepareStatement(query);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		    Date parsedstartTime = dateFormat.parse(startTime);
		    Timestamp startTimestamp = new Timestamp(parsedstartTime.getTime());
		    Date parsedendTime = dateFormat.parse(endTime);
		    Timestamp endTimestamp = new Timestamp(parsedendTime.getTime());
            String title = "";
            String description = "";

        	String metaData = new ModelFactory().getActivityMetaData(activityID);
        	JSONObject actvtMetaData = new JSONObject();
        	actvtMetaData = (JSONObject) new JSONParser().parse(metaData);
        	String typeOfActivity = (String)actvtMetaData.get("typeoFActivity");
        	System.out.println("The activity id is::"+typeOfActivity);

            if(typeOfActivity.equalsIgnoreCase("Weekly")){
              //Setting the title and description.
                title = DAOFactory.getDAOProperties().getProperty("sickle.weekly.title");
                description = DAOFactory.getDAOProperties().getProperty("sickle.weekly.description");
            }
            else if(typeOfActivity.equalsIgnoreCase("Daily")){
                //Setting the title and description.
                title = DAOFactory.getDAOProperties().getProperty("sickle.daily.title");
                description = DAOFactory.getDAOProperties().getProperty("sickle.daily.description");
            }
				ps.setTimestamp(1,startTimestamp);
				ps.setTimestamp(2,endTimestamp);
				ps.setString(3, "pending");
				ps.setString(4,patientPIN);
				ps.setString(5,sequence);
				ps.setTimestamp(6,new Timestamp(new Date().getTime()));
				ps.setString(7, title);
				ps.setString(8, description);

				//Gonna execute the query.
				int rowCount = ps.executeUpdate();
				boolean result = (rowCount >=1)? true:false;

				//The result of the insert statement.
				vo.putAttribute("result", result);

		} catch (Throwable t) {
			t.printStackTrace();
			throw new DAOException("Unable to process results from query sql.createActvyIns");
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
				if (connection != null) connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
				// YYY need a logging facility, but this does not have to be rethrown
				log.error(se);
			}
		}

		return vo;
	}

	//This method is the wrapper that constructs the medical adherence questions and its question options.
	@Override
	public ValueObject getMedicalAdherence(String patientPIN) throws DAOException {
		ValueObject vo = new ValueObject(); // need to fill this up
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			String hydroxyUreaPrescribed = DAOFactory.getDAOProperties().getProperty("sql.ishydroxyprescribed");
			ps = connection.prepareStatement(hydroxyUreaPrescribed);
			ps.setString(1, patientPIN);
			rs = ps.executeQuery();
			boolean ishydroxyUreaPrescribed = false;
			String patientType = "";
			if(rs.next())
			{
				String hydroxyUreaTablet = rs.getString("HydroxyureaPrescribed");
				if(hydroxyUreaTablet.equals("0"))
					ishydroxyUreaPrescribed = false;
				else
					ishydroxyUreaPrescribed = true;

				patientType =  rs.getString("type");
			}

			rs.close();
			ps.close();

			//get survey block id
			String queryBlockId = DAOFactory.getDAOProperties().getProperty("sql.surveyBlockId");
			ps = connection.prepareStatement(queryBlockId);
			String surveyBlockId = "";
			ps.setString(1, "MA");
			ps.setString(2, patientType);
			rs = ps.executeQuery();
			if(rs.next())
			{
				surveyBlockId =  rs.getString("SurveyBlockId");


			}
			rs.close();
			ps.close();

			String maqueryToselect = (ishydroxyUreaPrescribed == true)?"sql.ma":"sql.mawithouthydroxy";
			String query = DAOFactory.getDAOProperties().getProperty(maqueryToselect);
			ps = connection.prepareStatement(query);
			ps.setString(1, surveyBlockId);

			if(!ishydroxyUreaPrescribed)
			{
				String hydroxyUrea = surveyBlockId + "_HYDROXYUREAQUESTION";
				String hydroxyUreaquestionID = DAOFactory.getDAOProperties().getProperty(hydroxyUrea);
				System.out.println("hydroxyUreaQuestionID::"+hydroxyUreaquestionID);
				ps.setInt(2,Integer.parseInt(hydroxyUreaquestionID));
				String withExceptClause =  surveyBlockId + "_WITHEXCEPTCLAUSE";
				String withExceptClauseQuestionID = DAOFactory.getDAOProperties().getProperty(withExceptClause);
				ps.setInt(3,Integer.parseInt(withExceptClauseQuestionID));
				System.out.println("withExceptClause::"+withExceptClauseQuestionID);
			}
			else
			{
				String withoutExcept = surveyBlockId + "_WITHOUTEXCEPTCLAUSE";
				String withoutExceptClause = DAOFactory.getDAOProperties().getProperty(withoutExcept);
				System.out.println("WithoutExceptClause::"+withoutExceptClause);
				ps.setInt(2,Integer.parseInt(withoutExceptClause));
			}

			rs = ps.executeQuery();
			ArrayList<Question> questions = new ArrayList<Question>();
			while(rs.next())
			{
				String questionId = rs.getString("QuestionId");
				String questionText = rs.getString("QuestionText");
				int questionOption = rs.getInt("QuestionOptionType");
				String questionType = rs.getString("QuestionType");
				Type questionTyp = Question.Type.valueOf(questionType);

				Question question = new Question(questionId,questionTyp,questionText,"",questionOption,"");
				questions.add(question);
			}
				vo.putAttribute("Questions",questions);
				rs.close();
				ps.close();
				Patient patient = new ModelFactory().getPatient(patientPIN);
				String childPIN = "";
				if(patient.getType().equals(Patient.PatientType.parent_proxy.toString()))
					childPIN = patient.getChildPIN();
				String medicationInfo = DAOFactory.getDAOProperties().getProperty("sql.medicationInfo");
				ps = connection.prepareStatement(medicationInfo);
				if(patient.getType().equals(Patient.PatientType.parent_proxy.toString()))
					ps.setString(1, childPIN); //If it is the parent , we get the child medication info.
				else
					ps.setString(1, patientPIN);
				rs = ps.executeQuery();
				ArrayList<MedicationInfo> medicationInfoList = new ArrayList<MedicationInfo>();
				while(rs.next())
				{
					String medicationName = rs.getString("MedicationName");
					int defaultDosage = rs.getInt("defaultDosage");
					int prescribedDosage = rs.getInt("prescribedDosage");
					int noOfTablets = rs.getInt("noOfTablets");
					String units = rs.getString("units");
					int questionOptionId = rs.getInt("QuestionOptionId");
					MedicalAdherence med = new MedicalAdherence("MA");
					MedicationInfo medInfo = med.new MedicationInfo(medicationName,defaultDosage,noOfTablets,prescribedDosage,units,questionOptionId);

					medicationInfoList.add(medInfo);
				}
				vo.putAttribute("medicationInfo",medicationInfoList);

		} catch (Throwable t) {
			t.printStackTrace();
			throw new DAOException("Unable to process results from query sql.ma");
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
				if (connection != null) connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
				// YYY need a logging facility, but this does not have to be rethrown
				log.error(se);
			}
		}
		return vo;
	}

	//This method is the wrapper that constructs the adaptive questions and its question options.
		@Override
		public ValueObject getAdaptive(String patientPIN) throws DAOException {
			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			try	{
				//Need to get the surveyBlockType from patients table
				String patientQuery = DAOFactory.getDAOProperties().getProperty("sql.patientType");
				ps = connection.prepareStatement(patientQuery);
				ps.setString(1, patientPIN);
				rs = ps.executeQuery();
				String patientType = "";
				if(rs.next())
				{
					patientType =  rs.getString("type");

				}
				rs.close();
				ps.close();

				//get survey block id
				String queryBlockId = DAOFactory.getDAOProperties().getProperty("sql.surveyBlockId");
				ps = connection.prepareStatement(queryBlockId);
				ps.setString(1, "CAT");
				ps.setString(2, patientType);
				rs = ps.executeQuery();
				String surveyBlockId = "";
				if(rs.next())
				{
					surveyBlockId =  rs.getString("SurveyBlockId");
				}
				rs.close();
				ps.close();

				String query = DAOFactory.getDAOProperties().getProperty("sql.adaptive");
				ps = connection.prepareStatement(query);
				//ps.setString(1, "likert5");
				ps.setString(1,surveyBlockId);
				rs = ps.executeQuery();
				ArrayList<Question> questions = new ArrayList<Question>();
				while(rs.next())
				{
					String questionId = rs.getString("QuestionId");
					String questionText = rs.getString("QuestionText");
					int questionOption = rs.getInt("QuestionOptionType");
					String questionType = rs.getString("QuestionType");
					Question.Type questionTypeEnum = null;
					for(Question.Type type :Question.Type.values())
					{
						if(type.name().equalsIgnoreCase(questionType))
						{
							questionTypeEnum = type;
						}
					}
					Question question = new Question(questionId,questionTypeEnum,questionText,"",questionOption,"");
					questions.add(question);
				}
				vo.putAttribute("Questions",questions);

				rs.close();
				ps.close();

				String catMappingQuery = DAOFactory.getDAOProperties().getProperty("sql.catmapping");
				ps = connection.prepareStatement(catMappingQuery);
				rs = ps.executeQuery();
				HashMap<Integer,ArrayList<Integer>> catMapping = new HashMap<Integer,ArrayList<Integer>>();
				while(rs.next())
				{
					int questionId = rs.getInt("QuestionIdFK");
					int nestedQuestionId = rs.getInt("NestedQuestionIdFK");
					int nextQuestionId = rs.getInt("NextQuestionIdFK");

					ArrayList<Integer> catQuestionIdMapping = new ArrayList<Integer>();
					catQuestionIdMapping.add(nestedQuestionId);
					catQuestionIdMapping.add(nextQuestionId);
					catMapping.put(questionId, catQuestionIdMapping);
				}
				vo.putAttribute("catquestionmapping",catMapping);

			} catch (Throwable t) {
				throw new DAOException("Unable to process results from query sql.adaptive");
			} finally {
				try {
					if (rs != null) rs.close();
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
					log.error(se);
				}
			}
			return vo;
		}

	//This method changes the state of the activity instance.
		@Override
		public ValueObject changeActivityInstanceState(int activityInstance,String state) throws DAOException
		{
			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			int updateCount = -1;
			try
			{
				String query = DAOFactory.getDAOProperties().getProperty("sql.updateActvyIns");
				ps = connection.prepareStatement(query);
				ps.setString(1,state);
				ps.setInt(2,activityInstance);

				updateCount = ps.executeUpdate();
			}
			catch(Exception e)
			{
				throw new DAOException("Unable to process results from query sql.updateActvyIns");
			}
			finally {
				try {
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
					log.error(se);
				}
			}
			vo.putAttribute("result",(updateCount>0) ? true:false);
			return vo;
		}

		@Override
		public ValueObject postUILogger(ArrayList<UILogger> loggerResults) throws SQLException, DAOException{

			ValueObject vo = new ValueObject(); // need to fill this up
			int insertCount = -1;

			if(loggerResults != null){
				Connection connection = getConnection();
				try{
					//This boolean is set to false for performing the SQL transactions semantics.
	                connection.setAutoCommit(false);
	                String query = DAOFactory.getDAOProperties().getProperty("sql.submitUILogger");

	                PreparedStatement ps = null;
	                ps = connection.prepareStatement(query);
	                try{
	                	for(UILogger logger : loggerResults){
	                		ps.setTimestamp(1, new Timestamp(new Date().getTime()));
		                	ps.setString(2, logger.getPatientPin());
		                	ps.setString(3, logger.getEventName());
		                	ps.setString(4, logger.getMetaData().toString());
		                	ps.setTimestamp(5, logger.getEventTime());
		                	ps.addBatch();
		                }
	                }catch(Exception e){
	                	e.printStackTrace();
                        if(ps != null)
                            ps.close();
                        connection.rollback();
                        throw e;
	                }

	                int[] executeResult = ps.executeBatch();
	                insertCount = executeResult[0];
	                ps.close();

	                //If the  UI_Logger Table is not inserted then we have to abort the transaction.

	                if(executeResult.length <= 0 || (executeResult.length > 0 && executeResult[0] < 0))
	                {
	                    connection.rollback();
	                    log.info("The batch processing of ui_logger failed::Rollingback Transactions");
	                    vo.putAttribute("result", false);
	                    return vo;
	                }
	                if(insertCount > 0){
	                	connection.commit();
	                }
	                else{
	                	connection.rollback();
	                }
				}
				catch(Exception e){
					  connection.rollback();
		              e.printStackTrace();
		              throw e;
				}
				finally
	            {
	                try
	                {
	                    connection.close();
	                } catch (SQLException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                    throw e;
	                }
	            }
			}

			vo.putAttribute("result",(insertCount > 0) ? true:false);
	        return vo;

		}

		/**
		 * This function returns the metadata for the specified actvity.
		 * @param activityID - The activity for which we the metadata.
		 * @return value object - It contains the metadata.
		 * @throws DAOException
		 */
		@Override
		public ValueObject getActivityMetaData(String activityID) throws DAOException
		{
			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			String jsonMetaData = null;
			try
			{
				String query = DAOFactory.getDAOProperties().getProperty("sql.actvtymetadata");
				ps = connection.prepareStatement(query);
				ps.setString(1,activityID);

				rs = ps.executeQuery();

				if(rs.next())
				{
					jsonMetaData = rs.getString("metadata");
				}
			}
			catch(Exception e)
			{
				throw new DAOException("Unable to process results from query sql.updateActvyIns");
			}
			finally {
				try {
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
					log.error(se);
				}
			}
			vo.putAttribute("metadata",jsonMetaData);
			return vo;

		}

		//This is the method is taken by
		@Override
		public ValueObject enrollPatients(ArrayList<Patient.PatientEnroll> patientsInfos) throws SQLException,DAOException
		{
			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			LocalDateTime startDate = null;
			LocalDateTime endDate = null;
			try	{
					connection.setAutoCommit(false);
					String query = "";
					int maxPatientPin = -1;
					for(Patient.PatientEnroll patientInfo:patientsInfos)
					{
						query = DAOFactory.getDAOProperties().getProperty("sql.getMaxPatientPIN");
						ps = connection.prepareStatement(query);
						ps.setString(1, patientInfo.getPatientType());
						rs = ps.executeQuery();
						QuestionOption questionOpt = null;
						if(rs.next())
						{
							maxPatientPin =  rs.getInt("MaxPatientPIN");
						}

						rs.close();
						ps.close();

						if(maxPatientPin < 0)
						{
							//connection.rollback();
							log.error("Unable to get the last patient pin");
							vo.putAttribute("result","-1");
							return vo;
						}
						String ptype = patientInfo.getPatientType();
						System.out.println("patient type : " + ptype);
						int temp_pin = maxPatientPin + 1;
						Boolean enhancedContent = false;
						System.out.println("New pin is : " + temp_pin);

						// check whether pin generated is odd or even
						// if it is odd then we need to set the EnhancedContent field to true
						// else EnhancedContent field is set to false
						
						if(!(ptype.equals("parent_proxy"))){
							System.out.println("It is not a parent pin");
							if((temp_pin % 2) == 1){
								System.out.println("The pin generated is an odd number");
								System.out.println("EnhancedContent flag set to TRUE");
								enhancedContent = true;
							}
						}
						
						startDate = LocalDateTime.now();	//	DateStarted is current Date and Time
						endDate = startDate.plusDays(36);	//	DateCompleted is 36 days from DateStarted
						
						
						query = DAOFactory.getDAOProperties().getProperty("sql.createPatient");
						System.out.println("The values are::"+maxPatientPin+"::"
								+patientInfo.getDeviceType()+"::"+patientInfo.getDeviceVersion()+"::"+patientInfo.getChildPIN()+
								patientInfo.getPatientType()+patientInfo.isHydroxyUreaPrescribed());
						ps = connection.prepareStatement(query);
						ps.setString(1,Integer.toString(++maxPatientPin));
						ps.setString(2,patientInfo.getDeviceType());
						ps.setString(3,patientInfo.getDeviceVersion());
						ps.setTimestamp(4, new Timestamp(startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
                        ps.setTimestamp(5, new Timestamp(endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
						ps.setString(6,"1");
						ps.setString(7,patientInfo.getChildPIN());
						ps.setString(8,patientInfo.getPatientType());
						//System.out.println("The boolean value::"+Boolean.(patientInfo.isHydroxyUreaPrescribed()));
						ps.setString(9,patientInfo.isHydroxyUreaPrescribed());
						ps.setBoolean(10, enhancedContent);

						int insertCount = ps.executeUpdate();
						ps.close();

						if(insertCount <=0)
						{
							connection.rollback();
							log.error("Unexpected error while performing insert for patient");
							vo.putAttribute("result", "-1");
							return vo;
						}

						if(!patientInfo.getPatientType().equals(Patient.PatientType.parent_proxy.toString()))
						{
							query = DAOFactory.getDAOProperties().getProperty("sql.insertMedicationInfo");
							ps = connection.prepareStatement(query);
							for(Patient.medicationInfo medicationInfo : patientInfo.getPatientMedication())
							{
								ps.setString(1,Integer.toString(maxPatientPin));
								ps.setString(2,medicationInfo.getMediceName());
								ps.setInt(3,1);
								ps.setInt(4,medicationInfo.getPrescribedDosage());
								ps.setInt(5,medicationInfo.getNoOfTablets());
								ps.setString(6,medicationInfo.getUnits());
								ps.addBatch();
							}

							int[] executeResult = ps.executeBatch();
							ps.close();

							if(executeResult.length <= 0 || (executeResult.length > 0 && executeResult[0] < 0))
			                {
			                    connection.rollback();
			                    log.error("The batch processing failed::Rollingback Transactions");
			                    vo.putAttribute("result", "-1");
			                    return vo;
			                }
						}

						// If everything goes fine,we are committing the transaction
						connection.commit();
						vo.putAttribute("result", maxPatientPin);

						//ps.close();
					}
				} catch (Throwable t) {
				connection.rollback();
				t.printStackTrace();
				throw new DAOException("Unable to process results for patient enrollment");
			} finally {
				try {
					if (rs != null) rs.close();
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
					log.error(se);
					throw se;
				}
			}
			return vo;
		}
		/**
		 * This method will return the trial duration
		 * @param trial name
		 * @return valueObject
		 * @throws SQLException
		 * @throws DAOException
		 */
		@Override
		public ValueObject getTrialDuration(String trialName) throws SQLException,DAOException{
			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			String startTime = "";
			String endTime = "";
			int duration = -1;
			try
			{
				String query = DAOFactory.getDAOProperties().getProperty("sql.trialDuration");
				ps = connection.prepareStatement(query);
				ps.setString(1, trialName);
				rs = ps.executeQuery();

				if(rs.next())
				{
					startTime = rs.getString("IRBStart");
					endTime = rs.getString("IRBEnd");
					duration = rs.getInt("Duration");
				}
				if(!startTime.isEmpty() && !endTime.isEmpty())
				{
					vo.putAttribute("startTime", startTime);
					vo.putAttribute("endTime", endTime);
					vo.putAttribute("Duration", duration);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new DAOException("Unable to process results from query sql.trialDuration");
			}
			finally {
				try {
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
					log.error(se);
				}
			}
			return vo;
		}

		@Override
		public ValueObject getPatientBadges(String patientPIN) throws SQLException, DAOException
		{
			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;

			try{
				String query = DAOFactory.getDAOProperties().getProperty("sql.getPatBadges");
				ps = connection.prepareStatement(query);
				ps.setString(1,patientPIN);

				rs = ps.executeQuery();

				ArrayList<PatientBadges> patientBadgesArray = new ArrayList<PatientBadges>();

				while(rs.next()){
					String pin = rs.getString("PatientPinFK");
					String badgeId = rs.getString("BadgeIdFK");
					String activityInstanceId = rs.getString("ActivityInstanceIdFK");
					Boolean badgeUsed = false;
					if(rs.getBoolean("Used")){
						badgeUsed = true;
					}

					PatientBadges patBadge = new PatientBadges(pin, badgeId, activityInstanceId, badgeUsed);
					patientBadgesArray.add(patBadge);
				}
				vo.putAttribute("PatientBadges",patientBadgesArray);

			}catch(Exception e){
				throw new DAOException("Unable to process results from query sql.getPatBadges");
			}
			finally{
				try {
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be thrown again
					log.error(se);
				}
			}
			return vo;

		}

		@Override
		public ValueObject getBadge(String badgeId) throws SQLException, DAOException{

			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			Badge badge = null;
			try{
				String query = DAOFactory.getDAOProperties().getProperty("sql.getBadge");
				ps = connection.prepareStatement(query);
				ps.setString(1, badgeId);
				rs = ps.executeQuery();

				if(rs.next()){
					String badgeID = rs.getString("Id");
					String badgeName = rs.getString("Name");
					String badgeDesc = rs.getString("Description");
					String badgeType = rs.getString("Type");

					badge = new Badge(badgeID, badgeName,badgeDesc,badgeType);
				}
				vo.putAttribute("Badge",badge);
			}catch(Exception e){
				e.printStackTrace();
				throw new DAOException("Unable to process results from query sql.getBadge");
			}finally{
				try {
					if (rs != null) rs.close();
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
					log.error(se);
				}
			}
			return vo;
		}

		@Override
		public ValueObject getBadgePowerup(String badgeId) throws SQLException, DAOException{

			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;

			BagdePowerups badgePowerup = null;

			try{
				String query = DAOFactory.getDAOProperties().getProperty("sql.getBadgePowrup");
				ps = connection.prepareStatement(query);
				ps.setString(1,badgeId);

				rs = ps.executeQuery();

				if(rs.next()){
					String badgeIdFK = rs.getString("BadgeIdFK");
					String powerupIdFK = rs.getString("PowerupIdFK");
					int powerupCount = rs.getInt("PowerupCount");

					badgePowerup = new BagdePowerups(badgeIdFK, powerupIdFK, powerupCount);
				}
				vo.putAttribute("BadgePowerup",badgePowerup);
			}catch(Exception e){
				throw new DAOException("Unable to process results from query sql.getPatBadges");
			}finally{
				try {
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be thrown again
					log.error(se);
				}
			}
			return vo;
		}

		@Override
		public ValueObject getPowerup(String powerupId) throws SQLException, DAOException{

			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			Powerups powerup = null;
			try{
				String query = DAOFactory.getDAOProperties().getProperty("sql.getPowerup");
				ps = connection.prepareStatement(query);
				ps.setString(1, powerupId);
				rs = ps.executeQuery();


				if(rs.next()){
					String pid = rs.getString("Id");
					String powerupName = rs.getString("Name");
					String powerupDesc = rs.getString("Description");

					powerup = new Powerups(pid, powerupName,powerupDesc);
				}
				vo.putAttribute("Powerup",powerup);
			}catch(Exception e){
				throw new DAOException("Unable to process results from query sql.getPowerup");
			}finally{
				try {
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be thrown again
					log.error(se);
				}
			}
			return vo;
		}

		@Override
		public ValueObject insertPatientPowerup(PatientPowerups patientPowerup) throws SQLException, DAOException{

			System.out.println("Inside insertPatientPowerup JDBCDAO");
			ValueObject vo = new ValueObject();

			if(patientPowerup != null){
				Connection connection = getConnection();
				try{
					connection.setAutoCommit(false);
	                String query = DAOFactory.getDAOProperties().getProperty("sql.insertPatPowerup");
	                PreparedStatement ps = null;
	                ps = connection.prepareStatement(query);

	                ps.setString(1, patientPowerup.getPatientPin());
	                ps.setString(2, patientPowerup.getPowerupId());
	                ps.setInt(3, patientPowerup.getCount());
	                ps.setTimestamp(4, new Timestamp(new Date().getTime()));
	                ps.setTimestamp(5, new Timestamp(new Date().getTime()));

	                int insertCount = ps.executeUpdate();
	                ps.close();

	                if(insertCount <=0 ){
	                	connection.rollback();
	                	log.error("Unexpected error while performing insert for patient_powerups");
						vo.putAttribute("result", false);
						return vo;
	                }

	                connection.commit();
					vo.putAttribute("result", true);

				}catch(Exception e){
					connection.rollback();
					e.printStackTrace();
					throw new DAOException("Unable to process insert for patient powerup");
				}finally{
					try {
						if (connection != null) connection.close();
					} catch (SQLException se) {
						se.printStackTrace();
						// YYY need a logging facility, but this does not have to be rethrown
						log.error(se);
						throw se;
					}
				}
			}
			return vo;
		}

		@Override
		public ValueObject updatePatientPowerup(PatientPowerups patientPowerup) throws SQLException, DAOException{

			System.out.println("Inside updatePatientPowerup JDBCDAO");
			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			int updateCount = -1;

			try{
				connection.setAutoCommit(false);
				String query = DAOFactory.getDAOProperties().getProperty("sql.updPatPowerup");
				ps = connection.prepareStatement(query);

				ps.setInt(1, patientPowerup.getCount());
				ps.setTimestamp(2, new Timestamp(new Date().getTime()));
				ps.setString(3, patientPowerup.getPatientPin());
				ps.setString(4, patientPowerup.getPowerupId());


				updateCount = ps.executeUpdate();

				if(updateCount <= 0){
					log.error("Unexpected error while performing update for patient_powerup");
					vo.putAttribute("result", false);
					return vo;
				}
				connection.commit();
				vo.putAttribute("result", true);
			}catch(Exception e){
				throw new DAOException("Unable to process results from query sql.updPatPowerup");
			}finally{
				try {
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
					log.error(se);
				}
			}

			return vo;
		}

		@Override
		public ValueObject checkPatientPowerup(String patientPIN, String powerupId) throws SQLException, DAOException{

			System.out.println("Inside getPatientPowerup JDBCDAO");
			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;

			try{

				String query = DAOFactory.getDAOProperties().getProperty("sql.chkPatPowerup");
				ps = connection.prepareStatement(query);
				ps.setString(1, patientPIN);
				ps.setString(2, powerupId);

				rs = ps.executeQuery();
				PatientPowerups patientPowerup = null;

				if(rs.next()){
					String pin = rs.getString("PatientPinFK");
					String powUpId = rs.getString("PowerupIdFK");
					int pcount = rs.getInt("Count");

					patientPowerup = new PatientPowerups(pin, powUpId, pcount);
				}
				vo.putAttribute("PatientPowerup", patientPowerup);
			}catch(Exception e){
				e.printStackTrace();
				throw new DAOException("Unable to process results from query sql.chkPatPowerup");
			}finally{
				try {
					if (rs != null) rs.close();
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
					log.error(se);
				}
			}
			return vo;
		}

		@Override
		public ValueObject getGameDetails(String gameId) throws SQLException, DAOException{

			System.out.println("Inside getGameDetails");
			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;

			try{
				String query = DAOFactory.getDAOProperties().getProperty("sql.getGameDetails");
				ps = connection.prepareStatement(query);
				ps.setString(1, gameId);

				rs = ps.executeQuery();
				Games game = null;

				if(rs.next()){
					String gId = gameId;
					String gName = rs.getString("Name");
					String gDesc = rs.getString("Description");

					game = new Games(gId, gName, gDesc);
				}
				vo.putAttribute("GameInfo", game);
			}catch(Exception e){
				e.printStackTrace();
				throw new DAOException("Unable to process results from query sql.getGameDetails");
			}finally{
				try {
					if (rs != null) rs.close();
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
					log.error(se);
				}
			}
			return vo;
		}

		@Override
		public ValueObject getPatGamePlay(String patientPIN) throws SQLException, DAOException{

			System.out.println("Inside getPatientGamePlay JDBC DAO");
			ValueObject vo = new ValueObject();
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;

			PatientGamePlay patientGamePlay = null;

			try{
				String query = DAOFactory.getDAOProperties().getProperty("sql.getPatGamePlay");
				ps = connection.prepareStatement(query);
				ps.setString(1, patientPIN);
				rs = ps.executeQuery();

				if(rs.next()){
					String gameId = rs.getString("GameIdFK");
					Timestamp startTime = rs.getTimestamp("StartTime");
					Timestamp endTime = rs.getTimestamp("EndTime");

					patientGamePlay = new PatientGamePlay(patientPIN, gameId, startTime, endTime);
				}
			}
			catch(Exception e){
				throw new DAOException("Unable to process results from query sql.getPatGamePlay");
			}
			finally{
				try {
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be thrown again
					log.error(se);
				}
			}
			vo.putAttribute("PatientGamePlay",patientGamePlay);
			return vo;
		}

		@Override
		public ValueObject getPatientPowerups(String patientPIN) throws SQLException, DAOException{

			System.out.println("Inside getPatientPowerup JDBCDAO");
			System.out.println("patientPIN input : " + patientPIN);
			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			PatientPowerups patientPowerups = null;

			try{

				String query = DAOFactory.getDAOProperties().getProperty("sql.getPatPowerups");
				ps = connection.prepareStatement(query);
				ps.setString(1, patientPIN);
				System.out.println("Query param set");
				rs = ps.executeQuery();
				System.out.println("Executed the query");
				if(rs.next()){
					System.out.println("Inside IF statement");
					String powUpId = rs.getString("PowerupIdFK");
					System.out.println("powerupId : " + powUpId);
					int pcount = rs.getInt("Count");
					System.out.println("count received: " + pcount);

					patientPowerups = new PatientPowerups(patientPIN, powUpId, pcount);
					System.out.println("PatientPowerups object created");
				}
				vo.putAttribute("PatientPowerupDetails", patientPowerups);
			}catch(Exception e){
				e.printStackTrace();
				throw new DAOException("Unable to process results from query sql.getPatPowerups");
			}finally{
				try {
					if (rs != null) rs.close();
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
					log.error(se);
				}
			}
			return vo;
		}

		@Override
		public ValueObject insertPatientGamePlay(PatientGamePlay patientGamePlay) throws SQLException, DAOException{
			System.out.println("Inside insertPatientGamePlay JDBCDAO");
			ValueObject vo = new ValueObject();
			int insertCount = 0;

			if(patientGamePlay != null){
				Connection connection = getConnection();
				try{
					connection.setAutoCommit(false);
					String query = DAOFactory.getDAOProperties().getProperty("sql.insertPatGamePlay");
	                PreparedStatement ps = null;
	                ps = connection.prepareStatement(query);

	                ps.setString(1, patientGamePlay.getPatientPin());
	                ps.setString(2, patientGamePlay.getGameId());
	                ps.setTimestamp(3, patientGamePlay.getStartTime());
	                ps.setTimestamp(4, new Timestamp(new Date().getTime()));
	                ps.setTimestamp(5, new Timestamp(new Date().getTime()));

	                System.out.println("Executing the query");
	                insertCount = ps.executeUpdate();

	                System.out.println("Insert count is: " + insertCount);
	                ps.close();

	                if(insertCount <=0 ){
	                	connection.rollback();
	                	log.error("Unexpected error while performing insert for patientGamePlay");
						vo.putAttribute("result", false);
						return vo;
	                }
	                System.out.println("Query executed successfully");
	                connection.commit();
					vo.putAttribute("result", true);
				}catch(Exception e){
					connection.rollback();
					e.printStackTrace();
					throw new DAOException("Unable to process insert for patient Game play");
				}finally{
					try {
						if (connection != null) connection.close();
					} catch (SQLException se) {
						se.printStackTrace();
						// YYY need a logging facility, but this does not have to be rethrown
						log.error(se);
						throw se;
					}
				}
			}
			return vo;
		}

		@Override
		public ValueObject updatePatientGamePlay(PatientGamePlay patientGamePlay) throws SQLException, DAOException{
			System.out.println("Inside updatePatientGamePlay JDBCDAO");
			ValueObject vo = new ValueObject();
			int updateCount = -1;

			if(patientGamePlay != null){
				Connection connection = getConnection();
				try{
					connection.setAutoCommit(false);
					String query = DAOFactory.getDAOProperties().getProperty("sql.updPatGamePlay");
	                PreparedStatement ps = null;
	                ps = connection.prepareStatement(query);

	                ps.setTimestamp(1, patientGamePlay.getEndTime());
	                ps.setTimestamp(2, new Timestamp(new Date().getTime()));
	                ps.setString(3, patientGamePlay.getPatientPin());
	                ps.setString(4, patientGamePlay.getGameId());

	                System.out.println("Executing the query");
	                updateCount = ps.executeUpdate();

	                System.out.println("updateCount is: " + updateCount);
	                ps.close();

	                if(updateCount <=0 ){
	                	connection.rollback();
	                	log.error("Unexpected error while performing insert for updPatGamePlay");
						vo.putAttribute("result", false);
						return vo;
	                }
	                System.out.println("Query executed successfully");
	                connection.commit();
					vo.putAttribute("result", true);
				}catch(Exception e){
					connection.rollback();
					e.printStackTrace();
					throw new DAOException("Unable to process update for patient Game play");
				}finally{
					try {
						if (connection != null) connection.close();
					} catch (SQLException se) {
						se.printStackTrace();
						// YYY need a logging facility, but this does not have to be rethrown
						log.error(se);
						throw se;
					}
				}
			}
			return vo;
		}

		@Override
		public ValueObject checkPatientBadge(PatientBadges patientBadge) throws SQLException, DAOException{

			System.out.println("Inside checkPatientBadge JDBCDAO");
			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			Boolean result = false;

			try{
				String query = DAOFactory.getDAOProperties().getProperty("sql.chkPatBadge");
				ps = connection.prepareStatement(query);
				ps.setString(1, patientBadge.getPatientPin());
				ps.setString(2, patientBadge.getBadgeId());
				ps.setString(3, patientBadge.getActivityInstanceId());
				System.out.println("BadgeUsed : " + patientBadge.getBadgeUsed());
				ps.setBoolean(4, patientBadge.getBadgeUsed());

				rs = ps.executeQuery();

				PatientBadges patBadge = null;

				if(rs.next()){
					System.out.println("Record exists");
					result = true;
				}
				vo.putAttribute("result", result);
			}catch(Exception e){
				e.printStackTrace();
				throw new DAOException("Unable to process results from query sql.chkPatBadge");
			}finally{
				try {
					if (rs != null) rs.close();
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
					log.error(se);
				}
			}
			return vo;
		}

		@Override
		public ValueObject activatePatientBadge(ActivatePatientBadge activatePatientBadge) throws SQLException, DAOException{

			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;

			try{
				connection.setAutoCommit(false);
				String query = "";
				// check for patient powerup record first
				query = DAOFactory.getDAOProperties().getProperty("sql.chkPatPowerup");
				ps = connection.prepareStatement(query);
				ps.setString(1, activatePatientBadge.getPatientPin());
				ps.setString(2, activatePatientBadge.getPowerupId());

				rs = ps.executeQuery();

				if(rs.next()){
					System.out.println("Powerup exists for the patient");
					int powerupCount = rs.getInt("Count");

					// need to update the powerup count
					ps.close();
					System.out.println("Executing the update query for patient powerup record");
					query = DAOFactory.getDAOProperties().getProperty("sql.updPatPowerup");
					System.out.println("Query is initialized");
					ps = connection.prepareStatement(query);
					System.out.println("All set");
					int newCount = activatePatientBadge.getPowerupCount() + powerupCount;
					System.out.println("New count is : " + newCount);
					ps.setInt(1, newCount);
					ps.setTimestamp(2, new Timestamp(new Date().getTime()));
					ps.setString(3, activatePatientBadge.getPatientPin());
					ps.setString(4, activatePatientBadge.getPowerupId());

					System.out.println("All params set");
					int updateCount = ps.executeUpdate();
					System.out.println("Query executed");
					ps.close();

					if(updateCount <= 0){
						connection.rollback();
						log.error("Unexpected error while performing update for patient_powerup");
						vo.putAttribute("result", "-1");
						return vo;
					}
				}
				else{

					System.out.println("powerup does not exist for the patient");

					ps.close();

					System.out.println("Executing the insert query for patient powerup record");
					query = DAOFactory.getDAOProperties().getProperty("sql.insertPatPowerup");
					ps = connection.prepareStatement(query);
					ps.setString(1, activatePatientBadge.getPatientPin());
					ps.setString(2, activatePatientBadge.getPowerupId());
					ps.setInt(3, activatePatientBadge.getPowerupCount());
					ps.setTimestamp(4, new Timestamp(new Date().getTime()));
	                ps.setTimestamp(5, new Timestamp(new Date().getTime()));

	                int insertCount = ps.executeUpdate();
	                ps.close();

	                if(insertCount <=0 ){
	                	connection.rollback();
	                	log.error("Unexpected error while performing insert for patient_powerups");
						vo.putAttribute("result", false);
						return vo;
	                }
				}

				// need to update the patient_badges table

				rs.close();
				ps.close();
				System.out.println("Executing the update query for patient badge record");
				query = DAOFactory.getDAOProperties().getProperty("sql.updPatBadge");
				ps = connection.prepareStatement(query);
				ps.setBoolean(1, activatePatientBadge.getBadgeUsed());
				ps.setTimestamp(2, new Timestamp(new Date().getTime()));
				ps.setString(3, activatePatientBadge.getPatientPin());
				ps.setString(4, activatePatientBadge.getBadgeId());
				ps.setString(5, activatePatientBadge.getActivityInstanceId());

				int updateCount = ps.executeUpdate();
				ps.close();

				if(updateCount <= 0){
					connection.rollback();
					log.error("Unexpected error while performing update for patient_badges");
					vo.putAttribute("result", false);
					return vo;
				}

				// if everything goes fine
				connection.commit();
				vo.putAttribute("result", true);

			}catch (Throwable t) {
				connection.rollback();
				t.printStackTrace();
				throw new DAOException("Unable to process results for activate patient badge");
			}finally{
				try {
					if (rs != null) rs.close();
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
					log.error(se);
					throw se;
				}
			}
			return vo;
		}

		public ValueObject getWeeklyActivityCount(String patientPIN, String activityInstanceId) throws SQLException, DAOException{

			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;

			try{

				String query = DAOFactory.getDAOProperties().getProperty("sql.getWeeklyActivities");
				ps = connection.prepareStatement(query);
				ps.setString(1, patientPIN);
				rs = ps.executeQuery();
				int weeklyActivityCount = 0;
				while(rs.next()){

					String state = rs.getString("State");
					String currentInstanceId = rs.getString("ActivityInstanceId");
					System.out.println("Current activity instance id is : " + currentInstanceId);
					System.out.println("State received for the record is : " + state);
					if(state.equals("completed")){
						System.out.println("completed state found");
						weeklyActivityCount++;
						System.out.println("weeklyActivityCount is : " + weeklyActivityCount);
						if(currentInstanceId.equals(activityInstanceId)){
							System.out.println("Current instance id matches with input instance id. Finish searching");
							break;
						}
					}
					else{
						System.out.println("completed state not found");
						weeklyActivityCount = 0;
					}
				}

				System.out.println("The final weeklyActivityCount is : " + weeklyActivityCount);
				vo.putAttribute("WeeklyActivityCount", weeklyActivityCount);
			}catch(Throwable t){
				t.printStackTrace();
				throw new DAOException("Unable to process results from query sql.getWeeklyActivities");
			}finally {
				try {
					if (rs != null) rs.close();
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
				}
			}
			return vo;
		}

		public ValueObject getActivityBadges(String activityType, int activityCount) throws SQLException, DAOException{

			System.out.println("Inside getActivityBadges JDBC DAO method");
			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			String badgeId = null;
			try{

				String query = DAOFactory.getDAOProperties().getProperty("sql.getActivityBadges");
				ps = connection.prepareStatement(query);
				ps.setString(1, activityType);
				ps.setInt(2, activityCount);

				rs = ps.executeQuery();

				if(rs.next()){
					System.out.println("Received record");
					badgeId = rs.getString("BadgeIdFK");
					System.out.println("Badge id received is : " + badgeId);
				}
				System.out.println("Sending badgeId to model");
				vo.putAttribute("BadgeId", badgeId);
			}catch (Throwable t) {
				t.printStackTrace();
				throw new DAOException("Unable to process results from query sql.getActivityBadges");
			} finally {
				try {
					if (rs != null) rs.close();
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
				}
			}
			System.out.println("Returning back from JDBC DAO");
			return vo;
		}

		public ValueObject insertPatientBadge(PatientBadges patientBadge) throws SQLException, DAOException{

			ValueObject vo = new ValueObject(); // need to fill this up

			if(patientBadge != null){
				Connection connection = getConnection();

				try{

					connection.setAutoCommit(false);
	                String query = DAOFactory.getDAOProperties().getProperty("sql.insertPatBadge");
	                PreparedStatement ps = null;
	                ps = connection.prepareStatement(query);

	                ps.setString(1, patientBadge.getPatientPin());
	                ps.setString(2, patientBadge.getBadgeId());
	                ps.setString(3, patientBadge.getActivityInstanceId());
	                ps.setBoolean(4, patientBadge.getBadgeUsed());
	                ps.setTimestamp(5, new Timestamp(new Date().getTime()));
	                ps.setTimestamp(6, new Timestamp(new Date().getTime()));

	                int insertCount = ps.executeUpdate();
	                ps.close();

	                if(insertCount <=0 ){
	                	connection.rollback();
	                	log.error("Unexpected error while performing insert for patient_badges");
						vo.putAttribute("result", false);
						return vo;
	                }

	                connection.commit();
	                vo.putAttribute("result", true);
				}catch(Exception e){
					connection.rollback();
					e.printStackTrace();
					throw new DAOException("Unable to process insert for patient powerup");
				}finally{
					try {
						if (connection != null) connection.close();
					} catch (SQLException se) {
						se.printStackTrace();
						// YYY need a logging facility, but this does not have to be rethrown
						log.error(se);
						throw se;
					}
				}
			}

			return vo;
		}

		public ValueObject getDailyActivityCount(String patientPIN, String activityInstanceId, Date dailyInstanceStartTime) throws SQLException, DAOException{

			System.out.println("Inside getDailyActivityCount JDBC DAO method");

			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;

			try{

				String query = "";
				query = DAOFactory.getDAOProperties().getProperty("sql.getWeeklyActivities");
				ps = connection.prepareStatement(query);
				ps.setString(1, patientPIN);
				rs = ps.executeQuery();
				Date weekStart = new Date();
				Date weekEnd = new Date();
				weekStart = null;
				weekEnd = null;

				while(rs.next()){

					System.out.println("Query executed. Checking for the week start");
					Date currentInstanceStart =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("StartTime"));
					System.out.println("Current instance start time : " + currentInstanceStart);
						
					if(currentInstanceStart.after(dailyInstanceStartTime)){
						System.out.println("We have passed the week start already");
						break;
					}else{
						weekStart = currentInstanceStart;
						System.out.println("Setting the current instance of weekly time as week start");
						System.out.println("weekStart till now is : " + weekStart);
					}
				}

				System.out.println("Week start time received is : " + weekStart);

				rs.close();
				ps.close();

				// now calculate week end time

				if(weekStart != null){
					Calendar cal = Calendar.getInstance();
					cal.setTime(weekStart);
					cal.add(Calendar.DATE, 6);
					weekEnd = cal.getTime();
				}

				System.out.println("Week end time calculated is : " + weekEnd);

				 // call the query to get daily activities having start time between weekStart and weekEnd time

				 query = DAOFactory.getDAOProperties().getProperty("sql.getDailyActivities");
				 ps = connection.prepareStatement(query);
				 ps.setString(1, patientPIN);
				 ps.setTimestamp(2, new Timestamp(weekStart.getTime()));
				 ps.setTimestamp(3, new Timestamp(weekEnd.getTime()));
				 rs = ps.executeQuery();

				 int dailyActivityCount = 0;

				 while(rs.next()){
					 String state = rs.getString("State");
					 String currentInstanceId = rs.getString("ActivityInstanceId");

					 System.out.println("State received is : " + state);
					 System.out.println("current instance id received is : " + currentInstanceId);

						if(state.equals("completed")){
							dailyActivityCount++;
							if(currentInstanceId.equals(activityInstanceId)){
								System.out.println("current instance id matches with the input activityInstanceId");
								break;
							}
						}
						else{
							System.out.println("completed activity instance not found");
							dailyActivityCount = 0;
						}
				 }
				 System.out.println("The final weeklyActivityCount is : " + dailyActivityCount);
				 vo.putAttribute("DailyActivityCount", dailyActivityCount);
			}catch(Throwable t){
				t.printStackTrace();
				throw new DAOException("Unable to process results from query sql.getWeeklyActivities");
			}finally{
				try {
					if (rs != null) rs.close();
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be rethrown
				}
			}

			return vo;
		}

		@Override
		public ValueObject getPatientBadgeCount(String patientPIN, Boolean usedFlag) throws SQLException, DAOException{

			ValueObject vo = new ValueObject(); // need to fill this up
			Connection connection = getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;

			try{

				String query = DAOFactory.getDAOProperties().getProperty("sql.getPatBadges");
				ps = connection.prepareStatement(query);
				ps.setString(1,patientPIN);

				rs = ps.executeQuery();

				int badgeCount = 0;

				while(rs.next()){

					Boolean used = rs.getBoolean("Used");

					if(used.equals(usedFlag)){
						System.out.println("UsedFlag received matches with requirement");
						badgeCount++;
						System.out.println("badgeCount : " + badgeCount);
					}
				}

				System.out.println("Final badgeCount : " + badgeCount);
				vo.putAttribute("BadgeCount",badgeCount);
			}catch(Exception e){
				throw new DAOException("Unable to process results from query sql.getPatBadges");
			}finally{
				try {
					if (ps != null) ps.close();
					if (connection != null) connection.close();
				} catch (SQLException se) {
					se.printStackTrace();
					// YYY need a logging facility, but this does not have to be thrown again
					log.error(se);
				}
			}
			return vo;
		}
}
