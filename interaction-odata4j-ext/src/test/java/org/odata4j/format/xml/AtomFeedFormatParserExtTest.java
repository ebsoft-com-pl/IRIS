package org.odata4j.format.xml;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OProperty;
import org.odata4j.edm.EdmComplexType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmType;
import org.odata4j.format.Entry;
import org.odata4j.format.xml.AtomFeedFormatParser.AtomFeed;
import org.odata4j.internal.FeedCustomizationMapping;
import org.odata4j.stax2.XMLEventReader2;
import org.odata4j.stax2.XMLFactoryProvider2;

import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.Action;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;
import com.temenos.interaction.odataext.entity.MetadataOData4j;

/**
 * This is to verify the extension written for Odata4j Issue https://code.google.com/p/odata4j/issues/detail?id=193
 * The change has been contributed to the project.
 * 
 * @author sjunejo
 *
 */

public class AtomFeedFormatParserExtTest {

	// Common
	private static EdmDataServices edmMetadata;
	private static final String entitySetName = "FtCommissionTypes";
	private static final String entityKeyValue = "SCTRDEFAULT";
	private static OEntityKey key;

	@BeforeClass
	public static void setup() {
		edmMetadata = getMetadata();
		key = OEntityKey.create(entityKeyValue);
	}

	@AfterClass
	public static void tearDown() {
		edmMetadata = null;
		key = null;
	}

	@Test
	public void testParseNonServiceDocResource() throws Exception {
		ResourceState serviceRoot = new ResourceState("SD", "ServiceDocument", new ArrayList<Action>(), "/");
		ResourceStateMachine hypermediaEngine = new ResourceStateMachine(serviceRoot);
		
		Metadata metadata = new Metadata("hothouse-models");		
		MetadataOData4j metadataOData4j = new MetadataOData4j(metadata, hypermediaEngine);

		FeedCustomizationMapping fcMapping = new FeedCustomizationMapping();
		AtomFeedFormatParserExt parser = new AtomFeedFormatParserExt(metadataOData4j, "oentitys", OEntityKey.create(1),  fcMapping);
		
  		InputStream xml = getClass().getClassLoader().getResourceAsStream("data-oentity.xml");  		
		AtomFeed feed = parser.parse(new InputStreamReader(xml));
		xml.close();
				
		for(Entry entry :feed.getEntries()) {
			OEntity entity = entry.getEntity();
			
			assertNotNull(entity);
			assertEquals("Joe", entity.getProperty("FirstName").getValue());
			assertEquals("Blogs", entity.getProperty("LastName").getValue());
			
			OProperty address = entity.getProperty("Address");
			assertNotNull(address);
			
			EdmType addressType = address.getType();
			assertTrue(addressType instanceof EdmComplexType);
			assertTrue("hothouse-modelsModel.oentity_Address".equals(addressType.getFullyQualifiedTypeName()));		
			
			List<OProperty> addressInfo = (List<OProperty>)address.getValue();			
			assertNotNull(addressInfo);
			
			for(OProperty property: addressInfo) {
				if("Number".equals(property.getName())) {
					assertEquals(10L, property.getValue());
				}
				
				if("Road".equals(property.getName())) {
					assertEquals("Downing Street", property.getValue());
				}
				
				if("Town".equals(property.getName())) {
					assertEquals("London", property.getValue());
				}
				
				if("County".equals(property.getName())) {
					assertEquals("LONDON", property.getValue());
				}								
			}
		}

	}

	/**
	 * Basic Bag(...) type support
	 */
	@Test
	public void testParsePropertiesWithBag() {
		InputStream xml = getClass().getClassLoader().getResourceAsStream("issue193_entry_with_Bag.xml");
        FeedCustomizationMapping fcMapping = new FeedCustomizationMapping();
        AtomFeed feed = new AtomFeedFormatParserExt(edmMetadata, entitySetName, key,  fcMapping).parse(new InputStreamReader(xml));
        assertNotNull(feed);
		Entry entry = feed.entries.iterator().next();
		assertNotNull(entry);
		OEntity entity = entry.getEntity();
		assertNotNull(entity);
		assertNotNull(entity.getLinks());
		assertTrue(!entity.getLinks().isEmpty());

		// We can add more asserts here to verify each OProperty

	}

	/**
	 * This test will verify that we can parse Entry posted by Odata (.NET) Clients with Collection(...) 
	 */
	@Test
	public void testParsePropertiesWithCollection() {
		InputStream xml = getClass().getClassLoader().getResourceAsStream("issue193_entry_with_Collection.xml");		
		FeedCustomizationMapping fcMapping = new FeedCustomizationMapping();				
		AtomFeed feed = new AtomFeedFormatParserExt(edmMetadata, entitySetName, key, fcMapping).parse(new InputStreamReader(xml));
		assertNotNull(feed);
		Entry entry = feed.entries.iterator().next();
		assertNotNull(entry);
		OEntity entity = entry.getEntity();
		assertNotNull(entity);
	}
	
	// Load the metadata
	private static EdmDataServices getMetadata() {
		InputStream metadataStream = AtomFeedFormatParserExtTest.class.getClassLoader().getResourceAsStream("issue193_metadata.xml");
		XMLEventReader2 reader = XMLFactoryProvider2.getInstance().newXMLInputFactory2().createXMLEventReader(new InputStreamReader(metadataStream));
		return new EdmxFormatParser().parseMetadata(reader);
	}

}
