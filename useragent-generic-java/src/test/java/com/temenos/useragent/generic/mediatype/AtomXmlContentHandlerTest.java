package com.temenos.useragent.generic.mediatype;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import static com.temenos.useragent.generic.mediatype.AtomUtil.NS_ODATA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AtomXmlContentHandlerTest {

	private AtomXmlContentHandler xmlContentHandler;
	private Document document;

	@Before
	public void setUp() {
		InputStream stream = AtomEntryHandler.class
				.getResourceAsStream("/atom_entry_content_update.xml");
		try {
			document = AtomUtil.buildXmlDocument(IOUtils.toString(stream));
			xmlContentHandler = new AtomXmlContentHandler(document);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testSetAddingNewElementAtLevelZero() {
		xmlContentHandler.setValue("text-2", "text2");
		NodeList text2Nodes = document.getElementsByTagNameNS(NS_ODATA,
				"text-2");
		assertEquals(1, text2Nodes.getLength());
		Node text2Node = text2Nodes.item(0);
		assertEquals("text2", text2Node.getTextContent());
		assertEquals(0, text2Node.getAttributes().getLength());
	}

	@Test
	public void testSetingExistingElementAtLevelZero() {
		xmlContentHandler.setValue("text-1", "text1");
		NodeList text1Nodes = document.getElementsByTagNameNS(NS_ODATA,
				"text-1");
		assertEquals(1, text1Nodes.getLength());
		Node text1Node1 = text1Nodes.item(0);
		assertEquals("text1", text1Node1.getTextContent());
		assertEquals(0, text1Node1.getAttributes().getLength());
	}

	@Test
	public void testSetAddingElementUnderNewGroupAtLevelZero() {
		xmlContentHandler.setValue("foo-group(1)/foo-1", "foo1");
		assertEquals("foo1", xmlContentHandler.getValue("foo-group(1)/foo-1"));
	}

	@Test
	public void testSetAddingElementUnderExistingGroupAtLevelOne() {
		xmlContentHandler.setValue("foo-group(0)/foo-4", "foo4");
		assertEquals("foo4", xmlContentHandler.getValue("foo-group(0)/foo-4"));
	}

	@Test
	public void testSetAddingElementUnderNewGroungAtLevelTwo() {
		xmlContentHandler.setValue("foo-group(0)/bar-group(1)/bar-1", "bar1");
		assertEquals("bar1",
				xmlContentHandler.getValue("foo-group(0)/bar-group(1)/bar-1"));
	}

	@Test(expected = IllegalStateException.class)
	public void testSetAddingElementUnderWrongGroupAtLevelZero() {
		xmlContentHandler.setValue("whatever(0)/foo-1", "foo1");
	}

	@Test(expected = IllegalStateException.class)
	public void testSetAddingElementUnderWrongGroupAtLevelOne() {
		xmlContentHandler.setValue("foo-group(1)/whatever(0)/foo-1", "foo1");
	}

	@Test(expected = IllegalStateException.class)
	public void testSetAddingElementUnderWrongGroupAtLevelTwo() {
		xmlContentHandler.setValue(
				"foo-group(1)/bar-group(1)/whatever(0)/foo-1", "foo1");
	}

	@Test(expected = IllegalStateException.class)
	public void testSetAddingGroupElementWithGapsAtLevelZero() {
		xmlContentHandler.setValue("foo-group(7)/foo-1", "foo1");
	}

	@Test(expected = IllegalStateException.class)
	public void testSetAddingGroupElementWithGapsAtLevelOne() {
		xmlContentHandler.setValue("foo-group(0)/bar-group(7)/bar-1", "bar1");
	}

	@Test(expected = IllegalStateException.class)
	public void testSetAddingGroupElementWithGapsAtLevelTwo() {
		xmlContentHandler.setValue(
				"foo-group(0)/bar-group(0)/blah-group(7)/blah-1", "blah1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetAddingGroupElementWithNoIndexAtLevelZero() {
		xmlContentHandler.setValue("foo-group/foo-1", "foo1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetAddingGroupElementWithNoIndexAtLevelOne() {
		xmlContentHandler.setValue("foo-group(0)/bar-group/bar-1", "bar1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetAddingGroupElementWithNoIndexAtLevelTwo() {
		xmlContentHandler.setValue(
				"foo-group(0)/bar-group(0)/blah-group/blah-1", "blah1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSettingValueToGroupElementAtLevelZero() {
		xmlContentHandler.setValue("foo-group(0)", "foo1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSettingValueToGroupElementAtLevelOne() {
		xmlContentHandler.setValue("foo-group(0)/bar-group(0)", "bar1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSettingValueToGroupElementAtLevelTwo() {
		xmlContentHandler.setValue("foo-group(0)/bar-group(0)/blah-group(0)",
				"blah1");
	}

	@Test
	public void testRemoveNewElementAtLevelZero() {
		xmlContentHandler.setValue("text-2", "text2");
		xmlContentHandler.remove("text-2");
		assertNull(xmlContentHandler.getValue("text-2"));
	}

	@Test
	public void testRemoveingExistingElementAtLevelZero() {
		xmlContentHandler.setValue("text-1", "text1");
		xmlContentHandler.remove("text-1");
		assertNull(xmlContentHandler.getValue("text-1"));
	}

	@Test
	public void testRemoveElementUnderNewGroupAtLevelZero() {
		xmlContentHandler.setValue("foo-group(1)/foo-1", "foo1");
		xmlContentHandler.remove("foo-group(1)/foo-1");
		assertNull(xmlContentHandler.getValue("foo-group(1)/foo-1"));
	}

	@Test
	public void testRemoveElementUnderExistingGroupAtLevelZero() {
		xmlContentHandler.setValue("foo-group(0)/foo-4", "foo4");
		xmlContentHandler.setValue("foo-group(0)/foo-5", "foo5");
		xmlContentHandler.remove("foo-group(0)/foo-4");
		assertNull(xmlContentHandler.getValue("foo-group(0)/foo-4"));
		assertEquals("foo5", xmlContentHandler.getValue("foo-group(0)/foo-5"));
	}

	@Test
	public void testRemoveElementUnderExistingGroupAtLevelOne() {
		xmlContentHandler.setValue("foo-group(0)/bar-group(0)/bar-1", "bar1");
		xmlContentHandler.setValue("foo-group(0)/bar-group(1)/bar-1", "bar2");
		xmlContentHandler.remove("foo-group(0)/bar-group(0)/bar-1");
		assertNull(xmlContentHandler
				.getValue("foo-group(0)/bar-group(0)/bar-1"));
		assertEquals("bar2",
				xmlContentHandler.getValue("foo-group(0)/bar-group(1)/bar-1"));
	}

	@Test
	public void testRemoveElementUnderNewGroungAtLevelOne() {
		xmlContentHandler.setValue("foo-group(0)/bar-group(1)/bar-1", "bar1");
		xmlContentHandler.remove("foo-group(0)/bar-group(1)/bar-1");
		assertNull(xmlContentHandler
				.getValue("foo-group(0)/bar-group(1)/bar-1"));
	}

	@Test
	public void testRemoveLastGroupElementAtLevelTwo() {
		xmlContentHandler.setValue(
				"foo-group(0)/bar-group(1)/blah-group(0)/blah-1", "blah-1");
		xmlContentHandler.remove("foo-group(0)/bar-group(1)/blah-group(0)");
		assertNull(xmlContentHandler
				.getValue("foo-group(0)/bar-group(1)/blah-group(0)/blah-1"));
	}

	@Test
	public void testRemoveLastGroupElementAtLevelOne() {
		xmlContentHandler.setValue("foo-group(0)/bar-group(1)/bar-1", "bar1");
		assertEquals("bar1",
				xmlContentHandler.getValue("foo-group(0)/bar-group(1)/bar-1"));
		xmlContentHandler.remove("foo-group(0)/bar-group(1)");
		assertNull(xmlContentHandler
				.getValue("foo-group(0)/bar-group(1)/bar-1"));
	}

	@Test
	public void testRemoveLastGroupElementAtLevelZero() {
		xmlContentHandler.setValue("foo-group(0)/foo-1", "foo1");
		xmlContentHandler.remove("foo-group(0)");
		assertNull(xmlContentHandler.getValue("foo-group(0)/foo-1"));
	}

	@Test
	public void testRemoveMiddleGroupElementAtLevelTwo() {
		xmlContentHandler.setValue(
				"foo-group(0)/bar-group(1)/blah-group(1)/blah-1", "blah-1");
		assertEquals(
				"blah-1",
				xmlContentHandler
						.getValue("foo-group(0)/bar-group(1)/blah-group(1)/blah-1"));
		xmlContentHandler.remove("foo-group(0)/bar-group(1)/blah-group(0)");
		assertEquals(
				"blah-1",
				xmlContentHandler
						.getValue("foo-group(0)/bar-group(1)/blah-group(0)/blah-1"));
	}

	@Test
	public void testRemoveMiddleGroupElementAtLevelOne() {
		xmlContentHandler.setValue("foo-group(0)/bar-group(1)/bar-1", "bar1");
		xmlContentHandler.remove("foo-group(0)/bar-group(0)");
		assertEquals("bar1",
				xmlContentHandler.getValue("foo-group(0)/bar-group(0)/bar-1"));
	}

	@Test
	public void testRemoveMiddleGroupElementAtLevelZero() {
		xmlContentHandler.setValue("foo-group(1)/foo-1", "foo1");
		xmlContentHandler.remove("foo-group(0)");
		assertEquals("foo1", xmlContentHandler.getValue("foo-group(0)/foo-1"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForNullDocument() {
		xmlContentHandler = new AtomXmlContentHandler(null);
	}

}
