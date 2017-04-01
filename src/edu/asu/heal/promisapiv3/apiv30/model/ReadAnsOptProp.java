package edu.asu.heal.promisapiv3.apiv30.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

/**
 * This class is used for reading the question option prop file.
 * The property file is read only once during the loading of the class and,
 * maintained in the hashmap
 * @author Deepak S N
 * 
 */
public class ReadAnsOptProp {
	private static Properties _prop = null;
	public static HashMap<String,String>_questionOption = null;
	//Avoiding loading the prop file every time,we are setting it to a hashMap
	static
	{
		_prop = new Properties();
		_questionOption = new HashMap<String,String>();
		InputStream input = null;

		try 
		{
			input = ReadAnsOptProp.class.getResourceAsStream("questionOption.properties");
			// load a properties file
			_prop.load(input);
			
			if(_prop == null)
				throw new ModelException("Unexpected error while instantiating prop file");
			
			Enumeration e = _prop.propertyNames();
			
			 while (e.hasMoreElements()) 
			 {
			      String key = (String) e.nextElement();
			      
			      _questionOption.put(key,(String) _prop.getProperty(key));
		    } 
		} 
		catch (Exception ex) 
		{
			ex.printStackTrace();
		} 
		finally 
		{
			if (input != null) 
			{
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static String getQuestionOption(String questionOption) throws ModelException
	{
		if(_prop == null &&_questionOption.size() <= 0)
			throw new ModelException("Unexpected error while instantiating question option prop file");
			
		
		return _questionOption.get(questionOption);
	}
	
}
