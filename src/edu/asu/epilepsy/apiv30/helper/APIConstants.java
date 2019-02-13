package edu.asu.epilepsy.apiv30.helper;

public final class APIConstants {

  // This effectively makes the class static
  private APIConstants() {
  }

  public static final String SUCCESS = "SUCCESS";
  public static final String FAILURE = "FAILURE";
  public static final String COMPLETED = "completed";
  public static final String CHILD = "child";
  public static final String PENDING = "pending";
  public static final String INPROGRESS = "in progress";
  public static final String PI = "PI";
  public static final int OTHEROPTION = 67;
  public static final int MADOSAGEQUESTION = 76;
  public static final int PR_HYDROXYUREAQUESTION = 83;
  public static final int PR_WITHEXCEPTCLAUSE = 84;
  public static final int PR_WITHOUTEXCEPTCLAUSE = 85;
  public static final int ADLT_HYDROXYUREAQUESTION = 83;
  public static final int ADLT_WITHEXCEPTCLAUSE = 84;
  public static final int ADLT_WITHOUTEXCEPTCLAUSE = 85;
  public static final int CHLD__HYDROXYUREAQUESTION = 83;
  public static final int CHLD__WITHEXCEPTCLAUSE = 84;
  public static final int CHLD__WITHOUTEXCEPTCLAUSE = 85;
  public static final String dailyActivityID = "CA3";
  public static final String weeklyActivityID = "CA2";
  public static final String dailyActivityID_WITHOUT_PI = "CA3_WITHOUT_PI";
  public static final String dailyActivityID_MA = "MA";
  public static final String dailyActivityID_PI = "PI_DAILY";
  public static final String weeklyActivityID_PI = "PI_WEEKLY";
  public static final String weeklyActivityID_WITHOUT_PI = "CA2_WITHOUT_PI";
}
