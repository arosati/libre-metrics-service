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
public class ReportMetricAdapter extends XmlAdapter<ReportMetricList, Map<String, Integer>> {
	private static Logger logger = Logger.getLogger(ReportMetricAdapter.class);
		
	/* (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
	 */
	@Override
	public Map<String, Integer> unmarshal(ReportMetricList v) throws Exception {
		Map<String, Integer> metricMap = new HashMap<String, Integer>();
		for (ReportColumn rv : v.getReportValues()) {
			//metricMap.put(rv.getMetric(), rv.getCount());
		}
		
		return metricMap;
	}

	/* (non-Javadoc)
	 * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
	 */
	@Override
	public ReportMetricList marshal(Map<String, Integer> v) throws Exception {
		logger.trace("Marshalling values...");
		
		ReportMetricList metrics = new ReportMetricList();
		for (String key : v.keySet()) {
			logger.trace("Adding value: " + key + ", " + v.get(key));
			//metrics.addReportValue(new ReportColumn(key, v.get(key)));
		}
		
		return metrics;
	}

}
