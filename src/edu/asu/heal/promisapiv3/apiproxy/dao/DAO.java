package edu.asu.heal.promisapiv3.apiproxy.dao;

/**
 * This interface defines the datastore methods for instantiating and persisting model objects
 * 
 * @author Deepak S N
 *
 */
public interface DAO {
	/**
	 * Returns a model object based on the given ID, which should exist
	 * @param activityInstanceId
	 * @return a HashMap of name/value pairs that can be used to construct the target model object
	 * @throws DAOException if there is a problem creating the object
	 */
	public default ValueObject getApiVersion(String appVersion) throws DAOException {
		return null;
	}
}
