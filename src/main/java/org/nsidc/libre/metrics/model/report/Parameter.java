/*National Snow & Ice Data Center, University of Colorado, Boulder
  Copyright 2010 Regents of the University of Colorado*/
package org.nsidc.libre.metrics.model.report;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * @author glewis
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameter {
	@XmlAttribute
	private String name;
	
	@XmlValue
	private String value;
	
	public Parameter() {
		
	}
	
	public Parameter(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
