package edu.asu.heal.promisapiv3.apiv30.model;

import java.io.IOException;
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
 * A PROMIS survey represents a single PROMIS form.
 * @author kevinagary
 *
 */
public class PromisSurvey extends LeafActivity {

    private final List<Question> questions;
    private final String random;
    private final HashMap<Integer,String> questionOptionMap;
    
    public PromisSurvey(String ActivityId, List<Question> qs, HashMap<Integer,String> questionOptions, String random) {
        super(ActivityId);
        // populated from the DB, immutable
        questions = qs;
        this.random = random;
        this.questionOptionMap = questionOptions;
    }

    public HashMap<Integer,String> getQuestionOptions() {
        return questionOptionMap;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public String getRandom() {
        return random;
    }

    @Override
    public String generateJSON() throws JsonProcessingException {
        System.out.println("The promis survey generate json called");
        ObjectMapper mapper = new ObjectMapper();
         
        SimpleModule module = new SimpleModule();
        module.addSerializer(PromisSurvey.class, new PromisSurveyJSON());
        mapper.registerModule(module);
         
        String serialized = mapper.writeValueAsString(this);
        return serialized;
    }

    /*@Override
    public ActivityInstance generateActivityInstance() {
        // This implementation should compose the list of questions into a
        // JSON collection string usable for the ActivityInstance. It should
        // randomize the questions in the list if the flag is set.
        return null;
    }*/
    
    //Inner class for the pain intensity serialization(Jackson mapping).
        final private class PromisSurveyJSON extends JsonSerializer<PromisSurvey> 
        {
            @Override
            public void serialize(PromisSurvey promisSurvey, JsonGenerator jgen, SerializerProvider provider)
                    throws IOException, JsonProcessingException {
                jgen.writeStartObject();
                JSONParser parser = new JSONParser();
                JSONObject answerOpt = new JSONObject();
                jgen.writeStringField("activityBlockId", promisSurvey._activityId);
                jgen.writeBooleanField("isPromisBlock", true);
                jgen.writeArrayFieldStart("questions");
                for(Question question : promisSurvey.getQuestions())
                {
                    JSONObject quest = new JSONObject();
                    quest.put("question", question.getQuestionStem());    
                    quest.put("quesID", question.getQuestionId());
                    quest.put("shortForm", question.getShortForm());
                    int questionOptionID = question.getQuestionOptionType();
                    String [] answerOptions = promisSurvey.getQuestionOptions().get(questionOptionID).split(";");
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
                jgen.writeEndArray();
                jgen.writeEndObject();        
            }
        }
}