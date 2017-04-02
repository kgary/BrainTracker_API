package edu.asu.epilepsy.apiv30.model;

import com.fasterxml.jackson.core.JsonProcessingException;

public class FingerTapping extends LeafActivity{

	protected FingerTapping(String ActivityId) {
		super(ActivityId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String generateJSON() throws JsonProcessingException {
		// TODO Auto-generated method stub
		
		System.out.println("The Finger Tapping generate json called");
		return null;
	}

}
