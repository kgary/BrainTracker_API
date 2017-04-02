package edu.asu.epilepsy.apiv30.errorHandler;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class NotFoundException extends WebApplicationException implements ExceptionMapper<NotFoundException> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotFoundException() {
		super("Not Found");
	}

	public NotFoundException(Response.Status errorcode,String errorJSON) {
		super(Response.status(errorcode).entity(errorJSON).type("application/json").build());
	}

	@Override
	public Response toResponse(NotFoundException e) {
		return Response.status(404).entity(e.getMessage())
				.type("application/json").build();
	}

}
