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
public class ReportMetricList {
	@XmlElement(name="metric")
	List<ReportColumn> metrics;
	
	public ReportMetricList() {
		metrics = new ArrayList<ReportColumn>();
	}
	
	public void addReportValue(ReportColumn metric) {
		metrics.add(metric);
	}
	
	public List<ReportColumn> getReportValues() {
		return metrics;
	}
}
