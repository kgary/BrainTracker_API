package edu.asu.epilepsy.apiv30.model.response;

public class Response {
  private Status status;
  private String message;
  private String pin;

  public Response() {
    this.status = null;
    this.message = "";
    this.pin = "";
  }

  public Response(Status status) {
    this.status = status;
    this.message = "";
    this.pin = "";
  }

  public Response(Status status, String message) {
    this.status = status;
    this.message = message;
    this.pin = "";
  }

  public String getPin() {
    return pin;
  }

  public void setPin(String pin) {
    this.pin = pin;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
