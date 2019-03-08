package edu.asu.epilepsy.apiv30.model.request;

import com.google.gson.JsonArray;
import edu.asu.epilepsy.apiv30.model.response.Response;
import edu.asu.epilepsy.apiv30.model.response.Status;

public class EnrollPatientsRequest extends Response {
    private String patientGroup;
    private String childPin;
    private String deviceType;
    private String deviceVersion;
    private String hydroxureaTablets;
    private String isChildOnMed;
    private JsonArray medDetails;

    public EnrollPatientsRequest(Status status, String patientGroup, String childPin, String deviceType, String deviceVersion, String hydroxureaTablets, String isChildOnMed, JsonArray medDetails) {
        super(status);
        this.patientGroup = patientGroup;
        this.childPin = childPin;
        this.deviceType = deviceType;
        this.deviceVersion = deviceVersion;
        this.hydroxureaTablets = hydroxureaTablets;
        this.isChildOnMed = isChildOnMed;
        this.medDetails = medDetails;
    }

    public String getPatientGroup() {
        return patientGroup;
    }

    public void setPatientGroup(String patientGroup) {
        this.patientGroup = patientGroup;
    }

    public String getChildPin() {
        return childPin;
    }

    public void setChildPin(String childPin) {
        this.childPin = childPin;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceVersion() {
        return deviceVersion;
    }

    public void setDeviceVersion(String deviceVersion) {
        this.deviceVersion = deviceVersion;
    }

    public String getChildOnMed() {
        return isChildOnMed;
    }

    public void setIsChildOnMed(String isChildOnMed) {
        this.isChildOnMed = isChildOnMed;
    }

    public JsonArray getMedDetails() {
        return medDetails;
    }

    public void setMedDetails(JsonArray medDetails) {
        this.medDetails = medDetails;
    }

    public String getHydroxureaTablets() {
        return hydroxureaTablets;
    }

    public void setHydroxureaTablets(String hydroxureaTablets) {
        this.hydroxureaTablets = hydroxureaTablets;
    }
}
