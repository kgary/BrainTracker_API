package edu.asu.heal.promisapiv3.apiproxy.service;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
//import org.apache.log4j.Logger;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.asu.heal.promisapiv3.apiproxy.dao.DAOFactory;
import edu.asu.heal.promisapiv3.apiproxy.model.APIVersion;
import edu.asu.heal.promisapiv3.apiproxy.model.ModelException;
import edu.asu.heal.promisapiv3.apiproxy.model.ModelFactory;

/**
 * This is the Service layer which will handle the business logic
 * @author Deepak S N
 *
 */
public class PromisService {
	
    private ModelFactory __modelFactory = null;
    static Logger log = LogManager.getLogger(PromisService.class);
    private static final PromisService __theService = new PromisService();
	ObjectMapper mapper = new ObjectMapper();
	
    public PromisService() {
        try {
            __modelFactory = new ModelFactory();
        } catch (ModelException me) {
            me.printStackTrace();
            // YYY If we can't get model objects we really can't do anything
        }
    }
    
    public static PromisService getPromisService() {
        return __theService;
    }
    
    public String getAPIVersion(String appVersion,
    		@Context HttpHeaders headers,
    		@Context UriInfo uriInfo) throws Exception
    {
            JSONObject api = new JSONObject();
            
            String apiVersion = "";
            APIVersion apiversionFromDB = null;
            
            if(appVersion != null && appVersion.isEmpty())
            {
            	apiVersion = DAOFactory.getDAOProperties().getProperty("latestAPIVersion");
            }
            else
            {
            	apiversionFromDB = __modelFactory.getApiVersion(appVersion);
                
                if(apiversionFromDB != null)
                {
                	apiVersion =  apiversionFromDB.getApiVersion();
                	System.out.println("The selected api version is::"+apiVersion);
                }
            }
            
            
            String host = headers.getRequestHeader("Host").get(0);
            System.out.println("The host is::"+host);
            
            StringBuilder redirectBaseURL = new StringBuilder();
       
            redirectBaseURL.append(host);
            redirectBaseURL.append("/");
            redirectBaseURL.append(apiVersion);
            redirectBaseURL.trimToSize();
    		
    		System.out.println("The url::"+redirectBaseURL.toString());
            api.put("apiendpoint",redirectBaseURL.toString());
            
            return api.toString();
    }
}