package edu.asu.epilepsy.apiv30.model.response;

import com.google.gson.JsonArray;

public class CheckActivityResponse extends Response {
  private JsonArray activities;
  private boolean showEnhancedContent;

  public CheckActivityResponse(Status message, JsonArray activities, boolean showEnhancedContent) {
    super(message);
    this.activities = activities;
    this.showEnhancedContent = showEnhancedContent;
  }

  public JsonArray getActivities() {
    return activities;
  }

  public void setActivities(JsonArray activities) {
    this.activities = activities;
  }

  public boolean isShowEnhancedContent() {
    return showEnhancedContent;
  }

  public void setShowEnhancedContent(boolean showEnhancedContent) {
    this.showEnhancedContent = showEnhancedContent;
  }
}
