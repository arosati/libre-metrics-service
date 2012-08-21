package org.nsidc.libre.metrics.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlID;


/**
 * The persistent class for the serviceName database table.
 * 
 */
@Entity
@Table(name="service",
		uniqueConstraints={@UniqueConstraint(columnNames={"service_name","instance_name","sponsor_name"})})
public class Service implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true, nullable=false)
	@XmlID
	private Integer id; 

	@Column(name="instance_name", length=80)
	private String instance;

	@Column(name="service_name", length=45)
	private String serviceName;

	@Column(name="sponsor_name", length=80)
	private String sponsor;
	
    public Service() {
    }

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getInstance() {
		return this.instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getSponsor() {
		return this.sponsor;
	}

	public void setSponsor(String sponsor) {
		this.sponsor = sponsor;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SERVICE: {");
		sb.append("Service Name : " + serviceName + ", ");
		sb.append("Instance : " + instance + ", ");
		sb.append("Sponsor : " + sponsor + "}");
		
		return sb.toString();
	}

}