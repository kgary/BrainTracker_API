package edu.asu.epilepsy.apiv30.model;

import java.io.Serializable;

/**
 * This is the POJO for the Question_Option table.
 * @author Deepak S N
 */
public class QuestionOption implements Serializable {
	
	private static final long serialVersionUID = 939394312806436799L;
	
	 private int _questionOptionId;
     private String _optionText;
     private int _optionOrder;
     private int _questionOptionType;
     
     public QuestionOption(int questionOptionId,String optionText,int optionOrder,int questionOptionType)
     {
    	 _questionOptionId = questionOptionId;
    	 _optionText = optionText;
    	 _optionOrder = optionOrder;
    	 _questionOptionType = questionOptionType; 
     }

	public int get_questionOptionId() {
		return _questionOptionId;
	}

	public String get_optionText() {
		return _optionText;
	}

	public int get_optionOrder() {
		return _optionOrder;
	}

	public int get_questionOptionType() {
		return _questionOptionType;
	}
}
