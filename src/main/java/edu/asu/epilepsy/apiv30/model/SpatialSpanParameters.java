package edu.asu.epilepsy.apiv30.model;

public class SpatialSpanParameters {

    private String maxLevel;
    private String maxTrialCount;

    public SpatialSpanParameters() {
    }

    public String getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(String maxLevel) {
        this.maxLevel = maxLevel;
    }

    public String getMaxTrialCount() {
        return maxTrialCount;
    }

    public void setMaxTrialCount(String maxTrialCount) {
        this.maxTrialCount = maxTrialCount;
    }
}
