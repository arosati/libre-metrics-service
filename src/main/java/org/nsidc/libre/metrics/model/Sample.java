package org.nsidc.libre.metrics.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import org.nsidc.libre.metrics.model.DateAdapter;
import org.nsidc.libre.metrics.model.Metric;
import org.nsidc.libre.metrics.model.Service;


/**
 * The persistent class for the sample database table.
 * 
 */
@Entity
@Table(name="sample")
@XmlRootElement
public class Sample implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true, nullable=false)
	@XmlID
	@XmlAttribute
	private Integer id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="entry_ts")
	private Date entryTs;

	@Column(name="host", length=80)
	private String host;

	@Column(name="ip_address", length=80)
	private String ipAddress;

	@Column(name="session_id", length=254)
	private String sessionId;
	
	@Column(name="user_agent", length=254)
	private String userAgent;

	//bi-directional many-to-one association to Metric
	@OneToMany(mappedBy="sample", cascade={CascadeType.ALL})
	private List<Metric> metrics;

	//uni-directional many-to-one association to Service
    @ManyToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="service_id")	
	private Service service;

    public Sample() {
    }

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getEntryTs() {
		return this.entryTs;
	}

	public void setEntryTs(Date entryTs) {
		this.entryTs = entryTs;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getIpAddress() {
		return this.ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getUserAgent() {
		return this.userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public List<Metric> getMetrics() {
		return this.metrics;
	}

	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}
	
	public Service getService() {
		return this.service;
	}

	public void setService(Service service) {
		this.service = service;
	}
	
	
	public Metric findMetricByName(String metricName) {
		for (Metric m : metrics) {
			if (m.getName().equals(metricName))
				return m;
		}
		
		return null;	// it's not found if it gets here
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SAMPLE: {");
		try {
			sb.append("Timestamp : " + new DateAdapter().marshal(entryTs) + ", ");
		} catch (Exception e) {
			sb.append("Timestamp : " + entryTs + ", ");
		}
		sb.append("Host : " + host + ", ");
		sb.append("IP Address : " + ipAddress + ", ");
		sb.append("Agent : " + userAgent + ", ");
		sb.append("Session ID: " + sessionId + ", ");
		sb.append("Service : " + service + ", ");
		
		sb.append("Metrics : [\n");
		boolean first = true;
		for (Metric m : metrics) {
			if (!first) sb.append(",\n");
			sb.append("\t" + m);
			first = false;
		}
		
		sb.append("]}");
		
		return sb.toString();
	}
	
}