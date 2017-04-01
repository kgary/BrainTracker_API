package edu.asu.heal.promisapiv3.apiproxy.model;

public class APIVersion {
	
	private String appVersion;
	private String apiVersion;
	
	public APIVersion(String appVerison, String apiVersion)
	{
		this.apiVersion = apiVersion;
		this.appVersion = appVerison;
	}
	
	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
	
	public String getAppVersion() {
		return appVersion;
	}
}
