package edu.asu.epilepsy.apiv30.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * This the Promis Pain Intensity Question Template.
 * This should always be the first Question.
 *
 * @author Deepak S N
 */
public class PainIntensity extends LeafActivity {

  private List<Question> questions = null;
  private HashMap<Integer, String> answerOptions = null;
  private String questionId = null;

  public PainIntensity(String activityId, List<Question> questions, HashMap<Integer, String> answerOptions, String questionId) {
    super(activityId);
    this.questionId = questionId;
    this.questions = questions;
    this.answerOptions = answerOptions;
  }

  public String getQuestionId() {
    return questionId;
  }

  public List<Question> getQuestions() {
    return questions;
  }

  public HashMap<Integer, String> getAnswerOptions() {
    return answerOptions;
  }

  public void setQuestions(List<Question> questions) {
    this.questions = questions;
  }

  public void setAnswerOptions(HashMap<Integer, String> answerOptions) {
    this.answerOptions = answerOptions;
  }

  @Override
  public String generateJSON() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    SimpleModule module = new SimpleModule();
    module.addSerializer(PainIntensity.class, new PainIntensityJSON());
    mapper.registerModule(module);

    String serialized = mapper.writeValueAsString(this);
    return serialized;
  }

  //Inner class for the pain intensity serialization(Jackson mapping).
  final private class PainIntensityJSON extends JsonSerializer<PainIntensity> {
    @Override
    public void serialize(PainIntensity painIntensity, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      JSONParser parser = new JSONParser();
      JSONObject answerOpt = new JSONObject();
      jgen.writeStringField("activityBlockId", painIntensity._activityId);
      jgen.writeBooleanField("isPromisBlock", false);
      jgen.writeArrayFieldStart("questions");
      HashMap<Integer, String> questionOptionMap = painIntensity.getAnswerOptions();

      for (Question qs : painIntensity.getQuestions()) {
        JSONObject quest = new JSONObject();
        quest.put("quesID", qs.getQuestionId());
        quest.put("question", qs.getQuestionStem());
        quest.put("questionType", qs.getQuestionType().name());
        if (questionOptionMap != null) {
          String questionOptions = questionOptionMap.get(Integer.parseInt(qs.getQuestionId()));
          if (questionOptions != null) {
            String[] answerOptions = questionOptions.split(";");
            JSONArray answerOpts = new JSONArray();
            for (String answerOption : answerOptions) {
              try {
                answerOpt = (JSONObject) parser.parse(answerOption);
              } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
              answerOpts.add(answerOpt);
            }
            quest.put("answerOptions", answerOpts);
          }
        }
        jgen.writeObject(quest);
      }


      //jgen.writeStringField("answerOptions", painIntensity.getAnswerOptions());
      jgen.writeEndArray();
      jgen.writeEndObject();
    }
  }
}
