package edu.asu.epilepsy.apiv30.model;

import java.util.List;

public class Sequence {
  private List<String> sequence;
  private String parentactivity;

  public Sequence() {
  }

  public Sequence(List<String> sequence, String parentactivity) {
    this.sequence = sequence;
    this.parentactivity = parentactivity;
  }

  public List<String> getSequence() {
    return sequence;
  }

  public void setSequence(List<String> sequence) {
    this.sequence = sequence;
  }

  public String getParentactivity() {
    return parentactivity;
  }

  public void setParentactivity(String parentactivity) {
    this.parentactivity = parentactivity;
  }
}
