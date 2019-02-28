package edu.asu.epilepsy.apiv30.model.response;


import edu.asu.epilepsy.apiv30.model.Patient;

public class GenDailyWeeklyResponse extends CreateActInstanceResponse {
  private Patient.Trial trial_type;
  public GenDailyWeeklyResponse() {
    trial_type = null;
  }

  public Patient.Trial getTrial_type() {
    return trial_type;
  }

  public void setTrial_type(Patient.Trial trial_type) {
    this.trial_type = trial_type;
  }
}
