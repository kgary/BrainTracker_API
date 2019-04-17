package edu.asu.epilepsy.apiv30.resource;

import edu.asu.epilepsy.apiv30.service.PromisService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

	/**
	 * @api {GET} /activities/scheduledactivity Get All Scheduled Activities
	 * @apiName GetScheduledActivities
	 * @apiGroup ActivityInstance
	 * @apiVersion 0.0.0
	 * @apiDescription View all Scheduled Activities for a specific patient, designated by pin.
	 * @apiParam {Integer} pin Pin of the patient
	 *
	 * @apiSuccess {JSON} Result List of existing activities for the patient with matching pin.
	 *
	 * @apiSuccessExample Example data on Success:
	 * {
	 *     "activities": [
	 *         {
	 *             "sequence": [
	 *                 "SPATIALSPAN"
	 *             ],
	 *             "activityTitle": "Epilepsy Weekly Survey",
	 *             "nextDueAt": "Thu Apr 18 04:59:00 PDT 2019",
	 *             "description": "Weekly Activity To be completed for Epilepsy disease patients",
	 *             "activityInstanceID": "681",
	 *             "state": "pending",
	 *             "parameters": {
	 *                 "lightUpTime": "2",
	 *                 "maxDifficulty": "10",
	 *                 "maxCorrectSoFar": "1",
	 *                 "maxTrialCount": "2"
	 *             }
	 *         },
	 *         {
	 *             "sequence": [
	 *                 "PATTERNCOMPARISON"
	 *             ],
	 *             "activityTitle": "Epilepsy Weekly Survey",
	 *             "nextDueAt": "Thu Apr 18 04:59:00 PDT 2019",
	 *             "description": "Weekly Activity To be completed for Epilepsy disease patients",
	 *             "activityInstanceID": "682",
	 *             "state": "pending",
	 *             "parameters": {
	 *                 "numQuestions": "20",
	 *                 "maxTime": "90",
	 *                 "newImages": "false",
	 *                 "ratio": "50"
	 *             }
	 *         },
	 *         {
	 *             "sequence": [
	 *                 "FLANKER"
	 *             ],
	 *             "activityTitle": "Epilepsy Weekly Survey",
	 *             "nextDueAt": "Thu Apr 18 04:59:00 PDT 2019",
	 *             "description": "Weekly Activity To be completed for Epilepsy disease patients",
	 *             "activityInstanceID": "683",
	 *             "state": "pending",
	 *             "parameters": {
	 *                 "pauseTime": "5",
	 *                 "noOfQuestions": "20"
	 *             }
	 *         },
	 *         {
	 *             "sequence": [
	 *                 "FINGERTAPPING"
	 *             ],
	 *             "activityTitle": "Epilepsy Weekly Survey",
	 *             "nextDueAt": "Thu Apr 18 04:59:00 PDT 2019",
	 *             "description": "Weekly Activity To be completed for Epilepsy disease patients",
	 *             "activityInstanceID": "684",
	 *             "state": "pending",
	 *             "parameters": {
	 *                 "maxDiff": "5",
	 *                 "twoHands": "true",
	 *                 "trialTime": "5",
	 *                 "consecTrials": "5",
	 *                 "maxTrials": "10"
	 *             }
	 *         }
	 *     ],
	 *     "enhancedContent": false,
	 *     "message": "SUCCESS"
	 * }
	 *
	 * @apiError (Error 404) UserNotFound The PIN is invalid
	 * @apiError (Error 500) JsonError The JSON is invalid
	 * @apiErrorExample {json} Error response:
	 *    {
	 *     "developerMessage": null,
	 *     "message": "The PIN is invalid",
	 *     "code": 0,
	 *     "status": 0
	 *    }
	 */
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

	/**
	 * @api {GET} /activities/activityinstance/{activityInstanceId} Get Activity status
	 * @apiName UpdateActivityState
	 * @apiGroup ActivityInstance
	 * @apiVersion 0.0.0
	 * @apiDescription Get the status of a specific Activity and set to in progress.
	 *
	 * @apiParam {Integer} activityInstanceId activity instance's Id
	 * @apiParam {Integer} pin Pin of the patient
	 * @apiSuccess {JSON} Result Result of the activity
	 * @apiSuccessExample Example data on Success:
	 * {
	 *     "mandatoryBlocks": null,
	 *     "sequence": [
	 *         "PATTERNCOMPARISON"
	 *     ],
	 *     "activitySequence": [
	 *         {
	 *             "activityBlockId": "PATTERNCOMPARISON"
	 *         }
	 *     ],
	 *     "activityName": "Epilepsy Weekly Survey",
	 *     "showGame": true,
	 *     "activityInstanceState": "in progress",
	 *     "startTime": "Tue Apr 16 01:00:45 PDT 2019",
	 *     "activityInstanceId": "682",
	 *     "endTime": "Thu Apr 18 04:59:00 PDT 2019",
	 *     "message": "SUCCESS"
	 * }
	 *
	 * @apiError (Error 404) UserNotFound The PIN is invalid
	 * @apiError (Error 400) BadRequest Pin parameter is mandatory
	 * @apiError (Error 409) InputMismatch ActivityInstance does not match Pin
	 * @apiError (Error 500) JsonError The JSON is invalid
	 * @apiErrorExample {json} UserNotFound:
	 * {
	 *  "developerMessage": null,
	 *  "message": "The PIN is invalid",
	 *  "code": 0,
	 *  "status": 0
	 * }
	 * @apiErrorExample {json} InputMismatch:
	 * {
	 *     "developerMessage": null,
	 *     "message": "The Activity Instance ID is not for the given Patient Pin.",
	 *     "code": 0,
	 *     "status": 0
	 * }
	 * @apiErrorExample {txt} BadRequest:
	 * Pin parameter is mandatory
	 */
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

	/**
	 * @api {POST} /activities/activityinstanceresult/{activityInstanceId} Submit Activity Results
	 * @apiName PostActivityInstanceResult
	 * @apiGroup ActivityInstance
	 * @apiVersion 0.0.0
	 * @apiDescription Submitting an Activity's result.
	 * @apiParam {Integer} activityInstanceId activity instance's Id
	 * @apiParam {Integer} pin Pin of the patient
	 *
	 * @apiExample Example of PATTERNCOMPARISON:
	 * {
	 *    "activityInstanceID":"404",
	 *    "timeStamp":1554826423134,
	 *    "preTaskSurvey":{
	 *       "task":"Pre-task Survey",
	 *       "HadSeizure":false,
	 *       "Recent Seizure info":[
	 *
	 *       ],
	 *       "MissedMedications":true,
	 *       "NewMedication":false,
	 *       "Medication Report":[
	 *
	 *       ]
	 *    },
	 *    "parameters":{
	 *       "maxTime":"90",
	 *       "newImages":"false",
	 *       "numQuestions":"20",
	 *       "ratio":"50"
	 *    },
	 *    "activityResults":[
	 *       {
	 *          "activityBlockId":"PATTERNCOMPARISON",
	 *          "screenWidth":2392,
	 *          "screenHeight":1440,
	 *          "timeTakenToComplete":4368,
	 *          "score":"0.35",
	 *          "answers":[
	 *             {
	 *                "pattern":"01",
	 *                "result":false,
	 *                "timeTaken":887,
	 *                "questionIndex":1
	 *             },
	 *             {
	 *                "pattern":"10",
	 *                "result":false,
	 *                "timeTaken":671,
	 *                "questionIndex":2
	 *             },
	 *             {
	 *                "pattern":"01",
	 *                "result":false,
	 *                "timeTaken":142,
	 *                "questionIndex":3
	 *             },
	 *             {
	 *                "pattern":"11",
	 *                "result":true,
	 *                "timeTaken":148,
	 *                "questionIndex":4
	 *             },
	 *             {
	 *                "pattern":"00",
	 *                "result":true,
	 *                "timeTaken":123,
	 *                "questionIndex":5
	 *             },
	 *             {
	 *                "pattern":"00",
	 *                "result":true,
	 *                "timeTaken":138,
	 *                "questionIndex":6
	 *             },
	 *             {
	 *                "pattern":"10",
	 *                "result":false,
	 *                "timeTaken":146,
	 *                "questionIndex":7
	 *             },
	 *             {
	 *                "pattern":"11",
	 *                "result":true,
	 *                "timeTaken":141,
	 *                "questionIndex":8
	 *             },
	 *             {
	 *                "pattern":"10",
	 *                "result":false,
	 *                "timeTaken":141,
	 *                "questionIndex":9
	 *             },
	 *             {
	 *                "pattern":"01",
	 *                "result":false,
	 *                "timeTaken":154,
	 *                "questionIndex":10
	 *             },
	 *             {
	 *                "pattern":"10",
	 *                "result":false,
	 *                "timeTaken":144,
	 *                "questionIndex":11
	 *             },
	 *             {
	 *                "pattern":"01",
	 *                "result":false,
	 *                "timeTaken":129,
	 *                "questionIndex":12
	 *             },
	 *             {
	 *                "pattern":"11",
	 *                "result":true,
	 *                "timeTaken":163,
	 *                "questionIndex":13
	 *             },
	 *             {
	 *                "pattern":"01",
	 *                "result":false,
	 *                "timeTaken":152,
	 *                "questionIndex":14
	 *             },
	 *             {
	 *                "pattern":"10",
	 *                "result":false,
	 *                "timeTaken":156,
	 *                "questionIndex":15
	 *             },
	 *             {
	 *                "pattern":"00",
	 *                "result":true,
	 *                "timeTaken":154,
	 *                "questionIndex":16
	 *             },
	 *             {
	 *                "pattern":"01",
	 *                "result":false,
	 *                "timeTaken":152,
	 *                "questionIndex":17
	 *             },
	 *             {
	 *                "pattern":"01",
	 *                "result":false,
	 *                "timeTaken":140,
	 *                "questionIndex":18
	 *             },
	 *             {
	 *                "pattern":"00",
	 *                "result":true,
	 *                "timeTaken":143,
	 *                "questionIndex":19
	 *             },
	 *             {
	 *                "pattern":"01",
	 *                "result":false,
	 *                "timeTaken":163,
	 *                "questionIndex":20
	 *             }
	 *          ]
	 *       }
	 *    ]
	 * }
	 * @apiExample Example of FINGERTAPPING:
	 * {
	 *    "activityInstanceID":"673",
	 *    "timeStamp":1554868564110,
	 *    "preTaskSurvey":{
	 *       "task":"Pre-task Survey",
	 *       "HadSeizure":false,
	 *       "Recent Seizure info":[
	 *
	 *       ],
	 *       "MissedMedications":true,
	 *       "NewMedication":false,
	 *       "Medication Report":[
	 *
	 *       ]
	 *    },
	 *    "parameters":{
	 *       "maxTrials":"10",
	 *       "consecTrials":"5",
	 *       "maxDiff":"5",
	 *       "trialTime":"5",
	 *       "twoHands":"true"
	 *    },
	 *    "activityResults":[
	 *       {
	 *          "activityBlockId":"FINGERTAPPING",
	 *          "timeToTap":5,
	 *          "screenWidth":2392,
	 *          "screenHeight":1440,
	 *          "timeTakenToComplete":75210,
	 *          "score":"14.300000190734863",
	 *          "answers":[
	 *             {
	 *                "operatingHand":"LEFT",
	 *                "tapNumber":11
	 *             },
	 *             {
	 *                "operatingHand":"RIGHT",
	 *                "tapNumber":15
	 *             },
	 *             {
	 *                "operatingHand":"LEFT",
	 *                "tapNumber":10
	 *             },
	 *             {
	 *                "operatingHand":"RIGHT",
	 *                "tapNumber":9
	 *             },
	 *             {
	 *                "operatingHand":"LEFT",
	 *                "tapNumber":16
	 *             },
	 *             {
	 *                "operatingHand":"RIGHT",
	 *                "tapNumber":16
	 *             },
	 *             {
	 *                "operatingHand":"LEFT",
	 *                "tapNumber":20
	 *             },
	 *             {
	 *                "operatingHand":"RIGHT",
	 *                "tapNumber":14
	 *             },
	 *             {
	 *                "operatingHand":"LEFT",
	 *                "tapNumber":16
	 *             },
	 *             {
	 *                "operatingHand":"RIGHT",
	 *                "tapNumber":16
	 *             }
	 *          ]
	 *       }
	 *    ]
	 * }
	 * @apiSuccess {JSON} message The message of the operation.
	 *
	 * @apiSuccessExample Example data on Success:
	 * {
	 *     "message": "SUCCESS"
	 * }
	 *
	 * @apiError (Error 404) ExpiredInstance The PIN is invalid
	 * @apiError (Error 400) UserNotFound Pin parameter is mandatory
	 * @apiError (Error 500) JsonError The JSON is invalid
	 * @apiError (Error 409) InputMismatch Payload and parameters do not match
	 * @apiError (Error 409) AlreadyComplete Survey_instance has been completed
	 * @apiErrorExample {json} ExpiredInstance:
	 * {
	 *     "developerMessage": null,
	 *     "message": "Survey instance has expired",
	 *     "code": 0,
	 *     "status": 0
	 *  }
	 * @apiErrorExample {json} JsonError
	 * {
	 *     "developerMessage": null,
	 *     "message": "The JSON is invalid",
	 *     "code": 0,
	 *     "status": 0
	 * }
	 * @apiErrorExample {json} InputMismatch:
	 * {
	 *     "message": "The Activity Instance ID is not for the given Patient Pin.",
	 *     "code": 0,
	 *     "status": 0
	 * }
	 *
	 * @apiErrorExample {json} AlreadyComplete:
	 * {
	 *     "developerMessage": null,
	 *     "message": "Survey_instance has been completed",
	 *     "code": 0,
	 *     "status": 0
	 * }
	 */
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
