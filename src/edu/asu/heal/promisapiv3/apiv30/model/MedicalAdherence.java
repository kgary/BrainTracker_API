/**
 * 
 */
package edu.asu.heal.promisapiv3.apiv30.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;


/**
 * This class is the model for medical adherence.
 * @author Deepak S N
 *
 */
public class MedicalAdherence extends LeafActivity{
	
	 	private  List<Question> _questions = null;
	    private  HashMap<Integer,String> _questionOptions = null;
	    private ArrayList<MedicationInfo> _medicationInfo = null;
	    private QuestionOption _otherOption = null;

    public MedicalAdherence(String activityId)
    {
    	super(activityId);
    }
    	
	public MedicalAdherence(String activityId,List<Question> question, HashMap<Integer,String> questionOptions,ArrayList<MedicationInfo> medicationInfo,QuestionOption otherOption) {
		super(activityId);
		_questions = question;
		_questionOptions = questionOptions;
		_medicationInfo = medicationInfo;
		_otherOption = otherOption;
		// TODO Auto-generated constructor stub
	}

	public List<Question> getQuestions() {
		return _questions;
	}

	public HashMap<Integer,String> getQuestionOptions() {
		return _questionOptions;
	}
	
	public QuestionOption getOtherOption()
	{
		return _otherOption;
	}

	@Override
	public String generateJSON() throws JsonProcessingException {
		
        ObjectMapper mapper = new ObjectMapper();
         
        SimpleModule module = new SimpleModule();
        module.addSerializer(MedicalAdherence.class, new MedicalAdherenceJSON());
        mapper.registerModule(module);
         
        String serialized = mapper.writeValueAsString(this);
        return serialized;
	}
	
    //Inner class for the pain intensity serialization(Jackson mapping).
    final private class MedicalAdherenceJSON extends JsonSerializer<MedicalAdherence> 
    {
        @Override
        public void serialize(MedicalAdherence medicalAdherence, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            JSONParser parser = new JSONParser();
            JSONObject answerOpt = new JSONObject();
            jgen.writeStringField("activityBlockId", medicalAdherence._activityId);
            jgen.writeBooleanField("isPromisBlock", false);
            jgen.writeArrayFieldStart("questions");
            StringBuilder answerText = new StringBuilder();
            for(Question question : medicalAdherence.getQuestions())
            {
            	if(question.getQuestionOptionType()!=-1)
            	{
                JSONObject quest = new JSONObject();
                quest.put("question", question.getQuestionStem());    
                quest.put("quesID", question.getQuestionId());
                String [] answerOptions = _questionOptions.get(question.getQuestionOptionType()).split(";");
                quest.put("questionType", "multiChoiceSingleAnswer");
                JSONArray answerOpts = new JSONArray();
                for(String answerOption : answerOptions)
                {
                    try {
                        answerOpt = (JSONObject) parser.parse(answerOption);
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    answerOpts.add(answerOpt);
                }
                quest.put("answerOptions", answerOpts);
                //jgen.writeStringField("answerOptions", promisSurvey.getQuestionOption());
                jgen.writeObject(quest);
            	}
            	else
            	{
            		JSONObject quest = new JSONObject();
                    quest.put("question", question.getQuestionStem());    
                    quest.put("quesID", question.getQuestionId());
                    //String [] answerOptions = _questionOptions.get(question.getQuestionOptionType()).split(";");
                    quest.put("questionType", question.getQuestionType());
                    JSONArray answerOpts = new JSONArray();
                    for(MedicationInfo medication : _medicationInfo)
                    {
                    	JSONObject medicationInfoAnswerOpt = new JSONObject();
                    	answerText.setLength(0);
                    	answerText.append(medication.getMedicationName());
                    	answerText.append("-");
                    	answerText.append(medication.getPrescribedDosage());
                    	answerText.append(medication.getUnits());
                    	medicationInfoAnswerOpt.put("answerText", answerText.toString());
                    	medicationInfoAnswerOpt.put("answerID", medication.getQuestionOptionId());
                    	medicationInfoAnswerOpt.put("units",medication.getUnits());
	                    JSONArray dosage = new JSONArray();
	                    for(int i=0;i<=(medication.getNoOfTablets() * 3);i++)
	                    {
	                    	if(i == medication.getNoOfTablets())
	                    		dosage.add(Integer.toString(i) + "*");
	                    	else if(i < medication.getNoOfTablets() * 3)	                    	
	                    		dosage.add(Integer.toString(i));
	                    	else
	                    		dosage.add(Integer.toString(i) + " or more"); //This is for the one or more option in the dropdown.		
	                    }
	                     
	                    	
	                    medicationInfoAnswerOpt.put("dosage", dosage);
                        answerOpts.add(medicationInfoAnswerOpt);
                        //answerOpt.clear();
                    }
                    JSONObject oterOption = new JSONObject();
                    oterOption.put("answerText", getOtherOption().get_optionText());
                    oterOption.put("answerID", getOtherOption().get_questionOptionId());
                    answerOpts.add(oterOption);
                    quest.put("answerOptions", answerOpts);
                    //jgen.writeStringField("answerOptions", promisSurvey.getQuestionOption());
                    jgen.writeObject(quest);
            	}
            }
            jgen.writeEndArray();
            jgen.writeEndObject();        
        }
    }
    public class MedicationInfo
    {
    	private String _medicationName;
		private int _prescribedDosage;
		private int _noOfTablets;
		private int _defaultDosage;
		private String _units;
		private int _questionOptionId;
		
		public MedicationInfo(String medicationName,int defaultDosage,int noOfTablets,int prescribedDosage,String units,int questionOptionId)
		{
			_medicationName = medicationName;
			_prescribedDosage = prescribedDosage;
			_defaultDosage = defaultDosage;
			_noOfTablets = noOfTablets;
			_units = units;
			_questionOptionId = questionOptionId;
		}
		
		public String getMedicationName() {
			return _medicationName;
		}
		
		public int getDefaultDosage() {
			return _defaultDosage;
		}
		
		public int getPrescribedDosage() {
			return _prescribedDosage;
		}
		
		public int getNoOfTablets() {
			return _noOfTablets;
		}
		
		public String getUnits() {
			return _units;
		}
		
		public int getQuestionOptionId() {
			return _questionOptionId;
		}
    }
}
