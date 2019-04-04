package edu.asu.epilepsy.apiv30.dao.impl;

import edu.asu.epilepsy.apiv30.dao.DAO;
import edu.asu.epilepsy.apiv30.dao.DAOException;
import edu.asu.epilepsy.apiv30.dao.DAOFactory;
import edu.asu.epilepsy.apiv30.dao.ValueObject;
import edu.asu.epilepsy.apiv30.helper.APIConstants;
import edu.asu.epilepsy.apiv30.helper.GsonFactory;
import edu.asu.epilepsy.apiv30.model.ActivityInstance;
import edu.asu.epilepsy.apiv30.model.ContainerActivity;
import edu.asu.epilepsy.apiv30.model.FingerTappingParameters;
import edu.asu.epilepsy.apiv30.model.FlankerParameters;
import edu.asu.epilepsy.apiv30.model.MedicalAdherence;
import edu.asu.epilepsy.apiv30.model.MedicalAdherence.MedicationInfo;
import edu.asu.epilepsy.apiv30.model.ModelFactory;
import edu.asu.epilepsy.apiv30.model.Patient;
import edu.asu.epilepsy.apiv30.model.Patient.Trial;
import edu.asu.epilepsy.apiv30.model.PatternComparisonParameters;
import edu.asu.epilepsy.apiv30.model.PostActivity;
import edu.asu.epilepsy.apiv30.model.PostFingerTapping;
import edu.asu.epilepsy.apiv30.model.PostFlanker;
import edu.asu.epilepsy.apiv30.model.PostPainIntensity;
import edu.asu.epilepsy.apiv30.model.PostPatternComparison;
import edu.asu.epilepsy.apiv30.model.PostPromisSurvey;
import edu.asu.epilepsy.apiv30.model.PostPromisSurvey.OptionToValue;
import edu.asu.epilepsy.apiv30.model.PostSpatialSpan;
import edu.asu.epilepsy.apiv30.model.Question;
import edu.asu.epilepsy.apiv30.model.Question.Type;
import edu.asu.epilepsy.apiv30.model.QuestionOption;
import edu.asu.epilepsy.apiv30.model.Sequence;
import edu.asu.epilepsy.apiv30.model.SpatialSpanParameters;
import edu.asu.epilepsy.apiv30.model.UILogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class JdbcDAO implements DAO {
    private static final String TAG = JdbcDAO.class.getSimpleName();
    static Logger log = LogManager.getLogger(JdbcDAO.class);
    private String __jdbcDriver;
    protected String _jdbcUser;
    protected String _jdbcPasswd;
    protected String _jdbcUrl;

    public JdbcDAO(Properties props) throws DAOException {

        // For MySQL we expect the JDBC Driver, user, password, and the URI. Maybe more in the future.
        _jdbcUrl = props.getProperty("jdbc.url");
        _jdbcUser = props.getProperty("jdbc.user");
        _jdbcPasswd = props.getProperty("jdbc.passwd");
        __jdbcDriver = props.getProperty("jdbc.driver");

        try {
            Class.forName(__jdbcDriver); // ensure the driver is loaded
        } catch (ClassNotFoundException cnfe) {
            throw new DAOException("*** Cannot find the JDBC driver " + __jdbcDriver, cnfe);
        } catch (Throwable t) {
            throw new DAOException(t);
        }
    }

    /**
     * We really should implement some simple wrapper and pooling YYY
     *
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
        try {
            String query = DAOFactory.getDAOProperties().getProperty("sql.activityInstance");
            ps = connection.prepareStatement(query);
            ps.setString(1, activityInstanceId);
            rs = ps.executeQuery();
            if (rs.next()) {
                vo = new ValueObject();
                Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("StartTime"));
                Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("EndTime"));
                Date userSubmissionTime = null;
                if (!(rs.getString("UserSubmissionTime") == null || rs.getString("UserSubmissionTime").isEmpty()))
                    userSubmissionTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("UserSubmissionTime"));
                Date actualSubmissionTime = null;
                if (!(rs.getString("ActualSubmissionTime") == null || rs.getString("UserSubmissionTime").isEmpty()))
                    actualSubmissionTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("ActualSubmissionTime"));
                String state = rs.getString("State");
                Sequence sequence = GsonFactory.getInstance().getGson().fromJson(rs.getString("Sequence"),Sequence.class);
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
     * Get the activity parameters for an activity instance from the backing store
     */
    public ValueObject getActivityParameters(String activityName) throws DAOException {
        ValueObject vo = null; // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String query = DAOFactory.getDAOProperties().getProperty("sql.activityParameters");
            ps = connection.prepareStatement(query);
            ps.setString(1, activityName);
            rs = ps.executeQuery();
            if (rs.next()) {
                vo = new ValueObject();
                System.out.println(TAG + " getActivityParameters :- " + "ACTIVITY Parameters - " + rs.getString("Parameters"));

                if(activityName.equals("Flanker-Test")){
                    FlankerParameters parameters = GsonFactory.getInstance().getGson().fromJson(rs.getString("Parameters"), FlankerParameters.class);
                    vo.putAttribute("Parameters", parameters);
                }else if(activityName.equals("Spatial-Span")){
                    SpatialSpanParameters parameters= GsonFactory.getInstance().getGson().fromJson(rs.getString("Parameters"), SpatialSpanParameters.class);
                    System.out.println(TAG+"spatialSpanParameters:-"+rs.getNString("Parameters"));
                    vo.putAttribute("Parameters", parameters);
                } else if(activityName.equals("Pattern-Comparison")){
                    PatternComparisonParameters parameters= GsonFactory.getInstance().getGson().fromJson(rs.getString("Parameters"), PatternComparisonParameters.class);
                    System.out.println(TAG+"PatternComparisonParameters:-"+rs.getNString("Parameters"));
                    vo.putAttribute("Parameters", parameters);
                } else if(activityName.equals("Finger-Tapping")){
                    FingerTappingParameters parameters= GsonFactory.getInstance().getGson().fromJson(rs.getString("Parameters"), FingerTappingParameters.class);
                    System.out.println(TAG+"FingerTapping Parameters:-"+rs.getNString("Parameters"));
                    vo.putAttribute("Parameters", parameters);
                }
          }
        } catch (Throwable t) {
            t.printStackTrace();
            throw new DAOException("Unable to process results from query sql.activityParameters");
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
        try {
            String query = DAOFactory.getDAOProperties().getProperty("sql.pi");
            ps = connection.prepareStatement(query);
            if (activityID.equals(APIConstants.dailyActivityID_PI))
                ps.setString(1, "bodyPain_daily");
            else if (activityID.equals(APIConstants.weeklyActivityID_PI))
                ps.setString(1, "bodyPain_weekly");
            rs = ps.executeQuery();
            List<Question> questions = new ArrayList<Question>();
            while (rs.next()) {
                String questionId = rs.getString("QuestionId");
                String questionText = rs.getString("QuestionText");
                int questionOption = rs.getInt("QuestionOptionType");
                String questionType = rs.getString("QuestionType");
                Question.Type questionTypeEnum = null;
                for (Question.Type type : Question.Type.values()) {
                    if (type.name().equalsIgnoreCase(questionType)) {
                        questionTypeEnum = type;
                    }
                }
                Question question = new Question(questionId, questionTypeEnum, questionText, "", questionOption, "");
                questions.add(question);
            }
            vo.putAttribute("Question", questions);
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

        try {
            //Need to get the surveyBlockType from patients table
            String patientQuery = DAOFactory.getDAOProperties().getProperty("sql.patientType");
            ps = connection.prepareStatement(patientQuery);
            ps.setString(1, patientPIN);
            rs = ps.executeQuery();
            if (rs.next()) {
                patientType = rs.getString("type");

            }

            rs.close();
            ps.close();

            // get the sequencing - whether random or not from activity table
            String querySeq = DAOFactory.getDAOProperties().getProperty("sql.seq");
            ps = connection.prepareStatement(querySeq);
            ps.setString(1, activityId);
            rs = ps.executeQuery();
            if (rs.next()) {
                sequencing = rs.getString("canonicalOrder");


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
            if (rs.next()) {
                surveyBlockId = rs.getString("SurveyBlockId");


            }
            rs.close();
            ps.close();

            //get questions based on survey block id
            String queryForQues = (sequencing.equals("RANDOM")) ? "sql.questions.rand" : "sql.questions";
            String queryQues = DAOFactory.getDAOProperties().getProperty(queryForQues);

            ps = connection.prepareStatement(queryQues);
            ps.setString(1, surveyBlockId);
            rs = ps.executeQuery();
            List<Question> qs = new ArrayList<Question>();
            String quesOptionType = null;

            while (rs.next()) {
                String quesId = rs.getString("QuestionId");
                String quesText = rs.getString("QuestionText");
                quesOptionType = rs.getString("QuestionOptionType");
                String shortForm = rs.getString("ShortForm");
                Question ques = new Question(quesId, Question.Type.LIKERT5, quesText, "", Integer.parseInt(quesOptionType), shortForm);

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
    public ValueObject getJoinActivity(String parentActivityId) throws DAOException {
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String query = DAOFactory.getDAOProperties().getProperty("sql.ja");
            ps = connection.prepareStatement(query);
            ps.setString(1, parentActivityId);
            rs = ps.executeQuery();
            ArrayList<String> childActivities = new ArrayList<String>();
            String canonicalOrder = "";
            while (rs.next()) {
                String childActivityId = rs.getString("ChildActivityId");
                System.out.println("The child activity::" + childActivityId);
                canonicalOrder = rs.getString("canonicalOrder");
                childActivities.add(childActivityId);
                //System.out.println(Arrays.toString(childActivities));
                childActivities.forEach(System.out::println);

            }

            ModelFactory.ContainerActivityMapping actvtToOrderingMapping = new ModelFactory().new ContainerActivityMapping();
            actvtToOrderingMapping._childActivities = childActivities;
            //We have to do the seq mapping,only if the canonical ordering is not not empty.
            if (!canonicalOrder.isEmpty()) {
                for (ContainerActivity.Sequencing seq : ContainerActivity.Sequencing.values()) {
                    if (seq.name().equals(canonicalOrder)) {
                        actvtToOrderingMapping._sequence = seq;
                        break;
                    }
                }
            }
            vo.putAttribute("containerActivityReference", actvtToOrderingMapping);
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
    public ValueObject getpatient(String patientPIN) throws DAOException {
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String query = DAOFactory.getDAOProperties().getProperty("sql.pa");
            ps = connection.prepareStatement(query);
            System.out.println("The patient pin is::" + patientPIN);
            ps.setString(1, patientPIN);
            rs = ps.executeQuery();
            Patient patient = null;
            if (rs.next()) {
                String stageID = rs.getString("StageIdFK");
                String childPIN = rs.getString("ParentPinFK");
                String type = rs.getString("type");
                Boolean enhancedContent = rs.getBoolean("EnhancedContent");
                patient = new Patient(patientPIN, stageID, childPIN, type, enhancedContent);
            }

            vo.putAttribute("Patient", patient);
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
    public ValueObject checkActivity(String patientPIN) throws DAOException {
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String query = DAOFactory.getDAOProperties().getProperty("sql.chkActvyIns");
            ps = connection.prepareStatement(query);
            ps.setString(1, patientPIN);
            rs = ps.executeQuery();
            ArrayList<ActivityInstance> activityInstances = new ArrayList<ActivityInstance>();
            while (rs.next()) {
                Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("StartTime"));
                Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("EndTime"));
                Date userSubmissionTime = null;
                if (!(rs.getString("UserSubmissionTime") == null || rs.getString("UserSubmissionTime").isEmpty()))
                    userSubmissionTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("UserSubmissionTime"));
                Date actualSubmissionTime = null;
                if (!(rs.getString("ActualSubmissionTime") == null || rs.getString("UserSubmissionTime").isEmpty()))
                    actualSubmissionTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(rs.getString("ActualSubmissionTime"));
                String state = rs.getString("State");
                String sequence = rs.getString("Sequence");
                String activityInstanceId = rs.getString("ActivityInstanceId");
                String activityTitle = rs.getString("activityTitle");
                String description = rs.getString("description");
                String patientPin = rs.getString("PatientPinFK");


                ActivityInstance activityIns = new ActivityInstance(activityInstanceId, startTime, endTime, userSubmissionTime, actualSubmissionTime, state, GsonFactory.getInstance().getGson().fromJson(sequence, Sequence.class), activityTitle, description, patientPin);
                activityInstances.add(activityIns);

            }
            vo.putAttribute("ActivityInstances", activityInstances);
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
    public ValueObject postActivityInstances(ArrayList<PostActivity> questionResults) throws SQLException, DAOException {
        ValueObject vo = new ValueObject(); // need to fill this up
        int updateCount = -1;
        if (questionResults != null) {
            //questionResults = subSurvey.questionResult;
            Connection connection = getConnection();
            try {
                //This boolean is set to false for performing the SQL transactions semantics.
                connection.setAutoCommit(false);
                String query = DAOFactory.getDAOProperties().getProperty("sql.submitSurvy");
                Timestamp userSubmissionTime = null;
                int activityInstanceId = -1;
                PreparedStatement ps = null;
                ps = connection.prepareStatement(query);
                for (PostActivity activity : questionResults) {
                    if (activity.getActivityId().equals("PI_DAILY") || activity.getActivityId().equals("PI_WEEKLY")) {
                        PostPainIntensity painIntensity = (PostPainIntensity) activity;
                        userSubmissionTime = painIntensity.getUserSubmittedTimeStamp();
                        activityInstanceId = painIntensity.getActivityInstanceId();
                        try {
                            //This insert is for the bodypain intensity.
                            if (painIntensity.getBodyPainLocation() != null) {
                                //This insert is for the bodypain location.
                                ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                                ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                                ps.setInt(3, painIntensity.getQuestionIds().get("bodyPain"));
                                ps.setInt(4, painIntensity.getBodyPainLocation());
                                //ps.setInt(4, painIntensity.getLocation());
                                ps.setInt(5, painIntensity.getActivityInstanceId());
                                ps.setString(6, "");
                                ps.setString(7, "");
                                ps.addBatch();
                            }

                            //This insert is for the bodypain intensity.
                            if (painIntensity.getBodyPainIntensity() != null) {

                                ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                                ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                                ps.setInt(3, painIntensity.getQuestionIds().get("bodyPain"));
                                ps.setInt(4, painIntensity.getBodyPainIntensity());
                                ps.setInt(5, painIntensity.getActivityInstanceId());
                                ps.setString(7, "");
                                ps.addBatch();
                            }

                            //This insert is for generalized pain intensity.
                            if (painIntensity.get_generalizedPainInensity() != null) {
                                for (int genralizedPainOption : painIntensity.get_generalizedPainInensity()) {
                                    System.out.println("The timeStamp is::" + new Timestamp(new Date().getTime()));
                                    ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                                    ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                                    ps.setInt(3, painIntensity.getQuestionIds().get("generalizedPain"));
                                    ps.setInt(4, genralizedPainOption);
                                    ps.setInt(5, painIntensity.getActivityInstanceId());
                                    ps.setString(7, "");
                                    ps.addBatch();
                                }
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                            if (ps != null)
                                ps.close();
                            connection.rollback();
                            throw e;
                        }
                    } else if (activity.getActivityId().equals("CAT") || activity.getActivityId().equals("MA") || activity.getActivityId().equals("PR_Anxiety") || activity.getActivityId().equals("PR_Fatigue") || activity.getActivityId().equals("PR_PainInt") || activity.getActivityId().equals("PR_PhysFuncMob")) {
                        PostPromisSurvey promisSurvey = (PostPromisSurvey) activity;
                        userSubmissionTime = promisSurvey.getUserSubmittedTimeStamp();
                        activityInstanceId = promisSurvey.getActivityInstanceId();
                        HashMap<Integer, ArrayList<PostPromisSurvey.OptionToValue>> questionToOption = promisSurvey.getQuestionToOptions();
                        for (int questionId : questionToOption.keySet()) {
                            ArrayList<PostPromisSurvey.OptionToValue> listOfOptions = questionToOption.get(questionId);
                            for (OptionToValue eachValue : listOfOptions) {
                                try {
                                    ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                                    ps.setTimestamp(2, new Timestamp(new Date().getTime()));
                                    ps.setInt(3, questionId);
                                    ps.setInt(4, eachValue.getOptionId());
                                    ps.setInt(5, promisSurvey.getActivityInstanceId());
                                    ps.setString(6, eachValue.getValue());
                                    ps.setString(7, eachValue.getDosage());
                                    ps.addBatch();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    if (ps != null)
                                        ps.close();
                                    connection.rollback();
                                    throw e;
                                }
                            }

                        }
                    } else if (activity.getActivityId().equals("FINGERTAPPING")) {
                        query = DAOFactory.getDAOProperties().getProperty("sql.fingerTappingSubmit");
                        ps = connection.prepareStatement(query);
                        PostFingerTapping postFingerTapping = (PostFingerTapping) activity;
                        userSubmissionTime = postFingerTapping.get_userSubmittedTimeStamp();
                        activityInstanceId = postFingerTapping.getActivityInstanceId();
                        int patientPin = postFingerTapping.getPatientPin();
                        int timeToTap = postFingerTapping.getTimeToTap();
                        int timeToComplete = postFingerTapping.getTimeToComplete();
                        float screenWidth = postFingerTapping.getScreenWidth();
                        float screenHeight = postFingerTapping.getScreenHeight();
                        HashMap<String, Integer> fingertappingResult = postFingerTapping.getResults();
                        JSONObject handObject = new JSONObject();
                        for (Map.Entry<String, Integer> item : fingertappingResult.entrySet()) {
                            String hand = item.getKey();
                            int value = item.getValue();
                            handObject.put(hand, value);
                        }
                        String fingerTappingResult = handObject.toJSONString();
                        System.out.println(TAG + " postActivityInstances() :- " + handObject.toJSONString());

                        try {
                            ps.setInt(1, patientPin);
                            ps.setInt(2, activityInstanceId);
                            ps.setInt(3, timeToTap);
                            ps.setFloat(4, screenHeight);
                            ps.setFloat(5, screenWidth);
                            ps.setTimestamp(6, userSubmissionTime);
                            ps.setString(7, fingerTappingResult);
                            ps.setInt(8, timeToComplete);
                            ps.addBatch();
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (ps != null)
                                ps.close();
                            connection.rollback();
                            throw e;
                        }
//                    	modifyActivityInstance(activityInstanceId,patientPin,activity.getParentactivity());

                    } else if (activity.getActivityId().equals("FLANKER")) {
                        query = DAOFactory.getDAOProperties().getProperty("sql.flankerSubmit");
                        ps = connection.prepareStatement(query);
                        PostFlanker postFlanker = (PostFlanker) activity;
                        userSubmissionTime = postFlanker.get_userSubmittedTimeStamp();
                        activityInstanceId = postFlanker.getActivityInstanceId();
                        int patientPin = postFlanker.getPatientPin();
                        int timeToComplete = postFlanker.getTotalTimeTaken();
                        int screenWidth = (int) postFlanker.getScreenWidth();
                        int screenHeight = (int) postFlanker.getScreenHeight();
                        ArrayList<String> results = postFlanker.getResults();
                        String resultToSubmit = results.toString();

                        try {
                            ps.setInt(1, patientPin);
                            ps.setInt(2, activityInstanceId);
                            ps.setInt(3, timeToComplete);
                            ps.setFloat(4, screenHeight);
                            ps.setFloat(5, screenWidth);
                            ps.setTimestamp(6, userSubmissionTime);
                            ps.setString(7, resultToSubmit);
                            ps.addBatch();
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (ps != null)
                                ps.close();
                            connection.rollback();
                            throw e;
                        }
//                    	modifyActivityInstance(activityInstanceId,patientPin,activity.getParentactivity());


                    } else if (activity.getActivityId().equals("PATTERNCOMPARISON")) {
                        query = DAOFactory.getDAOProperties().getProperty("sql.patternComparisonSubmit");
                        ps = connection.prepareStatement(query);
                        PostPatternComparison postPatternComparison = (PostPatternComparison) activity;
                        userSubmissionTime = postPatternComparison.get_userSubmittedTimeStamp();
                        activityInstanceId = postPatternComparison.getActivityInstanceId();
                        int patientPin = postPatternComparison.getPatientPin();
                        int timeToComplete = postPatternComparison.getTotalTimeTaken();
                        int screenWidth = (int) postPatternComparison.getScreenWidth();
                        int screenHeight = (int) postPatternComparison.getScreenHeight();
                        ArrayList<String> results = postPatternComparison.getResults();
                        String resultToSubmit = results.toString();

                        try {
                            ps.setInt(1, patientPin);
                            ps.setInt(2, activityInstanceId);
                            ps.setInt(3, timeToComplete);
                            ps.setFloat(4, screenHeight);
                            ps.setFloat(5, screenWidth);
                            ps.setTimestamp(6, userSubmissionTime);
                            ps.setString(7, resultToSubmit);
                            ps.addBatch();
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (ps != null)
                                ps.close();
                            connection.rollback();
                            throw e;
                        }
//                    	modifyActivityInstance(activityInstanceId,patientPin,activity.getParentactivity());

                    } else if (activity.getActivityId().equals("SPATIALSPAN")) {
                        query = DAOFactory.getDAOProperties().getProperty("sql.spatialspansubmit");
                        ps = connection.prepareStatement(query);
                        PostSpatialSpan postSpatialSpan = (PostSpatialSpan) activity;
                        userSubmissionTime = postSpatialSpan.get_userSubmittedTimeStamp();
                        activityInstanceId = postSpatialSpan.getActivityInstanceId();
                        int patientPin = postSpatialSpan.getPatientPin();
                        int timeToComplete = postSpatialSpan.getTotalTimeTaken();
                        int screenWidth = (int) postSpatialSpan.getScreenWidth();
                        int screenHeight = (int) postSpatialSpan.getScreenHeight();
                        ArrayList<String> results = postSpatialSpan.getResults();
                        String resultToSubmit = results.toString();

                        try {
                            ps.setInt(1, patientPin);
                            ps.setInt(2, activityInstanceId);
                            ps.setInt(3, timeToComplete);
                            ps.setFloat(4, screenHeight);
                            ps.setFloat(5, screenWidth);
                            ps.setTimestamp(6, userSubmissionTime);
                            ps.setString(7, resultToSubmit);
                            ps.addBatch();
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (ps != null)
                                ps.close();
                            connection.rollback();
                            throw e;
                        }
//                    	modifyActivityInstance(activityInstanceId,patientPin,activity.getParentactivity());

                    }
                }

                int[] executeResult = ps.executeBatch();
                ;

                ps.close();
//                If the Question Result Table is not inserted then we have to abort the transaction. 

                if (executeResult.length <= 0 || (executeResult.length > 0 && executeResult[0] < 0)) {
                    connection.rollback();
                    log.info("The batch processing failed::Rollingback Transactions");
                    vo.putAttribute("result", false);
                    return vo;
                }
                ps = null;
                Date date = new Date();
                Timestamp currentTime = new Timestamp(date.getTime());

                try {
                    query = DAOFactory.getDAOProperties().getProperty("sql.subSurvyActvyIns");
                    ps = connection.prepareStatement(query);
                    ps.setTimestamp(1, userSubmissionTime);
                    ps.setTimestamp(2, currentTime);
//                    if(checkIfActivitySequenceIsFinished(activityInstanceId)){
                    ps.setString(3, "completed");
//                    }else{
//                    	ps.setString(3,"in progress");
//                    }

                    ps.setInt(4, activityInstanceId);
                    updateCount = ps.executeUpdate();
                    ps.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (ps != null)
                        ps.close();
                    connection.rollback();
                    throw e;
                }
                //Only if the insert into question_result and the update in the survey_instance succeed we have to commit the transaction.
                if (updateCount > 0)
                    connection.commit();
                else
                    connection.rollback();
            } catch (Exception e) {
                connection.rollback();
                e.printStackTrace();
                throw e;
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        vo.putAttribute("result", (updateCount > 0) ? true : false);
        return vo;
    }

    private boolean checkIfActivitySequenceIsFinished(int activityInstanceId) throws DAOException {
        // TODO Auto-generated method stub
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String query = DAOFactory.getDAOProperties().getProperty("sql.getActivityInstance");
            ps = connection.prepareStatement(query);
            ps.setInt(1, activityInstanceId);
            rs = ps.executeQuery();
            boolean isCompleted;
            if (rs.next()) {
                JSONObject obj = (JSONObject) new JSONParser().parse(rs.getString("Sequence"));
                JSONArray seq = (JSONArray) obj.get("sequence");
                if (!seq.contains("FLANKER") && !seq.contains("PATTERNCOMPARISON") && !seq.contains("FINGERTAPPING") && !seq.contains("SPATIALSPAN")) {
                    return true;

                }

            }
        } catch (Throwable t) {
            t.printStackTrace();

            throw new DAOException("Unable to Modify Activity Instance");
        }
        return false;

    }

    private void modifyActivityInstance(int activityInstanceId, int patientPin, String activityPassed) throws DAOException {
        // TODO Auto-generated method stub
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String query = DAOFactory.getDAOProperties().getProperty("sql.getActivityInstance");
            ps = connection.prepareStatement(query);
            ps.setInt(1, activityInstanceId);
            rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println(TAG + " modifyActivityInstance() :- " + rs.getString("Sequence"));
                JSONObject obj = (JSONObject) new JSONParser().parse(rs.getString("Sequence"));
                JSONArray seq = (JSONArray) obj.get("sequence");

                ArrayList<String> tempList = new ArrayList<>();
                ArrayList<String> tempListCompleted = new ArrayList<>();
                if (!(seq.get(0).toString().equalsIgnoreCase(obj.get("parentactivity").toString()))) {
                    JSONArray completed;
                    if (obj.containsKey("completed")) {
                        completed = (JSONArray) obj.get("completed");
                        for (int i = 0; i < completed.size(); i++) {
                            tempListCompleted.add(completed.get(i).toString());
                        }
                    } else {
                        completed = new JSONArray();
                    }

                    for (int i = 0; i < seq.size(); i++) {
                        String temp = seq.get(i).toString();
                        tempList.add(temp);
                    }
                    tempList.remove(activityPassed);
                    tempListCompleted.add(activityPassed);

                    JSONArray tempSeq = new JSONArray();
                    JSONArray tempCompleted = new JSONArray();
                    for (String seqItem : tempList) {
                        tempSeq.add(seqItem);
                    }
                    for (String item : tempListCompleted) {
                        tempCompleted.add(item);
                    }
                    seq = tempSeq;
                    completed = tempCompleted;
                    obj.put("sequence", seq);
                    obj.put("completed", completed);
                    String modifyQuery = DAOFactory.getDAOProperties().getProperty("sql.updateActivityInstance");
                    ps = connection.prepareStatement(modifyQuery);
                    String seqNew = obj.toString();
                    System.out.println(TAG + " modifyActivityInstance() :- " + seqNew);
                    ps.setString(1, seqNew);
                    ps.setInt(2, activityInstanceId);
                    System.out.println(TAG + " modifyActivityInstance() :- " + ps.toString());
                    ps.addBatch();
                    int[] result = ps.executeBatch();

                }

            }
        } catch (Throwable t) {
            t.printStackTrace();

            throw new DAOException("Unable to Modify Activity Instance");
        }
    }

    @Override
    public ValueObject getQuestionOptionByText(String questionOptionText) throws DAOException {
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String query = DAOFactory.getDAOProperties().getProperty("sql.quesOpt");
            ps = connection.prepareStatement(query);
            ps.setString(1, questionOptionText);
            rs = ps.executeQuery();
            QuestionOption questionOpt = null;
            if (rs.next()) {
                int questionOptionId = rs.getInt("QuestionOptionId");
                int questionOptionOrder = rs.getInt("OptionOrder");
                int questionOptionType = rs.getInt("QuestionOptionType");

                questionOpt = new QuestionOption(questionOptionId, questionOptionText, questionOptionOrder, questionOptionType);
            }
            vo.putAttribute("questionOption", questionOpt);
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
    public ValueObject createActivityInstance(String sequence, String patientPIN, String startTime, String endTime, Trial trialType, String activityID) throws DAOException {
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
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
            String typeOfActivity = (String) actvtMetaData.get("typeoFActivity");
            System.out.println(TAG + " createActivityInstance() :- " + sequence);


            if (typeOfActivity.equalsIgnoreCase("Weekly")) {
                title = DAOFactory.getDAOProperties().getProperty("epilepsy.weekly.title");
                description = DAOFactory.getDAOProperties().getProperty("epilepsy.weekly.description");
            } else if (typeOfActivity.equalsIgnoreCase("Daily")) {
                //Setting the title and description.
                title = DAOFactory.getDAOProperties().getProperty("epilepsy.daily.title");
                description = DAOFactory.getDAOProperties().getProperty("epilepsy.daily.description");
            }

            System.out.println(TAG + " createActivityInstance() :- startTimestamp" + startTimestamp);
            System.out.println(TAG + " createActivityInstance() :- endTimestamp" + endTimestamp);
            System.out.println(TAG + " createActivityInstance() :- patientPIN" + patientPIN);
            System.out.println(TAG + " createActivityInstance() :- sequence" + sequence);
            System.out.println(TAG + " createActivityInstance() :- title" + title);
            System.out.println(TAG + " createActivityInstance() :- description" + description);

            ps.setTimestamp(1, startTimestamp);
            ps.setTimestamp(2, endTimestamp);
            ps.setString(3, "pending");
            ps.setString(4, patientPIN);
            ps.setString(5, sequence);
            ps.setTimestamp(6, new Timestamp(new Date().getTime()));
            ps.setString(7, title);
            ps.setString(8, description);

            //Gonna execute the query.
            int rowCount = ps.executeUpdate();
            boolean result = (rowCount >= 1) ? true : false;

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
        try {
            String hydroxyUreaPrescribed = DAOFactory.getDAOProperties().getProperty("sql.ishydroxyprescribed");
            ps = connection.prepareStatement(hydroxyUreaPrescribed);
            ps.setString(1, patientPIN);
            rs = ps.executeQuery();
            boolean ishydroxyUreaPrescribed = false;
            String patientType = "";
            if (rs.next()) {
                String hydroxyUreaTablet = rs.getString("HydroxyureaPrescribed");
                if (hydroxyUreaTablet.equals("0"))
                    ishydroxyUreaPrescribed = false;
                else
                    ishydroxyUreaPrescribed = true;

                patientType = rs.getString("type");
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
            if (rs.next()) {
                surveyBlockId = rs.getString("SurveyBlockId");


            }
            rs.close();
            ps.close();

            String maqueryToselect = (ishydroxyUreaPrescribed == true) ? "sql.ma" : "sql.mawithouthydroxy";
            String query = DAOFactory.getDAOProperties().getProperty(maqueryToselect);
            ps = connection.prepareStatement(query);
            ps.setString(1, surveyBlockId);

            if (!ishydroxyUreaPrescribed) {
                String hydroxyUrea = surveyBlockId + "_HYDROXYUREAQUESTION";
                String hydroxyUreaquestionID = DAOFactory.getDAOProperties().getProperty(hydroxyUrea);
                System.out.println("hydroxyUreaQuestionID::" + hydroxyUreaquestionID);
                ps.setInt(2, Integer.parseInt(hydroxyUreaquestionID));
                String withExceptClause = surveyBlockId + "_WITHEXCEPTCLAUSE";
                String withExceptClauseQuestionID = DAOFactory.getDAOProperties().getProperty(withExceptClause);
                ps.setInt(3, Integer.parseInt(withExceptClauseQuestionID));
                System.out.println("withExceptClause::" + withExceptClauseQuestionID);
            } else {
                String withoutExcept = surveyBlockId + "_WITHOUTEXCEPTCLAUSE";
                String withoutExceptClause = DAOFactory.getDAOProperties().getProperty(withoutExcept);
                System.out.println("WithoutExceptClause::" + withoutExceptClause);
                ps.setInt(2, Integer.parseInt(withoutExceptClause));
            }

            rs = ps.executeQuery();
            ArrayList<Question> questions = new ArrayList<Question>();
            while (rs.next()) {
                String questionId = rs.getString("QuestionId");
                String questionText = rs.getString("QuestionText");
                int questionOption = rs.getInt("QuestionOptionType");
                String questionType = rs.getString("QuestionType");
                Type questionTyp = Question.Type.valueOf(questionType);

                Question question = new Question(questionId, questionTyp, questionText, "", questionOption, "");
                questions.add(question);
            }
            vo.putAttribute("Questions", questions);
            rs.close();
            ps.close();
            Patient patient = new ModelFactory().getPatient(patientPIN);
            String childPIN = "";
            if (patient.getType().equals(Patient.PatientType.parent_proxy.toString()))
                childPIN = patient.getChildPIN();
            String medicationInfo = DAOFactory.getDAOProperties().getProperty("sql.medicationInfo");
            ps = connection.prepareStatement(medicationInfo);
            if (patient.getType().equals(Patient.PatientType.parent_proxy.toString()))
                ps.setString(1, childPIN); //If it is the parent , we get the child medication info.
            else
                ps.setString(1, patientPIN);
            rs = ps.executeQuery();
            ArrayList<MedicationInfo> medicationInfoList = new ArrayList<MedicationInfo>();
            while (rs.next()) {
                String medicationName = rs.getString("MedicationName");
                int defaultDosage = rs.getInt("defaultDosage");
                int prescribedDosage = rs.getInt("prescribedDosage");
                int noOfTablets = rs.getInt("noOfTablets");
                String units = rs.getString("units");
                int questionOptionId = rs.getInt("QuestionOptionId");
                MedicalAdherence med = new MedicalAdherence("MA");
                MedicationInfo medInfo = med.new MedicationInfo(medicationName, defaultDosage, noOfTablets, prescribedDosage, units, questionOptionId);

                medicationInfoList.add(medInfo);
            }
            vo.putAttribute("medicationInfo", medicationInfoList);

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
        try {
            //Need to get the surveyBlockType from patients table
            String patientQuery = DAOFactory.getDAOProperties().getProperty("sql.patientType");
            ps = connection.prepareStatement(patientQuery);
            ps.setString(1, patientPIN);
            rs = ps.executeQuery();
            String patientType = "";
            if (rs.next()) {
                patientType = rs.getString("type");

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
            if (rs.next()) {
                surveyBlockId = rs.getString("SurveyBlockId");
            }
            rs.close();
            ps.close();

            String query = DAOFactory.getDAOProperties().getProperty("sql.adaptive");
            ps = connection.prepareStatement(query);
            //ps.setString(1, "likert5");
            ps.setString(1, surveyBlockId);
            rs = ps.executeQuery();
            ArrayList<Question> questions = new ArrayList<Question>();
            while (rs.next()) {
                String questionId = rs.getString("QuestionId");
                String questionText = rs.getString("QuestionText");
                int questionOption = rs.getInt("QuestionOptionType");
                String questionType = rs.getString("QuestionType");
                Question.Type questionTypeEnum = null;
                for (Question.Type type : Question.Type.values()) {
                    if (type.name().equalsIgnoreCase(questionType)) {
                        questionTypeEnum = type;
                    }
                }
                Question question = new Question(questionId, questionTypeEnum, questionText, "", questionOption, "");
                questions.add(question);
            }
            vo.putAttribute("Questions", questions);

            rs.close();
            ps.close();

            String catMappingQuery = DAOFactory.getDAOProperties().getProperty("sql.catmapping");
            ps = connection.prepareStatement(catMappingQuery);
            rs = ps.executeQuery();
            HashMap<Integer, ArrayList<Integer>> catMapping = new HashMap<Integer, ArrayList<Integer>>();
            while (rs.next()) {
                int questionId = rs.getInt("QuestionIdFK");
                int nestedQuestionId = rs.getInt("NestedQuestionIdFK");
                int nextQuestionId = rs.getInt("NextQuestionIdFK");

                ArrayList<Integer> catQuestionIdMapping = new ArrayList<Integer>();
                catQuestionIdMapping.add(nestedQuestionId);
                catQuestionIdMapping.add(nextQuestionId);
                catMapping.put(questionId, catQuestionIdMapping);
            }
            vo.putAttribute("catquestionmapping", catMapping);

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
    public ValueObject changeActivityInstanceState(int activityInstance, String state) throws DAOException {
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        int updateCount = -1;
        try {
            String query = DAOFactory.getDAOProperties().getProperty("sql.updateActvyIns");
            ps = connection.prepareStatement(query);
            ps.setString(1, state);
            ps.setInt(2, activityInstance);

            updateCount = ps.executeUpdate();
        } catch (Exception e) {
            throw new DAOException("Unable to process results from query sql.updateActvyIns");
        } finally {
            try {
                if (ps != null) ps.close();
                if (connection != null) connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
                // YYY need a logging facility, but this does not have to be rethrown
                log.error(se);
            }
        }
        vo.putAttribute("result", (updateCount > 0) ? true : false);
        return vo;
    }

    @Override
    public ValueObject postUILogger(ArrayList<UILogger> loggerResults) throws SQLException, DAOException {

        ValueObject vo = new ValueObject(); // need to fill this up
        int insertCount = -1;

        if (loggerResults != null) {
            Connection connection = getConnection();
            try {
                //This boolean is set to false for performing the SQL transactions semantics.
                connection.setAutoCommit(false);
                String query = DAOFactory.getDAOProperties().getProperty("sql.submitUILogger");

                PreparedStatement ps = null;
                ps = connection.prepareStatement(query);
                try {
                    for (UILogger logger : loggerResults) {
                        ps.setTimestamp(1, new Timestamp(new Date().getTime()));
                        ps.setString(2, logger.getPatientPin());
                        ps.setString(3, logger.getEventName());
                        ps.setString(4, logger.getMetaData().toString());
                        ps.setTimestamp(5, logger.getEventTime());
                        ps.addBatch();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (ps != null)
                        ps.close();
                    connection.rollback();
                    throw e;
                }

                int[] executeResult = ps.executeBatch();
                insertCount = executeResult[0];
                ps.close();

                //If the  UI_Logger Table is not inserted then we have to abort the transaction.

                if (executeResult.length <= 0 || (executeResult.length > 0 && executeResult[0] < 0)) {
                    connection.rollback();
                    log.info("The batch processing of ui_logger failed::Rollingback Transactions");
                    vo.putAttribute("result", false);
                    return vo;
                }
                if (insertCount > 0) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (Exception e) {
                connection.rollback();
                e.printStackTrace();
                throw e;
            } finally {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    throw e;
                }
            }
        }

        vo.putAttribute("result", (insertCount > 0) ? true : false);
        return vo;

    }

    /**
     * This function returns the metadata for the specified actvity.
     *
     * @param activityID - The activity for which we the metadata.
     * @return value object - It contains the metadata.
     * @throws DAOException
     */
    @Override
    public ValueObject getActivityMetaData(String activityID) throws DAOException {
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String jsonMetaData = null;
        try {
            String query = DAOFactory.getDAOProperties().getProperty("sql.actvtymetadata");
            ps = connection.prepareStatement(query);
            ps.setString(1, activityID);

            rs = ps.executeQuery();

            if (rs.next()) {
                jsonMetaData = rs.getString("metadata");
            }
        } catch (Exception e) {
            throw new DAOException("Unable to process results from query sql.updateActvyIns");
        } finally {
            try {
                if (ps != null) ps.close();
                if (connection != null) connection.close();
            } catch (SQLException se) {
                se.printStackTrace();
                // YYY need a logging facility, but this does not have to be rethrown
                log.error(se);
            }
        }
        vo.putAttribute("metadata", jsonMetaData);
        return vo;

    }

    //This is the method is taken by
    @Override
    public ValueObject enrollPatients(ArrayList<Patient.PatientEnroll> patientsInfos) throws SQLException, DAOException {
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        try {
            connection.setAutoCommit(false);
            String query = "";
            int maxPatientPin = -1;
            for (Patient.PatientEnroll patientInfo : patientsInfos) {
                query = DAOFactory.getDAOProperties().getProperty("sql.getMaxPatientPIN");
                ps = connection.prepareStatement(query);
                ps.setString(1, patientInfo.getPatientType());
                rs = ps.executeQuery();
                QuestionOption questionOpt = null;
                if (rs.next()) {
                    maxPatientPin = rs.getInt("MaxPatientPIN");
                }
                rs.close();
                ps.close();

                if(IsPinExist(connection,++maxPatientPin)){
                    maxPatientPin = getMaxPatientPinFromWholeTable(connection);
                }


                if (maxPatientPin < 0) {
                    //connection.rollback();
                    log.error("Unable to get the last patient pin");
                    vo.putAttribute("result", "-1");
                    return vo;
                }

                int temp_pin = maxPatientPin + 1;
                Boolean enhancedContent = false;

                System.out.println("New pin is : " + temp_pin);

                if ((temp_pin % 2) == 1) {
                    System.out.println("The pin is ODD");
                    System.out.println("EnhancedContent is set to TRUE");
                    enhancedContent = true;
                }

                startDate = LocalDateTime.now();  //	DateStarted is current Date and Time
                endDate = startDate.plusDays(36);  //	DateCompleted is 36 days from DateStarted

                query = DAOFactory.getDAOProperties().getProperty("sql.createPatient");
                System.out.println("The values are::" + maxPatientPin + "::"
                        + patientInfo.getDeviceType() + "::" + patientInfo.getDeviceVersion() + "::" + patientInfo.getChildPIN() +
                        patientInfo.getPatientType() + patientInfo.isHydroxyUreaPrescribed());
                ps = connection.prepareStatement(query);
                ps.setString(1, Integer.toString(++maxPatientPin));
                ps.setString(2, patientInfo.getDeviceType());
                ps.setString(3, patientInfo.getDeviceVersion());
                ps.setTimestamp(4, new Timestamp(startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
                ps.setTimestamp(5, new Timestamp(endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
                ps.setString(6, "1");
                ps.setString(7, patientInfo.getChildPIN());
                ps.setString(8, patientInfo.getPatientType());
                //System.out.println("The boolean value::"+Boolean.(patientInfo.isHydroxyUreaPrescribed()));
                ps.setString(9, patientInfo.isHydroxyUreaPrescribed());
                ps.setString(10, patientInfo.isChildOnMedication());
                ps.setBoolean(11, enhancedContent);

                int insertCount = ps.executeUpdate();
                ps.close();

                if (insertCount <= 0) {
                    connection.rollback();
                    log.error("Unexpected error while performing insert for patient");
                    vo.putAttribute("result", "-1");
                    return vo;
                }

                if (!patientInfo.getPatientType().equals(Patient.PatientType.parent_proxy.toString())) {
                    query = DAOFactory.getDAOProperties().getProperty("sql.insertMedicationInfo");
                    ps = connection.prepareStatement(query);
                    for (Patient.medicationInfo medicationInfo : patientInfo.getPatientMedication()) {
                        ps.setString(1, Integer.toString(maxPatientPin));
                        ps.setString(2, medicationInfo.getMediceName());
                        ps.setInt(3, 1);
                        ps.setInt(4, medicationInfo.getPrescribedDosage());
                        ps.setInt(5, medicationInfo.getNoOfTablets());
                        ps.setString(6, medicationInfo.getUnits());
                        ps.addBatch();
                    }

                    int[] executeResult = ps.executeBatch();
                    ps.close();

                    if (executeResult.length <= 0 || (executeResult.length > 0 && executeResult[0] < 0)) {
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

    private int getMaxPatientPinFromWholeTable(Connection connection) throws SQLException {
        int maxPin = -1;
        String getMaxPinQuery = DAOFactory.getDAOProperties().getProperty("sql.getMaxPatientPINWithoutType");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(getMaxPinQuery);
        while (resultSet.next()){
            maxPin = resultSet.getInt("MaxPatientPIN");
        }
        statement.close();
        resultSet.close();
        return maxPin;
    }

    private boolean IsPinExist(Connection connection, int pin) throws SQLException {
        boolean exist = false;
        String checkPinQuery = DAOFactory.getDAOProperties().getProperty("sql.checkMaxPatientPIN");
        PreparedStatement ps = connection.prepareStatement(checkPinQuery);
        ps.setString(1,String.valueOf(pin));
        ResultSet resultSet = ps.executeQuery();
        if (resultSet.next()){
            exist = true;
        }
        ps.close();
        resultSet.close();
        return exist;
    }

    /**
     * This method will return the trial duration
     *
     * @param trial name
     * @return valueObject
     * @throws SQLException
     * @throws DAOException
     */
    @Override
    public ValueObject getTrialDuration(String trialName) throws SQLException, DAOException {
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String startTime = "";
        String endTime = "";
        int duration = -1;
        try {
            String query = DAOFactory.getDAOProperties().getProperty("sql.trialDuration");
            ps = connection.prepareStatement(query);
            ps.setString(1, trialName);
            rs = ps.executeQuery();

            if (rs.next()) {
                startTime = rs.getString("IRBStart");
                endTime = rs.getString("IRBEnd");
                duration = rs.getInt("Duration");
            }
            if (!startTime.isEmpty() && !endTime.isEmpty()) {
                vo.putAttribute("startTime", startTime);
                vo.putAttribute("endTime", endTime);
                vo.putAttribute("Duration", duration);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DAOException("Unable to process results from query sql.trialDuration");
        } finally {
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
    public ValueObject getFingerTapping(String activityID, String patientPIN) throws SQLException, DAOException {
        // TODO Auto-generated method stub
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String patientType = null;
        try {
            //Need to get the surveyBlockType from patients table
            String patientQuery = DAOFactory.getDAOProperties().getProperty("sql.patientType");
            ps = connection.prepareStatement(patientQuery);
            ps.setString(1, patientPIN);
            rs = ps.executeQuery();
            if (rs.next()) {
                patientType = rs.getString("type");

            }
            System.out.println(TAG + " getFingerTapping :- " + "PatientType = " + patientType);
            vo.putAttribute("type", patientType);
            rs.close();
            ps.close();
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
    public ValueObject getSpatialSpan(String activityID, String patientPIN) throws SQLException, DAOException {
        // TODO Auto-generated method stub
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String patientType = null;
        try {
            //Need to get the surveyBlockType from patients table
            String patientQuery = DAOFactory.getDAOProperties().getProperty("sql.patientType");
            ps = connection.prepareStatement(patientQuery);
            ps.setString(1, patientPIN);
            rs = ps.executeQuery();
            if (rs.next()) {
                patientType = rs.getString("type");

            }
            System.out.println(TAG + " getSpatialSpan() :- " + "PatientType = " + patientType);
            vo.putAttribute("type", patientType);
            rs.close();
            ps.close();
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
    public ValueObject getFlanker(String activityID, String patientPIN) throws SQLException, DAOException {
        // TODO Auto-generated method stub
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String patientType = null;
        try {
            //Need to get the surveyBlockType from patients table
            String patientQuery = DAOFactory.getDAOProperties().getProperty("sql.patientType");
            ps = connection.prepareStatement(patientQuery);
            ps.setString(1, patientPIN);
            rs = ps.executeQuery();
            if (rs.next()) {
                patientType = rs.getString("type");

            }
            System.out.println(TAG + " getFlanker() :- " + "PatientType = " + patientType);
            vo.putAttribute("type", patientType);
            rs.close();
            ps.close();
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
    public ValueObject getPatternComparision(String activityID, String patientPIN)
            throws SQLException, DAOException {
        // TODO Auto-generated method stub
        ValueObject vo = new ValueObject(); // need to fill this up
        Connection connection = getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String patientType = null;
        try {
            //Need to get the surveyBlockType from patients table
            String patientQuery = DAOFactory.getDAOProperties().getProperty("sql.patientType");
            ps = connection.prepareStatement(patientQuery);
            ps.setString(1, patientPIN);
            rs = ps.executeQuery();
            if (rs.next()) {
                patientType = rs.getString("type");

            }
            System.out.println(TAG + " getPatternComparision() :- " + "PatientType = " + patientType);
            vo.putAttribute("type", patientType);
            rs.close();
            ps.close();
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


}
