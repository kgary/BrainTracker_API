package edu.asu.epilepsy.apiv30.model;

public class CheckActivity {
  private String activityInstanceID;
  private String nextDueAt;
  private String activityTitle;
  private String description;
  private String state;
  private Sequence sequence;

  public CheckActivity(String activityInstanceID, String nextDueAt, String activityTitle, String description, String state, Sequence sequence) {
    this.activityInstanceID = activityInstanceID;
    this.nextDueAt = nextDueAt;
    this.activityTitle = activityTitle;
    this.description = description;
    this.state = state;
    this.sequence = sequence;
  }

  public String getActivityInstanceID() {
    return activityInstanceID;
  }

  public void setActivityInstanceID(String activityInstanceID) {
    this.activityInstanceID = activityInstanceID;
  }

  public String getNextDueAt() {
    return nextDueAt;
  }

  public void setNextDueAt(String nextDueAt) {
    this.nextDueAt = nextDueAt;
  }

  public String getActivityTitle() {
    return activityTitle;
  }

  public void setActivityTitle(String activityTitle) {
    this.activityTitle = activityTitle;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public Sequence getSequence() {
    return sequence;
  }

  public void setSequence(Sequence sequence) {
    this.sequence = sequence;
  }
}
