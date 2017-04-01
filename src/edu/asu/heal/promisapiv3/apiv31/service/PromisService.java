package edu.asu.heal.promisapiv3.apiv31.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
//import org.apache.log4j.Logger;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.asu.heal.promisapiv3.apiv31.dao.DAOException;
import edu.asu.heal.promisapiv3.apiv31.dao.DAOFactory;
import edu.asu.heal.promisapiv3.apiv31.errorHandler.BadRequestCustomException;
import edu.asu.heal.promisapiv3.apiv31.errorHandler.ErrorMessage;
import edu.asu.heal.promisapiv3.apiv31.errorHandler.NotFoundException;
import edu.asu.heal.promisapiv3.apiv31.helper.APIConstants;
import edu.asu.heal.promisapiv3.apiv31.model.ActivatePatientBadge;
import edu.asu.heal.promisapiv3.apiv31.model.Activity;
import edu.asu.heal.promisapiv3.apiv31.model.ActivityInstance;
import edu.asu.heal.promisapiv3.apiv31.model.Badge;
import edu.asu.heal.promisapiv3.apiv31.model.BagdePowerups;
import edu.asu.heal.promisapiv3.apiv31.model.ModelException;
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
import edu.asu.heal.promisapiv3.apiv31.model.Powerups;
import edu.asu.heal.promisapiv3.apiv31.model.QuestionOption;
import edu.asu.heal.promisapiv3.apiv31.model.UILogger;
public class PromisService {

	//ToDo Can handle this better by doing it dynamically without hardcoding and getting values from the database and checking if they are of that particular type.
	public static int otherOption = 67;
	//public static int maDosageQuestion = 75;
    private ModelFactory __modelFactory = null;
    static Logger log = LogManager.getLogger(PromisService.class);
    private static final PromisService __theService = new PromisService();
	ObjectMapper mapper = new ObjectMapper();
	public static ArrayList<String>maDosageQuestion;

	static
	{
		String maDosageIds = DAOFactory.getDAOProperties().getProperty("maDosageQuestions");
		String [] maDosageIdsArray = maDosageIds.split(",");
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

    public String getActivityInstance(String activityInstanceId,String pin) throws Exception
    {

    		Date timeStamp =  new Date();
            JSONArray activityArray = new JSONArray();
            JSONArray initialactivityArray = new JSONArray();
            Boolean showGame;
            Object  intervention;
            Patient patient =  __modelFactory.getPatient(pin);
            if(patient == null)
            {
            	//The error validation has to go here
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The PIN is invalid"));
    			log.info("The PIN is invalid");
    			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);

            }
            else
            {
    	        ActivityInstance activityInstance = __modelFactory.getActivityInstance(activityInstanceId);
    	        if(activityInstance!=null)
    	        {

    	        	if(!activityInstance.getPatientPin().equals(pin))
    	        	{
    	        		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The Activity Instance ID is not for the given Patient Pin."));
    	        		log.info("The Activity Instance ID is not for the given Patient Pin.");
    	    			throw new NotFoundException(Response.Status.CONFLICT,JsonErrorMessage);
    	        	}
    	        	else if(timeStamp.compareTo(activityInstance.getStartTime())<0)
    	        	{
    	        		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Survey instance is not active"));
    	    			log.info("Survey instance is not active");
    	    			throw new BadRequestCustomException(JsonErrorMessage);

    	        	}
    	        	else if (timeStamp.compareTo(activityInstance.getEndTime())>0)
    	        	{
    	        		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Survey instance has expired"));
    	        		log.info("Survey instance has expired");
    	    			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
    	        	}
    	        	else if (activityInstance.getState().equals("completed"))
    	        	{
    	        		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Survey instance has been completed"));
    	    			log.info("Survey instance has been completed");
    	    			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
    	        	}
    	        	else
    	        	{
    	        		boolean state = __modelFactory.changeActivityInsState(Integer.parseInt(activityInstanceId), "in progress");
    			        String json_sequence = activityInstance.getSequence();
    			        JSONParser parser = new JSONParser();
    			        JSONObject jsonObject = (JSONObject) parser.parse(json_sequence);
    			        JSONArray sequenceArray=(JSONArray)jsonObject.get("sequence");
    			        String activityId = (String)jsonObject.get("parentactivity");
    			        Activity activity = __modelFactory.getActivity(activityId,pin);
    			        //String act=activity.generateJSON().toString().replace("\\","");
    			        intervention=parser.parse(activity.generateJSON().toString().replace("\\",""));

    			        if(intervention instanceof JSONArray)
    			        {
    			        	initialactivityArray = (JSONArray) intervention;
    			        	for (int i = 0; i<sequenceArray.size(); i++)
    			        	{
    			        		String seqId = (String) sequenceArray.get(i);
    			        		for(int j = 0; j<initialactivityArray.size(); j++)
    			        		{
    			        			JSONObject randomizer = (JSONObject) initialactivityArray.get(j);
    			        			if(seqId.equals(randomizer.get("activityBlockId")))
    			        			{
    			        				activityArray.add(randomizer);
    			        				break;
    			        			}
    			        		}
    			        	}
    			        }
    			        else if(intervention instanceof JSONObject)
    			        {
    			        	activityArray.add((JSONObject) intervention);
    			        }
    			        if(patient.getType().equals("child"))
    			        	showGame = true;
    			        else
    			        	showGame = false;
    			        JSONObject reply = new JSONObject();
    			        reply.put("message", "SUCCESS");
    			        reply.put("sequence", sequenceArray);
    			        reply.put("activityName", activityInstance.getActivityTitle());
    			        reply.put("activityInstanceId", activityInstanceId);
    			        reply.put("startTime",activityInstance.getStartTime().toString());
    			        reply.put("endTime", activityInstance.getEndTime().toString());
    			        reply.put("activityInstanceState",activityInstance.getState());
    			        reply.put("activitySequence", activityArray);
    			        reply.put("showGame", showGame);
    			        reply.put("mandatoryBlocks", jsonObject.get("mandatory"));


    			        return reply.toString().replace("\\","");
    		        }
    	        }
    	        else
    	        {
    	        	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Invalid survey instance ID"));
    	    		log.info("Invalid survey instance ID");
    	    		throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
    	    	}
            }

    }

    public String checkActivityInstance(String patientPIN) throws Exception
    {
    		Patient patient =  __modelFactory.getPatient(patientPIN);
            if(patient == null)
            {
            	//The error validation has to go here
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The PIN is invalid"));
    			log.info("The PIN is invalid");
    			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);

            }
            else
            {
            System.out.println("Collecting the enhancedContent flag for the patient");
            Boolean showEnhancedContent = patient.getEnhancedContent();
            System.out.println("showEnhancedContent flag received for the pin is : " + showEnhancedContent);

            ArrayList<ActivityInstance> activityInstanceList = new ArrayList<ActivityInstance>();
            activityInstanceList = __modelFactory.checkActivityInstance(patientPIN);
            JSONArray activitySeqArray = new JSONArray();


            for (ActivityInstance activityInstance : activityInstanceList )
            {
                JSONObject act = new JSONObject();
                act.put("activityInstanceID", activityInstance.getActivityInstanceId());
                act.put("nextDueAt", activityInstance.getEndTime().toString());
                //act.put("okayToStart", "true");
                act.put("activityTitle", activityInstance.getActivityTitle());
                act.put("description",activityInstance.getDescription());
                act.put("state", activityInstance.getState());
                activitySeqArray.add(act);
            }

            JSONObject reply = new JSONObject();
            // if enhancedContent return the count of unUsed badges present with the patient

            if(showEnhancedContent){
            	Boolean usedFlag = false;
            	int badgeCount = __modelFactory.getPatientBadgeCount(patientPIN, usedFlag);
            	System.out.println("Unused BadgeCount received is : " + badgeCount);
            	reply.put("unusedBadgeCount", badgeCount);
            }

            reply.put("message", "SUCCESS");
            reply.put("activities", activitySeqArray);
            reply.put("enhancedContent", showEnhancedContent);
            return reply.toString().replace("\\","");
            }


    }

    public String submitActivityInstance(String post_result, String pin,String activityInsId) throws ParseException, ModelException, SQLException, DAOException, JsonProcessingException
    {

    		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    		ArrayList<PostActivity> questionResult= new ArrayList<PostActivity>();
    		JSONParser parser = new JSONParser();
    		JSONObject json = new JSONObject();
    		try{
    			json = (JSONObject) parser.parse(post_result);
    		}
    		catch(Exception e)
        	{
        		e.printStackTrace();
        		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The JSON is invalid"));
    			log.error("Error: The JSON is invalid" );
    			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
        	}
    		int activityInstanceId = Integer.parseInt(json.get("activityInstanceID").toString());
    		Timestamp timeStamp = new Timestamp((long)json.get("timeStamp"));
    		if(activityInstanceId==Integer.parseInt(activityInsId))
    		{
            ActivityInstance activityInstance = __modelFactory.getActivityInstance(Integer.toString(activityInstanceId));
            if(activityInstance != null)
            {
            	if(!activityInstance.getPatientPin().equals(pin))
            	{
            		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The Activity Instance ID is not for the given Patient Pin."));
        			log.info("The Activity Instance ID is not for the given Patient Pin.");
        			throw new NotFoundException(Response.Status.CONFLICT,JsonErrorMessage);
            	}
            	else if(timestamp.compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(activityInstance.getStartTime()))<0)
            	{
            		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Survey instance is not active"));
        			log.info("Survey instance is not active");
        			throw new BadRequestCustomException(JsonErrorMessage);

            	}
            	else if (timestamp.compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(activityInstance.getEndTime()))>0)
            	{
            		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Survey instance has expired"));
        			log.info("Survey instance has expired");
        			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
            	}
            	else if(activityInstance.getState().equals("pending")|| activityInstance.getState().equals("in progress"))
            	{
    				JSONArray question_results=(JSONArray)json.get("activityResults");
    				for(int i=0;i<question_results.size();i++)
    				{
    					JSONObject result=(JSONObject) question_results.get(i);
    					String activityType = result.get("activityBlockId").toString();
    					if(activityType.equals("PI_DAILY")||activityType.equals("PI_WEEKLY"))
    					{
    						JSONArray answers =(JSONArray) result.get("answers");
    						Integer questionOptionLocation = null;
    						Integer questionOptionIntensity = null;
    						ArrayList<Integer> questOptionGeneralizedPain = null;
    						for(int j=0;j<answers.size();j++)
    						{
    							HashMap<String,Integer> questionIDs = new HashMap<String,Integer>();
    							JSONObject answerInstance=(JSONObject) answers.get(j);
    							if((JSONArray)answerInstance.get("bodyPain") != null)
    							{
    								JSONArray bodypain=(JSONArray)answerInstance.get("bodyPain");
    								JSONObject bodypain_instance=(JSONObject) bodypain.get(0);
    								System.out.println("bodyPain::"+bodypain_instance.toJSONString());
    								String location = (String) bodypain_instance.get("location");
    	    						String intensity = bodypain_instance.get("intensity").toString();
    	    						QuestionOption questOptionBodyPainLocation = __modelFactory.getOptionByText(location);
    	    						questionOptionLocation = new Integer(questOptionBodyPainLocation.get_questionOptionId());
    	    						QuestionOption questOptionBodyPainIntensity = __modelFactory.getOptionByText(intensity);
    	    						questionOptionIntensity = new Integer(questOptionBodyPainIntensity.get_questionOptionId());
    	    						questionIDs.put("bodyPain", Integer.parseInt( answerInstance.get("quesID").toString()));
    							}
    							else if((JSONArray)answerInstance.get("generalizedpain") != null)
    							{
    								questOptionGeneralizedPain = new ArrayList<Integer>();
    								JSONArray generalizedbodypain=(JSONArray)answerInstance.get("generalizedpain");
    								for(int k=0;k<generalizedbodypain.size();k++)
    								{
    									questOptionGeneralizedPain.add(Integer.parseInt(generalizedbodypain.get(k).toString()));
    								}
    								questionIDs.put("generalizedPain", Integer.parseInt( answerInstance.get("quesID").toString()));
    							}
    							else
    							{
    								//Unexpected key-value pair in json
    							}

    							PostPainIntensity postPainIntensity = new PostPainIntensity(activityType,questionIDs,activityInstanceId,timeStamp,questionOptionLocation,questionOptionIntensity,questOptionGeneralizedPain);
    							questionResult.add(postPainIntensity);
    						}
    					}
    					else
    					{
    						JSONArray answers =(JSONArray) result.get("answers");
    						HashMap<Integer,ArrayList<PostPromisSurvey.OptionToValue>> _questionToOptions = new HashMap<Integer,ArrayList<PostPromisSurvey.OptionToValue>>();
    						for(int j=0;j<answers.size();j++)
    						{
    							JSONObject answerInstance=(JSONObject) answers.get(j);
    							JSONArray selected_optionsarray = (JSONArray) answerInstance.get("selectedOptions");
    							int k = 0;
    							ArrayList<PostPromisSurvey.OptionToValue> listOfOptionValues = new ArrayList<PostPromisSurvey.OptionToValue>();
    							while(k<selected_optionsarray.size())
    							{
    								int optionID = -1;
    								String value = "";
    								String dosage = "";
    								//If it is a MA dosage question,then it will have answerID and dosage.
    								if(maDosageQuestion.contains((String)answerInstance.get("quesID")))
									{
    									JSONObject selectedanswerOptions =(JSONObject) selected_optionsarray.get(k);
    									System.out.println("The answerID::"+(((Long)selectedanswerOptions.get("answerID")).intValue()));
    									optionID = ((Long)selectedanswerOptions.get("answerID")).intValue();
    									if(optionID == otherOption)
    										value = (selectedanswerOptions.get("dosage")).toString();
    									else
    										dosage = (selectedanswerOptions.get("dosage")).toString();
									}
    								else
    								{
    									optionID = Integer.parseInt((String)selected_optionsarray.get(k));
    								}

    								PostPromisSurvey.OptionToValue qo = null;
    								PostPromisSurvey postPromis = new PostPromisSurvey();
    								//If the selected option is other,we need to get the value field for it.
    								if(optionID == otherOption && activityType.equals("CAT"))
    								{
    									value = (String)selected_optionsarray.get(++k);
    								}
    								qo = postPromis.new OptionToValue(optionID,value,dosage);
    								k++;
    								listOfOptionValues.add(qo);
    							}
    							_questionToOptions.put(Integer.parseInt((String) answerInstance.get("quesID")),listOfOptionValues);
    						}
    						PostPromisSurvey postPromisSurvey = new PostPromisSurvey(activityType,activityInstanceId,_questionToOptions,timeStamp);
    						questionResult.add(postPromisSurvey);
    					}

    				}

    				Patient patient = __modelFactory.getPatient(pin);
    				Boolean enhancedContentFlag = false;

    				if(patient != null){
    					enhancedContentFlag = patient.getEnhancedContent();
    				}

    				JSONObject reply = new JSONObject();
    			    if(__modelFactory.postActivityInstance(questionResult))
    			    {
    			    	System.out.println("Finished submitting activity instance to DB");
    			    	String json_response = null;

    			    	try {
    			    		if(enhancedContentFlag){
        			    		System.out.println("Calling the badge allocation function now");
    							json_response = __theService.allocatePatientBadge(activityInsId, pin);
    							System.out.println("Response received from allocatePatientBadge function is : " + json_response);
    							reply.put("badgeAllocationMessage", json_response);
    			    		}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Badge allocation function call failed"));
							log.info("Badge allocation function call failed");
							throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
						}finally{
	    			    	reply.put("message", "SUCCESS");
	    			    	return reply.toJSONString();
						}
    			    }
    			    else
    			    {
    			    	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Survey_instance could not posted"));
    	    			log.info("Survey_instance could not posted");
    	    			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
    			    }


            	}
            	else
            	{
            		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Survey_instance has been completed"));
        			log.info("Survey_instance has been completed");
        			throw new NotFoundException(Response.Status.CONFLICT,JsonErrorMessage);
            	}
            }
            else
            {
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Survey_instance does not exist"));
    			log.info("Survey_instance does not exist");
    			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
    		}
    		}
    		else
    		{
    			String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The ID's in the URL and JSON do not match"));
    			log.info("The ID's in the URL and JSON do not match");
    			throw new NotFoundException(Response.Status.CONFLICT,JsonErrorMessage);
    		}

    }

    public String cronJob(String result) throws ParseException, ModelException, DAOException, JsonProcessingException, java.text.ParseException
    {
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
    		JSONParser parser = new JSONParser();
    		JSONObject json = new JSONObject();
    		try
        	{
    			json = (JSONObject) parser.parse(result);
        	}
    		catch(Exception e)
        	{
        		e.printStackTrace();
        		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The JSON is invalid"));
    			log.info("The JSON is invalid");
    			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
        	}

    		String activityId = (String)json.get("parentactivity");
    		String pin = (String)json.get("pin");
    		Patient patient =  __modelFactory.getPatient(pin);
            if(patient == null)
            {
            	//The error validation has to go here
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The PIN is invalid"));
    			log.info("The PIN is invalid");
    			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);

            }
            else
            {
            Activity activity = __modelFactory.getActivity(activityId,pin);
            if(activity != null)
            {
    	        String sequence = __modelFactory.generateSequence(activity);
    			String seq_array[] = sequence.split(",");
    	        JSONArray activitySeqArray = new JSONArray();
    	        for (String activity_seq : seq_array)
    	        	activitySeqArray.add(activity_seq);
    	        JSONObject seq = new JSONObject();
    	        seq.put("sequence", activitySeqArray);
    	        seq.put("parentactivity", activityId);
    	        //seq.put("mandatory", (JSONArray)json.get("mandatory"));

    	        sequence = seq.toString().replace("\\","");
    	        //boolean isWeekly = (Boolean)json.get("isWeekly");
    	        String startTime = null;
    	        String endTime = null;
    	        if((String)json.get("starttime") == null && (String)json.get("endtime") == null) //If the user does not provide the start and end time,we take the default from the metadata.
    	        {
    	        	String metaData = __modelFactory.getActivityMetaData(activityId);
    	        	JSONObject actvtMetaData = new JSONObject();
    	        	actvtMetaData = (JSONObject) parser.parse(metaData);
    	        	JSONObject duration = (JSONObject)actvtMetaData.get("defaulttime");
    	        	//duration = (JSONObject) parser.parse(defaultTime);
    	        	int durations =  Integer.parseInt((String)duration.get("duration"));
    	        	String unit = (String)duration.get("units");
    	        	ArrayList<String> startandEndTime = computeStartandEndTime(activityId,durations,0);
    	        	startTime= startandEndTime.get(0); //The first index is the start time
    	        	endTime = startandEndTime.get(1); // The second index is the end time.
    	        }
    	        else //If the user provides the start and end time,we override it.
    	        {
    	        	startTime = (String)json.get("starttime");
        	        endTime = (String)json.get("endtime");
    	        }

    	        String trial = (String)json.get("trial_type");
    	        Trial trial_type = null;
    	        if(timeStamp.compareTo(startTime)>=0)
    	        {
     	    	   String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Invalid StartTime."));
     				log.info("Invalid StartTime.");
     				throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
     	       }
    	        if(endTime.compareTo(startTime)<=0)
    	        {
     	    	   String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Invalid EndTime."));
     				log.info("Invalid EndTime.");
     				throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
     	       }
    	        for (Trial type : Trial.values())
    	        {
    	        	if(type.name().equals(trial))
    	        	{
    	        		trial_type= type;
    	        		break;
    	        	}

    	        }
    	       if(trial_type == null)
    	       {
    	    	   String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The trial type does not exists"));
    				log.info("The trial type does not exists");
    				throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
    	       }
    	        JSONObject reply = new JSONObject();
    	        if(__modelFactory.createActivityInstance(sequence,pin,trial_type,startTime,endTime,activityId))
    	        	reply.put("message", "SUCCESS");
    	        else
    	    		reply.put("message", "FAILURE");
    	        return reply.toString().replace("\\","");
            }
            else
            {
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Activity does not exist"));
    			log.info("Activity does not exist");
    			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
            }
            }
    }

    public String submitUILoggerResults(String ui_logger_results) throws SQLException, DAOException, JsonProcessingException, ModelException
    {
    	System.out.println("Inside submitUILoggerResults promis service method");
    	ArrayList<UILogger> loggerResult= new ArrayList<UILogger>();

    	JSONParser parser = new JSONParser();
		JSONObject json = new JSONObject();
		try{
			json = (JSONObject) parser.parse(ui_logger_results);
			System.out.println("JSON has been parsed");

		}
		catch(Exception e)
    	{
			e.printStackTrace();
    		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The JSON is invalid"));
			log.error("Error: The JSON is invalid" );
			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
    	}

    	JSONArray logger_results=(JSONArray)json.get("loggerResults");
    	System.out.println("JSON Array for logger results collected");

    	for(int i=0;i<logger_results.size();i++)
    	{
    		System.out.println("Inside for loop");
    		JSONObject uiLogObj =(JSONObject) logger_results.get(i);
     		String pin = (String) uiLogObj.get("pin");
     		Patient patient;
			patient = __modelFactory.getPatient(pin);

            if(patient == null)
            {
            	//The error validation has to go here
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The PIN is invalid"));
      			log.error("Error: " + JsonErrorMessage);
    			//TODO: Copy these erred entries into an error file for future reference.
    			continue;

            }
            System.out.println("Patient PIN exists");
    		String eventName = (String) uiLogObj.get("eventName");
    		System.out.println("Collected eventName");
    		String metaData = (String) uiLogObj.get("metaData").toString();
    		System.out.println("Collected metaData");
    		Timestamp eventTime = new Timestamp(Long.parseLong(uiLogObj.get("eventTime").toString()));
    		UILogger uiLoggerObject = new UILogger(pin,eventName,metaData,eventTime);
    		loggerResult.add(uiLoggerObject);

    	}

    	JSONObject reply = new JSONObject();
    	if(loggerResult.size() > 0){
    	    if(__modelFactory.postUILoggerResults(loggerResult))
    	    {
    	    	reply.put("message", "SUCCESS");
    	    	return reply.toJSONString();
    	    }
    	    else
    	    {
    	    	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("UI Logger results could not posted"));
    			log.info("UI Logger results could not posted");
    			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
    	    }
    	}
    	else{
    		reply.put("message", "The loggerResult could not be logged successfully");
    		reply.put("status", Response.Status.CONFLICT);
    		return reply.toJSONString();
    	}
    }
    /**
     * This function is used to enroll the patients with the received JSON.
     * @param enrollPatientsJSON - the received JSON for the client.
     * @return A JSON string to returned to the client.
     * @throws SQLException
     * @throws DAOException
     * @throws JsonProcessingException
     * @throws ModelException
     * @throws ParseException
     * @throws java.text.ParseException
     */
    public String enrollPatients(String enrollPatientsJSON) throws SQLException, DAOException, JsonProcessingException, ModelException, java.text.ParseException, ParseException
    {
    	JSONParser parser = new JSONParser();
		JSONObject json = new JSONObject();

		try
    	{
			json = (JSONObject) parser.parse(enrollPatientsJSON);
    	}
		catch(Exception e)
    	{
    		e.printStackTrace();
    		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The JSON is invalid"));
			log.info("The JSON is invalid");
			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
    	}

		System.out.println("The json is::"+json);
		String patientType = (String)json.get("patientGroup");
		String associatedPin = (String) json.get("childPin");
		Patient isValidpatient =  __modelFactory.getPatient(associatedPin);
		//Checking if both the parent/child pin is invalid
		if(patientType.equals("parent_proxy"))
		{
			if(isValidpatient == null)
	        {
	        	//The error validation has to go here
	        	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The associated parent/child PIN is invalid"));
				log.info("The associated parent/child PIN is invalid");
				throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);

	        }
			//Check if the associated pin is a valid child pin.
			if(!isValidpatient.getType().equals(Patient.PatientType.child.toString()))
			{
				String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The given child pin is not a valid child pin"));
				log.info("The given child pin is not a valid child pin");
				throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
			}
		}
		if(patientType.equals("parent_proxy") && (associatedPin == null || associatedPin.equals("")))
		{
			String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Child patient should be mapped to parent PIN"));
			log.info("Child patient should be mapped to parent PIN");
			throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
		}

		System.out.println("ChildPIN::"+associatedPin);

		associatedPin = (associatedPin == null || associatedPin.isEmpty())?null:associatedPin;
		String deviceType = (String)json.get("deviceType");
		String deviceVersion = (String)json.get("deviceVersion");
		String ishydroxyUreaPrescribed = (String)json.get("hydroxureaTablets");
		String otherMedicine = (String)json.get("otherMedicine");
		String otherInfo = (String)json.get("otherInfo");

		//Getting the JSON array of the medicationInformation
		JSONArray medDetails=(JSONArray)json.get("medDetails");
    	ArrayList<medicationInfo> medicationInfos = new ArrayList<medicationInfo>();
    	Patient patient = new Patient();
    	for(int i=0;i<medDetails.size();i++)
    	{
    		JSONObject medicineDetails =(JSONObject) medDetails.get(i);

     		String medicineName = (String) medicineDetails.get("medicine");
    		int prescribedDosage = Integer.parseInt((String) medicineDetails.get("prescribedDosage"));
    		int noOfTablets = Integer.parseInt((String) medicineDetails.get("tablet"));
    		medicationInfo medInfo = patient.new medicationInfo(medicineName,"mg",prescribedDosage,noOfTablets);

    		medicationInfos.add(medInfo);
    	}

    	PatientEnroll enroll = patient.new PatientEnroll(patientType,associatedPin,deviceType,deviceVersion
    													,ishydroxyUreaPrescribed,medicationInfos,Trial.SICKLE_CELL);
		ArrayList<PatientEnroll> patientInfos = new ArrayList<PatientEnroll>();
		patientInfos.add(enroll);


		String patientPIN =  __modelFactory.enrollPatients(patientInfos);
		Patient newPatient = __modelFactory.getPatient(patientPIN);
		String activityInstanceresult = generateDailyandWeeklyInstances(newPatient);

		JSONObject reply = new JSONObject();

    	if(patientPIN == null || patientPIN.equals("-1")){
    		reply.put("message", "Error while generating patient pin");
    		reply.put("status", Response.Status.CONFLICT);
    		return reply.toJSONString();
    	}

    	//This is the success case.
    	reply.put("message", "SUCCESS");
    	reply.put("patientPIN",patientPIN);
    	return reply.toJSONString();
	}

    private ArrayList<String> computeStartandEndTime(String activityID,int duration,int offSet) throws java.text.ParseException
    {
    	ArrayList<String> startTimeandendTime = new ArrayList<String>();
    	String startTime = null;
    	String endTime = null;
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		if(offSet <= 0)
		{
			cal.add(Calendar.MINUTE, 1);
			cal.add(Calendar.DAY_OF_MONTH,offSet);
			startTime = df.format(cal.getTime()); //The calculated StartTime.
		}
		else
		{
			/**
			 * To Do
			 * Need a permanent fix for handling clients from different timezone.
			 * The idle way is to use the app's GMT TimeZone offset
			 * But hacking it to handle EST alone which is 5 hrs behing GMT.
			 * So startTime of 5:01:00 in GMT wil be 00:01:00 in EST. 
			 */
			//cal.add(Calendar.MINUTE, 1);
			cal.add(Calendar.DAY_OF_MONTH,offSet);
			cal.set(Calendar.HOUR_OF_DAY, 05); 
			cal.set(Calendar.MINUTE, 01);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			startTime = df.format(cal.getTime()); //The calculated StartTime.
		}
		
		
		if(duration == 48)
		{
			/**
			 * To Do
			 * Need a permanent fix for handling clients from different timezone.
			 * The idle way is to use the app's GMT TimeZone offset
			 * But hacking it to handle EST alone which is 5 hrs behing GMT.
			 * So endTime of 04:49:00 in the next day in GMT will be 23:59:00 in EST the previous night.
			 * We are adding an additional day for compensating for the time window difference. 
			 */
			System.out.println("EndTime being calculated");
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

		if(duration == 24)
		{
			//cal.clear(); //Clearing the previous calendar

			//cal.setTime(new Date());
			cal.add(Calendar.DAY_OF_MONTH, 1); //We are adding an additional day to compensate for the time window difference 
			cal.set(Calendar.HOUR_OF_DAY, 04);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			endTime = df.format(cal.getTime()); //The calculated endTime.
			System.out.println("The endTime for daily is::"+endTime);

		}

    	startTimeandendTime.add(startTime);
    	startTimeandendTime.add(endTime);

    	return startTimeandendTime;
    }
    /**
     * This method calculates the no of weekly and daily for the patient
     * @throws SQLException
     * @throws DAOException
     * @throws java.text.ParseException
     * @throws ModelException
     * @throws JsonProcessingException
     */
    public String generateDailyandWeeklyInstances(Patient patient) throws DAOException, SQLException, java.text.ParseException, JsonProcessingException, ParseException, ModelException
    {
    	//call the model method.
    	String trialName = DAOFactory.getDAOProperties().getProperty("trial.name");
    	int trialDuration =  __modelFactory.getTrialDuration(trialName);
    	System.out.println("The duration is::"+trialDuration);
    	String result = "";
    	if(patient == null)
    	{
    		//The error handling
    	}
    	JSONObject createActivityJSON = new JSONObject();

    	for(int i=0;i<trialDuration;i++)
    	{
    		createActivityJSON.put("pin",patient.getPatientPin());
    		String activityID = "";
        	if(patient.getType().equals(Patient.PatientType.parent_proxy.toString()))
        		activityID = APIConstants.weeklyActivityID_WITHOUT_PI;
        	else
        		activityID = APIConstants.weeklyActivityID;

    		createActivityJSON.put("parentactivity",activityID);

        	createActivityJSON.put("trial_type",Patient.Trial.SICKLE_CELL.toString());
        	ArrayList<String> startandendTime = computeStartandEndTime(activityID,48,(i*7));
        	String startTime = startandendTime.get(0);
        	String endTime = startandendTime.get(1);
        	createActivityJSON.put("starttime",startTime);
        	createActivityJSON.put("endtime",endTime);
        	System.out.println("json::"+createActivityJSON.toJSONString());
        	result = cronJob(createActivityJSON.toJSONString());
        	System.out.println("json::"+createActivityJSON.toJSONString());
    	}
    	int noOfDailies = (trialDuration -1) * 7; // if it is a 6 week trial,we need to calculate for 5 weeks and just for one more day in the next week.
    	for(int i=0;i< noOfDailies ;i++)
    	{
    		System.out.println("Gonna create daily activiy");
    		createActivityJSON.put("pin",patient.getPatientPin());
    		String activityID = "";
        	if(patient.getType().equals(Patient.PatientType.parent_proxy.toString()))
        		activityID = APIConstants.dailyActivityID_MA;
        	else
        		activityID = APIConstants.dailyActivityID;

    		createActivityJSON.put("parentactivity",activityID);

        	createActivityJSON.put("trial_type",Patient.Trial.SICKLE_CELL.toString());
        	ArrayList<String> startandendTime = computeStartandEndTime(activityID,24,i);
        	String startTime = startandendTime.get(0);
        	String endTime = startandendTime.get(1);
        	createActivityJSON.put("starttime",startTime);
        	createActivityJSON.put("endtime",endTime);
        	System.out.println("json::"+createActivityJSON.toJSONString());
        	result = cronJob(createActivityJSON.toJSONString());
        	System.out.println("json::"+createActivityJSON.toJSONString());
    	}

    	//For calculating the remaining 1 daily activity for the last week.
    	createActivityJSON.put("pin",patient.getPatientPin());
		String activityID = "";
    	if(patient.getType().equals(Patient.PatientType.parent_proxy.toString()))
    		activityID = APIConstants.dailyActivityID_MA;
    	else
    		activityID = APIConstants.dailyActivityID;

		createActivityJSON.put("parentactivity",activityID);

    	createActivityJSON.put("trial_type",Patient.Trial.SICKLE_CELL.toString());
    	ArrayList<String> startandendTime = computeStartandEndTime(activityID,24,noOfDailies);
    	String startTime = startandendTime.get(0);
    	String endTime = startandendTime.get(1);
    	createActivityJSON.put("starttime",startTime);
    	createActivityJSON.put("endtime",endTime);
    	System.out.println("json::"+createActivityJSON.toJSONString());
    	result = cronJob(createActivityJSON.toJSONString());
    	System.out.println("json::"+createActivityJSON.toJSONString());

    	return result;
    }

    public String getPatientBadges(String patientPIN) throws Exception{

    	Patient patient =  __modelFactory.getPatient(patientPIN);
        if(patient == null)
        {
        	System.out.println("PIN does not exist in the system");
        	//The error validation has to go here
        	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The PIN is invalid"));
			log.info("The PIN is invalid");
			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);

        }
        else
        {
        	  System.out.println("PIN exists in the system");
        	  ArrayList<PatientBadges> patientBadgeList = new ArrayList<PatientBadges>();
        	  patientBadgeList = __modelFactory.getPatientBadges(patientPIN);
        	  System.out.println("patientBadgeList size : " + patientBadgeList.size());

              JSONArray patientBadgeArray = new JSONArray();

              for (PatientBadges patientBadge : patientBadgeList ){

            	  String badgeId = patientBadge.getBadgeId();
            	  Badge badgeInfo = __modelFactory.getBadge(badgeId);

            	  if(badgeInfo == null){
            		System.out.println("Badge does not exist in the system");
                  	//The error validation has to go here
                  	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The BadgeId is invalid"));
          			log.info("The BadgeId is invalid");
          			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
            	  }else{
            		  JSONObject patBadge = new JSONObject();
            		  patBadge.put("badgeId", patientBadge.getBadgeId());
            		  patBadge.put("badgeName", badgeInfo.getBadgeName());
            		  patBadge.put("badgeDesc", badgeInfo.getBadgeDesc());
            		  patBadge.put("badgeUsed", patientBadge.getBadgeUsed());
            		  patBadge.put("badgeType", badgeInfo.getBadgeType());
            		  patBadge.put("activityInstanceId", patientBadge.getActivityInstanceId());

            		  if(!patientBadge.getBadgeUsed()){
            			  // call BadgePowerup function to get the Powerup information
            			  BagdePowerups badgePowerup = __modelFactory.getBadgePowerup(patientBadge.getBadgeId());
            			  if(badgePowerup == null){
            				  System.out.println("Badge-powerup record does not exist in the system");
            				  //The error validation has to go here
                              String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The badge-powerup record does not exist"));
                    		  log.info("The badge-powerup record does not exist");
                    		  throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
            			  }else{
            				  String powerupId = badgePowerup.getPowerupId();

                			  Powerups powerup = __modelFactory.getPowerup(powerupId);

                			  if(powerup == null){
                				  System.out.println("Badge-powerup record does not exist in the system");
                				  //The error validation has to go here
                                  String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The badge-powerup record does not exist"));
                        		  log.info("The badge-powerup record does not exist");
                        		  throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
                			  }else{
                				  JSONObject powerupObj = new JSONObject();
                				  powerupObj.put("powerupId", powerupId);
                				  powerupObj.put("powerupCount", badgePowerup.getPowerupCount());

                				  patBadge.put("powerupInfo", powerupObj);
                			  }
            			  }
            		  }
            		  patientBadgeArray.add(patBadge);
            	  }
              }

              System.out.println("patientBadgeArray is : " + patientBadgeArray);

              JSONObject reply = new JSONObject();
              reply.put("message", "SUCCESS");
              reply.put("patientBadges", patientBadgeArray);
              return reply.toString().replace("\\","");
        }
    }

    public String activatePatientBadge(String patientBadgeJSON, String patientPIN) throws Exception{

    	System.out.println("Inside promis service method for activatePatientBadge");

    	Patient patient = __modelFactory.getPatient(patientPIN);

    	if(patient == null){
    		System.out.println("PIN does not exist in the system");
        	//The error validation has to go here
        	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The PIN is invalid"));
			log.info("The PIN is invalid");
			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
    	}
    	else{
    		System.out.println("Patient exists in the system");

    		JSONParser parser = new JSONParser();
    		JSONObject json = new JSONObject();

    		try{
    			json = (JSONObject) parser.parse(patientBadgeJSON);
    		}catch(Exception e){
    			e.printStackTrace();
        		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The JSON is invalid"));
    			log.info("The JSON is invalid");
    			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
    		}

    		System.out.println("The json is :" + json);

    		String badgeId = (String) json.get("badgeId");
    		String activityInstanceId = (String) json.get("activityInstanceId");
    		String powerupId = (String) json.get("powerupId");
    		String powerupcnt = (String) json.get("powerupCount");
    		
    		if(badgeId.isEmpty() || activityInstanceId.isEmpty() || powerupId.isEmpty() || powerupcnt.isEmpty())
    		{
    			System.out.println("Invalid request");
            	//The error validation has to go here
    			String errorMessage = badgeId.isEmpty() ? "Badge Id is empty" : activityInstanceId.isEmpty() ? "activity instance id is empty" : powerupId.isEmpty()? "power up id is empty": powerupcnt.isEmpty() ? "power up count is empty": "";
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage(errorMessage));
    			log.info(errorMessage);
    			throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
    		}
    		int powerupCount = -1;
    		try
    		{
    			powerupCount = Integer.parseInt(powerupcnt);
    		}
    		catch(NumberFormatException e)
    		{
    			System.out.println("power up count is not a valid integer");
            	//The error validation has to go here
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("power up count is not a valid integer"));
    			log.info("power up count is not a valid integer");
    			throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
    		}
    		

    		Boolean badgeUsed = false;

    		PatientBadges patientBadge = new PatientBadges(patientPIN, badgeId, activityInstanceId, badgeUsed);

    		if(__modelFactory.checkPatientBadge(patientBadge)){

    			System.out.println("This is an unused badge");

    			// check whether powerup id exists in the system or not

    			Powerups powerup = __modelFactory.getPowerup(powerupId);

    			if(powerup == null){
    				System.out.println("Powerup does not exist in the system");
    	        	//The error validation has to go here
    	        	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Powerup does not exist in the system"));
    				log.info("Powerup does not exist in the system");
    				throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
    			}
    			else{

    				System.out.println("Powerup record found");

    				// need to call the model factory method for activating a patient badge

    				ActivatePatientBadge activatePatientBadge = new ActivatePatientBadge(patientPIN,badgeId,activityInstanceId,true,powerupId, powerupCount);

    				if(__modelFactory.activatePatientBadge(activatePatientBadge)){

    					System.out.println("The badge has been activated for the patient");
    					JSONObject reply = new JSONObject();
    		            reply.put("message", "SUCCESS");
    		            return reply.toString().replace("\\","");
    				}
    				else{
    					System.out.println("The badge could not be activated for the patient and powerups were not allocated");
        	        	//The error validation has to go here
        	        	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The badge could not be activated for the patient and powerups were not allocated"));
        				log.info("The badge could not be activated for the patient and powerups were not allocated");
        				throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
    				}
    			}
    		}
    		else{
    			System.out.println("The badge is not present with the patient or has already been activated");
    			String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The badge is not present with the patient or has already been activated"));
    			throw new NotFoundException(Response.Status.CONFLICT,JsonErrorMessage);
    		}
    	}
    }

    public String updatePatientPowerup(String patientPowerupJSON, String patientPIN) throws Exception{

    	System.out.println("Inside updatePatientPowerup");

    	Patient patient = __modelFactory.getPatient(patientPIN);

    	if(patient == null){
    		System.out.println("PIN does not exist in the system");
        	//The error validation has to go here
        	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The PIN is invalid"));
			log.info("The PIN is invalid");
			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
    	}
    	else{
    		System.out.println("PIN exists in the system");

    		JSONParser parser = new JSONParser();
    		JSONObject json = new JSONObject();

    		try
        	{
    			json = (JSONObject) parser.parse(patientPowerupJSON);
        	}
    		catch(Exception e)
        	{
        		e.printStackTrace();
        		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The JSON is invalid"));
    			log.info("The JSON is invalid");
    			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
        	}

    		System.out.println("The json is::"+json);

    		String powerupId = (String) json.get("powerupId");
    		System.out.println("PowerupID received from json is : " + powerupId);
    		String powerupCnt = (String) json.get("powerupCount");
    		
    		System.out.println("Powerup count received from json is : " + powerupCnt);
    		
    		if(powerupId.isEmpty() || powerupCnt.isEmpty())
    		{
    			System.out.println("Invalid request");
            	//The error validation has to go here
    			String errorMessage = powerupId.isEmpty() ? "power up Id is empty" : powerupCnt.isEmpty() ? "powerup count is empty" : "";
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage(errorMessage));
    			log.info(errorMessage);
    			throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
    		}
    		
    		int powerupCount = -1;
    		
    		try
    		{
    			powerupCount = Integer.parseInt(powerupCnt);
    		}
    		catch(NumberFormatException e)
    		{
    			System.out.println("power up count is not a valid integer");
            	//The error validation has to go here
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("power up count is not a valid integer"));
    			log.info("power up count is not a valid integer");
    			throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
    		}
    		

    		Powerups powerup = __modelFactory.getPowerup(powerupId);

    		if(powerup == null){
    		  System.out.println("powerup record does not exist in the system");
				  //The error validation has to go here
              String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The powerup record does not exist"));
      		  log.info("The powerup record does not exist");
      		  throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
    		}
    		else{
    			System.out.println("Powerup exists in the system");

    			if(powerupCount < 0){
    				powerupCount = 0;
    			}

    			JSONObject reply = new JSONObject();

    			PatientPowerups patientPowerup = __modelFactory.checkPatientPowerup(patientPIN, powerupId);

    			if(patientPowerup == null){
    				System.out.println("No record exists for this patient and powerup combination");
    				System.out.println("Need to insert the patient powerup record in patient_powerups table");

    				PatientPowerups paPow = new PatientPowerups(patientPIN, powerupId, powerupCount);

    				if(__modelFactory.insertPatientPowerup(paPow))
    	    	    {
    	    	    	reply.put("message", "SUCCESS");
    	    	    	return reply.toJSONString();
    	    	    }
    	    	    else
    	    	    {
    	    	    	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Powerup could not be allocated to the patient"));
    	    			log.info("Powerup could not be allocated to the patient");
    	    			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
    	    	    }
    			}else{

    				System.out.println("Patient powerup record exists in the system");
    				System.out.println("Need to update the record with correct count");

    				patientPowerup.setCount(powerupCount);

    				if(__modelFactory.updatePatientPowerup(patientPowerup)){

    					reply.put("message", "SUCCESS");
    	    	    	return reply.toJSONString();
    				}
    				else{

    					String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Powerup could not be updated for the patient"));
    	    			log.info("Powerup could not be updated for the patient");
    	    			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
    				}
    			}
    		}
    	}
    }

    public String getPatientGamePlay(String patientPIN) throws Exception{

		System.out.println("Inside getPatientGamePlay service call");
    	JSONObject reply = new JSONObject();

    	Patient patient =  __modelFactory.getPatient(patientPIN);

    	if(patient == null)
        {
        	System.out.println("PIN does not exist in the system");
        	//The error validation has to go here
        	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The PIN is invalid"));
			log.info("The PIN is invalid");
			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);

        }
    	else{
    		System.out.println("PIN exists in the system");

    		JSONObject patGamePlay = new JSONObject();

    		PatientGamePlay patientGamePlay = __modelFactory.getPatGamePlay(patientPIN);

    		if(patientGamePlay == null){
    			System.out.println("Patient game play does not exist in the system");
    			log.info("Patient game play does not exist in the system");
    			patGamePlay.put("lastPlayedGame", null);
    		}
    		else{
    			System.out.println("PatientGamePlay record exists.. Getting details");

    			patGamePlay.put("lastPlayedGame", patientGamePlay.getGameId());

    			System.out.println("patGamePlay after adding lastPlayedGame is : " + patGamePlay.toJSONString());
    		}

    		System.out.println("Collecting patient powerups information from the system");

    		PatientPowerups patPowerups = __modelFactory.getPatientPowerups(patientPIN);

    		if(patPowerups == null){
    			System.out.println("Patient powerup information does not exist in the system");
    			log.info("Patient powerup information does not exist in the system");
    			patGamePlay.put("powerupInfo", null);
    		}
    		else{
    			System.out.println("Patient powerup record found.. Getting details");
    			JSONObject powerupInfo = new JSONObject();
    			powerupInfo.put("powerupId", patPowerups.getPowerupId());
    			System.out.println("powerupCount: " + patPowerups.getCount());
    			powerupInfo.put("powerupCount", patPowerups.getCount());

    			patGamePlay.put("powerupInfo", powerupInfo);
    			System.out.println("patGamePlay after adding powerupInfo is : " + patGamePlay.toJSONString());

    		}

			reply.put("PatientGamePlay", patGamePlay);
			return reply.toJSONString().replace("\\","");
    	}
    }

    public String insertPatientGamePlay(String patientGamePlayJSON, String patientPIN) throws Exception{

    	System.out.println("Inside insertPatientGamePlay service method");

    	System.out.println("Patient PIN received is: " + patientPIN);

    	Patient patient =  __modelFactory.getPatient(patientPIN);

    	if(patient == null)
        {
        	System.out.println("PIN does not exist in the system");
        	//The error validation has to go here
        	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The PIN is invalid"));
			log.info("The PIN is invalid");
			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);

        }
    	else{

    		JSONParser parser = new JSONParser();
    		JSONObject json = new JSONObject();

    		try
        	{
    			json = (JSONObject) parser.parse(patientGamePlayJSON);
        	}
    		catch(Exception e){
    			e.printStackTrace();
        		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The JSON is invalid"));
    			log.info("The JSON is invalid");
    			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
    		}

    		System.out.println("The json is::"+json);

    		String gameId = (String) json.get("gameId");

    		System.out.println("gameId received is : " + gameId);
    		
    		String startTimestr = (String) json.get("startTime");
    		
    		if(gameId.isEmpty() || startTimestr.isEmpty())
    		{
    			System.out.println("Invalid request");
            	//The error validation has to go here
    			String errorMessage = gameId.isEmpty() ? "Game Id is empty" : startTimestr.isEmpty() ? "start time is empty" : "";
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage(errorMessage));
    			log.info(errorMessage);
    			throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
    		}

    		Timestamp startTime = null;
    		try
    		{
    			startTime = new Timestamp(Long.parseLong(startTimestr));
    		}
    		catch(NumberFormatException e)
    		{
    			System.out.println("start time is not a valid long");
            	//The error validation has to go here
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("start time is not a valid long"));
    			log.info("start time is not a valid long");
    			throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
    		}

    		System.out.println("Start Time received after conversion is : " + startTime);

    		PatientGamePlay patientGamePlay = new PatientGamePlay(patientPIN, gameId, startTime, null);

    		JSONObject reply = new JSONObject();

    		if(__modelFactory.insertPatGamePlay(patientGamePlay)){
    			System.out.println("Record inserted. Return success");
    			reply.put("message","SUCCESS");
    			return reply.toJSONString().replace("\\","");
    		}else{
    			String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Game play stats could not be inserted for the patient"));
    			log.info("Game play stats could not be inserted for the patient");
    			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
    		}

    	}
    }

    public String updatePatientGamePlay(String patientGamePlayJSON, String patientPIN) throws Exception
    {
    	System.out.println("Inside updatePatientGamePlay service method");

    	System.out.println("Patient PIN received is: " + patientPIN);

    	Patient patient =  __modelFactory.getPatient(patientPIN);

    	if(patient == null)
        {
        	System.out.println("PIN does not exist in the system");
        	//The error validation has to go here
        	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The PIN is invalid"));
			log.info("The PIN is invalid");
			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);

        }
    	else{

    		JSONParser parser = new JSONParser();
    		JSONObject json = new JSONObject();

    		try
        	{
    			json = (JSONObject) parser.parse(patientGamePlayJSON);
        	}
    		catch(Exception e){
    			e.printStackTrace();
        		String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The JSON is invalid"));
    			log.info("The JSON is invalid");
    			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
    		}

    		System.out.println("The json is::"+json);

    		String gameId = (String) json.get("gameId");
    		
    		System.out.println("PatientGamePlay update details have been received");
    		
    		String endTimestr = (String) json.get("endTime");
    		
    		if(gameId.isEmpty() || endTimestr.isEmpty())
    		{
    			System.out.println("Invalid request");
            	//The error validation has to go here
    			String errorMessage = gameId.isEmpty() ? "Game Id is empty" : endTimestr.isEmpty() ? "end time is empty" : "";
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage(errorMessage));
    			log.info(errorMessage);
    			throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
    		}
    		
    		Timestamp endTime = null;
    		
    		try
    		{
    			endTime = new Timestamp(Long.parseLong(endTimestr));
    		}
    		catch(NumberFormatException e)
    		{
    			System.out.println("end time is not a valid long");
            	//The error validation has to go here
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("end time is not a valid long"));
    			log.info("end time is not a valid long");
    			throw new NotFoundException(Response.Status.BAD_REQUEST,JsonErrorMessage);
    		}
    				

    		PatientGamePlay patientGamePlay = new PatientGamePlay(patientPIN, gameId, null, endTime);

    		System.out.println("calling the update model factory method");

    		JSONObject reply = new JSONObject();

    		if(__modelFactory.updatePatGamePlay(patientGamePlay)){
    			System.out.println("Record updated. Return success");
    			reply.put("message","SUCCESS");
    			return reply.toJSONString().replace("\\","");
    		}else{
    			String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Game play stats could not be updated for the patient"));
    			log.info("Game play stats could not be updated for the patient");
    			throw new NotFoundException(Response.Status.INTERNAL_SERVER_ERROR,JsonErrorMessage);
    		}
    	}
    }

    public String allocatePatientBadge(String activityInstanceId, String patientPIN) throws Exception{

    	System.out.println("Inside promis service method for allocatePatientBadge");

    	Patient patient = __modelFactory.getPatient(patientPIN);

    	if(patient == null){
    		System.out.println("PIN does not exist in the system");
        	//The error validation has to go here
        	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The PIN is invalid"));
			log.info("The PIN is invalid");
			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
    	}else{

    		System.out.println("Patient exists in the system");

    		// get the activityTitle for the current activityInstanceId passed in the input
    		ActivityInstance activityInstance = __modelFactory.getActivityInstance(activityInstanceId);

    		if(activityInstance == null){
    			System.out.println("activity instance does not exist in the system");
            	//The error validation has to go here
            	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("The activity instance id is invalid"));
    			log.info("The activity instance id is invalid");
    			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
    		}
    		else{

    			String state = activityInstance.getState();
    			String activityTitle = activityInstance.getActivityTitle();

    			if(!(state.equals("completed"))){
    				System.out.println("Activity instance is not in the completed state. Badge cannot be allocated");
                	//The error validation has to go here
                	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Activity instance is not in the completed state. Badge cannot be allocated"));
        			log.info("Activity instance is not in the completed state. Badge cannot be allocated");
        			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
    			}
    			else{

    				// Parse the activityTitle String to check for Weekly or Daily

    				String weeklyType = "Weekly";
    				String dailyType = "Daily";
    				String activityType = null;
    				int activityCompletionCount = 0;

    				if(activityTitle.toLowerCase().contains(weeklyType.toLowerCase())){
    					System.out.println("The activity type is Weekly");
    					activityType = weeklyType;

    					// call the modelFactory method to get the activityCompletion Count for daily surveys

    					activityCompletionCount = __modelFactory.getWeeklyActivityCompletionCount(patientPIN, activityInstanceId);

    					System.out.println("Weekly activity completion count received is : " + activityCompletionCount);

    				}
    				else if(activityTitle.toLowerCase().contains(dailyType.toLowerCase())){
    					System.out.println("The activity type is Daily");
    					activityType = dailyType;
    					Date dailyInstanceStartTime = activityInstance.getStartTime();
    					System.out.println("dailyInstanceStartTime received is : " + dailyInstanceStartTime);
    					// call the modelFactory method to get the activityCompletion Count for daily surveys
    					activityCompletionCount = __modelFactory.getDailyActivityCompletionCount(patientPIN, activityInstanceId, dailyInstanceStartTime);
    					System.out.println("Daily activity completion count received is : " + activityCompletionCount);
    				}
    				JSONObject reply = new JSONObject();

    				if(activityCompletionCount <= 0){
    					System.out.println("This activity instance does not support any badge allocation");
    					reply.put("message", "No badge is allocated for completion of the activity instance");
    					return reply.toJSONString().replace("\\","");
    				}
    				else{
    					// Get the badgeId from the activity_badges table
    					System.out.println("Going for collecting the badgeId from activity_badges");
    					System.out.println("activityType being passed : " + activityType);
    					System.out.println("activityCompletionCount being passed : " + activityCompletionCount);
        				String badgeId = __modelFactory.getActivityBadges(activityType, activityCompletionCount);

        				if(badgeId == null){

        					System.out.println("No badgeId found for completing this activity completion count");
        		        	//The error validation has to go here
        					reply.put("message", "No badgeId found for completing this activity completion count");
        					return reply.toJSONString().replace("\\","");
        				}
        				else{

        					System.out.println("BadgeId found for the completed activity instance record");

        					// Check whether badge exists in the system

        					Badge badge = __modelFactory.getBadge(badgeId);

        					if(badge == null){
        						System.out.println("Badge does not exist in the system");
        	                	//The error validation has to go here
        	                	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Badge does not exist in the system"));
        	        			log.info("Badge does not exist in the system");
        	        			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
        					}
        					else{

        						// Check whether a record exists in the patient_badges table for pin, badgeId and activityInstanceID

        						PatientBadges patientBadge = new PatientBadges(patientPIN, badgeId, activityInstanceId, false);

        						if(__modelFactory.checkPatientBadge(patientBadge)){
        							System.out.println("Badge has been already allocated for this activity instance completion to the patient");
            	                	//The error validation has to go here
            	                	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Badge has been already allocated for this activity instance completion to the patient"));
            	        			log.info("Badge has been already allocated for this activity instance completion to the patient");
            	        			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
        						}
        						else{

        							// insert a new record in patient_badges table for the pin, badgeId and activityInstanceId

        							if(__modelFactory.insertPatientBadge(patientBadge)){
        								System.out.println("Badge allocated successfully to the patient");
        								reply.put("message", "Success");
        	        					return reply.toJSONString().replace("\\","");
        							}
        							else{
        								System.out.println("Badge could not allocated to the patient");
                	                	//The error validation has to go here
                	                	String JsonErrorMessage = mapper.writeValueAsString(new ErrorMessage("Badge could not allocated to the patient"));
                	        			log.info("Badge could not allocated to the patient");
                	        			throw new NotFoundException(Response.Status.NOT_FOUND,JsonErrorMessage);
        							}
        						}
        					}
        				}
    				}
    			}
    		}
    	}
    }
}
