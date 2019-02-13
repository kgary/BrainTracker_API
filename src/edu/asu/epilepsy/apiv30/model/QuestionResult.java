package edu.asu.epilepsy.apiv30.model;

import java.sql.Timestamp;

/**
 * This class is the POJO for the question_result table
 *
 * @author Deepak S N
 */
public class QuestionResult implements java.io.Serializable {

  private static final long serialVersionUID = 939394312806437799L;

  private int _id;
  private Timestamp _createdAt;
  private Timestamp _updatedAt;
  private int _questionIdFk;
  private int _questionOptionIdFk;
  private int _activityInstanceIdFk;

  //The constructor without the id for the insert as it is autoIncrement.
  public QuestionResult(Timestamp createdAt, Timestamp updatedAt, int questionId, int questionOptionIdFk, int activityInstanceIdFk) {
    _createdAt = createdAt;
    _updatedAt = updatedAt;
    _questionIdFk = questionId;
    _questionOptionIdFk = questionOptionIdFk;
    _activityInstanceIdFk = activityInstanceIdFk;
  }

  //The constructor is with the id for the select.
  public QuestionResult(int id, Timestamp createdAt, Timestamp updatedAt, int questionId, int questionOptionIdFk, int activityInstanceIdFk) {
    _id = id;
    _createdAt = createdAt;
    _updatedAt = updatedAt;
    _questionIdFk = questionId;
    _questionOptionIdFk = questionOptionIdFk;
    _activityInstanceIdFk = activityInstanceIdFk;
  }

  public int getId() {
    return _id;
  }

  public Timestamp getCreatedAt() {
    return _createdAt;
  }

  public Timestamp getUpdatedAt() {
    return _updatedAt;
  }

  public int getQuestionIdFk() {
    return _questionIdFk;
  }

  public int getQuestionOptionIdFk() {
    return _questionOptionIdFk;
  }

  public int getActivityInstanceIdFk() {
    return _activityInstanceIdFk;
  }
}
