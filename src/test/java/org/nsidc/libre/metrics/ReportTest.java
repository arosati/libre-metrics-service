package org.nsidc.libre.metrics;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.BeforeClass;
import org.junit.Test;
import org.nsidc.libre.metrics.model.report.Report;
import org.nsidc.libre.metrics.model.report.ReportColumn;
import org.nsidc.libre.metrics.model.report.ReportRow;
import org.postgresql.ds.PGPoolingDataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReportTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
		// rcarver - setup the jndi context and the datasource
		// Create initial context

		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
		System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
		InitialContext ic = new InitialContext();

		ic.createSubcontext("jdbc");
		
		// Construct DataSource
		PGPoolingDataSource ds = new PGPoolingDataSource();
		

		
		ds.setServerName("yoursqlserver");
		ds.setPortNumber(5433);
		ds.setDatabaseName("metrics");
		ds.setUser("user");
		ds.setPassword("password");


		ic.bind("jdbc/libre", ds);

	}

	@Test
	public void testReportNoDates() {
		String project = "metrics";
		String serviceName = "opensearch";
		String instance = "YOURINSTANCENAME";
		List<PathSegment> fields = new ArrayList<PathSegment>();
		PathSegment mockUserSegment = mock(PathSegment.class);
		when(mockUserSegment.getPath()).thenReturn("users");
		fields.add(mockUserSegment);
		
		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> mockParams = mock(MultivaluedMap.class);
		UriInfo ui = mock(UriInfo.class);
		when(ui.getQueryParameters()).thenReturn(mockParams);
		
		MetricServiceImpl metric = new MetricServiceImpl();

		Report r = metric.getReport(project, serviceName, instance, fields, ui);

		int total = 0;
		for (ReportRow rr : r.getRows()) {
			for (ReportColumn rc : rr.getColumns()) {
				if (rc.getName().equals("total") && rc.getValue().equals("total")) {
					total = rr.getCount();
					break;
				}
			}
			if (total > 0) break;
		}
		System.out.println("Total : " + total);
		assertTrue(r.getRows().size() > 0);
	}
	
	@Test
	public void testReportAug2011() {
		String project = "metrics";
		String serviceName = "opensearch";
		String instance = "YOURINSTANCENAME";
		List<PathSegment> fields = new ArrayList<PathSegment>();
		PathSegment mockUserSegment = mock(PathSegment.class);
		when(mockUserSegment.getPath()).thenReturn("users");
		fields.add(mockUserSegment);
		
		@SuppressWarnings("unchecked")
		MultivaluedMap<String, String> mockParams = mock(MultivaluedMap.class);
		when(mockParams.get("start")).thenReturn(Arrays.asList(new String[] {"2011-08-01"}));
		when(mockParams.get("stop")).thenReturn(Arrays.asList(new String[] {"2011-09-01"}));
		UriInfo ui = mock(UriInfo.class);
		when(ui.getQueryParameters()).thenReturn(mockParams);
		
		MetricServiceImpl metric = new MetricServiceImpl();

		Report r = metric.getReport(project, serviceName, instance, fields, ui);
		
		int total = 0;
		for (ReportRow rr : r.getRows()) {
			for (ReportColumn rc : rr.getColumns()) {
				if (rc.getName().equals("total") && rc.getValue().equals("total")) {
					total = rr.getCount();
					break;
				}
			}
			if (total > 0) break;
		}
		System.out.println("Total : " + total);

		assertTrue(r.getRows().size() > 0);
	}
}
