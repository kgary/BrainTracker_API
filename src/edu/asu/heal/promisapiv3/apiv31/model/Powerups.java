package edu.asu.heal.promisapiv3.apiv31.model;

public final class Powerups {

	private String powerupId;
	private String powerupName;
	private String powerupDesc;

	public Powerups(String powerupId, String powerupName, String powerupDesc) {
		super();
		this.powerupId = powerupId;
		this.powerupName = powerupName;
		this.powerupDesc = powerupDesc;
	}

	public String getPowerupId() {
		return powerupId;
	}

	public void setPowerupId(String powerupId) {
		this.powerupId = powerupId;
	}

	public String getPowerupName() {
		return powerupName;
	}

	public void setPowerupName(String powerupName) {
		this.powerupName = powerupName;
	}

	public String getPowerupDesc() {
		return powerupDesc;
	}

	public void setPowerupDesc(String powerupDesc) {
		this.powerupDesc = powerupDesc;
	}
}
