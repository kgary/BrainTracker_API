/**
 * This is the Data Access layer which will talk to the underlying database.
 * @author Deepak S N
 */
package edu.asu.heal.promisapiv3.apiproxy.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.asu.heal.promisapiv3.apiproxy.dao.DAO;
import edu.asu.heal.promisapiv3.apiproxy.dao.DAOException;
import edu.asu.heal.promisapiv3.apiproxy.dao.DAOFactory;
import edu.asu.heal.promisapiv3.apiproxy.dao.ValueObject;

public abstract class JdbcDAO implements DAO {
	static Logger log = LogManager.getLogger(JdbcDAO.class);
	private String __jdbcDriver;
	protected String _jdbcUser;
	protected String _jdbcPasswd;
	protected String _jdbcUrl;
	
	public JdbcDAO(Properties props) throws DAOException {
		
		// For MySQL we expect the JDBC Driver, user, password, and the URI. Maybe more in the future.
        _jdbcUrl    = props.getProperty("jdbc.url");
        _jdbcUser   = props.getProperty("jdbc.user");
        _jdbcPasswd = props.getProperty("jdbc.passwd");
        __jdbcDriver = props.getProperty("jdbc.driver");
        
        try {
        		Class.forName(__jdbcDriver); // ensure the driver is loaded
        }
        catch (ClassNotFoundException cnfe) {
        		throw new DAOException("*** Cannot find the JDBC driver " + __jdbcDriver, cnfe);
        }
        catch (Throwable t) {
        		throw new DAOException(t);
        }
	}
	
	/**
	 * We really should implement some simple wrapper and pooling YYY
	 * @return database Connection
	 * @throws DAOException
	 */
	protected Connection getConnection() throws DAOException {
		try {
			return DriverManager.getConnection(_jdbcUrl, _jdbcUser, _jdbcPasswd);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DAOException("Unable to get connection to database", e);
		}
	}
	
	/**
	 * Get the api version for the corresponding app version.
	 * @author Deepak S N
	 * @return ValueObject
	 * @param String app version number
	 */
	@Override
	public ValueObject getApiVersion(String appVersion) throws DAOException {
		ValueObject vo = null; // need to fill this up
		Connection connection = getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try	{
			String query = DAOFactory.getDAOProperties().getProperty("sql.apiversion");
			ps = connection.prepareStatement(query);
			ps.setString(1, appVersion);
			rs = ps.executeQuery();
			vo = new ValueObject();
			if(rs.next())
			{
				String apiversion = rs.getString("apiversion");
				//Filling the value objects.
				vo.putAttribute("apiversion", apiversion);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new DAOException("Unable to process results from query sql.apiversion");
		} finally {
			try {
				if (rs != null) rs.close();
				if (ps != null) ps.close();
				if (connection != null) connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
				// YYY need a logging facility, but this does not have to be rethrown
			}
		}
		return vo;
	}	
}
