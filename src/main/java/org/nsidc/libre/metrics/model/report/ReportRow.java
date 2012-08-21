/*National Snow & Ice Data Center, University of Colorado, Boulder
  Copyright 2010 Regents of the University of Colorado*/
package org.nsidc.libre.metrics.model.report;

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author glewis
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportRow implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@XmlAttribute
	private Integer count;
	
	@XmlElement(name="column")
	private Set<ReportColumn> columns;
	
	public ReportRow() {
	}
	
	public ReportRow(Set<ReportColumn> columns) {
		this(columns, 1);
	}
		
	public ReportRow(Set<ReportColumn> columns, Integer count) {
		this.columns = columns;
		this.count = count;
	}
	
	public void setCount(Integer count) {
		this.count = count;
	}
	
	public Integer getCount() {
		return count;
	}
	
	public void incrementCount() {
		count++;
	}
	
	public void setColumns(Set<ReportColumn> columns) {
		this.columns = columns;
	}
	
	public Set<ReportColumn> getColumns() {
		return columns;
	}
}
