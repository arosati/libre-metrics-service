/*National Snow & Ice Data Center, University of Colorado, Boulder
  Copyright 2010 Regents of the University of Colorado*/
package org.nsidc.libre.metrics.model.report;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author glewis
 *
 */
public class ParameterList {
	@XmlElement(name="parameter")
	private List<Parameter> params;
	
	public ParameterList() {
		params = new ArrayList<Parameter>();
	}
		
	public void addParameter(Parameter p) {
		params.add(p);
	}
	
	public List<Parameter> getParams() {
		return params;
	}
	
	public boolean isEmpty() {
		return params.isEmpty();
	}
}
