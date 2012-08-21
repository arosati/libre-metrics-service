/*National Snow & Ice Data Center, University of Colorado, Boulder
  Copyright 2010 Regents of the University of Colorado*/
package org.nsidc.metrics.model;

import java.util.Set;

import junit.framework.TestCase;

import org.nsidc.libre.metrics.model.IpAddressClass;

/**
 * @author glewis
 *
 */
public class TestIpAddressClass extends TestCase {

	// Check to see if Non-US Domains work
	public void testNonUs() {
		// This address should be NON_US
		String hostname = "pc054.seg15.nipr.ac.jp";	// IP address 133.57.15.54
		System.out.println("Testing host " + hostname + ", expecting NON_US entry");
		Set<IpAddressClass> domains = IpAddressClass.getMatchingDomains(hostname);
		System.out.println("Domains : ");
		for (IpAddressClass iac : domains) {
			System.out.println(iac);
		}
		assertTrue(domains.contains(IpAddressClass.NON_US));
		
		System.out.println("");
		// This address should NOT be NON_US
		hostname = "kayak.colorado.edu"; // IP address is 128.138.135.12
		System.out.println("Testing host " + hostname + ", NOT expecting NON_US entry");
		domains = IpAddressClass.getMatchingDomains(hostname);
		System.out.println("Domains : ");
		for (IpAddressClass iac : domains) {
			System.out.println(iac);
		}
		assertFalse(domains.contains(IpAddressClass.NON_US));
		
	}
}
