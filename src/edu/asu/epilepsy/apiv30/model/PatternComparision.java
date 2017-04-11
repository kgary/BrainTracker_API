package edu.asu.epilepsy.apiv30.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PatternComparision extends LeafActivity{

	protected PatternComparision(String ActivityId) {
		super(ActivityId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String generateJSON() throws JsonProcessingException {
		// TODO Auto-generated method stub
		return null;
	}

	final private class PatternComparisionJSON extends JsonSerializer<PatternComparision>{

		@Override
		public void serialize(PatternComparision arg0, JsonGenerator arg1, SerializerProvider arg2)
				throws IOException, JsonProcessingException {
			// TODO Auto-generated method stub
			
		}
		
	}

}
