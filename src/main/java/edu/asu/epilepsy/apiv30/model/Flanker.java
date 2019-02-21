package edu.asu.epilepsy.apiv30.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;


public class Flanker extends LeafActivity {
  private static final String TAG = Flanker.class.getSimpleName();
  private String activityId;

  protected Flanker(String ActivityId) {
    super(ActivityId);
    // TODO Auto-generated constructor stub
    this._activityId = ActivityId;
  }

  public String getActivityId() {
    return activityId;
  }

  @Override
  public String generateJSON() throws JsonProcessingException {
    System.out.println(TAG + " generateJSON() :- ");
    ObjectMapper mapper = new ObjectMapper();

    SimpleModule module = new SimpleModule();
    module.addSerializer(Flanker.class, new FlankerJSON());
    mapper.registerModule(module);

    String serialized = mapper.writeValueAsString(this);
    System.out.println(TAG + " generateJSON() :- " + serialized);
    return serialized;
  }


  final private class FlankerJSON extends JsonSerializer<Flanker> {

    @Override
    public void serialize(Flanker flanker, JsonGenerator jgen, SerializerProvider arg2)
      throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeStringField("activityBlockId", flanker._activityId);
      jgen.writeBooleanField("isPromisBlock", false);
      jgen.writeEndObject();

    }
  }
}

	
