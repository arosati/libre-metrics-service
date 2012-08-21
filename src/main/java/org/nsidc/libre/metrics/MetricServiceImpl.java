package org.nsidc.libre.metrics;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;

import org.apache.log4j.Logger;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.nsidc.libre.metrics.model.DateAdapter;
import org.nsidc.libre.metrics.model.FieldList;
import org.nsidc.libre.metrics.model.IpAddressClass;
import org.nsidc.libre.metrics.model.Metric;
import org.nsidc.libre.metrics.model.ObjectFactory;
import org.nsidc.libre.metrics.model.Sample;
import org.nsidc.libre.metrics.model.Service;
import org.nsidc.libre.metrics.model.report.Report;
import org.xbill.DNS.Address;

import com.sun.jersey.core.util.MultivaluedMapImpl;

@Path("")
public class MetricServiceImpl {

	private static Logger logger = Logger.getLogger(MetricServiceImpl.class);
	
	private static String pattern = "yyyy-MM-dd";
	private static String ipPatternStr = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
	 
	public ApplicationService applicationService = null;
	private Pattern ipPattern;

	@SuppressWarnings("unused")
	@Context
	private UriInfo context;

	@Context
	protected Providers _providers;

	@Provider
	public static class JAXBContextResolver implements
			ContextResolver<JAXBContext> {

		private JAXBContext context;
		private Class<?>[] types = { Sample.class, Service.class, Metric.class };

		public JAXBContextResolver() throws Exception {
			Map<String, Object> properties = new HashMap<String, Object>();
			this.context = JAXBContextFactory.createContext(types, properties);
		}

		public JAXBContext getContext(Class<?> objectType) {
			int numTypes = types.length;
			for (int i = 0; i < numTypes; i++) {
				if (types[i].equals(objectType)) {
					return context;
				}
			}
			return null;
		}
	}

	/**
	 * Default constructor.
	 */
	public MetricServiceImpl() {
		applicationService = new ApplicationService();
		ipPattern = Pattern.compile(ipPatternStr);
	}

	/**
	 * Log a sample to the metrics log file, used temporarily while we work on
	 * getting a more formalized DB in place
	 * 
	 * @param sample
	 */
	private void writeSampleToLog(Sample sample) {
		logger.info(sample.toString());
	}

	/**
	 * Record the sample to the metrics database
	 * 
	 * @param sample
	 */
	private void saveSampleToDatabase(Sample sample) {
		Service service = sample.getService();

		for (Metric m : sample.getMetrics()) {
			m.setSample(sample);
		}

		EntityManager entityManager = applicationService.getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();

			TypedQuery<Service> q = entityManager.createQuery(
					"SELECT svc FROM Service svc "
							+ "WHERE svc.serviceName = :serviceName "
							+ "AND svc.instance = :instance "
							+ "AND svc.sponsor = :sponsor", Service.class);

			q.setParameter("serviceName", service.getServiceName());
			q.setParameter("instance", service.getInstance());
			q.setParameter("sponsor", service.getSponsor());

			List<Service> l = q.getResultList();
			if (l.size() > 0) {
				sample.setService(l.get(0));
			}
			entityManager.persist(sample);

			transaction.commit();
		} finally {
			if (transaction.isActive()) {
				transaction.rollback();
			}

			entityManager.close();
		}
	}

	/**
	 * Find an existing sample matching the specified criteria and return it; if
	 * no match is found, null is returned
	 * 
	 * @param sampleId
	 *            The Sample ID
	 * @param project
	 *            The name of the project
	 * @param service
	 *            The name of the service
	 * @param instance
	 *            The name of the instance of the service
	 * @return A Sample object if one is found that matches, otherwise null
	 */
	private Sample findExistingSample(Integer sampleId, String project,
			String service, String instance) {
		EntityManager entityManager = applicationService.getEntityManager();
		Sample sample = null;

		TypedQuery<Sample> q = entityManager.createQuery(
				"SELECT sample FROM Sample sample "
						+ "INNER JOIN sample.service svc "
						+ "WHERE sample.id = :sampleId "
						+ "AND svc.serviceName = :serviceName "
						+ "AND svc.instance = :instance "
						+ "AND svc.sponsor = :sponsor", Sample.class);

		q.setParameter("sampleId", sampleId);
		q.setParameter("serviceName", service);
		q.setParameter("instance", instance);
		q.setParameter("sponsor", project);

		List<Sample> l = q.getResultList();
		if (l.size() > 0) {
			sample = l.get(0);
		}

		return sample;
	}
	
	
	private List<Sample> getReportSamples(String project, String service, String instance, Date startDate, Date stopDate) {
		EntityManager entityManager = applicationService.getEntityManager();
		String query = "SELECT sample " 
			+ "FROM Sample sample "
			+ "INNER JOIN sample.service svc "
			+ "WHERE svc.serviceName = :serviceName "
			+ "AND svc.instance = :instance "
			+ "AND svc.sponsor = :sponsor";
		
		boolean useDates = false;
		if (startDate != null && stopDate != null && !startDate.after(stopDate)) {
			useDates = true;
			query += " AND sample.entryTs BETWEEN :startDate AND :stopDate";
		}
		
		TypedQuery<Sample> q = entityManager.createQuery(query, Sample.class);
		
		q.setParameter("serviceName", service);
		q.setParameter("instance", instance);
		q.setParameter("sponsor", project);
		
		if (useDates) {
			q.setParameter("startDate", startDate);
			q.setParameter("stopDate", stopDate);
		}
		
		List<Sample> samples = q.getResultList();
		
		return samples;
	}
	
	
	private void buildReport(String project, String service, String instance, Date startDate, Date stopDate, List<String> fields, Report report) {
		List<Sample> samples = getReportSamples(project, service, instance, startDate, stopDate);
		int count = 0;
		int cacheHits = 0;
		int cacheMisses = 0;
		Map<String, String> hostNameCache = new HashMap<String, String>();
		for (Sample s : samples) {
			count++;
			logger.debug("Sample count: " + count + ", " + s.getId());
			Map<String, String> fieldMetrics = new HashMap<String, String>();
			Set<IpAddressClass> iacs = null;
			for (String field : fields) {
				if (field.equals("users")) {
					// user field is a special case
					String user = s.getIpAddress();
					logger.debug("Looking for user...");
					if (user == null || user.isEmpty()) {
						logger.debug("Failed to find user based in IP Address field, looking in Host field");
						String host = s.getHost();
						Matcher m = ipPattern.matcher(host);
						if (m.matches()) {
							try {
								String hostName;
								if (hostNameCache.containsKey(host)) {
									hostName = hostNameCache.get(host);
									cacheHits++;
									logger.debug("Found host name in cache...");
								}
								else {
									logger.debug("Host name not in cache, looking up...");
									InetAddress inet = Address.getByAddress(host);
									hostName = inet.getHostName();
									hostNameCache.put(host, hostName);
									cacheMisses++;
									logger.debug("Done with lookup, adding to cache...");
								}
								user = hostName;					
							} catch (UnknownHostException uhe) {
								user = host;
							}
						}
					} else {
						try {
							if (hostNameCache.containsKey(user)) {
								logger.debug("Found use IP address in cache...");
								user = hostNameCache.get(user);
								cacheHits++;
							} else {
								logger.debug("User IP Address not in cache, looking up...");
								InetAddress inet = Address.getByAddress(user);
								String hostName = inet.getHostName();
								hostNameCache.put(user, hostName);
								user = hostName;
								cacheMisses++;
								logger.debug("Done with lookup, adding to cache...");
							}
						} catch (UnknownHostException uhe) {
							// No change
						}
					}
					iacs = IpAddressClass.getMatchingDomains(user);
					logger.debug("Found user...");
				} else {
					// field other than user
					Metric m = s.findMetricByName(field);
					if (m != null)
						fieldMetrics.put(m.getName(), m.getValue());
				}
			}
			
			if (fieldMetrics.size() == fields.size()) {
				// no user field
				report.addMetric(fieldMetrics);
				logger.trace("Found all needed metrics, recording information...");
			} else if (fieldMetrics.size() == fields.size()-1 && iacs != null) {
				// user field
				for (IpAddressClass iac : iacs) {
					Map<String, String> metrics = new HashMap<String, String>(fieldMetrics);
					metrics.put("domain", iac.getDomain());
					report.addMetric(metrics);
					logger.trace("Found needed metrics, adding information for domain class " + iac.getDomain());
				}
			} else {
				// this means that not all the requested metrics were found, so ignore this row
				continue;
			}
			
			logger.debug("Adding metric");
			
			Map<String, String> totalMetric = new HashMap<String, String>();
			totalMetric.put("total", "total");
			report.addMetric(totalMetric);
			logger.trace("Adding sample information for sample id " + s.getId());
		}
		
		logger.debug("Cache Hits: " + cacheHits + ", Cache Misses: " + cacheMisses);
	}
	
	
	private List<String> getReportableFields(String project, String service, String instance, Date startDate, Date stopDate) {
		EntityManager entityManager = applicationService.getEntityManager();
		String query = "SELECT DISTINCT metrics.name " 
			+ "FROM Sample sample "
			+ "INNER JOIN sample.service svc "
			+ "INNER JOIN sample.metrics metrics "
			+ "WHERE svc.serviceName = :serviceName "
			+ "AND svc.instance = :instance "
			+ "AND svc.sponsor = :sponsor "
			+ "AND metrics.public_ = TRUE";
		
		boolean useDates = false;
		if (startDate != null && stopDate != null && !startDate.after(stopDate)) {
			useDates = true;
			query += " AND sample.entryTs BETWEEN :startDate AND :stopDate";
		}
		
		Query q = entityManager.createQuery(query);
		
		q.setParameter("serviceName", service);
		q.setParameter("instance", instance);
		q.setParameter("sponsor", project);
		
		if (useDates) {
			q.setParameter("startDate", startDate);
			q.setParameter("stopDate", stopDate);
		}
		
		@SuppressWarnings("unchecked")
		List<String> fields = q.getResultList();
		
		return fields;
	}

	
	/**
	 * Successful recording of metrics which will return a response with the
	 * specified content
	 * 
	 * @param content
	 * @return
	 */
	private Response recordSuccessful(String content) {
		Response res;
		res = Response.ok(content).type(MediaType.TEXT_PLAIN).build();
		return res;
	}
	

	/**
	 * A helper function to create a new sample from a form request
	 * 
	 * @param hsr
	 *            A servlet request object with client info
	 * @param form
	 *            A map containing various form fields
	 * @return A sample object
	 */
	private Sample createNewSample(HttpServletRequest hsr,
			MultivaluedMap<String, String> form) {
		ObjectFactory objectFactory = new ObjectFactory();

		Sample sample = objectFactory.createSample();

		/*
		 * This should be removed prior to going "live" The user agent (browser)
		 * information should be required
		 */
		if (form != null) {
			sample.setHost(form.getFirst("host"));
			sample.setIpAddress(form.getFirst("ipAddress"));
			sample.setSessionId(form.getFirst("sessionId"));
			sample.setUserAgent(form.getFirst("userAgent"));
		}
		checkSampleAgentInfo(hsr, sample);

		String ts = form.getFirst("entryTs");
		Date d;
		try {
			DateAdapter da = new DateAdapter();
			d = da.unmarshal(ts);
		} catch (Exception e) {
			d = new Date();
		}
		sample.setEntryTs(d);

		return sample;
	}

	/**
	 * Check the sample object for Agent information. If it is not there,
	 * populate it using the Servlet Request object.
	 * 
	 * @param hsr
	 *            the servlet request object containing client information
	 * @param sample
	 *            the sample object to check/update
	 */
	private void checkSampleAgentInfo(HttpServletRequest hsr, Sample sample) {
		// If the post did not contain an agent, retrieve it from the servlet
		// context
		if (sample.getHost() == null) {
			sample.setHost(hsr.getRemoteHost());
		}
		if (sample.getIpAddress() == null) {
			sample.setIpAddress(hsr.getRemoteAddr());
		}
		if (sample.getSessionId() == null) {
			sample.setSessionId(hsr.getSession().getId());
		}
		if (sample.getUserAgent() == null) {
			sample.setUserAgent(hsr.getHeader("User-Agent"));
		}
	}

	/**
	 * POST method for recording a sample (with many metrics) using an xml input
	 * 
	 * @param hsr
	 *            the servlet request object with client information
	 * @param sample
	 *            the sample data parsed from the xml
	 * @return an HTTP response with content of the updated or created resource.
	 */
	@POST
	@Path("projects/{project}/services/{service}/instances/{instance}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	//public Response recordSample(@PathParam("project") String project,
	public Sample recordSample(@PathParam("project") String project,
			@PathParam("service") String serviceName,
			@PathParam("instance") String instance,
			@Context HttpServletRequest hsr, Sample sample) {

		for (Metric m : sample.getMetrics()) {
			//System.out.println(m.getName() + "=" + m.getValue());
			logger.info(m.getName() + "=" + m.getValue());
		}

		/*
		 * This should be removed prior to going "live" The user agent (browser)
		 * information should be required??
		 */
		checkSampleAgentInfo(hsr, sample);

		// For this method of entering samples, service info in the XML is ignored (and overwritten)
		Service service = sample.getService();
		if (service == null) {			
			service = new Service();
			sample.setService(service);
		}
		service.setSponsor(project);
		service.setServiceName(serviceName);
		service.setInstance(instance);

		writeSampleToLog(sample);
		saveSampleToDatabase(sample);

		//return recordSuccessful("sample saved to database!!!");
		return sample;
	}

	/**
	 * Record a single metric to a new sample.
	 * 
	 * @param project
	 *            The project for the service
	 * @param serviceName
	 *            The name of the service
	 * @param instance
	 *            The instance of the service
	 * @param hsr
	 *            The Server Request object for getting client info
	 * @param form
	 *            The form containing the metric information
	 * @return A response with the id of the new sample created (useful for
	 *         creating multiple metrics for the same sample later)
	 */
	@POST
	@Path("projects/{project}/services/{service}/instances/{instance}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response recordMetric(@PathParam("project") String project,
			@PathParam("service") String serviceName,
			@PathParam("instance") String instance,
			@Context HttpServletRequest hsr, MultivaluedMap<String, String> form) {

		ObjectFactory objectFactory = new ObjectFactory();

		Sample sample = createNewSample(hsr, form);

		// Record the service information
		Service service = objectFactory.createService();
		service.setServiceName(serviceName);
		service.setSponsor(project);
		service.setInstance(instance);
		sample.setService(service);

		// Add the metric
		Metric metric = objectFactory.createMetric();
		metric.setName(form.getFirst("name"));
		metric.setValue(form.getFirst("value"));
		metric.setPublic_(!form.getFirst("public").equals("false"));
		metric.setSample(sample);
		List<Metric> metrics = new ArrayList<Metric>();
		metrics.add(metric);
		sample.setMetrics(metrics);

		// Save the metric
		writeSampleToLog(sample);
		saveSampleToDatabase(sample);

		return recordSuccessful(sample.getId().toString());
	}

	/**
	 * Record a single metric to an existing sample; If the sample doesn't
	 * exist, a new sample will be created For "security", the sample ID must
	 * also correlate to the project, service, and instance information
	 * 
	 * @param project
	 *            The project for the service
	 * @param serviceName
	 *            The name of the service
	 * @param instance
	 *            The instance of the service
	 * @param hsr
	 *            The Server Request object for getting client info
	 * @param form
	 *            The form containing the metric information
	 * @return A response with the id of the sample (useful for creating
	 *         multiple metrics for the same sample later). Note that if it
	 *         writes to an existing sample, the id for that existing sample
	 *         will be returned.
	 */
	@POST
	@Path("projects/{project}/services/{service}/instances/{instance}/samples/{sampleId}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response recordAnotherMetric(@PathParam("project") String project,
			@PathParam("service") String serviceName,
			@PathParam("instance") String instance,
			@PathParam("sampleId") Integer sampleId,
			@Context HttpServletRequest hsr, MultivaluedMap<String, String> form) {

		ObjectFactory objectFactory = new ObjectFactory();

		// Find the sample or create a new one
		Sample sample = findExistingSample(sampleId, project, serviceName,
				instance);
		if (sample == null) {
			sample = createNewSample(hsr, form);

			// Record the service information for the new sample
			Service service = objectFactory.createService();
			service.setServiceName(serviceName);
			service.setSponsor(project);
			service.setInstance(instance);
			sample.setService(service);
			sample.setMetrics(new ArrayList<Metric>());
		}

		// Add the metric
		Metric metric = objectFactory.createMetric();
		metric.setName(form.getFirst("name"));
		metric.setValue(form.getFirst("value"));
		metric.setPublic_(!form.getFirst("public").equals("false"));
		metric.setSample(sample);
		sample.getMetrics().add(metric);

		writeSampleToLog(sample);
		saveSampleToDatabase(sample);

		return recordSuccessful(sample.getId().toString());
	}
	
	@GET
	@Path("projects/{project}/services/{service}/instances/{instance}/reports/{reportField : .+}")
	@Produces(MediaType.APPLICATION_XML)
	public Report getReport(@PathParam("project") String project,
			@PathParam("service") String serviceName,
			@PathParam("instance") String instance,
			@PathParam("reportField") List<PathSegment> fields,
			@Context UriInfo ui) {
		
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		List<String> reportFields = new ArrayList<String>();
		for (PathSegment ps : fields) {
			reportFields.add(ps.getPath());
		}
		
		String startDateStr = queryParams.get("start") != null ? queryParams.get("start").get(0) : null;
		String stopDateStr = queryParams.get("stop") != null ? queryParams.get("stop").get(0) : null;
		
		logger.info("Start Date: " + startDateStr + "\nStop Date: " + stopDateStr);
		
		Map<String, String> params = new HashMap<String, String>();
		
		Date startDate = null;
		Date stopDate = null;
		if (startDateStr != null && stopDateStr != null && !startDateStr.isEmpty() && !stopDateStr.isEmpty()) {
			try {
				startDate = new SimpleDateFormat(pattern).parse(startDateStr);
				stopDate = new SimpleDateFormat(pattern).parse(stopDateStr);
				params.put("start", startDateStr);
				params.put("stop", stopDateStr);
			} catch (ParseException pe) {
				logger.error("Error parsing dates: ", pe);
				startDate = null;
				stopDate = null;
			}
		}
		
		Report r = new Report(project, serviceName, instance, reportFields, params);
		
		buildReport(project, serviceName, instance, startDate, stopDate, reportFields, r);
		
		return r;
	}
	
	
	@GET
	@Path("projects/{project}/services/{service}/instances/{instance}/fields")
	@Produces(MediaType.APPLICATION_XML)
	public FieldList getFields(@PathParam("project") String project,
			@PathParam("service") String serviceName,
			@PathParam("instance") String instance,
			@Context UriInfo ui) {
	
		String startDateStr = null;
		String stopDateStr = null;
		
		if (ui != null) {
			MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
			
			startDateStr = queryParams.get("start") != null ? queryParams.get("start").get(0) : null;
			stopDateStr = queryParams.get("stop") != null ? queryParams.get("stop").get(0) : null;
		}
		
		logger.info("Start Date: " + startDateStr + "\nStop Date: " + stopDateStr);
		
		Date startDate = null;
		Date stopDate = null;
		if (startDateStr != null && stopDateStr != null && !startDateStr.isEmpty() && !stopDateStr.isEmpty()) {
			try {
				startDate = new SimpleDateFormat(pattern).parse(startDateStr);
				stopDate = new SimpleDateFormat(pattern).parse(stopDateStr);
			} catch (ParseException pe) {
				logger.error("Error parsing dates: ", pe);
				startDate = null;
				stopDate = null;
			}
		}
		
		List<String> fields = getReportableFields(project, serviceName, instance, startDate, stopDate);
		
		FieldList fl = new FieldList();
		fl.addField("users");
		for (String field : fields) {
			fl.addField(field);
		}
		
		return fl;
	}
	

	/**
	 * A test method to create a sample object and log it, returning it in XML
	 * form
	 * 
	 * @return a Sample object, which will be converted to XML
	 */
	@GET
	@Path("test")
	@Produces(MediaType.APPLICATION_XML)
	public Sample test() {
		ObjectFactory objectFactory = new ObjectFactory();
		Sample sample = objectFactory.createSample();

		Metric metric = objectFactory.createMetric();
		metric.setName("someMetricName");
		metric.setValue("someValue");

		Metric metric2 = objectFactory.createMetric();
		metric2.setName("anotherMetricName");
		metric2.setValue("anotherValue");

		List<Metric> metrics = new ArrayList<Metric>();
		metrics.add(metric);
		metrics.add(metric2);

		Service service = objectFactory.createService();
		service.setInstance("MyInstance");
		service.setServiceName("aService");
		service.setSponsor("ThatSponsor");

		Calendar calendar = Calendar.getInstance();
		sample.setEntryTs(calendar.getTime());
		sample.setIpAddress("1.2.3.256");
		sample.setSessionId("MySession");
		sample.setUserAgent("AgentMan");
		sample.setMetrics(metrics);
		sample.setService(service);

		writeSampleToLog(sample);

		return sample;

	}

	// TODO: Try and fix this, it does not work, returns a "not supported yet"
	// error
	/**
	 * A test method to create a sample object and log it, returning it in XML
	 * form
	 * 
	 * @return a Sample object, which will be converted to XML
	 */
	@GET
	@Path("testjson")
	@Produces(MediaType.APPLICATION_JSON)
	public Sample testJson() {
		ObjectFactory objectFactory = new ObjectFactory();
		Sample sample = objectFactory.createSample();

		Metric metric = objectFactory.createMetric();
		metric.setName("someMetricName");
		metric.setValue("someValue");

		Metric metric2 = objectFactory.createMetric();
		metric2.setName("anotherMetricName");
		metric2.setValue("anotherValue");

		List<Metric> metrics = new ArrayList<Metric>();
		metrics.add(metric);
		metrics.add(metric2);

		Service service = objectFactory.createService();
		service.setInstance("MyInstance");
		service.setServiceName("aService");
		service.setSponsor("ThatSponsor");

		Calendar calendar = Calendar.getInstance();
		sample.setEntryTs(calendar.getTime());
		sample.setIpAddress("1.2.3.256");
		sample.setSessionId("MySession");
		sample.setUserAgent("AgentMan");
		sample.setMetrics(metrics);
		sample.setService(service);

		writeSampleToLog(sample);

		return sample;
	}

	// TODO: Try and fix this, it does not work, returns a "not supported yet"
	// error
	/**
	 * A test method to create a sample object and log it, returning it in XML
	 * form
	 * 
	 * @return a Sample object, which will be converted to XML
	 */
	@GET
	@Path("testmetric")
	@Produces(MediaType.APPLICATION_JSON)
	public Metric testMetric() {
		ObjectFactory objectFactory = new ObjectFactory();

		Metric metric = objectFactory.createMetric();
		metric.setName("JSON Metric Name");
		metric.setValue("some JSON Value");

		return metric;
	}
	
	
	@GET
	@Path("testfields")
	@Produces(MediaType.APPLICATION_XML)
	public FieldList testFields() {
		FieldList fl = new FieldList();
		
		fl.addField("Field 1");
		fl.addField("Another Field");
		fl.addField("Last and Final Field");
			
		return fl;
	}
	

	@GET
	@Path("proxy")
	@Produces(MediaType.TEXT_HTML)
	public String metricProxy(@QueryParam("project") String project,
			@QueryParam("service") String serviceName,
			@QueryParam("instance") String instance,
			@Context HttpServletRequest hsr,
			@QueryParam("metrics") String metrics) {

		MultivaluedMap<String, String> metricMapForm = new MultivaluedMapImpl();
		ObjectFactory objectFactory = new ObjectFactory();

		Sample sample = createNewSample(hsr, metricMapForm);

		// Record the service information
		Service service = objectFactory.createService();
		service.setServiceName(serviceName);
		service.setSponsor(project);
		service.setInstance(instance);
		sample.setService(service);

		// Add the metrics
		List<Metric> metricList = new ArrayList<Metric>();
		for (String param : metrics.split("&")) {
			String[] pair = param.split("=");
			String key = pair[0];
			String value = pair[1];
		
			Metric metric = objectFactory.createMetric();		
			metric.setName(key);
			metric.setValue(value);
			metric.setPublic_(true);
			
			metric.setSample(sample);			
			metricList.add(metric);
		}
		sample.setMetrics(metricList);

		// Save the metric
		writeSampleToLog(sample);
		saveSampleToDatabase(sample);
		

		return "<html><body>empty</body></html>";
	}
}
