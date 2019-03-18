package edu.asu.epilepsy.apiv30.model;

public class PatternComparisonParameters {

    private String numQuestions;
    private String maxTime;
    private String newImages;

    public PatternComparisonParameters() {
    }

    public String getNumQuestions() {
        return numQuestions;
    }

    public void setNumQuestions(String numQuestions) {
        this.numQuestions = numQuestions;
    }

    public String getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(String maxTime) {
        this.maxTime = maxTime;
    }

    public String getNewImages() {
        return newImages;
    }

    public void setNewImages(String newImages) {
        this.newImages = newImages;
    }
}
