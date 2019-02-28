package edu.asu.epilepsy.apiv30.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import edu.asu.epilepsy.apiv30.dao.DAOException;
import edu.asu.epilepsy.apiv30.dao.DAOFactory;
import edu.asu.epilepsy.apiv30.errorHandler.BadRequestCustomException;
import edu.asu.epilepsy.apiv30.errorHandler.ErrorMessage;
import edu.asu.epilepsy.apiv30.errorHandler.NotFoundException;
import edu.asu.epilepsy.apiv30.helper.APIConstants;
import edu.asu.epilepsy.apiv30.helper.GsonFactory;
import edu.asu.epilepsy.apiv30.model.Activity;
import edu.asu.epilepsy.apiv30.model.ActivityInstance;
import edu.asu.epilepsy.apiv30.model.CheckActivity;
import edu.asu.epilepsy.apiv30.model.ModelException;
import edu.asu.epilepsy.apiv30.model.ModelFactory;
import edu.asu.epilepsy.apiv30.model.Patient;
import edu.asu.epilepsy.apiv30.model.Patient.PatientEnroll;
import edu.asu.epilepsy.apiv30.model.Patient.Trial;
import edu.asu.epilepsy.apiv30.model.Patient.medicationInfo;
import edu.asu.epilepsy.apiv30.model.PostActivity;
import edu.asu.epilepsy.apiv30.model.PostFingerTapping;
import edu.asu.epilepsy.apiv30.model.PostFlanker;
import edu.asu.epilepsy.apiv30.model.PostPainIntensity;
import edu.asu.epilepsy.apiv30.model.PostPatternComparison;
import edu.asu.epilepsy.apiv30.model.PostPromisSurvey;
import edu.asu.epilepsy.apiv30.model.PostSpatialSpan;
import edu.asu.epilepsy.apiv30.model.QuestionOption;
import edu.asu.epilepsy.apiv30.model.Sequence;
import edu.asu.epilepsy.apiv30.model.UILogger;
import edu.asu.epilepsy.apiv30.model.response.CheckActivityResponse;
import edu.asu.epilepsy.apiv30.model.response.CreateActInstanceResponse;
import edu.asu.epilepsy.apiv30.model.response.GenDailyWeeklyResponse;
import edu.asu.epilepsy.apiv30.model.response.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PromisService {

  private static final String TAG = PromisService.class.getSimpleName();
  //ToDo Can handle this better by doing it dynamically without hardcoding and getting values from the database and checking if they are of that particular type.
  public static int otherOption = 67;
  //public static int maDosageQuestion = 75;
  private ModelFactory __modelFactory = null;
  static Logger log = LogManager.getLogger(PromisService.class);
  private static final PromisService __theService = new PromisService();
  private Gson gsonConverter = GsonFactory.getInstance().getGson();
  public static ArrayList<String> maDosageQuestion;

  static {
    String maDosageIds = DAOFactory.getDAOProperties().getProperty("maDosageQuestions");
    String[] maDosageIdsArray = maDosageIds.split(",");
    maDosageQuestion = new ArrayList<String>(Arrays.asList(maDosageIdsArray));
  }

  public PromisService() {
    try {
      __modelFactory = new ModelFactory();
    } catch (ModelException me) {
      me.printStackTrace();
      // YYY If we can't get model objects we really can't do anything
    }
  }

  public static PromisService getPromisService() {
    return __theService;
  }

  public String getActivityInstance(String activityInstanceId, String pin) throws Exception {

    Date timeStamp = new Date();
    JsonArray activityArray = new JsonArray();
    JsonArray initialactivityArray = new JsonArray();
    boolean showGame;
    Object intervention;
    Patient patient = __modelFactory.getPatient(pin);
    if (patient == null) {
      //The error validation has to go here
      String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("The PIN is invalid"));
      log.info("The PIN is invalid");
      throw new NotFoundException(Response.Status.NOT_FOUND, JsonErrorMessage);

    } else {
      ActivityInstance activityInstance = __modelFactory.getActivityInstance(activityInstanceId);
      if (activityInstance != null) {

        if (!activityInstance.getPatientPin().equals(pin)) {
          String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("The Activity Instance ID is not for the given Patient Pin."));
          log.info("The Activity Instance ID is not for the given Patient Pin.");
          throw new NotFoundException(Response.Status.CONFLICT, JsonErrorMessage);
        } else if (timeStamp.compareTo(activityInstance.getStartTime()) < 0) {
          String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Survey instance is not active"));
          log.info("Survey instance is not active");
          throw new BadRequestCustomException(JsonErrorMessage);

        } else if (timeStamp.compareTo(activityInstance.getEndTime()) > 0) {
          String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Survey instance has expired"));
          log.info("Survey instance has expired");
          throw new NotFoundException(Response.Status.NOT_FOUND, JsonErrorMessage);
        } else if (activityInstance.getState().equals("completed")) {
          String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Survey instance has been completed"));
          log.info("Survey instance has been completed");
          throw new NotFoundException(Response.Status.NOT_FOUND, JsonErrorMessage);
        } else {
          boolean state = __modelFactory.changeActivityInsState(Integer.parseInt(activityInstanceId), "in progress");
          Sequence sequence = activityInstance.getSequence();
          List<String> sequenceArray =  sequence.getSequence();
          String activityId = sequence.getParentactivity();
          Activity activity = __modelFactory.getActivity(activityId, pin);
          System.out.println(TAG + " getActivityInstance() :- " + activity);
          String act = activity.generateJSON();
          System.out.println(TAG + " getActivityInstance() :- " + act);
          intervention = gsonConverter.fromJson(activity.generateJSON(),Object.class);
          JsonElement element = gsonConverter.toJsonTree(intervention);
          if (element instanceof JsonArray) {
            initialactivityArray = (JsonArray) element;
            for (int i = 0; i < sequenceArray.size(); i++) {
              String seqId = sequenceArray.get(i);
              for (int j = 0; j < initialactivityArray.size(); j++) {
                JsonObject randomizer = (JsonObject) initialactivityArray.get(j);
                if (seqId.equals(randomizer.get("activityBlockId").getAsString())) {
                  activityArray.add(randomizer);
                  break;
                }
              }
            }
          } else if (element instanceof JsonObject) {
            activityArray.add(element);
          }
          showGame = patient.getType().equals("child");
          CreateActInstanceResponse createActInstanceResponse = new CreateActInstanceResponse(
            Status.SUCCESS,
            sequenceArray,
            activityInstance.getActivityTitle(),
            activityInstanceId,
            activityInstance.getStartTime().toString(),
            activityInstance.getEndTime().toString(),
            activityInstance.getState(),
            activityArray,
            showGame);
          return gsonConverter.toJson(createActInstanceResponse,CreateActInstanceResponse.class);
        }
      } else {
        String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Invalid survey instance ID"));
        log.info("Invalid survey instance ID");
        throw new NotFoundException(Response.Status.BAD_REQUEST, JsonErrorMessage);
      }
    }

  }

  public String checkActivityInstance(String patienPIN) throws Exception {

    Patient patient = __modelFactory.getPatient(patienPIN);
    if (patient == null) {
      //The error validation has to go here
      String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("The PIN is invalid"));
      log.info("The PIN is invalid");
      throw new NotFoundException(Response.Status.NOT_FOUND, JsonErrorMessage);

    } else {
      Boolean showEnhancedContent = false;
      ArrayList<ActivityInstance> activityInstanceList = new ArrayList<ActivityInstance>();
      activityInstanceList = __modelFactory.checkActivityInstance(patienPIN);
      JsonArray activitySeqArray = new JsonArray();


      for (ActivityInstance activityInstance : activityInstanceList) {
        JsonObject act = new JsonObject();
        String sequenceJson = gsonConverter.toJson(activityInstance.getSequence(),Sequence.class);
        CheckActivity checkActivity = new CheckActivity(
          activityInstance.getActivityInstanceId(),
          activityInstance.getEndTime().toString(),
          activityInstance.getActivityTitle(),
          activityInstance.getDescription(),
          activityInstance.getState(),
          sequenceJson
        );
        act = gsonConverter.toJsonTree(checkActivity).getAsJsonObject();

        activitySeqArray.add(act);
      }
      System.out.println(TAG + " checkActivityInstance() :- " + activitySeqArray.getAsString());

      CheckActivityResponse checkActivityResponse = new CheckActivityResponse(
        Status.SUCCESS,
        activitySeqArray,
        showEnhancedContent
      );

      return gsonConverter.toJson(checkActivityResponse,CheckActivityResponse.class);
    }


  }

  public String submitActivityInstance(String post_result, String pin, String activityInsId) throws ModelException, SQLException, DAOException {

    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    ArrayList<PostActivity> questionResult = new ArrayList<PostActivity>();
    JsonObject json = getJsonObject(post_result);
    int activityInstanceId = Integer.parseInt(json.get("activityInstanceID").toString());
    System.out.println(TAG + " submitActivityInstance() :- " + "ActivityInstanceID - " + activityInsId);
    Timestamp timeStamp = new Timestamp(json.get("timeStamp").getAsLong());
    System.out.println(TAG + " submitActivityInstance() :- " + "timestamp - " + timestamp);
    if (activityInstanceId == Integer.parseInt(activityInsId)) {
      ActivityInstance activityInstance = __modelFactory.getActivityInstance(Integer.toString(activityInstanceId));
      if (activityInstance != null) {
        if (!activityInstance.getPatientPin().equals(pin)) {
          String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("The Activity Instance ID is not for the given Patient Pin."));
          log.info("The Activity Instance ID is not for the given Patient Pin.");
          throw new NotFoundException(Response.Status.CONFLICT, JsonErrorMessage);
        } else if (timestamp.compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(activityInstance.getStartTime())) < 0) {
          String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Survey instance is not active"));
          log.info("Survey instance is not active");
          throw new BadRequestCustomException(JsonErrorMessage);

        } else if (timestamp.compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(activityInstance.getEndTime())) > 0) {
          String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Survey instance has expired"));
          log.info("Survey instance has expired");
          throw new NotFoundException(Response.Status.NOT_FOUND, JsonErrorMessage);
        } else if (activityInstance.getState().equals("pending") || activityInstance.getState().equals("in progress")) {
          System.out.println(TAG + " submitActivityInstance() :- " + "Activity Instance needs to be added to databse");
          JsonArray question_results = (JsonArray) json.get("activityResults");
          for (int i = 0; i < question_results.size(); i++) {
            JsonObject result = (JsonObject) question_results.get(i);
            String activityType = result.get("activityBlockId").toString();
            if (activityType.equals("PI_DAILY") || activityType.equals("PI_WEEKLY")) {
              JsonArray answers = (JsonArray) result.get("answers");
              Integer questionOptionLocation = null;
              Integer questionOptionIntensity = null;
              ArrayList<Integer> questOptionGeneralizedPain = null;
              for (int j = 0; j < answers.size(); j++) {
                HashMap<String, Integer> questionIDs = new HashMap<String, Integer>();
                JsonObject answerInstance = (JsonObject) answers.get(j);
                if (answerInstance.get("bodyPain") != null) {
                  JsonArray bodypain = (JsonArray) answerInstance.get("bodyPain");
                  JsonObject bodypain_instance = (JsonObject) bodypain.get(0);
                  System.out.println("bodyPain::" + bodypain_instance.getAsString());
                  String location = bodypain_instance.get("location").getAsString();
                  String intensity = bodypain_instance.get("intensity").toString();
                  QuestionOption questOptionBodyPainLocation = __modelFactory.getOptionByText(location);
                  questionOptionLocation = new Integer(questOptionBodyPainLocation.get_questionOptionId());
                  QuestionOption questOptionBodyPainIntensity = __modelFactory.getOptionByText(intensity);
                  questionOptionIntensity = new Integer(questOptionBodyPainIntensity.get_questionOptionId());
                  questionIDs.put("bodyPain", Integer.parseInt(answerInstance.get("quesID").toString()));
                } else if (answerInstance.get("generalizedpain") != null) {
                  questOptionGeneralizedPain = new ArrayList<Integer>();
                  JsonArray generalizedbodypain = (JsonArray) answerInstance.get("generalizedpain");
                  for (int k = 0; k < generalizedbodypain.size(); k++) {
                    questOptionGeneralizedPain.add(Integer.parseInt(generalizedbodypain.get(k).toString()));
                  }
                  questionIDs.put("generalizedPain", Integer.parseInt(answerInstance.get("quesID").toString()));
                } else {
                  //Unexpected key-value pair in json
                }

                PostPainIntensity postPainIntensity = new PostPainIntensity(activityType, questionIDs, activityInstanceId, timeStamp, questionOptionLocation, questionOptionIntensity, questOptionGeneralizedPain);
                questionResult.add(postPainIntensity);
              }
            } else if (activityType.equals("FINGERTAPPING")) {
              System.out.println(TAG + " submitActivityInstance() :- " + "Called");
              int screenWidth = Integer.parseInt(result.get("screenWidth").toString());
              int screenHeight = Integer.parseInt(result.get("screenHeight").toString());
              int timeToTap = Integer.parseInt(result.get("timeToTap").toString());
              int timeTakenToComplete = Integer.parseInt(result.get("timeTakenToComplete").toString());

              System.out.println(TAG + " submitActivityInstance() :- ScreenHeight=" + screenHeight);
              System.out.println(TAG + " submitActivityInstance() :- ScreenWidth=" + screenWidth);
              System.out.println(TAG + " submitActivityInstance() :- Time TO Tap =" + timeToTap);
              JsonArray answers = (JsonArray) result.get("answers");
              HashMap<String, Integer> fingerTappingResult = new HashMap<>();
              for (int j = 0; j < answers.size(); j++) {
                JsonObject answerInstance = (JsonObject) answers.get(j);
                String operatingHand = answerInstance.get("operatingHand").toString();
                int tapNumber = Integer.parseInt(answerInstance.get("tapNumber").toString());
                fingerTappingResult.put(operatingHand, tapNumber);
              }
              PostFingerTapping postFingerTapping = new PostFingerTapping(activityType, activityInstanceId, fingerTappingResult,
                timeToTap, screenWidth, screenHeight, timeTakenToComplete, timeStamp, Integer.parseInt(pin));
              questionResult.add(postFingerTapping);

            } else if (activityType.equals("SPATIALSPAN")) {
              int screenWidth = Integer.parseInt(result.get("screenWidth").toString());
              int screenHeight = Integer.parseInt(result.get("screenHeight").toString());
              int timeToComplete = Integer.parseInt(result.get("timeTakenToComplete").toString());
              System.out.println(TAG + " submitActivityInstance() :- ScreenHeight=" + screenHeight);
              System.out.println(TAG + " submitActivityInstance() :- ScreenWidth=" + screenWidth);
              System.out.println(TAG + " submitActivityInstance() :- Time taken to complete =" + timeToComplete);
              JsonArray answers = (JsonArray) result.get("answers");
              ArrayList<String> results = new ArrayList<String>();

              for (int j = 0; j < answers.size(); j++) {
                JsonObject answerInstance = (JsonObject) answers.get(j);
                System.out.println(TAG + " submitActivityInstance() :- " + answerInstance.getAsString());
                results.add(answerInstance.getAsString());
              }
              PostSpatialSpan postSpatialSpan = new PostSpatialSpan(activityType, activityInstanceId, results, timeToComplete, screenWidth, screenHeight,
                timeStamp, Integer.parseInt(pin));
              questionResult.add(postSpatialSpan);

            } else if (activityType.equals("FLANKER")) {

              int screenWidth = Integer.parseInt(result.get("screenWidth").toString());
              int screenHeight = Integer.parseInt(result.get("screenHeight").toString());
              int timeToComplete = Integer.parseInt(result.get("timeTakenToComplete").toString());
              System.out.println(TAG + " submitActivityInstance() :- ScreenHeight=" + screenHeight);
              System.out.println(TAG + " submitActivityInstance() :- ScreenWidth=" + screenWidth);
              System.out.println(TAG + " submitActivityInstance() :- Time taken to complete =" + timeToComplete);
              JsonArray answers = (JsonArray) result.get("answers");
              ArrayList<String> results = new ArrayList<String>();

              for (int j = 0; j < answers.size(); j++) {
                JsonObject answerInstance = (JsonObject) answers.get(j);
                System.out.println(TAG + " submitActivityInstance() :- " + answerInstance.getAsString());
                results.add(answerInstance.getAsString());
              }
              PostFlanker postFlanker = new PostFlanker(activityType, activityInstanceId, results, timeToComplete, screenWidth, screenHeight,
                timeStamp, Integer.parseInt(pin));
              questionResult.add(postFlanker);

            } else if (activityType.equals("PATTERNCOMPARISON")) {

              int screenWidth = Integer.parseInt(result.get("screenWidth").toString());
              int screenHeight = Integer.parseInt(result.get("screenHeight").toString());
              int timeToComplete = Integer.parseInt(result.get("timeTakenToComplete").toString());
              System.out.println(TAG + " submitActivityInstance() :- ScreenHeight=" + screenHeight);
              System.out.println(TAG + " submitActivityInstance() :- ScreenWidth=" + screenWidth);
              System.out.println(TAG + " submitActivityInstance() :- Time taken to complete =" + timeToComplete);
              JsonArray answers = (JsonArray) result.get("answers");
              ArrayList<String> results = new ArrayList<String>();

              for (int j = 0; j < answers.size(); j++) {
                JsonObject answerInstance = (JsonObject) answers.get(j);
                System.out.println(TAG + " submitActivityInstance() :- " + answerInstance.getAsString());
                results.add(answerInstance.getAsString());
              }
              PostPatternComparison postPatternComparison = new PostPatternComparison(activityType, activityInstanceId, results, timeToComplete, screenWidth, screenHeight,
                timeStamp, Integer.parseInt(pin));
              questionResult.add(postPatternComparison);

            } else {
              JsonArray answers = (JsonArray) result.get("answers");
              HashMap<Integer, ArrayList<PostPromisSurvey.OptionToValue>> _questionToOptions = new HashMap<Integer, ArrayList<PostPromisSurvey.OptionToValue>>();
              for (int j = 0; j < answers.size(); j++) {
                JsonObject answerInstance = (JsonObject) answers.get(j);
                JsonArray selected_optionsarray = (JsonArray) answerInstance.get("selectedOptions");
                int k = 0;
                ArrayList<PostPromisSurvey.OptionToValue> listOfOptionValues = new ArrayList<PostPromisSurvey.OptionToValue>();
                while (k < selected_optionsarray.size()) {
                  int optionID = -1;
                  String value = "";
                  String dosage = "";
                  //If it is a MA dosage question,then it will have answerID and dosage.
                  if (maDosageQuestion.contains(answerInstance.get("quesID"))) {
                    JsonObject selectedanswerOptions = (JsonObject) selected_optionsarray.get(k);
                    System.out.println("The answerID::" +  selectedanswerOptions.get("answerID").getAsInt());
                    optionID = selectedanswerOptions.get("answerID").getAsInt();
                    if (optionID == otherOption)
                      value = (selectedanswerOptions.get("dosage")).toString();
                    else
                      dosage = (selectedanswerOptions.get("dosage")).toString();
                  } else {
                    optionID = Integer.parseInt(selected_optionsarray.get(k).getAsString());
                  }

                  PostPromisSurvey.OptionToValue qo = null;
                  PostPromisSurvey postPromis = new PostPromisSurvey();
                  //If the selected option is other,we need to get the value field for it.
                  if (optionID == otherOption && activityType.equals("CAT")) {
                    value = selected_optionsarray.get(++k).getAsString();
                  }
                  qo = postPromis.new OptionToValue(optionID, value, dosage);
                  k++;
                  listOfOptionValues.add(qo);
                }
                _questionToOptions.put(Integer.parseInt(answerInstance.get("quesID").getAsString()), listOfOptionValues);
              }
              PostPromisSurvey postPromisSurvey = new PostPromisSurvey(activityType, activityInstanceId, _questionToOptions, timeStamp);
              questionResult.add(postPromisSurvey);
            }

          }

          if (__modelFactory.postActivityInstance(questionResult)) {
            edu.asu.epilepsy.apiv30.model.response.Response response = new edu.asu.epilepsy.apiv30.model.response.Response(Status.SUCCESS);
            return gsonConverter.toJson(response, edu.asu.epilepsy.apiv30.model.response.Response.class);
          } else {
            String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Survey_instance could not posted"));
            log.info("Survey_instance could not posted");
            throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR, JsonErrorMessage);
          }
        } else {
          String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Survey_instance has been completed"));
          log.info("Survey_instance has been completed");
          throw new NotFoundException(Response.Status.CONFLICT, JsonErrorMessage);
        }
      } else {
        String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Survey_instance does not exist"));
        log.info("Survey_instance does not exist");
        throw new NotFoundException(Response.Status.NOT_FOUND, JsonErrorMessage);
      }
    } else {
      String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("The ID's in the URL and JSON do not match"));
      log.info("The ID's in the URL and JSON do not match");
      throw new NotFoundException(Response.Status.CONFLICT, JsonErrorMessage);
    }

  }

  public String cronJob(String request) throws ModelException, DAOException, java.text.ParseException {
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    JsonObject jsonObject = new JsonObject();
    jsonObject = tryGetJsonObject(request);

    String activityId = jsonObject.get("parentactivity").getAsString();
    String pin = jsonObject.get("pin").getAsString();
    Patient patient = __modelFactory.getPatient(pin);
    if (patient == null) {
      //The error validation has to go here
      String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("The PIN is invalid"));
      log.info("The PIN is invalid");
      throw new NotFoundException(Response.Status.NOT_FOUND, JsonErrorMessage);

    } else {
      Activity activity = __modelFactory.getActivity(activityId, pin);
      if (activity != null) {
        String sequence = __modelFactory.generateSequence(activity);
        System.out.println(TAG + " cronJob() :- " + sequence);
        String seq_array[] = sequence.split(",");
        List<String> activitySeqArray = new ArrayList<>();
        for (String activity_seq : seq_array) {
          activitySeqArray.add(activity_seq);
        }
        Sequence seq = new Sequence();
        seq.setSequence(activitySeqArray);
        seq.setParentactivity(activityId);
        //seq.put("mandatory", (JSONArray)json.get("mandatory"));

        sequence = gsonConverter.toJson(seq,Sequence.class);
        //boolean isWeekly = (Boolean)json.get("isWeekly");
        String startTime = null;
        String endTime = null;
        if (jsonObject.get("starttime") == null && jsonObject.get("endtime") == null) //If the user does not provide the start and end time,we take the default from the metadata.
        {
          String metaData = __modelFactory.getActivityMetaData(activityId);
          System.out.println(TAG + " cronJob() :- " + metaData);
          JsonObject actvtMetaData = new JsonObject();
          actvtMetaData = gsonConverter.fromJson(metaData,JsonObject.class);
          JsonObject duration = (JsonObject) actvtMetaData.get("defaulttime");
          //duration = (JSONObject) parser.parse(defaultTime);
          int durations = duration.get("duration").getAsInt();
          String unit = duration.get("units").getAsString();
          ArrayList<String> startandEndTime = computeStartandEndTime(activityId, durations, 0);
          System.out.println(TAG + " cronJob() :- startandend" + startandEndTime.toString());
          startTime = startandEndTime.get(0); //The first index is the start time
          endTime = startandEndTime.get(1); // The second index is the end time.
        } else //If the user provides the start and end time,we override it.
        {
          startTime = jsonObject.get("starttime").getAsString();
          endTime = jsonObject.get("endtime").getAsString();
        }

        String trial = jsonObject.get("trial_type").getAsString();
        System.out.println(TAG + " cronJob() :- trial = " + trial);
        Trial trial_type = null;
        System.out.println(TAG + " cronJob() :- " + Trial.values());

        System.out.println(TAG + " timestamp :- timestamp " + timeStamp + " " + startTime);
        if (timeStamp.compareTo(startTime) >= 0) {
          System.out.println(TAG + " cronJob() :- Invalid Start TIme");
          String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Invalid StartTime."));
          log.info("Invalid StartTime.");
          throw new NotFoundException(Response.Status.BAD_REQUEST, JsonErrorMessage);
        }
        System.out.println(TAG + " cronJob() :- " + endTime + " " + startTime);
        if (endTime.compareTo(startTime) <= 0) {
          System.out.println(TAG + " cronJob() :- Invalid EndTime.");
          String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Invalid EndTime."));
          log.info("Invalid EndTime.");
          throw new NotFoundException(Response.Status.BAD_REQUEST, JsonErrorMessage);
        }
        for (Trial type : Trial.values()) {
          System.out.println(TAG + " cronJob() :- type.name() = " + "trial");
          if (type.name().equals(trial)) {
            trial_type = type;
            break;
          }

        }
        if (trial_type == null) {
          System.out.println(TAG + " cronJob() :- " + "Trial not exists");
          String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("The trial type does not exists"));
          log.info("The trial type does not exists");
          throw new NotFoundException(Response.Status.NOT_FOUND, JsonErrorMessage);
        }
        System.out.println(TAG + " cronJob() :- Sequence = " + sequence);
        System.out.println(TAG + " cronJob() :- pin = " + pin);

        System.out.println(TAG + " cronJob() :- trial_type = " + trial_type);

        System.out.println(TAG + " cronJob() :- startTime = " + startTime);

        System.out.println(TAG + " cronJob() :- endTime = " + endTime);

        System.out.println(TAG + " cronJob() :- activityId = " + activityId);
        edu.asu.epilepsy.apiv30.model.response.Response response;
        if (__modelFactory.createActivityInstance(sequence, pin, trial_type, startTime, endTime, activityId)) {
          response = new edu.asu.epilepsy.apiv30.model.response.Response(Status.SUCCESS);
        }
        else{
          response = new edu.asu.epilepsy.apiv30.model.response.Response(Status.FAILURE);
        }
        return gsonConverter.toJson(response, edu.asu.epilepsy.apiv30.model.response.Response.class);
      } else {
        String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Activity does not exist"));
        log.info("Activity does not exist");
        throw new NotFoundException(Response.Status.NOT_FOUND, JsonErrorMessage);
      }
    }
  }

  private JsonObject tryGetJsonObject(String request) {
    JsonObject jsonObject;
    try {
      jsonObject = gsonConverter.fromJson(request,JsonObject.class);
    } catch (Exception e) {
      e.printStackTrace();
      String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("The JSON is invalid"));
      log.info("The JSON is invalid");
      throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR, JsonErrorMessage);
    }
    return jsonObject;
  }

  public String submitUILoggerResults(String ui_logger_results) throws SQLException, DAOException, ModelException {

    ArrayList<UILogger> loggerResult = new ArrayList<UILogger>();
    JsonObject json = new JsonObject();
    json = getJsonObject(ui_logger_results);

    JsonArray logger_results = (JsonArray) json.get("loggerResults");

    for (int i = 0; i < logger_results.size(); i++) {

      JsonObject uiLogObj = (JsonObject) logger_results.get(i);
      String pin = uiLogObj.get("pin").getAsString();
      Patient patient;
      patient = __modelFactory.getPatient(pin);

      if (patient == null) {
        //The error validation has to go here
        String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("The PIN is invalid"));
        log.error("Error: " + JsonErrorMessage);
        //TODO: Copy these erred entries into an error file for future reference.
        continue;

      }
      String eventName = uiLogObj.get("eventName").getAsString();
      String metaData = uiLogObj.get("metaData").toString();
      Timestamp eventTime = new Timestamp(Long.parseLong(uiLogObj.get("eventTime").toString()));
      UILogger uiLoggerObject = new UILogger(pin, eventName, metaData, eventTime);
      loggerResult.add(uiLoggerObject);

    }

    edu.asu.epilepsy.apiv30.model.response.Response response = new edu.asu.epilepsy.apiv30.model.response.Response();
    if (loggerResult.size() > 0) {
      if (__modelFactory.postUILoggerResults(loggerResult)) {
        response.setStatus(Status.SUCCESS);
        return gsonConverter.toJson(response, edu.asu.epilepsy.apiv30.model.response.Response.class);
      } else {
        String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("UI Logger results could not posted"));
        log.info("UI Logger results could not posted");
        throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR, JsonErrorMessage);
      }
    } else {
      response.setMessage("The loggerResult could not be logged successfully");
      response.setStatus(Status.CONFLICT);
      return gsonConverter.toJson(response, edu.asu.epilepsy.apiv30.model.response.Response.class);
    }
  }

  private JsonObject getJsonObject(String ui_logger_results) {
    JsonObject json;
    try {
      json = (JsonObject) gsonConverter.toJsonTree(ui_logger_results,JsonObject.class);

    } catch (Exception e) {
      e.printStackTrace();
      String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("The JSON is invalid"));
      log.error("Error: The JSON is invalid");
      throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR, JsonErrorMessage);
    }
    return json;
  }

  /**
   * This function is used to enroll the patients with the received JSON.
   *
   * @param enrollPatientsJSON - the received JSON for the client.
   * @return A JSON string to returned to the client.
   * @throws SQLException
   * @throws DAOException
   * @throws ModelException
   * @throws java.text.ParseException
   */
  public String enrollPatients(String enrollPatientsJSON) throws SQLException, DAOException, ModelException, java.text.ParseException {
    JsonObject json = new JsonObject();

    json = tryGetJsonObject(enrollPatientsJSON);

    System.out.println("The json is::" + json);
    String patientType = json.get("patientGroup").getAsString();
    String associatedPin = json.get("childPin").getAsString();
    Patient isValidpatient = __modelFactory.getPatient(associatedPin);
    //Checking if both the parent/child pin is invalid
    if (patientType.equals("parent_proxy")) {
      if (isValidpatient == null) {
        //The error validation has to go here
        String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("The associated parent/child PIN is invalid"));
        log.info("The associated parent/child PIN is invalid");
        throw new NotFoundException(Response.Status.BAD_REQUEST, JsonErrorMessage);

      }
      //Check if the associated pin is a valid child pin.
      if (!isValidpatient.getType().equals(Patient.PatientType.child.toString())) {
        String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("The given child pin is not a valid child pin"));
        log.info("The given child pin is not a valid child pin");
        throw new NotFoundException(Response.Status.BAD_REQUEST, JsonErrorMessage);
      }
    }
    if (patientType.equals("parent_proxy") && (associatedPin == null || associatedPin.equals(""))) {
      String JsonErrorMessage = gsonConverter.toJson(new ErrorMessage("Child patient should be mapped to parent PIN"));
      log.info("Child patient should be mapped to parent PIN");
      throw new NotFoundException(Response.Status.BAD_REQUEST, JsonErrorMessage);
    }

    System.out.println("ChildPIN::" + associatedPin);

    associatedPin = (associatedPin == null || associatedPin.isEmpty()) ? null : associatedPin;
    String deviceType = json.get("deviceType").getAsString();
    String deviceVersion = json.get("deviceVersion").getAsString();
    String ishydroxyUreaPrescribed = json.get("hydroxureaTablets").getAsString();
    String otherMedicine = json.get("otherMedicine").getAsString();
    String otherInfo = json.get("otherInfo").getAsString();

    //Getting the JSON array of the medicationInformation
    JsonArray medDetails = (JsonArray) json.get("medDetails");
    ArrayList<medicationInfo> medicationInfos = new ArrayList<medicationInfo>();
    Patient patient = new Patient();
    for (int i = 0; i < medDetails.size(); i++) {
      JsonObject medicineDetails = (JsonObject) medDetails.get(i);

      String medicineName = medicineDetails.get("medicine").getAsString();
      int prescribedDosage = medicineDetails.get("prescribedDosage").getAsInt();
      int noOfTablets = medicineDetails.get("tablet").getAsInt();
      medicationInfo medInfo = patient.new medicationInfo(medicineName, "mg", prescribedDosage, noOfTablets);

      medicationInfos.add(medInfo);
    }

    PatientEnroll enroll = patient.new PatientEnroll(patientType, associatedPin, deviceType, deviceVersion
      , ishydroxyUreaPrescribed, medicationInfos, Trial.SICKLE_CELL);
    ArrayList<PatientEnroll> patientInfos = new ArrayList<PatientEnroll>();
    patientInfos.add(enroll);


    String patientPIN = __modelFactory.enrollPatients(patientInfos);
    Patient newPatient = __modelFactory.getPatient(patientPIN);
    String activityInstanceresult = generateDailyandWeeklyInstances(newPatient);

    edu.asu.epilepsy.apiv30.model.response.Response response = new edu.asu.epilepsy.apiv30.model.response.Response();
    if (patientPIN == null || patientPIN.equals("-1")) {
      response.setMessage("Error while generating patient pin");
      response.setStatus(Status.CONFLICT);
      return gsonConverter.toJson(response, edu.asu.epilepsy.apiv30.model.response.Response.class);
    }

    //This is the success case.
    response.setStatus(Status.SUCCESS);
    response.setPin(patientPIN);
    return gsonConverter.toJson(response, edu.asu.epilepsy.apiv30.model.response.Response.class);
  }

  private ArrayList<String> computeStartandEndTime(String activityID, int duration, int offSet) throws java.text.ParseException {
    ArrayList<String> startTimeandendTime = new ArrayList<String>();
    String startTime = null;
    String endTime = null;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    if (offSet <= 0) {
      cal.add(Calendar.MINUTE, 1);
      cal.add(Calendar.DAY_OF_MONTH, offSet);
      startTime = df.format(cal.getTime()); //The calculated StartTime.
    } else {
      /**
       * To Do
       * Need a permanent fix for handling clients from different timezone.
       * The idle way is to use the app's GMT TimeZone offset
       * But hacking it to handle EST alone which is 5 hrs behing GMT.
       * So startTime of 5:01:00 in GMT wil be 00:01:00 in EST.
       */
      //cal.add(Calendar.MINUTE, 1);
      cal.add(Calendar.DAY_OF_MONTH, offSet);
      cal.set(Calendar.HOUR_OF_DAY, 05);
      cal.set(Calendar.MINUTE, 01);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      startTime = df.format(cal.getTime()); //The calculated StartTime.
    }


    if (duration == 48) {
      /**
       * To Do
       * Need a permanent fix for handling clients from different timezone.
       * The idle way is to use the app's GMT TimeZone offset
       * But hacking it to handle EST alone which is 5 hrs behing GMT.
       * So endTime of 04:49:00 in the next day in GMT will be 23:59:00 in EST the previous night.
       * We are adding an additional day for compensating for the time window difference.
       */
      System.out.println(TAG + " computeStartandEndTime() :- EndTime being calculated");
      cal.clear(); //Clearing the previous calendar
      Date StartTime = df.parse(startTime);
      cal.setTime(StartTime);
      //cal.add(Calendar.DAY_OF_MONTH, 1); This is the original, the weekly is suppose to expire the following day.
      cal.add(Calendar.DAY_OF_MONTH, 2); //We are adding an additional day to compensate for the time window difference
      cal.set(Calendar.HOUR_OF_DAY, 04);
      cal.set(Calendar.MINUTE, 59);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      endTime = df.format(cal.getTime()); //The calculated endTime.
    }

    if (duration == 24) {
      //cal.clear(); //Clearing the previous calendar

      //cal.setTime(new Date());
      cal.add(Calendar.DAY_OF_MONTH, 1); //We are adding an additional day to compensate for the time window difference
      cal.set(Calendar.HOUR_OF_DAY, 04);
      cal.set(Calendar.MINUTE, 59);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      endTime = df.format(cal.getTime()); //The calculated endTime.
      System.out.println("The endTime for daily is::" + endTime);

    }

    startTimeandendTime.add(startTime);
    startTimeandendTime.add(endTime);

    return startTimeandendTime;
  }

  /**
   * This method calculates the no of weekly and daily for the patient
   *
   * @throws SQLException
   * @throws DAOException
   * @throws java.text.ParseException
   * @throws ModelException
   */
  public String generateDailyandWeeklyInstances(Patient patient) throws DAOException, SQLException, java.text.ParseException, ModelException {
    //call the model method.
    String trialName = DAOFactory.getDAOProperties().getProperty("trial.name");
    int trialDuration = __modelFactory.getTrialDuration(trialName);
    System.out.println("The duration is::" + trialDuration);
    String result = "";
    if (patient == null) {
      //The error handling
    }
    GenDailyWeeklyResponse genDailyWeeklyResponse = new GenDailyWeeklyResponse();

    for (int i = 0; i < trialDuration; i++) {
      if (patient != null) {
        genDailyWeeklyResponse.setPin(patient.getPatientPin());
      }
      String activityID = "";
      if (patient.getType().equals(Patient.PatientType.parent_proxy.toString()))
        activityID = APIConstants.weeklyActivityID_WITHOUT_PI;
      else
        activityID = APIConstants.weeklyActivityID;

      genDailyWeeklyResponse.setActivityId(activityID);

      genDailyWeeklyResponse.setTrial_type(Patient.Trial.SICKLE_CELL);
      ArrayList<String> startandendTime = computeStartandEndTime(activityID, 48, (i * 7));
      String startTime = startandendTime.get(0);
      String endTime = startandendTime.get(1);
      genDailyWeeklyResponse.setStartTime(startTime);
      genDailyWeeklyResponse.setEndTime(endTime);

      String genJSON = gsonConverter.toJson(genDailyWeeklyResponse,GenDailyWeeklyResponse.class);
      System.out.println("json::" + genJSON);
      result = cronJob(genJSON);
      System.out.println("json::" + genJSON);
    }
    int noOfDailies = (trialDuration - 1) * 7; // if it is a 6 week trial,we need to calculate for 5 weeks and just for one more day in the next week.
    for (int i = 0; i < noOfDailies; i++) {
      System.out.println("Gonna create daily activiy");
      if (patient != null) {
        genDailyWeeklyResponse.setPin(patient.getPatientPin());
      }
      String activityID = "";
      if (patient.getType().equals(Patient.PatientType.parent_proxy.toString()))
        activityID = APIConstants.dailyActivityID_MA;
      else
        activityID = APIConstants.dailyActivityID;

      genDailyWeeklyResponse.setActivityId(activityID);

      genDailyWeeklyResponse.setTrial_type(Patient.Trial.SICKLE_CELL);
      ArrayList<String> startandendTime = computeStartandEndTime(activityID, 24, i);
      String startTime = startandendTime.get(0);
      String endTime = startandendTime.get(1);
      genDailyWeeklyResponse.setStartTime(startTime);
      genDailyWeeklyResponse.setEndTime(endTime);

      String genJSON = gsonConverter.toJson(genDailyWeeklyResponse,GenDailyWeeklyResponse.class);
      System.out.println("json::" + genJSON);
      result = cronJob(genJSON);
      System.out.println("json::" + genJSON);
    }

    //For calculating the remaining 1 daily activity for the last week.
    genDailyWeeklyResponse.setPin(patient.getPatientPin());
    String activityID = "";
    if (patient.getType().equals(Patient.PatientType.parent_proxy.toString()))
      activityID = APIConstants.dailyActivityID_MA;
    else
      activityID = APIConstants.dailyActivityID;

    genDailyWeeklyResponse.setActivityId(activityID);

    genDailyWeeklyResponse.setTrial_type(Patient.Trial.SICKLE_CELL);
    ArrayList<String> startandendTime = computeStartandEndTime(activityID, 24, noOfDailies);
    String startTime = startandendTime.get(0);
    String endTime = startandendTime.get(1);
    genDailyWeeklyResponse.setStartTime(startTime);
    genDailyWeeklyResponse.setEndTime(endTime);

    String genJSON = gsonConverter.toJson(genDailyWeeklyResponse,GenDailyWeeklyResponse.class);
    System.out.println("json::" + genJSON);
    result = cronJob(genJSON);
    System.out.println("json::" + genJSON);

    return result;
  }

}
