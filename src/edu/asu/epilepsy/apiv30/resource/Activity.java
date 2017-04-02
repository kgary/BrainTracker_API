package edu.asu.epilepsy.apiv30.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.asu.epilepsy.apiv30.service.PromisService;

/**
 * An Activity represents both discrete tasks a Patient is asked to perform as part of a
 * protocol (or specifically, within a stage of the protocol), and combinations of these
 * tasks into high-order tasks that will be delivered together under some constraints or 
 * schedule. An Activity is a template used to instantiate ActivityInstances.
 * @author kevinagary
 *
 */
@Path("/activity/")
@Produces(MediaType.APPLICATION_JSON)
public class Activity {
	
	PromisService promis_service = new PromisService();//-?|KG-review-080416|kevinagary|c0|
	@POST
	@Path("/scheduleactivity/")
	public Response activityInstance(String content
				) throws NumberFormatException, Exception
    {
    	
		Response response=null;
		String jsonstring=promis_service.cronJob(content);
		response = Response.status(Response.Status.CREATED)
					.entity(jsonstring).build();		
		return response;
    }
	
//
//	@GET
//	@Path("/checkactivity/{pin}")
//	public Response getActivities(
//			@PathParam("pin") String pin
//		) throws Exception {
//		Response response = null;
//		String json_string=promis_service.checkActivityInstance(pin);
//		System.out.println(json_string);
//        response = Response.status(Response.Status.OK).entity(json_string).build();
//		return response;
//	}
//	
//	@GET
//	@Path("/getactivity/{activityId}")
//	public Response getActivity(@PathParam("activityId") String activityId, @QueryParam("patientpin") String patientPIN) throws Exception {
//		Response response = null;
//		 //need to get the patientPIN from the app.
//		System.out.println("Patient Pin at root - "+patientPIN);
//		String json_string=promis_service.getActivity(activityId,patientPIN);
//		System.out.println(json_string);
//        response = Response.status(Response.Status.OK).entity(json_string).build();
//		return response;
//	}
//	/*-|KG-review-080416|kevinagary|c0|?*/
//	@POST
//	@Consumes(MediaType.APPLICATION_JSON)
//	public void postActivity(String payload) {  // need JAXB here
//		// The json we accept should be arbitrarily complex, meaning a single task all
//		// the way up to a complex tree
//	}
}
