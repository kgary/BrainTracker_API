package edu.asu.epilepsy.apiv30.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.asu.epilepsy.apiv30.service.PromisService;

/*
 * Haven't added GET methods to retrieve log info or scored activity data by patient as
 * I'm not quite sure yet which way that part of the API should be navigated.
 * Right now this assumes we only get activities per Patient but that may be wrong. 
 */

/**
 * An ActivityIntance is an instantiated version of an Activity tree. As such is represents
 * something a Patient actually does, including the order in which s/he does it. An instance
 * that is in the queue to be performed by a Patient is in the state Scheduled, and such an
 * instance is either not in its active window yet (can't be completed yet), is in its
 * active window (should get a button on the landing page), or has expired (Patient did not
 * do it in the active window). When the Patient completes the instance it is changed to a
 * "Completed" state. So ActivityInstances have a simple state model.
 * An ActivityInstance is either a single discrete delievred event (like a simple gameplay)
 * or a 1-level "bush" that defines the sequence of delivered events as one unit.
 * @author kevinagary
 *
 */
@Path("/activities/")
@Produces(MediaType.APPLICATION_JSON)
public class ActivityInstance {
	/**
	 * return the activities scheduled for the patient, not exactly sure if we can have 
	 * activities that are not active (expired or in future) but including due to cron job legacy
	 * @param pin
	 * @param isActive a flag that forces return of only those activities currently active
	 * @return
	 */
	
	PromisService promis_service = new PromisService();
	
    @GET
    @Path("/scheduledactivity/")
    public Response getScheduledActivities(
    		@QueryParam("pin") String pin
        ) throws Exception {
    	
        Response response = null;
        String json_string=promis_service.checkActivityInstance(pin);
        
       response = Response.status(Response.Status.OK).entity(json_string).build();
        return response;
    
    }
    
    @GET
    @Path("/activityinstance/{activityInstanceId}")
    public Response getActivityInstance(            
            @PathParam("activityInstanceId") String activityInstanceId,
            @QueryParam("pin") String pin
            ) throws Exception {
    	if(pin != null)
    	{
    		Response response = null;
    		String json_string=promis_service.getActivityInstance(activityInstanceId,pin);
    		
    		response = Response.status(Response.Status.OK).entity(json_string).build();
    		return response;
    	}
    	else 
    	{
    		throw new WebApplicationException(
  			      Response.status(Response.Status.BAD_REQUEST)
  			      .entity("Pin parameter is mandatory")
  			      .build());
    	}
    }
    
    @POST
	@Path("/activityinstanceresult/{activityInstanceId}")
	public Response submitActivity(String content,
			@PathParam("activityInstanceId") String activityInstanceId,
            @QueryParam("pin") String pin
				) throws NumberFormatException, Exception
    {
    	if(pin != null)
    	{
    		
    		Response response=null;
    		String jsonstring=promis_service.submitActivityInstance(content,pin,activityInstanceId);
    		response = Response.status(Response.Status.CREATED)
    					.entity(jsonstring).build();
    		return response;
    	}
    	else
    	{
    		throw new WebApplicationException(
    			      Response.status(Response.Status.BAD_REQUEST)
    			      .entity("name parameter is mandatory")
    			      .build());
    	}
		
    }
    
}
