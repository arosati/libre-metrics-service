/*National Snow & Ice Data Center, University of Colorado, Boulder
  Copyright 2010 Regents of the University of Colorado*/
package org.nsidc.libre.metrics.model.report;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

/**
 * @author glewis
 *
 */
public class ParameterAdapter extends XmlAdapter<ParameterList, Map<String, String>> {
	private static Logger logger = Logger.getLogger(ParameterAdapter.class);
	
	/* (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public Map<String, String> unmarshal(ParameterList v) throws Exception {
		Map<String, String> paramMap = new HashMap<String, String>();
		for (Parameter p : v.getParams()) {
			paramMap.put(p.getName(), p.getValue());
		}
		
		return paramMap;
	}

	/* (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public ParameterList marshal(Map<String, String> v) throws Exception {
		
		logger.trace("Marshalling parameters...");
		
		ParameterList params = new ParameterList();
		for (String key : v.keySet()) {
			logger.trace("Adding parameter: " + key + ", " + v.get(key));
			params.addParameter(new Parameter(key, v.get(key)));
		}
		
		if (params.isEmpty()) params = null;
		
		return params;
	}

}
