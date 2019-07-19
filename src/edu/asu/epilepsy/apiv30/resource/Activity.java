package edu.asu.epilepsy.apiv30.resource;

import edu.asu.epilepsy.apiv30.service.PromisService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

	/**
	 * @api {POST} /activity/scheduleactivity Schedule New Activity
	 * @apiName ScheduleActivity
	 * @apiGroup Activity
	 * @apiVersion 0.0.0
	 * @apiDescription Create a new activity instance for a specific patient, designated by pin. 
	 * 				   May optionally include activity-specific parameters, which are not verified.
	 * @apiExample Example Flanker:
	 * {
	 *    "pin":"1009",
	 *    "parentactivity":"FLANKER",
	 *    "trial_type":"EPILEPSY",
	 *    "parameters":{
	 *       "noOfQuestions":"20",
	 *       "pauseTime":"5"
	 *    }
	 * }
	 *
	 * @apiExample Example Pattern Comparison:
	 * {
	 *    "pin":"1009",
	 *    "parentactivity":"PATTERNCOMPARISON",
	 *    "trial_type":"EPILEPSY",
	 *    "parameters":{
	 *       "maxTime":"90",
	 *       "newImages":"false",
	 *       "numQuestions":"130",
	 *       "ratio":"50",
	 *       "bound":"5"
	 *    }
	 * }
	 *
	 * @apiExample Example Finger Tapping:
	 * {
	 *    "pin":"1009",
	 *    "parentactivity":"FINGERTAPPING",
	 *    "trial_type":"EPILEPSY",
	 *    "parameters":{
	 *       "maxTrials":"10",
	 *       "consecTrials":"2",
	 *       "maxDiff":"5",
	 *       "trialTime":"5",
	 *       "twoHands":"true"
	 *    }
	 * }
	 *
	 * @apiExample Example Spatial Span:
	 * {
	 *    "pin":"1009",
	 *    "parentactivity":"SPATIALSPAN",
	 *    "trial_type":"EPILEPSY",
	 *    "parameters":{
	 *       "maxDifficulty":"10",
	 *       "maxTrialCount":"2",
	 *       "maxCorrectSoFar":"1",
	 *       "lightUpTime":"2"
	 *    }
	 * }
	 *
	 * @apiSuccess {JSON} message The success response message.
	 *
	 * @apiSuccessExample Example success response message:
	 * {
	 *     "message": "SUCCESS"
	 * }
	 *
	 * @apiError (Error 404) UserNotFound The PIN is invalid
	 * @apiErrorExample {json} UserNotFound:
	 * {
	 *     "developerMessage": null,
	 *     "message": "The PIN is invalid",
	 *     "code": 0,
	 *     "status": 0
	 * }
	 *
	 * @apiError (Error 404) ParentActivityNotFound ACTIVITY does not exist.
	 * @apiErrorExample {json} ParentActivityNotFound:
	 * {
	 *     "developerMessage": null,
	 *     "message": "Activity does not exist",
	 *     "code": 0,
	 *     "status": 0
	 * }
	 *
	 * @apiError (Error 404) TrialTypeNotFound The TRIAL TYPE does not exist.
	 * @apiErrorExample {json} TrialTypeNotFound:
	 * {
	 *     "developerMessage": null,
	 *     "message": "The trial type does not exists",
	 *     "code": 0,
	 *     "status": 0
	 * }
	 *
	 * @apiError (Error 500) JsonError The JSON is invalid
	 * @apiErrorExample {json} JsonError:
	 *    {
	 *     "developerMessage": "[stack trace]",
	 *     "message": null,
	 *     "code": 500,
	 *     "status": 500
	 * }
	 *
	 */
	@POST
	@Path("/scheduleactivity/")
	public Response activityInstance(String content) throws NumberFormatException, Exception
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
