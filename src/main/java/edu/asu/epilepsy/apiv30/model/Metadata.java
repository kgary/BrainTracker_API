package edu.asu.epilepsy.apiv30.model;

public class Metadata {
  private DefaultTime defaultTime;
  private String MandatoryBlocks;

  public Metadata(DefaultTime defaultTime, String mandatoryBlocks) {
    this.defaultTime = defaultTime;
    MandatoryBlocks = mandatoryBlocks;
  }

  public DefaultTime getDefaultTime() {
    return defaultTime;
  }

  public void setDefaultTime(DefaultTime defaultTime) {
    this.defaultTime = defaultTime;
  }

  public String getMandatoryBlocks() {
    return MandatoryBlocks;
  }

  public void setMandatoryBlocks(String mandatoryBlocks) {
    MandatoryBlocks = mandatoryBlocks;
  }
}

class DefaultTime{
  private String duration;
  private String units;

  public DefaultTime(String duration, String units) {
    this.duration = duration;
    this.units = units;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }

  public String getUnits() {
    return units;
  }

  public void setUnits(String units) {
    this.units = units;
  }
}
