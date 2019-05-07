package com.interaction.example.hateoas.simple;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.JerseyTest;
import com.temenos.interaction.media.hal.MediaType;
import com.theoryinpractise.halbuilder.api.Link;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.standard.StandardRepresentationFactory;

/**
 * This test ensures that we can navigate from one application state to another
 * using hypermedia (links).
 * 
 * @author aphethean
 */
public class HypermediaITCase extends JerseyTest {

	public HypermediaITCase() throws Exception {
		super();
	}

	@Before
	public void initTest() {
		// -DTEST_ENDPOINT_URI={someurl} to test with external server
		webResource = Client.create().resource(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI));
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testGetEntryPointLinks() {
		ClientResponse response = webResource.path("/").accept(MediaType.APPLICATION_HAL_JSON)
				.get(ClientResponse.class);
		assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(response.getStatus())
				.getFamily());

		RepresentationFactory representationFactory = new StandardRepresentationFactory();
		ReadableRepresentation resource = representationFactory.readRepresentation(MediaType.APPLICATION_HAL_JSON.toString(),new InputStreamReader(response
				.getEntityInputStream()));

		List<Link> links = resource.getLinks();
		assertEquals(4, links.size());
		for (Link link : links) {
			if (link.getRel().equals("self")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/", link.getHref());
			} else if (link.getName().equals("HOME.home>GET>Preferences.preferences")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/preferences", link.getHref());
			} else if (link.getName().equals("HOME.home>GET>Profile.profile")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/profile", link.getHref());
			} else if (link.getName().equals("HOME.home>GET>Note.notes")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes", link.getHref());
			} else {
				fail("unexpected link [" + link.getName() + "]");
			}
		}
	}

	@Test
	public void testGetEntryPointEmbeddedPreferences() {
		ClientResponse response = webResource.path("/").accept(MediaType.APPLICATION_HAL_JSON)
				.get(ClientResponse.class);
		assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(response.getStatus())
				.getFamily());

		RepresentationFactory representationFactory = new StandardRepresentationFactory();
		ReadableRepresentation resource = representationFactory.readRepresentation(MediaType.APPLICATION_HAL_JSON.toString(),new InputStreamReader(response
				.getEntityInputStream()));

		// the embedded resources
		Map<String, Collection<ReadableRepresentation>> subresources = resource.getResourceMap();
		assertNotNull(subresources);
		assertTrue(subresources.size() == 1);

		Collection<ReadableRepresentation> preferences = subresources.get("http://relations.rimdsl.org/preferences");
		assertNotNull(preferences);
		assertTrue(preferences.size() == 1);

		ReadableRepresentation preference = preferences.iterator().next();
		assertEquals("user", preference.getValue("userID"));
		assertEquals("GBP", preference.getValue("currency"));
		assertEquals("en", preference.getValue("language"));
	}

	@Test
	public void testCollectionLinks() {
		ClientResponse response = webResource.path("/notes").accept(MediaType.APPLICATION_HAL_JSON)
				.get(ClientResponse.class);
		assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(response.getStatus())
				.getFamily());

		RepresentationFactory representationFactory = new StandardRepresentationFactory();
		ReadableRepresentation resource = representationFactory.readRepresentation(MediaType.APPLICATION_HAL_JSON.toString(),new InputStreamReader(response
				.getEntityInputStream()));

		// the links from the collection
		List<Link> links = resource.getLinks();
		assertEquals(2, links.size());
		for (Link link : links) {
			if (link.getRel().equals("self")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes", link.getHref());
			} else if (link.getName().equals("Note.notes>POST>Note.createNote")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes", link.getHref());
			} else {
				fail("unexpected link [" + link.getName() + "]");
			}
		}

		// the items, and links on each item
		Collection<Map.Entry<String, ReadableRepresentation>> subresources = resource.getResources();
		assertNotNull(subresources);
		/*
		 * Test that there are actually some subresource returned. If the 'self'
		 * link rel in the HALProvider is broken then we won't get any
		 * subresources here.
		 */
		assertTrue(subresources.size() > 0);
		boolean beveragesBody = false;
		boolean condimentsBody = false;
    	boolean confectionsBody = false;
    	boolean dairyProductsBody = false;
    	boolean grainsCerealsBody = false;
    	boolean meatPoultryBody = false;
    	boolean produceBody = false;
    	boolean seafoodBody = false;
    	boolean importantBody = false;
		
		for (Map.Entry<String, ReadableRepresentation> entry : subresources) {
			ReadableRepresentation item = entry.getValue();
			List<Link> itemLinks = item.getLinks();
			if (!item.getProperties().get("noteID").equals("9")) {
				assertEquals(2, itemLinks.size());
			}
			for (Link link : itemLinks) {
				if (link.getRel().contains("item")) {
					assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes/" + item.getProperties().get("noteID"),
							link.getHref());
				} else if (link.getName().contains("Note.deletedNote")) {
					assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes/" + item.getProperties().get("noteID"),
							link.getHref());
				} else {
					fail("unexpected link [" + link.getName() + "]");
				}
			}
			
			if(entryBodyMatches(entry.getValue(), "Beverages"))
			    beveragesBody = true;
			if(entryBodyMatches(entry.getValue(), "Condiments"))
			    condimentsBody = true;
			
			if(entryBodyMatches(entry.getValue(), "Confections"))
			    confectionsBody = true;
			
			if(entryBodyMatches(entry.getValue(), "Dairy Products"))
			    dairyProductsBody = true;
			
			if(entryBodyMatches(entry.getValue(), "Grains/Cereals"))
			    grainsCerealsBody = true;
			
			if(entryBodyMatches(entry.getValue(), "Meat/Poultry"))
			    meatPoultryBody =  true;
			
			if(entryBodyMatches(entry.getValue(), "Produce"))
			    produceBody = true;
			
			if(entryBodyMatches(entry.getValue(), "Seafood"))
			    seafoodBody = true;
			
			if(entryBodyMatches(entry.getValue(), "IMPORTANT"))
			    importantBody = true;
		}
				
        assertTrue(beveragesBody);
        assertTrue(condimentsBody);
        assertTrue(confectionsBody);
        assertTrue(dairyProductsBody);
        assertTrue(grainsCerealsBody);
        assertTrue(meatPoultryBody);
        assertTrue(produceBody);
        assertTrue(seafoodBody);
        assertTrue(importantBody);
	}
	
	private boolean entryBodyMatches(ReadableRepresentation representation, String valueToMatch) {
        return valueToMatch.equals(representation.getValue("body"));
	}

	@Test
	public void testConditionalCollectionLinks() {
		ClientResponse response = webResource.path("/notes").accept(MediaType.APPLICATION_HAL_JSON)
				.get(ClientResponse.class);
		assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(response.getStatus())
				.getFamily());

		RepresentationFactory representationFactory = new StandardRepresentationFactory();
		ReadableRepresentation resource = representationFactory.readRepresentation(MediaType.APPLICATION_HAL_JSON.toString(),new InputStreamReader(response
				.getEntityInputStream()));

		// the links from the collection
		List<Link> links = resource.getLinks();
		assertEquals(2, links.size());
		for (Link link : links) {
			if (link.getRel().equals("self")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes", link.getHref());
			} else if (link.getName().equals("Note.notes>POST>Note.createNote")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes", link.getHref());
			} else {
				fail("unexpected link [" + link.getName() + "]");
			}
		}

		// the items, and links on each item
		Collection<Map.Entry<String, ReadableRepresentation>> subresources = resource.getResources();
		assertNotNull(subresources);
		/*
		 * Test that there are actually some subresource returned. If the 'self'
		 * link rel in the HALProvider is broken then we won't get any
		 * subresources here.
		 */
		assertTrue(subresources.size() > 0);
		List<Link> testLinks = null;
		for (Map.Entry<String, ReadableRepresentation> entry : subresources) {
			ReadableRepresentation item = entry.getValue();
			List<Link> itemLinks = item.getLinks();
			if (item.getProperties().get("noteID").equals("9")) {
				// the links for out note number 9, i.e. the IMPORTANT links
				testLinks = itemLinks;
			} else {
				assertEquals(2, itemLinks.size());
			}
		}

		assertNotNull(testLinks);
		assertEquals(1, testLinks.size());
		for (Link link : testLinks) {
			if (link.getRel().contains("item")) {
				assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes/9", link.getHref());
// in the test we assert we cannot delete IMPORTANT notes
//			} else if (link.getName().contains("Note.deletedNote")) {
//				assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes/9", link.getHref());
			} else {
				fail("unexpected link [" + link.getName() + "]");
			}
		}
	
	}

	/**
	 * Found a small issue where a GET to a non-existent resource still
	 * generated the links and this resulted in a server side error (500)
	 */
	@Test
	public void testGET404() {
		ClientResponse response = webResource.path("/notes/666").accept(MediaType.APPLICATION_HAL_JSON)
				.get(ClientResponse.class);
		assertEquals(404, response.getStatus());
	}

	@Test
	public void testFollowDeleteItemLink() {
		ClientResponse response = webResource.path("/notes").accept(MediaType.APPLICATION_HAL_JSON)
				.get(ClientResponse.class);
		assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(response.getStatus())
				.getFamily());

		RepresentationFactory representationFactory = new StandardRepresentationFactory();
		ReadableRepresentation resource = representationFactory.readRepresentation(MediaType.APPLICATION_HAL_JSON.toString(),new InputStreamReader(response
				.getEntityInputStream()));

		// the items in the collection
		Collection<Map.Entry<String, ReadableRepresentation>> subresources = resource.getResources();
		assertNotNull(subresources);

		// follow the link to delete the first in the collection
		if (subresources.size() == 0) {
			// we might have run the integration tests more times than we have
			// rows in our table
		} else {
			ReadableRepresentation item = subresources.iterator().next().getValue();
			List<Link> itemLinks = item.getLinks();
			assertEquals(2, itemLinks.size());
			Link deleteLink = null;
			for (Link link : itemLinks) {
				if (link.getName() != null && link.getName().contains("Note.notes>DELETE>Note.deletedNote")) {
					deleteLink = link;
				}
			}
			assertNotNull(deleteLink);

			// create http client
			Client client = Client.create();
			// do not follow the Location redirect
			client.setFollowRedirects(false);
			// execute delete without custom link relation, will find the only
			// DELETE transition from entity
			ClientResponse deleteResponse = client.resource(deleteLink.getHref())
					.accept(MediaType.APPLICATION_HAL_JSON).delete(ClientResponse.class);
			// 303 "See Other" instructs user agent to fetch another resource as
			// specified by the 'Location' header
			assertEquals(303, deleteResponse.getStatus());
			assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes", deleteResponse.getHeaders().getFirst("Location"));
		}
	}

	@Test
	public void testFollowDeleteItemLinkRelation() {
		ClientResponse response = webResource.path("/notes").accept(MediaType.APPLICATION_HAL_JSON)
				.get(ClientResponse.class);
		assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(response.getStatus())
				.getFamily());

		RepresentationFactory representationFactory = new StandardRepresentationFactory();
		ReadableRepresentation resource = representationFactory.readRepresentation(MediaType.APPLICATION_HAL_JSON.toString(),new InputStreamReader(response
				.getEntityInputStream()));

		// the items in the collection
		Collection<Map.Entry<String, ReadableRepresentation>> subresources = resource.getResources();
		assertNotNull(subresources);

		// follow the link to delete the first in the collection
		if (subresources.size() == 0) {
			// we might have run the integration tests more times than we have
			// rows in our table
		} else {
			ReadableRepresentation item = subresources.iterator().next().getValue();
			List<Link> itemLinks = item.getLinks();
			assertEquals(2, itemLinks.size());
			Link deleteLink = null;
			for (Link link : itemLinks) {
				if (link.getName() != null && link.getName().contains("Note.notes>DELETE>Note.deletedNote")) {
					deleteLink = link;
				}
			}
			assertNotNull(deleteLink);

			// execute delete with custom link relation (see rfc5988)
			Client client = Client.create();
			client.setFollowRedirects(false);
			ClientResponse deleteResponse = client
					.resource(deleteLink.getHref())
					.header("Link", "<" + deleteLink.getHref() + ">; rel=\"" + deleteLink.getName() + "\"")
					.accept(MediaType.APPLICATION_HAL_JSON).delete(ClientResponse.class);
			// 205 "Reset Content" instructs user agent to reload the resource
			// that contained this link
			assertEquals(205, deleteResponse.getStatus());
		}
	}

	@Test
	public void testFollowDeleteLinkWithLinkRel() {
		ClientResponse response = webResource.path("/notes").accept(MediaType.APPLICATION_HAL_JSON)
				.get(ClientResponse.class);
		assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(response.getStatus())
				.getFamily());

		RepresentationFactory representationFactory = new StandardRepresentationFactory();
		ReadableRepresentation resource = representationFactory.readRepresentation(MediaType.APPLICATION_HAL_JSON.toString(),new InputStreamReader(response
				.getEntityInputStream()));

		// the items in the collection
		Collection<Map.Entry<String, ReadableRepresentation>> subresources = resource.getResources();
		assertNotNull(subresources);

		// follow the link to delete the first in the collection
		if (subresources.size() == 0) {
			// we might have run the integration tests more times than we have
			// rows in our table
		} else {
			ReadableRepresentation item = subresources.iterator().next().getValue();
			List<Link> itemLinks = item.getLinks();
			assertEquals(2, itemLinks.size());

			// GET item link (Note.notes->Note.note)
			Link getLink = null;
			for (Link link : itemLinks) {
				if (link.getRel().contains("item")) {
					getLink = link;
				}
			}
			// follow GET item link
			assertNotNull(getLink);
			ClientResponse getResponse = Client.create().resource(getLink.getHref())
					.accept(MediaType.APPLICATION_HAL_JSON).get(ClientResponse.class);
			assertEquals(Response.Status.Family.SUCCESSFUL, Response.Status.fromStatusCode(getResponse.getStatus())
					.getFamily());
			// the item
			ReadableRepresentation itemResource = representationFactory.readRepresentation(MediaType.APPLICATION_HAL_JSON.toString(),new InputStreamReader(
					getResponse.getEntityInputStream()));
			List<Link> links = itemResource.getLinks();
			assertNotNull(links);
			/*
			 * 2 links. One to 'self' One to 'item' which contains DELETE in
			 * href (this should be changed to 'edit' rel)
			 */
			assertEquals(2, links.size());

			// DELETE item link (Note.note>Note.deletedNote, Note.deletedNote is
			// an auto transition to Note.notes)
			Link deleteLink = null;
			for (Link link : links) {
				if (link.getName() != null && link.getName().contains("Note.note>DELETE>Note.deletedNote")) {
					deleteLink = link;
				}
			}
			assertNotNull(deleteLink);
			String uri = deleteLink.getHref();

			// create http client
			Client client = Client.create();
			// do not follow the Location redirect
			client.setFollowRedirects(false);
			// execute delete with custom link relation (see rfc5988)
			ClientResponse deleteResponse = client.resource(uri)
					.header("Link", "<" + uri + ">; rel=\"" + deleteLink.getName() + "\"")
					.accept(MediaType.APPLICATION_HAL_JSON).delete(ClientResponse.class);
			// 303 "See Other" instructs user agent to fetch another resource as
			// specified by the 'Location' header
			assertEquals(303, deleteResponse.getStatus());
			assertEquals(Configuration.TEST_ENDPOINT_URI + "/notes", deleteResponse.getHeaders().getFirst("Location"));
		}
	}

	/**
	 * Attempt a DELETE to the notes resource (a collection resource)
	 */
	@Test
	public void deletePersonMethodNotAllowed() throws Exception {
		// attempt to delete the Person root, rather than an individual
		ClientResponse response = webResource.path("/notes").delete(ClientResponse.class);
		assertEquals(405, response.getStatus());

		assertEquals(4, response.getAllow().size());
		assertTrue(response.getAllow().contains("GET"));
		assertTrue(response.getAllow().contains("POST"));
		assertTrue(response.getAllow().contains("OPTIONS"));
		assertTrue(response.getAllow().contains("HEAD"));
	}

	/**
	 * Attempt a PUT to the notes collection resource (method not allowed)
	 */
	@Test
	public void putNoteToCollection() throws Exception {
		String halRequest = "{}";
		// attempt to put to the notes collection, rather than an individual
		ClientResponse response = webResource.path("/notes").type(MediaType.APPLICATION_HAL_JSON)
				.put(ClientResponse.class, halRequest);
		assertEquals(405, response.getStatus());
	}

	/**
	 * Attempt a POST an invalid notes resource (a collection resource)
	 */
	@Test
	public void postNoteBadRequest() throws Exception {
		String halRequest = "{{";
		// attempt to put to the notes collection, rather than an individual
		ClientResponse response = webResource.path("/notes").type(MediaType.APPLICATION_HAL_JSON)
				.post(ClientResponse.class, halRequest);
		assertEquals(400, response.getStatus());
	}

	/**
	 * Attempt a PUT an invalid notes resource (a collection resource)
	 */
	@Test
	public void putNoteBadRequest() throws Exception {
		String halRequest = "{{";
		// attempt to put to the notes collection, rather than an individual
		ClientResponse response = webResource.path("/notes").type(MediaType.APPLICATION_HAL_JSON)
				.put(ClientResponse.class, halRequest);
		assertEquals(400, response.getStatus());
	}

    @Test
    public void testGetProfile() throws Exception {
        ClientResponse response = webResource.path("/profiles/test%2BUser+1%5C2%2F3%274").accept(MediaType.APPLICATION_HAL_JSON)
                .get(ClientResponse.class);
        assertEquals(200, response.getStatus());

        RepresentationFactory representationFactory = new StandardRepresentationFactory();
        ReadableRepresentation resource = representationFactory.readRepresentation(
                MediaType.APPLICATION_HAL_JSON.toString(), new InputStreamReader(response.getEntityInputStream()));

        Map<String, Object> properties = resource.getProperties();
        assertNotNull(resource.getLinkByRel("self"));
        assertThat(properties.get("userID"), equalTo("test+User 1\\2/3'4"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetProfiles() throws Exception {
        ClientResponse response = webResource.path("/profiles").accept(MediaType.APPLICATION_HAL_JSON)
                .get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        RepresentationFactory representationFactory = new StandardRepresentationFactory();
        ReadableRepresentation resource = representationFactory.readRepresentation(
                MediaType.APPLICATION_HAL_JSON.toString(), new InputStreamReader(response.getEntityInputStream()));

        assertNotNull(resource.getLinkByRel("self"));
        Iterator<Entry<String, ReadableRepresentation>> propsIter = resource.getResources().iterator();
        while (propsIter.hasNext()) {
            Entry<String, ReadableRepresentation> entry = propsIter.next();
            Map<String, Object> properties = entry.getValue().getProperties();
            Link link = entry.getValue().getLinkByRel("item");// Rel self??
            assertNotNull(link);
            String href = link.getHref();
            assertTrue(href.endsWith("test%2BUser+1%5C2%2F3%274") || href.endsWith("aphethean"));
            assertThat(properties.get("userID"), anyOf(equalTo("test+User 1\\2/3'4"), equalTo("aphethean")));
        }
    }

}
