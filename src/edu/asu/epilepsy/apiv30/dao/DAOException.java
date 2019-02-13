package edu.asu.epilepsy.apiv30.dao;

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
