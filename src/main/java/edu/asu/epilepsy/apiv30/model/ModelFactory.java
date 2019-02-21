package edu.asu.epilepsy.apiv30.model;

import edu.asu.epilepsy.apiv30.dao.DAO;
import edu.asu.epilepsy.apiv30.dao.DAOException;
import edu.asu.epilepsy.apiv30.dao.DAOFactory;
import edu.asu.epilepsy.apiv30.dao.ValueObject;
import edu.asu.epilepsy.apiv30.model.MedicalAdherence.MedicationInfo;
import edu.asu.epilepsy.apiv30.model.Patient.Trial;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * The ModelFactory knows how to create each type of model object. Client classes should
 * call this, not the constructors of the model objects directly.
 *
 * @author kevinagary
 */
public final class ModelFactory {

  private static final String TAG = ModelFactory.class.getSimpleName();
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

      if (vo == null) {
        System.out.println(TAG + " getActivityInstance :- " + "vo is null");
        return null;
      } else {
        System.out.println(TAG + " getActivityInstance :- " + "ACTIVITY INSTANCE - " + activityInstanceId);
        System.out.println();
        return new ActivityInstance(activityInstanceId,
          (Date) vo.getAttribute("StartTime"),
          (Date) vo.getAttribute("EndTime"),
          (Date) vo.getAttribute("UserSubmissionTime"),
          (Date) vo.getAttribute("ActualSubmissionTime"),
          (String) vo.getAttribute("State"),  // what is this again?
          (String) vo.getAttribute("Sequence"), // assuming this is JSON?
          (String) vo.getAttribute("activityTitle"),
          (String) vo.getAttribute("description"),
          (String) vo.getAttribute("patientPin"));
      }

    } catch (DAOException de) {
      de.printStackTrace();
      log.error(de);
      throw new ModelException("Unable to create Model Object", de);
    }
  }


  public Activity getActivity(String activityId, String patientPIN) throws ModelException {
    try {
      //ValueObject vo = __theDAO.getActivity(activityId);
      ValueObject vo = null;
      vo = __theDAO.getJoinActivity(activityId);
      ContainerActivityMapping containerActvtyRef = (ContainerActivityMapping) vo.getAttribute("containerActivityReference");
      ArrayList<String> childActivities = containerActvtyRef._childActivities;
      //The check to find if the activity is a child activity or parent activity.


      childActivities.forEach(System.out::println);
      if (childActivities.size() > 0) {
        ArrayList<Activity> activities = new ArrayList<Activity>();
        for (String childactivityId : childActivities) {
          Activity activity = getActivity(childactivityId, patientPIN);
          activities.add(activity);
        }
        if (activities.size() > 2) {
          System.out.println(TAG + " getActivity() :- " + activities.size());
          return new ContainerActivity(activityId, activities, containerActvtyRef._sequence);
        } else if (activities.size() == 2) {
          System.out.println(TAG + " getActivity() :- " + activities.size());
          return new ContainerActivity(activityId, activities.get(0), activities.get(1), containerActvtyRef._sequence);
        }
      } else {
        if (activityId.equals("PI_DAILY") || activityId.equals("PI_WEEKLY")) {
          vo = __theDAO.getPainIntensity(activityId);
          List<Question> questions = (List<Question>) vo.getAttribute("Question");
          HashMap<Integer, String> questionOptionsMap = new HashMap<Integer, String>();
          for (Question question : questions) {
            int questionOptionType = question.getQuestionOptionType();
            if (questionOptionType != -1) {
              String questionOptions = ReadAnsOptProp.getQuestionOption(Integer.toString(questionOptionType));
              questionOptionsMap.put((Integer.parseInt(question.getQuestionId())), questionOptions);
            }
          }

          return new PainIntensity(activityId, (List<Question>) vo.getAttribute("Question"), questionOptionsMap, (String) vo.getAttribute("QuestionId"));
        } else if (activityId.equals("MA")) {
          vo = __theDAO.getMedicalAdherence(patientPIN);
          ArrayList<MedicationInfo> medicationInfo = (ArrayList<MedicationInfo>) vo.getAttribute("medicationInfo");
          ArrayList<Question> questions = null;
          HashMap<Integer, String> questionOptionsMap = null;
          if (vo.getAttribute("Questions") != null && vo.getAttribute("medicationInfo") != null) {
            questionOptionsMap = new HashMap<Integer, String>();
            questions = (ArrayList<Question>) vo.getAttribute("Questions");
            int questionOptionType = -1;
            for (Question question : questions) {
              questionOptionType = question.getQuestionOptionType();
              System.out.println("questionOptionType::" + questionOptionType);
              if (questionOptionType != -1) {
                if (!questionOptionsMap.containsKey(questionOptionType)) {
                  String questionOptions = ReadAnsOptProp.getQuestionOption(Integer.toString(questionOptionType));
                  questionOptionsMap.put(questionOptionType, questionOptions);
                }
              }
            }
          }
          System.out.println("map::" + questionOptionsMap);
          System.out.println("ArrayList::" + medicationInfo);
          QuestionOption otherOption = getOptionByText("Other");
          return new MedicalAdherence(activityId, (List<Question>) vo.getAttribute("Questions"), questionOptionsMap, medicationInfo, otherOption);
        } else if (activityId.equals("CAT")) {

          vo = __theDAO.getAdaptive(patientPIN);
          //Looping through the questions.
          ArrayList<Question> questions = null;
          HashMap<Integer, String> questionOptionsMap = null;
          if (vo.getAttribute("Questions") != null && vo.getAttribute("catquestionmapping") != null) {
            questionOptionsMap = new HashMap<Integer, String>();
            questions = (ArrayList<Question>) vo.getAttribute("Questions");
            int questionOptionType = -1;
            for (Question question : questions) {
              questionOptionType = question.getQuestionOptionType();
              if (!questionOptionsMap.containsKey(questionOptionType)) {
                String questionOptions = ReadAnsOptProp.getQuestionOption(Integer.toString(questionOptionType));
                questionOptionsMap.put(questionOptionType, questionOptions);
              }
            }
            return new ComputerAdaptiveTesting(activityId, (List<Question>) vo.getAttribute("Questions"), questionOptionsMap, (HashMap<Integer, ArrayList<Integer>>) vo.getAttribute("catquestionmapping"));
          } else {
            throw new ModelException("Unable to create adaptive question obj");
          }
        } else if (activityId.equalsIgnoreCase("PR_Anxiety") || activityId.equals("PR_Fatigue") || activityId.equals("PR_PainInt") || activityId.equals("PR_PhysFuncMob") || activityId.equals("PR_COGNITIVE") || activityId.equals("PR_DEPRESSIVE") || activityId.equals("PR_PEER_RELATIONSHIP")) {

          vo = __theDAO.getPromisSurvey(activityId, patientPIN); //We are harcoding it as parent proxy for now,not sure whether we need to get it from the app or we should have our own logic from DB.
          //Looping through the questions.
          ArrayList<Question> questions = null;
          HashMap<Integer, String> questionOptionsMap = null;
          if (vo.getAttribute("Questions") != null) {
            questionOptionsMap = new HashMap<Integer, String>();
            questions = (ArrayList<Question>) vo.getAttribute("Questions");
            int questionOptionType = -1;
            for (Question question : questions) {
              questionOptionType = question.getQuestionOptionType();
              if (!questionOptionsMap.containsKey(questionOptionType)) {
                String questionOptions = ReadAnsOptProp.getQuestionOption(Integer.toString(questionOptionType));
                questionOptionsMap.put(questionOptionType, questionOptions);
              }
            }
          }
          //String questionOptions = ReadAnsOptProp.getQuestionOption(Integer.toString(((Question)((ArrayList)vo.getAttribute("Questions")).get(0)).getQuestionOptionType()));

          return new PromisSurvey(activityId, (List<Question>) vo.getAttribute("Questions"), questionOptionsMap, (String) vo.getAttribute("sequencing"));
        } else if (activityId.equals("FINGERTAPPING")) {

          vo = __theDAO.getFingerTapping(activityId, patientPIN);
          String patientType = (String) vo.getAttribute("type");
          System.out.println(TAG + " getActivity() :- " + "PatientType = " + patientType);
          return new FingerTapping(activityId, patientType);

        } else if (activityId.equals("SPATIALSPAN")) {
          vo = __theDAO.getSpatialSpan(activityId, patientPIN);
          String patientType = (String) vo.getAttribute("type");
          System.out.println(TAG + " getActivity() :- " + "PatientType = " + patientType);
          return new SpatialSpan(activityId);

        } else if (activityId.equals("FLANKER")) {
          vo = __theDAO.getFlanker(activityId, patientPIN);
          String patientType = (String) vo.getAttribute("type");
          System.out.println(TAG + " getActivity() :- " + "PatientType = " + patientType);
          return new Flanker(activityId);

        } else if (activityId.equals("PATTERNCOMPARISON")) {
          vo = __theDAO.getPatternComparision(activityId, patientPIN);
          String patientType = (String) vo.getAttribute("type");
          System.out.println(TAG + " getActivity() :- Patient Type = " + patientType);
          return new PatternComparision(activityId);

        } else {
          System.out.println("Error from MODELFACTORY - Cannot create model object for activity:");
          log.warn("Cannot create model object for activity:" + activityId);//Need to have a better way for handling exception,moving forward quickly,will do it later
          return null;
        }
      }
    } catch (DAOException e) {
      log.error("Cannot create model object.");//Need to have a better way for handling exception,moving forward quickly,will do it later
      e.printStackTrace();
      throw new ModelException("Unable to create model obj");

    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public Patient getPatient(String patientPIN) throws ModelException {
    ValueObject vo = null;
    try {
      System.out.println(patientPIN);
      vo = __theDAO.getpatient(patientPIN);
    } catch (DAOException e) {
      e.printStackTrace();
      log.error(e);
      throw new ModelException("Error in creating model obj: " + e.getMessage());
    }

    return (Patient) (vo.getAttribute("Patient"));
  }

  public ArrayList<ActivityInstance> checkActivityInstance(String patientPIN) throws ModelException {
    ValueObject vo = null;
    try {
      vo = __theDAO.checkActivity(patientPIN);
      return (ArrayList<ActivityInstance>) vo.getAttribute("ActivityInstances"); // assuming this is JSON?
    } catch (DAOException de) {
      de.printStackTrace();
      log.error(de);
      throw new ModelException("Unable to create Model Object", de);
    }
  }

  public QuestionOption getOptionByText(String optionText) throws ModelException {
    ValueObject vo = null;
    try {
      vo = __theDAO.getQuestionOptionByText(optionText);
      return (QuestionOption) vo.getAttribute("questionOption");
    } catch (DAOException de) {
      de.printStackTrace();
      log.error(de);
      throw new ModelException("Unable to create Model Object", de);
    }
  }

  public boolean postActivityInstance(ArrayList<PostActivity> result) throws SQLException, DAOException {
    ValueObject vo = __theDAO.postActivityInstances(result);

    return (boolean) vo.getAttribute("result");
  }

  public String generateSequence(Activity activity) throws ModelException {
    ContainerActivity contAct = null;
    if (activity.getClass().getName().endsWith("ContainerActivity"))
      contAct = (ContainerActivity) activity;
    else
      return activity._activityId; //If the activity is a leaf activity,we just return the activity id.

    StringBuilder sequence = new StringBuilder("");

    //The if logic must be tested.
    if (contAct.getSequencing().name().equals("INTERLEAVE_ORDERED") && contAct.getSubActivities().get(0).getClass().getName().equals("ContainerActivity") && contAct.getSubActivities().get(1).getClass().getName().equals("ContainerActivity")) {
      //This logic needs to be tested by adding sufficient data.

      String ChildSequence1 = generateSequence(contAct.getSubActivities().get(0));
      String ChildSequence2 = generateSequence(contAct.getSubActivities().get(1));
      String[] sequence1 = ChildSequence1.split(",");
      String[] sequence2 = ChildSequence2.split(",");
      int i = 0;
      int j = 0;
      while (true) {
        if (sequence1[i] != null && sequence2[j] != null) {
          sequence.append(sequence1[i++] + "," + sequence2[j++] + ",");
        } else if (sequence1[i] != null) {
          sequence.append(sequence1[i++]);
        } else if (sequence2[j] != null) {
          sequence.append(sequence2[j++]);
        } else {
          break;
        }
      }
    } else {
      //If the Container Activity is RANDOM,We are randomizing the blocks.
      if (contAct.getSequencing().name().equals("RANDOM")) {
        Collections.shuffle(contAct.getSubActivities());
      }

      for (Activity atvt : contAct.getSubActivities()) {

        if (atvt.getClass().getName().endsWith("ContainerActivity")) {

          sequence.append(generateSequence(atvt) + ",");
        } else {

          sequence.append(atvt._activityId + ",");
        }
      }
    }
    if (sequence.toString().endsWith(",")) {
      sequence.deleteCharAt(sequence.length() - 1);
    }
    return sequence.toString();
  }

  //Inserting the generated sequence in the database.
  public boolean createActivityInstance(String sequence, String patientPIN, Trial trialType, String startTime, String endTime, String activityID) throws ModelException, DAOException {
    ValueObject vo = __theDAO.createActivityInstance(sequence, patientPIN, startTime, endTime, trialType, activityID);

    return (boolean) vo.getAttribute("result");
  }

  public boolean changeActivityInsState(int activityInstance, String state) throws DAOException {
    ValueObject vo = __theDAO.changeActivityInstanceState(activityInstance, state);

    return (boolean) vo.getAttribute("result");
  }

  public final class ContainerActivityMapping {
    public ContainerActivity.Sequencing _sequence;
    public ArrayList<String> _childActivities;

  }

  public boolean postUILoggerResults(ArrayList<UILogger> loggerResults) throws SQLException, DAOException {
    ValueObject vo = __theDAO.postUILogger(loggerResults);

    return (boolean) vo.getAttribute("result");
  }

  public String getActivityMetaData(String activityId) throws DAOException {
    ValueObject vo = __theDAO.getActivityMetaData(activityId);

    return (String) vo.getAttribute("metadata");
  }

  /**
   * This function will call the DAO for the enrolling the patients
   *
   * @param patientsInfos An ArrayList of patients Info
   * @return The newly enrolled patient pin
   * @throws DAOException
   * @throws SQLException
   */
  public String enrollPatients(ArrayList<Patient.PatientEnroll> patientsInfos) throws DAOException, SQLException {
    ValueObject vo = __theDAO.enrollPatients(patientsInfos);
    System.out.println("The result is" + vo.getAttribute("result"));
    return Integer.toString((int) (vo.getAttribute("result")));
  }

  /**
   * This method will return the trial name
   *
   * @param trialName
   * @return integer - The duration of the trial
   * @throws DAOException
   * @throws SQLException
   */
  public int getTrialDuration(String trialName) throws DAOException, SQLException {
    ValueObject vo = __theDAO.getTrialDuration(trialName);
    String startTime = (String) vo.getAttribute("startTime");
    String endTime = (String) vo.getAttribute("endTime");
    int duration = (int) vo.getAttribute("Duration");

    return duration;
  }
}
