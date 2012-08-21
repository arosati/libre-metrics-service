/*National Snow & Ice Data Center, University of Colorado, Boulder
  Copyright 2010 Regents of the University of Colorado*/
package org.nsidc.libre.metrics.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author glewis
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FieldList implements Serializable {
	private static final long serialVersionUID = 1L;

	@XmlElement(name="field")
	private List<String> fields;  // the field being reported
	
	public FieldList() {
		fields = new ArrayList<String>();
	}
	
	public void setFields(List<String> fields) {
		this.fields = fields;
	}
	
	public List<String> getFields() {
		return fields;
	}
	
	public void addField(String field) {
		fields.add(field);
	}
}
