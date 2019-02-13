package edu.asu.epilepsy.apiv30.model;

/**
 * This the Generic abstract model for the for the PostActivity.
 *
 * @author Deepak S N
 */
public abstract class PostActivity {

  protected String _activityId;
  protected int _activityInstanceId;

  public int getActivityInstanceId() {
    return _activityInstanceId;
  }

  public String getActivityId() {
    return _activityId;
  }
}
