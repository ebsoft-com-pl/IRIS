package com.interaction.example.odata.embedded;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.Parser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
/**
 * 
 * Integration test for LinkId
 * @author mjangid
 *
 */
public class LinkIdITCase {

	private String baseUri = null;
	private HttpClient client;
	private GetMethod method = null;
	private final static String AIRPORTS = "Airports";


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		baseUri = ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI);
		client = new HttpClient();
	}

	@Test
	public void testLinkId() {
		try {
			method = new GetMethod(baseUri + AIRPORTS);
			client.executeMethod(method);
			assertEquals(200, method.getStatusCode());

			if (method.getStatusCode() == HttpStatus.SC_OK) {
				// read as string for debugging
				String response = method.getResponseBodyAsString();
				Abdera abdera = new Abdera();
				Parser parser = abdera.getParser();
				Document<Feed> feedEntry = parser.parse(new StringReader(response));
				Feed feed = feedEntry.getRoot();
				List<Entry> entries= feed.getEntries();
				assertEquals(6, entries.size());
//				for(int i=0; i<entries.size(); i++) {
//					List<Link> link = entries.get(i).getLinks();
//					for(int j=0; j<link.size(); j++) {
//						System.out.println(link.get(j).getAttributeValue("id"));
//					}
//				}
				List<Link> link = entries.get(0).getLinks();
				assertNotNull(link);
				assertEquals("123456", link.get(0).getAttributeValue("id"));
				assertEquals("654321", link.get(1).getAttributeValue("id"));
				
				link = entries.get(1).getLinks();
				assertNotNull(link);
				assertEquals("123456", link.get(0).getAttributeValue("id"));
				assertEquals("654321", link.get(1).getAttributeValue("id"));
				
				link = entries.get(2).getLinks();
				assertNotNull(link);
				assertEquals("123456", link.get(0).getAttributeValue("id"));
				assertEquals("654321", link.get(1).getAttributeValue("id"));
				
				link = entries.get(3).getLinks();
				assertNotNull(link);
				assertEquals("123456", link.get(0).getAttributeValue("id"));
				assertEquals("654321", link.get(1).getAttributeValue("id"));
				
				link = entries.get(4).getLinks();
				assertNotNull(link);
				assertEquals("123456", link.get(0).getAttributeValue("id"));
				assertEquals("654321", link.get(1).getAttributeValue("id"));
				
				link = entries.get(5).getLinks();
				assertNotNull(link);
				assertEquals("123456", link.get(0).getAttributeValue("id"));
				assertEquals("654321", link.get(1).getAttributeValue("id"));
			}
		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			method.releaseConnection();
		}		
	}

	@Test
	public void testEntryCount() {
		try {
			method = new GetMethod(baseUri + AIRPORTS);
			client.executeMethod(method);
			assertEquals(200, method.getStatusCode());

			if (method.getStatusCode() == HttpStatus.SC_OK) {
				// read as string for debugging
				String response = method.getResponseBodyAsString();
				Abdera abdera = new Abdera();
				Parser parser = abdera.getParser();
				Document<Feed> feedEntry = parser.parse(new StringReader(response));
				Feed feed = feedEntry.getRoot();
				List<Entry> entries= feed.getEntries();
				assertEquals(6, entries.size());
			}
		} catch (IOException e) {
			fail(e.getMessage());
		} finally {
			method.releaseConnection();
		}
	}
}
