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
public class ComputerAdaptiveTesting extends LeafActivity{
	
	 	private  List<Question> _questions = null;
	    private  HashMap<Integer,String> _questionOptions = null;
	    private HashMap<Integer,ArrayList<Integer>> _catQuestionMapping = null;

	public ComputerAdaptiveTesting(String activityId,List<Question> question, HashMap<Integer,String> questionOptions,HashMap<Integer,ArrayList<Integer>> catQuestionMapping) {
		super(activityId);
		_questions = question;
		_questionOptions = questionOptions;
		_catQuestionMapping = catQuestionMapping;
		// TODO Auto-generated constructor stub
	}

	public List<Question> getQuestions() {
		return _questions;
	}

	public HashMap<Integer,String> getQuestionOptions() {
		return _questionOptions;
	}

	@Override
	public String generateJSON() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
         
        SimpleModule module = new SimpleModule();
        module.addSerializer(ComputerAdaptiveTesting.class, new ComputerAdaptiveTestingJSON());
        mapper.registerModule(module);
         
        String serialized = mapper.writeValueAsString(this);
        return serialized;
	}
	
    //Inner class for the pain intensity serialization(Jackson mapping).
    final private class ComputerAdaptiveTestingJSON extends JsonSerializer<ComputerAdaptiveTesting> 
    {
        @Override
        public void serialize(ComputerAdaptiveTesting computerAdaptiveTesting, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            JSONParser parser = new JSONParser();
            JSONObject answerOpt = new JSONObject();
            jgen.writeStringField("activityBlockId", computerAdaptiveTesting._activityId);
            jgen.writeBooleanField("isPromisBlock", false);
            jgen.writeArrayFieldStart("questions");
            ArrayList<Integer>nestedQuestionID = new ArrayList<Integer>();
            for(Question question : computerAdaptiveTesting.getQuestions())
            {
                JSONObject quest = new JSONObject();
                quest.put("question", question.getQuestionStem());
                String questionID = question.getQuestionId();
                if(nestedQuestionID.contains(Integer.parseInt(questionID))) //Can be a ternary operator,but i am lazy to do it.
                {
                	quest.put("isMandatory",false);
                }
                else
                {
                	quest.put("isMandatory",true);
                }
                quest.put("quesID", questionID);
                int questionOptionId = question.getQuestionOptionType();
                String [] answerOptions = computerAdaptiveTesting.getQuestionOptions().get(questionOptionId).split(";");
                String questionType = question.getQuestionType().name().equals("MCSA")?"multiChoiceSingleAnswer":question.getQuestionType().name();
                quest.put("questionType", questionType);
                
                JSONArray answerOpts = new JSONArray();
                for(String answerOption : answerOptions)
                {
                    try {
                        answerOpt = (JSONObject) parser.parse(answerOption);
                        String answerText = (String) answerOpt.get("answerText");
                        int QuestionID = Integer.parseInt(questionID);
                    	ArrayList<Integer> catQuestionMapping = _catQuestionMapping.get(QuestionID);
                    	//System.out.println("cat map size::"+catQuestionMapping.size());
                    	if(catQuestionMapping != null && catQuestionMapping.size() >0)
                    	{
                    		quest.put("isNested",true);
                    		if(answerText != null && !answerText.isEmpty() && answerText.equals("0"))
                            {
                            	answerOpt.put("nextQuestion", catQuestionMapping.get(1));
                            }
                            else
                            {
                            	nestedQuestionID.add(catQuestionMapping.get(0));
                            	answerOpt.put("nextQuestion", catQuestionMapping.get(0));
                            }
                    	}
                    	else
                    	{
                    		quest.put("isNested",false);
                    	}
                        
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
            jgen.writeEndArray();
            jgen.writeEndObject();        
        }
    }

}
