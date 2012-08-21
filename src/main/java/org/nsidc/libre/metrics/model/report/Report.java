package org.nsidc.libre.metrics.model.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.XmlAccessType;

/*National Snow & Ice Data Center, University of Colorado, Boulder
  Copyright 2010 Regents of the University of Colorado*/

/**
 * The root class for Report results
 * @author glewis
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Report implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@XmlAttribute
	private String project;
	@XmlAttribute
	private String service;
	@XmlAttribute
	private String instance;
	
	@XmlElementWrapper(name="reportFields")
	@XmlElement(name="field")
	private List<String> reportFields;  // the field being reported
	
	@XmlJavaTypeAdapter(ParameterAdapter.class)
	@XmlElement(name="parameters")
	private Map<String, String> parameters;
	
	@XmlElementWrapper(name="rows")
	@XmlElement(name="row")
	private List<ReportRow> rows;
	
	public Report() {
		rows = new ArrayList<ReportRow>();
	}
	
	public Report(String project, String service, String instance, List<String> reportFields, Map<String, String> parameters) {
		this();
		
		this.project = project;
		this.service = service;
		this.instance = instance;
		this.reportFields = reportFields;
		this.parameters = parameters;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public List<String> getReportFields() {
		return reportFields;
	}

	public void setReportField(List<String> reportFields) {
		this.reportFields = reportFields;
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
	
	public List<ReportRow> getRows() {
		return rows;
	}
	
	public void addMetric(Map<String, String> metrics) {
		for (ReportRow row : rows) {
			Set<ReportColumn> cols = row.getColumns();
			
			// if there aren't the same number of keys in the metrics as there are columns,
			// this is not the right row
			if (metrics.keySet().size() != cols.size())
				continue;
			
			// Check the columns to see if it matches
			boolean thisRow = true;
			for (ReportColumn col : cols) {
				String metricVal = metrics.get(col.getName());
				if (metricVal == null || !metricVal.equals(col.getValue())) {
					thisRow = false;
					break;
				}
			}
			if (!thisRow) continue;
			
			// if it reaches here, it means we have the right row
			// inrement and return, we are done
			row.incrementCount();
			return;
		}
		
		// if it reaches here, it means we did not find a match, so this is a
		// new row
		Set<ReportColumn> cols = new HashSet<ReportColumn>();
		for (String colName : metrics.keySet()) {
			cols.add(new ReportColumn(colName, metrics.get(colName)));
		}
		rows.add(new ReportRow(cols));
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("REPORT {");
		sb.append("Project : " + project + ", ");
		sb.append("Service : " + service + ", ");
		sb.append("Instance : " + instance + ", ");
		sb.append("Report Fields : [");
		for (String field : reportFields) {
			sb.append(field + ", ");
		}
		sb.delete(sb.length()-2,sb.length());
		sb.append("]}");
		
		return sb.toString();
	}
}
