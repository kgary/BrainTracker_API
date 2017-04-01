package edu.asu.heal.promisapiv3.apiv31.model;

public final class BagdePowerups {

	private String badgeId;
	private String powerupId;
	private int powerupCount;

	public BagdePowerups(String badgeId, String powerupId, int powerupCount){
		this.badgeId = badgeId;
		this.powerupId = powerupId;
		this.powerupCount = powerupCount;
	}

	public String getBadgeId() {
		return badgeId;
	}

	public void setBadgeId(String badgeId) {
		this.badgeId = badgeId;
	}

	public String getPowerupId() {
		return powerupId;
	}

	public void setPowerupId(String powerupId) {
		this.powerupId = powerupId;
	}

	public int getPowerupCount() {
		return powerupCount;
	}

	public void setPowerupCount(int powerupCount) {
		this.powerupCount = powerupCount;
	}
}
