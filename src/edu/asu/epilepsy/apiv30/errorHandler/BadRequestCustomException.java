package edu.asu.epilepsy.apiv30.errorHandler;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class BadRequestCustomException extends WebApplicationException implements ExceptionMapper<BadRequestCustomException> {
	
	/**
	 * This class is the exception handler for a Bad request.
	 * @author Deepak S N
	 */

	private static final long serialVersionUID = 1L;
	

	public BadRequestCustomException() {
		super("This is a bad request.");
	}

	public BadRequestCustomException(String errorJSON) {
		super(Response.status(400).entity(errorJSON).type("application/json").build());
	}

	@Override
	public Response toResponse(BadRequestCustomException exception) 
	{
		return Response.status(400).entity(exception.getMessage())
				.type("application/json").build();
	} 

}
