package edu.asu.epilepsy.apiv30.resource;

import edu.asu.epilepsy.apiv30.service.PromisService;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
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
 *
 * @author kevinagary
 */
@Path("/activities/")
@Produces(MediaType.APPLICATION_JSON)
public class ActivityInstance {
  /**
   * return the activities scheduled for the patient, not exactly sure if we can have
   * activities that are not active (expired or in future) but including due to cron job legacy
   *
   * @param pin
   * @param isActive a flag that forces return of only those activities currently active
   * @return
   */

  PromisService promis_service = new PromisService();

  /**
   * @api {GET} /activities/scheduledactivity Get All Scheduled Activities
   * @apiName GetScheduledActivity
   * @apiGroup Activities
   * @apiVersion 0.0.0
   * @apiDescription This is a API which designed for peeking activity instances.
   * @apiParam {Integer} pin Pin of the patient
   *
   * @apiSuccess {JSON} Result Result of list of existing activities for the patient with the pin
   *
   * @apiSuccessExample Example data on Success:
   * {
   *     "activities": [
   *         {
   *             "activityInstanceID": "625",
   *             "nextDueAt": "Mon Mar 11 04:59:00 MST 2019",
   *             "activityTitle": "Epilepsy Weekly Survey",
   *             "description": "Weekly Activity To be completed for Epilepsy disease patients",
   *             "state": "pending",
   *             "sequence": "{\"sequence\":[\"PATTERNCOMPARISON\"],\"parentactivity\":\"PATTERNCOMPARISON\"}"
   *         }
   *     ],
   *     "showEnhancedContent": false,
   *     "status": "SUCCESS",
   *     "message": "",
   *     "pin": ""
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
  public Response getScheduledActivities(@QueryParam("pin") String pin) throws Exception {

    Response response = null;
    String json_string = promis_service.checkActivityInstance(pin);

    response = Response.status(Response.Status.OK).entity(json_string).build();
    return response;

  }

/**
 * @api {GET} /activities/activityinstance/{activityInstanceId} Get Specific Activity
 * @apiName GetSpecificActivityById
 * @apiGroup Activities
 * @apiVersion 0.0.0
 * @apiDescription This is a API which designed for peeking a activity instance.
 *
 * @apiParam {Integer} activityInstanceId activity instance's Id
 * @apiParam {Integer} pin Pin of the patient
 * @apiParamExample {JSON} Example of param:
 * {"pin": 1004}
 * @apiSuccess {JSON} Result Result of the activity
 * @apiSuccessExample Example data on Success:
 * {
 *     "sequences": [
 *         "PATTERNCOMPARISON"
 *     ],
 *     "activityName": "Epilepsy Weekly Survey",
 *     "parentactivity": "625",
 *     "startTime": "Sat Mar 09 22:05:17 MST 2019",
 *     "endTime": "Mon Mar 11 04:59:00 MST 2019",
 *     "state": "pending",
 *     "activitySequence": [
 *         {
 *             "activityBlockId": "PATTERNCOMPARISON"
 *         }
 *     ],
 *     "showGame": false,
 *     "status": "SUCCESS",
 *     "message": "",
 *     "pin": ""
 * }
 *
 * @apiError (Error 404) UserNotFound The PIN is invalid
 * @apiError (Error 400) UserNotFound Pin parameter is mandatory
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
  @Path("/activityinstance/{activityInstanceId}")
  public Response getActivityInstance(
    @PathParam("activityInstanceId") String activityInstanceId,
    @QueryParam("pin") String pin
  ) throws Exception {
    if (pin != null) {
      Response response = null;
      String json_string = promis_service.getActivityInstance(activityInstanceId, pin);

      response = Response.status(Response.Status.OK).entity(json_string).build();
      return response;
    } else {
      throw new WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST)
          .entity("Pin parameter is mandatory")
          .build());
    }
  }

  /**
   * @api {POST} /activities/activityinstanceresult/{activityInstanceId} Submit a New Activity Result
   * @apiName PostActivityInstanceResult
   * @apiGroup Activity
   * @apiVersion 0.0.0
   * @apiDescription This is a API which designed for submitting a activity result.
   * @apiParam {Integer} activityInstanceId activity instance's Id
   * @apiParam {Integer} pin Pin of the patient
   * @apiParamExample {JSON} Example of param:
   * {"pin": 4003}
   *
   * @apiExample Example of body:
   * {
   *     "activityInstanceID": 625,
   *     "timeStamp": 1552196109000,
   *     "activityResults": [
   *         {
   *             "activityBlockId": "PATTERNCOMPARISON",
   *             "screenWidth": 1920,
   *             "screenHeight": 1080,
   *             "timeTakenToComplete": 11000,
   *             "answers": [
   *                 {
   *                     "result": true,
   *                     "timeTaken": 6932,
   *                     "questionIndex": 1,
   *                     "pattern": "11"
   *                 },
   *                 {
   *                     "result": false,
   *                     "timeTaken": 2629,
   *                     "questionIndex": 2,
   *                     "pattern": "01"
   *                 },
   *                 {
   *                     "result": true,
   *                     "timeTaken": 1626,
   *                     "questionIndex": 3,
   *                     "pattern": "11"
   *                 },
   *                 {
   *                     "result": false,
   *                     "timeTaken": 2350,
   *                     "questionIndex": 4,
   *                     "pattern": "01"
   *                 },
   *                 {
   *                     "result": true,
   *                     "timeTaken": 12974,
   *                     "questionIndex": 5,
   *                     "pattern": "00"
   *                 }
   *             ]
   *         }
   *     ]
   * }
   *
   * @apiSuccess {JSON} message The message of the operation.
   *
   * @apiSuccessExample Example data on Success:
   * {
   *     "status": "SUCCESS",
   *     "message": "",
   *     "pin": ""
   * }
   *
   * @apiError (Error 404) UserNotFound The PIN is invalid
   * @apiError (Error 400) UserNotFound Pin parameter is mandatory
   * @apiError (Error 500) JsonError The JSON is invalid
   * @apiError (Error 409) JsonError The Activity Instance ID is not for the given Patient Pin
   * @apiError (Error 409) JsonError Survey_instance has been completed
   * @apiErrorExample {json} Error response:
   * {
   *     "message": "The Activity Instance ID is not for the given Patient Pin.",
   *     "code": 0,
   *     "status": 0
   * }
   */
  @POST
  @Path("/activityinstanceresult/{activityInstanceId}")
  public Response submitActivity(String content,
                                 @PathParam("activityInstanceId") String activityInstanceId,
                                 @QueryParam("pin") String pin
  ) throws NumberFormatException, Exception {
    if (pin != null) {

      Response response = null;
      String jsonstring = promis_service.submitActivityInstance(content, pin, activityInstanceId);
      response = Response.status(Response.Status.CREATED)
        .entity(jsonstring).build();
      return response;
    } else {
      throw new WebApplicationException(
        Response.status(Response.Status.BAD_REQUEST)
          .entity("name parameter is mandatory")
          .build());
    }

  }

}
