package org.nsidc.libre.metrics.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.nsidc.libre.metrics.model.Sample;


@XmlRootElement(name="samples")
public class Root {

	private Sample sample;

	public Sample getSample() {
		return sample;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}
	
}
