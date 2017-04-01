package edu.asu.heal.promisapiv3.apiv31.model;

public final class Badge {

	private String badgeId;
	private String badgeName;
	private String badgeDesc;
	private String badgeType;

	public Badge(String badgeId, String badgeName, String badgeDesc, String badgeType) {
		this.badgeId = badgeId;
		this.badgeName = badgeName;
		this.badgeDesc = badgeDesc;
		this.badgeType = badgeType;
	}

	public String getBadgeDesc() {
		return badgeDesc;
	}

	public void setBadgeDesc(String badgeDesc) {
		this.badgeDesc = badgeDesc;
	}

	public String getBadgeId() {
		return badgeId;
	}

	public void setBadgeId(String badgeId) {
		this.badgeId = badgeId;
	}

	public String getBadgeName() {
		return badgeName;
	}

	public void setBadgeName(String badgeName) {
		this.badgeName = badgeName;
	}

	public String getBadgeType() {
		return badgeType;
	}

	public void setBadgeType(String badgeType) {
		this.badgeType = badgeType;
	}

}
