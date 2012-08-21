package org.nsidc.libre.metrics;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.xbill.DNS.ARecord;
import org.xbill.DNS.Address;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

import junit.framework.TestCase;

public class TestDNS extends TestCase {
	
	public void testDNS() {
		
		try {
			Lookup lookup = new Lookup("130.16.233.72.static.reverse.ltdomains.com", Type.ANY);
			Resolver resolver = new SimpleResolver();
			lookup.setResolver(resolver);
			lookup.setCache(null);
			Record[] records = lookup.run();
			if(lookup.getResult() == Lookup.SUCCESSFUL) {
				String responseMessage = null;
				String listingType = null;
				for (int i = 0; i < records.length; i++) {
					if(records[i] instanceof TXTRecord) {
						TXTRecord txt = (TXTRecord) records[i];
						
						@SuppressWarnings("unchecked")
						List<String> strings = new ArrayList<String>(txt.getStrings());
						
						for (String j : strings) {
							responseMessage += j;
						}
					}
					else if(records[i] instanceof ARecord) {
						listingType = ((ARecord)records[i]).getAddress().getHostAddress();
					}
				}

				System.err.println("Found!");
				System.err.println("Response Message: " + responseMessage);
				System.err.println("Listing Type: " + listingType);
		    }
			else if(lookup.getResult() == Lookup.HOST_NOT_FOUND) {
				System.err.println("Not found.");
			}
			else {
				System.err.println("Error!");
			}
		} catch (Throwable t) {
			
		}
	}
	
	public void testDNS2() {
		System.err.println("DNS2");
		try {
			InetAddress addr = Address.getByAddress("128.138.135.12");
			System.err.println("Host Name : " + addr.getHostName());
			System.err.println("Canonical Host Name : " + addr.getCanonicalHostName());
			System.err.println("Host Address : " + addr.getHostAddress());
			System.err.println("Family : " + Address.familyOf(addr));
		} catch (Throwable t) {
			
		}
	}
	
	public void testDNS3() {
		System.err.println("DNS3");
		byte[] ip = {(byte)128,(byte)138,(byte)135,12};
		try {
			InetAddress addr = InetAddress.getByAddress(ip);
			System.err.println("Host Name : " + addr.getHostName());
			System.err.println("Canonical Host Name : " + addr.getCanonicalHostName());
			System.err.println("Host Address : " + addr.getHostAddress());
			System.err.println("Family : " + Address.familyOf(addr));
		} catch (Throwable t) {
			
		}
	}
}
