/*National Snow & Ice Data Center, University of Colorado, Boulder
  Copyright 2010 Regents of the University of Colorado*/
package org.nsidc.libre.metrics.model;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.nsidc.libre.metrics.model.IpAddressClass;

import net.sf.javainetlocator.InetAddressLocator;
import net.sf.javainetlocator.InetAddressLocatorException;

/**
 * @author glewis
 *
 */
public enum IpAddressClass {

	NASA_GOV("nasa.gov", false, true),
	USGS_GOV("usgs.gov", false, true),
	NOAA_GOV("noaa.gov", false, true),
	EPA_GOV("epa.gov", false, true),
	NGA_GOV("nga.gov", false, true),
	USDA_GOV("usda.gov", false, true),
	DOT_GOV("dot.gov", false, true),
	GOV(".gov", false, true),
	MIL(".mil", false, true),
	US(".us", false, true),
	EDU(".edu"),
	ORG(".org"),
	COM(".com"),
	NET(".net"),
	NON_US("non-US", true),
	OTHER_FEDERAL("other federal", true),
	UNRESOLVED("unresolved", true);
	
	private String domain;
	private boolean special = false;
	private boolean federal = false;
	
	private IpAddressClass(String domain) {
		this(domain, false);
	}
	
	private IpAddressClass(String domain, boolean special) {
		this(domain, special, false);
	}
	
	private IpAddressClass(String domain, boolean special, boolean federal) {
		this.domain = domain;
		this.special = special;
		this.federal = federal;
	}
	
	public boolean isSpecial() {
		return special;
	}
	
	public boolean isFederal() {
		return federal;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public String toString() {
		return domain;
	}
	
	public static Set<IpAddressClass> getMatchingDomains(String host) {
		Set<IpAddressClass> doms = new HashSet<IpAddressClass>();
		
		// Find all matching domains
		for (IpAddressClass c : IpAddressClass.values()) {
			// Special ones are handled individually
			if (c.isSpecial()) 
				continue;
			
			// If the host ends with this domain, add it
			if (host.endsWith(c.getDomain()))
				doms.add(c);
		}
		
		// Check for non-us domains
		try {
			Locale l = InetAddressLocator.getLocale(host);
			if (!l.getCountry().equals("US")) {
				doms.add(NON_US);
			}
		} catch (InetAddressLocatorException e) {
			// Do nothing, DB is corrupt and we can't get locale info
		}
		
		// Check for other federal domains
		// "other federal" is one with the basic federal domains, but not a specific sub-domain
		int numFed = 0;
		for (IpAddressClass dom : doms) {
			if (dom.isFederal()) numFed++;
		}
		// An "other federal" domain will be one that matches exactly ONE federal class.
		// If it matches two or more, it must match a sub-domain also
		if (numFed == 1) {
			doms.add(OTHER_FEDERAL);
		}		
		
		// If no domains were found that match, add the unresolved domain
		if (doms.isEmpty()) 
			doms.add(UNRESOLVED);
		
		return doms;
	}
}
