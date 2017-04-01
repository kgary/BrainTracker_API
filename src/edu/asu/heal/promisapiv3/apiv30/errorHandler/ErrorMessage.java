package edu.asu.heal.promisapiv3.apiv30.errorHandler;

/**
 * This class is the POJO for the Runtime Exception
 * The Class is used by Jackson for Object Mapping
 * @author Deepak S N
 *
 */
public class ErrorMessage {

	private String developerMessage;
	private String message;
	private int code;
	private int status;

	public ErrorMessage() {}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ErrorMessage(String errorMessage)
	{
		//this.errorCode = errorCode;
		this.message = errorMessage;
	}

	public void setDeveloperMessage(String developerMessage) {
		this.developerMessage = developerMessage;
	}

	public void setCode(int code) {
		this.code = code;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}

	public String getDeveloperMessage() {
		return developerMessage;
	}

	public int getCode() {
		return code;
	}
}
