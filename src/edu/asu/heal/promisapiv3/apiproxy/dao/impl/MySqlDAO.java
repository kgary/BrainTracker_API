/**
 * This is the underlying implementation for the backing store.
 * @author Deepak S N
 */
package edu.asu.heal.promisapiv3.apiproxy.dao.impl;
import java.util.Properties;

import edu.asu.heal.promisapiv3.apiproxy.dao.DAOException;

public class MySqlDAO extends JdbcDAO {

	public MySqlDAO(Properties props) throws DAOException {
		super(props);
		// TODO Auto-generated constructor stub
	}

}
