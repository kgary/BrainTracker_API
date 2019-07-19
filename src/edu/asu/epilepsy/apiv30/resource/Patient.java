/**
 *
 */
package edu.asu.epilepsy.apiv30.resource;

import edu.asu.epilepsy.apiv30.service.PromisService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/patients")
@Produces(MediaType.APPLICATION_JSON)
public class Patient {
	PromisService promis_service = new PromisService(); //Instantiating the promis service.

	/**
	 * @api {GET} /patients/{pin} Get Patient Information
	 * @apiName GetPatientInfo
	 * @apiGroup Patients
	 * @apiVersion 0.0.0
	 * @apiParam {Integer} pin Pin of the patient
	 * @apiDeprecated Method not implemented
	 * @apiSuccess {String} Result null
	 *
	 */
	@GET
	@Path("{pin}")
	public Response getPatient(@PathParam("pin") String pin) {
		return null;
	}

	/**
	 * @api {GET} /patients Get All Patient Information
	 * @apiName GetAllPatientInfo
	 * @apiGroup Patients
	 * @apiVersion 0.0.0
	 * @apiParam {String} trial the trial of the patient
	 * @apiParam {String} compliance the compliance of the patient
	 * @apiParam {String} above the above of the patient
	 * @apiParam {String} includeDeleted the includeDeleted of the patient
	 * @apiParam {String} stage the stage of the patient
	 * @apiDeprecated Method not implemented
	 * @apiSuccess {String} Result null
	 *
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

	/**
	 * @api {POST} /patients/enrollpatient Enroll A New Patient
	 * @apiName EnrollPatient
	 * @apiGroup Patients
	 * @apiVersion 0.0.0
	 * @apiDescription Enroll new patient with provisional data.
	 * @apiExample Example of body:
	 * {
	 *     "patientGroup": "adult",
	 *     "childPin": "2017",
	 *     "deviceType": "android",
	 *     "deviceVersion": null,
	 *     "hydroxureaTablets": "0",
	 *     "isChildOnMed": "1",
	 *     "medDetails": [
	 *         {
	 *             "medicine": "ACTH",
	 *             "prescribedDosage": 2,
	 *             "tablet": 2
	 *         }
	 *     ]
	 * }
	 *
	 * @apiSuccess {JSON} message The message of the operation.
	 *
	 * @apiSuccessExample Example data on Success:
	 * {
	 *     "patientPIN": "4003",
	 *     "message": "SUCCESS"
	 * }
	 */
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
	 * @api {PUT} /patients/{pin} Update A Patient
	 * @apiName UpdatePatient
	 * @apiGroup Patients
	 * @apiVersion 0.0.0
	 * @apiDeprecated Method not implemented
	 *
	 * @apiSuccess {JSON} message The message of the operation.
	 *
	 */
	@PUT
	@Path("{pin}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putPatient(@PathParam("pin") String pin) {
		// update the patient
		return null;
	}

	/**
	 * @api {DELETE} /patients Delete All Patients
	 * @apiName DeleteAllPatients
	 * @apiGroup Patients
	 * @apiVersion 0.0.0
	 * @apiDeprecated Method not implemented
	 */
	@DELETE
	public void deletePatient() {
		// In reality this should just deactivate the patient
	}
}
