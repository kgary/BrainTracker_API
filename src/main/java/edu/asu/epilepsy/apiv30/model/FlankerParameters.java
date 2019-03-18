package edu.asu.epilepsy.apiv30.model;

public class FlankerParameters {

    private String noOfQuestions;

    public FlankerParameters() {
    }

    public FlankerParameters(String noOfQuestions) {
        this.noOfQuestions = noOfQuestions;
    }

    public String getNoOfQuestions() {
        return noOfQuestions;
    }

    public void setNoOfQuestions(String noOfQuestions) {
        this.noOfQuestions = noOfQuestions;
    }
}
