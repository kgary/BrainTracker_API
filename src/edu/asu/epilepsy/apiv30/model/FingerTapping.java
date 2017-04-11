package edu.asu.epilepsy.apiv30.model;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class FingerTapping extends LeafActivity{
	private static final String TAG = FingerTapping.class.getSimpleName();
	
	private String activityId;
	private String patientType;
	

	protected FingerTapping(String ActivityId, String patientType) {
		super(ActivityId);
		System.out.println(TAG + " FingerTapping() :- " + ActivityId + " " + patientType);
		this._activityId = ActivityId;
		this.patientType = patientType;
	}

	public String getActivityId() {
		return activityId;
	}

	public String getPatientType() {
		return patientType;
	}

	@Override
	public String generateJSON() throws JsonProcessingException {
		// TODO Auto-generated method stub	
			System.out.println(TAG + " generateJSON() :- ");
			ObjectMapper mapper = new ObjectMapper();
	         
	        SimpleModule module = new SimpleModule();
	        module.addSerializer(FingerTapping.class,new FingerTappingJSON());
	        mapper.registerModule(module);
	         
	        String serialized = mapper.writeValueAsString(this);
	        System.out.println(TAG + " generateJSON() :- " + serialized);
	        return serialized;
	}
	
	final private class FingerTappingJSON extends JsonSerializer<FingerTapping>{

		@Override
		public void serialize(FingerTapping fingerTapping, JsonGenerator jgen, SerializerProvider provider)
				throws IOException, JsonProcessingException {
			 jgen.writeStartObject();
             jgen.writeStringField("activityBlockId", fingerTapping._activityId);
             jgen.writeBooleanField("isPromisBlock", false);
             jgen.writeStringField("type",fingerTapping.patientType);
             jgen.writeEndObject();
			
		}
		
	}

}
