package edu.asu.epilepsy.apiv30.dao;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple wrapper object. This should be converted later to support heterogenous collections via JBloch, see
 * http://www.codeaffine.com/2015/03/04/map-distinct-value-types-using-java-generics/
 * For now this is type unsafe and requires a downcast to work in the caller, meaning they have to be sure at
 * runtime what the type of the parameter is. All this to avoid writing upteen VOs and retrieve methods.
 * @author kevinagary
 *
 */
public final class ValueObject {
	private Map<String, Object> __attributes = new HashMap<String, Object>();
	
	public ValueObject() {}
	
	public boolean equals(Object obj) {
		return (obj instanceof ValueObject) && this.__attributes.equals(((ValueObject)obj).__attributes);
	}
	
	public int hashCode() {
		return __attributes.hashCode();
	}
	
	public Object getAttribute(String key) {
		return __attributes.get(key);
	}
	public Object putAttribute(String key, Object value) {
		return __attributes.put(key, value);
	}
}
