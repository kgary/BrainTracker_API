package edu.asu.epilepsy.apiv30.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;


public class SpatialSpan extends LeafActivity {
  private static final String TAG = SpatialSpan.class.getSimpleName();

  private String activityId;

  protected SpatialSpan(String ActivityId) {
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
    module.addSerializer(SpatialSpan.class, new SptialSpanJSON());
    mapper.registerModule(module);

    String serialized = mapper.writeValueAsString(this);
    System.out.println(TAG + " generateJSON() :- " + serialized);
    return serialized;
  }


  final private class SptialSpanJSON extends JsonSerializer<SpatialSpan> {

    @Override
    public void serialize(SpatialSpan spatialSpan, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeStringField("activityBlockId", spatialSpan._activityId);
      jgen.writeBooleanField("isPromisBlock", false);
      jgen.writeEndObject();

    }


  }

}
