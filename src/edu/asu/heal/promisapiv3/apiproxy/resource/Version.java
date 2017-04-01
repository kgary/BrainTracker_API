package edu.asu.heal.promisapiv3.apiproxy.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.asu.heal.promisapiv3.apiproxy.service.PromisService;

/**
 * This is the version endpoint, which will send the compatible api version to the app version.
 * @author Deepak S N
 */
@Path("/version/")
@Produces(MediaType.APPLICATION_JSON)
public class Version {
	/**
	 * return the activities scheduled for the patient, not exactly sure if we can have 
	 * activities that are not active (expired or in future) but including due to cron job legacy
	 * @param pin
	 * @param isActive a flag that forces return of only those activities currently active
	 * @return
	 */
	/*private static HashMap<String,String> appToAPIVersion = new HashMap<String,String>();
	static
	{
		appToAPIVersion.put("2-1.1.11072016-2-3.0-Android", "apiv31");
		appToAPIVersion.put("2-1.1.11072016-2-3.0-ios", "apiv3");
		appToAPIVersion.put("3.0","apiv30");
		appToAPIVersion.put("3.1","apiv31");
		appToAPIVersion.put("1.0-9-3.0-iOS", "apiv31");
		appToAPIVersion.put("", "apiv31");
	}*/
	
	PromisService promis_service = new PromisService();
	
    @GET
    @Path("/apiversion/")
    public Response getAPIVersion(
    		@Context HttpHeaders headers,
    		@Context UriInfo uriInfo
        ) throws Exception {
    	
        Response response = null;
        String appVersion = "";
       
    	
        if(headers.getRequestHeader("version") != null)
        {
        	appVersion = headers.getRequestHeader("version").get(0);
        	System.out.println("The app version is::"+appVersion);
        }
        
        String apiVersionJSON = promis_service.getAPIVersion(appVersion,headers,uriInfo);
       
       response = Response.status(Response.Status.OK).entity(apiVersionJSON).build();

        return response;
    }
}
