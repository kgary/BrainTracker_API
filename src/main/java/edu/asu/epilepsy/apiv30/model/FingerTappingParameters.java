package edu.asu.epilepsy.apiv30.model;

public class FingerTappingParameters {

    private String maxTrials;
    private String consecTrials;
    private String tapVariance;
    private String trialTime;

    public FingerTappingParameters() {
    }

    public String getMaxTrials() {
        return maxTrials;
    }

    public void setMaxTrials(String maxTrials) {
        this.maxTrials = maxTrials;
    }

    public String getConsecTrials() {
        return consecTrials;
    }

    public void setConsecTrials(String consecTrials) {
        this.consecTrials = consecTrials;
    }

    public String getTapVariance() {
        return tapVariance;
    }

    public void setTapVariance(String tapVariance) {
        this.tapVariance = tapVariance;
    }

    public String getTrialTime() {
        return trialTime;
    }

    public void setTrialTime(String trialTime) {
        this.trialTime = trialTime;
    }
}
