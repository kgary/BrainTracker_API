/**
 *
 */
package edu.asu.heal.promisapiv3.apiv31.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.asu.heal.promisapiv3.apiv31.service.PromisService;

@Path("/patients")
@Produces(MediaType.APPLICATION_JSON)
public class Patient {
	PromisService promis_service = new PromisService(); //Instantiating the promis service.
	/**
	 * Fetches an individual Patient by PIN
	 * @param pin
	 * @return
	 */
	@GET
	@Path("{pin}")
	public Response getPatient(@PathParam("pin") String pin) {
		return null;
	}

	/**
	 * Not fully specified. An example showing possible filter params on a collection of patients
	 * @param trial
	 * @param compliance ask for patients above or below a given compliance level
	 * @param used in conjunction with previous param
	 * @param includeDeleted should we include deactivated patients?
	 * @param stage
	 * @return
	 */
	@GET
	public Response getPatients(
			@QueryParam("trial") int trial,
			@QueryParam("compliance") float complianceLevel,
			@DefaultValue("false") @QueryParam("above") boolean above,
			@QueryParam("includeDeleted") boolean includeDeleted,
			@QueryParam("stage") int stage) {
		return null;
	}

	@POST
	@Path("/enrollpatient")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postPatient(String patientJSON) throws Exception {
		// set the Location header if the patient was created
		System.out.println("Enrolling patients");
		Response response=null;
		String jsonstring=promis_service.enrollPatients(patientJSON);
		response = Response.status(Response.Status.CREATED)
					.entity(jsonstring).build();
		return response;
	}

	/**
	 * Normally we'd do PATCH not PUT on a Patient but Jersey doesn't support PATCH out of the box
	 * @param pin
	 * @return
	 */
	@PUT
	@Path("{pin}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putPatient(@PathParam("pin") String pin) {
		// update the patient
		return null;
	}

	/**
	 * Patients are never fully deleted, they are only deactivated
	 */
	@DELETE
	public void deletePatient() {
		// In reality this should just deactivate the patient
	}

	@GET
	@Path("/badges/")
	public Response getPatientBadges(@QueryParam("pin") String pin) throws Exception{
		if(pin != null)
    	{
			Response response = null;
    		String json_string=promis_service.getPatientBadges(pin);

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
	@Path("/badges/activate/")
	public Response activatePatientBadge(
			@QueryParam("pin") String pin,
			String patientBadgeJson) throws Exception{
		if(pin != null){
			System.out.println("Activating a patient badge");
			Response response = null;
			String json_string=promis_service.activatePatientBadge(patientBadgeJson, pin);

			response = Response.status(Response.Status.OK).entity(json_string).build();
			return response;
		}
		else{
			throw new WebApplicationException(
	  			      Response.status(Response.Status.BAD_REQUEST)
	  			      .entity("Pin parameter is mandatory")
	  			      .build());
		}
	}

	@POST
	@Path("/game/powerups/")
	public Response updatePatientPowerup(
			@QueryParam("pin") String pin,
			String patientPowerupJSON) throws Exception{
		System.out.println("Allocating powerups to the patient");
		Response response=null;
		String jsonstring=promis_service.updatePatientPowerup(patientPowerupJSON, pin);
		response = Response.status(Response.Status.CREATED)
					.entity(jsonstring).build();
		return response;
	}

	@GET
	@Path("/game/stats/")
	public Response getPatientGamePlay(
			@QueryParam("pin") String pin) throws Exception{
		System.out.println("Inside GET patient game stats call");
		if(pin != null)
    	{
			Response response = null;
    		String json_string=promis_service.getPatientGamePlay(pin);

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
	@Path("/game/stats/start/")
	public Response postPatientGameStats(
			@QueryParam("pin") String pin,
			String patientGamePlayJSON
			) throws Exception{

		System.out.println("Inside POST game stats for patient call");
		if(pin != null)
		{
			Response response = null;
			String json_string=promis_service.insertPatientGamePlay(patientGamePlayJSON, pin);
			response = Response.status(Response.Status.OK).entity(json_string).build();
			return response;
		}
		else{
			throw new WebApplicationException(
	  			      Response.status(Response.Status.BAD_REQUEST)
	  			      .entity("Pin parameter is mandatory")
	  			      .build());
		}
	}

	@POST
	@Path("/game/stats/end/")
	public Response updatePatientGameStats(
			@QueryParam("pin") String pin,
			String patientGamePlayJSON
			) throws Exception{

		System.out.println("Inside update game stats for patient call");
		if(pin != null)
		{
			Response response = null;
			String json_string=promis_service.updatePatientGamePlay(patientGamePlayJSON, pin);
			response = Response.status(Response.Status.OK).entity(json_string).build();
			return response;
		}
		else{
			throw new WebApplicationException(
	  			      Response.status(Response.Status.BAD_REQUEST)
	  			      .entity("Pin parameter is mandatory")
	  			      .build());
		}
	}

	@POST
	@Path("/badges/allocate/{activityInstanceId}")
	public Response allocatePatientBadge(
			@PathParam("activityInstanceId") String activityInstanceId,
			@QueryParam("pin") String pin) throws Exception{

		System.out.println("Inside allocate badge to patient");
		if(pin != null){
			Response response = null;
			String json_string = promis_service.allocatePatientBadge(activityInstanceId, pin);
			response = Response.status(Response.Status.OK).entity(json_string).build();
			return response;
		}else{
			throw new WebApplicationException(
	  			      Response.status(Response.Status.BAD_REQUEST)
	  			      .entity("Pin parameter is mandatory")
	  			      .build());
		}
	}
}
