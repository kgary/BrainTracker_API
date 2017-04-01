package edu.asu.heal.promisapiv3.apiproxy.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.asu.heal.promisapiv3.apiproxy.dao.DAO;
import edu.asu.heal.promisapiv3.apiproxy.dao.DAOException;
import edu.asu.heal.promisapiv3.apiproxy.dao.DAOFactory;
import edu.asu.heal.promisapiv3.apiproxy.dao.ValueObject;

/**
 * The ModelFactory knows how to create each type of model object. Client classes should
 * call this, not the constructors of the model objects directly.
 * 
 * @author Deepak S N
 *
 */
public final class ModelFactory {
	static Logger log = LogManager.getLogger(ModelFactory.class);
	private DAO __theDAO = null;
	
	public ModelFactory() throws ModelException {
		try {
			__theDAO = DAOFactory.getTheDAO();
		} catch (DAOException de) {
			log.error(de);
			throw new ModelException("Unable to initialize the DAO", de);
		}
	}

	/**
	 * This is the model method which construct the object for the API Version.
	 * @param appVersion
	 * @return
	 * @throws ModelException
	 */
	public APIVersion getApiVersion(String appVersion) throws ModelException {
		try {
			// Has to take a ValueObject and create an ActivityInstance
			ValueObject vo = __theDAO.getApiVersion(appVersion);
			if(vo == null)
				return null;
			else if(vo.getAttribute("apiversion") != null)
			{
				return new APIVersion(appVersion,(String)vo.getAttribute("apiversion"));
			}
			else
			{
				//If we dont have the appropriate apiVersion for the app,then we send the latest api version. 
				String latestAPIVersion = DAOFactory.getDAOProperties().getProperty("latestAPIVersion");
				return new APIVersion(appVersion,latestAPIVersion);
			}
			
		} catch (DAOException de) {
			de.printStackTrace();
			log.error(de);
			throw new ModelException("Unable to create Model Object APIVersion", de);
		}
	}
}
