package org.nsidc.libre.metrics;

import java.net.URL;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.PropertyConfigurator;

public class Log4jInit extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void init() {
		URL log4jUrl = Log4jInit.class.getClassLoader().getResource("log4j.properties");
		PropertyConfigurator.configure(log4jUrl);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) {
	}
}
