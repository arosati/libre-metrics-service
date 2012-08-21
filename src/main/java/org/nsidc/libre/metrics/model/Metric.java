package org.nsidc.libre.metrics.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

import org.nsidc.libre.metrics.model.Sample;


/**
 * The persistent class for the metric database table.
 * 
 */
@Entity
@Table(name="metric")
@XmlRootElement
public class Metric implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(unique=true, nullable=false)
	@XmlID
	private Integer id;

	@Column(length=128)
	private String name;

	@Column(name="public")
	private Boolean public_ = true;

	@Column(length=2147483647)
	private String value;

	//bi-directional many-to-one association to Sample
	@ManyToOne(cascade={CascadeType.ALL})
	@JoinColumn(name="sample_id")
    @XmlIDREF	
	private Sample sample;

    public Metric() {
    }

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getPublic_() {
		return this.public_;
	}

	public void setPublic_(Boolean public_) {
		this.public_ = public_;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Sample getSample() {
		return this.sample;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("METRIC {");
		sb.append("Name : " + name + ", ");
		sb.append("Value : " + this.value + ", ");
		sb.append("Public : " + public_ + "}");
		
		return sb.toString();
	}
	
}