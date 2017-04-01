package edu.asu.heal.promisapiv3.apiv31.model;

public final class Games {

	private String gameId;
	private String gameName;
	private String gameDesc;

	public Games(String gameId, String gameName, String GameDesc){

		this.gameId = gameId;
		this.gameName = gameName;
		this.gameDesc = gameDesc;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public String getGameDesc() {
		return gameDesc;
	}

	public void setGameDesc(String gameDesc) {
		this.gameDesc = gameDesc;
	}
}
