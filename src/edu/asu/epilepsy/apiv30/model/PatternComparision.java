package edu.asu.epilepsy.apiv30.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class PatternComparision extends LeafActivity{
	
	private static final String TAG = PatternComparision.class.getSimpleName();
	private String activityID;

	protected PatternComparision(String ActivityId) {
		super(ActivityId);
		// TODO Auto-generated constructor stub
		this.activityID = ActivityId;
	}
	
	public String getActivityID(){
		return activityID;
	}
	
	@Override
	public String generateJSON() throws JsonProcessingException {
		// TODO Auto-generated method stub
		System.out.println(TAG + " generateJSON() :- ");
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		
		module.addSerializer(PatternComparision.class,new PatternComparisionJSON());
		
		mapper.registerModule(module);
        
		String serialized = mapper.writeValueAsString(this);
		

		return serialized;
	}

	final private class PatternComparisionJSON extends JsonSerializer<PatternComparision>{

		@Override
		public void serialize(PatternComparision patternComparision, JsonGenerator jgen, SerializerProvider arg2)
				throws IOException, JsonProcessingException {
			// TODO Auto-generated method stub
			jgen.writeStartObject();
            jgen.writeStringField("activityBlockId", patternComparision._activityId);
            jgen.writeEndObject();
		}
		
	}

}
