package edu.asu.heal.promisapiv3.apiv31.dao;

public class DAOException extends Exception {
	public DAOException() {
		super();
	}
	public DAOException(Throwable t) {
		super(t);
	}
	public DAOException(String msg) {
		super(msg);
	}
	public DAOException(String msg, Throwable t) {
		super(msg, t);
	}
}
